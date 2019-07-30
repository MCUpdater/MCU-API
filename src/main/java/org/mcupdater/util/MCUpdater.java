package org.mcupdater.util;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import org.apache.commons.codec.DecoderException;
import org.apache.commons.codec.binary.Hex;
import org.apache.commons.io.Charsets;
import org.apache.commons.io.FileUtils;
import org.apache.commons.io.IOUtils;
import org.apache.commons.lang3.ArrayUtils;
import org.apache.commons.lang3.StringUtils;
import org.mcupdater.FMLStyleFormatter;
import org.mcupdater.MCUApp;
import org.mcupdater.api.Version;
import org.mcupdater.certs.SSLExpansion;
import org.mcupdater.database.DatabaseManager;
import org.mcupdater.downloadlib.DownloadQueue;
import org.mcupdater.downloadlib.Downloadable;
import org.mcupdater.downloadlib.TaskableExecutor;
import org.mcupdater.instance.FileInfo;
import org.mcupdater.instance.Instance;
import org.mcupdater.model.*;
import org.mcupdater.mojang.*;
import org.mcupdater.mojang.AssetIndex.Asset;
import org.mcupdater.mojang.nbt.TagByte;
import org.mcupdater.mojang.nbt.TagCompound;
import org.mcupdater.mojang.nbt.TagList;
import org.mcupdater.mojang.nbt.TagString;

import javax.swing.*;
import java.awt.image.BufferedImage;
import java.io.*;
import java.math.BigInteger;
import java.net.MalformedURLException;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.*;
import java.util.logging.FileHandler;
import java.util.logging.Level;
import java.util.logging.Logger;


public class MCUpdater {
	private final Path MCFolder;
	private DatabaseManager dbManager;
	private Path archiveFolder;
	private Path instanceRoot;
	private MCUApp parent;
	private final String sep = System.getProperty("file.separator");
	public MessageDigest md5;
	public ImageIcon defaultIcon;
	public static Logger apiLogger;
	private int timeoutLength = 5000;
	private final Gson gson = new GsonBuilder().setPrettyPrinting().create();
	public static String defaultMemory = "4G";
	public static String defaultPermGen = "128M";
	public static String defaultJVMArgs = "-XX:+UseG1GC -Dsun.rmi.dgc.server.gcInterval=2147483646 -XX:+UnlockExperimentalVMOptions -XX:G1NewSizePercent=20 -XX:G1ReservePercent=20 -XX:MaxGCPauseMillis=50 -XX:G1HeapRegionSize=32M";
	private static MCUpdater INSTANCE;

	public static MCUpdater getInstance(File file) {
		if( INSTANCE == null ) {
			INSTANCE = new MCUpdater(file);
            if( file == null ) {
                apiLogger.finest("MCUpdater intialized without path");
            } else {
                apiLogger.finest("MCUpdater intialized with path: " + file.getAbsolutePath());
            }
		}
		return INSTANCE;
	}
	
	public static MCUpdater getInstance() {
		if( INSTANCE == null ) {
			INSTANCE = new MCUpdater(null);
			apiLogger.finest("MCUpdater intialized without path");
		}
		return INSTANCE;		
	}
	
	public static String cpDelimiter() {
		String osName = System.getProperty("os.name");
		if (osName.startsWith("Windows")) {
			return ";";
		} else {
			return ":";
		}
	}
	
