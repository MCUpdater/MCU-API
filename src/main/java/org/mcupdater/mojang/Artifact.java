package org.mcupdater.mojang;

public class Artifact {
    private String path;
    private String sha1;
    private Integer size;
    private String url;

    public String getPath() {
        return path;
    }

    public String getSha1() {
        return sha1;
    }

    public Integer getSize() {
        return size;
    }

    public String getUrl() {
        return url;
    }
}
