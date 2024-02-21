/*
 * Copyright (c) 2009--2014 Red Hat, Inc.
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
package com.redhat.rhn.frontend.action.systems.entitlements.test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import com.redhat.rhn.common.conf.Config;
import com.redhat.rhn.common.conf.ConfigDefaults;
import com.redhat.rhn.common.db.datasource.DataResult;
import com.redhat.rhn.domain.channel.Channel;
import com.redhat.rhn.domain.org.OrgFactory;
import com.redhat.rhn.domain.server.EntitlementServerGroup;
import com.redhat.rhn.domain.server.Server;
import com.redhat.rhn.domain.server.ServerConstants;
import com.redhat.rhn.domain.server.ServerFactory;
import com.redhat.rhn.domain.server.test.MinionServerFactoryTest;
import com.redhat.rhn.domain.server.test.ServerFactoryTest;
import com.redhat.rhn.domain.user.UserFactory;
import com.redhat.rhn.frontend.action.systems.entitlements.SystemEntitlementsSetupAction;
import com.redhat.rhn.frontend.struts.RequestContext;
import com.redhat.rhn.manager.entitlement.EntitlementManager;
import com.redhat.rhn.manager.formula.FormulaMonitoringManager;
import com.redhat.rhn.manager.system.ServerGroupManager;
import com.redhat.rhn.manager.system.SystemManager;
import com.redhat.rhn.manager.system.entitling.SystemEntitlementManager;
import com.redhat.rhn.manager.system.entitling.SystemEntitler;
import com.redhat.rhn.manager.system.entitling.SystemUnentitler;
import com.redhat.rhn.testing.ChannelTestUtils;
import com.redhat.rhn.testing.RhnMockStrutsTestCase;
import com.redhat.rhn.testing.ServerTestUtils;
import com.redhat.rhn.testing.UserTestUtils;

import com.suse.manager.virtualization.VirtManagerSalt;
import com.suse.manager.webui.services.iface.MonitoringManager;
import com.suse.manager.webui.services.iface.VirtManager;
import com.suse.manager.webui.services.impl.SaltSSHService;
import com.suse.manager.webui.services.impl.SaltService;
import com.suse.salt.netapi.datatypes.target.MinionList;

import org.jmock.Expectations;
import org.jmock.Mockery;
import org.jmock.imposters.ByteBuddyClassImposteriser;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.Map;

/**
 * SystemEntitlementsSetupActionTest
 */
public class SystemEntitlementsSetupActionTest extends RhnMockStrutsTestCase {
    /**
     * {@inheritDoc}
     */

    private Mockery context = new Mockery();
    private SaltService saltServiceMock;
    private SystemEntitlementManager systemEntitlementManager;

    @Override
    @BeforeEach
    public void setUp() throws Exception {
        super.setUp();
        Config.get().setBoolean(ConfigDefaults.KIWI_OS_IMAGE_BUILDING_ENABLED, "true");
        context.setImposteriser(ByteBuddyClassImposteriser.INSTANCE);
        saltServiceMock = context.mock(SaltService.class);
        ServerGroupManager serverGroupManager = new ServerGroupManager(saltServiceMock);
        VirtManager virtManager = new VirtManagerSalt(saltServiceMock);
        MonitoringManager monitoringManager = new FormulaMonitoringManager(saltServiceMock);
        systemEntitlementManager = new SystemEntitlementManager(
                new SystemUnentitler(virtManager, monitoringManager, serverGroupManager),
                new SystemEntitler(saltServiceMock, virtManager, monitoringManager, serverGroupManager)
        );
        context.checking(new Expectations() {{
            allowing(saltServiceMock).refreshPillar(with(any(MinionList.class)));
        }});
        setRequestPathInfo("/systems/SystemEntitlements");
        UserTestUtils.addManagement(user.getOrg());
        UserTestUtils.addVirtualization(user.getOrg());
    }
    /**
     *
     * @throws Exception exception if test fails
     */
    @Test
    public void testUpdateEntitledUser() throws Exception {
        ServerFactoryTest.createTestServer(user);
        executeTests();

        assertNull(request.getAttribute(SystemEntitlementsSetupAction.SHOW_NO_SYSTEMS));
        assertNotNull(request.getAttribute(SystemEntitlementsSetupAction.SHOW_COMMANDS));

        Map baseEntitlementCounts = (Map) request
                .getAttribute(SystemEntitlementsSetupAction.BASE_ENTITLEMENT_COUNTS);

        assertEquals("1 system(s).",
                baseEntitlementCounts.get(EntitlementManager.MANAGEMENT.getLabel()));
    }

    @Test
    public void testVirtualizationType() throws Exception {
        Server server = ServerTestUtils.createTestSystem(user,
                ServerConstants.getServerGroupTypeEnterpriseEntitled());

        Channel[] ch = ChannelTestUtils.setupBaseChannelForVirtualization(user, server.getBaseChannel());
        server.addChannel(ch[0]);
        server.addChannel(ch[1]);

        assertTrue(EntitlementManager.VIRTUALIZATION.isAllowedOnServer(server));
        boolean hasErrors =
                systemEntitlementManager.addEntitlementToServer(server, EntitlementManager.VIRTUALIZATION).hasErrors();

        assertFalse(hasErrors);
        assertTrue(SystemManager.hasEntitlement(server.getId(), EntitlementManager.VIRTUALIZATION));

        executeTests();
        assertNotNull(request.getAttribute(
                SystemEntitlementsSetupAction.ADDON_ENTITLEMENTS));

        Map<String, String> addonEntitlementCounts = (Map<String, String>) request
                .getAttribute(SystemEntitlementsSetupAction.ADDON_ENTITLEMENT_COUNTS);

        assertEquals("1 system(s).",
                addonEntitlementCounts.get(EntitlementManager.VIRTUALIZATION.getLabel()));
    }

