package org.mcupdater.model;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;


public class Module extends GenericModule {
	private List<ConfigFile> configs = new ArrayList<>();
	private List<Submodule> submodules = new ArrayList<>();
	
	public Module(String name, String id, List<PrioritizedURL> url, CurseProject curse, String depends, boolean required, ModType modType, int jarOrder, boolean keepMeta, boolean inRoot, boolean isDefault, String md5, List<ConfigFile> configs, String side, String path, HashMap<String, String> meta, String launchArgs, String jreArgs, List<Submodule> submodules, String parent) {
		super(name, id, url, curse, depends, required, modType, jarOrder, keepMeta, inRoot, isDefault, md5, side, path, meta, launchArgs, jreArgs, parent);
		if(configs != null) {
			this.configs = configs;
		} else {
			this.configs = new ArrayList<>();
		}
		if(submodules != null) {
			this.submodules = submodules;
		} else {
			this.submodules = new ArrayList<>();
		}
	}

	public Module(String name, String id, List<PrioritizedURL> url, CurseProject curse, String depends, boolean required, boolean inJar, int jarOrder, boolean keepMeta, boolean extract, boolean inRoot, boolean isDefault, boolean coreMod, String md5, List<ConfigFile> configs, String side, String path, HashMap<String, String> meta, String launchArgs, String jreArgs, String parent){
		super(name,id,url,curse,depends,required,inJar,jarOrder,keepMeta,extract,inRoot,isDefault,coreMod,md5,side,path,meta,launchArgs,jreArgs,parent);
		if(configs != null)
		{
			this.configs = configs;
		} else {
			this.configs = new ArrayList<>();
		}
		this.submodules = new ArrayList<>();
	}

	public List<ConfigFile> getConfigs()
	{
		return configs;
	}
	
	public void setConfigs(List<ConfigFile> configs)
	{
		this.configs = configs;
	}
	
	public boolean hasConfigs() {
		return (this.configs.size() > 0);
	}
	
	public boolean hasSubmodules() {
		return (this.submodules.size() > 0);
	}
	
	public List<Submodule> getSubmodules() {
		return this.submodules;
	}

	@Override
	public String getFriendlyName() {
		return "Module: " + getName();
	}

	public static Module createBlankModule() {
		return new Module("New Mod","newmod",new ArrayList<PrioritizedURL>(),null,"",false,ModType.Regular, 0, false, false, false, "", new ArrayList<ConfigFile>(), "BOTH", "", null, "", "", new ArrayList<Submodule>(), null);
	}
}

