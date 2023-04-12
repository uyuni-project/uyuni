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
/**
 * Copyright (c) 2014 Red Hat, Inc.
 */

package com.redhat.rhn.frontend.xmlrpc.chain.test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.junit.jupiter.api.Assertions.fail;

import com.redhat.rhn.common.db.datasource.DataResult;
import com.redhat.rhn.domain.action.Action;
import com.redhat.rhn.domain.action.ActionChain;
import com.redhat.rhn.domain.action.ActionChainEntry;
import com.redhat.rhn.domain.action.ActionChainFactory;
import com.redhat.rhn.domain.action.ActionFactory;
import com.redhat.rhn.domain.action.script.ScriptRunAction;
import com.redhat.rhn.domain.channel.Channel;
import com.redhat.rhn.domain.channel.test.ChannelFactoryTest;
import com.redhat.rhn.domain.config.ConfigFile;
import com.redhat.rhn.domain.config.ConfigRevision;
import com.redhat.rhn.domain.errata.Errata;
import com.redhat.rhn.domain.errata.ErrataFactory;
import com.redhat.rhn.domain.errata.test.ErrataFactoryTest;
import com.redhat.rhn.domain.rhnpackage.Package;
import com.redhat.rhn.domain.rhnpackage.test.PackageTest;
import com.redhat.rhn.domain.server.InstalledPackage;
import com.redhat.rhn.domain.server.Server;
import com.redhat.rhn.domain.server.ServerFactory;
import com.redhat.rhn.domain.server.test.ServerFactoryTest;
import com.redhat.rhn.domain.session.InvalidSessionIdException;
import com.redhat.rhn.frontend.dto.PackageListItem;
import com.redhat.rhn.frontend.xmlrpc.BaseHandler;
import com.redhat.rhn.frontend.xmlrpc.InvalidPackageException;
import com.redhat.rhn.frontend.xmlrpc.InvalidParameterException;
import com.redhat.rhn.frontend.xmlrpc.NoSuchActionChainException;
import com.redhat.rhn.frontend.xmlrpc.NoSuchActionException;
import com.redhat.rhn.frontend.xmlrpc.chain.ActionChainHandler;
import com.redhat.rhn.frontend.xmlrpc.test.BaseHandlerTestCase;
import com.redhat.rhn.manager.errata.cache.ErrataCacheManager;
import com.redhat.rhn.manager.errata.cache.test.ErrataCacheManagerTest;
import com.redhat.rhn.manager.rhnpackage.PackageManager;
import com.redhat.rhn.manager.system.SystemManager;
import com.redhat.rhn.manager.system.test.SystemManagerTest;
import com.redhat.rhn.testing.ConfigTestUtils;
import com.redhat.rhn.testing.TestUtils;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.text.DateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Base64;
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * Test cases for the Action Chain XML-RPC API
 */
public class ActionChainHandlerTest extends BaseHandlerTestCase {

    private ActionChainHandler ach;
    private static final String CHAIN_LABEL = "Quick Brown Fox";
    private static final String SCRIPT_LABEL = "Script Label";
    private static final String SCRIPT_SAMPLE = "#!/bin/bash\nexit 0;";
    private static final String B64_SCRIPT_SAMPLE = Base64.getEncoder().encodeToString(SCRIPT_SAMPLE.getBytes());
    private Server server;
    private Server server2;
    private Package pkg;
    private Package channelPackage;
    private Errata errata;
    private Errata errata2;
    private ActionChain actionChain;
    private static final String UNAUTHORIZED_EXCEPTION_EXPECTED =
            "Expected an exception of type " +
                      InvalidSessionIdException.class.getCanonicalName();

