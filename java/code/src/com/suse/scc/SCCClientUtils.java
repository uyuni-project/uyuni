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
package com.suse.scc;

import com.redhat.rhn.common.conf.ConfigDefaults;

import org.apache.commons.codec.binary.Base64;
import org.apache.commons.lang.StringUtils;

import java.io.IOException;
import java.io.InputStream;
import java.io.UnsupportedEncodingException;
import java.net.HttpURLConnection;
import java.net.InetSocketAddress;
import java.net.MalformedURLException;
import java.net.Proxy;
import java.net.URL;
import java.util.Scanner;

/**
 * Utilities for {@link SCCClient}.
 */
public class SCCClientUtils {

    /**
     * Returns an HTTP connection object for given method and location. Implicitly will read
     * proxy settings from SUSE Manager configuration which needs to be changed.
     *
     * @param method HTTP method to use
     * @param location URL to make requests to
     * @return the http connection
     * @throws IOException if network errors happen
     * @throws MalformedURLException if location is not valid
     */
    public static HttpURLConnection getConnection(String method, String location)
        throws MalformedURLException, IOException {
        URL url = new URL(location);
        HttpURLConnection connection = null;

        // TODO: Take proxy settings as parameter instead of reading from config
        ConfigDefaults configDefaults = ConfigDefaults.get();
        String proxyHost = configDefaults.getProxyHost();

        if (!StringUtils.isEmpty(proxyHost)) {
            // Create proxied connection
            int proxyPort = configDefaults.getProxyPort();
            Proxy proxy = new Proxy(Proxy.Type.HTTP, new InetSocketAddress(
                    proxyHost, proxyPort));
            connection = (HttpURLConnection) url.openConnection(proxy);

            String proxyUsername = configDefaults.getProxyUsername();
            String proxyPassword = configDefaults.getProxyPassword();
            if (!StringUtils.isEmpty(proxyUsername) &&
                !StringUtils.isEmpty(proxyPassword)) {
                try {
                    byte[] encodedBytes = Base64.encodeBase64(
                            (proxyUsername + ':' + proxyPassword).getBytes("iso-8859-1"));
                    final String encoded = new String(encodedBytes, "iso-8859-1");
                    connection.addRequestProperty("Proxy-Authorization", encoded);
                }
                catch (UnsupportedEncodingException e) {
                    // can't happen
                }
            }
        }
        else {
            // No proxy
            connection = (HttpURLConnection) url.openConnection();
        }
        connection.setRequestMethod(method);
        return connection;
    }

    /**
     * Convert a given {@link InputStream} to a {@link String}.
     *
     * @param is an input stream
     * @return the string in the input stream
     */
    public static String streamToString(InputStream is) {
        Scanner scanner = new Scanner(is);
        String ret = scanner.useDelimiter("\\A").next();
        scanner.close();
        return ret;
    }

    /**
     * Quietly close a given stream, suppressing exceptions.
     *
     * @param stream
     */
    public static void closeQuietly(InputStream stream) {
        if (stream == null) {
            return;
        }
        try {
            stream.close();
        } catch (IOException e) {
        }
    }
}
