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

package com.suse.proxy.update;

import static com.suse.proxy.ProxyConfigUtils.REGISTRY_MODE_SIMPLE;
import static com.suse.proxy.ProxyConfigUtils.SOURCE_MODE_REGISTRY;
import static com.suse.proxy.update.ProxyConfigUpdateTestUtils.DUMMY_ADMIN_MAIL;
import static com.suse.proxy.update.ProxyConfigUpdateTestUtils.DUMMY_INTERMEDIATE_CA_1;
import static com.suse.proxy.update.ProxyConfigUpdateTestUtils.DUMMY_INTERMEDIATE_CA_2;
import static com.suse.proxy.update.ProxyConfigUpdateTestUtils.DUMMY_MAX_CACHE;
import static com.suse.proxy.update.ProxyConfigUpdateTestUtils.DUMMY_PARENT_FQDN;
import static com.suse.proxy.update.ProxyConfigUpdateTestUtils.DUMMY_PROXY_CERT;
import static com.suse.proxy.update.ProxyConfigUpdateTestUtils.DUMMY_PROXY_FQDN;
import static com.suse.proxy.update.ProxyConfigUpdateTestUtils.DUMMY_PROXY_KEY;
import static com.suse.proxy.update.ProxyConfigUpdateTestUtils.DUMMY_PROXY_PORT;
import static com.suse.proxy.update.ProxyConfigUpdateTestUtils.DUMMY_ROOT_CA;
import static com.suse.proxy.update.ProxyConfigUpdateTestUtils.DUMMY_SERVER_ID;
import static com.suse.proxy.update.ProxyConfigUpdateTestUtils.DUMMY_TAG;
import static com.suse.proxy.update.ProxyConfigUpdateTestUtils.DUMMY_URL_PREFIX;
import static com.suse.proxy.update.ProxyConfigUpdateTestUtils.assertExpectedErrors;
import static com.suse.proxy.update.ProxyConfigUpdateTestUtils.getDummyTag;
import static com.suse.proxy.update.ProxyConfigUpdateTestUtils.getDummyUrl;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import com.redhat.rhn.domain.server.MinionServer;
import com.redhat.rhn.domain.server.Pillar;
import com.redhat.rhn.domain.server.Server;
import com.redhat.rhn.domain.server.ServerConstants;
import com.redhat.rhn.domain.server.ServerFactory;
import com.redhat.rhn.domain.server.test.MinionServerFactoryTest;
import com.redhat.rhn.domain.server.test.ServerFactoryTest;
import com.redhat.rhn.manager.entitlement.EntitlementManager;
import com.redhat.rhn.manager.system.entitling.SystemEntitlementManager;
import com.redhat.rhn.manager.system.entitling.SystemEntitler;
import com.redhat.rhn.manager.system.entitling.SystemUnentitler;
import com.redhat.rhn.testing.BaseTestCaseWithUser;
import com.redhat.rhn.testing.TestUtils;

import com.suse.manager.webui.services.iface.SaltApi;
import com.suse.manager.webui.services.test.TestSaltApi;
import com.suse.manager.webui.utils.gson.ProxyConfigUpdateJson;
import com.suse.proxy.ProxyConfigUtils;
import com.suse.proxy.ProxyContainerImagesEnum;
import com.suse.proxy.RegistryUrl;
import com.suse.proxy.model.ProxyConfig;
import com.suse.utils.Json;

import org.jmock.imposters.ByteBuddyClassImposteriser;
import org.jmock.junit5.JUnit5Mockery;
import org.jmock.lib.concurrent.Synchroniser;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.RegisterExtension;

import java.util.HashMap;
import java.util.List;

/**
 * Tests for the {@link ProxyConfigUpdateAcquisitor} class
 */
public class ProxyConfigUpdateAcquisitorTest extends BaseTestCaseWithUser {

