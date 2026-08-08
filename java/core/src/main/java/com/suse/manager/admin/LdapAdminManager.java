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

package com.suse.manager.admin;

import com.redhat.rhn.common.hibernate.LookupException;
import com.redhat.rhn.common.validator.ValidatorException;
import com.redhat.rhn.common.validator.ValidatorResult;
import com.redhat.rhn.domain.credentials.CredentialsFactory;
import com.redhat.rhn.domain.credentials.LdapCredentials;
import com.redhat.rhn.domain.org.Org;
import com.redhat.rhn.domain.org.OrgFactory;
import com.redhat.rhn.manager.EntityExistsException;

import com.suse.manager.ldap.DbLdapAuthConfigProvider;
import com.suse.manager.ldap.DefaultLdapServiceFactory;
import com.suse.manager.ldap.LdapConnectionFactory;
import com.suse.manager.ldap.LdapProvisioningMode;
import com.suse.manager.ldap.LdapServerConfig;
import com.suse.manager.ldap.LdapServerType;
import com.suse.manager.ldap.LdapServiceException;
import com.suse.manager.ldap.LdapTransport;
import com.suse.manager.model.ldap.LdapAuthServer;
import com.suse.manager.model.ldap.LdapAuthServerFactory;
import com.suse.manager.webui.controllers.admin.beans.LdapProperties;
import com.suse.utils.CertificateUtils;

import com.unboundid.ldap.sdk.LDAPConnection;

import org.apache.commons.lang3.StringUtils;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.security.cert.CertificateException;
import java.util.List;
import java.util.Locale;

/**
 * Creates, updates, deletes and lists LDAP directory server records for the Setup Wizard admin UI.
 *
 * <p>Reuses {@link LdapAuthServerFactory} and {@link CredentialsFactory} — there is no separate
 * persistence layer. Bind passwords live only in {@link LdapCredentials}; the bind DN stays on the
 * server row.</p>
 */
public class LdapAdminManager {

    private static final Logger LOG = LogManager.getLogger(LdapAdminManager.class);

    private final LdapConnectionFactory connectionFactory;

    /**
     * Creates a manager using the default connection factory.
     */
    public LdapAdminManager() {
        this(new LdapConnectionFactory());
    }

    /**
     * Creates a manager with an injectable connection factory (useful for tests).
     *
     * @param connectionFactoryIn factory used by connection tests
     */
    public LdapAdminManager(LdapConnectionFactory connectionFactoryIn) {
        this.connectionFactory = connectionFactoryIn;
    }

    /**
     * @return all configured LDAP directory servers, ordered by priority
     */
    public List<LdapAuthServer> list() {
        return LdapAuthServerFactory.listAll();
    }

    /**
     * @param id the server id
     * @return the matching server
     * @throws LookupException if the id is unknown
     */
    public LdapAuthServer get(long id) {
        return LdapAuthServerFactory.lookupById(id)
                .orElseThrow(() -> new LookupException("LDAP server not found for id: " + id));
    }

    /**
     * Creates a new LDAP directory server from the UI properties.
     *
     * @param properties request body from the admin UI
     * @return the persisted server
     */
    public LdapAuthServer create(LdapProperties properties) {
        validate(properties, true);

        String label = properties.getLabel().trim();
        if (LdapAuthServerFactory.lookupByLabel(label).isPresent()) {
            throw new EntityExistsException("Duplicated LDAP label: " + label);
        }

        LdapAuthServer server = new LdapAuthServer();
        applyProperties(server, properties, true);
        return LdapAuthServerFactory.save(server);
    }

    /**
     * Updates an existing LDAP directory server.
     *
     * @param id the server id
     * @param properties request body from the admin UI
     * @return the updated server
     */
    public LdapAuthServer update(long id, LdapProperties properties) {
        validate(properties, false);

        LdapAuthServer server = get(id);
        String label = properties.getLabel().trim();
        LdapAuthServerFactory.lookupByLabel(label).ifPresent(existing -> {
            if (!existing.getId().equals(server.getId())) {
                throw new EntityExistsException("Duplicated LDAP label: " + label);
            }
        });

        applyProperties(server, properties, false);
        return LdapAuthServerFactory.save(server);
    }

