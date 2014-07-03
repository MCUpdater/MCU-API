package org.mcupdater;

import org.mcupdater.downloadlib.DownloadQueue;
import org.mcupdater.downloadlib.Downloadable;
import org.mcupdater.mojang.MinecraftVersion;
import org.mcupdater.settings.Profile;

import java.io.File;
import java.util.Collection;
import java.util.logging.Logger;

public abstract class MCUApp {
	
	public Logger baseLogger;
	private AuthManager authManager;

	public abstract void setStatus(String string);
	//public abstract void setProgressBar(int i);
	public abstract void log(String msg);
	public abstract Profile requestLogin(String username);
	public abstract DownloadQueue submitNewQueue(String queueName, String parent, Collection<Downloadable> files, File basePath, File cachePath);
	public abstract DownloadQueue submitAssetsQueue(String queueName, String parent, MinecraftVersion version);

	public AuthManager getAuthManager() {
		return this.authManager;
	};

	public void setAuthManager(AuthManager newAuth) {
		this.authManager = newAuth;
	}
}