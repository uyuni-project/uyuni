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
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.junit.jupiter.api.Test;

public class LdapServerConfigTest {

    @Test
    public void appliesServerTypeDefaults() {
        LdapServerConfig config = LdapServerConfig
                .builder(LdapServerType.ACTIVE_DIRECTORY, "ad.example.com", "OU=Users,DC=example,DC=com")
                .bind("CN=reader,DC=example,DC=com", "secret")
                .build();

        assertEquals("sAMAccountName", config.getLoginAttribute());
        assertEquals("(&(objectClass=user)(sAMAccountName={login}))", config.getUserFilter());
        assertEquals("cn", config.getGroupNameAttribute());
        assertEquals("givenName", config.getFirstNameAttribute());
        // group base DN defaults to the user base DN until overridden
        assertEquals("OU=Users,DC=example,DC=com", config.getGroupBaseDn());
    }

    @Test
    public void ldapsIsTheDefaultTransportWithItsDefaultPort() {
        LdapServerConfig config = LdapServerConfig
                .builder(LdapServerType.FREE_IPA, "ipa.example.com", "dc=example,dc=com")
                .bind("uid=reader,dc=example,dc=com", "secret")
                .build();

        assertEquals(LdapTransport.LDAPS, config.getTransport());
        assertEquals(636, config.getPort());
        assertTrue(config.getTransport().isSecure());
    }

    @Test
    public void transportSwitchUpdatesPortWhenNoExplicitPortIsSet() {
        // Regression: selecting PLAIN without an explicit port must use the PLAIN default (389),
        // not remain on the LDAPS default (636).
        LdapServerConfig config = LdapServerConfig
                .builder(LdapServerType.OPEN_LDAP, "ldap.example.com", "ou=users,dc=example,dc=com")
                .transport(LdapTransport.PLAIN)
                .anonymousBind()
                .build();

        assertEquals(LdapTransport.PLAIN, config.getTransport());
        assertEquals(389, config.getPort());
    }

    @Test
    public void explicitPortSurvivesLaterTransportChange() {
        // An explicitly chosen port must win even if the transport is set afterwards.
        LdapServerConfig config = LdapServerConfig
                .builder(LdapServerType.OPEN_LDAP, "ldap.example.com", "ou=users,dc=example,dc=com")
                .port(1636)
                .transport(LdapTransport.PLAIN)
                .anonymousBind()
                .build();

        assertEquals(1636, config.getPort());
    }

    @Test
    public void overridesAreHonored() {
        LdapServerConfig config = LdapServerConfig
                .builder(LdapServerType.OPEN_LDAP, "ldap.example.com", "ou=users,dc=example,dc=com")
                .transport(LdapTransport.PLAIN)
                .port(1389)
                .bind("cn=admin,dc=example,dc=com", "admin")
                .groupBaseDn("ou=groups,dc=example,dc=com")
                .userFilter("(&(objectClass=posixAccount)(uid={login}))")
                .build();

        assertEquals(1389, config.getPort());
        assertEquals(LdapTransport.PLAIN, config.getTransport());
        assertFalse(config.getTransport().isSecure());
        assertEquals("ou=groups,dc=example,dc=com", config.getGroupBaseDn());
        assertEquals("(&(objectClass=posixAccount)(uid={login}))", config.getUserFilter());
    }

    @Test
    public void anonymousBindIsAllowedWhenExplicit() {
        LdapServerConfig config = LdapServerConfig
                .builder(LdapServerType.OPEN_LDAP, "ldap.example.com", "ou=users,dc=example,dc=com")
                .anonymousBind()
                .build();

        assertTrue(config.isAllowAnonymousBind());
        assertTrue(config.getBindDn().isEmpty());
    }

    @Test
    public void rejectsMissingBindWithoutExplicitAnonymous() {
        LdapServerConfig.Builder builder = LdapServerConfig
                .builder(LdapServerType.OPEN_LDAP, "ldap.example.com", "ou=users,dc=example,dc=com");
        assertThrows(IllegalStateException.class, builder::build);
    }

    @Test
    public void rejectsBindDnWithoutPassword() {
        LdapServerConfig.Builder builder = LdapServerConfig
                .builder(LdapServerType.OPEN_LDAP, "ldap.example.com", "ou=users,dc=example,dc=com")
                .bind("cn=admin,dc=example,dc=com", "");
        assertThrows(IllegalStateException.class, builder::build);
    }
}
