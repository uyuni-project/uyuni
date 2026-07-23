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

import java.util.Optional;

/**
 * Authenticates a user against a single directory server and resolves the profile attributes
 * and external group labels needed by the login layer.
 *
 * <p>The service performs the directory work only; it does not touch the Uyuni database, create
 * accounts, or assign roles. Those steps belong to the login orchestration layer.</p>
 */
public interface LdapAuthenticationService {

    /**
     * Attempts to authenticate the given credentials using the bind/search/bind sequence.
     *
     * <p>An empty result means the credentials were rejected (unknown user, ambiguous match,
     * empty or wrong password). An {@link LdapException} means the directory could not be
     * consulted (unreachable host, failed service bind, malformed filter). Callers should treat
     * both as a failed login for the user while logging the exception details for the
     * administrator.</p>
     *
     * @param login the user-supplied login name
     * @param password the user-supplied password
     * @return the authenticated user on success, or empty if the credentials were rejected
     * @throws LdapException if the directory cannot be consulted because of an infrastructure
     *                       or configuration problem
     */
    Optional<LdapUser> authenticate(String login, String password) throws LdapException;
}
