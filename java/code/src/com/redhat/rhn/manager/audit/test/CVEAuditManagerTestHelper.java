package com.redhat.rhn.manager.audit.test;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import com.redhat.rhn.common.db.datasource.DataResult;
import com.redhat.rhn.common.db.datasource.ModeFactory;
import com.redhat.rhn.common.db.datasource.SelectMode;
import com.redhat.rhn.common.db.datasource.WriteMode;
import com.redhat.rhn.common.hibernate.HibernateFactory;
import com.redhat.rhn.domain.channel.Channel;
import com.redhat.rhn.domain.channel.ChannelFamily;
import com.redhat.rhn.domain.channel.ChannelFamilyFactory;
import com.redhat.rhn.domain.channel.ChannelProduct;
import com.redhat.rhn.domain.channel.test.ChannelFactoryTest;
import com.redhat.rhn.domain.errata.Cve;
import com.redhat.rhn.domain.errata.Errata;
import com.redhat.rhn.domain.errata.impl.PublishedClonedErrata;
import com.redhat.rhn.domain.errata.test.ErrataFactoryTest;
import com.redhat.rhn.domain.product.SUSEProduct;
import com.redhat.rhn.domain.rhnpackage.Package;
import com.redhat.rhn.domain.rhnpackage.PackageEvr;
import com.redhat.rhn.domain.rhnpackage.PackageEvrFactory;
import com.redhat.rhn.domain.rhnpackage.PackageFactory;
import com.redhat.rhn.domain.rhnpackage.test.PackageTest;
import com.redhat.rhn.domain.role.RoleFactory;
import com.redhat.rhn.domain.server.InstalledPackage;
import com.redhat.rhn.domain.server.Server;
import com.redhat.rhn.domain.server.ServerConstants;
import com.redhat.rhn.domain.server.ServerFactory;
import com.redhat.rhn.domain.server.test.ServerFactoryTest;
import com.redhat.rhn.domain.user.User;
import com.redhat.rhn.frontend.action.channel.manage.PublishErrataHelper;
import com.redhat.rhn.manager.audit.ServerChannelIdPair;
import com.redhat.rhn.testing.TestUtils;
import com.redhat.rhn.testing.UserTestUtils;

/**
 * A collection of utility methods for testing.
 *
 * @version $Rev$
 */
public class CVEAuditManagerTestHelper {

    /**
     * Not to be instantiated.
     */
    private CVEAuditManagerTestHelper() {
    }

    /**
     * Create a {@link User} for the "testorg" organization.
     * @return the newly created user
     */
    public static User createTestUser() {
        User user = UserTestUtils.findNewUser("testuser", "testorg");
        user.addPermanentRole(RoleFactory.ORG_ADMIN);
        return user;
    }

    /**
     * Create a {@link Server} owned by a user, without channels.
     * @param user the user
     * @return the newly created server
     * @throws Exception if anything goes wrong
     */
    public static Server createTestServer(User user) throws Exception {
        return createTestServer(user, new ArrayList<Channel>());
    }

    /**
     * Create a {@link Channel}.
     * @param user the user
     * @return the newly created channel
     * @throws Exception if anything goes wrong
     */
    public static Channel createTestChannel(User user) throws Exception {
        return ChannelFactoryTest.createTestChannel(user);
    }

    /**
     * Create a CVE audit-relevant channel (a line in suseCVEServerChannel).
     * @param server the server
     * @param channel the channel which is relevant
     */
    public static void createTestRelevantChannel(Server server, Channel channel) {
        WriteMode m = ModeFactory.getWriteMode("cve_audit_queries",
                "insert_relevant_channel");

        Map<String, Long> parameters = new HashMap<String, Long>(2);
        parameters.put("sid", server.getId());
        parameters.put("cid", channel.getId());

        m.executeUpdate(parameters);
        HibernateFactory.getSession().flush();
    }

    /**
     * Create a {@link ChannelProduct}.
     * @return the newly created channel product
     * @throws Exception if anything goes wrong
     */
    public static ChannelProduct createTestChannelProduct() throws Exception {
        ChannelProduct product = new ChannelProduct();
        product.setProduct("ChannelProduct" + TestUtils.randomString());
        product.setVersion("11.3");
        product.setBeta("N");
        TestUtils.saveAndFlush(product);
        return product;
    }

