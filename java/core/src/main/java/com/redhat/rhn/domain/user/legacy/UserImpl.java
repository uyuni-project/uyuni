/*
 * Copyright (c) 2025 SUSE LLC
 * Copyright (c) 2009--2015 Red Hat, Inc.
 *
 * This software is licensed to you under the GNU General Public License,
 * version 2 (GPLv2). There is NO WARRANTY for this software, express or
 * implied, including the implied warranties of MERCHANTABILITY or FITNESS
 * FOR A PARTICULAR PURPOSE. You should have received a copy of GPLv2
 * along with this software; if not, see
 * http://www.gnu.org/licenses/old-licenses/gpl-2.0.txt.
 *
 * Red Hat trademarks are not licensed under GPLv2. No permission is
 * granted to use or replicate Red Hat trademarks that are incorporated
 * in this software or its documentation.
 */

package com.redhat.rhn.domain.user.legacy;

import com.redhat.rhn.common.conf.Config;
import com.redhat.rhn.common.conf.ConfigDefaults;
import com.redhat.rhn.common.localization.LocalizationService;
import com.redhat.rhn.common.util.CryptHelper;
import com.redhat.rhn.common.util.SHA256Crypt;
import com.redhat.rhn.domain.BaseDomainHelper;
import com.redhat.rhn.domain.access.AccessGroup;
import com.redhat.rhn.domain.access.AccessGroupFactory;
import com.redhat.rhn.domain.access.Namespace;
import com.redhat.rhn.domain.org.Org;
import com.redhat.rhn.domain.org.usergroup.UserGroup;
import com.redhat.rhn.domain.org.usergroup.UserGroupFactory;
import com.redhat.rhn.domain.org.usergroup.UserGroupImpl;
import com.redhat.rhn.domain.org.usergroup.UserGroupMembers;
import com.redhat.rhn.domain.role.Role;
import com.redhat.rhn.domain.role.RoleFactory;
import com.redhat.rhn.domain.server.Server;
import com.redhat.rhn.domain.server.ServerGroup;
import com.redhat.rhn.domain.user.Address;
import com.redhat.rhn.domain.user.AddressImpl;
import com.redhat.rhn.domain.user.EnterpriseUser;
import com.redhat.rhn.domain.user.Pane;
import com.redhat.rhn.domain.user.RhnTimeZone;
import com.redhat.rhn.domain.user.StateChange;
import com.redhat.rhn.domain.user.User;
import com.redhat.rhn.domain.user.UserFactory;
import com.redhat.rhn.manager.user.UserManager;

import com.suse.pam.DefaultPamServiceFactory;
import com.suse.pam.PamReturnValue;
import com.suse.pam.PamService;
import com.suse.pam.PamServiceFactory;

import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.builder.EqualsBuilder;
import org.apache.commons.lang3.builder.HashCodeBuilder;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.hibernate.annotations.Type;
import org.hibernate.type.YesNoConverter;

import java.util.Collections;
import java.util.Date;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Set;
import java.util.stream.Collectors;

import jakarta.persistence.CascadeType;
import jakarta.persistence.Column;
import jakarta.persistence.Convert;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GenerationType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.JoinTable;
import jakarta.persistence.ManyToMany;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.OneToMany;
import jakarta.persistence.OneToOne;
import jakarta.persistence.SequenceGenerator;
import jakarta.persistence.Table;
import jakarta.persistence.Transient;

/**
 * Class UserImpl that reflects the DB representation of web_contact
 * and ancillary tables.
 * DB table: web_contact
 */
@Entity
@Table(name = "WEB_CONTACT")
public class UserImpl extends BaseDomainHelper implements User {

    private static final Logger LOG = LogManager.getLogger(UserImpl.class);

