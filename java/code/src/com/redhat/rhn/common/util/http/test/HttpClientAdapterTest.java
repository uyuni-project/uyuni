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
package com.redhat.rhn.common.util.http.test;

import com.redhat.rhn.common.conf.Config;
import com.redhat.rhn.common.util.http.HttpClientAdapter;
import com.redhat.rhn.manager.setup.ProxySettingsDto;
import com.redhat.rhn.manager.setup.test.ProxySettingsManagerTest;
import com.redhat.rhn.testing.httpservermock.HttpServerMock;
import com.redhat.rhn.testing.httpservermock.Responder;

import org.apache.http.HttpStatus;
import org.apache.http.client.methods.HttpGet;

import java.io.IOException;
import java.lang.reflect.Method;
import java.net.URI;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.Callable;

import junit.framework.TestCase;
import simple.http.Request;
import simple.http.Response;

/**
 * Integrational unit tests for {@link HttpClientAdapter}.
 */
public class HttpClientAdapterTest extends TestCase {

    // Mock server for reuse
    private static final HttpServerMock SERVER_MOCK = new HttpServerMock();

    // String values
    private static final String TEST_USER = "testuser";
    private static final String TEST_PASSWORD = "testpassword";
    private static final String TEST_AUTHORITY = "foobar.com:1234";
    private static final String PROXY_TEST_USER = "proxyuser";
    private static final String PROXY_TEST_PASSWORD = "proxypassword";
    private static final String EXPECTED_AUTHORIZATION =
            "Basic dGVzdHVzZXI6dGVzdHBhc3N3b3Jk";
    private static final String EXPECTED_PROXY_AUTHORIZATION =
            "Basic cHJveHl1c2VyOnByb3h5cGFzc3dvcmQ=";

    /**
     * Test for executeRequest(): an authenticated GET request.
     * @throws Exception in case there is a problem
     */
    public void testGetRequestAuthenticated() throws Exception {
        Callable<Integer> requester = new Callable<Integer>() {
            @Override
            public Integer call() throws Exception {
                HttpGet request = new HttpGet(SERVER_MOCK.getURI().toString());
                HttpClientAdapter client = new HttpClientAdapter();
                return client.executeRequest(request, TEST_USER, TEST_PASSWORD)
                        .getStatusLine().getStatusCode();
            }
        };

        Map<String, String> headers = new HashMap<String, String>();
        headers.put("Host", SERVER_MOCK.getURI().getAuthority());
        headers.put("Authorization", EXPECTED_AUTHORIZATION);
        assertEquals((Integer) HttpStatus.SC_OK,
                SERVER_MOCK.getResult(requester, new TestResponder(headers)));
    }

    /**
     * Test for executeRequest(): an authenticated GET request via a proxy.
     * @throws Exception in case there is a problem
     */
    public void testGetRequestViaProxy() throws Exception {
        // Configure proxy
        ProxySettingsDto proxySettings = new ProxySettingsDto();
        proxySettings.setHostname(SERVER_MOCK.getURI().getAuthority());
        proxySettings.setUsername(PROXY_TEST_USER);
        proxySettings.setPassword(PROXY_TEST_PASSWORD);
        ProxySettingsManagerTest.setProxySettings(proxySettings);

        Callable<Integer> requester = new Callable<Integer>() {
            @Override
            public Integer call() throws Exception {
                HttpGet request = new HttpGet("http://" + TEST_AUTHORITY);
                HttpClientAdapter client = new HttpClientAdapter();
                return client.executeRequest(request, TEST_USER, TEST_PASSWORD)
                        .getStatusLine().getStatusCode();
            }
        };

        Map<String, String> headers = new HashMap<String, String>();
        headers.put("Host", TEST_AUTHORITY);
        headers.put("Authorization", EXPECTED_AUTHORIZATION);
        headers.put("Proxy-Authorization", EXPECTED_PROXY_AUTHORIZATION);
        headers.put("Proxy-Connection", "Keep-Alive");
        assertEquals((Integer) HttpStatus.SC_OK,
                SERVER_MOCK.getResult(requester, new TestResponder(headers)));
    }

    /**
     * Responds to HTTP requests coming from the tests in this class while
     * verifying a given map of headers and values.
     */
    private class TestResponder implements Responder {

        private final Map<String, String> headers;

        /**
         * This constructor takes a map of headers and expected values to verify.
         * @param headers the map of headers and expected values
         */
        TestResponder(Map<String, String> headersIn) {
            headers = headersIn;
        }

        @Override
        public void respond(Request request, Response response) {
            try {
                String proxyAuthKey = "Proxy-Authorization";
                String proxyAuthValue = request.getValue(proxyAuthKey);
                String authKey = "Authorization";
                String authValue = request.getValue(authKey);

                if (headers.containsKey(proxyAuthKey) && proxyAuthValue == null) {
                    response.set("Proxy-Authenticate", "Basic realm");
                    response.setCode(HttpStatus.SC_PROXY_AUTHENTICATION_REQUIRED);
                }
                else if (headers.containsKey(authKey) && authValue == null) {
                    response.set("WWW-Authenticate", "Basic realm");
                    response.setCode(HttpStatus.SC_UNAUTHORIZED);
                }
                else {
                    for (String header : headers.keySet()) {
                        assertEquals(headers.get(header), request.getValue(header));
                    }
                    response.setCode(HttpStatus.SC_OK);
                }

                response.commit();
            }
            catch (IOException e) {
                // never happens
            }
        }
    }

    /**
     * Test the logic in HttpClientAdapter.useProxyFor().
     * @throws Exception in case of a problem
     */
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

    /* (non-Javadoc)
     * @see junit.framework.TestCase#tearDown()
     */
    @Override
    protected void tearDown() throws Exception {
        super.tearDown();

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
     * @throws Exception in case of an Exception is thrown in here
     */
    private boolean callUseProxyFor(URI uri) throws Exception {
        Method method = HttpClientAdapter.class.getDeclaredMethod("useProxyFor", URI.class);
        method.setAccessible(true);
        return (Boolean) method.invoke(new HttpClientAdapter(), uri);
    }
}
