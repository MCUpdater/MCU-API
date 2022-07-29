package org.mcupdater.settings;

import org.mcupdater.MCUApp;
import org.mcupdater.auth.*;

public class MSAProfile extends Profile {

    private String refreshToken;
    private transient String authToken;

    public MSAProfile() {
        super();
        this.style = "MSA";
    }

    @Override
    public String getSessionKey(MCUApp caller) throws Exception {
        return null;
    }

    @Override
    public String getAuthAccessToken() {
        return authToken;
    }

    @Override
    public boolean refresh() {
        TokenResponse token = MicrosoftAuth.refreshAuthToken(getRefreshToken());
        XBLToken xblToken = MicrosoftAuth.getXBLAuth(token.getAccessToken());
        XBLToken xstsToken = MicrosoftAuth.getXSTSAuth(xblToken.getToken());
        MCToken mcToken = MicrosoftAuth.getMCToken(xstsToken.getDisplayClaims().getXui()[0].getUhs(), xstsToken.getToken());
        MCProfile mcProfile = MicrosoftAuth.getMinecraftProfile(mcToken.getAccessToken());
        if (mcProfile.getId().equals(this.getUUID())) {
            this.setRefreshToken(token.getRefreshToken());
            this.setAuthAccessToken(mcToken.getAccessToken());
            this.setName(mcProfile.getName());
            this.setUUID(mcProfile.getId());
            System.out.println("XUID: " + token.getUserId());
            return true;
        } else {
            return false;
        }
    }

    public void setRefreshToken(String token) {
        this.refreshToken = token;
    }

    public String getRefreshToken() {
        return this.refreshToken;
    }

    public void setAuthAccessToken(String authToken) {
        this.authToken = authToken;
    }
}
