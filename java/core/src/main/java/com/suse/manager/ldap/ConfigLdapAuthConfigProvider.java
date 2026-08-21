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

import com.redhat.rhn.common.conf.Config;

import org.apache.commons.lang3.StringUtils;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.util.List;
import java.util.Locale;
import java.util.Optional;
import java.util.function.Function;
import java.util.function.UnaryOperator;

/**
 * Interim {@link LdapAuthConfigProvider} that reads a single directory server from the product
 * configuration file ({@code rhn.conf}). Persisted directory records are the source of truth; this
 * provider is only consulted through {@link DefaultLdapAuthConfigProvider} while none exists, so
 * that native LDAP can still be set up before the LDAP administration UI lands.
 *
 * <p>LDAP is disabled unless {@code ldap.auth_enabled = 1} is set, so on a default installation
 * this provider returns no servers and login behaves exactly as it did before. A misconfigured
 * but enabled setup logs the problem and also returns no servers rather than throwing, so a
 * configuration mistake can never take down the login page.</p>
 *
 * <p>Recognized keys (all prefixed {@code ldap.}):</p>
 * <ul>
 *   <li>{@code auth_enabled} - {@code 1}/{@code true} to enable (default off)</li>
 *   <li>{@code server_type} - {@code ACTIVE_DIRECTORY}, {@code FREE_IPA} or {@code OPEN_LDAP}</li>
 *   <li>{@code host}, {@code port}, {@code transport} ({@code PLAIN}/{@code LDAPS}/{@code STARTTLS})</li>
 *   <li>{@code bind_dn}, {@code bind_password} (omit both to attempt an anonymous bind)</li>
 *   <li>{@code user_base_dn} (required), {@code user_filter}, {@code login_attribute}</li>
 *   <li>{@code group_base_dn}, {@code group_filter}, {@code group_name_attribute}</li>
 *   <li>{@code use_memberof} - {@code 1}/{@code true} to resolve groups via {@code memberOf}</li>
 *   <li>{@code provisioning_mode} ({@code JIT}/{@code EXISTING_ONLY}), {@code default_org_id}</li>
 * </ul>
 */
public class ConfigLdapAuthConfigProvider implements LdapAuthConfigProvider {

    /** Configuration key prefix for all native LDAP settings. */
    public static final String PREFIX = "ldap.";
    /** Key gating whether native LDAP authentication is active. */
    public static final String KEY_ENABLED = PREFIX + "auth_enabled";

    private static final Logger LOG = LogManager.getLogger(ConfigLdapAuthConfigProvider.class);
    private static final long DEFAULT_ORG_ID = 1L;

    private final UnaryOperator<String> stringReader;
    private final Function<String, Boolean> booleanReader;
    private final Function<String, Integer> intReader;

    /**
     * Creates a provider backed by the global {@link Config}.
     */
    public ConfigLdapAuthConfigProvider() {
        this(key -> Config.get().getString(key),
             key -> Config.get().getBoolean(key),
             key -> Config.get().getInt(key, 0));
    }

    /**
     * Creates a provider backed by explicit readers. Mainly useful for tests that want to feed
     * configuration values without a live {@link Config}.
     *
     * @param stringReaderIn reads a string value for a key, returning {@code null} if unset
     * @param booleanReaderIn reads a boolean value for a key
     * @param intReaderIn reads an int value for a key, returning {@code 0} if unset
     */
    public ConfigLdapAuthConfigProvider(UnaryOperator<String> stringReaderIn,
                                        Function<String, Boolean> booleanReaderIn,
                                        Function<String, Integer> intReaderIn) {
        this.stringReader = stringReaderIn;
        this.booleanReader = booleanReaderIn;
        this.intReader = intReaderIn;
    }

    @Override
    public boolean isEnabled() {
        return Boolean.TRUE.equals(booleanReader.apply(KEY_ENABLED));
    }

    @Override
    public List<LdapAuthServerSettings> getServers() {
        if (!isEnabled()) {
            return List.of();
        }
        try {
            return List.of(buildFromConfig());
        }
        catch (IllegalArgumentException | IllegalStateException e) {
            LOG.error("Native LDAP authentication is enabled but misconfigured; skipping LDAP: {}",
                    e.getMessage());
            return List.of();
        }
    }

    private LdapAuthServerSettings buildFromConfig() {
        LdapServerType serverType = parseServerType(str("server_type"));
        String host = requireValue("host", str("host"));
        String userBaseDn = requireValue("user_base_dn", str("user_base_dn"));

        LdapServerConfig.Builder builder = LdapServerConfig.builder(serverType, host, userBaseDn);

        parseTransport(str("transport")).ifPresent(builder::transport);
        int port = intReader.apply(PREFIX + "port");
        if (port > 0) {
            builder.port(port);
        }

        String bindDn = str("bind_dn");
        String bindPassword = str("bind_password");
        if (StringUtils.isNotBlank(bindDn)) {
            builder.bind(bindDn, bindPassword);
        }
        else {
            builder.anonymousBind();
        }

        applyIfSet("user_filter", builder::userFilter);
        applyIfSet("login_attribute", builder::loginAttribute);
        applyIfSet("group_base_dn", builder::groupBaseDn);
        applyIfSet("group_filter", builder::groupFilter);
        applyIfSet("group_name_attribute", builder::groupNameAttribute);
        if (Boolean.TRUE.equals(booleanReader.apply(PREFIX + "use_memberof"))) {
            builder.useMemberOf(true);
        }

        LdapProvisioningMode mode = LdapProvisioningMode.fromLabel(str("provisioning_mode"));
        int orgId = intReader.apply(PREFIX + "default_org_id");
        Long defaultOrgId = orgId > 0 ? (long) orgId : DEFAULT_ORG_ID;

        // No persisted record backs these settings, hence the null server id.
        return new LdapAuthServerSettings(null, builder.build(), mode, defaultOrgId, true, 0);
    }

    private void applyIfSet(String key, Function<String, LdapServerConfig.Builder> setter) {
        String value = str(key);
        if (StringUtils.isNotBlank(value)) {
            setter.apply(value);
        }
    }

    private String str(String key) {
        return stringReader.apply(PREFIX + key);
    }

    private static String requireValue(String key, String value) {
        if (StringUtils.isBlank(value)) {
            throw new IllegalArgumentException("missing required key " + PREFIX + key);
        }
        return value;
    }

    private static LdapServerType parseServerType(String value) {
        if (StringUtils.isBlank(value)) {
            return LdapServerType.OPEN_LDAP;
        }
        try {
            return LdapServerType.valueOf(value.trim().toUpperCase(Locale.ROOT));
        }
        catch (IllegalArgumentException e) {
            throw new IllegalArgumentException("unknown " + PREFIX + "server_type '" + value + "'");
        }
    }

    private static Optional<LdapTransport> parseTransport(String value) {
        if (StringUtils.isBlank(value)) {
            return Optional.empty();
        }
        try {
            return Optional.of(LdapTransport.valueOf(value.trim().toUpperCase(Locale.ROOT)));
        }
        catch (IllegalArgumentException e) {
            throw new IllegalArgumentException("unknown " + PREFIX + "transport '" + value + "'");
        }
    }
}
