package org.mcupdater.model;

import org.apache.commons.lang3.StringUtils;
import org.mcupdater.loaders.ForgeLoader;
import org.mcupdater.loaders.ILoader;
import org.mcupdater.loaders.NeoForgeLoader;

import java.util.List;

public class Loader implements IPackElement, Comparable<Loader> {
	private static List<String> validTypes = List.of("Forge","NeoForge");
	private String type = "";
	private String version = "";
	private int loadOrder;

	public Loader(String type, String version, int loadOrder) {
		this.type = type;
		this.version = version;
		this.loadOrder = loadOrder;
	}

	public Loader() {
	}

	public static boolean isValidType(String type) {
		for(String valid: validTypes){
			if (valid.equals(type)) return true;
		}
		return false;
	}

	public static List<String> getValidTypes(){ return validTypes; }

	public String getType() { return type; }

	public void setType(String type) { this.type = type; }

	public String getVersion() { return version; }

	public void setVersion(String version) { this.version = version; }

	@Override
	public String getFriendlyName() { return "Loader: " + this.type + "(" + this.version + ")";	}

	@Override
	public String toString() { return getFriendlyName(); }

	public int getLoadOrder() {
		return loadOrder;
	}

	public void setLoadOrder(int loadOrder) {
		this.loadOrder = loadOrder;
	}

	@Override
	public int compareTo(Loader o) {
		return Integer.valueOf(loadOrder).compareTo(o.getLoadOrder());
	}

	public static class InvalidTypeException extends Exception {

		public InvalidTypeException(String type) {
			super("An invalid type of [" + type + "] was specified for a Loader element.  Valid types are: " + StringUtils.join(validTypes, ", "));
		}
	}

	public ILoader getILoader(){
		switch (type){
			case "Forge":
				return new ForgeLoader(this);
			case "NeoForge":
				return new NeoForgeLoader(this);
			default:
				return null;
		}
	}
}
