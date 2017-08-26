package org.mcupdater.model.curse.feed;

import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;
import org.apache.commons.lang3.builder.EqualsBuilder;
import org.apache.commons.lang3.builder.HashCodeBuilder;
import org.apache.commons.lang3.builder.ReflectionToStringBuilder;

public class GameVersionLatestFile {

    @SerializedName("GameVesion")
    @Expose
    private String gameVesion;
    @SerializedName("ProjectFileID")
    @Expose
    private Integer projectFileID;
    @SerializedName("ProjectFileName")
    @Expose
    private String projectFileName;
    @SerializedName("FileType")
    @Expose
    private Integer fileType;

    public String getGameVesion() {
        return gameVesion;
    }

    public void setGameVesion(String gameVesion) {
        this.gameVesion = gameVesion;
    }

    public Integer getProjectFileID() {
        return projectFileID;
    }

    public void setProjectFileID(Integer projectFileID) {
        this.projectFileID = projectFileID;
    }

    public String getProjectFileName() {
        return projectFileName;
    }

    public void setProjectFileName(String projectFileName) {
        this.projectFileName = projectFileName;
    }

    public Integer getFileType() {
        return fileType;
    }

    public void setFileType(Integer fileType) {
        this.fileType = fileType;
    }

    @Override
    public String toString() {
        return ReflectionToStringBuilder.toStringExclude(this, new String[] {""});
    }

    @Override
    public int hashCode() {
        return new HashCodeBuilder().append(gameVesion).append(projectFileID).append(projectFileName).append(fileType).toHashCode();
    }

    @Override
    public boolean equals(Object other) {
        if (other == this) {
            return true;
        }
        if ((other instanceof GameVersionLatestFile) == false) {
            return false;
        }
        GameVersionLatestFile rhs = ((GameVersionLatestFile) other);
        return new EqualsBuilder().append(gameVesion, rhs.gameVesion).append(projectFileID, rhs.projectFileID).append(projectFileName, rhs.projectFileName).append(fileType, rhs.fileType).isEquals();
    }

}
