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

import java.io.IOException;
import java.net.URI;
import java.security.NoSuchAlgorithmException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;

import javax.net.ssl.SSLContext;

import org.apache.commons.lang3.StringUtils;
import org.apache.http.HttpException;
import org.apache.http.HttpHost;
import org.apache.http.HttpRequest;
import org.apache.http.HttpResponse;
import org.apache.http.auth.AuthScope;
import org.apache.http.auth.Credentials;
import org.apache.http.auth.UsernamePasswordCredentials;
import org.apache.http.client.CredentialsProvider;
import org.apache.http.client.HttpClient;
import org.apache.http.client.config.AuthSchemes;
import org.apache.http.client.config.CookieSpecs;
import org.apache.http.client.config.RequestConfig;
import org.apache.http.client.config.RequestConfig.Builder;
import org.apache.http.client.methods.HttpRequestBase;
import org.apache.http.conn.routing.HttpRoute;
import org.apache.http.conn.ssl.SSLConnectionSocketFactory;
import org.apache.http.impl.client.BasicCredentialsProvider;
import org.apache.http.impl.client.HttpClientBuilder;
import org.apache.http.impl.client.ProxyAuthenticationStrategy;
import org.apache.http.impl.conn.DefaultProxyRoutePlanner;
import org.apache.http.protocol.BasicHttpContext;
import org.apache.http.protocol.HttpContext;
import org.apache.log4j.Logger;

import com.redhat.rhn.common.conf.Config;
import com.redhat.rhn.common.conf.ConfigDefaults;

/**
 * Adapter class holding an {@link HttpClient} object and offering a simple API to execute
 * arbitrary HTTP requests while proxy settings are applied transparently.
 */
public class HttpClientAdapter {

    /**
     * Name of custom attribute used to pass the ignoreNoProxy flag down the
     * request chain.
     */
    private static final String IGNORE_NO_PROXY = "ignoreNoProxy";

    /** The Constant REQUEST_URI. */
    private static final String REQUEST_URI = "request_uri";

    /**
     * Configuration key for the proxy skip list.
     */
    public static final String NO_PROXY = "server.satellite.no_proxy";
    public static final String MAX_CONNCECTIONS = "java.mgr_sync_max_connections";

    /** The log. */
    private static Logger log = Logger.getLogger(HttpClientAdapter.class);

    /** The proxy host. */
    private HttpHost proxyHost;

    /** The http client. */
    private HttpClient httpClient;

    /** The no proxy domains. */
    private List<String> noProxyDomains = new ArrayList<String>();

    /** The request config. */
    private RequestConfig requestConfig;

    /** The credentials provider. */
    private CredentialsProvider credentialsProvider = new BasicCredentialsProvider();
    /**
     * Initialize an {@link HttpClient} for performing requests. Proxy settings will
     * be read from the configuration and applied transparently.
     */
    public HttpClientAdapter() {
        Optional<SSLConnectionSocketFactory> sslSocketFactory = Optional.empty();
        try {
            SSLContext sslContext = SSLContext.getDefault();
            sslSocketFactory = Optional.of(new SSLConnectionSocketFactory(
                    sslContext,
                    new String[]{"TLSv1", "TLSv1.1", "TLSv1.2"},
                    null,
                    SSLConnectionSocketFactory.getDefaultHostnameVerifier()));
        }
        catch (NoSuchAlgorithmException e) {
            log.warn("No such algorithm. Using default context", e);
        }

        HttpClientBuilder clientBuilder = HttpClientBuilder.create();
        sslSocketFactory.ifPresent(sf -> clientBuilder.setSSLSocketFactory(sf));

        clientBuilder.setDefaultCredentialsProvider(credentialsProvider);
        Builder requestConfigBuilder = RequestConfig.custom()
                .setCookieSpec(CookieSpecs.IGNORE_COOKIES);

        // Store the proxy settings
        String proxyHostname = ConfigDefaults.get().getProxyHost();
        if (!StringUtils.isBlank(proxyHostname)) {
            int proxyPort = ConfigDefaults.get().getProxyPort();

            proxyHost = new HttpHost(proxyHostname, proxyPort);
            clientBuilder.setProxy(proxyHost);

            String proxyUsername = ConfigDefaults.get().getProxyUsername();
            String proxyPassword = ConfigDefaults.get().getProxyPassword();
            if (!StringUtils.isBlank(proxyUsername) &&
                    !StringUtils.isBlank(proxyPassword)) {
                Credentials proxyCredentials = new UsernamePasswordCredentials(
                        proxyUsername, proxyPassword);

                credentialsProvider.setCredentials(new AuthScope(proxyHostname, proxyPort),
                        proxyCredentials);
            }

            // Explicitly exclude the NTLM authentication scheme
            requestConfigBuilder =  requestConfigBuilder.setProxyPreferredAuthSchemes(
                                    Arrays.asList(AuthSchemes.DIGEST, AuthSchemes.BASIC));

            clientBuilder.setProxyAuthenticationStrategy(new ProxyAuthenticationStrategy());

            clientBuilder.setRoutePlanner(new CustomProxyRoutePlanner(proxyHost));
        }

        // Read proxy exceptions from the "no_proxy" config option
        String noProxy = Config.get().getString(NO_PROXY);
        if (!StringUtils.isBlank(noProxy)) {
            for (String domain : Arrays.asList(noProxy.split(","))) {
                noProxyDomains.add(domain.toLowerCase().trim());
            }
        }

        requestConfig = requestConfigBuilder.build();
        clientBuilder.setMaxConnPerRoute(Config.get().getInt(MAX_CONNCECTIONS, 1));
        clientBuilder.setMaxConnTotal(Config.get().getInt(MAX_CONNCECTIONS, 1));
        httpClient = clientBuilder.build();
    }

