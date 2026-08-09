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

package com.suse.manager.model.ldap;

import com.redhat.rhn.domain.credentials.LdapCredentials;
import com.redhat.rhn.domain.org.Org;

import com.suse.manager.ldap.LdapProvisioningMode;
import com.suse.manager.ldap.LdapServerType;
import com.suse.manager.ldap.LdapTransport;

import java.io.Serializable;
import java.util.Date;
import java.util.Optional;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.SequenceGenerator;
import jakarta.persistence.Table;
import jakarta.persistence.Temporal;
import jakarta.persistence.TemporalType;

/**
 * A directory server the login layer authenticates against, as configured by an administrator.
 *
 * <p>One row per directory. The connection and search settings are kept in separate columns rather
 * than a single blob so they stay queryable and can be edited field by field from the admin UI. The
 * bind password is not stored here: it lives in {@code suseCredentials} as {@link LdapCredentials},
 * because {@code suseCredentials.username} is too short to also hold the bind DN, the bind DN is
 * kept on this record instead. A record with neither a bind DN nor credentials performs an
 * anonymous bind.</p>
 *
 * <p>Attribute and filter columns are nullable: a {@code null} means "use the default of the
 * configured {@link LdapServerType}", so an administrator only stores what actually differs from
 * their directory flavor's defaults.</p>
 */
@Entity
@Table(name = "suseLdapAuthServer")
public class LdapAuthServer implements Serializable {

