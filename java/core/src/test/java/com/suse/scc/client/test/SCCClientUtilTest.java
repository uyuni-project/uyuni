/*
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

import static org.junit.jupiter.api.Assertions.assertEquals;

import com.redhat.rhn.testing.MockObjectTestCase;
import com.redhat.rhn.testing.TestUtils;

import com.suse.scc.client.SCCClientUtils;

import org.apache.commons.io.FileUtils;
import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.jmock.Expectations;
import org.junit.jupiter.api.Test;

import java.io.BufferedReader;
import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.net.URI;
import java.net.URISyntaxException;
import java.nio.charset.StandardCharsets;

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
     * @throws URISyntaxException if URI syntax is wrong
     */
    @Test
    public void testFilenameFromURI() throws URISyntaxException {
        URI uri = new URI(TEST_URI);
        String actual = SCCClientUtils.getLogFilename(uri, TEST_USER_NAME);
        assertEquals(TEST_FILE_NAME, actual);
    }

    /**
     * Tests {@link SCCClientUtils#getLoggingReader}
     * @throws IOException if anything goes wrong
     * @throws URISyntaxException if URI has wrong syntax
     */
    @Test
    public void testGetLoggingReader() throws IOException, URISyntaxException {
        URI uri = new URI(TEST_URI);

        // get fake data
        String expected = "testGetLoggingReader" + TestUtils.randomString();
        InputStream expectedInputStream =
                new ByteArrayInputStream(expected.getBytes(StandardCharsets.UTF_8));

        // get fake connection to TEST_URL that returns fake data above
        HttpEntity entityMock = mock(HttpEntity.class);
        HttpResponse mockResponse = mock(HttpResponse.class);
        context().checking(new Expectations() { {
            atLeast(1).of(entityMock).getContent();
            will(returnValue(expectedInputStream));
            atLeast(1).of(mockResponse).getEntity();
            will(returnValue(entityMock));
            oneOf(mockResponse).getFirstHeader(with(any(String.class)));
        } });

        // get reader
        BufferedReader reader =
                SCCClientUtils.getLoggingReader(uri, mockResponse, TEST_USER_NAME,
                        System.getProperty("java.io.tmpdir"), false);

        // expect to read fake data from reader
        assertEquals(expected, reader.readLine());

        // expect log file also to contain the same fake data
        File logFile = new File(System.getProperty("java.io.tmpdir") + File.separator + TEST_FILE_NAME);
        String actual = FileUtils.readFileToString(logFile);
        assertEquals(expected, actual);
    }
}