    /**
     * {@inheritDoc}
     */
    @SuppressWarnings("deprecation")
    @Override
    @BeforeEach
    public void setUp() throws Exception {
        super.setUp();

        this.server = ServerFactoryTest.createTestServer(this.admin, true);
        this.server2 = ServerFactoryTest.createTestServer(this.admin, true);

        // Add capabilities
        SystemManagerTest.giveCapability(this.server.getId(), "script.run", 1L);
        SystemManagerTest.giveCapability(this.server.getId(),
                                         SystemManager.CAP_CONFIGFILES_DEPLOY, 2L);

        SystemManagerTest.giveCapability(this.server2.getId(), "script.run", 1L);
        SystemManagerTest.giveCapability(this.server2.getId(),
                                         SystemManager.CAP_CONFIGFILES_DEPLOY, 2L);

        // Channels
        this.pkg = PackageTest.createTestPackage(this.admin.getOrg());
        Channel channel = ChannelFactoryTest.createBaseChannel(this.admin);
        channel.addPackage(this.pkg);
        // Add package, available to the installation
        this.channelPackage = PackageTest.createTestPackage(this.admin.getOrg());
        channel.addPackage(this.channelPackage);
        this.server.addChannel(channel);
        this.server2.addChannel(channel);

        // Add errata available to the installation
        this.errata = ErrataFactoryTest.createTestErrata(this.admin.getOrg().getId());
        this.errata2 = ErrataFactoryTest.createTestErrata(this.admin.getOrg().getId());

        // Install one package on the servers
        InstalledPackage ipkg = new InstalledPackage();
        ipkg.setArch(this.pkg.getPackageArch());
        ipkg.setEvr(this.pkg.getPackageEvr());
        ipkg.setName(this.pkg.getPackageName());
        ipkg.setServer(this.server);
        Set<InstalledPackage> serverPkgs = server.getPackages();
        serverPkgs.add(ipkg);

        InstalledPackage ipkg2 = new InstalledPackage();
        ipkg2.setArch(this.pkg.getPackageArch());
        ipkg2.setEvr(this.pkg.getPackageEvr());
        ipkg2.setName(this.pkg.getPackageName());
        ipkg2.setServer(this.server);
        Set<InstalledPackage> serverPkgs2 = server2.getPackages();
        serverPkgs2.add(ipkg);

        ServerFactory.save(this.server);
        ServerFactory.save(this.server2);

        ErrataCacheManager.insertNeededErrataCache(
                this.server.getId(), this.errata.getId(), this.pkg.getId());

        ErrataCacheManager.insertNeededErrataCache(
                this.server2.getId(), this.errata2.getId(), this.pkg.getId());

        this.server = reload(this.server);
        this.server2 = reload(this.server2);

        ach = new ActionChainHandler();
        actionChain = ActionChainFactory.createActionChain(CHAIN_LABEL, admin);
    }

    /**
     * Test action chain create.
     */
    @Test
    public void testAcCreateActionChain() {
        String chainName = TestUtils.randomString();
        Integer chainId = this.ach.createChain(this.admin, chainName);
        ActionChain newActionChain = ActionChainFactory.getActionChain(admin, chainName);
        assertNotNull(newActionChain);
        assertEquals(newActionChain.getId().longValue(), chainId.longValue());
    }

    /**
     * Test creating an action chain failure on an empty chain name.
     */
    @Test
    public void testAcCreateActionChainFailureOnEmptyName() {
        try {
            this.ach.createChain(this.admin, "");
            fail("Expected exception: " +
                 InvalidParameterException.class.getCanonicalName());
        }
        catch (InvalidParameterException ex) {
            // expected
        }
    }

    /**
     * Test system reboot command schedule.
     */
    @Test
    public void testAcAddSystemReboot() {
        assertTrue(this.ach.addSystemReboot(this.admin,
                this.server.getId().intValue(),
                CHAIN_LABEL) > 0);

        assertEquals(1, actionChain.getEntries().size());
        assertEquals(ActionFactory.TYPE_REBOOT,
                     actionChain.getEntries().iterator().next()
                             .getAction().getActionType());
    }

