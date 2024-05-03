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
package com.redhat.rhn.manager.content.test;


import static com.redhat.rhn.domain.channel.test.ChannelFactoryTest.createTestClonedChannel;
import static com.redhat.rhn.testing.RhnBaseTestCase.assertContains;
import static com.redhat.rhn.testing.RhnBaseTestCase.assertNotEmpty;
import static org.junit.jupiter.api.Assertions.assertAll;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertInstanceOf;
import static org.junit.jupiter.api.Assertions.assertIterableEquals;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.junit.jupiter.api.Assertions.fail;

import com.redhat.rhn.common.conf.Config;
import com.redhat.rhn.common.db.datasource.ModeFactory;
import com.redhat.rhn.common.db.datasource.WriteMode;
import com.redhat.rhn.common.hibernate.HibernateFactory;
import com.redhat.rhn.domain.channel.Channel;
import com.redhat.rhn.domain.channel.ChannelFactory;
import com.redhat.rhn.domain.channel.ChannelFamily;
import com.redhat.rhn.domain.channel.ChannelFamilyFactory;
import com.redhat.rhn.domain.channel.ContentSource;
import com.redhat.rhn.domain.channel.test.ChannelFamilyFactoryTest;
import com.redhat.rhn.domain.cloudpayg.PaygSshData;
import com.redhat.rhn.domain.cloudpayg.PaygSshDataFactory;
import com.redhat.rhn.domain.common.ManagerInfoFactory;
import com.redhat.rhn.domain.credentials.CloudRMTCredentials;
import com.redhat.rhn.domain.credentials.Credentials;
import com.redhat.rhn.domain.credentials.CredentialsFactory;
import com.redhat.rhn.domain.credentials.RemoteCredentials;
import com.redhat.rhn.domain.credentials.SCCCredentials;
import com.redhat.rhn.domain.credentials.VHMCredentials;
import com.redhat.rhn.domain.product.MgrSyncChannelDto;
import com.redhat.rhn.domain.product.ReleaseStage;
import com.redhat.rhn.domain.product.SUSEProduct;
import com.redhat.rhn.domain.product.SUSEProductChannel;
import com.redhat.rhn.domain.product.SUSEProductFactory;
import com.redhat.rhn.domain.product.SUSEProductSCCRepository;
import com.redhat.rhn.domain.product.test.SUSEProductTestUtils;
import com.redhat.rhn.domain.rhnpackage.PackageArch;
import com.redhat.rhn.domain.rhnpackage.PackageFactory;
import com.redhat.rhn.domain.scc.SCCCachingFactory;
import com.redhat.rhn.domain.scc.SCCOrderItem;
import com.redhat.rhn.domain.scc.SCCRepository;
import com.redhat.rhn.domain.scc.SCCRepositoryAuth;
import com.redhat.rhn.domain.scc.SCCRepositoryBasicAuth;
import com.redhat.rhn.domain.scc.SCCRepositoryCloudRmtAuth;
import com.redhat.rhn.domain.scc.SCCRepositoryTokenAuth;
import com.redhat.rhn.domain.server.MinionServer;
import com.redhat.rhn.domain.server.Pillar;
import com.redhat.rhn.domain.server.ServerFactory;
import com.redhat.rhn.domain.server.test.MinionServerFactoryTest;
import com.redhat.rhn.frontend.xmlrpc.sync.content.ContentSyncSource;
import com.redhat.rhn.frontend.xmlrpc.sync.content.SCCContentSyncSource;
import com.redhat.rhn.manager.content.ContentSyncException;
import com.redhat.rhn.manager.content.ContentSyncManager;
import com.redhat.rhn.manager.content.MgrSyncProductDto;
import com.redhat.rhn.manager.content.ProductTreeEntry;
import com.redhat.rhn.manager.setup.MirrorCredentialsManager;
import com.redhat.rhn.manager.system.SystemManager;
import com.redhat.rhn.testing.JMockBaseTestCaseWithUser;
import com.redhat.rhn.testing.TestUtils;

import com.suse.cloud.CloudPaygManager;
import com.suse.cloud.test.TestCloudPaygManagerBuilder;
import com.suse.manager.webui.services.pillar.MinionGeneralPillarGenerator;
import com.suse.manager.webui.services.pillar.MinionPillarManager;
import com.suse.mgrsync.MgrSyncStatus;
import com.suse.salt.netapi.parser.JsonParser;
import com.suse.scc.client.SCCClient;
import com.suse.scc.client.SCCClientException;
import com.suse.scc.model.ChannelFamilyJson;
import com.suse.scc.model.SCCProductJson;
import com.suse.scc.model.SCCRepositoryJson;
import com.suse.scc.model.SCCSubscriptionJson;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.reflect.TypeToken;

import org.apache.commons.io.FileUtils;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.io.File;
import java.io.InputStreamReader;
import java.net.URI;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.Instant;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.function.Function;
import java.util.stream.Collectors;
import java.util.stream.Stream;

/**
 * Tests for {@link ContentSyncManager}.
 */
public class ContentSyncManagerTest extends JMockBaseTestCaseWithUser {

    private static final Logger LOGGER = LogManager.getLogger(ContentSyncManagerTest.class);

    // Files we read
    private static final String JARPATH = "/com/redhat/rhn/manager/content/test/";

    private static final String SUBSCRIPTIONS_JSON = "organizations_subscriptions.json";
    private static final String ORDERS_JSON = "organizations_orders.json";
    private static final String SUBSCRIPTIONS2_JSON = "organizations_subscriptions2.json";
    private static final String ORDERS2_JSON = "organizations_orders2.json";

    private static final String PRODUCTS_JSON = "productsUnscoped.json";
    private static final String TREE_JSON = "product_tree.json";
    private static final String REPOS_JSON = "repositories.json";

    @Test
    public void testSubscriptionDeleteCaching() throws Exception {

        int productId = 12345;
        assertNull(SUSEProductFactory.lookupByProductId(productId));
        String name = TestUtils.randomString();
        String identifier = TestUtils.randomString();
        String version = TestUtils.randomString();
        String releaseType = TestUtils.randomString();
        String friendlyName = TestUtils.randomString();
        String productClass = TestUtils.randomString();

        // Setup a product as it comes from SCC
        SCCProductJson p = new SCCProductJson(productId, name, identifier, version,
                releaseType, "i686", friendlyName, productClass, ReleaseStage.released, "", false,
                "", "", Collections.emptyList(), Collections.emptyList(), Collections.emptyList(),
                Collections.emptyList(), null, false);

        List<SCCProductJson> products = new ArrayList<>();
        products.add(p);

        // Call updateSUSEProducts()
        ContentSyncManager csm = new ContentSyncManager();
        csm.setSumaProductTreeJson(Optional.of(new File("/usr/share/susemanager/scc/product_tree.json")));
        csm.updateSUSEProducts(products);

        List<SCCSubscriptionJson> subscriptions = new LinkedList<>();
        SCCCredentials cred1 = CredentialsFactory.createSCCCredentials("hans", "pw1");
        CredentialsFactory.storeCredentials(cred1);

        SCCSubscriptionJson s1 = new SCCSubscriptionJson();
        s1.setName("SLES");
        s1.setProductClasses(Arrays.asList("7261"));
        s1.setProductIds(Arrays.asList((long)productId));
        s1.setId(1L);
        s1.setRegcode("abcdef");
        s1.setType("full");
        s1.setSystemLimit(5);
        SCCSubscriptionJson s2 = new SCCSubscriptionJson();
        s2.setName("SLES 12");
        s2.setProductClasses(Arrays.asList("7261"));
        s2.setProductIds(Arrays.asList((long)productId));
        s2.setId(2L);
        s2.setRegcode("12345");
        s2.setType("full");
        s2.setSystemLimit(4);

        subscriptions.add(s1);
        subscriptions.add(s2);

        csm.refreshSubscriptionCache(subscriptions, new SCCContentSyncSource(cred1));
        HibernateFactory.getSession().flush();

        com.redhat.rhn.domain.scc.SCCSubscription one = SCCCachingFactory.lookupSubscriptionBySccId(1L);
        assertEquals(s1.getName(), one.getName());
        com.redhat.rhn.domain.scc.SCCSubscription two = SCCCachingFactory.lookupSubscriptionBySccId(2L);
        assertEquals(two.getName(), two.getName());

        subscriptions.remove(s2);
        csm.refreshSubscriptionCache(subscriptions, new SCCContentSyncSource(cred1));
        HibernateFactory.getSession().flush();

        one = SCCCachingFactory.lookupSubscriptionBySccId(1L);
        assertEquals(s1.getName(), one.getName());
        two = SCCCachingFactory.lookupSubscriptionBySccId(2L);
        assertNull(two);
    }

    @Test
    public void testListSubscriptionsCaching() throws Exception {
        File subJson = new File(TestUtils.findTestData(
                new File(JARPATH,  "sccdata/" + SUBSCRIPTIONS_JSON).getAbsolutePath()).getPath());
        File orderJson = new File(TestUtils.findTestData(
                new File(JARPATH, "sccdata/" + ORDERS_JSON).getAbsolutePath()).getPath());
        File subJson2 = new File(TestUtils.findTestData(
                new File(JARPATH,  "sccdata/" + SUBSCRIPTIONS2_JSON).getAbsolutePath()).getPath());
        File orderJson2 = new File(TestUtils.findTestData(
                new File(JARPATH, "sccdata/" + ORDERS2_JSON).getAbsolutePath()).getPath());
        Path fromdir = Files.createTempDirectory("sumatest");
        File subtempFile = new File(fromdir.toString(), SUBSCRIPTIONS_JSON);
        File ordertempFile = new File(fromdir.toString(), ORDERS_JSON);
        Files.copy(subJson.toPath(), subtempFile.toPath());
        Files.copy(orderJson.toPath(), ordertempFile.toPath());
        try {
            Config.get().setString(ContentSyncManager.RESOURCE_PATH, fromdir.toString());

            SCCCachingFactory.clearOrderItems();
            SCCCachingFactory.clearSubscriptions();
            SUSEProductTestUtils.clearAllProducts();
            SUSEProductTestUtils.createVendorSUSEProducts();
            ContentSyncManager cm = new ContentSyncManager();
            Collection<SCCSubscriptionJson> s = cm.updateSubscriptions();
            assertNotNull(s);
            assertFalse(cm.hasToolsChannelSubscription(), "Should no find a SUSE Manager Server Subscription");

            for (com.redhat.rhn.domain.scc.SCCSubscription dbs : SCCCachingFactory.lookupSubscriptions()) {
                assertEquals("55REGCODE180", dbs.getRegcode());
                assertEquals("EMEA SLES x86/x86_64 Standard Support & Training", dbs.getName());
                assertEquals(1234, dbs.getSccId());
                assertEquals("ACTIVE", dbs.getStatus());
                assertContains(dbs.getProducts(), SUSEProductFactory.lookupByProductId(1322));
                assertContains(dbs.getProducts(), SUSEProductFactory.lookupByProductId(1324));
                assertEquals(702, dbs.getSystemLimit().longValue());
                assertEquals(Date.from(Instant.parse("2017-12-31T00:00:00.000Z")),
                        dbs.getExpiresAt());
            }
            s = cm.updateSubscriptions();
            HibernateFactory.getSession().flush();
            for (SCCOrderItem item : SCCCachingFactory.lookupOrderItems()) {
                if (item.getSccId() == 9998L) {
                    assertEquals(10, item.getQuantity().longValue());
                    assertEquals(1234, item.getSubscriptionId().longValue());
                    assertEquals(Date.from(Instant.parse("2013-01-01T00:00:00.000Z")),
                            item.getEndDate());
                    assertEquals("662644474670", item.getSku());
                }
                else if (item.getSccId() == 9999L) {
                    assertEquals(100, item.getQuantity().longValue());
                    assertEquals(1234, item.getSubscriptionId().longValue());
                    assertEquals(Date.from(Instant.parse("2017-01-01T00:00:00.000Z")),
                            item.getEndDate());
                    assertEquals("874-005117", item.getSku());
                }
                else {
                    fail();
                }
            }
            SUSEProductTestUtils.createVendorSUSEProductEnvironment(
                    user, "/com/redhat/rhn/manager/content/test", false);
            HibernateFactory.getSession().flush();
            HibernateFactory.getSession().clear();
            subtempFile.delete();
            ordertempFile.delete();
            Files.copy(subJson2.toPath(), subtempFile.toPath());
            Files.copy(orderJson2.toPath(), ordertempFile.toPath());
            s = cm.updateSubscriptions();
            HibernateFactory.getSession().flush();
            assertTrue(cm.hasToolsChannelSubscription(), "Should have a SUSE Manager Server Subscription");
        }
        finally {
            Config.get().remove(ContentSyncManager.RESOURCE_PATH);
            SUSEProductTestUtils.deleteIfTempFile(subJson);
            SUSEProductTestUtils.deleteIfTempFile(orderJson);
            SUSEProductTestUtils.deleteIfTempFile(subJson2);
            SUSEProductTestUtils.deleteIfTempFile(orderJson2);
            subtempFile.delete();
            ordertempFile.delete();
            fromdir.toFile().delete();
        }
    }

    @Test
    public void testUpdateProducts()  throws Exception {
        SUSEProductTestUtils.createVendorSUSEProductEnvironment(user, null, false);

        Gson gson = new GsonBuilder()
                .setDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSSX")
                .create();
        InputStreamReader inputStreamReader3 = new InputStreamReader(
                ContentSyncManager.class.getResourceAsStream("/com/redhat/rhn/manager/content/test/repositories.json"));
        List<SCCRepositoryJson> repositories = gson.fromJson(
                inputStreamReader3, new TypeToken<List<SCCRepositoryJson>>() { }.getType());

        SCCCredentials credentials = CredentialsFactory.listSCCCredentials()
                .stream()
                .filter(c -> c.getUsername().equals("dummy"))
                .findFirst().get();

        assertTrue(SCCCachingFactory.lookupRepositoryBySccId(633L).get()
                .getRepositoryAuth().isEmpty(), "Repo should not have authentication.");

        ContentSyncManager csm = new ContentSyncManager();
        // todo i think this doesn't mock correctly and causes timeouts
        csm.refreshRepositoriesAuthentication(repositories, new SCCContentSyncSource(credentials), null);

        Optional<SCCRepository> upRepoOpt = SCCCachingFactory.lookupRepositoryBySccId(633L);
        assertTrue(upRepoOpt.isPresent(), "Repo not found");
        SCCRepository upRepo = upRepoOpt.get();
        assertTrue(upRepo.getBestAuth().flatMap(SCCRepositoryAuth::tokenAuth).isPresent(),
                "Best Auth is not token auth");

    }

