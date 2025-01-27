package org.mcupdater.install;

import com.google.gson.Gson;
import org.mcupdater.FMLStyleFormatter;
import org.mcupdater.MCUApp;
import org.mcupdater.api.Install;
import org.mcupdater.auth.TokenResponse;
import org.mcupdater.downloadlib.DownloadQueue;
import org.mcupdater.downloadlib.Downloadable;
import org.mcupdater.downloadlib.TrackerListener;
import org.mcupdater.instance.Instance;
import org.mcupdater.model.Module;
import org.mcupdater.model.*;
import org.mcupdater.mojang.AssetManager;
import org.mcupdater.mojang.MinecraftVersion;
import org.mcupdater.settings.MSAProfile;
import org.mcupdater.settings.Profile;
import org.mcupdater.util.MCUpdater;
import org.mcupdater.util.ServerPackParser;

import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.*;
import java.util.logging.*;

import static org.mcupdater.model.ModSide.CLIENT;
import static org.mcupdater.model.ModSide.SERVER;

public class TestServerInstall extends MCUApp implements TrackerListener {
	// Configuration section
	public static final String SOURCE_URL = "https://files.mcupdater.com/smbarbour/test-1.21.1.xml";
	public static final String SERVER_ID = "mcu-test";
	public static final String ROOT_PATH = "c:\\temp\\MCU-InstallServerTest";
	//

	private final static Gson gson = new Gson();
	private static TestServerInstall instance;
	private Deque<DownloadQueue> queues = new ArrayDeque<>();

	public static void main(String[] args) {
		System.setProperty("java.net.preferIPv4Stack", "true");
		Path installPath = new File(ROOT_PATH).toPath().resolve(SERVER_ID);
		MCUpdater.getInstance(new File(ROOT_PATH));
		instance = new TestServerInstall();
		MCUpdater.getInstance().setParent(instance);
		try {
			MCUpdater.getInstance().downloadLoaders();
		} catch (IOException e) {
			e.printStackTrace();
		}
		ServerList testPack = ServerPackParser.loadFromURL(SOURCE_URL, SERVER_ID);
		ModSide side = SERVER;
		List<GenericModule> modList = new ArrayList<>();
		List<ConfigFile> configs = new ArrayList<>();
		Instance instData;
		final Path instanceFile = installPath.resolve("instance.json");
		try {
			BufferedReader reader = Files.newBufferedReader(instanceFile, StandardCharsets.UTF_8);
			instData = gson.fromJson(reader, Instance.class);
			reader.close();
		} catch (IOException ioe) {
			instData = new Instance();
		}
		Set<String> digests = new HashSet<>();
		List<Module> fullModList = new ArrayList<>();
		fullModList.addAll(testPack.getModules().values());
		fullModList.forEach(entry -> {
			if (!entry.getMD5().isEmpty()) {
				digests.add(entry.getMD5());
			}
			entry.getConfigs().forEach(config -> {
				if (!config.getMD5().isEmpty()) {
					digests.add(config.getMD5());
				}
			});
			entry.getSubmodules().forEach(submodule -> {
				if (!submodule.getMD5().isEmpty()) {
					digests.add(submodule.getMD5());
				}
			});
		});
		instData.setHash(MCUpdater.calculateGroupHash(digests));
		if (instData.getOptionalMods() == null) {
			instData.setOptionalMods(new HashMap<>());
		}

		Instance finalInstData = instData;
		testPack.getModules().forEach((modId, mod) -> {
			if (mod.isSideValid(side)) {
				if (mod.getRequired() || ((finalInstData.getOptionalMods().containsKey(modId)) ? finalInstData.getOptionalMods().get(modId) : mod.getIsDefault())) {
					modList.add(mod);
					if (mod.hasSubmodules()) {
						mod.getSubmodules().stream().filter(submodule -> submodule.isSideValid(side)).forEach(submodule -> modList.add(submodule));
					}
					if (mod.hasConfigs()) {
						configs.addAll(mod.getConfigs());
					}
				}
			}
		});
		try {
			Install install = new Install(testPack, modList, configs);
			install.doInstall(installPath, false, instData, side);
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	private TestServerInstall() {
		this.baseLogger = Logger.getLogger("MCUpdater");
		this.baseLogger.setLevel(Level.ALL);
		try {
			ConsoleHandler consoleHandler = new ConsoleHandler();
			consoleHandler.setFormatter(new SimpleFormatter());
			FileHandler fileHandler = new FileHandler(new File(ROOT_PATH).toPath().resolve("test.log").toString(), 0, 1);
			fileHandler.setFormatter(new FMLStyleFormatter());
			this.baseLogger.addHandler(consoleHandler);
			this.baseLogger.addHandler(fileHandler);
		} catch (SecurityException | IOException e) {
			e.printStackTrace();
		}
	}

	@Override
	public void setStatus(String string) {
		log("Status: " + string);
	}

	@Override
	public void log(String msg) {
		baseLogger.info(msg);
	}

	@Override
	public Profile requestLogin(String username) {
		return null;
	}

	@Override
	public DownloadQueue submitNewQueue(String queueName, String parent, Collection<Downloadable> files, File basePath, File cachePath) {
		DownloadQueue newQueue = new DownloadQueue(queueName, parent, this, files, basePath, cachePath, this.baseLogger);
		queues.add(newQueue);
		return newQueue;
	}

	@Override
	public DownloadQueue submitAssetsQueue(String queueName, String parent, MinecraftVersion version) {
		DownloadQueue newQueue = AssetManager.downloadAssets(queueName, parent, MCUpdater.getInstance().getArchiveFolder().resolve("assets").toFile(), this, version);
		queues.add(newQueue);
		return newQueue;
	}

	@Override
	public void alert(String msg) {
		baseLogger.warning(msg);
	}

	@Override
	public TokenResponse refreshAuth(MSAProfile msaProfile) {
		return null;
	}

	@Override
	public void onQueueFinished(DownloadQueue queue) {
		baseLogger.log(Level.INFO,"{0} has finished.", queue.getName());
		queues.remove(queue);
		if (queues.isEmpty()) {
			baseLogger.log(Level.INFO,"All download queues have completed.");
		}
	}

	@Override
	public void onQueueProgress(DownloadQueue queue) {
		for (DownloadQueue active : queues) {
			System.out.print(active.getName() + ": " + active.getProgress() * 100.00F + " / ");
		}
		System.out.print("\r");
	}

	@Override
	public void printMessage(String msg) {

	}
}
