package com.suse.oval.config;

import static com.suse.utils.Json.GSON;

import java.io.File;
import java.io.FileReader;
import java.io.IOException;

public class OVALConfigLoader {
    public static OVALConfig load() {
        File jsonConfigFile;
        try {
            jsonConfigFile = new File("/usr/share/susemanager/scc/oval.config.json");
            return GSON.fromJson(new FileReader(jsonConfigFile), OVALConfig.class);
        }
        catch (IOException e) {
            throw new RuntimeException("Failed to load OVAL config file", e);
        }
    }
}
