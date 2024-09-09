/*
 * Copyright (c) 2009--2017 Red Hat, Inc.
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
package com.redhat.rhn.manager.system.entitling.test;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import com.redhat.rhn.common.conf.ConfigDefaults;
import com.redhat.rhn.common.hibernate.HibernateFactory;
import com.redhat.rhn.common.validator.ValidatorResult;
import com.redhat.rhn.domain.channel.Channel;
import com.redhat.rhn.domain.role.RoleFactory;
import com.redhat.rhn.domain.server.Server;
import com.redhat.rhn.domain.server.ServerConstants;
import com.redhat.rhn.domain.server.ServerFactory;
import com.redhat.rhn.domain.server.test.MinionServerFactoryTest;
import com.redhat.rhn.domain.user.User;
import com.redhat.rhn.manager.entitlement.EntitlementManager;
import com.redhat.rhn.manager.formula.FormulaMonitoringManager;
import com.redhat.rhn.manager.system.ServerGroupManager;
import com.redhat.rhn.manager.system.entitling.SystemEntitlementManager;
import com.redhat.rhn.manager.system.entitling.SystemEntitler;
import com.redhat.rhn.manager.system.entitling.SystemUnentitler;
import com.redhat.rhn.testing.ChannelTestUtils;
import com.redhat.rhn.testing.JMockBaseTestCaseWithUser;
import com.redhat.rhn.testing.ServerTestUtils;
import com.redhat.rhn.testing.TestUtils;
import com.redhat.rhn.testing.UserTestUtils;

import com.suse.manager.virtualization.VirtManagerSalt;
import com.suse.manager.webui.services.iface.MonitoringManager;
import com.suse.manager.webui.services.iface.VirtManager;
import com.suse.manager.webui.services.impl.SaltSSHService;
import com.suse.manager.webui.services.impl.SaltService;
import com.suse.salt.netapi.calls.LocalCall;
import com.suse.salt.netapi.datatypes.target.MinionList;

import org.jmock.Expectations;
import org.jmock.imposters.ByteBuddyClassImposteriser;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

public class SystemEntitlementManagerTest extends JMockBaseTestCaseWithUser {

    private SaltService saltServiceMock;
    private SystemEntitlementManager systemEntitlementManager;

    @Override
    @BeforeEach
    public void setUp() throws Exception {
        super.setUp();
        setImposteriser(ByteBuddyClassImposteriser.INSTANCE);
        saltServiceMock = mock(SaltService.class);
        ServerGroupManager serverGroupManager = new ServerGroupManager(saltServiceMock);
        VirtManager virtManager = new VirtManagerSalt(saltServiceMock);
        MonitoringManager monitoringManager = new FormulaMonitoringManager(saltServiceMock);
        systemEntitlementManager = new SystemEntitlementManager(
                new SystemUnentitler(virtManager, monitoringManager, serverGroupManager),
                new SystemEntitler(saltServiceMock, virtManager, monitoringManager, serverGroupManager)
        );
        context().checking(new Expectations() {{
            allowing(saltServiceMock).refreshPillar(with(any(MinionList.class)));
            allowing(saltServiceMock).callSync(with(any(LocalCall.class)), with(any(String.class)));
        }});
    }

    /**
     * Tests adding and removing entitlement on a server
     * @throws Exception if something goes wrong
     */
    @Test
    public void testEntitleServer() throws Exception {
        User user = UserTestUtils.findNewUser("testUser",
                "testOrg" + this.getClass().getSimpleName());
        user.addPermanentRole(RoleFactory.ORG_ADMIN);
        Server server = ServerTestUtils.createTestSystem(user, ServerConstants.getServerGroupTypeEnterpriseEntitled());
        ChannelTestUtils.setupBaseChannelForVirtualization(user,
                server.getBaseChannel());
        UserTestUtils.addVirtualization(user.getOrg());
        TestUtils.saveAndFlush(user.getOrg());

        //Test Virtualization Host
        assertTrue(systemEntitlementManager.canEntitleServer(server, EntitlementManager.VIRTUALIZATION));
        boolean hasErrors =
                systemEntitlementManager.addEntitlementToServer(server, EntitlementManager.VIRTUALIZATION).hasErrors();
        assertFalse(hasErrors);
        assertTrue(server.hasEntitlement(EntitlementManager.VIRTUALIZATION));

        // Removal
        systemEntitlementManager.removeServerEntitlement(server, EntitlementManager.VIRTUALIZATION);
        server = reload(server);
        assertFalse(server.hasEntitlement(EntitlementManager.VIRTUALIZATION));

        //Test Container Build Host
        Server minion = MinionServerFactoryTest.createTestMinionServer(user);
        assertTrue(systemEntitlementManager.canEntitleServer(minion, EntitlementManager.CONTAINER_BUILD_HOST));
        hasErrors = systemEntitlementManager.addEntitlementToServer(minion, EntitlementManager.CONTAINER_BUILD_HOST)
                .hasErrors();
        assertFalse(hasErrors);
        assertTrue(minion.hasEntitlement(EntitlementManager.CONTAINER_BUILD_HOST));

        // Removal
        systemEntitlementManager.removeServerEntitlement(minion, EntitlementManager.CONTAINER_BUILD_HOST);
        minion = reload(minion);
        assertFalse(minion.hasEntitlement(EntitlementManager.CONTAINER_BUILD_HOST));

        //Test OS Image Build Host

        context().checking(new Expectations() {{
            allowing(saltServiceMock).generateSSHKey(with(equal(SaltSSHService.SSH_KEY_PATH)),
                    with(equal(SaltSSHService.SUMA_SSH_PUB_KEY)));
        }});

        minion.setServerArch(ServerFactory.lookupServerArchByLabel("x86_64-redhat-linux"));
        ServerFactory.save(minion);
        assertTrue(systemEntitlementManager.canEntitleServer(minion, EntitlementManager.OSIMAGE_BUILD_HOST));
        hasErrors = systemEntitlementManager.addEntitlementToServer(minion, EntitlementManager.OSIMAGE_BUILD_HOST)
                .hasErrors();
        assertFalse(hasErrors);
        assertTrue(minion.hasEntitlement(EntitlementManager.OSIMAGE_BUILD_HOST));

        // Removal
        systemEntitlementManager.removeServerEntitlement(minion, EntitlementManager.OSIMAGE_BUILD_HOST);
        minion = reload(minion);
        assertFalse(minion.hasEntitlement(EntitlementManager.OSIMAGE_BUILD_HOST));
    }

    @Test
    public void testEntitleVirtForGuest() throws Exception {
        Server host = ServerTestUtils.createVirtHostWithGuest(systemEntitlementManager);
        User user = host.getCreator();
        UserTestUtils.addVirtualization(user.getOrg());

        Server guest =
            (host.getGuests().iterator().next()).getGuestSystem();
        guest.addChannel(ChannelTestUtils.createBaseChannel(user));
        ServerTestUtils.addVirtualization(user, guest);

        assertTrue(
                systemEntitlementManager.addEntitlementToServer(guest, EntitlementManager.VIRTUALIZATION).hasErrors());
        assertFalse(guest.hasEntitlement(EntitlementManager.VIRTUALIZATION));
    }

    @Test
    public void testVirtualEntitleServer() throws Exception {
        // User and server
        User user = UserTestUtils.findNewUser("testUser",
                "testOrg" + this.getClass().getSimpleName());
        user.addPermanentRole(RoleFactory.ORG_ADMIN);
        Server server = ServerTestUtils.createTestSystem(user, ServerConstants.getServerGroupTypeEnterpriseEntitled());
        Channel[] children = ChannelTestUtils.setupBaseChannelForVirtualization(user,
                server.getBaseChannel());

        Channel rhnTools = children[0];
        Channel rhelVirt = children[1];

        // Entitlements
        UserTestUtils.addVirtualization(user.getOrg());
        TestUtils.saveAndFlush(user.getOrg());

        assertTrue(systemEntitlementManager.canEntitleServer(server, EntitlementManager.VIRTUALIZATION));

        ValidatorResult retval =
                systemEntitlementManager.addEntitlementToServer(server, EntitlementManager.VIRTUALIZATION);

        server = reload(server);

        String key = null;
        if (!retval.getErrors().isEmpty()) {
            key = retval.getErrors().get(0).getKey();
        }
        assertFalse(retval.hasErrors(), "Got back: " + key);

        // Test stuff!
        assertTrue(server.hasEntitlement(EntitlementManager.VIRTUALIZATION));
        if (!ConfigDefaults.get().isSpacewalk()) {
            // this is actually Satellite-specific
            // assertTrue(server.getChannels().contains(rhelVirt));
        }


        // Test removal
        systemEntitlementManager.removeServerEntitlement(server, EntitlementManager.VIRTUALIZATION);

        server = reload(server);
        assertFalse(server.hasEntitlement(EntitlementManager.VIRTUALIZATION));

    }

    /**
     * Test entitling a traditional client with Ansible Control Node
     * @throws Exception
     */
    @Test
    public void testEntitleAnsibleControlNodeToTradClient() throws Exception {
        User user = UserTestUtils.findNewUser("testUser",
                "testOrg" + this.getClass().getSimpleName());
        user.addPermanentRole(RoleFactory.ORG_ADMIN);
        Server server = ServerTestUtils.createTestSystem(user, ServerConstants.getServerGroupTypeEnterpriseEntitled());
        server.setServerArch(ServerFactory.lookupServerArchByLabel("x86_64-redhat-linux"));
        server = HibernateFactory.reload(server);

        assertFalse(systemEntitlementManager.canEntitleServer(server, EntitlementManager.ANSIBLE_CONTROL_NODE));
        systemEntitlementManager.addEntitlementToServer(server, EntitlementManager.ANSIBLE_CONTROL_NODE);
        // adding the entitlement should have no effect
        assertFalse(server.hasEntitlement(EntitlementManager.ANSIBLE_CONTROL_NODE));
    }

    /**
     * Test entitling a salt minion with Ansible Control Node
     * @throws Exception
     */
    @Test
    public void testEntitleAnsibleControlNodeToSaltClient() throws Exception {
        User user = UserTestUtils.findNewUser("testUser",
                "testOrg" + this.getClass().getSimpleName());
        user.addPermanentRole(RoleFactory.ORG_ADMIN);
        Server server = MinionServerFactoryTest.createTestMinionServer(user);
        server.setServerArch(ServerFactory.lookupServerArchByLabel("x86_64-redhat-linux"));
        server = HibernateFactory.reload(server);

        assertTrue(systemEntitlementManager.canEntitleServer(server, EntitlementManager.ANSIBLE_CONTROL_NODE));
        systemEntitlementManager.addEntitlementToServer(server, EntitlementManager.ANSIBLE_CONTROL_NODE);
        assertTrue(server.hasEntitlement(EntitlementManager.ANSIBLE_CONTROL_NODE));

        systemEntitlementManager.removeServerEntitlement(server, EntitlementManager.ANSIBLE_CONTROL_NODE);
        assertFalse(server.hasEntitlement(EntitlementManager.ANSIBLE_CONTROL_NODE));
    }
}
