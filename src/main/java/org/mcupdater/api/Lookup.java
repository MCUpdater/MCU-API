package org.mcupdater.api;

import org.mcupdater.curse.CurseApi;
import org.mcupdater.database.LocalCache;
import org.mcupdater.skynet.SkynetApiV1;
import org.mcupdater.util.MCUpdater;
import java.util.logging.Level;

public class Lookup {
	public static String getDownloadUrl(Platform platform, String fileId, String projectId) {
		String url;
		switch (platform) {
			case CURSEFORGE:
				Integer cfProject = Integer.valueOf(projectId);
				Integer cfFile = Integer.valueOf(fileId);
				url = LocalCache.lookupCF(cfFile);
				if (!url.isEmpty()) return url;
				url = SkynetApiV1.lookupCF(cfFile);
				if (url.isEmpty()) {
					url = CurseApi.getDownloadUrl(cfProject, cfFile);
				}
				if (url.startsWith("http")) {
					LocalCache.saveFileCF(cfFile, url);
				}
				return url;

			case MODRINTH:
				// TODO: Handle Modrinth

			case MCUPDATER:
				// TODO: Handle MCUpdater repository

			case OTHER:
				// TODO: Handle Others?

			default:
				return null;
		}
	}
}
