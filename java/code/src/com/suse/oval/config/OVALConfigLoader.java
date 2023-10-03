package com.suse.oval.config;

import static com.suse.utils.Json.GSON;

import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.util.Objects;

public class OVALConfigLoader {
    private static final String DEFAULT_CONFIG_PATH = "/usr/share/susemanager/scc/oval.config.json";
    private final String configPath;

    public OVALConfigLoader(String configPathIn) {
        Objects.requireNonNull(configPathIn);

        this.configPath = configPathIn;
    }

    public OVALConfigLoader() {
        this(DEFAULT_CONFIG_PATH);
    }

    public OVALConfig load() {
        File jsonConfigFile;
        try {
            jsonConfigFile = new File(configPath);
            return GSON.fromJson(new FileReader(jsonConfigFile), OVALConfig.class);
        }
        catch (IOException e) {
            throw new RuntimeException("Failed to load OVAL config file", e);
        }
    }

    public static OVALConfig loadDefaultConfig() {
        return new OVALConfigLoader().load();
    }
}
