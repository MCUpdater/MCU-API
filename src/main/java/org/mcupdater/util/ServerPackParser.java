package org.mcupdater.util;

import org.apache.commons.codec.binary.Base64;
import org.mcupdater.api.Version;
import org.mcupdater.model.*;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.SAXException;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.xpath.XPath;
import javax.xml.xpath.XPathConstants;
import javax.xml.xpath.XPathExpressionException;
import javax.xml.xpath.XPathFactory;
import java.io.File;
import java.io.IOException;
import java.net.*;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.logging.Level;

import static org.mcupdater.util.MCUpdater.apiLogger;

public class ServerPackParser {

	public static Document readXmlFromFile(File packFile) throws Exception
	{
		DocumentBuilderFactory dbf = DocumentBuilderFactory.newInstance();
		
		try {
			DocumentBuilder db = dbf.newDocumentBuilder();
			return db.parse(packFile);
		} catch(ParserConfigurationException | SAXException pce) {
			apiLogger.log(Level.SEVERE, "Parser error", pce);
		} catch(IOException ioe) {
			apiLogger.log(Level.SEVERE, "I/O error", ioe);
		}
		return null;
	}
	
	public static Document readXmlFromUrl(String serverUrl) throws Exception
	{
		apiLogger.fine("readXMLFromUrl(" + serverUrl + ")");
		if (serverUrl.equals("http://www.example.org/ServerPack.xml") || serverUrl.isEmpty()) {
			return null;
		}
		//_log("Reading "+serverUrl+"...");
		final URL server;
		try {
			server = new URL(serverUrl);
		} catch( MalformedURLException e ) {
			apiLogger.log(Level.WARNING, "Malformed URL", e);
			return null;
		}
		DocumentBuilderFactory dbf = DocumentBuilderFactory.newInstance();
		URLConnection serverConn = redirectAndConnect(server,null);
		try {
			DocumentBuilder db = dbf.newDocumentBuilder();
			return db.parse(serverConn.getInputStream());
		}catch(ParserConfigurationException | SAXException pce) {
			apiLogger.log(Level.SEVERE, "Parser error", pce);
		} catch(IOException ioe) {
			apiLogger.log(Level.SEVERE, "I/O error", ioe);
		}
		return null;
	}

	public static ServerList parseDocument(Document dom, String serverId, Map<String,Module> modList, String hierarchy, String version) throws Exception {
		//Map<String,Module> modList = new HashMap<>();
		Element parent = dom.getDocumentElement();
		ServerEntry server = getServerEntry(serverId, parent, version);
		ServerList sl = ServerList.fromElement(server.mcuVersion, "", server.serverElement);
		apiLogger.log(Level.FINE, serverId + ": format=" + server.packVersion);
		NodeList nl;
		switch (server.packVersion) {
		case 2:
			// Handle ServerPacks designed for MCUpdater 3.0 and later
			assert server.serverElement != null;
			nl = server.serverElement.getElementsByTagName("Import");
			if(nl != null && nl.getLength() > 0) {
				for(int i = 0; i < nl.getLength(); i++) {
					Element el = (Element)nl.item(i);
                    ServerList child = doImportV2(el, dom, sl, modList, hierarchy);
                    sl.getLibOverrides().putAll(child.getLibOverrides());
					//modList.putAll(child.getModules());
				}
			}
			nl = server.serverElement.getElementsByTagName("Module");
			if(nl != null && nl.getLength() > 0)
			{
				for(int i = 0; i < nl.getLength(); i++)
				{
					Element el = (Element)nl.item(i);
					Module m = getModuleV2(el, hierarchy);
					if (m.getModType() == ModType.Removal) {
						modList.remove(m.getId());
					}
					else if (m.getModType() == ModType.Override) {
						if (modList.containsKey(m.getId())) { // If modList does not contain the mod, ignore it.
							Module existing = modList.get(m.getId());
							existing.getPrioritizedUrls().addAll(m.getPrioritizedUrls());
							existing.getConfigs().addAll(m.getConfigs());
							existing.getSubmodules().addAll(m.getSubmodules());
							existing.setRequired(m.getRequired());
							existing.setIsDefault(m.getIsDefault());
						}
					} else {
						modList.put(m.getId(), m);
					}
				}
			}
            sl.setModules(modList);
			return sl;
			
		case 1:
			// Handle ServerPacks designed for MCUpdater 2.7 and earlier
			assert server.serverElement != null;
			nl = server.serverElement.getElementsByTagName("Module");
			if(nl != null && nl.getLength() > 0)
			{
				for(int i = 0; i < nl.getLength(); i++)
				{
					Element el = (Element)nl.item(i);
					Module m = getModuleV1(el, hierarchy);
					modList.put(m.getId(), m);
				}
			}
            sl.setModules(modList);
			return sl;

		default:
			return null;
		}
	}

