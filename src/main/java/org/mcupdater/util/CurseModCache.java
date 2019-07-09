package org.mcupdater.util;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;
import org.mcupdater.api.Version;
import org.mcupdater.downloadlib.DownloadUtil;
import org.mcupdater.model.CurseProject;

import java.io.IOException;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLConnection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.logging.Level;
import java.util.regex.Pattern;

public enum CurseModCache {
	INSTANCE;
	
	private static final String DOWNLOAD = "/download/";
	private static final String FILE = "/file";
	private static final String BASE_URL = "https://minecraft.curseforge.com/projects/";
	private static final String BASE_URL2 = "https://www.curseforge.com/minecraft/mc-mods/";
	private static final Map<String,Integer> versions = new HashMap<>();
	private static final String GAMEVERSIONS_URL = "https://minecraft.curseforge.com/api/game/versions?token=a98e4aa8-f43e-4c6a-b245-70327d9c2f85";
	private static Pattern md5Pattern = Pattern.compile("[a-fA-F0-9]{32}"); // regex pattern to match MD5 strings
	
	private CurseModCache() {
		// TODO: handle a serialized data cache location (possibly lean on DownloadCache here?)
		/**
		 * This is primarily a performance concern, as re-scraping Curse with EVERY SINGLE LOAD
		 * can get very slow very quickly. We don't want that, and we don't want Curse outages
		 * to prevent people from loading the game. So caching results is important.
		 */
	}
	
	private static String baseURL(CurseProject curse) {
		return BASE_URL+curse.getProject();
	}
	private static String baseURL2(CurseProject curse) {
		return BASE_URL2+curse.getProject();
	}

	public static String fetchURL(CurseProject curse) {
		if( curse.getFile() > 0 ) {
			// if we have a file, just go there
			final String url = baseURL2(curse)+DOWNLOAD+curse.getFile()+FILE;
			curse.setURL(url);
			return curse.getURL();
		} else {
			// autodiscovery scraping time
			final String pack_mc_version = curse.getMCVersion();
			final CurseProject.ReleaseType min_release_type = curse.getReleaseType();
			MCUpdater.apiLogger.log(Level.INFO, "Performing URL autodiscovery for "+curse);
			if (versions.size() == 0) {
				MCUpdater.apiLogger.log(Level.INFO, "Populating CurseForge version map");
				Gson gson = new GsonBuilder().create();
				try {
					URLConnection conn = new URL(GAMEVERSIONS_URL).openConnection();
					GameVersion[] curseversions = gson.fromJson(new InputStreamReader(conn.getInputStream()),GameVersion[].class);
					for (int i = 0; i < curseversions.length; i++) {
						versions.put(curseversions[i].getName(),curseversions[i].getId());
					}
				} catch (IOException e) {
					e.printStackTrace();
				}
			}
			final String filesURL = baseURL(curse)+"/files?filter-game-version=2020709689:" + versions.get(pack_mc_version);
			Document filesDoc;
			try {
				filesDoc = Jsoup.connect(filesURL).validateTLSCertificates(false).get();
			} catch (IOException e) {
				MCUpdater.apiLogger.log(Level.SEVERE, "Unable to read project data for "+curse, e);
				return null;
			}
			
			// TODO: re-request this, filtering by MC version on their end before proceeding
			
			// for better error messaging
			CurseProject.ReleaseType best_release_type = null;
			int version_matches = 0;

			// identify and set file, then re-invoke fetchURL
			Elements fileList = filesDoc.getElementsByClass("project-file-list-item");
			ProjectFile file = new ProjectFile();
			for( Element el : fileList) {
				final Element elFname = el.getElementsByClass("project-file-name-container").first().children().first();
				//MCUpdater.apiLogger.log(Level.FINEST, elFname.toString());
				final String href = elFname.attr("href");
				final String fileNum = href.substring(href.lastIndexOf('/')+1);
				final int id = Integer.parseInt(fileNum);

				// filter for MC version first
				final String mc_version = el.getElementsByClass("version-label").first().text();
				if( !Version.fuzzyMatch(mc_version, pack_mc_version) ) {
					MCUpdater.apiLogger.log(Level.FINE, "Skipping "+curse+":"+id+", mc version mismatch, "+mc_version);
					continue;
				} else {
					++version_matches;
				}

				// then filter by release type
				final Element elRelease = el.getElementsByClass("project-file-release-type").first();
				final String release_type = elRelease.children().first().attr("title");
				CurseProject.ReleaseType type = CurseProject.ReleaseType.parse(release_type);
				if( type.worseThan(min_release_type) || type.worseThan(file.release_type) ) {
					MCUpdater.apiLogger.log(Level.FINE, "Skipping "+curse+":"+id+", release type mismatch, "+type);
					// notice if we found something -slightly- better
					if( best_release_type == null || best_release_type.worseThan(file.release_type) ) {
						best_release_type = file.release_type;
					}
					continue;
				}
				
				final int upload_date = Integer.parseInt(el.getElementsByClass("standard-date").first().attr("data-epoch"));
				if( upload_date > file.upload_date ) {
					// take the newer build
					MCUpdater.apiLogger.log(Level.FINE, "Selecting "+curse+":"+id);
					file.release_type = type;
					file.upload_date = upload_date;
					file.id = id;
					file.mc_version = mc_version;
				} else {
					MCUpdater.apiLogger.log(Level.FINE, "Skipping "+curse+":"+id+", older upload date");
				}
			}
			
			// did we find a url?
			if( file.id > 0 ) {
				curse.setFile(file.id);
				return fetchURL(curse);
			} else {
				String errMsg = "Unable to find candidate for "+curse+" after checking "+fileList.size()+" files";
				if( fileList.size() == 0 ) {
					errMsg += ". MCU found zero public releases for this project.";
				} else if( best_release_type != null && best_release_type.worseThan(min_release_type) ) {
					errMsg += ". Best mc "+pack_mc_version+" release found was "+best_release_type+", but pack requested "+min_release_type+".";
				} else if( version_matches == 0 ) {
					errMsg += ". Found zero releases for mc "+pack_mc_version+".";
				}
				MCUpdater.apiLogger.log(Level.SEVERE, errMsg);
			}
		}
		
		return null;
	}
	
