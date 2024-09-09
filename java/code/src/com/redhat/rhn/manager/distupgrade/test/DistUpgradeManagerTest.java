/*
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
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
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
import com.redhat.rhn.domain.product.ReleaseStage;
import com.redhat.rhn.domain.product.SUSEProduct;
import com.redhat.rhn.domain.product.SUSEProductExtension;
import com.redhat.rhn.domain.product.SUSEProductSCCRepository;
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
                targetProductSets, Optional.empty());
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
                targetProductSets, Optional.empty());
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
                targetProductSets, Optional.empty());

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
                Optional.of(sourceProducts), targetProductSets, Optional.empty());

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
                Optional.of(sourceProducts), targetProductSets, Optional.empty());

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
        List<SUSEProductSet> targetProductSets =
                DistUpgradeManager.getTargetProductSets(Optional.of(sourceProducts), arch, user);

        targetProductSets = DistUpgradeManager.removeIncompatibleTargets(
                Optional.of(sourceProducts), targetProductSets, Optional.empty());

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
                user, server, targetSet, channelIDs, true, false, scheduleDate, false);
        // Get the scheduled action and check the contents
        DistUpgradeAction action = (DistUpgradeAction) ActionFactory.lookupById(actionID);
        assertEquals(ActionFactory.TYPE_DIST_UPGRADE, action.getActionType());
        assertEquals(user, action.getSchedulerUser());
        assertEquals(scheduleDate, action.getEarliestAction());
        Set<ServerAction> serverActions = action.getServerActions();
        assertEquals(server, serverActions.iterator().next().getServer());
        DistUpgradeActionDetails details = action.getDetails();
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

        Set<String> msg = new HashSet<>();
        targetProductSets = DistUpgradeManager.removeIncompatibleTargets(
                Optional.of(sourceProducts), targetProductSets, Optional.of(msg));

        assertNotNull(targetProductSets);
        assertEquals(0, targetProductSets.size());
        assert msg.contains(ltssSP1AddonProduct.getFriendlyName());

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
                Optional.of(sourceProducts), targetProductSets, Optional.empty());
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

}
