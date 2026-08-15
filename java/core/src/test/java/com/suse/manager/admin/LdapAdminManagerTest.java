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

package com.suse.manager.admin;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import com.redhat.rhn.common.validator.ValidatorException;
import com.redhat.rhn.manager.EntityExistsException;
import com.redhat.rhn.testing.BaseTestCaseWithUser;
import com.redhat.rhn.testing.TestUtils;

import com.suse.manager.ldap.LdapProvisioningMode;
import com.suse.manager.ldap.LdapServerType;
import com.suse.manager.ldap.LdapTransport;
import com.suse.manager.model.ldap.LdapAuthServer;
import com.suse.manager.model.ldap.LdapAuthServerFactory;
import com.suse.manager.webui.controllers.admin.beans.LdapProperties;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

/**
 * Exercises {@link LdapAdminManager} validation and CRUD against a real Hibernate session.
 */
public class LdapAdminManagerTest extends BaseTestCaseWithUser {

    private LdapAdminManager manager;

    @BeforeEach
    public void setUpManager() {
        manager = new LdapAdminManager();
    }

    private LdapProperties validProperties(String label) {
        LdapProperties properties = new LdapProperties();
        properties.setLabel(label);
        properties.setEnabled(true);
        properties.setPriority(10);
        properties.setServerType(LdapServerType.OPEN_LDAP.name());
        properties.setHost("ldap.example.com");
        properties.setPort(636);
        properties.setTransport(LdapTransport.LDAPS.name());
        properties.setUserBaseDn("ou=users,dc=example,dc=com");
        properties.setProvisioningMode(LdapProvisioningMode.JIT.name());
        properties.setDefaultOrgId(user.getOrg().getId());
        properties.setAutoJoinRegularUser(true);
        return properties;
    }

    @Test
    public void createPersistsServerAndBindCredentials() {
        String label = "ldap-" + TestUtils.randomString();
        LdapProperties properties = validProperties(label);
        properties.setBindDn("uid=reader,dc=example,dc=com");
        properties.setBindPassword("bind-secret");

        LdapAuthServer created = manager.create(properties);

        assertNotNull(created.getId());
        LdapAuthServer reloaded = LdapAuthServerFactory.lookupById(created.getId()).orElseThrow();
        assertEquals(label, reloaded.getLabel());
        assertEquals("uid=reader,dc=example,dc=com", reloaded.getBindDn());
        assertNotNull(reloaded.getCredentials());
        assertEquals("bind-secret", reloaded.getCredentials().getPassword());
        assertEquals(user.getOrg(), reloaded.getDefaultOrg());
    }

    @Test
    public void updateKeepsPasswordWhenEmptyAndRejectsDuplicateLabel() {
        String label = "ldap-" + TestUtils.randomString();
        LdapProperties createdProps = validProperties(label);
        createdProps.setBindDn("uid=reader,dc=example,dc=com");
        createdProps.setBindPassword("original");
        LdapAuthServer created = manager.create(createdProps);

        LdapProperties update = validProperties(label);
        update.setBindDn("uid=reader,dc=example,dc=com");
        update.setBindPassword("");
        update.setHost("ldap2.example.com");
        LdapAuthServer updated = manager.update(created.getId(), update);

        assertEquals("ldap2.example.com", updated.getHost());
        assertEquals("original", updated.getCredentials().getPassword());

        LdapAuthServer other = manager.create(validProperties("other-" + TestUtils.randomString()));
        LdapProperties clash = validProperties(label);
        clash.setDefaultOrgId(user.getOrg().getId());
        assertThrows(EntityExistsException.class, () -> manager.update(other.getId(), clash));
    }

    @Test
    public void deleteRemovesServerAndCredentials() {
        String label = "ldap-" + TestUtils.randomString();
        LdapProperties properties = validProperties(label);
        properties.setBindDn("uid=reader,dc=example,dc=com");
        properties.setBindPassword("secret");
        LdapAuthServer created = manager.create(properties);
        long id = created.getId();

        assertTrue(manager.delete(id));
        assertTrue(LdapAuthServerFactory.lookupById(id).isEmpty());
    }

    @Test
    public void createRequiresBindPasswordWhenBindDnSet() {
        LdapProperties properties = validProperties("ldap-" + TestUtils.randomString());
        properties.setBindDn("uid=reader,dc=example,dc=com");
        properties.setBindPassword("");

        assertThrows(ValidatorException.class, () -> manager.create(properties));
    }

    @Test
    public void createRequiresDefaultOrgForJit() {
        LdapProperties properties = validProperties("ldap-" + TestUtils.randomString());
        properties.setProvisioningMode(LdapProvisioningMode.JIT.name());
        properties.setDefaultOrgId(null);

        assertThrows(ValidatorException.class, () -> manager.create(properties));
    }

    @Test
    public void createRejectsInvalidRootCa() {
        LdapProperties properties = validProperties("ldap-" + TestUtils.randomString());
        properties.setRootCa("not-a-pem-certificate");

        assertThrows(ValidatorException.class, () -> manager.create(properties));
    }

    @Test
    public void createRejectsDuplicateLabel() {
        String label = "ldap-" + TestUtils.randomString();
        manager.create(validProperties(label));
        assertThrows(EntityExistsException.class, () -> manager.create(validProperties(label)));
    }

    @Test
    public void listAndGetRoundTrip() {
        String label = "ldap-" + TestUtils.randomString();
        LdapAuthServer created = manager.create(validProperties(label));

        assertTrue(manager.list().stream().anyMatch(server -> server.getId().equals(created.getId())));
        assertEquals(label, manager.get(created.getId()).getLabel());
    }

    @Test
    public void clearingBindDnRemovesCredentials() {
        String label = "ldap-" + TestUtils.randomString();
        LdapProperties properties = validProperties(label);
        properties.setBindDn("uid=reader,dc=example,dc=com");
        properties.setBindPassword("secret");
        LdapAuthServer created = manager.create(properties);

        LdapProperties update = validProperties(label);
        update.setBindDn("");
        update.setBindPassword("");
        LdapAuthServer updated = manager.update(created.getId(), update);

        assertNull(updated.getBindDn());
        assertNull(updated.getCredentials());
        assertFalse(updated.isUseMemberOf());
    }

    @Test
    public void testUserLookupRejectsBlankLogin() {
        String label = "ldap-" + TestUtils.randomString();
        LdapAuthServer created = manager.create(validProperties(label));

        assertThrows(ValidatorException.class, () -> manager.testUserLookup(created.getId(), ""));
        assertThrows(ValidatorException.class, () -> manager.testGroupResolution(created.getId(), "   "));
    }

    @Test
    public void createPersistsUseMemberOfFlag() {
        String label = "ldap-" + TestUtils.randomString();
        LdapProperties properties = validProperties(label);
        properties.setUseMemberOf(true);

        LdapAuthServer created = manager.create(properties);
        LdapAuthServer reloaded = LdapAuthServerFactory.lookupById(created.getId()).orElseThrow();

        assertTrue(reloaded.isUseMemberOf());
    }
}
