package org.mcupdater.api;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import org.apache.commons.lang3.StringUtils;
import org.mcupdater.MCUApp;
import org.mcupdater.downloadlib.DownloadQueue;
import org.mcupdater.downloadlib.Downloadable;
import org.mcupdater.instance.Instance;
import org.mcupdater.model.*;
import org.mcupdater.mojang.*;
import org.mcupdater.util.Archive;
import org.mcupdater.util.DownloadCache;
import org.mcupdater.util.MCUpdater;

import java.io.BufferedWriter;
import java.io.File;
import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;
import java.util.*;
import java.util.logging.Level;
import java.util.logging.Logger;

public class Install {
	private final Gson gson = new GsonBuilder().setPrettyPrinting().create();
	private final ServerList server;
	private final List<GenericModule> installList;
	private final List<ConfigFile> configList;
	private final MinecraftVersion mcVersion;
	private final MCUApp parent = MCUpdater.getInstance().getParent();
	private Path clientJar;
	private Path serverJar;
	private Path targetJar;
	private DownloadQueue assetsQueue = null;
	private DownloadQueue jarQueue = null;
	private DownloadQueue generalQueue = null;
	private DownloadQueue libraryQueue = null;
	private Map<String,Boolean> metaRebuild = new TreeMap<>();
	private Logger logger = MCUpdater.apiLogger;
	private Downloadable baseJar;
	private File tmpFolder;

	Runnable postProcessJar = () -> {
		File buildJar = MCUpdater.getInstance().getArchiveFolder().resolve("build.jar").toFile();
		if (buildJar.exists()) buildJar.delete();
		parent.log("Extracting files for jar insertion");
		metaRebuild.entrySet().forEach(entry -> {
			File entryFile = new File(tmpFolder,entry.getKey());
			Archive.extractZip(entryFile, tmpFolder, entry.getValue());
			entryFile.delete();
		});
		try {
			buildJar.createNewFile();
		} catch (IOException e) {
			MCUpdater.apiLogger.log(Level.SEVERE, "I/O Error", e);
		}
		List<File> buildList = recurseFolder(tmpFolder, true);
		boolean doManifest = buildList.stream().noneMatch(entry -> entry.getPath().contains("META-INF"));
		if (tmpFolder.listFiles().length > 0) {
			parent.log("Packaging updated jar...");
			try {
				Archive.createJar(buildJar, buildList, tmpFolder.getPath() + System.getProperty("file. separator"), doManifest);
			} catch (IOException e) {
				parent.log("Failed to create jar!");
				MCUpdater.apiLogger.log(Level.SEVERE, "I/O Error", e);
			}
			try {
				Files.createDirectories(targetJar.getParent());
				Files.copy(buildJar.toPath(), targetJar, StandardCopyOption.REPLACE_EXISTING);
			} catch (IOException e) {
				MCUpdater.apiLogger.log(Level.SEVERE, "Failed to copy new jar to instance!", e);
			}
			parent.log("Jar build/update complete");
		}
		recurseFolder(tmpFolder, true).stream().forEach(file -> file.delete());
	};

	public Install(ServerList server, List<GenericModule> toInstall, List<ConfigFile> configs) {
		this.server = server;
		this.installList = toInstall;
		this.configList = configs;
		this.mcVersion = MinecraftVersion.loadVersion(server.getVersion());
	}

	public boolean doInstall(Path instancePath, boolean clearExisting, Instance instData, ModSide side) throws Exception {
		tmpFolder = instancePath.resolve("temp" + (new Random()).nextInt(100)).toFile();
		tmpFolder.mkdirs();
		if (side.equals(ModSide.BOTH)) {
			logger.severe("Invalid API call: Side cannot be BOTH");
			return false;
		}
		if (clearExisting) {
			List<File> contents = recurseFolder(instancePath.toFile(), true);
			parent.setStatus("Clearing existing instance");
			logger.info("Clearing existing instance");
			contents.removeIf(entry -> checkExclusion(entry.getPath()));
			contents.forEach(file -> {
				logger.fine("Deleting: " + file.getAbsolutePath());
				file.delete();
			});
		}
		Collections.sort(installList, new ModuleComparator(ModuleComparator.Mode.IMPORTANCE));
		if (side.equals(ModSide.CLIENT)) {
			clientJar = instancePath.resolve("bin").resolve("minecraft.jar");
			prepareClient(instancePath);
			// Check: Does jar exist, does it match the necessary version, do any mods need to be inserted into the jar
			if (jarBuildNeeded(clientJar, instData))  {
				// Build Jar
				// TODO - Implement
				targetJar = clientJar;
				DownloadInfo downloadInfo = mcVersion.getDownloadInfo(DownloadType.CLIENT);
				List<URL> jarUrls = new ArrayList<>();
				if (downloadInfo != null) {
					jarUrls.add(downloadInfo.getUrl());
					Set<Downloadable> jarFiles = new HashSet<>();
					jarFiles.add(new Downloadable("Minecraft jar", "0.jar", Downloadable.HashAlgorithm.SHA1, downloadInfo.getSha1(), downloadInfo.getSize(), jarUrls));
					metaRebuild.put("0.jar", Version.requestedFeatureLevel(this.server.getVersion(), "1.6"));
					installList.stream().filter(mod -> mod.getModType() == ModType.Jar).forEach(mod -> {
						jarFiles.add(new Downloadable(mod.getName(), mod.getJarOrder() + "-" + cleanForFile(mod.getId()) + ".jar",mod.getMD5(),mod.getFilesize(),mod.getUrls()));
						metaRebuild.put(mod.getJarOrder() + "-" + cleanForFile(mod.getId()) + ".jar", mod.getKeepMeta());
						instData.addJarMod(mod.getId(), mod.getMD5());
					});
					jarQueue = parent.submitNewQueue("Jar build files", server.getServerId(), jarFiles, tmpFolder, DownloadCache.getDir());
					jarQueue.processQueue(2, postProcessJar);
				}
			}
			if (server.isGenerateList()) MCUpdater.getInstance().writeMCServerFile(instancePath, server.getName(), server.getAddress());
		} else {
			serverJar = instancePath.resolve("minecraft_server." + server.getVersion() + ".jar");
			prepareServer(instancePath);
		}
		instData.setMCVersion(server.getVersion());
		instData.setRevision(server.getRevision());
		instData.setPackName(server.getName());
		instData.setPackId(server.getServerId());
		String jsonOut = gson.toJson(instData);
		try {
			BufferedWriter writer = Files.newBufferedWriter(instancePath.resolve("instance.json"), StandardCharsets.UTF_8);
			writer.append(jsonOut);
			writer.close();
		} catch (IOException e) {
			MCUpdater.apiLogger.log(Level.SEVERE, "I/O error", e);
		}
		return false;
	}