    public static final String DUMMY_REPLACE_ROOT_CA = "replace_rootCA";
    public static final String DUMMY_REPLACE_INTERMEDIATE_CA_1 = "replace_intermediateCA1";
    public static final String DUMMY_REPLACE_INTERMEDIATE_CA_2 = "replace_intermediateCA2";
    public static final String DUMMY_REPLACE_PROXY_CERT = "replace_proxyCert";
    public static final String DUMMY_REPLACE_PROXY_KEY = "replace_proxyKey";

    private final SaltApi saltApi = new TestSaltApi();
    private final SystemEntitlementManager systemEntitlementManager = new SystemEntitlementManager(
            new SystemUnentitler(saltApi),
            new SystemEntitler(saltApi)
    );

    @SuppressWarnings({"java:S1171", "java:S3599"})
    @RegisterExtension
    protected final JUnit5Mockery context = new JUnit5Mockery() {{
        setThreadingPolicy(new Synchroniser());
    }};

    @Override
    @BeforeEach
    public void setUp() throws Exception {
        super.setUp();
        context.setImposteriser(ByteBuddyClassImposteriser.INSTANCE);
    }

    /**
     * Test a scenario where provided {@link ProxyConfigUpdateJson} request is empty
     */
    @Test
    public void testBlankRequest() {
        ProxyConfigUpdateJson request = Json.GSON.fromJson("{}", ProxyConfigUpdateJson.class);
        ProxyConfigUpdateContext proxyConfigUpdateContext = new ProxyConfigUpdateContext(request, null, null);

        // execution
        new ProxyConfigUpdateAcquisitor().handle(proxyConfigUpdateContext);

        //assertions
        assertFalse(proxyConfigUpdateContext.getErrorReport().hasErrors());
        // acquireProxyMinion
        assertNull(proxyConfigUpdateContext.getProxyMinion());
        assertNull(proxyConfigUpdateContext.getProxyFqdn());
        assertNull(proxyConfigUpdateContext.getProxyConfig());
        // acquireCertificates
        assertNull(proxyConfigUpdateContext.getProxyCert());
        assertNull(proxyConfigUpdateContext.getIntermediateCAs());
        assertNull(proxyConfigUpdateContext.getProxyCert());
        assertNull(proxyConfigUpdateContext.getProxyKey());
        // acquireParentServer
        assertNull(proxyConfigUpdateContext.getParentServer());
        // buildRegistryUrls
        assertTrue(proxyConfigUpdateContext.getRegistryUrls().isEmpty());


    }

