/**
 * Copyright (c) 2013 SUSE LLC
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
package com.redhat.rhn.manager.distupgrade.test;

import static com.redhat.rhn.testing.ErrataTestUtils.createTestChannelFamily;
import static com.redhat.rhn.testing.ErrataTestUtils.createTestChannelProduct;

import com.redhat.rhn.common.hibernate.HibernateFactory;
import com.redhat.rhn.domain.action.Action;
import com.redhat.rhn.domain.action.ActionFactory;
import com.redhat.rhn.domain.action.dup.DistUpgradeAction;
import com.redhat.rhn.domain.action.dup.DistUpgradeActionDetails;
import com.redhat.rhn.domain.action.dup.DistUpgradeChannelTask;
import com.redhat.rhn.domain.action.server.ServerAction;
import com.redhat.rhn.domain.action.test.ActionFactoryTest;
import com.redhat.rhn.domain.channel.Channel;
import com.redhat.rhn.domain.channel.ChannelArch;
import com.redhat.rhn.domain.channel.ChannelFactory;
import com.redhat.rhn.domain.channel.ChannelFamily;
import com.redhat.rhn.domain.channel.ChannelProduct;
import com.redhat.rhn.domain.channel.test.ChannelFactoryTest;
import com.redhat.rhn.domain.credentials.Credentials;
import com.redhat.rhn.domain.product.ReleaseStage;
import com.redhat.rhn.domain.product.SUSEProduct;
import com.redhat.rhn.domain.product.SUSEProductChannel;
import com.redhat.rhn.domain.product.SUSEProductExtension;
import com.redhat.rhn.domain.product.SUSEProductFactory;
import com.redhat.rhn.domain.product.SUSEProductSCCRepository;
import com.redhat.rhn.domain.product.SUSEProductSet;
import com.redhat.rhn.domain.product.SUSEProductUpgrade;
import com.redhat.rhn.domain.product.test.SUSEProductTestUtils;
import com.redhat.rhn.domain.rhnpackage.Package;
import com.redhat.rhn.domain.rhnpackage.PackageName;
import com.redhat.rhn.domain.rhnpackage.test.PackageTest;
import com.redhat.rhn.domain.scc.SCCRepository;
import com.redhat.rhn.domain.server.Server;
import com.redhat.rhn.domain.server.test.ServerFactoryTest;
import com.redhat.rhn.frontend.dto.EssentialChannelDto;
import com.redhat.rhn.manager.action.ActionManager;
import com.redhat.rhn.manager.distupgrade.DistUpgradeException;
import com.redhat.rhn.manager.distupgrade.DistUpgradeManager;
import com.redhat.rhn.manager.rhnpackage.PackageManager;
import com.redhat.rhn.manager.system.test.SystemManagerTest;
import com.redhat.rhn.taskomatic.TaskomaticApi;
import com.redhat.rhn.testing.BaseTestCaseWithUser;
import com.redhat.rhn.testing.ErrataTestUtils;
import com.redhat.rhn.testing.TestUtils;

import org.jmock.Expectations;
import org.jmock.Mockery;
import org.jmock.integration.junit3.JUnit3Mockery;
import org.jmock.lib.concurrent.Synchroniser;
import org.jmock.lib.legacy.ClassImposteriser;

import java.util.*;

/**
 * Tests for {@link DistUpgradeManager} methods.
 */
public class DistUpgradeManagerTest extends BaseTestCaseWithUser {

    private static final Mockery CONTEXT = new JUnit3Mockery() {{
        setThreadingPolicy(new Synchroniser());
    }};

    @Override
    public void setUp() throws Exception {
        super.setUp();
        CONTEXT.setImposteriser(ClassImposteriser.INSTANCE);
    }

