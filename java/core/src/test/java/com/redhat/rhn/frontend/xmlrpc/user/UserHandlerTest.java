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
package com.redhat.rhn.frontend.xmlrpc.user;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.junit.jupiter.api.Assertions.fail;

import com.redhat.rhn.FaultException;
import com.redhat.rhn.common.hibernate.LookupException;
import com.redhat.rhn.domain.role.Role;
import com.redhat.rhn.domain.role.RoleFactory;
import com.redhat.rhn.domain.server.ServerGroup;
import com.redhat.rhn.domain.user.User;
import com.redhat.rhn.domain.user.UserFactory;
import com.redhat.rhn.frontend.action.common.BadParameterException;
import com.redhat.rhn.frontend.xmlrpc.BaseHandlerTestCase;
import com.redhat.rhn.frontend.xmlrpc.InvalidParameterException;
import com.redhat.rhn.frontend.xmlrpc.InvalidServerGroupException;
import com.redhat.rhn.frontend.xmlrpc.NoSuchRoleException;
import com.redhat.rhn.frontend.xmlrpc.NoSuchUserException;
import com.redhat.rhn.frontend.xmlrpc.PermissionCheckFailureException;
import com.redhat.rhn.manager.access.AccessGroupManager;
import com.redhat.rhn.manager.system.ServerGroupManager;
import com.redhat.rhn.testing.ServerGroupTestUtils;
import com.redhat.rhn.testing.TestUtils;
import com.redhat.rhn.testing.UserTestUtils;

import com.suse.manager.webui.services.TestSaltApi;

import org.junit.jupiter.api.Test;

import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Set;

public class UserHandlerTest extends BaseHandlerTestCase {

    private UserHandler handler = new UserHandler(new ServerGroupManager(new TestSaltApi()), new AccessGroupManager());

    @Test
    public void testListUsers() {
        //admin should be able to call list users, regular should not
        List result = handler.listUsers(admin);
        assertNotNull(result);

        //make sure we get a permission exception if a regular user tries to get the user
        //list.
        try {
            handler.listUsers(regular);
            fail();
        }
        catch (PermissionCheckFailureException e) {
            //success
        }
    }

    @Test
    public void testListRoles() {
        Set<String> result = handler.listRoles(admin, regular.getLogin());
        assertEquals(0, result.size());

        result = handler.listRoles(admin, admin.getLogin());
        assertEquals(7, result.size());
    }

    @Test
    public void testListAssignableRoles() {
        assertTrue(handler.listAssignableRoles(admin).contains(RoleFactory.ORG_ADMIN.getLabel()));
        assertTrue(handler.listAssignableRoles(regular).isEmpty());
        assertTrue(handler.listAssignableRoles(satAdmin).contains(RoleFactory.SAT_ADMIN.getLabel()));

    }

    @Test
    public void testGetDetails() {
        //admin looking up self
        Map result = handler.getDetails(admin, admin.getLogin());
        assertEquals(admin.getFirstNames(), result.get("first_name"));
        assertEquals(admin.getFirstNames(), result.get("first_names"));

        //admin looking up regular
        result = handler.getDetails(admin, regular.getLogin());
        assertEquals(regular.getFirstNames(), result.get("first_name"));
        assertEquals(regular.getFirstNames(), result.get("first_names"));

        //regular looking up self
        result = handler.getDetails(regular, regular.getLogin());
        assertEquals(regular.getFirstNames(), result.get("first_name"));
        assertEquals(regular.getFirstNames(), result.get("first_names"));

        //regular looking up admin
        try {
            handler.getDetails(regular, admin.getLogin());
            fail();
        }
        catch (PermissionCheckFailureException e) {
            //Success
        }
    }

    @Test
    public void testPasswordViaSetDetails() {
        Map<String, String> details = new HashMap<>();
        details.put("password", "");

        try {
            handler.setDetails(admin, admin.getLogin(), details);
            fail("invalid password should've caused exception");
        }
        catch (FaultException expected) {
            // expected
        }
    }

