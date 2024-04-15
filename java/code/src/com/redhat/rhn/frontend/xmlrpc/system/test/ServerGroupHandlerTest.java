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
package com.redhat.rhn.frontend.xmlrpc.system.test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.junit.jupiter.api.Assertions.fail;

import com.redhat.rhn.FaultException;
import com.redhat.rhn.domain.config.ConfigChannel;
import com.redhat.rhn.domain.formula.Formula;
import com.redhat.rhn.domain.role.RoleFactory;
import com.redhat.rhn.domain.server.ManagedServerGroup;
import com.redhat.rhn.domain.server.Server;
import com.redhat.rhn.domain.server.ServerGroup;
import com.redhat.rhn.domain.server.test.ServerFactoryTest;
import com.redhat.rhn.domain.user.User;
import com.redhat.rhn.domain.user.UserFactory;
import com.redhat.rhn.frontend.xmlrpc.ServerGroupAccessChangeException;
import com.redhat.rhn.frontend.xmlrpc.ServerNotInGroupException;
import com.redhat.rhn.frontend.xmlrpc.configchannel.ConfigChannelHandler;
import com.redhat.rhn.frontend.xmlrpc.system.XmlRpcSystemHelper;
import com.redhat.rhn.frontend.xmlrpc.systemgroup.ServerGroupHandler;
import com.redhat.rhn.frontend.xmlrpc.test.BaseHandlerTestCase;
import com.redhat.rhn.manager.system.ServerGroupManager;
import com.redhat.rhn.testing.ServerGroupTestUtils;
import com.redhat.rhn.testing.ServerTestUtils;
import com.redhat.rhn.testing.TestUtils;
import com.redhat.rhn.testing.UserTestUtils;

import com.suse.cloud.CloudPaygManager;
import com.suse.manager.attestation.AttestationManager;
import com.suse.manager.webui.controllers.bootstrap.RegularMinionBootstrapper;
import com.suse.manager.webui.controllers.bootstrap.SSHMinionBootstrapper;
import com.suse.manager.webui.services.iface.SaltApi;
import com.suse.manager.webui.services.iface.SystemQuery;
import com.suse.manager.webui.services.test.TestSaltApi;
import com.suse.manager.webui.services.test.TestSystemQuery;

import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Calendar;
import java.util.Collections;
import java.util.List;


/**
 * ServerGroupHandlerTest
 */
public class ServerGroupHandlerTest extends BaseHandlerTestCase {
    private final SaltApi saltApi = new TestSaltApi();
    private final SystemQuery systemQuery = new TestSystemQuery();
    private final CloudPaygManager paygManager = new CloudPaygManager();
    private final AttestationManager attestationManager = new AttestationManager();
    private final RegularMinionBootstrapper regularMinionBootstrapper =
            new RegularMinionBootstrapper(systemQuery, saltApi, paygManager, attestationManager);
    private final SSHMinionBootstrapper sshMinionBootstrapper =
            new SSHMinionBootstrapper(systemQuery, saltApi, paygManager, attestationManager);
    private final XmlRpcSystemHelper xmlRpcSystemHelper = new XmlRpcSystemHelper(
            regularMinionBootstrapper,
            sshMinionBootstrapper
    );
    private final ServerGroupManager manager = new ServerGroupManager(saltApi);
    private final ServerGroupHandler handler = new ServerGroupHandler(xmlRpcSystemHelper, manager);
    private static final String NAME = "HAHAHA" + TestUtils.randomString();
    private static final String DESCRIPTION =  TestUtils.randomString();

    @Test
    public void testCreate() {
        handler.create(admin, NAME, DESCRIPTION);
        assertNotNull(manager.lookup(NAME, admin));

        try {
            handler.create(admin, NAME, DESCRIPTION);
            fail("Duplicate key didn't raise an exception");
        }
        catch (Exception e) {
            //duplicate check successful.
        }
        regular.removePermanentRole(RoleFactory.SYSTEM_GROUP_ADMIN);
        try {

            handler.create(regular, NAME + "F", DESCRIPTION + "F");
            fail("Regular user allowed to create server groups");
        }
        catch (Exception e) {
            //Cool only sys admins can create.
        }
    }

