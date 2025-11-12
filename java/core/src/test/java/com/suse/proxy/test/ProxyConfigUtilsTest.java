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
 * SPDX-License-Identifier: GPL-2.0-only
 *
 * Red Hat trademarks are not licensed under GPLv2. No permission is
 * granted to use or replicate Red Hat trademarks that are incorporated
 * in this software or its documentation.
 */

package com.suse.proxy.test;

import static com.suse.proxy.ProxyConfigUtils.EMAIL_FIELD;
import static com.suse.proxy.ProxyConfigUtils.INTERMEDIATE_CAS_FIELD;
import static com.suse.proxy.ProxyConfigUtils.MAX_CACHE_FIELD;
import static com.suse.proxy.ProxyConfigUtils.PARENT_FQDN_FIELD;
import static com.suse.proxy.ProxyConfigUtils.PILLAR_REGISTRY_ENTRY;
import static com.suse.proxy.ProxyConfigUtils.PILLAR_REGISTRY_TAG_ENTRY;
import static com.suse.proxy.ProxyConfigUtils.PILLAR_REGISTRY_URL_ENTRY;
import static com.suse.proxy.ProxyConfigUtils.PROXY_CERT_FIELD;
import static com.suse.proxy.ProxyConfigUtils.PROXY_FQDN_FIELD;
import static com.suse.proxy.ProxyConfigUtils.PROXY_KEY_FIELD;
import static com.suse.proxy.ProxyConfigUtils.PROXY_PORT_FIELD;
import static com.suse.proxy.ProxyConfigUtils.ROOT_CA_FIELD;
import static com.suse.proxy.ProxyConfigUtils.SERVER_ID_FIELD;
import static com.suse.proxy.test.ProxyConfigUpdateTestUtils.DUMMY_ADMIN_MAIL;
import static com.suse.proxy.test.ProxyConfigUpdateTestUtils.DUMMY_INTERMEDIATE_CA_1;
import static com.suse.proxy.test.ProxyConfigUpdateTestUtils.DUMMY_INTERMEDIATE_CA_2;
import static com.suse.proxy.test.ProxyConfigUpdateTestUtils.DUMMY_MAX_CACHE;
import static com.suse.proxy.test.ProxyConfigUpdateTestUtils.DUMMY_PARENT_FQDN;
import static com.suse.proxy.test.ProxyConfigUpdateTestUtils.DUMMY_PROXY_CERT;
import static com.suse.proxy.test.ProxyConfigUpdateTestUtils.DUMMY_PROXY_FQDN;
import static com.suse.proxy.test.ProxyConfigUpdateTestUtils.DUMMY_PROXY_KEY;
import static com.suse.proxy.test.ProxyConfigUpdateTestUtils.DUMMY_PROXY_PORT;
import static com.suse.proxy.test.ProxyConfigUpdateTestUtils.DUMMY_ROOT_CA;
import static com.suse.proxy.test.ProxyConfigUpdateTestUtils.DUMMY_SERVER_ID;
import static com.suse.proxy.test.ProxyConfigUpdateTestUtils.DUMMY_TAG;
import static com.suse.proxy.test.ProxyConfigUpdateTestUtils.DUMMY_URL_PREFIX;
import static com.suse.proxy.test.ProxyConfigUpdateTestUtils.getDummyTag;
import static com.suse.proxy.test.ProxyConfigUpdateTestUtils.getDummyUrl;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import com.redhat.rhn.domain.server.Pillar;
import com.redhat.rhn.testing.MockObjectTestCase;

import com.suse.proxy.ProxyConfigUtils;
import com.suse.proxy.ProxyContainerImagesEnum;
import com.suse.proxy.model.ProxyConfig;
import com.suse.proxy.model.ProxyConfigImage;

import org.jmock.Expectations;
import org.jmock.imposters.ByteBuddyClassImposteriser;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

/**
 * Test class for {@link ProxyConfigUtils}
 */
@SuppressWarnings({"squid:S3599", "java:S1171"}) //jmock
public class ProxyConfigUtilsTest extends MockObjectTestCase {
    public static final String DUMMY_TAG_2 = "anotherTag";

    private Pillar mockPillar;

    @BeforeEach
    public void setUp() {
        setImposteriser(ByteBuddyClassImposteriser.INSTANCE);
        mockPillar = context.mock(Pillar.class);
    }