    /**
     * Verify that the correct product base channels are returned for a given
     * {@link ChannelArch}. The arch parameter has been added to fix this bug:
     *
     * https://bugzilla.novell.com/show_bug.cgi?id=841054.
     *
     * @throws Exception if anything goes wrong
     */
    public void testGetProductBaseChannelDto() throws Exception {
        // Create SUSE product and channel product
        ChannelFamily channelFamily = createTestChannelFamily();
        SUSEProduct product = createTestSUSEProductNoArch(channelFamily);
        ChannelProduct channelProduct = createTestChannelProduct();

        // Create product base channels with different channel archs
        ChannelArch channelArch;
        channelArch = ChannelFactory.findArchByLabel("channel-ia32");
        Channel baseChannelIA32 = createTestBaseChannel(
                channelFamily, channelProduct, channelArch);
        SUSEProductTestUtils.createTestSUSEProductChannel(baseChannelIA32, product, true);
        channelArch = ChannelFactory.findArchByLabel("channel-x86_64");
        Channel baseChannelX8664 = createTestBaseChannel(
                channelFamily, channelProduct, channelArch);
        SUSEProductTestUtils.createTestSUSEProductChannel(baseChannelX8664, product, true);

        // Check the product base channels for given architectures
        EssentialChannelDto productBaseChannel;
        productBaseChannel = DistUpgradeManager.getProductBaseChannelDto(product.getId(),
                        ChannelFactory.findArchByLabel("channel-ia32"));
        assertEquals(baseChannelIA32.getId(), productBaseChannel.getId());
        productBaseChannel = DistUpgradeManager.getProductBaseChannelDto(product.getId(),
                ChannelFactory.findArchByLabel("channel-x86_64"));
        assertEquals(baseChannelX8664.getId(), productBaseChannel.getId());
        productBaseChannel = DistUpgradeManager.getProductBaseChannelDto(product.getId(),
                ChannelFactory.findArchByLabel("channel-s390"));
        assertNull(productBaseChannel);
    }

    /**
     * Test getTargetProductSets(): No target product found.
     * @throws Exception if anything goes wrong
     */
    public void testGetTargetProductSetsEmpty() throws Exception {
        // Setup source products
        ChannelFamily family = createTestChannelFamily();
        SUSEProduct sourceProduct = SUSEProductTestUtils.createTestSUSEProduct(family);
        SUSEProductSet sourceProducts = new SUSEProductSet(
                sourceProduct.getId(), Collections.emptyList());
        ChannelArch arch = ChannelFactory.findArchByLabel("channel-ia32");
        List<SUSEProductSet> targetProductSets = DistUpgradeManager.getTargetProductSets(
                Optional.of(sourceProducts), arch, user);
        assertNotNull(targetProductSets);
        assertTrue(targetProductSets.isEmpty());
    }

    public void testGetTargetProductSetsEmptyWithTarget() throws Exception {
        // Setup source products
        ChannelFamily family = createTestChannelFamily();
        SUSEProduct sourceProduct = SUSEProductTestUtils.createTestSUSEProduct(family);
        SUSEProductTestUtils.createBaseChannelForBaseProduct(sourceProduct, user);
        sourceProduct = TestUtils.saveAndReload(sourceProduct);
        SUSEProductSet sourceProducts = new SUSEProductSet(
                sourceProduct, Collections.emptyList());

        SUSEProduct targetBaseProduct = SUSEProductTestUtils.createTestSUSEProduct(family);
        SUSEProductTestUtils.createBaseChannelForBaseProduct(targetBaseProduct, user);
        sourceProduct.setUpgrades(Collections.singleton(targetBaseProduct));

        ChannelArch arch = ChannelFactory.findArchByLabel("channel-ia32");
        List<SUSEProductSet> targetProductSets = DistUpgradeManager.getTargetProductSets(
                Optional.of(sourceProducts), arch , user);
        assertNotNull(targetProductSets);
        assertEquals(1, targetProductSets.size());
        assertEquals(targetBaseProduct, targetProductSets.get(0).getBaseProduct());
        assertTrue(targetProductSets.get(0).getAddonProducts().isEmpty());
    }