    /**
     * Test Errata update command schedule.
     */
    @Test
    public void testAcAddErrataUpdate() {
        List<Integer> errataIds = new ArrayList<>();
        errataIds.add(this.errata.getId().intValue());
        errataIds.add(this.errata2.getId().intValue());
        assertTrue(this.ach.addErrataUpdate(this.admin,
                List.of(this.server.getId().intValue()),
                errataIds,
                CHAIN_LABEL) > 0);

        assertEquals(1, actionChain.getEntries().size());
        assertEquals(ActionFactory.TYPE_ERRATA,
                     actionChain.getEntries().iterator().next()
                             .getAction().getActionType());

        assertTrue(this.ach.addErrataUpdate(this.admin,
                Arrays.asList(this.server.getId().intValue(), this.server2.getId().intValue()),
                errataIds,
                CHAIN_LABEL) > 0);

        /* an action is created for each server, so after the next step
         * the action will be 3
         */
        assertEquals(3, actionChain.getEntries().size());
        assertEquals(ActionFactory.TYPE_ERRATA,
                actionChain.getEntries().iterator().next()
                            .getAction().getActionType());
    }

    /**
     * Test package installation schedule.
     */
    @Test
    public void testAcPackageInstallation() {
        List<Integer> packages = new ArrayList<>();
        packages.add(this.channelPackage.getId().intValue());
        assertTrue(this.ach.addPackageInstall(this.admin,
                this.server.getId().intValue(),
                packages,
                CHAIN_LABEL) > 0);
        assertEquals(1, actionChain.getEntries().size());
        assertEquals(ActionFactory.TYPE_PACKAGES_UPDATE,
                     actionChain.getEntries().iterator().next()
                             .getAction().getActionType());
    }

    /**
     * Test package installation schedule.
     */
    @Test
    public void testAcPackageInstallationFailed() {
        List<Integer> packages = new ArrayList<>();
        packages.add(0);
        try {
            this.ach.addPackageInstall(this.admin,
                                       this.server.getId().intValue(),
                                       packages,
                                       CHAIN_LABEL);
            fail("Expected exception: " + InvalidPackageException.class.getCanonicalName());
        }
        catch (InvalidPackageException ex) {
            assertTrue(actionChain.getEntries().isEmpty());
        }
    }

    /**
     * Test package removal.
     */
    @Test
    public void testAcPackageRemoval() {
        List<Integer> packagesToRemove = new ArrayList<>();
        packagesToRemove.add(this.pkg.getId().intValue());
        assertTrue(this.ach.addPackageRemoval(this.admin,
                this.server.getId().intValue(),
                packagesToRemove,
                CHAIN_LABEL) > 0);
        assertEquals(1, actionChain.getEntries().size());
        assertEquals(ActionFactory.TYPE_PACKAGES_REMOVE,
                     actionChain.getEntries().iterator().next()
                             .getAction().getActionType());
    }

    /**
     * Test package removal failure when empty list of packages is passed.
     */
    @Test
    public void testAcPackageRemovalFailureOnEmpty() {
        try {
            assertTrue(this.ach.addPackageRemoval(
                    this.admin, this.server.getId().intValue(),
                    new ArrayList<>(), CHAIN_LABEL) > 0);
            fail("Expected exception: " +
                 InvalidParameterException.class.getCanonicalName());
        }
        catch (InvalidParameterException ex) {
            assertEquals(0, actionChain.getEntries().size());
        }
    }

    /**
     * Test package removal failure when list of unknown packages is passed.
     */
    @Test
    public void testAcPackageRemovalFailureOnUnknownPackages() {
        List<Integer> packagesToRemove = new ArrayList<>();
        packagesToRemove.add(0);

        try {
            assertTrue(this.ach.addPackageRemoval(this.admin,
                    this.server.getId().intValue(),
                    packagesToRemove,
                    CHAIN_LABEL) > 0);
            fail("Expected exception: " + InvalidPackageException.class.getCanonicalName());
        }
        catch (InvalidPackageException ex) {
            assertEquals(0, actionChain.getEntries().size());
        }
    }

    /**
     * Test list chains.
     */
    @Test
    public void testAcListChains() {
        String[] labels = new String[]{
            TestUtils.randomString(),
            TestUtils.randomString(),
            TestUtils.randomString()
        };

        int previousChains = ActionChainFactory.getActionChains(this.admin).size();
        for (String label : labels) {
            ActionChainFactory.createActionChain(label, admin);
        }

        List<Map<String, Object>> chains = this.ach.listChains(this.admin);
        assertEquals(labels.length, chains.size() - previousChains);

        for (String label : labels) {
            ActionChain chain = ActionChainFactory.getActionChain(this.admin, label);
            assertEquals(0, chain.getEntries().size());
        }
    }

