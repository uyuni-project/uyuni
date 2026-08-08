/*
 * Copyright (c) 2009--2013 Red Hat, Inc.
 * Copyright (c) 2026 SUSE LLC
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

package com.redhat.rhn.domain.org.usergroup;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;

import com.redhat.rhn.domain.org.Org;
import com.redhat.rhn.domain.role.RoleFactory;
import com.redhat.rhn.testing.BaseTestCase;
import com.redhat.rhn.testing.TestUtils;
import com.redhat.rhn.testing.UserTestUtils;

import com.suse.manager.ldap.LdapServerType;
import com.suse.manager.ldap.LdapTransport;
import com.suse.manager.model.ldap.LdapAuthServer;
import com.suse.manager.model.ldap.LdapAuthServerFactory;

import org.junit.jupiter.api.Test;

import java.util.HashSet;
import java.util.Set;

/** JUnit test case for the UserGroup
 *  class.
 */

public class UserGroupFactoryTest extends BaseTestCase {

    /**
    * Test to see if the Org can translate a Role to the
    * appropriate UserGroupId.  This is the only public
    * usage of anything related to a UserGroup
     */
    @Test
    public void testGetUserGroup() {
        Org org1 = UserTestUtils.createOrg(this);
        UserGroup ugid = org1.getUserGroup(RoleFactory.ORG_ADMIN);
        assertNotNull(ugid);
    }

    @Test
    public void generatedCoverageTestLookupExtGroupById() {
        // this test has been generated programmatically to test UserGroupFactory.lookupExtGroupById
        // containing a hibernate query that is not covered by any test so far
        // feel free to modify and/or complete it
        UserGroupFactory.lookupExtGroupById(0L);
    }

    @Test
    public void generatedCoverageTestLookupOrgExtGroupByIdAndOrg() {
        // this test has been generated programmatically to test UserGroupFactory.lookupOrgExtGroupByIdAndOrg
        // containing a hibernate query that is not covered by any test so far
        // feel free to modify and/or complete it
        Org arg1 = new Org();
        UserGroupFactory.lookupOrgExtGroupByIdAndOrg(0L, arg1);
    }

    @Test
    public void lookupExtGroupByLabelAndServerMatchesExactly() {
        String label = "ext-" + TestUtils.randomString();

        UserExtGroup agnostic = new UserExtGroup();
        agnostic.setLabel(label);
        agnostic.setRoles(new HashSet<>(Set.of(RoleFactory.SYSTEM_GROUP_ADMIN)));
        agnostic.setLdapServerId(null);
        UserGroupFactory.save(agnostic);

        LdapAuthServer server = new LdapAuthServer();
        server.setLabel("ldap-" + TestUtils.randomString());
        server.setEnabled(true);
        server.setPriority(0);
        server.setServerType(LdapServerType.OPEN_LDAP);
        server.setHost("ldap.example.com");
        server.setPort(636);
        server.setTransport(LdapTransport.LDAPS);
        server.setUserBaseDn("ou=users,dc=example,dc=com");
        server = LdapAuthServerFactory.save(server);

        UserExtGroup scoped = new UserExtGroup();
        scoped.setLabel(label);
        scoped.setRoles(new HashSet<>(Set.of(RoleFactory.CONFIG_ADMIN)));
        scoped.setLdapServerId(server.getId());
        UserGroupFactory.save(scoped);

        UserExtGroup foundAgnostic = UserGroupFactory.lookupExtGroupByLabelAndServer(label, null);
        assertNotNull(foundAgnostic);
        assertNull(foundAgnostic.getLdapServerId());
        assertEquals(agnostic.getId(), foundAgnostic.getId());

        UserExtGroup foundScoped = UserGroupFactory.lookupExtGroupByLabelAndServer(label, server.getId());
        assertNotNull(foundScoped);
        assertEquals(server.getId(), foundScoped.getLdapServerId());
        assertEquals(scoped.getId(), foundScoped.getId());

        assertNull(UserGroupFactory.lookupExtGroupByLabelAndServer(label, server.getId() + 999));
    }

    @Test
    public void lookupExtGroupByLabelFallsBackToAgnostic() {
        String label = "ext-" + TestUtils.randomString();
        UserExtGroup agnostic = new UserExtGroup();
        agnostic.setLabel(label);
        agnostic.setRoles(new HashSet<>(Set.of(RoleFactory.SYSTEM_GROUP_ADMIN)));
        UserGroupFactory.save(agnostic);

        UserExtGroup found = UserGroupFactory.lookupExtGroupByLabel(label, 42L);
        assertNotNull(found);
        assertEquals(agnostic.getId(), found.getId());
    }
}