    /**
     * Test scenario with following conditions:
     * - {@link ProxyConfigUpdateJson} request is provided with all fields filled;
     * - No data is acquired.
     */
    @Test
    public void testFullRequestNoAcquisitions() {
        ProxyConfigUpdateJson request = new ProxyConfigUpdateJsonBuilder()
                .serverId(DUMMY_SERVER_ID)
                .parentFqdn(DUMMY_ADMIN_MAIL)
                .proxyPort(DUMMY_PROXY_PORT)
                .maxCache(DUMMY_MAX_CACHE)
                .email(DUMMY_ADMIN_MAIL)
                .replaceCerts(
                        DUMMY_ROOT_CA,
                        List.of(DUMMY_INTERMEDIATE_CA_1, DUMMY_INTERMEDIATE_CA_2),
                        DUMMY_PROXY_CERT,
                        DUMMY_PROXY_KEY
                )
                .sourceRegistryAdvanced(
                        getDummyUrl(ProxyContainerImagesEnum.PROXY_HTTPD),
                        getDummyTag(ProxyContainerImagesEnum.PROXY_HTTPD),
                        getDummyUrl(ProxyContainerImagesEnum.PROXY_SALT_BROKER),
                        getDummyTag(ProxyContainerImagesEnum.PROXY_SALT_BROKER),
                        getDummyUrl(ProxyContainerImagesEnum.PROXY_SQUID),
                        getDummyTag(ProxyContainerImagesEnum.PROXY_SQUID),
                        getDummyUrl(ProxyContainerImagesEnum.PROXY_SSH),
                        getDummyTag(ProxyContainerImagesEnum.PROXY_SSH),
                        getDummyUrl(ProxyContainerImagesEnum.PROXY_TFTPD),
                        getDummyTag(ProxyContainerImagesEnum.PROXY_TFTPD)
                )
                .build();
        ProxyConfigUpdateContext proxyConfigUpdateContext = new ProxyConfigUpdateContext(request, null, null);

        // execution
        new ProxyConfigUpdateAcquisitor().handle(proxyConfigUpdateContext);

        //assertions
        assertFalse(proxyConfigUpdateContext.getErrorReport().hasErrors());
        // acquireProxyMinion
        assertNull(proxyConfigUpdateContext.getProxyMinion());
        assertNull(proxyConfigUpdateContext.getProxyFqdn());
        assertNull(proxyConfigUpdateContext.getProxyConfig());
        // acquireCertificates
        assertEquals(DUMMY_PROXY_CERT, proxyConfigUpdateContext.getProxyCert());
        assertEquals(DUMMY_ROOT_CA, proxyConfigUpdateContext.getRootCA());
        assertEquals(
                List.of(DUMMY_INTERMEDIATE_CA_1, DUMMY_INTERMEDIATE_CA_2),
                proxyConfigUpdateContext.getIntermediateCAs()
        );
        assertEquals(DUMMY_PROXY_CERT, proxyConfigUpdateContext.getProxyCert());
        // acquireParentServer
        assertNull(proxyConfigUpdateContext.getParentServer());
        // buildRegistryUrls
        assertEquals(ProxyContainerImagesEnum.values().length, proxyConfigUpdateContext.getRegistryUrls().size());
        for (ProxyContainerImagesEnum image : ProxyContainerImagesEnum.values()) {
            assertTrue(proxyConfigUpdateContext.getRegistryUrls().containsKey(image));
            RegistryUrl registryUrl = proxyConfigUpdateContext.getRegistryUrls().get(image);
            assertEquals(DUMMY_TAG + "_" + image.getImageName(), registryUrl.getTag());
            assertEquals(DUMMY_URL_PREFIX + image.getImageName(), registryUrl.getUrl());
        }
    }

    /**
     * Test acquireProxyMinion in a scenario with following conditions:
     * - {@link ProxyConfigUpdateJson} request provides required data;
     * - All minion data is acquirable.
     */
    @Test
    public void testAcquireProxyMinionWhenSuccessfulAcquisitions() {
        // minion setup
        MinionServer minion = MinionServerFactoryTest.createTestMinionServer(user);
        minion.setServerArch(ServerFactory.lookupServerArchByLabel("x86_64-redhat-linux"));
        minion.addFqdn(DUMMY_PROXY_FQDN);
        systemEntitlementManager.addEntitlementToServer(minion, EntitlementManager.PROXY);
        Pillar pillar = new Pillar(ProxyConfigUtils.PROXY_PILLAR_CATEGORY, new HashMap<>(), minion);
        pillar.add(ProxyConfigUtils.PROXY_FQDN_FIELD, DUMMY_PROXY_FQDN);
        pillar.add(ProxyConfigUtils.PARENT_FQDN_FIELD, DUMMY_PARENT_FQDN);
        minion.addPillar(pillar);
        TestUtils.saveAndFlush(minion);

        ProxyConfigUpdateJson request = new ProxyConfigUpdateJsonBuilder().serverId(minion.getId()).build();
        ProxyConfigUpdateContext proxyConfigUpdateContext = new ProxyConfigUpdateContext(request, null, null);

        // execution
        new ProxyConfigUpdateAcquisitor().handle(proxyConfigUpdateContext);

        //assertions
        assertFalse(proxyConfigUpdateContext.getErrorReport().hasErrors());

        // acquireProxyMinion
        assertNotNull(proxyConfigUpdateContext.getProxyMinion());
        assertEquals(DUMMY_PROXY_FQDN, proxyConfigUpdateContext.getProxyFqdn());
        ProxyConfig proxyConfig = proxyConfigUpdateContext.getProxyConfig();
        assertNotNull(proxyConfig);
        assertEquals(DUMMY_PROXY_FQDN, proxyConfig.getProxyFqdn());
        assertEquals(DUMMY_PARENT_FQDN, proxyConfig.getParentFqdn());
    }