    /**
     * Test getTargetProductSets(): No target product found.
     * @throws Exception if anything goes wrong
     */
    public void testGetTargetProductSetsEmptyWithAddon() throws Exception {
        // Setup source products
        ChannelFamily family = createTestChannelFamily();
        SUSEProduct sourceProduct = SUSEProductTestUtils.createTestSUSEProduct(family);
        SUSEProduct sourceAddonProduct = SUSEProductTestUtils.createTestSUSEProduct(family);
        SUSEProductSet sourceProducts = new SUSEProductSet(
                sourceProduct.getId(), Collections.singletonList(sourceAddonProduct.getId()));
        ChannelArch arch = ChannelFactory.findArchByLabel("channel-ia32");
        List<SUSEProductSet> targetProductSets = DistUpgradeManager.getTargetProductSets(
                Optional.of(sourceProducts), arch, user);
        assertNotNull(targetProductSets);
        assertTrue(targetProductSets.isEmpty());
    }

    /**
     * Test getTargetProductSets(): target products are actually found (base + addon).
     * @throws Exception if anything goes wrong
     */
    public void testGetTargetProductSets() throws Exception {
        Credentials sccc = SUSEProductTestUtils.createSCCCredentials("dummy", user);
        // Setup source products
        ChannelFamily family = createTestChannelFamily();
        SUSEProduct sourceBaseProduct = SUSEProductTestUtils.createTestSUSEProduct(family);
        Channel sourceBaseChannel = SUSEProductTestUtils.createBaseChannelForBaseProduct(sourceBaseProduct, user);

        List<SUSEProduct> sourceAddons = new ArrayList<>();
        SUSEProduct sourceAddonProduct = SUSEProductTestUtils.createTestSUSEProduct(family);
        Channel sourceChildChannel = SUSEProductTestUtils.createChildChannelsForProduct(sourceAddonProduct, sourceBaseChannel, user);
        SUSEProductExtension e = new SUSEProductExtension(sourceBaseProduct, sourceAddonProduct, sourceBaseProduct, false);
        TestUtils.saveAndReload(e);

        sourceAddons.add(sourceAddonProduct);
        SUSEProductSet sourceProducts = new SUSEProductSet(sourceBaseProduct, sourceAddons);

        SCCRepository sbase = SUSEProductTestUtils.createSCCRepository();
        SUSEProductTestUtils.createSCCRepositoryTokenAuth(sccc, sbase);

        SUSEProductSCCRepository sspsc = new SUSEProductSCCRepository();
        sspsc.setProduct(sourceBaseProduct);
        sspsc.setRootProduct(sourceBaseProduct);
        sspsc.setRepository(sbase);
        sspsc.setChannelLabel(sourceBaseChannel.getLabel());
        sspsc.setParentChannelLabel(sourceBaseChannel.getLabel());
        sspsc.setChannelName(sourceBaseChannel.getLabel());
        sspsc.setMandatory(true);
        sspsc = TestUtils.saveAndReload(sspsc);

        SCCRepository saddon = SUSEProductTestUtils.createSCCRepository();
        SUSEProductTestUtils.createSCCRepositoryTokenAuth(sccc, saddon);

        SUSEProductSCCRepository sspsca = new SUSEProductSCCRepository();
        sspsca.setProduct(sourceAddonProduct);
        sspsca.setRootProduct(sourceBaseProduct);
        sspsca.setRepository(saddon);
        sspsca.setChannelLabel(sourceChildChannel.getLabel());
        sspsca.setParentChannelLabel(sourceBaseChannel.getLabel());
        sspsca.setChannelName(sourceBaseChannel.getLabel());
        sspsca.setMandatory(true);
        sspsca = TestUtils.saveAndReload(sspsca);

        // Setup migration target product + upgrade path
        SUSEProduct targetBaseProduct = SUSEProductTestUtils.createTestSUSEProduct(family);
        Channel targetBaseChannel = SUSEProductTestUtils.createBaseChannelForBaseProduct(targetBaseProduct, user);
        SUSEProductChannel pcbase = new SUSEProductChannel();
        pcbase.setChannel(targetBaseChannel);
        pcbase.setProduct(targetBaseProduct);
        pcbase.setMandatory(true);
        SUSEProductFactory.save(pcbase);
        sourceBaseProduct.setUpgrades(Collections.singleton(targetBaseProduct));

        // Setup target addon product + upgrade path
        SUSEProduct targetAddonProduct = SUSEProductTestUtils.createTestSUSEProduct(family);
        Channel targetAddonChannel = SUSEProductTestUtils.createChildChannelsForProduct(targetAddonProduct, targetBaseChannel, user);
        SUSEProductChannel pcaddon = new SUSEProductChannel();
        pcaddon.setChannel(targetAddonChannel);
        pcaddon.setProduct(targetAddonProduct);
        pcaddon.setMandatory(true);
        SUSEProductFactory.save(pcaddon);
        sourceAddonProduct.setUpgrades(Collections.singleton(targetAddonProduct));
        SUSEProductExtension e2 = new SUSEProductExtension(sourceBaseProduct, targetAddonProduct, sourceBaseProduct, false);
        SUSEProductExtension e3 = new SUSEProductExtension(targetBaseProduct, targetAddonProduct, targetBaseProduct, false);
        TestUtils.saveAndReload(e2);
        TestUtils.saveAndReload(e3);

        SCCRepository base = SUSEProductTestUtils.createSCCRepository();
        SUSEProductTestUtils.createSCCRepositoryTokenAuth(sccc, base);

        SUSEProductSCCRepository spsc = new SUSEProductSCCRepository();
        spsc.setProduct(targetBaseProduct);
        spsc.setRootProduct(targetBaseProduct);
        spsc.setRepository(base);
        spsc.setChannelLabel(targetBaseChannel.getLabel());
        spsc.setParentChannelLabel(targetBaseChannel.getLabel());
        spsc.setChannelName(targetBaseChannel.getLabel());
        spsc.setMandatory(true);
        spsc = TestUtils.saveAndReload(spsc);

        SCCRepository addon = SUSEProductTestUtils.createSCCRepository();
        SUSEProductTestUtils.createSCCRepositoryTokenAuth(sccc, addon);

        SUSEProductSCCRepository spsca = new SUSEProductSCCRepository();
        spsca.setProduct(targetAddonProduct);
        spsca.setRootProduct(targetBaseProduct);
        spsca.setRepository(addon);
        spsca.setChannelLabel(targetAddonChannel.getLabel());
        spsca.setParentChannelLabel(targetBaseChannel.getLabel());
        spsca.setChannelName(targetBaseChannel.getLabel());
        spsca.setMandatory(true);
        spsca = TestUtils.saveAndReload(spsca);

        // Verify that target products are returned correctly

        ChannelArch arch = ChannelFactory.findArchByLabel("channel-x86_64");
        List<SUSEProductSet> targetProductSets = DistUpgradeManager.getTargetProductSets(Optional.of(sourceProducts), arch, user);

        assertNotNull(targetProductSets);
        assertEquals(2, targetProductSets.size());

        for (SUSEProductSet target : targetProductSets) {
            if (target.getBaseProduct().getId() == sourceBaseProduct.getId()) {
                List<SUSEProduct> addonProducts = target.getAddonProducts();
                assertEquals(1, addonProducts.size());
                assertEquals(targetAddonProduct, addonProducts.get(0));
            }
            else if (target.getBaseProduct().getId() == targetBaseProduct.getId()) {
                List<SUSEProduct> addonProducts = target.getAddonProducts();
                assertEquals(1, addonProducts.size());
                assertEquals(targetAddonProduct, addonProducts.get(0));
            }
            else {
                fail("unexpected product " + target.getBaseProduct());
            }
        }
    }

