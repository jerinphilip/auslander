package com.github.jerinphilip.auslander;

import java.util.Map;

public class Model {
    private final String modelName;
    private final String shortName;
    private final String type;
    private final String src;
    private final String trg;
    private final Map<String, String> srcTags;
    private final String trgTag;
    private final String repository;
    private final int version;
    private final int API;
    private final String checksum;
    private final String url;
    private final String name;
    private final String code;


    // Constructor
    public Model(String modelName, String shortName, String type, String src, String trg,
                 Map<String, String> srcTags, String trgTag, String repository,
                 int version, int API, String checksum, String url, String name, String code) {
        this.modelName = modelName;
        this.shortName = shortName;
        this.type = type;
        this.src = src;
        this.trg = trg;
        this.srcTags = srcTags;
        this.trgTag = trgTag;
        this.repository = repository;
        this.version = version;
        this.API = API;
        this.checksum = checksum;
        this.url = url;
        this.name = name;
        this.code = code;
    }

    // Getters
    public String getModelName() {
        return modelName;
    }

    public String getShortName() {
        return shortName;
    }

    public String getType() {
        return type;
    }

    public String getSrc() {
        return src;
    }

    public String getTrg() {
        return trg;
    }

    public Map<String, String> getSrcTags() {
        return srcTags;
    }

    public String getTrgTag() {
        return trgTag;
    }

    public String getRepository() {
        return repository;
    }

    public int getVersion() {
        return version;
    }

    public int getAPI() {
        return API;
    }

    public String getChecksum() {
        return checksum;
    }

    public String getUrl() {
        return url;
    }

    public String getName() {
        return name;
    }

    public String getCode() {
        return code;
    }
}
