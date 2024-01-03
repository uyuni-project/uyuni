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
package com.redhat.rhn.manager.org.test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.junit.jupiter.api.Assertions.fail;

import com.redhat.rhn.common.hibernate.HibernateFactory;
import com.redhat.rhn.common.security.PermissionException;
import com.redhat.rhn.domain.config.ConfigChannel;
import com.redhat.rhn.domain.org.Org;
import com.redhat.rhn.domain.org.SystemMigration;
import com.redhat.rhn.domain.org.SystemMigrationFactory;
import com.redhat.rhn.domain.role.RoleFactory;
import com.redhat.rhn.domain.server.EntitlementServerGroup;
import com.redhat.rhn.domain.server.ManagedServerGroup;
import com.redhat.rhn.domain.server.Server;
import com.redhat.rhn.domain.server.ServerFactory;
import com.redhat.rhn.domain.server.ServerHistoryEvent;
import com.redhat.rhn.domain.server.test.ServerFactoryTest;
import com.redhat.rhn.domain.user.User;
import com.redhat.rhn.domain.user.UserFactory;
import com.redhat.rhn.manager.entitlement.EntitlementManager;
import com.redhat.rhn.manager.formula.FormulaMonitoringManager;
import com.redhat.rhn.manager.org.MigrationManager;
import com.redhat.rhn.manager.system.ServerGroupManager;
import com.redhat.rhn.manager.system.entitling.SystemEntitlementManager;
import com.redhat.rhn.manager.system.entitling.SystemEntitler;
import com.redhat.rhn.manager.system.entitling.SystemUnentitler;
import com.redhat.rhn.testing.BaseTestCaseWithUser;
import com.redhat.rhn.testing.ConfigTestUtils;
import com.redhat.rhn.testing.ServerTestUtils;
import com.redhat.rhn.testing.UserTestUtils;

import com.suse.manager.virtualization.VirtManagerSalt;
import com.suse.manager.webui.services.iface.MonitoringManager;
import com.suse.manager.webui.services.iface.SaltApi;
import com.suse.manager.webui.services.iface.VirtManager;
import com.suse.manager.webui.services.test.TestSaltApi;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

/**
 * MigrationManagerTest
 */
public class MigrationManagerTest extends BaseTestCaseWithUser {

    private Set<User> origOrgAdmins = new HashSet<>();
    private Set<User> destOrgAdmins = new HashSet<>();
    private Org origOrg;
    private Org destOrg;
    private Server server;  // virt host w/guests
    private Server server2; // server w/provisioning ent

    private final SaltApi saltApi = new TestSaltApi();
    private final ServerGroupManager serverGroupManager = new ServerGroupManager(saltApi);
    private final VirtManager virtManager = new VirtManagerSalt(saltApi);
    private final MonitoringManager monitoringManager = new FormulaMonitoringManager(saltApi);
    private final SystemEntitlementManager systemEntitlementManager = new SystemEntitlementManager(
            new SystemUnentitler(virtManager, monitoringManager, serverGroupManager),
            new SystemEntitler(saltApi, virtManager, monitoringManager, serverGroupManager)
    );
    private final MigrationManager migrationManager = new MigrationManager(serverGroupManager);