    /**
     * Tests the {@link ProxyConfigUtils#proxyConfigFromPillar(Pillar)} method when the pillar returns null.
     **/
    @Test
    public void testProxyConfigWhenNoPillar() {
        context.checking(new Expectations() {{
            oneOf(mockPillar).getPillar();
            will(returnValue(null));
        }});

        ProxyConfig proxyConfig = ProxyConfigUtils.proxyConfigFromPillar(mockPillar);

        assertNull(proxyConfig.getServerId());
        assertNull(proxyConfig.getProxyFqdn());
        assertNull(proxyConfig.getParentFqdn());
        assertNull(proxyConfig.getProxyPort());
        assertNull(proxyConfig.getMaxCache());
        assertNull(proxyConfig.getEmail());
        assertNull(proxyConfig.getRootCA());
        assertNull(proxyConfig.getProxyCert());
        assertNull(proxyConfig.getProxyKey());
        assertNull(proxyConfig.getIntermediateCAs());
        assertNull(proxyConfig.getHttpdImage());
        assertNull(proxyConfig.getSaltBrokerImage());
        assertNull(proxyConfig.getSquidImage());
        assertNull(proxyConfig.getSshImage());
        assertNull(proxyConfig.getTftpdImage());
    }

    /**
     * Tests the {@link ProxyConfigUtils#proxyConfigFromPillar(Pillar)} method when the pillar is returns no data.
     */
    @Test
    public void testProxyConfigWhenBlankPillar() {
        context.checking(new Expectations() {{
            allowing(mockPillar).getPillar();
            will(returnValue(new HashMap<>()));
        }});

        ProxyConfig proxyConfig = ProxyConfigUtils.proxyConfigFromPillar(mockPillar);

        assertNull(proxyConfig.getServerId());
        assertNull(proxyConfig.getProxyFqdn());
        assertNull(proxyConfig.getParentFqdn());
        assertNull(proxyConfig.getProxyPort());
        assertNull(proxyConfig.getMaxCache());
        assertNull(proxyConfig.getEmail());
        assertNull(proxyConfig.getRootCA());
        assertNull(proxyConfig.getProxyCert());
        assertNull(proxyConfig.getProxyKey());
        assertNull(proxyConfig.getIntermediateCAs());
        assertNull(proxyConfig.getHttpdImage());
        assertNull(proxyConfig.getSaltBrokerImage());
        assertNull(proxyConfig.getSquidImage());
        assertNull(proxyConfig.getSshImage());
        assertNull(proxyConfig.getTftpdImage());
    }

    /**
     * Tests the {@link ProxyConfigUtils#proxyConfigFromPillar(Pillar)} method when the pillar returns full data
     * with registries.
     */
    @Test
    public void testProxyConfigFromPillarWithRegistries() {
        final String expectedHttpdUrl = getDummyUrl(ProxyContainerImagesEnum.PROXY_HTTPD);
        final String expectedSaltBrokerUrl = getDummyUrl(ProxyContainerImagesEnum.PROXY_SALT_BROKER);
        final String expectedSquidUrl = getDummyUrl(ProxyContainerImagesEnum.PROXY_SQUID);
        final String expectedSshUrl = getDummyUrl(ProxyContainerImagesEnum.PROXY_SSH);
        final String expectedTftpdUrl = getDummyUrl(ProxyContainerImagesEnum.PROXY_TFTPD);

        final String expectedHttpdTag = getDummyTag(ProxyContainerImagesEnum.PROXY_HTTPD);
        final String expectedSaltBrokerTag = getDummyTag(ProxyContainerImagesEnum.PROXY_SALT_BROKER);
        final String expectedSquidTag = getDummyTag(ProxyContainerImagesEnum.PROXY_SQUID);
        final String expectedSshTag = getDummyTag(ProxyContainerImagesEnum.PROXY_SSH);
        final String expectedTftpdTag = getDummyTag(ProxyContainerImagesEnum.PROXY_TFTPD);

        context.checking(new Expectations() {{
            allowing(mockPillar).getPillar();
            will(returnValue(createPillarMapWithRegistryAdvancedMode()));
        }});

        //
        ProxyConfig proxyConfig = ProxyConfigUtils.proxyConfigFromPillar(mockPillar);

        assertEquals(DUMMY_SERVER_ID, proxyConfig.getServerId());
        assertEquals(DUMMY_PROXY_FQDN, proxyConfig.getProxyFqdn());
        assertEquals(DUMMY_PARENT_FQDN, proxyConfig.getParentFqdn());
        assertEquals(DUMMY_PROXY_PORT, proxyConfig.getProxyPort());
        assertEquals(DUMMY_MAX_CACHE, proxyConfig.getMaxCache());
        assertEquals(DUMMY_ADMIN_MAIL, proxyConfig.getEmail());
        assertEquals(DUMMY_ROOT_CA, proxyConfig.getRootCA());
        assertEquals(DUMMY_PROXY_CERT, proxyConfig.getProxyCert());
        assertEquals(DUMMY_PROXY_KEY, proxyConfig.getProxyKey());
        assertTrue(proxyConfig.getIntermediateCAs().contains(DUMMY_INTERMEDIATE_CA_1));
        assertTrue(proxyConfig.getIntermediateCAs().contains(DUMMY_INTERMEDIATE_CA_2));

        ProxyConfigImage httpdImage = proxyConfig.getHttpdImage();
        assertNotNull(httpdImage);
        assertEquals(expectedHttpdUrl, httpdImage.getUrl());
        assertEquals(expectedHttpdTag, httpdImage.getTag());

        ProxyConfigImage saltBrokerImage = proxyConfig.getSaltBrokerImage();
        assertNotNull(saltBrokerImage);
        assertEquals(expectedSaltBrokerUrl, saltBrokerImage.getUrl());
        assertEquals(expectedSaltBrokerTag, saltBrokerImage.getTag());

        ProxyConfigImage squidImage = proxyConfig.getSquidImage();
        assertNotNull(squidImage);
        assertEquals(expectedSquidUrl, squidImage.getUrl());
        assertEquals(expectedSquidTag, squidImage.getTag());

        ProxyConfigImage sshImage = proxyConfig.getSshImage();
        assertNotNull(sshImage);
        assertEquals(expectedSshUrl, sshImage.getUrl());
        assertEquals(expectedSshTag, sshImage.getTag());

        ProxyConfigImage tftpdImage = proxyConfig.getTftpdImage();
        assertNotNull(tftpdImage);
        assertEquals(expectedTftpdUrl, tftpdImage.getUrl());
        assertEquals(expectedTftpdTag, tftpdImage.getTag());
    }

