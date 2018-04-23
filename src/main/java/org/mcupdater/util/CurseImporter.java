package org.mcupdater.util;

import java.io.File;
import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;
import java.nio.file.Files;
import java.nio.file.Path;

import org.apache.commons.io.FileUtils;
import org.mcupdater.api.Version;
import org.mcupdater.model.CurseProject;
import org.mcupdater.model.Module;
import org.mcupdater.model.ServerList;
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
				FileUtils.copyURLToFile(url, tmp);
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
						CurseProject proj = new CurseProject(modData.getProjectID().toString(), mcVersion);
						proj.setFile(modData.getFileID());
						mod.setCurseProject(proj);
						mod.setRequired(modData.getRequired());
						
						// TODO: get actual human readables from curse here instead
						mod.setId("_"+modData.getProjectID());
						mod.setName(mod.getId());
						
						definition.addModule(mod);
					}
					
					// TODO: add overrides as special case unzip
					
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
