/*
 * Copyright (c) 2022 SUSE LLC
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

package com.redhat.rhn.frontend.xmlrpc.system.test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import com.redhat.rhn.domain.action.Action;
import com.redhat.rhn.domain.action.ActionFactory;
import com.redhat.rhn.domain.action.rhnpackage.PackageAction;
import com.redhat.rhn.domain.channel.Channel;
import com.redhat.rhn.domain.channel.ChannelFactory;
import com.redhat.rhn.domain.channel.test.ChannelFactoryTest;
import com.redhat.rhn.domain.rhnpackage.Package;
import com.redhat.rhn.domain.rhnpackage.test.PackageTest;
import com.redhat.rhn.domain.server.Server;
import com.redhat.rhn.domain.server.ServerFactory;
import com.redhat.rhn.domain.server.ServerGroupFactory;
import com.redhat.rhn.domain.server.test.ServerFactoryTest;
import com.redhat.rhn.frontend.xmlrpc.PtfMasterFault;
import com.redhat.rhn.frontend.xmlrpc.PtfPackageFault;
import com.redhat.rhn.frontend.xmlrpc.system.SystemHandler;
import com.redhat.rhn.frontend.xmlrpc.system.XmlRpcSystemHelper;
import com.redhat.rhn.frontend.xmlrpc.test.BaseHandlerTestCase;
import com.redhat.rhn.manager.formula.FormulaMonitoringManager;
import com.redhat.rhn.manager.system.ServerGroupManager;
import com.redhat.rhn.manager.system.SystemManager;
import com.redhat.rhn.manager.system.entitling.SystemEntitlementManager;
import com.redhat.rhn.manager.system.entitling.SystemEntitler;
import com.redhat.rhn.manager.system.entitling.SystemUnentitler;
import com.redhat.rhn.taskomatic.TaskomaticApi;
import com.redhat.rhn.testing.PackageTestUtils;

import com.suse.cloud.CloudPaygManager;
import com.suse.cloud.test.TestCloudPaygManagerBuilder;
import com.suse.manager.attestation.AttestationManager;
import com.suse.manager.virtualization.VirtManagerSalt;
import com.suse.manager.webui.controllers.bootstrap.RegularMinionBootstrapper;
import com.suse.manager.webui.controllers.bootstrap.SSHMinionBootstrapper;
import com.suse.manager.webui.services.iface.MonitoringManager;
import com.suse.manager.webui.services.iface.SaltApi;
import com.suse.manager.webui.services.iface.SystemQuery;
import com.suse.manager.webui.services.iface.VirtManager;
import com.suse.manager.webui.services.test.TestSaltApi;
import com.suse.manager.webui.services.test.TestSystemQuery;

import org.jmock.imposters.ByteBuddyClassImposteriser;
import org.jmock.junit5.JUnit5Mockery;
import org.jmock.lib.concurrent.Synchroniser;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.RegisterExtension;

import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * Test for the {@link SystemHandler} to verify the handling of PTF packages.
 */
public class SystemHandlerPtfTest extends BaseHandlerTestCase {

    private SystemHandler handler;

    @RegisterExtension
    protected JUnit5Mockery mockContext;

    private Package standard;
    private Package standardUpdated;

    private Package standardUpdatedPtf;
    private Package ptfMaster;
    private Package ptfMasterUpdated;
    private Package ptfPackage;
    private Package ptfPackageUpdated;
    private Server server;
    private Channel channel;

    public SystemHandlerPtfTest() {
        mockContext = new JUnit5Mockery();
        mockContext.setThreadingPolicy(new Synchroniser());
        mockContext.setImposteriser(ByteBuddyClassImposteriser.INSTANCE);
    }

    @Override
    @BeforeEach
    public void setUp() throws Exception {
        super.setUp();

        TaskomaticApi taskomaticApi = new TaskomaticApi();
        SystemQuery systemQuery = new TestSystemQuery();
        SaltApi saltApi = new TestSaltApi();
        CloudPaygManager paygMgr = new TestCloudPaygManagerBuilder().build();
        AttestationManager attMgr = new AttestationManager();

        RegularMinionBootstrapper regularBootstrapper =
                new RegularMinionBootstrapper(systemQuery, saltApi, paygMgr, attMgr);
        SSHMinionBootstrapper sshBootstrapper = new SSHMinionBootstrapper(systemQuery, saltApi, paygMgr, attMgr);
        XmlRpcSystemHelper xmlRpcHelper = new XmlRpcSystemHelper(regularBootstrapper, sshBootstrapper);

        ServerGroupManager groupManager = new ServerGroupManager(saltApi);
        VirtManager virtManager = new VirtManagerSalt(saltApi);
        MonitoringManager monitoringManager = new FormulaMonitoringManager(saltApi);

        SystemUnentitler unentitler = new SystemUnentitler(virtManager, monitoringManager, groupManager);
        SystemEntitler entitler = new SystemEntitler(saltApi, virtManager, monitoringManager, groupManager);

        SystemEntitlementManager entitlementManager = new SystemEntitlementManager(unentitler, entitler);
        SystemManager systemManager = new SystemManager(ServerFactory.SINGLETON, ServerGroupFactory.SINGLETON, saltApi);

        handler = new SystemHandler(taskomaticApi, xmlRpcHelper, entitlementManager, systemManager, groupManager,
            new TestCloudPaygManagerBuilder().build(), new AttestationManager());

        standard = PackageTest.createTestPackage(admin.getOrg());
        standardUpdated = PackageTestUtils.newVersionOfPackage(standard, null, "2.0.0", null, admin.getOrg());
        standardUpdatedPtf = PackageTestUtils.createPtfPackage(standardUpdated, "123456", "1", admin.getOrg());
        ptfMaster = PackageTestUtils.createPtfMaster("123456", "1", admin.getOrg());
        ptfMasterUpdated = PackageTestUtils.newVersionOfPackage(ptfMaster, null, "2", null, admin.getOrg());
        ptfPackage = PackageTestUtils.createPtfPackage("123456", "1", admin.getOrg());
        ptfPackageUpdated = PackageTestUtils.createPtfPackage("123456", "2", admin.getOrg());

        server = ServerFactoryTest.createTestServer(admin, true);
        channel = ChannelFactoryTest.createTestChannel(admin);

        SystemManager.subscribeServerToChannel(admin, server, channel);
    }

