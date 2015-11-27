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

import com.suse.scc.client.SCCConfig;
import com.suse.scc.client.SCCRequestFactory;

import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpRequestBase;

import java.net.URI;

import junit.framework.TestCase;

/**
 * Tests {@link SCCRequestFactory}
 */
public class SCCRequestFactoryTest extends TestCase {

    // Headers to be verified
    private static final String TEST_SCHEME = "https";
    private static final String TEST_HOST = "testhost";
    private static final String TEST_PATH = "/test_url";
    private static final String TEST_UUID = "test_uuid";
    private static final String EXPECTED_ACCEPT = "application/vnd.scc.suse.com.v4+json";
    private static final String EXPECTED_ACCEPT_ENCODING = "gzip, deflate";

    /**
     * Tests initRequest(): Init a request to SCC and check it for correctness.
     * @throws Exception in case anything goes wrong
     */
    public void testInitRequest() throws Exception {
        SCCConfig config = new SCCConfig(new URI(TEST_SCHEME + "://" + TEST_HOST),
                "user", "pass", TEST_UUID, null, SCCConfig.DEFAULT_LOGGING_DIR);
        SCCRequestFactory factory = SCCRequestFactory.getInstance();
        HttpRequestBase request = factory.initRequest("GET", TEST_PATH, config);
        assertTrue(request instanceof HttpGet);
        assertEquals(TEST_SCHEME, request.getURI().getScheme());
        assertEquals(TEST_HOST, request.getURI().getHost());
        assertEquals(TEST_PATH, request.getURI().getPath());
        assertEquals(EXPECTED_ACCEPT,
                request.getFirstHeader("Accept").getValue());
        assertEquals(EXPECTED_ACCEPT_ENCODING,
                request.getFirstHeader("Accept-Encoding").getValue());
        assertEquals(TEST_UUID,
                request.getFirstHeader("SMS").getValue());
    }
}