    @Test
    public void testUpdate() {

        ServerGroup group = handler.create(admin, NAME, DESCRIPTION);
        assertNotNull(manager.lookup(NAME, admin));
        regular.addPermanentRole(RoleFactory.SYSTEM_GROUP_ADMIN);
        String newDescription = DESCRIPTION + TestUtils.randomString();
        try {
            handler.update(regular, NAME, newDescription);
            fail("Can't access .. Should throw access / permission exception");
        }
        catch (Exception e) {
            //access check successful.
        }
        group = handler.update(admin, NAME, newDescription);
        assertEquals(group.getDescription(), newDescription);
    }

    @Test
    public void testListAdministrators() {
        regular.addPermanentRole(RoleFactory.SYSTEM_GROUP_ADMIN);
        ServerGroup group = handler.create(regular, NAME, DESCRIPTION);
        List<User> admins = handler.listAdministrators(regular, group.getName());
        assertTrue(admins.contains(regular));
        assertTrue(admins.contains(admin));
        //now test on permissions
        regular.removePermanentRole(RoleFactory.SYSTEM_GROUP_ADMIN);
        try {
            handler.listAdministrators(regular, group.getName());
            fail("Should throw access / permission exception for regular is not a sys admin");
        }
        catch (Exception e) {
          //access check successful.
        }
    }

    @Test
    public void testAddRemoveAdmins() {
        ServerGroup group = handler.create(admin, NAME, DESCRIPTION);
        assertNotNull(manager.lookup(NAME, admin));
        User newbie = UserTestUtils.createUser("Hahaha", admin.getOrg().getId());

        List<String> logins = new ArrayList<>();
        logins.add(newbie.getLogin());


        try {
            handler.addOrRemoveAdmins(regular, group.getName(), logins, true);
            fail("Regular user allowed to create server groups");
        }
        catch (Exception e) {
            //Cool only sys admins can create.
        }

        handler.addOrRemoveAdmins(admin, group.getName(),
                Collections.singletonList(regular.getLogin()), true);

        regular.addPermanentRole(RoleFactory.SYSTEM_GROUP_ADMIN);
        handler.addOrRemoveAdmins(regular, group.getName(), logins, true);
        List<User> admins = handler.listAdministrators(regular, group.getName());
        assertTrue(admins.contains(newbie));

        handler.addOrRemoveAdmins(regular, group.getName(), logins, false);
        assertFalse(manager.canAccess(newbie, group));
        admins = handler.listAdministrators(admin, group.getName());
        assertFalse(admins.contains(newbie));

        // verify that neither an org or sat admin may have their
        // group access changed
        User orgAdmin = UserTestUtils.findNewUser("orgAdmin", "newOrg", true);
        assertTrue(orgAdmin.hasRole(RoleFactory.ORG_ADMIN));
        assertFalse(orgAdmin.hasRole(RoleFactory.SAT_ADMIN));
        UserFactory.save(orgAdmin);

        addOrRemoveAnAdmin(group, orgAdmin, true);
        addOrRemoveAnAdmin(group, orgAdmin, false);

        User satAdmin = UserTestUtils.findNewUser("satAdmin", "newOrg", false);
        satAdmin.addPermanentRole(RoleFactory.SAT_ADMIN);
        assertTrue(satAdmin.hasRole(RoleFactory.SAT_ADMIN));
        assertFalse(satAdmin.hasRole(RoleFactory.ORG_ADMIN));

        addOrRemoveAnAdmin(group, satAdmin, true);
        addOrRemoveAnAdmin(group, satAdmin, false);
    }