	public static String fetchMD5(CurseProject curse) {
		// must have a URL before we can look for an MD5 for it
		if( curse.getURL().isEmpty() ) {
			fetchURL(curse);
		}
		
		final String downloadURL = baseURL2(curse)+"/files/"+curse.getFile();
		if( downloadURL.isEmpty() ) {
			MCUpdater.apiLogger.log(Level.SEVERE, "Unable to fetch MD5 for "+curse+" with no URL");
			return null;
		}
		
		final String fileURL = downloadURL;

		Document fileDoc;
		try {
			fileDoc = Jsoup.connect(fileURL).validateTLSCertificates(false).get();
		} catch (IOException e) {
			MCUpdater.apiLogger.log(Level.SEVERE, "Unable to read file data for "+curse, e);
			return null;
		}
		MCUpdater.apiLogger.finest(fileDoc.toString());
		List<Element> textElements = fileDoc.getElementsByClass("text-sm");
		for (Element el : textElements) {
			if(md5Pattern.matcher(el.text()).matches()) {
				curse.setMD5(el.text());
			}
		}
		//curse.setMD5(elMD5.text());
		return curse.getMD5();
	}
	
	public static String getTextID(long modID) {
		final String origURL = BASE_URL+modID;
		try {
			URL sourceURL = new URL(origURL);
			HttpURLConnection conn = DownloadUtil.getMCUHttpURLConnection(sourceURL);
			conn.setUseCaches(false);
			conn.setInstanceFollowRedirects(false);
			String newURL = "";
			if (conn.getResponseCode() / 100 == 3) {
				newURL = conn.getHeaderField("Location");
				if (newURL.startsWith("//")) { //Handling of schemeless URLs - protocol comes from context.
					newURL = sourceURL.getProtocol() + ":" + newURL;
				}
			}
			final String textID = newURL.substring(newURL.lastIndexOf('/')+1);
			MCUpdater.apiLogger.log(Level.FINE, "Found text ID '"+textID+"' for curse:"+modID);
			return textID;
		} catch (IOException e) {
			MCUpdater.apiLogger.log(Level.WARNING, "Unable to find text ID for curse:"+modID, e);
			return Long.toString(modID);
		}
	}

	public static String getTextID(CurseProject curse) {
		final Long modID = Long.parseLong(curse.getProject());
		final String textID = getTextID(modID);
		curse.setProject(textID);
		return textID;
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