    @Id
    @Column(name = "id")
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "suse_ldap_auth_srv_seq")
    @SequenceGenerator(name = "suse_ldap_auth_srv_seq", sequenceName = "suse_ldap_auth_srv_id_seq",
            allocationSize = 1)
    private Long id;

    @Column(name = "label", nullable = false)
    private String label;

    @Column(name = "enabled", nullable = false)
    private boolean enabled = true;

    @Column(name = "priority", nullable = false)
    private int priority;

    @Enumerated(EnumType.STRING)
    @Column(name = "server_type", nullable = false)
    private LdapServerType serverType;

    @Column(name = "host", nullable = false)
    private String host;

    @Column(name = "port", nullable = false)
    private int port;

    @Enumerated(EnumType.STRING)
    @Column(name = "transport", nullable = false)
    private LdapTransport transport = LdapTransport.LDAPS;

    @Column(name = "connect_timeout")
    private Integer connectTimeout;

    @Column(name = "response_timeout")
    private Integer responseTimeout;

    @Column(name = "bind_dn")
    private String bindDn;

    @ManyToOne
    @JoinColumn(name = "credentials_id")
    private LdapCredentials credentials;

    @Column(name = "user_base_dn", nullable = false)
    private String userBaseDn;

    @Column(name = "user_filter")
    private String userFilter;

    @Column(name = "login_attribute")
    private String loginAttribute;

    @Column(name = "first_name_attribute")
    private String firstNameAttribute;

    @Column(name = "last_name_attribute")
    private String lastNameAttribute;

    @Column(name = "email_attribute")
    private String emailAttribute;

    @Column(name = "group_base_dn")
    private String groupBaseDn;

    @Column(name = "group_filter")
    private String groupFilter;

    @Column(name = "group_name_attribute")
    private String groupNameAttribute;

    @Column(name = "use_memberof", nullable = false)
    private boolean useMemberOf;

    @Enumerated(EnumType.STRING)
    @Column(name = "provisioning_mode", nullable = false)
    private LdapProvisioningMode provisioningMode = LdapProvisioningMode.JIT;

    @ManyToOne
    @JoinColumn(name = "default_org_id")
    private Org defaultOrg;

    @Column(name = "auto_join_regular_user", nullable = false)
    private boolean autoJoinRegularUser = true;

    @Column(name = "root_ca")
    private String rootCa;

    @Temporal(TemporalType.TIMESTAMP)
    @Column(name = "created", insertable = false, updatable = false)
    private Date created;

    @Temporal(TemporalType.TIMESTAMP)
    @Column(name = "modified", nullable = false)
    private Date modified = new Date();

    /**
     * @return the surrogate key, {@code null} until the record is persisted
     */
    public Long getId() {
        return id;
    }

    /**
     * @param idIn the surrogate key
     */
    public void setId(Long idIn) {
        this.id = idIn;
    }

    /**
     * @return the administrator-facing unique name of this directory
     */
    public String getLabel() {
        return label;
    }

    /**
     * @param labelIn the administrator-facing unique name of this directory
     */
    public void setLabel(String labelIn) {
        this.label = labelIn;
    }

    /**
     * @return {@code true} if the login layer may consult this directory
     */
    public boolean isEnabled() {
        return enabled;
    }

    /**
     * @param enabledIn whether the login layer may consult this directory
     */
    public void setEnabled(boolean enabledIn) {
        this.enabled = enabledIn;
    }

    /**
     * @return the probe order for unknown users; lower values are tried first
     */
    public int getPriority() {
        return priority;
    }

    /**
     * @param priorityIn the probe order for unknown users; lower values are tried first
     */
    public void setPriority(int priorityIn) {
        this.priority = priorityIn;
    }

    /**
     * @return the directory flavor, which supplies the filter and attribute defaults
     */
    public LdapServerType getServerType() {
        return serverType;
    }

    /**
     * @param serverTypeIn the directory flavor
     */
    public void setServerType(LdapServerType serverTypeIn) {
        this.serverType = serverTypeIn;
    }

    /**
     * @return the directory host name or address
     */
    public String getHost() {
        return host;
    }

    /**
     * @param hostIn the directory host name or address
     */
    public void setHost(String hostIn) {
        this.host = hostIn;
    }

    /**
     * @return the directory TCP port
     */
    public int getPort() {
        return port;
    }

    /**
     * @param portIn the directory TCP port
     */
    public void setPort(int portIn) {
        this.port = portIn;
    }

    /**
     * @return the transport security mode
     */
    public LdapTransport getTransport() {
        return transport;
    }

    /**
     * @param transportIn the transport security mode
     */
    public void setTransport(LdapTransport transportIn) {
        this.transport = transportIn;
    }

    /**
     * @return the TCP connect timeout in milliseconds, empty to use the built-in default
     */
    public Optional<Integer> getConnectTimeout() {
        return Optional.ofNullable(connectTimeout);
    }

    /**
     * @param connectTimeoutIn the TCP connect timeout in milliseconds, {@code null} for the default
     */
    public void setConnectTimeout(Integer connectTimeoutIn) {
        this.connectTimeout = connectTimeoutIn;
    }

    /**
     * @return the per-operation response timeout in milliseconds, empty to use the built-in default
     */
    public Optional<Integer> getResponseTimeout() {
        return Optional.ofNullable(responseTimeout);
    }

    /**
     * @param responseTimeoutIn the response timeout in milliseconds, {@code null} for the default
     */
    public void setResponseTimeout(Integer responseTimeoutIn) {
        this.responseTimeout = responseTimeoutIn;
    }

    /**
     * @return the service-account bind DN, {@code null} for an anonymous bind
     */
    public String getBindDn() {
        return bindDn;
    }

    /**
     * @param bindDnIn the service-account bind DN, {@code null} for an anonymous bind
     */
    public void setBindDn(String bindDnIn) {
        this.bindDn = bindDnIn;
    }

    /**
     * @return the credentials holding the bind password, {@code null} for an anonymous bind
     */
    public LdapCredentials getCredentials() {
        return credentials;
    }

    /**
     * @param credentialsIn the credentials holding the bind password
     */
    public void setCredentials(LdapCredentials credentialsIn) {
        this.credentials = credentialsIn;
    }

    /**
     * @return the base DN under which user entries are searched
     */
    public String getUserBaseDn() {
        return userBaseDn;
    }

    /**
     * @param userBaseDnIn the base DN under which user entries are searched
     */
    public void setUserBaseDn(String userBaseDnIn) {
        this.userBaseDn = userBaseDnIn;
    }

    /**
     * @return the user search filter template, {@code null} to use the server type default
     */
    public String getUserFilter() {
        return userFilter;
    }

    /**
     * @param userFilterIn the user search filter template, {@code null} for the default
     */
    public void setUserFilter(String userFilterIn) {
        this.userFilter = userFilterIn;
    }

    /**
     * @return the attribute carrying the normalized login name, {@code null} for the default
     */
    public String getLoginAttribute() {
        return loginAttribute;
    }

    /**
     * @param loginAttributeIn the attribute carrying the normalized login name
     */
    public void setLoginAttribute(String loginAttributeIn) {
        this.loginAttribute = loginAttributeIn;
    }

    /**
     * @return the attribute carrying the user's first name, {@code null} for the default
     */
    public String getFirstNameAttribute() {
        return firstNameAttribute;
    }

    /**
     * @param firstNameAttributeIn the attribute carrying the user's first name
     */
    public void setFirstNameAttribute(String firstNameAttributeIn) {
        this.firstNameAttribute = firstNameAttributeIn;
    }

    /**
     * @return the attribute carrying the user's last name, {@code null} for the default
     */
    public String getLastNameAttribute() {
        return lastNameAttribute;
    }

    /**
     * @param lastNameAttributeIn the attribute carrying the user's last name
     */
    public void setLastNameAttribute(String lastNameAttributeIn) {
        this.lastNameAttribute = lastNameAttributeIn;
    }

    /**
     * @return the attribute carrying the user's e-mail address, {@code null} for the default
     */
    public String getEmailAttribute() {
        return emailAttribute;
    }

    /**
     * @param emailAttributeIn the attribute carrying the user's e-mail address
     */
    public void setEmailAttribute(String emailAttributeIn) {
        this.emailAttribute = emailAttributeIn;
    }

    /**
     * @return the base DN under which group entries are searched, {@code null} to reuse the user base DN
     */
    public String getGroupBaseDn() {
        return groupBaseDn;
    }

    /**
     * @param groupBaseDnIn the base DN under which group entries are searched
     */
    public void setGroupBaseDn(String groupBaseDnIn) {
        this.groupBaseDn = groupBaseDnIn;
    }

    /**
     * @return the group search filter template, {@code null} to use the server type default
     */
    public String getGroupFilter() {
        return groupFilter;
    }

    /**
     * @param groupFilterIn the group search filter template
     */
    public void setGroupFilter(String groupFilterIn) {
        this.groupFilter = groupFilterIn;
    }

    /**
     * @return the attribute carrying a group's external label, {@code null} for the default
     */
    public String getGroupNameAttribute() {
        return groupNameAttribute;
    }

    /**
     * @param groupNameAttributeIn the attribute carrying a group's external label
     */
    public void setGroupNameAttribute(String groupNameAttributeIn) {
        this.groupNameAttribute = groupNameAttributeIn;
    }

    /**
     * @return {@code true} if group resolution uses the user entry's {@code memberOf} attribute
     *         (including Active Directory ranged attribute pages) instead of a group-tree search
     */
    public boolean isUseMemberOf() {
        return useMemberOf;
    }

    /**
     * @param useMemberOfIn whether group resolution may use the {@code memberOf} attribute
     */
    public void setUseMemberOf(boolean useMemberOfIn) {
        this.useMemberOf = useMemberOfIn;
    }

    /**
     * @return whether unknown users may be created just-in-time on this directory
     */
    public LdapProvisioningMode getProvisioningMode() {
        return provisioningMode;
    }

    /**
     * @param provisioningModeIn whether unknown users may be created just-in-time
     */
    public void setProvisioningMode(LdapProvisioningMode provisioningModeIn) {
        this.provisioningMode = provisioningModeIn;
    }

    /**
     * @return the organization just-in-time provisioned users are created in
     */
    public Org getDefaultOrg() {
        return defaultOrg;
    }

    /**
     * @param defaultOrgIn the organization just-in-time provisioned users are created in
     */
    public void setDefaultOrg(Org defaultOrgIn) {
        this.defaultOrg = defaultOrgIn;
    }

    /**
     * @return {@code true} if provisioned users join the {@code regular_user} access group, as
     *         every locally created user does; {@code false} leaves access entirely to LDAP group
     *         mapping
     */
    public boolean isAutoJoinRegularUser() {
        return autoJoinRegularUser;
    }

    /**
     * @param autoJoinRegularUserIn whether provisioned users join the {@code regular_user} access group
     */
    public void setAutoJoinRegularUser(boolean autoJoinRegularUserIn) {
        this.autoJoinRegularUser = autoJoinRegularUserIn;
    }

    /**
     * @return the PEM-encoded public CA used to trust this directory over LDAPS/STARTTLS, or
     *         {@code null} to rely on the JVM default trust store
     */
    public String getRootCa() {
        return rootCa;
    }

    /**
     * @param rootCaIn the PEM-encoded public CA, or {@code null} to clear it
     */
    public void setRootCa(String rootCaIn) {
        this.rootCa = rootCaIn;
    }

    /**
     * @return the creation timestamp, maintained by the database
     */
    public Date getCreated() {
        return created;
    }

    /**
     * @return the last modification timestamp, refreshed whenever the record is saved
     */
    public Date getModified() {
        return modified;
    }

    /**
     * @param modifiedIn the last modification timestamp
     */
    public void setModified(Date modifiedIn) {
        this.modified = modifiedIn;
    }

    @Override
    public String toString() {
        return "LdapAuthServer[id=" + id + ", label=" + label + ", host=" + host + ", enabled=" + enabled + "]";
    }
}