    /**
     * Custom ProxyRoutePlanner that routes the request to a proxy
     * or to a direct route depending on the setting
     * {@code server.satellite.no_proxy} and the calling class passing a
     * {@code ignoreNoProxy} flag.
     */
    class CustomProxyRoutePlanner extends DefaultProxyRoutePlanner {

        /**
         * Instantiates a new custom proxy route planner.
         *
         * @param proxy the proxy
         */
        CustomProxyRoutePlanner(HttpHost proxy) {
            super(proxy);
        }

        /**
         * {@inheritDoc}
         */
        @Override
        public HttpRoute determineRoute(final HttpHost host, final HttpRequest request,
                final HttpContext context) throws HttpException {

            Boolean ignoreNoProxy = (Boolean) context.getAttribute(IGNORE_NO_PROXY);
            URI requestUri = (URI) context.getAttribute(REQUEST_URI);

            if (proxyHost != null &&
                    (Boolean.TRUE.equals(ignoreNoProxy) || useProxyFor(requestUri))) {
                if (log.isDebugEnabled()) {
                    log.debug("Using proxy: " + proxyHost);
                }
                return super.determineRoute(host, request, context);
            }
            if (log.isDebugEnabled()) {
                log.debug("Using a direct connection (no proxy)");
            }
            // Return direct route
            return new HttpRoute(host);
        }
    }

    /**
     * Take a request as {@link HttpMethod} and execute it.
     *
     * @param request the {@link HttpMethod} to be executed
     * @return the return code of the request
     * @throws IOException in case of errors
     */
    public HttpResponse executeRequest(HttpRequestBase request) throws IOException {
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
    public HttpResponse executeRequest(HttpRequestBase request, boolean ignoreNoProxy)
            throws IOException {
        if (log.isDebugEnabled()) {
            log.debug(request.getMethod() + " " + request.getURI());
        }
        // Decide if a proxy should be used for this request

        HttpContext httpContxt = new BasicHttpContext();
        httpContxt.setAttribute(IGNORE_NO_PROXY, ignoreNoProxy);

        httpContxt.setAttribute(REQUEST_URI, request.getURI());

        // Execute the request
        request.setConfig(requestConfig);
        HttpResponse httpResponse = httpClient.execute(request, httpContxt);

        if (log.isDebugEnabled()) {
            log.debug("Response code: " + httpResponse.getStatusLine().getStatusCode());
        }
        return httpResponse;
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
    public HttpResponse executeRequest(HttpRequestBase request, String username,
            String password)
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
    public HttpResponse executeRequest(HttpRequestBase request, String username,
            String password,
            boolean ignoreNoProxy) throws IOException {
        // Setup authentication
        if (!StringUtils.isBlank(username) && !StringUtils.isBlank(password)) {
            Credentials creds = new UsernamePasswordCredentials(username, password);
            URI uri = request.getURI();
            credentialsProvider.setCredentials(new AuthScope(uri.getHost(), uri.getPort()),
                    creds);
        }

        return executeRequest(request, ignoreNoProxy);
    }

    /**
     * Check for a given {@link URI} if a proxy should be used or not.
     *
     * @param uri the URI to check
     * @return true if proxy should be used, else false
     */
    private boolean useProxyFor(URI uri) {
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
