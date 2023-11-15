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
package com.redhat.rhn.domain.product.test;

import com.redhat.rhn.common.conf.Config;
import com.redhat.rhn.common.hibernate.HibernateFactory;
import com.redhat.rhn.domain.channel.Channel;
import com.redhat.rhn.domain.channel.ChannelArch;
import com.redhat.rhn.domain.channel.ChannelFactory;
import com.redhat.rhn.domain.channel.ChannelFamily;
import com.redhat.rhn.domain.channel.ChannelFamilyFactory;
import com.redhat.rhn.domain.channel.test.ChannelFactoryTest;
import com.redhat.rhn.domain.channel.test.ChannelFamilyFactoryTest;
import com.redhat.rhn.domain.channel.test.ChannelFamilyTest;
import com.redhat.rhn.domain.common.ManagerInfoFactory;
import com.redhat.rhn.domain.credentials.CredentialsFactory;
import com.redhat.rhn.domain.credentials.SCCCredentials;
import com.redhat.rhn.domain.product.ReleaseStage;
import com.redhat.rhn.domain.product.SUSEProduct;
import com.redhat.rhn.domain.product.SUSEProductChannel;
import com.redhat.rhn.domain.product.SUSEProductFactory;
import com.redhat.rhn.domain.product.SUSEProductSCCRepository;
import com.redhat.rhn.domain.rhnpackage.PackageFactory;
import com.redhat.rhn.domain.scc.SCCRepository;
import com.redhat.rhn.domain.scc.SCCRepositoryAuth;
import com.redhat.rhn.domain.scc.SCCRepositoryTokenAuth;
import com.redhat.rhn.domain.server.InstalledProduct;
import com.redhat.rhn.domain.server.Server;
import com.redhat.rhn.domain.user.User;
import com.redhat.rhn.frontend.xmlrpc.sync.content.ContentSyncSource;
import com.redhat.rhn.frontend.xmlrpc.sync.content.LocalDirContentSyncSource;
import com.redhat.rhn.frontend.xmlrpc.sync.content.SCCContentSyncSource;
import com.redhat.rhn.manager.content.ContentSyncException;
import com.redhat.rhn.manager.content.ContentSyncManager;
import com.redhat.rhn.manager.content.ProductTreeEntry;
import com.redhat.rhn.testing.ChannelTestUtils;
import com.redhat.rhn.testing.TestUtils;

import com.suse.salt.netapi.parser.JsonParser;
import com.suse.scc.model.ChannelFamilyJson;
import com.suse.scc.model.SCCProductJson;
import com.suse.scc.model.SCCRepositoryJson;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.reflect.TypeToken;

import org.apache.commons.collections.CollectionUtils;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.hibernate.Session;

import java.io.File;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.file.Path;
import java.util.Collection;
import java.util.HashSet;
import java.util.List;
import java.util.Random;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * Utility methods for creating SUSE related test data.
 */
public class SUSEProductTestUtils extends HibernateFactory {

    private static final Random RANDOM = new Random();
    private static Logger log = LogManager.getLogger(SUSEProductTestUtils.class);

    /**
     * Not to be instantiated.
     */
    private SUSEProductTestUtils() {
    }

    /**
     * Create a SUSE product (which is different from a {@link com.redhat.rhn.domain.channel.ChannelProduct}).
     * @param family the channel family
     * @return the newly created SUSE product
     */
    public static SUSEProduct createTestSUSEProduct(ChannelFamily family) throws Exception {
        return createTestSUSEProduct(family, TestUtils.randomString().toLowerCase());
    }