    private void addOrRemoveAnAdmin(ServerGroup group, User user, boolean add) {
        List<String> logins = new ArrayList<>();
        logins.add(user.getLogin());

        try {
            handler.addOrRemoveAdmins(admin, group.getName(), logins, false);
            if (user.hasRole(RoleFactory.SAT_ADMIN)) {
                fail("Allowed changing admin access for a satellite admin.  add=" + add);
            }
            else if (user.hasRole(RoleFactory.ORG_ADMIN)) {
                fail("Allowed changing admin access for an org admin.  add=" + add);
            }
        }
        catch (ServerGroupAccessChangeException e) {
            //Cool cannot change access permissions for an sat/org admin.
        }
    }

    @Test
    public void testListGroupsWithNoAssociatedAdmins() {
        ServerGroup group = handler.create(admin, NAME, DESCRIPTION);
        ServerGroup group1 = handler.create(admin, NAME + "1",
                                                    DESCRIPTION + "1");
        ServerGroup group2 = handler.create(admin, NAME + "2",
                                                    DESCRIPTION + "2");
        List<ServerGroup> groups = handler.listGroupsWithNoAssociatedAdmins(admin);
        assertTrue(groups.contains(group));
        assertTrue(groups.contains(group1));
        assertTrue(groups.contains(group2));

        List<String> logins = new ArrayList<>();
        logins.add(regular.getLogin());
        handler.addOrRemoveAdmins(admin, group1.getName(), logins, true);
        assertTrue(manager.canAccess(regular, group1));
        groups = handler.listGroupsWithNoAssociatedAdmins(admin);
        assertFalse(groups.contains(group1));

        assertTrue(groups.contains(group));
        assertTrue(groups.contains(group2));
    }

    @Test
    public void testDelete() {
        handler.create(admin, NAME, DESCRIPTION);
        handler.delete(admin, NAME);
        try {
            manager.lookup(NAME, admin);
            fail("Should throw a lookup exception");
        }
        catch (Exception e) {
            //exception successfully thrown.
        }
    }

    @Test
    public void testAddRemoveSystems() {
        ServerGroup group = handler.create(admin, NAME, DESCRIPTION);
        assertNotNull(manager.lookup(NAME, admin));

        User unpriv = UserTestUtils.createUser("Unpriv", admin.getOrg().getId());
        List<String> logins = new ArrayList<>();
        logins.add(regular.getLogin());
        logins.add(unpriv.getLogin());

        handler.addOrRemoveAdmins(admin, group.getName(), logins, true);
        regular.addPermanentRole(RoleFactory.SYSTEM_GROUP_ADMIN);

        Server server1 = ServerFactoryTest.createTestServer(regular, true);
        Server server2 = ServerFactoryTest.createTestServer(regular, true);
        Server server3 = ServerFactoryTest.createTestServer(regular, true);

        handler.addOrRemoveSystems(regular, group.getName(),
                List.of(server3.getId().intValue()), Boolean.TRUE);

        List<Long> systems = new ArrayList<>();
        systems.add(server1.getId());
        systems.add(server2.getId());
        systems.add(server3.getId());
        handler.addOrRemoveSystems(regular, group.getName(), systems, Boolean.TRUE);


        List<Server> actual = handler.listSystems(unpriv, group.getName());
        assertTrue(actual.contains(server1));

        handler.addOrRemoveSystems(regular, group.getName(), systems,
                Boolean.FALSE);

        actual = handler.listSystems(regular, group.getName());
        assertFalse(actual.contains(server1));
    }

    @Test
    public void testRemoveNonExistentServer() {
        ServerGroup group = handler.create(admin, NAME, DESCRIPTION);
        List<Long> systems = new ArrayList<>();
        Server server1 = ServerFactoryTest.createTestServer(admin, true);
        systems.add(server1.getId());
        try {
            handler.addOrRemoveSystems(admin, group.getName(), systems,
                    Boolean.FALSE);
            fail();
        }
        catch (ServerNotInGroupException e) {
            // expected
        }
    }