    @Test
    public void testUpdateRepositories() {
        Gson gson = new GsonBuilder()
                .setDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSSX")
                .create();
        SCCCredentials credentials = CredentialsFactory.createSCCCredentials("dummy", "dummy");
        credentials.setUrl("dummy");
        credentials.setUser(user);
        CredentialsFactory.storeCredentials(credentials);

        InputStreamReader inputStreamReader = new InputStreamReader(
                ContentSyncManager.class.getResourceAsStream("/com/redhat/rhn/manager/content/test/repositories.json"));
        List<SCCRepositoryJson> repositories = gson.fromJson(inputStreamReader,
                new TypeToken<List<SCCRepositoryJson>>() { }.getType());

        // Create Repositories
        SCCRepository repo = new SCCRepository();
        repo.setAutorefresh(false);
        repo.setDescription("SLE-Product-SLES15-Pool for sle-15-x86_64");
        repo.setName("SLE-Product-SLES15-Pool");
        repo.setDistroTarget("sle-15-x86_64");
        repo.setSccId(2707L);
        repo.setUrl("https://updates.suse.com/SUSE/Products/SLE-Product-SLES/15/x86_64/product");
        SCCCachingFactory.saveRepository(repo);

        SCCRepository repo2 = new SCCRepository();
        repo2.setAutorefresh(true);
        repo2.setDescription("SLE-Product-SLES15-Updates for sle-15-x86_64");
        repo2.setName("SLE-Product-SLES15-Updates");
        repo2.setDistroTarget("sle-15-x86_64");
        repo2.setSccId(2705L);
        repo2.setUrl("https://updates.suse.com/SUSE/Updates/SLE-Product-SLES/15/x86_64/update/");
        SCCCachingFactory.saveRepository(repo2);

        ContentSyncManager csm = new ContentSyncManager();
        csm.refreshRepositoriesAuthentication(repositories, new SCCContentSyncSource(credentials), null);

        Optional<SCCRepository> upRepoOpt = SCCCachingFactory.lookupRepositoryBySccId(2705L);
        assertTrue(upRepoOpt.isPresent(), "Repo not found");
        SCCRepositoryAuth upRepoBest = upRepoOpt.flatMap(SCCRepository::getBestAuth).orElse(null);
        assertNotNull(upRepoBest);
        assertTrue(upRepoBest instanceof SCCRepositoryTokenAuth, "Best Auth is not token auth");

        csm.refreshRepositoriesAuthentication(repositories, new SCCContentSyncSource(credentials), null);
        upRepoOpt = SCCCachingFactory.lookupRepositoryBySccId(2707L);
        assertTrue(upRepoOpt.isPresent(), "Repo not found");
        upRepoBest = upRepoOpt.flatMap(SCCRepository::getBestAuth).orElse(null);
        assertNotNull(upRepoBest);
        assertTrue(upRepoBest instanceof SCCRepositoryTokenAuth, "Best Auth is not token auth");
    }

    @Test
    public void testReleaseStageOverride() throws Exception {
        SUSEProductTestUtils.createVendorSUSEProductEnvironment(
                user, "/com/redhat/rhn/manager/content/test/smallBase", true);
        HibernateFactory.getSession().flush();
        HibernateFactory.getSession().clear();
        SUSEProduct product = SUSEProductFactory.lookupByProductId(1150);
        assertEquals(ReleaseStage.beta, product.getReleaseStage());
    }

    @Test
    public void testClonedVendorChannelMandadory() throws Exception {
        SUSEProductTestUtils.createVendorSUSEProductEnvironment(
                user, "/com/redhat/rhn/manager/content/test/smallBase", true);

        SUSEProductTestUtils.addChannelsForProduct(SUSEProductFactory.lookupByProductId(1575));
        SUSEProductTestUtils.addChannelsForProduct(SUSEProductFactory.lookupByProductId(1576));
        SUSEProductTestUtils.addChannelsForProduct(SUSEProductFactory.lookupByProductId(1580));



        Channel baseChannel = ChannelFactory.lookupByLabel("sle-product-sles15-pool-x86_64");
        Channel basesystemPool = ChannelFactory.lookupByLabel("sle-module-basesystem15-pool-x86_64");
        Channel applicationsPool = ChannelFactory.lookupByLabel("sle-module-server-applications15-pool-x86_64");


        Channel baseClone = createTestClonedChannel(baseChannel, user, "", "-clone",
                "Clone of", "", null);
        Channel basesystemJan = createTestClonedChannel(basesystemPool, user, "", "-jan",
                "", " Jan", baseClone);
        Channel applicationsJan = createTestClonedChannel(applicationsPool, user, "", "-jan",
                "", " Jan", baseClone);

        Channel basesystemFeb = createTestClonedChannel(basesystemPool, user, "", "-feb",
                "", " Feb", baseClone);
        Channel applicationsFeb = createTestClonedChannel(applicationsPool, user, "", "-feb",
                "", " Feb", baseClone);

        HibernateFactory.getSession().flush();
        HibernateFactory.getSession().clear();

        List<Channel> resultAppFeb = SUSEProductFactory
                .findSyncedMandatoryChannels("sle-module-server-applications15-pool-x86_64-feb")
                .collect(Collectors.toList());

        assertIterableEquals(
                List.of(applicationsFeb, basesystemFeb).stream().distinct().sorted().collect(Collectors.toList()),
                resultAppFeb.stream().distinct().sorted().collect(Collectors.toList())
        );

        List<Channel> resultAppJan = SUSEProductFactory
                .findSyncedMandatoryChannels("sle-module-server-applications15-pool-x86_64-jan")
                .collect(Collectors.toList());

        assertIterableEquals(
                List.of(applicationsJan, basesystemJan).stream().distinct().sorted().collect(Collectors.toList()),
                resultAppJan.stream().distinct().sorted().collect(Collectors.toList())
        );
    }

    /**
     * Test if changes in SCC data result in updates of the channel data in the DB
     * @throws Exception if anything goes wrong
     */
    @Test
    public void testUpdateChannels() throws Exception {
        SUSEProductTestUtils.createVendorSUSEProductEnvironment(
                user, "/com/redhat/rhn/manager/content/test/smallBase", true);
        HibernateFactory.getSession().flush();
        HibernateFactory.getSession().clear();

        // SLES12 GA
        SUSEProductTestUtils.addChannelsForProduct(SUSEProductFactory.lookupByProductId(1117));
        HibernateFactory.getSession().flush();
        HibernateFactory.getSession().clear();

        Channel pool = ChannelFactory.lookupByLabel("sles12-pool-x86_64");
        Channel update = ChannelFactory.lookupByLabel("sles12-updates-x86_64");
        assertEquals("SLES12-Pool for x86_64", pool.getName());
        assertEquals("SUSE Linux Enterprise Server 12 x86_64", pool.getSummary());
        assertEquals("SUSE Linux Enterprise offers a comprehensive suite of products built on a single code base. " +
                "The platform addresses business needs from the smallest thin-client devices to the world's most " +
                "powerful high-performance computing and mainframe servers. SUSE Linux Enterprise offers common " +
                "management tools and technology certifications across the platform, and each product is " +
                "enterprise-class.", pool.getDescription());
        assertTrue(pool.getSuseProductChannels().stream().findFirst().get().isMandatory());
        assertEquals("SLES12-Updates for x86_64", update.getName());
        assertEquals("SUSE Linux Enterprise Server 12 x86_64", update.getSummary());
        assertEquals("SUSE Linux Enterprise offers a comprehensive suite of products built on a single code base. " +
                "The platform addresses business needs from the smallest thin-client devices to the world's most " +
                "powerful high-performance computing and mainframe servers. SUSE Linux Enterprise offers common " +
                "management tools and technology certifications across the platform, and each product is " +
                "enterprise-class.", update.getDescription());

        Gson gson = new GsonBuilder()
                .setDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSSX")
                .create();
        InputStreamReader inReaderProducts = new InputStreamReader(ContentSyncManager.class
                .getResourceAsStream("/com/redhat/rhn/manager/content/test/data1/productsUnscoped.json"));
        List<SCCProductJson> productsChanged = gson.fromJson(
                inReaderProducts, new TypeToken<List<SCCProductJson>>() { }.getType());
        InputStreamReader inReaderTree = new InputStreamReader(ContentSyncManager.class
                .getResourceAsStream("/com/redhat/rhn/manager/content/test/data1/product_tree.json"));
        List<ProductTreeEntry> staticTreeChanged = JsonParser.GSON.fromJson(
                inReaderTree, new TypeToken<List<ProductTreeEntry>>() { }.getType());

        InputStreamReader inReaderAddRepos = new InputStreamReader(ContentSyncManager.class
                .getResourceAsStream("/com/redhat/rhn/manager/content/test/smallBase/additional_repositories.json"));
        List<SCCRepositoryJson> additionalRepos = gson.fromJson(inReaderAddRepos,
                new TypeToken<List<SCCRepositoryJson>>() { }.getType());

        ContentSyncManager csm = new ContentSyncManager();
        csm.updateSUSEProducts(productsChanged, staticTreeChanged, additionalRepos);
        HibernateFactory.getSession().flush();
        HibernateFactory.getSession().clear();

        Channel changedPool = ChannelFactory.lookupByLabel("sles12-pool-x86_64");
        Channel changedUpdate = ChannelFactory.lookupByLabel("sles12-updates-x86_64");
        assertEquals("SLES12-Pool for x86_64 UPDATED", changedPool.getName());
        assertEquals("SUSE Linux Enterprise Server 12 x86_64 UPDATED", changedPool.getSummary());
        assertEquals(
            "UPDATED: SUSE Linux Enterprise offers a comprehensive suite of products built on a single code base. " +
            "The platform addresses business needs from the smallest thin-client devices to the world's most " +
            "powerful high-performance computing and mainframe servers. SUSE Linux Enterprise offers common " +
            "management tools and technology certifications across the platform, and each product is " +
            "enterprise-class.", changedPool.getDescription());
        assertFalse(changedPool.getSuseProductChannels().stream().findFirst().get().isMandatory());
        assertEquals("SLES12-Updates for x86_64 UPDATED", changedUpdate.getName());
        assertEquals("SUSE Linux Enterprise Server 12 x86_64 UPDATED", changedUpdate.getSummary());
        assertEquals(
            "UPDATED: SUSE Linux Enterprise offers a comprehensive suite of products built on a single code base. " +
            "The platform addresses business needs from the smallest thin-client devices to the world's most " +
            "powerful high-performance computing and mainframe servers. SUSE Linux Enterprise offers common " +
            "management tools and technology certifications across the platform, and each product is " +
            "enterprise-class.", changedUpdate.getDescription());
    }

    /**
     * Test if changes in SCC data result in updates of the channel data in the DB and pillar
     * data for an assigned system
     * @throws Exception if anything goes wrong
     */
    @Test
    public void testUpdateChannelsPillar() throws Exception {
        MinionServer testMinionServer = MinionServerFactoryTest.createTestMinionServer(user);
        testMinionServer.setServerArch(ServerFactory.lookupServerArchByLabel("x86_64-redhat-linux"));
        testMinionServer = TestUtils.saveAndReload(testMinionServer);

        SUSEProductTestUtils.createVendorSUSEProductEnvironment(
                user, "/com/redhat/rhn/manager/content/test/smallBase", true);
        HibernateFactory.getSession().flush();
        HibernateFactory.getSession().clear();

        // SLES12 GA
        SUSEProductTestUtils.addChannelsForProduct(SUSEProductFactory.lookupByProductId(1117));
        SUSEProductTestUtils.addChannelsForProduct(SUSEProductFactory.lookupByProductId(1150));
        HibernateFactory.getSession().flush();
        HibernateFactory.getSession().clear();

        Channel pool = ChannelFactory.lookupByLabel("sles12-pool-x86_64");
        Channel update = ChannelFactory.lookupByLabel("sles12-updates-x86_64");
        Channel legacy = ChannelFactory.lookupByLabel("sle-module-legacy12-pool-x86_64");
        assertEquals("SLES12-Pool for x86_64", pool.getName());
        assertEquals("SUSE Linux Enterprise Server 12 x86_64", pool.getSummary());
        assertEquals(
                "SUSE Linux Enterprise offers a comprehensive suite of products built on a single code base. " +
                "The platform addresses business needs from the smallest thin-client devices to the world's most " +
                "powerful high-performance computing and mainframe servers. SUSE Linux Enterprise offers common " +
                "management tools and technology certifications across the platform, and each product is " +
                "enterprise-class.", pool.getDescription());
        assertTrue(pool.getSuseProductChannels().stream().findFirst().get().isMandatory());
        assertEquals("SLES12-Updates for x86_64", update.getName());
        assertEquals("SUSE Linux Enterprise Server 12 x86_64", update.getSummary());
        assertEquals(
                "SUSE Linux Enterprise offers a comprehensive suite of products built on a single code base. " +
                "The platform addresses business needs from the smallest thin-client devices to the world's most " +
                "powerful high-performance computing and mainframe servers. SUSE Linux Enterprise offers common " +
                "management tools and technology certifications across the platform, and each product is " +
                "enterprise-class.", update.getDescription());

        assertEquals("file:///usr/lib/rpm/gnupg/keys/gpg-pubkey-39db7c82-5f68629b.asc", legacy.getGPGKeyUrl());

        SystemManager.subscribeServerToChannel(user, testMinionServer, pool);
        SystemManager.subscribeServerToChannel(user, testMinionServer, update);
        SystemManager.subscribeServerToChannel(user, testMinionServer, legacy);


        // Refresh pillar data for the assigned clients
        MinionPillarManager.INSTANCE.generatePillar(testMinionServer, true,
                MinionPillarManager.PillarSubset.GENERAL);
        Pillar pillar = testMinionServer.getPillarByCategory(MinionGeneralPillarGenerator.CATEGORY).orElseThrow();
        Object channelPillar = pillar.getPillar().get("channels");

        Gson gson = new GsonBuilder()
                .setDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSSX")
                .create();
        InputStreamReader inReaderProducts = new InputStreamReader(ContentSyncManager.class
                .getResourceAsStream("/com/redhat/rhn/manager/content/test/data1/productsUnscoped.json"));
        List<SCCProductJson> productsChanged = gson.fromJson(
                inReaderProducts, new TypeToken<List<SCCProductJson>>() { }.getType());
        InputStreamReader inReaderTree = new InputStreamReader(ContentSyncManager.class
                .getResourceAsStream("/com/redhat/rhn/manager/content/test/data1/product_tree.json"));
        List<ProductTreeEntry> staticTreeChanged = JsonParser.GSON.fromJson(
                inReaderTree, new TypeToken<List<ProductTreeEntry>>() { }.getType());

        InputStreamReader inReaderAddRepos = new InputStreamReader(ContentSyncManager.class
                .getResourceAsStream("/com/redhat/rhn/manager/content/test/smallBase/additional_repositories.json"));
        List<SCCRepositoryJson> additionalRepos = gson.fromJson(inReaderAddRepos,
                new TypeToken<List<SCCRepositoryJson>>() { }.getType());

        ContentSyncManager csm = new ContentSyncManager();
        csm.updateSUSEProducts(productsChanged, staticTreeChanged, additionalRepos);
        HibernateFactory.getSession().flush();
        HibernateFactory.getSession().clear();

        Channel changedPool = ChannelFactory.lookupByLabel("sles12-pool-x86_64");
        Channel changedUpdate = ChannelFactory.lookupByLabel("sles12-updates-x86_64");
        Channel changedLegacy = ChannelFactory.lookupByLabel("sle-module-legacy12-pool-x86_64");
        assertEquals("SLES12-Pool for x86_64 UPDATED", changedPool.getName());
        assertEquals("SUSE Linux Enterprise Server 12 x86_64 UPDATED", changedPool.getSummary());
        assertEquals(
            "UPDATED: SUSE Linux Enterprise offers a comprehensive suite of products built on a single code base. " +
            "The platform addresses business needs from the smallest thin-client devices to the world's most " +
            "powerful high-performance computing and mainframe servers. SUSE Linux Enterprise offers common " +
            "management tools and technology certifications across the platform, and each product is " +
            "enterprise-class.", changedPool.getDescription());
        assertFalse(changedPool.getSuseProductChannels().stream().findFirst().get().isMandatory());
        assertEquals("SLES12-Updates for x86_64 UPDATED", changedUpdate.getName());
        assertEquals("SUSE Linux Enterprise Server 12 x86_64 UPDATED", changedUpdate.getSummary());
        assertEquals(
            "UPDATED: SUSE Linux Enterprise offers a comprehensive suite of products built on a single code base. " +
            "The platform addresses business needs from the smallest thin-client devices to the world's most " +
            "powerful high-performance computing and mainframe servers. SUSE Linux Enterprise offers common " +
            "management tools and technology certifications across the platform, and each product is " +
            "enterprise-class.", changedUpdate.getDescription());
        assertEquals("file:///usr/lib/rpm/gnupg/keys/gpg-pubkey-39db7c82-5f68629b.asc " +
                        "file:///etc/pki/rpm-gpg/suse-addon-97a636db0bad8ecc.key",
                changedLegacy.getGPGKeyUrl());
        Pillar pillarChanged = testMinionServer.getPillarByCategory(MinionGeneralPillarGenerator.CATEGORY)
                .orElseThrow();
        Map<String, Map<String, String>> changedChannelPillar = (Map<String, Map<String, String>>) pillarChanged
                .getPillar().get("channels");
        assertNotEquals(channelPillar, changedChannelPillar);
        assertEquals("file:///usr/lib/rpm/gnupg/keys/gpg-pubkey-39db7c82-5f68629b.asc " +
                        "file:///etc/pki/rpm-gpg/suse-addon-97a636db0bad8ecc.key",
            changedChannelPillar.get("sle-module-legacy12-pool-x86_64").get("gpgkeyurl"));
    }