    @Test
    public void testSetDetails() {

        Map<String, String> newDetails = new HashMap<>();
        newDetails.put("first_name", "firstnames_edited");

        //admin editing self
        int result = handler.setDetails(admin, admin.getLogin(), newDetails);
        assertEquals(1, result);
        assertEquals(newDetails.get("first_name"), admin.getFirstNames());

        //admin editing regular
        result = handler.setDetails(admin, regular.getLogin(), newDetails);
        assertEquals(1, result);
        assertEquals(newDetails.get("first_name"), regular.getFirstNames());

        //regular editing admin
        try {
            handler.setDetails(regular, admin.getLogin(), newDetails);
            fail();
        }
        catch (PermissionCheckFailureException e) {
            //success
        }

        //regular editing self
        result = handler.setDetails(regular, regular.getLogin(), newDetails);
        assertEquals(1, result);
        assertEquals(newDetails.get("first_name"), regular.getFirstNames());
    }

    @Test
    public void testAddRemoveRole() throws Exception {
        Set<Role> roles = regular.getRoles();
        assertEquals(5, roles.size());

        //Add org_admin to regular user
        handler.addRole(admin, regular.getLogin(), "org_admin");

        roles = regular.getRoles();
        assertFalse(roles.isEmpty());

        //Remove org_admin from regular user
        handler.removeRole(admin, regular.getLogin(), "org_admin");

        roles = regular.getRoles();
        assertEquals(5, roles.size());

        //make sure regular user can't edit roles
        try {
            handler.removeRole(regular, admin.getLogin(), "org_admin");
            fail();
        }
        catch (FaultException e) {
            //succcess
        }

        try {
            handler.addRole(regular, regular.getLogin(), "org_admin");
            fail();
        }
        catch (FaultException e) {
            //success
        }

        try {
            handler.addRole(admin, regular.getLogin(), "badrole");
            fail("passed in a bad role this is very bad");
        }
        catch (NoSuchRoleException e) {
            // Cool No such role exception
        }

        try {
            handler.addRole(admin, regular.getLogin(),
                    RoleFactory.SAT_ADMIN.getLabel());
            fail();
        }
        catch (FaultException e) {
            //success
        }
        handler.addRole(satAdmin, regular.getLogin(), RoleFactory.SAT_ADMIN.getLabel());
        assertTrue(regular.hasRole(RoleFactory.SAT_ADMIN));


    }

    @Test
    public void testUsePamAuthentication() {
        Integer one = 1;
        Integer zero = 0;

        regular.setUsePamAuthentication(false); //start off false
        handler.usePamAuthentication(admin, regular.getLogin(), one);
        assertTrue(regular.getUsePamAuthentication());
        handler.usePamAuthentication(admin, regular.getLogin(), zero);
        assertFalse(regular.getUsePamAuthentication());

        //make sure regular users can't call this method
        try {
            handler.usePamAuthentication(regular, regular.getLogin(), zero);
            fail();
        }
        catch (PermissionCheckFailureException e) {
            //success
        }
    }

    private void invalidUsername(String login) throws FaultException {
        try {
            handler.create(admin, login, "password", "Bill",
                    "And Ted", "iron.maiden@rufus.com");
            fail();
        }
        catch (InvalidParameterException e) {
            // success
        }
    }

    private void validUsername(String login) throws FaultException {
        try {
            int rc = handler.create(admin, login, "password", "Bill",
                    "And Ted", "iron.maiden@rufus.com");
            assertEquals(1, rc);
        }
        catch (BadParameterException e) {
            fail(login + " cause an error");
        }
    }

    @Test
    public void testCreateWithManyUsernames() {
        // We only need to run this test on satellite

        invalidUsername("foo&user");
        invalidUsername("joe+page");
        invalidUsername("joe user");
        invalidUsername("10%users");
        invalidUsername("joe'suser");
        invalidUsername("`eval`");
        invalidUsername("joe=page");
        invalidUsername("foo#user");
        invalidUsername("joe\"user");
        invalidUsername("機能拡張を");
        invalidUsername("shughes login");
        invalidUsername("shughes%login");
        invalidUsername(" shughes");
        invalidUsername("a p&i+u%s'e r1150586011843"); // bug195807

        validUsername("john.cusack@foobar.com");
        validUsername("a$user");
        validUsername("!@$^*()-_{}[]|\\:;?");
        validUsername("/usr/bin/ls");
        validUsername("shughes_login");
        validUsername("shughes@redhat.com");
        validUsername("/shughes_login");
        validUsername("/\\/\\ark");
    }

