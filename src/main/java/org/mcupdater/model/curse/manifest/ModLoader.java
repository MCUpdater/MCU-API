package org.mcupdater.model.curse.manifest;

import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;
import org.apache.commons.lang3.builder.EqualsBuilder;
import org.apache.commons.lang3.builder.HashCodeBuilder;
import org.apache.commons.lang3.builder.ReflectionToStringBuilder;

public class ModLoader {

    @SerializedName("id")
    @Expose
    private String id;
    @SerializedName("primary")
    @Expose
    private Boolean primary;

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public Boolean getPrimary() {
        return primary;
    }

    public void setPrimary(Boolean primary) {
        this.primary = primary;
    }

    @Override
    public String toString() {
        return ReflectionToStringBuilder.toStringExclude(this, new String[] {""});
    }

    @Override
    public int hashCode() {
        return new HashCodeBuilder().append(id).append(primary).toHashCode();
    }

    @Override
    public boolean equals(Object other) {
        if (other == this) {
            return true;
        }
        if ((other instanceof ModLoader) == false) {
            return false;
        }
        ModLoader rhs = ((ModLoader) other);
        return new EqualsBuilder().append(id, rhs.id).append(primary, rhs.primary).isEquals();
    }

}