    /**
     * Test changes of the repo URL (result in change of the repository)
     * @throws Exception if anything goes wrong
     */
    @Test
    public void testUpdateChannelsWithSimilarPath() throws Exception {
        SUSEProductTestUtils.createVendorSUSEProductEnvironment(
                user, "/com/redhat/rhn/manager/content/test/smallBase", true);
        HibernateFactory.getSession().flush();
        HibernateFactory.getSession().clear();

        // SLES12 GA
        SUSEProductTestUtils.addChannelsForProduct(SUSEProductFactory.lookupByProductId(1117));
        HibernateFactory.getSession().flush();
        HibernateFactory.getSession().clear();

        SUSEProduct sles = SUSEProductFactory.lookupByProductId(1117);
        sles.getRepositories().stream()
            .filter(SUSEProductSCCRepository::isMandatory)
            .forEach(pr -> {
                assertNotNull(pr.getRepository());
                SCCRepositoryAuth bestAuth = pr.getRepository().getBestAuth().get();
                ContentSource cs = bestAuth.getContentSource();
                assertNotNull(cs);
                assertEquals(bestAuth.getUrl(), cs.getSourceUrl());
            });
        SCCRepository slesUpRepo = SCCCachingFactory.lookupRepositoryBySccId(1632L).get();
        assertEquals("https://updates.suse.com/SUSE/Updates/SLE-SERVER/12/x86_64/update/",
                slesUpRepo.getUrl());
        assertEquals("https://updates.suse.com/SUSE/Updates/SLE-SERVER/12/x86_64/update/?my-fake-token",
                slesUpRepo.getBestAuth().get().getUrl());

        Gson gson = new GsonBuilder()
                .setDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSSX")
                .create();
        InputStreamReader inReaderProducts = new InputStreamReader(ContentSyncManager.class
                .getResourceAsStream("/com/redhat/rhn/manager/content/test/data1/productsUnscoped.json"));
        List<SCCProductJson> productsChanged = gson.fromJson(
                inReaderProducts, new TypeToken<List<SCCProductJson>>() { }.getType());
        InputStreamReader inReaderTree = new InputStreamReader(ContentSyncManager.class
                .getResourceAsStream("/com/redhat/rhn/manager/content/test/data1/product_tree.json"));
        List<ProductTreeEntry> staticTreeChanged = JsonParser.GSON.fromJson(
                inReaderTree, new TypeToken<List<ProductTreeEntry>>() { }.getType());
        InputStreamReader inReaderRepos = new InputStreamReader(ContentSyncManager.class
                .getResourceAsStream("/com/redhat/rhn/manager/content/test/data1/repositories.json"));
        List<SCCRepositoryJson> repositoriesChanged = gson.fromJson(
                inReaderRepos, new TypeToken<List<SCCRepositoryJson>>() { }.getType());

        InputStreamReader inReaderAddRepos = new InputStreamReader(ContentSyncManager.class
                .getResourceAsStream("/com/redhat/rhn/manager/content/test/smallBase/additional_repositories.json"));
        List<SCCRepositoryJson> additionalRepos = gson.fromJson(inReaderAddRepos,
                new TypeToken<List<SCCRepositoryJson>>() { }.getType());
        repositoriesChanged.addAll(additionalRepos);

        SCCCredentials sccCreds = CredentialsFactory.listSCCCredentials().get(0);

        ContentSyncManager csm = new ContentSyncManager();
        csm.updateSUSEProducts(productsChanged, staticTreeChanged, additionalRepos);
        csm.refreshRepositoriesAuthentication(repositoriesChanged, new SCCContentSyncSource(sccCreds), null);
        csm.linkAndRefreshContentSource(null);

        HibernateFactory.getSession().flush();
        HibernateFactory.getSession().clear();

        SCCRepository slesUpAwayRepo = SCCCachingFactory.lookupRepositoryBySccId(1632L).orElse(null);
        assertNull(slesUpAwayRepo);
        SCCRepository slesUpNewRepo = SCCCachingFactory.lookupRepositoryBySccId(-1632L).orElse(null);
        assertNotNull(slesUpNewRepo);
        assertEquals("https://updates.suse.com/SUSE/Updates/SLE-SERVER/12/x86_64/update_changed/",
                slesUpNewRepo.getUrl());
        SCCRepositoryAuth newRepoAuth = slesUpNewRepo.getBestAuth().get();
        assertEquals("https://updates.suse.com/SUSE/Updates/SLE-SERVER/12/x86_64/update_changed/?my-fake-token",
                newRepoAuth.getUrl());
        assertNotNull(newRepoAuth.getContentSource());

        SUSEProduct slesChanged = SUSEProductFactory.lookupByProductId(1117);
        slesChanged.getRepositories().stream()
            .filter(SUSEProductSCCRepository::isMandatory)
            .forEach(pr -> {
                assertNotNull(pr.getRepository());
                SCCRepositoryAuth bestAuth = pr.getRepository().getBestAuth().get();
                ContentSource cs = bestAuth.getContentSource();
                assertNotNull(cs);
                assertEquals(bestAuth.getUrl(), cs.getSourceUrl());
            });
    }

    /**
     * Test generation of channels for PTF repositories
     * @throws Exception if anything goes wrong
     */
    public void testUpdateChannelsWithPtfReposMainProducts() throws Exception {
        SUSEProductTestUtils.createVendorSUSEProductEnvironment(user,
                "/com/redhat/rhn/manager/content/test/data2", true);
        HibernateFactory.getSession().flush();
        HibernateFactory.getSession().clear();

        // SLES12 GA
        SUSEProductTestUtils.addChannelsForProduct(SUSEProductFactory.lookupByProductId(1117));
        HibernateFactory.getSession().flush();
        HibernateFactory.getSession().clear();

        SUSEProduct sles = SUSEProductFactory.lookupByProductId(1117);
        sles.getRepositories().stream()
            .peek(pr -> LOGGER.info("Repository {}", pr.getRepository().getName()))
            .filter(pr -> pr.isMandatory())
            .forEach(pr -> {
                assertNotNull(pr.getRepository());
                SCCRepositoryAuth bestAuth = pr.getRepository().getBestAuth().orElse(null);
                assertNotNull(bestAuth, "Best authorization is null for repository " + pr.getRepository().getName());
                ContentSource cs = bestAuth.getContentSource();
                assertNotNull(cs);
                assertEquals(bestAuth.getUrl(), cs.getSourceUrl());
            });
        ContentSyncManager csm = new ContentSyncManager();
        sles.getRepositories()
        .stream()
        .filter(pr -> pr.getRepository().getSccId().equals(9999L))
        .forEach(pr -> {
            try {
                csm.addChannel(pr.getChannelLabel(), null);
            }
            catch (ContentSyncException e) {
                throw new RuntimeException(e);
            }
        });
        SCCRepository slesUpRepo = SCCCachingFactory.lookupRepositoryBySccId(1632L).get();
        assertEquals("https://updates.suse.com/SUSE/Updates/SLE-SERVER/12/x86_64/update/",
                slesUpRepo.getUrl());
        assertEquals("https://updates.suse.com/SUSE/Updates/SLE-SERVER/12/x86_64/update/?my-fake-token",
                slesUpRepo.getBestAuth().get().getUrl());

        SUSEProduct slesChanged = SUSEProductFactory.lookupByProductId(1117);
        slesChanged.getRepositories().stream()
            .filter(pr -> pr.isMandatory())
            .forEach(pr -> {
                assertNotNull(pr.getRepository());
                SCCRepositoryAuth bestAuth = pr.getRepository().getBestAuth().get();
                ContentSource cs = bestAuth.getContentSource();
                assertNotNull(cs);
                assertEquals(bestAuth.getUrl(), cs.getSourceUrl());
            });

        SCCRepository ptfRepo = SCCCachingFactory.lookupRepositoryBySccId(9999L).orElse(null);
        assertNotNull(ptfRepo, "PTF repo not found");

        slesChanged.getRepositories().stream()
            .filter(pr -> !pr.isMandatory())
            .forEach(pr -> {
                assertNotNull(pr.getRepository());

                if (pr.getRepository().getSccId() == 9999L) {
                    // The PTF repo
                    assertEquals("a123456-sles-12-ptfs-x86_64", pr.getChannelLabel());
                    SCCRepositoryAuth bestAuth = pr.getRepository().getBestAuth().get();
                    ContentSource cs = bestAuth.getContentSource();
                    assertNotNull(cs);
                    assertEquals(bestAuth.getUrl(), cs.getSourceUrl());
                }
            });
    }

    /**
     * Test generation of channels for PTF repositories per module
     * @throws Exception if anything goes wrong
     */
    public void testUpdateChannelsWithPtfReposAllModules() throws Exception {
        SUSEProductTestUtils.createVendorSUSEProductEnvironment(user,
                "/com/redhat/rhn/manager/content/test/data3", true);
        HibernateFactory.getSession().flush();
        HibernateFactory.getSession().clear();

        // SLES15 SP2
        SUSEProduct rootSLES = SUSEProductFactory.lookupByProductId(1939L);
        SUSEProductTestUtils.addChannelsForProduct(rootSLES);
        // sle-module-basesystem 15 SP2 with PTF repos
        SUSEProductTestUtils.addChannelsForProductAndParent(SUSEProductFactory.lookupByProductId(1946L),
                rootSLES, true, Arrays.asList(15000L, 15001L));
        // sle-manager-tools 15 with PTF repos
        SUSEProductTestUtils.addChannelsForProductAndParent(SUSEProductFactory.lookupByProductId(1712L),
                rootSLES, true, Arrays.asList(15002L, 15003L));
        HibernateFactory.getSession().flush();
        HibernateFactory.getSession().clear();

        // Initialization complete

        SUSEProduct sles = SUSEProductFactory.lookupByProductId(1939L);
        sles.getRepositories().stream()
            .filter(pr -> pr.isMandatory())
            .forEach(pr -> {
                assertNotNull(pr.getRepository());
                SCCRepositoryAuth bestAuth = pr.getRepository().getBestAuth().get();
                ContentSource cs = bestAuth.getContentSource();
                assertNotNull(cs);
                assertEquals(bestAuth.getUrl(), cs.getSourceUrl());
            });

        SCCRepository ptfRepo = SCCCachingFactory.lookupRepositoryBySccId(15000L).orElse(null);
        assertNotNull(ptfRepo, "PTF repo not found");

        // test basesystem module PTFs for SLES root product
        SUSEProduct basesystem = SUSEProductFactory.lookupByProductId(1946L);
        List<SUSEProductSCCRepository> r = basesystem.getRepositories().stream()
            .filter(pr -> pr.getRootProduct().equals(sles))
            .filter(pr -> Arrays.asList(15000L, 15001L).contains(pr.getRepository().getSccId()))
            .collect(Collectors.toList());
        assertNotEmpty(r);
        r.forEach(pr -> {
                assertNotNull(pr.getRepository());
                // The PTF repo
                if (pr.getRepository().getSccId().equals(15000L)) {
                    assertEquals("a123456-sle-module-basesystem-15.2-ptfs-x86_64", pr.getChannelLabel());
                    assertEquals("A123456 sle-module-basesystem 15.2 PTFs x86_64", pr.getChannelName());
                }
                else {
                    fail("Unexpected repository " + pr);
                }
                SCCRepositoryAuth bestAuth = pr.getRepository().getBestAuth().get();
                ContentSource cs = bestAuth.getContentSource();
                assertNotNull(cs);
                assertEquals(bestAuth.getUrl(), cs.getSourceUrl());
            });
        // test basesystem module PTFs for SAP root product
        r = basesystem.getRepositories().stream()
            .filter(pr -> pr.getRootProduct().equals(SUSEProductFactory.lookupByProductId(1941L)))
            .filter(pr -> Arrays.asList(15000L, 15001L).contains(pr.getRepository().getSccId()))
            .collect(Collectors.toList());
        assertNotEmpty(r);
        r.forEach(pr -> {
            assertNotNull(pr.getRepository());
            // The PTF repo
            if (pr.getRepository().getSccId().equals(15000L)) {
                assertEquals("a123456-sle-module-basesystem-15.2-ptfs-x86_64-sap", pr.getChannelLabel());
                assertEquals("A123456 sle-module-basesystem 15.2 PTFs x86_64 SAP", pr.getChannelName());
            }
            else {
                fail("Unexpected repository " + pr);
            }
            SCCRepositoryAuth bestAuth = pr.getRepository().getBestAuth().get();
            ContentSource cs = bestAuth.getContentSource();
            assertNotNull(cs);
            assertEquals(bestAuth.getUrl(), cs.getSourceUrl());
        });
        // test tools ptf repositories for SLES
        SUSEProduct tools = SUSEProductFactory.lookupByProductId(1712L);
        r = tools.getRepositories().stream()
            .filter(pr -> pr.getRootProduct().equals(sles))
            .filter(pr -> Arrays.asList(15002L, 15003L).contains(pr.getRepository().getSccId()))
            .collect(Collectors.toList());
        assertNotEmpty(r);
        r.forEach(pr -> {
                assertNotNull(pr.getRepository());
                // The PTF repo
                if (pr.getRepository().getSccId().equals(15002L)) {
                    assertEquals("a123456-sle-manager-tools-15-ptfs-x86_64-sp2", pr.getChannelLabel());
                    assertEquals("A123456 sle-manager-tools 15 PTFs x86_64 SP2", pr.getChannelName());
                }
                else {
                    fail("Unexpected repository " + pr);
                }
                SCCRepositoryAuth bestAuth = pr.getRepository().getBestAuth().get();
                ContentSource cs = bestAuth.getContentSource();
                assertNotNull(cs);
                assertEquals(bestAuth.getUrl(), cs.getSourceUrl());
            });
        // test tools ptf repositories for SAP
        r = tools.getRepositories().stream()
            .filter(pr -> pr.getRootProduct().equals(SUSEProductFactory.lookupByProductId(1941L)))
            .filter(pr -> Arrays.asList(15002L, 15003L).contains(pr.getRepository().getSccId()))
            .collect(Collectors.toList());
        assertNotEmpty(r);
        r.forEach(pr -> {
                assertNotNull(pr.getRepository());
                // The PTF repo
                if (pr.getRepository().getSccId().equals(15002L)) {
                    assertEquals("a123456-sle-manager-tools-15-ptfs-x86_64-sap-sp2", pr.getChannelLabel());
                    assertEquals("A123456 sle-manager-tools 15 PTFs x86_64 SAP SP2", pr.getChannelName());
                }
                else {
                    fail("Unexpected repository " + pr);
                }
                SCCRepositoryAuth bestAuth = pr.getRepository().getBestAuth().get();
                ContentSource cs = bestAuth.getContentSource();
                assertNotNull(cs);
                assertEquals(bestAuth.getUrl(), cs.getSourceUrl());
            });
    }

