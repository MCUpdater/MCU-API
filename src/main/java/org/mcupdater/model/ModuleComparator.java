package org.mcupdater.model;

import java.util.Comparator;


public class ModuleComparator implements Comparator<GenericModule> {

	private Mode mode;

	public ModuleComparator(Mode mode) {
		this.mode = mode;
	}

	@Override
	public int compare(GenericModule o1, GenericModule o2) {
        if (mode.equals(Mode.HIERARCHY)) {
            if (!o1.getParent().equals(o2.getParent())) {
                return o1.getParent().compareToIgnoreCase(o2.getParent());
            }
        }
		if (mode.equals(Mode.OPTIONAL_FIRST)) {
			if (!o1.getRequired() && !o2.getRequired()) {
				return o1.getName().compareToIgnoreCase(o2.getName());
			}
			if (!o1.getRequired()) {
				return -1;
			}
			if (!o2.getRequired()) {
				return 1;
			}
		}
		Integer o1weight = (o1.getModType().equals(ModType.Jar) ? 0 : (o1.getModType().equals(ModType.Library) ? 1 : (o1.getModType().equals(ModType.Coremod) ? 2 : 3)));
		Integer o2weight = (o2.getModType().equals(ModType.Jar) ? 0 : (o2.getModType().equals(ModType.Library) ? 1 : (o2.getModType().equals(ModType.Coremod) ? 2 : 3)));
		if (o1weight.equals(o2weight) && !o1.getModType().equals(ModType.Jar) && !o2.getModType().equals(ModType.Jar) && !o1.getModType().equals(ModType.Library) && !o2.getModType().equals(ModType.Library)) {
			return o1.getName().compareToIgnoreCase(o2.getName());
		} else if ((o1.getModType().equals(ModType.Jar) && o2.getModType().equals(ModType.Jar)) || (o1.getModType().equals(ModType.Library) && o2.getModType().equals(ModType.Library))) {
			return Integer.valueOf(o1.getJarOrder()).compareTo(o2.getJarOrder());
		} else {
			return o1weight.compareTo(o2weight);
		}

	}

	public enum Mode
	{
		IMPORTANCE,
        HIERARCHY,
        OPTIONAL_FIRST
	}
}