    @Override
    @BeforeEach
    public void setUp() throws Exception {
        super.setUp();

        // Create 2 orgs, each with multiple org admins
        origOrgAdmins.add(UserTestUtils.findNewUser("origAdmin", "origOrg", true));
        origOrg = origOrgAdmins.iterator().next().getOrg();
        for (Integer i = 0; i < 2; i++) {
            User user = UserTestUtils.createUser("origAdmin", origOrg.getId());
            user.addPermanentRole(RoleFactory.ORG_ADMIN);
            UserFactory.save(user);
            origOrgAdmins.add(user);
        }

        destOrgAdmins.add(UserTestUtils.findNewUser("destAdmin", "destOrg", true));
        destOrg = destOrgAdmins.iterator().next().getOrg();
        for (Integer i = 0; i < 2; i++) {
            User user = UserTestUtils.createUser("destAdmin", destOrg.getId());
            user.addPermanentRole(RoleFactory.ORG_ADMIN);
            UserFactory.save(user);
            destOrgAdmins.add(user);
        }

        // Create a virtual host with guests and a server with provisioning entitlements
        // and associate the first org's admins with them both
        server = ServerTestUtils.createVirtHostWithGuests(
                origOrgAdmins.iterator().next(), 2, systemEntitlementManager);
        server2 = ServerFactoryTest.createTestServer(origOrgAdmins.iterator().next(), true);

        ServerFactory.save(server);
        ServerFactory.save(server2);
        HibernateFactory.getSession().flush();
    }

    @Test
    public void testMigrateSystemNotSatAdmin() {
        User user = UserTestUtils.findNewUser("testUser",
                "testOrg" + this.getClass().getSimpleName());
        try {
            migrationManager.removeOrgRelationships(user, server);
            fail();
        }
        catch (PermissionException e) {
            // expected
        }
    }

    @Test
    public void testRemoveEntitlements() {
        assertFalse(server.getEntitlements().isEmpty());

        migrationManager.removeOrgRelationships(origOrgAdmins.iterator().next(), server);
        server = ServerFactory.lookupById(server.getId());

        assertTrue(server.getEntitlements().isEmpty());
    }

    @Test
    public void testRemoveSystemGroups() {
        assertFalse(server.getGuests().isEmpty());
        assertEquals(1, server.getManagedGroups().size());
        ManagedServerGroup serverGroup1 = server.getManagedGroups().get(0);

        migrationManager.removeOrgRelationships(origOrgAdmins.iterator().next(), server);
        server = ServerFactory.lookupById(server.getId());

        //serverGroup1 = (ManagedServerGroup) reload(serverGroup1);
        assertEquals(0, serverGroup1.getCurrentMembers().intValue());

        assertEquals(0, server.getManagedGroups().size());
    }

    @Test
    public void testRemoveChannels() {

        // verify that server was initially created w/channels
        assertFalse(server.getChannels().isEmpty());

        migrationManager.removeOrgRelationships(origOrgAdmins.iterator().next(), server);

        assertEquals(0, server.getChannels().size());
    }

    @Test
    public void testRemoveConfigChannels() {

        ConfigChannel configChannel = ConfigTestUtils.createConfigChannel(origOrg);
        ConfigChannel configChannel2 = ConfigTestUtils.createConfigChannel(origOrg);

        server2.subscribeConfigChannel(configChannel, user);
        server2.subscribeConfigChannel(configChannel2, user);

        assertEquals(2, server2.getConfigChannelCount());

        migrationManager.removeOrgRelationships(origOrgAdmins.iterator().next(), server2);

        assertEquals(0, server2.getConfigChannelCount());
    }

    @Test
    public void testUpdateAdminRelationships() {
        for (User origOrgAdmin : origOrgAdmins) {
            assertTrue(origOrgAdmin.getServers().contains(server));
        }
        for (User destOrgAdmin : destOrgAdmins) {
            assertFalse(destOrgAdmin.getServers().contains(server));
        }

        MigrationManager.updateAdminRelationships(origOrg, destOrg, server);

        for (User origOrgAdmin : origOrgAdmins) {
            assertFalse(origOrgAdmin.getServers().contains(server));
        }
        for (User destOrgAdmin : destOrgAdmins) {
            assertTrue(destOrgAdmin.getServers().contains(server));
        }
    }

