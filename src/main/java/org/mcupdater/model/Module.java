package org.mcupdater.model;

import org.mcupdater.downloadlib.Downloadable;
import org.mcupdater.util.CurseModCache;
import org.mcupdater.util.MCUpdater;
import org.mcupdater.util.PathWalker;
import org.mcupdater.util.ServerDefinition;

import java.io.File;
import java.io.IOException;
import java.net.URL;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.*;


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

	public static Module parseFile(CurseProject curseProject, List<PrioritizedURL> urls) throws Exception {
		File tmp = null;
		Path path;
		List<PrioritizedURL> downloadUrls = new ArrayList<>();
		Integer curseFile = null;
		if (curseProject != null){
			downloadUrls.add(new PrioritizedURL(CurseModCache.fetchURL(curseProject), 0));
			curseFile = curseProject.getFile();
		}
		if (urls.size() > 0) downloadUrls.addAll(urls);
		if (downloadUrls.size() == 0) {
			throw new Exception("No URLs to download from");
		}
		URL finalUrl = null;
		for (PrioritizedURL url : downloadUrls) {
			try {
				tmp = File.createTempFile("import", ".jar");
				finalUrl = new URL(url.getUrl());
				System.out.println("Temp file: " + tmp.getAbsolutePath());
				Downloadable downloadable = new Downloadable("import.jar", tmp.getAbsolutePath(), "force", 0, new ArrayList<>(Collections.singleton(finalUrl)));
				downloadable.download(tmp.getParentFile().getParentFile(), MCUpdater.getInstance().getArchiveFolder().resolve("cache").toFile());
				tmp.deleteOnExit();
				path = tmp.toPath();
				if (Files.size(path) == 0) {
					System.out.println("!! got zero bytes from " + url);
				}
			} catch(IOException e){
				System.out.println("!! Unable to download " + url);
				e.printStackTrace();
			}
		}
		final ServerDefinition definition = new ServerDefinition();
		final String fname = finalUrl.toString();
		Module parsed = (Module) PathWalker.handleOneFile(definition, tmp, fname);
		parsed.setCurseProject(curseProject);
		return parsed;
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

