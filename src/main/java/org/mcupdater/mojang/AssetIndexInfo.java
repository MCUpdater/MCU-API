package org.mcupdater.mojang;

import org.mcupdater.util.MCUpdater;

import java.net.MalformedURLException;
import java.net.URL;
import java.util.logging.Level;

public class AssetIndexInfo extends DownloadInfo {
	protected long totalSize;
	protected String id;
	protected boolean known = true;

	public AssetIndexInfo() {
	}

	public AssetIndexInfo(String id) {
		this.id = id;
		try {
			// This is a legacy URL.  The new URL should be returned in the version definition.
			this.url = new URL("https://s3.amazonaws.com/Minecraft.Download/indexes/" + id + ".json");
		} catch (MalformedURLException e) {
			MCUpdater.apiLogger.log(Level.SEVERE, "Error getting asset index!", e);
		}
		this.known = false;
	}

	public long getTotalSize() {
		return this.totalSize;
	}

	public String getId() {
		return this.id;
	}

	public boolean isKnown() {
		return this.known;
	}

}
