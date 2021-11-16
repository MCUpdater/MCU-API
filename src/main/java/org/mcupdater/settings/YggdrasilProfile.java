package org.mcupdater.settings;

import org.mcupdater.MCUApp;
import org.mcupdater.model.JSON;

import java.util.logging.Level;

@JSON
public class YggdrasilProfile extends Profile {
	private String username;
	private transient String sessionKey;
	private String accessToken;
	private String userId;
	private boolean legacy;

	public YggdrasilProfile() {
		this.style = "Yggdrasil";
	}

	@Override
	public String getSessionKey(MCUApp caller) throws Exception {
		String currentSessionKey = null;
		if (this.sessionKey == null || this.sessionKey.isEmpty()) {
			if (this.style.equals("Yggdrasil")) {
				try {
					currentSessionKey = caller.getAuthManager().getSessionKey(this);
				} catch (Exception e) {
					Profile newProfile = caller.requestLogin(this.username);
					SettingsManager.getInstance().getSettings().addOrReplaceProfile(newProfile);
					if (!SettingsManager.getInstance().isDirty()) {
						SettingsManager.getInstance().saveSettings();
					}
					currentSessionKey = newProfile.getSessionKey(caller);
					caller.baseLogger.log(Level.INFO, "A full login request occurred due to the following exception", e);
					caller.baseLogger.finer("Session key: " + currentSessionKey);
				}
			}
		} else {
			currentSessionKey = this.sessionKey;
		}
		return currentSessionKey;
	}

	public void setSessionKey(String sessionKey) {
		this.sessionKey = sessionKey;
	}

	@Override
	public String getAuthAccessToken() {
		return accessToken;
	}

	@Override
	public boolean refresh() {
		//TODO: Implement proper Yggdrasil refresh
		return true;
	}

	public void setAccessToken(String accessToken) {
		this.accessToken = accessToken;
	}

	public String getUserId() {
		return userId;
	}

	public void setUserId(String userId) {
		this.userId = userId;
	}

	public boolean isLegacy() {
		return legacy;
	}

	public void setLegacy(boolean legacy) {
		this.legacy = legacy;
	}

	public String getUsername() {
		return username;
	}

	public void setUsername(String username) {
		this.username = username;
	}

}