    /**
     * Test 2 Credentials giving access to the same repo and switching "best auth"
     * @throws Exception if anything goes wrong
     */
    @Test
    public void testSwitchingBestAuthItem() throws Exception {
        SUSEProductTestUtils.createVendorSUSEProductEnvironment(user,
                "/com/redhat/rhn/manager/content/test/smallBase", true);
        HibernateFactory.getSession().flush();
        HibernateFactory.getSession().clear();

        // SLES15 GA
        SUSEProductTestUtils.addChannelsForProduct(SUSEProductFactory.lookupByProductId(1575));
        HibernateFactory.getSession().flush();
        HibernateFactory.getSession().clear();

        SUSEProduct sles = SUSEProductFactory.lookupByProductId(1575);
        sles.getRepositories().stream()
            .filter(SUSEProductSCCRepository::isMandatory)
            .forEach(pr -> {
                assertNotNull(pr.getRepository());
                SCCRepositoryAuth bestAuth = pr.getRepository().getBestAuth().get();
                ContentSource cs = bestAuth.getContentSource();
                assertNotNull(cs);
                assertEquals(bestAuth.getUrl(), cs.getSourceUrl());
            });

        for (SCCRepositoryAuth a : SCCCachingFactory.lookupRepositoryAuthWithContentSource()) {
            String csUrl = a.getContentSource().getSourceUrl();
            String repoUrl = a.getUrl();
            assertEquals(repoUrl, csUrl);
            assertTrue(a.tokenAuth().isPresent());
            assertEquals("my-fake-token", a.tokenAuth().get().getAuth());
        }
        MirrorCredentialsManager mgr = new MirrorCredentialsManager();
        SCCCredentials scc1st = CredentialsFactory.listSCCCredentials().get(0);
        SCCCredentials scc2nd = SUSEProductTestUtils.createSecondarySCCCredentials("dummy2", user);

        Gson gson = new GsonBuilder()
                .setDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSSX")
                .create();
        InputStreamReader inReaderProducts = new InputStreamReader(ContentSyncManager.class
                .getResourceAsStream("/com/redhat/rhn/manager/content/test/data1/productsUnscoped.json"));
        List<SCCProductJson> productsChanged = gson.fromJson(
                inReaderProducts, new TypeToken<List<SCCProductJson>>() { }.getType());
        InputStreamReader inReaderTree = new InputStreamReader(ContentSyncManager.class
                .getResourceAsStream("/com/redhat/rhn/manager/content/test/data1/product_tree.json"));
        List<ProductTreeEntry> staticTreeChanged = JsonParser.GSON.fromJson(
                inReaderTree, new TypeToken<List<ProductTreeEntry>>() { }.getType());
        InputStreamReader inReaderRepos = new InputStreamReader(ContentSyncManager.class
                .getResourceAsStream("/com/redhat/rhn/manager/content/test/data1/repositories.json"));
        List<SCCRepositoryJson> repositoriesChanged = gson.fromJson(
                inReaderRepos, new TypeToken<List<SCCRepositoryJson>>() { }.getType());

        ContentSyncManager csm = new ContentSyncManager();
        //csm.updateSUSEProducts(productsChanged, upgradePaths, staticTreeChanged, Collections.emptyList());
        csm.refreshRepositoriesAuthentication(repositoriesChanged, new SCCContentSyncSource(scc2nd), null);
        csm.linkAndRefreshContentSource(null);

        HibernateFactory.getSession().flush();
        HibernateFactory.getSession().clear();

        SUSEProduct slesChanged = SUSEProductFactory.lookupByProductId(1575);
        slesChanged.getRepositories().stream()
            .filter(SUSEProductSCCRepository::isMandatory)
            .forEach(pr -> {
                assertNotNull(pr.getRepository());
                SCCRepositoryAuth bestAuth = pr.getRepository().getBestAuth().get();
                ContentSource cs = bestAuth.getContentSource();
                assertNotNull(cs);
                assertEquals(bestAuth.getUrl(), cs.getSourceUrl());
            });

        // The 2nd credential is just secondary, we still should get tokens from the 1st
        for (SCCRepositoryAuth a : SCCCachingFactory.lookupRepositoryAuthWithContentSource()) {
            String csUrl = a.getContentSource().getSourceUrl();
            String repoUrl = a.getUrl();
            assertEquals(repoUrl, csUrl);
            assertTrue(a.tokenAuth().isPresent());
            assertEquals("my-fake-token", a.tokenAuth().get().getAuth());
        }

        // make 2nd the primary creds and expect token 2
        mgr.makePrimaryCredentials(scc2nd.getId());
        csm.linkAndRefreshContentSource(null);
        for (SCCRepositoryAuth a : SCCCachingFactory.lookupRepositoryAuthWithContentSource()) {
            String csUrl = a.getContentSource().getSourceUrl();
            String repoUrl = a.getUrl();
            assertEquals(repoUrl, csUrl);
            assertTrue(a.tokenAuth().isPresent());
            assertEquals("my-fake-token2", a.tokenAuth().get().getAuth());
        }

        // switch back to 1st credential as primary and test for 1st token
        mgr.makePrimaryCredentials(scc1st.getId());
        csm.linkAndRefreshContentSource(null);
        for (SCCRepositoryAuth a : SCCCachingFactory.lookupRepositoryAuthWithContentSource()) {
            String csUrl = a.getContentSource().getSourceUrl();
            String repoUrl = a.getUrl();
            assertEquals(repoUrl, csUrl);
            assertTrue(a.tokenAuth().isPresent());
            assertEquals("my-fake-token", a.tokenAuth().get().getAuth());
        }
    }

    @Test
    public void testCredentials() {
        SCCCredentials sccCredentials = SUSEProductTestUtils.createSCCCredentials("scccred", user);
        HibernateFactory.getSession().save(sccCredentials);

        CloudRMTCredentials rmtCredential = CredentialsFactory.createCloudRmtCredentials("rmtuser", "rmtpassword",
            "dummy");
        rmtCredential.setUser(user);
        HibernateFactory.getSession().save(rmtCredential);

        VHMCredentials vhmCredentials = CredentialsFactory.createVHMCredentials("vhmuser", "vhmpassword");
        vhmCredentials.setUser(user);
        HibernateFactory.getSession().save(vhmCredentials);

        List<SCCCredentials> sccCredentials1 = CredentialsFactory.listCredentialsByType(SCCCredentials.class);
        List<CloudRMTCredentials> rmtCredentials = CredentialsFactory.listCredentialsByType(CloudRMTCredentials.class);
        List<SCCCredentials> sccCredentials2 = CredentialsFactory.listSCCCredentials();
        List<Credentials> credentials = CredentialsFactory.listCredentials();
        System.out.println(sccCredentials1);
        System.out.println(sccCredentials2);
        System.out.println(rmtCredentials);
        System.out.println(vhmCredentials);
        System.out.println(credentials);
    }

    /**
     * Test 2 Credentials giving access to the same repo and switching "best auth"
     */
    @Test
    public void testSwitchFromCloudRmtToScc() {
        CloudRMTCredentials rmtCredentials = createCloudCredentials("dummy");
        SCCCredentials sccCredentials = createSccCredentials("dummy");

        // Create Repositories
        SCCRepository repoUpdates = createRepo(2705L, "SLE-Product-SLES15-Updates", "sle-15-x86_64");
        SCCRepository repoPool = createRepo(2707L, "SLE-Product-SLES15-Pool", "sle-15-x86_64");

        // Setup credentials access
        createRepoAuth(sccCredentials, repoUpdates);
        createRepoAuth(rmtCredentials, repoPool);

        HibernateFactory.getSession().flush();
        HibernateFactory.getSession().clear();

        assertAuthAndCredentials(repoUpdates.getSccId(), SCCRepositoryTokenAuth.class, SCCCredentials.class);
        assertAuthAndCredentials(repoPool.getSccId(), SCCRepositoryCloudRmtAuth.class, CloudRMTCredentials.class);

        // Add additional authentications for the repo
        createRepoAuth(rmtCredentials, repoUpdates);
        createRepoAuth(sccCredentials, repoPool);

        HibernateFactory.getSession().flush();
        HibernateFactory.getSession().clear();

        // Repo for Updates should switch to RMT credentials
        assertAuthAndCredentials(repoUpdates.getSccId(), SCCRepositoryCloudRmtAuth.class, CloudRMTCredentials.class);
        // Repo for Pool should be unchanged
        assertAuthAndCredentials(repoPool.getSccId(), SCCRepositoryCloudRmtAuth.class, CloudRMTCredentials.class);

    }

    private static void assertAuthAndCredentials(long sccId, Class<? extends SCCRepositoryAuth> authClass,
                                                 Class<? extends RemoteCredentials> credsClass) {
        Optional<SCCRepository> upRepoOpt = SCCCachingFactory.lookupRepositoryBySccId(sccId);
        assertTrue(upRepoOpt.isPresent(), "Repo not found");

        SCCRepositoryAuth bestAuth = upRepoOpt.flatMap(SCCRepository::getBestAuth).orElse(null);
        assertNotNull(bestAuth, "No best authentication");
        assertInstanceOf(authClass, bestAuth, "Best Auth is not " + authClass.getSimpleName());

        RemoteCredentials credentials = bestAuth.getOptionalCredentials().orElse(null);
        assertNotNull(credentials);
        assertInstanceOf(credsClass, credentials, "Credentials are not " + credsClass.getSimpleName());
    }