    /**
     * Tests the {@link ProxyConfigUtils#getCommonTag} method when the images have a common tag.
     */
    @Test
    public void testGetCommonTagWhenCommonTag() {
        ProxyConfigImage image1 = new ProxyConfigImage(DUMMY_URL_PREFIX, DUMMY_TAG);
        ProxyConfigImage image2 = new ProxyConfigImage(DUMMY_URL_PREFIX, DUMMY_TAG);

        Optional<String> commonTag = ProxyConfigUtils.getCommonTag(image1, image2);

        assertTrue(commonTag.isPresent());
        assertEquals(DUMMY_TAG, commonTag.get());
    }

    /**
     * Tests the {@link ProxyConfigUtils#getCommonTag} method when the images have no common tag.
     */
    @Test
    public void testGetCommonTagWhenNoCommonTag() {
        ProxyConfigImage image1 = new ProxyConfigImage(DUMMY_URL_PREFIX, DUMMY_TAG);
        ProxyConfigImage image2 = new ProxyConfigImage(DUMMY_URL_PREFIX, DUMMY_TAG_2);

        Optional<String> commonTag = ProxyConfigUtils.getCommonTag(image1, image2);

        assertFalse(commonTag.isPresent());
    }

    /**
     * Tests the {@link ProxyConfigUtils#getCommonPrefix} method when the images have a common prefix.
     */
    @Test
    public void testGetCommonPrefixWhenCommonPrefixAndImageNamesMatch() {
        ProxyConfigImage httpdImage =
                new ProxyConfigImage(getDummyUrl(ProxyContainerImagesEnum.PROXY_HTTPD), DUMMY_TAG);
        ProxyConfigImage squidImage =
                new ProxyConfigImage(getDummyUrl(ProxyContainerImagesEnum.PROXY_SQUID), DUMMY_TAG_2);

        Optional<String> commonPrefix = ProxyConfigUtils.getCommonPrefix(httpdImage, squidImage);

        assertTrue(commonPrefix.isPresent());
        assertEquals(DUMMY_URL_PREFIX, commonPrefix.get());
    }

    /**
     * Tests the {@link ProxyConfigUtils#getCommonPrefix} method when the images have no common prefix.
     */
    @Test
    public void testGetCommonPrefixWhenNoCommonPrefixAndImageNamesMatch() {
        ProxyConfigImage httpdImage = new ProxyConfigImage(
                "http://not.suse.com/images/" + ProxyContainerImagesEnum.PROXY_HTTPD.getImageName(), DUMMY_TAG
        );
        ProxyConfigImage squidImage =
                new ProxyConfigImage(getDummyUrl(ProxyContainerImagesEnum.PROXY_SQUID), DUMMY_TAG_2);

        Optional<String> commonPrefix = ProxyConfigUtils.getCommonPrefix(httpdImage, squidImage);

        assertFalse(commonPrefix.isPresent());
    }

