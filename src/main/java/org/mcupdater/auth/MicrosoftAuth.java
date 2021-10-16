package org.mcupdater.auth;

import com.google.gson.Gson;
import org.mcupdater.util.MCUpdater;

import java.net.URI;
import java.net.URLEncoder;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicReference;
import java.util.logging.Level;

public class MicrosoftAuth {

    private static Gson gson = new Gson();

    private final static String azureClientId = "77e242c1-e649-406c-9b77-2d05f3de190d"; // MCUpdater's Azure Client ID
    private final static String oAuthUrl = "https://login.live.com/oauth20_authorize.srf";
    public final static String redirectUri = "https://login.live.com/oauth20_desktop.srf";
    private final static String scope = "XboxLive.signin%20XboxLive.offline_access";
    private final static String tokenUrl = "https://login.live.com/oauth20_token.srf";
    private final static String xblUrl = "https://user.auth.xboxlive.com/user/authenticate";
    private final static String xstsUrl = "https://xsts.auth.xboxlive.com/xsts/authorize";
    private final static String minecraftAuthUrl = "https://api.minecraftservices.com/authentication/login_with_xbox";
    private final static String minecraftOwnedUrl = "https://api.minecraftservices.com/entitlements/mcstore";
    private final static String minecraftProfileUrl = "https://api.minecraftservices.com/minecraft/profile";

    public static String getAuthUrl() {
        return String.format("%s?client_id=%s&response_type=code&redirect_uri=%s&scope=%s",oAuthUrl,azureClientId,redirectUri,scope);
    }

    /**
     *
     * @param authCode Value provided during initial OAuth 2.0 authentication
     * @return TokenResponse
     */
    public static TokenResponse getAuthToken(String authCode) {
        AtomicReference<TokenResponse> tokenResponse = new AtomicReference<>(null);
        try {
            URI authUri = new URI(tokenUrl);

            Map<String,Object> params = Map.of(
                    "client_id", azureClientId,
                    "code", authCode,
                    "grant_type", "authorization_code",
                    "redirect_uri", redirectUri
            );

            HttpRequest request = HttpRequest.newBuilder(authUri)
                    .header("Content-Type", "application/x-www-form-urlencoded")
                    .POST(getFormBody(params))
                    .build();

            HttpResponse<String> response = HttpClient.newBuilder().build().send(request, HttpResponse.BodyHandlers.ofString());
            if (response.statusCode() >= 200 && response.statusCode() < 300) { // HTTP Response codes starting with 2 are success responses
                String body = response.body();
                tokenResponse.set(gson.fromJson(body, TokenResponse.class));
            } else {
                //TODO: Handle Auth Failure
            }

        } catch (Exception e) {
            MCUpdater.apiLogger.log(Level.SEVERE, e.getMessage(), e);
        }
        return tokenResponse.get();
    }

    /**
     *
     * @param refreshToken Refresh token value from previous getAuthToken call
     * @return TokenResponse
     */
    public static TokenResponse refreshAuthToken(String refreshToken) {
        AtomicReference<TokenResponse> tokenResponse = new AtomicReference<>(null);
        try {
            URI authUri = new URI(tokenUrl);

            Map<String,Object> params = Map.of(
                    "client_id", azureClientId,
                    "refresh_token", refreshToken,
                    "grant_type", "refresh_token",
                    "redirect_uri", redirectUri
            );

            HttpRequest request = HttpRequest.newBuilder(authUri)
                    .header("Content-Type", "application/x-www-form-urlencoded")
                    .POST(getFormBody(params))
                    .build();

            HttpResponse<String> response = HttpClient.newBuilder().build().send(request, HttpResponse.BodyHandlers.ofString());
            if (response.statusCode() >= 200 && response.statusCode() < 300) { // HTTP Response codes starting with 2 are success responses
                String body = response.body();
                tokenResponse.set(gson.fromJson(body, TokenResponse.class));
            } else {
                MCUpdater.apiLogger.log(Level.WARNING, String.format("%d: %s\n====\n%s\n====\n%s",response.statusCode(), response.headers().toString(), response.body(), params));
            }

        } catch (Exception e) {
            MCUpdater.apiLogger.log(Level.SEVERE, e.getMessage(), e);
        }
        return tokenResponse.get();
    }

