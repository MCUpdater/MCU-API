package org.mcupdater.model;

import com.google.gson.annotations.SerializedName;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class FabricModInfo {
    public int schemaVersion;
    @SerializedName("id")
    public String modId;
    public String version;
    public String name;
    public String description;
    public List<String> authors = new ArrayList<>();
    public Map<String,String> contact = new HashMap<>();
    public String license;
    public String icon;
    public String environment;
    public Map<String,List<String>> entrypoints = new HashMap<>();
    ArrayList<String> mixins = new ArrayList<>();
    public Map<String,String> depends = new HashMap<>();
}
