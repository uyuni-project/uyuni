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
import org.apache.commons.httpclient.HttpMethod;
import org.apache.commons.httpclient.ProxyHost;
import org.apache.commons.httpclient.URI;
import org.apache.commons.httpclient.UsernamePasswordCredentials;
import org.apache.commons.httpclient.auth.AuthScope;
import org.apache.commons.lang.StringUtils;
import org.apache.log4j.Logger;

import java.io.IOException;

/**
 * Adapter class holding an {@link HttpClient} object and offering a simple API to execute
 * arbitrary HTTP requests while proxy settings are applied transparently.
 */
public class HttpClientAdapter {

    private static Logger log = Logger.getLogger(HttpClientAdapter.class);
    private ProxyHost proxyHost;
    private HttpClient httpClient;

    /**
     * Initialize an {@link HttpClient} for performing requests. Proxy settings will
     * be read from the configuration and applied transparently.
     *
     * @return {@link HttpClientAdapter} object
     */
    public HttpClientAdapter() {
        this(ConfigDefaults.get().getProxyHost(),
                ConfigDefaults.get().getProxyPort(),
                ConfigDefaults.get().getProxyUsername(),
                ConfigDefaults.get().getProxyPassword());
    }

    /**
     * Initialize an {@link HttpClient} using proxy settings as they are passed in.
     *
     * @param proxyHost proxy hostname
     * @param proxyPort proxy port
     * @param proxyUsername username for proxy authentication
     * @param proxyPassword password for proxy authentication
     * @return {@link HttpClientAdapter} object
     */
    public HttpClientAdapter(String proxyHostname, int proxyPort,
            String proxyUsername, String proxyPassword) {
        httpClient = new HttpClient();

        // Store the proxy settings
        if (!StringUtils.isBlank(proxyHostname)) {
            proxyHost = new ProxyHost(proxyHostname, proxyPort);
            if (!StringUtils.isBlank(proxyUsername) &&
                    !StringUtils.isBlank(proxyPassword)) {
                Credentials proxyCredentials = new UsernamePasswordCredentials(
                        proxyUsername, proxyPassword);
                httpClient.getState().setProxyCredentials(
                        new AuthScope(proxyHostname, proxyPort), proxyCredentials);
            }
        }
    }

    /**
     * Take a request as {@link HttpMethod} and execute it.
     *
     * @param request the {@link HttpMethod} to be executed
     * @return the return code of the request
     * @throws IOException in case of errors
     */
    public int executeRequest(HttpMethod request) throws IOException {
        if (log.isDebugEnabled()) {
            log.debug(request.getName() + " " + request.getURI());
        }

        // Decide if this request should go via a proxy
        if (proxyHost != null && useProxy(request.getURI())) {
            if (log.isDebugEnabled()) {
                log.debug("Using proxy: " + proxyHost);
            }
            httpClient.getHostConfiguration().setProxyHost(proxyHost);
        }
        else {
            if (log.isDebugEnabled()) {
                log.debug("Using a direct connection (no proxy)");
            }
            httpClient.getHostConfiguration().setProxyHost(null);
        }

        // Execute the request
        int returnCode = httpClient.executeMethod(request);
        if (log.isDebugEnabled()) {
            log.debug("Response code: " + returnCode);
        }
        return returnCode;
    }

    /**
     * Execute an authenticated request given as {@link HttpMethod}.
     *
     * @param request the request to execute
     * @param username username for authentication
     * @param password password for authentication
     * @return the return code of the request
     * @throws IOException in case of errors
     */
    public int executeRequest(HttpMethod request, String username, String password)
            throws IOException {
        // Setup authentication
        if (!StringUtils.isBlank(username) && !StringUtils.isBlank(password)) {
            Credentials creds = new UsernamePasswordCredentials(username, password);
            URI uri = request.getURI();
            httpClient.getState().setCredentials(
                    new AuthScope(uri.getHost(), uri.getPort()), creds);

            // Enable preemptive authentication
            httpClient.getParams().setAuthenticationPreemptive(true);
        }

        return executeRequest(request);
    }

    /**
     * Check for a given {@link URI} if a proxy should be used or not.
     *
     * @param uri the URI to check
     * @return true if proxy should be used, else false
     */
    private boolean useProxy(URI uri) {
        // TODO: Lookup the "no_proxy" option from config
        return true;
    }
}