    /**
     * Tests the {@link ProxyConfigUtils#getCommonPrefix} method when the images have a common prefix but the images
     * names do not match the expected ones at {@link ProxyContainerImagesEnum}.
     */
    @Test
    public void testGetCommonPrefixWhenCommonPrefixAndImageNamesDoNotMatch() {
        ProxyConfigImage image =
                new ProxyConfigImage(DUMMY_URL_PREFIX + "not-fitting-image-name", DUMMY_TAG);
        ProxyConfigImage squidImage =
                new ProxyConfigImage(getDummyUrl(ProxyContainerImagesEnum.PROXY_SQUID), DUMMY_TAG);

        Optional<String> commonPrefix = ProxyConfigUtils.getCommonPrefix(image, squidImage);

        assertFalse(commonPrefix.isPresent());
    }

    @Test
    public void testGetCommonPrefixWhenInvalidScenarios() {
        ProxyConfigImage nullImage = null;
        ProxyConfigImage emptyImage = new ProxyConfigImage();
        ProxyConfigImage emptyUrlAndTagImage = new ProxyConfigImage("", "");
        ProxyConfigImage emptyUrlImage = new ProxyConfigImage("", DUMMY_TAG);
        ProxyConfigImage noImageUrlImage =
                new ProxyConfigImage("https://suse.com", "");
        ProxyConfigImage invalidUrlImage = new ProxyConfigImage("something@not.valid", DUMMY_TAG);

        Optional<String> nullImageCommonPrefix = ProxyConfigUtils.getCommonPrefix(nullImage);
        Optional<String> emptyImageCommonPrefix = ProxyConfigUtils.getCommonPrefix(emptyImage);
        Optional<String> emptyUrlAndTagImageCommonPrefix = ProxyConfigUtils.getCommonPrefix(emptyUrlAndTagImage);
        Optional<String> emptyUrlImageCommonPrefix = ProxyConfigUtils.getCommonPrefix(emptyUrlImage);
        Optional<String> noImageUrlImageCommonPrefix = ProxyConfigUtils.getCommonPrefix(noImageUrlImage);
        Optional<String> invalidUrlImageCommonPrefix = ProxyConfigUtils.getCommonPrefix(invalidUrlImage);

        assertFalse(nullImageCommonPrefix.isPresent());
        assertFalse(emptyImageCommonPrefix.isPresent());
        assertFalse(emptyUrlAndTagImageCommonPrefix.isPresent());
        assertFalse(emptyUrlImageCommonPrefix.isPresent());
        assertFalse(noImageUrlImageCommonPrefix.isPresent());
        assertFalse(invalidUrlImageCommonPrefix.isPresent());
    }


    /**
     * Tests the {@link ProxyConfigUtils#dataMapFromProxyConfig} method when the proxy config is empty.
     */
    @Test
    public void testDataMapFromProxyConfigWhenEmpty() {
        Map<String, Object> dataMap = ProxyConfigUtils.dataMapFromProxyConfig(new ProxyConfig());

        assertNull(dataMap.get(ProxyConfigUtils.SERVER_ID_FIELD));
        assertNull(dataMap.get(ProxyConfigUtils.PROXY_FQDN_FIELD));
        assertNull(dataMap.get(ProxyConfigUtils.PARENT_FQDN_FIELD));
        assertNull(dataMap.get(ProxyConfigUtils.PROXY_PORT_FIELD));
        assertNull(dataMap.get(ProxyConfigUtils.MAX_CACHE_FIELD));
        assertNull(dataMap.get(ProxyConfigUtils.EMAIL_FIELD));
    }

