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

import org.junit.jupiter.api.Test;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class ConfigLdapAuthConfigProviderTest {

    private ConfigLdapAuthConfigProvider providerFor(Map<String, String> values) {
        return new ConfigLdapAuthConfigProvider(
                values::get,
                key -> {
                    String v = values.get(key);
                    return "1".equals(v) || "true".equalsIgnoreCase(v) || "y".equalsIgnoreCase(v);
                },
                key -> {
                    String v = values.get(key);
                    if (v == null || v.isBlank()) {
                        return 0;
                    }
                    return Integer.parseInt(v);
                });
    }

    @Test
    public void disabledByDefault() {
        ConfigLdapAuthConfigProvider provider = providerFor(new HashMap<>());
        assertFalse(provider.isEnabled());
        assertTrue(provider.getServers().isEmpty());
    }

    @Test
    public void enabledButMisconfiguredYieldsNoServers() {
        Map<String, String> values = new HashMap<>();
        values.put("ldap.auth_enabled", "1");
        // No host / user_base_dn: misconfigured, must not throw and must not return a server.
        ConfigLdapAuthConfigProvider provider = providerFor(values);
        assertTrue(provider.isEnabled());
        assertTrue(provider.getServers().isEmpty());
    }

    @Test
    public void buildsSingleServerFromConfig() {
        Map<String, String> values = new HashMap<>();
        values.put("ldap.auth_enabled", "1");
        values.put("ldap.server_type", "ACTIVE_DIRECTORY");
        values.put("ldap.host", "ad.example.com");
        values.put("ldap.transport", "LDAPS");
        values.put("ldap.bind_dn", "CN=reader,DC=example,DC=com");
        values.put("ldap.bind_password", "secret");
        values.put("ldap.user_base_dn", "OU=Users,DC=example,DC=com");
        values.put("ldap.provisioning_mode", "EXISTING_ONLY");
        values.put("ldap.default_org_id", "3");

        List<LdapAuthServerSettings> servers = providerFor(values).getServers();

        assertEquals(1, servers.size());
        LdapAuthServerSettings server = servers.get(0);
        assertEquals("ad.example.com", server.connectionConfig().getHost());
        assertEquals(LdapServerType.ACTIVE_DIRECTORY, server.connectionConfig().getServerType());
        assertEquals(LdapTransport.LDAPS, server.connectionConfig().getTransport());
        assertEquals(LdapProvisioningMode.EXISTING_ONLY, server.provisioningMode());
        assertFalse(server.allowsJit());
        assertEquals(3L, server.defaultOrgId());
    }

    @Test
    public void defaultsProvisioningToJitAndOrgToOne() {
        Map<String, String> values = new HashMap<>();
        values.put("ldap.auth_enabled", "true");
        values.put("ldap.host", "ldap.example.com");
        values.put("ldap.bind_dn", "uid=reader,dc=example,dc=com");
        values.put("ldap.bind_password", "secret");
        values.put("ldap.user_base_dn", "ou=users,dc=example,dc=com");

        List<LdapAuthServerSettings> servers = providerFor(values).getServers();

        assertEquals(1, servers.size());
        LdapAuthServerSettings server = servers.get(0);
        assertEquals(LdapServerType.OPEN_LDAP, server.connectionConfig().getServerType());
        assertTrue(server.allowsJit());
        assertEquals(1L, server.defaultOrgId());
    }
}
