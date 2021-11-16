package org.mcupdater.settings;

import org.mcupdater.model.JSON;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@JSON
public class Settings {
	public UUID getClientToken() {
		return clientToken;
	}

	public enum TextField {
		minMemory,
		maxMemory,
		permGen,
		resWidth,
		resHeight,
		jrePath,
		jvmOpts,
		instanceRoot,
		programWrapper,
		timeoutLength
	}

	private List<Profile> profiles = new ArrayList<>();
	private String lastProfile;
	private String minMemory;
	private String maxMemory;
	private String permGen;
	private int resWidth;
	private int resHeight;
	private boolean fullScreen;
	private String jrePath;
	private String jvmOpts;
	private String instanceRoot;
	private String programWrapper;
	private int timeoutLength;
	private boolean autoConnect;
	private boolean minecraftToConsole = true;
	private boolean minimizeOnLaunch;
	private List<String> packURLs = new ArrayList<>();
	private UUID clientToken = UUID.randomUUID();
	private boolean professionalMode;

	public List<Profile> getProfiles() {
		return profiles;
	}

	public void setProfiles(List<Profile> profiles) {
		this.profiles = profiles;
	}

	public String getLastProfile() {
		return lastProfile;
	}

	public void setLastProfile(String lastProfile) {
		this.lastProfile = lastProfile;
	}

	public String getMinMemory() {
		return minMemory;
	}

	public void setMinMemory(String minMemory) {
		this.minMemory = minMemory;
	}

	public String getMaxMemory() {
		return maxMemory;
	}

	public void setMaxMemory(String maxMemory) {
		this.maxMemory = maxMemory;
	}

	@Deprecated
	public String getPermGen() {
		return permGen;
	}

	@Deprecated
	public void setPermGen(String permGen) {
		this.permGen = permGen;
	}

	public int getResWidth() {
		return resWidth;
	}

	public void setResWidth(int resWidth) {
		this.resWidth = resWidth;
	}

	public int getResHeight() {
		return resHeight;
	}

	public void setResHeight(int resHeight) {
		this.resHeight = resHeight;
	}

	public boolean isFullScreen() {
		return fullScreen;
	}

	public void setFullScreen(boolean fullScreen) {
		this.fullScreen = fullScreen;
	}

	@Deprecated
	public String getJrePath() {
		return jrePath;
	}

	@Deprecated
	public void setJrePath(String jrePath) {
		this.jrePath = jrePath;
	}

	public String getJvmOpts() {
		return jvmOpts;
	}

	public void setJvmOpts(String jvmOpts) {
		this.jvmOpts = jvmOpts;
	}

	public String getInstanceRoot() {
		return instanceRoot;
	}

	public void setInstanceRoot(String instanceRoot) {
		this.instanceRoot = instanceRoot;
	}

	public String getProgramWrapper() {
		return programWrapper;
	}

	public void setProgramWrapper(String programWrapper) {
		this.programWrapper = programWrapper;
	}

	public int getTimeoutLength() {
		return timeoutLength;
	}

	public void setTimeoutLength(int timeoutLength) {
		this.timeoutLength = timeoutLength;
	}

	public boolean isAutoConnect() {
		return autoConnect;
	}

	public void setAutoConnect(boolean autoConnect) {
		this.autoConnect = autoConnect;
	}

	public boolean isMinimizeOnLaunch() {
		return minimizeOnLaunch;
	}

	public void setMinimizeOnLaunch(boolean minimizeOnLaunch) {
		this.minimizeOnLaunch = minimizeOnLaunch;
	}

	public boolean isMinecraftToConsole() {
		return minecraftToConsole;
	}

	public void setMinecraftToConsole(boolean minecraftToConsole) {
		this.minecraftToConsole = minecraftToConsole;
	}

	public List<String> getPackURLs() {
		return packURLs;
	}

	public void setPackURLs(List<String> packURLs) {
		this.packURLs = packURLs;
	}

	public void addPackURL(String newUrl) {
		this.packURLs.add(newUrl);
	}

	public void removePackUrl(String oldUrl) {
		this.packURLs.remove(oldUrl);
	}

	public synchronized void addOrReplaceProfile(Profile newProfile) {
		for (Profile entry : new ArrayList<>(this.profiles)) {
			if (entry.getName().equals(newProfile.getName())) {
				this.profiles.remove(entry);
			}
		}
		this.profiles.add(newProfile);
	}

	public void updateField(TextField field, String value) {
		switch(field){
			case instanceRoot:
				setInstanceRoot(value);
				break;
			case jrePath:
				setJrePath(value);
				break;
			case jvmOpts:
				setJvmOpts(value);
				break;
			case maxMemory:
				setMaxMemory(value);
				break;
			case minMemory:
				setMinMemory(value);
				break;
			case permGen:
				setPermGen(value);
				break;
			case programWrapper:
				setProgramWrapper(value);
				break;
			case resHeight:
				try {
					setResHeight(Integer.parseInt(value));
				} catch (Exception e) {
					// ignore errors
				}
				break;
			case resWidth:
				try {
					setResWidth(Integer.parseInt(value));
				} catch (Exception e) {
					// ignore errors
				}
				break;
			case timeoutLength:
				setTimeoutLength(Integer.parseInt(value));
				break;
			default:
				break;
		}
	}

	public Profile findProfile(String name) {
		for (Profile entry : this.profiles) {
			if (entry.getName().equals(name)) {
				return entry;
			}
		}
		return null;
	}

	public void removeProfile(String name) {
		this.profiles.remove(findProfile(name));
	}

	public boolean isProfessionalMode() {
		return professionalMode;
	}

	public void setProfessionalMode(boolean professionalMode) {
		this.professionalMode = professionalMode;
	}


}
