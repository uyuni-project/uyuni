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
package com.redhat.rhn.manager.content.test;

import com.redhat.rhn.common.conf.Config;
import com.redhat.rhn.common.hibernate.HibernateFactory;
import com.redhat.rhn.domain.channel.Channel;
import com.redhat.rhn.domain.channel.ChannelFactory;
import com.redhat.rhn.domain.channel.ChannelFamily;
import com.redhat.rhn.domain.channel.ChannelFamilyFactory;
import com.redhat.rhn.domain.channel.ContentSource;
import com.redhat.rhn.domain.channel.test.ChannelFamilyFactoryTest;
import com.redhat.rhn.domain.credentials.Credentials;
import com.redhat.rhn.domain.credentials.CredentialsFactory;
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
import com.redhat.rhn.domain.scc.SCCRepositoryTokenAuth;
import com.redhat.rhn.manager.content.ContentSyncException;
import com.redhat.rhn.manager.content.ContentSyncManager;
import com.redhat.rhn.manager.content.MgrSyncProductDto;
import com.redhat.rhn.manager.content.ProductTreeEntry;
import com.redhat.rhn.testing.BaseTestCaseWithUser;
import com.redhat.rhn.testing.TestUtils;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.reflect.TypeToken;
import com.suse.mgrsync.MgrSyncStatus;
import com.suse.salt.netapi.parser.JsonParser;
import com.suse.scc.model.ChannelFamilyJson;
import com.suse.scc.model.SCCProductJson;
import com.suse.scc.model.SCCRepositoryJson;
import com.suse.scc.model.SCCSubscriptionJson;
import com.suse.scc.model.UpgradePathJson;

import org.apache.commons.io.FileUtils;

import java.io.File;
import java.io.InputStreamReader;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.Instant;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.Date;
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
public class ContentSyncManagerTest extends BaseTestCaseWithUser {

    // Files we read
    private static final String JARPATH = "/com/redhat/rhn/manager/content/test/";

    private static final String SUBSCRIPTIONS_JSON = "organizations_subscriptions.json";
    private static final String ORDERS_JSON = "organizations_orders.json";

    private static final String PRODUCTS_JSON = "productsUnscoped.json";
    private static final String TREE_JSON = "product_tree.json";
    private static final String REPOS_JSON = "repositories.json";
    private static final String UPGRADE_PATHS_JSON = "upgrade_paths.json";
    private static final String UPGRADE_PATHS_EMPTY_JSON = JARPATH + "upgrade_paths_empty.json";

    public void testSubscriptionDeleteCaching() throws Exception {

        File upgradePathsJson = new File(
                TestUtils.findTestData(UPGRADE_PATHS_JSON).getPath());
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

        List<SCCProductJson> products = new ArrayList<SCCProductJson>();
        products.add(p);

        // Call updateSUSEProducts()
        ContentSyncManager csm = new ContentSyncManager();
        csm.setSumaProductTreeJson(Optional.of(new File("/usr/share/susemanager/scc/product_tree.json")));
        csm.setUpgradePathsJson(upgradePathsJson);
        csm.updateSUSEProducts(products);

        List<SCCSubscriptionJson> subscriptions = new LinkedList<>();
        Credentials cred1 = CredentialsFactory.createSCCCredentials();
        cred1.setUsername("hans");
        cred1.setPassword("pw1");
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

        csm.refreshSubscriptionCache(subscriptions, cred1);
        HibernateFactory.getSession().flush();

        com.redhat.rhn.domain.scc.SCCSubscription one = SCCCachingFactory.lookupSubscriptionBySccId(1L);
        assertEquals(s1.getName(), one.getName());
        com.redhat.rhn.domain.scc.SCCSubscription two = SCCCachingFactory.lookupSubscriptionBySccId(2L);
        assertEquals(two.getName(), two.getName());

        subscriptions.remove(s2);
        csm.refreshSubscriptionCache(subscriptions, cred1);
        HibernateFactory.getSession().flush();

        one = SCCCachingFactory.lookupSubscriptionBySccId(1L);
        assertEquals(s1.getName(), one.getName());
        two = SCCCachingFactory.lookupSubscriptionBySccId(2L);
        assertNull(two);
    }