    /**
     * Test 2 Credentials giving access to the same repo and switching "best auth"
     */
    @Test
    public void testMultipleCloudRMTCredentials() {
        // Create Repositories
        SCCRepository sle15Pool = createRepo(2707L, "SLE-Product-SLES15-Pool", "sle-15-x86_64");
        SCCRepository sle15Updates = createRepo(2705L, "SLE-Product-SLES15-Updates", "sle-15-x86_64");

        SCCRepository sle12Pool = createRepo(2703L, "SLE-Product-SLES12-Pool", "sle-12-x86_64");
        SCCRepository sle12Updates = createRepo(2701L, "SLE-Product-SLES12-Updates", "sle-12-x86_64");

        // First RMT Credentials: gives access to SLE-Product-SLES15-Pool and SLE-Product-SLES15-Updates
        CloudRMTCredentials cloudCred1 = createCloudCredentials("cloudCred1");

        SCCRepositoryAuth authRepo1 = createRepoAuth(cloudCred1, sle15Pool);
        SCCRepositoryAuth authRepo2 = createRepoAuth(cloudCred1, sle15Updates);

        // Second RMT Credentials: gives access to SLE-Product-SLES12-Pool and SLE-Product-SLES12-Updates but it's
        // invalid
        CloudRMTCredentials cloudCred2 = createCloudCredentials("cloudCred2");
        cloudCred2.invalidate();
        CredentialsFactory.storeCredentials(cloudCred2);

        createRepoAuth(cloudCred2, sle12Pool);
        createRepoAuth(cloudCred2, sle12Updates);

        // Third RMT Credentials: gives access to SLE-Product-SLES15-Pool and SLE-Product-SLES12-Pool
        CloudRMTCredentials cloudCred3 = createCloudCredentials("cloudCred3");
        CredentialsFactory.storeCredentials(cloudCred3);

        createRepoAuth(cloudCred3, sle15Pool);
        SCCRepositoryAuth authRepo5 = createRepoAuth(cloudCred3, sle12Pool);

        // SCC Repository: gives access to SLE-Product-SLES15-Updates
        SCCCredentials sccCred1 = createSccCredentials("sccCred1");

        createRepoAuth(sccCred1, sle15Updates);
        SCCRepositoryAuth authRepo6 = createRepoAuth(sccCred1, sle12Updates);

        HibernateFactory.getSession().flush();
        HibernateFactory.getSession().clear();

        // Using assertAll to test all repo independently
        assertAll(
            () -> {
                // SLE-Product-SLES15-Pool -> should be cloud rmt 1
                Optional<SCCRepository> upRepoOpt = SCCCachingFactory.lookupRepositoryBySccId(2707L);
                assertTrue(upRepoOpt.isPresent(), "SLE-Product-SLES15-Pool Repo not found");
                assertTrue(upRepoOpt.get().getBestAuth().isPresent(), "No best auth found for SLE-Product-SLES15-Pool");

                SCCRepositoryAuth bestRepoAuth = upRepoOpt.get().getBestAuth().get();
                assertTrue(bestRepoAuth instanceof SCCRepositoryCloudRmtAuth,
                    "Best Auth is not cloud rmt auth for SLE-Product-SLES15-Pool");
                assertTrue(bestRepoAuth.getOptionalCredentials().isPresent(),
                    "No credentials for auth for SLE-Product-SLES15-Pool");
                Optional<CloudRMTCredentials> bestCred = bestRepoAuth.getOptionalCredentials()
                    .flatMap(rc -> rc.castAs(CloudRMTCredentials.class));
                assertTrue(bestCred.isPresent());
                assertEquals(
                    authRepo1.getOptionalCredentials()
                        .flatMap(c -> c.castAs(CloudRMTCredentials.class)).get().getUsername(),
                    bestRepoAuth.getOptionalCredentials()
                        .flatMap(c -> c.castAs(CloudRMTCredentials.class)).get().getUsername(),
                    "Wrong credentials for SLE-Product-SLES15-Pool"
                );
                assertEquals(authRepo1.getId(), bestRepoAuth.getId(),
                    "Auth Id do not match for SLE-Product-SLES15-Pool");
            },
            () -> {
                // SLE-Product-SLES15-Updates -> Should be rmt 1
                Optional<SCCRepository> upRepoOpt = SCCCachingFactory.lookupRepositoryBySccId(2705L);
                assertTrue(upRepoOpt.isPresent(), "SLE-Product-SLES15-Updates Repo not found");
                assertTrue(upRepoOpt.get().getBestAuth().isPresent(),
                    "No best auth found for SLE-Product-SLES15-Updates");

                SCCRepositoryAuth bestRepoAuth = upRepoOpt.get().getBestAuth().get();
                assertTrue(bestRepoAuth instanceof SCCRepositoryCloudRmtAuth,
                    "Best Auth is not cloud rmt auth for SLE-Product-SLES15-Updates");
                assertTrue(bestRepoAuth.getOptionalCredentials().isPresent(),
                    "No credentials for auth for SLE-Product-SLES15-Updates");
                RemoteCredentials bestCred = bestRepoAuth.getOptionalCredentials().get();
                assertTrue(bestCred.castAs(CloudRMTCredentials.class).isPresent());
                assertEquals(authRepo2.getOptionalCredentials()
                        .flatMap(c -> c.castAs(CloudRMTCredentials.class)).get().getUsername(),
                    bestRepoAuth.getOptionalCredentials()
                        .flatMap(c -> c.castAs(CloudRMTCredentials.class)).get().getUsername(),
                    "Wrong credentials for SLE-Product-SLES15-Updates");
                assertEquals(authRepo2.getId(), bestRepoAuth.getId(),
                    "Auth Id do not match for SLE-Product-SLES15-Updates");
            },
            () -> {
                // SLE-Product-SLES12-Pool -> Should be cloud rmt 3
                Optional<SCCRepository> upRepoOpt = SCCCachingFactory.lookupRepositoryBySccId(2703L);
                assertTrue(upRepoOpt.isPresent(), "SLE-Product-SLES12-Pool Repo not found");
                assertTrue(upRepoOpt.get().getBestAuth().isPresent(), "No best auth found for SLE-Product-SLES12-Pool");

                SCCRepositoryAuth bestRepoAuth = upRepoOpt.get().getBestAuth().get();
                assertTrue(bestRepoAuth instanceof SCCRepositoryCloudRmtAuth,
                    "Best Auth is not cloud rmt auth for SLE-Product-SLES12-Pool");
                assertTrue(bestRepoAuth.getOptionalCredentials().isPresent(),
                    "No credentials for auth for SLE-Product-SLES12-Pool");
                RemoteCredentials bestCred = bestRepoAuth.getOptionalCredentials().get();
                assertTrue(bestCred.castAs(CloudRMTCredentials.class).isPresent());
                assertEquals(authRepo5.getOptionalCredentials()
                        .flatMap(c -> c.castAs(CloudRMTCredentials.class)).get().getUsername(),
                    bestRepoAuth.getOptionalCredentials()
                        .flatMap(c -> c.castAs(CloudRMTCredentials.class)).get().getUsername(),
                    "Wrong credentials for SLE-Product-SLES12-Pool");
                assertEquals(authRepo5.getId(), bestRepoAuth.getId(),
                    "Auth Id do not match for SLE-Product-SLES12-Pool");
            },
            () -> {
                // SLE-Product-SLES12-Updated -> Should be scc
                Optional<SCCRepository> upRepoOpt = SCCCachingFactory.lookupRepositoryBySccId(2701L);
                assertTrue(upRepoOpt.isPresent(), "SLE-Product-SLES12-Update Repo not found");
                assertTrue(upRepoOpt.get().getBestAuth().isPresent(),
                    "No best auth found for SLE-Product-SLES12-Update");

                SCCRepositoryAuth bestRepoAuth = upRepoOpt.get().getBestAuth().get();
                assertTrue(bestRepoAuth instanceof SCCRepositoryTokenAuth,
                    "Best Auth is not token auth for SLE-Product-SLES12-Update");
                assertTrue(bestRepoAuth.getOptionalCredentials().isPresent(),
                    "No credentials for auth for SLE-Product-SLES12-Update");
                RemoteCredentials bestCred = bestRepoAuth.getOptionalCredentials().get();
                assertTrue(bestCred.castAs(SCCCredentials.class).isPresent());
                assertEquals(authRepo6.getOptionalCredentials()
                        .flatMap(c -> c.castAs(SCCCredentials.class)).get().getUsername(),
                    bestRepoAuth.getOptionalCredentials()
                        .flatMap(c -> c.castAs(SCCCredentials.class)).get().getUsername(),
                    "Wrong credentials for SLE-Product-SLES12-Update");
                assertEquals(authRepo6.getId(), bestRepoAuth.getId(),
                    "Auth Id do not match for SLE-Product-SLES12-Update");
            }
        );
    }


    private static SCCRepositoryAuth createRepoAuth(RemoteCredentials cred, SCCRepository repo) {
        SCCRepositoryAuth authRepo;
        if (cred.isTypeOf(CloudRMTCredentials.class)) {
            authRepo = new SCCRepositoryCloudRmtAuth();
        }
        else if (cred.isTypeOf(SCCCredentials.class)) {
            authRepo = new SCCRepositoryTokenAuth();
        }
        else {
            authRepo = new SCCRepositoryBasicAuth();
        }

        authRepo.setRepo(repo);
        authRepo.setCredentials(cred);
        SCCCachingFactory.saveRepositoryAuth(authRepo);
        return authRepo;
    }

    private SCCCredentials createSccCredentials(String username) {
        SCCCredentials sccCred1 = CredentialsFactory.createSCCCredentials(username, "dummy");
        sccCred1.setUrl("dummy");
        sccCred1.setUser(user);
        CredentialsFactory.storeCredentials(sccCred1);
        return sccCred1;
    }

    private CloudRMTCredentials createCloudCredentials(String username) {
        CloudRMTCredentials cloudCred1 = CredentialsFactory.createCloudRmtCredentials(username, "dummy", "dummy");
        cloudCred1.setUser(user);
        CredentialsFactory.storeCredentials(cloudCred1);
        return cloudCred1;
    }

    private static SCCRepository createRepo(long sccId, String name, String target) {
        SCCRepository repository = new SCCRepository();
        repository.setAutorefresh(false);
        repository.setDescription(name + " for " + target);
        repository.setName(name);
        repository.setDistroTarget(target);
        repository.setSccId(sccId);
        repository.setUrl("https://updates.suse.com/dummy/" + name);
        SCCCachingFactory.saveRepository(repository);
        return repository;
    }

    /**
     * Test for {@link ContentSyncManager#updateSUSEProducts} inserting a new product.
     * @throws Exception if anything goes wrong
     */
    @Test
    public void testUpdateSUSEProductsNew() throws Exception {
        // Create test product attributes
        int productId = 12345;
        assertNull(SUSEProductFactory.lookupByProductId(productId));
        String name = TestUtils.randomString();
        String identifier = TestUtils.randomString();
        String version = TestUtils.randomString();
        String releaseType = TestUtils.randomString();
        String friendlyName = TestUtils.randomString();
        String productClass = TestUtils.randomString();

        // Setup a product as it comes from SCC
        SCCProductJson p = new SCCProductJson(productId, name, identifier, version, releaseType, "i686",
                friendlyName, productClass, ReleaseStage.released, "", false, "", "",
                Collections.emptyList(), Collections.emptyList(), Collections.emptyList(), Collections.emptyList(),
                null, false);
        List<SCCProductJson> products = new ArrayList<>();
        products.add(p);

        // Call updateSUSEProducts()
        ContentSyncManager csm = new ContentSyncManager();
        csm.setSumaProductTreeJson(Optional.of(new File("/usr/share/susemanager/scc/product_tree.json")));
        csm.updateSUSEProducts(products);

        // Verify that a new product has been created correctly
        SUSEProduct suseProduct = SUSEProductFactory.lookupByProductId(productId);
        assertEquals(identifier.toLowerCase(), suseProduct.getName());
        assertEquals(version.toLowerCase(), suseProduct.getVersion());
        assertEquals(releaseType.toLowerCase(), suseProduct.getRelease());
        assertEquals(friendlyName, suseProduct.getFriendlyName());
        assertEquals(PackageFactory.lookupPackageArchByLabel("i686"),
                suseProduct.getArch());
    }

    /**
     * Test for {@link ContentSyncManager#updateSUSEProducts} update a product.
     * @throws Exception if anything goes wrong
     */
    @Test
    public void testUpdateSUSEProductsUpdate() throws Exception {
        // Create test product attributes
        int productId = 12345;
        assertNull(SUSEProductFactory.lookupByProductId(productId));
        String name = TestUtils.randomString().toLowerCase();
        String version = TestUtils.randomString().toLowerCase();
        String releaseType = TestUtils.randomString().toLowerCase();
        String friendlyName = TestUtils.randomString();

        // Store a SUSE product with those attributes
        SUSEProduct suseProduct = new SUSEProduct();
        suseProduct.setName(name);
        suseProduct.setVersion(version);
        suseProduct.setRelease(releaseType);
        suseProduct.setFriendlyName(friendlyName);
        suseProduct.setProductId(productId);
        suseProduct.setReleaseStage(ReleaseStage.released);
        PackageArch arch = PackageFactory.lookupPackageArchByLabel("i686");
        suseProduct.setArch(arch);
        SUSEProductFactory.save(suseProduct);

        String productClass = TestUtils.randomString();
        // Setup SCC product accordingly
        SCCProductJson p = new SCCProductJson(productId, null, name, version, releaseType, "i686",
                friendlyName, productClass, ReleaseStage.released, "", false, "", "",
                Collections.emptyList(), Collections.emptyList(), Collections.emptyList(), Collections.emptyList(),
                null, false);

        // Set a new friendly name that should be updated
        String friendlyNameNew = TestUtils.randomString();
        p = p.copy().setFriendlyName(friendlyNameNew).build();
        List<SCCProductJson> products = new ArrayList<>();
        products.add(p);

        // Call updateSUSEProducts()
        ContentSyncManager csm = new ContentSyncManager();
        csm.setSumaProductTreeJson(Optional.of(new File("/usr/share/susemanager/scc/product_tree.json")));
        csm.updateSUSEProducts(products);

        // Verify that the product has been updated correctly
        suseProduct = SUSEProductFactory.lookupByProductId(productId);
        assertEquals(friendlyNameNew, suseProduct.getFriendlyName());
    }

    /**
     * Test for {@link ContentSyncManager#getAvailableChannels}.
     * @throws Exception if anything goes wrong
     */
    @Test
    public void testGetAvailableChannels() throws Exception {
        SUSEProductTestUtils.createVendorSUSEProductEnvironment(
                user, "/com/redhat/rhn/manager/content/test/smallBase", true);
        HibernateFactory.getSession().flush();
        HibernateFactory.getSession().clear();

        ContentSyncManager csm = new ContentSyncManager();
        List<SUSEProductSCCRepository> availableChannels = csm.getAvailableChannels();

        List<String> avChanLanbels = availableChannels
                .stream().map(SUSEProductSCCRepository::getChannelLabel).collect(Collectors.toList());

        assertContains(avChanLanbels, "sles12-pool-x86_64");
        assertContains(avChanLanbels, "sle-12-cloud-compute5-updates-x86_64");
        assertContains(avChanLanbels, "sles12-ltss-updates-x86_64");
        assertContains(avChanLanbels, "sle-ha-geo12-debuginfo-pool-x86_64");
        assertContains(avChanLanbels, "sle-we12-updates-x86_64");
        // Installer Updates is optional and not in repositories.json
        assertFalse(avChanLanbels.contains("sles12-installer-updates-x86_64"), "unexpected optional channel found");
        // Storage 2 is not in repositories.json to emulate no subscription
        assertFalse(avChanLanbels.contains("suse-enterprise-storage-2-updates-x86_64"),
                "Storage should not be avaliable");
    }

    /**
     * Test for duplicates in {@link ContentSyncManager#getAvailableChannels} output.
     * @throws Exception if anything goes wrong
     */
    @Test
    public void testNoDupInGetAvailableChannels() throws Exception {
        SUSEProductTestUtils.createVendorSUSEProductEnvironment(user, "/com/redhat/rhn/manager/content/test/", true);
        HibernateFactory.getSession().flush();
        HibernateFactory.getSession().clear();

        ContentSyncManager csm = new ContentSyncManager();
        List<SUSEProductSCCRepository> availableChannels = csm.getAvailableChannels();

        List<String> duplicates = new LinkedList<>();
        availableChannels.stream()
                .map(SUSEProductSCCRepository::getChannelLabel)
                .collect(Collectors.groupingBy(Function.identity(), Collectors.counting()))
                .entrySet().stream().forEach(e -> {
                    if (e.getValue() > 1) {
                        duplicates.add(e.getKey());
                    }
                });
        List<String> dupExceptions = Arrays.asList(
                "sle11-sdk-sp1-pool-i586",
                "sle11-sdk-sp1-pool-i586-vmware",
                "sle11-sdk-sp1-pool-ia64",
                "sle11-sdk-sp1-pool-ppc64",
                "sle11-sdk-sp1-pool-s390x",
                "sle11-sdk-sp1-pool-x86_64",
                "sle11-sdk-sp1-pool-x86_64-vmware",
                "sle11-sdk-sp1-updates-i586",
                "sle11-sdk-sp1-updates-i586-vmware",
                "sle11-sdk-sp1-updates-ia64",
                "sle11-sdk-sp1-updates-ppc64",
                "sle11-sdk-sp1-updates-s390x",
                "sle11-sdk-sp1-updates-x86_64",
                "sle11-sdk-sp1-updates-x86_64-vmware",
                "sle11-sp1-debuginfo-pool-i586",
                "sle11-sp1-debuginfo-pool-i586-vmware",
                "sle11-sp1-debuginfo-pool-ia64",
                "sle11-sp1-debuginfo-pool-ppc64",
                "sle11-sp1-debuginfo-pool-s390x",
                "sle11-sp1-debuginfo-pool-x86_64",
                "sle11-sp1-debuginfo-pool-x86_64-vmware",
                "sle11-sp1-debuginfo-updates-i586",
                "sle11-sp1-debuginfo-updates-i586-vmware",
                "sle11-sp1-debuginfo-updates-ia64",
                "sle11-sp1-debuginfo-updates-ppc64",
                "sle11-sp1-debuginfo-updates-s390x",
                "sle11-sp1-debuginfo-updates-x86_64",
                "sle11-sp1-debuginfo-updates-x86_64-vmware",
                "sle-15-ga-desktop-nvidia-driver",
                "sles11-extras-i586",
                "sles11-extras-i586-vmware",
                "sles11-extras-ia64",
                "sles11-extras-ppc64",
                "sles11-extras-s390x",
                "sles11-extras-x86_64",
                "sles11-extras-x86_64-vmware",
                "sles11-sp1-pool-i586",
                "sles11-sp1-pool-ia64",
                "sles11-sp1-pool-ppc64",
                "sles11-sp1-pool-s390x",
                "sles11-sp1-pool-x86_64",
                "sles11-sp1-updates-i586",
                "sles11-sp1-updates-ia64",
                "sles11-sp1-updates-ppc64",
                "sles11-sp1-updates-s390x",
                "sles11-sp1-updates-x86_64",
                "sles11-sp1-vmware-pool-i586",
                "sles11-sp1-vmware-pool-x86_64",
                "sles11-sp1-vmware-updates-i586",
                "sles11-sp1-vmware-updates-x86_64",
                "suse-caasp-all-debuginfo-pool-x86_64",
                "suse-caasp-all-debuginfo-updates-x86_64",
                "suse-caasp-all-pool-x86_64",
                "suse-caasp-all-updates-x86_64");
        duplicates.removeAll(dupExceptions);
        assertTrue(duplicates.isEmpty(),
                duplicates.size() + " Duplicate labels found: " + String.join("\n", duplicates));
    }