	@Deprecated
	private static ServerEntry getServerEntry(String serverId, Element parent) {
		return getServerEntry(serverId, parent, null);
	}

	private static ServerEntry getServerEntry(String serverId, Element parent, String mcVersion) {
		int version;
		String mcuVersion;
		Element docEle = null;
		if (parent.getNodeName().equals("ServerPack")) {
			mcuVersion = parent.getAttribute("version");
			if (Version.requestedFeatureLevel(parent.getAttribute("version"), "3.0")) {
				version = 2;
			} else {
				version = 1;
			}
			NodeList servers = parent.getElementsByTagName("Server");
			for (int i = 0; i < servers.getLength(); i++) {
				docEle = (Element) servers.item(i);
				apiLogger.fine(docEle.getAttribute("id") + ":" + docEle.getAttribute("version"));
				if (docEle.getAttribute("id").equals(serverId) && (mcVersion == null || docEle.getAttribute("version").equals(mcVersion))) {
					break;
				}
			}
		} else {
			mcuVersion = "1.0";
			docEle = parent;
			version = 1;
		}
		return new ServerEntry(version, docEle, mcuVersion);
	}

	private static ServerList doImportV2(Element el, Document dom, ServerList parent, Map<String,Module> modList, String hierarchy) throws Exception {
		String url = el.getAttribute("url");
		if (!url.isEmpty()){
			try {
				dom = readXmlFromUrl(url);
			} catch (Exception e) {
				apiLogger.log(Level.SEVERE, e.getMessage(), e);
			}
		}
		ServerEntry server = getServerEntry(el.getTextContent(), dom.getDocumentElement(), parent.getVersion());
		ServerList child = ServerList.fromElement(server.mcuVersion, "", server.serverElement);
		if (!Version.fuzzyMatch(parent.getVersion(), child.getVersion())) {
			throw new Exception("Import " + (url.isEmpty() ? "" : url + ":") + el.getTextContent() + " failed version checking.");
		}
		return parseDocument(dom, el.getTextContent(), modList, hierarchy + "/" + el.getTextContent(), parent.getVersion());

	}

