package org.mcupdater.util;

import org.apache.commons.codec.language.Soundex;
import org.apache.commons.lang3.StringUtils;
import org.mcupdater.api.Version;
import org.mcupdater.model.Module;
import org.mcupdater.model.*;
import org.w3c.dom.Document;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

import java.io.BufferedWriter;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardOpenOption;
import java.util.*;

public class ServerDefinition {
	public final Map<String, String> modExceptions = new HashMap<>();
	public final Map<String, String> configExceptions = new HashMap<>();
	public static boolean hasLitemods = false;
	private ServerList entry;
	private List<Import> imports;
	private Map<String, Module> modules;
	private List<ConfigFile> tempConfigs;
	private List<Loader> loaders;

	public ServerDefinition() {
		this.entry = new ServerList();
		this.loaders = new ArrayList<>();
		this.imports = new ArrayList<>();
		this.modules = new HashMap<>();
		this.tempConfigs = new ArrayList<>();
		initExceptions();
	}

	private void initExceptions() {
		modExceptions.put("NotEnoughItems", "NEI");
		modExceptions.put("AWWayofTime", "BloodMagic");
		modExceptions.put("WR-CBE|Core", "WirelessRedstone");
		modExceptions.put("TConstruct", "TinkersWorkshop");
		modExceptions.put("inventorytweaks", "InvTweaks");
		modExceptions.put("ProjRed|Core", "ProjectRed");
		configExceptions.put("AWWayofTime", "BloodMagic");
		configExceptions.put("microblocks", "ForgeMultipart");
		configExceptions.put("cofh/world", "CoFHCore");
		configExceptions.put("cofh/Lexicon-Whitelist", "CoFHCore");
		configExceptions.put("hqm", "HardcoreQuesting");
		configExceptions.put("forgeChunkLoading", "forge-\\d+.\\d+.\\d+.\\d+");
		configExceptions.put("forge.cfg", "forge-\\d+.\\d+.\\d+.\\d+");
		configExceptions.put("splash.properties", "forge-\\d+.\\d+.\\d+.\\d+");
		configExceptions.put("scripts", "MineTweaker3");
		configExceptions.put(".zs", "MineTweaker3");
		configExceptions.put("resources", "ResourceLoader");
		configExceptions.put("advRocketry","advancedRocketry");
		configExceptions.put("AppliedEnergistics2","appliedenergistics2");
		configExceptions.put("brandon3055","brandonscore");
		configExceptions.put("Extreme Reactors","bigreactors");
		configExceptions.put("Tiny Progressions","tp");
		configExceptions.put("DEPSAMarker.txt","draconicevolution");
		configExceptions.put("WirelessCraftingTerminal.cfg","wct");
		configExceptions.put("structures","reccomplex");
		configExceptions.put("kubejs","kubejs");
	}

