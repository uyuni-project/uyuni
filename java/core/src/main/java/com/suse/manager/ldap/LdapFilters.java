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

import com.unboundid.ldap.sdk.Filter;
import com.unboundid.ldap.sdk.LDAPException;

/**
 * Builds search filters from administrator-supplied templates.
 *
 * <p>The only supported placeholders are {@code {login}} (the user-supplied login name) and
 * {@code {userDn}} (the resolved user entry DN). Every substituted value is escaped with
 * {@link Filter#encodeValue(String)} <em>before</em> it is inserted into the template, so that
 * untrusted input can never alter the structure of the resulting filter (LDAP injection).</p>
 */
public final class LdapFilters {

    /** Placeholder replaced by the escaped, user-supplied login name. */
    public static final String LOGIN_PLACEHOLDER = "{login}";

    /** Placeholder replaced by the escaped DN of the resolved user entry. */
    public static final String USER_DN_PLACEHOLDER = "{userDn}";

    private LdapFilters() {
    }

    /**
     * Builds a user search filter by substituting the escaped login into the template.
     *
     * @param template the filter template, must contain {@value #LOGIN_PLACEHOLDER}
     * @param login the user-supplied login name
     * @return a parsed, injection-safe {@link Filter}
     * @throws LdapException if the template is missing the placeholder or is not a valid filter
     */
    public static Filter userFilter(String template, String login) throws LdapException {
        return buildFilter(template, LOGIN_PLACEHOLDER, login, "user");
    }

    /**
     * Builds a group search filter by substituting the escaped user DN into the template.
     *
     * @param template the filter template, must contain {@value #USER_DN_PLACEHOLDER}
     * @param userDn the DN of the resolved user entry
     * @return a parsed, injection-safe {@link Filter}
     * @throws LdapException if the template is missing the placeholder or is not a valid filter
     */
    public static Filter groupFilter(String template, String userDn) throws LdapException {
        return buildFilter(template, USER_DN_PLACEHOLDER, userDn, "group");
    }

    private static Filter buildFilter(String template, String placeholder, String rawValue, String kind)
            throws LdapException {
        if (template == null || !template.contains(placeholder)) {
            throw new LdapException(
                    "The " + kind + " filter template must contain the " + placeholder + " placeholder");
        }
        if (rawValue == null || rawValue.isEmpty()) {
            throw new LdapException("Refusing to build a " + kind + " filter from an empty value");
        }
        String escaped = Filter.encodeValue(rawValue);
        String filterString = template.replace(placeholder, escaped);
        try {
            return Filter.create(filterString);
        }
        catch (LDAPException e) {
            throw new LdapException("Configured " + kind + " filter is not a valid LDAP filter", e);
        }
    }
}
