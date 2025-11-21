/*
 * Copyright (c) 2013--2025 SUSE LLC
 *
 * This software is licensed to you under the GNU General Public License,
 * version 2 (GPLv2). There is NO WARRANTY for this software, express or
 * implied, including the implied warranties of MERCHANTABILITY or FITNESS
 * FOR A PARTICULAR PURPOSE. You should have received a copy of GPLv2
 * along with this software; if not, see
 * http://www.gnu.org/licenses/old-licenses/gpl-2.0.txt.
 */
package com.redhat.rhn.manager.distupgrade.test;

import static com.redhat.rhn.testing.ErrataTestUtils.createTestChannelFamily;
import static com.redhat.rhn.testing.ErrataTestUtils.createTestChannelProduct;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertInstanceOf;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.junit.jupiter.api.Assertions.fail;

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
import com.redhat.rhn.domain.credentials.SCCCredentials;
import com.redhat.rhn.domain.iss.IssFactory;
import com.redhat.rhn.domain.iss.IssMaster;
import com.redhat.rhn.domain.product.ChannelTemplate;
import com.redhat.rhn.domain.product.ReleaseStage;
import com.redhat.rhn.domain.product.SUSEProduct;
import com.redhat.rhn.domain.product.SUSEProductExtension;
import com.redhat.rhn.domain.product.SUSEProductSet;
import com.redhat.rhn.domain.product.SUSEProductUpgrade;
import com.redhat.rhn.domain.product.test.SUSEProductTestUtils;
import com.redhat.rhn.domain.rhnpackage.Package;
import com.redhat.rhn.domain.rhnpackage.PackageName;
import com.redhat.rhn.domain.rhnpackage.test.PackageTest;
import com.redhat.rhn.domain.scc.SCCRepository;
import com.redhat.rhn.domain.server.InstalledProduct;
import com.redhat.rhn.domain.server.Server;
import com.redhat.rhn.domain.server.ServerConstants;
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
import org.jmock.imposters.ByteBuddyClassImposteriser;
import org.jmock.junit5.JUnit5Mockery;
import org.jmock.lib.concurrent.Synchroniser;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.api.extension.RegisterExtension;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Date;
import java.util.HashSet;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.Set;

/**
 * Tests for {@link DistUpgradeManager} methods.
 */
@ExtendWith(JUnit5Mockery.class)
public class DistUpgradeManagerTest extends BaseTestCaseWithUser {

    @RegisterExtension
    protected final Mockery context = new JUnit5Mockery() {{
        setThreadingPolicy(new Synchroniser());
    }};

    @Override
    @BeforeEach
    public void setUp() throws Exception {
        super.setUp();
        context.setImposteriser(ByteBuddyClassImposteriser.INSTANCE);
    }

    /**
     * Verify that the correct product base channels are returned for a given
     * {@link ChannelArch}. The arch parameter has been added to fix this bug:
     *
     * https://bugzilla.novell.com/show_bug.cgi?id=841054.
     *
     * @throws Exception if anything goes wrong
     */
    @Test
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
    @Test
    public void testGetTargetProductSetsEmpty() throws Exception {
        // Setup source products
        ChannelFamily family = createTestChannelFamily();
        SUSEProduct sourceProduct = SUSEProductTestUtils.createTestSUSEProduct(family);
        SUSEProductSet sourceProducts = new SUSEProductSet(
                sourceProduct.getId(), Collections.emptyList());
        ChannelArch arch = ChannelFactory.findArchByLabel("channel-ia32");
        List<SUSEProductSet> targetProductSets = DistUpgradeManager.getTargetProductSets(
                Optional.of(sourceProducts), arch, user);
        targetProductSets = DistUpgradeManager.removeIncompatibleTargets(Optional.of(sourceProducts),
                targetProductSets);
        assertNotNull(targetProductSets);
        assertTrue(targetProductSets.isEmpty());
    }

    @Test
    public void testGetTargetProductSetsEmptyWithTarget() throws Exception {
        // Setup source products
        ChannelFamily family = createTestChannelFamily();
        SUSEProduct sourceProduct = SUSEProductTestUtils.createTestSUSEProduct(family);
        Channel sourceChannel = SUSEProductTestUtils.createBaseChannelForBaseProduct(sourceProduct, user);
        sourceProduct = TestUtils.saveAndReload(sourceProduct);
        SUSEProductSet sourceProducts = new SUSEProductSet(sourceProduct, Collections.emptyList());
        SUSEProductTestUtils.populateRepository(sourceProduct, sourceChannel, sourceProduct, sourceChannel, user);

        SUSEProduct targetProduct = SUSEProductTestUtils.createTestSUSEProduct(family);
        Channel targetChannel = SUSEProductTestUtils.createBaseChannelForBaseProduct(targetProduct, user);
        SUSEProductTestUtils.populateRepository(targetProduct, targetChannel, targetProduct, targetChannel, user);
        sourceProduct.setUpgrades(Collections.singleton(targetProduct));

        ChannelArch arch = ChannelFactory.findArchByLabel("channel-ia32");
        List<SUSEProductSet> targetProductSets = DistUpgradeManager.getTargetProductSets(
                Optional.of(sourceProducts), arch , user);
        targetProductSets = DistUpgradeManager.removeIncompatibleTargets(Optional.of(sourceProducts),
                targetProductSets);
        assertNotNull(targetProductSets);
        assertEquals(1, targetProductSets.size());
        assertEquals(targetProduct, targetProductSets.get(0).getBaseProduct());
        assertTrue(targetProductSets.get(0).getAddonProducts().isEmpty());
    }