    @Test
    public void testMigrateServers() {

        assertEquals(server.getOrg(), origOrg);
        assertEquals(server2.getOrg(), origOrg);

        List<EntitlementServerGroup> entGroups = server.getEntitledGroups();
        List<EntitlementServerGroup> entGroups2 = server2.getEntitledGroups();

        assertNotNull(entGroups);
        entGroups.forEach(ent -> assertEquals(origOrg.getId(), ent.getOrg().getId()));
        assertNotNull(entGroups2);
        entGroups2.forEach(ent -> assertEquals(origOrg.getId(), ent.getOrg().getId()));

        List<Server> servers = new ArrayList<>();
        servers.add(server);
        servers.add(server2);
        User origOrgAdmin = origOrgAdmins.iterator().next();
        migrationManager.migrateServers(origOrgAdmin, destOrg, servers);

        assertEquals(server.getOrg(), destOrg);
        assertEquals(server2.getOrg(), destOrg);

        assertEquals(server.getEntitledGroups().size(), entGroups.size());
        server.getEntitledGroups().forEach(ent -> assertEquals(destOrg.getId(), ent.getOrg().getId()));
        assertEquals(server2.getEntitledGroups().size(), entGroups2.size());
        server2.getEntitledGroups().forEach(ent -> assertEquals(destOrg.getId(), ent.getOrg().getId()));

        assertNotNull(server.getHistory());
        assertFalse(server.getHistory().isEmpty());
        boolean migrationRecorded = false;
        for (ServerHistoryEvent event : server.getHistory()) {
            if (event.getSummary().equals(String.format("System migration scheduled by %s", origOrgAdmin.getLogin())) &&
                event.getDetails().contains("From organization: " + origOrg.getName()) &&
                event.getDetails().contains("To organization: " + destOrg.getName()) &&
                (event.getCreated() != null)) {
                migrationRecorded = true;
            }
        }
        assertTrue(migrationRecorded);

        List<SystemMigration> s1Migrations = SystemMigrationFactory.lookupByServer(server);
        List<SystemMigration> s2Migrations = SystemMigrationFactory.lookupByServer(
                server2);
        assertNotNull(s1Migrations);
        assertNotNull(s2Migrations);
        assertEquals(1, s1Migrations.size());
        assertEquals(1, s2Migrations.size());
    }

    @Test
    public void testMigrateBootstrapServer() {
        User origOrgAdmin = origOrgAdmins.iterator().next();
        Server bootstrapServer = ServerFactoryTest.createUnentitledTestServer(origOrgAdmin,
            true, ServerFactoryTest.TYPE_SERVER_NORMAL, getNow());
        systemEntitlementManager.addEntitlementToServer(bootstrapServer, EntitlementManager.BOOTSTRAP);

        assertEquals(1, bootstrapServer.getEntitlements().size());

        assertEquals(bootstrapServer.getOrg(), origOrg);

        List<Server> servers = new ArrayList<>();
        servers.add(bootstrapServer);
        migrationManager.migrateServers(origOrgAdmin, destOrg, servers);

        assertEquals(bootstrapServer.getOrg(), destOrg);

        assertNotNull(bootstrapServer.getHistory());
        assertFalse(bootstrapServer.getHistory().isEmpty());
        boolean migrationRecorded = false;
        for (ServerHistoryEvent event : bootstrapServer.getHistory()) {
            if (event.getSummary().equals(String.format("System migration scheduled by %s", origOrgAdmin.getLogin())) &&
                event.getDetails().contains("From organization: " + origOrg.getName()) &&
                event.getDetails().contains("To organization: " + destOrg.getName()) &&
                (event.getCreated() != null)) {
                migrationRecorded = true;
            }
        }
        assertTrue(migrationRecorded);

        List<SystemMigration> s1Migrations = SystemMigrationFactory
            .lookupByServer(bootstrapServer);
        assertNotNull(s1Migrations);
        assertEquals(1, s1Migrations.size());

        assertEquals(1, bootstrapServer.getEntitlements().size());
        assertEquals(EntitlementManager.BOOTSTRAP_ENTITLED, bootstrapServer
            .getEntitlements().iterator().next().getLabel());
    }
}
