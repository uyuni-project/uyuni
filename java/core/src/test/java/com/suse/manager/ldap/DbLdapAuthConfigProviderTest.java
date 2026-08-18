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
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import com.suse.manager.model.ldap.LdapAuthServer;

import org.junit.jupiter.api.Test;

import java.util.List;

/**
 * Verifies that persisted directory records are translated into the settings the login layer
 * consumes, including the "empty column means use the server type default" rule.
 */
public class DbLdapAuthConfigProviderTest {

    private static LdapAuthServer minimalServer() {
        LdapAuthServer server = new LdapAuthServer();
        server.setId(7L);
        server.setLabel("corp-ad");
        server.setServerType(LdapServerType.ACTIVE_DIRECTORY);
        server.setHost("ad.example.com");
        server.setTransport(LdapTransport.LDAPS);
        server.setUserBaseDn("OU=Users,DC=example,DC=com");
        return server;
    }

    private static DbLdapAuthConfigProvider providerFor(LdapAuthServer... servers) {
        return new DbLdapAuthConfigProvider(() -> List.of(servers));
    }

    @Test
    public void noConfiguredServerMeansLdapIsOff() {
        DbLdapAuthConfigProvider provider = providerFor();
        assertFalse(provider.isEnabled());
        assertTrue(provider.getServers().isEmpty());
    }

    @Test
    public void mapsRecordToLoginSettings() {
        LdapAuthServer server = minimalServer();
        server.setPriority(5);
        server.setProvisioningMode(LdapProvisioningMode.EXISTING_ONLY);
        server.setAutoJoinRegularUser(false);
        server.setConnectTimeout(2000);
        server.setResponseTimeout(3000);

        List<LdapAuthServerSettings> settings = providerFor(server).getServers();

        assertEquals(1, settings.size());
        LdapAuthServerSettings only = settings.get(0);
        assertEquals(7L, only.serverId());
        assertEquals(5, only.priority());
        assertFalse(only.allowsJit());
        assertFalse(only.autoJoinRegularUser());
        assertEquals("ad.example.com", only.connectionConfig().getHost());
        assertEquals(LdapTransport.LDAPS.getDefaultPort(), only.connectionConfig().getPort());
        assertEquals(2000, only.connectionConfig().getConnectTimeoutMillis());
        assertEquals(3000, only.connectionConfig().getResponseTimeoutMillis());
    }

    @Test
    public void emptyColumnsFallBackToServerTypeDefaults() {
        List<LdapAuthServerSettings> settings = providerFor(minimalServer()).getServers();

        LdapServerConfig config = settings.get(0).connectionConfig();
        assertEquals(LdapServerType.ACTIVE_DIRECTORY.getDefaultUserFilter(), config.getUserFilter());
        assertEquals(LdapServerType.ACTIVE_DIRECTORY.getDefaultLoginAttribute(), config.getLoginAttribute());
        assertEquals(LdapServerType.ACTIVE_DIRECTORY.getDefaultGroupFilter(), config.getGroupFilter());
        assertEquals(LdapServerType.ACTIVE_DIRECTORY.getDefaultEmailAttribute(), config.getEmailAttribute());
    }

    @Test
    public void customizingOneProfileAttributeKeepsTheOtherDefaults() {
        LdapAuthServer server = minimalServer();
        server.setEmailAttribute("userPrincipalName");

        LdapServerConfig config = providerFor(server).getServers().get(0).connectionConfig();

        assertEquals("userPrincipalName", config.getEmailAttribute());
        assertEquals(LdapServerType.ACTIVE_DIRECTORY.getDefaultFirstNameAttribute(),
                config.getFirstNameAttribute());
        assertEquals(LdapServerType.ACTIVE_DIRECTORY.getDefaultLastNameAttribute(),
                config.getLastNameAttribute());
    }

    @Test
    public void recordWithoutBindDnBindsAnonymously() {
        LdapServerConfig config = providerFor(minimalServer()).getServers().get(0).connectionConfig();

        assertTrue(config.isAllowAnonymousBind());
        assertTrue(config.getBindDn().isEmpty());
    }

    @Test
    public void brokenRecordIsSkippedWithoutHidingTheHealthyOnes() {
        // A bind DN with no stored password cannot produce a usable connection. That record must be
        // skipped instead of failing the whole login page.
        LdapAuthServer broken = minimalServer();
        broken.setId(1L);
        broken.setLabel("broken");
        broken.setBindDn("CN=reader,DC=example,DC=com");

        LdapAuthServer healthy = minimalServer();
        healthy.setId(2L);
        healthy.setLabel("healthy");

        List<LdapAuthServerSettings> settings = providerFor(broken, healthy).getServers();

        assertEquals(1, settings.size());
        assertEquals(2L, settings.get(0).serverId());
    }

    @Test
    public void serverWithoutDefaultOrgHasNoOrgId() {
        assertNull(providerFor(minimalServer()).getServers().get(0).defaultOrgId());
    }
}
