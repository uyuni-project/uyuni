/**
 * Copyright (c) 2014 SUSE
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
package com.suse.scc.client;

import java.util.Properties;

/**
 * SCC configuration wrapper class.
 */
public class SCCConfig {

    // Valid keys
    public static final String SCHEMA = "schema";
    public static final String HOSTNAME = "hostname";
    public static final String ENCODED_CREDS = "encoded-creds";
    public static final String UUID = "uuid";

    // Default values
    private static final String DEFAULT_SCHEMA = "https://";
    private static final String DEFAULT_HOSTNAME = "scc.suse.com";

    // The properties object
    private Properties properties;

    // Singleton instance
    private static SCCConfig instance;

    /**
     * Default constructor.
     */
    private SCCConfig() {
        this.properties = new Properties();
    }

    /**
     * Returns the singleton instance.
     *
     * @return instance
     */
    public static SCCConfig getInstance() {
        if (instance == null) {
            instance = new SCCConfig();
        }
        return instance;
    }

    /**
     * Sets a preference given by key and value. Use one of the public key strings above.
     *
     * @param key
     * @param value
     */
    public void put(String key, String value) {
        properties.setProperty(key, value);
    }

    /**
     * Returns the configured hostname or "scc.suse.com".
     *
     * @return hostname
     */
    public String getHostname() {
        return properties.getProperty(HOSTNAME, DEFAULT_HOSTNAME);
    }

    /**
     * Returns the configured schema or "https://".
     *
     * @return schema
     */
    public String getSchema() {
        return properties.getProperty(SCHEMA, DEFAULT_SCHEMA);
    }

    /**
     * Returns the encoded credentials or null.
     *
     * @return credentials
     */
    public String getEncodedCredentials() {
        return properties.getProperty(ENCODED_CREDS, null);
    }

    /**
     * Returns the UUID or null.
     *
     * @return UUID
     */
    public String getUUID() {
        return properties.getProperty(UUID, null);
    }
}