    /**
     * Test getTargetProductSets(): No target product found.
     * @throws Exception if anything goes wrong
     */
    @Test
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
        targetProductSets = DistUpgradeManager.removeIncompatibleTargets(Optional.of(sourceProducts),
                targetProductSets);

        assertNotNull(targetProductSets);
        assertTrue(targetProductSets.isEmpty());
    }

    /**
     * Test getTargetProductSets(): target products are actually found (base + addon).
     * @throws Exception if anything goes wrong
     */
    @Test
    public void testGetTargetProductSets() throws Exception {
        SCCCredentials sccc = SUSEProductTestUtils.createSCCCredentials("dummy", user);
        // Setup source products
        ChannelFamily family = createTestChannelFamily();
        SUSEProduct sourceBaseProduct = SUSEProductTestUtils.createTestSUSEProduct(family);
        Channel sourceBaseChannel = SUSEProductTestUtils.createBaseChannelForBaseProduct(sourceBaseProduct, user);

        List<SUSEProduct> sourceAddons = new ArrayList<>();
        SUSEProduct sourceAddonProduct = SUSEProductTestUtils.createTestSUSEProduct(family);
        Channel sourceChildChannel = SUSEProductTestUtils.createChildChannelsForProduct(
                sourceAddonProduct, sourceBaseChannel, user);
        SUSEProductExtension e = new SUSEProductExtension(
                sourceBaseProduct, sourceAddonProduct, sourceBaseProduct, false);
        TestUtils.saveAndReload(e);

        sourceAddons.add(sourceAddonProduct);
        SUSEProductSet sourceProducts = new SUSEProductSet(sourceBaseProduct, sourceAddons);

        SUSEProductTestUtils.populateRepository(sourceBaseProduct, sourceBaseChannel, sourceBaseProduct,
                sourceBaseChannel, user);
        SUSEProductTestUtils.populateRepository(sourceBaseProduct, sourceBaseChannel, sourceAddonProduct,
                sourceChildChannel, user);

        // Setup migration target product + upgrade path
        SUSEProduct targetBaseProduct = SUSEProductTestUtils.createTestSUSEProduct(family);
        Channel targetBaseChannel = SUSEProductTestUtils.createBaseChannelForBaseProduct(targetBaseProduct, user);
        sourceBaseProduct.setUpgrades(Collections.singleton(targetBaseProduct));

        // Setup target addon product + upgrade path
        SUSEProduct targetAddonProduct = SUSEProductTestUtils.createTestSUSEProduct(family);
        Channel targetAddonChannel =
                SUSEProductTestUtils.createChildChannelsForProduct(targetAddonProduct, targetBaseChannel, user);
        sourceAddonProduct.setUpgrades(Collections.singleton(targetAddonProduct));
        SUSEProductExtension e2 = new SUSEProductExtension(
                sourceBaseProduct, targetAddonProduct, sourceBaseProduct, false);
        SUSEProductExtension e3 = new SUSEProductExtension(
                targetBaseProduct, targetAddonProduct, targetBaseProduct, false);
        TestUtils.saveAndReload(e2);
        TestUtils.saveAndReload(e3);

        SCCRepository base = SUSEProductTestUtils.createSCCRepository();
        SUSEProductTestUtils.createSCCRepositoryTokenAuth(sccc, base);

        SUSEProductTestUtils.populateRepository(targetBaseProduct, targetBaseChannel, targetBaseProduct,
                targetBaseChannel, user);
        SUSEProductTestUtils.populateRepository(targetBaseProduct, targetBaseChannel, targetAddonProduct,
                targetAddonChannel, user);

        // Verify that target products are returned correctly

        ChannelArch arch = ChannelFactory.findArchByLabel("channel-x86_64");
        List<SUSEProductSet> targetProductSets = DistUpgradeManager.getTargetProductSets(
                Optional.of(sourceProducts), arch, user);

        targetProductSets = DistUpgradeManager.removeIncompatibleTargets(
                Optional.of(sourceProducts), targetProductSets);

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

    /**
     * Test getTargetProductSets(): target products are actually found (base + addon).
     * @throws Exception if anything goes wrong
     */
    @Test
    public void testGetTargetProductSetsOnISSSlave() throws Exception {
        // setup a Slave by defining its master
        IssMaster master = new IssMaster();
        master.setLabel("dummy-master");
        master.makeDefaultMaster();
        IssFactory.save(master);

        SCCCredentials sccc = SUSEProductTestUtils.createSCCCredentials("dummy", user);
        // Setup source products
        ChannelFamily family = createTestChannelFamily();
        SUSEProduct sourceBaseProduct = SUSEProductTestUtils.createTestSUSEProduct(family);
        Channel sourceBaseChannel = SUSEProductTestUtils.createBaseChannelForBaseProduct(sourceBaseProduct, user);

        List<SUSEProduct> sourceAddons = new ArrayList<>();
        SUSEProduct sourceAddonProduct = SUSEProductTestUtils.createTestSUSEProduct(family);
        Channel sourceChildChannel = SUSEProductTestUtils.createChildChannelsForProduct(
                sourceAddonProduct, sourceBaseChannel, user);
        SUSEProductExtension e = new SUSEProductExtension(
                sourceBaseProduct, sourceAddonProduct, sourceBaseProduct, false);
        TestUtils.saveAndReload(e);

        sourceAddons.add(sourceAddonProduct);
        SUSEProductSet sourceProducts = new SUSEProductSet(sourceBaseProduct, sourceAddons);

        SUSEProductTestUtils.populateRepository(sourceBaseProduct, sourceBaseChannel, sourceBaseProduct,
                sourceBaseChannel, user);
        SUSEProductTestUtils.populateRepository(sourceBaseProduct, sourceBaseChannel, sourceAddonProduct,
                sourceChildChannel, user);

        // Setup migration target product + upgrade path
        SUSEProduct targetBaseProduct = SUSEProductTestUtils.createTestSUSEProduct(family);
        Channel targetBaseChannel = SUSEProductTestUtils.createBaseChannelForBaseProduct(targetBaseProduct, user);
        sourceBaseProduct.setUpgrades(Collections.singleton(targetBaseProduct));

        // Setup target addon product + upgrade path
        SUSEProduct targetAddonProduct = SUSEProductTestUtils.createTestSUSEProduct(family);
        Channel targetAddonChannel = SUSEProductTestUtils.createChildChannelsForProduct(
                targetAddonProduct, targetBaseChannel, user);
        sourceAddonProduct.setUpgrades(Collections.singleton(targetAddonProduct));
        SUSEProductExtension e2 = new SUSEProductExtension(
                sourceBaseProduct, targetAddonProduct, sourceBaseProduct, false);
        SUSEProductExtension e3 = new SUSEProductExtension(
                targetBaseProduct, targetAddonProduct, targetBaseProduct, false);
        TestUtils.saveAndReload(e2);
        TestUtils.saveAndReload(e3);

        SUSEProductTestUtils.populateRepository(targetBaseProduct, targetBaseChannel, targetBaseProduct,
                targetBaseChannel, user);
        SUSEProductTestUtils.populateRepository(targetBaseProduct, targetBaseChannel, targetAddonProduct,
                targetAddonChannel, user);

        // Verify that target products are returned correctly

        ChannelArch arch = ChannelFactory.findArchByLabel("channel-x86_64");
        List<SUSEProductSet> targetProductSets =
                DistUpgradeManager.getTargetProductSets(Optional.of(sourceProducts), arch, user);

        targetProductSets = DistUpgradeManager.removeIncompatibleTargets(
                Optional.of(sourceProducts), targetProductSets);

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

    @Test
    public void testGetTargetProductSetsMissingChannel() throws Exception {
        SCCCredentials sccc = SUSEProductTestUtils.createSCCCredentials("dummy", user);
        // Setup source products
        ChannelFamily family = createTestChannelFamily();
        SUSEProduct sourceBaseProduct = SUSEProductTestUtils.createTestSUSEProduct(family);
        Channel sourceBaseChannel = SUSEProductTestUtils.createBaseChannelForBaseProduct(sourceBaseProduct, user);
        sourceBaseChannel.setLabel("sourceBaseChannel");

        List<SUSEProduct> sourceAddons = new ArrayList<>();
        SUSEProduct sourceAddonProduct = SUSEProductTestUtils.createTestSUSEProduct(family);
        Channel sourceChildChannel = SUSEProductTestUtils.createChildChannelsForProduct(
                sourceAddonProduct, sourceBaseChannel, user);
        sourceChildChannel.setLabel("sourceChildChannel");
        SUSEProductExtension e = new SUSEProductExtension(
                sourceBaseProduct, sourceAddonProduct, sourceBaseProduct, false);
        TestUtils.saveAndReload(e);

        sourceAddons.add(sourceAddonProduct);
        SUSEProductSet sourceProducts = new SUSEProductSet(sourceBaseProduct, sourceAddons);

        SUSEProductTestUtils.populateRepository(sourceBaseProduct, sourceBaseChannel, sourceBaseProduct,
                sourceBaseChannel, user);
        SUSEProductTestUtils.populateRepository(sourceBaseProduct, sourceBaseChannel, sourceAddonProduct,
                sourceChildChannel, user);

        // Setup migration target product + upgrade path
        SUSEProduct targetBaseProduct = SUSEProductTestUtils.createTestSUSEProduct(family);
        Channel targetBaseChannel = SUSEProductTestUtils.createBaseChannelForBaseProduct(targetBaseProduct, user);
        targetBaseChannel.setLabel("targetBaseChannel");
        sourceBaseProduct.setUpgrades(Collections.singleton(targetBaseProduct));

        // Setup target addon product + upgrade path
        SUSEProduct targetAddonProduct = SUSEProductTestUtils.createTestSUSEProduct(family);

        /* No Synced Channel for the target addon product !*/

        sourceAddonProduct.setUpgrades(Collections.singleton(targetAddonProduct));
        SUSEProductExtension e2 = new SUSEProductExtension(
                sourceBaseProduct, targetAddonProduct, sourceBaseProduct, false);
        SUSEProductExtension e3 = new SUSEProductExtension(
                targetBaseProduct, targetAddonProduct, targetBaseProduct, false);
        TestUtils.saveAndReload(e2);
        TestUtils.saveAndReload(e3);

        SUSEProductTestUtils.populateRepository(targetBaseProduct, targetBaseChannel, targetBaseProduct,
                targetBaseChannel, user);

        SCCRepository addon = SUSEProductTestUtils.createSCCRepository();
        SUSEProductTestUtils.createSCCRepositoryTokenAuth(sccc, addon);

        ChannelTemplate template = new ChannelTemplate();
        template.setProduct(targetAddonProduct);
        template.setRootProduct(targetBaseProduct);
        template.setRepository(addon);
        template.setChannelLabel("missing-addon-channel");
        template.setParentChannelLabel(targetBaseChannel.getLabel());
        template.setChannelName(targetBaseChannel.getLabel());
        template.setMandatory(true);
        template = TestUtils.saveAndReload(template);

        // Verify that target products are returned correctly

        ChannelArch arch = ChannelFactory.findArchByLabel("channel-x86_64");
        List<SUSEProductSet> targetProductSets =
                DistUpgradeManager.getTargetProductSets(Optional.of(sourceProducts), arch, user);

        targetProductSets = DistUpgradeManager.removeIncompatibleTargets(
                Optional.of(sourceProducts), targetProductSets);

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
     */
    @Test
    public void testCapabilityMissing() {
        Server server = ServerFactoryTest.createTestServer(user, true,
                ServerConstants.getServerGroupTypeEnterpriseEntitled());
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
    @Test
    public void testZyppPluginNotInstalled() throws Exception {
        Server server = ServerFactoryTest.createTestServer(user, true,
                ServerConstants.getServerGroupTypeEnterpriseEntitled());
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
    @Test
    public void testDistUpgradeScheduled() throws Exception {
        Server server = ServerFactoryTest.createTestServer(user, true,
                ServerConstants.getServerGroupTypeEnterpriseEntitled());
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
    @Test
    public void testMoreThanOneBaseChannel() throws Exception {
        Channel channel1 = ChannelFactoryTest.createTestChannel(user);
        Channel channel2 = ChannelFactoryTest.createTestChannel(user);

        // Try to upgrade with 2 base channels
        List<String> channels = new ArrayList<>();
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
    @Test
    public void testNoBaseChannel() throws Exception {
        Channel channel1 = ChannelFactoryTest.createTestChannel(user);
        Channel channel2 = ChannelFactoryTest.createTestChannel(user);
        Channel channel3 = ChannelFactoryTest.createTestChannel(user);

        // Make child channels and try to upgrade with no base channel
        channel2.setParentChannel(channel1);
        channel3.setParentChannel(channel1);
        List<String> channels = new ArrayList<>();
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
    @Test
    public void testIncompatibleBaseChannel() throws Exception {
        Channel channel1 = ChannelFactoryTest.createTestChannel(user);
        Channel channel2 = ChannelFactoryTest.createTestChannel(user);
        Channel channel3 = ChannelFactoryTest.createTestChannel(user);
        Channel channel4 = ChannelFactoryTest.createTestChannel(user);

        // Upgrade with child channels having different parents
        channel2.setParentChannel(channel1);
        channel4.setParentChannel(channel3);
        List<String> channels = new ArrayList<>();
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
    @Test
    public void testScheduleDistUpgrade() throws Exception {
        Channel subscribedChannel = ChannelFactoryTest.createTestChannel(user);
        List<Channel> subscribedChannels = new ArrayList<>(
                Arrays.asList(subscribedChannel));
        Server server = ErrataTestUtils.createTestServer(user, subscribedChannels);

        TaskomaticApi taskomaticMock = context.mock(TaskomaticApi.class);
        ActionManager.setTaskomaticApi(taskomaticMock);

        context.checking(new Expectations() { {
            allowing(taskomaticMock).scheduleActionExecution(with(any(Action.class)));
            allowing(taskomaticMock).scheduleActionExecution(with(any(Action.class)),
                    with(any(Boolean.class)));
        } });

        // Setup product upgrade
        ChannelFamily family = createTestChannelFamily();
        SUSEProduct sourceProduct = SUSEProductTestUtils.createTestSUSEProduct(family);
        Channel baseChannel = SUSEProductTestUtils.createBaseChannelForBaseProduct(sourceProduct, user);
        SUSEProduct addonProduct = SUSEProductTestUtils.createTestSUSEProduct(family);
        addonProduct.setBase(false);
        SUSEProduct target = SUSEProductTestUtils.createTestSUSEProduct(family);
        SUSEProductSet targetSet = new SUSEProductSet();
        targetSet.setBaseProduct(target);
        Set<InstalledProduct> installedProducts = new HashSet<>();
        installedProducts.add(SUSEProductTestUtils.getInstalledProduct(sourceProduct));
        installedProducts.add(SUSEProductTestUtils.getInstalledProduct(addonProduct));

        server.setInstalledProducts(installedProducts);
        // Setup channel tasks
        Channel channel1 = ChannelFactoryTest.createTestChannel(user);
        Channel channel2 = ChannelFactoryTest.createTestChannel(user);
        List<Long> channelIDs = new ArrayList<>();
        channelIDs.add(channel1.getId());
        channelIDs.add(channel2.getId());
        Date scheduleDate = new Date();

        Long actionID = DistUpgradeManager.scheduleDistUpgrade(
            user, List.of(server), targetSet, channelIDs, true, false, false, scheduleDate, null
        ).get(0).getId();

        // Get the scheduled action and check the contents
        DistUpgradeAction action = (DistUpgradeAction) ActionFactory.lookupById(actionID);
        assertInstanceOf(DistUpgradeAction.class, action);

        assertEquals(user, action.getSchedulerUser());
        assertEquals(scheduleDate, action.getEarliestAction());
        Set<ServerAction> serverActions = action.getServerActions();
        assertEquals(server, serverActions.iterator().next().getServer());
        DistUpgradeActionDetails details = action.getDetails(server.getId());
        assertTrue(details.isDryRun());
        assertFalse(details.isAllowVendorChange());
        //These products will be removed after migration
        assertEquals(addonProduct.getName(), details.getMissingSuccessors());

        // Check product upgrade
        Set<SUSEProductUpgrade> upgrades = details.getProductUpgrades();
        assertEquals(1, upgrades.size());
        for (SUSEProductUpgrade upgrade : upgrades) {
            assertEquals(sourceProduct, upgrade.getFromProduct());
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
     * Test getTargetProductSets():
     * SLES as base product with LTSS as addon. On the target product there is no successor for LTSS
     * Expected outcome: No migration available
     *
     * @throws Exception if anything goes wrong
     */
    @Test
    public void testGetTargetProductSetsLTSScase() throws Exception {
        SCCCredentials sccc = SUSEProductTestUtils.createSCCCredentials("dummy", user);
        // Setup source products
        ChannelFamily family = createTestChannelFamily();
        SUSEProduct slesSP1BaseProduct = SUSEProductTestUtils.createTestSUSEProduct(family);
        Channel slesSP1BaseChannel = SUSEProductTestUtils.createBaseChannelForBaseProduct(slesSP1BaseProduct, user);

        List<SUSEProduct> slesSP1Addons = new ArrayList<>();
        SUSEProduct ltssSP1AddonProduct = SUSEProductTestUtils.createTestSUSEProduct(family);
        Channel ltssSP1ChildChannel = SUSEProductTestUtils.createChildChannelsForProduct(
                ltssSP1AddonProduct, slesSP1BaseChannel, user);
        SUSEProductExtension e = new SUSEProductExtension(
                slesSP1BaseProduct, ltssSP1AddonProduct, slesSP1BaseProduct, false);
        TestUtils.saveAndReload(e);

        slesSP1Addons.add(ltssSP1AddonProduct);
        SUSEProductSet sourceProducts = new SUSEProductSet(slesSP1BaseProduct, slesSP1Addons);
        SUSEProductTestUtils.populateRepository(slesSP1BaseProduct, slesSP1BaseChannel, slesSP1BaseProduct,
                slesSP1BaseChannel, user);
        SUSEProductTestUtils.populateRepository(slesSP1BaseProduct, slesSP1BaseChannel, ltssSP1AddonProduct,
                ltssSP1ChildChannel, user);
        // Setup migration target product + upgrade path
        SUSEProduct slesSP2BaseProduct = SUSEProductTestUtils.createTestSUSEProduct(family);
        Channel slesSP2BaseChannel = SUSEProductTestUtils.createBaseChannelForBaseProduct(slesSP2BaseProduct, user);
        slesSP1BaseProduct.setUpgrades(Collections.singleton(slesSP2BaseProduct));
        SUSEProductTestUtils.populateRepository(slesSP2BaseProduct, slesSP2BaseChannel, slesSP2BaseProduct,
                slesSP2BaseChannel, user);
        // Verify that target products are returned correctly

        ChannelArch arch = ChannelFactory.findArchByLabel("channel-x86_64");
        List<SUSEProductSet> targetProductSets =
                DistUpgradeManager.getTargetProductSets(Optional.of(sourceProducts), arch, user);

        Set<SUSEProduct> msg = new HashSet<>();
        targetProductSets = DistUpgradeManager.removeIncompatibleTargets(
                Optional.of(sourceProducts), targetProductSets, msg);

        assertNotNull(targetProductSets);
        assertEquals(0, targetProductSets.size());
        assertTrue(msg.stream().anyMatch(missingProduct ->
            Objects.equals(missingProduct.getFriendlyName(), ltssSP1AddonProduct.getFriendlyName()))
        );

        // Setup target ltss addon product + upgrade path
        SUSEProduct ltssSP2AddonProduct = SUSEProductTestUtils.createTestSUSEProduct(family);
        Channel ltssSP2AddonChannel = SUSEProductTestUtils.createChildChannelsForProduct(
                ltssSP2AddonProduct, slesSP2BaseChannel, user);

        ltssSP1AddonProduct.setUpgrades(Collections.singleton(ltssSP2AddonProduct));
        SUSEProductExtension e3 = new SUSEProductExtension(
                slesSP2BaseProduct, ltssSP2AddonProduct, slesSP2BaseProduct, false);
        TestUtils.saveAndReload(e3);
        SUSEProductTestUtils.populateRepository(slesSP2BaseProduct, slesSP2BaseChannel, ltssSP2AddonProduct,
                ltssSP2AddonChannel, user);
        targetProductSets = DistUpgradeManager.getTargetProductSets(Optional.of(sourceProducts), arch, user);
        targetProductSets = DistUpgradeManager.removeIncompatibleTargets(
                Optional.of(sourceProducts), targetProductSets);
        // now the migration should be possible
        assertNotNull(targetProductSets);
        assertEquals(1, targetProductSets.size());

        for (SUSEProductSet target : targetProductSets) {
            if (target.getBaseProduct().getId() == slesSP2BaseProduct.getId()) {
                List<SUSEProduct> addonProducts = target.getAddonProducts();
                assertEquals(1, addonProducts.size());
                assertEquals(ltssSP2AddonProduct, addonProducts.get(0));
            }
            else {
                fail("unexpected product " + target.getBaseProduct());
            }
        }
    }

    /**
     * Create a SUSE product with arch == null. This is actually the case with
     * e.g. SLES for VMWARE.
     *
     * @param family the channel family
     * @return the channel product
     */
    public static SUSEProduct createTestSUSEProductNoArch(ChannelFamily family) {
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

    /**
     * Test for getTargetProductSets():
     * Product migration from Rocky Linux 9 includes as target RHEL and Liberty 9 Base
     * which in turn should show SUSE Liberty Linux 9 x86_64 as an extension
     *
     * @throws Exception if anything goes wrong
     */
    @Test
    public void testProductMigrationFromRocky9() throws Exception {
        // Setup source product Rocky Linux 9 and SUSE Manager Client Tools addon
        ChannelFamily family = createTestChannelFamily();

        SUSEProduct sourceBaseProductRocky9 = SUSEProductTestUtils.createTestSUSEProduct(family);
        sourceBaseProductRocky9.setName("rockylinux");
        sourceBaseProductRocky9.setVersion("9");
        sourceBaseProductRocky9.setFriendlyName("Test Rocky Linux 9 x86_64");
        sourceBaseProductRocky9 = TestUtils.saveAndReload(sourceBaseProductRocky9);
        Channel sourceBaseChannelRocky9 = SUSEProductTestUtils.createBaseChannelForBaseProduct(sourceBaseProductRocky9,
                user);
        SUSEProductTestUtils.populateRepository(sourceBaseProductRocky9, sourceBaseChannelRocky9,
                sourceBaseProductRocky9, sourceBaseChannelRocky9, user);


        SUSEProduct sourceAddonManagerTools = SUSEProductTestUtils.createTestSUSEProduct(family);
        sourceAddonManagerTools.setName("el-managertools");
        sourceAddonManagerTools.setVersion("9");
        sourceAddonManagerTools.setFriendlyName("Test SUSE Manager Client Tools for RHEL, Liberty and Clones 9 x86_64");
        SUSEProductExtension eManagerTools = new SUSEProductExtension(
                sourceBaseProductRocky9, sourceAddonManagerTools, sourceBaseProductRocky9, false);
        TestUtils.saveAndReload(eManagerTools);
        Channel sourceChildChannelManagerTools = SUSEProductTestUtils.createChildChannelsForProduct(
                sourceAddonManagerTools, sourceBaseChannelRocky9, user);
        SUSEProductTestUtils.populateRepository(sourceBaseProductRocky9, sourceBaseChannelRocky9,
                sourceAddonManagerTools, sourceChildChannelManagerTools, user);

        // Setup target product RHEL and Liberty 9 Base
        SUSEProduct targetBaseProductRhel9 = SUSEProductTestUtils.createTestSUSEProduct(family);
        targetBaseProductRhel9.setName("el-base");
        targetBaseProductRhel9.setVersion("9");
        targetBaseProductRhel9.setFriendlyName("Test RHEL and Liberty 9 Base");
        targetBaseProductRhel9 = TestUtils.saveAndReload(targetBaseProductRhel9);
        Channel targetBaseChannelRhel9 = SUSEProductTestUtils.createBaseChannelForBaseProduct(targetBaseProductRhel9,
                user);
        SUSEProductTestUtils.populateRepository(targetBaseProductRhel9, targetBaseChannelRhel9,
                targetBaseProductRhel9, targetBaseChannelRhel9, user);

        SUSEProductExtension eTargetRhelManagerTools = new SUSEProductExtension(
                targetBaseProductRhel9, sourceAddonManagerTools, targetBaseProductRhel9, false);
        TestUtils.saveAndReload(eTargetRhelManagerTools);

        SUSEProduct targetAddonLiberty9 = SUSEProductTestUtils.createTestSUSEProduct(family);
        targetAddonLiberty9.setName("sll");
        targetAddonLiberty9.setVersion("9");
        targetAddonLiberty9.setFriendlyName("Test SUSE Liberty Linux 9 x86_64");
        SUSEProductExtension eLiberty9 = new SUSEProductExtension(
                targetBaseProductRhel9, targetAddonLiberty9, targetBaseProductRhel9, false);
        TestUtils.saveAndReload(eLiberty9);
        Channel targeChildChannelLiberty9 = SUSEProductTestUtils.createChildChannelsForProduct(
                targetAddonLiberty9, targetBaseChannelRhel9, user);
        SUSEProductTestUtils.populateRepository(targetBaseProductRhel9, targetBaseChannelRhel9,
                targetAddonLiberty9, targeChildChannelLiberty9, user);


        targetBaseProductRhel9.setDowngrades(Collections.singleton(sourceBaseProductRocky9));
        sourceBaseProductRocky9.setUpgrades(Collections.singleton(targetBaseProductRhel9));

        List<SUSEProduct> sourceAddonsRocky9 = new ArrayList<>();
        sourceAddonsRocky9.add(sourceAddonManagerTools);

        SUSEProductSet sourceProducts = new SUSEProductSet(sourceBaseProductRocky9, sourceAddonsRocky9);

        ChannelArch arch = ChannelFactory.findArchByLabel("channel-x86_64");
        List<SUSEProductSet> targetProductSets =
                DistUpgradeManager.getTargetProductSets(Optional.of(sourceProducts), arch, user);

        //tests
        assertNotNull(targetProductSets);
        assertEquals(1, targetProductSets.size());

        for (SUSEProductSet target : targetProductSets) {
            if (target.getBaseProduct().getId() == targetBaseProductRhel9.getId()) {
                assertEquals("el-base", target.getBaseProduct().getName());
                assertEquals("9", target.getBaseProduct().getVersion());

                List<SUSEProduct> addonProducts = target.getAddonProducts();
                assertEquals(2, addonProducts.size());
                for (SUSEProduct addonProduct : addonProducts) {
                    if (addonProduct.getId() == sourceAddonManagerTools.getId()) {
                        assertEquals("el-managertools", targetProductSets.get(0).getAddonProducts().get(0).getName());
                    }
                    else if (addonProduct.getId() == targetAddonLiberty9.getId()) {
                        assertEquals("sll", targetProductSets.get(0).getAddonProducts().get(1).getName());
                    }
                    else {
                        fail("unexpected addon " + addonProduct);
                    }
                }

            }
            else {
                fail("unexpected product " + target.getBaseProduct());
            }
        }

        targetProductSets = DistUpgradeManager.removeIncompatibleTargets(
                Optional.of(sourceProducts), targetProductSets);

        assertNotNull(targetProductSets);
        assertEquals(1, targetProductSets.size());
        assertEquals(2, targetProductSets.get(0).getAddonProducts().size());
    }


    /**
     * Test for getTargetProductSets():
     * Product migration from RHEL and Liberty 9 Base
     * should show no migrations possible
     *
     * @throws Exception if anything goes wrong
     */
    @Test
    public void testProductMigrationFromLiberty9() throws Exception {
        // Setup source product RHEL and Liberty 9 Base
        ChannelFamily family = createTestChannelFamily();

        SUSEProduct sourceBaseProductRhel9 = SUSEProductTestUtils.createTestSUSEProduct(family);
        sourceBaseProductRhel9.setName("el-base");
        sourceBaseProductRhel9.setVersion("9");
        sourceBaseProductRhel9.setFriendlyName("Test RHEL and Liberty 9 Base");
        sourceBaseProductRhel9 = TestUtils.saveAndReload(sourceBaseProductRhel9);
        Channel sourceBaseChannelRhel9 = SUSEProductTestUtils.createBaseChannelForBaseProduct(sourceBaseProductRhel9,
                user);
        SUSEProductTestUtils.populateRepository(sourceBaseProductRhel9, sourceBaseChannelRhel9,
                sourceBaseProductRhel9, sourceBaseChannelRhel9, user);


        SUSEProduct sourceAddonManagerTools = SUSEProductTestUtils.createTestSUSEProduct(family);
        sourceAddonManagerTools.setName("el-managertools");
        sourceAddonManagerTools.setVersion("9");
        sourceAddonManagerTools.setFriendlyName("Test SUSE Manager Client Tools for RHEL, Liberty and Clones 9 x86_64");
        SUSEProductExtension eManagerTools = new SUSEProductExtension(
                sourceBaseProductRhel9, sourceAddonManagerTools, sourceBaseProductRhel9, false);
        TestUtils.saveAndReload(eManagerTools);
        Channel sourceChildChannelManagerTools = SUSEProductTestUtils.createChildChannelsForProduct(
                sourceAddonManagerTools, sourceBaseChannelRhel9, user);
        SUSEProductTestUtils.populateRepository(sourceBaseProductRhel9, sourceBaseChannelRhel9,
                sourceAddonManagerTools, sourceChildChannelManagerTools, user);


        SUSEProduct sourceAddonLiberty9 = SUSEProductTestUtils.createTestSUSEProduct(family);
        sourceAddonLiberty9.setName("sll");
        sourceAddonLiberty9.setVersion("9");
        sourceAddonLiberty9.setFriendlyName("Test SUSE Liberty Linux 9 x86_64");
        SUSEProductExtension eLiberty9 = new SUSEProductExtension(
                sourceBaseProductRhel9, sourceAddonLiberty9, sourceBaseProductRhel9, false);
        TestUtils.saveAndReload(eLiberty9);
        Channel sourceChildChannelLiberty9 = SUSEProductTestUtils.createChildChannelsForProduct(
                sourceAddonLiberty9, sourceBaseChannelRhel9, user);
        SUSEProductTestUtils.populateRepository(sourceBaseProductRhel9, sourceBaseChannelRhel9,
                sourceAddonLiberty9, sourceChildChannelLiberty9, user);


        List<SUSEProduct> sourceAddonsLiberty9 = new ArrayList<>();
        sourceAddonsLiberty9.add(sourceAddonLiberty9);
        sourceAddonsLiberty9.add(sourceAddonManagerTools);

        SUSEProductSet sourceProducts = new SUSEProductSet(sourceBaseProductRhel9, sourceAddonsLiberty9);

        ChannelArch arch = ChannelFactory.findArchByLabel("channel-x86_64");
        List<SUSEProductSet> targetProductSets = DistUpgradeManager.getTargetProductSets(
                Optional.of(sourceProducts), arch, user);

        //tests
        assertNotNull(targetProductSets);
        assertEquals(0, targetProductSets.size());

        targetProductSets = DistUpgradeManager.removeIncompatibleTargets(
                Optional.of(sourceProducts), targetProductSets);

        assertNotNull(targetProductSets);
        assertEquals(0, targetProductSets.size());
    }
}