    /**
     * Test acquireCertificates in a scenario with following conditions:
     * - {@link ProxyConfigUpdateJson} request provides required data, indicating we want to keep existing certificates;
     * - Minion has certificates saved in its pillar.
     * Expects the {@link ProxyConfigUpdateContext} to keep the existing ones in the pillars, discarding certificates
     * data provided in {@link ProxyConfigUpdateJson}.
     */
    @Test
    public void testAcquireCertificatesKeepWhenSuccessfulRetrieveCertificates() {
        // minion setup
        MinionServer minion = MinionServerFactoryTest.createTestMinionServer(user);
        systemEntitlementManager.addEntitlementToServer(minion, EntitlementManager.PROXY);
        Pillar pillar = new Pillar(ProxyConfigUtils.PROXY_PILLAR_CATEGORY, new HashMap<>(), minion);
        pillar.add(ProxyConfigUtils.ROOT_CA_FIELD, DUMMY_ROOT_CA);
        pillar.add(ProxyConfigUtils.INTERMEDIATE_CAS_FIELD, List.of(DUMMY_INTERMEDIATE_CA_1, DUMMY_INTERMEDIATE_CA_2));
        pillar.add(ProxyConfigUtils.PROXY_CERT_FIELD, DUMMY_PROXY_CERT);
        pillar.add(ProxyConfigUtils.PROXY_KEY_FIELD, DUMMY_PROXY_KEY);
        minion.addPillar(pillar);
        TestUtils.saveAndFlush(minion);

        ProxyConfigUpdateJson request = new ProxyConfigUpdateJsonBuilder()
                .serverId(minion.getId())
                .keepCerts(
                        DUMMY_REPLACE_ROOT_CA,
                        List.of(DUMMY_REPLACE_INTERMEDIATE_CA_1, DUMMY_REPLACE_INTERMEDIATE_CA_2),
                        DUMMY_REPLACE_PROXY_CERT,
                        DUMMY_REPLACE_PROXY_KEY
                ).build();
        ProxyConfigUpdateContext proxyConfigUpdateContext = new ProxyConfigUpdateContext(request, null, null);

        // execution
        new ProxyConfigUpdateAcquisitor().handle(proxyConfigUpdateContext);

        //assertions
        assertFalse(proxyConfigUpdateContext.getErrorReport().hasErrors());

        // acquireCertificates
        assertEquals(DUMMY_ROOT_CA, proxyConfigUpdateContext.getRootCA());
        assertEquals(
                List.of(DUMMY_INTERMEDIATE_CA_1, DUMMY_INTERMEDIATE_CA_2),
                proxyConfigUpdateContext.getIntermediateCAs()
        );
        assertEquals(DUMMY_PROXY_CERT, proxyConfigUpdateContext.getProxyCert());
        assertEquals(DUMMY_PROXY_KEY, proxyConfigUpdateContext.getProxyKey());
    }

