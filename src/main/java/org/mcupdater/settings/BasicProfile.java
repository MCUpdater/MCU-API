package org.mcupdater.settings;

import org.mcupdater.MCUApp;

public class BasicProfile extends Profile {
    public BasicProfile(String style) {
        this.style = style;
    }

    @Override
    public String getSessionKey(MCUApp caller) throws Exception {
        return null;
    }

    @Override
    public String getAuthAccessToken() {
        return null;
    }

    @Override
    public boolean refresh() {
        return true;
    }
}
