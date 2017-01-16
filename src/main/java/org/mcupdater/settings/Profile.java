package org.mcupdater.settings;

import org.mcupdater.MCUApp;
import org.mcupdater.model.JSON;

import java.util.logging.Level;

@JSON
public class Profile {
	private String style;
	private String name;
	private String username;
	private String sessionKey;
	private String accessToken;
	private String lastInstance;
	private String uuid;
	private String userId;
	private boolean legacy;

	public String getStyle() {
		return style;
	}

	public void setStyle(String style) {
		this.style = style;
	}

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public String getUsername() {
		return username;
	}

	public void setUsername(String username) {
		this.username = username;
	}

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

	public String getAccessToken() {
		return accessToken;
	}

	public void setAccessToken(String accessToken) {
		this.accessToken = accessToken;
	}

	public String getLastInstance() {
		return lastInstance;
	}

	public void setLastInstance(String lastInstance) {
		this.lastInstance = lastInstance;
	}

	public String getUUID() {
		return uuid;
	}

	public void setUUID(String uuid) { this.uuid = uuid; }

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

	@Override
	public String toString() { return this.getName(); }
}
