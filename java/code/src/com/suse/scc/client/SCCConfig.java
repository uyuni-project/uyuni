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

/**
 * SCC configuration container class.
 */
public class SCCConfig {

    /** Default SCC URL. */
    private static final URI DEFAULT_URL;
    // Fairly complex (yet valid) initialization code for the constant
    static {
        URI temp = null;
        try {
            temp = new URI("https://scc.suse.com");
        }
        catch (URISyntaxException e) {
            // never happens
        }
        DEFAULT_URL = temp;
    }

    /** Default directory where to save logging files. */
    public static final String DEFAULT_LOGGING_DIR = "/var/lib/spacewalk/scc/scc-data/";

    /** The url. */
    private URI url;

    /** The username. */
    private String username;

    /** The password. */
    private String password;

    /** The client UUID for SCC debugging. */
    private String uuid;

    /** The local resource path for local access to SMT files. */
    private String localResourcePath;

    /** Path to the logging directory. */
    private String loggingDir;

    /** The proxy settings. */
    private SCCProxySettings proxySettings;

    /**
     * Instantiates a new SCC config to read from a local file and default
     * logging directory.
     * @param localResourcePathIn the local resource path
     */
    public SCCConfig(String localResourcePathIn) {
        this(DEFAULT_URL, null, null, null, localResourcePathIn, DEFAULT_LOGGING_DIR, null);
    }

    /**
     * Instantiates a new SCC config to read from SCC with default logging directory.
     * @param urlIn the url
     * @param usernameIn the username
     * @param passwordIn the password
     * @param uuidIn the UUID
     * @param proxySettingsIn the proxy settings
     */
    public SCCConfig(URI urlIn, String usernameIn, String passwordIn, String uuidIn,
            SCCProxySettings proxySettingsIn) {
        this(urlIn, usernameIn, passwordIn, uuidIn, null, DEFAULT_LOGGING_DIR,
                proxySettingsIn);
    }

    /**
     * Full constructor.
     * @param urlIn the url
     * @param usernameIn the username
     * @param passwordIn the password
     * @param uuidIn the UUID
     * @param localResourcePathIn the local resource path
     * @param loggingDirIn the logging dir
     * @param proxySettingsIn the proxy settings
     */
    public SCCConfig(URI urlIn, String usernameIn, String passwordIn, String uuidIn,
            String localResourcePathIn, String loggingDirIn,
            SCCProxySettings proxySettingsIn) {
        url = urlIn;
        username = usernameIn;
        password = passwordIn;
        uuid = uuidIn;
        localResourcePath = localResourcePathIn;
        loggingDir = loggingDirIn;
        proxySettings = proxySettingsIn;
    }

    /**
     * Gets the url.
     * @return the url
     */
    public URI getUrl() {
        return url;
    }

    /**
     * Gets the username.
     * @return the username
     */
    public String getUsername() {
        return username;
    }

    /**
     * Gets the password.
     * @return the password
     */
    public String getPassword() {
        return password;
    }

    /**
     * Gets the UUID.
     * @return the UUID
     */
    public String getUUID() {
        return uuid;
    }

    /**
     * Gets the local resource path.
     * @return the local resource path
     */
    public String getLocalResourcePath() {
        return localResourcePath;
    }

    /**
     * Gets the logging dir.
     * @return the logging dir
     */
    public String getLoggingDir() {
        return loggingDir;
    }

    /**
     * Gets the proxy settings.
     * @return the proxy settings
     */
    public SCCProxySettings getProxySettings() {
        return proxySettings;
    }
}
