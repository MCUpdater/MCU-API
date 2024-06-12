package org.mcupdater.model;

import org.apache.commons.lang3.StringUtils;
import org.mcupdater.api.Version;
import org.mcupdater.util.ServerPackParser;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

import java.util.*;

public abstract class Server implements Comparable<Server>, IPackElement{

	protected String name;
	String packUrl;
	String newsUrl;
	String iconUrl;
	String version;		// minecraft version
	String mcuVersion;	// minimum version of MCU required to use this pack
	String loaderVersion; // forge (or other loader) version, used when launching the pack
	String address;
	boolean generateList = true;
	boolean autoConnect = true;
	boolean fakeServer = false;
	String revision;	// serverpack revision
	String serverId;
	String mainClass;
	String serverClass;
	Map<String,String> libOverrides = new HashMap<>();
	String rawOverrides = "";
	String launcherType = "Legacy";

	public static void fromElement(String mcuVersion, String serverUrl, Element docEle, Server newSL) {
		newSL.setMCUVersion(mcuVersion);
		newSL.setPackUrl(serverUrl);
		newSL.setServerId(docEle.getAttribute("id"));
		newSL.setName(docEle.getAttribute("name"));
		newSL.setNewsUrl(docEle.getAttribute("newsUrl"));
		newSL.setIconUrl(docEle.getAttribute("iconUrl"));
		final String mcVersion = docEle.getAttribute("version");
		newSL.setVersion(mcVersion);
		newSL.setAddress(docEle.getAttribute("serverAddress"));
		newSL.setGenerateList(ServerPackParser.parseBoolean(docEle.getAttribute("generateList"), true));
		newSL.setAutoConnect(ServerPackParser.parseBoolean(docEle.getAttribute("autoConnect"), true));
		newSL.setRevision(docEle.getAttribute("revision"));
		newSL.setFakeServer(ServerPackParser.parseBoolean(docEle.getAttribute("abstract"), false));
		newSL.setMainClass(docEle.getAttribute("mainClass"));
		newSL.setServerClass(docEle.getAttribute("serverClass"));
		if (docEle.hasAttribute("launcherType")) {
			final String launcherType = docEle.getAttribute("launcherType");
			newSL.setLauncherType(launcherType);
			// determine if we have a <Launcher/> child to parse a version number out of...
			// NB: do this the hard way for now - is only going to work for forge atm
			final NodeList nodes = docEle.getChildNodes();
			for (int i = 0; i < nodes.getLength(); i++) {
				final Node node = nodes.item(i);
				if (node.getNodeName().equals("Loader") && node.getAttributes().getNamedItem("type").getNodeValue().equals(launcherType)) {
					final String loaderVersion = node.getAttributes().getNamedItem("version").getNodeValue();
					if (loaderVersion.contains("-")) {
						int idx = loaderVersion.lastIndexOf("-");
						newSL.loaderVersion = mcVersion + "-" + launcherType.toLowerCase() + loaderVersion.substring(idx);
					}
				}
			}
		} else {
			if (Version.requestedFeatureLevel(mcuVersion,"3")) {
				newSL.setLauncherType("Vanilla");
			}
		}
		newSL.setRawOverrides(docEle.getAttribute("libOverrides"));
	}

	private static Map<String,String> mapOverrides(String rawOverrides) {
		Map<String,String> mapTemp = new HashMap<>();
		String[] overrides = rawOverrides.split(" ");
		for (String entry : overrides) {
			String key = StringUtils.join(Arrays.copyOfRange(entry.split(":"), 0, 2), ":");
			mapTemp.put(key, entry);
		}
		return mapTemp;
	}

	public String getName()
	{
		return name;
	}

	public void setName(String name)
	{
		this.name = name;
	}

	public String getPackUrl()
	{
		return packUrl;
	}

	public void setPackUrl(String url)
	{
		this.packUrl = url;
	}

	public String getNewsUrl() {
		return newsUrl;
	}

	public void setNewsUrl(String newsUrl) {
		this.newsUrl = newsUrl;
	}

	public String getVersion() {
		return version;
	}

	public String getLoaderVersion() {
		return loaderVersion;
	}

	public void setVersion(String version) {
		this.version = version;
	}

	public String getAddress() {
		return address;
	}

	public void setAddress(String address) {
		this.address = address;
	}

	public boolean isGenerateList() {
		return generateList;
	}

	public void setGenerateList(boolean generateList) {
		this.generateList = generateList;
	}

	public String getIconUrl() {
		return iconUrl;
	}

	public void setIconUrl(String iconUrl) {
		this.iconUrl = iconUrl;
	}

	public String getRevision() {
		return revision;
	}

	public void setRevision(String revision) {
		this.revision = revision;
	}

	public String getServerId() {
		return serverId;
	}

	public void setServerId(String serverId) {
		this.serverId = serverId;
	}

	public String getMCUVersion() {
		return mcuVersion;
	}

	public void setMCUVersion(String mcuVersion) {
		this.mcuVersion = mcuVersion;
	}

	public boolean isAutoConnect() {
		return this.generateList && autoConnect;
	}

	public void setAutoConnect(boolean autoConnect) {
		this.autoConnect = autoConnect;
	}

	public boolean isFakeServer() {
		return fakeServer;
	}

	public void setFakeServer(boolean fakeServer) {
		this.fakeServer = fakeServer;
	}

	public String getMainClass() {
		return mainClass;
	}

	public void setMainClass(String mainClass) {
		this.mainClass = mainClass;
	}

	public String getLauncherType() {
		return launcherType;
	}

	public void setLauncherType(String launcherType) {
		this.launcherType = launcherType;
	}

	public Map<String, String> getLibOverrides() {
		return libOverrides;
	}

	private void setLibOverrides(Map<String, String> libOverrides) {
		this.libOverrides = libOverrides;
	}

	public String getServerClass() {
		if (serverClass.isEmpty()) {
			return "net.minecraft.server.MinecraftServer";
		} else {
			return serverClass;
		}
	}

	public String getServerClass_Raw() {
		return (serverClass != null ? serverClass : "");
	}

	public void setServerClass(String serverClass) {
		this.serverClass = serverClass;
	}

	public String getRawOverrides() {
		return rawOverrides;
	}

	public void setRawOverrides(String rawOverrides) {
		this.rawOverrides = rawOverrides;
		if (this.rawOverrides.length() > 0) {
			setLibOverrides(mapOverrides(this.rawOverrides));
		}

	}

	@Override
	public int compareTo(Server that) {
		return this.getName().compareTo(that.getName());
	}

	@Override
	public String getFriendlyName() {
		return (this.isFakeServer() ? "*" : " ") + "[" + this.getVersion() + "] " + this.name;
	}

	@Override
	public String toString() {
		return getFriendlyName();
	}

}
