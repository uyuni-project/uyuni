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

package com.suse.manager.model.ldap;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import com.redhat.rhn.domain.credentials.CredentialsFactory;
import com.redhat.rhn.domain.credentials.LdapCredentials;
import com.redhat.rhn.testing.BaseTestCaseWithUser;
import com.redhat.rhn.testing.TestUtils;

import com.suse.manager.ldap.LdapProvisioningMode;
import com.suse.manager.ldap.LdapServerType;
import com.suse.manager.ldap.LdapTransport;

import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.Optional;

/**
 * Round-trips {@link LdapAuthServer} records through the database, which also verifies the entity
 * mapping against the real schema.
 */
public class LdapAuthServerFactoryTest extends BaseTestCaseWithUser {

    private LdapAuthServer newServer(String label, boolean enabled, int priority) {
        LdapAuthServer server = new LdapAuthServer();
        server.setLabel(label);
        server.setEnabled(enabled);
        server.setPriority(priority);
        server.setServerType(LdapServerType.OPEN_LDAP);
        server.setHost("ldap.example.com");
        server.setPort(636);
        server.setTransport(LdapTransport.LDAPS);
        server.setUserBaseDn("ou=users,dc=example,dc=com");
        return server;
    }

    @Test
    public void storesAndReloadsADirectory() {
        LdapAuthServer server = newServer("corp-" + TestUtils.randomString(), true, 0);
        server.setBindDn("uid=reader,dc=example,dc=com");
        server.setGroupBaseDn("ou=groups,dc=example,dc=com");
        server.setProvisioningMode(LdapProvisioningMode.EXISTING_ONLY);
        server.setDefaultOrg(user.getOrg());
        server.setAutoJoinRegularUser(false);
        LdapAuthServerFactory.save(server);
        TestUtils.flushAndEvict(server);

        LdapAuthServer reloaded = LdapAuthServerFactory.lookupById(server.getId()).orElseThrow();

        assertEquals(server.getLabel(), reloaded.getLabel());
        assertEquals(LdapServerType.OPEN_LDAP, reloaded.getServerType());
        assertEquals(LdapTransport.LDAPS, reloaded.getTransport());
        assertEquals(636, reloaded.getPort());
        assertEquals("uid=reader,dc=example,dc=com", reloaded.getBindDn());
        assertEquals("ou=groups,dc=example,dc=com", reloaded.getGroupBaseDn());
        assertEquals(LdapProvisioningMode.EXISTING_ONLY, reloaded.getProvisioningMode());
        assertEquals(user.getOrg(), reloaded.getDefaultOrg());
        assertFalse(reloaded.isAutoJoinRegularUser());
        assertNotNull(reloaded.getCreated());
    }

    @Test
    public void bindPasswordIsStoredAsLdapCredentials() {
        LdapCredentials credentials = CredentialsFactory.createLdapCredentials("bind-secret");
        CredentialsFactory.storeCredentials(credentials);

        LdapAuthServer server = newServer("creds-" + TestUtils.randomString(), true, 0);
        server.setBindDn("uid=reader,dc=example,dc=com");
        server.setCredentials(credentials);
        LdapAuthServerFactory.save(server);
        TestUtils.flushAndEvict(server);

        LdapAuthServer reloaded = LdapAuthServerFactory.lookupById(server.getId()).orElseThrow();

        assertNotNull(reloaded.getCredentials());
        assertEquals("bind-secret", reloaded.getCredentials().getPassword());
    }

    @Test
    public void storesDirectoryCaAndMemberOfFlag() {
        LdapAuthServer server = newServer("ca-" + TestUtils.randomString(), true, 0);
        server.setRootCa("-----BEGIN CERTIFICATE-----\nMIIB\n-----END CERTIFICATE-----");
        server.setUseMemberOf(true);
        LdapAuthServerFactory.save(server);
        TestUtils.flushAndEvict(server);

        LdapAuthServer reloaded = LdapAuthServerFactory.lookupById(server.getId()).orElseThrow();

        assertTrue(reloaded.isUseMemberOf());
        assertTrue(reloaded.getRootCa().contains("BEGIN CERTIFICATE"));
    }

    @Test
    public void onlyEnabledDirectoriesAreListedAndTheyFollowPriority() {
        LdapAuthServer disabled = newServer("disabled-" + TestUtils.randomString(), false, 0);
        LdapAuthServer second = newServer("second-" + TestUtils.randomString(), true, 20);
        LdapAuthServer first = newServer("first-" + TestUtils.randomString(), true, 10);
        LdapAuthServerFactory.save(disabled);
        LdapAuthServerFactory.save(second);
        LdapAuthServerFactory.save(first);
        TestUtils.flushSession();

        List<String> enabledLabels = LdapAuthServerFactory.listEnabled().stream()
                .map(LdapAuthServer::getLabel)
                .toList();
        // Ignore directories other tests may have left behind, but keep their relative order.
        List<String> ownLabels = enabledLabels.stream()
                .filter(label -> label.equals(first.getLabel()) || label.equals(second.getLabel()))
                .toList();

        assertEquals(List.of(first.getLabel(), second.getLabel()), ownLabels);
        assertFalse(enabledLabels.contains(disabled.getLabel()));
    }

    @Test
    public void findsADirectoryByItsLabel() {
        LdapAuthServer server = newServer("by-label-" + TestUtils.randomString(), true, 0);
        LdapAuthServerFactory.save(server);
        TestUtils.flushSession();

        Optional<LdapAuthServer> found = LdapAuthServerFactory.lookupByLabel(server.getLabel());

        assertTrue(found.isPresent());
        assertEquals(server.getId(), found.get().getId());
    }

    @Test
    public void removesADirectory() {
        LdapAuthServer server = newServer("removable-" + TestUtils.randomString(), true, 0);
        LdapAuthServerFactory.save(server);
        TestUtils.flushSession();
        Long id = server.getId();

        LdapAuthServerFactory.remove(server);
        TestUtils.flushSession();

        assertTrue(LdapAuthServerFactory.lookupById(id).isEmpty());
    }
}