    /**
     * Deletes an LDAP directory server and its bind credentials, if any.
     *
     * @param id the server id
     * @return {@code true} when the server was removed
     */
    public boolean delete(long id) {
        LdapAuthServer server = get(id);
        LdapCredentials credentials = server.getCredentials();
        if (credentials != null) {
            // Drop the FK before deleting the credential row and server record.
            server.setCredentials(null);
            LdapAuthServerFactory.save(server);
            CredentialsFactory.removeCredentials(credentials);
        }
        LdapAuthServerFactory.remove(server);
        return true;
    }

    /**
     * Opens a connection (and service bind when configured) to verify reachability of the stored
     * server settings.
     *
     * @param id the server id
     * @throws LdapServiceException when the connection or bind fails
     */
    public void testConnection(long id) throws LdapServiceException {
        LdapAuthServer server = get(id);
        LdapServerConfig config = DbLdapAuthConfigProvider.settingsFor(server).connectionConfig();
        // Touch the service factory so the same wiring as login is exercised; the connection
        // factory performs the actual open/bind used for the admin smoke test.
        new DefaultLdapServiceFactory(connectionFactory).getInstance(config);
        try (LDAPConnection connection = connectionFactory.openConnection(config)) {
            if (config.getBindDn().isPresent()) {
                connection.bind(config.getBindDn().get(), config.getBindPassword().orElse(""));
            }
        }
        catch (com.unboundid.ldap.sdk.LDAPException e) {
            throw new LdapServiceException("LDAP connection test failed for server " + config.getHost(), e);
        }
    }

    private void applyProperties(LdapAuthServer server, LdapProperties properties, boolean creating) {
        server.setLabel(properties.getLabel().trim());
        server.setEnabled(properties.getEnabled() == null || properties.getEnabled());
        server.setPriority(properties.getPriority() == null ? 0 : properties.getPriority());
        server.setServerType(LdapServerType.valueOf(properties.getServerType().trim().toUpperCase(Locale.ROOT)));
        server.setHost(properties.getHost().trim());
        LdapTransport transport = LdapTransport.valueOf(properties.getTransport().trim().toUpperCase(Locale.ROOT));
        server.setTransport(transport);
        if (properties.getPort() != null && properties.getPort() > 0) {
            server.setPort(properties.getPort());
        }
        else {
            server.setPort(transport.getDefaultPort());
        }
        server.setConnectTimeout(properties.getConnectTimeout());
        server.setResponseTimeout(properties.getResponseTimeout());
        server.setUserBaseDn(properties.getUserBaseDn().trim());
        server.setUserFilter(blankToNull(properties.getUserFilter()));
        server.setLoginAttribute(blankToNull(properties.getLoginAttribute()));
        server.setFirstNameAttribute(blankToNull(properties.getFirstNameAttribute()));
        server.setLastNameAttribute(blankToNull(properties.getLastNameAttribute()));
        server.setEmailAttribute(blankToNull(properties.getEmailAttribute()));
        server.setGroupBaseDn(blankToNull(properties.getGroupBaseDn()));
        server.setGroupFilter(blankToNull(properties.getGroupFilter()));
        server.setGroupNameAttribute(blankToNull(properties.getGroupNameAttribute()));
        server.setUseMemberOf(Boolean.TRUE.equals(properties.getUseMemberOf()));
        server.setProvisioningMode(parseProvisioningMode(properties.getProvisioningMode()));
        server.setAutoJoinRegularUser(properties.getAutoJoinRegularUser() == null ||
                properties.getAutoJoinRegularUser());
        server.setDefaultOrg(resolveDefaultOrg(properties));
        server.setRootCa(blankToNull(properties.getRootCa()));
        applyBindCredentials(server, properties, creating);
    }

    private void applyBindCredentials(LdapAuthServer server, LdapProperties properties, boolean creating) {
        String bindDn = blankToNull(properties.getBindDn());
        server.setBindDn(bindDn);

        if (bindDn == null) {
            LdapCredentials existing = server.getCredentials();
            server.setCredentials(null);
            if (existing != null) {
                CredentialsFactory.removeCredentials(existing);
            }
            return;
        }

        String password = properties.getBindPassword();
        LdapCredentials credentials = server.getCredentials();
        if (creating || credentials == null) {
            if (StringUtils.isBlank(password)) {
                ValidatorResult result = new ValidatorResult();
                result.addFieldError("bindPassword", "ldap.bind_password_required");
                throw new ValidatorException(result);
            }
            credentials = CredentialsFactory.createLdapCredentials(password);
            CredentialsFactory.storeCredentials(credentials);
            server.setCredentials(credentials);
            return;
        }

        if (StringUtils.isNotEmpty(password)) {
            credentials.setPassword(password);
            CredentialsFactory.storeCredentials(credentials);
        }
    }

