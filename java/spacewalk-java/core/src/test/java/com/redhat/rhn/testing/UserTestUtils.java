/*
 * Copyright (c) 2015--2025 SUSE LLC
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

package com.redhat.rhn.testing;

import static com.redhat.rhn.common.ExceptionMessage.NOT_INSTANTIABLE;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.junit.jupiter.api.Assertions.fail;

import com.redhat.rhn.common.localization.LocalizationService;
import com.redhat.rhn.domain.access.AccessGroup;
import com.redhat.rhn.domain.access.AccessGroupFactory;
import com.redhat.rhn.domain.org.Org;
import com.redhat.rhn.domain.org.OrgFactory;
import com.redhat.rhn.domain.role.Role;
import com.redhat.rhn.domain.role.RoleFactory;
import com.redhat.rhn.domain.server.EntitlementServerGroup;
import com.redhat.rhn.domain.server.ServerConstants;
import com.redhat.rhn.domain.user.Address;
import com.redhat.rhn.domain.user.User;
import com.redhat.rhn.domain.user.UserFactory;
import com.redhat.rhn.manager.user.UserManager;

import java.util.List;
import java.util.Optional;

/**
 * A class that allows us to easily create test users.
 */
public class UserTestUtils {

    public static final String TEST_PASSWORD = "password";

    /**
     * Creates a new User and an Org with default names.
     * The user will be added to the org.
     *
     * @return User created
     */
    public static User createUser() {
        return new UserBuilder().build();
    }

    /**
     * Creates a new User and an Org with the given userName and orgName.
     * The user will be added to the org.
     *
     * @param userName the userName for the User
     * @param orgName  the orgName for the User
     * @return User created
     */
    public static User createUser(String userName, String orgName) {
        return new UserBuilder().userName(userName).orgName(orgName).build();
    }

    /**
     * Creates a new User and an Org with the given userName, orgName and
     * an object whose class name will be appended to the org name.
     * The user will be added to the org.
     *
     * @param userName the userName for the User
     * @param orgName  the orgName for the User
     * @param object   an object whose class name will be appended to the org name
     * @return User created
     */
    public static User createUser(String userName, String orgName, Object object) {
        return new UserBuilder().userName(userName).orgName(orgName).orgObjectSuffix(object).build();
    }

    /**
     * Creates a new User with the given userName and adds it to the orgId.
     *
     * @param userName the userName for the User
     * @param orgId    the orgId for the User
     * @return User created
     */
    public static User createUser(String userName, Long orgId) {
        return new UserBuilder().userName(userName).orgId(orgId).build();
    }

    /**
     * Creates a new User with default name.
     * Creates a new Org with an object whose class name will be the org name.
     * The user will be added to the org.
     *
     * @param object   an object whose class name will be the org name
     * @return User created
     */
    public static User createUser(Object object) {
        return new UserBuilder().orgObjectSuffix(object).build();
    }

    /**
     * Create a new Org with default name.
     *
     * @return Org created
     */
    public static Org createOrg() {
        return new OrgBuilder().build();
    }

    /**
     * Create a new Org with an object whose class name will be the org name.
     *
     * @param object   an object whose class name will be the org name
     * @return Org created
     */
    public static Org createOrg(Object object) {
        return new OrgBuilder().orgObjectSuffix(object).build();
    }

    /**
     * Create a new Org with the given name.
     *
     * @param orgName  the orgName for the Org
     * @return Org created
     */
    public static Org createOrg(String orgName) {
        return new OrgBuilder().orgName(orgName).build();
    }

    /**
     * Create a dummy address to test against
     *
     * @param user the User we want to be the parent of this Address.
     * @return A dummy address to test against.
     */
    public static Address createTestAddress(User user) {
        user.setAddress1("444 Castro");
        user.setAddress2("#1");
        user.setCity("Mountain View");
        user.setState("CA");
        user.setZip("94043");
        user.setCountry("US");
        user.setPhone("650-555-1212");
        user.setFax("650-555-1212");
        return user.getEnterpriseUser().getAddress();
    }

