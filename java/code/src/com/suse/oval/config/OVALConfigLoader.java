/*
 * Copyright (c) 2023 SUSE LLC
 *
 * This software is licensed to you under the GNU General Public License,
 * version 2 (GPLv2). There is NO WARRANTY for this software, express or
 * implied, including the implied warranties of MERCHANTABILITY or FITNESS
 * FOR A PARTICULAR PURPOSE. You should have received a copy of GPLv2
 * along with this software; if not, see
 * http://www.gnu.org/licenses/old-licenses/gpl-2.0.txt.
 *
 * Red Hat trademarks are not licensed under GPLv2. No permission is
 * granted to use or replicate Red Hat trademarks that are incorporated
 * in this software or its documentation.
 */

package com.suse.oval.config;

import static com.suse.utils.Json.GSON;

import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.util.Objects;

public class OVALConfigLoader {
    private static final String DEFAULT_CONFIG_PATH = "/usr/share/susemanager/scc/oval.config.json";
    private final String configPath;

    /**
     * Default constructor
     * @param configPathIn the path of oval.config.json
     * */
    public OVALConfigLoader(String configPathIn) {
        Objects.requireNonNull(configPathIn);

        this.configPath = configPathIn;
    }

    /**
     * Empty constructor
     * */
    public OVALConfigLoader() {
        this(DEFAULT_CONFIG_PATH);
    }

    /**
     * Reads {@code oval.config.json} from the given path, parses it and return it as a {@link OVALConfig} object.
     *
     * @return A configuration object that corresponds to {@code oval.config.json}
     * */
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

    /**
     * Loads the OVAL configuration file from the default path.
     *
     * @return the default OVAL config object.
     * */
    public static OVALConfig loadDefaultConfig() {
        return new OVALConfigLoader().load();
    }
}