    /**
     * Test chain actions content.
     */
    @Test
    public void testAcChainActionsContent() {
        assertTrue(this.ach.addSystemReboot(this.admin,
                this.server.getId().intValue(),
                CHAIN_LABEL) > 0);

        for (Map<String, Object> action : this.ach.listChainActions(this.admin,
                                                                    CHAIN_LABEL)) {
            assertEquals("System reboot", action.get("label"));
            assertEquals("System reboot", action.get("type"));
            assertEquals(DateFormat.getDateTimeInstance(DateFormat.SHORT,
                                                        DateFormat.SHORT)
                                 .format((Date) action.get("created")),
                         DateFormat.getDateTimeInstance(DateFormat.SHORT,
                                                        DateFormat.SHORT)
                                 .format((Date) action.get("earliest")));
        }
    }

    /**
     * Test chains removal.
     */
    @Test
    public void testAcRemoveChain() {
        int previousChainCount = this.ach.listChains(this.admin).size();
        this.ach.deleteChain(this.admin, actionChain.getLabel());
        assertEquals(1, previousChainCount - this.ach.listChains(this.admin).size());
    }

    /**
     * Test chains removal failure when empty chain is passed.
     */
    @Test
    public void testAcRemoveChainsFailureOnEmpty() {
        int previousChainCount = this.ach.listChains(this.admin).size();
        try {
            this.ach.deleteChain(this.admin, "");
            fail("Expected exception: " +
                 NoSuchActionChainException.class.getCanonicalName());
        }
        catch (NoSuchActionChainException ex) {
            assertEquals(0, previousChainCount - this.ach.listChains(this.admin).size());
        }
    }

    /**
     * Test chains removal failure when unknown chain is passed.
     */
    @Test
    public void testAcRemoveChainsFailureOnUnknown() {
        int previousChainCount = this.ach.listChains(this.admin).size();
        try {
            this.ach.deleteChain(this.admin, TestUtils.randomString());
            fail("Expected exception: " +
                 NoSuchActionChainException.class.getCanonicalName());
        }
        catch (NoSuchActionChainException ex) {
            assertEquals(0, previousChainCount - this.ach.listChains(this.admin).size());
        }
    }

    /**
     * Test actions removal.
     */
    @Test
    public void testAcRemoveActions() {
        assertTrue(this.ach.addSystemReboot(this.admin,
                this.server.getId().intValue(),
                CHAIN_LABEL) > 0);
        assertFalse(this.ach.listChainActions(
                this.admin, CHAIN_LABEL).isEmpty());
        assertTrue(this.ach.removeAction(
                this.admin, CHAIN_LABEL,
                ((Long) ((Map) this.ach.listChainActions(this.admin, CHAIN_LABEL).get(0))
                        .get("id")).intValue()) > 0);
        assertTrue(this.ach.listChainActions(this.admin, CHAIN_LABEL).isEmpty());
    }

    /**
     * Test empty list does not remove any actions, schedule does not happening.
     */
    @Test
    public void testAcRemoveActionsEmpty() {
        assertTrue(this.ach.addSystemReboot(this.admin,
                this.server.getId().intValue(),
                CHAIN_LABEL) > 0);
        try {
            this.ach.removeAction(this.admin, CHAIN_LABEL, 0);
            fail("Expected exception: " +
                 NoSuchActionException.class.getCanonicalName());
        }
        catch (NoSuchActionException ex) {
            assertFalse(this.ach.listChainActions(this.admin, CHAIN_LABEL).isEmpty());
        }
    }