    /**
     * Check that <code>user</code> is an org_admin, and that
     * there is at least one server visible to her. The second check
     * is necessary because of bz156752
     *
     * @param user the user for which to check
     */
    public static void assertOrgAdmin(User user) {
        boolean act = user.hasRole(RoleFactory.ORG_ADMIN);
        int servers = UserManager.visibleSystems(user).size();
        assertTrue(act, "User must be org_admin");
        assertTrue(servers > 0, "User sees some systems");
    }

    /**
     * Check that <code>user</code> is <em>not</em> an org_admin, and that
     * she can see no servers. The second check
     * is necessary because of bz156752
     *
     * @param user the user for which to check
     */
    public static void assertNotOrgAdmin(User user) {
        boolean act = user.hasRole(RoleFactory.ORG_ADMIN);
        int servers = UserManager.visibleSystems(user).size();
        assertFalse(act, "User must not be org_admin");
        assertEquals(0, servers, "User sees no servers");
    }

    /**
     * Simple method to add a Role to a User.  Will
     * make sure the User's org has the role too
     *
     * @param user to add Role to
     * @param r    Role to add.
     */
    public static void addUserRole(User user, Role r) {
        Org o = user.getOrg();
        o.addRole(r);
        user.addPermanentRole(r);
    }

    /**
     * Simple method to add an access group to a User.
     *
     * @param user    to add group to
     * @param groupIn the group to add.
     */
    public static void addAccessGroup(User user, AccessGroup groupIn) {
        user.addToGroup(groupIn);
    }

    /**
     * Add provisioning to an org
     *
     * @param orgIn to add to
     */
    public static void addManagement(Org orgIn) {
        ServerGroupTestUtils.createEntitled(orgIn,
                ServerConstants.getServerGroupTypeEnterpriseEntitled());
    }

    /**
     * Add virtualization to an org
     *
     * @param orgIn to add to
     */
    public static void addVirtualization(Org orgIn) {
        EntitlementServerGroup sg =
                ServerGroupTestUtils.createEntitled(orgIn,
                        ServerConstants.getServerGroupTypeVirtualizationEntitled());
        sg = TestUtils.saveAndFlush(sg);
    }

    /**
     * Find an Org_ADMIN for the Org passed in.  Create Org_ADMIN if not.
     *
     * @param orgIn to find/create
     * @return User who is Org_ADMIN
     */
    public static User ensureOrgAdminExists(Org orgIn) {
        User retval = UserFactory.findRandomOrgAdmin(orgIn);
        if (retval == null) {
            retval = new UserTestUtils.UserBuilder().orgId(orgIn.getId()).build();
            UserTestUtils.addUserRole(retval, RoleFactory.ORG_ADMIN);
            orgIn = TestUtils.saveAndFlush(orgIn);
        }
        return retval;
    }

    /**
     * Private constructor to prevent instantiation
     */
    private UserTestUtils() {
        throw new UnsupportedOperationException(NOT_INSTANTIABLE);
    }

    /**
     * Builder for creating a test User
     */
    public static class UserBuilder {

        private String userName = TestStatics.TEST_USER;
        private String orgName = TestStatics.TEST_ORG;
        private Long orgId = null;
        private Object orgObjectSuffix = null;
        private boolean orgAdmin = false;

        /**
         * Set the userName for the User
         * @param userNameIn the userName
         * @return this builder
         */
        public UserBuilder userName(String userNameIn) {
            this.userName = userNameIn;
            return this;
        }

        /**
         * Set the orgId for the User. If set, orgName will be ignored.
         * @param orgIdIn the org id
         * @return this builder
         */
        public UserBuilder orgId(Long orgIdIn) {
            this.orgId = orgIdIn;
            return this;
        }

