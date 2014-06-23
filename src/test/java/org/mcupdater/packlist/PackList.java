package org.mcupdater.packlist;

import org.mcupdater.model.GenericModule;
import org.mcupdater.model.Module;
import org.mcupdater.util.MCUpdater;
import org.mcupdater.util.ServerPackParser;

import java.util.List;

public class PackList {

	public static void main(String[] args) {
		MCUpdater.getInstance();
		List<Module> mods;
		mods = ServerPackParser.loadFromURL(args[0],args[1]);
		for (Module x : mods) {
			System.out.println(x.getName() + "," + x.getId() + "," + x.getUrls().get(0));
			if (x.hasSubmodules()) {
				for (GenericModule sub : x.getSubmodules()) {
					System.out.println(sub.getName() + "," + sub.getId() + "," + sub.getUrls().get(0));
				}
			}
		}
	}
}