    /**
     * Test acquireCertificates in a scenario with following conditions:
     * - {@link ProxyConfigUpdateJson} request provides required data, indicating we want to keep existing certificates;
     * - Minion does NOT have proxy pillars in DB.
     * Expects the {@link ProxyConfigUpdateContext} certificates data to be null.
     */
    @Test
    public void testAcquireCertificatesKeepWhenFailRetrieveCertificates() {
        // minion setup
        MinionServer minion = MinionServerFactoryTest.createTestMinionServer(user);
        systemEntitlementManager.addEntitlementToServer(minion, EntitlementManager.PROXY);
        TestUtils.saveAndFlush(minion);

        ProxyConfigUpdateJson request = new ProxyConfigUpdateJsonBuilder()
                .serverId(minion.getId())
                .keepCerts(
                        DUMMY_REPLACE_ROOT_CA,
                        List.of(DUMMY_REPLACE_INTERMEDIATE_CA_1, DUMMY_REPLACE_INTERMEDIATE_CA_2),
                        DUMMY_REPLACE_PROXY_CERT,
                        DUMMY_REPLACE_PROXY_KEY
                ).build();
        ProxyConfigUpdateContext proxyConfigUpdateContext = new ProxyConfigUpdateContext(request, null, null);

        // execution
        new ProxyConfigUpdateAcquisitor().handle(proxyConfigUpdateContext);

        //assertions
        assertFalse(proxyConfigUpdateContext.getErrorReport().hasErrors());

        // acquireCertificates
        assertNull(proxyConfigUpdateContext.getRootCA());
        assertNull(proxyConfigUpdateContext.getIntermediateCAs());
        assertNull(proxyConfigUpdateContext.getProxyCert());
        assertNull(proxyConfigUpdateContext.getProxyKey());
    }

    /**
     * Test acquireCertificates in a scenario with following conditions:
     * - {@link ProxyConfigUpdateJson} request provides required data, indicating we want to replace certificates;
     * - Minion has certificates saved in its pillar.
     * Expects the {@link ProxyConfigUpdateContext} to replace certificates data.
     */
    @Test
    public void testAcquireCertificatesReplaceWhenSuccessfulRetrieveCertificates() {
        // minion setup
        MinionServer minion = MinionServerFactoryTest.createTestMinionServer(user);
        systemEntitlementManager.addEntitlementToServer(minion, EntitlementManager.PROXY);
        Pillar pillar = new Pillar(ProxyConfigUtils.PROXY_PILLAR_CATEGORY, new HashMap<>(), minion);
        pillar.add(ProxyConfigUtils.ROOT_CA_FIELD, DUMMY_ROOT_CA);
        pillar.add(ProxyConfigUtils.INTERMEDIATE_CAS_FIELD, List.of(DUMMY_INTERMEDIATE_CA_1, DUMMY_INTERMEDIATE_CA_2));
        pillar.add(ProxyConfigUtils.PROXY_CERT_FIELD, DUMMY_PROXY_CERT);
        pillar.add(ProxyConfigUtils.PROXY_KEY_FIELD, DUMMY_PROXY_KEY);
        minion.addPillar(pillar);
        TestUtils.saveAndFlush(minion);

        ProxyConfigUpdateJson request = new ProxyConfigUpdateJsonBuilder()
                .serverId(minion.getId())
                .replaceCerts(
                        DUMMY_REPLACE_ROOT_CA,
                        List.of(DUMMY_REPLACE_INTERMEDIATE_CA_1, DUMMY_REPLACE_INTERMEDIATE_CA_2),
                        DUMMY_REPLACE_PROXY_CERT,
                        DUMMY_REPLACE_PROXY_KEY
                ).build();
        ProxyConfigUpdateContext proxyConfigUpdateContext = new ProxyConfigUpdateContext(request, null, null);

        // execution
        new ProxyConfigUpdateAcquisitor().handle(proxyConfigUpdateContext);

        //assertions
        assertFalse(proxyConfigUpdateContext.getErrorReport().hasErrors());

        // acquireCertificates
        assertEquals(DUMMY_REPLACE_ROOT_CA, proxyConfigUpdateContext.getRootCA());
        assertEquals(
                List.of(DUMMY_REPLACE_INTERMEDIATE_CA_1, DUMMY_REPLACE_INTERMEDIATE_CA_2),
                proxyConfigUpdateContext.getIntermediateCAs()
        );
        assertEquals(DUMMY_REPLACE_PROXY_CERT, proxyConfigUpdateContext.getProxyCert());
        assertEquals(DUMMY_REPLACE_PROXY_KEY, proxyConfigUpdateContext.getProxyKey());
    }