    @Id
	@GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "web_contact_seq")
	@SequenceGenerator(name = "web_contact_seq", sequenceName = "WEB_CONTACT_ID_SEQ", allocationSize = 1)
    @Column(name = "id", insertable = false, updatable = false)
    private Long id;

    @Column(name = "login", length = 64)
    private String login;

    @Column(name = "login_uc", length = 64)
    private String loginUc;

    @Column(name = "password", length = 110)
    private String password;  // Note: access = field can be added if using field-based access

    @Column(name = "read_only", nullable = false)
    @Convert(converter = YesNoConverter.class)
    private boolean readOnly;

    @OneToOne(mappedBy = "user", cascade = CascadeType.ALL, fetch = FetchType.EAGER, optional = false)
    private PersonalInfo personalInfo  = new PersonalInfo();

    @OneToOne(mappedBy = "user", cascade = CascadeType.ALL, fetch = FetchType.EAGER, optional = false)
    private UserInfo userInfo = new UserInfo();

    @ManyToOne(fetch = FetchType.LAZY, cascade = CascadeType.ALL, optional = false)
    @JoinColumn(name = "org_id")
    private Org org;

    @OneToMany(mappedBy = "id", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    private Set<AddressImpl> addresses = new HashSet<>();

    @OneToMany(cascade = CascadeType.REMOVE, fetch = FetchType.LAZY, orphanRemoval = true)
    @JoinColumn(name = "user_id", updatable = false)
    private Set<UserGroupMembers> groupMembers = new HashSet<>();

    @OneToMany(mappedBy = "user", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    private Set<StateChange> stateChanges = new HashSet<>();

    @ManyToMany(fetch = FetchType.LAZY)
    @JoinTable(
            name = "RHNUSERINFOPANE",
            joinColumns = @JoinColumn(name = "user_id"),
            inverseJoinColumns = @JoinColumn(name = "pane_id")
    )
    private Set<Pane> hiddenPanes = new HashSet<>();

    @ManyToMany(cascade = CascadeType.PERSIST, fetch = FetchType.LAZY)
    @JoinTable(
            name = "rhnUserServerGroupPerms",
            joinColumns = @JoinColumn(name = "user_id"),
            inverseJoinColumns = @JoinColumn(name = "server_group_id")
    )
    private Set<ServerGroup> associatedServerGroups;

    @ManyToMany(fetch = FetchType.LAZY)
    @JoinTable(
            name = "rhnUserServerPerms",
            joinColumns = @JoinColumn(name = "user_id"),
            inverseJoinColumns = @JoinColumn(name = "server_id")
    )
    private Set<Server> servers = new HashSet<>();

    @ManyToMany
    @JoinTable(
            schema = "access",
            name = "userNamespace",
            joinColumns = @JoinColumn(name = "user_id"),
            inverseJoinColumns = @JoinColumn(name = "namespace_id")
    )
    private Set<Namespace> namespaces;

    @ManyToMany(fetch = FetchType.EAGER)
    @JoinTable(
            schema = "access",
            name = "userAccessGroup",
            joinColumns = @JoinColumn(name = "user_id"),
            inverseJoinColumns = @JoinColumn(name = "group_id")
    )
    private Set<AccessGroup> accessGroups;

    @Transient
    private Boolean wasOrgAdmin;

    @Transient
    private transient EnterpriseUser euser;

    @Transient
    private transient PamServiceFactory pamServiceFactory = new DefaultPamServiceFactory();

    /**
     * Gets the current value of id
     * @return long the current value
     */
    @Override
    public Long getId() {
        return this.id;
    }

    /**
     * Sets the value of id to new value
     * @param idIn New value for id
     */
    @Override
    public void setId(Long idIn) {
        this.id = idIn;
    }

    /**
     * Gets the current value of login
     * @return String the current value
     */
    @Override
    public String getLogin() {
        return this.login;
    }

    /**
     * Sets the value of login to new value
     * @param loginIn New value for login
     */
    @Override
    public void setLogin(String loginIn) {
        this.login = loginIn;
        setLoginUc(loginIn.toUpperCase());
    }

    /**
     * Gets the current value of loginUc
     * @return String the current value
     */
    @Override
    public String getLoginUc() {
        return this.loginUc;
    }

    /**
     * Sets the value of loginUc to new value
     * @param loginUcIn New value for loginUc
     */
    @Override
    public void setLoginUc(String loginUcIn) {
        this.loginUc = loginUcIn;
    }

    /**
     * Gets the current value of password
     * @return String the current value
     */
    @Override
    public String getPassword() {
        return this.password;
    }

    /**
     * Sets the value of password as is to new value, no encryption
     * @param passwordIn New raw value for password
     */
    @Override
    public void setRawPassword(String passwordIn) {
        this.password = passwordIn;
    }

    /**
     * Sets the value of password to new value
     * @param passwordIn New value for password
     */
    @Override
    public void setPassword(String passwordIn) {
        /**
         * If we're using encrypted passwords, encode the
         * password before setting it. Otherwise, just
         * set it.
         */
        if (Config.get().getBoolean(ConfigDefaults.WEB_ENCRYPTED_PASSWORDS)) {
            this.password = SHA256Crypt.crypt(passwordIn);
        }
        else {
            this.password = passwordIn;
        }
    }

    /**
     * Set the user group members set
     * @param ugIn The new Set of UserGroupMembers to set
     */
    protected void setGroupMembers(Set<UserGroupMembers> ugIn) {
        groupMembers = ugIn;
    }

    /** get the set of usergroups
     * @return Set of UserGroups
     */
    protected Set<UserGroup> getUserGroups() {
        Set<UserGroup> ugmSet = new HashSet<>();
        for (UserGroupMembers ugm : groupMembers) {
            ugmSet.add(ugm.getUserGroup());
        }
        return ugmSet;
    }

    /** get the set of user group members
     * @return Set of UserGroupMembers
     */
    protected Set<UserGroupMembers> getGroupMembers() {
        return groupMembers;
    }

    /** {@inheritDoc} */
    @Override
    public Set<Role> getRoles() {
        Set<Role> userRoles = groupMembers.stream()
                .map(ugm -> ugm.getUserGroup().getRole())
                .collect(Collectors.toSet());

        if (userRoles.contains(RoleFactory.ORG_ADMIN)) {
            Set<Role> orgRoles = org.getRoles();
            Set<Role> localImplied = new HashSet<>();
            localImplied.addAll(UserFactory.IMPLIEDROLES);
            localImplied.retainAll(orgRoles);
            userRoles.addAll(localImplied);
        }
        return Collections.unmodifiableSet(userRoles);
    }

    /** {@inheritDoc} */
    @Override
    public Set<Role> getTemporaryRoles() {
        Set<Role> userRoles = new HashSet<>();
        for (UserGroupMembers ugm : groupMembers) {
            if (ugm.isTemporary()) {
                userRoles.add(ugm.getUserGroup().getRole());
            }
        }

        if (userRoles.contains(RoleFactory.ORG_ADMIN)) {
            Set<Role> orgRoles = org.getRoles();
            Set<Role> localImplied = new HashSet<>();
            localImplied.addAll(UserFactory.IMPLIEDROLES);
            localImplied.retainAll(orgRoles);
            userRoles.addAll(localImplied);
        }
        return Collections.unmodifiableSet(userRoles);
    }

    /** {@inheritDoc} */
    @Override
    public Set<Role> getPermanentRoles() {
        Set<Role> userRoles = new HashSet<>();
        for (UserGroupMembers ugm : groupMembers) {
            if (!ugm.isTemporary()) {
                userRoles.add(ugm.getUserGroup().getRole());
            }
        }

        if (userRoles.contains(RoleFactory.ORG_ADMIN)) {
            Set<Role> orgRoles = org.getRoles();
            Set<Role> localImplied = new HashSet<>();
            localImplied.addAll(UserFactory.IMPLIEDROLES);
            localImplied.retainAll(orgRoles);
            userRoles.addAll(localImplied);
        }
        return Collections.unmodifiableSet(userRoles);
    }

    /** {@inheritDoc} */
    @Override
    public boolean hasRole(Role label) {
        // We use checkRoleSet to get the correct logic for the
        // implied roles.
        return getRoles().contains(label);
    }

    /** {@inheritDoc} */
    public boolean hasTemporaryRole(Role label) {
        return getTemporaryRoles().contains(label);
    }

    /** {@inheritDoc} */
    @Override
    public boolean hasPermanentRole(Role label) {
        return getPermanentRoles().contains(label);
    }

    /** {@inheritDoc} */
    @Override
    public void addTemporaryRole(Role label) {
        addRole(label, true);
    }

    /** {@inheritDoc} */
    @Override
    public void addPermanentRole(Role label) {
        addRole(label, false);

        if (RoleFactory.ORG_ADMIN.equals(label)) {
            getAccessGroups().addAll(AccessGroupFactory.DEFAULT_GROUPS);
        }
    }

    /** {@inheritDoc} */
    private void addRole(Role label, boolean temporary) {
        checkPermanentOrgAdmin();
        Set<Role> roles;
        if (temporary) {
            roles = this.getTemporaryRoles();
        }
        else {
            roles = this.getPermanentRoles();
        }
        if (!roles.contains(label)) {
            UserGroupImpl ug = org.getUserGroup(label);
            if (ug != null) {
                UserGroupMembers ugm = new UserGroupMembers(this, ug, temporary);
                UserGroupMembers managedUgm = UserGroupFactory.save(ugm);
                groupMembers.add(managedUgm);
            }
            else {
                throw new IllegalArgumentException("Org doesn't have role: " + label);
            }
        }
    }

    /** {@inheritDoc} */
    @Override
    public void removeTemporaryRole(Role label) {
        removeRole(label, true);
    }

    /** {@inheritDoc} */
    @Override
    public void removePermanentRole(Role label) {
        removeRole(label, false);
    }

    /** {@inheritDoc} */
    private void removeRole(Role label, boolean temporary) {
        checkPermanentOrgAdmin();
        UserGroup ug = org.getUserGroup(label);
        if (ug != null) {
            for (Iterator<UserGroupMembers> ugmIter = groupMembers.iterator();
                    ugmIter.hasNext();) {
                UserGroupMembers ugm = ugmIter.next();
                if (ugm.getUserGroup().equals(ug) && ugm.isTemporary() == temporary) {
                    UserGroupFactory.delete(ugm);
                    ugmIter.remove();
                }
            }
        }
    }

    /** {@inheritDoc} */
    public Boolean wasOrgAdmin() {
        return wasOrgAdmin;
    }

    /** {@inheritDoc} */
    public void resetWasOrgAdmin() {
        wasOrgAdmin = null;
    }

    private void checkPermanentOrgAdmin() {
        if (wasOrgAdmin == null) {
            wasOrgAdmin = hasPermanentRole(RoleFactory.ORG_ADMIN);
        }
    }

    /** {@inheritDoc} */
    @Override
    public boolean authenticate(String thePassword) {
        String pamAuthService = Config.get().getString(ConfigDefaults.WEB_PAM_AUTH_SERVICE);
        boolean result = false;
        /*
         * If we have a valid pamAuthService and the user uses pam authentication,
         * authenticate via pam, otherwise, use the db.
         */
        if (!StringUtils.isBlank(pamAuthService) && this.getUsePamAuthentication()) {
            if (password.startsWith(CryptHelper.getMD5Prefix())) {
                // password field in DB is NOT NULL, so we set a random password
                // when using PAM authentication. Here the password is still MD5
                // based. Just set a new one with SHA256crypt
                setPassword(CryptHelper.getRandomPasswordForPamAuth());
            }

            PamService pam = pamServiceFactory.getInstance(pamAuthService);
            PamReturnValue ret = pam.authenticate(getLogin(), thePassword);
            result = PamReturnValue.PAM_SUCCESS.equals(ret);
            if (!result) {
                LOG.warn("PAM login for user {} failed with error {}", this, ret);
            }
        }
        else {
            /*
             * If we're using encrypted passwords, check
             * thePassword encrypted, otherwise just do
             * a straight clear-text comparison.
             */
            boolean useEncrPasswds = Config.get().getBoolean(ConfigDefaults.WEB_ENCRYPTED_PASSWORDS);
            if (useEncrPasswds) {
                // user uses SHA-256 encrypted password
                if (password.startsWith(CryptHelper.getSHA256Prefix())) {
                    result = SHA256Crypt.crypt(thePassword, password).equals(password);
                }
            }
            else {
                result = password.equals(thePassword);
            }
            debug(!result, "DB login for user {} {} encrypted passwords failed", this,
                        useEncrPasswds ? "with" : "without");
        }
        debug(result, "PAM login for user {} succeeded. ", this);
        return result;
    }

    private void debug(boolean condition, String message, Object... args) {
        if (condition) {
            LOG.debug(message, args);
        }
    }

    /**
     * Associates the user with an Org.
     * @param orgIn Org to be associated to this user.
     */
    @Override
    public void setOrg(Org orgIn) {
        org = orgIn;
    }

    /** {@inheritDoc} */
    @Override
    public Org getOrg() {
        return org;
    }

    /** {@inheritDoc} */
    @Override
    public Set<Long> getDefaultSystemGroupIds() {
        return UserManager.getDefaultSystemGroupIds(this);
    }

    /** {@inheritDoc} */
    @Override
    public void setDefaultSystemGroupIds(Set<Long> dsg) {
        UserManager.setDefaultSystemGroupIds(this, dsg);
    }

    /**
    * Return the PersonalInfo object
    * @return PersonalInfo object associated with this User
    */
    protected PersonalInfo getPersonalInfo() {
        if (personalInfo == null) {
            personalInfo = new PersonalInfo();
        }
        return personalInfo;
    }

    /**
     * Set the PersonalInfo object
     * @param persIn the PersonalInfo object
     */
    protected void setPersonalInfo(PersonalInfo persIn) {
        this.personalInfo = persIn;
    }

    /**
     * Set the UserInfo object
     * @param infoIn the UserInfo object
     */
    protected void setUserInfo(UserInfo infoIn) {
        this.userInfo = infoIn;
    }

    /**
     * Get the UserInfo sub object
     * @return UserInfo
     */
    public UserInfo getUserInfo() {
        return this.userInfo;
    }


    /**
     * Convenience method to determine whether a user is disabled
     * or not
     * @return Returns true if the user is disabled
     */
    @Override
    public boolean isDisabled() {
        return UserFactory.isDisabled(this);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void addChange(StateChange change) {
        this.stateChanges.add(change);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Set<StateChange> getStateChanges() {
        return stateChanges;
    }

    /**
     * @param s The stateChanges to set.
     */
    public void setStateChanges(Set<StateChange> s) {
        this.stateChanges = s;
    }

    // ************   UserInfo methods **************
    /** {@inheritDoc} */
    @Override
    public int getPageSize() {
        return this.userInfo.getPageSize();
    }

    /** {@inheritDoc} */
    @Override
    public void setPageSize(int pageSizeIn) {
        this.userInfo.setPageSize(pageSizeIn);
    }

    /** {@inheritDoc} */
    @Override
    public boolean getUsePamAuthentication() {
        return this.userInfo.getUsePamAuthentication();
    }

    /** {@inheritDoc} */
    @Override
    public void setUsePamAuthentication(boolean usePamAuthenticationIn) {
        this.userInfo.setUsePamAuthentication(usePamAuthenticationIn);
    }

    /** {@inheritDoc} */
    @Override
    public String getShowSystemGroupList() {
        return this.userInfo.getShowSystemGroupList();
    }

    /** {@inheritDoc} */
    @Override
    public PamServiceFactory getPamServiceFactory() {
        return pamServiceFactory;
    }

    /** {@inheritDoc} */
    @Override
    public void setPamServiceFactory(PamServiceFactory pamServiceFactoryIn) {
        this.pamServiceFactory = pamServiceFactoryIn;
    }

    /** {@inheritDoc} */
    @Override
    public void setShowSystemGroupList(String showSystemGroupListIn) {
        this.userInfo.setShowSystemGroupList(showSystemGroupListIn);
    }

    /** {@inheritDoc} */
    @Override
    public Date getLastLoggedIn() {
        return this.userInfo.getLastLoggedIn();
    }

    /** {@inheritDoc} */
    @Override
    public void setLastLoggedIn(Date lastLoggedInIn) {
        this.userInfo.setLastLoggedIn(lastLoggedInIn);
    }

    /** {@inheritDoc} */
    @Override
    public void setPreferredLocale(String locale) {
        this.userInfo.setPreferredLocale(locale);
    }

    /** {@inheritDoc} */
    @Override
    public String getPreferredLocale() {
        return this.userInfo.getPreferredLocale();
    }

    /** {@inheritDoc} */
    @Override
    public void setPreferredDocsLocale(String docsLocale) {
        this.userInfo.setPreferredDocsLocale(docsLocale);
    }

    /** {@inheritDoc} */
    @Override
    public String getPreferredDocsLocale() {
        return this.userInfo.getPreferredDocsLocale();
    }

    /** {@inheritDoc} */
    @Override
    public void setCsvSeparator(char csvSeparator) {
        this.userInfo.setCsvSeparator(csvSeparator);
    }

    /** {@inheritDoc} */
    @Override
    public char getCsvSeparator() {
        return this.userInfo.getCsvSeparator();
    }

    /********* PersonalInfo Methods **********/

    /**
     * Gets the current value of prefix
     * @return String the current value
     */
    @Override
    public String getPrefix() {
        return this.personalInfo.getPrefix();
    }

    /**
     * Sets the value of prefix to new value
     * @param prefixIn New value for prefix
     */
    @Override
    public void setPrefix(String prefixIn) {
        this.personalInfo.setPrefix(prefixIn);
    }

    /**
     * Gets the current value of firstNames
     * @return String the current value
     */
    @Override
    public String getFirstNames() {
        return this.personalInfo.getFirstNames();
    }

    /**
     * Sets the value of firstNames to new value
     * @param firstNamesIn New value for firstNames
     */
    @Override
    public void setFirstNames(String firstNamesIn) {
        this.personalInfo.setFirstNames(firstNamesIn);
    }

    /**
     * Gets the current value of lastName
     * @return String the current value
     */
    @Override
    public String getLastName() {
        return this.personalInfo.getLastName();
    }

    /**
     * Sets the value of lastName to new value
     * @param lastNameIn New value for lastName
     */
    @Override
    public void setLastName(String lastNameIn) {
        this.personalInfo.setLastName(lastNameIn);
    }

    /**
     * Gets the current value of company
     * @return String the current value
     */
    @Override
    public String getCompany() {
        return this.personalInfo.getCompany();
    }

    /**
     * Sets the value of company to new value
     * @param companyIn New value for company
     */
    @Override
    public void setCompany(String companyIn) {
        this.personalInfo.setCompany(companyIn);
    }

    /**
     * Gets the current value of title
     * @return String the current value
     */
    @Override
    public String getTitle() {
        return this.personalInfo.getTitle();
    }

    /**
     * Sets the value of title to new value
     * @param titleIn New value for title
     */
    @Override
    public void setTitle(String titleIn) {
        this.personalInfo.setTitle(titleIn);
    }

    /**
     * Gets the current value of phone
     * @return String the current value
     */
    @Override
    public String getPhone() {
        return getAddress().getPhone();
    }

    /**
     * Sets the value of phone to new value
     * @param phoneIn New value for phone
     */
    @Override
    public void setPhone(String phoneIn) {
        getAddress().setPhone(phoneIn);
    }

    /**
     * Gets the current value of fax
     * @return String the current value
     */
    @Override
    public String getFax() {
        return getAddress().getFax();
    }

    /**
     * Sets the value of fax to new value
     * @param faxIn New value for fax
     */
    @Override
    public void setFax(String faxIn) {
        getAddress().setFax(faxIn);
    }

    /**
     * Gets the current value of email
     * @return String the current value
     */
    @Override
    public String getEmail() {
        return this.personalInfo.getEmail();
    }

    /**
     * Sets the value of email to new value
     * @param emailIn New value for email
     */
    @Override
    public void setEmail(String emailIn) {
        this.personalInfo.setEmail(emailIn);
    }


    /** {@inheritDoc} */
    @Override
    public RhnTimeZone getTimeZone() {
        return this.userInfo.getTimeZone();
    }

    /** {@inheritDoc} */
    @Override
    public void setTimeZone(RhnTimeZone timeZoneIn) {
        this.userInfo.setTimeZone(timeZoneIn);
    }




    /**
    * Output User to String for debugging
    * @return String output of the User
    */
    @Override
    public String toString() {
        return LocalizationService.getInstance().getDebugMessage("user") + " " + getLogin() +
                " (id " + getId() + ", org_id " + getOrg().getId() + ")";
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public boolean equals(Object other) {
        if (!(other instanceof User otherUser)) {
            return false;
        }
        return new EqualsBuilder().append(login, otherUser.getLogin())
                                  .append(org, otherUser.getOrg())
                                  .append(id, otherUser.getId())
                                  .isEquals();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public int hashCode() {
        return new HashCodeBuilder().append(login).append(org).append(id).toHashCode();
    }

    /**
     * Getter for address1
     * @return Address1
     */
    @Override
    public String getAddress1() {
        return getAddress().getAddress1();
    }

    /**
     * Setter for address1
     * @param address1In New value for address1
     */
    @Override
    public void setAddress1(String address1In) {
        getAddress().setAddress1(address1In);
    }

    /**
     * Getter for address2
     * @return Address2
     */
    @Override
    public String getAddress2() {
        return getAddress().getAddress2();
    }

    /**
     * Setter for address2
     * @param address2In New value for address2
     */
    @Override
    public void setAddress2(String address2In) {
        getAddress().setAddress2(address2In);
    }

    /**
     * Getter for city
     * @return City
     */
    @Override
    public String getCity() {
        return getAddress().getCity();
    }

    /**
     * Setter for city
     * @param cityIn New value for city
     */
    @Override
    public void setCity(String cityIn) {
        getAddress().setCity(cityIn);
    }

    /**
     * Getter for state
     * @return State
     */
    @Override
    public String getState() {
        return getAddress().getState();
    }

    /**
     * Setter for state
     * @param stateIn New value for state
     */
    @Override
    public void setState(String stateIn) {
        getAddress().setState(stateIn);
    }

    /**
     * Getter for zip
     * @return Zip
     */
    @Override
    public String getZip() {
        return getAddress().getZip();
    }

    /**
     * Setter for zip
     * @param zipIn New value for zip
     */
    @Override
    public void setZip(String zipIn) {
        getAddress().setZip(zipIn);
    }

    /**
     * Getter for country
     * @return Country
     */
    @Override
    public String getCountry() {
        return getAddress().getCountry();
    }

    /**
     * Setter for country
     * @param countryIn New value for country
     */
    @Override
    public void setCountry(String countryIn) {
        getAddress().setCountry(countryIn);
    }


    /**
     * Getter for isPoBox
     * @return isPoBox
     */
    @Override
    public String getIsPoBox() {
        return getAddress().getIsPoBox();
    }

    /**
     * Setter for isPoBox
     * @param isPoBoxIn New value for isPoBox
     */
    @Override
    public void setIsPoBox(String isPoBoxIn) {
        getAddress().setIsPoBox(isPoBoxIn);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public EnterpriseUser getEnterpriseUser() {
        if (euser == null) {
            euser = new EnterpriseUserImpl();
        }
        return euser;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Set<Pane> getHiddenPanes() {
        return hiddenPanes;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void setHiddenPanes(Set<Pane> p) {
        hiddenPanes = p;
    }

    protected void setAddress(Address addIn) {
        addresses.clear();
        addresses.add((AddressImpl) addIn);
    }

    protected Address getAddress() {
        Address baddr = null;
        Address addr = null;
        Address[] addrA = addresses.toArray(new Address[addresses.size()]);
        if (!addresses.isEmpty()) {
            for (Address addressIn : addrA) {
                if (addressIn.getType().equals(Address.TYPE_MARKETING)) {
                    addr = addressIn;
                }
                if (addressIn.getType().equals("B")) {
                    baddr = addressIn;
                }
            }
        }
        if (addr == null) {
            addr = UserFactory.createAddress();
            if (baddr != null) {
                addr.setAddress1(baddr.getAddress1());
                addr.setAddress2(baddr.getAddress2());
                addr.setCity(baddr.getCity());
                addr.setCountry(baddr.getCountry());
                addr.setFax(baddr.getFax());
                addr.setIsPoBox(baddr.getIsPoBox());
                addr.setPhone(baddr.getPhone());
                addr.setState(baddr.getState());
                addr.setZip(baddr.getZip());
            }
            addresses.add((AddressImpl)addr);
        }
        return addr;
    }

    /**
     * Set the addresses.
     * @param s the set
     */
    protected void setAddresses(Set<AddressImpl> s) {
        addresses = s;
    }

    /**
     * Get the addresses
     * @return Set of addresses
     */
    protected Set<AddressImpl> getAddresses() {
        return addresses;
    }

    /**
     * Default POJO imple of EnterpriseUser done as an internal
     * class to facilitate compatibility between hosted/sat.
     * EnterpriseUserImpl
     */
    class EnterpriseUserImpl extends BaseDomainHelper
                implements EnterpriseUser {

        /**
        * Default constructor
        *
        */
        protected EnterpriseUserImpl() {
        }

        /**
        * Gets the current value of id
        * @return long the current value
        */
        @Override
        public Long getId() {
            return id;
        }

        /**
        * Sets the value of id to new value
        * @param idIn New value for id
        */
        @Override
        public void setId(Long idIn) {
            id = idIn;
        }

        /**
        * Gets the current value of login
        * @return String the current value
        */
        @Override
        public String getLogin() {
            return login;
        }

        /**
        * Sets the value of login to new value
        * @param loginIn New value for login
        */
        @Override
        public void setLogin(String loginIn) {
            login = loginIn;
        }

        /**
        * Gets the current value of password
        * @return String the current value
        */
        @Override
        public String getPassword() {
            return password;
        }

        /**
        * Sets the value of password to new value
        * @param passwordIn New value for password
        */
        @Override
        public void setPassword(String passwordIn) {
            /**
            * If we're using encrypted passwords, encode the
            * password before setting it. Otherwise, just
            * set it.
            */
            if (Config.get().getBoolean(ConfigDefaults.WEB_ENCRYPTED_PASSWORDS)) {
                password = SHA256Crypt.crypt(passwordIn);
            }
            else {
                password = passwordIn;
            }
        }

        /**
        * Gets the current value of prefix
        * @return String the current value
        */
        @Override
        public String getPrefix() {
            return personalInfo.getPrefix();
        }

        /**
        * Sets the value of prefix to new value
        * @param prefixIn New value for prefix
        */
        @Override
        public void setPrefix(String prefixIn) {
            personalInfo.setPrefix(prefixIn);
        }

        /**
        * Gets the current value of firstNames
        * @return String the current value
        */
        @Override
        public String getFirstNames() {
            return personalInfo.getFirstNames();
        }

        /**
        * Sets the value of firstNames to new value
        * @param firstNamesIn New value for firstNames
        */
        @Override
        public void setFirstNames(String firstNamesIn) {
            personalInfo.setFirstNames(firstNamesIn);
        }

        /**
        * Gets the current value of lastName
        * @return String the current value
        */
        @Override
        public String getLastName() {
            return personalInfo.getLastName();
        }

        /**
        * Sets the value of lastName to new value
        * @param lastNameIn New value for lastName
        */
        @Override
        public void setLastName(String lastNameIn) {
            personalInfo.setLastName(lastNameIn);
        }

        /**
        * Gets the current value of title
        * @return String the current value
        */
        @Override
        public String getTitle() {
            return personalInfo.getTitle();
        }

        /**
        * Sets the value of title to new value
        * @param titleIn New value for title
        */
        @Override
        public void setTitle(String titleIn) {
            personalInfo.setTitle(titleIn);
        }

        /**
        * Gets the current value of email
        * @return String the current value
        */
        @Override
        public String getEmail() {
            return personalInfo.getEmail();
        }

        /**
        * Sets the value of email to new value
        * @param emailIn New value for email
        */
        @Override
        public void setEmail(String emailIn) {
            personalInfo.setEmail(emailIn);
        }

        /**
        * Getter for lastLoggedIn
        * @return lastLoggedIn
        */
        @Override
        public Date getLastLoggedIn() {
            return userInfo.getLastLoggedIn();
        }

        /**
        * Setter for lastLoggedIn
        * @param lastLoggedInIn New value for lastLoggedIn
        */
        @Override
        public void setLastLoggedIn(Date lastLoggedInIn) {
            userInfo.setLastLoggedIn(lastLoggedInIn);
        }

        /**
        * @inheritDoc
        * @param modifiedIn the modified date
        */
        @Override
        public void setModified(Date modifiedIn) {
            // Not implemented
        }

        /**
        * @inheritDoc
        * @return date modified
        */
        @Override
        public Date getModified() {
            return null;
        }

        /**
        * @inheritDoc
        * @param createdIn date created in
        */
        @Override
        public void setCreated(Date createdIn) {
            // Not implemented
        }

        /**
        * @inheritDoc
        * @return date was created
        */
        @Override
        public Date getCreated() {
            return null;
        }

        /**
        * @return Returns the timeZone.
        */
        @Override
        public RhnTimeZone getTimeZone() {
            return userInfo.getTimeZone();
        }
        /**
        * @param timeZoneIn The timeZone to set.
        */
        @Override
        public void setTimeZone(RhnTimeZone timeZoneIn) {
            userInfo.setTimeZone(timeZoneIn);
        }

        /**
        * Set the address of this enterprise user.
        * @param addressIn the address to set
        */
        @Override
        public void setAddress(Address addressIn) {
            addresses.clear();
            addresses.add((AddressImpl)addressIn);
        }

        /**
        *
        * @return returns the address info
        */
        @Override
        public Address getAddress() {
            Address baddr = null;
            Address addr = null;
            Address[] addrA = addresses.toArray(new Address[addresses.size()]);
            if (!addresses.isEmpty()) {
                for (Address addressIn : addrA) {
                    if (addressIn.getType().equals(Address.TYPE_MARKETING)) {
                        addr = addressIn;
                    }
                    if (addressIn.getType().equals("B")) {
                        baddr = addressIn;
                    }
                }
            }
            if (addr == null) {
                addr = UserFactory.createAddress();
                if (baddr != null) {
                    addr.setAddress1(baddr.getAddress1());
                    addr.setAddress2(baddr.getAddress2());
                    addr.setCity(baddr.getCity());
                    addr.setCountry(baddr.getCountry());
                    addr.setFax(baddr.getFax());
                    addr.setIsPoBox(baddr.getIsPoBox());
                    addr.setPhone(baddr.getPhone());
                    addr.setState(baddr.getState());
                    addr.setZip(baddr.getZip());
                }
                addresses.add((AddressImpl)addr);
            }
            return addr;
        }



        /**
        *
        * @param companyIn the company value
        */
        @Override
        public void setCompany(String companyIn) {
            personalInfo.setCompany(companyIn);
        }

        /**
        *
        * @return returns the company value
        */
        @Override
        public String getCompany() {
            return personalInfo.getCompany();
        }
    }

    /** {@inheritDoc} */
    @Override
    public void setEmailNotify(int emailNotifyIn) {
       this.userInfo.setEmailNotify(emailNotifyIn);
    }

    /** {@inheritDoc} */
    @Override
    public int getEmailNotify() {
        return this.userInfo.getEmailNotify();
    }

    /**
     *
     * {@inheritDoc}
     */
    @Override
    public Set<ServerGroup> getAssociatedServerGroups() {
        return associatedServerGroups;
    }

    /**
     * Sets the associatedServerGroups..
     * Meant for use by hibernate only (hence protected)
     * @param serverGroups the servergroups to set.
     */
    protected void setAssociatedServerGroups(Set<ServerGroup> serverGroups) {
        associatedServerGroups = serverGroups;
    }

    /** {@inheritDoc} */
    @Override
    public Set<Server> getServers() {
        return servers;
    }

    /**
     * @param serversIn the servers to set
     */
    public void setServers(Set<Server> serversIn) {
        // This method is used by hibernate
        this.servers = serversIn;
    }

    /** {@inheritDoc} */
    @Override
    public void addServer(Server server) {
        servers.add(server);
    }

    /** {@inheritDoc} */
    @Override
    public void removeServer(Server server) {
        servers.remove(server);
    }

    /**
     * @return Returns whether user is readonly
     */
    @Override
    public boolean isReadOnly() {
        return readOnly;
    }

    /**
     * @param readOnlyIn readOnly to set
     */
    @Override
    public void setReadOnly(boolean readOnlyIn) {
        this.readOnly = readOnlyIn;
    }

    /** {@inheritDoc} */
    @Override
    public boolean getTaskoNotify() {
        return this.userInfo.getTaskoNotify();
    }

    /** {@inheritDoc} */
    @Override
    public void setTaskoNotify(boolean taskoNotifyIn) {
        this.userInfo.settaskoNotify(taskoNotifyIn);
    }

    /** {@inheritDoc} */
    @Override
    public String getWebTheme() {
        return this.userInfo.getWebTheme();
    }

    /** {@inheritDoc} */
    @Override
    public void  setWebTheme(String webThemeIn) {
        this.userInfo.setWebTheme(webThemeIn);
    }

    @Override
    public Set<Namespace> getNamespaces() {
        return this.namespaces;
    }

    @Override
    public void setNamespaces(Set<Namespace> namespaceIn) {
        this.namespaces = namespaceIn;
    }

    @Override
    public Set<AccessGroup> getAccessGroups() {
        return accessGroups;
    }

    @Override
    public void setAccessGroups(Set<AccessGroup> accessGroupsIn) {
        accessGroups = accessGroupsIn;
    }

    /** {@inheritDoc} */
    @Override
    public boolean isMemberOf(AccessGroup accessGroupIn) {
        Set<AccessGroup> userGroups = getAccessGroups();
        if (userGroups != null && userGroups.contains(accessGroupIn)) {
            return true;
        }

        // Satellite admin has access to everything.
        if (hasRole(RoleFactory.SAT_ADMIN)) {
            return true;
        }

        if (hasRole(RoleFactory.ORG_ADMIN)) {
            // Org admin implicitly has default RBAC roles (when org is null)
            if (accessGroupIn.getOrg() == null) {
                return UserFactory.IMPLIEDROLES.stream()
                        .anyMatch(role -> role.getLabel().equals(accessGroupIn.getLabel()));
            }
            // Org admin has access to any group within their org
            return accessGroupIn.getOrg().getId().equals(getOrg().getId());
        }
        return false;
    }

    /** {@inheritDoc} */
    @Override
    public void removeFromGroup(AccessGroup accessGroupIn) {
        if (accessGroups != null) {
            accessGroups.remove(accessGroupIn);
        }
    }

    /** {@inheritDoc} */
    @Override
    public void addToGroup(AccessGroup accessGroupIn) {
        if (accessGroups == null) {
            accessGroups = new HashSet<>();
        }
            accessGroups.add(accessGroupIn);
    }
}
