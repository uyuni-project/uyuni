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

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.util.List;

/**
 * The configuration source the login layer uses by default: persisted directory records, with the
 * {@code ldap.*} keys of {@code rhn.conf} as a fallback.
 *
 * <p>Persisted {@code suseLdapAuthServer} records are the source of truth. The configuration file
 * is only consulted while no directory record exists at all, so that an administrator can still set
 * up and try native LDAP before the LDAP administration UI lands. Once a directory is configured in
 * the database, the file is ignored entirely and there is a single source of truth. This fallback
 * (and {@link ConfigLdapAuthConfigProvider} with it) is meant to be dropped once directories can be
 * created from the UI.</p>
 */
public class DefaultLdapAuthConfigProvider implements LdapAuthConfigProvider {

    private static final Logger LOG = LogManager.getLogger(DefaultLdapAuthConfigProvider.class);

    private final LdapAuthConfigProvider persisted;
    private final LdapAuthConfigProvider configFile;

    /**
     * Creates the default provider over the persisted records and the configuration file.
     */
    public DefaultLdapAuthConfigProvider() {
        this(new DbLdapAuthConfigProvider(), new ConfigLdapAuthConfigProvider());
    }

    /**
     * Creates a provider over explicit sources. Mainly useful for tests.
     *
     * @param persistedIn the persisted directory records, always preferred
     * @param configFileIn the configuration-file source, used only when no record exists
     */
    public DefaultLdapAuthConfigProvider(LdapAuthConfigProvider persistedIn, LdapAuthConfigProvider configFileIn) {
        this.persisted = persistedIn;
        this.configFile = configFileIn;
    }

    @Override
    public boolean isEnabled() {
        return !getServers().isEmpty();
    }

    @Override
    public List<LdapAuthServerSettings> getServers() {
        List<LdapAuthServerSettings> servers = persisted.getServers();
        if (!servers.isEmpty()) {
            return servers;
        }
        List<LdapAuthServerSettings> fromConfigFile = configFile.getServers();
        if (!fromConfigFile.isEmpty()) {
            LOG.warn("Using the native LDAP settings from rhn.conf: no LDAP server is configured in the " +
                    "database. Configured directories always take precedence over the configuration file.");
        }
        return fromConfigFile;
    }
}
