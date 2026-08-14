/*
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

import static com.github.tomakehurst.wiremock.client.WireMock.aResponse;
import static com.github.tomakehurst.wiremock.client.WireMock.absent;
import static com.github.tomakehurst.wiremock.client.WireMock.anyUrl;
import static com.github.tomakehurst.wiremock.client.WireMock.equalTo;
import static com.github.tomakehurst.wiremock.client.WireMock.get;
import static com.github.tomakehurst.wiremock.client.WireMock.getRequestedFor;
import static com.github.tomakehurst.wiremock.client.WireMock.urlPathEqualTo;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import com.redhat.rhn.common.conf.Config;
import com.redhat.rhn.manager.setup.ProxySettingsDto;
import com.redhat.rhn.manager.setup.ProxySettingsManagerTest;
import com.redhat.rhn.testing.BaseTestCase;
import com.redhat.rhn.testing.TestStatics;

import com.github.tomakehurst.wiremock.WireMockServer;
import com.github.tomakehurst.wiremock.core.WireMockConfiguration;

import org.apache.http.HttpStatus;
import org.apache.http.StatusLine;
import org.apache.http.client.methods.HttpGet;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.net.URI;
import java.util.Base64;

/**
 * Integrational unit tests for {@link HttpClientAdapter}.
 */
public class HttpClientAdapterTest extends BaseTestCase {

    private WireMockServer wireMockServer;

    // String values
    private static final String TEST_PASSWORD = "testpassword";
    private static final String TEST_AUTHORITY = "foobar.com:1234";
    private static final String PROXY_TEST_USER = "proxyuser";
    private static final String PROXY_TEST_PASSWORD = "proxypassword";

    @BeforeEach
    void setup() {
        wireMockServer = new WireMockServer(WireMockConfiguration.wireMockConfig().dynamicPort());
        wireMockServer.start();
    }

    @AfterEach
    public void tearDown() {
        wireMockServer.stop();

        // Clear proxy settings
        ProxySettingsDto proxySettings = new ProxySettingsDto();
        proxySettings.setHostname("");
        proxySettings.setUsername("");
        proxySettings.setPassword("");
        ProxySettingsManagerTest.setProxySettings(proxySettings);

        // Clean up the no_proxy setting
        setNoProxy("");
    }

    /**
     * Test for executeRequest(): an authenticated GET request.
     * @throws Exception in case there is a problem
     */
    @Test
    public void testGetRequestAuthenticated() throws Exception {
        String authHeader = basicAuthHeader(TestStatics.TEST_USER, TEST_PASSWORD);
        String host = "localhost:" + wireMockServer.port();

        // First request without credentials
        wireMockServer.stubFor(
            get(urlPathEqualTo("/"))
                .atPriority(1)
                .withHeader("Authorization", absent())
                .willReturn(aResponse()
                    .withStatus(HttpStatus.SC_UNAUTHORIZED)
                    .withHeader("WWW-Authenticate", "Basic realm"))
        );

        // Second request with the authorization
        wireMockServer.stubFor(
            get(urlPathEqualTo("/"))
                .atPriority(2)
                .withHeader("Authorization", equalTo(authHeader))
                .willReturn(aResponse().withStatus(HttpStatus.SC_OK))
        );

        HttpGet request = new HttpGet(wireMockServer.baseUrl() + "/");
        HttpClientAdapter client = new HttpClientAdapter();
        StatusLine status = client.executeRequest(request, TestStatics.TEST_USER, TEST_PASSWORD).getStatusLine();

        assertEquals(HttpStatus.SC_OK, status.getStatusCode());
        wireMockServer.verify(
            getRequestedFor(urlPathEqualTo("/"))
                .withHeader("Host", equalTo(host))
                .withHeader("Authorization", absent())
        );
        wireMockServer.verify(
            getRequestedFor(urlPathEqualTo("/"))
                .withHeader("Host", equalTo(host))
                .withHeader("Authorization", equalTo(authHeader))
        );
    }

