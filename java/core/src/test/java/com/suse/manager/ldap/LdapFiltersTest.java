/*
 * Copyright (c) 2026 SUSE LLC
 *
 * This software is licensed to you under the GNU General Public License,
 * version 2 (GPLv2). There is NO WARRANTY for this software, express or
 * implied, including the implied warranties of MERCHANTABILITY or FITNESS
 * FOR A PARTICULAR PURPOSE. You should have received a copy of GPLv2
 * along with this software; if not, see
 * http://www.gnu.org/licenses/old-licenses/gpl-2.0.txt.
 */

package com.suse.manager.ldap;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import com.unboundid.ldap.sdk.Filter;

import org.junit.jupiter.api.Test;

public class LdapFiltersTest {

    @Test
    public void buildsUserFilterFromTemplate() throws Exception {
        Filter filter = LdapFilters.userFilter("(&(objectClass=inetOrgPerson)(uid={login}))", "alice");
        assertEquals("(&(objectClass=inetOrgPerson)(uid=alice))", filter.toString());
    }

    @Test
    public void buildsGroupFilterFromTemplate() throws Exception {
        Filter filter = LdapFilters.groupFilter("(&(objectClass=groupOfNames)(member={userDn}))",
                "uid=alice,ou=users,dc=uyuni,dc=test");
        assertEquals("(&(objectClass=groupOfNames)(member=uid=alice,ou=users,dc=uyuni,dc=test))",
                filter.toString());
    }

    @Test
    public void buildsActiveDirectoryNestedGroupFilterFromTemplate() throws Exception {
        // Phase 5 default for ACTIVE_DIRECTORY: LDAP_MATCHING_RULE_IN_CHAIN (1.2.840.113556.1.4.1941)
        Filter filter = LdapFilters.groupFilter(
                "(&(objectClass=group)(member:1.2.840.113556.1.4.1941:={userDn}))",
                "CN=Alice,OU=Users,DC=example,DC=com");
        assertEquals(
                "(&(objectClass=group)(member:1.2.840.113556.1.4.1941:=CN=Alice,OU=Users,DC=example,DC=com))",
                filter.toString());
    }

    @Test
    public void escapesSpecialCharactersToPreventInjection() throws Exception {
        Filter filter = LdapFilters.userFilter("(uid={login})", "a)(uid=*");
        // The asterisk and parentheses must be escaped so the supplied value cannot widen the
        // filter into a wildcard or inject extra clauses.
        String rendered = filter.toString();
        assertTrue(rendered.contains("\\2a"), "asterisk should be escaped: " + rendered);
        assertTrue(rendered.contains("\\28") || rendered.contains("\\29"),
                "parentheses should be escaped: " + rendered);
        assertEquals("(uid=a\\29\\28uid=\\2a)", rendered);
    }

    @Test
    public void rejectsTemplateWithoutPlaceholder() {
        assertThrows(LdapServiceException.class, () -> LdapFilters.userFilter("(uid=fixed)", "alice"));
    }

    @Test
    public void rejectsEmptyValue() {
        assertThrows(LdapServiceException.class, () -> LdapFilters.userFilter("(uid={login})", ""));
        assertThrows(LdapServiceException.class, () -> LdapFilters.groupFilter("(member={userDn})", null));
    }

    @Test
    public void rejectsMalformedTemplate() {
        assertThrows(LdapServiceException.class, () -> LdapFilters.userFilter("(&(uid={login})", "alice"));
    }
}
