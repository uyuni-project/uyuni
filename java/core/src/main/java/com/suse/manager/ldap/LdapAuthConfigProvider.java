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

import java.util.List;

/**
 * Supplies the LDAP servers the login layer should consult, in priority order.
 *
 * <p>This is the seam between login orchestration and the source of directory configuration. The
 * Phase 2 implementation reads server settings from the product configuration file; a later phase
 * will provide an implementation backed by persisted {@code suseLdapAuthServer} records and the
 * admin UI, without any change to the callers in the login layer.</p>
 */
public interface LdapAuthConfigProvider {

    /**
     * @return {@code true} if native LDAP authentication is configured and enabled; when
     *         {@code false} the login layer skips LDAP entirely and behaves exactly as before
     */
    boolean isEnabled();

    /**
     * @return the enabled directory servers ordered by ascending {@link LdapAuthServerSettings#priority()};
     *         an empty list when LDAP is disabled or unconfigured
     */
    List<LdapAuthServerSettings> getServers();
}
