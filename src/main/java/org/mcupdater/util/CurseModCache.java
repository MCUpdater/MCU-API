package org.mcupdater.util;

import java.io.IOException;
import java.util.logging.Level;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;
import org.mcupdater.api.Version;
import org.mcupdater.model.CurseProject;

public enum CurseModCache {
	INSTANCE;
	
	private static final String DOWNLOAD = "/download";
	
	private CurseModCache() {
		// TODO: handle a serialized data cache location (possibly lean on DownloadCache here?)
	}
	
	private static String baseURL(CurseProject curse) {
		return "https://minecraft.curseforge.com/projects/"+curse.getProject();
	}
	
	public static String fetchURL(CurseProject curse) {
		if( curse.getFile() > 0 ) {
			// if we have a file, just go there
			final String url = baseURL(curse)+"/files/"+curse.getFile()+DOWNLOAD;
			curse.setURL(url);
			return curse.getURL();
		} else {
			// autodiscovery scraping time
			final String filesURL = baseURL(curse)+"/files";
			Document filesDoc;
			try {
				filesDoc = Jsoup.connect(filesURL).get();
			} catch (IOException e) {
				MCUpdater.apiLogger.log(Level.SEVERE, "Unable to read project data for "+curse, e);
				return null;
			}
			
			// TODO: re-request this, filtering by MC version on their end before proceeding
			//if( Version.fuzzyMatch(a, b) ) {
			//	
			//}
			
			// TODO: identify and set file, then re-invoke fetchURL
			Elements fileList = filesDoc.getElementsByClass("project-file-list-item");
			ProjectFile file = new ProjectFile();
			for( Element el : fileList) {
				String release_type = el.getElementsByClass("project-file-release-type").first().childNode(0).attr("title");
				CurseProject.ReleaseType type = CurseProject.ReleaseType.parse(release_type);
				if( type.worseThan(curse.getReleaseType()) || type.worseThan(file.release_type) ) {
					continue;
				}

				// TODO: filter for MC version
				String mc_version = el.getElementsByClass("version-label").first().text();
				
				int upload_date = Integer.parseInt(el.getElementsByClass("standard-date").first().attr("data-epoch"));
				if( upload_date > file.upload_date ) {
					// take the newer build
					int id = Integer.parseInt(el.getElementsByClass("project-file-name-container").first().childNode(0).attr("data-id"));
					file.release_type = type;
					file.upload_date = upload_date;
					file.id = id;
					file.mc_version = mc_version;
				}
			}
			
			// did we find a url?
			if( file.id > 0 ) {
				curse.setFile(file.id);
				return fetchURL(curse);
			} else {
				MCUpdater.apiLogger.log(Level.SEVERE, "Unable to find candidate for "+curse+" after checking "+fileList.size()+" files");
			}
		}
		
		return null;
	}
	
	public static String fetchMD5(CurseProject curse) {
		// must have a URL before we can look for an MD5 for it
		if( curse.getURL().isEmpty() ) {
			fetchURL(curse);
		}
		
		final String downloadURL = curse.getURL();
		if( downloadURL.isEmpty() ) {
			MCUpdater.apiLogger.log(Level.SEVERE, "Unable to fetch MD5 for "+curse+" with no URL");
			return null;
		}
		
		final String fileURL;
		if( downloadURL.endsWith(DOWNLOAD) )
			fileURL = downloadURL.substring(0, downloadURL.length() - DOWNLOAD.length());
		else {
			MCUpdater.apiLogger.log(Level.SEVERE, "Download URL for "+curse+" did not end with "+DOWNLOAD+", refusing to look for MD5");
			return null;
		}
		
		Document fileDoc;
		try {
			fileDoc = Jsoup.connect(fileURL).get();
		} catch (IOException e) {
			MCUpdater.apiLogger.log(Level.SEVERE, "Unable to read file data for "+curse, e);
			return null;
		}
		Element elMD5 = fileDoc.getElementsByClass("md5").first();
		curse.setMD5(elMD5.text());
		return curse.getMD5();
	}
	
	public static class ProjectFile {
		public CurseProject.ReleaseType release_type = CurseProject.ReleaseType.ALPHA;
		public int upload_date = 0;
		public String mc_version = null;
		public int id = 0;
		// TODO: add support for file size because why not
		
		public ProjectFile() {}
	}
}
