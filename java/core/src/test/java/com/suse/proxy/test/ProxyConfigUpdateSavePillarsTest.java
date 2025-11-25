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

import static com.suse.proxy.ProxyConfigUtils.PILLAR_REGISTRY_ENTRY;
import static com.suse.proxy.ProxyConfigUtils.PILLAR_REGISTRY_TAG_ENTRY;
import static com.suse.proxy.ProxyConfigUtils.PILLAR_REGISTRY_URL_ENTRY;
import static com.suse.proxy.ProxyConfigUtils.PROXY_PILLAR_CATEGORY;
import static com.suse.proxy.ProxyConfigUtils.ROOT_CA_FIELD;
import static com.suse.proxy.ProxyConfigUtils.SOURCE_MODE_REGISTRY;
import static com.suse.proxy.ProxyConfigUtils.SOURCE_MODE_RPM;
import static com.suse.proxy.ProxyContainerImagesEnum.PROXY_HTTPD;
import static com.suse.proxy.ProxyContainerImagesEnum.PROXY_SALT_BROKER;
import static com.suse.proxy.ProxyContainerImagesEnum.PROXY_SQUID;
import static com.suse.proxy.ProxyContainerImagesEnum.PROXY_SSH;
import static com.suse.proxy.ProxyContainerImagesEnum.PROXY_TFTPD;
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
import static com.suse.proxy.test.ProxyConfigUpdateTestUtils.getDummyTag;
import static com.suse.proxy.test.ProxyConfigUpdateTestUtils.getDummyUrl;
import static com.suse.utils.Predicates.isAbsent;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.junit.jupiter.api.Assertions.fail;

import com.redhat.rhn.domain.server.MinionServer;
import com.redhat.rhn.domain.server.MinionServerFactory;
import com.redhat.rhn.domain.server.Pillar;
import com.redhat.rhn.domain.server.ServerFactory;
import com.redhat.rhn.domain.server.ServerGroupFactory;
import com.redhat.rhn.domain.server.test.MinionServerFactoryTest;
import com.redhat.rhn.manager.entitlement.EntitlementManager;
import com.redhat.rhn.manager.system.SystemManager;
import com.redhat.rhn.manager.system.entitling.SystemEntitlementManager;
import com.redhat.rhn.manager.system.entitling.SystemEntitler;
import com.redhat.rhn.manager.system.entitling.SystemUnentitler;
import com.redhat.rhn.testing.BaseTestCaseWithUser;
import com.redhat.rhn.testing.TestUtils;

import com.suse.manager.ssl.SSLCertGenerationException;
import com.suse.manager.webui.services.iface.SaltApi;
import com.suse.manager.webui.services.test.TestSaltApi;
import com.suse.manager.webui.utils.gson.ProxyConfigUpdateJson;
import com.suse.proxy.ProxyConfigUtils;
import com.suse.proxy.ProxyContainerImagesEnum;
import com.suse.proxy.RegistryUrl;
import com.suse.proxy.update.ProxyConfigUpdateContext;
import com.suse.proxy.update.ProxyConfigUpdateSavePillars;
import com.suse.proxy.update.ProxyConfigUpdateValidation;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.net.URISyntaxException;
import java.util.EnumMap;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;


/**
 * Tests for the ProxyConfigUpdateSavePillars class.
 * These will assume the previous step in the chain of responsibility such as {@link ProxyConfigUpdateValidation}
 * has been executed and, no errors have been added to the context.
 */
public class ProxyConfigUpdateSavePillarsTest extends BaseTestCaseWithUser {

    private final SaltApi saltApi = new TestSaltApi();
    private final SystemManager systemManager =
            new SystemManager(ServerFactory.SINGLETON, ServerGroupFactory.SINGLETON, saltApi);
    private final SystemEntitlementManager systemEntitlementManager = new SystemEntitlementManager(
            new SystemUnentitler(saltApi), new SystemEntitler(saltApi)
    );

    private MinionServer minion;

    @Override
    @BeforeEach
    public void setUp() throws Exception {
        super.setUp();
        this.minion = MinionServerFactoryTest.createTestMinionServer(user);
        minion.setServerArch(ServerFactory.lookupServerArchByLabel("x86_64-redhat-linux"));
        minion.addFqdn(DUMMY_PROXY_FQDN);
        systemEntitlementManager.addEntitlementToServer(minion, EntitlementManager.PROXY);
    }

