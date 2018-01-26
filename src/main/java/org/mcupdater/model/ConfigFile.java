package org.mcupdater.model;

import org.mcupdater.util.MCUpdater;

import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Locale;

public class ConfigFile implements IPackElement {
	protected List<PrioritizedURL> urls = new ArrayList<>();
	private String path;
	private String md5;
	private boolean noOverwrite;
	
	public ConfigFile(String url, String path, boolean noOverwrite, String md5)
	{
		addUrl(new PrioritizedURL(url,0));
		setPath(path);
		setNoOverwrite(noOverwrite);
		setMD5(md5);
	}

	public ConfigFile(List<PrioritizedURL> urls, String path, boolean noOverwrite, String md5) {
		setUrls(urls);
		setPath(path);
		setNoOverwrite(noOverwrite);
		setMD5(md5);
	}

	public List<URL> getUrls()
	{
		List<URL> result = new ArrayList<>();
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

	public List<PrioritizedURL> getPrioritizedUrls() {
		return this.urls;
	}

	public String getPath()
	{
		return path;
	}
	
	public void setPath(String path)
	{
		this.path = path;
	}
	
	public boolean isNoOverwrite() {
		return noOverwrite;
	}

	public void setNoOverwrite(boolean noOverwrite) {
		this.noOverwrite = noOverwrite;
	}

	public String getMD5()
	{
		if (md5 == null) {
			MCUpdater.apiLogger.warning("No MD5 for ConfigFile: " + path);
			return "";
		}
		return md5;
	}
	
	public void setMD5(String md5)
	{
		if( md5 != null )
			this.md5 = md5.toLowerCase(Locale.ENGLISH);
	}

	@Override
	public String getFriendlyName() {
		return "ConfigFile: " + getPath();
	}

	@Override
	public String toString() {
		return getFriendlyName();
	}

}
