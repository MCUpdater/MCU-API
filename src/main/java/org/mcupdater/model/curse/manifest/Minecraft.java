package org.mcupdater.model.curse.manifest;

import java.util.ArrayList;
import java.util.List;
import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;
import org.apache.commons.lang3.builder.EqualsBuilder;
import org.apache.commons.lang3.builder.HashCodeBuilder;
import org.apache.commons.lang3.builder.ReflectionToStringBuilder;

public class Minecraft {

    @SerializedName("modLoaders")
    @Expose
    private List<ModLoader> modLoaders = new ArrayList<ModLoader>();
    @SerializedName("version")
    @Expose
    private String version;

    public List<ModLoader> getModLoaders() {
        return modLoaders;
    }

    public void setModLoaders(List<ModLoader> modLoaders) {
        this.modLoaders = modLoaders;
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
        return new HashCodeBuilder().append(modLoaders).append(version).toHashCode();
    }

    @Override
    public boolean equals(Object other) {
        if (other == this) {
            return true;
        }
        if ((other instanceof Minecraft) == false) {
            return false;
        }
        Minecraft rhs = ((Minecraft) other);
        return new EqualsBuilder().append(modLoaders, rhs.modLoaders).append(version, rhs.version).isEquals();
    }

}
