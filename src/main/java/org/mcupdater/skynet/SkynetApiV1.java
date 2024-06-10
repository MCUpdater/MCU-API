package org.mcupdater.skynet;

import com.google.gson.Gson;
import org.mcupdater.util.MCUpdater;

import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.util.logging.Level;

public class SkynetApiV1 {
	static final String API_BASE = "https://skynet.mcupdater.com/api/v1/";

	public static String lookupCF(Integer fileId){
		Gson gson = new Gson();
		HttpClient client = HttpClient.newHttpClient();
		HttpRequest request = HttpRequest.newBuilder()
				.uri(URI.create(API_BASE + "cf_files/" + fileId))
				.GET()
				.build();
		HttpResponse<String> response = null;
		try {
			response = client.send(request, HttpResponse.BodyHandlers.ofString());
		} catch (Exception e) {
			MCUpdater.apiLogger.log(Level.SEVERE, "Failed to read from Skynet", e);
		}
		if (response.statusCode() == 404) return "";
		CFFileEntry jsonResponse = gson.fromJson(response.body(), CFFileEntry.class);
		return jsonResponse.getUrl();
	}

	private static class CFFileEntry {
		private Integer projectid;
		private Integer fileid;
		private String filename;
		private String md5;
		private Integer size;

		public String getUrl() {
			String fileidString = fileid.toString();
			return "http://edge.forgecdn.net/files/" + new StringBuffer(fileidString).insert(fileidString.length()-3, "/") + "/" + filename;
		}

		public Integer getProjectid() {
			return projectid;
		}

		public Integer getFileid() {
			return fileid;
		}

		public String getFilename() {
			return filename;
		}

		public String getMd5() {
			return md5;
		}

		public Integer getSize() {
			return size;
		}
	}
}
