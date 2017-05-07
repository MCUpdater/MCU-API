package org.mcupdater.util;

import org.apache.commons.codec.language.Soundex;
import org.apache.commons.lang3.StringUtils;
import org.mcupdater.api.Version;
import org.mcupdater.model.*;

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

	public ServerDefinition() {
		this.entry = new ServerList();
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
		configExceptions.put("scripts", "MineTweaker3");
		configExceptions.put(".zs", "MineTweaker3");
		configExceptions.put("resources", "ResourceLoader");
	}

	public void writeServerPack(String stylesheet, Path outputFile, List<Module> sortedModules, Boolean onlyOverrides) {
		try {
			BufferedWriter fileWriter = Files.newBufferedWriter(outputFile, StandardCharsets.UTF_8, StandardOpenOption.CREATE, StandardOpenOption.TRUNCATE_EXISTING, StandardOpenOption.WRITE);
			fileWriter.write("<?xml version=\"1.0\" encoding=\"UTF-8\"?>");
			fileWriter.newLine();
			if (!stylesheet.isEmpty()) {
				fileWriter.write("<?xml-stylesheet href=\"" + xmlEscape(stylesheet) + "\" type=\"text/xsl\" ?>");
				fileWriter.newLine();
			}
			fileWriter.write("<ServerPack version=\"" + Version.API_VERSION + "\" xmlns=\"http://www.mcupdater.com\" xmlns:xsi=\"http://www.w3.org/2001/XMLSchema-instance\" xsi:schemaLocation=\"http://www.mcupdater.com http://files.mcupdater.com/ServerPackv2.xsd\">");
			fileWriter.newLine();
			fileWriter.write("\t<Server id=\"" + xmlEscape(this.getServerEntry().getServerId()) +
					"\" name=\"" + xmlEscape(this.getServerEntry().getName()) +
					"\" newsUrl=\"" + xmlEscape(this.getServerEntry().getNewsUrl()) +
					"\" version=\"" + xmlEscape(this.getServerEntry().getVersion()) +
					"\" mainClass=\"" + xmlEscape(this.getServerEntry().getMainClass()) +
					(this.getServerEntry().getAddress().isEmpty() ? "" : ("\" serverAddress=\"" + xmlEscape(this.getServerEntry().getAddress()))) +
					(this.getServerEntry().getIconUrl().isEmpty() ? "" : ("\" iconUrl=\"" + xmlEscape(this.getServerEntry().getIconUrl()))) +
					"\" revision=\"" + xmlEscape(this.getServerEntry().getRevision()) +
					"\" autoConnect=\"" + Boolean.toString(this.getServerEntry().isAutoConnect()) +
					"\">");
			fileWriter.newLine();
			for (Import importEntry : this.getImports()) {
				fileWriter.write("\t\t<Import" + (importEntry.getUrl().isEmpty() ? ">" : (" url=\"" + xmlEscape(importEntry.getUrl())) + "\">") + importEntry.getServerId() + "</Import>");
				fileWriter.newLine();
			}
			if (hasLitemods && !onlyOverrides) {
				fileWriter.write("\t\t<Module name=\"LiteLoader\" id=\"liteloader\">");
				fileWriter.newLine();
				fileWriter.write("\t\t\t<URL>http://dl.liteloader.com/versions/com/mumfrey/liteloader/" + this.getServerEntry().getVersion() + "/liteloader-" + this.getServerEntry().getVersion() + ".jar</URL>");
				fileWriter.newLine();
				fileWriter.write("\t\t\t<Required isDefault=\"true\">false</Required>");
				fileWriter.newLine();
				fileWriter.write("\t\t\t<ModType order=\"100\" launchArgs=\"--tweakClass com.mumfrey.liteloader.launch.LiteLoaderTweaker\">Library</ModType>");
				fileWriter.newLine();
				fileWriter.write("\t\t\t<MD5></MD5>");
				fileWriter.newLine();
				fileWriter.write("\t\t</Module>");
				fileWriter.newLine();
			}
			for (Module moduleEntry : sortedModules) {
				fileWriter.write("\t\t<Module name=\"" + xmlEscape(moduleEntry.getName()) + "\" id=\"" + moduleEntry.getId() + "\" depends=\"" + moduleEntry.getDepends() + "\" side=\"" + moduleEntry.getSide() + "\">");
				fileWriter.newLine();
				if (!onlyOverrides) {
					for (PrioritizedURL url : moduleEntry.getPrioritizedUrls()) {
						fileWriter.write("\t\t\t<URL priority=\"" + url.getPriority() + "\">" + xmlEscape(url.getUrl()) + "</URL>");
						fileWriter.newLine();
					}
					if (!moduleEntry.getPath().isEmpty()) {
						fileWriter.write("\t\t\t<ModPath>" + xmlEscape(moduleEntry.getPath()) + "</ModPath>");
						fileWriter.newLine();
					}
					fileWriter.write("\t\t\t<Size>" + Long.toString(moduleEntry.getFilesize()) + "</Size>");
					fileWriter.newLine();
					fileWriter.write("\t\t\t<Required");
					if (!moduleEntry.getRequired() && moduleEntry.getIsDefault()) {
						fileWriter.write(" isDefault=\"true\"");
					}
					fileWriter.write(">" + (moduleEntry.getRequired() ? "true" : "false") + "</Required>");
					fileWriter.newLine();
					fileWriter.write("\t\t\t<ModType");
					if (moduleEntry.getInRoot()) {
						fileWriter.write(" inRoot=\"true\"");
					}
					if (moduleEntry.getJarOrder() > 0) {
						fileWriter.write(" order=\"" + moduleEntry.getJarOrder() + "\"");
					}
					if (moduleEntry.getKeepMeta()) {
						fileWriter.write(" keepMeta=\"true\"");
					}
					if (!moduleEntry.getLaunchArgs().isEmpty()) {
						fileWriter.write(" launchArgs=\"" + xmlEscape(moduleEntry.getLaunchArgs()) + "\"");
					}
					if (!moduleEntry.getJreArgs().isEmpty()) {
						fileWriter.write(" jreArgs=\"" + xmlEscape(moduleEntry.getJreArgs()));
					}
					fileWriter.write(">" + moduleEntry.getModType().toString() + "</ModType>");
					fileWriter.newLine();
					if (!moduleEntry.getMD5().isEmpty()) {
						fileWriter.write("\t\t\t<MD5>" + moduleEntry.getMD5() + "</MD5>");
						fileWriter.newLine();
					}
					if (moduleEntry.getMeta().size() > 0) {
						fileWriter.write("\t\t\t<Meta>");
						fileWriter.newLine();
						for (Map.Entry<String, String> metaEntry : moduleEntry.getMeta().entrySet()) {
							fileWriter.write("\t\t\t\t<" + xmlEscape(metaEntry.getKey()) + ">" + xmlEscape(metaEntry.getValue()) + "</" + xmlEscape(metaEntry.getKey()) + ">");
							fileWriter.newLine();
						}
						fileWriter.write("\t\t\t</Meta>");
						fileWriter.newLine();
					}
					for (GenericModule submodule : moduleEntry.getSubmodules()) {
						fileWriter.write("\t\t\t<Submodule name=\"" + xmlEscape(submodule.getName()) + "\" id=\"" + submodule.getId() + "\" depends=\"" + submodule.getDepends() + "\" side=\"" + submodule.getSide() + "\">");
						fileWriter.newLine();
						for (PrioritizedURL url : submodule.getPrioritizedUrls()) {
							fileWriter.write("\t\t\t\t<URL priority=\"" + url.getPriority() + "\">" + xmlEscape(url.getUrl()) + "</URL>");
							fileWriter.newLine();
						}
						fileWriter.write("\t\t\t\t<Required");
						if (!submodule.getRequired() && submodule.getIsDefault()) {
							fileWriter.write(" isDefault=\"true\"");
						}
						fileWriter.write(">" + (submodule.getRequired() ? "true" : "false") + "</Required>");
						fileWriter.newLine();
						fileWriter.write("<ModType");
						if (submodule.getInRoot()) {
							fileWriter.write(" inRoot=\"true\"");
						}
						if (submodule.getJarOrder() > 0) {
							fileWriter.write(" order=\"" + submodule.getJarOrder() + "\"");
						}
						if (submodule.getKeepMeta()) {
							fileWriter.write(" keepMeta=\"true\"");
						}
						if (!submodule.getLaunchArgs().isEmpty()) {
							fileWriter.write(" launchArgs=\"" + xmlEscape(submodule.getLaunchArgs()) + "\"");
						}
						if (!submodule.getJreArgs().isEmpty()) {
							fileWriter.write(" jreArgs=\"" + xmlEscape(submodule.getJreArgs()));
						}
						fileWriter.write(">" + submodule.getModType().toString() + "</ModType>");
						fileWriter.newLine();
						fileWriter.write("\t\t\t\t<MD5>" + submodule.getMD5() + "</MD5>");
						fileWriter.newLine();
						if (submodule.getMeta().size() > 0) {
							fileWriter.write("\t\t\t\t<Meta>");
							fileWriter.newLine();
							for (Map.Entry<String, String> metaEntry : submodule.getMeta().entrySet()) {
								fileWriter.write("\t\t\t\t\t<" + xmlEscape(metaEntry.getKey()) + ">" + xmlEscape(metaEntry.getValue()) + "</" + xmlEscape(metaEntry.getKey()) + ">");
								fileWriter.newLine();
							}
							fileWriter.write("\t\t\t\t</Meta>");
							fileWriter.newLine();
						}
						fileWriter.write("\t\t\t</Submodule>");
						fileWriter.newLine();
					}
				} else {
					fileWriter.write("\t\t\t<Required");
					if (!moduleEntry.getRequired() && moduleEntry.getIsDefault()) {
						fileWriter.write(" isDefault=\"true\"");
					}
					fileWriter.write(">" + (moduleEntry.getRequired() ? "true" : "false") + "</Required>");
					fileWriter.newLine();
					fileWriter.write("\t\t\t<ModType>Override</ModType>");
					fileWriter.newLine();
				}
				for (ConfigFile config : moduleEntry.getConfigs()) {
					fileWriter.write("\t\t\t<ConfigFile>");
					fileWriter.newLine();
					fileWriter.write("\t\t\t\t<URL>" + xmlEscape(config.getUrl()) + "</URL>");
					fileWriter.newLine();
					fileWriter.write("\t\t\t\t<Path>" + xmlEscape(config.getPath()) + "</Path>");
					fileWriter.newLine();
					fileWriter.write("\t\t\t\t<NoOverwrite>" + config.isNoOverwrite() + "</NoOverwrite>");
					fileWriter.newLine();
					fileWriter.write("\t\t\t\t<MD5>" + xmlEscape(config.getMD5()) + "</MD5>");
					fileWriter.newLine();
					fileWriter.write("\t\t\t</ConfigFile>");
					fileWriter.newLine();
				}
				fileWriter.write("\t\t</Module>");
				fileWriter.newLine();
			}
			fileWriter.write("\t</Server>");
			fileWriter.newLine();
			fileWriter.write("</ServerPack>");
			fileWriter.newLine();
			fileWriter.close();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	private String xmlEscape(String input) {
		String result;
		try {
			result = input.replace("&", "&amp;").replace("\"", "&quot;").replace("'", "&apos;").replace("<", "&lt;").replace(">", "&gt;");
		} catch (Exception e) {
			result = "!!!! Error !!!!";
		}
		return result;
	}

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

	public void assignConfigs(boolean debug) {
		System.out.println("Assigning configs to mods\n===============");
		//this.modules.get(0).setConfigs(tempConfigs);
		Soundex snd = new Soundex();
		int distance;
		for (ConfigFile config : tempConfigs) {
			System.out.println(config.getPath() + ":");
			Module tempModule = null;
			distance = 10000;
			String configName = config.getPath().substring(config.getPath().indexOf("/"), config.getPath().lastIndexOf("."));
			for (Module mod : modules.values()) {
				try {
					int newDistance = StringUtils.getLevenshteinDistance(configName, mod.getId());
					for (Map.Entry<String, String> exception : configExceptions.entrySet()) {
						if (config.getPath().contains(exception.getKey()) && mod.getId().matches(exception.getValue())) {
							newDistance -= 15;
						}
					}
					if (configName.toLowerCase().contains(mod.getId().toLowerCase())) {
						newDistance -= 10;
					}
					if (configName.toLowerCase().contains(mod.getId().toLowerCase().substring(0, mod.getId().length() < 3 ? mod.getId().length() : 3))) {
						newDistance -= 1;
					}
					if (snd.soundex(mod.getId()).equals(snd.soundex(configName))) {
						newDistance -= 10;
					} else if (snd.soundex(mod.getName()).equals(snd.soundex(configName))) {
						newDistance -= 10;
					}
					if (newDistance <= 5 || debug) {
						System.out.println("   >" + mod.getId() + " - " + newDistance + " (potential)");
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
		List<Module> sorted = new ArrayList<>(modules.values());
		Collections.sort(sorted, new ModuleComparator(ModuleComparator.Mode.IMPORTANCE));
		return sorted;
	}
}
