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

package com.redhat.rhn.manager.user.test;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Set;

import com.redhat.rhn.common.ObjectCreateWrapperException;
import com.redhat.rhn.common.db.datasource.DataResult;
import com.redhat.rhn.common.hibernate.HibernateFactory;
import com.redhat.rhn.common.hibernate.LookupException;
import com.redhat.rhn.common.security.PermissionException;
import com.redhat.rhn.common.security.user.StateChangeException;
import com.redhat.rhn.domain.org.Org;
import com.redhat.rhn.domain.org.OrgFactory;
import com.redhat.rhn.domain.rhnpackage.Package;
import com.redhat.rhn.domain.rhnpackage.test.PackageTest;
import com.redhat.rhn.domain.rhnset.RhnSet;
import com.redhat.rhn.domain.rhnset.SetCleanup;
import com.redhat.rhn.domain.role.Role;
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

/** JUnit test case for the User
 *  class.
 */
public class UserManagerTest extends RhnBaseTestCase {

    private Set<User> users;
    private boolean committed = false;

    /**
     * {@inheritDoc}
     */
    @Override
    public void setUp() throws Exception {
        super.setUp();
        this.users = new HashSet<User>();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void tearDown() throws Exception {
        super.tearDown();

        // If at some point we created an org and committed the transaction, we must erase it from the database
        if (this.committed) {
            users.stream().forEach(user -> OrgFactory.deleteOrg(user.getOrg().getId(), user));
           commitAndCloseSession();
        }
        this.committed = false;
        this.users = null;
    }

    // If we have to commit in mid-test, set up the next transaction correctly
    protected void commitHappened() {
        committed = true;
    }

    public void testGrantServerGroupPermission() throws Exception {
        //Group and user have the same org, so should be possible to grant permits
        User user = UserTestUtils.findNewUser("user_1", "org_1");
        this.users.add(user);

        Server server = ServerFactoryTest.createTestServer(user, false,
                ServerConstants.getServerGroupTypeEnterpriseEntitled());

        ServerGroup group = ServerGroupTest
                .createTestServerGroup(user.getOrg(), null);

        SystemManager.addServerToServerGroup(server, group);
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

        Set userGroups = foundUser.getAssociatedServerGroups();
        assertNotNull(userGroups);
        assertTrue(userGroups.isEmpty());

        UserManager.grantServerGroupPermission(foundUser, group.getId());
        HibernateFactory.commitTransaction();
        commitHappened();

        HibernateFactory.getSession().clear();

        foundUser = UserFactory.lookupById(foundUser.getId());
        userGroups = foundUser.getAssociatedServerGroups();
        assertNotNull(userGroups);
        assertEquals(userGroups.size(), 1);
        assertTrue(userGroups.stream().allMatch(g -> ((ServerGroup) g).getId().equals(group.getId())));

        //Group and user have different orgs, so should not be possible to grant permits
        User user2 = UserTestUtils.findNewUser("user_2", "org_2");
        this.users.add(user);

        User foundUser2 = UserFactory.lookupById(user2.getId());
        assertEquals(foundUser2.getId(), user2.getId());
        assertFalse(foundUser2.getOrg().getId().equals(group.getOrg().getId()));

        Set userGroups2 = foundUser2.getAssociatedServerGroups();
        assertNotNull(userGroups2);
        assertTrue(userGroups2.isEmpty());

        UserManager.grantServerGroupPermission(foundUser2, group.getId());
        HibernateFactory.commitTransaction();
        commitHappened();

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

        SystemManager.addServerToServerGroup(server2, group2);
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
        HibernateFactory.commitTransaction();
        commitHappened();

        HibernateFactory.getSession().clear();

        foundUser2 = UserFactory.lookupById(user2.getId());
        userGroups2 = foundUser2.getAssociatedServerGroups();
        assertNotNull(userGroups2);
        assertTrue(userGroups2.isEmpty());

    }

    public void testRevokeServerGroupPermission() throws Exception {
        User user = UserTestUtils.findNewUser("user_test_revoke", "org_test_revoke");
        this.users.add(user);

        Server server = ServerFactoryTest.createTestServer(user, false,
                ServerConstants.getServerGroupTypeEnterpriseEntitled());

        ServerGroup group = ServerGroupTest
                .createTestServerGroup(user.getOrg(), null);

        SystemManager.addServerToServerGroup(server, group);
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

        Set userGroups = foundUser.getAssociatedServerGroups();
        assertNotNull(userGroups);
        assertTrue(userGroups.isEmpty());

        UserManager.grantServerGroupPermission(foundUser, group.getId());
        HibernateFactory.commitTransaction();
        commitHappened();

        HibernateFactory.getSession().clear();

        foundUser = UserFactory.lookupById(foundUser.getId());
        userGroups = foundUser.getAssociatedServerGroups();
        assertNotNull(userGroups);
        assertEquals(userGroups.size(), 1);
        assertTrue(userGroups.stream().allMatch(g -> ((ServerGroup) g).getId().equals(group.getId())));

        UserManager.revokeServerGroupPermission(foundUser, group.getId());
        HibernateFactory.commitTransaction();
        commitHappened();

        HibernateFactory.getSession().clear();

        foundUser = UserFactory.lookupById(foundUser.getId());
        userGroups = foundUser.getAssociatedServerGroups();
        assertNotNull(userGroups);
        assertTrue(userGroups.isEmpty());
    }

    public void testListRolesAssignable() throws Exception {
        User user = UserTestUtils.findNewUser();
        assertTrue(UserManager.listRolesAssignableBy(user).isEmpty());
        user.addPermanentRole(RoleFactory.ORG_ADMIN);
        UserManager.storeUser(user);
        assertTrue(UserManager.listRolesAssignableBy(user).
                                contains(RoleFactory.CONFIG_ADMIN));
        assertFalse(UserManager.listRolesAssignableBy(user).
                contains(RoleFactory.SAT_ADMIN));

        User sat = UserTestUtils.createSatAdminInOrgOne();
        assertTrue(UserManager.listRolesAssignableBy(sat).
                contains(RoleFactory.SAT_ADMIN));


    }

    public void testVerifyPackageAccess() throws Exception {
        User user = UserTestUtils.findNewUser("testuser", "testorg");
        Package pkg = PackageTest.createTestPackage(user.getOrg());
        assertTrue(UserManager.verifyPackageAccess(user.getOrg(), pkg.getId()));

        // Since we have only one org on a sat, all custom created packages will be
        // available to all users in that org.
    }

    public void testLookup() {
        User admin = UserTestUtils.findNewUser("testUser",
                "testOrg" + this.getClass().getSimpleName());
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
            test = UserManager.lookupUser(regular, admin.getId());
            fail();
        }
        catch (PermissionException e) {
            //success
        }

        try {
            test = UserManager.lookupUser(regular, admin.getLogin());
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

    public void testUserDisableEnable() {
        //Create test users
        User org1admin = UserTestUtils.createUser("orgAdmin1",
                                            UserTestUtils.createOrg("UMTOrg1"));
        org1admin.addPermanentRole(RoleFactory.ORG_ADMIN);
        UserManager.storeUser(org1admin);

        User org1admin2 = UserTestUtils.createUser("orgAdmin2",
                                                  org1admin.getOrg().getId());
        org1admin2.addPermanentRole(RoleFactory.ORG_ADMIN);
        UserManager.storeUser(org1admin2);

        User org1normal = UserTestUtils.createUser("normaluser1",
                                                    org1admin.getOrg().getId());
        User org1normal2 = UserTestUtils.createUser("normaluser2",
                                                    org1admin.getOrg().getId());

        User org2admin = UserTestUtils.createUser("orgAdmin2",
                                             UserTestUtils.createOrg("UMTOrg2"));
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
        User org2normal = UserTestUtils.createUser("normaluser2",
                                                   org2admin.getOrg().getId());

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

    /**
    * Test to ensure functionality of translating
    * usergroup ids to Roles
     * @throws Exception something bad happened
    */
    public void aTestUpdateUserRolesFromRoleLabels() throws Exception {
        User usr = UserTestUtils.findNewUser("testUser",
                "testOrg" + this.getClass().getSimpleName());
        ServerFactoryTest.createTestServer(usr);

        Org o1 = usr.getOrg();
        Set<Role> oRoles = o1.getRoles();
        List<String> roleLabels = new LinkedList<String>();
        // We know that all newly created Orgs have the ORG_ADMIN
        // so if we add all the UserGroup IDs to the list then
        // the User should have the ORG_ADMIN assigned to it.
        for (Role role : oRoles) {
            roleLabels.add(role.getLabel());
        }
        UserManager.addRemoveUserRoles(usr, roleLabels, new LinkedList<String>());
        UserManager.storeUser(usr);

        UserTestUtils.assertOrgAdmin(usr);

        // Make sure we can take roles away from ourselves:
        int numRoles = usr.getRoles().size();
        List<String> removeRoles = new LinkedList<String>();
        removeRoles.add(RoleFactory.ORG_ADMIN.getLabel());
        UserManager.addRemoveUserRoles(usr, new LinkedList<String>(),
                removeRoles);
        UserManager.storeUser(usr);
        assertEquals(numRoles - 1, usr.getRoles().size());

        // Test that taking away org admin properly removes
        // permissions for the user (bz156752). Note that calling
        // UserManager.storeUser is absolutely vital for this to work
        UserTestUtils.assertNotOrgAdmin(usr);
    }

    public void testUsersInOrg() {
        int numTotal = 1;
        int numDisabled = 0;
        int numActive = 1;
        User user = UserTestUtils.findNewUser("testUser",
                "testOrg" + this.getClass().getSimpleName());
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

        DataResult users = UserManager.usersInOrg(user, pc);
        assertNotNull(users);
        assertEquals(numTotal + 1, users.getTotalSize());

        users = UserManager.activeInOrg(user, pc);
        assertNotNull(users);
        assertEquals(numActive + 1, users.getTotalSize());

        users = UserManager.disabledInOrg(user, pc);
        assertNotNull(users);
        assertEquals(numDisabled, users.getTotalSize());

        UserFactory.getInstance().disable(peon, user);

        users = UserManager.usersInOrg(user, pc);
        assertNotNull(users);
        assertEquals(numTotal + 1, users.getTotalSize());

        users = UserManager.activeInOrg(user, pc);
        assertNotNull(users);
        assertEquals(numActive, users.getTotalSize());

        users = UserManager.disabledInOrg(user, pc);
        assertNotNull(users);
        assertEquals(numDisabled + 1, users.getTotalSize());
    }


    public void testLookupUserOrgBoundaries() {
        User usr1 = UserTestUtils.findNewUser("testUser", "testOrg1", true);
        User usr2 = UserTestUtils.findNewUser("testUser", "testOrg2");
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
    public void testStoreUser() {
        User usr = UserTestUtils.findNewUser("testUser",
                "testOrg" + this.getClass().getSimpleName());
        Long id = usr.getId();
        usr.setEmail("something@changed.redhat.com");
        UserManager.storeUser(usr);
        User u2 = UserFactory.lookupById(id);
        assertEquals("something@changed.redhat.com", u2.getEmail());
    }

    public void testGetSystemGroups() {
        User usr = UserTestUtils.findNewUser("testUser",
                "testOrg" + this.getClass().getSimpleName());
        PageControl pc = new PageControl();
        pc.setIndexData(false);
        pc.setFilterColumn("name");
        pc.setStart(1);
        assertNotNull(UserManager.getSystemGroups(usr, pc));
    }

    public void testGetTimeZoneId() {
        RhnTimeZone tz = UserManager.getTimeZone(UserManager
                .getTimeZone("Indian/Maldives").getTimeZoneId());
        assertEquals(UserManager.getTimeZone("Indian/Maldives"), tz);
        assertEquals("Indian/Maldives", tz.getOlsonName());

        RhnTimeZone tz2 = UserManager.getTimeZone(-23);
        assertNull(tz2);
    }

    public void testGetTimeZoneOlson() {
        RhnTimeZone tz = UserManager.getTimeZone("America/New_York");
        assertNotNull(tz);
        assertEquals(tz.getOlsonName(), "America/New_York");

        RhnTimeZone tz2 = UserManager.getTimeZone("foo");
        assertNull(tz2);
    }

    public void testGetTimeZoneDefault() {
        RhnTimeZone tz = UserManager.getDefaultTimeZone();
        assertNotNull(tz);
        assertEquals(tz.getTimeZone().getRawOffset(), UserFactory.getDefaultTimeZone()
                .getTimeZone().getRawOffset());
    }

    public void testLookupTimeZoneAll() {
        List<RhnTimeZone> lst = UserManager.lookupAllTimeZones();
        assertTrue(lst.size() > 30);
        assertTrue(lst.get(5) instanceof RhnTimeZone);
        assertTrue(lst.get(29) instanceof RhnTimeZone);

        assertEquals(UserManager.getTimeZone("GMT"), lst.get(0));
        assertEquals("GMT", lst.get(0).getOlsonName());
        assertEquals("America/Sao_Paulo", lst.get(5).getOlsonName());
        assertEquals(UserManager.getTimeZone("America/Sao_Paulo"), lst.get(5));

        assertEquals("Europe/Paris", lst.get(lst.size() - 1).getOlsonName());
        assertEquals(UserManager.getTimeZone("Europe/Paris"), lst.get(lst.size() - 1));
    }

   public void testUsersInSet() throws Exception {
       User user = UserTestUtils.findNewUser("testUser",
               "testOrg" + this.getClass().getSimpleName());
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
       DataResult dr = UserManager.usersInSet(user, "test_user_list", pc);

       assertEquals(5, dr.size());
       assertTrue(dr.iterator().hasNext());
       assertTrue(dr.iterator().next() instanceof UserOverview);
       UserOverview m = (UserOverview)(dr.iterator().next());
       assertNotNull(m.getUserLogin());
   }

   public void testLookupServerPreferenceValue() throws Exception {
       User user = UserTestUtils.findNewUser(TestStatics.TESTUSER,
               TestStatics.TESTORG);

       Server s = ServerFactoryTest.createTestServer(user, true,
               ServerConstants.getServerGroupTypeEnterpriseEntitled());


       assertTrue(UserManager.lookupUserServerPreferenceValue(user,
                                                              s,
                                                              UserServerPreferenceId
                                                              .RECEIVE_NOTIFICATIONS));

       UserServerPreferenceId id = new UserServerPreferenceId(user,
                                       s,
                                       UserServerPreferenceId
                                       .RECEIVE_NOTIFICATIONS);

       UserServerPreference usp = new UserServerPreference();
       usp.setId(id);
       usp.setValue("0");

       TestUtils.saveAndFlush(usp);

       assertFalse(UserManager.lookupUserServerPreferenceValue(user,
                                                               s,
                                                               UserServerPreferenceId
                                                               .RECEIVE_NOTIFICATIONS));
   }

   public void testVisibleSystemsAsDtoFromList() throws Exception {
       User user = UserTestUtils.findNewUser(TestStatics.TESTUSER,
               TestStatics.TESTORG);

       Server s = ServerFactoryTest.createTestServer(user, true,
               ServerConstants.getServerGroupTypeEnterpriseEntitled());
       List<Long> ids = new ArrayList<Long>();
       ids.add(s.getId());
       List<SystemSearchResult> dr =
           UserManager.visibleSystemsAsDtoFromList(user, ids);
       assertTrue(dr.size() >= 1);
   }

   public void testSystemSearchResults() throws Exception {
       User user = UserTestUtils.findNewUser(TestStatics.TESTUSER,
               TestStatics.TESTORG);

       Server s = ServerFactoryTest.createTestServer(user, true,
               ServerConstants.getServerGroupTypeEnterpriseEntitled());
       NetworkInterface lo =
               NetworkInterfaceTest.createTestNetworkInterface(s, "lo", "127.0.0.1", null);
       s.addNetworkInterface(lo);
       s.setPrimaryInterface(lo);
       s.setDescription("Test Description Value");
       List<Long> ids = new ArrayList<Long>();
       ids.add(s.getId());
       DataResult<SystemSearchResult> dr =
           UserManager.visibleSystemsAsDtoFromList(user, ids);
       assertTrue(dr.size() >= 1);
       dr.elaborate(Collections.EMPTY_MAP);
       SystemSearchResult sr = dr.get(0);
       assertNotNull(sr.getDescription());
   }
}
