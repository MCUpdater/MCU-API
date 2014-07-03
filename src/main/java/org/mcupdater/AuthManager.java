package org.mcupdater;

import org.mcupdater.settings.Profile;

public abstract class AuthManager {

	public abstract String getSessionKey(Profile profile) throws Exception;

	public abstract Object authenticate(String name, String s, String clientToken);
}
