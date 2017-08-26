package org.mcupdater.model.curse.feed;

import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;
import org.apache.commons.lang3.builder.EqualsBuilder;
import org.apache.commons.lang3.builder.HashCodeBuilder;
import org.apache.commons.lang3.builder.ReflectionToStringBuilder;

public class CategorySection {

    @SerializedName("ID")
    @Expose
    private Integer iD;
    @SerializedName("GameID")
    @Expose
    private Integer gameID;
    @SerializedName("Name")
    @Expose
    private String name;
    @SerializedName("PackageType")
    @Expose
    private Integer packageType;
    @SerializedName("Path")
    @Expose
    private String path;
    @SerializedName("InitialInclusionPattern")
    @Expose
    private String initialInclusionPattern;
    @SerializedName("ExtraIncludePattern")
    @Expose
    private Object extraIncludePattern;

    public Integer getID() {
        return iD;
    }

    public void setID(Integer iD) {
        this.iD = iD;
    }

    public Integer getGameID() {
        return gameID;
    }

    public void setGameID(Integer gameID) {
        this.gameID = gameID;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public Integer getPackageType() {
        return packageType;
    }

    public void setPackageType(Integer packageType) {
        this.packageType = packageType;
    }

    public String getPath() {
        return path;
    }

    public void setPath(String path) {
        this.path = path;
    }

    public String getInitialInclusionPattern() {
        return initialInclusionPattern;
    }

    public void setInitialInclusionPattern(String initialInclusionPattern) {
        this.initialInclusionPattern = initialInclusionPattern;
    }

    public Object getExtraIncludePattern() {
        return extraIncludePattern;
    }

    public void setExtraIncludePattern(Object extraIncludePattern) {
        this.extraIncludePattern = extraIncludePattern;
    }

    @Override
    public String toString() {
        return ReflectionToStringBuilder.toStringExclude(this, new String[] {""});
    }

    @Override
    public int hashCode() {
        return new HashCodeBuilder().append(iD).append(gameID).append(name).append(packageType).append(path).append(initialInclusionPattern).append(extraIncludePattern).toHashCode();
    }

    @Override
    public boolean equals(Object other) {
        if (other == this) {
            return true;
        }
        if ((other instanceof CategorySection) == false) {
            return false;
        }
        CategorySection rhs = ((CategorySection) other);
        return new EqualsBuilder().append(iD, rhs.iD).append(gameID, rhs.gameID).append(name, rhs.name).append(packageType, rhs.packageType).append(path, rhs.path).append(initialInclusionPattern, rhs.initialInclusionPattern).append(extraIncludePattern, rhs.extraIncludePattern).isEquals();
    }

}