    /**
     * Create a child {@link Channel} of an existing channel.
     * @param user the user
     * @param parent the parent channel
     * @return the newly created channel
     * @throws Exception if anything goes wrong
     */
    public static Channel createTestChannel(User user, Channel parent) throws Exception {
        Channel result = createTestChannel(user);
        result.setParentChannel(parent);
        TestUtils.saveAndFlush(result);
        TestUtils.saveAndFlush(parent);

        return result;
    }

    /**
     * Create a SUSE product (which is different from a {@link ChannelProduct}).
     * @param family the channel family
     * @return the newly created channel product
     * @throws Exception if anything goes wrong
     */
    public static SUSEProduct createTestSUSEProduct(ChannelFamily family) throws Exception {
        SUSEProduct product = new SUSEProduct();
        String name = TestUtils.randomString().toLowerCase();
        product.setName(name);
        product.setVersion("1");
        product.setFriendlyName("SUSE Test product " + name);
        product.setArch(PackageFactory.lookupPackageArchByLabel("x86_64"));
        product.setRelease("test");
        product.setChannelFamilyId(family.getId().toString());
        product.setProductList('Y');
        product.setProductId(0);

        TestUtils.saveAndFlush(product);

        return product;
    }

    /**
     * Create a SUSE product channel (that is, links a channel to a SUSE
     * product, eg. a row in suseproductchannel).
     * @param channel the channel
     * @param product the SUSE product
     */
    public static void createTestSUSEProductChannel(Channel channel, SUSEProduct product) {
        WriteMode m = ModeFactory.getWriteMode("test_queries",
                "insert_into_suseproductchannel");

        Channel parentChannel = channel.getParentChannel();

        Map<String, Object> parameters = new HashMap<String, Object>();
        parameters.put("product_id", product.getId());
        parameters.put("channel_id", channel.getId());
        parameters.put("channel_label", channel.getLabel());
        parameters.put("parent_channel_label",
                parentChannel != null ? parentChannel.getLabel() : null);

        m.executeUpdate(parameters);
        HibernateFactory.getSession().flush();
    }

    /**
     * Create a {@link ChannelFamily}.
     * @return the newly created channel family
     * @throws Exception if anything goes wrong
     */
    public static ChannelFamily createTestChannelFamily() throws Exception {
        String label = "ChannelFamilyLabel" + TestUtils.randomString();
        String name = "ChannelFamilyName" + TestUtils.randomString();
        String productUrl = "http://www.example.com";

        ChannelFamily channelFamily = new ChannelFamily();
        channelFamily.setOrg(null);
        channelFamily.setLabel(label);
        channelFamily.setName(name);
        channelFamily.setProductUrl(productUrl);

        ChannelFamilyFactory.save(channelFamily);
        channelFamily = (ChannelFamily) TestUtils.reload(channelFamily);
        return channelFamily;
    }

    /**
     * Marks one SUSE product as a possible upgrade of another.
     * @param from the first SUSE product
     * @param to the second SUSE product
     */
    public static void createTestSUSEUpgradePath(SUSEProduct from, SUSEProduct to) {
        WriteMode m = ModeFactory.getWriteMode("test_queries",
                "insert_into_suseupgradepath");

        Map<String, Object> parameters = new HashMap<String, Object>();
        parameters.put("from_pdid", from.getId());
        parameters.put("to_pdid", to.getId());

        m.executeUpdate(parameters);
        HibernateFactory.getSession().flush();
    }

    /**
     * Create a {@link Server} owned by a user, with channels.
     * @param user the user
     * @param channels the channels
     * @return the newly created server
     * @throws Exception if anything goes wrong
     */
    public static Server createTestServer(User user, Collection<Channel> channels)
            throws Exception {
        Server server = ServerFactoryTest.createTestServer(user, true,
                ServerConstants.getServerGroupTypeEnterpriseEntitled(),
                ServerFactoryTest.TYPE_SERVER_NORMAL);

        for (Channel channel : channels) {
            server.addChannel(channel);
        }
        TestUtils.saveAndFlush(server);

        return server;
    }