    @Test
    public void testCreateDelete() {
        // We only need to run this test on satellite

        String login = "testCreateDelete" + TestUtils.randomString();
        String email = "java-xmlrpc-tests@redhat.com";
        String firstName = "Chuck";
        String lastName = "Norris";
        String invalidPassword = "p";
        String validPassword = "password";


        try {
            handler.create(admin, login, invalidPassword, firstName, lastName, email);
            fail();
        }
        catch (InvalidParameterException e) {
            //success
        }

        try {
            handler.create(regular, login, validPassword, firstName, lastName, email);
            fail();
        }
        catch (PermissionCheckFailureException e) {
            //success
        }

        int result = handler.create(admin, login, validPassword, firstName,
                                    lastName, email);
        assertEquals(1, result);

        User created = UserFactory.lookupByLogin(login);
        assertNotNull(created);

        try {
            handler.delete(regular, login);
            fail();
        }
        catch (PermissionCheckFailureException e) {
            //success
        }

        handler.delete(admin, login);
        try {
            UserFactory.lookupByLogin(login);
            fail("Deleted User Exists!");
        }
        catch (LookupException le) {
         //cool deleted user is gone!
        }

    }

    @Test
    public void testDisableEnable() {
        // We only need to run this test on satellite

        //Test that org admin can disable/enable normal user
        assertFalse(regular.isDisabled());
        handler.disable(admin, regular.getLogin());
        assertTrue(regular.isDisabled());
        handler.enable(admin, regular.getLogin());
        assertFalse(regular.isDisabled());

        //Make sure regular user can't disable/enable the admin
        assertFalse(admin.isDisabled());
        try {
            handler.disable(regular, admin.getLogin());
            fail();
        }
        catch (PermissionCheckFailureException e) {
            //success
        }
    }

    @Test
    public void testPrefixes() {
        Map<String, String> details = new HashMap<>();
        details.put("prefix", "");

        try {
            handler.setDetails(admin, admin.getLogin(), details);
            fail("invalid prefix should've caused exception");
        }
        catch (FaultException expected) {
            // expected
        }

        details.put("prefix", "Miss");
        try {
            assertEquals(1,
                    handler.setDetails(admin, admin.getLogin(), details));

        }
        catch (FaultException expected) {
            fail("valid prefix should not have caused exception");
        }
    }

    @Test
    public void testCreateUsingPamAuth() throws FaultException {
        // We only need to run this test on satellite

        String login = "testCreateDelete" + TestUtils.randomString();
        String email = "java-xmlrpc-tests@redhat.com";
        String firstName = "Chuck";
        String lastName = "Norris";
        Integer usePamAuth = 1;
        Integer noPamAuth = 0;

        // pass in empty password
        handler.create(admin, login, "", firstName, lastName, email, usePamAuth);

        login = "testCreateDelete" + TestUtils.randomString();
        // pass in empty password
        try {
            handler.create(admin, login, "", firstName, lastName, email, noPamAuth);
            fail("empty password and not using pam auth should cause an error");
        }
        catch (FaultException expected) {
            // expected
        }
    }

    @Test
    public void testAddDefaultSystemGroup() {
        ServerGroupTestUtils.createManaged(regular);
        Object[] groups = handler.listAssignedSystemGroups(
                regular, regular.getLogin());
        assertTrue(groups.length > 0);

        Object[] defGrps = handler.listDefaultSystemGroups(
                regular, regular.getLogin());
        assertEquals(0, defGrps.length);

        assertEquals(1, handler.addDefaultSystemGroup(regular,
                regular.getLogin(), ((ServerGroup)groups[0]).getName()));

        defGrps = handler.listDefaultSystemGroups(
                regular, regular.getLogin());
        assertEquals(1, defGrps.length);
    }

    @Test
    public void testAddDefaultSystemGroupsEmpty() throws FaultException {

        // pass in null
        try {
            handler.addDefaultSystemGroups(
                    admin, admin.getLogin(), null);
            fail("null should be invalid");
        }
        catch (InvalidParameterException iae) {
            // expected exception
        }

        // pass in empty array
        try {
            handler.addDefaultSystemGroups(
                    admin, admin.getLogin(), new LinkedList<>());
            fail("empty array should be invalid");
        }
        catch (InvalidParameterException iae) {
            // expected exception
        }
    }

    @Test
    public void testAddDefaultSystemGroupWithInvalidParams() {
        try {
            handler.addDefaultSystemGroup(admin, admin.getLogin(),
                    "IntentionalBadValue--" + System.currentTimeMillis());
            fail("bad name passed in, should throw exception");
        }
        catch (FaultException e) {
            // expected exception
        }
    }