    /**
     * Test for {@link ContentSyncManager#updateChannelFamilies} method, insert case.
     */
    @Test
    public void testUpdateChannelFamiliesInsert() {
        // Get test data and insert
        List<ChannelFamilyJson> channelFamilies = getChannelFamilies();
        ContentSyncManager csm = new ContentSyncManager();
        csm.updateChannelFamilies(channelFamilies);

        // Assert that families have been inserted correctly
        for (ChannelFamilyJson cf : channelFamilies) {
            ChannelFamily family = ChannelFamilyFactory.lookupByLabel(
                    cf.getLabel(), null);
            assertNotNull(family);
            assertEquals(cf.getLabel(), family.getLabel());
            assertEquals(cf.getName(), family.getName());
            assertNotNull(family.getPublicChannelFamily());

            // Check for ALPHA and BETA families
            String label = cf.getLabel() + "-ALPHA";
            String name = cf.getName() + " (ALPHA)";
            family = ChannelFamilyFactory.lookupByLabel(label, null);
            assertNotNull(family);
            assertEquals(label, family.getLabel());
            assertEquals(name, family.getName());
            assertNotNull(family.getPublicChannelFamily());

            label = cf.getLabel() + "-BETA";
            name = cf.getName() + " (BETA)";
            family = ChannelFamilyFactory.lookupByLabel(label, null);
            assertNotNull(family);
            assertEquals(label, family.getLabel());
            assertEquals(name, family.getName());
            assertNotNull(family.getPublicChannelFamily());
        }
    }

    /**
     * Test for {@link ContentSyncManager#updateChannelFamilies} method, update case.
     */
    @Test
    public void testUpdateChannelFamiliesUpdate() {
        // Get test data and insert
        int familynumbers = ChannelFamilyFactory.getAllChannelFamilies().size();

        List<ChannelFamilyJson> channelFamilies = getChannelFamilies();
        ContentSyncManager csm = new ContentSyncManager();
        csm.updateChannelFamilies(channelFamilies);

        int newFamilyNumbers = ChannelFamilyFactory.getAllChannelFamilies().size();
        // 2 families added with suffix none, alpha and beta
        assertEquals(familynumbers + (3 * 2), newFamilyNumbers);

        // Change the name
        for (ChannelFamilyJson cf : channelFamilies) {
            cf.setName(TestUtils.randomString());
        }

        // Update again
        csm.updateChannelFamilies(channelFamilies);

        newFamilyNumbers = ChannelFamilyFactory.getAllChannelFamilies().size();
        assertEquals(familynumbers + (3 * 2), newFamilyNumbers);

        // Assert everything is as expected
        for (ChannelFamilyJson cf : channelFamilies) {
            ChannelFamily family = ChannelFamilyFactory.lookupByLabel(
                    cf.getLabel(), null);
            assertNotNull(family);
            assertEquals(cf.getLabel(), family.getLabel());
            assertEquals(cf.getName(), family.getName());
            assertNotNull(family.getPublicChannelFamily());

            // Check for ALPHA and BETA families
            String label = cf.getLabel() + "-ALPHA";
            String name = cf.getName() + " (ALPHA)";
            family = ChannelFamilyFactory.lookupByLabel(label, null);
            assertNotNull(family);
            assertEquals(label, family.getLabel());
            assertEquals(name, family.getName());
            assertNotNull(family.getPublicChannelFamily());

            label = cf.getLabel() + "-BETA";
            name = cf.getName() + " (BETA)";
            family = ChannelFamilyFactory.lookupByLabel(label, null);
            assertNotNull(family);
            assertEquals(label, family.getLabel());
            assertEquals(name, family.getName());
            assertNotNull(family.getPublicChannelFamily());
        }
    }

    /**
     * Update the upgrade paths test.
     * @throws Exception if anything goes wrong
     */
    @Test
    public void testUpdateUpgradePaths() throws Exception {
        // Prepare products since they will be looked up
        ChannelFamily family = ChannelFamilyFactoryTest.createTestChannelFamily();
        SUSEProduct p;

        List<SCCProductJson> products = new ArrayList<>();
        int productId = 10012345;
        assertNull(SUSEProductFactory.lookupByProductId(productId));
        String name = TestUtils.randomString();
        String identifier = TestUtils.randomString();
        String version = TestUtils.randomString();
        String releaseType = TestUtils.randomString();
        String friendlyName = TestUtils.randomString();
        String productClass = TestUtils.randomString();

        // Setup a product as it comes from SCC
        SCCProductJson prd = new SCCProductJson(productId, name, identifier, version, releaseType, "i686",
                friendlyName, productClass, ReleaseStage.released, "", false, "", "",
                Collections.emptyList(), Collections.emptyList(), Collections.emptyList(), Collections.emptyList(),
                null, false);
        products.add(prd);

        productId = 10012346;
        assertNull(SUSEProductFactory.lookupByProductId(productId));
        name = TestUtils.randomString();
        identifier = TestUtils.randomString();
        version = TestUtils.randomString();
        releaseType = TestUtils.randomString();
        friendlyName = TestUtils.randomString();
        productClass = TestUtils.randomString();

        // Setup a 2nd product as it comes from SCC
        SCCProductJson prd2 = new SCCProductJson(productId, name, identifier, version, releaseType, "i686",
                friendlyName, productClass, ReleaseStage.released, "", false, "", "",
                Collections.emptyList(), Collections.emptyList(),
                Collections.singletonList(10012345L), Collections.emptyList(),
                null, false);
        products.add(prd2);

        if (SUSEProductFactory.lookupByProductId(10012345) == null) {
            p = SUSEProductTestUtils.createTestSUSEProduct(family);
            p.setProductId(10012345);
            TestUtils.saveAndFlush(p);
        }
        if (SUSEProductFactory.lookupByProductId(10012346) == null) {
            p = SUSEProductTestUtils.createTestSUSEProduct(family);
            p.setProductId(10012346);
            TestUtils.saveAndFlush(p);
        }

        // Update the upgrade paths
        ContentSyncManager csm = new ContentSyncManager();
        csm.updateUpgradePaths(products);

        // Check the results
        SUSEProduct p10012345 = SUSEProductFactory.lookupByProductId(10012345);
        SUSEProduct p10012346 = SUSEProductFactory.lookupByProductId(10012346);

        assertContains(p10012345.getUpgrades(), p10012346);
    }

    /**
     * There is an upgrade path in the DB and SCC deletes the "from" product.
     * @throws Exception if anything goes wrong
     */
    @Test
    public void testUpgradePathPredecessorDeleted() throws Exception {
        List<SCCProductJson> products = new ArrayList<>();

        // Setup a product as it comes from SCC
        long product1Id = 10012345;
        assertNull(SUSEProductFactory.lookupByProductId(product1Id));
        String name = TestUtils.randomString();
        String identifier = TestUtils.randomString();
        String version = TestUtils.randomString();
        String releaseType = TestUtils.randomString();
        String friendlyName = TestUtils.randomString();
        String productClass = TestUtils.randomString();

        SCCProductJson product1 = new SCCProductJson(product1Id, name, identifier, version, releaseType, "i686",
                friendlyName, productClass, ReleaseStage.released, "", false, "", "",
                Collections.emptyList(), Collections.emptyList(), Collections.emptyList(), Collections.emptyList(),
                null, false);
        products.add(product1);

        // Setup a 2nd product as it comes from SCC
        int product2Id = 10012346;
        assertNull(SUSEProductFactory.lookupByProductId(product2Id));
        name = TestUtils.randomString();
        identifier = TestUtils.randomString();
        version = TestUtils.randomString();
        releaseType = TestUtils.randomString();
        friendlyName = TestUtils.randomString();
        productClass = TestUtils.randomString();

        SCCProductJson product2 = new SCCProductJson(product2Id, name, identifier, version, releaseType, "i686",
                friendlyName, productClass, ReleaseStage.released, "", false, "", "",
                Collections.emptyList(), Collections.emptyList(),
                Collections.singletonList(product1Id), Collections.emptyList(),
                null, false);
        products.add(product2);

        // Update SUSE products and upgrade paths
        ContentSyncManager csm = new ContentSyncManager();
        csm.setSumaProductTreeJson(Optional.of(new File("/usr/share/susemanager/scc/product_tree.json")));
        csm.updateSUSEProducts(products);
        HibernateFactory.getSession().flush();

        // There should be an upgrade path from product1 to product2
        assertEquals(1, SUSEProductFactory.lookupByProductId(product1Id).getUpgrades().size());

        // Remove the first product
        products.remove(product1);
        csm.updateSUSEProducts(Collections.singletonList(
                product2.copy().setOnlinePredecessorIds(Collections.emptyList()).build()));
        HibernateFactory.getSession().flush();

        // There should be no upgrade paths
        assertTrue(SUSEProductFactory.lookupByProductId(product1Id).getUpgrades().isEmpty());
    }

    /**
     * An upgrade path between two products is removed while the products still exist.
     * @throws Exception if anything goes wrong
     */
    @Test
    public void testUpgradePathRemoved() throws Exception {
        List<SCCProductJson> products = new ArrayList<>();

        // Setup a product as it comes from SCC
        long product1Id = 10012345;
        assertNull(SUSEProductFactory.lookupByProductId(product1Id));
        String name = TestUtils.randomString();
        String identifier = TestUtils.randomString();
        String version = TestUtils.randomString();
        String releaseType = TestUtils.randomString();
        String friendlyName = TestUtils.randomString();
        String productClass = TestUtils.randomString();

        SCCProductJson product1 = new SCCProductJson(product1Id, name, identifier, version, releaseType, "i686",
                friendlyName, productClass, ReleaseStage.released, "", false, "", "",
                Collections.emptyList(), Collections.emptyList(), Collections.emptyList(), Collections.emptyList(),
                null, false);
        products.add(product1);

        // Setup a 2nd product as it comes from SCC
        int product2Id = 10012346;
        assertNull(SUSEProductFactory.lookupByProductId(product2Id));
        name = TestUtils.randomString();
        identifier = TestUtils.randomString();
        version = TestUtils.randomString();
        releaseType = TestUtils.randomString();
        friendlyName = TestUtils.randomString();
        productClass = TestUtils.randomString();

        SCCProductJson product2 = new SCCProductJson(product2Id, name, identifier, version, releaseType, "i686",
                friendlyName, productClass, ReleaseStage.released, "", false, "", "",
                Collections.emptyList(), Collections.emptyList(),
                Collections.singletonList(product1Id), Collections.emptyList(),
                null, false);
        products.add(product2);

        // Update SUSE products and upgrade paths
        ContentSyncManager csm = new ContentSyncManager();
        csm.setSumaProductTreeJson(Optional.of(new File("/usr/share/susemanager/scc/product_tree.json")));
        csm.updateSUSEProducts(products);

        // There should be an upgrade path from product1 to product2
        assertEquals(1, SUSEProductFactory.lookupByProductId(product1Id).getUpgrades().size());

        // Remove the upgrade path via the predecessor Id
        csm.updateSUSEProducts(Stream.of(product1, product2.copy().setOnlinePredecessorIds(Collections.emptyList())
                .build()).collect(Collectors.toList()));

        // There should be no upgrade paths
        assertTrue(SUSEProductFactory.lookupByProductId(product1Id).getUpgrades().isEmpty());
    }

    /**
     * Test for {@link ContentSyncManager#listChannels}.
     * @throws Exception if anything goes wrong
     */
    @Test
    public void testListChannels() throws Exception {
        SUSEProductTestUtils.createVendorSUSEProductEnvironment(
                user, "/com/redhat/rhn/manager/content/test/smallBase", true);
        HibernateFactory.getSession().flush();
        HibernateFactory.getSession().clear();

        // SLES12 GA
        SUSEProductTestUtils.addChannelsForProduct(SUSEProductFactory.lookupByProductId(1117));
        HibernateFactory.getSession().flush();
        HibernateFactory.getSession().clear();

        // List channels and verify status
        ContentSyncManager csm = new ContentSyncManager();
        List<MgrSyncChannelDto> channels = csm.listChannels();
        boolean foundPool = false;
        boolean foundDebugPool = false;
        for (MgrSyncChannelDto c : channels) {
            if (c.getLabel().equals("sles12-pool-x86_64")) {
                assertEquals("https://updates.suse.com/SUSE/Products/SLE-SERVER/12/x86_64/product/", c.getSourceUrl());
                assertEquals(MgrSyncStatus.INSTALLED, c.getStatus());
                foundPool = true;
            }
            else if (c.getLabel().equals("sles12-debuginfo-pool-x86_64")) {
                assertEquals("https://updates.suse.com/SUSE/Products/SLE-SERVER/12/x86_64/product_debug/",
                        c.getSourceUrl());
                assertEquals(MgrSyncStatus.AVAILABLE, c.getStatus());
                foundDebugPool = true;
            }
            else if (c.getLabel().equals("sles12-installer-updates-x86_64")) {
                fail("Unexpected Installer Update channel found");
            }
            else if (c.getLabel().startsWith("suse-enterprise-storage")) {
                fail("Storage Channels should not be listed");
            }
        }
        assertTrue(foundPool, "Pool channel not found");
        assertTrue(foundDebugPool, "Debuginfo Pool channel not found");
        Map<MgrSyncStatus, List<MgrSyncChannelDto>> collect = channels.stream()
                .collect(Collectors.groupingBy(MgrSyncChannelDto::getStatus));
        assertEquals(2, collect.get(MgrSyncStatus.INSTALLED).size());
        assertEquals(113, collect.get(MgrSyncStatus.AVAILABLE).size());
    }