    private void validate(LdapProperties properties, boolean creating) {
        ValidatorResult result = new ValidatorResult();
        if (properties == null) {
            result.addError("ldap.properties_required");
            throw new ValidatorException(result);
        }

        requireNonBlank(result, "label", properties.getLabel(), "ldap.label_required");
        requireNonBlank(result, "host", properties.getHost(), "ldap.host_required");
        requireNonBlank(result, "serverType", properties.getServerType(), "ldap.server_type_required");
        requireNonBlank(result, "transport", properties.getTransport(), "ldap.transport_required");
        requireNonBlank(result, "userBaseDn", properties.getUserBaseDn(), "ldap.user_base_dn_required");

        if (properties.getPort() != null && properties.getPort() <= 0) {
            result.addFieldError("port", "ldap.port_invalid");
        }

        if (StringUtils.isNotBlank(properties.getServerType())) {
            try {
                LdapServerType.valueOf(properties.getServerType().trim().toUpperCase(Locale.ROOT));
            }
            catch (IllegalArgumentException e) {
                result.addFieldError("serverType", "ldap.server_type_invalid");
            }
        }
        if (StringUtils.isNotBlank(properties.getTransport())) {
            try {
                LdapTransport.valueOf(properties.getTransport().trim().toUpperCase(Locale.ROOT));
            }
            catch (IllegalArgumentException e) {
                result.addFieldError("transport", "ldap.transport_invalid");
            }
        }
        if (StringUtils.isNotBlank(properties.getProvisioningMode())) {
            try {
                parseProvisioningMode(properties.getProvisioningMode());
            }
            catch (IllegalArgumentException e) {
                result.addFieldError("provisioningMode", "ldap.provisioning_mode_invalid");
            }
        }

        LdapProvisioningMode mode = LdapProvisioningMode.JIT;
        if (StringUtils.isNotBlank(properties.getProvisioningMode())) {
            try {
                mode = parseProvisioningMode(properties.getProvisioningMode());
            }
            catch (IllegalArgumentException ignored) {
                // already reported above
            }
        }
        if (mode == LdapProvisioningMode.JIT && properties.getDefaultOrgId() == null) {
            result.addFieldError("defaultOrgId", "ldap.default_org_required");
        }
        if (properties.getDefaultOrgId() != null && OrgFactory.lookupById(properties.getDefaultOrgId()) == null) {
            result.addFieldError("defaultOrgId", "ldap.default_org_invalid");
        }

        if (creating && StringUtils.isNotBlank(properties.getBindDn()) &&
                StringUtils.isBlank(properties.getBindPassword())) {
            result.addFieldError("bindPassword", "ldap.bind_password_required");
        }

        if (StringUtils.isNotBlank(properties.getRootCa())) {
            try {
                CertificateUtils.parse(properties.getRootCa())
                        .orElseThrow(() -> new CertificateException("empty certificate"));
            }
            catch (CertificateException e) {
                result.addFieldError("rootCa", "ldap.root_ca_invalid");
            }
        }

        if (result.hasErrors()) {
            throw new ValidatorException(result);
        }
    }

    private static void requireNonBlank(ValidatorResult result, String field, String value, String key) {
        if (StringUtils.isBlank(value)) {
            result.addFieldError(field, key);
        }
    }

    private static Org resolveDefaultOrg(LdapProperties properties) {
        if (properties.getDefaultOrgId() == null) {
            return null;
        }
        return OrgFactory.lookupById(properties.getDefaultOrgId());
    }

    private static LdapProvisioningMode parseProvisioningMode(String value) {
        if (StringUtils.isBlank(value)) {
            return LdapProvisioningMode.JIT;
        }
        return LdapProvisioningMode.valueOf(value.trim().toUpperCase(Locale.ROOT));
    }

    private static String blankToNull(String value) {
        return StringUtils.isBlank(value) ? null : value.trim();
    }
}
