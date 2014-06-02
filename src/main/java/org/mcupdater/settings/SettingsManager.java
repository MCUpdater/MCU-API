package org.mcupdater.settings;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import org.mcupdater.util.MCUpdater;

import java.io.*;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;
import java.util.Properties;
import java.util.concurrent.CopyOnWriteArrayList;

public class SettingsManager {

	private static SettingsManager instance;
	private final List<SettingsListener> listeners = new CopyOnWriteArrayList<>();
	private final Gson gson = new GsonBuilder().setPrettyPrinting().create();
	private Settings settings;
	private final Path configFile = MCUpdater.getInstance().getArchiveFolder().resolve("config.json");
	private boolean dirty = false;
	
	public SettingsManager() {
		if (!configFile.toFile().exists()) {
			System.out.println("New config file does not exist!");
			File oldConfig = MCUpdater.getInstance().getArchiveFolder().resolve("config.properties").toFile();
			if (oldConfig.exists()) {
				System.out.println("Importing old config file");
				this.settings = convertOldSettings(oldConfig);
			} else {
				System.out.println("Creating default config");
				this.settings = getDefaultSettings();  
			}
			saveSettings();
			return;
		}
		System.out.println("Loading config");
		loadSettings();
	}

	public void loadSettings() {
		try {
			BufferedReader reader = Files.newBufferedReader(configFile, StandardCharsets.UTF_8);
			this.settings = gson.fromJson(reader, Settings.class);
			reader.close();
			this.dirty=false;
			fireStateUpdate();
			fireSettingsUpdate();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	private void fireStateUpdate() {
		for (SettingsListener listener : listeners) {
			listener.stateChanged(this.isDirty());
		}
	}

	public void fireSettingsUpdate() {
		for (SettingsListener listener : listeners) {
			listener.settingsChanged(this.settings);
		}
	}

	public void reload() {
		loadSettings();
	}
	
	private Settings convertOldSettings(File oldConfigFile) {
		Settings newSettings = new Settings();
		Properties oldConfig = new Properties();
		try {
			oldConfig.load(new FileInputStream(oldConfigFile));
		} catch (IOException e) {
			e.printStackTrace();
		}
		newSettings.setMinMemory(oldConfig.getProperty("minimumMemory","1G"));
		newSettings.setMaxMemory(oldConfig.getProperty("maximumMemory","1G"));
		newSettings.setPermGen(oldConfig.getProperty("permGen","128M"));
		newSettings.setResWidth(Integer.parseInt(oldConfig.getProperty("width","1280")));
		newSettings.setResHeight(Integer.parseInt(oldConfig.getProperty("height","720")));
		newSettings.setFullScreen(false);
		newSettings.setJrePath(oldConfig.getProperty("jrePath",System.getProperty("java.home")));
		newSettings.setJvmOpts(oldConfig.getProperty("jvmOpts","-XX:+UseConcMarkSweepGC -XX:+CMSIncrementalMode -XX:+AggressiveOpts"));
		newSettings.setInstanceRoot(oldConfig.getProperty("instanceRoot",MCUpdater.getInstance().getArchiveFolder().resolve("instances").toString()));
		newSettings.setProgramWrapper(oldConfig.getProperty("jvmContainer",""));
		newSettings.setTimeoutLength(Integer.parseInt(oldConfig.getProperty("timeoutLength","5000")));
		newSettings.setAutoConnect(Boolean.parseBoolean(oldConfig.getProperty("allowAutoConnect","true")));
		newSettings.setMinimizeOnLaunch(Boolean.parseBoolean(oldConfig.getProperty("minimizeOnLaunch","true")));
		Path oldServers = MCUpdater.getInstance().getArchiveFolder().resolve("mcuServers.dat");
		if (oldServers.toFile().exists()){
			try {
				BufferedReader reader = Files.newBufferedReader(oldServers, StandardCharsets.UTF_8);
				String entry = reader.readLine();
				while(entry != null) {
					newSettings.addPackURL(entry);
					entry = reader.readLine();
				}
				reader.close();
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
		return newSettings;
	}

	public Settings getDefaultSettings() {
		Settings newSettings = new Settings();
		newSettings.setMinMemory("1G");
		newSettings.setMaxMemory("1G");
		newSettings.setPermGen("128M");
		newSettings.setResWidth(1280);
		newSettings.setResHeight(720);
		newSettings.setFullScreen(false);
		newSettings.setJrePath(System.getProperty("java.home"));
		newSettings.setJvmOpts("-XX:+UseG1GC -XX:+AggressiveOpts");
		newSettings.setInstanceRoot(MCUpdater.getInstance().getArchiveFolder().resolve("instances").toString());
		newSettings.setProgramWrapper("");
		newSettings.setTimeoutLength(5000);
		newSettings.setAutoConnect(true);
		newSettings.setMinimizeOnLaunch(true);
		return newSettings;
	}

	public Settings getSettings() {
		return settings;
	}

	public void setSettings(Settings newSettings) {
		this.settings = newSettings;
		this.setDirty();
		fireSettingsUpdate();
	}

	public static SettingsManager getInstance() {
		if (instance == null) {
			instance = new SettingsManager();
		}
		return instance;
	}

	public void saveSettings() {
		String jsonOut = gson.toJson(this.settings);
		try {
			BufferedWriter writer = Files.newBufferedWriter(configFile, StandardCharsets.UTF_8);
			writer.append(jsonOut);
			writer.close();
			this.dirty = false;
			fireStateUpdate();
		} catch (IOException e) {
			e.printStackTrace();
		}		
	}

	public boolean isDirty() {
		return this.dirty;
	}
	
	public void setDirty() {
		this.dirty = true;
		fireStateUpdate();
	}

	public void addListener(SettingsListener listener) {
		listeners.add(listener);
	}

	@SuppressWarnings("UnusedDeclaration")
	public void removeListener(SettingsListener listener) {
		listeners.remove(listener);
	}
}
