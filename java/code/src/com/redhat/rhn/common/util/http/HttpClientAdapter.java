/*
 * Copyright (c) 2015--2024 SUSE LLC
 *
 * This software is licensed to you under the GNU General Public License,
 * version 2 (GPLv2). There is NO WARRANTY for this software, express or
 * implied, including the implied warranties of MERCHANTABILITY or FITNESS
 * FOR A PARTICULAR PURPOSE. You should have received a copy of GPLv2
 * along with this software; if not, see
 * http://www.gnu.org/licenses/old-licenses/gpl-2.0.txt.
 */
package com.redhat.rhn.common.util.http;

import com.redhat.rhn.common.conf.Config;
import com.redhat.rhn.common.conf.ConfigDefaults;

import org.apache.commons.collections.CollectionUtils;
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
import org.apache.http.cookie.Cookie;
import org.apache.http.impl.client.BasicCookieStore;
import org.apache.http.impl.client.BasicCredentialsProvider;
import org.apache.http.impl.client.HttpClientBuilder;
import org.apache.http.impl.client.ProxyAuthenticationStrategy;
import org.apache.http.impl.conn.DefaultProxyRoutePlanner;
import org.apache.http.protocol.BasicHttpContext;
import org.apache.http.protocol.HttpContext;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.URI;
import java.security.KeyStore;
import java.security.NoSuchAlgorithmException;
import java.security.cert.Certificate;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Objects;
import java.util.Optional;

import javax.net.ssl.SSLContext;
import javax.net.ssl.TrustManagerFactory;

