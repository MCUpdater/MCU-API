package org.mcupdater.model;

import java.util.ArrayList;
import java.util.List;

public class RawServer extends Server {

	private List<IPackElement> packElements = new ArrayList<>();

	public List<IPackElement> getPackElements() {
		return packElements;
	}

	public void setPackElements(List<IPackElement> packElements) {
		this.packElements = packElements;
	}
}
