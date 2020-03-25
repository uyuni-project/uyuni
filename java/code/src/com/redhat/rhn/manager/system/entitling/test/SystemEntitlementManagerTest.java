/**
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

import static com.redhat.rhn.testing.RhnBaseTestCase.reload;

import com.redhat.rhn.common.conf.ConfigDefaults;
import com.redhat.rhn.common.validator.ValidatorResult;
import com.redhat.rhn.domain.channel.Channel;
import com.redhat.rhn.domain.role.RoleFactory;
import com.redhat.rhn.domain.server.Server;
import com.redhat.rhn.domain.server.ServerFactory;
import com.redhat.rhn.domain.server.test.MinionServerFactoryTest;
import com.redhat.rhn.domain.user.User;
import com.redhat.rhn.manager.entitlement.EntitlementManager;
import com.redhat.rhn.manager.system.entitling.SystemEntitlementManager;
import com.redhat.rhn.manager.system.entitling.SystemEntitler;
import com.redhat.rhn.manager.system.entitling.SystemUnentitler;
import com.redhat.rhn.testing.ChannelTestUtils;
import com.redhat.rhn.testing.JMockBaseTestCaseWithUser;
import com.redhat.rhn.testing.ServerTestUtils;
import com.redhat.rhn.testing.TestUtils;
import com.redhat.rhn.testing.UserTestUtils;

import com.suse.manager.virtualization.VirtManagerSalt;
import com.suse.manager.webui.services.impl.SaltSSHService;
import com.suse.manager.webui.services.impl.SaltService;

import org.jmock.Expectations;
import org.jmock.lib.legacy.ClassImposteriser;


public class SystemEntitlementManagerTest extends JMockBaseTestCaseWithUser {

    private SaltService saltServiceMock;
    private SystemEntitlementManager systemEntitlementManager;

    @Override
    public void setUp() throws Exception {
        super.setUp();
        setImposteriser(ClassImposteriser.INSTANCE);
        saltServiceMock = mock(SaltService.class);
        SaltService saltService = new SaltService();
        systemEntitlementManager = new SystemEntitlementManager(
                new SystemUnentitler(),
                new SystemEntitler(saltService, new VirtManagerSalt(saltService))
        );
    }

    /**
     * Tests adding and removing entitlement on a server
     * @throws Exception if something goes wrong
     */
    public void testEntitleServer() throws Exception {
        User user = UserTestUtils.findNewUser("testUser",
                "testOrg" + this.getClass().getSimpleName());
        user.addPermanentRole(RoleFactory.ORG_ADMIN);
        Server server = ServerTestUtils.createTestSystem(user);
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
            allowing(saltServiceMock).generateSSHKey(with(equal(SaltSSHService.SSH_KEY_PATH)));
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

    public void testVirtualEntitleServer() throws Exception {
        // User and server
        User user = UserTestUtils.findNewUser("testUser",
                "testOrg" + this.getClass().getSimpleName());
        user.addPermanentRole(RoleFactory.ORG_ADMIN);
        Server server = ServerTestUtils.createTestSystem(user);
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
        if (retval.getErrors().size() > 0) {
            key = retval.getErrors().get(0).getKey();
        }
        assertFalse("Got back: " + key, retval.hasErrors());

        // Test stuff!
        assertTrue(server.hasEntitlement(EntitlementManager.VIRTUALIZATION));
        assertTrue(server.getChannels().contains(rhnTools));
        if (!ConfigDefaults.get().isSpacewalk()) {
            // this is actually Satellite-specific
            // assertTrue(server.getChannels().contains(rhelVirt));
        }


        // Test removal
        systemEntitlementManager.removeServerEntitlement(server, EntitlementManager.VIRTUALIZATION);

        server = reload(server);
        assertFalse(server.hasEntitlement(EntitlementManager.VIRTUALIZATION));

    }
}
