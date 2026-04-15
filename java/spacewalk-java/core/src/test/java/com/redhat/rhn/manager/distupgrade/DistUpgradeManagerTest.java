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
package com.redhat.rhn.manager.distupgrade;

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
import com.redhat.rhn.domain.action.ActionFactoryTest;
import com.redhat.rhn.domain.action.dup.DistUpgradeAction;
import com.redhat.rhn.domain.action.dup.DistUpgradeActionDetails;
import com.redhat.rhn.domain.action.dup.DistUpgradeChannelTask;
import com.redhat.rhn.domain.action.server.ServerAction;
import com.redhat.rhn.domain.channel.Channel;
import com.redhat.rhn.domain.channel.ChannelArch;
import com.redhat.rhn.domain.channel.ChannelFactory;
import com.redhat.rhn.domain.channel.ChannelFactoryTest;
import com.redhat.rhn.domain.channel.ChannelFamily;
import com.redhat.rhn.domain.channel.ChannelProduct;
import com.redhat.rhn.domain.credentials.SCCCredentials;
import com.redhat.rhn.domain.iss.IssFactory;
import com.redhat.rhn.domain.iss.IssMaster;
import com.redhat.rhn.domain.product.ChannelTemplate;
import com.redhat.rhn.domain.product.ReleaseStage;
import com.redhat.rhn.domain.product.SUSEProduct;
import com.redhat.rhn.domain.product.SUSEProductExtension;
import com.redhat.rhn.domain.product.SUSEProductSet;
import com.redhat.rhn.domain.product.SUSEProductTestUtils;
import com.redhat.rhn.domain.product.SUSEProductUpgrade;
import com.redhat.rhn.domain.rhnpackage.Package;
import com.redhat.rhn.domain.rhnpackage.PackageName;
import com.redhat.rhn.domain.rhnpackage.PackageTest;
import com.redhat.rhn.domain.scc.SCCRepository;
import com.redhat.rhn.domain.server.InstalledProduct;
import com.redhat.rhn.domain.server.MinionServer;
import com.redhat.rhn.domain.server.MinionServerFactoryTest;
import com.redhat.rhn.domain.server.Server;
import com.redhat.rhn.domain.server.ServerConstants;
import com.redhat.rhn.domain.server.ServerFactoryTest;
import com.redhat.rhn.frontend.dto.EssentialChannelDto;
import com.redhat.rhn.manager.action.ActionManager;
import com.redhat.rhn.manager.rhnpackage.PackageManager;
import com.redhat.rhn.manager.system.SystemManagerTest;
import com.redhat.rhn.taskomatic.TaskomaticApi;
import com.redhat.rhn.testing.BaseTestCaseWithUser;
import com.redhat.rhn.testing.ErrataTestUtils;
import com.redhat.rhn.testing.TestUtils;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;

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
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
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
        SUSEProduct product = createTestSUSEProductNoArch();
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
        e = TestUtils.saveAndReload(e);

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
        e2 = TestUtils.saveAndReload(e2);
        e3 = TestUtils.saveAndReload(e3);

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

        SUSEProductTestUtils.createSCCCredentials("dummy", user);
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
        e = TestUtils.saveAndReload(e);

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
        e2 = TestUtils.saveAndReload(e2);
        e3 = TestUtils.saveAndReload(e3);

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
        e = TestUtils.saveAndReload(e);

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
        e2 = TestUtils.saveAndReload(e2);
        e3 = TestUtils.saveAndReload(e3);

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
            name = TestUtils.saveAndFlush(name);
        }
        else {
            // Handle the case that the package name exists in the DB
            zyppPlugin.setPackageName(name);
            zyppPlugin = TestUtils.saveAndFlush(zyppPlugin);
        }
        ErrataTestUtils.createTestInstalledPackage(zyppPlugin, server);

        // Store a dist upgrade action for this server
        Action action = ActionFactoryTest.createAction(user,
                ActionFactory.TYPE_DIST_UPGRADE);
        ServerAction serverAction = ActionFactoryTest.createServerAction(server, action);
        serverAction = TestUtils.saveAndFlush(serverAction);

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
        SUSEProductTestUtils.createBaseChannelForBaseProduct(sourceProduct, user);
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
        SUSEProductTestUtils.createSCCCredentials("dummy", user);
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
        e = TestUtils.saveAndReload(e);

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
        e3 = TestUtils.saveAndReload(e3);
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
     * @return the channel product
     */
    public static SUSEProduct createTestSUSEProductNoArch() {
        SUSEProduct product = new SUSEProduct();
        String name = TestUtils.randomString().toLowerCase();
        product.setName(name);
        product.setVersion("1");
        product.setFriendlyName("SUSE Test product " + name);
        product.setArch(null);
        product.setRelease("test");
        product.setReleaseStage(ReleaseStage.released);
        product.setProductId(0);
        product = TestUtils.saveAndFlush(product);
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
        channel = TestUtils.saveAndFlush(channel);
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
        eManagerTools = TestUtils.saveAndReload(eManagerTools);
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
        eTargetRhelManagerTools = TestUtils.saveAndReload(eTargetRhelManagerTools);

        SUSEProduct targetAddonLiberty9 = SUSEProductTestUtils.createTestSUSEProduct(family);
        targetAddonLiberty9.setName("sll");
        targetAddonLiberty9.setVersion("9");
        targetAddonLiberty9.setFriendlyName("Test SUSE Liberty Linux 9 x86_64");
        SUSEProductExtension eLiberty9 = new SUSEProductExtension(
                targetBaseProductRhel9, targetAddonLiberty9, targetBaseProductRhel9, false);
        eLiberty9 = TestUtils.saveAndReload(eLiberty9);
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
        eManagerTools = TestUtils.saveAndReload(eManagerTools);
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
        eLiberty9 = TestUtils.saveAndReload(eLiberty9);
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

    private static SUSEProduct buildSlesProduct(String name, String version, boolean base) {
        SUSEProduct p = new SUSEProduct(name);
        p.setVersion(version);
        p.setBase(base);
        p.setProductId(42L);
        p.setFriendlyName(name + " " + version);
        return p;
    }

    private static JsonObject buildVerifySuccessResult(String comment) {
        JsonObject state = new JsonObject();
        state.addProperty("result", true);
        state.addProperty("comment", comment);
        JsonObject root = new JsonObject();
        root.add("sles16_migration_success", state);
        return root;
    }

    private static JsonObject buildVerifyFailureResult(String comment) {
        JsonObject state = new JsonObject();
        state.addProperty("result", false);
        state.addProperty("comment", comment);
        JsonObject root = new JsonObject();
        root.add("sles16_migration_failed", state);
        return root;
    }

    private static JsonObject buildUnrelatedStateResult() {
        JsonObject state = new JsonObject();
        state.addProperty("result", true);
        JsonObject root = new JsonObject();
        root.add("some_other_state", state);
        return root;
    }
    @Test
    public void testIsSles15To16MigrationTrue() {
        SUSEProduct from = buildSlesProduct("sles", "15.7", true);
        SUSEProduct to   = buildSlesProduct("sles", "16.0", true);
        DistUpgradeActionDetails details = new DistUpgradeActionDetails();
        details.setProductUpgrades(Set.of(new SUSEProductUpgrade(from, to)));
        assertTrue(details.isSles15To16Migration());
    }

    @Test
    public void testIsSles15To16MigrationFalseForReverseUpgrade() {
        SUSEProduct from = buildSlesProduct("sles", "16.0", true);
        SUSEProduct to   = buildSlesProduct("sles", "15.7", true);
        DistUpgradeActionDetails details = new DistUpgradeActionDetails();
        details.setProductUpgrades(Set.of(new SUSEProductUpgrade(from, to)));
        assertFalse(details.isSles15To16Migration());
    }

    @Test
    public void testIsVerificationStateResultTrueForSuccess() {
        assertTrue(DistUpgradeAction.isMajorMigrationVerificationResult(
                buildVerifySuccessResult("SLES 16 migration completed successfully")));
    }

    @Test
    public void testIsVerificationStateResultTrueForFailure() {
        assertTrue(DistUpgradeAction.isMajorMigrationVerificationResult(
                buildVerifyFailureResult("SLES 16 migration may have failed")));
    }

    @Test
    public void testIsVerificationStateResultFalseForUnrelatedResult() {
        assertFalse(DistUpgradeAction.isMajorMigrationVerificationResult(buildUnrelatedStateResult()));
    }

    @Test
    public void testIsVerificationStateResultFalseForNull() {
        assertFalse(DistUpgradeAction.isMajorMigrationVerificationResult(null));
    }

    @Test
    public void testIsVerificationStateResultFalseForNonObject() {
        assertFalse(DistUpgradeAction.isMajorMigrationVerificationResult(
                JsonParser.parseString("\"not-an-object\"")));
    }
    @Test
    public void testHandleVerificationResultSuccess() throws Exception {
        MinionServer minion = MinionServerFactoryTest.createTestMinionServer(user);
        DistUpgradeAction dupAction = (DistUpgradeAction) ActionFactoryTest.createAction(
                user, ActionFactory.TYPE_DIST_UPGRADE);
        dupAction.setDetailsMap(new HashMap<>());
        ServerAction sa = ActionFactoryTest.createServerAction(minion, dupAction);
        dupAction.addServerAction(sa);
        DistUpgradeActionDetails det = new DistUpgradeActionDetails();
        det.setServer(minion);
        det.setDryRun(false);
        dupAction.setDetails(det);

        String comment = "SLES 16 migration completed successfully\nCurrent OS: SLES 16.0";
        sa.setStatusPickedUp();
        dupAction.handleUpdateServerAction(sa, buildVerifySuccessResult(comment), null);

        assertTrue(sa.isStatusCompleted(), "Verify success must set action COMPLETED");
        assertNotNull(sa.getCompletionTime(), "Completion time must be set");
        assertTrue(sa.getResultMsg().contains(comment), "Result must include Salt comment");
        assertTrue(sa.getResultMsg().contains("zypper packages --orphaned"),
                "Result must include post-migration recommendations");
    }

    @Test
    public void testHandleVerificationResultFailure() throws Exception {
        MinionServer minion = MinionServerFactoryTest.createTestMinionServer(user);
        DistUpgradeAction dupAction = (DistUpgradeAction) ActionFactoryTest.createAction(
                user, ActionFactory.TYPE_DIST_UPGRADE);
        dupAction.setDetailsMap(new HashMap<>());
        ServerAction sa = ActionFactoryTest.createServerAction(minion, dupAction);
        dupAction.addServerAction(sa);
        DistUpgradeActionDetails det = new DistUpgradeActionDetails();
        det.setServer(minion);
        det.setDryRun(false);
        dupAction.setDetails(det);

        sa.setStatusPickedUp();
        dupAction.handleUpdateServerAction(sa,
                buildVerifyFailureResult("SLES 16 migration may have failed"), null);

        assertTrue(sa.isStatusFailed(), "Verify failure must set action FAILED");
        assertNotNull(sa.getCompletionTime(), "Completion time must be set on failure");
        assertTrue(sa.getResultMsg().contains("failed"));
    }

    // -- handleUpdateServerAction(): initial migration keeps action In Progress

    @Test
    public void testInitialMigrationStateKeepsActionInProgress() throws Exception {
        MinionServer minion = MinionServerFactoryTest.createTestMinionServer(user);
        DistUpgradeAction dupAction = (DistUpgradeAction) ActionFactoryTest.createAction(
                user, ActionFactory.TYPE_DIST_UPGRADE);
        dupAction.setDetailsMap(new HashMap<>());
        ServerAction sa = ActionFactoryTest.createServerAction(minion, dupAction);
        dupAction.addServerAction(sa);

        DistUpgradeActionDetails det = new DistUpgradeActionDetails();
        det.setServer(minion);
        det.setDryRun(false);
        det.setProductUpgrades(Set.of(new SUSEProductUpgrade(
                buildSlesProduct("sles", "15.7", true),
                buildSlesProduct("sles", "16.0", true))));
        dupAction.setDetails(det);

        // Simulate SaltUtils prematurely marking the action complete
        sa.setStatusCompleted();

        // A non-verify result (e.g. the initial sles16.sls state completing)
        dupAction.handleUpdateServerAction(sa, buildUnrelatedStateResult(), null);

        assertTrue(sa.isStatusPickedUp(),
                "Action must be reset to In Progress while minion is offline after migration start");
    }

    /**
     * ActionManager.scheduleDistUpgrade() must pass forcePackageListRefresh=false to Taskomatic
     * for a SLES 15->16 cross-major migration.
     *
     * This is critical: the minion reboots offline during the DMS migration. Any package
     * refresh triggered beforehand would fail or produce stale data. The flag is suppressed
     * and will be triggered manually once the minion reconnects and verification passes.
     */
    @Test
    public void testScheduleDistUpgradeSles16DoesNotForcePackageListRefresh() throws Exception {
        // Set up a SLES 15 -> 16 migration — products that satisfy isSles15To16Migration()
        SUSEProduct sles15 = SUSEProductTestUtils.createTestSUSEProduct(user, "sles", "15.7", "x86_64", "7261", true);
        SUSEProduct sles16 = SUSEProductTestUtils.createTestSUSEProduct(user, "sles", "16.0", "x86_64", "7261", true);

        MinionServer minion = MinionServerFactoryTest.createTestMinionServer(user);
        Channel subscribedChannel = ChannelFactoryTest.createTestChannel(user);
        minion.addChannel(subscribedChannel);

        DistUpgradeActionDetails det = new DistUpgradeActionDetails();
        det.setServer(minion);
        det.setDryRun(false);
        det.setAllowVendorChange(false);
        det.setProductUpgrades(Set.of(new SUSEProductUpgrade(sles15, sles16)));

        Channel sles16Channel = ChannelFactoryTest.createTestChannel(user);
        DistUpgradeChannelTask subscribeTask = new DistUpgradeChannelTask();
        subscribeTask.setChannel(sles16Channel);
        subscribeTask.setTask(DistUpgradeChannelTask.SUBSCRIBE);
        det.addChannelTask(subscribeTask);

        Map<Long, DistUpgradeActionDetails> detailsMap = Map.of(minion.getId(), det);

        // Capture the forcePackageListRefresh argument passed to Taskomatic
        TaskomaticApi taskomaticMock = context.mock(TaskomaticApi.class, "taskomaticSles16Test");
        ActionManager.setTaskomaticApi(taskomaticMock);

        // The key assertion: must call scheduleActionExecution(action, false) for SLES16 migrations
        context.checking(new Expectations() { {
            oneOf(taskomaticMock).scheduleActionExecution(
                    with(any(Action.class)),
                    with(equal(false)));
        } });

        List<DistUpgradeAction> actions = ActionManager.scheduleDistUpgrade(
                user, new Date(), null, false, detailsMap);

        assertFalse(actions.isEmpty(), "Should have scheduled at least one action");
    }

    private JsonElement loadSles16VerifySuccessFixture() {
        try (var reader = new java.io.InputStreamReader(
                getClass().getResourceAsStream(
                        "/com/suse/manager/reactor/messaging/sles16_verify_success.json"))) {
            return JsonParser.parseReader(reader);
        }
        catch (java.io.IOException e) {
            throw new RuntimeException("Failed to load sles16_verify_success.json fixture", e);
        }
    }

    @Test
    public void testIsVerificationStateResultWithRealFullStateKey() {
        JsonElement result = loadSles16VerifySuccessFixture();
        assertTrue(DistUpgradeAction.isMajorMigrationVerificationResult(result),
                "The real full state key must be detected as a verify result");
    }

    @Test
    public void testIsVerificationStateResultFalseForRealInitialMigrationResult() {
        JsonObject rebootState = new JsonObject();
        rebootState.addProperty("result", true);
        rebootState.addProperty("comment", "Command \"/usr/sbin/shutdown -r +1\" run");

        JsonObject markerState = new JsonObject();
        markerState.addProperty("result", true);
        markerState.addProperty("comment", "File /var/lib/uyuni/sles16_migration_started updated");

        JsonObject result = new JsonObject();
        result.add("cmd_|-sles16_migration_reboot_|-/usr/sbin/shutdown -r +1_|-run", rebootState);
        result.add("file_|-sles16_migration_marker_|-/var/lib/uyuni/sles16_migration_started_|-managed",
                markerState);

        assertFalse(DistUpgradeAction.isMajorMigrationVerificationResult(result),
                "The initial sles16.sls result must NOT be detected as a verify result");
    }

    @Test
    public void testHandleVerificationResultSuccessWithRealFixture() throws Exception {
        MinionServer minion = MinionServerFactoryTest.createTestMinionServer(user);
        DistUpgradeAction dupAction = (DistUpgradeAction) ActionFactoryTest.createAction(
                user, ActionFactory.TYPE_DIST_UPGRADE);
        dupAction.setDetailsMap(new HashMap<>());
        ServerAction sa = ActionFactoryTest.createServerAction(minion, dupAction);
        dupAction.addServerAction(sa);
        DistUpgradeActionDetails det = new DistUpgradeActionDetails();
        det.setServer(minion);
        det.setDryRun(false);
        dupAction.setDetails(det);

        sa.setStatusPickedUp();
        dupAction.handleUpdateServerAction(sa, loadSles16VerifySuccessFixture(), null);

        assertTrue(sa.isStatusCompleted(),
                "Real verify success result must set action to COMPLETED");
        assertNotNull(sa.getCompletionTime());

        String msg = sa.getResultMsg();
        assertTrue(msg.contains("SLES 16 migration completed successfully"),
                "Result message must contain the real Salt comment prefix");
        assertTrue(msg.contains("Current OS: SLES 16.0"),
                "Result message must contain the OS version from the real comment");
        assertTrue(msg.contains("zypper packages --orphaned"),
                "Result message must include post-migration recommendations");
    }
    @Test
    public void testHandleVerificationExtractsMessageFallbackWhenNoComment() throws Exception {
        MinionServer minion = MinionServerFactoryTest.createTestMinionServer(user);
        DistUpgradeAction dupAction = (DistUpgradeAction) ActionFactoryTest.createAction(
                user, ActionFactory.TYPE_DIST_UPGRADE);
        dupAction.setDetailsMap(new HashMap<>());
        ServerAction sa = ActionFactoryTest.createServerAction(minion, dupAction);
        dupAction.addServerAction(sa);
        DistUpgradeActionDetails det = new DistUpgradeActionDetails();
        det.setServer(minion);
        det.setDryRun(false);
        dupAction.setDetails(det);

        JsonObject stateNoComment = new JsonObject();
        stateNoComment.addProperty("result", false);
        JsonObject noCommentResult = new JsonObject();
        noCommentResult.add(
                "test_|-sles16_migration_failed_|-sles16_migration_failed_|-configurable_test_state",
                stateNoComment);

        sa.setStatusPickedUp();
        dupAction.handleUpdateServerAction(sa, noCommentResult, null);

        assertTrue(sa.isStatusFailed(), "Missing comment must still fail the action");
        assertTrue(sa.getResultMsg().contains("Migration verification finished"),
                "Fallback message must be used when 'comment' field is absent");
    }

    @Test
    public void testHandleVerificationResultFailureForcesRepoRevertForSles16() throws Exception {
        MinionServer minion = MinionServerFactoryTest.createTestMinionServer(user);
        DistUpgradeAction dupAction = (DistUpgradeAction) ActionFactoryTest.createAction(
                user, ActionFactory.TYPE_DIST_UPGRADE);
        dupAction.setDetailsMap(new HashMap<>());
        ServerAction sa = ActionFactoryTest.createServerAction(minion, dupAction);
        dupAction.addServerAction(sa);

        // Set up a SLES 15 -> 16 migration
        SUSEProduct sles15 = SUSEProductTestUtils.createTestSUSEProduct(user, "sles", "15.7", "x86_64", "7261", true);
        SUSEProduct sles16 = SUSEProductTestUtils.createTestSUSEProduct(user, "sles", "16.0", "x86_64", "7261", true);

        DistUpgradeActionDetails det = new DistUpgradeActionDetails();
        det.setServer(minion);
        det.setDryRun(false);
        det.addProductUpgrade(new SUSEProductUpgrade(sles15, sles16));

        // Channels setup
        Channel sp7Channel = ChannelFactoryTest.createTestChannel(user);
        DistUpgradeChannelTask unsubscribeTask = new DistUpgradeChannelTask();
        unsubscribeTask.setChannel(sp7Channel);
        unsubscribeTask.setTask(DistUpgradeChannelTask.UNSUBSCRIBE);
        det.addChannelTask(unsubscribeTask);

        Channel sles16Channel = ChannelFactoryTest.createTestChannel(user);
        DistUpgradeChannelTask subscribeTask = new DistUpgradeChannelTask();
        subscribeTask.setChannel(sles16Channel);
        subscribeTask.setTask(DistUpgradeChannelTask.SUBSCRIBE);
        det.addChannelTask(subscribeTask);

        dupAction.setDetails(det);
        TestUtils.saveAndFlush(dupAction);

        // Result indicating failure
        JsonObject stateFailed = new JsonObject();
        stateFailed.addProperty("result", false);
        stateFailed.addProperty("comment", "Migration failed");
        JsonObject failedResult = new JsonObject();
        failedResult.add(
                "test_|-sles16_migration_failed_|-sles16_migration_failed_|-configurable_test_state",
                stateFailed);

        sa.setStatusPickedUp();

        // This triggers revertToOriginalChannels -> switchChannels
        // For SLES 15->16, even if hasSles16Channels is false, it should call switchChannels
        dupAction.handleUpdateServerAction(sa, failedResult, null);

        assertTrue(sa.isStatusFailed(), "Action must be failed");
        String msg = sa.getResultMsg();
        assertTrue(msg != null && (msg.toLowerCase().contains("migration failed") ||
                                   msg.contains("distupgrade.sles16.migration.failed")),
                "Result message should contain the failure message. Msg was: " + msg);
    }
}
