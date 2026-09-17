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
 * Well-known directory server flavors. Each value supplies sensible default search filters
 * and attribute names so an administrator only has to override values that differ from the
 * defaults for their directory.
 *
 * <p>Filter templates use two placeholders that {@link LdapFilters} substitutes at search time:
 * {@code {login}} for the supplied user name and {@code {userDn}} for the resolved user entry DN.
 * Placeholder values are always LDAP-escaped before substitution.</p>
 */
public enum LdapServerType {

    /** Microsoft Active Directory. */
    ACTIVE_DIRECTORY(
            "(&(objectClass=user)(sAMAccountName={login}))",
            "sAMAccountName",
            "(&(objectClass=group)(member={userDn}))",
            "cn",
            "givenName",
            "sn",
            "mail"),

    /** FreeIPA / Red Hat Identity Management (389 Directory Server based). */
    FREE_IPA(
            "(&(objectClass=person)(uid={login}))",
            "uid",
            "(&(objectClass=groupOfNames)(member={userDn}))",
            "cn",
            "givenName",
            "sn",
            "mail"),

    /** Generic OpenLDAP using inetOrgPerson and groupOfNames. */
    OPEN_LDAP(
            "(&(objectClass=inetOrgPerson)(uid={login}))",
            "uid",
            "(&(objectClass=groupOfNames)(member={userDn}))",
            "cn",
            "givenName",
            "sn",
            "mail");

    private final String defaultUserFilter;
    private final String defaultLoginAttribute;
    private final String defaultGroupFilter;
    private final String defaultGroupNameAttribute;
    private final String defaultFirstNameAttribute;
    private final String defaultLastNameAttribute;
    private final String defaultEmailAttribute;

    @SuppressWarnings("checkstyle:ParameterNumber")
    LdapServerType(String userFilterIn, String loginAttributeIn, String groupFilterIn,
                   String groupNameAttributeIn, String firstNameAttributeIn, String lastNameAttributeIn,
                   String emailAttributeIn) {
        this.defaultUserFilter = userFilterIn;
        this.defaultLoginAttribute = loginAttributeIn;
        this.defaultGroupFilter = groupFilterIn;
        this.defaultGroupNameAttribute = groupNameAttributeIn;
        this.defaultFirstNameAttribute = firstNameAttributeIn;
        this.defaultLastNameAttribute = lastNameAttributeIn;
        this.defaultEmailAttribute = emailAttributeIn;
    }

    /**
     * @return default user search filter template (contains the {@code {login}} placeholder)
     */
    public String getDefaultUserFilter() {
        return defaultUserFilter;
    }

    /**
     * @return default attribute carrying the normalized login name
     */
    public String getDefaultLoginAttribute() {
        return defaultLoginAttribute;
    }

    /**
     * @return default group search filter template (contains the {@code {userDn}} placeholder)
     */
    public String getDefaultGroupFilter() {
        return defaultGroupFilter;
    }

    /**
     * @return default attribute carrying a group's external label
     */
    public String getDefaultGroupNameAttribute() {
        return defaultGroupNameAttribute;
    }

    /**
     * @return default attribute carrying the user's first (given) name
     */
    public String getDefaultFirstNameAttribute() {
        return defaultFirstNameAttribute;
    }

    /**
     * @return default attribute carrying the user's last (family) name
     */
    public String getDefaultLastNameAttribute() {
        return defaultLastNameAttribute;
    }

    /**
     * @return default attribute carrying the user's e-mail address
     */
    public String getDefaultEmailAttribute() {
        return defaultEmailAttribute;
    }
}
