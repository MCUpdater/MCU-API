package org.mcupdater.util;

import com.google.gson.Gson;

import java.io.IOException;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.sql.Timestamp;

public class MojangStatus {
	long _t;
	boolean status;
	boolean auth;
	boolean session;

	public static MojangStatus getMojangStatus() {
		Gson gson = new Gson();
		try {
			HttpURLConnection conn = (HttpURLConnection) new URL("http://files.mcupdater.com/MojangStatus.php").openConnection();

			InputStream inputStream = conn.getInputStream();

			StringBuilder response = new StringBuilder();
			byte[] buffer = new byte[1024];
			int bytesRead;
			while ((bytesRead = inputStream.read(buffer)) > 0) {
				response.append(new String(buffer,"UTF-8").substring(0,bytesRead));
			}
			return gson.fromJson(response.toString(), MojangStatus.class);

		} catch (IOException e) {
			e.printStackTrace();
			return null;
		}
	}

	public Timestamp getUpdated() {
		return new Timestamp(_t * 1000);
	}

	public boolean getStatus() {
		return status;
	}

	public boolean getAuth() {
		return auth;
	}

	public boolean getSession() {
		return session;
	}
}
