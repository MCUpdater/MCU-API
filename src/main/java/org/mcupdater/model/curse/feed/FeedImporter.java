package org.mcupdater.model.curse.feed;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import org.apache.commons.compress.compressors.bzip2.BZip2CompressorInputStream;
import org.mcupdater.database.DatabaseManager;
import org.mcupdater.mojang.LowerCaseEnumTypeAdapterFactory;
import org.mcupdater.util.MCUpdater;

import java.io.IOException;
import java.io.InputStreamReader;
import java.net.URL;
import java.net.URLConnection;
import java.sql.Connection;
import java.sql.SQLException;

public class FeedImporter {
    private static String COMPLETE_FEED = "http://clientupdate-v6.cursecdn.com/feed/addons/432/v10/complete.json.bz2";
    private static String HOURLY_FEED = "http://clientupdate-v6.cursecdn.com/feed/addons/432/v10/hourly.json.bz2";

    public static Feed getFeed(boolean complete) {
        GsonBuilder builder = new GsonBuilder();
        builder.registerTypeAdapterFactory(new LowerCaseEnumTypeAdapterFactory());
        builder.enableComplexMapKeySerialization();
        Gson gson = builder.create();

        URLConnection conn;
        try {
            conn = (new URL(complete ? COMPLETE_FEED : HOURLY_FEED)).openConnection();
            BZip2CompressorInputStream bzIn = new BZip2CompressorInputStream(conn.getInputStream());
            return gson.fromJson(new InputStreamReader(bzIn),Feed.class);
        } catch (IOException e) {
            e.printStackTrace();
        }
        return null;
    }

    public static void createTables() {
    	try {
		    Connection conn = MCUpdater.getInstance().getDbManager().getConnection();
		    conn.prepareStatement("CREATE TABLE");
	    } catch (SQLException e) {
		    e.printStackTrace();
	    }
    }
}