    @Test
    public void allInstallablePackagesDoesNotListPtfsPackages() {
        channel.getPackages()
               .addAll(List.of(standard, standardUpdated, ptfMaster, ptfMasterUpdated, ptfPackage, ptfPackageUpdated));

        List<Map<String, Object>> resultMap = handler.listAllInstallablePackages(admin, server.getId().intValue());

        // Only standard and ptf master packages should be listed
        assertEquals(4, resultMap.size());

        assertEquals(
            Set.of(standard.getId(), standardUpdated.getId(), ptfMaster.getId(), ptfMasterUpdated.getId()),
            resultMap.stream().map(map -> (Long) map.get("id")).collect(Collectors.toSet())
        );
    }

    @Test
    public void latestInstallablePackagesDoesNotListPtfsPackages() {
        channel.getPackages()
               .addAll(List.of(standard, standardUpdated, ptfMaster, ptfMasterUpdated, ptfPackage, ptfPackageUpdated));
        ChannelFactory.refreshNewestPackageCache(channel, "java::test");

        List<Map<String, Object>> resultMap = handler.listLatestInstallablePackages(admin, server.getId().intValue());

        // Only standard and ptf master packages should be listed
        assertEquals(2, resultMap.size());

        Set<Long> actualIds = resultMap.stream().map(map -> (Long) map.get("id")).collect(Collectors.toSet());
        Set<Long> expectedIds = Set.of(standardUpdated.getId(), ptfMasterUpdated.getId());
        assertEquals(expectedIds, actualIds);
    }

    @Test
    public void listLatestAvailablePackageDoesNotConsiderPtfPackages() {
        channel.getPackages().addAll(List.of(standard, standardUpdated, standardUpdatedPtf));
        PackageTestUtils.installPackageOnServer(standard, server);

        List<Map<String, Object>> results = handler.listLatestAvailablePackage(admin,
            List.of(server.getId().intValue()), standard.getPackageName().getName());

        assertEquals(1, results.size());

        @SuppressWarnings("unchecked")
        Map<String, Object> packageInfoMap = (Map<String, Object>) results.get(0).get("package");
        assertNotNull(packageInfoMap);
        assertEquals(standardUpdated.getId(), packageInfoMap.get("id"));
    }

    @Test
    public void installingPtfPackageFailsWithPtfPackageFault() {
        channel.getPackages().add(ptfPackage);

        assertThrows(PtfPackageFault.class, () -> {
            handler.schedulePackageInstall(admin, List.of(server.getId().intValue()),
                List.of(ptfPackage.getId().intValue()), new Date());
        });
    }

    @Test
    public void upgradingToPtfPackageFailsWithPtfPackageFault() {
        channel.getPackages().addAll(List.of(standard, standardUpdated));
        PackageTestUtils.installPackageOnServer(standard, server);

        assertThrows(PtfPackageFault.class, () -> {
            handler.schedulePackageInstall(admin, List.of(server.getId().intValue()),
                List.of(standardUpdatedPtf.getId().intValue()), new Date());
        });
    }

    @Test
    public void removingPtfPackageFailsWithPtfPackageFault() {
        channel.getPackages().add(ptfPackage);
        PackageTestUtils.installPackageOnServer(ptfPackage, server);

        assertThrows(PtfPackageFault.class, () -> {
            handler.schedulePackageRemove(admin, List.of(server.getId().intValue()),
                List.of(ptfPackage.getId().intValue()), new Date());
        });
    }

    @Test
    public void masterPtfPackagesCanBeInstalled() {
        channel.getPackages().add(ptfMaster);

        Long[] scheduledActions = handler.schedulePackageInstall(admin, List.of(server.getId().intValue()),
            List.of(ptfMaster.getId().intValue()), new Date());

        assertNotNull(scheduledActions);
        assertEquals(1, scheduledActions.length);

        Action action = ActionFactory.lookupByUserAndId(admin, scheduledActions[0]);
        assertNotNull(action);
        assertTrue(action instanceof PackageAction);
    }

    @Test
    public void removingMasterPtfFailsWithPtfMasterFault() {
        channel.getPackages().add(ptfMaster);
        PackageTestUtils.installPackageOnServer(ptfMaster, server);

        assertThrows(PtfMasterFault.class, () -> {
            handler.schedulePackageRemove(admin, List.of(server.getId().intValue()),
                List.of(ptfMaster.getId().intValue()), new Date());
        });

    }

}