    @Test
    public void testContainerBuildHostType() throws Exception {
        Server server = MinionServerFactoryTest.createTestMinionServer(user);

        assertTrue(EntitlementManager.CONTAINER_BUILD_HOST.isAllowedOnServer(server));
        boolean hasErrors = systemEntitlementManager
                .addEntitlementToServer(server, EntitlementManager.CONTAINER_BUILD_HOST).hasErrors();

        assertFalse(hasErrors);
        assertTrue(SystemManager.hasEntitlement(server.getId(), EntitlementManager.CONTAINER_BUILD_HOST));

        executeTests();
        assertNotNull(request.getAttribute(
                SystemEntitlementsSetupAction.ADDON_ENTITLEMENTS));

        Map<String, String> addonEntitlementCounts = (Map<String, String>) request
                .getAttribute(SystemEntitlementsSetupAction.ADDON_ENTITLEMENT_COUNTS);

        assertEquals("1 system(s).",
                addonEntitlementCounts.get(EntitlementManager.CONTAINER_BUILD_HOST.getLabel()));
    }

    @Test
    public void testOSImageBuildHostType() throws Exception {
        context.checking(new Expectations() {{
            allowing(saltServiceMock).generateSSHKey(
                    with(equal(SaltSSHService.SSH_KEY_PATH)), with(equal(SaltSSHService.SUMA_SSH_PUB_KEY)));
        }});

        Server server = MinionServerFactoryTest.createTestMinionServer(user);
        // OS Image building is SUSE only
        server.setServerArch(ServerFactory.lookupServerArchByLabel("x86_64-redhat-linux"));
        server.setOs("SLES");
        server.setRelease("15.1");
        ServerFactory.save(server);

        assertTrue(EntitlementManager.OSIMAGE_BUILD_HOST.isAllowedOnServer(server));

        boolean hasErrors = systemEntitlementManager
                .addEntitlementToServer(server, EntitlementManager.OSIMAGE_BUILD_HOST).hasErrors();

        assertFalse(hasErrors);
        assertTrue(SystemManager.hasEntitlement(server.getId(), EntitlementManager.OSIMAGE_BUILD_HOST));

        executeTests();
        assertNotNull(request.getAttribute(
                SystemEntitlementsSetupAction.ADDON_ENTITLEMENTS));

        Map<String, String> addonEntitlementCounts = (Map<String, String>) request
                .getAttribute(SystemEntitlementsSetupAction.ADDON_ENTITLEMENT_COUNTS);
        assertEquals("1 system(s).",
                addonEntitlementCounts.get(EntitlementManager.OSIMAGE_BUILD_HOST.getLabel()));
        context.assertIsSatisfied();
    }

    /**
     *
     */
    @Test
    public void testManagementEntitledUser() {
        Server server = ServerFactoryTest.createTestServer(user, true,
                        ServerConstants.getServerGroupTypeEnterpriseEntitled());

        UserFactory.save(user);
        OrgFactory.save(user.getOrg());


        executeTests();
        assertNotNull(request.getAttribute(
                            SystemEntitlementsSetupAction.ADDON_ENTITLEMENTS));
    }

    /**
     *
     *
     */
    @Test
    public void testNoEntitlements() {
        actionPerform();
        DataResult dr = (DataResult) request.getAttribute(RequestContext.PAGE_LIST);
        assertTrue(dr.isEmpty());
        assertNull(request.getAttribute(SystemEntitlementsSetupAction.SHOW_COMMANDS));
        assertNotNull(request.getAttribute(SystemEntitlementsSetupAction.SHOW_NO_SYSTEMS));
    }

    private void executeTests() {
        actionPerform();
        DataResult dr = (DataResult) request.getAttribute(RequestContext.PAGE_LIST);
        assertNotNull(dr);
        assertFalse(dr.isEmpty());
    }

    /**
     *
     */
    @Test
    public void testEntitlementCountMessage() {
        Server server = ServerFactoryTest.createTestServer(user, true,
                        ServerConstants.getServerGroupTypeEnterpriseEntitled());
        assertFalse(server.getEntitlements().isEmpty());

        EntitlementServerGroup eGrp = null;
        for (EntitlementServerGroup sg : server.getEntitledGroups()) {
            if (sg.getGroupType().equals(
                    ServerConstants.getServerGroupTypeEnterpriseEntitled())) {
                eGrp = sg;
                break;
            }
        }

        executeTests();

        Map<String, String> baseEntitlementCounts = (Map<String, String>) request
                .getAttribute(SystemEntitlementsSetupAction.BASE_ENTITLEMENT_COUNTS);

        String message =
                baseEntitlementCounts.get(EntitlementManager.MANAGEMENT.getLabel());

        assertTrue(message.contains(String.valueOf(eGrp.getCurrentMembers())));
    }
}