    /**
     * Test acquireParentServer in a scenario with following conditions:
     * - {@link ProxyConfigUpdateJson} request provides required data (ie. parent fqdn);
     * - Parent server is reachable using the provided fqdn.
     */
    @Test
    public void testAcquireParentServerWhenSuccessfulRetrieveServer() {
        // parent server setup
        Server parent = ServerFactoryTest.createTestServer(user, true,
                ServerConstants.getServerGroupTypeSaltEntitled(),
                ServerFactoryTest.TYPE_SERVER_MGR);
        parent.addFqdn(DUMMY_PARENT_FQDN);

        ProxyConfigUpdateJson request = new ProxyConfigUpdateJsonBuilder().parentFqdn(DUMMY_PARENT_FQDN).build();
        ProxyConfigUpdateContext proxyConfigUpdateContext = new ProxyConfigUpdateContext(request, null, null);

        // execution
        new ProxyConfigUpdateAcquisitor().handle(proxyConfigUpdateContext);

        //assertions
        assertFalse(proxyConfigUpdateContext.getErrorReport().hasErrors());

        // acquireParentServer
        assertEquals(parent, proxyConfigUpdateContext.getParentServer());
    }

    /**
     * Test acquireParentServer in a scenario with following conditions:
     * - {@link ProxyConfigUpdateJson} request provides required data (ie. parent fqdn);
     * - Parent server is not found using the provided fqdn.
     */
    @Test
    public void testAcquireParentServerWhenFailRetrieveServer() {
        // parent server setup
        Server parent = ServerFactoryTest.createTestServer(user, true,
                ServerConstants.getServerGroupTypeSaltEntitled(),
                ServerFactoryTest.TYPE_SERVER_MGR);
        parent.addFqdn(DUMMY_PARENT_FQDN);

        ProxyConfigUpdateJson request = new ProxyConfigUpdateJsonBuilder().parentFqdn("not.existent.com").build();
        ProxyConfigUpdateContext proxyConfigUpdateContext = new ProxyConfigUpdateContext(request, null, null);

        // execution
        new ProxyConfigUpdateAcquisitor().handle(proxyConfigUpdateContext);

        //assertions
        assertFalse(proxyConfigUpdateContext.getErrorReport().hasErrors());

        // acquireParentServer
        assertNull(proxyConfigUpdateContext.getParentServer());
    }

    /**
     * Test buildRegistryUrls in a scenario with following conditions:
     * - {@link ProxyConfigUpdateJson} request provides required data, indicating we want to use registries,
     * providing a common registry and tag for all images.
     */
    @Test
    public void testBuildRegistryUrlsWhenSimpleRegistry() {
        ProxyConfigUpdateJson request = new ProxyConfigUpdateJsonBuilder()
                .sourceMode(SOURCE_MODE_REGISTRY)
                .registryMode(REGISTRY_MODE_SIMPLE)
                .registryBaseURL("http://suse.com/common/image")
                .registryBaseTag(DUMMY_TAG)
                .build();

        ProxyConfigUpdateContext proxyConfigUpdateContext = new ProxyConfigUpdateContext(request, null, null);

        // execution
        new ProxyConfigUpdateAcquisitor().handle(proxyConfigUpdateContext);

        //assertions
        assertFalse(proxyConfigUpdateContext.getErrorReport().hasErrors());

        // buildRegistryUrls
        assertEquals(ProxyContainerImagesEnum.values().length, proxyConfigUpdateContext.getRegistryUrls().size());
        for (ProxyContainerImagesEnum image : ProxyContainerImagesEnum.values()) {
            assertTrue(proxyConfigUpdateContext.getRegistryUrls().containsKey(image));
            RegistryUrl registryUrl = proxyConfigUpdateContext.getRegistryUrls().get(image);
            assertEquals(DUMMY_TAG, registryUrl.getTag());
            assertEquals("http://suse.com/common/image/" + image.getImageName(), registryUrl.getUrl());
        }
    }

