package com.suse.oval.config;

import static com.suse.utils.Json.GSON;

import com.redhat.rhn.testing.TestUtils;

import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.net.URISyntaxException;
import java.net.URL;

public class OVALConfigLoader {
    public static OVALConfig load() {
        URL jsonConfigFile;
        try {
            jsonConfigFile = TestUtils.findTestData("/com/suse/oval/oval.config.json");
            return GSON.fromJson(new FileReader(new File(jsonConfigFile.toURI())), OVALConfig.class);
        } catch (ClassNotFoundException | IOException | URISyntaxException e) {
            throw new RuntimeException(e);
        }
    }
}