    public void testListSubscriptionsCaching() throws Exception {
        File subJson = new File(TestUtils.findTestData(
                new File(JARPATH,  "sccdata/" + SUBSCRIPTIONS_JSON).getAbsolutePath()).getPath());
        File orderJson = new File(TestUtils.findTestData(
                new File(JARPATH, "sccdata/" + ORDERS_JSON).getAbsolutePath()).getPath());
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
        }
        finally {
            Config.get().remove(ContentSyncManager.RESOURCE_PATH);
            SUSEProductTestUtils.deleteIfTempFile(subJson);
            SUSEProductTestUtils.deleteIfTempFile(orderJson);
            subtempFile.delete();
            ordertempFile.delete();
            fromdir.toFile().delete();
        }
    }

    public void testUpdateProducts()  throws Exception {
        SUSEProductTestUtils.createVendorSUSEProductEnvironment(user, null, false);

        Gson gson = new GsonBuilder()
                .setDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSSX")
                .create();
        InputStreamReader inputStreamReader3 = new InputStreamReader(ContentSyncManager.class.getResourceAsStream("/com/redhat/rhn/manager/content/test/repositories.json"));
        List<SCCRepositoryJson> repositories = gson.fromJson(inputStreamReader3, new TypeToken<List<SCCRepositoryJson>>() {}.getType());

        Credentials credentials = CredentialsFactory.lookupSCCCredentials()
                .stream()
                .filter(c -> c.getUsername().equals("dummy"))
                .findFirst().get();

        assertTrue("Repo should not have authentication.", SCCCachingFactory.lookupRepositoryBySccId(633L).get()
                .getRepositoryAuth().isEmpty());

        ContentSyncManager csm = new ContentSyncManager();
        csm.refreshRepositoriesAuthentication(repositories, credentials, null);

        Optional<SCCRepository> upRepoOpt = SCCCachingFactory.lookupRepositoryBySccId(633L);
        assertTrue("Repo not found", upRepoOpt.isPresent());
        SCCRepository upRepo = upRepoOpt.get();
        assertTrue("Best Auth is not token auth", upRepo.getBestAuth().flatMap(auth -> auth.tokenAuth()).isPresent());

    }

    public void testUpdateRepositories() throws Exception {
        Gson gson = new GsonBuilder()
                .setDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSSX")
                .create();
        Credentials credentials = CredentialsFactory.createSCCCredentials();
        credentials.setPassword("dummy");
        credentials.setUrl("dummy");
        credentials.setUsername("dummy");
        credentials.setUser(user);
        CredentialsFactory.storeCredentials(credentials);

        InputStreamReader inputStreamReader = new InputStreamReader(
                ContentSyncManager.class.getResourceAsStream("/com/redhat/rhn/manager/content/test/repositories.json"));
        List<SCCRepositoryJson> repositories = gson.fromJson(inputStreamReader,
                new TypeToken<List<SCCRepositoryJson>>() {}.getType());

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
        csm.refreshRepositoriesAuthentication(repositories, credentials, null);

        Optional<SCCRepository> upRepoOpt = SCCCachingFactory.lookupRepositoryBySccId(2705L);
        assertTrue("Repo not found", upRepoOpt.isPresent());
        SCCRepository upRepo = upRepoOpt.get();
        assertTrue("Best Auth is not token auth", upRepo.getBestAuth().get() instanceof SCCRepositoryTokenAuth);

        csm.refreshRepositoriesAuthentication(repositories, credentials, null);
        upRepoOpt = SCCCachingFactory.lookupRepositoryBySccId(2707L);
        assertTrue("Repo not found", upRepoOpt.isPresent());
        upRepo = upRepoOpt.get();
        assertTrue("Best Auth is not token auth", upRepo.getBestAuth().get() instanceof SCCRepositoryTokenAuth);
    }
    /**
     * Test if changes in SCC data result in updates of the channel data in the DB
     * @throws Exception if anything goes wrong
     */
    public void testUpdateChannels() throws Exception {
        SUSEProductTestUtils.createVendorSUSEProductEnvironment(user, "/com/redhat/rhn/manager/content/test/smallBase", true);
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
        assertEquals(true, pool.getSuseProductChannels().stream().findFirst().get().isMandatory());
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
        InputStreamReader inReaderProducts = new InputStreamReader(ContentSyncManager.class.getResourceAsStream("/com/redhat/rhn/manager/content/test/data1/productsUnscoped.json"));
        List<SCCProductJson> productsChanged = gson.fromJson(inReaderProducts, new TypeToken<List<SCCProductJson>>() {}.getType());
        InputStreamReader inReaderUpgrade = new InputStreamReader(ContentSyncManager.class.getResourceAsStream("/com/redhat/rhn/manager/content/test/upgrade_paths.json"));
        List<UpgradePathJson> upgradePaths = gson.fromJson(inReaderUpgrade, new TypeToken<List<UpgradePathJson>>() {}.getType());
        InputStreamReader inReaderTree = new InputStreamReader(ContentSyncManager.class.getResourceAsStream("/com/redhat/rhn/manager/content/test/data1/product_tree.json"));
        List<ProductTreeEntry> staticTreeChanged = JsonParser.GSON.fromJson(inReaderTree, new TypeToken<List<ProductTreeEntry>>() {}.getType());

        ContentSyncManager csm = new ContentSyncManager();
        csm.updateSUSEProducts(productsChanged, upgradePaths, staticTreeChanged, Collections.emptyList());
        HibernateFactory.getSession().flush();
        HibernateFactory.getSession().clear();

        Channel changedPool = ChannelFactory.lookupByLabel("sles12-pool-x86_64");
        Channel changedUpdate = ChannelFactory.lookupByLabel("sles12-updates-x86_64");
        assertEquals("SLES12-Pool for x86_64 UPDATED", changedPool.getName());
        assertEquals("SUSE Linux Enterprise Server 12 x86_64 UPDATED", changedPool.getSummary());
        assertEquals("UPDATED: SUSE Linux Enterprise offers a comprehensive suite of products built on a single code base. " +
                "The platform addresses business needs from the smallest thin-client devices to the world's most " +
                "powerful high-performance computing and mainframe servers. SUSE Linux Enterprise offers common " +
                "management tools and technology certifications across the platform, and each product is " +
                "enterprise-class.", changedPool.getDescription());
        assertEquals(false, changedPool.getSuseProductChannels().stream().findFirst().get().isMandatory());
        assertEquals("SLES12-Updates for x86_64 UPDATED", changedUpdate.getName());
        assertEquals("SUSE Linux Enterprise Server 12 x86_64 UPDATED", changedUpdate.getSummary());
        assertEquals("UPDATED: SUSE Linux Enterprise offers a comprehensive suite of products built on a single code base. " +
                "The platform addresses business needs from the smallest thin-client devices to the world's most " +
                "powerful high-performance computing and mainframe servers. SUSE Linux Enterprise offers common " +
                "management tools and technology certifications across the platform, and each product is " +
                "enterprise-class.", changedUpdate.getDescription());
    }

    /**
     * Test changes of the repo URL (result in change of the repository)
     * @throws Exception if anything goes wrong
     */
    public void testUpdateChannelsWithSimilarPath() throws Exception {
        SUSEProductTestUtils.createVendorSUSEProductEnvironment(user, "/com/redhat/rhn/manager/content/test/smallBase", true);
        HibernateFactory.getSession().flush();
        HibernateFactory.getSession().clear();

        // SLES12 GA
        SUSEProductTestUtils.addChannelsForProduct(SUSEProductFactory.lookupByProductId(1117));
        HibernateFactory.getSession().flush();
        HibernateFactory.getSession().clear();

        SUSEProduct sles = SUSEProductFactory.lookupByProductId(1117);
        sles.getRepositories().stream()
            .filter(pr -> pr.isMandatory())
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
        InputStreamReader inReaderProducts = new InputStreamReader(ContentSyncManager.class.getResourceAsStream("/com/redhat/rhn/manager/content/test/data1/productsUnscoped.json"));
        List<SCCProductJson> productsChanged = gson.fromJson(inReaderProducts, new TypeToken<List<SCCProductJson>>() {}.getType());
        InputStreamReader inReaderUpgrade = new InputStreamReader(ContentSyncManager.class.getResourceAsStream("/com/redhat/rhn/manager/content/test/upgrade_paths.json"));
        List<UpgradePathJson> upgradePaths = gson.fromJson(inReaderUpgrade, new TypeToken<List<UpgradePathJson>>() {}.getType());
        InputStreamReader inReaderTree = new InputStreamReader(ContentSyncManager.class.getResourceAsStream("/com/redhat/rhn/manager/content/test/data1/product_tree.json"));
        List<ProductTreeEntry> staticTreeChanged = JsonParser.GSON.fromJson(inReaderTree, new TypeToken<List<ProductTreeEntry>>() {}.getType());
        InputStreamReader inReaderRepos = new InputStreamReader(ContentSyncManager.class.getResourceAsStream("/com/redhat/rhn/manager/content/test/data1/repositories.json"));
        List<SCCRepositoryJson> repositoriesChanged = gson.fromJson(inReaderRepos, new TypeToken<List<SCCRepositoryJson>>() {}.getType());

        Credentials sccCreds = CredentialsFactory.lookupByUserAndType(user, Credentials.TYPE_SCC);

        ContentSyncManager csm = new ContentSyncManager();
        csm.updateSUSEProducts(productsChanged, upgradePaths, staticTreeChanged, Collections.emptyList());
        csm.refreshRepositoriesAuthentication(repositoriesChanged, sccCreds, null);
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
            .filter(pr -> pr.isMandatory())
            .forEach(pr -> {
                assertNotNull(pr.getRepository());
                SCCRepositoryAuth bestAuth = pr.getRepository().getBestAuth().get();
                ContentSource cs = bestAuth.getContentSource();
                assertNotNull(cs);
                assertEquals(bestAuth.getUrl(), cs.getSourceUrl());
            });
    }

    /**
     * Test for {@link ContentSyncManager#updateSUSEProducts} inserting a new product.
     * @throws Exception if anything goes wrong
     */
    public void testUpdateSUSEProductsNew() throws Exception {
        File upgradePathsJson = new File(
                TestUtils.findTestData(JARPATH + UPGRADE_PATHS_JSON).getPath());
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
        List<SCCProductJson> products = new ArrayList<SCCProductJson>();
        products.add(p);

        // Call updateSUSEProducts()
        ContentSyncManager csm = new ContentSyncManager();
        csm.setSumaProductTreeJson(Optional.of(new File("/usr/share/susemanager/scc/product_tree.json")));
        csm.setUpgradePathsJson(upgradePathsJson);
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
    public void testUpdateSUSEProductsUpdate() throws Exception {
        File upgradePathsJson = new File(
                TestUtils.findTestData(JARPATH + UPGRADE_PATHS_JSON).getPath());
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
        List<SCCProductJson> products = new ArrayList<SCCProductJson>();
        products.add(p);

        // Call updateSUSEProducts()
        ContentSyncManager csm = new ContentSyncManager();
        csm.setSumaProductTreeJson(Optional.of(new File("/usr/share/susemanager/scc/product_tree.json")));
        csm.setUpgradePathsJson(upgradePathsJson);
        csm.updateSUSEProducts(products);

        // Verify that the product has been updated correctly
        suseProduct = SUSEProductFactory.lookupByProductId(productId);
        assertEquals(friendlyNameNew, suseProduct.getFriendlyName());
    }

    /**
     * Test for {@link ContentSyncManager#getAvailableChannels}.
     * @throws Exception if anything goes wrong
     */
    public void testGetAvailableChannels() throws Exception {
        SUSEProductTestUtils.createVendorSUSEProductEnvironment(user, "/com/redhat/rhn/manager/content/test/smallBase", true);
        HibernateFactory.getSession().flush();
        HibernateFactory.getSession().clear();

        ContentSyncManager csm = new ContentSyncManager();
        List<SUSEProductSCCRepository> availableChannels = csm.getAvailableChannels();

        List<String> avChanLanbels = availableChannels
                .stream().map(pr -> pr.getChannelLabel()).collect(Collectors.toList());

        assertContains(avChanLanbels, "sles12-pool-x86_64");
        assertContains(avChanLanbels, "sle-12-cloud-compute5-updates-x86_64");
        assertContains(avChanLanbels, "sles12-ltss-updates-x86_64");
        assertContains(avChanLanbels, "sle-ha-geo12-debuginfo-pool-x86_64");
        assertContains(avChanLanbels, "sle-we12-updates-x86_64");
        // Storage 2 is not in repositories.json to emulate no subscription
        assertFalse("Storage should not be avaliable", avChanLanbels.contains("suse-enterprise-storage-2-updates-x86_64"));
    }

    /**
     * Test for duplicates in {@link ContentSyncManager#getAvailableChannels} output.
     * @throws Exception if anything goes wrong
     */
    public void testNoDupInGetAvailableChannels() throws Exception {
        SUSEProductTestUtils.createVendorSUSEProductEnvironment(user, "/com/redhat/rhn/manager/content/test/", true);
        HibernateFactory.getSession().flush();
        HibernateFactory.getSession().clear();

        ContentSyncManager csm = new ContentSyncManager();
        List<SUSEProductSCCRepository> availableChannels = csm.getAvailableChannels();

        List<String> duplicates = new LinkedList<>();
        availableChannels.stream()
                .map(pr -> pr.getChannelLabel())
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
        assertTrue(duplicates.size() + " Duplicate labels found: " + String.join("\n", duplicates), duplicates.isEmpty());
    }

    /**
     * Test for {@link ContentSyncManager#updateChannelFamilies} method, insert case.
     * @throws Exception if anything goes wrong
     */
    public void testUpdateChannelFamiliesInsert() throws Exception {
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
     * @throws Exception if anything goes wrong
     */
    public void testUpdateChannelFamiliesUpdate() throws Exception {
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
    public void testUpdateUpgradePaths() throws Exception {
        File upgradePathsJson = new File(
                TestUtils.findTestData(JARPATH + UPGRADE_PATHS_JSON).getPath());
        try {
            // Prepare products since they will be looked up
            ChannelFamily family = ChannelFamilyFactoryTest.createTestChannelFamily();
            SUSEProduct p;
            if (SUSEProductFactory.lookupByProductId(690) == null) {
                p = SUSEProductTestUtils.createTestSUSEProduct(family);
                p.setProductId(690);
                TestUtils.saveAndFlush(p);
            }
            if (SUSEProductFactory.lookupByProductId(814) == null) {
                p = SUSEProductTestUtils.createTestSUSEProduct(family);
                p.setProductId(814);
                TestUtils.saveAndFlush(p);
            }
            if (SUSEProductFactory.lookupByProductId(1002) == null) {
                p = SUSEProductTestUtils.createTestSUSEProduct(family);
                p.setProductId(1002);
                TestUtils.saveAndFlush(p);
            }
            if (SUSEProductFactory.lookupByProductId(1141) == null) {
                p = SUSEProductTestUtils.createTestSUSEProduct(family);
                p.setProductId(1141);
                TestUtils.saveAndFlush(p);
            }
            if (SUSEProductFactory.lookupByProductId(1193) == null) {
                p = SUSEProductTestUtils.createTestSUSEProduct(family);
                p.setProductId(1193);
                TestUtils.saveAndFlush(p);
            }
            if (SUSEProductFactory.lookupByProductId(1198) == null) {
                p = SUSEProductTestUtils.createTestSUSEProduct(family);
                p.setProductId(1198);
                TestUtils.saveAndFlush(p);
            }

            List<SCCProductJson> products = new ArrayList<SCCProductJson>();
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
                    Collections.emptyList(), Collections.emptyList(), Collections.singletonList(10012345L), Collections.emptyList(),
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
            csm.setUpgradePathsJson(upgradePathsJson);
            csm.updateUpgradePaths(products, csm.readUpgradePaths());

            // Check the results
            SUSEProduct p690 = SUSEProductFactory.lookupByProductId(690);
            SUSEProduct p814 = SUSEProductFactory.lookupByProductId(814);
            SUSEProduct p1002 = SUSEProductFactory.lookupByProductId(1002);
            SUSEProduct p1141 = SUSEProductFactory.lookupByProductId(1141);
            SUSEProduct p1193 = SUSEProductFactory.lookupByProductId(1193);
            SUSEProduct p1198 = SUSEProductFactory.lookupByProductId(1198);
            SUSEProduct p10012345 = SUSEProductFactory.lookupByProductId(10012345);
            SUSEProduct p10012346 = SUSEProductFactory.lookupByProductId(10012346);

            assertContains(p690.getUpgrades(), p814);
            assertContains(p1002.getUpgrades(), p1141);
            assertContains(p1193.getUpgrades(), p1198);
            assertContains(p10012345.getUpgrades(), p10012346);
            assertContains(p690.getUpgrades(), p814);
        }
        finally {
            SUSEProductTestUtils.deleteIfTempFile(upgradePathsJson);
        }
    }

    /**
     * There is an upgrade path in the DB and SCC deletes the "from" product.
     * @throws Exception if anything goes wrong
     */
    public void testUpgradePathPredecessorDeleted() throws Exception {
        File upgradePathsEmptyJson = new File(
                TestUtils.findTestData(UPGRADE_PATHS_EMPTY_JSON).getPath());
        try {
            List<SCCProductJson> products = new ArrayList<SCCProductJson>();

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
                    Collections.emptyList(), Collections.emptyList(), Collections.singletonList(product1Id), Collections.emptyList(),
                    null, false);
            products.add(product2);

            // Update SUSE products and upgrade paths
            ContentSyncManager csm = new ContentSyncManager();
            csm.setSumaProductTreeJson(Optional.of(new File("/usr/share/susemanager/scc/product_tree.json")));
            csm.setUpgradePathsJson(upgradePathsEmptyJson);
            csm.updateSUSEProducts(products);
            HibernateFactory.getSession().flush();

            // There should be an upgrade path from product1 to product2
            assertEquals(1, SUSEProductFactory.lookupByProductId(product1Id).getUpgrades().size());

            // Remove the first product
            products.remove(product1);
            csm.updateSUSEProducts(Collections.singletonList(product2.copy().setOnlinePredecessorIds(Collections.emptyList()).build()));
            HibernateFactory.getSession().flush();

            // There should be no upgrade paths
            assertEquals(true, SUSEProductFactory.lookupByProductId(product1Id).getUpgrades().isEmpty());
        }
        finally {
            SUSEProductTestUtils.deleteIfTempFile(upgradePathsEmptyJson);
        }
    }

    /**
     * An upgrade path between two products is removed while the products still exist.
     * @throws Exception if anything goes wrong
     */
    public void testUpgradePathRemoved() throws Exception {
        File upgradePathsEmptyJson = new File(
                TestUtils.findTestData(UPGRADE_PATHS_EMPTY_JSON).getPath());
        try {
            List<SCCProductJson> products = new ArrayList<SCCProductJson>();

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
                    Collections.emptyList(), Collections.emptyList(), Collections.singletonList(product1Id), Collections.emptyList(),
                    null, false);
            products.add(product2);

            // Update SUSE products and upgrade paths
            ContentSyncManager csm = new ContentSyncManager();
            csm.setSumaProductTreeJson(Optional.of(new File("/usr/share/susemanager/scc/product_tree.json")));
            csm.setUpgradePathsJson(upgradePathsEmptyJson);
            csm.updateSUSEProducts(products);

            // There should be an upgrade path from product1 to product2
            assertEquals(1, SUSEProductFactory.lookupByProductId(product1Id).getUpgrades().size());

            // Remove the upgrade path via the predecessor Id
            csm.updateSUSEProducts(Stream.of(product1, product2.copy().setOnlinePredecessorIds(Collections.emptyList())
                    .build()).collect(Collectors.toList()));

            // There should be no upgrade paths
            assertEquals(true, SUSEProductFactory.lookupByProductId(product1Id).getUpgrades().isEmpty());
        }
        finally {
            SUSEProductTestUtils.deleteIfTempFile(upgradePathsEmptyJson);
        }
    }

    /**
     * Test for {@link ContentSyncManager#listChannels}.
     * @throws Exception if anything goes wrong
     */
    public void testListChannels() throws Exception {
        SUSEProductTestUtils.createVendorSUSEProductEnvironment(user, "/com/redhat/rhn/manager/content/test/smallBase", true);
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
                assertEquals("https://updates.suse.com/SUSE/Products/SLE-SERVER/12/x86_64/product_debug/", c.getSourceUrl());
                assertEquals(MgrSyncStatus.AVAILABLE, c.getStatus());
                foundDebugPool = true;
            }
            else if (c.getLabel().startsWith("suse-enterprise-storage")) {
                assertTrue("Storage Channels should not be listed", false);
            }
        }
        assertTrue("Pool channel not found", foundPool);
        assertTrue("Debuginfo Pool channel not found", foundDebugPool);
        Map<MgrSyncStatus, List<MgrSyncChannelDto>> collect = channels.stream().collect(Collectors.groupingBy(c -> c.getStatus()));
        assertEquals(2, collect.get(MgrSyncStatus.INSTALLED).size());
        assertEquals(62, collect.get(MgrSyncStatus.AVAILABLE).size());
    }

    /**
     * Tests {@link ContentSyncManager#listProducts}, in particular the
     * filtering of unavailable products.
     * @throws Exception if anything goes wrong
     */
    public void testListProductsAvailability() throws Exception {
        SUSEProductTestUtils.createVendorSUSEProductEnvironment(user, "/com/redhat/rhn/manager/content/test/smallBase", true);
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
                foundSLES = true;

                for (MgrSyncProductDto ext : product.getExtensions()) {
                    if (ext.getFriendlyName().equals("SUSE Linux Enterprise High Availability GEO Extension 12 x86_64")) {
                        assertEquals(MgrSyncStatus.AVAILABLE, ext.getStatus());
                        foundHAGEO = true;
                    }
                }
            }
        }
        assertTrue("SLES not found", foundSLES);
        assertTrue("HA-GEO not found", foundHAGEO);
    }

    /**
     * Test for {@link ContentSyncManager#addChannel}.
     * @throws Exception if anything goes wrong
     */
    public void testAddChannel() throws Exception {
        SUSEProductTestUtils.createVendorSUSEProductEnvironment(user, "/com/redhat/rhn/manager/content/test/smallBase", true);
        HibernateFactory.getSession().flush();
        HibernateFactory.getSession().clear();

        ContentSyncManager csm = new ContentSyncManager();
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
            assertEquals("https://updates.suse.com/SUSE/Products/SLE-SERVER/12/x86_64/product/?my-fake-token", cs.getSourceUrl());
            assertEquals("sles12-pool-x86_64", cs.getLabel());
        }
        for (SUSEProductChannel pc : channel.getSuseProductChannels()) {
            assertEquals(true, pc.isMandatory());
            assertEquals(1117L, pc.getProduct().getProductId());
        }

        channel = ChannelFactory.lookupByLabel("sle-module-legacy12-debuginfo-pool-x86_64");
        assertTrue(channel.getLabel().equals("sle-module-legacy12-debuginfo-pool-x86_64"));
        for (SUSEProductChannel pc : channel.getSuseProductChannels()) {
            assertEquals(false, pc.isMandatory());
            assertEquals(1150L, pc.getProduct().getProductId());
        }
        assertTrue(csm.listChannels().stream().anyMatch(c -> c.getLabel().equals("rhel-x86_64-server-7")));
        assertTrue(csm.listChannels()
                .stream()
                .filter(c -> c.getLabel().equals("rhel-x86_64-server-7"))
                .anyMatch(c -> c.getStatus().equals(MgrSyncStatus.INSTALLED)));
    }

    /**
     * Tests {@link ContentSyncManager#setupSourceURL} with a local filesystem link
     * using an URL pointing to an official SUSE server.
     * @throws Exception if anything goes wrong
     */
    public void testSetupSourceURLLocalFS() throws Exception {
        File reposJson = new File(TestUtils.findTestData(
                new File(JARPATH,  "smallBase/" + REPOS_JSON).getAbsolutePath()).getPath());
        File prdJson = new File(TestUtils.findTestData(
                new File(JARPATH,  "smallBase/" + PRODUCTS_JSON).getAbsolutePath()).getPath());
        File upJson = new File(TestUtils.findTestData(
                new File(JARPATH,  "smallBase/" + UPGRADE_PATHS_JSON).getAbsolutePath()).getPath());
        File treeJson = new File(TestUtils.findTestData(
                new File(JARPATH,  "smallBase/" + TREE_JSON).getAbsolutePath()).getPath());

        Path fromdir = Files.createTempDirectory("sumatest");
        File prdTmp = new File(fromdir.toString(), "organizations_products_unscoped.json");
        File repoTemp = new File(fromdir.toString(), "organizations_repositories.json");
        File upTemp = new File(fromdir.toString(), UPGRADE_PATHS_JSON);
        File treeTemp = new File(fromdir.toString(), TREE_JSON);
        Files.copy(prdJson.toPath(), prdTmp.toPath());
        Files.copy(reposJson.toPath(), repoTemp.toPath());
        Files.copy(upJson.toPath(), upTemp.toPath());
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
            InputStreamReader inputStreamReader3 = new InputStreamReader(ContentSyncManager.class.getResourceAsStream(JARPATH + "smallBase/channel_families.json"));
            List<ChannelFamilyJson> channelFamilies = gson.fromJson(inputStreamReader3, new TypeToken<List<ChannelFamilyJson>>() {}.getType());

            ContentSyncManager csm = new ContentSyncManager();
            csm.setUpgradePathsJson(upTemp);
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
                .filter(pr -> pr.isMandatory())
                .forEach(pr -> {
                    assertNotNull(pr.getRepository());
                    SCCRepositoryAuth bestAuth = pr.getRepository().getBestAuth().get();
                    ContentSource cs = bestAuth.getContentSource();
                    assertNotNull(cs);
                    assertEquals(bestAuth.getUrl(), cs.getSourceUrl());
                    assertContains(cs.getSourceUrl(), "file:" + fromdir.toString() + "/SUSE/");
                });
            slewe.getRepositories().stream()
            .filter(pr -> pr.isMandatory())
            .forEach(pr -> {
                assertNotNull(pr.getRepository());
                SCCRepositoryAuth bestAuth = pr.getRepository().getBestAuth().get();
                ContentSource cs = bestAuth.getContentSource();
                assertNotNull(cs);
                assertEquals(bestAuth.getUrl(), cs.getSourceUrl());
                if (pr.getChannelName().toLowerCase().contains("nvidia")) {
                    assertContains(cs.getSourceUrl(), "file:" + fromdir.toString() + "/repo/RPMMD/");
                }
                else {
                    assertContains(cs.getSourceUrl(), "file:" + fromdir.toString() + "/SUSE/");
                }
            });
        }
        finally {
            Config.get().remove(ContentSyncManager.RESOURCE_PATH);
            SUSEProductTestUtils.deleteIfTempFile(reposJson);
            SUSEProductTestUtils.deleteIfTempFile(prdJson);
            repoTemp.delete();
            prdTmp.delete();
            upTemp.delete();
            treeTemp.delete();
            FileUtils.deleteDirectory(fromdir.toFile());
        }
    }

    /**
     * Tests changing from fromdir to SCC
     * @throws Exception if something goes wrong
     */
    public void testSwitchFromdirToSCC() throws Exception {
        File reposJson = new File(TestUtils.findTestData(
                new File(JARPATH,  "smallBase/" + REPOS_JSON).getAbsolutePath()).getPath());
        File prdJson = new File(TestUtils.findTestData(
                new File(JARPATH,  "smallBase/" + PRODUCTS_JSON).getAbsolutePath()).getPath());
        File upJson = new File(TestUtils.findTestData(
                new File(JARPATH,  "smallBase/" + UPGRADE_PATHS_JSON).getAbsolutePath()).getPath());
        File treeJson = new File(TestUtils.findTestData(
                new File(JARPATH,  "smallBase/" + TREE_JSON).getAbsolutePath()).getPath());

        Path fromdir = Files.createTempDirectory("sumatest");
        File prdTmp = new File(fromdir.toString(), "organizations_products_unscoped.json");
        File repoTemp = new File(fromdir.toString(), "organizations_repositories.json");
        File upTemp = new File(fromdir.toString(), UPGRADE_PATHS_JSON);
        File treeTemp = new File(fromdir.toString(), TREE_JSON);
        Files.copy(prdJson.toPath(), prdTmp.toPath());
        Files.copy(reposJson.toPath(), repoTemp.toPath());
        Files.copy(upJson.toPath(), upTemp.toPath());
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
            InputStreamReader inputStreamReader3 = new InputStreamReader(ContentSyncManager.class.getResourceAsStream(JARPATH + "smallBase/channel_families.json"));
            List<ChannelFamilyJson> channelFamilies = gson.fromJson(inputStreamReader3, new TypeToken<List<ChannelFamilyJson>>() {}.getType());

            ContentSyncManager csm = new ContentSyncManager();
            csm.setUpgradePathsJson(upTemp);
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

            // remove "fromdir" config
            Config.get().remove(ContentSyncManager.RESOURCE_PATH);
            SUSEProductTestUtils.createVendorSUSEProductEnvironment(user, "/com/redhat/rhn/manager/content/test/smallBase", true);
            new ContentSyncManager().linkAndRefreshContentSource(null);
            HibernateFactory.getSession().flush();
            HibernateFactory.getSession().clear();

            sles = SUSEProductFactory.lookupByProductId(1117);
            slewe = SUSEProductFactory.lookupByProductId(1222);

            sles.getRepositories().stream()
                .filter(pr -> pr.isMandatory())
                .forEach(pr -> {
                    assertNotNull(pr.getRepository());
                    SCCRepositoryAuth bestAuth = pr.getRepository().getBestAuth().get();
                    ContentSource cs = bestAuth.getContentSource();
                    assertNotNull(cs);
                    assertEquals(bestAuth.getUrl(), cs.getSourceUrl());
                    assertContains(cs.getSourceUrl(), "https://updates.suse.com");
                });
            slewe.getRepositories().stream()
            .filter(pr -> pr.isMandatory())
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
            upTemp.delete();
            treeTemp.delete();
            FileUtils.deleteDirectory(fromdir.toFile());
        }
    }

    /**
     * Tests that the SUSEProductChannel class behaves correctly with Hibernate
     * (ensures there is no regression wrt bsc#932052).
     * @throws Exception if something goes wrong
     */
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
        for (Credentials creds : CredentialsFactory.lookupSCCCredentials()) {
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
    public void testUpdateProductsMultipleTimes() throws Exception {
        File upgradePathsJson = new File(
                TestUtils.findTestData(UPGRADE_PATHS_JSON).getPath());
        // clear existing products
        SUSEProductTestUtils.clearAllProducts();

        InputStreamReader inputStreamReader = new InputStreamReader(ContentSyncManager.class.getResourceAsStream(JARPATH + PRODUCTS_JSON));
        List<SCCProductJson> sccProducts =
                new Gson().fromJson(inputStreamReader,
                        new TypeToken<List<SCCProductJson>>() { } .getType());

        ContentSyncManager csm = new ContentSyncManager();
        csm.setSumaProductTreeJson(Optional.of(new File("/usr/share/susemanager/scc/product_tree.json")));
        csm.setUpgradePathsJson(upgradePathsJson);

        csm.updateSUSEProducts(sccProducts);
        csm.updateSUSEProducts(sccProducts);
    }

    /**
     * Test debian and repomd url building
     * @throws Exception
     */
    public void testBuildRepoFileUrl() throws Exception {
        SCCRepository debrepo = new SCCRepository();
        debrepo.setDistroTarget("amd64");
        SCCRepository rpmrepo = new SCCRepository();
        rpmrepo.setDistroTarget("sle-12-x86_64");

        String repourl = "http://localhost/pub/myrepo/";
        ContentSyncManager csm = new ContentSyncManager();

        assertEquals(repourl + "Release", csm.buildRepoFileUrl(repourl, debrepo));
        assertEquals(repourl + "repodata/repomd.xml", csm.buildRepoFileUrl(repourl, rpmrepo));
    }


    /**
     * {@inheritDoc}
     */
    @Override
    public void setUp() throws Exception {
        super.setUp();

        // Clear data for all tests
        clearCredentials();
        SCCCachingFactory.clearRepositories();
        renameVendorChannels();
    }
}