    public void testGetTargetProductSetsMissingChannel() throws Exception {
        Credentials sccc = SUSEProductTestUtils.createSCCCredentials("dummy", user);
        // Setup source products
        ChannelFamily family = createTestChannelFamily();
        SUSEProduct sourceBaseProduct = SUSEProductTestUtils.createTestSUSEProduct(family);
        Channel sourceBaseChannel = SUSEProductTestUtils.createBaseChannelForBaseProduct(sourceBaseProduct, user);
        sourceBaseChannel.setLabel("sourceBaseChannel");

        List<SUSEProduct> sourceAddons = new ArrayList<>();
        SUSEProduct sourceAddonProduct = SUSEProductTestUtils.createTestSUSEProduct(family);
        Channel sourceChildChannel = SUSEProductTestUtils.createChildChannelsForProduct(sourceAddonProduct, sourceBaseChannel, user);
        sourceChildChannel.setLabel("sourceChildChannel");
        SUSEProductExtension e = new SUSEProductExtension(sourceBaseProduct, sourceAddonProduct, sourceBaseProduct, false);
        e = TestUtils.saveAndReload(e);

        sourceAddons.add(sourceAddonProduct);
        SUSEProductSet sourceProducts = new SUSEProductSet(sourceBaseProduct, sourceAddons);

        SCCRepository sbase = SUSEProductTestUtils.createSCCRepository();
        SUSEProductTestUtils.createSCCRepositoryTokenAuth(sccc, sbase);

        SUSEProductSCCRepository sspsc = new SUSEProductSCCRepository();
        sspsc.setProduct(sourceBaseProduct);
        sspsc.setRootProduct(sourceBaseProduct);
        sspsc.setRepository(sbase);
        sspsc.setChannelLabel(sourceBaseChannel.getLabel());
        sspsc.setParentChannelLabel(sourceBaseChannel.getLabel());
        sspsc.setChannelName(sourceBaseChannel.getLabel());
        sspsc.setMandatory(true);
        sspsc = TestUtils.saveAndReload(sspsc);

        SCCRepository saddon = SUSEProductTestUtils.createSCCRepository();
        SUSEProductTestUtils.createSCCRepositoryTokenAuth(sccc, saddon);

        SUSEProductSCCRepository sspsca = new SUSEProductSCCRepository();
        sspsca.setProduct(sourceAddonProduct);
        sspsca.setRootProduct(sourceBaseProduct);
        sspsca.setRepository(saddon);
        sspsca.setChannelLabel(sourceChildChannel.getLabel());
        sspsca.setParentChannelLabel(sourceBaseChannel.getLabel());
        sspsca.setChannelName(sourceBaseChannel.getLabel());
        sspsca.setMandatory(true);
        sspsca = TestUtils.saveAndReload(sspsca);

        // Setup migration target product + upgrade path
        SUSEProduct targetBaseProduct = SUSEProductTestUtils.createTestSUSEProduct(family);
        Channel targetBaseChannel = SUSEProductTestUtils.createBaseChannelForBaseProduct(targetBaseProduct, user);
        targetBaseChannel.setLabel("targetBaseChannel");
        SUSEProductChannel pcbase = new SUSEProductChannel();
        pcbase.setChannel(targetBaseChannel);
        pcbase.setProduct(targetBaseProduct);
        pcbase.setMandatory(true);
        SUSEProductFactory.save(pcbase);
        sourceBaseProduct.setUpgrades(Collections.singleton(targetBaseProduct));

        // Setup target addon product + upgrade path
        SUSEProduct targetAddonProduct = SUSEProductTestUtils.createTestSUSEProduct(family);

        /* No Synced Channel for the target addon product !*/

        sourceAddonProduct.setUpgrades(Collections.singleton(targetAddonProduct));
        SUSEProductExtension e2 = new SUSEProductExtension(sourceBaseProduct, targetAddonProduct, sourceBaseProduct, false);
        SUSEProductExtension e3 = new SUSEProductExtension(targetBaseProduct, targetAddonProduct, targetBaseProduct, false);
        e2 = TestUtils.saveAndReload(e2);
        e3 = TestUtils.saveAndReload(e3);

        SCCRepository base = SUSEProductTestUtils.createSCCRepository();
        SUSEProductTestUtils.createSCCRepositoryTokenAuth(sccc, base);

        SUSEProductSCCRepository spsc = new SUSEProductSCCRepository();
        spsc.setProduct(targetBaseProduct);
        spsc.setRootProduct(targetBaseProduct);
        spsc.setRepository(base);
        spsc.setChannelLabel(targetBaseChannel.getLabel());
        spsc.setParentChannelLabel(targetBaseChannel.getLabel());
        spsc.setChannelName(targetBaseChannel.getLabel());
        spsc.setMandatory(true);
        spsc = TestUtils.saveAndReload(spsc);

        SCCRepository addon = SUSEProductTestUtils.createSCCRepository();
        SUSEProductTestUtils.createSCCRepositoryTokenAuth(sccc, addon);

        SUSEProductSCCRepository spsca = new SUSEProductSCCRepository();
        spsca.setProduct(targetAddonProduct);
        spsca.setRootProduct(targetBaseProduct);
        spsca.setRepository(addon);
        spsca.setChannelLabel("missing-addon-channel");
        spsca.setParentChannelLabel(targetBaseChannel.getLabel());
        spsca.setChannelName(targetBaseChannel.getLabel());
        spsca.setMandatory(true);
        spsca = TestUtils.saveAndReload(spsca);

        // Verify that target products are returned correctly

        ChannelArch arch = ChannelFactory.findArchByLabel("channel-x86_64");
        List<SUSEProductSet> targetProductSets = DistUpgradeManager.getTargetProductSets(Optional.of(sourceProducts), arch, user);

        assertNotNull(targetProductSets);
        assertEquals(2, targetProductSets.size());

        for (SUSEProductSet target : targetProductSets) {
            if (target.getBaseProduct().getId() == sourceBaseProduct.getId()) {
                List<SUSEProduct> addonProducts = target.getAddonProducts();
                assertEquals(1, addonProducts.size());
                assertEquals(targetAddonProduct, addonProducts.get(0));
                assertEquals(0, target.getMissingChannels().size());
            }
            else if (target.getBaseProduct().getId() == targetBaseProduct.getId()) {
                List<SUSEProduct> addonProducts = target.getAddonProducts();
                assertEquals(1, addonProducts.size());
                assertEquals(targetAddonProduct, addonProducts.get(0));
                assertEquals(1, target.getMissingChannels().size());
                assertEquals("missing-addon-channel", target.getMissingChannels().get(0));
            }
            else {
                fail("unexpected product " + target.getBaseProduct());
            }
        }
    }

