package org.mcupdater.model;

import java.util.ArrayList;
import java.util.List;

public class ServerPack {
	private String xsltPath;
	private String version;
	private List<RawServer> servers;

	public ServerPack(String xsltPath, String version) {
		this.xsltPath = xsltPath;
		this.version = version;
		this.servers = new ArrayList<>();
	}

	public String getXsltPath() {
		return xsltPath;
	}

	public void setXsltPath(String xsltPath) {
		this.xsltPath = xsltPath;
	}

	public String getVersion() {
		return version;
	}

	public void setVersion(String version) {
		this.version = version;
	}

	public List<RawServer> getServers() {
		return servers;
	}

	public void setServers(List<RawServer> servers) {
		this.servers = servers;
	}

	@Override
	public String toString() {
		return "ServerPack";
	}
}
