package org.mcupdater.model;

import java.util.Comparator;


public class ModuleComparator implements Comparator<Module> {

	@Override
	public int compare(Module o1, Module o2) {
		Integer o1weight = (o1.getModType().equals(ModType.Jar) ? 0 : (o1.getModType().equals(ModType.Library) ? 1 : (o1.getModType().equals(ModType.Coremod) ? 2 : 3)));
		Integer o2weight = (o2.getModType().equals(ModType.Jar) ? 0 : (o2.getModType().equals(ModType.Library) ? 1 : (o2.getModType().equals(ModType.Coremod) ? 2 : 3)));
		if (o1weight.equals(o2weight) && !o1.getModType().equals(ModType.Jar) && !o2.getModType().equals(ModType.Jar) && !o1.getModType().equals(ModType.Library) && !o2.getModType().equals(ModType.Library)) {
			return o1.getName().compareToIgnoreCase(o2.getName());
		} else if ((o1.getModType().equals(ModType.Jar) && o2.getModType().equals(ModType.Jar)) || (o1.getModType().equals(ModType.Library) && o2.getModType().equals(ModType.Library))) {
			return Integer.valueOf(o1.getJarOrder()).compareTo(o2.getJarOrder());
		}
		else {
			return o1weight.compareTo(o2weight);
		}
	}

}