	private static Module getModuleV2(Element el, String hierarchy) {
		XPath xpath = XPathFactory.newInstance().newXPath();
		try {

			String name = el.getAttribute("name");
			String id = el.getAttribute("id");
			String depends = el.getAttribute("depends");
			String side = el.getAttribute("side");
			List<PrioritizedURL> urls = new ArrayList<>();
			NodeList nl;
			nl = (NodeList) xpath.evaluate("URL", el, XPathConstants.NODESET);
			for (int i = 0; i < nl.getLength(); i++) {
				Element elURL = (Element) nl.item(i);
				String url = elURL.getTextContent();
				int priority = parseInt(elURL.getAttribute("priority"));
				urls.add(new PrioritizedURL(url, priority));
			}
			String loadPrefix = (String) xpath.evaluate("LoadPrefix", el, XPathConstants.STRING);
			String path = (String) xpath.evaluate("ModPath", el, XPathConstants.STRING);
			String sizeString = (String) xpath.evaluate("Size", el, XPathConstants.STRING);
			if (sizeString.isEmpty()) {
				sizeString = "100000";
			}
			long size = Long.parseLong(sizeString);
			Element elReq = (Element) el.getElementsByTagName("Required").item(0);
			boolean required;
			boolean isDefault;
			if (elReq == null) {
				required = true;
				isDefault = true;
			} else {
				required = parseBooleanWithDefault(elReq.getTextContent(),true);
				isDefault = parseBooleanWithDefault(elReq.getAttribute("isDefault"),false);
			}
			Element elType = (Element) el.getElementsByTagName("ModType").item(0);
			boolean inRoot = parseBooleanWithDefault(elType.getAttribute("inRoot"), false);
			int order = parseInt(elType.getAttribute("order"));
			boolean keepMeta = parseBooleanWithDefault(elType.getAttribute("keepMeta"),false);
			String launchArgs = elType.getAttribute("launchArgs");
			String jreArgs = elType.getAttribute("jreArgs");
			ModType modType;
			modType = ModType.valueOf(elType.getTextContent());
			String md5 = (String) xpath.evaluate("MD5", el, XPathConstants.STRING);
			List<ConfigFile> configs = new ArrayList<>();
			List<GenericModule> submodules = new ArrayList<>();
			nl = el.getElementsByTagName("ConfigFile");
			for(int i = 0; i < nl.getLength(); i++) 
			{
				Element elConfig = (Element)nl.item(i);
				ConfigFile cf = getConfigFileV1(elConfig);
				configs.add(cf);
			}
			nl = el.getElementsByTagName("Submodule");
			for(int i = 0; i < nl.getLength(); i++)
			{
				Element elSubmod = (Element)nl.item(i);
				GenericModule sm = getModuleV2(elSubmod, hierarchy);
				submodules.add(sm);
			}
			HashMap<String,String> mapMeta = new HashMap<>();
			NodeList nlMeta = el.getElementsByTagName("Meta");
			if (nlMeta.getLength() > 0){
				Element elMeta = (Element) nlMeta.item(0);
				NodeList nlMetaChildren = elMeta.getElementsByTagName("*");
				for(int i = 0; i < nlMetaChildren.getLength(); i++)
				{
					Node child = nlMetaChildren.item(i);
					mapMeta.put(child.getNodeName(), getTextValue(elMeta, child.getNodeName()));
				}
			}
			Module out = new Module(name, id, urls, depends, required, modType, order, keepMeta, inRoot, isDefault, md5, configs, side, path, mapMeta, launchArgs, jreArgs, submodules, hierarchy);
			out.setLoadPrefix(loadPrefix);
			out.setFilesize(size);
			return out;
		} catch (XPathExpressionException e) {
			apiLogger.log(Level.SEVERE, e.getMessage(), e);
			return null;
		}
	}

	private static int parseInt(String attribute) {
		try {
			return Integer.parseInt(attribute);
		} catch (NumberFormatException e) {
			return 0;
		}
	}

	private static Module getModuleV1(Element modEl, String hierarchy)
	{
		String name = modEl.getAttribute("name");
		String id = modEl.getAttribute("id");
		PrioritizedURL url = new PrioritizedURL(getTextValue(modEl,"URL"),0);
		List<PrioritizedURL> urls = new ArrayList<>();
		urls.add(url);
		String path = getTextValue(modEl,"ModPath");
		String depends = modEl.getAttribute("depends");
		String side = modEl.getAttribute("side");
		Boolean required = getBooleanValue(modEl,"Required");
		Boolean isDefault = getBooleanValue(modEl,"IsDefault");
		Boolean inJar = getBooleanValue(modEl,"InJar");
		int jarOrder = getIntValue(modEl,"JarOrder");
		Boolean keepMeta = getBooleanValue(modEl,"KeepMeta");
		Boolean extract = getBooleanValue(modEl,"Extract");
		Boolean inRoot = getBooleanValue(modEl,"InRoot");
		Boolean coreMod = getBooleanValue(modEl,"CoreMod");
		String md5 = getTextValue(modEl,"MD5");
		List<ConfigFile> configs = new ArrayList<>();
		NodeList nl = modEl.getElementsByTagName("ConfigFile");
		for(int i = 0; i < nl.getLength(); i++) 
		{
			Element el = (Element)nl.item(i);
			ConfigFile cf = getConfigFileV1(el);
			configs.add(cf);
		}
		HashMap<String,String> mapMeta = new HashMap<>();
		NodeList nlMeta = modEl.getElementsByTagName("Meta");
		if (nlMeta.getLength() > 0){
			Element elMeta = (Element) nlMeta.item(0);
			NodeList nlMetaChildren = elMeta.getElementsByTagName("*");
			for(int i = 0; i < nlMetaChildren.getLength(); i++)
			{
				Node child = nlMetaChildren.item(i);
				mapMeta.put(child.getNodeName(), getTextValue(elMeta, child.getNodeName()));
			}
		}
		return new Module(name, id, urls, depends, required, inJar, jarOrder, keepMeta, extract, inRoot, isDefault, coreMod, md5, configs, side, path, mapMeta, "", "", hierarchy);
	}
	