    /**
     * Create a vendor base channel for a given {@link ChannelFamily} and
     * {@link ChannelProduct}.
     * @param channelFamily channelFamily
     * @param channelProduct channelProduct
     * @return the new vendor base channel
     * @throws Exception if anything goes wrong
     */
    public static Channel createTestVendorBaseChannel(ChannelFamily channelFamily,
            ChannelProduct channelProduct) throws Exception {
        Channel channel = ChannelFactoryTest.
                createTestChannel(null, channelFamily);
        channel.setProduct(channelProduct);
        TestUtils.saveAndFlush(channel);
        return channel;
    }

    /**
     * Create a vendor child channel for a given {@link ChannelFamily} and
     * {@link ChannelProduct}.
     * @param parent the parent channel
     * @param channelProduct channelProduct
     * @return the new vendor child channel
     * @throws Exception if anything goes wrong
     */
    public static Channel createTestVendorChildChannel(Channel parent,
            ChannelProduct channelProduct) throws Exception {
        Channel channel = ChannelFactoryTest.
                createTestChannel(null, parent.getChannelFamily());
        channel.setParentChannel(parent);
        channel.setProduct(channelProduct);
        TestUtils.saveAndFlush(channel);
        return channel;
    }

    /**
     * Link a {@link SUSEProduct} with a {@link Server}.
     * @param product the product
     * @param server the server
     */
    @SuppressWarnings("unchecked")
    public static void installSUSEProductOnServer(SUSEProduct product, Server server) {
        // Get the next id from the sequence
        SelectMode selectMode = ModeFactory.getMode("test_queries",
                "select_next_suseinstalledproduct_id");
        DataResult<Map<String, Long>> dataResults = selectMode.execute();
        Long installedProductId = dataResults.get(0).get("id");

        // Insert into suseInstalledProduct
        WriteMode writeMode1 = ModeFactory.getWriteMode("test_queries",
                "insert_into_suseinstalledproduct");
        Map<String, Object> params1 = new HashMap<String, Object>();
        params1.put("id", installedProductId);
        params1.put("name", product.getName());
        params1.put("version", product.getVersion());
        params1.put("arch_type_id", product.getArch().getId());
        params1.put("release", product.getRelease());
        params1.put("is_baseproduct", "Y");
        writeMode1.executeUpdate(params1);

        // Insert into suseServerInstalledProduct
        WriteMode writeMode2 = ModeFactory.getWriteMode("test_queries",
                "insert_into_suseserverinstalledproduct");
        Map<String, Object> params2 = new HashMap<String, Object>();
        params2.put("rhn_server_id", server.getId());
        params2.put("suse_installed_product_id", installedProductId);
        writeMode2.executeUpdate(params2);
        HibernateFactory.getSession().flush();
    }

    /**
     * Create a {@link Cve}.
     * @param name the vulnerability identifier
     * @return the newly created CVE object
     * @throws Exception if anything goes wrong
     */
    public static Cve createTestCve(String name) throws Exception {
        Cve result = new Cve();
        result.setName(name);
        TestUtils.saveAndFlush(result);

        return result;
    }

    /**
     * Create a {@link Errata}.
     * @param user the errata owner
     * @param cves a set of CVE vulnerabilities fixed by this errata
     * @return the newly created errata
     * @throws Exception if anything goes wrong
     */
    public static Errata createTestErrata(User user, Set<Cve> cves)
            throws Exception {
        Errata result = ErrataFactoryTest.createTestErrata(user.getOrg().getId());
        result.setCves(cves);
        TestUtils.saveAndFlush(result);

        return result;
    }

    /**
     * Create a clone of a given {@link Errata}.
     * @param user the errata owner
     * @param original the original errata to clone from
     * @param cves a set of CVE vulnerabilities fixed by this errata
     * @param aPackage a package to include in the cloned errata
     * @return the newly created errata
     * @throws Exception if anything goes wrong
     */
    public static Errata createTestClonedErrata(User user, Errata original, Set<Cve> cves,
            Package aPackage) throws Exception {
        PublishedClonedErrata clone = new PublishedClonedErrata();
        copyErrataDetails(clone, original);
        PublishErrataHelper.setUniqueAdvisoryCloneName(original, clone);
        clone.setOriginal(original);
        clone.setOrg(user.getOrg());
        clone.setCves(cves);
        clone.addPackage(aPackage);
        TestUtils.saveAndFlush(clone);
        return clone;
    }