    /**
     * Tests if an existing proxy pillar data is updated.
     */
    @Test
    public void testHandleWhenUpdatingProxyPillar() throws SSLCertGenerationException {
        // Add proxy pillar to the minion
        final String expectedOutdatedProxyFqdn = "OUTDATED_PROXY_FQDN";
        final String expectedOutdatedParentFqdn = "OUTDATED_PARENT_FQDN";

        Pillar pillar = new Pillar(ProxyConfigUtils.PROXY_PILLAR_CATEGORY, new HashMap<>(), this.minion);
        pillar.add(ProxyConfigUtils.PROXY_FQDN_FIELD, expectedOutdatedProxyFqdn);
        pillar.add(ProxyConfigUtils.PARENT_FQDN_FIELD, expectedOutdatedParentFqdn);

        this.minion.addPillar(pillar);
        TestUtils.saveAndFlush(this.minion);

        //
        ProxyConfigUpdateContext proxyConfigUpdateContext = getCommonContext(this.minion, SOURCE_MODE_RPM);

        // asserting the proxy pillar exists before the handler is called
        minion.getPillarByCategory(PROXY_PILLAR_CATEGORY).ifPresentOrElse(
                p -> {
                    assertEquals(expectedOutdatedProxyFqdn, p.getPillar().get(ProxyConfigUtils.PROXY_FQDN_FIELD));
                    assertEquals(expectedOutdatedParentFqdn, p.getPillar().get(ProxyConfigUtils.PARENT_FQDN_FIELD));
                },
                () -> {
                    throw new AssertionError("Proxy pillar not found");
                }
        );

        // action
        new ProxyConfigUpdateSavePillars().handle(proxyConfigUpdateContext);
        TestUtils.reload(this.minion);

        //
        commonAssertions(proxyConfigUpdateContext, null);
    }


    /**
     * Test the handle method when saving the pillars of a proxy configuration
     * that uses RPM as the source mode and no proxy pillar exists.
     */
    @Test
    public void testHandleWhenUsingRpm() {
        ProxyConfigUpdateContext proxyConfigUpdateContext = getCommonContext(this.minion, SOURCE_MODE_RPM);

        this.minion.getPillarByCategory(PROXY_PILLAR_CATEGORY).ifPresent(p -> fail("Proxy pillar already exists"));

        new ProxyConfigUpdateSavePillars().handle(proxyConfigUpdateContext);
        TestUtils.reload(this.minion);

        commonAssertions(proxyConfigUpdateContext, null);
    }

    /**
     * Test the handle method when no proxy pillar exists.
     */
    @Test
    public void testHandleWhenUsingRegistries() throws SSLCertGenerationException, URISyntaxException {
        ProxyConfigUpdateContext proxyConfigUpdateContext = getCommonContext(this.minion, SOURCE_MODE_REGISTRY);

        RegistryUrl expectedHttpdRegistryUrl = new RegistryUrl(getDummyUrl(PROXY_HTTPD), getDummyTag(PROXY_HTTPD));
        RegistryUrl expectedSaltRegistryUrl =
                new RegistryUrl(getDummyUrl(PROXY_SALT_BROKER), getDummyTag(PROXY_SALT_BROKER));
        RegistryUrl expectedSquidRegistryUrl = new RegistryUrl(getDummyUrl(PROXY_SQUID), getDummyTag(PROXY_SQUID));
        RegistryUrl expectedSshRegistryUrl = new RegistryUrl(getDummyUrl(PROXY_SSH), getDummyTag(PROXY_SSH));
        RegistryUrl expectedTftpdRegistryUrl = new RegistryUrl(getDummyUrl(PROXY_TFTPD), getDummyTag(PROXY_TFTPD));

        Map<ProxyContainerImagesEnum, RegistryUrl> registryUrls = new EnumMap<>(ProxyContainerImagesEnum.class);
        registryUrls.put(PROXY_HTTPD, expectedHttpdRegistryUrl);
        registryUrls.put(PROXY_SALT_BROKER, expectedSaltRegistryUrl);
        registryUrls.put(PROXY_SQUID, expectedSquidRegistryUrl);
        registryUrls.put(PROXY_SSH, expectedSshRegistryUrl);
        registryUrls.put(PROXY_TFTPD, expectedTftpdRegistryUrl);

        proxyConfigUpdateContext.getRegistryUrls().putAll(registryUrls);

        // preconditions
        minion.getPillarByCategory(PROXY_PILLAR_CATEGORY).ifPresent(p -> fail("Proxy pillar already exists"));

        // action
        new ProxyConfigUpdateSavePillars().handle(proxyConfigUpdateContext);
        TestUtils.reload(minion);

        // assertions
        commonAssertions(proxyConfigUpdateContext, registryUrls);
    }