    @Test
    public void testAddDefaultSystemGroups() {
        ServerGroupTestUtils.createManaged(regular);
        ServerGroupTestUtils.createManaged(regular);

        Object[] groups = handler.listAssignedSystemGroups(
                regular, regular.getLogin());
        assertTrue(groups.length > 0);


        Object[] defGrps = handler.listDefaultSystemGroups(
                regular, regular.getLogin());
        assertEquals(0, defGrps.length);

        List<String> names = new LinkedList<>();
        names.add(((ServerGroup)groups[0]).getName());
        names.add(((ServerGroup)groups[1]).getName());
        assertEquals(1, handler.addDefaultSystemGroups(regular,
                regular.getLogin(), names));

        defGrps = handler.listDefaultSystemGroups(
                regular, regular.getLogin());
        assertEquals(2, defGrps.length);
    }

    @Test
    public void testListAssignedSystemGroups() {
        ServerGroupTestUtils.createManaged(admin);
        Object[] groups = handler.listAssignedSystemGroups(
                admin, admin.getLogin());
        assertNotNull(groups);
        assertTrue(groups.length > 0);
    }

    @Test
    public void testListDefaultSystemGroups() {
        ServerGroupTestUtils.createManaged(admin);
        Object[] groups = handler.listDefaultSystemGroups(
                admin, admin.getLogin());
        assertNotNull(groups);
        assertFalse(groups.length > 0);
    }

    @Test
    public void testAddAssignedSystemGroups() {
        ServerGroup sg1 = ServerGroupTestUtils.createManaged(admin);
        ServerGroup sg2 = ServerGroupTestUtils.createManaged(admin);

        Object [] groups = handler.listAssignedSystemGroups(admin,
                regular.getLogin());
        assertEquals(0, groups.length);

        List<String> names = new LinkedList<>();
        names.add(sg1.getName());
        names.add(sg2.getName());
        handler.addAssignedSystemGroups(admin, regular.getLogin(), names,
                Boolean.FALSE);

        groups = handler.listAssignedSystemGroups(admin,
                regular.getLogin());
        assertEquals(2, groups.length);
    }

    @Test
    public void testAddAssignedSystemGroupsWithInvalidGroup() {
        ServerGroup sg1 = ServerGroupTestUtils.createManaged(admin);
        ServerGroup sg2 = ServerGroupTestUtils.createManaged(admin);

        Object [] groups = handler.listAssignedSystemGroups(admin,
                regular.getLogin());
        assertEquals(0, groups.length);

        List<String> names = new LinkedList<>();
        names.add(sg1.getName());
        names.add(sg2.getName());
        names.add("notarealgroup");
        try {
            handler.addAssignedSystemGroups(admin, regular.getLogin(), names,
                    Boolean.FALSE);
            fail();
        }
        catch (InvalidServerGroupException e) {
            // expected
        }

        groups = handler.listAssignedSystemGroups(admin,
                regular.getLogin());
        assertEquals(0, groups.length);
    }

    @Test
    public void testAddAssignedSystemGroupsAndSetDefault() {
        ServerGroup sg1 = ServerGroupTestUtils.createManaged(admin);
        ServerGroup sg2 = ServerGroupTestUtils.createManaged(admin);
        ServerGroup sg3 = ServerGroupTestUtils.createManaged(admin);

        Object [] groups = handler.listAssignedSystemGroups(admin,
                regular.getLogin());
        assertEquals(0, groups.length);
        Object [] defaults = handler.listDefaultSystemGroups(admin,
                regular.getLogin());
        assertEquals(0, defaults.length);

        List<String> names = new LinkedList<>();
        names.add(sg1.getName());
        handler.addAssignedSystemGroups(admin, regular.getLogin(), names,
                Boolean.FALSE);

        groups = handler.listAssignedSystemGroups(admin,
                regular.getLogin());
        assertEquals(1, groups.length);
        defaults = handler.listDefaultSystemGroups(admin,
                regular.getLogin());
        assertEquals(0, defaults.length);

        names.clear();
        names.add(sg2.getName());
        names.add(sg3.getName());
        handler.addAssignedSystemGroups(admin, regular.getLogin(), names,
                Boolean.TRUE);

        groups = handler.listAssignedSystemGroups(admin,
                regular.getLogin());
        assertEquals(3, groups.length);
        defaults = handler.listDefaultSystemGroups(admin,
                regular.getLogin());
        assertEquals(2, defaults.length);

    }

