package org.mcupdater.model;

import java.util.Comparator;

public class OrderComparator implements Comparator<Loader> {

	@Override
	public int compare(Loader o1, Loader o2) {
		return Integer.compare(o1.getLoadOrder(), o2.getLoadOrder());
	}
}
