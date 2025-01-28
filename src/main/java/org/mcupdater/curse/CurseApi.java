package org.mcupdater.curse;

import com.google.gson.Gson;
import org.mcupdater.util.MCUpdater;

import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.nio.ByteBuffer;
import java.nio.charset.StandardCharsets;
import java.util.logging.Level;

public class CurseApi {
	public static final int GAME_ID_MINECRAFT = 432;
	private static final String API_KEY = CurseApi.deobf(607281444,825238642,1182230647,1767987267,1499944058,1684557142,1479438895,1331057715,842346799,1330799960,778598767,1480077400,913928045,1630744368,1515473970);
	public static final String API_BASE = "https://api.curseforge.com";

	public static String getDownloadUrl(int projectId, int fileId) {
		Gson gson = new Gson();
		HttpClient client = HttpClient.newHttpClient();
		HttpRequest request = HttpRequest.newBuilder()
				.uri(URI.create(API_BASE + "/v1/mods/" + projectId + "/files/" + fileId))
				.header("x-api-key",API_KEY)
				.GET()
				.build();
		HttpResponse<String> response = null;
		try {
			response = client.send(request, HttpResponse.BodyHandlers.ofString());
		} catch (Exception e) {
			MCUpdater.apiLogger.log(Level.SEVERE, "Failed to read from CurseForge", e);
		}
		CurseFile curseFile = gson.fromJson(response.body(), CurseFile.class);
		if (curseFile.data().downloadUrl() == null) {
			MCUpdater.apiLogger.warning(String.format("3rd party download restricted mod detected! Project: %d File: %d",projectId, fileId));
		}
		return curseFile.data().getDownloadUrl();
	}

	private static String deobf(int... a){
		StringBuilder output = new StringBuilder();
		for (int segment : a) {
			ByteBuffer bb = ByteBuffer.allocate(4);
			bb.putInt(segment);
			output.append(new String(bb.array(), StandardCharsets.ISO_8859_1));
		}
		return output.toString();
	}

	private static class DownloadUrl {
		private String data;

		public String getData() {
			return data;
		}
	}
}
