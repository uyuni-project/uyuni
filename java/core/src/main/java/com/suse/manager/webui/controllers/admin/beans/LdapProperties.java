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

package com.suse.manager.webui.controllers.admin.beans;

/**
 * Gson-friendly request body for creating or updating an LDAP directory server.
 *
 * <p>{@code bindPassword} is write-only: clients may send it on create/update, but responses never
 * echo it back. An empty/null password on update keeps the previously stored secret.</p>
 */
public class LdapProperties {

    private String label;
    private Boolean enabled;
    private Integer priority;
    private String serverType;
    private String host;
    private Integer port;
    private String transport;
    private Integer connectTimeout;
    private Integer responseTimeout;
    private String bindDn;
    private String bindPassword;
    private String userBaseDn;
    private String userFilter;
    private String loginAttribute;
    private String firstNameAttribute;
    private String lastNameAttribute;
    private String emailAttribute;
    private String groupBaseDn;
    private String groupFilter;
    private String groupNameAttribute;
    private Boolean useMemberOf;
    private String provisioningMode;
    private Long defaultOrgId;
    private Boolean autoJoinRegularUser;
    private String rootCa;

    /**
     * Default constructor for Gson.
     */
    public LdapProperties() {
        // Intentionally empty: Gson requires a public no-arg constructor.
    }

    public String getLabel() {
        return label;
    }

    public void setLabel(String labelIn) {
        this.label = labelIn;
    }

    public Boolean getEnabled() {
        return enabled;
    }

    public void setEnabled(Boolean enabledIn) {
        this.enabled = enabledIn;
    }

    public Integer getPriority() {
        return priority;
    }

    public void setPriority(Integer priorityIn) {
        this.priority = priorityIn;
    }

    public String getServerType() {
        return serverType;
    }

    public void setServerType(String serverTypeIn) {
        this.serverType = serverTypeIn;
    }

    public String getHost() {
        return host;
    }

    public void setHost(String hostIn) {
        this.host = hostIn;
    }

    public Integer getPort() {
        return port;
    }

    public void setPort(Integer portIn) {
        this.port = portIn;
    }

    public String getTransport() {
        return transport;
    }

    public void setTransport(String transportIn) {
        this.transport = transportIn;
    }

    public Integer getConnectTimeout() {
        return connectTimeout;
    }

    public void setConnectTimeout(Integer connectTimeoutIn) {
        this.connectTimeout = connectTimeoutIn;
    }

    public Integer getResponseTimeout() {
        return responseTimeout;
    }

    public void setResponseTimeout(Integer responseTimeoutIn) {
        this.responseTimeout = responseTimeoutIn;
    }

    public String getBindDn() {
        return bindDn;
    }

    public void setBindDn(String bindDnIn) {
        this.bindDn = bindDnIn;
    }

    public String getBindPassword() {
        return bindPassword;
    }

    public void setBindPassword(String bindPasswordIn) {
        this.bindPassword = bindPasswordIn;
    }

    public String getUserBaseDn() {
        return userBaseDn;
    }

    public void setUserBaseDn(String userBaseDnIn) {
        this.userBaseDn = userBaseDnIn;
    }

    public String getUserFilter() {
        return userFilter;
    }

    public void setUserFilter(String userFilterIn) {
        this.userFilter = userFilterIn;
    }

    public String getLoginAttribute() {
        return loginAttribute;
    }

    public void setLoginAttribute(String loginAttributeIn) {
        this.loginAttribute = loginAttributeIn;
    }

    public String getFirstNameAttribute() {
        return firstNameAttribute;
    }

    public void setFirstNameAttribute(String firstNameAttributeIn) {
        this.firstNameAttribute = firstNameAttributeIn;
    }

    public String getLastNameAttribute() {
        return lastNameAttribute;
    }

    public void setLastNameAttribute(String lastNameAttributeIn) {
        this.lastNameAttribute = lastNameAttributeIn;
    }

    public String getEmailAttribute() {
        return emailAttribute;
    }

    public void setEmailAttribute(String emailAttributeIn) {
        this.emailAttribute = emailAttributeIn;
    }

    public String getGroupBaseDn() {
        return groupBaseDn;
    }

    public void setGroupBaseDn(String groupBaseDnIn) {
        this.groupBaseDn = groupBaseDnIn;
    }

    public String getGroupFilter() {
        return groupFilter;
    }

    public void setGroupFilter(String groupFilterIn) {
        this.groupFilter = groupFilterIn;
    }

    public String getGroupNameAttribute() {
        return groupNameAttribute;
    }

    public void setGroupNameAttribute(String groupNameAttributeIn) {
        this.groupNameAttribute = groupNameAttributeIn;
    }

    public Boolean getUseMemberOf() {
        return useMemberOf;
    }

    public void setUseMemberOf(Boolean useMemberOfIn) {
        this.useMemberOf = useMemberOfIn;
    }

    public String getProvisioningMode() {
        return provisioningMode;
    }

    public void setProvisioningMode(String provisioningModeIn) {
        this.provisioningMode = provisioningModeIn;
    }

    public Long getDefaultOrgId() {
        return defaultOrgId;
    }

    public void setDefaultOrgId(Long defaultOrgIdIn) {
        this.defaultOrgId = defaultOrgIdIn;
    }

    public Boolean getAutoJoinRegularUser() {
        return autoJoinRegularUser;
    }

    public void setAutoJoinRegularUser(Boolean autoJoinRegularUserIn) {
        this.autoJoinRegularUser = autoJoinRegularUserIn;
    }

    public String getRootCa() {
        return rootCa;
    }

    public void setRootCa(String rootCaIn) {
        this.rootCa = rootCaIn;
    }
}
