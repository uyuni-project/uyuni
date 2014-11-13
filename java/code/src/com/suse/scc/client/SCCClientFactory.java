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

import org.apache.commons.codec.binary.Base64;

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
     * @param proxySettings a proxy settings object
     * @param uuid the UUID or null
     * @return the new {@link SCCWebClient}
     */
    public static SCCClient getInstance(String url, String username, String password,
            String resourcePath, SCCProxySettings proxySettings, String uuid) {
        SCCConfig config = new SCCConfig();

        if (resourcePath != null || new SCCConfig().getLocalResourcePath() != null) {
            config.put(SCCConfig.RESOURCE_PATH, resourcePath);

            return new SCCFileClient(config);
        }
        else {
            config.put(SCCConfig.URL, url);

            byte[] credsBytes = Base64.encodeBase64((username + ':' + password).getBytes());
            String credsString = new String(credsBytes);
            config.put(SCCConfig.ENCODED_CREDS, credsString);

            config.put(proxySettings);
            config.put(SCCConfig.UUID, uuid);

            return new SCCWebClient(config);
        }
    }
}
