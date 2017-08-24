package org.mcupdater.model;

public class CurseProject {
	public enum ReleaseType {
		ALPHA,BETA,RELEASE;
	}
	
	private String project;
	private int file = 0;
	private ReleaseType type = ReleaseType.RELEASE;
	private boolean autoupgrade = false;
	
	public CurseProject(String project) {
		this.setProject(project);
	}
	public CurseProject(String project, int file, String typeStr, boolean autoupgrade) {
		this(project);
		this.setFile(file);
		this.setReleaseType(typeStr);
		this.setAutoUpgrade(autoupgrade);
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
		try {
			if( typeStr == null || typeStr.isEmpty() ) {
				typeStr = "RELEASE";
			}
			setReleaseType( ReleaseType.valueOf(typeStr.toUpperCase()) );
		} catch (IllegalArgumentException e) {
			setReleaseType( ReleaseType.RELEASE );
		}
	}
	
	public boolean getAutoUpgrade() {
		return autoupgrade;
	}
	public void setAutoUpgrade(boolean autoupgrade) {
		this.autoupgrade = autoupgrade;
	}
}