    @Test
    public void testGetRequestViaProxy() throws Exception {
        String proxyAuthHeader = basicAuthHeader(PROXY_TEST_USER, PROXY_TEST_PASSWORD);
        String userAuthHeader = basicAuthHeader(TestStatics.TEST_USER, TEST_PASSWORD);

        ProxySettingsDto proxySettings = new ProxySettingsDto();
        proxySettings.setHostname("localhost:" + wireMockServer.port());
        proxySettings.setUsername(PROXY_TEST_USER);
        proxySettings.setPassword(PROXY_TEST_PASSWORD);
        ProxySettingsManagerTest.setProxySettings(proxySettings);

        wireMockServer.stubFor(
            get(anyUrl())
                .atPriority(1)
                .withHeader("Proxy-Authorization", absent())
                .willReturn(aResponse()
                    .withStatus(HttpStatus.SC_PROXY_AUTHENTICATION_REQUIRED)
                    .withHeader("Proxy-Authenticate", "Basic realm"))
        );

        wireMockServer.stubFor(
            get(anyUrl())
                .atPriority(2)
                .withHeader("Proxy-Authorization", equalTo(proxyAuthHeader))
                .withHeader("Authorization", absent())
                .willReturn(aResponse()
                    .withStatus(HttpStatus.SC_UNAUTHORIZED)
                    .withHeader("WWW-Authenticate", "Basic realm"))
        );

        wireMockServer.stubFor(
            get(anyUrl())
                .atPriority(3)
                .withHeader("Proxy-Authorization", equalTo(proxyAuthHeader))
                .withHeader("Authorization", equalTo(userAuthHeader))
                .withHeader("Host", equalTo(TEST_AUTHORITY))
                .willReturn(aResponse().withStatus(HttpStatus.SC_OK))
        );

        HttpGet request = new HttpGet("http://" + TEST_AUTHORITY);
        HttpClientAdapter client = new HttpClientAdapter();
        int status = client.executeRequest(request, TestStatics.TEST_USER, TEST_PASSWORD)
                .getStatusLine().getStatusCode();

        assertEquals(HttpStatus.SC_OK, status);

        // Verify both phases happened
        wireMockServer.verify(
            getRequestedFor(urlPathEqualTo("/"))
                .withHeader("Proxy-Authorization", absent())
        );
        wireMockServer.verify(
            getRequestedFor(urlPathEqualTo("/"))
                .withHeader("Host", equalTo(TEST_AUTHORITY))
                .withHeader("Authorization", equalTo(userAuthHeader))
                .withHeader("Proxy-Authorization", equalTo(proxyAuthHeader))
                .withHeader("Proxy-Connection", equalTo("Keep-Alive"))
        );
    }

    /**
     * Test the logic in HttpClientAdapter.useProxyFor().
     * @throws Exception in case of a problem
     */
    @Test
    public void testUseProxyFor() throws Exception {
        // Configure "no_proxy"
        setNoProxy("example.com, false.com");

        // No proxy would be used for these examples
        boolean result = callUseProxyFor(new URI("http://example.com:1234"));
        assertFalse(result);

        result = callUseProxyFor(new URI("http://foo.example.com"));
        assertFalse(result);

        result = callUseProxyFor(new URI("http://check.false.com"));
        assertFalse(result);

        result = callUseProxyFor(new URI("http://localhost:1234"));
        assertFalse(result);

        result = callUseProxyFor(new URI("http://127.0.0.1:1234"));
        assertFalse(result);

        // ... while a proxy would be used for those
        result = callUseProxyFor(new URI("http://fooexample.com:1234"));
        assertTrue(result);

        result = callUseProxyFor(new URI("http://foobar.com"));
        assertTrue(result);

        result = callUseProxyFor(new URI("http://truefalse.com"));
        assertTrue(result);
    }

    /**
     * Test the logic in HttpClientAdapter.useProxyFor(): "no_proxy" contains "*".
     * @throws Exception in case of a problem
     */
    @Test
    public void testUseProxyForAsterisk() throws Exception {
        // Configure "no_proxy" cotaining an asterisk
        setNoProxy("example.com, *");

        // No proxy should be used for *all* hosts, even for example.com
        boolean result = callUseProxyFor(new URI("http://example.com"));
        assertFalse(result);

        result = callUseProxyFor(new URI("http://foobar.com"));
        assertFalse(result);
    }

    /**
     * Test the logic in HttpClientAdapter.useProxyFor(): "no_proxy" is empty.
     * @throws Exception in case of a problem
     */
    @Test
    public void testUseProxyForEmpty() throws Exception {
        // Configure "no_proxy" cotaining an asterisk
        setNoProxy("");

        // Proxy should be used for *all* hosts (except localhost etc.)
        boolean result = callUseProxyFor(new URI("http://example.com"));
        assertTrue(result);

        result = callUseProxyFor(new URI("http://foobar.com"));
        assertTrue(result);

        result = callUseProxyFor(new URI("http://localhost:1234"));
        assertFalse(result);
    }

    /**
     * Configure the "no_proxy" setting for testing.
     * @param value
     */
    private void setNoProxy(String value) {
        Config.get().setString(HttpClientAdapter.NO_PROXY, value);
    }

    /**
     * Call the private method useProxyFor() on a new {@link HttpClientAdapter}.
     *
     * @param uri the URI to call useProxyFor() with
     * @return the result of useProxyFor()
     */
    private boolean callUseProxyFor(URI uri) {
        var httpClientAdapter = new HttpClientAdapter() {
            @Override
            public boolean useProxyFor(URI uri) {
                return super.useProxyFor(uri);
            }
        };

        return httpClientAdapter.useProxyFor(uri);
    }

    private static String basicAuthHeader(String username, String password) {
        String credentials = username + ":" + password;
        String encoded = Base64.getEncoder().encodeToString(credentials.getBytes());
        return "Basic " + encoded;
    }

}
