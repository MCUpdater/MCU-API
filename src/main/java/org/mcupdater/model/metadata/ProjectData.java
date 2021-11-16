package org.mcupdater.model.metadata;

import java.util.ArrayList;
import java.util.List;

public class ProjectData {
	private String name;
	private String modid;
	private ArrayList<String> dependencies;
	private ArrayList<Downloadable> downloadables;
	private String modloader;
	private String url;
	private String author;
	private Long curseId;
	private String slug;
	private List<Author> authors;
	private long timestamp;

	public ProjectData() {
		dependencies = new ArrayList<>();
		downloadables = new ArrayList<>();
		authors = new ArrayList<>();
		modloader = "Forge";
	}

	public String getModid() {
		return modid;
	}

	public void setModid(String modid) {
		this.modid = modid;
	}

	public ArrayList<String> getDependencies() {
		return dependencies;
	}

	public void setDependencies(ArrayList<String> dependencies) {
		this.dependencies = dependencies;
	}

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public String getUrl() {
		if (slug != null && !slug.isEmpty()) {
			return "https://minecraft.curseforge.com/projects/" + slug;
		} else if (curseId != null && curseId > 0) {
			return "https://minecraft.curseforge.com/projects/" + curseId;
		} else {
			return url;
		}
	}

	public void setUrl(String url) {
		this.url = url;
	}

	public ArrayList<Downloadable> getDownloadables() {
		return downloadables;
	}

	public void setDownloadables(ArrayList<Downloadable> downloadables) {
		this.downloadables = downloadables;
	}

	public String getAuthor() {
		return author;
	}

	public void setAuthor(String author) {
		this.author = author;
	}

	public long getCurseId() {
		return curseId;
	}

	public void setCurseId(long curseId) {
		this.curseId = curseId;
	}

	public String getSlug() {
		return slug;
	}

	public void setSlug(String slug) {
		this.slug = slug;
	}

	public List<Author> getAuthors() {
		return authors;
	}

	public void setAuthors(List<Author> authors) {
		this.authors = authors;
	}

	public long getTimestamp() {
		return timestamp;
	}

	public void setTimestamp(long timestamp) {
		this.timestamp = timestamp;
	}

	public String getModloader() {
		return modloader;
	}

	public void setModloader(String modloader) {
		this.modloader = modloader;
	}
}
