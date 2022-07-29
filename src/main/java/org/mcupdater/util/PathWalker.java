package org.mcupdater.util;

import com.google.gson.Gson;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonParser;
import org.apache.commons.codec.digest.DigestUtils;
import org.mcupdater.model.Module;
import org.mcupdater.model.*;
import org.tomlj.Toml;
import org.tomlj.TomlArray;
import org.tomlj.TomlParseResult;

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
	
	public static IPackElement handleOneFile(ServerDefinition server, File file, String downloadUrl) {
		final Path searchPath;
		if( file.getParent() == null ) {
			searchPath = new File(".").toPath();
		} else {
			searchPath = file.getParentFile().toPath();	
		}
		
		final PathWalker walker = new PathWalker(server,searchPath,(downloadUrl==null?"[PATH]":"[URL]"));
		try {
			if( downloadUrl == null ) {
				return walker.handleFile(file.toPath());
			} else {
				return walker.handleFile(file.toPath(), downloadUrl);
			}
		} catch (IOException e) {
			e.printStackTrace();
		}
		return null;
	}

	public IPackElement handleFile(Path file) throws IOException {
		Path relativePath = rootPath.relativize(file);
		String downloadURL = urlBase + "/" + relativePath.toString().replace("\\","/").replace(" ", "%20");
		return handleFile(file,downloadURL);
	}
	
	public IPackElement handleFile(Path file, String downloadURL) throws IOException {
		Path relativePath = rootPath.relativize(file);
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
		if (relativePath.toString().contains(".DS_Store")) { return null; }
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
					return null;
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
				case "structures":
				{
					String newPath = relativePath.toString();
					if (sep.equals("\\")) {
						newPath = newPath.replace("\\", "/");
					}
					ConfigFile newConfig = new ConfigFile(downloadURL, newPath, false, md5);
					if (newPath.contains("client")) {
						newConfig.setNoOverwrite(true);
					}
					//server.addConfig(newConfig);
					return newConfig;
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
			if (cleanPath.contains("OpenTerrainGenerator/")) {
				ConfigFile newConfig = new ConfigFile(downloadURL, cleanPath, false, md5);
				//server.addConfig(newConfig);
				return newConfig;
			}
            if (cleanPath.split("/")[1].matches("\\d+(\\.\\d+)*")) {
                modPath = cleanPath.replaceAll("^(optional|client|server)", "mods");
            }

		}
		try {
			name = name.substring(0,name.lastIndexOf("."));
		} catch (StringIndexOutOfBoundsException e) {
			MCUpdater.apiLogger.warning("[PathWalker] Unable to process filename without '.' Skipping:" + name);
			return null;
		}
		id = name.replace(" ", "");
		id = id.toLowerCase();
		id = id.replaceAll("\\d","");
		id = id.replaceAll("\\W\\W.+","");
		id = id.replaceAll("\\W","");
		id = id.replaceAll( "(client|server|universal)$", "");
		try {
			ZipFile zf = new ZipFile(file.toFile());
			MCUpdater.apiLogger.finer("[PathWalker] " + file.toString() + ": " + zf.size() + " entries in file.");
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
				if (zf.getEntry("META-INF/mods.toml") != null) {
					// Parse Forge mods.toml format
					String whichFile = "META-INF/mods.toml";
					BufferedReader reader = new BufferedReader(new InputStreamReader(zf.getInputStream(zf.getEntry(whichFile))));
					TomlParseResult parsed = Toml.parse(reader);
					MCUpdater.apiLogger.fine("[PathWalker] TOML:");
					parsed.dottedKeySet().stream().forEach(entry -> {
						MCUpdater.apiLogger.fine("[PathWalker] \t" + entry + " : " + parsed.get(entry).getClass().getCanonicalName());
						if (parsed.isArray(entry)) {
							MCUpdater.apiLogger.fine("[PathWalker] \t\t" + parsed.getArray(entry).get(0).getClass().getCanonicalName());
						}
					});
					name = parsed.getArray("mods").getTable(0).getString("displayName");
					id = parsed.getArray("mods").getTable(0).getString("modId");
					mapMeta.put("version", parsed.getArray("mods").getTable(0).getString("version"));
					mapMeta.put("authors", parsed.getArray("mods").getTable(0).getString("authors"));
					mapMeta.put("description", parsed.getArray("mods").getTable(0).getString("description"));
					if (parsed.contains("license")) {
						mapMeta.put("license", parsed.getString("license"));
					}
					if (parsed.contains("dependencies." + id)) {
						StringBuilder deps = new StringBuilder();
						TomlArray localDeps = parsed.getArray("dependencies." + id);
						for (int index=0; index < localDeps.size(); index++) {
							if (!localDeps.getTable(index).getString("modId").equals("forge") && !localDeps.getTable(index).getString("modId").equals("minecraft")) { // ignore forge and minecraft because they are not "normal" mods
								deps.append(localDeps.getTable(index).getString("modId")).append(" ");
							}
						}
						depends = deps.toString().trim();
					}
					reader.close();
				}
				if (zf.getEntry("fabric.mod.json") != null) {
					String whichFile = "fabric.mod.json";
					BufferedReader reader = new BufferedReader(new InputStreamReader(zf.getInputStream(zf.getEntry(whichFile))));
					FabricModInfo info;
					JsonParser parser = new JsonParser();
					JsonElement rootElement = parser.parse(reader);
					info = gson.fromJson(rootElement, FabricModInfo.class);
					if (!(info.modId.equals("examplemod") || info.modId.isEmpty())) {
						id = info.modId;
						name = info.name;
						String authors;
						authors = info.authors.toString();
						mapMeta.put("version", info.version);
						mapMeta.put("authors", authors.substring(1, authors.length() - 1));
						mapMeta.put("description", info.description);
						mapMeta.put("license", info.license);
					}
					if (name.isEmpty()) {
						name = id;
					}
					reader.close();
				}
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
			MCUpdater.apiLogger.severe("[PathWalker] Unable to process, not a zipfile? Skipping:" + name);
		    return null;
		} catch (Exception e) {
			e.printStackTrace();
		} finally {
			if (server.modExceptions.containsKey(id)) {
				id = server.modExceptions.get(id);
			}
			List<PrioritizedURL> urls = new ArrayList<>();
			urls.add(new PrioritizedURL(downloadURL,1));
			Module newMod = new Module(name,id,urls,null,size,depends,required,modType,order,false,false,true,md5,new ArrayList<ConfigFile>(),side.name(),null,mapMeta,"","",new ArrayList<Submodule>(),"");
			if (modType.equals(ModType.Extract)) {
				newMod.setInRoot(true);
			}
			if (newMod.getModType().equals(ModType.Litemod)) {
				newMod.setDepends("liteloader");
			}
			if (!modPath.isEmpty()) {
				newMod.setPath(modPath);
			}
			//server.addModule(newMod);
			return newMod;
		}
	}

	@Override
	public FileVisitResult visitFile(Path file, BasicFileAttributes attrs) throws IOException {
		IPackElement element = handleFile(file);
		if (element != null) {
			if (element instanceof ConfigFile) {
				server.addConfig((ConfigFile) element);
				return FileVisitResult.CONTINUE;
			}
			if (element instanceof Module) {
				server.addModule((Module) element);
			}
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
