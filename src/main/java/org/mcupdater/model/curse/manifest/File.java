package org.mcupdater.model.curse.manifest;

import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;
import org.apache.commons.lang3.builder.EqualsBuilder;
import org.apache.commons.lang3.builder.HashCodeBuilder;
import org.apache.commons.lang3.builder.ReflectionToStringBuilder;

public class File {

    @SerializedName("fileID")
    @Expose
    private Integer fileID;
    @SerializedName("projectID")
    @Expose
    private Integer projectID;
    @SerializedName("required")
    @Expose
    private Boolean required;

    public Integer getFileID() {
        return fileID;
    }

    public void setFileID(Integer fileID) {
        this.fileID = fileID;
    }

    public Integer getProjectID() {
        return projectID;
    }

    public void setProjectID(Integer projectID) {
        this.projectID = projectID;
    }

    public Boolean getRequired() {
        return required;
    }

    public void setRequired(Boolean required) {
        this.required = required;
    }

    @Override
    public String toString() {
        return ReflectionToStringBuilder.toStringExclude(this, new String[] {""});
    }

    @Override
    public int hashCode() {
        return new HashCodeBuilder().append(fileID).append(projectID).append(required).toHashCode();
    }

    @Override
    public boolean equals(Object other) {
        if (other == this) {
            return true;
        }
        if ((other instanceof File) == false) {
            return false;
        }
        File rhs = ((File) other);
        return new EqualsBuilder().append(fileID, rhs.fileID).append(projectID, rhs.projectID).append(required, rhs.required).isEquals();
    }

}
