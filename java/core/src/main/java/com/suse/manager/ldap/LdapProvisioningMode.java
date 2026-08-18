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

import java.util.Locale;

/**
 * Controls whether a successful LDAP authentication may create a Uyuni account on the fly.
 */
public enum LdapProvisioningMode {

    /** Create the Uyuni user just-in-time on first successful LDAP login if it does not exist. */
    JIT,

    /** Authenticate only users that already exist in Uyuni; never create accounts from LDAP. */
    EXISTING_ONLY;

    /**
     * Resolves a mode from its stored label, defaulting to {@link #JIT} for an unknown value.
     *
     * @param label the stored label, case-insensitive
     * @return the matching mode, or {@link #JIT} if the label cannot be resolved
     */
    public static LdapProvisioningMode fromLabel(String label) {
        if (label == null || label.isBlank()) {
            return JIT;
        }
        try {
            return valueOf(label.trim().toUpperCase(Locale.ROOT));
        }
        catch (IllegalArgumentException e) {
            return JIT;
        }
    }
}