    public static XBLToken getXBLAuth(String accessToken) {
        AtomicReference<XBLToken> xblResponse = new AtomicReference<>(null);
        try {
            URI xblUri = new URI(xblUrl);

            String requestBody = "{\"Properties\": {\"AuthMethod\": \"RPS\", \"SiteName\": \"user.auth.xboxlive.com\", \"RpsTicket\": \"d="+accessToken+"\"}, \"RelyingParty\": \"http://auth.xboxlive.com\", \"TokenType\": \"JWT\" }";

            HttpRequest request = HttpRequest.newBuilder(xblUri)
                    .header("Content-Type", "application/json")
                    .header("Accept", "application/json")
                    .POST(HttpRequest.BodyPublishers.ofString(requestBody))
                    .build();

            HttpResponse<String> response = HttpClient.newBuilder().build().send(request, HttpResponse.BodyHandlers.ofString());
            if (response.statusCode() >= 200 && response.statusCode() < 300) { // HTTP Response codes starting with 2 are success responses
                String responseBody = response.body();
                xblResponse.set(gson.fromJson(responseBody, XBLToken.class));
            } else {
                MCUpdater.apiLogger.log(Level.WARNING, String.format("%d: %s\n====\n%s\n====\n%s",response.statusCode(), response.headers().toString(), response.body(), requestBody));
            }

        } catch (Exception e) {
            MCUpdater.apiLogger.log(Level.SEVERE, e.getMessage(), e);
        }
        return xblResponse.get();
    }

    public static XBLToken getXSTSAuth(String xblToken) {
        AtomicReference<XBLToken> xstsResponse = new AtomicReference<>(null);
        try {
            URI xstsUri = new URI(xstsUrl);

            String requestBody = "{\"Properties\": {\"SandboxId\": \"RETAIL\", \"UserTokens\": [\"" + xblToken + "\"]},\"RelyingParty\": \"rp://api.minecraftservices.com/\", \"TokenType\": \"JWT\" }";

            HttpRequest request = HttpRequest.newBuilder(xstsUri)
                    .header("Content-Type", "application/json")
                    .header("Accept", "application/json")
                    .POST(HttpRequest.BodyPublishers.ofString(requestBody))
                    .build();

            HttpResponse<String> response = HttpClient.newBuilder().build().send(request, HttpResponse.BodyHandlers.ofString());
            if (response.statusCode() >= 200 && response.statusCode() < 300) { // HTTP Response codes starting with 2 are success responses
                String responseBody = response.body();
                xstsResponse.set(gson.fromJson(responseBody, XBLToken.class));
            } else {
                //TODO: Handle XSTS Auth Failure
                MCUpdater.apiLogger.log(Level.WARNING, String.format("%d: %s\n====\n%s\n====\n%s",response.statusCode(), response.headers().toString(), response.body(), requestBody));
            }

        } catch (Exception e) {
            MCUpdater.apiLogger.log(Level.SEVERE, e.getMessage(), e);
        }
        return xstsResponse.get();
    }

