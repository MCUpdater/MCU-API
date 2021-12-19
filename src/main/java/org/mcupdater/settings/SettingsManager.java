package org.mcupdater.settings;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import org.mcupdater.util.MCUpdater;

import java.io.*;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;
import java.util.Properties;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicInteger;

public class SettingsManager {

	private static SettingsManager instance;
	private final List<SettingsListener> listeners = new CopyOnWriteArrayList<>();
	private final Gson gson = new GsonBuilder().registerTypeAdapter(Profile.class, new Profile.ProfileJsonHandler()).setPrettyPrinting().create();
	private Settings settings;
	private final Path configFile = MCUpdater.getInstance().getArchiveFolder().resolve("config.json");
	private AtomicBoolean dirty = new AtomicBoolean(false);
	private AtomicInteger delayCountdown = new AtomicInteger(0);
	private final Thread saveDelay = new Thread(() -> {
		while (true) {
			try {
				Thread.sleep(1000);
			} catch (InterruptedException e) {
				e.printStackTrace();
			}
			if (dirty.get()) {
				if (delayCountdown.decrementAndGet() == 0) {
					saveSettings();
				}
			}
		}
	});
	
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
		saveDelay.setDaemon(true);
		saveDelay.start();
	}

	public void loadSettings() {
		try {
			BufferedReader reader = Files.newBufferedReader(configFile, StandardCharsets.UTF_8);
			this.settings = gson.fromJson(reader, Settings.class);
			reader.close();
			if(this.settings.getJvmOpts().equals("")) {
				this.settings.setJvmOpts(MCUpdater.defaultJVMArgs);
			}
			Path jrePath = Paths.get(this.settings.getJrePath());
			if (!jrePath.toFile().exists()) {
				this.settings.setJrePath(System.getProperty("java.home"));
				MCUpdater.getInstance().getParent().alert("Java was not found at: " + jrePath.toString() + " JRE path has automatically been changed to: " + this.settings.getJrePath() + ".");
				saveSettings();
			}
			Path instancePath = Paths.get(this.settings.getInstanceRoot());
			if (!instancePath.toFile().exists()) {
				instancePath.toFile().mkdirs();
			}
			this.dirty.set(false);
			fireSettingsUpdate();
		} catch (IOException e) {
			e.printStackTrace();
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
		newSettings.setMinMemory(oldConfig.getProperty("minimumMemory",MCUpdater.defaultMemory));
		newSettings.setMaxMemory(oldConfig.getProperty("maximumMemory", MCUpdater.defaultMemory));
		newSettings.setPermGen(oldConfig.getProperty("permGen",MCUpdater.defaultPermGen));
		newSettings.setResWidth(Integer.parseInt(oldConfig.getProperty("width","1280")));
		newSettings.setResHeight(Integer.parseInt(oldConfig.getProperty("height","720")));
		newSettings.setFullScreen(false);
		newSettings.setJrePath(oldConfig.getProperty("jrePath",System.getProperty("java.home")));
		newSettings.setJvmOpts(oldConfig.getProperty("jvmOpts",MCUpdater.defaultJVMArgs));
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
		newSettings.setMinMemory(MCUpdater.defaultMemory);
		newSettings.setMaxMemory(MCUpdater.defaultMemory);
		newSettings.setPermGen(MCUpdater.defaultPermGen);
		newSettings.setResWidth(1280);
		newSettings.setResHeight(720);
		newSettings.setFullScreen(false);
		newSettings.setJrePath(System.getProperty("java.home"));
		newSettings.setJvmOpts(MCUpdater.defaultJVMArgs);
		newSettings.setInstanceRoot(MCUpdater.getInstance().getArchiveFolder().resolve("instances").toString());
		newSettings.setProgramWrapper("");
		newSettings.setTimeoutLength(5000);
		newSettings.setAutoConnect(true);
		newSettings.setMinimizeOnLaunch(true);
		newSettings.setMinecraftToConsole(true);
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
			this.dirty.set(false);
			fireSettingsUpdate();
		} catch (IOException e) {
			e.printStackTrace();
		}		
	}

	public boolean isDirty() {
		return this.dirty.get();
	}
	
	public void setDirty() {
		delayCountdown.set(10);
		this.dirty.set(true);
	}

	public void addListener(SettingsListener listener) {
		MCUpdater.apiLogger.finer(String.format("Added settings listener: %s%n",listener.getClass().toString()));
		listeners.add(listener);
	}

	@SuppressWarnings("UnusedDeclaration")
	public void removeListener(SettingsListener listener) {
		listeners.remove(listener);
	}
}