    @Test
    public void testListAllGroups() {
        int preSize = handler.listAllGroups(admin).size();

        ManagedServerGroup group = ServerGroupTestUtils.createManaged(admin);
        List<ManagedServerGroup> groups = handler.listAllGroups(admin);
        assertTrue(groups.contains(group));
        assertEquals(1, groups.size() - preSize);
    }

    @Test
    public void testGetDetailsById() {
        ManagedServerGroup group = ServerGroupTestUtils.createManaged(admin);
        ServerGroup sg = handler.getDetails(admin,
                group.getId().intValue());
        assertEquals(sg, group);
    }

    @Test
    public void testGetDetailsByName() {
        ManagedServerGroup group = ServerGroupTestUtils.createManaged(admin);
        ServerGroup sg = handler.getDetails(admin, group.getName());
        assertEquals(sg, group);

    }

    @Test
    public void testGetDetailsByUnknownId() {
        boolean exceptCaught = false;
        int badValue = -80;
        try {
            handler.getDetails(admin, badValue);
        }
        catch (FaultException e) {
            exceptCaught = true;
        }
        assertTrue(exceptCaught);
    }

    @Test
    public void testGetDetailsByUnknownName() {
        boolean exceptCaught = false;
        String badName = "intentionalBadName123456789";
        try {
            handler.getDetails(admin, badName);
        }
        catch (FaultException e) {
            exceptCaught = true;
        }
        assertTrue(exceptCaught);
    }


    @Test
    public void testListInactiveServersInGroup() throws Exception {
        ManagedServerGroup group = ServerGroupTestUtils.createManaged(admin);
        Server server = ServerTestUtils.createTestSystem(admin);
        Server server2 = ServerTestUtils.createTestSystem(admin);

        List<Server>  test = new ArrayList<>();
        test.add(server);
        test.add(server2);
        manager.addServers(group, test, admin);


        Calendar cal = Calendar.getInstance();
        cal.add(Calendar.HOUR, -442);
        server.getServerInfo().setCheckin(cal.getTime());
        TestUtils.saveAndFlush(server);
        TestUtils.saveAndFlush(group);

        List<Long> list = handler.listInactiveSystemsInGroup(admin, group.getName(), 1);
        assertEquals(1, list.size());
        assertEquals(server.getId().toString(), list.get(0).toString());
    }

    @Test
    public void testListActiveServersInGroup() throws Exception {
        ManagedServerGroup group = ServerGroupTestUtils.createManaged(admin);
        Server server = ServerTestUtils.createTestSystem(admin);
        Server server2 = ServerTestUtils.createTestSystem(admin);

        List<Server>  test = new ArrayList<>();
        test.add(server);
        test.add(server2);

        Calendar cal = Calendar.getInstance();
        cal.add(Calendar.HOUR, -442);
        server2.getServerInfo().setCheckin(cal.getTime());

        manager.addServers(group, test, admin);

        TestUtils.saveAndFlush(server);
        TestUtils.saveAndFlush(group);

        List<Long> list = handler.listActiveSystemsInGroup(admin, group.getName());

        assertEquals(1, list.size());
        assertEquals(server.getId().toString(), list.get(0).toString());
    }

    @Test
    public void testSubscribeAndListAssignedConfigChannels() {
        ConfigChannelHandler ccHandler = new ConfigChannelHandler();
        String ccLabel1 = "CC-LABEL-1-" + TestUtils.randomString();
        String ccLabel2 = "CC-LABEL-2-" + TestUtils.randomString();

        ConfigChannel cc1 = ccHandler.create(admin,
                ccLabel1,
                "CC NAME " + TestUtils.randomString(),
                "CC DESCRIPTION " + TestUtils.randomString(),
                "state");
        ConfigChannel cc2 = ccHandler.create(admin,
                ccLabel2,
                "CC NAME " + TestUtils.randomString(),
                "CC DESCRIPTION " + TestUtils.randomString(),
                "state");

        ManagedServerGroup group1 = ServerGroupTestUtils.createManaged(admin);
        ManagedServerGroup group2 = ServerGroupTestUtils.createManaged(admin);

        handler.subscribeConfigChannel(admin, group1.getName(), List.of(ccLabel1));
        handler.subscribeConfigChannel(admin, group2.getName(), List.of(ccLabel2));
        List<ConfigChannel> assignedChannels1 = handler.listAssignedConfigChannels(admin, group1.getName());
        List<ConfigChannel> assignedChannels2 = handler.listAssignedConfigChannels(admin, group2.getName());

        assertContains(assignedChannels1, cc1);
        assertContains(assignedChannels2, cc2);
    }

