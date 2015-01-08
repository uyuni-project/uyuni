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
package com.suse.scc.client.test;

import com.redhat.rhn.testing.httpservermock.HttpServerMock;
import com.redhat.rhn.testing.httpservermock.Responder;

import com.suse.scc.client.SCCConfig;
import com.suse.scc.client.SCCProxySettings;
import com.suse.scc.client.SCCRequestFactory;

import java.io.IOException;
import java.net.HttpURLConnection;
import java.net.URI;
import java.util.concurrent.Callable;

import junit.framework.TestCase;
import simple.http.Request;
import simple.http.Response;

/**
 * Tests {@link SCCRequestFactory}
 */
public class SCCRequestFactoryTest extends TestCase {
    private static final String TEST_UUID = "test uuid";
    private static final String TEST_HOST = "test_host:666";
    private static final String EXPECTED_ACCEPT = "application/vnd.scc.suse.com.v4+json";
    private static final String EXPECTED_AUTH =
            "BASIC dGVzdCBzZXJ2ZXIgdXNlcm5hbWU6dGVzdCBzZXJ2ZXIgcGFzc3dvcmQ=";
    private static final String EXPECTED_PROXY_AUTH =
            "Basic dGVzdCBwcm94eSB1c2VybmFtZTp0ZXN0IHByb3h5IHBhc3N3b3Jk";

    /**
     * Tests initConnection().
     * @throws Exception in case anything goes wrong
     */
    public void testInitConnection() throws Exception {
        HttpServerMock serverMock = new HttpServerMock();
        final URI proxyUri = serverMock.getURI();
        Callable<Integer> requester = new Callable<Integer>() {

            @Override
            public Integer call() throws Exception {
                SCCProxySettings proxySettings = new SCCProxySettings(proxyUri.getHost(),
                    proxyUri.getPort(), "test proxy username", "test proxy password");
                SCCConfig config = new SCCConfig(new URI("http://" + TEST_HOST),
                    "test server username", "test server password", TEST_UUID,
                    null, SCCConfig.DEFAULT_LOGGING_DIR, proxySettings);
                SCCRequestFactory factory = SCCRequestFactory.getInstance();
                HttpURLConnection connection = factory.initConnection("GET", "/test_url",
                    config);
                connection.connect();
                return connection.getResponseCode();
            }
        };

        assertEquals((Integer) HttpURLConnection.HTTP_OK,
                serverMock.getResult(requester, new TestResponder()));
    }

    /**
     * Responds to HTTP requests coming from testInitConnection().
     */
    private class TestResponder implements Responder {

        @Override
        public void respond(Request request, Response response) {
            try {
                // 1. a first request should be sent without proxy authorization
                // data, and we respond with 407 - Proxy Authorization Required
                String authorizationData = request.getValue("Proxy-Authorization");
                if (authorizationData == null) {
                    response.set("Proxy-Authenticate", "Basic");
                    response.setCode(HttpURLConnection.HTTP_PROXY_AUTH);
                }
                else {
                    // 2. a second request should be sent with proper
                    // authorization data, we respond with 200 - OK
                    assertEquals(EXPECTED_PROXY_AUTH, authorizationData);
                    assertEquals(EXPECTED_AUTH, request.getValue("Authorization"));
                    assertEquals(EXPECTED_ACCEPT, request.getValue("Accept"));
                    assertEquals(TEST_UUID, request.getValue("SMS"));
                    assertEquals(TEST_HOST, request.getValue("host"));

                    response.setCode(HttpURLConnection.HTTP_OK);
                }
                response.commit();
            }
            catch (IOException e) {
                // never happens
            }

        }
    }
}
