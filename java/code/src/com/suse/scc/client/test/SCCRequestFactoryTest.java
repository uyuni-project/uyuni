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

import com.redhat.rhn.common.util.http.HttpClientAdapter;
import com.redhat.rhn.manager.setup.ProxySettingsDto;
import com.redhat.rhn.manager.setup.test.ProxySettingsManagerTest;
import com.redhat.rhn.testing.httpservermock.HttpServerMock;
import com.redhat.rhn.testing.httpservermock.Responder;

import com.suse.scc.client.SCCConfig;
import com.suse.scc.client.SCCRequestFactory;

import org.apache.commons.httpclient.HttpMethod;
import org.apache.commons.httpclient.HttpStatus;

import java.io.IOException;
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
    private static final String TEST_HOST = "test_host";
    private static final String EXPECTED_ACCEPT = "application/vnd.scc.suse.com.v4+json";
    private static final String EXPECTED_AUTH =
            "Basic dGVzdCBzZXJ2ZXIgdXNlcm5hbWU6dGVzdCBzZXJ2ZXIgcGFzc3dvcmQ=";
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
                ProxySettingsDto proxySettings = new ProxySettingsDto();
                proxySettings.setHostname(proxyUri.getHost() + ":" + proxyUri.getPort());
                proxySettings.setUsername("test proxy username");
                proxySettings.setPassword("test proxy password");
                ProxySettingsManagerTest.setProxySettings(proxySettings);

                SCCConfig config = new SCCConfig(new URI("http://" + TEST_HOST),
                    "test server username", "test server password", TEST_UUID,
                    null, SCCConfig.DEFAULT_LOGGING_DIR);

                SCCRequestFactory factory = SCCRequestFactory.getInstance();
                HttpMethod request = factory.initRequest("GET", "/test_url", config);
                HttpClientAdapter client = new HttpClientAdapter();
                return client.executeRequest(request,
                        config.getUsername(), config.getPassword());
            }
        };

        assertEquals((Integer) HttpStatus.SC_OK,
                serverMock.getResult(requester, new TestResponder()));
    }

    /**
     * Responds to HTTP requests coming from testInitConnection().
     */
    private class TestResponder implements Responder {

        @Override
        public void respond(Request request, Response response) {
            try {
                // All auth headers are sent directly (preemptive authentication mode)
                assertEquals(EXPECTED_PROXY_AUTH, request.getValue("Proxy-Authorization"));
                assertEquals(EXPECTED_AUTH, request.getValue("Authorization"));
                assertEquals(EXPECTED_ACCEPT, request.getValue("Accept"));
                assertEquals(TEST_UUID, request.getValue("SMS"));
                assertEquals(TEST_HOST, request.getValue("Host"));
                response.setCode(HttpStatus.SC_OK);
                response.commit();
            }
            catch (IOException e) {
                // never happens
            }
        }
    }
}