import spark.route.HttpMethod;

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
    public static final String HTTP_CONNECTION_TIMEOUT = "java.http_connection_timeout";
    public static final String HTTP_SOCKET_TIMEOUT = "java.http_socket_timeout";
    public static final String SALT_API_HTTP_SOCKET_TIMEOUT = "java.salt_api_http_socket_timeout";
    private static final int TO_MILLISECONDS = 1000;

    /** The log. */
    private static final Logger LOG = LogManager.getLogger(HttpClientAdapter.class);

    /** The proxy host. */
    private HttpHost proxyHost;

    /** The http client. */
    private final HttpClient httpClient;

    /** The no proxy domains. */
    private final List<String> noProxyDomains = new ArrayList<>();

    /** The request config. */
    private final RequestConfig requestConfig;

    /** The credentials provider. */
    private final CredentialsProvider credentialsProvider = new BasicCredentialsProvider();

    /**
     * The cookie store
     */
    private BasicCookieStore cookieStore;

    /**
     * Initialize an {@link HttpClient} for performing requests. Proxy settings will
     * be read from the configuration and applied transparently. The cookies will not be supported.
     */
    public HttpClientAdapter() {
        this(List.of(), false);
    }

    /**
     * Initialize an {@link HttpClient} for performing requests. Proxy settings will
     * be read from the configuration and applied transparently. The cookies will not be supported.
     *
     * @param additionalCertificates a list of additional certificate to consider when establishing the connection
     */
    public HttpClientAdapter(List<Certificate> additionalCertificates) {
        this(additionalCertificates, false);
    }

    /**
     * Initialize an {@link HttpClient} for performing requests. Proxy settings will
     * be read from the configuration and applied transparently.
     *
     * @param allowCookies true, to allow and use cookies.
     * @param additionalCertificates a list of additional certificate to consider when establishing the connection
     */
    public HttpClientAdapter(List<Certificate> additionalCertificates, boolean allowCookies) {
        Optional<SSLConnectionSocketFactory> sslSocketFactory = Optional.empty();
        try {
            SSLContext sslContext = buildSslSocketContext(additionalCertificates);
            List<String> supportedProtocols = Arrays.asList(sslContext.getSupportedSSLParameters().getProtocols());
            List<String> wantedProtocols = Arrays.asList("TLSv1", "TLSv1.1", "TLSv1.2", "TLSv1.3");
            wantedProtocols.retainAll(supportedProtocols);
            sslSocketFactory = Optional.of(new SSLConnectionSocketFactory(
                    sslContext,
                    wantedProtocols.toArray(new String[0]),
                    null,
                    SSLConnectionSocketFactory.getDefaultHostnameVerifier()));
        }
        catch (NoSuchAlgorithmException e) {
            LOG.warn("No such algorithm. Using default context", e);
        }

        HttpClientBuilder clientBuilder = HttpClientBuilder.create();
        sslSocketFactory.ifPresent(clientBuilder::setSSLSocketFactory);

        clientBuilder.setDefaultCredentialsProvider(credentialsProvider);
        Builder requestConfigBuilder = RequestConfig.custom()
                .setConnectTimeout(HttpClientAdapter.getHTTPConnectionTimeout(5))
                .setSocketTimeout(HttpClientAdapter.getHTTPSocketTimeout(5 * 60))
                .setCookieSpec(allowCookies ? CookieSpecs.STANDARD : CookieSpecs.IGNORE_COOKIES);

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
            requestConfigBuilder = requestConfigBuilder.setProxyPreferredAuthSchemes(
                    Arrays.asList(AuthSchemes.DIGEST, AuthSchemes.BASIC));

            clientBuilder.setProxyAuthenticationStrategy(new ProxyAuthenticationStrategy());

            clientBuilder.setRoutePlanner(new CustomProxyRoutePlanner(proxyHost));
        }

        // Read proxy exceptions from the "no_proxy" config option
        String noProxy = Config.get().getString(NO_PROXY);
        if (!StringUtils.isBlank(noProxy)) {
            for (String domain : noProxy.split(",")) {
                noProxyDomains.add(domain.toLowerCase().trim());
            }
        }

        requestConfig = requestConfigBuilder.build();
        clientBuilder.setMaxConnPerRoute(Config.get().getInt(MAX_CONNCECTIONS, 1));
        clientBuilder.setMaxConnTotal(Config.get().getInt(MAX_CONNCECTIONS, 1));
        if (allowCookies) {
            cookieStore = new BasicCookieStore();
            clientBuilder.setDefaultCookieStore(cookieStore);
        }

        httpClient = clientBuilder.build();
    }

    private SSLContext buildSslSocketContext(List<Certificate> additionalCertificates) throws NoSuchAlgorithmException {

        LOG.info("Started checking for certificates and if it finds the certificates will be loaded...");

        String defaultLocation = System.getProperty("java.home") + "/lib/security/cacerts";
        String keyStoreLoc = System.getProperty("javax.net.ssl.trustStore", defaultLocation);

        SSLContext context;

        try (InputStream in = new FileInputStream(keyStoreLoc)) {
            // Create a KeyStore containing our trusted CAs
            KeyStore keystore = KeyStore.getInstance(KeyStore.getDefaultType());
            keystore.load(in, null);

            // Add any additional certificate to the store, if specified
            if (CollectionUtils.isNotEmpty(additionalCertificates)) {
                int customCert = 0;
                for (Certificate certificate : additionalCertificates) {
                    keystore.setCertificateEntry("additional_certificate_" + customCert++, certificate);
                }
            }

            // Create a TrustManager that trusts the CAs in our KeyStore
            String tmfAlgorithm = TrustManagerFactory.getDefaultAlgorithm();
            TrustManagerFactory tmf = TrustManagerFactory.getInstance(tmfAlgorithm);
            tmf.init(keystore);

            // Create an SSLContext that uses our TrustManager
            context = SSLContext.getInstance("TLS");
            context.init(null, tmf.getTrustManagers(), null);
            LOG.info("Completed loading of certificates.");
        }
        catch (Exception e) {
            LOG.error("unable to create ssl context {}." +
                    "If the trust store has been updated, some certificates might not have been loaded.",
                    e.getMessage());
            context = SSLContext.getDefault();
        }
        return context;
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
                if (LOG.isDebugEnabled()) {
                    LOG.debug("Using proxy: {}", proxyHost);
                }
                return super.determineRoute(host, request, context);
            }
            if (LOG.isDebugEnabled()) {
                LOG.debug("Using a direct connection (no proxy)");
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
        if (LOG.isDebugEnabled()) {
            LOG.debug("{} {}", request.getMethod(), request.getURI());
        }
        // Decide if a proxy should be used for this request

        HttpContext httpContxt = new BasicHttpContext();
        httpContxt.setAttribute(IGNORE_NO_PROXY, ignoreNoProxy);

        httpContxt.setAttribute(REQUEST_URI, request.getURI());

        // Execute the request
        request.setConfig(requestConfig);
        HttpResponse httpResponse = httpClient.execute(request, httpContxt);

        if (LOG.isDebugEnabled()) {
            LOG.debug("Response code: {}", httpResponse.getStatusLine().getStatusCode());
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
     * Set the value of a cookie
     * @param cookie the cookie
     */
    public void setCookie(Cookie cookie) {
        cookieStore.addCookie(cookie);
    }

    /**
     * Retrieves the cookies with the specified name from the cookie store.
     * @param name the name of the cookie to retrieve
     * @return the cookie with a matching name.
     */
    public List<Cookie> getCookies(String name) {
        return cookieStore.getCookies().stream()
            .filter(cookie -> Objects.equals(name, cookie.getName()))
            .toList();
    }

    /**
     * Check for a given {@link URI} if a proxy should be used or not.
     *
     * @param uri the URI to check
     * @return true if proxy should be used, else false
     */
    protected boolean useProxyFor(URI uri) {
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

    /**
     * @param defaultTimeout default timeout in seconds
     * @return return the HTTP Connection Timeout in milliseconds
     */
    public static int getHTTPConnectionTimeout(int defaultTimeout) {
        return Config.get().getInt(HTTP_CONNECTION_TIMEOUT, defaultTimeout) * TO_MILLISECONDS;
    }

    /**
     * @param defaultTimeout default timeout in seconds
     * @return return the HTTP Socket Timeout in milliseconds
     */
    public static int getHTTPSocketTimeout(int defaultTimeout) {
        return Config.get().getInt(HTTP_SOCKET_TIMEOUT, defaultTimeout) * TO_MILLISECONDS;
    }

    /**
     * @param defaultTimeout default timeout in seconds
     * @return return the HTTP Socket Timeout in milliseconds for salt api connections
     */
    public static int getSaltApiHTTPSocketTimeout(int defaultTimeout) {
        return Config.get().getInt(SALT_API_HTTP_SOCKET_TIMEOUT, defaultTimeout) * TO_MILLISECONDS;
    }
}