    /**
     * Test for performServerChecks(): capability "distupgrade.upgrade" is missing.
     * @throws Exception if anything goes wrong
     */
    public void testCapabilityMissing() throws Exception {
        Server server = ServerFactoryTest.createTestServer(user, true);
        try {
            DistUpgradeManager.performServerChecks(server.getId(), user);
            fail("Missing capability should make the server checks fail!");
        }
        catch (DistUpgradeException e) {
            assertEquals("Dist upgrade not supported for server: " +
                    server.getId(), e.getMessage());
        }
    }

    /**
     * Test for performServerChecks(): "zypp-plugin-spacewalk" is not installed.
     * @throws Exception if anything goes wrong
     */
    public void testZyppPluginNotInstalled() throws Exception {
        Server server = ServerFactoryTest.createTestServer(user, true);
        SystemManagerTest.giveCapability(server.getId(), "distupgrade.upgrade", 1L);
        try {
            DistUpgradeManager.performServerChecks(server.getId(), user);
            fail("Missing package zyppPluginSpacewalk should make the server checks fail!");
        }
        catch (DistUpgradeException e) {
            assertEquals("Package zypp-plugin-spacewalk is not installed: " +
                    server.getId(), e.getMessage());
        }
    }

    /**
     * Test for performServerChecks(): a dist upgrade action is already scheduled.
     * @throws Exception if anything goes wrong
     */
    public void testDistUpgradeScheduled() throws Exception {
        Server server = ServerFactoryTest.createTestServer(user, true);
        SystemManagerTest.giveCapability(server.getId(), "distupgrade.upgrade", 1L);

        // Install the zypp-plugin-spacewalk package
        Package zyppPlugin = PackageTest.createTestPackage(user.getOrg());
        PackageName name = PackageManager.lookupPackageName("zypp-plugin-spacewalk");
        if (name == null) {
            name = zyppPlugin.getPackageName();
            name.setName("zypp-plugin-spacewalk");
            TestUtils.saveAndFlush(name);
        }
        else {
            // Handle the case that the package name exists in the DB
            zyppPlugin.setPackageName(name);
            TestUtils.saveAndFlush(zyppPlugin);
        }
        ErrataTestUtils.createTestInstalledPackage(zyppPlugin, server);

        // Store a dist upgrade action for this server
        Action action = ActionFactoryTest.createAction(user,
                ActionFactory.TYPE_DIST_UPGRADE);
        ServerAction serverAction = ActionFactoryTest.createServerAction(server, action);
        TestUtils.saveAndFlush(serverAction);

        try {
            DistUpgradeManager.performServerChecks(server.getId(), user);
            fail("A scheduled dist upgrade should make the server checks fail!");
        }
        catch (DistUpgradeException e) {
            assertEquals("Another dist upgrade is in the schedule for server: " +
                    server.getId(), e.getMessage());
        }
    }

