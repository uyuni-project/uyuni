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
import java.util.Optional;

/**
 * Immutable connection and search configuration for a single directory server.
 *
 * <p>Callers build these instances in code until persisted LDAP server records (and bind
 * credentials from {@code suseCredentials}) are available.</p>
 *
 * <p>Defaults for search filters and attribute names come from the chosen {@link LdapServerType}
 * and can each be overridden through the {@link Builder}.</p>
 */
public final class LdapServerConfig {

    private final LdapServerType serverType;
    private final String host;
    private final int port;
    private final LdapTransport transport;

    private final String bindDn;
    private final String bindPassword;
    private final boolean allowAnonymousBind;

    private final String userBaseDn;
    private final String userFilter;
    private final String loginAttribute;

    private final String groupBaseDn;
    private final String groupFilter;
    private final String groupNameAttribute;

    private final String firstNameAttribute;
    private final String lastNameAttribute;
    private final String emailAttribute;

    private final int connectTimeoutMillis;
    private final int responseTimeoutMillis;

    private LdapServerConfig(Builder builder) {
        this.serverType = builder.serverType;
        this.host = builder.host;
        this.port = builder.port;
        this.transport = builder.transport;
        this.bindDn = builder.bindDn;
        this.bindPassword = builder.bindPassword;
        this.allowAnonymousBind = builder.allowAnonymousBind;
        this.userBaseDn = builder.userBaseDn;
        this.userFilter = builder.userFilter;
        this.loginAttribute = builder.loginAttribute;
        this.groupBaseDn = builder.groupBaseDn;
        this.groupFilter = builder.groupFilter;
        this.groupNameAttribute = builder.groupNameAttribute;
        this.firstNameAttribute = builder.firstNameAttribute;
        this.lastNameAttribute = builder.lastNameAttribute;
        this.emailAttribute = builder.emailAttribute;
        this.connectTimeoutMillis = builder.connectTimeoutMillis;
        this.responseTimeoutMillis = builder.responseTimeoutMillis;
    }

    /**
     * Starts a new builder for the given server type, host and user base DN.
     *
     * @param serverTypeIn the directory flavor, supplies the default filters and attributes
     * @param hostIn the directory host name or address
     * @param userBaseDnIn the base DN under which user entries are searched
     * @return a builder pre-populated with the defaults of the server type
     */
    public static Builder builder(LdapServerType serverTypeIn, String hostIn, String userBaseDnIn) {
        return new Builder(serverTypeIn, hostIn, userBaseDnIn);
    }

    /**
     * @return the directory flavor
     */
    public LdapServerType getServerType() {
        return serverType;
    }

    /**
     * @return the directory host name or address
     */
    public String getHost() {
        return host;
    }

    /**
     * @return the directory TCP port
     */
    public int getPort() {
        return port;
    }

    /**
     * @return the transport security mode
     */
    public LdapTransport getTransport() {
        return transport;
    }

    /**
     * @return the service-account bind DN, empty for an anonymous bind
     */
    public Optional<String> getBindDn() {
        return Optional.ofNullable(bindDn);
    }

    /**
     * @return the service-account bind password, empty for an anonymous bind
     */
    public Optional<String> getBindPassword() {
        return Optional.ofNullable(bindPassword);
    }

    /**
     * @return {@code true} if an anonymous service bind is explicitly permitted
     */
    public boolean isAllowAnonymousBind() {
        return allowAnonymousBind;
    }

    /**
     * @return the base DN under which user entries are searched
     */
    public String getUserBaseDn() {
        return userBaseDn;
    }

    /**
     * @return the user search filter template (contains the {@code {login}} placeholder)
     */
    public String getUserFilter() {
        return userFilter;
    }

    /**
     * @return the attribute carrying the normalized login name
     */
    public String getLoginAttribute() {
        return loginAttribute;
    }

    /**
     * @return the base DN under which group entries are searched
     */
    public String getGroupBaseDn() {
        return groupBaseDn;
    }

    /**
     * @return the group search filter template (contains the {@code {userDn}} placeholder)
     */
    public String getGroupFilter() {
        return groupFilter;
    }

    /**
     * @return the attribute carrying a group's external label
     */
    public String getGroupNameAttribute() {
        return groupNameAttribute;
    }

    /**
     * @return the attribute carrying the user's first (given) name
     */
    public String getFirstNameAttribute() {
        return firstNameAttribute;
    }

    /**
     * @return the attribute carrying the user's last (family) name
     */
    public String getLastNameAttribute() {
        return lastNameAttribute;
    }

    /**
     * @return the attribute carrying the user's e-mail address
     */
    public String getEmailAttribute() {
        return emailAttribute;
    }

    /**
     * @return the TCP connect timeout in milliseconds
     */
    public int getConnectTimeoutMillis() {
        return connectTimeoutMillis;
    }

    /**
     * @return the per-operation response timeout in milliseconds
     */
    public int getResponseTimeoutMillis() {
        return responseTimeoutMillis;
    }

    /**
     * Fluent builder for {@link LdapServerConfig}. Optional values default to those of the
     * configured {@link LdapServerType}; timeouts default to ten seconds.
     */
    public static final class Builder {

        private static final int DEFAULT_TIMEOUT_MILLIS = 10_000;

        private final LdapServerType serverType;
        private final String host;
        private final String userBaseDn;

        private int port;
        private LdapTransport transport = LdapTransport.LDAPS;
        private String bindDn;
        private String bindPassword;
        private boolean allowAnonymousBind;
        private String userFilter;
        private String loginAttribute;
        private String groupBaseDn;
        private String groupFilter;
        private String groupNameAttribute;
        private String firstNameAttribute;
        private String lastNameAttribute;
        private String emailAttribute;
        private int connectTimeoutMillis = DEFAULT_TIMEOUT_MILLIS;
        private int responseTimeoutMillis = DEFAULT_TIMEOUT_MILLIS;