    /**
     * Tests the {@link ProxyConfigUtils#dataMapFromProxyConfig} method when the proxy config is null.
     */
    @Test
    public void testDataMapFromProxyConfigWhenNull() {
        Map<String, Object> dataMap = ProxyConfigUtils.dataMapFromProxyConfig(null);

        assertNull(dataMap.get(ProxyConfigUtils.SERVER_ID_FIELD));
        assertNull(dataMap.get(ProxyConfigUtils.PROXY_FQDN_FIELD));
        assertNull(dataMap.get(ProxyConfigUtils.PARENT_FQDN_FIELD));
        assertNull(dataMap.get(ProxyConfigUtils.PROXY_PORT_FIELD));
        assertNull(dataMap.get(ProxyConfigUtils.MAX_CACHE_FIELD));
        assertNull(dataMap.get(ProxyConfigUtils.EMAIL_FIELD));
        assertNull(dataMap.get(ProxyConfigUtils.SOURCE_MODE_FIELD));
        assertNull(dataMap.get(ProxyConfigUtils.REGISTRY_MODE));
        assertNull(dataMap.get(ProxyConfigUtils.REGISTRY_BASE_URL));
        assertNull(dataMap.get(ProxyConfigUtils.REGISTRY_BASE_TAG));
        assertNull(dataMap.get(ProxyContainerImagesEnum.PROXY_HTTPD.getUrlField()));
        assertNull(dataMap.get(ProxyContainerImagesEnum.PROXY_HTTPD.getTagField()));
        assertNull(dataMap.get(ProxyContainerImagesEnum.PROXY_SALT_BROKER.getUrlField()));
        assertNull(dataMap.get(ProxyContainerImagesEnum.PROXY_SALT_BROKER.getTagField()));
        assertNull(dataMap.get(ProxyContainerImagesEnum.PROXY_SQUID.getUrlField()));
        assertNull(dataMap.get(ProxyContainerImagesEnum.PROXY_SQUID.getTagField()));
        assertNull(dataMap.get(ProxyContainerImagesEnum.PROXY_SSH.getUrlField()));
        assertNull(dataMap.get(ProxyContainerImagesEnum.PROXY_SSH.getTagField()));
        assertNull(dataMap.get(ProxyContainerImagesEnum.PROXY_TFTPD.getUrlField()));
        assertNull(dataMap.get(ProxyContainerImagesEnum.PROXY_TFTPD.getTagField()));
    }

    /**
     * Tests the {@link ProxyConfigUtils#dataMapFromProxyConfig} method in a scenario of using RPM as the source mode.
     */
    @Test
    public void testDataMapFromProxyConfigWhenRpm() {
        ProxyConfig proxyConfig = new ProxyConfig();

        proxyConfig.setServerId(DUMMY_SERVER_ID);
        proxyConfig.setProxyFqdn(DUMMY_PROXY_FQDN);
        proxyConfig.setParentFqdn(DUMMY_PARENT_FQDN);
        proxyConfig.setProxyPort(DUMMY_PROXY_PORT);
        proxyConfig.setMaxCache(DUMMY_MAX_CACHE);
        proxyConfig.setEmail(DUMMY_ADMIN_MAIL);

        //
        Map<String, Object> dataMap = ProxyConfigUtils.dataMapFromProxyConfig(proxyConfig);

        //
        assertEquals(DUMMY_SERVER_ID, dataMap.get(ProxyConfigUtils.SERVER_ID_FIELD));
        assertEquals(DUMMY_PROXY_FQDN, dataMap.get(ProxyConfigUtils.PROXY_FQDN_FIELD));
        assertEquals(DUMMY_PARENT_FQDN, dataMap.get(ProxyConfigUtils.PARENT_FQDN_FIELD));
        assertEquals(DUMMY_PROXY_PORT, dataMap.get(ProxyConfigUtils.PROXY_PORT_FIELD));
        assertEquals(DUMMY_MAX_CACHE, dataMap.get(ProxyConfigUtils.MAX_CACHE_FIELD));
        assertEquals(DUMMY_ADMIN_MAIL, dataMap.get(ProxyConfigUtils.EMAIL_FIELD));

        assertEquals(ProxyConfigUtils.SOURCE_MODE_RPM, dataMap.get(ProxyConfigUtils.SOURCE_MODE_FIELD));
        assertNull(dataMap.get(ProxyConfigUtils.REGISTRY_MODE));
        assertNull(dataMap.get(ProxyConfigUtils.REGISTRY_BASE_URL));
        assertNull(dataMap.get(ProxyConfigUtils.REGISTRY_BASE_TAG));

        assertNull(dataMap.get(ProxyContainerImagesEnum.PROXY_HTTPD.getUrlField()));
        assertNull(dataMap.get(ProxyContainerImagesEnum.PROXY_HTTPD.getTagField()));
        assertNull(dataMap.get(ProxyContainerImagesEnum.PROXY_SALT_BROKER.getUrlField()));
        assertNull(dataMap.get(ProxyContainerImagesEnum.PROXY_SALT_BROKER.getTagField()));
        assertNull(dataMap.get(ProxyContainerImagesEnum.PROXY_SQUID.getUrlField()));
        assertNull(dataMap.get(ProxyContainerImagesEnum.PROXY_SQUID.getTagField()));
        assertNull(dataMap.get(ProxyContainerImagesEnum.PROXY_SSH.getUrlField()));
        assertNull(dataMap.get(ProxyContainerImagesEnum.PROXY_SSH.getTagField()));
        assertNull(dataMap.get(ProxyContainerImagesEnum.PROXY_TFTPD.getUrlField()));
        assertNull(dataMap.get(ProxyContainerImagesEnum.PROXY_TFTPD.getTagField()));
    }

