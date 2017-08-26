package org.mcupdater.model.curse.feed;

import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;
import org.apache.commons.lang3.builder.EqualsBuilder;
import org.apache.commons.lang3.builder.HashCodeBuilder;
import org.apache.commons.lang3.builder.ReflectionToStringBuilder;

public class Attachment {

    @SerializedName("Description")
    @Expose
    private String description;
    @SerializedName("IsDefault")
    @Expose
    private Boolean isDefault;
    @SerializedName("ThumbnailUrl")
    @Expose
    private String thumbnailUrl;
    @SerializedName("Title")
    @Expose
    private String title;
    @SerializedName("Url")
    @Expose
    private String url;

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public Boolean getIsDefault() {
        return isDefault;
    }

    public void setIsDefault(Boolean isDefault) {
        this.isDefault = isDefault;
    }

    public String getThumbnailUrl() {
        return thumbnailUrl;
    }

    public void setThumbnailUrl(String thumbnailUrl) {
        this.thumbnailUrl = thumbnailUrl;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getUrl() {
        return url;
    }

    public void setUrl(String url) {
        this.url = url;
    }

    @Override
    public String toString() {
        return ReflectionToStringBuilder.toStringExclude(this, new String[] {""});
    }

    @Override
    public int hashCode() {
        return new HashCodeBuilder().append(description).append(isDefault).append(thumbnailUrl).append(title).append(url).toHashCode();
    }

    @Override
    public boolean equals(Object other) {
        if (other == this) {
            return true;
        }
        if ((other instanceof Attachment) == false) {
            return false;
        }
        Attachment rhs = ((Attachment) other);
        return new EqualsBuilder().append(description, rhs.description).append(isDefault, rhs.isDefault).append(thumbnailUrl, rhs.thumbnailUrl).append(title, rhs.title).append(url, rhs.url).isEquals();
    }

}