    /**
     * Create a SUSE product (which is different from a {@link com.redhat.rhn.domain.channel.ChannelProduct}).
     * @param family the channel family
     * @param name the product name
     * @return the newly created SUSE product
     * @throws Exception if anything goes wrong
     */
    public static SUSEProduct createTestSUSEProduct(ChannelFamily family, String name) throws Exception {
        SUSEProduct product = new SUSEProduct();
        product.setName(name);
        product.setVersion("1");
        product.setFriendlyName("SUSE Test product " + name);
        product.setArch(PackageFactory.lookupPackageArchByLabel("x86_64"));
        product.setRelease("test");
        product.setProductId(RANDOM.nextInt(999999));
        product.setBase(true);
        product.setReleaseStage(ReleaseStage.released);
        product.setChannelFamily(family);

        product = TestUtils.saveAndReload(product);

        return product;
    }

    /**
     * Create a SUSEProduct for test with the given pieces of information.
     * @param user the user
     * @param name the name of the product
     * @param version the version
     * @param arch the architecture
     * @param family the channel family
     * @param isBase true if it is a base product
     * @return the created SUSEProduct
     */
    public static SUSEProduct createTestSUSEProduct(User user, String name, String version, String arch, String family,
                                                    boolean isBase) {
        SUSEProduct product = new SUSEProduct();

        product.setName(name);
        product.setVersion(version);
        product.setFriendlyName("SUSE Test product " + name);
        product.setArch(PackageFactory.lookupPackageArchByLabel(arch));
        product.setRelease("test");
        product.setProductId(RANDOM.nextInt(999999));
        product.setBase(isBase);
        product.setReleaseStage(ReleaseStage.released);
        product.setChannelFamily(ChannelFamilyTest.ensureChannelFamilyExists(user, family));

        product = TestUtils.saveAndReload(product);

        return product;
    }

    /**
     * Create SUSEProductSCCRepository for the product
     * @param baseProduct Base product
     * @param baseChannel base channe
     * @param product product
     * @param channel channel
     * @param user user
     */
    public static void populateRepository(SUSEProduct baseProduct, Channel baseChannel, SUSEProduct product,
                                    Channel channel, User user) {
        SCCCredentials sccc = SUSEProductTestUtils.createSCCCredentials("dummy", user);
        SCCRepository repository = SUSEProductTestUtils.createSCCRepository();
        SUSEProductTestUtils.createSCCRepositoryTokenAuth(sccc, repository);

        SUSEProductSCCRepository ltssSP1ProdRepo = new SUSEProductSCCRepository();
        ltssSP1ProdRepo.setRepository(repository);
        ltssSP1ProdRepo.setRootProduct(baseProduct);
        ltssSP1ProdRepo.setProduct(product);
        ltssSP1ProdRepo.setParentChannelLabel(baseChannel.getLabel());
        ltssSP1ProdRepo.setChannelName(baseChannel.getLabel());
        ltssSP1ProdRepo.setChannelLabel(channel.getLabel());
        ltssSP1ProdRepo.setMandatory(true);
        TestUtils.saveAndReload(ltssSP1ProdRepo);
    }

    /**
     * Return the InstalledProduct instance for a SUSEProduct
     * @param product SUSEProduct instance
     * @return InstalledProduct instance
     */
    public static InstalledProduct getInstalledProduct(SUSEProduct product) {
        InstalledProduct prod = new InstalledProduct();
        prod.setName(product.getName());
        prod.setVersion(product.getVersion());
        prod.setRelease(product.getRelease());
        prod.setArch(product.getArch());
        prod.setBaseproduct(product.isBase());
        return prod;
    }

    /**
     * Create a vendor channel (org is null) for testing.
     * @return vendor channel for testing
     * @throws Exception
     */
    public static Channel createTestVendorChannel() throws Exception {
        Channel c = ChannelFactoryTest.createTestChannel(null,
                ChannelFamilyFactoryTest.createNullOrgTestChannelFamily());
        ChannelFactory.save(c);
        return c;
    }

    /**
     * For a given file, delete it in case it is a temp file.
     * @param file test file to delete
     */
    public static void deleteIfTempFile(File file) {
        if (file.exists() && file.getAbsolutePath().startsWith(
                System.getProperty("java.io.tmpdir") + File.separatorChar)) {
            file.delete();
        }
    }

