/*
 * Copyright (c) 2024--2025 SUSE LLC
 *
 * This software is licensed to you under the GNU General Public License,
 * version 2 (GPLv2). There is NO WARRANTY for this software, express or
 * implied, including the implied warranties of MERCHANTABILITY or FITNESS
 * FOR A PARTICULAR PURPOSE. You should have received a copy of GPLv2
 * along with this software; if not, see
 * http://www.gnu.org/licenses/old-licenses/gpl-2.0.txt.
 *
 * SPDX-License-Identifier: GPL-2.0-only
 */
package com.suse.scc.client;

import java.net.URI;
import java.net.URISyntaxException;
import java.security.cert.Certificate;
import java.util.List;
import java.util.Map;

public class SCCConfigBuilder {

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

    private URI url = DEFAULT_URL;
    private String username = null;
    private String password = null;
    private String uuid = null;
    private String loggingDir = SCCConfig.DEFAULT_LOGGING_DIR;
    private boolean skipOwner = false;
    private Map<String, String> additionalHeaders = null;
    private List<Certificate> additionalCerts;

    /**
     * Set the scc url
     * @param urlIn scc url
     * @return the builder
     */
    public SCCConfigBuilder setUrl(URI urlIn) {
        this.url = urlIn;
        return this;
    }

    /**
     * Set the additional certificates to trust
     * @param certificatesIn the certificates
     * @return the builder
     */
    public SCCConfigBuilder setCertificates(List<Certificate> certificatesIn) {
        this.additionalCerts = certificatesIn;
        return this;
    }

    /**
     * Set the SCC password
     * @param usernameIn the SCC username
     * @return the builder
     */
    public SCCConfigBuilder setUsername(String usernameIn) {
        this.username = usernameIn;
        return this;
    }

    /**
     * Set the SCC password
     * @param passwordIn the SCC password
     * @return the builder
     */
    public SCCConfigBuilder setPassword(String passwordIn) {
        this.password = passwordIn;
        return this;
    }

    /**
     * Set the uuid to identify this susemanager instance to SCC
     * @param uuidIn the uuid
     * @return the builder
     */
    public SCCConfigBuilder setUuid(String uuidIn) {
        this.uuid = uuidIn;
        return this;
    }

    /**
     * Set the directory in which logs are written
     * @param loggingDirIn the directory to write logs in
     * @return the builder
     */
    public SCCConfigBuilder setLoggingDir(String loggingDirIn) {
        this.loggingDir = loggingDirIn;
        return this;
    }

    /**
     * Option to skip file owner changes (only used during tests)
     * @param skipOwnerIn flag to skip file owner changes
     * @return the builder
     */
    public SCCConfigBuilder setSkipOwner(boolean skipOwnerIn) {
        this.skipOwner = skipOwnerIn;
        return this;
    }

    /**
     * Set the additional http headers to use
     * @param additionalHeadersIn map of headers
     * @return the builder
     */
    public SCCConfigBuilder setAdditionalHeaders(Map<String, String> additionalHeadersIn) {
        this.additionalHeaders = additionalHeadersIn;
        return this;
    }

    /**
     * Create the SCC config from the builders current settings
     * @return the resulting SCC config
     */
    public SCCConfig createSCCConfig() {
        return new SCCConfig(url, username, password, uuid, loggingDir, skipOwner, additionalHeaders,
                additionalCerts);
    }
}