    /**
     * Test removal of the actions on the unknown chain.
     */
    @Test
    public void testAcRemoveActionsUnknownChain() {
        assertTrue(this.ach.addSystemReboot(this.admin,
                this.server.getId().intValue(),
                CHAIN_LABEL) > 0);
        try {
            this.ach.removeAction(this.admin, "", 0);
            fail("Expected exception: " +
                 NoSuchActionChainException.class.getCanonicalName());
        }
        catch (NoSuchActionChainException ex) {
            assertFalse(this.ach.listChainActions(
                    this.admin, CHAIN_LABEL).isEmpty());
        }
    }

    /**
     * Test unknown list of actions on certain chain does not remove anything
     * and schedule should not happen.
     */
    @Test
    public void testAcRemoveActionsUnknownChainActions() {
        assertTrue(this.ach.addSystemReboot(this.admin,
                this.server.getId().intValue(),
                CHAIN_LABEL) > 0);
        try {
            this.ach.removeAction(this.admin, CHAIN_LABEL, 0);
            fail("Expected exception: " + NoSuchActionException.class.getCanonicalName());
        }
        catch (NoSuchActionException ex) {
            assertFalse(this.ach.listChainActions(
                    this.admin, CHAIN_LABEL).isEmpty());
        }
    }

    /**
     * Test package upgrade.
     * @throws Exception if something bad happens
     */
    @Test
    public void testAcPackageUpgrade() throws Exception {
        Map<String, Object> info =
                ErrataCacheManagerTest.createServerNeededCache(this.admin,
                        ErrataFactory.ERRATA_TYPE_BUG);
        List<Integer> upgradePackages = new ArrayList<>();
        Server system = (Server) info.get("server");
        upgradePackages.add(this.pkg.getId().intValue());

        assertTrue(this.ach.addPackageUpgrade(this.admin,
                system.getId().intValue(),
                upgradePackages,
                CHAIN_LABEL) > 0);
        assertFalse(this.ach.listChains(this.admin).isEmpty());
        assertFalse(this.ach.listChainActions(this.admin, CHAIN_LABEL).isEmpty());

        assertEquals(1, actionChain.getEntries().size());
        assertEquals(ActionFactory.TYPE_PACKAGES_UPDATE, actionChain.getEntries()
                .iterator().next().getAction().getActionType());
    }

    /**
     * Test package upgrade with an empty list.
     */
    @Test
    public void testAcPackageUpgradeOnEmpty() {
        List<Integer> upgradePackages = new ArrayList<>();
        try {
            this.ach.addPackageUpgrade(this.admin,
                                       this.server.getId().intValue(),
                                       upgradePackages,
                                       CHAIN_LABEL);
            fail("Expected exception: " +
                 InvalidParameterException.class.getCanonicalName());
        }
        catch (InvalidParameterException ex) {
            assertEquals(0, actionChain.getEntries().size());
        }
    }

    /**
     * Test package upgrade with an empty list.
     */
    @Test
    public void testAcPackageUpgradeOnUnknown() {
        List<Integer> upgradePackages = new ArrayList<>();
        upgradePackages.add(0);
        try {
            this.ach.addPackageUpgrade(this.admin,
                                       this.server.getId().intValue(),
                                       upgradePackages,
                                       CHAIN_LABEL);
            fail("Expected exception: " + InvalidPackageException.class.getCanonicalName());
        }
        catch (InvalidPackageException ex) {
            assertTrue(actionChain.getEntries().isEmpty());
        }
    }

    /**
     * Test package verification.
     */
    @Test
    public void testAcPackageVerify() {
        DataResult<PackageListItem> packageListItems =
                PackageManager.systemPackageList(this.server.getId(), null);
        List<Integer> packages = new ArrayList<>();
        for (PackageListItem packageListItem : packageListItems) {
            packages.add(packageListItem.getPackageId().intValue());
        }

        assertTrue(this.ach.addPackageVerify(this.admin,
                this.server.getId().intValue(),
                packages,
                CHAIN_LABEL) > 0);
        assertEquals(1, actionChain.getEntries().size());
        assertEquals(ActionFactory.TYPE_PACKAGES_VERIFY, actionChain.getEntries()
                .iterator().next().getAction().getActionType());
    }