    /**
     * Create a SUSE product channel (that is, links a channel to a SUSE
     * product, eg. a row in suseproductchannel).
     * @param channel the channel
     * @param product the SUSE product
     * @param mandatory whether the channel is mandatory
     */
    public static void createTestSUSEProductChannel(Channel channel, SUSEProduct product, boolean mandatory) {
        Set<SUSEProductChannel> pcs = product.getSuseProductChannels();
        SUSEProductChannel pc = new SUSEProductChannel();
        pc.setChannel(channel);
        pc.setProduct(product);
        pc.setMandatory(mandatory);
        SUSEProductFactory.save(pc);
        pcs.add(pc);
        product.setSuseProductChannels(pcs);
    }

    /**
     * Marks one SUSE product as a possible upgrade of another.
     * @param from the first SUSE product
     * @param to the second SUSE product
     */
    public static void createTestSUSEUpgradePath(SUSEProduct from, SUSEProduct to) {
        Set<SUSEProduct> up = from.getUpgrades();
        up.add(to);
        from.setUpgrades(up);
        SUSEProductFactory.save(from);
    }

    /**
     * Link a {@link com.redhat.rhn.domain.product.SUSEProduct}
     * with a {@link com.redhat.rhn.domain.server.Server}.
     * @param product the product
     * @param server the server
     */
    public static void installSUSEProductOnServer(SUSEProduct product, Server server) {
        // Insert into suseInstalledProduct
        InstalledProduct prd = new InstalledProduct();
        prd.setName(product.getName());
        prd.setVersion(product.getVersion());
        prd.setRelease(product.getRelease());
        prd.setArch(product.getArch());
        prd.setBaseproduct(true);

        Set<InstalledProduct> products = new HashSet<>();
        products.add(prd);

        // Insert into suseServerInstalledProduct
        server.setInstalledProducts(products);
        HibernateFactory.getSession().flush();
    }

    /**
     * Install the specified products on the server
     * @param server the server
     * @param products the collection of products to install
     */
    public static void installSUSEProductsOnServer(Server server, Collection<SUSEProduct> products) {
        if (CollectionUtils.isEmpty(products)) {
            return;
        }

        products.stream()
                .map(product -> {
                    InstalledProduct prd = new InstalledProduct();

                    prd.setName(product.getName());
                    prd.setVersion(product.getVersion());
                    prd.setRelease(product.getRelease());
                    prd.setArch(product.getArch());
                    prd.setBaseproduct(product.isBase());

                    return prd;
                })
                .forEach(server::addInstalledProduct);

        HibernateFactory.getSession().flush();
    }

