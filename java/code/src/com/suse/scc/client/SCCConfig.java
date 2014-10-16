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

import java.net.URI;
import java.net.URISyntaxException;
import java.util.Properties;

/**
 * SCC configuration wrapper class.
 */
public class SCCConfig {

    // Valid keys
    public static final String URL = "url";
    public static final String ENCODED_CREDS = "encoded-creds";
    public static final String UUID = "uuid";
    public static final String RESOURCE_PATH = "scc-data-path";

    // Proxy settings
    public static final String PROXY_HOSTNAME = "proxy-hostname";
    public static final String PROXY_PORT = "proxy-port";
    public static final String PROXY_USERNAME = "proxy-username";
    public static final String PROXY_PASSWORD = "proxy-password";

    // Default values
    protected static final String DEFAULT_URL = "https://scc.suse.com";
    private static final String DEFAULT_PROXY_PORT = "3128";

    // The properties object
    private final Properties properties;

    /**
     * Default constructor.
     */
    public SCCConfig() {
        this.properties = new Properties();
    }

    /**
     * Sets a preference given by key and value. Use one of the public key strings above.
     * NOTE: To set the URL, use method setURL instead.
     *
     * @param key
     * @param value
     */
    public void put(String key, String value) {
        // The URL key should be treated differently.
        if (key.equals(SCCConfig.URL)) {
            try {
                this.setUrl(key);
            } catch (URISyntaxException ex) {
                // Complain to the log for invalid URL.
                throw new RuntimeException(ex);
            }
        } else {
            properties.setProperty(key, value);
        }
    }

    /**
     * Removes a preference given by key.
     *
     * @param key
     */
    public void remove(String key) {
        if (properties.containsKey(key)) {
            properties.remove(key);
        }
    }

    /**
     * Set the URL parameter
     *
     * @param url
     * @throws URISyntaxException
     */
    public void setUrl(String url) throws URISyntaxException {
        properties.setProperty(URL, new URI(url).toASCIIString());
    }

    /**
     * Returns the configured url or "https://scc.suse.com".
     *
     * @return url
     */
    public String getUrl() {
        return properties.getProperty(URL, DEFAULT_URL);
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

    /**
     * Returns the proxy hostname or null.
     *
     * @return proxy hostname
     */
    public String getProxyHostname() {
        return properties.getProperty(PROXY_HOSTNAME, null);
    }

     /**
     * Returns the configured proxy port or 3128 as default
     *
     * @return proxy port
     */
    public String getProxyPort() {
        return properties.getProperty(PROXY_PORT, DEFAULT_PROXY_PORT);
    }

    /**
     * Returns the proxy username or null.
     *
     * @return proxy username
     */
    public String getProxyUsername() {
        return properties.getProperty(PROXY_USERNAME, null);
    }

    /**
     * Returns the proxy password or null.
     *
     * @return proxy password
     */
    public String getProxyPassword() {
       return properties.getProperty(PROXY_PASSWORD, null);
    }

    /**
     * Returns the local path of the directory for resources. Null, if is not set.
     * @return directory path
     */
    public String getLocalResourcePath() {
        return properties.getProperty(SCCConfig.RESOURCE_PATH);
    }
}
