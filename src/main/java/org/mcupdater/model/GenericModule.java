package org.mcupdater.model;

import org.mcupdater.util.MCUpdater;

import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;

public class GenericModule {
	protected String parent = "";
	protected String name = "";
	protected String id = "";
	protected List<PrioritizedURL> urls = new ArrayList<>();
	protected String path = "";
	protected String depends = "";
	protected boolean required = false;
	//protected boolean inJar = false;
	protected int order = 1;
	protected boolean keepMeta = false;
	//protected boolean extract = false;
	protected boolean inRoot = false;
	protected boolean isDefault = false;
	//protected boolean coreMod = false;
	//protected boolean litemod = false;
	protected String md5 = "";
	protected ModSide side = ModSide.BOTH;
	protected HashMap<String,String> meta = new HashMap<>();
	//protected boolean isLibrary = false;
	protected String launchArgs = "";
	protected String jreArgs = "";
	protected ModType modType = ModType.Regular;
	protected String loadPrefix = "";
	protected long filesize = 100000;

	public GenericModule(String name, String id, List<PrioritizedURL> url, String depends, boolean required, ModType type, int jarOrder, boolean keepMeta, boolean inRoot, boolean isDefault, String md5, String side, String path, HashMap<String, String> meta, String launchArgs, String jreArgs, String parent) {
		this.setName(name);
		this.setId(id);
		this.setUrls(url);
		this.setDepends(depends);
		this.setRequired(required);
		this.setModType(type);
		this.setJarOrder(jarOrder + 1);
		this.setKeepMeta(keepMeta);
		this.setIsDefault(isDefault);
		this.setInRoot(inRoot);
		this.setMD5(md5);
		this.setSide(side);
		this.setPath(path);
		this.setLaunchArgs(launchArgs);
		this.setJreArgs(jreArgs);
		this.parent = parent;
		if(meta != null)
		{
			this.setMeta(meta);
		} else {
			this.setMeta(new HashMap<String,String>());
		}
	}

	public GenericModule(String name, String id, List<PrioritizedURL> url, String depends, boolean required, ModType type, int jarOrder, boolean keepMeta, boolean inRoot, boolean isDefault, String md5, String side, String path, HashMap<String, String> meta, String launchArgs, String jreArgs) {
		this(name, id, url, depends, required, type, jarOrder, keepMeta, inRoot, isDefault, md5, side, path, meta, launchArgs, jreArgs, "unspecified");
	}

	public GenericModule(String name, String id, List<PrioritizedURL> url, String depends, boolean required, boolean inJar, int jarOrder, boolean keepMeta, boolean extract, boolean inRoot, boolean isDefault, boolean coreMod, String md5, String side, String path, HashMap<String, String> meta, String launchArgs, String jreArgs, String parent){
		this(name, id, url, depends, required, ModType.Regular, jarOrder, keepMeta, inRoot, isDefault, md5, side, path, meta, launchArgs, jreArgs, parent);
		if (inJar) {
			this.setModType(ModType.Jar);
		} else if (extract) {
			this.setModType(ModType.Extract);
		} else if (coreMod) {
			this.setModType(ModType.Coremod);
		}
	}
	
	private void setJarOrder(int jarOrder) {
		this.order = jarOrder;
	}

	public String getName()
	{
		return name;
	}
	
	public void setName(String name)
	{
		this.name=name;
	}
	
