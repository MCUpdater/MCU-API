package org.mcupdater.model;

import org.apache.commons.lang3.StringUtils;
import org.mcupdater.api.Version;
import org.mcupdater.util.ServerPackParser;
import org.w3c.dom.Element;

import java.util.*;

public class ServerList implements Comparable<ServerList>{
	private String name;
	private String packUrl;
	private String newsUrl;
	private String iconUrl;
	private String version;		// minecraft version
	private String mcuVersion;	// minimum version of MCU required to use this pack
	private String address;
	private boolean generateList = true;
	private boolean autoConnect = true;
	private boolean fakeServer = false;
	private String revision;	// serverpack revision
	private String serverId;
	private String mainClass;
    private Map<String,String> libOverrides = new HashMap<>();
    private Map<String,Module> modules = new HashMap<>();
	private String launcherType = "Legacy";

	public ServerList() {}

	@Deprecated
	public ServerList(String serverId, String name, String packUrl, String newsUrl, String iconUrl, String version, String address, boolean generateList, boolean autoConnect, String revision, boolean fakeServer, String mainClass)
	{
		this.serverId = serverId;
		this.name = name;
		this.packUrl = packUrl;
		this.newsUrl = newsUrl;
		this.iconUrl = iconUrl;
		this.version = version;
		this.address = address;
		this.generateList = generateList;
		this.setAutoConnect(autoConnect);
		this.revision = revision;
		this.setFakeServer(fakeServer);
		this.setMainClass(mainClass);
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
	
	public String toString() {
		return this.name;
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

	@Override
	public int compareTo(ServerList that) {
		return this.getName().compareTo(that.getName());
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

    public void setLibOverrides(Map<String, String> libOverrides) {
        this.libOverrides = libOverrides;
    }

    public Map<String, Module> getModules() {
        return modules;
    }

    public void setModules(Map<String, Module> modules) {
        this.modules = modules;
    }

	public Set<String> getDigests() {
		Set<String> digests = new HashSet<>();
		List<Module> mods = new ArrayList<>(this.getModules().values());
		for (Module mod : mods) {
			if (!mod.getMD5().isEmpty()) {
				digests.add(mod.getMD5());
			}
			for (ConfigFile cf : mod.getConfigs()) {
				if (!cf.getMD5().isEmpty()) {
					digests.add(cf.getMD5());
				}
			}
			for (GenericModule sm : mod.getSubmodules()) {
				if (!sm.getMD5().isEmpty()) {
					digests.add(sm.getMD5());
				}
			}
		}
		return digests;
	}

    public static ServerList fromElement(String mcuVersion, String serverUrl, Element docEle) {
        ServerList newSL = new ServerList();
        newSL.setMCUVersion(mcuVersion);
        newSL.setPackUrl(serverUrl);
        newSL.setServerId(docEle.getAttribute("id"));
        newSL.setName(docEle.getAttribute("name"));
        newSL.setNewsUrl(docEle.getAttribute("newsUrl"));
        newSL.setIconUrl(docEle.getAttribute("iconUrl"));
        newSL.setVersion(docEle.getAttribute("version"));
        newSL.setAddress(docEle.getAttribute("serverAddress"));
        newSL.setGenerateList(ServerPackParser.parseBoolean(docEle.getAttribute("generateList"), true));
        newSL.setAutoConnect(ServerPackParser.parseBoolean(docEle.getAttribute("autoConnect"), true));
        newSL.setRevision(docEle.getAttribute("revision"));
        newSL.setFakeServer(ServerPackParser.parseBoolean(docEle.getAttribute("abstract"), false));
        newSL.setMainClass(docEle.getAttribute("mainClass"));
        if (docEle.hasAttribute("launcherType")) {
            newSL.setLauncherType(docEle.getAttribute("launcherType"));
        } else {
            if (Version.requestedFeatureLevel(mcuVersion,"3")) {
                newSL.setLauncherType("Vanilla");
            }
        }
        Map<String,String> mapOverrides = new HashMap<>();
        if (docEle.getAttribute("libOverrides").length() > 0) {
            String[] overrides = docEle.getAttribute("libOverrides").split(" ");
            for (String entry : overrides) {
                String key = StringUtils.join(Arrays.copyOfRange(entry.split(":"), 0, 2), ":");
                mapOverrides.put(key, entry);
            }
            newSL.setLibOverrides(mapOverrides);
        }
        return newSL;
    }
}
