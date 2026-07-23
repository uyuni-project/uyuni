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

/**
 * Raised when an LDAP operation cannot be completed because of an infrastructure or
 * configuration problem, for example the directory is unreachable, the service account
 * bind fails, or a configured filter is malformed.
 *
 * <p>This is deliberately distinct from an authentication <em>decision</em>: a user who
 * simply supplied the wrong password, or who does not exist in the directory, is reported
 * as an empty result by {@link LdapAuthenticationService#authenticate(String, String)}
 * rather than by throwing. Callers should log the details of this exception for the
 * administrator while presenting only a generic message to the end user.</p>
 */
public class LdapException extends Exception {

    private static final long serialVersionUID = 1L;

    /**
     * Builds an instance with the given message.
     *
     * @param messageIn human readable description of the failure
     */
    public LdapException(String messageIn) {
        super(messageIn);
    }

    /**
     * Builds an instance with the given message and cause.
     *
     * @param messageIn human readable description of the failure
     * @param causeIn the underlying cause
     */
    public LdapException(String messageIn, Exception causeIn) {
        super(messageIn, causeIn);
    }
}
