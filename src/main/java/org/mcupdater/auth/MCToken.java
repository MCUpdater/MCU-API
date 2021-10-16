package org.mcupdater.auth;

public class MCToken {
    private String username;
    private String[] roles; //assumed data type to be string
    private String access_token;
    private String token_type;
    private int expires_in;

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public String[] getRoles() {
        return roles;
    }

    public void setRoles(String[] roles) {
        this.roles = roles;
    }

    public String getAccessToken() {
        return access_token;
    }

    public void setAccessToken(String access_token) {
        this.access_token = access_token;
    }

    public String getTokenType() {
        return token_type;
    }

    public void setTokenType(String token_type) {
        this.token_type = token_type;
    }

    public int getExpiresIn() {
        return expires_in;
    }

    public void setExpiresIn(int expires_in) {
        this.expires_in = expires_in;
    }
}
