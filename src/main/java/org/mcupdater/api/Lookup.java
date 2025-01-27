package org.mcupdater.api;

import org.mcupdater.curse.CurseApi;
import org.mcupdater.database.LocalCache;
import org.mcupdater.skynet.SkynetApiV1;

import java.util.concurrent.atomic.AtomicReference;

public class Lookup {
	public static String getDownloadUrl(Platform platform, String fileId, String projectId) {
		AtomicReference<String> url = new AtomicReference<>();
		switch (platform) {
			case CURSEFORGE:
				Integer cfProject = Integer.valueOf(projectId);
				Integer cfFile = Integer.valueOf(fileId);
				url.set(LocalCache.lookupCF(cfFile));
				if (!url.get().isEmpty()) return url.get();
				SkynetApiV1.lookupCF(cfFile).ifPresent(skynet -> url.set(skynet.getUrl()));
				if (url.get().isEmpty()) {
					url.set(CurseApi.getDownloadUrl(cfProject, cfFile));
				}
				if (url.get().startsWith("http")) {
					LocalCache.saveFileCF(cfFile, url.get());
				}
				return url.get();

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
