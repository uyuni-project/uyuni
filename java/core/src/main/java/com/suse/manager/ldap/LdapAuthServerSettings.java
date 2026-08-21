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

import java.util.Objects;

/**
 * A single directory server together with the login-layer policy that applies to it: how the
 * connection and searches are configured ({@link #connectionConfig()}), whether accounts may be
 * provisioned just-in-time ({@link #provisioningMode()}), which organization JIT users are created
 * in ({@link #defaultOrgId()}), and the order in which servers are probed for unknown users
 * ({@link #priority()}, lower first).
 *
 * <p>This is the login layer's view of an LDAP server. Phase 2 builds these from configuration; a
 * later phase will build them from persisted {@code suseLdapAuthServer} records without changing
 * this contract.</p>
 *
 * @param connectionConfig the connection and search configuration for the directory
 * @param provisioningMode whether unknown users may be created just-in-time
 * @param defaultOrgId the organization id JIT users are created in
 * @param priority the probe order for unknown users; lower values are tried first
 */
public record LdapAuthServerSettings(
        LdapServerConfig connectionConfig,
        LdapProvisioningMode provisioningMode,
        Long defaultOrgId,
        int priority) {

    /**
     * Canonical constructor. Requires a connection configuration and defaults a missing
     * provisioning mode to {@link LdapProvisioningMode#JIT}.
     */
    public LdapAuthServerSettings {
        Objects.requireNonNull(connectionConfig, "connectionConfig must not be null");
        provisioningMode = provisioningMode == null ? LdapProvisioningMode.JIT : provisioningMode;
    }

    /**
     * @return {@code true} if unknown users may be created just-in-time on this server
     */
    public boolean allowsJit() {
        return provisioningMode == LdapProvisioningMode.JIT;
    }
}