    @Test
    public void testAddAssignedSystemGroup() {
        ServerGroup sg1 = ServerGroupTestUtils.createManaged(admin);

        Object [] groups = handler.listAssignedSystemGroups(admin,
                regular.getLogin());
        assertEquals(0, groups.length);

        handler.addAssignedSystemGroup(admin, regular.getLogin(),
            sg1.getName(), Boolean.FALSE);

        groups = handler.listAssignedSystemGroups(admin,
                regular.getLogin());
        assertEquals(1, groups.length);
    }

    @Test
    public void testAddAssignedSystemGroupUserAlreadyHas() {
        ServerGroup sg1 = ServerGroupTestUtils.createManaged(admin);

        handler.addAssignedSystemGroup(admin, regular.getLogin(),
            sg1.getName(), Boolean.FALSE);

        Object [] groups = handler.listAssignedSystemGroups(admin,
            regular.getLogin());
        assertEquals(1, groups.length);

        // Should just be a no-op.
        handler.addAssignedSystemGroup(admin, admin.getLogin(),
            sg1.getName(), Boolean.FALSE);

        groups = handler.listAssignedSystemGroups(admin,
            regular.getLogin());
        assertEquals(1, groups.length);

    }

    @Test
    public void testAddAssignedSystemGroupNoSuchUser() {
        ServerGroup sg1 = ServerGroupTestUtils.createManaged(admin);

        try {
            handler.addAssignedSystemGroup(admin, "notareallogin",
                sg1.getName(), Boolean.FALSE);
            fail();
        }
        catch (NoSuchUserException e) {
            // expected
        }
    }

    @Test
    public void testAddAssignedSystemGroupNoSuchGroup() {
        try {
            handler.addAssignedSystemGroup(admin, regular.getLogin(),
                "asdfadfawevxcttewfsafsd", Boolean.FALSE);
            fail();
        }
        catch (InvalidServerGroupException e) {
            // expected
        }
    }

    @Test
    public void testRemoveDefaultSystemGroup() {
        ServerGroupTestUtils.createManaged(regular);
        Object[] groups = handler.listAssignedSystemGroups(
                regular, regular.getLogin());
        assertTrue(groups.length > 0);

        Object[] defGrps = handler.listDefaultSystemGroups(
                regular, regular.getLogin());
        assertEquals(0, defGrps.length);

        assertEquals(1, handler.addDefaultSystemGroup(regular,
                regular.getLogin(), ((ServerGroup)groups[0]).getName()));

        defGrps = handler.listDefaultSystemGroups(
                regular, regular.getLogin());
        assertEquals(1, defGrps.length);

        assertEquals(1, handler.removeDefaultSystemGroup(
                admin, regular.getLogin(), ((ServerGroup)groups[0]).getName()));
    }

    @Test
    public void testRemoveDefaultSystemGroupWithInvalidParams() {
        try {
            handler.removeDefaultSystemGroup(admin, admin.getLogin(),
                    "IntentionalBadValue--" + System.currentTimeMillis());
            fail("bad name passed in, should throw exception");
        }
        catch (FaultException e) {
            // expected exception
        }
    }
    @Test
    public void testRemoveDefaultSystemGroupsEmpty() throws FaultException {

        // pass in null
        try {
            handler.removeDefaultSystemGroups(
                    admin, admin.getLogin(), null);
            fail("null should be invalid");
        }
        catch (InvalidParameterException iae) {
            // expected exception
        }

        // pass in empty array
        try {
            handler.removeDefaultSystemGroups(
                    admin, admin.getLogin(), new LinkedList<>());
            fail("empty array should be invalid");
        }
        catch (InvalidParameterException iae) {
            // expected exception
        }
    }

