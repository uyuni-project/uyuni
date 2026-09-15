/*
 * Copyright (c) 2014--2015 SUSE LLC
 *
 * This software is licensed to you under the GNU General Public License,
 * version 2 (GPLv2). There is NO WARRANTY for this software, express or
 * implied, including the implied warranties of MERCHANTABILITY or FITNESS
 * FOR A PARTICULAR PURPOSE. You should have received a copy of GPLv2
 * along with this software; if not, see
 * http://www.gnu.org/licenses/old-licenses/gpl-2.0.txt.
 *
 * SPDX-License-Identifier: GPL-2.0-only
 *
 * Red Hat trademarks are not licensed under GPLv2. No permission is
 * granted to use or replicate Red Hat trademarks that are incorporated
 * in this software or its documentation.
 */
package com.suse.scc.client;

import java.net.URI;
import java.security.cert.Certificate;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

/**
 * SCC configuration container class.
 */
public class SCCConfig {

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

    /** Path to the logging directory. */
    private String loggingDir;

    private Map<String, String> additionalHeaders;

    /** True to skip owner setting in tests */
    private boolean skipOwner = false;
    private List<Certificate> additionalCerts;


    /**
     * Full constructor.
     * @param urlIn the url
     * @param usernameIn the username
     * @param passwordIn the password
     * @param uuidIn the UUID
     * @param loggingDirIn the logging dir
     * @param additionalCertsIn  additional certificates to trust
     * @param skipOwnerIn skip owner setting for testing
     * @param additionalHeadersIn map of additional headers to set for the request
     */
    public SCCConfig(URI urlIn, String usernameIn, String passwordIn, String uuidIn,
            String loggingDirIn, boolean skipOwnerIn,
                     Map<String, String> additionalHeadersIn, List<Certificate> additionalCertsIn) {
        url = urlIn;
        username = usernameIn;
        password = passwordIn;
        uuid = uuidIn;
        loggingDir = loggingDirIn;
        skipOwner = skipOwnerIn;
        additionalHeaders = additionalHeadersIn;
        additionalCerts = additionalCertsIn;
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
     * Gets the logging dir.
     * @return the logging dir
     */
    public String getLoggingDir() {
        return loggingDir;
    }

    /**
     * @return value of skipOwner
     */
    public boolean isSkipOwner() {
        return skipOwner;
    }

    public List<Certificate> getAdditionalCerts() {
        return additionalCerts;
    }

    /**
     * @return additional headers
     */
    public Map<String, String> getAdditionalHeaders() {
        return Optional.ofNullable(additionalHeaders).orElse(new HashMap<>());
    }
}