    /**
     * Test for performChannelChecks(): More than one base channel given for dist upgrade
     * @throws Exception if anything goes wrong
     */
    public void testMoreThanOneBaseChannel() throws Exception {
        Channel channel1 = ChannelFactoryTest.createTestChannel(user);
        Channel channel2 = ChannelFactoryTest.createTestChannel(user);

        // Try to upgrade with 2 base channels
        List<String> channels = new ArrayList<String>();
        channels.add(channel1.getLabel());
        channels.add(channel2.getLabel());
        try {
            DistUpgradeManager.performChannelChecks(channels, user);
            fail("More than one base channel should make channel checks fail!");
        }
        catch (DistUpgradeException e) {
            assertEquals("More than one base channel given for dist upgrade",
                    e.getMessage());
        }
    }

    /**
     * Test for performChannelChecks(): No base channel given for dist upgrade
     * @throws Exception if anything goes wrong
     */
    public void testNoBaseChannel() throws Exception {
        Channel channel1 = ChannelFactoryTest.createTestChannel(user);
        Channel channel2 = ChannelFactoryTest.createTestChannel(user);
        Channel channel3 = ChannelFactoryTest.createTestChannel(user);

        // Make child channels and try to upgrade with no base channel
        channel2.setParentChannel(channel1);
        channel3.setParentChannel(channel1);
        List<String> channels = new ArrayList<String>();
        channels.add(channel2.getLabel());
        channels.add(channel3.getLabel());
        try {
            DistUpgradeManager.performChannelChecks(channels, user);
            fail("No base channel should make channel checks fail!");
        }
        catch (DistUpgradeException e) {
            assertEquals("No base channel given for dist upgrade", e.getMessage());
        }
    }