    public static MCToken getMCToken(String userhash, String xstsToken) {
        AtomicReference<MCToken> mcToken = new AtomicReference<>(null);
        try {
            URI mcAuth = new URI(minecraftAuthUrl);

            String requestBody = String.format("{\"identityToken\" :\"XBL3.0 x=%s;%s\"}", userhash, xstsToken);

            HttpRequest request = HttpRequest.newBuilder(mcAuth)
                    .header("Content-Type", "application/json")
                    .header("Accept", "application/json")
                    .POST(HttpRequest.BodyPublishers.ofString(requestBody))
                    .build();

            HttpResponse<String> response = HttpClient.newBuilder().build().send(request, HttpResponse.BodyHandlers.ofString());
            if (response.statusCode() >= 200 && response.statusCode() < 300) { // HTTP Response codes starting with 2 are success responses
                String responseBody = response.body();
                mcToken.set(gson.fromJson(responseBody, MCToken.class));
            } else {
                //TODO: Handle MC Auth Failure
                MCUpdater.apiLogger.log(Level.WARNING, String.format("%d: %s\n====\n%s\n====\n%s",response.statusCode(), response.headers().toString(), response.body(), requestBody));
            }

        } catch (Exception e) {
            MCUpdater.apiLogger.log(Level.SEVERE, e.getMessage(), e);
        }
        return mcToken.get();
    }

    public static boolean isMinecraftOwned(String mcToken) {
        //List<String> validItems = Arrays.asList("product_minecraft","game_minecraft");
        AtomicBoolean owned = new AtomicBoolean(false);
        try {
            URI mcOwned = new URI(minecraftOwnedUrl);

            HttpRequest request = HttpRequest.newBuilder(mcOwned)
                    .header("Authorization", "Bearer " + mcToken)
                    .header("Accept", "application/json")
                    .GET()
                    .build();

            HttpResponse<String> response = HttpClient.newBuilder().build().send(request, HttpResponse.BodyHandlers.ofString());
            if (response.statusCode() >= 200 && response.statusCode() < 300) { // HTTP Response codes starting with 2 are success responses
                String responseBody = response.body();
                OwnershipResult ownershipResult = gson.fromJson(responseBody, OwnershipResult.class);
                owned.set(ownershipResult.getItems().length > 0);
            } else {
                //TODO: Handle ownership check failure
                MCUpdater.apiLogger.log(Level.WARNING, String.format("%d: %s\n====\n%s\n====\n%s",response.statusCode(), response.headers().toString(), response.body(), request.headers().toString()));
            }

        } catch (Exception e) {
            MCUpdater.apiLogger.log(Level.SEVERE, e.getMessage(), e);
        }
        return owned.get();
    }

    public static MCProfile getMinecraftProfile(String mcToken) {
        AtomicReference<MCProfile> mcProfile = new AtomicReference<>(null);
        try {
            URI mcProfileUri = new URI(minecraftProfileUrl);

            HttpRequest request = HttpRequest.newBuilder(mcProfileUri)
                    .header("Authorization", "Bearer " + mcToken)
                    .header("Accept", "application/json")
                    .GET()
                    .build();

            HttpResponse<String> response = HttpClient.newBuilder().build().send(request, HttpResponse.BodyHandlers.ofString());
            if (response.statusCode() >= 200 && response.statusCode() < 300) { // HTTP Response codes starting with 2 are success responses
                String responseBody = response.body();
                mcProfile.set(gson.fromJson(responseBody, MCProfile.class));
            } else {
                //TODO: Handle ownership check failure
                MCUpdater.apiLogger.log(Level.WARNING, String.format("%d: %s\n====\n%s\n====\n%s",response.statusCode(), response.headers().toString(), response.body(), request.headers().toString()));
            }

        } catch (Exception e) {
            MCUpdater.apiLogger.log(Level.SEVERE, e.getMessage(), e);
        }
        return mcProfile.get();
    }

    private static HttpRequest.BodyPublisher getFormBody(Map<String, Object> params) {
        StringBuilder stringBuilder = new StringBuilder();
        for (Map.Entry<String, Object> entry : params.entrySet()) {
            stringBuilder.append(stringBuilder.length() > 0 ? "&" : "").append(URLEncoder.encode(entry.getKey(), StandardCharsets.UTF_8)).append("=").append(URLEncoder.encode(entry.getValue().toString(), StandardCharsets.UTF_8));
        }
        return HttpRequest.BodyPublishers.ofString(stringBuilder.toString());
    }
}
