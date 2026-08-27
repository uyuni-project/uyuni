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

import java.util.List;

/**
 * Verifies that persisted directory records win over the configuration file, and that the file is
 * only used while no directory is configured at all.
 */
public class DefaultLdapAuthConfigProviderTest {

    private static LdapAuthServerSettings settings(Long serverId, String host) {
        LdapServerConfig config = LdapServerConfig
                .builder(LdapServerType.OPEN_LDAP, host, "ou=users,dc=example,dc=com")
                .anonymousBind()
                .build();
        return new LdapAuthServerSettings(serverId, config, LdapProvisioningMode.JIT, 1L, true, 0);
    }

    private static LdapAuthConfigProvider stub(List<LdapAuthServerSettings> servers) {
        return new LdapAuthConfigProvider() {
            @Override
            public boolean isEnabled() {
                return !servers.isEmpty();
            }

            @Override
            public List<LdapAuthServerSettings> getServers() {
                return servers;
            }
        };
    }

    @Test
    public void withoutAnySourceLdapIsOff() {
        DefaultLdapAuthConfigProvider provider =
                new DefaultLdapAuthConfigProvider(stub(List.of()), stub(List.of()));

        assertFalse(provider.isEnabled());
        assertTrue(provider.getServers().isEmpty());
    }

    @Test
    public void persistedRecordsWinOverTheConfigurationFile() {
        DefaultLdapAuthConfigProvider provider = new DefaultLdapAuthConfigProvider(
                stub(List.of(settings(4L, "persisted.example.com"))),
                stub(List.of(settings(null, "from-config.example.com"))));

        List<LdapAuthServerSettings> servers = provider.getServers();

        assertTrue(provider.isEnabled());
        assertEquals(1, servers.size());
        assertEquals("persisted.example.com", servers.get(0).connectionConfig().getHost());
    }

    @Test
    public void configurationFileIsUsedWhileNoRecordExists() {
        DefaultLdapAuthConfigProvider provider = new DefaultLdapAuthConfigProvider(
                stub(List.of()),
                stub(List.of(settings(null, "from-config.example.com"))));

        List<LdapAuthServerSettings> servers = provider.getServers();

        assertTrue(provider.isEnabled());
        assertEquals(1, servers.size());
        assertEquals("from-config.example.com", servers.get(0).connectionConfig().getHost());
    }
}