        private Builder(LdapServerType serverTypeIn, String hostIn, String userBaseDnIn) {
            this.serverType = Objects.requireNonNull(serverTypeIn, "serverType must not be null");
            this.host = Objects.requireNonNull(hostIn, "host must not be null");
            this.userBaseDn = Objects.requireNonNull(userBaseDnIn, "userBaseDn must not be null");
            // Leave the port unset (0) so it can follow the transport's default until either the
            // transport or an explicit port is chosen; build() resolves a still-unset port to the
            // transport default.
            this.port = 0;
            this.userFilter = serverTypeIn.getDefaultUserFilter();
            this.loginAttribute = serverTypeIn.getDefaultLoginAttribute();
            this.groupBaseDn = userBaseDnIn;
            this.groupFilter = serverTypeIn.getDefaultGroupFilter();
            this.groupNameAttribute = serverTypeIn.getDefaultGroupNameAttribute();
            this.firstNameAttribute = serverTypeIn.getDefaultFirstNameAttribute();
            this.lastNameAttribute = serverTypeIn.getDefaultLastNameAttribute();
            this.emailAttribute = serverTypeIn.getDefaultEmailAttribute();
        }

        /**
         * @param portIn the directory TCP port
         * @return this builder
         */
        public Builder port(int portIn) {
            this.port = portIn;
            return this;
        }

        /**
         * Sets the transport mode. Also updates the port to that transport's default unless a
         * non-zero port has already been set explicitly.
         *
         * @param transportIn the transport security mode
         * @return this builder
         */
        public Builder transport(LdapTransport transportIn) {
            this.transport = Objects.requireNonNull(transportIn, "transport must not be null");
            if (this.port <= 0) {
                this.port = transportIn.getDefaultPort();
            }
            return this;
        }

        /**
         * Configures a service-account (authenticated) bind.
         *
         * @param bindDnIn the service-account DN
         * @param bindPasswordIn the service-account password
         * @return this builder
         */
        public Builder bind(String bindDnIn, String bindPasswordIn) {
            this.bindDn = bindDnIn;
            this.bindPassword = bindPasswordIn;
            this.allowAnonymousBind = false;
            return this;
        }

        /**
         * Explicitly permits an anonymous service bind (no service-account credentials).
         *
         * @return this builder
         */
        public Builder anonymousBind() {
            this.bindDn = null;
            this.bindPassword = null;
            this.allowAnonymousBind = true;
            return this;
        }

        /**
         * @param userFilterIn the user search filter template (must contain {@code {login}})
         * @return this builder
         */
        public Builder userFilter(String userFilterIn) {
            this.userFilter = userFilterIn;
            return this;
        }

        /**
         * @param loginAttributeIn the attribute carrying the normalized login name
         * @return this builder
         */
        public Builder loginAttribute(String loginAttributeIn) {
            this.loginAttribute = loginAttributeIn;
            return this;
        }

        /**
         * @param groupBaseDnIn the base DN under which group entries are searched
         * @return this builder
         */
        public Builder groupBaseDn(String groupBaseDnIn) {
            this.groupBaseDn = groupBaseDnIn;
            return this;
        }

        /**
         * @param groupFilterIn the group search filter template (must contain {@code {userDn}})
         * @return this builder
         */
        public Builder groupFilter(String groupFilterIn) {
            this.groupFilter = groupFilterIn;
            return this;
        }

        /**
         * @param groupNameAttributeIn the attribute carrying a group's external label
         * @return this builder
         */
        public Builder groupNameAttribute(String groupNameAttributeIn) {
            this.groupNameAttribute = groupNameAttributeIn;
            return this;
        }

        /**
         * Overrides the profile attribute names read from the user entry.
         *
         * @param firstNameAttributeIn first-name attribute
         * @param lastNameAttributeIn last-name attribute
         * @param emailAttributeIn e-mail attribute
         * @return this builder
         */
        public Builder profileAttributes(String firstNameAttributeIn, String lastNameAttributeIn,
                                         String emailAttributeIn) {
            this.firstNameAttribute = firstNameAttributeIn;
            this.lastNameAttribute = lastNameAttributeIn;
            this.emailAttribute = emailAttributeIn;
            return this;
        }

        /**
         * @param connectTimeoutMillisIn TCP connect timeout in milliseconds
         * @return this builder
         */
        public Builder connectTimeoutMillis(int connectTimeoutMillisIn) {
            this.connectTimeoutMillis = connectTimeoutMillisIn;
            return this;
        }

        /**
         * @param responseTimeoutMillisIn per-operation response timeout in milliseconds
         * @return this builder
         */
        public Builder responseTimeoutMillis(int responseTimeoutMillisIn) {
            this.responseTimeoutMillis = responseTimeoutMillisIn;
            return this;
        }

        /**
         * Validates and builds the immutable configuration.
         *
         * @return a new {@link LdapServerConfig}
         * @throws IllegalStateException if the bind configuration is inconsistent
         */
        public LdapServerConfig build() {
            if (port <= 0) {
                port = transport.getDefaultPort();
            }
            boolean hasBindDn = bindDn != null && !bindDn.isBlank();
            if (!hasBindDn && !allowAnonymousBind) {
                throw new IllegalStateException(
                        "A service-account bind DN is required unless anonymous bind is explicitly allowed");
            }
            if (hasBindDn && (bindPassword == null || bindPassword.isEmpty())) {
                throw new IllegalStateException("A bind password is required when a bind DN is set");
            }
            return new LdapServerConfig(this);
        }
    }
}
