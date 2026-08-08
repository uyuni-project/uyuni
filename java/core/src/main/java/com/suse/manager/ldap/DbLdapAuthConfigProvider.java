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

import com.redhat.rhn.common.util.StringUtil;
import com.redhat.rhn.domain.credentials.LdapCredentials;
import com.redhat.rhn.domain.org.Org;

import com.suse.manager.model.ldap.LdapAuthServer;
import com.suse.manager.model.ldap.LdapAuthServerFactory;

import org.apache.commons.lang3.StringUtils;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Consumer;
import java.util.function.Supplier;

/**
 * {@link LdapAuthConfigProvider} backed by the {@code suseLdapAuthServer} records an administrator
 * maintains. This is the source of truth for native LDAP configuration.
 *
 * <p>Native LDAP is active exactly when at least one enabled directory record exists, so a default
 * installation has no LDAP configured and login behaves as it did before. A record that cannot be
 * turned into a usable connection (for example a bind DN without a stored password) is logged and
 * skipped rather than raised, so one broken row can never take down the login page or hide the
 * remaining directories.</p>
 */
public class DbLdapAuthConfigProvider implements LdapAuthConfigProvider {

    private static final Logger LOG = LogManager.getLogger(DbLdapAuthConfigProvider.class);

    private final Supplier<List<LdapAuthServer>> enabledServersSupplier;

    /**
     * Creates a provider reading the persisted directory records.
     */
    public DbLdapAuthConfigProvider() {
        this(LdapAuthServerFactory::listEnabled);
    }

    /**
     * Creates a provider reading directories from the given supplier. Mainly useful for tests that
     * want to feed records without a live database.
     *
     * @param enabledServersSupplierIn supplies the enabled directories in probe order
     */
    public DbLdapAuthConfigProvider(Supplier<List<LdapAuthServer>> enabledServersSupplierIn) {
        this.enabledServersSupplier = enabledServersSupplierIn;
    }

    @Override
    public boolean isEnabled() {
        return !getServers().isEmpty();
    }

    @Override
    public List<LdapAuthServerSettings> getServers() {
        List<LdapAuthServerSettings> settings = new ArrayList<>();
        for (LdapAuthServer server : enabledServersSupplier.get()) {
            try {
                settings.add(toSettings(server));
            }
            catch (IllegalArgumentException | IllegalStateException e) {
                if (LOG.isErrorEnabled()) {
                    LOG.error("Skipping misconfigured LDAP server '{}': {}",
                            StringUtil.sanitizeLogInput(server.getLabel()), e.getMessage());
                }
            }
        }
        return settings;
    }

    private static LdapAuthServerSettings toSettings(LdapAuthServer server) {
        LdapServerConfig.Builder builder = LdapServerConfig
                .builder(requireServerType(server), requireValue("host", server.getHost()),
                        requireValue("user base DN", server.getUserBaseDn()))
                .transport(requireTransport(server));

        if (server.getPort() > 0) {
            builder.port(server.getPort());
        }
        server.getConnectTimeout().ifPresent(builder::connectTimeoutMillis);
        server.getResponseTimeout().ifPresent(builder::responseTimeoutMillis);

        applyBind(server, builder);
        applyIfSet(server.getUserFilter(), builder::userFilter);
        applyIfSet(server.getLoginAttribute(), builder::loginAttribute);
        applyIfSet(server.getGroupBaseDn(), builder::groupBaseDn);
        applyIfSet(server.getGroupFilter(), builder::groupFilter);
        applyIfSet(server.getGroupNameAttribute(), builder::groupNameAttribute);
        applyProfileAttributes(server, builder);

        return new LdapAuthServerSettings(server.getId(), builder.build(), server.getProvisioningMode(),
                orgId(server), server.isAutoJoinRegularUser(), server.getPriority());
    }

    private static void applyBind(LdapAuthServer server, LdapServerConfig.Builder builder) {
        LdapCredentials credentials = server.getCredentials();
        String bindPassword = credentials == null ? null : credentials.getPassword();
        if (StringUtils.isNotBlank(server.getBindDn())) {
            if (StringUtils.isBlank(bindPassword)) {
                throw new IllegalStateException("bind DN is set but no bind password is stored");
            }
            builder.bind(server.getBindDn(), bindPassword);
        }
        else {
            // No bind DN configured: the directory is searched anonymously.
            builder.anonymousBind();
        }
    }

    /**
     * Overrides the profile attribute names only when the record actually customizes one of them;
     * otherwise the defaults of the server type are kept. The builder sets all three at once, so a
     * blank column falls back to the server type default rather than clearing the attribute.
     */
    private static void applyProfileAttributes(LdapAuthServer server, LdapServerConfig.Builder builder) {
        LdapServerType type = server.getServerType();
        String firstName = server.getFirstNameAttribute();
        String lastName = server.getLastNameAttribute();
        String email = server.getEmailAttribute();
        if (StringUtils.isAllBlank(firstName, lastName, email)) {
            return;
        }
        builder.profileAttributes(
                StringUtils.defaultIfBlank(firstName, type.getDefaultFirstNameAttribute()),
                StringUtils.defaultIfBlank(lastName, type.getDefaultLastNameAttribute()),
                StringUtils.defaultIfBlank(email, type.getDefaultEmailAttribute()));
    }

    private static void applyIfSet(String value, Consumer<String> setter) {
        if (StringUtils.isNotBlank(value)) {
            setter.accept(value);
        }
    }

    private static Long orgId(LdapAuthServer server) {
        Org org = server.getDefaultOrg();
        return org == null ? null : org.getId();
    }

    private static LdapServerType requireServerType(LdapAuthServer server) {
        if (server.getServerType() == null) {
            throw new IllegalArgumentException("missing server type");
        }
        return server.getServerType();
    }

    private static LdapTransport requireTransport(LdapAuthServer server) {
        if (server.getTransport() == null) {
            throw new IllegalArgumentException("missing transport");
        }
        return server.getTransport();
    }

    private static String requireValue(String name, String value) {
        if (StringUtils.isBlank(value)) {
            throw new IllegalArgumentException("missing " + name);
        }
        return value;
    }
}
