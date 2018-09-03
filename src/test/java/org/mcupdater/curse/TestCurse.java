package org.mcupdater.curse;

import org.apache.commons.io.IOUtils;
import org.mcupdater.model.CurseProject;
import org.mcupdater.util.CurseModCache;
import org.mcupdater.util.MCUpdater;

import javax.net.ssl.HttpsURLConnection;
import java.io.*;
import java.net.URI;
import java.net.URL;
import java.net.URLConnection;

public class TestCurse {

	public static void main(String[] args) {
		try {
			URLConnection conn = (new URL("https://minecraft.curseforge.com/api/game/versions")).openConnection();
			conn.addRequestProperty("X-Api-Token","a98e4aa8-f43e-4c6a-b245-70327d9c2f85");
			StringWriter writer = new StringWriter();
			IOUtils.copy(conn.getInputStream(),writer, "UTF8");
			System.out.println(writer.toString());
		} catch (IOException e) {
			e.printStackTrace();
		}

	}
}
