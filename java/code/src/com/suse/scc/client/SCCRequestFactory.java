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

import java.io.IOException;
import java.net.HttpURLConnection;
import java.net.URL;

/**
 * Helper class for setting up {@link HttpURLConnection} objects.
 */
public class SCCRequestFactory {

    // Singleton instance
    private static SCCRequestFactory instance = new SCCRequestFactory();

    private SCCRequestFactory() {
    }

    public static SCCRequestFactory getInstance() {
        return instance;
    }

    /**
     * Init a {@link HttpURLConnection} object from a given URI.
     *
     * @param method
     * @param uri
     * @param encodedCredentials
     * @return connection
     * @throws IOException
     */
    public HttpURLConnection initConnection(
            String method, String uri, String encodedCredentials) throws IOException {
        // Init the connection
        URL url = new URL(uri);
        HttpURLConnection connection = (HttpURLConnection) url.openConnection();
        connection.setRequestMethod(method);

        // Basic authentication
        if (encodedCredentials != null) {
            connection.setRequestProperty("Authorization", "BASIC " + encodedCredentials);
        }

        // Set additional request headers here
        connection.setRequestProperty("Accept", "application/vnd.scc.suse.com.v2+json");
        connection.setRequestProperty("Accept-Encoding", "gzip, deflate");

        // Send the UUID for debugging if available
        String uuid = SCCConfig.getInstance().getUUID();
        connection.setRequestProperty("SMS", uuid != null ? uuid : "undefined");

        return connection;
    }
}