    /**
     * Create a {@link Channel}.
     * @param user the user
     * @param errata an errata in the channel
     * @return the newly created channel
     * @throws Exception if anything goes wrong
     */
    public static Channel createTestChannel(User user, Errata errata) throws Exception {
        Channel result = ChannelFactoryTest.createTestChannel(user);

        result.addErrata(errata);
        TestUtils.saveAndFlush(result);

        return result;
    }

    /**
     * Create a clone of a {@link Channel}.
     * @param user the user
     * @param errata an errata in the channel
     * @param original the channel to clone from
     * @param packages packages to include in the cloned channel
     * @return the newly created channel
     * @throws Exception if anything goes wrong
     */
    @SuppressWarnings("deprecation")
    public static Channel createTestClonedChannel(User user, Errata errata,
            Channel original, Collection<Package> packages) throws Exception {
        Channel clonedChannel = ChannelFactoryTest.createTestClonedChannel(original, user);
        clonedChannel.addErrata(errata);

        for (Package package1 : packages) {
            clonedChannel.addPackage(package1);
            TestUtils.saveAndFlush(clonedChannel);
        }

        TestUtils.saveAndFlush(clonedChannel);
        return clonedChannel;
    }

    /**
     * Create a {@link Package}.
     * @param user the package owner
     * @param channel the channel in which the new package is to be published
     * @param arch the package architecture label
     * @return the newly created patch
     * @throws Exception if anything goes wrong
     */
    public static Package createTestPackage(User user, Channel channel,
            String arch) throws Exception {
        return createTestPackage(user, null, channel, arch);
    }

    /**
     * Create a {@link Package}.
     * @param user the package owner
     * @param errata an errata that will contain the new package
     * @param channel the channel in which the new package is to be published
     * @param arch the package architecture label
     * @return the newly created patch
     * @throws Exception if anything goes wrong
     */
    public static Package createTestPackage(User user, Errata errata, Channel channel,
            String arch) throws Exception {
        Package result = PackageTest.createTestPackage(user.getOrg());
        result.setPackageArch(PackageFactory.lookupPackageArchByLabel(arch));

        List<Long> list = new ArrayList<Long>(1);
        list.add(result.getId());
        Map<String, Long> params = new HashMap<String, Long>();
        params.put("cid", channel.getId());
        WriteMode m = ModeFactory.getWriteMode("Channel_queries", "add_channel_packages");
        m.executeUpdate(params, list);
        HibernateFactory.getSession().refresh(channel);

        TestUtils.saveAndFlush(channel);

        if (errata != null) {
            errata.addPackage(result);
            TestUtils.saveAndFlush(errata);
        }

        return result;
    }

    /**
     * Create a {@link Package} which has a greater version number than another.
     * @param user the package owner
     * @param errata an errata that will contain the new package
     * @param channel the channel in which the new package is to be published
     * @param previous the previous channel
     * @return the newly created patch
     * @throws Exception if anything goes wrong
     */
    public static Package createLaterTestPackage(User user, Errata errata, Channel channel,
            Package previous) throws Exception {
        Package result =
                createTestPackage(user, errata, channel, previous.getPackageArch()
                        .getLabel());

        PackageEvr previousEvr = previous.getPackageEvr();
        String epoch = previousEvr.getEpoch();
        String version = previousEvr.getVersion();
        String release = (Integer.parseInt(previousEvr.getRelease()) + 1) + "";
        PackageEvr pevr =
                PackageEvrFactory.lookupOrCreatePackageEvr(epoch, version, release);

        result.setRpmVersion(previous.getRpmVersion());
        result.setDescription(previous.getDescription());
        result.setSummary(previous.getSummary());
        result.setPackageSize(previous.getPackageSize());
        result.setPayloadSize(previous.getPayloadSize());
        result.setBuildHost(previous.getBuildHost());
        result.setVendor(previous.getVendor());
        result.setPayloadFormat(previous.getPayloadFormat());
        result.setCompat(previous.getCompat());
        result.setPath(previous.getPath());
        result.setHeaderSignature(previous.getHeaderSignature());
        result.setCopyright(previous.getCopyright());
        result.setCookie(previous.getCookie());
        result.setPackageName(previous.getPackageName());
        result.setPackageEvr(pevr);
        result.setPackageGroup(previous.getPackageGroup());

        TestUtils.saveAndFlush(result);

        return result;
    }