	public List<URL> getUrls()
	{
		List<URL> result = new ArrayList<>();
		for (PrioritizedURL entry : urls) {
			try {
				result.add(new URL(entry.getUrl()));
			} catch (MalformedURLException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
		return result;
	}
	
	public void setUrls(List<PrioritizedURL> urls)
	{
		this.urls = urls;
	}
	
	public void addUrl(PrioritizedURL url)
	{
		this.urls.add(url);
	}
	
	public boolean getRequired()
	{
		return required;
	}
	
	public void setRequired(boolean required)
	{
		this.required=required;
	}
	
/*
	public boolean getInJar()
	{
		return inJar;
	}
	
	public void setInJar(boolean inJar)
	{
		this.inJar=inJar;
	}

	public boolean getExtract() {
		return extract;
	}

	public void setExtract(boolean extract) {
		this.extract = extract;
	}
*/
	public boolean getInRoot() {
		return inRoot;
	}

	public void setInRoot(boolean inRoot) {
		this.inRoot = inRoot;
	}
	
	public String getMD5() {
		if (md5 == null) {
			MCUpdater.apiLogger.warning("No MD5 for Module " + this.id);
			return "";
		}
		return md5;
	}
	
	public void setMD5(String md5) {
		if( md5 != null )
			this.md5 = md5.toLowerCase(Locale.ENGLISH);
	}

	public boolean getIsDefault() {
		return isDefault;
	}
	
	public void setIsDefault(boolean isDefault) {
		this.isDefault = isDefault;
	}

/*
	public boolean getCoreMod() {
		return coreMod;
	}

	public void setCoreMod(boolean coreMod) {
		this.coreMod = coreMod;
	}
*/
	public String getId() {
		return id;
	}

	public void setId(String id) {
		this.id = id;
	}

	public String getDepends() {
		return depends;
	}

	public void setDepends(String depends) {
		this.depends = depends;
	}
	
	@Override
	public String toString() {
		return "{id="+id+";name="+name+";type="+modType+";md5="+md5+";}";
	}

	public ModSide getSide() {
		return side;
	}

	public void setSide(ModSide side) {
		this.side = side;
	}
	public void setSide(String side) {
		if( side == null || side.length() == 0 ) {
			side = "BOTH";
		} else {
			side = side.toUpperCase();
		}
		try {
			setSide( ModSide.valueOf(side) );
		} catch( IllegalArgumentException e ) {
			setSide( ModSide.BOTH );
		}
	}
	
	public boolean isClientSide() {
		return side != ModSide.SERVER;
	}
	public boolean isServerSide() {
		return side != ModSide.CLIENT;
	}

	public String getPath() {
		return path;
	}

	public void setPath(String path) {
		if (path == null) {
			path = "";
		}
		this.path = path;
	}

	public int getJarOrder() {
		return order;
	}

	public boolean getKeepMeta() {
		return keepMeta;
	}

	public void setKeepMeta(boolean keepMeta) {
		this.keepMeta = keepMeta;
	}

	public HashMap<String,String> getMeta() {
		return meta;
	}

	public void setMeta(HashMap<String,String> meta) {
		this.meta = meta;
	}

	public String getLaunchArgs() {
		return launchArgs;
	}

	public void setLaunchArgs(String launchArgs) {
		this.launchArgs = launchArgs;
	}
/*
	public boolean getIsLibrary() {
		return isLibrary;
	}

	public void setIsLibrary(boolean isLibrary) {
		this.isLibrary = isLibrary;
	}
*/
	public List<PrioritizedURL> getPrioritizedUrls() {
		return this.urls;
	}

	public String getJreArgs() {
		return jreArgs;
	}

	public void setJreArgs(String jreArgs) {
		this.jreArgs = jreArgs;
	}

	public void setModType(ModType modType) {
		this.modType = modType;
	}

	public ModType getModType() {
		return this.modType;
	}

	public String getLoadPrefix() {
		return this.loadPrefix;
	}

	public void setLoadPrefix(String prefix) {
		this.loadPrefix = prefix;
	}

	public long getFilesize() {
		return this.filesize;
	}

	public void setFilesize(long size) {
		this.filesize = size;
	}

	public String getParent() {
		return this.parent;
	}

	public void setParent(String parent) {
		this.parent = parent;
	}

	public String getFilename() {
		if (!this.path.isEmpty()) {
			return this.path;
		} else {
			StringBuilder newPath = new StringBuilder();
			if (this.modType == ModType.Regular || this.modType == ModType.Litemod) {
				newPath.append("mods/");
			} else if (this.modType == ModType.Coremod) {
				newPath.append("coremods/");
			} else if (this.modType == ModType.Library) {
				newPath.append("lib/");
			}
			if (!this.loadPrefix.isEmpty()) {
				newPath.append(this.loadPrefix + "_");
			}
			newPath.append(cleanForFile(this.id));
			newPath.append(this.modType == ModType.Litemod ? ".litemod" : ".jar" );
			return newPath.toString();
		}
	}

	private String cleanForFile(String id) {
		return id.replaceAll("[^a-zA-Z_0-9\\-.]", "_");
	}

/*
	public boolean isLitemod() {
		return litemod;
	}

	public void setLitemod(boolean litemod) {
		this.litemod = litemod;
	}
*/
}
