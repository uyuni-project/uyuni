/*
 * Copyright (c) 2025 SUSE LLC
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

package com.suse.proxy.test;

import static java.util.Collections.emptyList;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import com.redhat.rhn.common.RhnError;
import com.redhat.rhn.common.RhnErrorReport;
import com.redhat.rhn.testing.MockObjectTestCase;

import com.suse.manager.api.ParseException;
import com.suse.proxy.ProxyRegistryUtils;
import com.suse.proxy.ProxyRegistryUtilsImpl;
import com.suse.proxy.RegistryUrl;
import com.suse.proxy.get.ProxyConfigGetRegistryTags;
import com.suse.rest.RestClientException;

import org.jmock.Expectations;
import org.jmock.imposters.ByteBuddyClassImposteriser;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.net.URISyntaxException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * Tests for the {@link ProxyConfigGetRegistryTags} class
 */
@SuppressWarnings({"java:S3599", "java:S1171", "java:S1130"})
public class ProxyConfigGetRegistryTagsTests extends MockObjectTestCase {
    private static final String DUMMY_REGISTRY_URL = "dummyRegistryUrl.com/at/some/path";
    private ProxyRegistryUtils mockProxyRegistryUtils;

    public static void assertExpectedErrors(String[] expectedErrorMessages, RhnErrorReport rhnErrorReport) {
        assertTrue(rhnErrorReport.hasErrors());
        assertEquals(expectedErrorMessages.length, rhnErrorReport.getErrors().size());

        Set<String> actualErrorMessages =
                rhnErrorReport.getErrors().stream().map(RhnError::getMessage).collect(Collectors.toSet());
        assertTrue(actualErrorMessages.containsAll(Set.of(expectedErrorMessages)));
    }

    @BeforeEach
    public void setUp() {
        setImposteriser(ByteBuddyClassImposteriser.INSTANCE);
        mockProxyRegistryUtils = context.mock(ProxyRegistryUtils.class);
    }

    /**
     * Tests a scenario where an url for a specific image is provided and 2 tags are retrieved
     */
    @Test
    public void testWhenExactMatch() throws ParseException {
        final List<String> expectedTags = Arrays.asList("tag1", "tag2");
        ProxyConfigGetRegistryTags getRegistryTags =
                new ProxyConfigGetRegistryTags(DUMMY_REGISTRY_URL, true, mockProxyRegistryUtils);

        context.checking(new Expectations() {{
            allowing(mockProxyRegistryUtils).getTags(with(any(RegistryUrl.class)));
            will(returnValue(expectedTags));
        }});

        getRegistryTags.retrieveTags();
        assertFalse(getRegistryTags.getErrorReport().hasErrors());
        assertEquals(expectedTags, getRegistryTags.getTags());
    }

    /**
     * Tests a scenario where an url for a specific image but not tags are found
     * It is expected that an error is registered in the error report.
     */
    @Test
    public void testWhenExactMatchReturnsNoTags() throws ParseException {
        final String[] expectedErrorMessages = {"No tags found on registry"};

        ProxyConfigGetRegistryTags getRegistryTags =
                new ProxyConfigGetRegistryTags(DUMMY_REGISTRY_URL, true, mockProxyRegistryUtils);

        context.checking(new Expectations() {{
            allowing(mockProxyRegistryUtils).getTags(with(any(RegistryUrl.class)));
            will(returnValue(emptyList()));
        }});

        getRegistryTags.retrieveTags();
        assertNotNull(getRegistryTags.getTags());
        assertExpectedErrors(expectedErrorMessages, getRegistryTags.getErrorReport());
    }

    /**
     * Tests a scenario retrieving tags throws a {@link ParseException}.
     * It is expected that the error is registered in the error report.
     */
    @Test
    public void testWhenParseException() throws ParseException {
        final String[] expectedErrorMessages = {"Error parsing response"};

        ProxyConfigGetRegistryTags getRegistryTags =
                new ProxyConfigGetRegistryTags(DUMMY_REGISTRY_URL, true, mockProxyRegistryUtils);
        context.checking(new Expectations() {{
            allowing(mockProxyRegistryUtils).getTags(with(any(RegistryUrl.class)));
            will(throwException(new ParseException("dummy parse exception")));
        }});

        getRegistryTags.retrieveTags();
        assertNull(getRegistryTags.getTags());
        assertExpectedErrors(expectedErrorMessages, getRegistryTags.getErrorReport());
    }

