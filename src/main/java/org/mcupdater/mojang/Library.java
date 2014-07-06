package org.mcupdater.mojang;

import org.apache.commons.lang3.text.StrSubstitutor;
import org.mcupdater.model.JSON;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@JSON
public class Library {

	private String name;
	private List<Rule> rules;
	private Map<OperatingSystem, String> natives;
	private Extract extract;
	private String url;
	
	public String getName(){ return name; }
	public List<Rule> getRules(){ return rules; }
	public Map<OperatingSystem, String> getNatives(){ return natives; }
	public Extract getExtract(){ return extract; }
	public String getUrl(){ return url; }
	
	public String getDownloadUrl() {
		if (this.url != null) {
			return this.url;
		} else {
			String baseUrl = "https://libraries.minecraft.net/";
			if (this.natives != null) {
				if (this.natives.containsKey(OperatingSystem.getCurrentPlatform())) {
					return baseUrl + getLibraryPath(natives.get(OperatingSystem.getCurrentPlatform()));
				} else {
					return null;
				}
			} else {
				return baseUrl + getLibraryPath(null);
			}
		}
	}

	public String getLibraryPath(String classifier) {
		String[] parts = this.name.split(":",3);
		return String.format("%s/%s/%s/%s-%s%s.jar", parts[0].replaceAll("\\.", "/"),parts[1],parts[2],parts[1],parts[2], (classifier == null ? "" : "-" + classifier)).replace("${arch}", System.getProperty("sun.arch.data.model"));
	}
	
	public boolean validForOS() {
		if (this.rules == null) { return true; }
		Rule.Action lastAction = Rule.Action.DISALLOW;
		
		for (Rule rule : this.rules) {
			Rule.Action action = rule.getAppliedAction();
			if (action != null) lastAction = action;
		}
		return lastAction == Rule.Action.ALLOW;
	}
	
	public String getFilename() {
		String result;
		String[] parts = this.name.split(":",3);
		if (this.natives != null) {
			if (this.natives.containsKey(OperatingSystem.getCurrentPlatform())) {
				result = String.format("%s-%s-%s.jar", parts[1], parts[2], natives.get(OperatingSystem.getCurrentPlatform()));
			} else {
				result = String.format("%s-%s.jar", parts[1], parts[2]);
			}
		} else {
			result = String.format("%s-%s.jar", parts[1], parts[2]);
		}
		return result.replace("${arch}",System.getProperty("sun.arch.data.model"));
	}
	
	public boolean hasNatives() {
		return this.natives != null;
	}
}