	private MCUpdater(File desiredRoot)
	{
		apiLogger = Logger.getLogger("MCU-API");
		apiLogger.setLevel(Level.ALL);
		if(System.getProperty("os.name").startsWith("Windows"))
		{
			MCFolder = new File(System.getenv("APPDATA")).toPath().resolve(".minecraft");
			archiveFolder = new File(System.getenv("APPDATA")).toPath().resolve(".MCUpdater");
		} else if(System.getProperty("os.name").startsWith("Mac"))
		{
			MCFolder = new File(System.getProperty("user.home")).toPath().resolve("Library").resolve("Application Support").resolve("minecraft");
			archiveFolder = new File(System.getProperty("user.home")).toPath().resolve("Library").resolve("Application Support").resolve("MCUpdater");
		}
		else
		{
			MCFolder = new File(System.getProperty("user.home")).toPath().resolve(".minecraft");
			archiveFolder = new File(System.getProperty("user.home")).toPath().resolve(".MCUpdater");
		}
		if (!(desiredRoot == null)) {
			archiveFolder = desiredRoot.toPath();
		}
		try {
			FileHandler apiHandler = new FileHandler(archiveFolder.resolve("MCU-API.log").toString(), 0, 3);
			apiHandler.setFormatter(new FMLStyleFormatter());
			apiLogger.addHandler(apiHandler);
			
		} catch (SecurityException | IOException e1) {
			e1.printStackTrace(); // Will only be thrown if there is a problem with logging.
		}
		try {
			md5 = MessageDigest.getInstance("MD5");
		} catch (NoSuchAlgorithmException e) {
			apiLogger.log(Level.SEVERE, "No MD5 support!", e);
		}

		try {
			defaultIcon = new ImageIcon(MCUpdater.class.getResource("/minecraft.png"));
		} catch( NullPointerException e ) {
			_debug( "Unable to load default icon?!" );
			defaultIcon = new ImageIcon(new BufferedImage(32,32,BufferedImage.TYPE_INT_ARGB));
		}
		// configure the download cache
		try {
			DownloadCache.init(archiveFolder.resolve("cache").toFile());
		} catch (IllegalArgumentException e) {
			_debug( "Suppressed attempt to re-init download cache?!" );
		}
		MCUpdater.apiLogger.info("Registering root certificates");
		SSLExpansion ssle = SSLExpansion.getInstance();
		try {
			List<String> resources = IOUtils.readLines(MCUpdater.class.getResourceAsStream("/org/mcupdater/certs/certlist.txt"), Charsets.UTF_8);
			for (String rsrc : resources) {
				if (rsrc.endsWith(".pem")) {
					ssle.addCertificateFromStream(MCUpdater.class.getResourceAsStream("/org/mcupdater/certs/" + rsrc), rsrc.substring(0, rsrc.length() - 4));
					apiLogger.info("Registered root certificate: " + rsrc.substring(0, rsrc.length() - 4));
				}
			}
			ssle.updateSSLContext();
		} catch (Exception e) {
			e.printStackTrace();
		}
		dbManager = new DatabaseManager(archiveFolder);
		/*
		try {
			long start = System.currentTimeMillis();
			URL md5s = new URL("http://files.mcupdater.com/md5.dat");
			URLConnection md5Con = md5s.openConnection();
			md5Con.setConnectTimeout(this.timeoutLength);
			md5Con.setReadTimeout(this.timeoutLength);
			InputStreamReader input = new InputStreamReader(md5Con.getInputStream());
			BufferedReader buffer = new BufferedReader(input);
			String currentLine;
			while(true){
				currentLine = buffer.readLine();
				if(currentLine != null){
					String entry[] = currentLine.split("\\|");
					versionMap.put(entry[0], entry[1]);
				} else {
					break;
				}
			}
			buffer.close();
			input.close();
			apiLogger.fine("Took "+(System.currentTimeMillis()-start)+"ms to load md5.dat");
		} catch (MalformedURLException e) {
			apiLogger.log(Level.SEVERE, "Bad URL", e);
		} catch (IOException e) {
			apiLogger.log(Level.SEVERE, "I/O Error", e);
		}
		*/
	}

	public MCUApp getParent() {
		return parent;
	}

	public void setParent(MCUApp parent) {
		this.parent = parent;
	}

	public Path getMCFolder()
	{
		return MCFolder;
	}

	public Path getArchiveFolder() {
		return archiveFolder;
	}

	public Path getInstanceRoot() {
		return instanceRoot;
	}

	public void setInstanceRoot(Path instanceRoot) {
		this.instanceRoot = instanceRoot;
		apiLogger.info("Instance root changed to: " + instanceRoot.toString());
	}

