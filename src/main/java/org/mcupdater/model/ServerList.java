package org.mcupdater.model;

import java.util.*;

public class ServerList extends Server {
    private Map<String,Module> modules = new HashMap<>();
    private List<Loader> loaders = new ArrayList<>();

	private State state = State.UNKNOWN;
	private boolean stylesheet;

	public enum State {
		UNKNOWN,
		READY, UPDATE, ERROR
	}

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
	
    public Map<String, Module> getModules() {
        return modules;
    }

    public void setModules(Map<String, Module> modules) {
        this.modules = modules;
    }

	public List<Loader> getLoaders() { return loaders; }

	public void setLoaders(List<Loader> loaders) { this.loaders = loaders; }

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
			for (Submodule sm : mod.getSubmodules()) {
				if (!sm.getMD5().isEmpty()) {
					digests.add(sm.getMD5());
				}
			}
		}
		return digests;
	}

	public State getState() { return state; }

	public void setState(State state) { this.state = state; }

	public boolean hasStylesheet() {
		return stylesheet;
	}

	public void setStylesheet(boolean stylesheet) {
		this.stylesheet = stylesheet;
	}

}
