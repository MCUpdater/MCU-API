package org.mcupdater.model;

public class CurseProject {
	public enum ReleaseType {
		ALPHA(0),BETA(1),RELEASE(2);
		public final int val;
		
		private ReleaseType(int val) {
			this.val = val;
		}
		
		public boolean worseThan(ReleaseType that) {
			return this.val < that.val;
		}
		
		public static ReleaseType parse(String typeStr) {
			try {
				if( typeStr == null || typeStr.isEmpty() ) {
					typeStr = "RELEASE";
				}
				return valueOf(typeStr.toUpperCase());
			} catch (IllegalArgumentException e) {
				return RELEASE;
			}
		}
	}
	
	private String mcVersion;
	private String project;
	private int file = 0;
	private ReleaseType type = ReleaseType.RELEASE;
	private boolean autoupgrade = false;
	
	private String url = "";
	private String md5 = "";
	
	public CurseProject(String project, String mcVersion) {
		this.setProject(project);
		this.setMCVersion(mcVersion);
	}
	public CurseProject(String project, String mcVersion, int file, String typeStr, boolean autoupgrade) {
		this(project, mcVersion);
		this.setFile(file);
		this.setReleaseType(typeStr);
		this.setAutoUpgrade(autoupgrade);
	}

	public CurseProject(String project, int file, ReleaseType type, boolean autoupgrade) {
		this.setProject(project);
		this.setFile(file);
		this.setReleaseType(type);
		this.setAutoUpgrade(autoupgrade);
	}
	
	public String toString() {
		return "curse:"+getProject();
	}
	
	public String getMCVersion() {
		return mcVersion;
	}
	public void setMCVersion(String mcVersion) {
		this.mcVersion = mcVersion;
	}
	
	public String getProject() {
		return project;
	}
	public void setProject(String project) {
		this.project = project;
	}
	
	public int getFile() {
		return file;
	}
	public void setFile(int file) {
		this.file = file;
	}
	
	public ReleaseType getReleaseType() {
		return type;
	}
	public void setReleaseType(ReleaseType type) {
		this.type = type;
	}
	public void setReleaseType(String typeStr) {
		this.type = ReleaseType.parse(typeStr);
	}
	
	public boolean getAutoUpgrade() {
		return autoupgrade;
	}
	public void setAutoUpgrade(boolean autoupgrade) {
		this.autoupgrade = autoupgrade;
	}
	
	public String getURL() {
		return url;
	}
	public void setURL(String url) {
		this.url = url;
	}
	
	public String getMD5() {
		return md5;
	}
	public void setMD5(String md5) {
		this.md5 = md5;
	}
}
