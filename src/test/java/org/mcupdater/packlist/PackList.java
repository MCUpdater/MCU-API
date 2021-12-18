package org.mcupdater.packlist;

import org.mcupdater.model.GenericModule;
import org.mcupdater.model.Module;
import org.mcupdater.model.ModuleComparator;
import org.mcupdater.util.MCUpdater;
import org.mcupdater.util.ServerPackParser;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class PackList {

	public static void main(String[] args) {
		MCUpdater.getInstance();
		List<Module> mods;
		mods = new ArrayList<>(ServerPackParser.loadFromURL(args[0],args[1]).getModules().values());
		Collections.sort(mods, new ModuleComparator(ModuleComparator.Mode.HIERARCHY));
		for (Module x : mods) {
			System.out.println(x.getParent() + "\t" + x.getName() + "\t" + x.getId() + "\t" + x.getUrls().get(0) + "\t" + x.getMeta().get("version"));
			if (x.hasSubmodules()) {
				for (GenericModule sub : x.getSubmodules()) {
					System.out.println(sub.getParent() + "\t" + sub.getName() + "\t" + sub.getId() + "\t" + sub.getUrls().get(0) + "\t" + sub.getMeta().get("version"));
				}
			}
		}
	}
}
