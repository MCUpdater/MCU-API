package org.mcupdater.util;

import com.google.gson.Gson;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonParser;
import org.apache.commons.codec.digest.DigestUtils;
import org.mcupdater.model.*;

import java.io.*;
import java.nio.file.FileVisitResult;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.SimpleFileVisitor;
import java.nio.file.attribute.BasicFileAttributes;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.zip.ZipException;
import java.util.zip.ZipFile;

public class PathWalker extends SimpleFileVisitor<Path> {
	private ServerDefinition server;
	private Path rootPath;
	private String urlBase;
	private final String sep = File.separator;
	private static int jarOrder;
	private Gson gson = new Gson();

	public PathWalker(ServerDefinition server, Path rootPath, String urlBase) {
		this.setServer(server);
		this.setRootPath(rootPath);
		this.setUrlBase(urlBase);
	}

	@Override
	public FileVisitResult visitFile(Path file, BasicFileAttributes attrs) throws IOException {
		Path relativePath = rootPath.relativize(file);
		String downloadURL = urlBase + "/" + relativePath.toString().replace("\\","/").replace(" ", "%20");
		long size = Files.size(file);
		InputStream is = Files.newInputStream(file);
		String md5 = DigestUtils.md5Hex(is);
		String name = file.getFileName().toString();
		String id;
		String modPath = "";
		int order = -1;
		String depends = "";
		Boolean required = true;
		ModType modType = ModType.Regular;
        ModSide side = ModSide.BOTH;
		HashMap<String,String> mapMeta = new HashMap<>();
		//System.out.println(relativePath.toString());
		if (relativePath.toString().contains(".DS_Store")) { return FileVisitResult.CONTINUE; }
		if (relativePath.toString().contains(sep)) {
			switch (relativePath.toString().substring(0, relativePath.toString().indexOf(sep))) {
				case "asm":
				case "bin":
				case "saves":
				case "screenshots":
				case "stats":
				case "texturepacks":
				case "texturepacks-mp-cache":
				case "assets":
				case "resourcepacks":
				case "lib":
				case "libraries":
				case "versions":
					return FileVisitResult.CONTINUE;
				//
				case "instMods":
				case "jar":
				{
					modType = ModType.Jar;
					order = ++jarOrder;
					break;
				}
				case "coremods":
				{
					modType = ModType.Coremod;
					break;
				}
				case "extract": {
					modType = ModType.Extract;
					break;
				}
				case "config":
                case "scripts":
				case "resources":
				{
					String newPath = relativePath.toString();
					if (sep.equals("\\")) {
						newPath = newPath.replace("\\", "/");
					}
					ConfigFile newConfig = new ConfigFile(downloadURL, newPath, false, md5);
					if (newPath.contains("client")) {
						newConfig.setNoOverwrite(true);
					}
					server.addConfig(newConfig);
					return FileVisitResult.CONTINUE;
				}
				case "optional":
					required = false;
                    break;
                case "client":
                    required = false;
                    side = ModSide.CLIENT;
                    break;
                case "server":
                    side = ModSide.SERVER;
                    break;
			}
            if (relativePath.toString().endsWith("litemod")) {
	            ServerDefinition.hasLitemods = true;
	            modType = ModType.Litemod;
            }
            String cleanPath = relativePath.toString().replace("\\","/");
            if (cleanPath.split("/")[1].matches("\\d+(\\.\\d+)*")) {
                modPath = cleanPath.replaceAll("^(optional|client|server)", "mods");
            }

		}
		try {
			name = name.substring(0,name.lastIndexOf("."));
		} catch (StringIndexOutOfBoundsException e) {
			System.out.println("Unable to process filename without '.' Skipping:" + name);
			return FileVisitResult.CONTINUE;
		}
		id = name.replace(" ", "");
		id = id.replaceAll("\\d","").replaceAll("[^a-zA-Z]*$","");
		try {
			ZipFile zf = new ZipFile(file.toFile());
			System.out.println(file.toString() + ": " + zf.size() + " entries in file.");
			if (modType.equals(ModType.Litemod)) {
				if (zf.getEntry("litemod.json") != null) {
					BufferedReader reader = new BufferedReader(new InputStreamReader(zf.getInputStream(zf.getEntry("litemod.json"))));
					LitemodInfo info;
					JsonParser parser = new JsonParser();
					JsonElement rootElement = parser.parse(reader);
					info = gson.fromJson(rootElement, LitemodInfo.class);
					name = info.name;
					id = info.name.replace(" ","");
					mapMeta.put("version", info.version);
					mapMeta.put("authors", info.author);
					mapMeta.put("description", info.description);
					mapMeta.put("url", info.url);
					mapMeta.put("revision", info.revision);
					reader.close();
				}
			} else {
				if (zf.getEntry("mcmod.info") != null || zf.getEntry("neimod.info") != null || zf.getEntry("cccmod.info") != null) {
					String whichFile = "mcmod.info";
					if (zf.getEntry("neimod.info") != null) {
						whichFile = "neimod.info";
					} else if (zf.getEntry("cccmod.info") != null) {
						whichFile = "cccmod.info";
					}
					BufferedReader reader = new BufferedReader(new InputStreamReader(zf.getInputStream(zf.getEntry(whichFile))));
					MCModInfo info;
					JsonParser parser = new JsonParser();
					JsonElement rootElement = parser.parse(reader);
					if (rootElement.isJsonArray()) {
						JsonArray jsonList = rootElement.getAsJsonArray();
						info = gson.fromJson(jsonList.get(0), MCModInfo.class);
					} else {
						if (rootElement.getAsJsonObject().has("modlist")) {
							info = gson.fromJson(rootElement.getAsJsonObject().getAsJsonArray("modlist").get(0), MCModInfo.class);
						} else if (rootElement.getAsJsonObject().has("modList")) {
							info = gson.fromJson(rootElement.getAsJsonObject().getAsJsonArray("modList").get(0), MCModInfo.class);
						} else {
							info = gson.fromJson(rootElement, MCModInfo.class);
						}
					}
					if (!(info.modId.equals("examplemod") || info.modId.isEmpty())) {
						id = info.modId;
						name = info.name;
						String authors;
						if (info.authors.size() > 0) {
							authors = info.authors.toString();
						} else {
							authors = info.authorList.toString();
						}
						mapMeta.put("version", info.version);
						mapMeta.put("authors", authors.substring(1, authors.length() - 1));
						mapMeta.put("description", info.description);
						mapMeta.put("credits", info.credits);
						mapMeta.put("url", info.url);
					}
					if (id.startsWith("mod_")) {
						id = id.substring(4);
					}
					if (name.isEmpty()) {
						name = id;
					}
					reader.close();
				}
			}
			zf.close();
		} catch (ZipException e) {
		    System.out.println("Unable to process, not a zipfile? Skipping:" + name);
		    return FileVisitResult.CONTINUE;
		} catch (Exception e) {
			e.printStackTrace();
		} finally {
			if (server.modExceptions.containsKey(id)) {
				id = server.modExceptions.get(id);
			}
			List<PrioritizedURL> urls = new ArrayList<>();
			urls.add(new PrioritizedURL(downloadURL,0));
			Module newMod = new Module(name,id,urls,null,depends,required,modType,order,false,false,true,md5,new ArrayList<ConfigFile>(),side.name(),null,mapMeta,"","",new ArrayList<Submodule>(),"");
			if (modType.equals(ModType.Extract)) {
				newMod.setInRoot(true);
			}
			newMod.setFilesize(size);
			if (newMod.getModType().equals(ModType.Litemod)) {
				newMod.setDepends("liteloader");
			}
			if (!modPath.isEmpty()) {
				newMod.setPath(modPath);
			}
			server.addModule(newMod);
		}
		return FileVisitResult.CONTINUE;
	}

	public void setRootPath(Path rootPath) {
		this.rootPath = rootPath;
	}

	public void setUrlBase(String urlBase) {
		this.urlBase = urlBase;
	}

	public void setServer(ServerDefinition server) {
		this.server = server;
	}
}