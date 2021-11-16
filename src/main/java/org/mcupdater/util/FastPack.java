package org.mcupdater.util;

import org.mcupdater.api.Version;
import org.mcupdater.model.Module;
import org.mcupdater.model.PrioritizedURL;
import org.mcupdater.model.ServerList;
import org.mcupdater.model.metadata.Downloadable;
import org.mcupdater.model.metadata.ProjectData;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.function.BiConsumer;

public class FastPack {
	public static ServerDefinition doFastPack(String sourcePack, String sourceId, String serverName, String serverId, String serverAddr, String mainClass, String newsURL, String iconURL, String revision, Boolean autoConnect, String MCVersion, Path searchPath, String baseURL, boolean debug, List<ProjectData> projects) {
		ServerDefinition definition = new ServerDefinition();
		ServerList entry;
		if (sourcePack.isEmpty()) {
			entry = new ServerList();
		} else {
			System.out.println(sourcePack + ": " + sourceId);
			entry = ServerPackParser.loadFromURL(sourcePack, sourceId);
			for (Module existing : entry.getModules().values()) {
				definition.addModule(existing);
			}
		}
		entry.setName(serverName);
		entry.setServerId(serverId);
		entry.setAddress(serverAddr);
		entry.setMainClass(mainClass);
		entry.setNewsUrl(newsURL);
		entry.setIconUrl(iconURL);
		entry.setRevision(revision);
		entry.setAutoConnect(autoConnect);
		entry.setVersion(MCVersion);
		if (Version.requestedFeatureLevel(MCVersion,"1.6")) {
			entry.setLauncherType("Vanilla");
		} else {
			entry.setLauncherType("Legacy");
		}
		definition.setServerEntry(entry);

		PathWalker pathWalk = new PathWalker(definition, searchPath, baseURL);
		try {
			Files.walkFileTree(searchPath, pathWalk);
		} catch (IOException e) {
			e.printStackTrace();
		}

		if (projects != null) {
			System.out.println("Projects array: " + projects.size() + " entries");
			Map<String, Downloadable> downloadables = new HashMap<>();
			projects.stream().forEach( project -> {
				project.getDownloadables().stream().forEach( downloadable -> {
					downloadables.put(downloadable.getMd5hash(), downloadable);
				});
			});
			System.out.println("Downloadables: " + downloadables.size() + " entries");
			definition.getModules().entrySet().stream().forEach(moduleEntry -> {
				if (downloadables.containsKey(moduleEntry.getValue().getMD5())){
					System.out.println("Matched " + moduleEntry.getValue().getMD5() + " - " + downloadables.get(moduleEntry.getValue().getMD5()));
					Module replacement = moduleEntry.getValue();
					replacement.addUrl(new PrioritizedURL(downloadables.get(replacement.getMD5()).getUrls().toArray()[0].toString(),0));
					definition.getModules().put(replacement.getId(), replacement);
				} else {
					System.out.println("No match for " + moduleEntry.getValue().getMD5());
				}
			});
		} else {
			System.out.println("No projects array");
		}

		if (debug) {
			for (Module modEntry : definition.getModules().values()) {
				System.out.println(modEntry.toString());
			}
		}
		return definition;
	}
	
	public static ServerDefinition doImport(String importURL, String serverName, String serverId, String serverAddr, String mainClass, String newsURL, String iconURL, Boolean autoConnect, boolean debug) {
		ServerDefinition definition = new ServerDefinition();
		ServerList entry = new ServerList();

		entry.setName(serverName);
		entry.setServerId(serverId);
		entry.setAddress(serverAddr);
		entry.setMainClass(mainClass);
		entry.setNewsUrl(newsURL);
		entry.setIconUrl(iconURL);
		entry.setAutoConnect(autoConnect);
		if(importURL.contains("curseforge.com") && !importURL.endsWith("/file")) {
			importURL = importURL.concat(importURL.endsWith("/") ? "" : "/" + "file");
		}
		
		CurseImporter importer = new CurseImporter(importURL);
		importer.run(definition, entry);
		definition.setServerEntry(entry);

		if (debug) {
			for (Module modEntry : definition.getModules().values()) {
				System.out.println(modEntry.toString());
			}
		}

		return definition;
	}
}
