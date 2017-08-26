package org.mcupdater.model.curse.feed;

import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;
import org.apache.commons.lang3.builder.EqualsBuilder;
import org.apache.commons.lang3.builder.HashCodeBuilder;
import org.apache.commons.lang3.builder.ReflectionToStringBuilder;

public class Category {

    @SerializedName("Id")
    @Expose
    private Integer id;
    @SerializedName("Name")
    @Expose
    private String name;
    @SerializedName("URL")
    @Expose
    private String uRL;

    public Integer getId() {
        return id;
    }

    public void setId(Integer id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getURL() {
        return uRL;
    }

    public void setURL(String uRL) {
        this.uRL = uRL;
    }

    @Override
    public String toString() {
        return ReflectionToStringBuilder.toStringExclude(this, new String[] {""});
    }

    @Override
    public int hashCode() {
        return new HashCodeBuilder().append(id).append(name).append(uRL).toHashCode();
    }

    @Override
    public boolean equals(Object other) {
        if (other == this) {
            return true;
        }
        if ((other instanceof Category) == false) {
            return false;
        }
        Category rhs = ((Category) other);
        return new EqualsBuilder().append(id, rhs.id).append(name, rhs.name).append(uRL, rhs.uRL).isEquals();
    }

}