	private boolean checkExclusion(String path) {
		String sep = System.getProperty("file.separator");
		if (
				path.contains("minecraft.jar") || // Minecraft Jar
				(path.contains("mods") && (path.contains(".zip") || path.contains(".jar"))) // Mods
		) return false;
		if (
				(path.contains("resources") && !path.contains("mods")) || // Resources folder except under mods
				path.contains("lib" + sep) || // lib folder
				path.contains("saves") || // saves folder
				path.contains("screenshots") || // screenshots folder
				path.contains("stats") ||
				(path.contains("texturepacks") || path.contains("resourcepacks") || path.contains("shaderpacks")) || // texutres, resources, shaders
				path.contains("lastlogin") ||
				path.contains("servers.dat") || // Server list
				path.contains("options.txt") || // Minecraft options
						// Client mod specific files
				path.contains("rei_minimap") || path.contains("macros") || path.contains("InvTweaks") || path.contains("optionsof.txt") || path.contains("voxelMap") || path.contains("journeymap")
		) return true;

		// Everything else
		return false;
	}

	private List<File> recurseFolder(File file, boolean includeFolders) {
		//TODO - Implement
		return null;
	}

	private boolean jarBuildNeeded(Path sourceJar, Instance instance) {
		return (
				!sourceJar.toFile().exists() || // Jar does not already exist or...
				(instance.getMCVersion() == null || !instance.getMCVersion().equals(server.getVersion())) || // Jar does not match installed version or...
				(
						this.installList.stream().anyMatch(mod -> mod.getModType() == ModType.Jar) && // Mods need to go in jar ...and
						instance.getJarMods().size() != this.installList.stream().filter(mod -> mod.getModType() == ModType.Jar).count() // the number does not match what is in the jar
				)
		);
	}

	private void prepareClient(Path instancePath) {
		logger.finer("Overrides: " + server.getLibOverrides().size());
		server.getLibOverrides().forEach((key, value) -> logger.finer(key + ": " + value));
		assetsQueue = parent.submitAssetsQueue("Assets", server.getServerId(), this.mcVersion);
		List<Library> libraries = mcVersion.getLibraries();
		Set<Downloadable> libDownloads = new HashSet<>();
		final List<String> toExtract = new ArrayList<>();
		libraries.forEach(library -> {
			Downloadable entry = processLibrary(library);
			if (entry != null) {
				libDownloads.add(entry);
				if (entry.getFriendlyName().contains("natives")) {
					logger.info(String.format("Will extract: %s", entry.getFilename()));
					toExtract.add(library.getFilename());
				}
			}
		});
	}
	private void prepareServer(Path instancePath) {
		//TODO - Implement
	}

	private Downloadable processLibrary(Library library) {
		if (!library.validForOS()) { // Ignore if not valid for OS
			return null;
		}
		String key = StringUtils.join(Arrays.copyOfRange(library.getName().split(":"), 0, 2), ":");
		logger.finer(library.getName() + " = " + key);
		if (server.getLibOverrides().containsKey(key)) {
			library.setName(server.getLibOverrides().get(key));
			logger.finer("Replaced library: " + library.getName());
		}
		List<URL> urls = new ArrayList<>();
		try {
			urls.add(new URL(library.getDownloadUrl()));
		} catch (MalformedURLException e) {
			logger.log(Level.SEVERE, "Bad URL", e);
		}
		Downloadable entry = null;
		if (library.getDownloads() == null) {
			entry = new Downloadable(library.getName(), library.getFilename(), "", 100000, urls);
		} else {
			if (library.getDownloads().getClassifiers() != null && library.getDownloads().getClassifiers().getNatives() != null) {
				Artifact natives = library.getDownloads().getClassifiers().getNatives();
				if (natives != null) {
					try {
						entry = new Downloadable(library.getName() + "-natives", natives.getPath(), Downloadable.HashAlgorithm.SHA1, natives.getSha1(), natives.getSize(), Collections.singletonList(new URL(natives.getUrl())));
					} catch (MalformedURLException e) {
						logger.log(Level.SEVERE, "Bad URL - natives", e);
						entry = null;
					}
				} else {
					if (library.getDownloads() != null && library.getDownloads().getArtifact() != null)
						entry = new Downloadable(library.getName(), library.getFilename(), Downloadable.HashAlgorithm.SHA1, library.getDownloads().getArtifact().getSha1(), library.getDownloads().getArtifact().getSize(), urls);
				}
			}
		}
		if (entry != null) {
			return entry;
		}
		return null;
	}

	private String cleanForFile(String id) {
		return id.replaceAll("[^a-zA-Z_0-9\\-.]", "_");
	}

}
