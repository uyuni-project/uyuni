/**
 * Copyright (c) 2015 SUSE LLC
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
package com.redhat.rhn.common.util;

import com.redhat.rhn.common.conf.ConfigDefaults;

import org.apache.commons.httpclient.Credentials;
import org.apache.commons.httpclient.HttpClient;
import org.apache.commons.httpclient.UsernamePasswordCredentials;
import org.apache.commons.httpclient.auth.AuthScope;
import org.apache.commons.lang.StringUtils;
import org.apache.log4j.Logger;

/**
 * Utility methods for working with HTTP.
 */
public class HttpUtils {

    private static Logger log = Logger.getLogger(HttpUtils.class);

    private HttpUtils() {
    }

    /**
     * Initialize and return {@link HttpClient} for performing unauthenticated requests.
     *
     * @return {@link HttpClient} object
     */
    public static HttpClient initHttpClient() {
       return initHttpClient(null, null);
    }

    /**
     * Initialize and return {@link HttpClient} for performing authenticated requests.
     * This method reads proxy settings from the configuration and applies those.
     *
     * @param username username for basic authentication
     * @param password password for basic authentication
     * @return {@link HttpClient} object
     */
    public static HttpClient initHttpClient(String username, String password) {
        ConfigDefaults cfg = ConfigDefaults.get();
        return initHttpClient(username, password, cfg.getProxyHost(), cfg.getProxyPort(),
                cfg.getProxyUsername(), cfg.getProxyPassword());
    }

    /**
     * Initialize and return {@link HttpClient} for performing authenticated requests.
     *
     * @param username username for basic authentication
     * @param password password for basic authentication
     * @param proxyHost proxy hostname
     * @param proxyPort proxy port
     * @param proxyUsername username for proxy authentication
     * @param proxyPassword password for proxy authentication
     * @return {@link HttpClient} object
     */
    public static HttpClient initHttpClient(String username, String password,
            String proxyHost, int proxyPort, String proxyUsername, String proxyPassword) {
        HttpClient httpClient = new HttpClient();

        // Apply proxy settings
        if (!StringUtils.isBlank(proxyHost)) {
            httpClient.getHostConfiguration().setProxy(proxyHost, proxyPort);
            if (log.isDebugEnabled()) {
                log.debug("Using proxy: " + proxyHost + ":" + proxyPort);
            }

            if (!StringUtils.isBlank(proxyUsername) &&
                    !StringUtils.isBlank(proxyPassword)) {
                Credentials proxyCredentials = new UsernamePasswordCredentials(
                        proxyUsername, proxyPassword);
                httpClient.getState().setProxyCredentials(AuthScope.ANY, proxyCredentials);
            }
        }

        // Basic authentication
        if (!StringUtils.isBlank(username) && !StringUtils.isBlank(password)) {
            Credentials creds = new UsernamePasswordCredentials(username, password);
            httpClient.getState().setCredentials(AuthScope.ANY, creds);
        }

        return httpClient;
    }
}
