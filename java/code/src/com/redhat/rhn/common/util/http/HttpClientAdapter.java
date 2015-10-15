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
package com.redhat.rhn.common.util.http;

import com.redhat.rhn.common.conf.Config;
import com.redhat.rhn.common.conf.ConfigDefaults;

import org.apache.commons.httpclient.Credentials;
import org.apache.commons.httpclient.HttpClient;
import org.apache.commons.httpclient.HttpMethod;
import org.apache.commons.httpclient.ProxyHost;
import org.apache.commons.httpclient.URI;
import org.apache.commons.httpclient.URIException;
import org.apache.commons.httpclient.UsernamePasswordCredentials;
import org.apache.commons.httpclient.auth.AuthPolicy;
import org.apache.commons.httpclient.auth.AuthScope;
import org.apache.commons.lang.StringUtils;
import org.apache.log4j.Logger;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/**
 * Adapter class holding an {@link HttpClient} object and offering a simple API to execute
 * arbitrary HTTP requests while proxy settings are applied transparently.
 */
public class HttpClientAdapter {

    /**
     * Configuration key for the proxy skip list.
     */
    public static final String NO_PROXY = "server.satellite.no_proxy";

    private static Logger log = Logger.getLogger(HttpClientAdapter.class);
    private ProxyHost proxyHost;
    private HttpClient httpClient;
    private List<String> noProxyDomains = new ArrayList<String>();

    /**
     * Initialize an {@link HttpClient} for performing requests. Proxy settings will
     * be read from the configuration and applied transparently.
     */
    public HttpClientAdapter() {
        httpClient = new HttpClient();

        // Store the proxy settings
        String proxyHostname = ConfigDefaults.get().getProxyHost();
        if (!StringUtils.isBlank(proxyHostname)) {
            int proxyPort = ConfigDefaults.get().getProxyPort();
            proxyHost = new ProxyHost(proxyHostname, proxyPort);
            String proxyUsername = ConfigDefaults.get().getProxyUsername();
            String proxyPassword = ConfigDefaults.get().getProxyPassword();
            if (!StringUtils.isBlank(proxyUsername) &&
                    !StringUtils.isBlank(proxyPassword)) {
                Credentials proxyCredentials = new UsernamePasswordCredentials(
                        proxyUsername, proxyPassword);
                httpClient.getState().setProxyCredentials(
                        new AuthScope(proxyHostname, proxyPort), proxyCredentials);
            }

            // Explicitly exclude the NTLM authentication scheme
            ArrayList<String> authPrefs = new ArrayList<String>() { {
                add(AuthPolicy.DIGEST);
                add(AuthPolicy.BASIC);
            } };
            httpClient.getParams().setParameter(AuthPolicy.AUTH_SCHEME_PRIORITY, authPrefs);
        }

        // Read proxy exceptions from the "no_proxy" config option
        String noProxy = Config.get().getString(NO_PROXY);
        if (noProxy != null) {
            for (String domain : Arrays.asList(noProxy.split(","))) {
                noProxyDomains.add(domain.toLowerCase().trim());
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
        return executeRequest(request, false);
    }

    /**
     * Take a request as {@link HttpMethod} and execute it.
     *
     * @param request the {@link HttpMethod} to be executed
     * @param ignoreNoProxy set true to ignore the "no_proxy" setting
     * @return the return code of the request
     * @throws IOException in case of errors
     */
    public int executeRequest(HttpMethod request, boolean ignoreNoProxy)
            throws IOException {
        if (log.isDebugEnabled()) {
            log.debug(request.getName() + " " + request.getURI());
        }

        // Decide if a proxy should be used for this request
        if (proxyHost != null && (ignoreNoProxy || useProxyFor(request.getURI()))) {
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
        return executeRequest(request, username, password, false);
    }

    /**
     * Execute an authenticated request given as {@link HttpMethod}.
     *
     * @param request the request to execute
     * @param username username for authentication
     * @param password password for authentication
     * @param ignoreNoProxy set true to ignore the "no_proxy" setting
     * @return the return code of the request
     * @throws IOException in case of errors
     */
    public int executeRequest(HttpMethod request, String username, String password,
            boolean ignoreNoProxy) throws IOException {
        // Setup authentication
        if (!StringUtils.isBlank(username) && !StringUtils.isBlank(password)) {
            Credentials creds = new UsernamePasswordCredentials(username, password);
            URI uri = request.getURI();
            httpClient.getState().setCredentials(
                    new AuthScope(uri.getHost(), uri.getPort()), creds);
        }

        return executeRequest(request, ignoreNoProxy);
    }

    /**
     * Check for a given {@link URI} if a proxy should be used or not.
     *
     * @param uri the URI to check
     * @return true if proxy should be used, else false
     */
    private boolean useProxyFor(URI uri) throws URIException {
        if (uri.getScheme().equals("file")) {
            return false;
        }
        String host = uri.getHost();
        if (host.equals("localhost") || host.equals("127.0.0.1") || host.equals("::1")) {
            return false;
        }

        if (noProxyDomains.isEmpty()) {
            return true;
        }
        else if (noProxyDomains.contains("*")) {
            return false;
        }

        // Check for either an exact match or the previous character is a '.',
        // so that host is within the same domain.
        for (String domain : noProxyDomains) {
            if (domain.startsWith(".")) {
                domain = domain.substring(1);
            }
            if (domain.equals(host) || host.endsWith("." + domain)) {
                return false;
            }
        }
        return true;
    }
}
