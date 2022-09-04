package org.mcupdater.mojang;

import com.google.gson.annotations.SerializedName;

public class Classifiers {
    @SerializedName("natives-linux")
    private Artifact linux;

    @SerializedName("natives-macos")
    private Artifact macos;

    @SerializedName("natives-windows")
    private Artifact windows;

    public Artifact getNatives() {
        return switch (OperatingSystem.getCurrentPlatform()) {
            default -> null;
            case OSX -> macos;
            case LINUX -> linux;
            case WINDOWS -> windows;
        };
    }
}