    /**
     * Tests {@link ContentSyncManager#listProducts}, in particular the
     * filtering of unavailable products.
     * @throws Exception if anything goes wrong
     */
    @Test
    public void testListProductsAvailability() throws Exception {
        SUSEProductTestUtils.createVendorSUSEProductEnvironment(
                user, "/com/redhat/rhn/manager/content/test/smallBase", true);
        HibernateFactory.getSession().flush();
        HibernateFactory.getSession().clear();

        // SLES12 GA
        SUSEProductTestUtils.addChannelsForProduct(SUSEProductFactory.lookupByProductId(1117));
        HibernateFactory.getSession().flush();
        HibernateFactory.getSession().clear();

        ContentSyncManager csm = new ContentSyncManager();
        Collection<MgrSyncProductDto> products = csm.listProducts();

        boolean foundSLES = false;
        boolean foundHAGEO = false;
        for (MgrSyncProductDto product : products) {
            if (product.getFriendlyName().equals("SUSE Linux Enterprise Server 12 x86_64")) {
                assertEquals(MgrSyncStatus.INSTALLED, product.getStatus());
                assertFalse(
                        product.getChannels().stream()
                                .anyMatch(c -> c.getLabel().equals("sles12-installer-updates-x86_64")),
                        "Unexpected Installer Update Channel found");
                foundSLES = true;

                for (MgrSyncProductDto ext : product.getExtensions()) {
                    if (ext.getFriendlyName()
                            .equals("SUSE Linux Enterprise High Availability GEO Extension 12 x86_64")) {
                        assertEquals(MgrSyncStatus.AVAILABLE, ext.getStatus());
                        foundHAGEO = true;
                    }
                }
            }
        }
        assertTrue(foundSLES, "SLES not found");
        assertTrue(foundHAGEO, "HA-GEO not found");
    }

    /**
     * Tests {@link ContentSyncManager#isRefreshNeeded}
     * @throws Exception if anything goes wrong
     */
    @Test
    public void testIsRefreshNeeded() throws Exception {
        SUSEProductTestUtils.createVendorSUSEProductEnvironment(
                user, "/com/redhat/rhn/manager/content/test/smallBase", true);
        Config.get().remove(ContentSyncManager.RESOURCE_PATH);
        HibernateFactory.getSession().flush();
        HibernateFactory.getSession().clear();

        // SLES12 GA
        SUSEProductTestUtils.addChannelsForProduct(SUSEProductFactory.lookupByProductId(1117));
        HibernateFactory.getSession().flush();
        HibernateFactory.getSession().clear();

        ContentSyncManager csm = new ContentSyncManager() {
            @Override
            protected boolean accessibleUrl(String url) {
                return true;
            }

            @Override
            protected boolean accessibleUrl(String url, String user, String password) {
                return true;
            }
        };

        assertFalse(csm.isRefreshNeeded(null));
        assertTrue(csm.isRefreshNeeded("https://mirror.example.com/"));

        // if mgr-sync never ran, return true
        WriteMode clear = ModeFactory.getWriteMode("test_queries", "delete_last_mgr_sync_refresh");
        clear.executeUpdate(new HashMap<>());
        assertTrue(csm.isRefreshNeeded(null));
    }

    /**
     * Tests {@link ContentSyncManager#isRefreshNeeded} when fromdir is configured
     * @throws Exception if anything goes wrong
     */
    @Test
    public void testIsRefreshNeededFromDir() throws Exception {
        SUSEProductTestUtils.createVendorSUSEProductEnvironment(
                user, "/com/redhat/rhn/manager/content/test/smallBase", true, true);
        HibernateFactory.getSession().flush();
        HibernateFactory.getSession().clear();

        // SLES12 GA
        SUSEProductTestUtils.addChannelsForProduct(SUSEProductFactory.lookupByProductId(1117));
        HibernateFactory.getSession().flush();
        HibernateFactory.getSession().clear();

        ContentSyncManager csm = new ContentSyncManager() {
            @Override
            protected boolean accessibleUrl(String url) {
                return true;
            }

            @Override
            protected boolean accessibleUrl(String url, String user, String password) {
                return true;
            }
        };

        assertFalse(csm.isRefreshNeeded(null));

        // different mirror has no effect as you cannot overwrite fromdir option
        assertFalse(csm.isRefreshNeeded("https://mirror.example.com/"));

        ManagerInfoFactory.setLastMgrSyncRefresh(System.currentTimeMillis() - (48 * 60 * 60 * 1000));
        assertTrue(csm.isRefreshNeeded(null));
    }

    /**
     * Tests {@link ContentSyncManager#isRefreshNeeded} when neither scc creds nor fromdir is configured
     * @throws Exception if anything goes wrong
     */
    @Test
    public void testIsRefreshNeededNothingConfigured() throws Exception {
        ContentSyncManager csm = new ContentSyncManager();
        assertFalse(csm.isRefreshNeeded(null));
    }

    @Test
    public void testIsRefreshNeededPAYG() {
        for (SCCCredentials c : CredentialsFactory.listSCCCredentials()) {
            CredentialsFactory.removeCredentials(c);
        }
        ManagerInfoFactory.setLastMgrSyncRefresh(0);
        CloudPaygManager mgr = new TestCloudPaygManagerBuilder()
            .withPaygInstance()
            .build();
        ContentSyncManager csm = new ContentSyncManager(null, mgr);
        CloudRMTCredentials rmtCreds = CredentialsFactory.createCloudRmtCredentials("RMTUSER", "secret",
            "http://example.com");
        PaygSshData sshData = PaygSshDataFactory.createPaygSshData();
        sshData.setHost("localhost");
        sshData.setUsername("admin");
        sshData.setPassword("secret");
        rmtCreds.setPaygSshData(sshData);
        CredentialsFactory.storeCredentials(rmtCreds);
        PaygSshDataFactory.savePaygSshData(sshData);

        assertTrue(csm.isRefreshNeeded(null));
    }

    /**
     * Test generation of channels for PTF repositories per module
     * @throws Exception if anything goes wrong
     */
    @Test
    public void testUpdateChannelsWithPtfReposUbuntuWithTools() throws Exception {
        SUSEProductTestUtils.createVendorSUSEProductEnvironment(user,
                "/com/redhat/rhn/manager/content/test/data3", true);
        HibernateFactory.getSession().flush();
        HibernateFactory.getSession().clear();

        //  Ubuntu 20.04
        SUSEProduct rootUbuntu = SUSEProductFactory.lookupByProductId(-18L);
        SUSEProductTestUtils.addChannelsForProduct(rootUbuntu);
        // sle-manager-tools 20.04 with PTF repos
        SUSEProductTestUtils.addChannelsForProductAndParent(SUSEProductFactory.lookupByProductId(2113L),
                rootUbuntu, true, Arrays.asList(15004L, 15005L));

        HibernateFactory.getSession().flush();
        HibernateFactory.getSession().clear();

        // Initialization complete

        SUSEProduct ubuntu = SUSEProductFactory.lookupByProductId(-18L);
        ubuntu.getRepositories().stream()
              .peek(pr -> LOGGER.info("Repository {}", pr.getRepository().getName()))
              .filter(pr -> pr.isMandatory())
              .forEach(pr -> {
                  assertNotNull(pr.getRepository());
                  SCCRepositoryAuth bestAuth = pr.getRepository().getBestAuth().orElse(null);
                  assertNotNull(bestAuth, "Best authorization is null for repository " + pr.getRepository().getName());
                  ContentSource cs = bestAuth.getContentSource();
                  assertNotNull(cs);
                  assertEquals(bestAuth.getUrl(), cs.getSourceUrl());
              });

        SCCRepository ptfRepo = SCCCachingFactory.lookupRepositoryBySccId(15004L).orElse(null);
        assertNotNull(ptfRepo, "PTF repo not found");

        SUSEProduct tools = SUSEProductFactory.lookupByProductId(2113L);
        tools.getRepositories().stream()
                .filter(pr -> pr.getRootProduct().equals(ubuntu))
                .filter(pr -> Arrays.asList(15004L, 15005L).contains(pr.getRepository().getSccId()))
                .forEach(pr -> {
                    assertNotNull(pr.getRepository());
                    // The PTF repo
                    if (pr.getRepository().getSccId().equals(15004L)) {
                        assertEquals("a123456-ubuntu-manager-client-2004-ptfs-amd64", pr.getChannelLabel());
                        assertEquals("A123456 ubuntu-manager-client 2004 PTFs amd64", pr.getChannelName());
                    }
                    else {
                        fail("Unexpected repository " + pr);
                    }
                    SCCRepositoryAuth bestAuth = pr.getRepository().getBestAuth().get();
                    ContentSource cs = bestAuth.getContentSource();
                    assertNotNull(cs);
                    assertEquals(bestAuth.getUrl(), cs.getSourceUrl());
                });
    }

    /**
     * Test for {@link ContentSyncManager#addChannel}.
     * @throws Exception if anything goes wrong
     */
    @Test
    public void testAddChannel() throws Exception {
        SUSEProductTestUtils.createVendorSUSEProductEnvironment(
                user, "/com/redhat/rhn/manager/content/test/smallBase", true);
        HibernateFactory.getSession().flush();
        HibernateFactory.getSession().clear();

        ContentSyncManager csm = new ContentSyncManager();

        try {
            csm.addChannel("non-existing-channel", null);
            fail("adding non-existing-channel should fail");
        }
        catch (ContentSyncException e) {
            assertContains(e.getMessage(), "No product tree entry found for label:");
        }

        try {
            csm.addChannel("sles12-updates-x86_64", null);
            fail("adding sles12-updates-x86_64 should not work here");
        }
        catch (ContentSyncException e) {
            assertContains(e.getMessage(), "The parent channel is not installed");
        }
        csm.addChannel("sles12-pool-x86_64", null);
        csm.addChannel("sles12-updates-x86_64", null);
        csm.addChannel("sle-module-legacy12-debuginfo-pool-x86_64", null);
        csm.addChannel("rhel-x86_64-server-7", null);

        HibernateFactory.getSession().flush();
        HibernateFactory.getSession().clear();

        assertTrue(csm.listChannels().stream().anyMatch(c -> c.getLabel().equals("sles12-pool-x86_64")));
        assertTrue(csm.listChannels().stream().anyMatch(c -> c.getLabel().equals("sles12-updates-x86_64")));

        Channel channel = ChannelFactory.lookupByLabel("sles12-pool-x86_64");
        assertNotNull(channel);
        for (ContentSource cs : channel.getSources()) {
            assertEquals("https://updates.suse.com/SUSE/Products/SLE-SERVER/12/x86_64/product/?my-fake-token",
                    cs.getSourceUrl());
            assertEquals("sles12-pool-x86_64", cs.getLabel());
        }
        for (SUSEProductChannel pc : channel.getSuseProductChannels()) {
            assertTrue(pc.isMandatory());
            assertEquals(1117L, pc.getProduct().getProductId());
        }

        channel = ChannelFactory.lookupByLabel("sle-module-legacy12-debuginfo-pool-x86_64");
        assertEquals("sle-module-legacy12-debuginfo-pool-x86_64", channel.getLabel());
        for (SUSEProductChannel pc : channel.getSuseProductChannels()) {
            assertFalse(pc.isMandatory());
            assertEquals(1150L, pc.getProduct().getProductId());
        }
        assertTrue(csm.listChannels().stream().anyMatch(c -> c.getLabel().equals("rhel-x86_64-server-7")));
        assertTrue(csm.listChannels()
                .stream()
                .filter(c -> c.getLabel().equals("rhel-x86_64-server-7"))
                .anyMatch(c -> c.getStatus().equals(MgrSyncStatus.INSTALLED)));
    }

    /**
     * Tests ContentSyncManager with a local filesystem link
     * using an URL pointing to an official SUSE server.
     * @throws Exception if anything goes wrong
     */
    @Test
    public void testSetupSourceURLLocalFS() throws Exception {
        File reposJson = new File(TestUtils.findTestData(
                new File(JARPATH,  "smallBase/" + REPOS_JSON).getAbsolutePath()).getPath());
        File prdJson = new File(TestUtils.findTestData(
                new File(JARPATH,  "smallBase/" + PRODUCTS_JSON).getAbsolutePath()).getPath());
        File treeJson = new File(TestUtils.findTestData(
                new File(JARPATH,  "smallBase/" + TREE_JSON).getAbsolutePath()).getPath());

        Path fromdir = Files.createTempDirectory("sumatest");
        File prdTmp = new File(fromdir.toString(), "organizations_products_unscoped.json");
        File repoTemp = new File(fromdir.toString(), "organizations_repositories.json");
        File treeTemp = new File(fromdir.toString(), TREE_JSON);
        Files.copy(prdJson.toPath(), prdTmp.toPath());
        Files.copy(reposJson.toPath(), repoTemp.toPath());
        Files.copy(treeJson.toPath(), treeTemp.toPath());
        Files.createDirectories(new File(fromdir + "/SUSE/Products/SLE-SERVER/12/x86_64/product/repodata/").toPath());
        Files.createDirectories(new File(fromdir + "/SUSE/Updates/SLE-SERVER/12/x86_64/update/repodata/").toPath());
        Files.createDirectories(new File(fromdir + "/SUSE/Products/SLE-WE/12/x86_64/product/repodata/").toPath());
        Files.createDirectories(new File(fromdir + "/SUSE/Updates/SLE-WE/12/x86_64/update/repodata/").toPath());
        Files.createDirectories(new File(fromdir + "/repo/RPMMD/SLE-12-GA-Desktop-NVIDIA-Driver/repodata/").toPath());
        new File(fromdir + "/SUSE/Products/SLE-SERVER/12/x86_64/product/repodata/repomd.xml").createNewFile();
        new File(fromdir + "/SUSE/Updates/SLE-SERVER/12/x86_64/update/repodata/repomd.xml").createNewFile();
        new File(fromdir + "/SUSE/Products/SLE-WE/12/x86_64/product/repodata/repomd.xml").createNewFile();
        new File(fromdir + "/SUSE/Updates/SLE-WE/12/x86_64/update/repodata/repomd.xml").createNewFile();
        new File(fromdir + "/repo/RPMMD/SLE-12-GA-Desktop-NVIDIA-Driver/repodata/repomd.xml").createNewFile();
        try {
            Gson gson = new GsonBuilder()
                    .setDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSSX")
                    .create();
            InputStreamReader inputStreamReader3 = new InputStreamReader(ContentSyncManager.class
                    .getResourceAsStream(JARPATH + "smallBase/channel_families.json"));
            List<ChannelFamilyJson> channelFamilies = gson.fromJson(
                    inputStreamReader3, new TypeToken<List<ChannelFamilyJson>>() { }.getType());

            ContentSyncManager csm = new ContentSyncManager();
            csm.setSumaProductTreeJson(Optional.of(treeTemp));
            csm.updateChannelFamilies(channelFamilies);
            Config.get().setString(ContentSyncManager.RESOURCE_PATH, fromdir.toString());
            csm.updateSUSEProducts(csm.getProducts());
            csm.updateRepositories(null);

            HibernateFactory.getSession().flush();
            HibernateFactory.getSession().clear();
            SUSEProduct sles = SUSEProductFactory.lookupByProductId(1117);
            SUSEProduct slewe = SUSEProductFactory.lookupByProductId(1222);

            SUSEProductTestUtils.addChannelsForProduct(sles);
            SUSEProductTestUtils.addChannelsForProduct(slewe);
            HibernateFactory.getSession().flush();
            HibernateFactory.getSession().clear();

            sles.getRepositories().stream()
                .filter(SUSEProductSCCRepository::isMandatory)
                .forEach(pr -> {
                    assertNotNull(pr.getRepository());
                    SCCRepositoryAuth bestAuth = pr.getRepository().getBestAuth().orElse(null);
                    assertNotNull(bestAuth, "Best auth is null for repository " + pr.getRepository().getName());
                    ContentSource cs = bestAuth.getContentSource();
                    assertNotNull(cs);
                    assertEquals(bestAuth.getUrl(), cs.getSourceUrl());
                    assertContains(cs.getSourceUrl(), "file://" + fromdir + "/SUSE/");
                });
            slewe.getRepositories().stream()
            .filter(SUSEProductSCCRepository::isMandatory)
            .forEach(pr -> {
                assertNotNull(pr.getRepository());
                SCCRepositoryAuth bestAuth = pr.getRepository().getBestAuth().get();
                ContentSource cs = bestAuth.getContentSource();
                assertNotNull(cs);
                assertEquals(bestAuth.getUrl(), cs.getSourceUrl());
                if (pr.getChannelName().toLowerCase().contains("nvidia")) {
                    assertContains(cs.getSourceUrl(), "file://" + fromdir + "/repo/RPMMD/");
                }
                else {
                    assertContains(cs.getSourceUrl(), "file://" + fromdir + "/SUSE/");
                }
            });
        }
        finally {
            Config.get().remove(ContentSyncManager.RESOURCE_PATH);
            SUSEProductTestUtils.deleteIfTempFile(reposJson);
            SUSEProductTestUtils.deleteIfTempFile(prdJson);
            repoTemp.delete();
            prdTmp.delete();
            treeTemp.delete();
            FileUtils.deleteDirectory(fromdir.toFile());
        }
    }

