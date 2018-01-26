package org.mcupdater.model;

import org.mcupdater.api.Version;
import org.mcupdater.util.ServerPackParser;

import java.util.ArrayList;
import java.util.List;

public class RawServer extends Server {

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

	private List<IPackElement> packElements = new ArrayList<>();


	public List<IPackElement> getPackElements() {
		return packElements;
	}

	public void setPackElements(List<IPackElement> packElements) {
		this.packElements = packElements;
	}

}
