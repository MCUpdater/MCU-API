package org.mcupdater.model.curse.feed;

import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;
import org.apache.commons.lang3.builder.EqualsBuilder;
import org.apache.commons.lang3.builder.HashCodeBuilder;
import org.apache.commons.lang3.builder.ReflectionToStringBuilder;

public class Module {

    @SerializedName("Foldername")
    @Expose
    private String foldername;
    @SerializedName("Fingerprint")
    @Expose
    private Integer fingerprint;

    public String getFoldername() {
        return foldername;
    }

    public void setFoldername(String foldername) {
        this.foldername = foldername;
    }

    public Integer getFingerprint() {
        return fingerprint;
    }

    public void setFingerprint(Integer fingerprint) {
        this.fingerprint = fingerprint;
    }

    @Override
    public String toString() {
        return ReflectionToStringBuilder.toStringExclude(this, new String[] {""});
    }

    @Override
    public int hashCode() {
        return new HashCodeBuilder().append(foldername).append(fingerprint).toHashCode();
    }

    @Override
    public boolean equals(Object other) {
        if (other == this) {
            return true;
        }
        if ((other instanceof Module) == false) {
            return false;
        }
        Module rhs = ((Module) other);
        return new EqualsBuilder().append(foldername, rhs.foldername).append(fingerprint, rhs.fingerprint).isEquals();
    }

}