    /**
     * Create two standard SUSE Vendor products.
     *
     * SLES12 SP1 x86_64
     * SLE-HA12 SP1 x86_64
     * SLE-Micro 5.4 x86_64
     */
    public static void createVendorSUSEProducts() {
        ChannelFamily cfsles = ChannelFamilyFactory.lookupByLabel("7261", null);
        if (cfsles == null) {
            cfsles = new ChannelFamily();
            cfsles.setLabel("7261");
            cfsles.setName("SUSE Linux Enterprise Server");
            TestUtils.saveAndFlush(cfsles);
        }

        ChannelFamily cfha = ChannelFamilyFactory.lookupByLabel("SLE-HAE-X86", null);
        if (cfha == null) {
            cfha = new ChannelFamily();
            cfha.setLabel("SLE-HAE-X86");
            cfha.setName("SUSE Linux Enterprise High Availability Extension (x86)");
            TestUtils.saveAndFlush(cfha);
        }

        ChannelFamily cfslem = ChannelFamilyFactory.lookupByLabel("MICROOS-X86", null);
        if (cfslem == null) {
            cfslem = new ChannelFamily();
            cfslem.setLabel("MICROOS-X86");
            cfslem.setName("SUSE Linux Enterprise Micro X86");
            TestUtils.saveAndFlush(cfslem);
        }

        SUSEProduct product = new SUSEProduct();
        product.setName("sles");
        product.setVersion("12.1");
        product.setFriendlyName("SUSE Linux Enterprise Server 12 SP1");
        product.setArch(PackageFactory.lookupPackageArchByLabel("x86_64"));
        product.setProductId(1322);
        product.setChannelFamily(cfsles);
        product.setBase(true);
        product.setReleaseStage(ReleaseStage.released);
        TestUtils.saveAndFlush(product);

        product = new SUSEProduct();
        product.setName("sle-ha");
        product.setVersion("12.1");
        product.setFriendlyName("SUSE Linux Enterprise High Availability Extension 12 SP1");
        product.setArch(PackageFactory.lookupPackageArchByLabel("x86_64"));
        product.setProductId(1324);
        product.setChannelFamily(cfha);
        product.setReleaseStage(ReleaseStage.released);
        TestUtils.saveAndFlush(product);

        product = new SUSEProduct();
        product.setName("sles");
        product.setVersion("15.1");
        product.setFriendlyName("SUSE Linux Enterprise Server 15 SP1");
        product.setArch(PackageFactory.lookupPackageArchByLabel("x86_64"));
        product.setProductId(1326);
        product.setChannelFamily(cfsles);
        product.setBase(true);
        product.setReleaseStage(ReleaseStage.released);
        TestUtils.saveAndFlush(product);

        product = new SUSEProduct();
        product.setName("sle-module-basesystem");
        product.setVersion("15.1");
        product.setFriendlyName("Basesystem Module");
        product.setArch(PackageFactory.lookupPackageArchByLabel("x86_64"));
        product.setProductId(1328);
        product.setChannelFamily(cfsles);
        product.setBase(false);
        product.setReleaseStage(ReleaseStage.released);
        TestUtils.saveAndFlush(product);

        product = new SUSEProduct();
        product.setName("sle-module-server-applications");
        product.setVersion("15.1");
        product.setFriendlyName("Server Applications Module");
        product.setArch(PackageFactory.lookupPackageArchByLabel("x86_64"));
        product.setProductId(1330);
        product.setChannelFamily(cfsles);
        product.setBase(false);
        product.setReleaseStage(ReleaseStage.released);
        TestUtils.saveAndFlush(product);

        product = new SUSEProduct();
        product.setName("sle-module-containers");
        product.setVersion("15.1");
        product.setFriendlyName("Containers Module");
        product.setArch(PackageFactory.lookupPackageArchByLabel("x86_64"));
        product.setProductId(1332);
        product.setChannelFamily(cfsles);
        product.setBase(false);
        product.setReleaseStage(ReleaseStage.released);
        TestUtils.saveAndFlush(product);

        product = new SUSEProduct();
        product.setName("caasp");
        product.setVersion("4.0");
        product.setFriendlyName("SUSE CaaS Platform 4.0");
        product.setArch(PackageFactory.lookupPackageArchByLabel("x86_64"));
        product.setProductId(1340);
        //product.setChannelFamily(cfsles);
        product.setBase(false);
        product.setReleaseStage(ReleaseStage.released);
        TestUtils.saveAndFlush(product);

        product = new SUSEProduct();
        product.setName("sle-micro");
        product.setVersion("5.4");
        product.setFriendlyName("SUSE Linux Enterprise Micro 5.4 x86_64");
        product.setArch(PackageFactory.lookupPackageArchByLabel("x86_64"));
        product.setProductId(2574);
        product.setChannelFamily(cfslem);
        product.setBase(true);
        product.setReleaseStage(ReleaseStage.released);
        TestUtils.saveAndFlush(product);

        ChannelFamily cfSlesSap = ChannelFamilyFactory.lookupByLabel("AiO", null);
        if (cfSlesSap == null) {
            cfSlesSap = new ChannelFamily();
            cfSlesSap.setLabel("AiO");
            cfSlesSap.setName("SUSE Linux Enterprise Server for SAP");
            TestUtils.saveAndFlush(cfSlesSap);
        }

        product = new SUSEProduct();
        product.setName("sles_sap");
        product.setVersion("15.5");
        product.setFriendlyName("SUSE Linux Enterprise Server for SAP Applications 15 SP5 x86_64");
        product.setArch(PackageFactory.lookupPackageArchByLabel("x86_64"));
        product.setProductId(2467);
        product.setChannelFamily(cfSlesSap);
        product.setBase(true);
        product.setReleaseStage(ReleaseStage.released);
        TestUtils.saveAndFlush(product);

        product = new SUSEProduct();
        product.setName("sle-module-certifications");
        product.setVersion("15.5");
        product.setFriendlyName("Certifications Module 15 SP5 x86_64");
        product.setArch(PackageFactory.lookupPackageArchByLabel("x86_64"));
        product.setProductId(2558);
        product.setChannelFamily(cfsles);
        product.setBase(false);
        product.setReleaseStage(ReleaseStage.released);
        TestUtils.saveAndFlush(product);

        product = new SUSEProduct();
        product.setName("sle-module-basesystem");
        product.setVersion("15.5");
        product.setFriendlyName("Basesystem Module 15 SP5 x86_64");
        product.setArch(PackageFactory.lookupPackageArchByLabel("x86_64"));
        product.setProductId(2474);
        product.setChannelFamily(cfsles);
        product.setBase(false);
        product.setReleaseStage(ReleaseStage.released);
        TestUtils.saveAndFlush(product);
    }

