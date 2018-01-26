package org.mcupdater.database;

import org.mcupdater.util.MCUpdater;

import java.nio.file.Path;
import java.sql.*;
import java.util.HashSet;
import java.util.Properties;
import java.util.Set;
import java.util.logging.Level;

public class DatabaseManager {

    private Connection conn;
    private String protocol = "jdbc:derby:";
    private Set<String> tables = new HashSet<>();

    public DatabaseManager(Path mcuRoot) {
        Properties dbCreds = new Properties();
        dbCreds.put("user","mcupdater");
        dbCreds.put("password","mcupdater");
        String dbName = mcuRoot.resolve("database").toString();
        try {
            conn = DriverManager.getConnection(protocol + dbName + ";create=true", dbCreds);
            DatabaseMetaData dbmeta = conn.getMetaData();
            MCUpdater.apiLogger.info("Database connection established");
            ResultSet rs = dbmeta.getTables(null,null,null, new String[]{"TABLE"});
            while (rs.next()) {
                String tableName = rs.getString("TABLE_NAME").toLowerCase();
                tables.add(tableName);
                MCUpdater.apiLogger.fine("Table: " + tableName);
            }
        } catch (SQLException e) {
            MCUpdater.apiLogger.log(Level.SEVERE, e.getMessage(), e);
        }
    }

    public boolean tableExists(String tableName) {
        return tables.contains(tableName.toLowerCase());
    }

    public Connection getConnection() {
        return conn;
    }

    public void shutdown() {
        try {
            DriverManager.getConnection("jdbc:derby:;shutdown=true");
        } catch (SQLException se) {
            if (( (se.getErrorCode() == 50000) && ("XJ015".equals(se.getSQLState()) ))) {
                MCUpdater.apiLogger.info("Database engine shut down normally.");
            } else {
                MCUpdater.apiLogger.severe("Database engine did not shut down properly!");
                SQLException e = se;
                while (e != null) {
                    MCUpdater.apiLogger.severe("--- SQLException ---");
                    MCUpdater.apiLogger.severe("SQL State: " + e.getSQLState());
                    MCUpdater.apiLogger.severe("Error Code: " + e.getErrorCode());
                    MCUpdater.apiLogger.severe("Message: " + e.getMessage());
                    e = e.getNextException();
                }
            }
        }
    }
}