    /**
     * Test buildRegistryUrls in a scenario with following conditions:
     * - {@link ProxyConfigUpdateJson} request provides required data, indicating we want to use registries,
     * providing a common registry and tag for all images.
     */
    @Test
    public void testFailBuildRegistryUrlsWhenSimpleRegistryWithBadUrl() {
        final String[] expectedErrorMessages = {"Invalid Registry URL"};
        ProxyConfigUpdateJson request = new ProxyConfigUpdateJsonBuilder()
                .sourceMode(SOURCE_MODE_REGISTRY)
                .registryMode(REGISTRY_MODE_SIMPLE)
                .registryBaseURL("http://invalid-url^") // This should cause URISyntaxException
                .registryBaseTag(DUMMY_TAG)
                .build();

        ProxyConfigUpdateContext proxyConfigUpdateContext = new ProxyConfigUpdateContext(request, null, null);

        //
        new ProxyConfigUpdateAcquisitor().handle(proxyConfigUpdateContext);
        assertExpectedErrors(expectedErrorMessages, proxyConfigUpdateContext);
    }

    /**
     * Test buildRegistryUrls in a scenario with following conditions:
     * - {@link ProxyConfigUpdateJson} request provides required data, indicating we want to use registries and
     * specifying for each one its url and tag.
     */
    @Test
    public void testBuildRegistryUrlsWhenAdvancedRegistry() {
        ProxyConfigUpdateJson request = getFullRequest().build();

        ProxyConfigUpdateContext proxyConfigUpdateContext = new ProxyConfigUpdateContext(request, null, null);

        // execution
        new ProxyConfigUpdateAcquisitor().handle(proxyConfigUpdateContext);

        //assertions
        assertFalse(proxyConfigUpdateContext.getErrorReport().hasErrors());

        // buildRegistryUrls
        assertEquals(ProxyContainerImagesEnum.values().length, proxyConfigUpdateContext.getRegistryUrls().size());
        for (ProxyContainerImagesEnum image : ProxyContainerImagesEnum.values()) {
            assertTrue(proxyConfigUpdateContext.getRegistryUrls().containsKey(image));
            RegistryUrl registryUrl = proxyConfigUpdateContext.getRegistryUrls().get(image);
            assertEquals(image.getImageName() + "-latest", registryUrl.getTag());
            assertEquals("http://suse.com/" + image.getImageName(), registryUrl.getUrl());
        }
    }


    /**
     * Creates a request with all fields filled.
     * Considers replacing certs and using registries.
     *
     * @return the request builder
     */
    private ProxyConfigUpdateJsonBuilder getFullRequest() {
        return new ProxyConfigUpdateJsonBuilder()
                .serverId(DUMMY_SERVER_ID)
                .parentFqdn(DUMMY_PARENT_FQDN)
                .proxyPort(DUMMY_PROXY_PORT)
                .maxCache(DUMMY_MAX_CACHE)
                .email(DUMMY_ADMIN_MAIL)
                .replaceCerts(
                        DUMMY_ROOT_CA,
                        List.of(DUMMY_INTERMEDIATE_CA_1, DUMMY_INTERMEDIATE_CA_2),
                        DUMMY_PROXY_CERT,
                        DUMMY_PROXY_KEY
                ).sourceRegistryAdvanced(
                        "http://suse.com/proxy-httpd", "proxy-httpd-latest",
                        "http://suse.com/proxy-salt-broker", "proxy-salt-broker-latest",
                        "http://suse.com/proxy-squid", "proxy-squid-latest",
                        "http://suse.com/proxy-ssh", "proxy-ssh-latest",
                        "http://suse.com/proxy-tftpd", "proxy-tftpd-latest"
                );
    }

}
