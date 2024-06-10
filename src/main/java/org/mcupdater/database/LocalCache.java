package org.mcupdater.database;

import org.mcupdater.util.MCUpdater;

import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.logging.Level;

public class LocalCache {
	public static void initDB(DatabaseManager dbManager) {
		try {
			Connection conn = dbManager.getConnection();
			Statement statement = conn.createStatement();
			if (!dbManager.tableExists("cf_files")) {
				statement.executeUpdate("CREATE TABLE cf_files (fileid INT NOT NULL PRIMARY KEY, url varchar(500))");
			}
		} catch (SQLException e) {
			MCUpdater.apiLogger.log(Level.SEVERE, "Database error", e);
		}
	}

	public static void saveFileCF(Integer fileId, String url){
		try {
			Statement sqlStatement = MCUpdater.getInstance().getDbManager().getConnection().createStatement();
			sqlStatement.executeUpdate("INSERT INTO cf_files (fileid, url) VALUES (" + fileId + ",'" + url + "')");
		} catch (SQLException e) {
			MCUpdater.apiLogger.log(Level.SEVERE,"Database error", e);
		}
	}

	public static String lookupCF(Integer cfFile) {
		try {
			Statement sqlStatement = MCUpdater.getInstance().getDbManager().getConnection().createStatement();
			ResultSet resultset = sqlStatement.executeQuery("SELECT url from cf_files where fileid = " + cfFile);
			while (resultset.next()) {
				return resultset.getString(1);
			}
		} catch (SQLException e) {
			MCUpdater.apiLogger.log(Level.SEVERE, "Database error", e);
		}
		return "";
	}
}