    @Test
    public void testRemoveDefaultSystemGroups() {

        // see if we have any default system groups first
        Object[] defGrps = handler.listDefaultSystemGroups(
                admin, admin.getLogin());
        int defLen = defGrps.length;

        if (defLen < 1) {
            // there are none, so let's add one
            ServerGroupTestUtils.createManaged(admin);
            Object[] groups = handler.listAssignedSystemGroups(
                    admin, admin.getLogin());
            assertTrue(groups.length > 0);
            assertEquals(1, handler.addDefaultSystemGroup(admin,
                admin.getLogin(), ((ServerGroup)groups[0]).getName()));

            // make sure we reload the list with the newly added default
            defGrps = handler.listDefaultSystemGroups(
                    admin, admin.getLogin());
            defLen = defGrps.length;
        }

        // let's use the first systemgroup in the list, or if length == 1
        // the only one :)
        List<String> names = new LinkedList<>();
        names.add(((ServerGroup)defGrps[0]).getName());
        assertEquals(1, handler.removeDefaultSystemGroups(admin,
                admin.getLogin(), names));

        // verify that after removal, the length of the new list + 1 ==
        // the original length, otherwise we did not remove anything.
        defGrps = handler.listDefaultSystemGroups(
                admin, admin.getLogin());
        assertEquals(defLen, defGrps.length + 1);
    }


    @Test
    public void testRemoveAssociatedSystemGroups() {

        User testUser = UserTestUtils.createUser("ksdjkfjasdkfjasdfjoiwenv", admin.getOrg().getId());

        Object[] assocGrps = handler.listAssignedSystemGroups(
                admin, testUser.getLogin());

        //should be empty now
        assertEquals(0, assocGrps.length);

        ServerGroupTestUtils.createManaged(testUser);
        Object[] groups = handler.listAssignedSystemGroups(
                admin, testUser.getLogin());
        assertTrue(groups.length > 0);
        assertEquals(1, handler.addAssignedSystemGroup(admin,
                testUser.getLogin(), ((ServerGroup)groups[0]).getName(), true));


        //should have 1 now
        assocGrps = handler.listAssignedSystemGroups(admin, testUser.getLogin());
        assertEquals(1, assocGrps.length);

        Object[] defGrps = handler.listDefaultSystemGroups(
                admin, testUser.getLogin());
        assertEquals(1, defGrps.length);


        List<String> names = new LinkedList<>();
        for (Object assocGrpIn : assocGrps) {
            names.add(((ServerGroup) assocGrpIn).getName());
        }

        assertEquals(1, handler.removeAssignedSystemGroups(admin,
                testUser.getLogin(), names, true));

        // verify that after removal, the length of the new list + 1 ==
        // the original length, otherwise we did not remove anything.
        assocGrps = handler.listAssignedSystemGroups(
                admin, testUser.getLogin());
        assertEquals(0, assocGrps.length);

        defGrps = handler.listDefaultSystemGroups(
                admin, testUser.getLogin());
        assertEquals(0, defGrps.length);

    }

    @Test
    public void testRemoveAssignedSystemGroupsInvalidGroup() {

        User testUser = UserTestUtils.createUser("ksdjkfjasdkfjasdfjoiwenv", admin.getOrg().getId());

         try {
             handler.removeAssignedSystemGroup(admin,
                     testUser.getLogin(), "kdfjkdsjflksdjf", false);
             fail();
         }
         catch (InvalidServerGroupException e) {
             // expected
         }
    }

    @Test
    public void testRemoveAssignedSystemGroupsWithInvalidGroup() {
        ServerGroup sg1 = ServerGroupTestUtils.createManaged(admin);
        ServerGroup sg2 = ServerGroupTestUtils.createManaged(admin);

        Object [] groups = handler.listAssignedSystemGroups(admin,
                regular.getLogin());
        assertEquals(0, groups.length);

        List<String> names = new LinkedList<>();
        names.add(sg1.getName());
        names.add(sg2.getName());
        handler.addAssignedSystemGroups(admin, regular.getLogin(), names,
                Boolean.FALSE);

        groups = handler.listAssignedSystemGroups(admin,
                regular.getLogin());
        assertEquals(2, groups.length);

        names.add("notarealgroup");
        try {
            handler.removeAssignedSystemGroups(admin, regular.getLogin(), names,
                Boolean.FALSE);
            fail();
        }
        catch (InvalidServerGroupException e) {
            // expected
        }

        groups = handler.listAssignedSystemGroups(admin,
                regular.getLogin());
        // None of the groups should have been removed:
        assertEquals(2, groups.length);
    }

    @Test
    public void testGetSetCreateDefaultSystemGroup() {
        boolean currentValue = handler.getCreateDefaultSystemGroup(admin);
        handler.setCreateDefaultSystemGroup(admin, !currentValue);
        assertEquals(!currentValue, handler.getCreateDefaultSystemGroup(admin));
        handler.setCreateDefaultSystemGroup(admin, currentValue);
        assertEquals(currentValue, handler.getCreateDefaultSystemGroup(admin));
    }
}