    /**
     * Test for performChannelChecks(): Channel has incompatible base channel
     * @throws Exception if anything goes wrong
     */
    public void testIncompatibleBaseChannel() throws Exception {
        Channel channel1 = ChannelFactoryTest.createTestChannel(user);
        Channel channel2 = ChannelFactoryTest.createTestChannel(user);
        Channel channel3 = ChannelFactoryTest.createTestChannel(user);
        Channel channel4 = ChannelFactoryTest.createTestChannel(user);

        // Upgrade with child channels having different parents
        channel2.setParentChannel(channel1);
        channel4.setParentChannel(channel3);
        List<String> channels = new ArrayList<String>();
        channels.add(channel1.getLabel());
        channels.add(channel2.getLabel());
        channels.add(channel4.getLabel());
        try {
            DistUpgradeManager.performChannelChecks(channels, user);
            fail("Incompatible base channel should make channel checks fail!");
        }
        catch (DistUpgradeException e) {
            assertEquals("Channel has incompatible base channel: " +
                    channel4.getLabel(), e.getMessage());
        }
    }

    /**
     * Test for scheduleDistUpgrade().
     * @throws Exception if anything goes wrong
     */
    public void testScheduleDistUpgrade() throws Exception {
        Channel subscribedChannel = ChannelFactoryTest.createTestChannel(user);
        List<Channel> subscribedChannels = new ArrayList<Channel>(
                Arrays.asList(subscribedChannel));
        Server server = ErrataTestUtils.createTestServer(user, subscribedChannels);

        TaskomaticApi taskomaticMock = CONTEXT.mock(TaskomaticApi.class);
        ActionManager.setTaskomaticApi(taskomaticMock);

        CONTEXT.checking(new Expectations() { {
            allowing(taskomaticMock).scheduleActionExecution(with(any(Action.class)));
            allowing(taskomaticMock).scheduleActionExecution(with(any(Action.class)),
                    with(any(Boolean.class)));
        } });

        // Setup product upgrade
        ChannelFamily family = createTestChannelFamily();
        SUSEProduct source = SUSEProductTestUtils.createTestSUSEProduct(family);
        SUSEProductTestUtils.installSUSEProductOnServer(source, server);
        SUSEProduct target = SUSEProductTestUtils.createTestSUSEProduct(family);
        SUSEProductSet targetSet = new SUSEProductSet();
        targetSet.setBaseProduct(target);

        // Setup channel tasks
        Channel channel1 = ChannelFactoryTest.createTestChannel(user);
        Channel channel2 = ChannelFactoryTest.createTestChannel(user);
        List<Long> channelIDs = new ArrayList<Long>();
        channelIDs.add(channel1.getId());
        channelIDs.add(channel2.getId());
        Date scheduleDate = new Date();
        Long actionID = DistUpgradeManager.scheduleDistUpgrade(
                user, server, targetSet, channelIDs, true, scheduleDate);

        // Get the scheduled action and check the contents
        DistUpgradeAction action = (DistUpgradeAction) ActionFactory.lookupById(actionID);
        assertEquals(ActionFactory.TYPE_DIST_UPGRADE, action.getActionType());
        assertEquals(user, action.getSchedulerUser());
        assertEquals(scheduleDate, action.getEarliestAction());
        Set<ServerAction> serverActions = action.getServerActions();
        assertEquals(server, serverActions.iterator().next().getServer());
        DistUpgradeActionDetails details = action.getDetails();
        assertTrue(details.isDryRun());

        // Check product upgrade
        Set<SUSEProductUpgrade> upgrades = details.getProductUpgrades();
        assertEquals(1, upgrades.size());
        for (SUSEProductUpgrade upgrade : upgrades) {
            assertEquals(source, upgrade.getFromProduct());
            assertEquals(target, upgrade.getToProduct());
        }

        // Check channel tasks
        Set<DistUpgradeChannelTask> channelTasks = details.getChannelTasks();
        assertEquals(3, channelTasks.size());
        for (DistUpgradeChannelTask task : channelTasks) {
            if (task.getChannel().equals(subscribedChannel)) {
                assertEquals('U', task.getTask());
            }
            else {
                assertTrue(task.getChannel().equals(channel1) ||
                        task.getChannel().equals(channel2));
                assertEquals('S', task.getTask());
            }
        }
    }

