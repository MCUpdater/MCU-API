package org.mcupdater.model;

public class Import implements IPackElement
{
	private String url = "";
	private String serverId;

	public Import(String url, String serverId) {
		this.url = url;
		this.serverId = serverId;
	}

	public String getUrl() {
		return url;
	}

	public void setUrl(String url) {
		this.url = url;
	}

	public String getServerId() {
		return serverId;
	}

	public void setServerId(String serverId) {
		this.serverId = serverId;
	}

	@Override
	public String getFriendlyName() {
		return "Import: " + serverId + "@" + url;
	}
}