    /**
     * Test package verification failure when empty list is passed.
     */
    @Test
    public void testAcPackageVerifyFailureOnEmpty() {
        try {
            this.ach.addPackageVerify(this.admin,
                                      this.server.getId().intValue(),
                    new ArrayList<>(),
                                      CHAIN_LABEL);
            fail("Expected exception: " +
                 InvalidParameterException.class.getCanonicalName());
        }
        catch (InvalidParameterException ex) {
            assertEquals(0, actionChain.getEntries().size());
        }
    }

    /**
     * Test package verification failure when unknown package is verified.
     */
    @Test
    public void testAcPackageVerifyFailureOnUnknown() {
        List<Integer> packages = new ArrayList<>();
        packages.add(0);
        try {
            this.ach.addPackageVerify(this.admin,
                                      this.server.getId().intValue(),
                                      packages,
                                      CHAIN_LABEL);
            fail("Expected exception: " + InvalidPackageException.class.getCanonicalName());
        }
        catch (InvalidPackageException ex) {
            assertEquals(0, actionChain.getEntries().size());
        }
    }

    /**
     * Test schedule remote command.
     */
    @Test
    public void testAcRemoteCommand() {
        assertTrue(this.ach.addScriptRun(this.admin,
                this.server.getId().intValue(),
                CHAIN_LABEL,
                "root", "root", 300,
                ActionChainHandlerTest.B64_SCRIPT_SAMPLE) > 0);
        assertEquals(1, actionChain.getEntries().size());
        Action action = actionChain.getEntries().iterator().next().getAction();
        assertEquals(ActionFactory.TYPE_SCRIPT_RUN, action.getActionType());
        assertEquals(ActionChainHandlerTest.SCRIPT_SAMPLE,
                ((ScriptRunAction)action).getScriptActionDetails().getScriptContents());
    }

    @Test
    public void testAcLabeledRemoteCommand() {
        assertTrue(this.ach.addScriptRun(this.admin,
                this.server.getId().intValue(),
                CHAIN_LABEL, SCRIPT_LABEL,
                "root", "root", 300,
                ActionChainHandlerTest.B64_SCRIPT_SAMPLE) > 0);
        assertEquals(1, actionChain.getEntries().size());
        Action action = actionChain.getEntries().iterator().next().getAction();
        assertEquals(ActionFactory.TYPE_SCRIPT_RUN, action.getActionType());
        assertEquals(SCRIPT_LABEL, action.getName());
        assertEquals(ActionChainHandlerTest.SCRIPT_SAMPLE,
                ((ScriptRunAction)action).getScriptActionDetails().getScriptContents());
    }

    @Test
    public void testAcRemoteCommandNoBase64() {
        try {
            assertFalse(this.ach.addScriptRun(this.admin,
                            this.server.getId().intValue(),
                            CHAIN_LABEL,
                            "root", "root", 300,
                            ActionChainHandlerTest.SCRIPT_SAMPLE) > 0,
                    "Exception expected and no success");
        }
        catch (IllegalArgumentException e) {
            assertContains(e.getMessage(), "Illegal base64 character");
        }
        catch (Exception e) {
            fail("Wrong exception thrown");
        }
    }

    /**
     * Test schedule on precise time.
     */
    @Test
    public void testAcScheduleOnTime() {
        assertEquals(Integer.valueOf(1),
                     this.ach.scheduleChain(this.admin, CHAIN_LABEL, new Date()));
    }

    /**
     * Test schedule on precise time.
     */
    @Test
    public void testAcScheduleOnTimeFailureNoChain() {
        try {
            this.ach.scheduleChain(this.admin, "", new Date());
            fail("Expected exception: " +
                 NoSuchActionChainException.class.getCanonicalName());
        }
        catch (NoSuchActionChainException ex) {
            //expected
        }
    }