        /**
         * Set the orgName for the User
         * @param orgNameIn the org name
         * @return this builder
         */
        public UserBuilder orgName(String orgNameIn) {
            this.orgName = orgNameIn;
            return this;
        }

        /**
         * Set the orgAdmin flag for the User
         * @param orgAdminIn whether the user should be an org admin
         * @return this builder
         */
        public UserBuilder orgAdmin(boolean orgAdminIn) {
            this.orgAdmin = orgAdminIn;
            return this;
        }

        /**
         * Set an object whose class name will be appended to the org name
         * @param obj the object to use
         * @return this builder
         */
        public UserBuilder orgObjectSuffix(Object obj) {
            this.orgObjectSuffix = obj;
            return this;
        }

        /**
         * Builds and persists the User
         *
         * @return the created User
         */
        public User build() {
            if (orgId != null && orgObjectSuffix != null) {
                fail("orgId and orgObjectSuffix cannot be both be provided");
            }

            Long resolvedOrgId = Optional.ofNullable(orgId)
                    .orElse(new OrgBuilder().orgName(orgName).orgObjectSuffix(orgObjectSuffix).build().getId());

            User userInternal = createUserInternal(userName);
            Address address = createTestAddress(userInternal);

            User user = UserFactory.saveNewUser(userInternal, address, resolvedOrgId);
            UserFactory.IMPLIEDROLES.forEach(user::addPermanentRole);
            assertTrue(user.getId() > 0);

            if (orgAdmin) {
                user.getAccessGroups().addAll(List.of(
                        AccessGroupFactory.CHANNEL_ADMIN,
                        AccessGroupFactory.SYSTEM_GROUP_ADMIN,
                        AccessGroupFactory.IMAGE_ADMIN,
                        AccessGroupFactory.ACTIVATION_KEY_ADMIN,
                        AccessGroupFactory.CONFIG_ADMIN)
                );
                user.addPermanentRole(RoleFactory.ORG_ADMIN);
            }
            return user;
        }

        /**
         * Internal method to create a user object with random values.
         *
         * @param userName base name of user
         * @return User the newly created User.
         */
        private static User createUserInternal(String userName) {
            UserFactory.getSession();
            User user = UserFactory.createUser();
            user.setLogin(userName + TestUtils.randomString());
            user.setPassword(TEST_PASSWORD);
            user.setFirstNames("userName" + TestUtils.randomString());
            user.setLastName("userName" + TestUtils.randomString());
            String prefix = (String) LocalizationService.getInstance().availablePrefixes().toArray()[0];
            user.setPrefix(prefix);
            user.setEmail("javaTest@example.com");

            return user;
        }
    }

    /**
     * Builder for creating a test User
     */
    public static class OrgBuilder {

        private String orgName = TestStatics.TEST_ORG;
        private Object orgObjectSuffix = null;

        /**
         * Set the orgName for the Org
         * @param orgNameIn the org name
         * @return this builder
         */
        public OrgBuilder orgName(String orgNameIn) {
            this.orgName = orgNameIn;
            return this;
        }

        /**
         * Set an object whose class name will be appended to the org name
         * @param obj the object to use
         * @return this builder
         */
        public OrgBuilder orgObjectSuffix(Object obj) {
            this.orgObjectSuffix = obj;
            return this;
        }

        /**
         * Builds and persists the Org
         *
         * @return the created Org
         */
        public Org build() {
            StringBuilder fullOrgNameSB = new StringBuilder(orgName);
            if (orgObjectSuffix != null) {
                fullOrgNameSB.append(orgObjectSuffix.getClass().getSimpleName());
            }
            fullOrgNameSB.append(TestUtils.randomString());

            Org org = OrgFactory.createOrg();
            org.setName(fullOrgNameSB.toString());
            org = OrgFactory.save(org);
            assertTrue(org.getId() > 0);
            return org;
        }
    }
}