    /**
     * Create an {@link InstalledPackage} (reification of the installation of a
     * package onto a server).
     * @param packageIn the package to install
     * @param server the server
     * @return the newly created installed package
     * @throws Exception if anything goes wrong
     */
    public static InstalledPackage createTestInstalledPackage(Package packageIn,
            Server server) throws Exception {
        InstalledPackage result = new InstalledPackage();
        result.setEvr(packageIn.getPackageEvr());
        result.setArch(packageIn.getPackageArch());
        result.setName(packageIn.getPackageName());
        result.setServer(server);
        Set<InstalledPackage> serverPackages = server.getPackages();
        serverPackages.add(result);
        server.setPackages(serverPackages);
        ServerFactory.save(server);

        return result;
    }

    /**
     * Create a {@link Server} owned by a user, with channels and a specific
     * server architecture.
     * @param user the user
     * @param channels the channels
     * @param serverArchLabel the server architecture label
     * @return the newly created server
     * @throws Exception if anything goes wrong
     */
    public static Server createTestServer(User user, Collection<Channel> channels,
            String serverArchLabel) throws Exception {
        Server result = createTestServer(user, channels);
        result.setServerArch(ServerFactory.lookupServerArchByLabel(serverArchLabel));

        TestUtils.saveAndFlush(result);

        return result;
    }

    /**
     * Return all CVE audit relevant channels for a given system
     * (rows from table suseCVEServerChannel).
     * @param systemID ID of system
     * @return list of relevant channel IDs
     */
    @SuppressWarnings("unchecked")
    public static List<ServerChannelIdPair> getRelevantChannels(Long systemID) {
        SelectMode selectMode = ModeFactory.getMode("test_queries",
                "find_relevant_channels");
        Map<String, Object> params = new HashMap<String, Object>();
        params.put("sid", systemID);
        DataResult<ServerChannelIdPair> ret = selectMode.execute(params);
        return ret;
    }

    /**
     * Return all {@link ServerChannelIdPair} objects from suseCVEServerChannel.
     * @return list of all relevant channels for all systems
     */
    @SuppressWarnings("unchecked")
    public static List<ServerChannelIdPair> getAllRelevantChannels() {
        SelectMode selectMode = ModeFactory.getMode("test_queries",
                "find_all_relevant_channels");
        Map<String, Object> params = new HashMap<String, Object>();
        DataResult<ServerChannelIdPair> ret = selectMode.execute(params);
        return ret;
    }

    /**
     * Copy errata details as in {@link ErrataFactory}.
     * @param copy
     * @param original
     */
    @SuppressWarnings({ "unchecked", "rawtypes" })
    private static void copyErrataDetails(Errata copy, Errata original) {
        copy.setAdvisoryType(original.getAdvisoryType());
        copy.setProduct(original.getProduct());
        copy.setErrataFrom(original.getErrataFrom());
        copy.setDescription(original.getDescription());
        copy.setSynopsis(original.getSynopsis());
        copy.setTopic(original.getTopic());
        copy.setSolution(original.getSolution());
        copy.setIssueDate(original.getIssueDate());
        copy.setUpdateDate(original.getUpdateDate());
        copy.setNotes(original.getNotes());
        copy.setRefersTo(original.getRefersTo());
        copy.setAdvisoryRel(original.getAdvisoryRel());
        copy.setLocallyModified(original.getLocallyModified());
        copy.setLastModified(original.getLastModified());

        // Copy the packages
        copy.setPackages(new HashSet(original.getPackages()));
    }
}
