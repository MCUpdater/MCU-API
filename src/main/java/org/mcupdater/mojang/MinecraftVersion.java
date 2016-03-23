package org.mcupdater.mojang;

import com.google.common.base.MoreObjects;
import com.google.common.collect.Maps;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import org.mcupdater.model.JSON;

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
	
	public String getId(){ return id; }
	public String getTime(){ return time; }
	public String getReleaseTime(){ return releaseTime; }
	public String getType(){ return type; }
	public String getMinecraftArguments(){ return minecraftArguments; }
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
			conn = (new URL("https://s3.amazonaws.com/Minecraft.Download/versions/" + version + "/" + version + ".json")).openConnection();
			return gson.fromJson(new InputStreamReader(conn.getInputStream()),MinecraftVersion.class);
		} catch (IOException e) {
			e.printStackTrace();
		}
		return null;
	}
	
}
