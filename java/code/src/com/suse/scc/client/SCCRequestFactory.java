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

import org.apache.commons.lang.StringUtils;

import java.io.IOException;
import java.net.Authenticator;
import java.net.HttpURLConnection;
import java.net.InetSocketAddress;
import java.net.Proxy;
import java.net.URL;

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
        String uri = config.getUrl() + endpoint;
        String encodedCredentials = config.getEncodedCredentials();
        URL url = new URL(uri);
        HttpURLConnection connection;

        SCCProxySettings proxySettings = config.getProxySettings();
        String proxyHost = null;
        if (proxySettings != null) {
            proxyHost = proxySettings.getHostname();
        }
        if (!StringUtils.isEmpty(proxyHost)) {
            int proxyPort = proxySettings.getPort();
            Proxy proxy = new Proxy(Proxy.Type.HTTP, new InetSocketAddress(proxyHost,
                proxyPort));
            connection = (HttpURLConnection) url.openConnection(proxy);

            String proxyUsername = proxySettings.getUsername();
            String proxyPassword = proxySettings.getPassword();
            if (!StringUtils.isEmpty(proxyUsername) &&
                    !StringUtils.isEmpty(proxyPassword)) {
                Authenticator.setDefault(new ProxyAuthenticator(
                        proxyUsername, proxyPassword));
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
        connection.setRequestProperty("Accept", "application/vnd.scc.suse.com.v4+json");
        connection.setRequestProperty("Accept-Encoding", "gzip, deflate");

        // Send the UUID for debugging if available
        String uuid = config.getUUID();
        connection.setRequestProperty("SMS", uuid != null ? uuid : "undefined");

        return connection;
    }
}