    @Test
    public void testSubscribeAndListAssignedConfigChannels2() {
        ConfigChannelHandler ccHandler = new ConfigChannelHandler();
        String ccLabel1 = "CC-LABEL-1-" + TestUtils.randomString();
        String ccLabel2 = "CC-LABEL-2-" + TestUtils.randomString();

        ConfigChannel cc1 = ccHandler.create(admin,
                ccLabel1,
                "CC NAME " + TestUtils.randomString(),
                "CC DESCRIPTION " + TestUtils.randomString(),
                "state");
        ConfigChannel cc2 = ccHandler.create(admin,
                ccLabel2,
                "CC NAME " + TestUtils.randomString(),
                "CC DESCRIPTION " + TestUtils.randomString(),
                "state");

        ManagedServerGroup group1 = ServerGroupTestUtils.createManaged(admin);
        ManagedServerGroup group2 = ServerGroupTestUtils.createManaged(admin);

        handler.subscribeConfigChannel(admin, group1.getName(), Arrays.asList(ccLabel1, ccLabel2));
        List<ConfigChannel> assignedChannels1 = handler.listAssignedConfigChannels(admin, group1.getName());
        List<ConfigChannel> assignedChannels2 = handler.listAssignedConfigChannels(admin, group2.getName());

        assertContains(assignedChannels1, cc1);
        assertContains(assignedChannels1, cc2);
        assertTrue(assignedChannels2.isEmpty(), "Unexpected assigned channels");
    }

    @Test
    public void testUnsubscribeConfigChannels() {
        ConfigChannelHandler ccHandler = new ConfigChannelHandler();
        String ccLabel1 = "CC-LABEL-1-" + TestUtils.randomString();
        String ccLabel2 = "CC-LABEL-2-" + TestUtils.randomString();

        ConfigChannel cc1 = ccHandler.create(admin,
                ccLabel1,
                "CC NAME " + TestUtils.randomString(),
                "CC DESCRIPTION " + TestUtils.randomString(),
                "state");
        ConfigChannel cc2 = ccHandler.create(admin,
                ccLabel2,
                "CC NAME " + TestUtils.randomString(),
                "CC DESCRIPTION " + TestUtils.randomString(),
                "state");

        ManagedServerGroup group1 = ServerGroupTestUtils.createManaged(admin);

        handler.subscribeConfigChannel(admin, group1.getName(), Arrays.asList(ccLabel1, ccLabel2));
        List<ConfigChannel> assignedChannels1 = handler.listAssignedConfigChannels(admin, group1.getName());

        assertContains(assignedChannels1, cc1);
        assertContains(assignedChannels1, cc2);

        handler.unsubscribeConfigChannel(admin, group1.getName(), List.of(ccLabel1));
        assignedChannels1 = handler.listAssignedConfigChannels(admin, group1.getName());
        assertContains(assignedChannels1, cc2);
        assertFalse(assignedChannels1.contains(cc1), "Unexpected channel found");
    }

    /*
     * Just check that we do not crash
     */
    @Test
    public void testListAssignedFormulas() {
        ManagedServerGroup group = ServerGroupTestUtils.createManaged(admin);

        List<Formula> assignedFormulas = handler.listAssignedFormuals(admin, group.getName());

        assertTrue(assignedFormulas.isEmpty(), "Unexpected assigned formulas found");
    }
}
