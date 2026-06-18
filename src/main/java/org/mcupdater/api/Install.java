package org.mcupdater.api;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import org.apache.commons.io.FileUtils;
import org.apache.commons.lang3.StringUtils;
import org.mcupdater.MCUApp;
import org.mcupdater.downloadlib.DownloadQueue;
import org.mcupdater.downloadlib.Downloadable;
import org.mcupdater.instance.Instance;
import org.mcupdater.model.v2.*;
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
import java.util.concurrent.atomic.AtomicInteger;
import java.util.logging.Level;
import java.util.logging.Logger;

public class Install {
	private final Gson gson = new GsonBuilder().setPrettyPrinting().create();
	private ServerList server;
	private final List<GenericModule> installList;
	private final List<ConfigFile> configList;
	private MinecraftVersion mcVersion;
	private final MCUApp parent = MCUpdater.getInstance().getParent();
	private final List<String> toExtract = new ArrayList<>();
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
	private Path instancePath;
	private ModSide side;
	private Map<String, Boolean> modExtract = new HashMap<>();

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
		recurseFolder(tmpFolder, true).forEach(File::delete);
	};

	Runnable postProcessLibraries = () -> {
		if (this.server != null) {
			if (!toExtract.isEmpty()) {
				logger.log(Level.INFO, "Extracting {0} library files", toExtract.size());
				toExtract.forEach(entry -> Archive.extractZip(instancePath.resolve(entry).toFile(), instancePath.resolve("libraries").resolve("natives").toFile(), false));
				logger.log(Level.INFO, "Library file extraction complete");
			}
			server.getLoaders().sort(new OrderComparator());
			server.getLoaders().forEach(loader -> loader.getILoader().install(instancePath, side));
		}
	};

	Runnable postProcessGeneral = () -> {
		if (!modExtract.isEmpty()) {
			logger.log(Level.INFO, "Performing {0} extraction(s)", modExtract.size());
			modExtract.forEach((filename, inRoot) -> {
				Archive.extractZip(instancePath.resolve(filename).toFile(), inRoot ? instancePath.toFile() : instancePath.resolve("mods").toFile());
				boolean success = instancePath.resolve(filename).toFile().delete();
				logger.log(Level.FINEST, "{0} deleted: {1}", new Object[]{filename, success});
			});
			logger.log(Level.INFO, "Extractions complete");
		}
	};

	Runnable postProcessAssets = () -> {
		if (mcVersion != null) {
			Path mcuRoot = MCUpdater.getInstance().getArchiveFolder();
			Gson gson = new Gson();
			String indexName = mcVersion.getAssets();
			if (indexName == null) indexName = "legacy";
			File indexesPath = mcuRoot.resolve("assets").resolve("indexes").toFile();
			File indexFile = new File(indexesPath, indexName + ".json");
			String json;
			try {
				json = FileUtils.readFileToString(indexFile, StandardCharsets.UTF_8);
				AssetIndex index = gson.fromJson(json, AssetIndex.class);
				logger.log(Level.FINER, "Assets virtual: {0}", index.isVirtual());
				if (index.isVirtual()) {
					var reference = new Object() {
						boolean doLinks = true;
					};
					try {
						Files.createSymbolicLink(mcuRoot.resolve("linktest"), mcuRoot.resolve("MCUpdater.log.0"));
						mcuRoot.resolve("linktest").toFile().delete();
					} catch (Exception e) {
						reference.doLinks = false;
						logger.log(Level.WARNING, "Unable to use symbolic linking", e);
					}
					Path assetsPath = mcuRoot.resolve("assets");
					Path virtualPath = assetsPath.resolve("virtual");
					index.getObjects().forEach((name, asset) -> {
						Path target = virtualPath.resolve(name);
						Path original = assetsPath.resolve("objects").resolve(asset.getHash().substring(0,2)).resolve(asset.getHash());

						if (!Files.exists(target)) {
							try {
								Files.createDirectories(target.getParent());
								if (reference.doLinks) {
									Files.createSymbolicLink(target, original);
								} else {
									Files.copy(original, target);
								}
							} catch (IOException e) {
								logger.log(Level.SEVERE, "Assets exception!", e);
							}
						}
					});
				}
			} catch (IOException e) {
				logger.log(Level.SEVERE, "Assets exception!", e);
			}
		}
	};

	public Install(ServerList server, List<GenericModule> toInstall, List<ConfigFile> configs) {
		this.server = server;
		this.installList = toInstall;
		this.configList = configs;
		this.mcVersion = MinecraftVersion.loadVersion(server.getVersion());
	}

	public boolean doInstall(Path instancePath, boolean clearExisting, Instance instData, ModSide side) throws Exception {
		this.instancePath = instancePath;
		this.side = side;
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
		DownloadInfo downloadInfo = null;
		if (side.equals(ModSide.CLIENT)) {
			clientJar = instancePath.resolve("bin").resolve("minecraft.jar");
			prepareClient(instancePath);
			// Check: Does jar exist, does it match the necessary version, do any mods need to be inserted into the jar
			if (jarBuildNeeded(clientJar, instData))  {
				// Build Jar
				targetJar = clientJar;
				downloadInfo = mcVersion.getDownloadInfo(DownloadType.CLIENT);
			}
			if (server.isGenerateList()) MCUpdater.getInstance().writeMCServerFile(instancePath, server.getName(), server.getAddress());
		} else {
			serverJar = instancePath.resolve("minecraft_server." + server.getVersion() + ".jar");
			prepareServer(instancePath);
			targetJar = serverJar;
			downloadInfo = mcVersion.getDownloadInfo(DownloadType.SERVER);
		}
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
		if (libraryQueue != null) {
			libraryQueue.processQueue(2, postProcessLibraries);
		}
		Set<Downloadable> generalFiles = gatherInstallables(instData);
		generalQueue = parent.submitNewQueue("Instance files", server.getServerId(), generalFiles, instancePath.toFile(), DownloadCache.getDir());
		generalQueue.processQueue(12, postProcessGeneral);
		if (assetsQueue != null) {
			assetsQueue.processQueue(8, postProcessAssets);
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

	private Set<Downloadable> gatherInstallables(final Instance instData) {
		Set<Downloadable> resultSet = new HashSet<>();
		int modCount = installList.size();
		AtomicInteger modsLoaded = new AtomicInteger();
		this.installList.stream().filter(entry -> entry.getModType() != ModType.Jar).forEach(entry -> {
			logger.log(Level.INFO, "Mod: {0}", entry.getName());
			Collections.sort(entry.getPrioritizedUrls());
			switch (entry.getModType()) {
				case Extract:
					resultSet.add(new Downloadable(entry.getName(), cleanForFile(entry.getId()) + ".zip", entry.getMD5(), entry.getFilesize(), entry.getUrls()));
					modExtract.put(cleanForFile(entry.getId()) + ".zip", entry.getInRoot());
					break;
				case Option:
					//TODO: Unimplemented
					break;
				default:
					resultSet.add(new Downloadable(entry.getName(), entry.getFilename(), entry.getMD5(), entry.getFilesize(), entry.getUrls()));
					instData.addMod(entry.getId(), entry.getMD5(), entry.getFilename());
			}
			modsLoaded.incrementAndGet();
			logger.log(Level.INFO, "  Queued ({0}/{1})", new Integer[]{modsLoaded.get(), modCount});
		});
		configList.forEach(cfEntry -> {
			final File confFile = instancePath.resolve(cfEntry.getPath()).toFile();
			if (!confFile.exists() || !cfEntry.isNoOverwrite())
				resultSet.add(new Downloadable(cfEntry.getPath(), cfEntry.getPath(), cfEntry.getMD5(), 10000, cfEntry.getUrls()));
		});
		return resultSet;
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

	private List<File> recurseFolder(File sourceFolder, boolean includeFolders) {
		List<File> output = new ArrayList<>();
		List<File> input = Arrays.asList(Objects.requireNonNull(sourceFolder.listFiles()));
		if (includeFolders) {
			output.add(sourceFolder);
		}
		input.forEach(entry -> {
			if (entry.isDirectory()) {
				List<File> subfolder = recurseFolder(entry, includeFolders);
				output.addAll(subfolder);
			} else {
				output.add(entry);
			}
		});
		return output;
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
		libraryQueue = parent.submitNewQueue("Libraries", server.getServerId(), libDownloads, instancePath.resolve("libraries").toFile(), DownloadCache.getDir());
	}

	private void prepareServer(Path instancePath) {
		Set<Downloadable> libDownloads = new HashSet<>();
		Library lib = new Library();
		lib.setName("net.sf.jopt-simple:jopt-simple:4.5"); // inject command line processor (and override for newer versions)
		if (Version.requestedFeatureLevel(server.getVersion(), "1.8")) lib.setName("net.sf.jopt-simple:jopt-simple:4.6");
		if (Version.requestedFeatureLevel(server.getVersion(), "1.12")) lib.setName("net.sf.jopt-simple:jopt-simple:5.0.3");
		Downloadable entry = processLibrary(lib);
		if (entry != null) {
			libDownloads.add(entry);
			if (entry.getFriendlyName().contains("natives")) {
				logger.info(String.format("Will extract: %s", entry.getFilename()));
				toExtract.add(lib.getFilename());
			}
		}
		libraryQueue = parent.submitNewQueue("Libraries", server.getServerId(), libDownloads, instancePath.resolve("libraries").toFile(), DownloadCache.getDir());
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
