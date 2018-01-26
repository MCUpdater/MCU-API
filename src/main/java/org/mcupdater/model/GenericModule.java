package org.mcupdater.model;

import org.mcupdater.util.CurseModCache;
import org.mcupdater.util.MCUpdater;

import java.net.MalformedURLException;
import java.net.URL;
import java.util.*;
import java.util.logging.Level;

public class GenericModule implements IPackElement {
	protected String parent = "";
	protected String name = "";
	protected String id = "";
	protected List<PrioritizedURL> urls = new ArrayList<>();
	protected CurseProject curse = null;
	protected String path = "";
	protected String depends = "";
	protected boolean required = false;
	protected int order = 1;
	protected boolean keepMeta = false;
	protected boolean inRoot = false;
	protected boolean isDefault = false;
	protected String md5 = "";
	protected ModSide side = ModSide.BOTH;
	protected HashMap<String,String> meta = new HashMap<>();
	protected String launchArgs = "";
	protected String jreArgs = "";
	protected ModType modType = ModType.Regular;
	protected String loadPrefix = "";
	protected long filesize = 100000;

	public GenericModule(String name, String id, List<PrioritizedURL> url, CurseProject curse, String depends, boolean required, ModType type, int jarOrder, boolean keepMeta, boolean inRoot, boolean isDefault, String md5, String side, String path, HashMap<String, String> meta, String launchArgs, String jreArgs, String parent) {
		this.setName(name);
		this.setId(id);
		this.setUrls(url);
		this.setCurseProject(curse);
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

	public GenericModule(String name, String id, List<PrioritizedURL> url, CurseProject curse, String depends, boolean required, ModType type, int jarOrder, boolean keepMeta, boolean inRoot, boolean isDefault, String md5, String side, String path, HashMap<String, String> meta, String launchArgs, String jreArgs) {
		this(name, id, url, curse, depends, required, type, jarOrder, keepMeta, inRoot, isDefault, md5, side, path, meta, launchArgs, jreArgs, "unspecified");
	}

	public GenericModule(String name, String id, List<PrioritizedURL> url, CurseProject curse, String depends, boolean required, boolean inJar, int jarOrder, boolean keepMeta, boolean extract, boolean inRoot, boolean isDefault, boolean coreMod, String md5, String side, String path, HashMap<String, String> meta, String launchArgs, String jreArgs, String parent){
		this(name, id, url, curse, depends, required, ModType.Regular, jarOrder, keepMeta, inRoot, isDefault, md5, side, path, meta, launchArgs, jreArgs, parent);
		if (inJar) {
			this.setModType(ModType.Jar);
		} else if (extract) {
			this.setModType(ModType.Extract);
		} else if (coreMod) {
			this.setModType(ModType.Coremod);
		}
	}

	public void setJarOrder(int jarOrder) {
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
		// prepend non-null curse project
		if( curse != null ) {
			try {
				final URL curseURL = new URL(CurseModCache.fetchURL(curse));
				result.add(curseURL);
				MCUpdater.apiLogger.log(Level.INFO, "Found URL for "+curse+":"+curse.getFile());
			} catch (MalformedURLException e) {
				MCUpdater.apiLogger.log(Level.SEVERE, "Unable to parse URL for "+curse, e);
			}
		}
		// iterate any manually specified url's
		Collections.sort(urls, new PriorityComparator());
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
	
	public CurseProject getCurseProject()
	{
		return curse;
	}
	public void setCurseProject(CurseProject curse)
	{
		this.curse = curse;
	}
	
	public boolean getRequired()
	{
		return required;
	}
	
	public void setRequired(boolean required)
	{
		this.required=required;
	}

	public boolean getInRoot() {
		return inRoot;
	}

	public void setInRoot(boolean inRoot) {
		this.inRoot = inRoot;
	}
	
	public String getMD5() {
		if (md5 == null || md5.isEmpty()) {
			// look at curse first if we have it
			if( curse != null ) {
				final String curseMD5 = CurseModCache.fetchMD5(curse);
				setMD5(curseMD5);
				MCUpdater.apiLogger.log(Level.INFO, "Found MD5 for "+curse+":"+curse.getFile());
				return md5;
			}
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
				newPath.append("libraries/");
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

	public boolean isSideValid(ModSide testSide) {
		return this.side.equals(ModSide.BOTH) || this.side.equals(testSide);
	}

	public String toDebugString() {
		return "{id="+id+";name="+name+";type="+modType+";md5="+md5+";}";
	}

	@Override
	public String getFriendlyName() {
		return "Submodule: " + getName();
	}

	@Override
	public String toString() {
		return getFriendlyName();
	}
}