    /**
     * Create the SUSE Manager Tools product.
     */
    public static void createSUMAToolsProduct() {
        ChannelFamily toolsChannelFamily = ChannelFamilyFactory.lookupByLabel("SLE-M-T", null);
        if (toolsChannelFamily == null) {
            toolsChannelFamily = new ChannelFamily();
            toolsChannelFamily.setLabel("SLE-M-T");
            toolsChannelFamily.setName("SUSE Manager Tools");
            TestUtils.saveAndFlush(toolsChannelFamily);
        }

        SUSEProduct product = new SUSEProduct();
        product.setName("sle-manager-tools");
        product.setVersion("12");
        product.setRelease("0");
        product.setFriendlyName("SUSE Manager Tools 12");
        product.setArch(PackageFactory.lookupPackageArchByLabel("x86_64"));
        product.setProductId(1248L);
        product.setChannelFamily(toolsChannelFamily);
        product.setBase(false);
        product.setReleaseStage(ReleaseStage.released);
        TestUtils.saveAndFlush(product);
    }

    public static Channel createBaseChannelForBaseProduct(SUSEProduct product, User admin) throws Exception {
        ChannelArch channelArch = ChannelFactory.findArchByLabel("channel-x86_64");
        Channel channel = ChannelTestUtils.createBaseChannel(admin);
        channel.setChannelArch(channelArch);
        channel.setName("Channel for " + product.getFriendlyName());
        channel.setOrg(null);
        channel = TestUtils.saveAndReload(channel);
        SUSEProductTestUtils.createTestSUSEProductChannel(channel, product, true);
        return channel;
    }

    public static Channel createChildChannelsForProduct(SUSEProduct product, Channel baseChannel, User admin)
            throws Exception {
        ChannelArch channelArch = ChannelFactory.findArchByLabel("channel-x86_64");
        Channel channel = ChannelFactoryTest.createTestChannel(admin);
        channel.setChannelArch(channelArch);
        channel.setParentChannel(baseChannel);
        channel.setName("Channel for " + product.getFriendlyName());
        channel.setOrg(null);
        channel = TestUtils.saveAndReload(channel);
        SUSEProductTestUtils.createTestSUSEProductChannel(channel, product, true);
        return channel;
    }