	public void writeServerPack(String stylesheet, Path outputFile, List<Module> moduleList, Boolean onlyOverrides) {
		try {
			if (hasLitemods && !hasMod(moduleList, "liteloader")) {
				moduleList.add(new Module("LiteLoader", "liteloader", Arrays.asList(new PrioritizedURL("http://dl.liteloader.com/versions/com/mumfrey/liteloader/" + this.getServerEntry().getVersion() + "/liteloader-" + this.getServerEntry().getVersion() + ".jar", 0)), null,100000, "", false, ModType.Library, 100, false, false, true, "", null, "CLIENT", "", null, "--tweakClass com.mumfrey.liteloader.launch.LiteLoaderTweaker", "", null, ""));
				moduleList = MCUpdater.getInstance().sortMods(moduleList);
			}

			BufferedWriter fileWriter = Files.newBufferedWriter(outputFile, StandardCharsets.UTF_8, StandardOpenOption.CREATE, StandardOpenOption.TRUNCATE_EXISTING, StandardOpenOption.WRITE);
			generateServerPackHeaderXML(stylesheet, fileWriter);
			generateServerHeaderXML(this.getServerEntry(), fileWriter);
			generateServerDetailXML(fileWriter, this.imports, this.loaders, moduleList, onlyOverrides);
			generateServerFooterXML(fileWriter);
			generateServerPackFooterXML(fileWriter);
			fileWriter.close();

		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	public boolean hasMod(List<Module> moduleList, String modId) {
		for (Module entry : moduleList) {
			if (entry.getId().equals(modId)) {
				return true;
			}
		}
		return false;
	}

	public static void generateServerHeaderXML(Server server, BufferedWriter writer) throws IOException {
		writer.write("\t<Server id=\"" + xmlEscape(server.getServerId()) +
				"\" abstract=\"" + Boolean.toString(server.isFakeServer()) +
				"\" name=\"" + xmlEscape(server.getName()) +
				(server.getNewsUrl().isEmpty() ? "" : ("\" newsUrl=\"" + xmlEscape(server.getNewsUrl()))) +
				(server.getIconUrl().isEmpty() ? "" : ("\" iconUrl=\"" + xmlEscape(server.getIconUrl()))) +
				"\" version=\"" + xmlEscape(server.getVersion()) +
				(server.getAddress().isEmpty() ? "" : ("\" serverAddress=\"" + xmlEscape(server.getAddress()))) +
				"\" generateList=\"" + Boolean.toString(server.isGenerateList()) +
				"\" autoConnect=\"" + Boolean.toString(server.isAutoConnect()) +
				"\" revision=\"" + xmlEscape(server.getRevision()) +
				"\" mainClass=\"" + xmlEscape(server.getMainClass()) +
				"\" launcherType=\"" + server.getLauncherType() +
				(server.getLibOverrides().size() == 0 ? "" : ("\" libOverrides=\"" + StringUtils.join(server.getLibOverrides().values()," "))) +
				(server.getServerClass_Raw().isEmpty() ? "" : ("\" serverClass=\"" + server.getServerClass_Raw())) +
				"\">");
		writer.newLine();
	}

	public static void generateServerPackHeaderXML(String stylesheet, BufferedWriter writer) throws IOException {
		writer.write("<?xml version=\"1.0\" encoding=\"UTF-8\"?>");
		writer.newLine();
		if (!stylesheet.isEmpty()) {
			writer.write("<?xml-stylesheet href=\"" + xmlEscape(stylesheet) + "\" type=\"text/xsl\" ?>");
			writer.newLine();
		}
		writer.write("<ServerPack version=\"" + Version.API_VERSION + "\" xmlns=\"http://www.mcupdater.com\" xmlns:xsi=\"http://www.w3.org/2001/XMLSchema-instance\" xsi:schemaLocation=\"http://www.mcupdater.com http://files.mcupdater.com/ServerPackv2.xsd\">");
		writer.newLine();
	}

	public static void generateServerDetailXML(BufferedWriter writer, List<Import> importsList, List<Loader> loaderList, List<Module> moduleList, Boolean onlyOverrides) throws IOException {

			for (Import importEntry : importsList) {
				writer.write("\t\t<Import" + (importEntry.getUrl().isEmpty() ? ">" : (" url=\"" + xmlEscape(importEntry.getUrl())) + "\">") + importEntry.getServerId() + "</Import>");
				writer.newLine();
			}
			for (Loader loaderEntry : loaderList) {
				writer.write("\t\t<Loader type=\"" + xmlEscape(loaderEntry.getType()) + "\" version=\"" + xmlEscape(loaderEntry.getVersion()) + "\" loadOrder=\"" + xmlEscape(String.valueOf(loaderEntry.getLoadOrder())) + "\"/>");
				writer.newLine();
			}
			for (Module moduleEntry : moduleList) {
				writer.write("\t\t<Module name=\"" + xmlEscape(moduleEntry.getName()) + "\" id=\"" + moduleEntry.getId() + "\" depends=\"" + moduleEntry.getDepends() + "\" side=\"" + moduleEntry.getSide() + "\">");
				writer.newLine();
				if (!onlyOverrides) {
					for (PrioritizedURL url : moduleEntry.getPrioritizedUrls()) {
						writer.write("\t\t\t<URL priority=\"" + url.getPriority() + "\">" + xmlEscape(url.getUrl()) + "</URL>");
						writer.newLine();
					}
					if (moduleEntry.getCurseProject() != null) {
						writer.write("\t\t\t<Curse" +
								" project=\"" + moduleEntry.getCurseProject().getProject() + "\"" +
								(moduleEntry.getCurseProject().getFile() != 0 ? (" file=\"" + Integer.toString(moduleEntry.getCurseProject().getFile()) + "\"") : "") +
								" type=\"" + moduleEntry.getCurseProject().getReleaseType().toString() + "\"" +
								" autoupgrade=\"" + Boolean.toString(moduleEntry.getCurseProject().getAutoUpgrade()) + "\"/>");
						writer.newLine();
					}
					if (!moduleEntry.getLoadPrefix().isEmpty()) {
						writer.write("\t\t\t<LoadPrefix>" + xmlEscape(moduleEntry.getLoadPrefix()) + "</LoadPrefix>");
						writer.newLine();
					}
					if (!moduleEntry.getPath().isEmpty()) {
						writer.write("\t\t\t<ModPath>" + xmlEscape(moduleEntry.getPath()) + "</ModPath>");
						writer.newLine();
					}
					writer.write("\t\t\t<Size>" + Long.toString(moduleEntry.getFilesize()) + "</Size>");
					writer.newLine();
					writer.write("\t\t\t<Required");
					if (!moduleEntry.getRequired() && moduleEntry.getIsDefault()) {
						writer.write(" isDefault=\"true\"");
					}
					writer.write(">" + (moduleEntry.getRequired() ? "true" : "false") + "</Required>");
					writer.newLine();
					writer.write("\t\t\t<ModType");
					if (moduleEntry.getInRoot()) {
						writer.write(" inRoot=\"true\"");
					}
					if (moduleEntry.getJarOrder() > 0 && moduleEntry.getModType().equals(ModType.Jar)) {
						writer.write(" order=\"" + moduleEntry.getJarOrder() + "\"");
					}
					if (moduleEntry.getKeepMeta()) {
						writer.write(" keepMeta=\"true\"");
					}
					if (!moduleEntry.getLaunchArgs().isEmpty()) {
						writer.write(" launchArgs=\"" + xmlEscape(moduleEntry.getLaunchArgs()) + "\"");
					}
					if (!moduleEntry.getJreArgs().isEmpty()) {
						writer.write(" jreArgs=\"" + xmlEscape(moduleEntry.getJreArgs()));
					}
					writer.write(">" + moduleEntry.getModType().toString() + "</ModType>");
					writer.newLine();
					if (!moduleEntry.getMD5().isEmpty()) {
						writer.write("\t\t\t<MD5>" + moduleEntry.getMD5() + "</MD5>");
						writer.newLine();
					}
					if (moduleEntry.getMeta().size() > 0) {
						writer.write("\t\t\t<Meta>");
						writer.newLine();
						for (Map.Entry<String, String> metaEntry : moduleEntry.getMeta().entrySet()) {
							writer.write("\t\t\t\t<" + xmlEscape(metaEntry.getKey()) + ">" + xmlEscape((metaEntry.getValue() == null ? "" : metaEntry.getValue())) + "</" + xmlEscape(metaEntry.getKey()) + ">");
							writer.newLine();
						}
						writer.write("\t\t\t</Meta>");
						writer.newLine();
					}
					for (GenericModule submodule : moduleEntry.getSubmodules()) {
						writer.write("\t\t\t<Submodule name=\"" + xmlEscape(submodule.getName()) + "\" id=\"" + submodule.getId() + "\" depends=\"" + submodule.getDepends() + "\" side=\"" + submodule.getSide() + "\">");
						writer.newLine();
						for (PrioritizedURL url : submodule.getPrioritizedUrls()) {
							writer.write("\t\t\t\t<URL priority=\"" + url.getPriority() + "\">" + xmlEscape(url.getUrl()) + "</URL>");
							writer.newLine();
						}
						if (submodule.getCurseProject() != null) {
							writer.write("\t\t\t\t<Curse" +
									" project=\"" + submodule.getCurseProject().getProject() + "\"" +
									(submodule.getCurseProject().getFile() != -1 ? " file=\"" + Integer.toString(submodule.getCurseProject().getFile()) + "\"" : "") +
									" type=\"" + submodule.getCurseProject().getReleaseType().toString() + "\"" +
									" autoupgrade=\"" + Boolean.toString(submodule.getCurseProject().getAutoUpgrade()) + "\"/>");
							writer.newLine();
						}
						if (!submodule.getLoadPrefix().isEmpty()) {
							writer.write("\t\t\t\t<LoadPrefix>" + xmlEscape(submodule.getLoadPrefix()) + "</LoadPrefix>");
							writer.newLine();
						}
						if (!submodule.getPath().isEmpty()) {
							writer.write("\t\t\t\t<ModPath>" + xmlEscape(submodule.getPath()) + "</ModPath>");
							writer.newLine();
						}
						writer.write("\t\t\t\t<Size>" + Long.toString(submodule.getFilesize()) + "</Size>");
						writer.newLine();
						writer.write("\t\t\t\t<Required");
						if (!submodule.getRequired() && submodule.getIsDefault()) {
							writer.write(" isDefault=\"true\"");
						}
						writer.write(">" + (submodule.getRequired() ? "true" : "false") + "</Required>");
						writer.newLine();
						writer.write("\t\t\t\t<ModType");
						if (submodule.getInRoot()) {
							writer.write(" inRoot=\"true\"");
						}
						if (submodule.getJarOrder() > 0 && submodule.getModType().equals(ModType.Jar)) {
							writer.write(" order=\"" + submodule.getJarOrder() + "\"");
						}
						if (submodule.getKeepMeta()) {
							writer.write(" keepMeta=\"true\"");
						}
						if (!submodule.getLaunchArgs().isEmpty()) {
							writer.write(" launchArgs=\"" + xmlEscape(submodule.getLaunchArgs()) + "\"");
						}
						if (!submodule.getJreArgs().isEmpty()) {
							writer.write(" jreArgs=\"" + xmlEscape(submodule.getJreArgs()));
						}
						writer.write(">" + submodule.getModType().toString() + "</ModType>");
						writer.newLine();
						writer.write("\t\t\t\t<MD5>" + submodule.getMD5() + "</MD5>");
						writer.newLine();
						if (submodule.getMeta().size() > 0) {
							writer.write("\t\t\t\t<Meta>");
							writer.newLine();
							for (Map.Entry<String, String> metaEntry : submodule.getMeta().entrySet()) {
								writer.write("\t\t\t\t\t<" + xmlEscape(metaEntry.getKey()) + ">" + xmlEscape(metaEntry.getValue()) + "</" + xmlEscape(metaEntry.getKey()) + ">");
								writer.newLine();
							}
							writer.write("\t\t\t\t</Meta>");
							writer.newLine();
						}
						writer.write("\t\t\t</Submodule>");
						writer.newLine();
					}
				} else {
					writer.write("\t\t\t<Required");
					if (!moduleEntry.getRequired() && moduleEntry.getIsDefault()) {
						writer.write(" isDefault=\"true\"");
					}
					writer.write(">" + (moduleEntry.getRequired() ? "true" : "false") + "</Required>");
					writer.newLine();
					writer.write("\t\t\t<ModType>Override</ModType>");
					writer.newLine();
				}
				for (ConfigFile config : moduleEntry.getConfigs()) {
					writer.write("\t\t\t<ConfigFile>");
					writer.newLine();
					for (PrioritizedURL url : config.getPrioritizedUrls()) {
						writer.write("\t\t\t\t<URL priority=\"" + url.getPriority() + "\">" + xmlEscape(url.getUrl()) + "</URL>");
						writer.newLine();
					}
					writer.write("\t\t\t\t<Path>" + xmlEscape(config.getPath()) + "</Path>");
					writer.newLine();
					writer.write("\t\t\t\t<NoOverwrite>" + config.isNoOverwrite() + "</NoOverwrite>");
					writer.newLine();
					writer.write("\t\t\t\t<MD5>" + xmlEscape(config.getMD5()) + "</MD5>");
					writer.newLine();
					writer.write("\t\t\t</ConfigFile>");
					writer.newLine();
				}
				writer.write("\t\t</Module>");
				writer.newLine();
			}
	}

	public static void generateServerFooterXML(BufferedWriter writer) throws IOException {
		writer.write("\t</Server>");
		writer.newLine();
	}

	public static void generateServerPackFooterXML(BufferedWriter writer) throws IOException {
		writer.write("</ServerPack>");
		writer.newLine();
	}

	private static String xmlEscape(String input) {
		String result;
		try {
			if (input.isEmpty()) return input;
			result = input.replace("&", "&amp;").replace("\"", "&quot;").replace("'", "&apos;").replace("<", "&lt;").replace(">", "&gt;");
		} catch (Exception e) {
			result = "!!!! Error !!!!";
			System.out.println(input);
			e.printStackTrace();
		}
		return result;
	}

	private void addLoader(Loader newLoader) { this.loaders.add(newLoader);	}

	public void addImport(Import newImport) {
		this.imports.add(newImport);
	}

	public void addConfig(ConfigFile newConfig) {
		tempConfigs.add(newConfig);
	}

	public void addModule(Module newMod) {
		if (modules.containsKey(newMod.getId())) {
			System.out.println("Warning: ModID: " + newMod.getId() + " belonging to " + newMod.getName() + " already exists in the list, and is being overwritten.");
		}
		modules.put(newMod.getId(), newMod);
	}

	public void setServerEntry(ServerList newEntry) {
		this.entry = newEntry;
	}

	public ServerList getServerEntry() {
		return entry;
	}

	public List<Import> getImports() {
		return imports;
	}

	public List<Loader> getLoaders() {
		return loaders;
	}

	public void assignConfigs(Map<String,String> issues, boolean debug) {
		System.out.println("Assigning configs to mods\n===============");
		//this.modules.get(0).setConfigs(tempConfigs);
		Soundex snd = new Soundex();
		int distance;
		for (ConfigFile config : tempConfigs) {
			int potential = 0;
			System.out.println(config.getPath() + ":");
			Module tempModule = null;
			distance = 10000;
			String configName = config.getPath().substring(config.getPath().indexOf("/"), config.getPath().contains(".") ? config.getPath().lastIndexOf(".") : config.getPath().length()) ;
			for (Module mod : modules.values()) {
				try {
					int newDistance = StringUtils.getLevenshteinDistance(configName, mod.getId());
					for (Map.Entry<String, String> exception : configExceptions.entrySet()) {
						if (config.getPath().contains(exception.getKey()) && mod.getId().matches(exception.getValue())) {
							newDistance -= 20;
						}
					}
					if (Arrays.asList(config.getPath().toLowerCase().split("/")).contains(mod.getId().toLowerCase())) {
						newDistance -= 20;
					}
					if (configName.toLowerCase().contains(mod.getId().toLowerCase())) {
						newDistance -= 10;
					}
					if (configName.toLowerCase().contains(mod.getId().toLowerCase().substring(0, mod.getId().length() < 3 ? mod.getId().length() : 3))) {
						newDistance -= 1;
					}
					if (StringUtils.isAsciiPrintable(configName)) {
						if (snd.soundex(mod.getId()).equals(snd.soundex(configName))) {
							newDistance -= 10;
						} else if (StringUtils.isAsciiPrintable(mod.getName()) && snd.soundex(mod.getName()).equals(snd.soundex(configName))) {
							newDistance -= 10;
						}
					}
					if (newDistance <= 5 || debug) {
						System.out.println("   >" + mod.getId() + " - " + newDistance + " (potential)");
						potential++;
					}
					if (newDistance < distance) {
						tempModule = mod;
						distance = newDistance;
					}
				} catch (Exception e) {
					System.out.println("Problem with Mod " + mod.getName() + " (" + mod.getId() + ") and config " + config.getPath() + " (" + configName + ")");
					e.printStackTrace();
				}
			}
			if (tempModule != null) {
				System.out.println(config.getPath() + ": " + tempModule.getName() + " (" + distance + ")\n");
				if (tempModule.getSide().equals(ModSide.CLIENT)) {
					config.setNoOverwrite(true);
				}
				if ((distance > 5 && potential > 1) || distance > 10) {
					issues.put(config.getPath(),tempModule.getName());
				}
				modules.get(tempModule.getId()).getConfigs().add(config);
			} else {
				System.out.println(config.getPath() + " could not be assigned to a module!");
			}
		}
	}

	public Map<String, Module> getModules() {
		return modules;
	}

	public List<Module> sortMods() {
		return MCUpdater.getInstance().sortMods(new ArrayList<>(modules.values()));
	}

	public void addForge(String mcVersion, String forgeVersion) {
		this.addLoader(new Loader("Forge",forgeVersion,0));
		this.addModule(new Module("Minecraft Forge", "forge-" + forgeVersion, new ArrayList<PrioritizedURL>(), null, 100000,"", true, ModType.Regular, 0, false, false, true, "", new ArrayList<ConfigFile>(), "BOTH", "", new HashMap<String, String>(), "", "", new ArrayList<Submodule>(), ""));
	}

	public void addFabric(String mcVersion, String fabricVersion, String yarnVersion) {
		// reference: https://fabricmc.net/wiki/modpack:mcupdater

		final String baseUrl = "https://fabricmc.net/download/mcupdater/";
		final String fabricMainClass = "net.fabricmc.loader.launch.knot.KnotClient";

		// if yarn version is unspecified, we need to look this up
		if ( yarnVersion.equals("latest") ) {
			final String mavenUrl = "https://maven.fabricmc.net/net/fabricmc/yarn/maven-metadata.xml";
			System.out.println("Scanning "+mavenUrl+" for yarn version...");
			try {
				final Document xml = ServerPackParser.readXmlFromUrl(mavenUrl);
				final String yarnPrefix = mcVersion+".";
				String foundVersion = null;
				// NB: I despise sifting through random xml, so we're doing it quick and ugly
				NodeList tmp = xml.getElementsByTagName("versioning");
				for( int i = 0; i < tmp.getLength(); ++i ) {
					NodeList children = tmp.item(i).getChildNodes();
					for( int j = 0; j < children.getLength(); ++j ) {
						Node child = children.item(j);
						if( child.getNodeName().equals("release") ) {
							String version = child.getTextContent();
							if( version.startsWith(yarnPrefix) ) {
								// our snapshot matches the current release, use that
								System.out.println("Current yarn release is "+version+", using");
								foundVersion = version;
							}
						} else if( child.getNodeName().equals("versions") ) {
							// we're here, start digging
							NodeList versions = child.getChildNodes();
							// scan backwards, the list should be sorted ascending
							for( int k = versions.getLength()-1; k > 0; --k ) {
								final Node v = versions.item(k);
								String version = v.getTextContent();
								if( version.startsWith(yarnPrefix) ) {
									// we found one, use it
									System.out.println("Found yarn build "+version+", using");
									foundVersion = version;
									break;
								}
							}
						}
						// export the version we found and go
						if( foundVersion != null ) {
							yarnVersion = foundVersion;
							break;
						}
					}
				}
			} catch( Exception e ) {
				System.out.println("Failed to parse yarn maven metadata xml, please specify yarn version manually.");
			}
		}

		this.addImport(new Import(baseUrl + "?yarn=" + yarnVersion + "&loader=" + fabricVersion, "fabric"));
		this.getServerEntry().setMainClass(fabricMainClass);
	}
}
