/**
 * Copyright (c) 2014 SUSE LLC
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

import com.redhat.rhn.testing.TestUtils;

import com.suse.scc.client.SCCClientUtils;

import org.apache.commons.httpclient.HttpMethod;
import org.apache.commons.httpclient.URI;
import org.apache.commons.httpclient.URIException;
import org.apache.commons.httpclient.methods.GetMethod;
import org.apache.commons.io.FileUtils;
import org.jmock.Mock;
import org.jmock.cglib.MockObjectTestCase;

import java.io.BufferedReader;
import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;

/**
 * Tests {@link SCCClientUtils}
 */
public class SCCClientUtilTest extends MockObjectTestCase {

    private static final String TEST_USER_NAME = "t";
    private static final String TEST_FILE_NAME =
            "t_organizations_subscriptions_page_316.json";
    private static final String TEST_URI =
            "https://scc.suse.com/connect/organizations/subscriptions/?page=316";

    /**
     * Tests {@link SCCClientUtils#getLogFilename}
     * @throws URIException never
     */
    public void testFilenameFromURI() throws URIException {
        URI uri = new URI(TEST_URI, false);
        String actual = SCCClientUtils.getLogFilename(uri, TEST_USER_NAME);
        assertEquals(TEST_FILE_NAME, actual);
    }

    /**
     * Tests {@link SCCClientUtils#getLoggingReader}
     * @throws IOException if anything goes wrong
     */
    public void testGetLoggingReader() throws IOException {
        URI uri = new URI(TEST_URI, false);

        // get fake data
        String expected = "testGetLoggingReader" + TestUtils.randomString();
        InputStream expectedInputStream =
                new ByteArrayInputStream(expected.getBytes("UTF-8"));

        // get fake connection to TEST_URL that returns fake data above
        Mock mockRequest =
                mock(GetMethod.class, new Class[] {String.class}, new Object[] {TEST_URI});
        mockRequest.expects(atLeastOnce()).method("getResponseBodyAsStream")
                .will(returnValue(expectedInputStream));
        mockRequest.expects(once()).method("getResponseHeader").withAnyArguments();
        mockRequest.expects(once()).method("getURI").will(returnValue(uri));
        HttpMethod request = (HttpMethod) mockRequest.proxy();

        // get reader
        BufferedReader reader =
                SCCClientUtils.getLoggingReader(request, TEST_USER_NAME,
                        System.getProperty("java.io.tmpdir"));

        // expect to read fake data from reader
        assertEquals(expected, reader.readLine());

        // expect log file also to contain the same fake data
        File logFile = new File(System.getProperty("java.io.tmpdir") + File.separator
                        + TEST_FILE_NAME);
        String actual = FileUtils.readFileToString(logFile);
        assertEquals(expected, actual);
    }
}
