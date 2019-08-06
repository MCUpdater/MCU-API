package org.mcupdater.loaders;

import org.mcupdater.api.Version;
import org.mcupdater.downloadlib.Downloadable;
import org.mcupdater.model.Loader;
import org.mcupdater.model.ModSide;
import org.mcupdater.model.PrioritizedURL;
import org.mcupdater.mojang.Library;
import org.mcupdater.mojang.MinecraftVersion;
import org.mcupdater.settings.SettingsManager;
import org.mcupdater.util.MCUpdater;

import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.URL;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.logging.Level;

import static java.lang.Thread.sleep;

public class ForgeLoader implements ILoader {

	private final String FORGE_BASE = "http://files.minecraftforge.net/maven/net/minecraftforge/forge/";
	private final Loader loader;

	public ForgeLoader(Loader loader) {
		this.loader = loader;
	}

	@Override
	public boolean install(Path installPath, ModSide side) {
		try {
			Path javaPath = getJava();
			Path mcuPath = MCUpdater.getInstance().getArchiveFolder();
			File tmp = null;
			List<PrioritizedURL> downloadUrls = new ArrayList<>();
			downloadUrls.add(new PrioritizedURL(FORGE_BASE + loader.getVersion() + "/forge-" + loader.getVersion() + "-installer.jar", 0));
			URL finalUrl;
			for (PrioritizedURL url : downloadUrls) {
				try {
					File tempFolder = MCUpdater.getInstance().getArchiveFolder().resolve("temp").toFile();
					tempFolder.mkdirs();
					tmp = File.createTempFile("installer", ".jar", tempFolder);
					finalUrl = new URL(url.getUrl());
					System.out.println("Temp file: " + tmp.getName());
					Downloadable downloadable = new Downloadable("installer.jar", tmp.getName(), "force", 0, new ArrayList<>(Collections.singleton(finalUrl)));
					downloadable.download(tempFolder, MCUpdater.getInstance().getArchiveFolder().resolve("cache").toFile());
					tmp.deleteOnExit();
					if (Files.size(tmp.toPath()) == 0) {
						System.out.println("!! got zero bytes from " + url);
					}
				} catch(IOException e){
					System.out.println("!! Unable to download " + url);
					e.printStackTrace();
				}
			}
			List<String> args = new ArrayList<>();
			args.add(javaPath.toString());
			args.add("-cp");
			String loaderLib;
			loaderLib = Version.requestedFeatureLevel(loader.getVersion().split("-")[0], "1.13") ? "MCU-ForgeLoader.jar" : "MCU-LegacyForgeLoader.jar";
			args.add(mcuPath.resolve("lib").resolve(loaderLib).toString() + System.getProperty("path.separator") + tmp.getAbsolutePath());
			args.add("org.mcupdater.forgeloader.ForgeLoader");
			args.add(installPath.toAbsolutePath().toString());
			args.add(side.toString());
			final ProcessBuilder pb = new ProcessBuilder(args);
			pb.directory(installPath.toFile());
			pb.redirectErrorStream(true);
			final File instancePath = installPath.toFile();
			final Thread installThread = new Thread(() -> {
				try {
					Process task = pb.start();
					BufferedReader buffRead = new BufferedReader(new InputStreamReader(task.getInputStream()));
					String line;
					while ((line = buffRead.readLine()) != null) {
						if (line.length() > 0) {
							MCUpdater.apiLogger.info("[ForgeLoader] " + line);
						}
					}
					File libPath = new File(instancePath, "libraries");
					MinecraftVersion forgeVersion = MinecraftVersion.loadLocalVersion(instancePath, getVersionFilename());
					for (Library lib : forgeVersion.getLibraries()) {
						if (lib.validForOS()) {
							if (!new File(libPath, lib.getFilename()).exists()) {
								Downloadable downloadable = new Downloadable(lib.getName(), lib.getFilename(), "force", 0, new ArrayList<>(Collections.singleton(new URL(lib.getDownloadUrl()))));
								downloadable.download(libPath, MCUpdater.getInstance().getArchiveFolder().resolve("cache").toFile());
								System.out.println(lib.getFilename());
							}
						}
					}
				} catch (IOException e) {
					MCUpdater.apiLogger.log(Level.SEVERE, "[ForgeLoader] " + e.getMessage(), e);
				}
			});
			installThread.start();
			while(installThread.isAlive()) {
				sleep(500);
			}
			MCUpdater.apiLogger.log(Level.INFO, "[ForgeLoader] Forge " + this.loader.getVersion() + " installed!");
			return true;
		} catch (Exception e) {
			MCUpdater.apiLogger.log(Level.SEVERE, e.getMessage(), e);
			return false;
		}
	}

	private String getVersionFilename() {
		return Version.requestedFeatureLevel(loader.getVersion().split("-")[0], "1.13") ? this.loader.getVersion().replace("-","-forge-") : this.loader.getVersion().split("-")[0] + "-forge" + this.loader.getVersion();
	}

	@Override
	public List<String> getClasspathEntries(File instancePath) {
		List<String> libs = new ArrayList<>();
		MinecraftVersion forgeVersion = MinecraftVersion.loadLocalVersion(instancePath, getVersionFilename());
		System.out.println(forgeVersion);
		for (Library lib : forgeVersion.getLibraries()) {
			if (lib.validForOS() && !lib.hasNatives()) {
				libs.add("libraries/" + lib.getFilename());
			}
		}
		return libs;
	}

	@Override
	public String getArguments(File instancePath) {
		MinecraftVersion forgeVersion = MinecraftVersion.loadLocalVersion(instancePath, getVersionFilename());
		return " " + forgeVersion.getEffectiveArguments();
	}

	private Path getJava() throws Exception {
		Path javaFile;
		if (System.getProperty("os.name").startsWith("Win")) {
			javaFile = new File(SettingsManager.getInstance().getSettings().getJrePath()).toPath().resolve("bin").resolve("javaw.exe");
		} else {
			javaFile = new File(SettingsManager.getInstance().getSettings().getJrePath()).toPath().resolve("bin").resolve("java");
		}
		if (Files.exists(javaFile)) {
			return javaFile;
		} else {
			throw new Exception("Java executable not found at specified JRE path!");
		}
	}

	@Override
	public String getMainClassClient() {
		if (Version.requestedFeatureLevel(this.loader.getVersion().split("-")[0],"1.13")) {
			return "cpw.mods.modlauncher.Launcher";
		} else {
			return "net.minecraft.launchwrapper.Launch";
		}
	}

}
