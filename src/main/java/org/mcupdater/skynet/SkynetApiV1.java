package org.mcupdater.skynet;

import com.google.gson.Gson;
import org.mcupdater.util.MCUpdater;

import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.util.Optional;
import java.util.logging.Level;

public class SkynetApiV1 {
	static final String API_BASE = "https://skynet.mcupdater.com/api/v1/";

	public static Optional<CFFileEntry> lookupCF(Integer fileId){
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
			return Optional.empty();
		}
		if (response.statusCode() == 404) return Optional.empty();
		CFFileEntry jsonResponse = gson.fromJson(response.body(), CFFileEntry.class);
		return Optional.of(jsonResponse);
	}

}
