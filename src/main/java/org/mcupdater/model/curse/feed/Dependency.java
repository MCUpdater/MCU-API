package org.mcupdater.model.curse.feed;

import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;
import org.apache.commons.lang3.builder.EqualsBuilder;
import org.apache.commons.lang3.builder.HashCodeBuilder;
import org.apache.commons.lang3.builder.ReflectionToStringBuilder;

public class Dependency {

    @SerializedName("AddOnId")
    @Expose
    private Integer addOnId;
    @SerializedName("Type")
    @Expose
    private Integer type;

    public Integer getAddOnId() {
        return addOnId;
    }

    public void setAddOnId(Integer addOnId) {
        this.addOnId = addOnId;
    }

    public Integer getType() {
        return type;
    }

    public void setType(Integer type) {
        this.type = type;
    }

    @Override
    public String toString() {
        return ReflectionToStringBuilder.toStringExclude(this, new String[] {""});
    }

    @Override
    public int hashCode() {
        return new HashCodeBuilder().append(addOnId).append(type).toHashCode();
    }

    @Override
    public boolean equals(Object other) {
        if (other == this) {
            return true;
        }
        if ((other instanceof Dependency) == false) {
            return false;
        }
        Dependency rhs = ((Dependency) other);
        return new EqualsBuilder().append(addOnId, rhs.addOnId).append(type, rhs.type).isEquals();
    }

}
