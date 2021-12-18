package org.mcupdater.mojang;

import com.google.common.base.MoreObjects;
import com.google.common.collect.Maps;
import com.google.gson.*;
import org.mcupdater.model.JSON;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.URL;
import java.net.URLConnection;
import java.util.List;
import java.util.Map;

/*
 * Implementation of version.json format used by Minecraft's launcher
 */
@JSON
public class MinecraftVersion {
	private String inheritsFrom;
	private String id;
	private String time;
	private String releaseTime;
	private String type;
	private String minecraftArguments;
	private int minimumLauncherVersion;
	private List<Library> libraries;
	private String mainClass;
	private String incompatibilityReason;
	private String assets;
	private List<Rule> compatibilityRules;
	private Map<DownloadType, DownloadInfo> downloads = Maps.newEnumMap(DownloadType.class);
	private AssetIndexInfo assetIndex;
	private Arguments arguments;
	
	public String getId(){ return id; }
	public String getTime(){ return time; }
	public String getReleaseTime(){ return releaseTime; }
	public String getType(){ return type; }
	public String getMinecraftArguments(){ return minecraftArguments; }
	public Arguments getArguments(){ return arguments; }
	public int getMinimumLauncherVersion(){ return minimumLauncherVersion; }
	public List<Library> getLibraries(){ return libraries; }
	public String getMainClass(){ return mainClass; }
	public String getIncompatibilityReason(){ return incompatibilityReason; }
	public String getAssets() { return this.assets; }

	public List<Rule> getRules() {
		return compatibilityRules;
	}

	public DownloadInfo getDownloadInfo(DownloadType type) {
		if (this.downloads.containsKey(type)) {
			return this.downloads.get(type);
		} else {
			return null;
		}
	}

	public AssetIndexInfo getAssetIndex() {
		if (this.assetIndex == null) {
			this.assetIndex = new AssetIndexInfo(MoreObjects.firstNonNull(this.assets, "legacy"));
		}
		return this.assetIndex;
	}
	
	public static MinecraftVersion loadVersion(String version) {
		GsonBuilder builder = new GsonBuilder();
		builder.registerTypeAdapterFactory(new LowerCaseEnumTypeAdapterFactory());
		builder.enableComplexMapKeySerialization();
		Gson gson = builder.create();
		
		URLConnection conn;
		try {
			conn = (new URL(VersionManifest.getCurrent(false).getVersion(version).getUrl())).openConnection();
			return gson.fromJson(new InputStreamReader(conn.getInputStream()),MinecraftVersion.class);
		} catch (IOException e) {
			e.printStackTrace();
		} catch (VersionManifest.VersionNotFoundException e) {
			e.printStackTrace();
		}
		return null;
	}

	public static MinecraftVersion loadLocalVersion(File instanceLocation, String version) {
		GsonBuilder builder = new GsonBuilder();
		builder.registerTypeAdapterFactory(new LowerCaseEnumTypeAdapterFactory());
		builder.enableComplexMapKeySerialization();
		Gson gson = builder.create();

		try {
			File versionFile = new File(instanceLocation, "versions/" + version + "/" + version + ".json");
			return gson.fromJson(new InputStreamReader(new FileInputStream(versionFile)),MinecraftVersion.class);
		} catch (IOException e) {
			e.printStackTrace();
		}
		return null;
	}

	public String getJVMArguments() {
		if (this.arguments != null) {
			StringBuilder argBuilder = new StringBuilder();
			for (JsonElement entry : this.arguments.getJvm()) {
				if (entry.isJsonPrimitive()) {
					argBuilder.append(entry.getAsString()).append(" ");
				}
			}
			return argBuilder.toString().trim();
		}
		return "";
	}

	public String getEffectiveArguments() {
		if (this.minecraftArguments != null) {
			return this.minecraftArguments;
		} else {
			if (this.arguments != null) {
				StringBuilder argBuilder = new StringBuilder();
				for (JsonElement entry : this.arguments.getGame()) {
					if (entry.isJsonPrimitive()) {
						argBuilder.append(entry.getAsString()).append(" ");
					}
				}
				return argBuilder.toString().trim();
			}
		}
		return "";
	}
	
}
