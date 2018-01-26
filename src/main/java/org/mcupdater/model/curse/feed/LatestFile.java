package org.mcupdater.model.curse.feed;

import java.util.ArrayList;
import java.util.List;
import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;
import org.apache.commons.lang3.builder.EqualsBuilder;
import org.apache.commons.lang3.builder.HashCodeBuilder;
import org.apache.commons.lang3.builder.ReflectionToStringBuilder;

public class LatestFile {

    @SerializedName("Id")
    @Expose
    private Long id;
    @SerializedName("FileName")
    @Expose
    private String fileName;
    @SerializedName("FileNameOnDisk")
    @Expose
    private String fileNameOnDisk;
    @SerializedName("FileDate")
    @Expose
    private String fileDate;
    @SerializedName("ReleaseType")
    @Expose
    private Integer releaseType;
    @SerializedName("FileStatus")
    @Expose
    private Integer fileStatus;
    @SerializedName("DownloadURL")
    @Expose
    private String downloadURL;
    @SerializedName("IsAlternate")
    @Expose
    private Boolean isAlternate;
    @SerializedName("AlternateFileId")
    @Expose
    private Long alternateFileId;
    @SerializedName("Dependencies")
    @Expose
    private List<Dependency> dependencies = new ArrayList<Dependency>();
    @SerializedName("IsAvailable")
    @Expose
    private Boolean isAvailable;
    @SerializedName("Modules")
    @Expose
    private List<Module> modules = new ArrayList<Module>();
    @SerializedName("PackageFingerprint")
    @Expose
    private Long packageFingerprint;
    @SerializedName("GameVersion")
    @Expose
    private List<String> gameVersion = new ArrayList<String>();

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getFileName() {
        return fileName;
    }

    public void setFileName(String fileName) {
        this.fileName = fileName;
    }

    public String getFileNameOnDisk() {
        return fileNameOnDisk;
    }

    public void setFileNameOnDisk(String fileNameOnDisk) {
        this.fileNameOnDisk = fileNameOnDisk;
    }

    public String getFileDate() {
        return fileDate;
    }

    public void setFileDate(String fileDate) {
        this.fileDate = fileDate;
    }

    public Integer getReleaseType() {
        return releaseType;
    }

    public void setReleaseType(Integer releaseType) {
        this.releaseType = releaseType;
    }

    public Integer getFileStatus() {
        return fileStatus;
    }

    public void setFileStatus(Integer fileStatus) {
        this.fileStatus = fileStatus;
    }

    public String getDownloadURL() {
        return downloadURL;
    }

    public void setDownloadURL(String downloadURL) {
        this.downloadURL = downloadURL;
    }

    public Boolean getIsAlternate() {
        return isAlternate;
    }

    public void setIsAlternate(Boolean isAlternate) {
        this.isAlternate = isAlternate;
    }

    public Long getAlternateFileId() {
        return alternateFileId;
    }

    public void setAlternateFileId(Long alternateFileId) {
        this.alternateFileId = alternateFileId;
    }

    public List<Dependency> getDependencies() {
        return dependencies;
    }

    public void setDependencies(List<Dependency> dependencies) {
        this.dependencies = dependencies;
    }

    public Boolean getIsAvailable() {
        return isAvailable;
    }

    public void setIsAvailable(Boolean isAvailable) {
        this.isAvailable = isAvailable;
    }

    public List<Module> getModules() {
        return modules;
    }

    public void setModules(List<Module> modules) {
        this.modules = modules;
    }

    public Long getPackageFingerprint() {
        return packageFingerprint;
    }

    public void setPackageFingerprint(Long packageFingerprint) {
        this.packageFingerprint = packageFingerprint;
    }

    public List<String> getGameVersion() {
        return gameVersion;
    }

    public void setGameVersion(List<String> gameVersion) {
        this.gameVersion = gameVersion;
    }

    @Override
    public String toString() {
        return ReflectionToStringBuilder.toStringExclude(this, new String[] {""});
    }

    @Override
    public int hashCode() {
        return new HashCodeBuilder().append(id).append(fileName).append(fileNameOnDisk).append(fileDate).append(releaseType).append(fileStatus).append(downloadURL).append(isAlternate).append(alternateFileId).append(dependencies).append(isAvailable).append(modules).append(packageFingerprint).append(gameVersion).toHashCode();
    }

    @Override
    public boolean equals(Object other) {
        if (other == this) {
            return true;
        }
        if ((other instanceof LatestFile) == false) {
            return false;
        }
        LatestFile rhs = ((LatestFile) other);
        return new EqualsBuilder().append(id, rhs.id).append(fileName, rhs.fileName).append(fileNameOnDisk, rhs.fileNameOnDisk).append(fileDate, rhs.fileDate).append(releaseType, rhs.releaseType).append(fileStatus, rhs.fileStatus).append(downloadURL, rhs.downloadURL).append(isAlternate, rhs.isAlternate).append(alternateFileId, rhs.alternateFileId).append(dependencies, rhs.dependencies).append(isAvailable, rhs.isAvailable).append(modules, rhs.modules).append(packageFingerprint, rhs.packageFingerprint).append(gameVersion, rhs.gameVersion).isEquals();
    }

}
