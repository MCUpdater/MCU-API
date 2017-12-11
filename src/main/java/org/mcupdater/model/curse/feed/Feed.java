package org.mcupdater.model.curse.feed;

import java.util.ArrayList;
import java.util.List;
import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;
import org.apache.commons.lang3.builder.EqualsBuilder;
import org.apache.commons.lang3.builder.HashCodeBuilder;
import org.apache.commons.lang3.builder.ReflectionToStringBuilder;

public class Feed {

    @SerializedName("timestamp")
    @Expose
    private Long timestamp;
    @SerializedName("data")
    @Expose
    private List<Project> projects = new ArrayList<Project>();

    public Long getTimestamp() {
        return timestamp;
    }

    public void setTimestamp(Long timestamp) {
        this.timestamp = timestamp;
    }

    public List<Project> getProjects() {
        return projects;
    }

    public void setProjects(List<Project> projects) {
        this.projects = projects;
    }

    @Override
    public String toString() {
        return ReflectionToStringBuilder.toStringExclude(this, new String[] {""});
    }

    @Override
    public int hashCode() {
        return new HashCodeBuilder().append(timestamp).append(projects).toHashCode();
    }

    @Override
    public boolean equals(Object other) {
        if (other == this) {
            return true;
        }
        if ((other instanceof Feed) == false) {
            return false;
        }
        Feed rhs = ((Feed) other);
        return new EqualsBuilder().append(timestamp, rhs.timestamp).append(projects, rhs.projects).isEquals();
    }

}