    /**
     * Tests the {@link ProxyConfigUtils#dataMapFromProxyConfig} method in a scenario of using the registry in
     * in simple mode.
     */
    @Test
    public void testDataMapFromProxyConfigWhenRegistrySimpleMode() {
        final String expectedHttpdUrl = getDummyUrl(ProxyContainerImagesEnum.PROXY_HTTPD);
        final String expectedSaltBrokerUrl = getDummyUrl(ProxyContainerImagesEnum.PROXY_SALT_BROKER);
        final String expectedSquidUrl = getDummyUrl(ProxyContainerImagesEnum.PROXY_SQUID);
        final String expectedSshUrl = getDummyUrl(ProxyContainerImagesEnum.PROXY_SSH);
        final String expectedTftpdUrl = getDummyUrl(ProxyContainerImagesEnum.PROXY_TFTPD);

        //
        ProxyConfig proxyConfig = new ProxyConfig();

        proxyConfig.setServerId(DUMMY_SERVER_ID);
        proxyConfig.setProxyFqdn(DUMMY_PROXY_FQDN);
        proxyConfig.setParentFqdn(DUMMY_PARENT_FQDN);
        proxyConfig.setProxyPort(DUMMY_PROXY_PORT);
        proxyConfig.setMaxCache(DUMMY_MAX_CACHE);
        proxyConfig.setEmail(DUMMY_ADMIN_MAIL);
        proxyConfig.setHttpdImage(new ProxyConfigImage(expectedHttpdUrl, DUMMY_TAG));
        proxyConfig.setSaltBrokerImage(new ProxyConfigImage(expectedSaltBrokerUrl, DUMMY_TAG));
        proxyConfig.setSquidImage(new ProxyConfigImage(expectedSquidUrl, DUMMY_TAG));
        proxyConfig.setSshImage(new ProxyConfigImage(expectedSshUrl, DUMMY_TAG));
        proxyConfig.setTftpdImage(new ProxyConfigImage(expectedTftpdUrl, DUMMY_TAG));

        //
        Map<String, Object> dataMap = ProxyConfigUtils.dataMapFromProxyConfig(proxyConfig);

        //
        assertEquals(DUMMY_SERVER_ID, dataMap.get(ProxyConfigUtils.SERVER_ID_FIELD));
        assertEquals(DUMMY_PROXY_FQDN, dataMap.get(ProxyConfigUtils.PROXY_FQDN_FIELD));
        assertEquals(DUMMY_PARENT_FQDN, dataMap.get(ProxyConfigUtils.PARENT_FQDN_FIELD));
        assertEquals(DUMMY_PROXY_PORT, dataMap.get(ProxyConfigUtils.PROXY_PORT_FIELD));
        assertEquals(DUMMY_MAX_CACHE, dataMap.get(ProxyConfigUtils.MAX_CACHE_FIELD));
        assertEquals(DUMMY_ADMIN_MAIL, dataMap.get(ProxyConfigUtils.EMAIL_FIELD));

        assertEquals(ProxyConfigUtils.SOURCE_MODE_REGISTRY, dataMap.get(ProxyConfigUtils.SOURCE_MODE_FIELD));
        assertEquals(ProxyConfigUtils.REGISTRY_MODE_SIMPLE, dataMap.get(ProxyConfigUtils.REGISTRY_MODE));
        assertEquals(DUMMY_URL_PREFIX, dataMap.get(ProxyConfigUtils.REGISTRY_BASE_URL));
        assertEquals(DUMMY_TAG, dataMap.get(ProxyConfigUtils.REGISTRY_BASE_TAG));

        assertNull(dataMap.get(ProxyContainerImagesEnum.PROXY_HTTPD.getUrlField()));
        assertNull(dataMap.get(ProxyContainerImagesEnum.PROXY_HTTPD.getTagField()));
        assertNull(dataMap.get(ProxyContainerImagesEnum.PROXY_SALT_BROKER.getUrlField()));
        assertNull(dataMap.get(ProxyContainerImagesEnum.PROXY_SALT_BROKER.getTagField()));
        assertNull(dataMap.get(ProxyContainerImagesEnum.PROXY_SQUID.getUrlField()));
        assertNull(dataMap.get(ProxyContainerImagesEnum.PROXY_SQUID.getTagField()));
        assertNull(dataMap.get(ProxyContainerImagesEnum.PROXY_SSH.getUrlField()));
        assertNull(dataMap.get(ProxyContainerImagesEnum.PROXY_SSH.getTagField()));
        assertNull(dataMap.get(ProxyContainerImagesEnum.PROXY_TFTPD.getUrlField()));
        assertNull(dataMap.get(ProxyContainerImagesEnum.PROXY_TFTPD.getTagField()));
    }

