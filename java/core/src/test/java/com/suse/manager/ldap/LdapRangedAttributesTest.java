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
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import com.unboundid.ldap.listener.InMemoryDirectoryServer;
import com.unboundid.ldap.listener.InMemoryDirectoryServerConfig;
import com.unboundid.ldap.sdk.Attribute;
import com.unboundid.ldap.sdk.Entry;
import com.unboundid.ldap.sdk.LDAPConnectionPool;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.Optional;
import java.util.Set;

/**
 * Unit tests for Active Directory {@code ;range=} option parsing and ranged attribute reads.
 */
public class LdapRangedAttributesTest {

    private static final String BASE_DN = "dc=uyuni,dc=test";
    private static final String USERS_DN = "ou=users,dc=uyuni,dc=test";
    private static final String GROUPS_DN = "ou=groups,dc=uyuni,dc=test";
    private static final String ADMIN_DN = "cn=admin,dc=uyuni,dc=test";
    private static final String ADMIN_PASSWORD = "admin";

    private InMemoryDirectoryServer directory;

    @BeforeEach
    public void startDirectory() throws Exception {
        InMemoryDirectoryServerConfig dsConfig = new InMemoryDirectoryServerConfig(BASE_DN);
        // memberOf is not in the default schema; disable checks so we can seed overlay values.
        dsConfig.setSchema(null);
        dsConfig.addAdditionalBindCredentials(ADMIN_DN, ADMIN_PASSWORD);
        directory = new InMemoryDirectoryServer(dsConfig);
        directory.add("dn: " + BASE_DN, "objectClass: top", "objectClass: domain", "dc: uyuni");
        directory.add("dn: " + USERS_DN, "objectClass: organizationalUnit", "ou: users");
        directory.add("dn: " + GROUPS_DN, "objectClass: organizationalUnit", "ou: groups");
        directory.add(
                "dn: uid=alice," + USERS_DN,
                "objectClass: inetOrgPerson",
                "cn: Alice",
                "sn: Anderson",
                "uid: alice",
                "memberOf: cn=uyuni-admins," + GROUPS_DN,
                "memberOf: cn=uyuni-users," + GROUPS_DN);
        directory.startListening();
    }

    @AfterEach
    public void stopDirectory() {
        if (directory != null) {
            directory.shutDown(true);
        }
    }

    @Test
    public void parsesOpenEndedTerminalRange() {
        Optional<LdapRangedAttributes.Range> range =
                LdapRangedAttributes.parseRange(Set.of("range=1500-*"));

        assertTrue(range.isPresent());
        assertTrue(range.get().complete());
        assertEquals(1500, range.get().start());
    }

    @Test
    public void parsesPartialRangePage() {
        Optional<LdapRangedAttributes.Range> range =
                LdapRangedAttributes.parseRange(Set.of("RANGE=0-1499"));

        assertTrue(range.isPresent());
        assertFalse(range.get().complete());
        assertEquals(0, range.get().start());
        assertEquals(1499, range.get().end());
    }

    @Test
    public void ignoresAttributesWithoutRangeOption() {
        assertTrue(LdapRangedAttributes.parseRange(Set.of("binary")).isEmpty());
        assertTrue(LdapRangedAttributes.parseRange(Set.of()).isEmpty());
        assertTrue(LdapRangedAttributes.parseRange(null).isEmpty());
    }

    @Test
    public void findByBaseNameIgnoresRangeOptions() {
        Entry entry = new Entry("uid=alice," + USERS_DN);
        entry.addAttribute(new Attribute("memberOf;range=0-1",
                "cn=uyuni-admins," + GROUPS_DN,
                "cn=uyuni-users," + GROUPS_DN));

        Optional<Attribute> attribute = LdapRangedAttributes.findByBaseName(entry, "memberOf");

        assertTrue(attribute.isPresent());
        assertEquals("memberOf", attribute.get().getBaseName());
        assertEquals(2, attribute.get().getValues().length);
    }

    @Test
    public void findByBaseNameReturnsEmptyForMissingAttribute() {
        Entry entry = new Entry("uid=alice," + USERS_DN);
        entry.addAttribute("uid", "alice");

        assertTrue(LdapRangedAttributes.findByBaseName(entry, "memberOf").isEmpty());
        assertTrue(LdapRangedAttributes.findByBaseName(null, "memberOf").isEmpty());
        assertTrue(LdapRangedAttributes.findByBaseName(entry, null).isEmpty());
    }

    @Test
    public void readAllValuesCollectsUnrangedMemberOf() throws Exception {
        // When AD does not page the attribute, readAllValues still returns every memberOf DN.
        // The pool takes ownership of the connection from getConnection().
        try (LDAPConnectionPool pool = new LDAPConnectionPool(directory.getConnection(), 1)) {
            List<String> values = LdapRangedAttributes.readAllValues(
                    pool, "uid=alice," + USERS_DN, "memberOf");

            assertEquals(List.of(
                    "cn=uyuni-admins," + GROUPS_DN,
                    "cn=uyuni-users," + GROUPS_DN), values);
        }
    }
}
