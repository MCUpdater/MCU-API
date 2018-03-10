package org.mcupdater.mojang;

import com.google.gson.Gson;

import java.io.IOException;
import java.io.InputStreamReader;
import java.net.URL;
import java.net.URLConnection;
import java.util.List;

public class VersionManifest {
	private Latest latest;
	private List<VersionInfo> versions;
	private static VersionManifest current;

	public static VersionManifest getCurrent(boolean forceRefresh) throws IOException {
		Gson gson = new Gson();
		if (current == null || forceRefresh) {
			URLConnection conn;
			conn = (new URL("https://launchermeta.mojang.com/mc/game/version_manifest.json")).openConnection();
			current = gson.fromJson(new InputStreamReader(conn.getInputStream()),VersionManifest.class);
		}
		return current;
	}

	public Latest getLatest() {
		return latest;
	}

	public List<VersionInfo> getVersions() {
		return versions;
	}

	public VersionInfo getVersion(String versionNumber) throws VersionNotFoundException {
		for (VersionInfo version : versions) {
			if (version.getId().equals(versionNumber)) {
				return version;
			}
		}
		throw new VersionNotFoundException();
	}

	public class Latest {
		private String snapshot;
		private String release;

		public String getSnapshot() {
			return snapshot;
		}

		public String getRelease() {
			return release;
		}
	}

	public class VersionInfo {
		private String id;
		private String type;
		private String time;
		private String releaseTime;
		private String url;

		public String getId() {
			return id;
		}

		public String getType() {
			return type;
		}

		public String getTime() {
			return time;
		}

		public String getReleaseTime() {
			return releaseTime;
		}

		public String getUrl() {
			return url;
		}

		@Override
		public String toString() {
			return type + ": " + id;
		}
	}

	public class VersionNotFoundException extends Exception {

		public VersionNotFoundException() {
			super("Requested version not found.");
		}
	}
}