    /**
     * Tests a scenario retrieving tags throws a {@link URISyntaxException}.
     * It is expected that the error is registered in the error report.
     */
    @Test
    public void testWhenURISyntaxException() {
        final String[] expectedErrorMessages = {"Invalid URL"};

        ProxyConfigGetRegistryTags getRegistryTags = new ProxyConfigGetRegistryTags("/", true, null);

        getRegistryTags.retrieveTags();
        assertNull(getRegistryTags.getTags());
        assertExpectedErrors(expectedErrorMessages, getRegistryTags.getErrorReport());
    }

    /**
     * Tests a scenario retrieving tags throws a {@link RestClientException}.
     * It is expected that the error is registered in the error report.
     */
    @Test
    public void testWhenRestClientException() {
        final String[] expectedErrorMessages = {"Error retrieving tags"};

        ProxyConfigGetRegistryTags getRegistryTags =
                new ProxyConfigGetRegistryTags("localhost", true, new ProxyRegistryUtilsImpl());

        getRegistryTags.retrieveTags();
        assertNull(getRegistryTags.getTags());
        assertExpectedErrors(expectedErrorMessages, getRegistryTags.getErrorReport());
    }

    /**
     * Tests a scenario where a base url is provided but no repositories are found.
     * It is expected that an error is registered in the error report.
     */
    @Test
    public void testWhenBaseUrlReturnsNoRepositories() throws ParseException {
        final String[] expectedErrorMessages = {"No repositories found on registry"};
        ProxyConfigGetRegistryTags getRegistryTags =
                new ProxyConfigGetRegistryTags(DUMMY_REGISTRY_URL, false, mockProxyRegistryUtils);

        context.checking(new Expectations() {{
            allowing(mockProxyRegistryUtils).getRepositories(with(any(RegistryUrl.class)));
            will(returnValue(new ArrayList<>()));
        }});

        getRegistryTags.retrieveTags();
        assertNull(getRegistryTags.getTags());
        assertExpectedErrors(expectedErrorMessages, getRegistryTags.getErrorReport());
    }


    /**
     * Tests a scenario where a base url is provided but no repositories are found.
     * It is expected that an error is registered in the error report.
     */
    @Test
    public void testWhenBaseUrlDoesNotHaveAllProxyImages() throws ParseException {
        final String[] expectedErrorMessages = {"Cannot find all images in catalog"};
        ProxyConfigGetRegistryTags getRegistryTags =
                new ProxyConfigGetRegistryTags(DUMMY_REGISTRY_URL, false, mockProxyRegistryUtils);

        context.checking(new Expectations() {{
            allowing(mockProxyRegistryUtils).getRepositories(with(any(RegistryUrl.class)));
            will(returnValue(Collections.singletonList("not it!")));
        }});

        getRegistryTags.retrieveTags();
        assertNull(getRegistryTags.getTags());
        assertExpectedErrors(expectedErrorMessages, getRegistryTags.getErrorReport());
    }

    /**
     * Tests a scenario where a base url is provided.
     * The first for images have common tags, but the last one returns no tags.
     * It is expected that an error is registered in the error report.
     */
    @Test
    public void testWhenOneImageReturnsNoTags() throws ParseException {
        final String[] expectedErrorMessages = {"No common tags found among proxy images"};

        ProxyConfigGetRegistryTags getRegistryTags =
                new ProxyConfigGetRegistryTags(DUMMY_REGISTRY_URL, false, mockProxyRegistryUtils);

        context.checking(new Expectations() {{
            allowing(mockProxyRegistryUtils).getRepositories(with(any(RegistryUrl.class)));
            will(returnValue(Arrays.asList(
                    "at/some/path/proxy-httpd",
                    "at/some/path/proxy-salt-broker",
                    "at/some/path/proxy-squid",
                    "at/some/path/proxy-ssh",
                    "at/some/path/proxy-tftpd"
            )));

            exactly(4).of(mockProxyRegistryUtils).getTags(with(any(RegistryUrl.class)));
            will(returnValue(Arrays.asList("tag1", "tag2")));
            oneOf(mockProxyRegistryUtils).getTags(with(any(RegistryUrl.class)));
            will(returnValue(emptyList()));
        }});

        getRegistryTags.retrieveTags();
        assertNull(getRegistryTags.getTags());
        assertExpectedErrors(expectedErrorMessages, getRegistryTags.getErrorReport());
    }

