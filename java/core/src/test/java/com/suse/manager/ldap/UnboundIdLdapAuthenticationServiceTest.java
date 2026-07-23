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

import com.unboundid.ldap.listener.InMemoryDirectoryServer;
import com.unboundid.ldap.listener.InMemoryDirectoryServerConfig;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.Optional;

/**
 * Integration test exercising the full bind/search/bind pipeline against an embedded
 * {@link InMemoryDirectoryServer}. The directory is seeded with the same entries as the
 * {@code tooling/dev-ldap} fixture so the test needs no external server or container.
 */
public class UnboundIdLdapAuthenticationServiceTest {

    private static final String BASE_DN = "dc=uyuni,dc=test";
    private static final String USERS_DN = "ou=users,dc=uyuni,dc=test";
    private static final String GROUPS_DN = "ou=groups,dc=uyuni,dc=test";
    private static final String ADMIN_DN = "cn=admin,dc=uyuni,dc=test";
    private static final String ADMIN_PASSWORD = "admin";

    private InMemoryDirectoryServer directory;

    @BeforeEach
    public void startDirectory() throws Exception {
        InMemoryDirectoryServerConfig dsConfig = new InMemoryDirectoryServerConfig(BASE_DN);
        dsConfig.addAdditionalBindCredentials(ADMIN_DN, ADMIN_PASSWORD);
        directory = new InMemoryDirectoryServer(dsConfig);
        seed();
        directory.startListening();
    }

    @AfterEach
    public void stopDirectory() {
        if (directory != null) {
            directory.shutDown(true);
        }
    }

    private void seed() throws Exception {
        directory.add("dn: " + BASE_DN, "objectClass: top", "objectClass: domain", "dc: uyuni");
        directory.add("dn: " + USERS_DN, "objectClass: organizationalUnit", "ou: users");
        directory.add("dn: " + GROUPS_DN, "objectClass: organizationalUnit", "ou: groups");

        directory.add(
                "dn: uid=alice," + USERS_DN,
                "objectClass: inetOrgPerson",
                "cn: Alice Anderson",
                "givenName: Alice",
                "sn: Anderson",
                "uid: alice",
                "mail: alice@uyuni.test",
                "userPassword: alice123");
        directory.add(
                "dn: uid=bob," + USERS_DN,
                "objectClass: inetOrgPerson",
                "cn: Bob Brown",
                "givenName: Bob",
                "sn: Brown",
                "uid: bob",
                "mail: bob@uyuni.test",
                "userPassword: bob123");

        directory.add(
                "dn: cn=uyuni-admins," + GROUPS_DN,
                "objectClass: groupOfNames",
                "cn: uyuni-admins",
                "member: uid=alice," + USERS_DN);
        directory.add(
                "dn: cn=uyuni-users," + GROUPS_DN,
                "objectClass: groupOfNames",
                "cn: uyuni-users",
                "member: uid=alice," + USERS_DN,
                "member: uid=bob," + USERS_DN);
    }

    private LdapAuthenticationService service() {
        return service(ADMIN_DN, ADMIN_PASSWORD);
    }

    private LdapAuthenticationService service(String bindDn, String bindPassword) {
        LdapServerConfig config = LdapServerConfig
                .builder(LdapServerType.OPEN_LDAP, "127.0.0.1", USERS_DN)
                .transport(LdapTransport.PLAIN)
                .port(directory.getListenPort())
                .bind(bindDn, bindPassword)
                .groupBaseDn(GROUPS_DN)
                .build();
        return new DefaultLdapServiceFactory().getInstance(config);
    }

    @Test
    public void authenticatesUserAndResolvesAllGroups() throws Exception {
        Optional<LdapUser> result = service().authenticate("alice", "alice123");

        assertTrue(result.isPresent());
        LdapUser user = result.get();
        assertEquals("alice", user.login());
        assertEquals("uid=alice," + USERS_DN, user.distinguishedName());
        assertEquals("Alice", user.firstName());
        assertEquals("Anderson", user.lastName());
        assertEquals("alice@uyuni.test", user.email());
        assertEquals(List.of("uyuni-admins", "uyuni-users"), user.groupLabels());
    }

    @Test
    public void authenticatesUserWithSubsetOfGroups() throws Exception {
        Optional<LdapUser> result = service().authenticate("bob", "bob123");

        assertTrue(result.isPresent());
        assertEquals(List.of("uyuni-users"), result.get().groupLabels());
    }

    @Test
    public void rejectsWrongPassword() throws Exception {
        assertTrue(service().authenticate("alice", "wrong-password").isEmpty());
    }

    @Test
    public void rejectsUnknownUser() throws Exception {
        assertTrue(service().authenticate("carol", "whatever").isEmpty());
    }

    @Test
    public void rejectsEmptyPasswordWithoutBinding() throws Exception {
        // A valid DN with an empty password must never be treated as a successful bind.
        assertTrue(service().authenticate("alice", "").isEmpty());
    }

    @Test
    public void rejectsNullCredentials() throws Exception {
        assertTrue(service().authenticate(null, "x").isEmpty());
        assertTrue(service().authenticate("alice", null).isEmpty());
    }

    @Test
    public void failedServiceBindRaisesLdapException() {
        LdapAuthenticationService service = service(ADMIN_DN, "wrong-service-password");
        assertThrows(LdapException.class, () -> service.authenticate("alice", "alice123"));
    }

    @Test
    public void rejectsAmbiguousUserSearch() throws Exception {
        directory.add(
                "dn: uid=alice-dup," + USERS_DN,
                "objectClass: inetOrgPerson",
                "cn: Alice Duplicate",
                "givenName: Alice",
                "sn: Duplicate",
                "uid: alice",
                "mail: alice-dup@uyuni.test",
                "userPassword: alice123");

        assertTrue(service().authenticate("alice", "alice123").isEmpty());
    }

    @Test
    public void authenticatesWhenGroupLookupFails() throws Exception {
        LdapServerConfig config = LdapServerConfig
                .builder(LdapServerType.OPEN_LDAP, "127.0.0.1", USERS_DN)
                .transport(LdapTransport.PLAIN)
                .port(directory.getListenPort())
                .bind(ADMIN_DN, ADMIN_PASSWORD)
                .groupBaseDn("ou=missing," + BASE_DN)
                .build();
        LdapAuthenticationService brokenGroupService = new DefaultLdapServiceFactory().getInstance(config);

        Optional<LdapUser> result = brokenGroupService.authenticate("alice", "alice123");

        assertTrue(result.isPresent());
        assertEquals("alice", result.get().login());
        assertTrue(result.get().groupLabels().isEmpty());
    }
}