    /**
     * Sets up a ProxyConfigUpdateContext with common parameters to all tests
     * These should all result in overriding any existing pillar entries.
     * The source mode is set to the given value.
     *
     * @param minionIn   the test minion
     * @param sourceMode the source mode
     * @return the context
     */
    private ProxyConfigUpdateContext getCommonContext(MinionServer minionIn, String sourceMode) {
        ProxyConfigUpdateJson request = new ProxyConfigUpdateJsonBuilder()
                .parentFqdn(DUMMY_PARENT_FQDN)
                .proxyPort(DUMMY_PROXY_PORT)
                .maxCache(DUMMY_MAX_CACHE)
                .email(DUMMY_ADMIN_MAIL)
                .sourceMode(sourceMode)
                .build();

        ProxyConfigUpdateContext proxyConfigUpdateContext =
                new ProxyConfigUpdateContext(request, systemManager, user);
        proxyConfigUpdateContext.setProxyMinion(minionIn);
        proxyConfigUpdateContext.setProxyFqdn(DUMMY_PROXY_FQDN);
        proxyConfigUpdateContext.setRootCA(DUMMY_ROOT_CA);
        proxyConfigUpdateContext.setIntermediateCAs(List.of(DUMMY_INTERMEDIATE_CA_1, DUMMY_INTERMEDIATE_CA_2));
        proxyConfigUpdateContext.setProxyCert(DUMMY_PROXY_CERT);
        proxyConfigUpdateContext.setProxyKey(DUMMY_PROXY_KEY);
        return proxyConfigUpdateContext;
    }


    /**
     * Common assertions after saving the configurations.
     *
     * @param proxyConfigUpdateContext the context
     */
    private void commonAssertions(
            ProxyConfigUpdateContext proxyConfigUpdateContext,
            Map<ProxyContainerImagesEnum, RegistryUrl> registryUrls
    ) {
        // assert no errors were reported
        assertFalse(proxyConfigUpdateContext.getErrorReport().hasErrors());

        // assert the minion has the proxy pillar and it us the same as the one in the context
        Optional<MinionServer> minionServer = MinionServerFactory.lookupById(this.minion.getId());
        assertTrue(minionServer.isPresent());
        Optional<Pillar> pillarByCategory = minionServer.get().getPillarByCategory(PROXY_PILLAR_CATEGORY);
        assertTrue(pillarByCategory.isPresent());

        assertEquals(proxyConfigUpdateContext.getPillar(), pillarByCategory.get());

        // assert pillar entries match the expected values
        Map<String, Object> proxyPillarMap = pillarByCategory.get().getPillar();
        assertEquals(DUMMY_PROXY_FQDN, proxyPillarMap.get(ProxyConfigUtils.PROXY_FQDN_FIELD));
        assertEquals(DUMMY_PARENT_FQDN, proxyPillarMap.get(ProxyConfigUtils.PARENT_FQDN_FIELD));
        assertEquals(DUMMY_PROXY_PORT, proxyPillarMap.get(ProxyConfigUtils.PROXY_PORT_FIELD));
        assertEquals(DUMMY_MAX_CACHE, proxyPillarMap.get(ProxyConfigUtils.MAX_CACHE_FIELD));
        assertEquals(DUMMY_ADMIN_MAIL, proxyPillarMap.get(ProxyConfigUtils.EMAIL_FIELD));

        assertEquals(ROOT_CA_FIELD, proxyPillarMap.get(ProxyConfigUtils.ROOT_CA_FIELD));
        assertEquals(
                List.of(DUMMY_INTERMEDIATE_CA_1, DUMMY_INTERMEDIATE_CA_2),
                proxyPillarMap.get(ProxyConfigUtils.INTERMEDIATE_CAS_FIELD)
        );
        assertEquals(DUMMY_PROXY_CERT, proxyPillarMap.get(ProxyConfigUtils.PROXY_CERT_FIELD));
        assertEquals(DUMMY_PROXY_KEY, proxyPillarMap.get(ProxyConfigUtils.PROXY_KEY_FIELD));

        if (isAbsent(registryUrls)) {
            assertFalse(proxyPillarMap.containsKey(ProxyConfigUtils.PILLAR_REGISTRY_ENTRY));
        }
        else {
            Map<String, RegistryUrl> registryEntriesMap = (Map) proxyPillarMap.get(PILLAR_REGISTRY_ENTRY);
            assertRegistryEntries(registryUrls, registryEntriesMap, PROXY_HTTPD);
            assertRegistryEntries(registryUrls, registryEntriesMap, PROXY_SALT_BROKER);
            assertRegistryEntries(registryUrls, registryEntriesMap, PROXY_SQUID);
            assertRegistryEntries(registryUrls, registryEntriesMap, PROXY_SSH);
            assertRegistryEntries(registryUrls, registryEntriesMap, PROXY_TFTPD);
        }
    }

    /**
     * Asserts the registry entries in the pillar map match the expected values.
     *
     * @param registryUrls        the registry URLs
     * @param registryEntriesMap  the registry entries map
     * @param imageEnum           the image enum
     */
    private void assertRegistryEntries(
            Map<ProxyContainerImagesEnum, RegistryUrl> registryUrls,
            Map<String, RegistryUrl> registryEntriesMap,
            ProxyContainerImagesEnum imageEnum
    ) {
        Map<String, String> registryMap = (Map) registryEntriesMap.get(imageEnum.getImageName());
        assertEquals(registryUrls.get(imageEnum).getRegistry(), registryMap.get(PILLAR_REGISTRY_URL_ENTRY));
        assertEquals(registryUrls.get(imageEnum).getTag(), registryMap.get(PILLAR_REGISTRY_TAG_ENTRY));
    }

}
