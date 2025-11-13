/*
 * Copyright (c) 2025 SUSE LLC
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

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import com.redhat.rhn.domain.role.RoleFactory;
import com.redhat.rhn.domain.user.Address;
import com.redhat.rhn.domain.user.User;

import org.junit.jupiter.api.Test;


public class UserTestUtilsTest {

    @Test
    void testCreateUserWhenAllParametersAreProvidedAndNonExistingOrg() {
        String username = "dummy_user";
        String orgName = "dummy_org_name";
        Object suffixObj = new Object();

        String expectedOrgNamePrefix = orgName + suffixObj.getClass().getSimpleName();

        //
        User userWithBuilder = new UserTestUtils.UserBuilder()
                .userName(username)
                .orgName(orgName)
                .orgObjectSuffix(suffixObj)
                .build();
        User userWithConstructor = UserTestUtils.createUser(username, orgName, suffixObj);

        //
        assertUserAndOrg(userWithBuilder, username, expectedOrgNamePrefix);
        assertUserAndOrg(userWithConstructor, username, expectedOrgNamePrefix);
    }

    @Test
    void testCreateUserWhenNoParametersAreProvided() {
        User userWithBuilder = new UserTestUtils.UserBuilder().build();
        User userWithConstructor = UserTestUtils.createUser();

        //
        assertUserAndOrg(userWithBuilder, TestStatics.TEST_USER, TestStatics.TEST_ORG);
        assertUserAndOrg(userWithConstructor, TestStatics.TEST_USER, TestStatics.TEST_ORG);
    }

    private static void assertUserAndOrg(User user, String expectedUsername, String expectedOrgName) {
        assertNotNull(user);
        assertTrue(user.getId() > 0);
        assertTrue(user.getLogin().startsWith(expectedUsername));
        assertEquals(expectedUsername.length() + 13, user.getLogin().length());
        assertNotNull(user.getOrg());
        assertTrue(user.getOrg().getId() > 0);
        assertTrue(user.getOrg().getName().startsWith(expectedOrgName));
        assertEquals(expectedOrgName.length() + 13, user.getOrg().getName().length());
    }

    @Test
    void testCreateUserWhenExistingOrg() {
        String username = "dummy_user";
        Long orgId = UserTestUtils.createOrg().getId();

        //
        User userWithBuilder = new UserTestUtils.UserBuilder()
                .userName(username)
                .orgId(orgId)
                .build();
        User userWithConstructor = UserTestUtils.createUser(username, orgId);

        //
        assertEquals(orgId, userWithBuilder.getOrg().getId());
        assertEquals(orgId, userWithConstructor.getOrg().getId());
    }

    @Test
    void testCreateTestAddress() {
        User user = UserTestUtils.createUser();
        Address address = UserTestUtils.createTestAddress(user);
        assertNotNull(address);
        assertEquals("444 Castro", address.getAddress1());
    }

    @Test
    void testUserBuilderOrgAdmin() {
        User user = new UserTestUtils.UserBuilder().orgAdmin(true).build();
        assertTrue(user.hasRole(RoleFactory.ORG_ADMIN));
    }

}
