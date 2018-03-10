package org.mcupdater.model;

import org.mcupdater.api.Version;
import org.mcupdater.util.ServerPackParser;

import java.util.ArrayList;
import java.util.List;

public class RawServer extends Server {

	private List<IPackElement> packElements = new ArrayList<>();

	public RawServer() {
		setMCUVersion(mcuVersion);
		setPackUrl("");
		setServerId("newserver");
		setName("New Server");
		setNewsUrl("about:blank");
		setIconUrl("");
		setVersion("1.12.2");
		setAddress("");
		setGenerateList(true);
		setAutoConnect(true);
		setRevision("");
		setFakeServer(false);
		setMainClass("net.minecraft.launchwrapper.Launch");
		setLauncherType("Vanilla");
		setRawOverrides("");
		setServerClass("");
	}

	public RawServer(Server source) {
		setMCUVersion(source.mcuVersion);
		setPackUrl(source.packUrl);
		setServerId(source.serverId);
		setName(source.name);
		setNewsUrl(source.newsUrl);
		setIconUrl(source.iconUrl);
		setVersion(source.version);
		setAddress(source.address);
		setGenerateList(source.generateList);
		setAutoConnect(source.autoConnect);
		setRevision(source.revision);
		setFakeServer(source.fakeServer);
		setMainClass(source.mainClass);
		setLauncherType(source.launcherType);
		setRawOverrides(source.rawOverrides);
		setServerClass(source.serverClass);
	}

	public List<IPackElement> getPackElements() {
		return packElements;
	}

	public void setPackElements(List<IPackElement> packElements) {
		this.packElements = packElements;
	}

}