    /**
     * Tests the {@link ProxyConfigUtils#dataMapFromProxyConfig} method in a scenario of using the registry in
     * advanced mode.
     */
    @Test
    public void testDataMapFromProxyConfigWhenRegistryAdvancedMode() {
        final String expectedHttpdUrl = getDummyUrl(ProxyContainerImagesEnum.PROXY_HTTPD);
        final String expectedSaltBrokerUrl = getDummyUrl(ProxyContainerImagesEnum.PROXY_SALT_BROKER);
        final String expectedSquidUrl = getDummyUrl(ProxyContainerImagesEnum.PROXY_SQUID);
        final String expectedSshUrl = getDummyUrl(ProxyContainerImagesEnum.PROXY_SSH);
        final String expectedTftpdUrl = getDummyUrl(ProxyContainerImagesEnum.PROXY_TFTPD);

        final String expectedHttpdTag = getDummyTag(ProxyContainerImagesEnum.PROXY_HTTPD);
        final String expectedSaltBrokerTag = getDummyTag(ProxyContainerImagesEnum.PROXY_SALT_BROKER);
        final String expectedSquidTag = getDummyTag(ProxyContainerImagesEnum.PROXY_SQUID);
        final String expectedSshTag = getDummyTag(ProxyContainerImagesEnum.PROXY_SSH);
        final String expectedTftpdTag = getDummyTag(ProxyContainerImagesEnum.PROXY_TFTPD);

        //
        ProxyConfig proxyConfig = new ProxyConfig();

        proxyConfig.setServerId(DUMMY_SERVER_ID);
        proxyConfig.setProxyFqdn(DUMMY_PROXY_FQDN);
        proxyConfig.setParentFqdn(DUMMY_PARENT_FQDN);
        proxyConfig.setProxyPort(DUMMY_PROXY_PORT);
        proxyConfig.setMaxCache(DUMMY_MAX_CACHE);
        proxyConfig.setEmail(DUMMY_ADMIN_MAIL);
        proxyConfig.setHttpdImage(new ProxyConfigImage(expectedHttpdUrl, expectedHttpdTag));
        proxyConfig.setSaltBrokerImage(new ProxyConfigImage(expectedSaltBrokerUrl, expectedSaltBrokerTag));
        proxyConfig.setSquidImage(new ProxyConfigImage(expectedSquidUrl, expectedSquidTag));
        proxyConfig.setSshImage(new ProxyConfigImage(expectedSshUrl, expectedSshTag));
        proxyConfig.setTftpdImage(new ProxyConfigImage(expectedTftpdUrl, expectedTftpdTag));

        //
        Map<String, Object> dataMap = ProxyConfigUtils.dataMapFromProxyConfig(proxyConfig);

        //
        assertEquals(DUMMY_SERVER_ID, dataMap.get(ProxyConfigUtils.SERVER_ID_FIELD));
        assertEquals(DUMMY_PROXY_FQDN, dataMap.get(ProxyConfigUtils.PROXY_FQDN_FIELD));
        assertEquals(DUMMY_PARENT_FQDN, dataMap.get(ProxyConfigUtils.PARENT_FQDN_FIELD));
        assertEquals(DUMMY_PROXY_PORT, dataMap.get(ProxyConfigUtils.PROXY_PORT_FIELD));
        assertEquals(DUMMY_MAX_CACHE, dataMap.get(ProxyConfigUtils.MAX_CACHE_FIELD));
        assertEquals(DUMMY_ADMIN_MAIL, dataMap.get(ProxyConfigUtils.EMAIL_FIELD));

        assertEquals(ProxyConfigUtils.SOURCE_MODE_REGISTRY, dataMap.get(ProxyConfigUtils.SOURCE_MODE_FIELD));
        assertEquals(ProxyConfigUtils.REGISTRY_MODE_ADVANCED, dataMap.get(ProxyConfigUtils.REGISTRY_MODE));
        assertNull(dataMap.get(ProxyConfigUtils.REGISTRY_BASE_URL));
        assertNull(dataMap.get(ProxyConfigUtils.REGISTRY_BASE_TAG));

        assertEquals(expectedHttpdUrl, dataMap.get(ProxyContainerImagesEnum.PROXY_HTTPD.getUrlField()));
        assertEquals(expectedHttpdTag, dataMap.get(ProxyContainerImagesEnum.PROXY_HTTPD.getTagField()));
        assertEquals(expectedSaltBrokerUrl, dataMap.get(ProxyContainerImagesEnum.PROXY_SALT_BROKER.getUrlField()));
        assertEquals(expectedSaltBrokerTag, dataMap.get(ProxyContainerImagesEnum.PROXY_SALT_BROKER.getTagField()));
        assertEquals(expectedSquidUrl, dataMap.get(ProxyContainerImagesEnum.PROXY_SQUID.getUrlField()));
        assertEquals(expectedSquidTag, dataMap.get(ProxyContainerImagesEnum.PROXY_SQUID.getTagField()));
        assertEquals(expectedSshUrl, dataMap.get(ProxyContainerImagesEnum.PROXY_SSH.getUrlField()));
        assertEquals(expectedSshTag, dataMap.get(ProxyContainerImagesEnum.PROXY_SSH.getTagField()));
        assertEquals(expectedTftpdUrl, dataMap.get(ProxyContainerImagesEnum.PROXY_TFTPD.getUrlField()));
        assertEquals(expectedTftpdTag, dataMap.get(ProxyContainerImagesEnum.PROXY_TFTPD.getTagField()));
    }

