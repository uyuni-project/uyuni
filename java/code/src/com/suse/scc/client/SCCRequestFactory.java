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
import java.io.UnsupportedEncodingException;
import java.net.HttpURLConnection;
import java.net.InetSocketAddress;
import java.net.Proxy;
import java.net.URL;
import org.apache.commons.codec.binary.Base64;
import org.apache.commons.lang.StringUtils;

/**
 * Helper class for setting up {@link HttpURLConnection} objects.
 */
public class SCCRequestFactory {

    /** Singleton instance. */
    private static SCCRequestFactory instance = new SCCRequestFactory();

    /**
     * Instantiates a new SCC request factory.
     */
    private SCCRequestFactory() {
    }

    /**
     * Gets the single instance of SCCRequestFactory.
     * @return single instance of SCCRequestFactory
     */
    public static SCCRequestFactory getInstance() {
        return instance;
    }

    /**
     * Init a {@link HttpURLConnection} object from a given URI.
     *
     * @param method the method
     * @param endpoint the endpoint
     * @param config the config
     * @return connection
     * @throws IOException Signals that an I/O exception has occurred.
     */
    public HttpURLConnection initConnection(
            String method, String endpoint, SCCConfig config) throws IOException {
        // Init the connection
        String uri = config.getSchema() + config.getHostname() + endpoint;
        String encodedCredentials = config.getEncodedCredentials();
        URL url = new URL(uri);
        HttpURLConnection connection;

        String proxyHost = config.getProxyHostname();
        if (!StringUtils.isEmpty(proxyHost)) {
            int proxyPort = Integer.parseInt(config.getProxyPort());
            Proxy proxy = new Proxy(Proxy.Type.HTTP, new InetSocketAddress(proxyHost,
                proxyPort));
            connection = (HttpURLConnection) url.openConnection(proxy);

            String proxyUsername = config.getProxyUsername();
            String proxyPassword = config.getProxyPassword();
            if (!StringUtils.isEmpty(proxyUsername) &&
                !StringUtils.isEmpty(proxyPassword)) {
                try {
                    byte[] encodedBytes =
                            Base64.encodeBase64((proxyUsername + ':' + proxyPassword)
                                    .getBytes("iso-8859-1"));
                    final String encoded = new String(encodedBytes, "iso-8859-1");
                    connection.addRequestProperty("Proxy-Authorization", encoded);
                }
                catch (UnsupportedEncodingException e) {
                    // can't happen
                }
            }
        }
        else {
            connection = (HttpURLConnection) url.openConnection();
        }

        connection.setRequestMethod(method);

        // Basic authentication
        if (encodedCredentials != null) {
            connection.setRequestProperty("Authorization", "BASIC " + encodedCredentials);
        }

        // Set additional request headers here
        connection.setRequestProperty("Accept", "application/vnd.scc.suse.com.v2+json");
        connection.setRequestProperty("Accept-Encoding", "gzip, deflate");

        // Send the UUID for debugging if available
        String uuid = config.getUUID();
        connection.setRequestProperty("SMT", uuid != null ? uuid : "undefined");

        return connection;
    }
}