    /**
     * Create a SUSE product with arch == null. This is actually the case with
     * e.g. SLES for VMWARE.
     *
     * @param family the channel family
     * @return the channel product
     * @throws Exception if anything goes wrong
     */
    public static SUSEProduct createTestSUSEProductNoArch(ChannelFamily family)
            throws Exception {
        SUSEProduct product = new SUSEProduct();
        String name = TestUtils.randomString().toLowerCase();
        product.setName(name);
        product.setVersion("1");
        product.setFriendlyName("SUSE Test product " + name);
        product.setArch(null);
        product.setRelease("test");
        product.setReleaseStage(ReleaseStage.released);
        product.setProductId(0);
        TestUtils.saveAndFlush(product);
        return product;
    }

    /**
     * Create a vendor base channel for a given {@link ChannelFamily},
     * {@link ChannelProduct} and {@link ChannelArch}.
     *
     * @param channelFamily channelFamily
     * @param channelProduct channelProduct
     * @param channelArch the channel architecture
     * @return the new vendor base channel
     * @throws Exception if anything goes wrong
     */
    public static Channel createTestBaseChannel(ChannelFamily channelFamily,
            ChannelProduct channelProduct, ChannelArch channelArch) throws Exception {
        Channel channel = ChannelFactoryTest.
                createTestChannel(null, channelFamily);
        channel.setProduct(channelProduct);
        channel.setChannelArch(channelArch);
        TestUtils.saveAndFlush(channel);
        return channel;
    }

}
