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

package com.redhat.rhn.manager.user.test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertInstanceOf;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.junit.jupiter.api.Assertions.fail;

import com.redhat.rhn.common.ObjectCreateWrapperException;
import com.redhat.rhn.common.db.datasource.DataResult;
import com.redhat.rhn.common.hibernate.HibernateFactory;
import com.redhat.rhn.common.hibernate.LookupException;
import com.redhat.rhn.common.security.PermissionException;
import com.redhat.rhn.common.security.user.StateChangeException;
import com.redhat.rhn.domain.rhnpackage.Package;
import com.redhat.rhn.domain.rhnpackage.test.PackageTest;
import com.redhat.rhn.domain.rhnset.RhnSet;
import com.redhat.rhn.domain.rhnset.SetCleanup;
import com.redhat.rhn.domain.role.RoleFactory;
import com.redhat.rhn.domain.server.ManagedServerGroup;
import com.redhat.rhn.domain.server.NetworkInterface;
import com.redhat.rhn.domain.server.Server;
import com.redhat.rhn.domain.server.ServerConstants;
import com.redhat.rhn.domain.server.ServerFactory;
import com.redhat.rhn.domain.server.ServerGroup;
import com.redhat.rhn.domain.server.ServerGroupFactory;
import com.redhat.rhn.domain.server.test.NetworkInterfaceTest;
import com.redhat.rhn.domain.server.test.ServerFactoryTest;
import com.redhat.rhn.domain.server.test.ServerGroupTest;
import com.redhat.rhn.domain.user.RhnTimeZone;
import com.redhat.rhn.domain.user.User;
import com.redhat.rhn.domain.user.UserFactory;
import com.redhat.rhn.domain.user.UserServerPreference;
import com.redhat.rhn.domain.user.UserServerPreferenceId;
import com.redhat.rhn.frontend.dto.SystemSearchResult;
import com.redhat.rhn.frontend.dto.UserOverview;
import com.redhat.rhn.frontend.listview.PageControl;
import com.redhat.rhn.manager.rhnset.RhnSetManager;
import com.redhat.rhn.manager.system.SystemManager;
import com.redhat.rhn.manager.user.UserManager;
import com.redhat.rhn.testing.RhnBaseTestCase;
import com.redhat.rhn.testing.TestStatics;
import com.redhat.rhn.testing.TestUtils;
import com.redhat.rhn.testing.UserTestUtils;

import com.suse.manager.webui.services.iface.SaltApi;
import com.suse.manager.webui.services.test.TestSaltApi;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.time.ZoneId;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

/** JUnit test case for the User
 *  class.
 */
public class UserManagerTest extends RhnBaseTestCase {

    private SystemManager systemManager;
    private Set<User> users;

