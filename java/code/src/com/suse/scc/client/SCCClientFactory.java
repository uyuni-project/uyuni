/*
 * Copyright (c) 2014 SUSE LLC
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

/**
 * Instantiates {@link SCCClient}
 */
public class SCCClientFactory {

    /**
     * Private constructor.
     */
    private SCCClientFactory() {
        // nothing to do here
    }

    /**
     * Factory method.
     * @param url the URL of scc
     * @param username the username
     * @param password the password
     * @param resourcePath the local path for JSON files or null
     * @param uuid the UUID or null
     * @return the new {@link SCCWebClient}
     */
    public static SCCClient getInstance(URI url, String username, String password,
            String resourcePath, String uuid) {

        if (resourcePath != null) {
            return new SCCFileClient(new SCCConfig(resourcePath));
        }
        else {
            SCCConfig config = new SCCConfig(url, username, password, uuid);
            return new SCCWebClient(config);
        }
    }
}