	private boolean getExcludedNames(String path, boolean forDelete) {
		if(path.contains("mcu" + sep)) {
			// never delete from the mcu folder
			return true;
		}
		if (path.contains("mods") && (path.contains(".zip") || path.contains(".jar"))) {
			// always delete mods in archive form
			return false;
		}
		if(path.contains("bin" + sep + "minecraft.jar")) {
			// always delete bin/minecraft.jar
			return false;
		}
		if(path.contains("bin" + sep)) {
			// never delete anything else in bin/
			return true;
		}
		if(path.contains("resources") && !path.contains("mods")) {
			// never delete resources unless it is under the mods directory
			return true;
		}
		if(path.contains("lib" + sep)) {
			// never delete the lib/ folder
			return true;
		}
		if(path.contains("saves")) {
			// never delete saves
			return true;
		}
		if(path.contains("screenshots")) {
			// never delete screenshots
			return true;
		}
		if(path.contains("stats")) {
			return true;
		}
		if(path.contains("texturepacks")) {
			return true;
		}
		if(path.contains("lastlogin")) {
			return true;
		}
		if(path.contains("instance.dat")) {
			return true;
		}
		if(path.contains("minecraft.jar")) {
			return true;
		}
		if(path.contains("options.txt")) {
			return forDelete;
		}
		if(path.contains("META-INF" + sep)) {
			return true;
		}
		// Temporary hardcoding of client specific mod configs (i.e. Don't clobber on update)
		if(path.contains("rei_minimap" + sep)) {
			return true;
		}
		if(path.contains("macros" + sep)) {
			return true;
		}
		if(path.contains("InvTweaks")) {
			return true;
		}
		if(path.contains("optionsof.txt")){
			return true;
		}
		if(path.contains("voxelMap")) {
			return true;
		}
		//
		return false;
	}

	private List<File> recurseFolder(File folder, boolean includeFolders)
	{
		List<File> output = new ArrayList<>();
		List<File> input = new ArrayList<>(Arrays.asList(folder.listFiles()));
		Iterator<File> fi = input.iterator();
		if(includeFolders) {
			output.add(folder);
		}
		while(fi.hasNext())
		{
			File entry = fi.next();
			if(entry.isDirectory())
			{
				List<File> subfolder = recurseFolder(entry, includeFolders);
				output.addAll(subfolder);
			} else {
				output.add(entry);
			}
		}
		return output;
	}