    /**
     * {@inheritDoc}
     */
    @Override
    @BeforeEach
    public void setUp() throws Exception {
        super.setUp();
        this.users = new HashSet<>();
        SaltApi saltApi = new TestSaltApi();
        systemManager = new SystemManager(ServerFactory.SINGLETON, ServerGroupFactory.SINGLETON, saltApi);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    @AfterEach
    public void tearDown() throws Exception {
        super.tearDown();

        this.users = null;
    }

    @Test
    public void testGrantServerGroupPermission() {
        //Group and user have the same org, so should be possible to grant permits
        User user = UserTestUtils.createUser("user_1", "org_1");
        this.users.add(user);

        Server server = ServerFactoryTest.createTestServer(user, false,
                ServerConstants.getServerGroupTypeEnterpriseEntitled());

        ServerGroup group = ServerGroupTest
                .createTestServerGroup(user.getOrg(), null);

        systemManager.addServerToServerGroup(server, group);
        ServerFactory.save(server);

        ServerGroup foundGroup = ServerGroupFactory.lookupByIdAndOrg(group.getId(),
                group.getOrg());
        assertNotNull(foundGroup);

        List<Server> servers = foundGroup.getServers();
        assertNotNull(servers);
        assertEquals(servers.size(), 1);
        assertTrue(servers.stream().allMatch(s -> s.getId().equals(server.getId())));

        User foundUser = UserFactory.lookupById(user.getId());
        assertEquals(foundUser.getOrg().getId(), user.getOrg().getId());
        assertEquals(foundUser.getOrg().getId(), group.getOrg().getId());

        Set<ServerGroup> userGroups = foundUser.getAssociatedServerGroups();
        assertNotNull(userGroups);
        assertTrue(userGroups.isEmpty());

        UserManager.grantServerGroupPermission(foundUser, group.getId());

        HibernateFactory.getSession().clear();

        foundUser = UserFactory.lookupById(foundUser.getId());
        userGroups = foundUser.getAssociatedServerGroups();
        assertNotNull(userGroups);
        assertEquals(userGroups.size(), 1);
        assertTrue(userGroups.stream().allMatch(g -> g.getId().equals(group.getId())));

        //Group and user have different orgs, so should not be possible to grant permits
        User user2 = UserTestUtils.createUser("user_2", "org_2");
        this.users.add(user);

        User foundUser2 = UserFactory.lookupById(user2.getId());
        assertEquals(foundUser2.getId(), user2.getId());
        assertNotEquals(foundUser2.getOrg().getId(), group.getOrg().getId());

        Set<ServerGroup> userGroups2 = foundUser2.getAssociatedServerGroups();
        assertNotNull(userGroups2);
        assertTrue(userGroups2.isEmpty());

        UserManager.grantServerGroupPermission(foundUser2, group.getId());

        HibernateFactory.getSession().clear();

        foundUser2 = UserFactory.lookupById(user2.getId());
        userGroups2 = foundUser2.getAssociatedServerGroups();
        assertNotNull(userGroups2);
        assertTrue(userGroups2.isEmpty());

        //One group with same org than the user's, and one group with different org.
        //It should not be possible to grant permissions
        Server server2 = ServerFactoryTest.createTestServer(foundUser2, false,
                ServerConstants.getServerGroupTypeEnterpriseEntitled());

        ServerGroup group2 = ServerGroupTest
                .createTestServerGroup(foundUser2.getOrg(), null);

        systemManager.addServerToServerGroup(server2, group2);
        ServerFactory.save(server2);

        ManagedServerGroup foundGroup2 = ServerGroupFactory.lookupByIdAndOrg(group2.getId(),
                group2.getOrg());
        assertNotNull(foundGroup2);

        List<Server> servers2 = foundGroup2.getServers();
        assertNotNull(servers2);
        assertEquals(servers2.size(), 1);
        assertTrue(servers2.stream().allMatch(s -> s.getId().equals(server2.getId())));

        assertEquals(foundUser2.getOrg().getId(), group2.getOrg().getId());

        userGroups2 = foundUser2.getAssociatedServerGroups();
        assertNotNull(userGroups2);
        assertTrue(userGroups2.isEmpty());

        UserManager.grantServerGroupPermission(foundUser2.getId(), Arrays.asList(group.getId(), group2.getId()));

        HibernateFactory.getSession().clear();

        foundUser2 = UserFactory.lookupById(user2.getId());
        userGroups2 = foundUser2.getAssociatedServerGroups();
        assertNotNull(userGroups2);
        assertTrue(userGroups2.isEmpty());

    }

    @Test
    public void testRevokeServerGroupPermission() {
        User user = UserTestUtils.createUser("user_test_revoke", "org_test_revoke");
        this.users.add(user);

        Server server = ServerFactoryTest.createTestServer(user, false,
                ServerConstants.getServerGroupTypeEnterpriseEntitled());

        ServerGroup group = ServerGroupTest
                .createTestServerGroup(user.getOrg(), null);

        systemManager.addServerToServerGroup(server, group);
        ServerFactory.save(server);

        ServerGroup foundGroup = ServerGroupFactory.lookupByIdAndOrg(group.getId(),
                group.getOrg());
        assertNotNull(foundGroup);

        List<Server> servers = foundGroup.getServers();
        assertNotNull(servers);
        assertEquals(servers.size(), 1);
        assertTrue(servers.stream().allMatch(s -> s.getId().equals(server.getId())));

        User foundUser = UserFactory.lookupById(user.getId());
        assertEquals(foundUser.getOrg().getId(), user.getOrg().getId());
        assertEquals(foundUser.getOrg().getId(), group.getOrg().getId());

        Set<ServerGroup> userGroups = foundUser.getAssociatedServerGroups();
        assertNotNull(userGroups);
        assertTrue(userGroups.isEmpty());

        UserManager.grantServerGroupPermission(foundUser, group.getId());

        HibernateFactory.getSession().clear();

        foundUser = UserFactory.lookupById(foundUser.getId());
        userGroups = foundUser.getAssociatedServerGroups();
        assertNotNull(userGroups);
        assertEquals(userGroups.size(), 1);
        assertTrue(userGroups.stream().allMatch(g -> g.getId().equals(group.getId())));

        UserManager.revokeServerGroupPermission(foundUser, group.getId());

        HibernateFactory.getSession().clear();

        foundUser = UserFactory.lookupById(foundUser.getId());
        userGroups = foundUser.getAssociatedServerGroups();
        assertNotNull(userGroups);
        assertTrue(userGroups.isEmpty());
    }

    @Test
    public void testListRolesAssignable() {
        User user = UserTestUtils.createUser();
        assertTrue(UserManager.listRolesAssignableBy(user).isEmpty());
        user.addPermanentRole(RoleFactory.ORG_ADMIN);
        UserManager.storeUser(user);
        assertFalse(UserManager.listRolesAssignableBy(user).
                contains(RoleFactory.SAT_ADMIN));

        User satAdmin = UserTestUtils.createUser(TestStatics.TEST_SAT_USER, user.getOrg().getId());
        satAdmin.addPermanentRole(RoleFactory.SAT_ADMIN);
        assertTrue(UserManager.listRolesAssignableBy(satAdmin).contains(RoleFactory.SAT_ADMIN));


    }

    @Test
    public void testVerifyPackageAccess() {
        User user = UserTestUtils.createUser();
        Package pkg = PackageTest.createTestPackage(user.getOrg());
        assertTrue(UserManager.verifyPackageAccess(user.getOrg(), pkg.getId()));

        // Since we have only one org on a sat, all custom created packages will be
        // available to all users in that org.
    }

    @Test
    public void testLookup() {
        User admin = UserTestUtils.createUser(this);
        admin.addPermanentRole(RoleFactory.ORG_ADMIN);

        User regular = UserTestUtils.createUser("testUser2", admin.getOrg().getId());
        regular.removePermanentRole(RoleFactory.ORG_ADMIN);

        assertTrue(admin.hasRole(RoleFactory.ORG_ADMIN));
        assertFalse(regular.hasRole(RoleFactory.ORG_ADMIN));

        // make sure admin can lookup regular by id and by login
        User test = UserManager.lookupUser(admin, regular.getId());
        assertNotNull(test);
        assertEquals(regular.getLogin(), test.getLogin());

        test = UserManager.lookupUser(admin, regular.getLogin());
        assertNotNull(test);
        assertEquals(regular.getLogin(), test.getLogin());

        // make sure regular user can't lookup users
        try {
            UserManager.lookupUser(regular, admin.getId());
            fail();
        }
        catch (PermissionException e) {
            //success
        }

        try {
            UserManager.lookupUser(regular, admin.getLogin());
            fail();
        }
        catch (PermissionException e) {
            //success
        }

        test = UserManager.lookupUser(regular, regular.getLogin());
        assertNotNull(test);
        assertEquals(regular.getLogin(), test.getLogin());

        test = UserManager.lookupUser(regular, regular.getId());
        assertNotNull(test);
        assertEquals(regular.getLogin(), test.getLogin());
    }

    @Test
    public void testUserDisableEnable() {
        //Create test users
        User org1admin = UserTestUtils.createUser("orgAdmin1", "UMTOrg1");
        org1admin.addPermanentRole(RoleFactory.ORG_ADMIN);
        UserManager.storeUser(org1admin);

        User org1admin2 = UserTestUtils.createUser("orgAdmin2", org1admin.getOrg().getId());
        org1admin2.addPermanentRole(RoleFactory.ORG_ADMIN);
        UserManager.storeUser(org1admin2);

        User org1normal = UserTestUtils.createUser("normaluser1", org1admin.getOrg().getId());
        User org1normal2 = UserTestUtils.createUser("normaluser2", org1admin.getOrg().getId());

        User org2admin = UserTestUtils.createUser("orgAdmin2", "UMTOrg2");
        org2admin.addPermanentRole(RoleFactory.ORG_ADMIN);
        UserManager.storeUser(org2admin);

        try {
            UserManager.disableUser(org1normal2, org1normal);
            fail("Normal user was allowed to disable an org admin");
        }
        catch (StateChangeException e) {
            assertEquals("userdisable.error.otheruser", e.getMessage());
        }

        //Can't disable other org admins
        try {
            UserManager.disableUser(org1admin2, org1admin);
            fail("Org admin was allowed to disable another org admin");
        }
        catch (StateChangeException e) {
            assertEquals("userdisable.error.orgadmin", e.getMessage());
        }

        //Make sure valid disables work
        //admin -> normal user
        UserManager.disableUser(org1admin, org1normal);
        assertTrue(org1normal.isDisabled());
        //admin -> self
        UserManager.disableUser(org1admin, org1admin);

        //Normal users can only disable themselves
        assertTrue(org1admin.isDisabled());
        //normal user -> self
        UserManager.disableUser(org1normal2, org1normal2);
        assertTrue(org1normal2.isDisabled());

        //Try to disable a user who is already disabled.
        // changing test for changed requirement.  Disabling a user
        // that was already disabled is a noop.  Not an error condition.
        try {
            UserManager.disableUser(org1admin2, org1normal);
            assertTrue(true);
        }
        catch (StateChangeException e) {
            fail("Org Admin disallowed to disable an already disabled user");
        }

        //Add a new user to org2
        User org2normal = UserTestUtils.createUser("normaluser2", org2admin.getOrg().getId());

        //Can't enable a user who isn't disabled
        try {
            UserManager.enableUser(org2admin, org2normal);
        }
        catch (StateChangeException e) {
            fail("Enabling an enabled user failed.  Should've passed silently");
        }


        //Enable org1normal2 for next test
        UserManager.enableUser(org1admin2, org1normal2);
        assertFalse(org1normal2.isDisabled());

        //Normal users can't enable users
        try {
            UserManager.enableUser(org1normal2, org1normal);
            fail("Normal user was allowed to enable a user");
        }
        catch (StateChangeException e) {
            assertEquals("userenable.error.orgadmin", e.getMessage());
        }

        //Make sure valid enables work
        //admin -> normal user
        UserManager.enableUser(org1admin2, org1normal);
        assertFalse(org1normal.isDisabled());
    }

    @Test
    public void testUsersInOrg() {
        int numTotal = 1;
        int numDisabled = 0;
        int numActive = 1;
        User user = UserTestUtils.createUser(this);
        user.addPermanentRole(RoleFactory.ORG_ADMIN);
        PageControl pc = new PageControl();
        pc.setStart(1);
        pc.setPageSize(5);

        numTotal = UserManager.usersInOrg(user, pc).getTotalSize();
        numDisabled = UserManager.disabledInOrg(user, pc).getTotalSize();
        numActive = UserManager.activeInOrg(user, pc).getTotalSize();

        //make sure usersInOrg and usersInOrgAsMap return the same number
        int uio1 = UserManager.usersInOrg(user, pc).getTotalSize();
        int uio2 = UserManager.usersInOrg(user, pc, Map.class).getTotalSize();
        assertEquals(uio1, uio2);

        try {
            UserManager.usersInOrg(user, pc, Set.class);
            fail();
        }
        catch (ObjectCreateWrapperException e) {
            //success
        }

        User peon = UserTestUtils.createUser("testBob", user.getOrg().getId());

        DataResult<UserOverview> orgUsers = UserManager.usersInOrg(user, pc);
        assertNotNull(orgUsers);
        assertEquals(numTotal + 1, orgUsers.getTotalSize());

        orgUsers = UserManager.activeInOrg(user, pc);
        assertNotNull(orgUsers);
        assertEquals(numActive + 1, orgUsers.getTotalSize());

        orgUsers = UserManager.disabledInOrg(user, pc);
        assertNotNull(orgUsers);
        assertEquals(numDisabled, orgUsers.getTotalSize());

        UserFactory.getInstance().disable(peon, user);

        orgUsers = UserManager.usersInOrg(user, pc);
        assertNotNull(orgUsers);
        assertEquals(numTotal + 1, orgUsers.getTotalSize());

        orgUsers = UserManager.activeInOrg(user, pc);
        assertNotNull(orgUsers);
        assertEquals(numActive, orgUsers.getTotalSize());

        orgUsers = UserManager.disabledInOrg(user, pc);
        assertNotNull(orgUsers);
        assertEquals(numDisabled + 1, orgUsers.getTotalSize());
    }


    @Test
    public void testLookupUserOrgBoundaries() {
        User usr1 = new UserTestUtils.UserBuilder()
                .orgName("testOrg1")
                .orgAdmin(true)
                .build();
        User usr2 = new UserTestUtils.UserBuilder().orgName("testOrg2").build();
        User usr3 = UserTestUtils.createUser("testUser123", usr1.getOrg().getId());
        try {
            UserManager.lookupUser(usr1, usr2.getLogin());
            String msg = "User1 of Org Id = %s should" +
                            "not be able to access Usr2  of Org Id= %s";
            fail(String.format(msg,
                    usr1.getOrg().getId(), usr2.getOrg().getId()));
        }
        catch (LookupException e) {
            //Success
        }
        assertEquals(usr3, UserManager.lookupUser(usr1, usr3.getLogin()));

    }
    @Test
    public void testStoreUser() {
        User usr = UserTestUtils.createUser(this);
        Long id = usr.getId();
        usr.setEmail("something@changed.redhat.com");
        UserManager.storeUser(usr);
        User u2 = UserFactory.lookupById(id);
        assertEquals("something@changed.redhat.com", u2.getEmail());
    }

    @Test
    public void testGetSystemGroups() {
        User usr = UserTestUtils.createUser(this);
        PageControl pc = new PageControl();
        pc.setIndexData(false);
        pc.setFilterColumn("name");
        pc.setStart(1);
        assertNotNull(UserManager.getSystemGroups(usr, pc));
    }

    @Test
    public void testGetTimeZoneId() {
        RhnTimeZone tz = UserManager.getTimeZone(UserManager
                .getTimeZone("Indian/Maldives").getTimeZoneId());
        assertEquals(UserManager.getTimeZone("Indian/Maldives"), tz);
        assertEquals("Indian/Maldives", tz.getOlsonName());

        RhnTimeZone tz2 = UserManager.getTimeZone(-23);
        assertNull(tz2);
    }

    @Test
    public void testGetTimeZoneOlson() {
        RhnTimeZone tz = UserManager.getTimeZone("America/New_York");
        assertNotNull(tz);
        assertEquals(tz.getOlsonName(), "America/New_York");

        RhnTimeZone tz2 = UserManager.getTimeZone("foo");
        assertNull(tz2);
    }

    @Test
    public void testGetTimeZoneDefault() {
        RhnTimeZone tz = UserManager.getDefaultTimeZone();
        assertNotNull(tz);
        assertEquals(tz.getTimeZone().getRawOffset(), UserFactory.getDefaultTimeZone()
                .getTimeZone().getRawOffset());
    }

    @Test
    public void testLookupTimeZoneAll() {
        List<RhnTimeZone> lst = UserManager.lookupAllTimeZones();
        assertTrue(lst.size() > 30);
        assertInstanceOf(RhnTimeZone.class, lst.get(5));
        assertInstanceOf(RhnTimeZone.class, lst.get(29));

        // Check if all configured timezones are valid
        Set<String> validTimezones = ZoneId.getAvailableZoneIds();
        assertTrue(lst.stream().filter(timezone ->
                !validTimezones.contains(timezone.getOlsonName()))
                .collect(Collectors.toList())
                .isEmpty());

        assertEquals(UserManager.getTimeZone("GMT"), lst.get(0));
        assertEquals("GMT", lst.get(0).getOlsonName());
        assertEquals("America/Sao_Paulo", lst.get(5).getOlsonName());
        assertEquals(UserManager.getTimeZone("America/Sao_Paulo"), lst.get(5));

        assertEquals("Europe/Paris", lst.get(lst.size() - 1).getOlsonName());
        assertEquals(UserManager.getTimeZone("Europe/Paris"), lst.get(lst.size() - 1));

    }

    @Test
   public void testUsersInSet() {
       User user = UserTestUtils.createUser();
        RhnSet set = RhnSetManager.createSet(user.getId(), "test_user_list",
               SetCleanup.NOOP);

       for (int i = 0; i < 5; i++) {
           User usr = UserTestUtils.createUser("testBob", user.getOrg().getId());
           set.addElement(usr.getId());
       }

       RhnSetManager.store(set);
       PageControl pc = new PageControl();
       pc.setStart(1);
       pc.setPageSize(10);
       DataResult<UserOverview> dr = UserManager.usersInSet(user, "test_user_list", pc);

       assertEquals(5, dr.size());
       assertTrue(dr.iterator().hasNext());
        assertNotNull(dr.iterator().next());
       UserOverview m = dr.iterator().next();
       assertNotNull(m.getUserLogin());
   }

    @Test
   public void testLookupServerPreferenceValue() {
       User user = UserTestUtils.createUser();

       Server s = ServerFactoryTest.createTestServer(user, true,
               ServerConstants.getServerGroupTypeEnterpriseEntitled());


       assertTrue(UserManager.lookupUserServerPreferenceValue(user,
                                                              s,
                                                              UserServerPreferenceId
                                                              .RECEIVE_NOTIFICATIONS));

       UserServerPreference usp = new UserServerPreference(user, s, UserServerPreferenceId.RECEIVE_NOTIFICATIONS);
       usp.setValue("0");

       usp = TestUtils.saveAndFlush(usp);

       assertFalse(UserManager.lookupUserServerPreferenceValue(user,
                                                               s,
                                                               UserServerPreferenceId
                                                               .RECEIVE_NOTIFICATIONS));
   }

    @Test
   public void testVisibleSystemsAsDtoFromList() {
       User user = UserTestUtils.createUser();
       Server s = ServerFactoryTest.createTestServer(user, true,
               ServerConstants.getServerGroupTypeEnterpriseEntitled());
       List<Long> ids = new ArrayList<>();
       ids.add(s.getId());
       List<SystemSearchResult> dr =
           UserManager.visibleSystemsAsDtoFromList(user, ids);
       assertTrue(!dr.isEmpty());
   }

    @Test
   public void testSystemSearchResults() {
       User user = UserTestUtils.createUser();
       Server s = ServerFactoryTest.createTestServer(user, true,
               ServerConstants.getServerGroupTypeEnterpriseEntitled());
       NetworkInterface lo =
               NetworkInterfaceTest.createTestNetworkInterface(s, "lo", "127.0.0.1", null);
       s.addNetworkInterface(lo);
       s.setPrimaryInterface(lo);
       s.setDescription("Test Description Value");
       List<Long> ids = new ArrayList<>();
       ids.add(s.getId());
       DataResult<SystemSearchResult> dr = UserManager.visibleSystemsAsDtoFromList(user, ids);
       assertNotNull(dr);
       assertFalse(dr.isEmpty());
       dr.elaborate(Collections.emptyMap());
       SystemSearchResult sr = dr.get(0);
       assertNotNull(sr.getDescription());
   }
}
