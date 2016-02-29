package org.mcupdater.mojang;

import java.net.URL;

public class DownloadInfo {
	protected URL url;
	protected String sha1;
	protected int size;

	public URL getUrl() {
		return this.url;
	}

	public String getSha1() {
		return this.sha1;
	}

	public int getSize() {
		return this.size;
	}
}