    /**
     * Create some SUSE Vendor products with channels
     *
     * SLES12 SP1 x86_64
     * SLE-HA12 SP1 x86_64
     * @param admin the user
     * @param testDataPath the path to test data
     * @param withRepos set true if repos should be added
     * @throws Exception
     */
    public static void createVendorSUSEProductEnvironment(User admin, String testDataPath, boolean withRepos)
            throws Exception {
        createVendorSUSEProductEnvironment(admin, testDataPath, withRepos, false);
    }

    /**
     * Create some SUSE Vendor products with channels
     *
     * @param admin the user
     * @param testDataPath the path to test data
     * @param withRepos set true if repos should be added
     * @param fromdir set true if fromdir option should be simulated
     */
    public static void createVendorSUSEProductEnvironment(User admin, String testDataPath, boolean withRepos,
                                                          boolean fromdir) {
        Gson gson = new GsonBuilder()
                .setDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSSX")
                .create();
        if (testDataPath == null) {
            testDataPath = "/com/redhat/rhn/manager/content/test/";
        }
        else if (!testDataPath.endsWith("/")) {
            testDataPath = testDataPath + "/";
        }
        InputStreamReader inputStreamReader = new InputStreamReader(
                ContentSyncManager.class.getResourceAsStream(testDataPath + "productsUnscoped.json"));
        List<SCCProductJson> products = gson.fromJson(
                inputStreamReader, new TypeToken<List<SCCProductJson>>() { }.getType());
        InputStreamReader inputStreamReader3 = new InputStreamReader(
                ContentSyncManager.class.getResourceAsStream(testDataPath + "channel_families.json"));
        List<ChannelFamilyJson> channelFamilies = gson.fromJson(
                inputStreamReader3, new TypeToken<List<ChannelFamilyJson>>() { }.getType());
        InputStreamReader inputStreamReader4 = new InputStreamReader(
                ContentSyncManager.class.getResourceAsStream(testDataPath + "product_tree.json"));
        List<ProductTreeEntry> staticTree = JsonParser.GSON.fromJson(
                inputStreamReader4, new TypeToken<List<ProductTreeEntry>>() { }.getType());
        InputStreamReader inputStreamReader5 = new InputStreamReader(
                ContentSyncManager.class.getResourceAsStream(testDataPath + "repositories.json"));
        List<SCCRepositoryJson> repositories = gson.fromJson(
                inputStreamReader5, new TypeToken<List<SCCRepositoryJson>>() { }.getType());

        InputStreamReader inputStreamReader6 = new InputStreamReader(
                ContentSyncManager.class.getResourceAsStream(testDataPath + "additional_repositories.json"));
        List<SCCRepositoryJson> addRepos = gson.fromJson(
                inputStreamReader6, new TypeToken<List<SCCRepositoryJson>>() { }.getType());
        InputStream pres = ContentSyncManager.class.getResourceAsStream(testDataPath + "additional_products.json");
        if (pres != null) {
            InputStreamReader inputStreamReader7 = new InputStreamReader(pres);
            List<SCCProductJson> addProducts = gson.fromJson(
                    inputStreamReader7, new TypeToken<List<SCCProductJson>>() { }.getType());
            products.addAll(addProducts);
            addRepos.addAll(ContentSyncManager.collectRepos(
                    ContentSyncManager.flattenProducts(addProducts).collect(Collectors.toList())));
        }
        repositories.addAll(addRepos);

        ContentSyncManager csm = new ContentSyncManager() {
            @Override
            protected boolean accessibleUrl(String url, String user, String password) {
                // allow all none SCC URLs
                return true;
            }
        };
        ContentSyncSource contentSyncSource = null;
        if (fromdir) {
            Config.get().setString(ContentSyncManager.RESOURCE_PATH, "sumatest");
            csm = new ContentSyncManager() {
                @Override
                protected boolean accessibleUrl(String url, String user, String password) {
                    // allow all none SCC URLs
                    return true;
                }
            };

            contentSyncSource = new LocalDirContentSyncSource(Path.of("sumatest"));
        }
        else {
            SCCCredentials credentials = CredentialsFactory.createSCCCredentials("dummy", "dummy");
            credentials.setUrl("dummy");
            credentials.setUser(admin);
            CredentialsFactory.storeCredentials(credentials);

            contentSyncSource = new SCCContentSyncSource(credentials);
        }

        csm.updateChannelFamilies(channelFamilies);
        csm.updateSUSEProducts(products, staticTree, addRepos);
        if (withRepos) {
            HibernateFactory.getSession().flush();
            HibernateFactory.getSession().clear();
            csm.refreshRepositoriesAuthentication(repositories, contentSyncSource, null);
        }
        ManagerInfoFactory.setLastMgrSyncRefresh();
    }