    /**
     * Deploy configuration.
     */
    @Test
    public void testAcDeployConfiguration() {
        ConfigRevision configRevision = ConfigTestUtils.createConfigRevision(
                this.admin.getOrg());
        Map<String, Object> revisionSpecifier = new HashMap<>();
        ConfigFile configFile = configRevision.getConfigFile();
        revisionSpecifier.put("channelLabel", configFile.getConfigChannel().getLabel());
        revisionSpecifier.put("filePath", configFile.getConfigFileName().getPath());
        revisionSpecifier.put("revision", configRevision.getRevision().intValue());

        assertEquals(Integer.valueOf(BaseHandler.VALID),
                     this.ach.addConfigurationDeployment(this.admin,
                             CHAIN_LABEL,
                             this.server.getId().intValue(),
                             Collections.singletonList(revisionSpecifier)));

        Set<ActionChainEntry> entries =
                ActionChainFactory.getActionChain(this.admin, CHAIN_LABEL).getEntries();
        assertEquals(1, entries.size());
        assertEquals(ActionFactory.TYPE_CONFIGFILES_DEPLOY,
                entries.iterator().next().getAction().getActionType());

    }

    /**
     * Deploy configuration should fail if no chain label has been passed.
     */
    @Test
    public void testAcDeployConfigurationFailureNoChain() {
        ConfigRevision configRevision = ConfigTestUtils.createConfigRevision(
                this.admin.getOrg());
        Map<String, Object> revisionSpecifier = new HashMap<>();
        ConfigFile configFile = configRevision.getConfigFile();
        revisionSpecifier.put("channelLabel", configFile.getConfigChannel().getLabel());
        revisionSpecifier.put("filePath", configFile.getConfigFileName().getPath());
        revisionSpecifier.put("revision", configRevision.getRevision().intValue());

        try {
            this.ach.addConfigurationDeployment(this.admin, "",
                                                this.server.getId().intValue(),
                                                Collections.singletonList(revisionSpecifier)
            );
            fail("Expected exception: " +
                 NoSuchActionChainException.class.getCanonicalName());
        }
        catch (NoSuchActionChainException ex) {
            //expected
        }
    }

    /**
     * Rename an action chain.
     */
    @Test
    public void testAcRenameActionChain() {
        assertTrue(actionChain.getLabel().equals(CHAIN_LABEL));
        assertEquals(Integer.valueOf(1),
                     this.ach.renameChain(
                             this.admin, CHAIN_LABEL, TestUtils.randomString()));
        assertFalse(actionChain.getLabel().equals(CHAIN_LABEL));
    }

    /**
     * Rename an action chain should fail when renaming to the same label.
     */
    @Test
    public void testAcRenameActionChainFailureOnSameLabel() {
        assertTrue(actionChain.getLabel().equals(CHAIN_LABEL));
        try {
            assertEquals(Integer.valueOf(1),
                         this.ach.renameChain(this.admin, CHAIN_LABEL, CHAIN_LABEL));
            fail("Expected exception: " +
                 InvalidParameterException.class.getCanonicalName());
        }
        catch (InvalidParameterException ex) {
            assertTrue(actionChain.getLabel().equals(CHAIN_LABEL));
        }
    }

    /**
     * Rename an action chain should fail when previous label is missing.
     */
    @Test
    public void testAcRenameActionChainFailureOnEmptyPreviousLabel() {
        assertTrue(actionChain.getLabel().equals(CHAIN_LABEL));
        try {
            assertEquals(Integer.valueOf(1),
                         this.ach.renameChain(this.admin, "", CHAIN_LABEL));
            fail("Expected exception: " +
                 InvalidParameterException.class.getCanonicalName());
        }
        catch (InvalidParameterException ex) {
            assertTrue(actionChain.getLabel().equals(CHAIN_LABEL));
        }
    }

    /**
     * Rename an action chain should fail when new label is missing.
     */
    @Test
    public void testAcRenameActionChainFailureOnEmptyNewLabel() {
        assertTrue(actionChain.getLabel().equals(CHAIN_LABEL));
        try {
            assertEquals(Integer.valueOf(1),
                         this.ach.renameChain(this.admin, CHAIN_LABEL, ""));
            fail("Expected exception: " +
                 InvalidParameterException.class.getCanonicalName());
        }
        catch (InvalidParameterException ex) {
            assertTrue(actionChain.getLabel().equals(CHAIN_LABEL));
        }
    }
}
