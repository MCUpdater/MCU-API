package org.mcupdater.util;

import org.mcupdater.api.Version;
import org.mcupdater.model.Module;
import org.mcupdater.model.ServerList;
import org.mcupdater.util.PathWalker;
import org.mcupdater.util.ServerDefinition;
import org.mcupdater.util.ServerPackParser;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;

public class FastPack {
	public static ServerDefinition doFastPack(String sourcePack, String sourceId, String serverName, String serverId, String serverAddr, String mainClass, String newsURL, String iconURL, String revision, Boolean autoConnect, String MCVersion, Path searchPath, String baseURL, boolean debug) {
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