    public static void addChannelsForProduct(SUSEProduct product) {
        ContentSyncManager csm = new ContentSyncManager();
        product.getRepositories()
        .stream()
        .filter(SUSEProductSCCRepository::isMandatory)
        .forEach(pr -> {
            try {
                if (pr.getParentChannelLabel() != null &&
                        ChannelFactory.lookupByLabel(pr.getParentChannelLabel()) == null) {
                    csm.addChannel(pr.getParentChannelLabel(), null);
                }
                csm.addChannel(pr.getChannelLabel(), null);
            }
            catch (ContentSyncException e) {
                log.error("unable to add channel", e);
                throw new RuntimeException(e);
            }
        });
    }

    /**
     * Add channels to given product
     * @param product the product
     * @param root the root product
     * @param mandatory add mandatory channels
     * @param optionalChannelIds list of optional channels ids to add
     */
    public static void addChannelsForProductAndParent(SUSEProduct product, SUSEProduct root,
            boolean mandatory, List<Long> optionalChannelIds) {
        ContentSyncManager csm = new ContentSyncManager();
        product.getRepositories()
        .stream()
        .filter(pr -> pr.getRootProduct().equals(root))
        .filter(pr -> (mandatory && pr.isMandatory()) || optionalChannelIds.contains(pr.getRepository().getSccId()))
        .forEach(pr -> {
            try {
                if (pr.getParentChannelLabel() != null &&
                        ChannelFactory.lookupByLabel(pr.getParentChannelLabel()) == null) {
                    csm.addChannel(pr.getParentChannelLabel(), null);
                }
                csm.addChannel(pr.getChannelLabel(), null);
            }
            catch (ContentSyncException e) {
                log.error("unable to add channel", e);
                throw new RuntimeException(e);
            }
        });
    }

