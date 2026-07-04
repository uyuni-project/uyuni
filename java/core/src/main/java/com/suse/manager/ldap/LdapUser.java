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
import java.util.Objects;
import java.util.Optional;

/**
 * Immutable description of a directory user that has successfully authenticated, together
 * with the profile attributes and external group labels resolved during the bind/search/bind
 * sequence.
 *
 * <p>This object intentionally carries no Uyuni domain types. It is the contract handed back
 * to the login layer, which is responsible for just-in-time provisioning and for mapping
 * {@link #groupLabels()} to Uyuni roles.</p>
 *
 * @param login the normalized login name as read from the configured login attribute
 * @param distinguishedName the full DN of the authenticated entry
 * @param firstName the user's first (given) name, may be {@code null}
 * @param lastName the user's last (family) name, may be {@code null}
 * @param email the user's e-mail address, may be {@code null}
 * @param groupLabels the external group labels the user belongs to, never {@code null}
 */
public record LdapUser(
        String login,
        String distinguishedName,
        String firstName,
        String lastName,
        String email,
        List<String> groupLabels) {

    public LdapUser {
        Objects.requireNonNull(login, "login must not be null");
        Objects.requireNonNull(distinguishedName, "distinguishedName must not be null");
        groupLabels = groupLabels == null ? List.of() : List.copyOf(groupLabels);
    }

    /**
     * @return the first name wrapped in an {@link Optional}
     */
    public Optional<String> firstNameOpt() {
        return Optional.ofNullable(firstName);
    }

    /**
     * @return the last name wrapped in an {@link Optional}
     */
    public Optional<String> lastNameOpt() {
        return Optional.ofNullable(lastName);
    }

    /**
     * @return the e-mail address wrapped in an {@link Optional}
     */
    public Optional<String> emailOpt() {
        return Optional.ofNullable(email);
    }
}