	private static ConfigFile getConfigFileV1(Element cfEl)
	{
		String url = getTextValue(cfEl,"URL");
		String path = getTextValue(cfEl,"Path");
		String md5 = getTextValue(cfEl,"MD5");
		boolean noOverwrite = getBooleanValue(cfEl, "NoOverwrite");
		return new ConfigFile(url,path,noOverwrite,md5);
	}
	
	private static int getIntValue(Element ele, String tagName) {
		return parseInt(getTextValue(ele,tagName));
	}
	
	private static String getTextValue(Element ele, String tagName) {
		String textVal = null;
		NodeList nl = ele.getElementsByTagName(tagName);
		if(nl != null && nl.getLength() > 0) {
			Element el = (Element)nl.item(0);
			if(el != null) {
				Node node = el.getFirstChild();
				if(node != null) textVal = unescapeXML(node.getNodeValue());
			}
		}
		return textVal;
	}
	
	private static String unescapeXML(String nodeValue) {
		return nodeValue.replace("&amp;", "&").replace("&quot;", "\"").replace("&apos;","'").replace("&lt;", "<").replace("&gt;", ">");
	}

	private static Boolean getBooleanValue(Element ele, String tagName) {
		return parseBooleanWithDefault(getTextValue(ele, tagName), false);
	}

	private static Boolean parseBooleanWithDefault(String textValue, boolean state) {
		try {
			return Boolean.parseBoolean(textValue);
		} catch (Exception e) {
			return state;
		}
	}

	@SuppressWarnings("unused")
	public static ServerList loadFromFile(File packFile, String serverId) {
		try {
			ServerList server = parseDocument(readXmlFromFile(packFile), serverId, new HashMap<String, Module>(), serverId, null);
			if (server != null) {
				server.setPackUrl(packFile.toURI().toURL().toString());
			}
			return server;
		} catch (Exception e) {
			apiLogger.log(Level.SEVERE, e.getMessage(), e);
			return null;
		}
	}
	
	public static ServerList loadFromURL(String serverUrl, String serverId)
	{
		try {
			ServerList server = parseDocument(readXmlFromUrl(serverUrl), serverId, new HashMap<String, Module>(), serverId, null);
			if (server != null) {
				server.setPackUrl(serverUrl);
			}
			return server;
		} catch (Exception e) {
			apiLogger.log(Level.SEVERE, e.getMessage(), e);
			return null;
		}
	}

	public static boolean parseBoolean(String attribute, boolean defaultValue) {
		if (attribute.isEmpty()) {
			return defaultValue;
		}
		return !attribute.equalsIgnoreCase("false");
	}

	private static URLConnection redirectAndConnect(URL target, URL referer) throws IOException {
		if (target.getProtocol().equals("file")) {
			URLConnection conn = target.openConnection();
			conn.connect();
			return conn;
		}
		HttpURLConnection conn = (HttpURLConnection) target.openConnection();
		conn.setRequestProperty("User-Agent","MCUpdater/" + Version.API_VERSION);
/*
		if (tracker.getQueue().getMCUser() != null) {
			conn.setRequestProperty("MC-User", tracker.getQueue().getMCUser());
		}
*/
		if (referer != null) {
			conn.setRequestProperty("Referer", referer.toString());
		}
		if (target.getUserInfo() != null) {
			String basicAuth = "Basic " + new String(new Base64().encode(target.getUserInfo().getBytes()));
			conn.setRequestProperty("Authorization", basicAuth);
		}
		conn.setUseCaches(false);
		conn.setInstanceFollowRedirects(false);
		if (conn.getResponseCode() / 100 == 3) {
			return redirectAndConnect(new URL(conn.getHeaderField("Location")), target);
		}
		conn.connect();
		return conn;
	}

	public static class ServerEntry {
		public int packVersion;
		public Element serverElement;
		private String mcuVersion;

		public ServerEntry(int packVersion, Element serverElement, String mcuVersion) {
			this.packVersion = packVersion;
			this.serverElement = serverElement;
			this.mcuVersion = mcuVersion;
		}
	}
}