    /**
     * Create standard SUSE Vendor Entitlement products.
     */
    public static void createVendorEntitlementProducts() {
        SUSEProduct product = new SUSEProduct();
        product.setName("suse-manager-mgmt-unlimited-virtual-z");
        product.setVersion("1.2");
        product.setFriendlyName("SUSE Manager Mgmt Unlimited Virtual Z 1.2");
        product.setProductId(1200);
        product.setReleaseStage(ReleaseStage.released);
        TestUtils.saveAndFlush(product);

        product = new SUSEProduct();
        product.setName("suse-manager-prov-unlimited-virtual-z");
        product.setVersion("1.2");
        product.setFriendlyName("SUSE Manager Prov Unlimited Virtual Z 1.2");
        product.setProductId(1205);
        product.setReleaseStage(ReleaseStage.released);
        TestUtils.saveAndFlush(product);

        product = new SUSEProduct();
        product.setName("suse-manager-mgmt-unlimited-virtual");
        product.setVersion("1.2");
        product.setFriendlyName("SUSE Manager Mgmt Unlimited Virtual 1.2");
        product.setProductId(1078);
        product.setReleaseStage(ReleaseStage.released);
        TestUtils.saveAndFlush(product);

        product = new SUSEProduct();
        product.setName("suse-manager-prov-unlimited-virtual");
        product.setVersion("1.2");
        product.setFriendlyName("SUSE Manager Prov Unlimited Virtual 1.2");
        product.setProductId(1204);
        product.setReleaseStage(ReleaseStage.released);
        TestUtils.saveAndFlush(product);

        product = new SUSEProduct();
        product.setName("suse-manager-mgmt-single");
        product.setVersion("1.2");
        product.setFriendlyName("SUSE Manager Mgmt Single 1.2");
        product.setProductId(1076);
        product.setReleaseStage(ReleaseStage.released);
        TestUtils.saveAndFlush(product);

        product = new SUSEProduct();
        product.setName("suse-manager-prov-single");
        product.setVersion("1.2");
        product.setFriendlyName("SUSE Manager Prov Single 1.2");
        product.setProductId(1097);
        product.setReleaseStage(ReleaseStage.released);
        TestUtils.saveAndFlush(product);

        product = new SUSEProduct();
        product.setName("suse-manager-mon-single");
        product.setVersion("1.2");
        product.setFriendlyName("SUSE Manager Monitoring Single 1.2");
        product.setProductId(1201);
        product.setReleaseStage(ReleaseStage.released);
        TestUtils.saveAndFlush(product);

        product = new SUSEProduct();
        product.setName("suse-manager-mon-unlimited-virtual");
        product.setVersion("1.2");
        product.setFriendlyName("SUSE Manager Monitoring Unlimited Virtual 1.2");
        product.setProductId(1202);
        product.setReleaseStage(ReleaseStage.released);
        TestUtils.saveAndFlush(product);

        product = new SUSEProduct();
        product.setName("suse-manager-mon-unlimited-virtual-z");
        product.setVersion("1.2");
        product.setFriendlyName("SUSE Manager Monitoring Unlimited Virtual Z 1.2");
        product.setProductId(1203);
        product.setReleaseStage(ReleaseStage.released);
        TestUtils.saveAndFlush(product);
    }

    /**
     * Resets all product data.
     */
    public static void clearAllProducts() {
        Session session = getSession();
        session.getNamedQuery("SUSEProductChannel.clear").executeUpdate();
        session.getNamedQuery("SUSEProduct.clear").executeUpdate();
        session.getNamedQuery("SUSEProductExtension.clear").executeUpdate();
    }

    /**
     * Create a SCCRepository
     * @return a SCCRepository
     */
    public static SCCRepository createSCCRepository() {
        SCCRepository bRepo = new SCCRepository();
        bRepo.setSccId(RANDOM.nextLong());
        bRepo.setAutorefresh(true);
        bRepo.setDescription(TestUtils.randomString());
        bRepo.setDistroTarget("sle-15-x86_64");
        bRepo.setName(TestUtils.randomString().toLowerCase());
        bRepo.setUrl("https://dummy.domain.top/" + TestUtils.randomString().toLowerCase());

        return TestUtils.saveAndReload(bRepo);
    }

    public static SCCRepositoryAuth createSCCRepositoryTokenAuth(SCCCredentials c, SCCRepository r) {
        SCCRepositoryAuth auth = new SCCRepositoryTokenAuth(TestUtils.randomString().toLowerCase());
        auth.setRepo(r);
        auth.setCredentials(c);
        return TestUtils.saveAndReload(auth);
    }

    public static SCCCredentials createSCCCredentials(String name, User user) {
        SCCCredentials credentials = createSecondarySCCCredentials(name, user);
        CredentialsFactory.storeCredentials(credentials);
        return credentials;
    }

    public static SCCCredentials createSecondarySCCCredentials(String name, User user) {
        SCCCredentials credentials = CredentialsFactory.createSCCCredentials(name, TestUtils.randomString());
        credentials.setUser(user);
        CredentialsFactory.storeCredentials(credentials);
        return credentials;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    protected Logger getLogger() {
        return log;
    }
}