	public boolean installMods(final ServerList server, List<GenericModule> toInstall, List<ConfigFile> configs, final Path instancePath, boolean clearExisting, final Instance instData, final ModSide side) throws FileNotFoundException {
		//TODO: Divide code into logical sections for better analysis
		if (Version.requestedFeatureLevel(server.getMCUVersion(), "2.2")) {
			// Sort mod list for InJar
			Collections.sort(toInstall, new ModuleComparator(ModuleComparator.Mode.IMPORTANCE));
		}
		//final Path instancePath = instanceRoot.resolve(server.getServerId());
		Path binPath = instancePath.resolve("bin");
		final Path productionJar;
		//File jar = null;
		final File tmpFolder = instancePath.resolve("temp" + Integer.toString((new Random()).nextInt(100))).toFile();
		tmpFolder.mkdirs();
		Set<Downloadable> jarMods = new HashSet<>();
		Set<Downloadable> generalFiles = new HashSet<>();
		DownloadQueue assetsQueue = null;
		DownloadQueue jarQueue;
		DownloadQueue generalQueue;
		DownloadQueue libraryQueue = null;
		final List<String> libExtract = new ArrayList<>();
		final Map<String,Boolean> modExtract = new HashMap<>();
		final Map<String,Boolean> keepMeta = new TreeMap<>();
		Downloadable baseJar = null;
		final MinecraftVersion version = MinecraftVersion.loadVersion(server.getVersion());
		List<URL> jarUrl = new ArrayList<>();
        Set<Downloadable> libSet = new HashSet<>();
		DownloadInfo downloadInfo;
		switch (side) {
		case CLIENT:
            System.out.println("Overrides: " + server.getLibOverrides().size());
			assetsQueue = parent.submitAssetsQueue("Assets", server.getServerId(), version);
            for (Map.Entry<String,String> entry : server.getLibOverrides().entrySet()) {
                System.out.println(entry.getKey() + ": " + entry.getValue());
            }
			for (Library lib : version.getLibraries()) {
				String key = StringUtils.join(Arrays.copyOfRange(lib.getName().split(":"),0,2),":");
				System.out.println(lib.getName() + " - " + key);
				if (server.getLibOverrides().containsKey(key)) {
					lib.setName(server.getLibOverrides().get(key));
					System.out.println(" - Replaced: " + lib.getName());
				}
				if (lib.validForOS()) {
					List<URL> urls = new ArrayList<>();
					try {
						urls.add(new URL(lib.getDownloadUrl()));
					} catch (MalformedURLException e) {
						apiLogger.log(Level.SEVERE, "Bad URL", e);
					}
					Downloadable entry = new Downloadable(lib.getName(),lib.getFilename(),"",100000,urls);
					libSet.add(entry);
					if (lib.hasNatives()) {
						libExtract.add(lib.getFilename());
					}
				}
			}
			downloadInfo = version.getDownloadInfo(DownloadType.CLIENT);
			productionJar = binPath.resolve("minecraft.jar");
			try {
				if (downloadInfo != null) {
					jarUrl.add(downloadInfo.getUrl());
				}
				// Add legacy URL to fall back to
				jarUrl.add(new URL("https://s3.amazonaws.com/Minecraft.Download/versions/" + server.getVersion() + "/" + server.getVersion() + ".jar"));
			} catch (MalformedURLException e2) {
				apiLogger.log(Level.SEVERE, "Bad URL", e2);
			}
			if (downloadInfo != null) {
				baseJar = new Downloadable("Minecraft jar", "0.jar", Downloadable.HashAlgorithm.SHA, downloadInfo.getSha1(), downloadInfo.getSize(), jarUrl);
			} else {
				baseJar = new Downloadable("Minecraft jar", "0.jar", "", 3000000, jarUrl);
			}
			keepMeta.put("0.jar", Version.requestedFeatureLevel(server.getVersion(), "1.6"));
			break;
		case SERVER:
			downloadInfo = version.getDownloadInfo(DownloadType.SERVER);
			productionJar = instancePath.resolve("minecraft_server." + server.getVersion() + ".jar");
			try {
				if (downloadInfo != null) {
					jarUrl.add(downloadInfo.getUrl());
				}
				// Add legacy URLs to fall back to
				jarUrl.add(new URL("https://s3.amazonaws.com/Minecraft.Download/versions/" + server.getVersion() + "/minecraft_server." + server.getVersion() + ".jar"));
				jarUrl.add(new URL("http://assets.minecraft.net/" + server.getVersion().replace(".", "_") + "/minecraft_server.jar"));
			} catch (MalformedURLException e2) {
				apiLogger.log(Level.SEVERE, "Bad URL", e2);
			}
			if (downloadInfo != null) {
				baseJar = new Downloadable("Server Jar", "0.jar", Downloadable.HashAlgorithm.SHA, downloadInfo.getSha1(), downloadInfo.getSize(), jarUrl);
			} else {
				baseJar = new Downloadable("Server jar", "0.jar", "", 3000000, jarUrl);
			}
			keepMeta.put("0.jar", Version.requestedFeatureLevel(server.getVersion(), "1.6"));

            Library lib = new Library();
			lib.setName("net.sf.jopt-simple:jopt-simple:4.5");
			if (Version.requestedFeatureLevel(server.getVersion(), "1.8")) {
				lib.setName("net.sf.jopt-simple:jopt-simple:4.6");
				if (Version.requestedFeatureLevel(server.getVersion(), "1.12")) {
					lib.setName("net.sf.jopt-simple:jopt-simple:5.0.3");
				}
			}
            String key = StringUtils.join(Arrays.copyOfRange(lib.getName().split(":"),0,2),":");
            System.out.println(lib.getName() + " - " + key);
            if (server.getLibOverrides().containsKey(key)) {
                lib.setName(server.getLibOverrides().get(key));
                System.out.println(" - Replaced: " + lib.getName());
            }
            if (lib.validForOS()) {
                List<URL> urls = new ArrayList<>();
                try {
                    urls.add(new URL(lib.getDownloadUrl()));
                } catch (MalformedURLException e) {
                    apiLogger.log(Level.SEVERE, "Bad URL", e);
                }
                Downloadable entry = new Downloadable(lib.getName(),lib.getFilename(),"",100000,urls);
                libSet.add(entry);
                if (lib.hasNatives()) {
                    libExtract.add(lib.getFilename());
                }
            }

			break;
		default:
			apiLogger.severe("Invalid API call to MCUpdater.installMods! (side cannot be " + side.toString() + ")");
			return false;
		}
        libraryQueue = parent.submitNewQueue("Libraries", server.getServerId(), libSet, instancePath.resolve("libraries").toFile(), DownloadCache.getDir());
		Boolean updateJar = clearExisting;
		if (side == ModSide.CLIENT) {
			if (!productionJar.toFile().exists()) {
				updateJar = true;
			}
			if (instData.getMCVersion() == null || !instData.getMCVersion().equals(server.getVersion())) {
				updateJar = true;
			}
		} else {
			updateJar = true;
		}			
		Iterator<GenericModule> iMods = toInstall.iterator();
		List<String> modIds = new ArrayList<>();
		Map<String,String> newFilenames = new HashMap<>();
		int jarModCount = 0;
		while (iMods.hasNext() && !updateJar) {
			GenericModule current = iMods.next();
			if (current.getModType() == ModType.Jar) {
				FileInfo jarMod = instData.findJarMod(current.getId());
				if (jarMod == null) {
					updateJar = true;
				} else if (current.getMD5().isEmpty() || (!current.getMD5().equalsIgnoreCase(jarMod.getMD5()))) {
					updateJar = true;
				}
				jarModCount++;
			} else {
				modIds.add(current.getId());
				newFilenames.put(current.getId(), current.getFilename());
			}
		}
		if (jarModCount != instData.getJarMods().size()) {
			updateJar = true;
		}
		if (updateJar && baseJar != null) {
			jarMods.add(baseJar);
		}
		for (FileInfo entry : instData.getInstanceFiles()) {
			if (!modIds.contains(entry.getModId()) || !newFilenames.get(entry.getModId()).equals(entry.getFilename())) {
				instancePath.resolve(entry.getFilename()).toFile().delete();
			}
		}
		instData.setJarMods(new ArrayList<FileInfo>());
		instData.setInstanceFiles(new ArrayList<FileInfo>());
		jarModCount = 0;
		apiLogger.info("Instance path: " + instancePath.toString());
		List<File> contents = recurseFolder(instancePath.toFile(), true);
		if (clearExisting){
			parent.setStatus("Clearing existing configuration");
			parent.log("Clearing existing configuration...");
			for (File entry : new ArrayList<>(contents)) {
				if (getExcludedNames(entry.getPath(), true)) {
					contents.remove(entry);
				}
			}
			ListIterator<File> liClear = contents.listIterator(contents.size());
			while(liClear.hasPrevious()) { 
				File entry = liClear.previous();
				entry.delete();
			}
		}
		Iterator<GenericModule> itMods = toInstall.iterator();
		final File buildJar = archiveFolder.resolve("build.jar").toFile();		
		if(buildJar.exists()) {
			buildJar.delete();
		}
		
		int modCount = toInstall.size();
		int modsLoaded = 0;
		int errorCount = 0;
		
		while(itMods.hasNext()) {
			GenericModule entry = itMods.next();
			parent.log("Mod: "+entry.getName());
			Collections.sort(entry.getPrioritizedUrls());
			switch (entry.getModType()) {
				case Jar:
					if (updateJar) {
						jarMods.add(new Downloadable(entry.getName(), entry.getJarOrder() + "-" + entry.getId() + ".jar",entry.getMD5(),entry.getFilesize(),entry.getUrls()));
						keepMeta.put(entry.getJarOrder() + "-" + cleanForFile(entry.getId()) + ".jar", entry.getKeepMeta());
						instData.addJarMod(entry.getId(), entry.getMD5());
						jarModCount++;
					}
					break;
				case Extract:
					generalFiles.add(new Downloadable(entry.getName(),cleanForFile(entry.getId()) + ".zip",entry.getMD5(),entry.getFilesize(),entry.getUrls()));
					modExtract.put(cleanForFile(entry.getId()) + ".zip", entry.getInRoot());
					break;
				case Option:
					//TODO: Unimplemented
					break;
				default:
					generalFiles.add(new Downloadable(entry.getName(), entry.getFilename(), entry.getMD5(),entry.getFilesize(),entry.getUrls()));
					instData.addMod(entry.getId(), entry.getMD5(), entry.getFilename());
			}
			modsLoaded++;
			parent.log("  Done ("+modsLoaded+"/"+modCount+")");
		}
		for (ConfigFile cfEntry : configs) {
			final File confFile = instancePath.resolve(cfEntry.getPath()).toFile();
			if (confFile.exists() && cfEntry.isNoOverwrite()) {
				continue;
			}
			List<URL> configUrl = new ArrayList<>();
			configUrl.addAll(cfEntry.getUrls());
			generalFiles.add(new Downloadable(cfEntry.getPath(), cfEntry.getPath(), cfEntry.getMD5(), 10000, configUrl));
		}

		generalQueue = parent.submitNewQueue("Instance files", server.getServerId(), generalFiles, instancePath.toFile(), DownloadCache.getDir());
		jarQueue = parent.submitNewQueue("Jar build files", server.getServerId(), jarMods, tmpFolder, DownloadCache.getDir());
		TaskableExecutor libExecutor = new TaskableExecutor(2, new Runnable(){

			@Override
			public void run() {
				parent.log("Extracting library files");
				for (String entry : libExtract){
					Archive.extractZip(instancePath.resolve("libraries").resolve(entry).toFile(), instancePath.resolve("libraries").resolve("natives").toFile(), false);
				}
				parent.log("Library file extraction complete");
				server.getLoaders().sort(new OrderComparator());
				for (Loader loader : server.getLoaders()) {
					loader.getILoader().install(instancePath, side);
				}
			}});
        if (libraryQueue != null) {
            libraryQueue.processQueue(libExecutor);
        }
		File branding = new File(tmpFolder, "fmlbranding.properties");
        boolean doBranding = false;
		if (!Version.requestedFeatureLevel(server.getVersion(),"1.13")) {
			try {
				doBranding = true;
				branding.createNewFile();
				Properties propBrand = new Properties();
				propBrand.setProperty("fmlbranding", "MCUpdater: " + server.getName() + " (rev " + server.getRevision() + ")");
				propBrand.store(new FileOutputStream(branding), "MCUpdater ServerPack branding");
			} catch (IOException e1) {
				apiLogger.log(Level.SEVERE, "I/O Error", e1);
			}
		}
		final boolean doJarUpdate = updateJar;
		final boolean finalDoBranding = doBranding;
		TaskableExecutor jarExecutor = new TaskableExecutor(2, new Runnable() {
			
			@Override
			public void run() {
				if (!doJarUpdate && finalDoBranding) {
					parent.log("Updating FML branding");
					try {
						Archive.updateArchive(productionJar.toFile(), new File[]{ branding });
					} catch (IOException e1) {
						apiLogger.log(Level.SEVERE, "I/O Error", e1);
					}
					parent.log("FML branding complete");
				} else {
					parent.log("Extracting files for jar insertion");
					for (Map.Entry<String,Boolean> entry : keepMeta.entrySet()) {
						File entryFile = new File(tmpFolder,entry.getKey());
						Archive.extractZip(entryFile, tmpFolder, entry.getValue());
						entryFile.delete();
					}
					try {
						buildJar.createNewFile();
					} catch (IOException e) {
						apiLogger.log(Level.SEVERE, "I/O Error", e);
					}
					boolean doManifest = true;
					List<File> buildList = recurseFolder(tmpFolder,true);
					for (File entry : new ArrayList<>(buildList)) {
						if (entry.getPath().contains("META-INF")) {
							doManifest = false;
						}
					}
					parent.log("Packaging updated jar...");
					try {
						Archive.createJar(buildJar, buildList, tmpFolder.getPath() + sep, doManifest);
					} catch (IOException e1) {
						parent.log("Failed to create jar!");
						apiLogger.log(Level.SEVERE, "I/O Error", e1);
					}
					try {
						Files.createDirectories(productionJar.getParent());
						Files.copy(buildJar.toPath(), productionJar, StandardCopyOption.REPLACE_EXISTING);
					} catch (IOException e) {
						apiLogger.log(Level.SEVERE, "Failed to copy new jar to instance!", e);
					}
					parent.log("Jar build/update complete");
				}
				List<File> tempFiles = recurseFolder(tmpFolder,true);
				ListIterator<File> li = tempFiles.listIterator(tempFiles.size());
				while(li.hasPrevious()) { 
					File entry = li.previous();
					entry.delete();
				}
				if (server.isGenerateList() && side != ModSide.SERVER) { writeMCServerFile(instancePath, server.getName(), server.getAddress()); }
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
					apiLogger.log(Level.SEVERE, "I/O error", e);
				}		
			}
		});
		jarQueue.processQueue(jarExecutor);
		TaskableExecutor genExecutor = new TaskableExecutor(12, new Runnable(){

			@Override
			public void run() {
				parent.log("Performing needed extractions");
				for (Map.Entry<String,Boolean> entry : modExtract.entrySet()) {
					if (entry.getValue()) {
						Archive.extractZip(instancePath.resolve(entry.getKey()).toFile(), instancePath.toFile());
					} else {
						Archive.extractZip(instancePath.resolve(entry.getKey()).toFile(), instancePath.resolve("mods").toFile());
					}
					instancePath.resolve(entry.getKey()).toFile().delete();
				}
				parent.log("Extractions complete");
			}
			
		});
		generalQueue.processQueue(genExecutor);
		TaskableExecutor assetsExecutor = new TaskableExecutor(8, new Runnable(){
			
			@Override
			public void run() {
				//check virtual
				Gson gson = new Gson();
				String indexName = version.getAssets();
				if (indexName == null) {
					indexName = "legacy";
				}
				File indexesPath = archiveFolder.resolve("assets").resolve("indexes").toFile();
				File indexFile = new File(indexesPath, indexName + ".json");
				String json;
				try {
					json = FileUtils.readFileToString(indexFile);
					AssetIndex index = gson.fromJson(json, AssetIndex.class);
					parent.log("Assets virtual: " + index.isVirtual());
					if (index.isVirtual()) {
						//Test symlink support
						boolean doLinks = true;
						try {
							java.nio.file.Files.createSymbolicLink(archiveFolder.resolve("linktest"), archiveFolder.resolve("MCUpdater.log.0"));
							archiveFolder.resolve("linktest").toFile().delete();
						} catch (Exception e) {
							doLinks = false;
						}
						Path assetsPath = archiveFolder.resolve("assets");
						Path virtualPath = assetsPath.resolve("virtual");
						for (Map.Entry<String, Asset> entry : index.getObjects().entrySet()) {
							Path target = virtualPath.resolve(entry.getKey());
							Path original = assetsPath.resolve("objects").resolve(entry.getValue().getHash().substring(0,2)).resolve(entry.getValue().getHash());
							
							if (!Files.exists(target)) {
								Files.createDirectories(target.getParent());
								if (doLinks) {
									Files.createSymbolicLink(target, original);
								} else {
									Files.copy(original, target);
								}
							}
						}
					}
				} catch (IOException e) {
					parent.baseLogger.log(Level.SEVERE, "Assets exception! " + e.getMessage());
				}

			}
			
		});
        if (assetsQueue != null) {
            assetsQueue.processQueue(assetsExecutor);
        }
		if( errorCount > 0 ) {
			parent.baseLogger.severe("Errors were detected with this update, please verify your files. There may be a problem with the serverpack configuration or one of your download sites.");
			return false;
		}
		return true;
	}
	
	private String cleanForFile(String id) {
		return id.replaceAll("[^a-zA-Z_0-9\\-.]", "_");
	}

	public void writeMCServerFile(Path installPath, String name, String ip) {
		apiLogger.info("Writing servers.dat");
		/*
		byte[] header = new byte[]{
				0x0A,0x00,0x00,0x09,0x00,0x07,0x73,0x65,0x72,0x76,0x65,0x72,0x73,0x0A,
				0x00,0x00,0x00,0x01,0x01,0x00,0x0B,0x68,0x69,0x64,0x65,0x41,0x64,0x64,
				0x72,0x65,0x73,0x73,0x01,0x08,0x00,0x04,0x6E,0x61,0x6D,0x65,0x00,
				(byte) (name.length() + 12), (byte) 0xC2,(byte) 0xA7,0x41,0x5B,0x4D,0x43,0x55,0x5D,0x20,(byte) 0xC2,(byte) 0xA7,0x46
				};
		byte[] nameBytes = name.getBytes();
		byte[] ipBytes = ip.getBytes();
		byte[] middle = new byte[]{0x08,0x00,0x02,0x69,0x70,0x00,(byte) ip.length()};
		byte[] end = new byte[]{0x00,0x00};
		int size = header.length + nameBytes.length + middle.length + ipBytes.length + end.length;
		byte[] full = new byte[size];
		int pos = 0;
		System.arraycopy(header, 0, full, pos, header.length);
		pos += header.length;
		System.arraycopy(nameBytes, 0, full, pos, nameBytes.length);
		pos += nameBytes.length;
		System.arraycopy(middle, 0, full, pos, middle.length);
		pos += middle.length;
		System.arraycopy(ipBytes, 0, full, pos, ipBytes.length);
		pos += ipBytes.length;
		System.arraycopy(end, 0, full, pos, end.length);
		*/

		TagCompound root = new TagCompound("");
		TagList servers = new TagList("servers", TagList.Type.Compound);
		TagCompound entry = new TagCompound("");
		entry.add(new TagByte("hideAddress", (byte) 1));
		String symbol = new String(new byte[]{(byte) 0xc2, (byte) 0xa7}, StandardCharsets.UTF_8);
		entry.add(new TagString("name", symbol + "A[MCU] " + symbol + "F" + name));
		entry.add(new TagString("ip",ip));
		servers.add(entry);
		root.add(servers);
		List<Byte> bytes = root.toBytes(true);
		byte[] full = ArrayUtils.toPrimitive(bytes.toArray(new Byte[bytes.size()]));
		File serverFile = installPath.resolve("servers.dat").toFile();
		try {
			serverFile.createNewFile();
			FileOutputStream fos = new FileOutputStream(serverFile);
			fos.write(full,0,full.length);
			fos.close();
		} catch (IOException e) {
			apiLogger.log(Level.SEVERE, "I/O Error", e);
		}
		
	}

	private static void _debug(String msg) {
		apiLogger.fine(msg);
	}

	public void setTimeout(int timeout) {
		this.timeoutLength = timeout;
	}
	
	public int getTimeout() {
		return this.timeoutLength;
	}

	public static String calculateGroupHash(Set<String> digests) {
		BigInteger hash = BigInteger.valueOf(0);
		for (String entry : digests) {
			try {
				BigInteger digest = new BigInteger(Hex.decodeHex(entry.toCharArray()));
				hash = hash.xor(digest);
			} catch (DecoderException e) {
				//e.printStackTrace();
				System.out.println("Entry '" + entry + "' is not a valid hexadecimal number");
			}
		}
		return Hex.encodeHexString(hash.toByteArray());
	}

	public List<Module> sortMods(List<Module> values) {
		Collections.sort(values, new ModuleComparator(ModuleComparator.Mode.IMPORTANCE));
		return values;
	}

	public DatabaseManager getDbManager() {
		return dbManager;
	}

}

