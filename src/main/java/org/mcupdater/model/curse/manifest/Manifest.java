package org.mcupdater.model.curse.manifest;

import java.util.ArrayList;
import java.util.List;
import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;
import org.apache.commons.lang3.builder.EqualsBuilder;
import org.apache.commons.lang3.builder.HashCodeBuilder;
import org.apache.commons.lang3.builder.ReflectionToStringBuilder;

public class Manifest {

    @SerializedName("author")
    @Expose
    private String author;
    @SerializedName("files")
    @Expose
    private List<File> files = new ArrayList<File>();
    @SerializedName("manifestType")
    @Expose
    private String manifestType;
    @SerializedName("manifestVersion")
    @Expose
    private Integer manifestVersion;
    @SerializedName("minecraft")
    @Expose
    private Minecraft minecraft;
    @SerializedName("name")
    @Expose
    private String name;
    @SerializedName("overrides")
    @Expose
    private String overrides;
    @SerializedName("projectID")
    @Expose
    private Integer projectID;
    @SerializedName("version")
    @Expose
    private String version;

    public String getAuthor() {
        return author;
    }

    public void setAuthor(String author) {
        this.author = author;
    }

    public List<File> getFiles() {
        return files;
    }

    public void setFiles(List<File> files) {
        this.files = files;
    }

    public String getManifestType() {
        return manifestType;
    }

    public void setManifestType(String manifestType) {
        this.manifestType = manifestType;
    }

    public Integer getManifestVersion() {
        return manifestVersion;
    }

    public void setManifestVersion(Integer manifestVersion) {
        this.manifestVersion = manifestVersion;
    }

    public Minecraft getMinecraft() {
        return minecraft;
    }

    public void setMinecraft(Minecraft minecraft) {
        this.minecraft = minecraft;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getOverrides() {
        return overrides;
    }

    public void setOverrides(String overrides) {
        this.overrides = overrides;
    }

    public Integer getProjectID() {
        return projectID;
    }

    public void setProjectID(Integer projectID) {
        this.projectID = projectID;
    }

    public String getVersion() {
        return version;
    }

    public void setVersion(String version) {
        this.version = version;
    }

    @Override
    public String toString() {
        return ReflectionToStringBuilder.toStringExclude(this, new String[] {""});
    }

    @Override
    public int hashCode() {
        return new HashCodeBuilder().append(author).append(files).append(manifestType).append(manifestVersion).append(minecraft).append(name).append(overrides).append(projectID).append(version).toHashCode();
    }

    @Override
    public boolean equals(Object other) {
        if (other == this) {
            return true;
        }
        if ((other instanceof Manifest) == false) {
            return false;
        }
        Manifest rhs = ((Manifest) other);
        return new EqualsBuilder().append(author, rhs.author).append(files, rhs.files).append(manifestType, rhs.manifestType).append(manifestVersion, rhs.manifestVersion).append(minecraft, rhs.minecraft).append(name, rhs.name).append(overrides, rhs.overrides).append(projectID, rhs.projectID).append(version, rhs.version).isEquals();
    }

}