    /**
     * Tests a scenario where a base url is provided.
     * All images returns tags but none of them is common .
     * It is expected that an error is registered in the error report.
     */
    @Test
    public void testWhenNoCommonTags() throws ParseException {
        final String[] expectedErrorMessages = {"No common tags found among proxy images"};
        String commonTag = "tag-common";

        ProxyConfigGetRegistryTags getRegistryTags =
                new ProxyConfigGetRegistryTags(DUMMY_REGISTRY_URL, false, mockProxyRegistryUtils);

        context.checking(new Expectations() {{
            allowing(mockProxyRegistryUtils).getRepositories(with(any(RegistryUrl.class)));
            will(returnValue(Arrays.asList(
                    "at/some/path/proxy-httpd",
                    "at/some/path/proxy-salt-broker",
                    "at/some/path/proxy-squid",
                    "at/some/path/proxy-ssh",
                    "at/some/path/proxy-tftpd"
            )));

            oneOf(mockProxyRegistryUtils).getTags(with(any(RegistryUrl.class)));
            will(returnValue(Arrays.asList("tag1", commonTag)));
            oneOf(mockProxyRegistryUtils).getTags(with(any(RegistryUrl.class)));
            will(returnValue(Arrays.asList("tag2", commonTag)));
            oneOf(mockProxyRegistryUtils).getTags(with(any(RegistryUrl.class)));
            will(returnValue(Arrays.asList("tag3", commonTag)));
            oneOf(mockProxyRegistryUtils).getTags(with(any(RegistryUrl.class)));
            will(returnValue(Arrays.asList("tag4", commonTag)));
            oneOf(mockProxyRegistryUtils).getTags(with(any(RegistryUrl.class)));
            will(returnValue(List.of("tag5")));
        }});

        getRegistryTags.retrieveTags();
        assertNull(getRegistryTags.getTags());
        assertExpectedErrors(expectedErrorMessages, getRegistryTags.getErrorReport());
    }

    /**
     * Tests a scenario where a base url is provided.
     * All images returns tags but only two of them are common.
     */
    @Test
    public void testWhenCommonTags() throws ParseException {
        final String commonTag1 = "tag-common";
        final String commonTag2 = "latest";
        final Set<String> expectedCommonTags = new HashSet<>(Arrays.asList(commonTag1, commonTag2));


        ProxyConfigGetRegistryTags getRegistryTags =
                new ProxyConfigGetRegistryTags(DUMMY_REGISTRY_URL, false, mockProxyRegistryUtils);

        context.checking(new Expectations() {{
            allowing(mockProxyRegistryUtils).getRepositories(with(any(RegistryUrl.class)));
            will(returnValue(Arrays.asList(
                    "at/some/path/proxy-httpd",
                    "at/some/path/proxy-salt-broker",
                    "at/some/path/proxy-squid",
                    "at/some/path/proxy-ssh",
                    "at/some/path/proxy-tftpd"
            )));

            oneOf(mockProxyRegistryUtils).getTags(with(any(RegistryUrl.class)));
            will(returnValue(Arrays.asList("tag1", "tag-a", commonTag1, commonTag2)));
            oneOf(mockProxyRegistryUtils).getTags(with(any(RegistryUrl.class)));
            will(returnValue(Arrays.asList("tag2", "tagA", commonTag1, commonTag2)));
            oneOf(mockProxyRegistryUtils).getTags(with(any(RegistryUrl.class)));
            will(returnValue(Arrays.asList("tag3", "tA", "tAA", "tAAA", commonTag1, commonTag2)));
            oneOf(mockProxyRegistryUtils).getTags(with(any(RegistryUrl.class)));
            will(returnValue(Arrays.asList("tag4", "taag", commonTag1, commonTag2)));
            oneOf(mockProxyRegistryUtils).getTags(with(any(RegistryUrl.class)));
            will(returnValue(Arrays.asList("tag5", commonTag1, commonTag2)));
        }});

        getRegistryTags.retrieveTags();
        assertFalse(getRegistryTags.getErrorReport().hasErrors());
        assertEquals(expectedCommonTags, new HashSet<>(getRegistryTags.getTags()));
    }
}