    /**
     * Tests changing from fromdir to SCC
     * @throws Exception if something goes wrong
     */
    @Test
    public void testSwitchFromdirToSCC() throws Exception {
        File reposJson = new File(TestUtils.findTestData(
                new File(JARPATH,  "smallBase/" + REPOS_JSON).getAbsolutePath()).getPath());
        File prdJson = new File(TestUtils.findTestData(
                new File(JARPATH,  "smallBase/" + PRODUCTS_JSON).getAbsolutePath()).getPath());
        File treeJson = new File(TestUtils.findTestData(
                new File(JARPATH,  "smallBase/" + TREE_JSON).getAbsolutePath()).getPath());

        Path fromdir = Files.createTempDirectory("sumatest");
        File prdTmp = new File(fromdir.toString(), "organizations_products_unscoped.json");
        File repoTemp = new File(fromdir.toString(), "organizations_repositories.json");
        File treeTemp = new File(fromdir.toString(), TREE_JSON);
        Files.copy(prdJson.toPath(), prdTmp.toPath());
        Files.copy(reposJson.toPath(), repoTemp.toPath());
        Files.copy(treeJson.toPath(), treeTemp.toPath());
        Files.createDirectories(new File(fromdir + "/SUSE/Products/SLE-SERVER/12/x86_64/product/repodata/").toPath());
        Files.createDirectories(new File(fromdir + "/SUSE/Updates/SLE-SERVER/12/x86_64/update/repodata/").toPath());
        Files.createDirectories(new File(fromdir + "/SUSE/Products/SLE-WE/12/x86_64/product/repodata/").toPath());
        Files.createDirectories(new File(fromdir + "/SUSE/Updates/SLE-WE/12/x86_64/update/repodata/").toPath());
        Files.createDirectories(new File(fromdir + "/repo/RPMMD/SLE-12-GA-Desktop-NVIDIA-Driver/repodata/").toPath());
        new File(fromdir + "/SUSE/Products/SLE-SERVER/12/x86_64/product/repodata/repomd.xml").createNewFile();
        new File(fromdir + "/SUSE/Updates/SLE-SERVER/12/x86_64/update/repodata/repomd.xml").createNewFile();
        new File(fromdir + "/SUSE/Products/SLE-WE/12/x86_64/product/repodata/repomd.xml").createNewFile();
        new File(fromdir + "/SUSE/Updates/SLE-WE/12/x86_64/update/repodata/repomd.xml").createNewFile();
        new File(fromdir + "/repo/RPMMD/SLE-12-GA-Desktop-NVIDIA-Driver/repodata/repomd.xml").createNewFile();
        try {
            Gson gson = new GsonBuilder()
                    .setDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSSX")
                    .create();
            InputStreamReader inputStreamReader3 = new InputStreamReader(ContentSyncManager.class
                    .getResourceAsStream(JARPATH + "smallBase/channel_families.json"));
            List<ChannelFamilyJson> channelFamilies = gson.fromJson(
                    inputStreamReader3, new TypeToken<List<ChannelFamilyJson>>() { }.getType());

            ContentSyncManager csm = new ContentSyncManager();
            csm.setSumaProductTreeJson(Optional.of(treeTemp));
            csm.updateChannelFamilies(channelFamilies);
            Config.get().setString(ContentSyncManager.RESOURCE_PATH, fromdir.toString());
            csm.updateSUSEProducts(csm.getProducts());
            csm.updateRepositories(null);

            clearSession();

            SUSEProduct sles = SUSEProductFactory.lookupByProductId(1117);
            SUSEProduct slewe = SUSEProductFactory.lookupByProductId(1222);

            SUSEProductTestUtils.addChannelsForProduct(sles);
            SUSEProductTestUtils.addChannelsForProduct(slewe);

            // remove "fromdir" config
            Config.get().remove(ContentSyncManager.RESOURCE_PATH);
            SUSEProductTestUtils.createVendorSUSEProductEnvironment(
                    user, "/com/redhat/rhn/manager/content/test/smallBase", true);

            clearSession();

            csm = new ContentSyncManager();
            csm.linkAndRefreshContentSource(null);

            clearSession();

            sles = SUSEProductFactory.lookupByProductId(1117);
            slewe = SUSEProductFactory.lookupByProductId(1222);

            sles.getRepositories().stream()
                .filter(SUSEProductSCCRepository::isMandatory)
                .forEach(pr -> {
                    assertNotNull(pr.getRepository());
                    SCCRepositoryAuth bestAuth = pr.getRepository().getBestAuth().get();
                    ContentSource cs = bestAuth.getContentSource();
                    assertNotNull(cs);
                    assertEquals(bestAuth.getUrl(), cs.getSourceUrl());
                    assertContains(cs.getSourceUrl(), "https://updates.suse.com");
                });
            slewe.getRepositories().stream()
            .filter(SUSEProductSCCRepository::isMandatory)
            .forEach(pr -> {
                assertNotNull(pr.getRepository());
                SCCRepositoryAuth bestAuth = pr.getRepository().getBestAuth().get();
                ContentSource cs = bestAuth.getContentSource();
                assertNotNull(cs);
                assertEquals(bestAuth.getUrl(), cs.getSourceUrl());
                if (pr.getChannelName().toLowerCase().contains("nvidia")) {
                    assertContains(cs.getSourceUrl(), "http://download.nvidia.com");
                }
                else {
                    assertContains(cs.getSourceUrl(), "https://updates.suse.com");
                }
            });
        }
        finally {
            Config.get().remove(ContentSyncManager.RESOURCE_PATH);
            SUSEProductTestUtils.deleteIfTempFile(reposJson);
            SUSEProductTestUtils.deleteIfTempFile(prdJson);
            repoTemp.delete();
            prdTmp.delete();
            treeTemp.delete();
            FileUtils.deleteDirectory(fromdir.toFile());
        }
    }

    /**
     * Tests that the SUSEProductChannel class behaves correctly with Hibernate
     * (ensures there is no regression wrt bsc#932052).
     * @throws Exception if something goes wrong
     */
    @Test
    public void testSUSEProductChannelUpdates() throws Exception {
        // Setup two products
        Channel channel = SUSEProductTestUtils.createTestVendorChannel();
        ChannelFamily family = channel.getChannelFamily();
        SUSEProduct product = SUSEProductTestUtils.createTestSUSEProduct(family);

        Channel channel2 = SUSEProductTestUtils.createTestVendorChannel();
        ChannelFamily family2 = channel2.getChannelFamily();
        SUSEProduct product2 = SUSEProductTestUtils.createTestSUSEProduct(family2);

        // Create a product channel
        SUSEProductChannel spc = new SUSEProductChannel();
        spc.setChannel(channel);
        spc.setProduct(product);

        SUSEProductFactory.save(spc);
        HibernateFactory.getSession().flush();

        // change the product
        spc.setProduct(product2);
        SUSEProductFactory.save(spc);
        HibernateFactory.getSession().flush();

        // removes the changed product
        SUSEProductFactory.remove(spc);

        // flushes again, used to fail with exception in bsc#932052
        HibernateFactory.getSession().flush();
    }

    /**
     * Clear all credentials from the database.
     */
    private void clearCredentials() {
        for (SCCCredentials creds : CredentialsFactory.listSCCCredentials()) {
            CredentialsFactory.removeCredentials(creds);
        }
    }

    /**
     * Return a list of channel families containing random data as attributes.
     * @return list of channel families for testing
     */
    private List<ChannelFamilyJson> getChannelFamilies() {
        List<ChannelFamilyJson> channelFamilies = new ArrayList<>();
        ChannelFamilyJson family1 = new ChannelFamilyJson();
        family1.setLabel(TestUtils.randomString());
        family1.setName(TestUtils.randomString());
        channelFamilies.add(family1);
        ChannelFamilyJson family2 = new ChannelFamilyJson();
        family2.setLabel(TestUtils.randomString());
        family2.setName(TestUtils.randomString());
        channelFamilies.add(family2);
        return channelFamilies;
    }

    /**
     * Temporarily rename all installed vendor channels in order to avoid conflicts
     * whenever we use real channel labels in tests.
     */
    private void renameVendorChannels() {
        for (Channel c : ChannelFactory.listVendorChannels()) {
            c.setLabel(TestUtils.randomString());
            c.setName(TestUtils.randomString());
            TestUtils.saveAndFlush(c);
        }
        for (ContentSource cs : ChannelFactory.listVendorContentSources()) {
            cs.setLabel(TestUtils.randomString());
            cs.setSourceUrl(TestUtils.randomString());
            TestUtils.saveAndFlush(cs);
        }
    }

    @Test
    public void testIsChannelOrLabelReserved() throws Exception {
        SUSEProductTestUtils.createVendorSUSEProductEnvironment(user, null, false);
        HibernateFactory.getSession().flush();
        HibernateFactory.getSession().clear();

        assertFalse(ContentSyncManager.isChannelNameReserved("suse"));
        assertFalse(ContentSyncManager.isChannelLabelReserved("label"));
        assertTrue(ContentSyncManager.isChannelLabelReserved("sles11-sp3-pool-x86_64"));
        assertTrue(ContentSyncManager.isChannelNameReserved("IBM-DLPAR-SDK for ppc64le"));
    }

    /**
     * Checks that updateSUSEProducts() can be called multiple times in a row
     * without failing.
     * @throws Exception if anything goes wrong
     */
    @Test
    public void testUpdateProductsMultipleTimes() throws Exception {
        // clear existing products
        SUSEProductTestUtils.clearAllProducts();

        InputStreamReader inputStreamReader = new InputStreamReader(
                ContentSyncManager.class.getResourceAsStream(JARPATH + PRODUCTS_JSON));
        List<SCCProductJson> sccProducts =
                new Gson().fromJson(inputStreamReader,
                        new TypeToken<List<SCCProductJson>>() { } .getType());

        ContentSyncManager csm = new ContentSyncManager();
        csm.setSumaProductTreeJson(Optional.of(new File("/usr/share/susemanager/scc/product_tree.json")));

        csm.updateSUSEProducts(sccProducts);
        csm.updateSUSEProducts(sccProducts);
    }

    /**
     * Test debian and repomd url building
     * @throws Exception
     */
    @Test
    public void testBuildRepoFileUrl() throws Exception {
        SCCRepository debrepo = new SCCRepository();
        debrepo.setDistroTarget("amd64");
        SCCRepository rpmrepo = new SCCRepository();
        rpmrepo.setDistroTarget("sle-12-x86_64");

        String repourl = "http://localhost/pub/myrepo/";
        ContentSyncManager csm = new ContentSyncManager();

        assertContains(csm.buildRepoFileUrls(repourl, rpmrepo), repourl + "repodata/repomd.xml");
        assertEquals(1, csm.buildRepoFileUrls(repourl, rpmrepo).size());

        assertContains(csm.buildRepoFileUrls(repourl, debrepo), repourl + "Packages.xz");
        assertContains(csm.buildRepoFileUrls(repourl, debrepo), repourl + "Packages.gz");
        assertContains(csm.buildRepoFileUrls(repourl, debrepo), repourl + "Packages");
        assertContains(csm.buildRepoFileUrls(repourl, debrepo), repourl + "Release");
        assertContains(csm.buildRepoFileUrls(repourl, debrepo), repourl + "InRelease");
        assertEquals(5, csm.buildRepoFileUrls(repourl, debrepo).size());

        repourl = "http://mirrorlist.centos.org/?release=8&arch=x86_64&repo=BaseOS&infra=stock";
        rpmrepo.setDistroTarget("x86_64");

        URI uri = new URI(repourl);
        String url1 = new URI(uri.getScheme(), null, uri.getHost(), uri.getPort(), "/repodata/repomd.xml",
                uri.getQuery(), null).toString();
        String url2 = new URI(uri.getScheme(), null, uri.getHost(), uri.getPort(), "/",
                uri.getQuery(), null).toString();
        assertContains(csm.buildRepoFileUrls(repourl, rpmrepo), url1);
        assertContains(csm.buildRepoFileUrls(repourl, rpmrepo), url2);
        assertEquals(2, csm.buildRepoFileUrls(repourl, rpmrepo).size());
    }

    @Test
    public void updateRepositoriesForPaygDoNotCallSCC() {
        SCCCredentials credentials = CredentialsFactory.createSCCCredentials("dummy", "dummy");
        credentials.setUrl("dummy");
        credentials.setUser(user);
        CredentialsFactory.storeCredentials(credentials);

        SCCClient sccClient = mock(SCCClient.class);

        checking(expectations -> {
            expectations.never(sccClient).listRepositories();
        });

        ContentSyncManager csm = new ContentSyncManager() {
            @Override
            protected SCCClient getSCCClient(ContentSyncSource source) throws SCCClientException {
                return sccClient;
            }
        };

        csm.updateRepositoriesPayg();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    @BeforeEach
    public void setUp() throws Exception {
        super.setUp();

        // Clear data for all tests
        clearCredentials();
        SCCCachingFactory.clearRepositories();
        renameVendorChannels();
    }

    @Override
    @AfterEach
    public void tearDown() throws Exception {
        Config.clear();
        super.tearDown();
    }
}
