package org.mcupdater.util;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.net.MalformedURLException;
import java.net.URL;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;

import org.apache.commons.codec.digest.DigestUtils;
import org.apache.commons.io.FileUtils;
import org.mcupdater.api.Version;
import org.mcupdater.downloadlib.DownloadUtil;
import org.mcupdater.model.*;
import org.mcupdater.model.curse.manifest.Manifest;
import org.mcupdater.model.curse.manifest.Minecraft;
import org.mcupdater.model.curse.manifest.ModLoader;

import com.google.gson.Gson;

public class CurseImporter {
	private File tmp;
	
	public CurseImporter(String importURL) {
		tmp = new File(importURL);
		if( !tmp.exists() ) {
			boolean downloaded = false;
			System.out.println( "[import] no such file, attempting download" );
			try {
				final URL url = new URL(importURL);
				tmp = File.createTempFile("import", "zip");			
				DownloadUtil.get(url, tmp);
				System.out.println( "[import] downloaded "+Files.size(tmp.toPath())+" bytes...");
				downloaded = true;
			} catch (MalformedURLException e) {
				e.printStackTrace();
			} catch (IOException e) {
				e.printStackTrace();
			}
			if( !downloaded ) {
				System.out.println("[import] unable to download pack from '"+importURL+"'");
				tmp = null;
			} else {
				// TODO: clean up after ourselves better
				tmp.deleteOnExit();
			}
		} else {
			System.out.println( "[import] found pack locally, parsing" );
		}
	}

	public void run(ServerDefinition definition, ServerList entry) {
		if( tmp != null ) {
			Path dir = null;
			File json = null;
			System.out.println( "[import] running import process..." );
			if( Archive.isArchive(tmp) ) {
				System.out.println( "[import] looks like a zipfile, extracting" );
				try {
					dir = Files.createTempDirectory("import");
					Archive.extractZip(tmp, dir.toFile());

					json = dir.resolve("manifest.json").toFile();
					json = new File(dir+File.separator+"manifest.json");
					json.deleteOnExit();
				} catch (IOException e) {
					e.printStackTrace();
				}
			} else {
				System.out.println( "[import] doesn't look like a zip, treating as json" );
				json = tmp;
			}
			
			if( json != null && json.canRead() ) {
				System.out.println( "[import] parsing manifest" );
				try {
					String data = FileUtils.readFileToString(json);
					MCUpdater.apiLogger.finest(data + "\n");
					Gson gson = new Gson();
					Manifest manifest = gson.fromJson(data, Manifest.class);
					
					// get minecraft version
					Minecraft mc = manifest.getMinecraft();
					final String mcVersion = mc.getVersion();
					entry.setVersion(mcVersion);
					System.out.println("[import] MC: "+mcVersion);
					
					if (Version.requestedFeatureLevel(mcVersion,"1.6")) {
						entry.setLauncherType("Vanilla");
					} else {
						entry.setLauncherType("Legacy");
					}

					// parse modloaders (ie, get forge version)
					for( ModLoader ml : mc.getModLoaders() ) {
						final String mlId = ml.getId();
						if( mlId.startsWith("forge") ) {
							final String forgeVersion = mlId.substring(6);
							definition.addForge(mcVersion, forgeVersion);
							System.out.println("[import] Forge: "+forgeVersion);
						} else {
							// TODO: support other modloaders...
						}
					}
					
					// get pack name
					final String name = manifest.getName();
					entry.setName(name);
					
					// get pack revision
					final String rev = manifest.getVersion();
					entry.setRevision(rev);
					System.out.println("[import] Pack: "+name+" v"+rev);
					
					// get mods
					for( org.mcupdater.model.curse.manifest.File modData : manifest.getFiles() ) {
						Module mod = Module.createBlankModule();
						System.out.println("[import] Project ID: " + modData.getProjectID());
						final String projId = CurseModCache.getTextID(modData.getProjectID());
						CurseProject proj = new CurseProject(projId, mcVersion);
						proj.setFile(modData.getFileID());
						mod.setCurseProject(proj);
						mod.setRequired(modData.getRequired());
						
						mod.setId(projId);
						mod.setName(projId);
						
						definition.addModule(mod);
					}

					// TODO: add overrides as special case unzip
					File overrides = dir.resolve("overrides").toFile();
					File outputOverrides = MCUpdater.getInstance().getArchiveFolder().resolve("FastPack").resolve(entry.getName() + "-overrides.zip").toFile();
					Archive.createZip(outputOverrides, new ArrayList<>(FileUtils.listFiles(overrides, null, true)), overrides.toPath(), null);
					System.out.println("[import] overrides recreated as separate zip at " + outputOverrides.getAbsolutePath());
					long size = Files.size(outputOverrides.toPath());
					InputStream is = Files.newInputStream(outputOverrides.toPath());
					String md5 = DigestUtils.md5Hex(is);

					Module modOverrides = Module.createBlankModule();
					modOverrides.setName("Overrides");
					modOverrides.setId("overrides");
					modOverrides.addUrl(new PrioritizedURL(outputOverrides.toURI().toURL().toString(),0));
					modOverrides.setRequired(true);
					modOverrides.setMD5(md5);
					modOverrides.setFilesize(size);
					modOverrides.setModType(ModType.Extract);
					modOverrides.setInRoot(true);
					modOverrides.setSide(ModSide.BOTH);

					definition.addModule(modOverrides);
					
				} catch (IOException e) {
					e.printStackTrace();
				}
			} else {
				System.out.println("[import] unable to find/read manifest" );
			}
			
			/*
			 * TODO: recursively delete the temp dir
			 * 
			try {
				Files.deleteIfExists(dir);
			} catch (IOException e) {
				e.printStackTrace();
			}
			 */
		} else {
			System.out.println( "[import] no pack found, nothing to do." );
		}
	}

}
