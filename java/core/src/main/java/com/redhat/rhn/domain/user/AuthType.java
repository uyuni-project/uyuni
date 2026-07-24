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

package com.redhat.rhn.domain.user;

import java.util.Locale;

/**
 * Backend that authenticates a given user's password.
 *
 * <p>Stored per user on {@code rhnUserInfo.auth_type} and used by the login layer to route each
 * known user to exactly one backend without cascading between them. Existing users are migrated
 * from the legacy {@code use_pam_authentication} flag: {@code Y} becomes {@link #PAM}, everything
 * else becomes {@link #LOCAL}. Just-in-time provisioned directory users are stored as
 * {@link #LDAP}.</p>
 */
public enum AuthType {

    /** Local database password check ({@code UserImpl.authenticate}, non-PAM branch). */
    LOCAL,

    /** Host PAM stack ({@code UserImpl.authenticate}, PAM branch); replaces {@code use_pam_authentication}. */
    PAM,

    /** Native LDAP/Active Directory bind handled by the login orchestration layer. */
    LDAP;

    /**
     * Resolves an {@link AuthType} from its stored label, falling back to {@link #LOCAL} for a
     * {@code null}, blank, or unrecognized value so that a bad row can never crash a login.
     *
     * @param label the stored label, case-insensitive
     * @return the matching auth type, or {@link #LOCAL} if the label cannot be resolved
     */
    public static AuthType fromLabel(String label) {
        if (label == null || label.isBlank()) {
            return LOCAL;
        }
        try {
            return valueOf(label.trim().toUpperCase(Locale.ROOT));
        }
        catch (IllegalArgumentException e) {
            return LOCAL;
        }
    }
}