    /**
     * Creates a dummy pillar map with RPM data (ie without registries).
     *
     * @return the dummy pillar map
     */
    private Map<String, Object> createPillarMapWithRpm() {
        Map<String, Object> pillarMap = new HashMap<>();
        pillarMap.put(SERVER_ID_FIELD, DUMMY_SERVER_ID);
        pillarMap.put(PROXY_FQDN_FIELD, DUMMY_PROXY_FQDN);
        pillarMap.put(PARENT_FQDN_FIELD, DUMMY_PARENT_FQDN);
        pillarMap.put(PROXY_PORT_FIELD, DUMMY_PROXY_PORT);
        pillarMap.put(MAX_CACHE_FIELD, DUMMY_MAX_CACHE);
        pillarMap.put(EMAIL_FIELD, DUMMY_ADMIN_MAIL);
        pillarMap.put(ROOT_CA_FIELD, DUMMY_ROOT_CA);
        pillarMap.put(PROXY_CERT_FIELD, DUMMY_PROXY_CERT);
        pillarMap.put(PROXY_KEY_FIELD, DUMMY_PROXY_KEY);
        pillarMap.put(INTERMEDIATE_CAS_FIELD, List.of(DUMMY_INTERMEDIATE_CA_1, DUMMY_INTERMEDIATE_CA_2));
        return pillarMap;
    }

    /**
     * Creates a dummy pillar map with registry data in advanced mode.
     *
     * @return the dummy pillar map
     */
    private Map<String, Object> createPillarMapWithRegistryAdvancedMode() {
        Map<String, Object> pillarMap = createPillarMapWithRpm();

        Map<String, Object> registriesList = new HashMap<>();
        for (ProxyContainerImagesEnum image : ProxyContainerImagesEnum.values()) {
            Map<String, Object> imageMap = new HashMap<>();
            imageMap.put(PILLAR_REGISTRY_URL_ENTRY, getDummyUrl(image));
            imageMap.put(PILLAR_REGISTRY_TAG_ENTRY, getDummyTag(image));
            registriesList.put(image.getImageName(), imageMap);
        }
        pillarMap.put(PILLAR_REGISTRY_ENTRY, registriesList);
        return pillarMap;
    }

    /**
     * Creates a dummy pillar map with registry data in simple mode.
     *
     * @return the dummy pillar map
     */
    private Map<String, Object> createPillarMapWithRegistrySimpleMode() {
        Map<String, Object> pillarMap = createPillarMapWithRpm();

        Map<String, Object> registriesList = new HashMap<>();
        for (ProxyContainerImagesEnum image : ProxyContainerImagesEnum.values()) {
            Map<String, Object> imageMap = new HashMap<>();
            imageMap.put(PILLAR_REGISTRY_URL_ENTRY, DUMMY_URL_PREFIX + image.getImageName());
            imageMap.put(PILLAR_REGISTRY_TAG_ENTRY, DUMMY_TAG);
            registriesList.put(image.getImageName(), imageMap);
        }
        pillarMap.put(PILLAR_REGISTRY_ENTRY, registriesList);
        return pillarMap;
    }
}

