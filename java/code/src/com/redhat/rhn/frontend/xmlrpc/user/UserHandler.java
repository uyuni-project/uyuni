/*
 * Copyright (c) 2009--2016 Red Hat, Inc.
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
package com.redhat.rhn.frontend.xmlrpc.user;

import com.redhat.rhn.FaultException;
import com.redhat.rhn.common.hibernate.LookupException;
import com.redhat.rhn.common.localization.LocalizationService;
import com.redhat.rhn.common.security.PermissionException;
import com.redhat.rhn.common.util.CryptHelper;
import com.redhat.rhn.common.util.MethodUtil;
import com.redhat.rhn.common.util.StringUtil;
import com.redhat.rhn.common.validator.ValidatorError;
import com.redhat.rhn.domain.role.Role;
import com.redhat.rhn.domain.role.RoleFactory;
import com.redhat.rhn.domain.server.ManagedServerGroup;
import com.redhat.rhn.domain.server.ServerGroup;
import com.redhat.rhn.domain.server.ServerGroupFactory;
import com.redhat.rhn.domain.user.User;
import com.redhat.rhn.domain.user.UserFactory;
import com.redhat.rhn.frontend.action.common.BadParameterException;
import com.redhat.rhn.frontend.xmlrpc.BaseHandler;
import com.redhat.rhn.frontend.xmlrpc.DeleteUserException;
import com.redhat.rhn.frontend.xmlrpc.InvalidOperationException;
import com.redhat.rhn.frontend.xmlrpc.InvalidServerGroupException;
import com.redhat.rhn.frontend.xmlrpc.LookupServerGroupException;
import com.redhat.rhn.frontend.xmlrpc.NoSuchRoleException;
import com.redhat.rhn.frontend.xmlrpc.PermissionCheckFailureException;
import com.redhat.rhn.frontend.xmlrpc.UserNotUpdatedException;
import com.redhat.rhn.manager.SatManager;
import com.redhat.rhn.manager.system.ServerGroupManager;
import com.redhat.rhn.manager.user.CreateUserCommand;
import com.redhat.rhn.manager.user.DeleteSatAdminException;
import com.redhat.rhn.manager.user.UpdateUserCommand;
import com.redhat.rhn.manager.user.UserManager;

import com.suse.manager.api.ReadOnly;

import org.apache.commons.lang3.BooleanUtils;
import org.apache.commons.lang3.StringUtils;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedHashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * UserHandler
 * Corresponds to User.pm in old perl code.
 * @apidoc.namespace user
 * @apidoc.doc User namespace contains methods to access common user functions
 * available from the web user interface.
 */
public class UserHandler extends BaseHandler {

    /**
     * Contains a mapping of details key as submitted by the call to the
     * {@link #setDetails(String, String, Map)} to the internal key used in the command
     * and domain objects. This is a band-aid to make the external API read correctly
     * (first_name instead of first_names) without having to refactor the entire code
     * base to use the singular version (for instance, User still uses first_names and
     * will be a significant change to refactor that). For more information, see
     * bugzilla 469957.
     */
    private static final Map<String, String> USER_EDITABLE_DETAILS =
            new HashMap<>();
    static {
        USER_EDITABLE_DETAILS.put("first_name", "first_names");
        USER_EDITABLE_DETAILS.put("first_names", "first_names");
        USER_EDITABLE_DETAILS.put("last_name", "last_name");
        USER_EDITABLE_DETAILS.put("email", "email");
        USER_EDITABLE_DETAILS.put("prefix", "prefix");
        USER_EDITABLE_DETAILS.put("password", "password");
    }

    private final ServerGroupManager serverGroupManager;

    /**
     * @param serverGroupManagerIn
     */
    public UserHandler(ServerGroupManager serverGroupManagerIn) {
        serverGroupManager = serverGroupManagerIn;
    }

    /**
     * Lists the users in the org.
     * @param loggedInUser The current user
     * @return Returns a list of userids and logins
     * @throws FaultException A FaultException is thrown if the loggedInUser
     * doesn't have permissions to list the users in their org.
     *
     * @apidoc.doc Returns a list of users in your organization.
     * @apidoc.param #session_key()
     * @apidoc.returntype
     * #return_array_begin()
     *     $UserSerializer
     * #array_end()
     */
    @ReadOnly
    public List listUsers(User loggedInUser) throws FaultException {
        // Get the logged in user
        try {
            return UserManager.usersInOrg(loggedInUser);
        }
        catch (PermissionException e) {
            throw new PermissionCheckFailureException();
        }
    }

    /**
     * Lists the roles for a user
     * @param loggedInUser The current user
     * @param login The login for the user you want to get the roles for
     * @return Returns a list of roles for the user specified by login
     * @throws FaultException A FaultException is thrown if the user doesn't have access
     * to lookup the user corresponding to login or if the user does not exist.
     *
     * @apidoc.doc Returns a list of the user's roles.
     * @apidoc.param #session_key()
     * @apidoc.param #param_desc("string", "login", "User's login name.")
     * @apidoc.returntype #array_single("string", "(role label)")
     */
    @ReadOnly
    public Object[] listRoles(User loggedInUser, String login) throws FaultException {
        // Get the logged in user
        User target = XmlRpcUserHelper.getInstance().lookupTargetUser(loggedInUser, login);
        List roles = new ArrayList<>(); //List of role labels to return

        //Loop through the target users roles and stick the labels into the ArrayList
        Set roleObjects = target.getPermanentRoles();
        for (Object roleObjectIn : roleObjects) {
            Role r = (Role) roleObjectIn;
            roles.add(r.getLabel());
        }

        return roles.toArray();
    }

    /**
     * Lists all the roles that can be assign by this user.
     * @param loggedInUser The current user
     * @return Returns a list of assignable roles for user
     * @throws FaultException A FaultException is thrown if the logged doesn't have access.
     *
     * @apidoc.doc Returns a list of user roles that this user can assign to others.
     * @apidoc.param #session_key()
     * @apidoc.returntype #array_single("string", "(role label)")
     */
    @ReadOnly
    public Set<String> listAssignableRoles(User loggedInUser) {
        return getAssignableRoles(loggedInUser);
    }

    /**
     * Gets details for a given user. These details include first names, last name, email,
     * prefix, last login date, and created on date.
     * @param loggedInUser The current user
     * @param login The login for the user you want the details for
     * @return Returns a Map containing the details for the given user.
     * @throws FaultException A FaultException is thrown if the user doesn't have access
     * to lookup the user corresponding to login or if the user does not exist.
     *
     * @apidoc.doc Returns the details about a given user.
     * @apidoc.param #session_key()
     * @apidoc.param #param_desc("string", "login", "User's login name.")
     * @apidoc.returntype
     *   #struct_begin("user details")
     *     #prop_desc("string", "first_names", "deprecated, use first_name")
     *     #prop("string", "first_name")
     *     #prop("string", "last_name")
     *     #prop("string", "email")
     *     #prop("int", "org_id")
     *     #prop("string", "org_name")
     *     #prop("string", "prefix")
     *     #prop("string", "last_login_date")
     *     #prop("string", "created_date")
     *     #prop_desc("boolean", "enabled", "true if user is enabled,
     *     false if the user is disabled")
     *     #prop_desc("boolean", "use_pam", "true if user is configured to use
     *     PAM authentication")
     *     #prop_desc("boolean", "read_only", "true if user is readonly")
     *     #prop_desc("boolean", "errata_notification", "true if errata e-mail notification
     *     is enabled for the user")
     *   #struct_end()
     */
    @ReadOnly
    public Map<String, Object> getDetails(User loggedInUser, String login) throws FaultException {
        User target = XmlRpcUserHelper.getInstance().lookupTargetUser(loggedInUser, login);
        LocalizationService ls = LocalizationService.getInstance();

        Map<String, Object> ret = new HashMap<>();
        ret.put("first_names", StringUtils.defaultString(target.getFirstNames()));
        ret.put("first_name", StringUtils.defaultString(target.getFirstNames()));
        ret.put("last_name",   StringUtils.defaultString(target.getLastName()));
        ret.put("email",       StringUtils.defaultString(target.getEmail()));
        ret.put("prefix",      StringUtils.defaultString(target.getPrefix()));

        //Last login date
        String lastLoggedIn = target.getLastLoggedIn() == null ?
                                  "" : ls.formatDate(target.getLastLoggedIn());
        ret.put("last_login_date", lastLoggedIn);

        //Created date
        String created = target.getCreated() == null ?
                                  "" : ls.formatDate(target.getCreated());
        ret.put("created_date", created);
        ret.put("org_id", loggedInUser.getOrg().getId());
        ret.put("org_name", loggedInUser.getOrg().getName());

        if (target.isDisabled()) {
            ret.put("enabled", Boolean.FALSE);
        }
        else {
            ret.put("enabled", Boolean.TRUE);
        }
        ret.put("use_pam", target.getUsePamAuthentication());
        ret.put("read_only", target.isReadOnly());
        ret.put("errata_notification", target.getEmailNotify() == 1);

        return ret;
    }

    /**
     * Sets the details for a given user. Settable details include: first names,
     * last name, email, prefix, and password.
     * @param loggedInUser The current user
     * user.
     * @param login The login for the user you want to edit
     * @param details A map containing the new details values
     * @return Returns 1 if edit was successful, an error is thrown otherwise
     * @throws FaultException A FaultException is thrown if the user doesn't
     * have access to lookup the user corresponding to login or if the user
     * does not exist.
     *
     * @apidoc.doc Updates the details of a user.
     * @apidoc.param #session_key()
     * @apidoc.param #param_desc("string", "login", "User's login name.")
     * @apidoc.param
     *   #struct_begin("details")
     *     #prop_desc("string", "first_names", "deprecated, use first_name")
     *     #prop("string", "first_name")
     *     #prop("string", "last_name")
     *     #prop("string", "email")
     *     #prop("string", "prefix")
     *     #prop("string", "password")
     *   #struct_end()
     * @apidoc.returntype #return_int_success()
     */
    public int setDetails(User loggedInUser, String login, Map<String, String> details)
        throws FaultException {

        validateMap(USER_EDITABLE_DETAILS.keySet(), details);

        // Lookup user handles the logic for making sure that the loggedInUser
        // has access to the login they are trying to edit.
        User target = XmlRpcUserHelper.getInstance().lookupTargetUser(
                loggedInUser, login);

        UpdateUserCommand uuc = new UpdateUserCommand(target);

        // Process each entry passed in by the user
        for (Map.Entry<String, String> entry : details.entrySet()) {

            // Check to make sure we have an internal key mapping to prevent issues
            // if the user passes in cruft
            String internalKey = USER_EDITABLE_DETAILS.get(entry.getKey());
            if (internalKey != null) {
                String newValue = StringUtils.defaultString(entry.getValue());
                prepareAttributeUpdate(internalKey, uuc, newValue);
            }
        }

        try {
            uuc.updateUser();
        }
        catch (IllegalArgumentException iae) {
            throw new UserNotUpdatedException(iae.getMessage());
        }

        // If we made it here without an exception, then we are a.o.k.
        return 1;
    }

    /**
     * Handles the vagaries related to granting or revoking sat admin role
     * @param loggedInUser the logged in user
     * @param login the login of the user who needs to be granted/revoked sat admin role
     * @param grant true if granting the role to the login, false for revoking...
     * @return 1 if it success.. Ofcourse error on failure..
     */
    private int  modifySatAdminRole(User loggedInUser, String login, boolean grant) {
        ensureUserRole(loggedInUser, RoleFactory.SAT_ADMIN);
        SatManager manager = SatManager.getInstance();
        User user = UserFactory.lookupByLogin(login);
        if (grant) {
            manager.grantSatAdminRoleTo(user, loggedInUser);
        }
        else {
            manager.revokeSatAdminRoleFrom(user, loggedInUser);
        }
        UserManager.storeUser(user);
        return 1;
    }

    /**
     * Returns all roles that are assignable to a given user
     * @return all the role labels that are assignable to a user.
     */
    private Set<String> getAssignableRoles(User user) {
        Set<String> assignableRoles = new LinkedHashSet<>();
        for (Role r : UserManager.listRolesAssignableBy(user)) {
            assignableRoles.add(r.getLabel());
        }
        return assignableRoles;
    }

    /**
     * Validates that the select roles is among the ones we support.
     * @param role the role that user wanted to be assigned
     * @param user the logged in user who wants to assign the given role.
     */
    private void validateRoleInputs(String role, User user) {
        Set<String> assignableRoles = getAssignableRoles(user);
        if (!assignableRoles.contains(role)) {
            String msg = "Role with the label [%s] cannot be " +
                          "assigned/revoked from the user." +
                         " Possible Roles assignable/revokable by this user %s";

            throw new NoSuchRoleException(String.format(msg, role, assignableRoles));
        }
    }

    /**
     * Adds a role to the given user
     * @param loggedInUser The current user
     * @param login The login for the user you would like to add the role to
     * @param role The role you would like to give the user
     * @return Returns 1 if successful (exception otherwise)
     * @throws FaultException A FaultException is thrown if the user doesn't have access
     * to lookup the user corresponding to login or if the user does not exist.
     *
     * @apidoc.doc Adds a role to a user.
     * @apidoc.param #session_key()
     * @apidoc.param #param_desc("string", "login", "User login name to update.")
     * @apidoc.param #param_desc("string", "role", "Role label to add.  Can be any of:
     * satellite_admin, org_admin, channel_admin, config_admin, system_group_admin, or
     * activation_key_admin.")
     * @apidoc.returntype #return_int_success()
     */
    public int addRole(User loggedInUser, String login, String role) throws FaultException {
        validateRoleInputs(role, loggedInUser);
        if (RoleFactory.SAT_ADMIN.getLabel().equals(role)) {
            return modifySatAdminRole(loggedInUser, login, true);
        }
        User target = XmlRpcUserHelper.getInstance().lookupTargetUser(loggedInUser, login);
        // Retrieve the role object corresponding to the role label passed in and
        // add to user
        Role r = RoleFactory.lookupByLabel(role);
        target.addPermanentRole(r);
        UserManager.storeUser(target);
        return 1;
    }


    /**
     * Removes a role from the given user
     * @param loggedInUser The current user
     * @param login The login for the user you would like to remove the role from
     * @param role The role you would like to remove from the user
     * @return Returns 1 if successful (exception otherwise)
     * @throws FaultException A FaultException is thrown if the user doesn't have access
     * to lookup the user corresponding to login or if the user does not exist.
     *
     * @apidoc.doc Remove a role from a user.
     * @apidoc.param #session_key()
     * @apidoc.param #param_desc("string", "login", "User login name to update.")
     * @apidoc.param #param_desc("string", "role", "Role label to remove.  Can be any of:
     * satellite_admin, org_admin, channel_admin, config_admin, system_group_admin, or
     * activation_key_admin.")
     * @apidoc.returntype #return_int_success()
     */
    public int removeRole(User loggedInUser, String login, String role)
        throws FaultException {
        validateRoleInputs(role, loggedInUser);

        if (RoleFactory.SAT_ADMIN.getLabel().equals(role)) {
            return modifySatAdminRole(loggedInUser, login, false);
        }

        ensureOrgAdmin(loggedInUser);
        User target = XmlRpcUserHelper.getInstance().lookupTargetUser(loggedInUser, login);

        /*
         * Perform some error checking here... we need to make sure that this
         * isn't the last org_admin in the org trying to remove org_admin
         * status from himself.
         */
        if (!target.isReadOnly()) {
            if (role.equals(RoleFactory.ORG_ADMIN.getLabel()) &&
                target.hasRole(RoleFactory.ORG_ADMIN) &&
                target.getOrg().numActiveOrgAdmins() <= 1) {
                    throw new PermissionCheckFailureException();
            }
        }

        // Retrieve the role object corresponding to the role label passed in and
        // remove from user
        Role r = RoleFactory.lookupByLabel(role);
        target.removePermanentRole(r);

        UserManager.storeUser(target);
        return 1;
    }

    /**
     * Creates a new user
     * @param loggedInUser The current user
     * @param login The login for the new user
     * @param password The password for the new user
     * @param firstName The first name of the new user
     * @param lastName The last name of the new user
     * @param email The email address for the new user
     * @return Returns 1 if successful (exception otherwise)
     * @throws FaultException A FaultException is thrown if the loggedInUser doesn't have
     * permissions to create new users in thier org.
     *
     * @apidoc.doc Create a new user.
     * @apidoc.param #session_key()
     * @apidoc.param #param_desc("string", "login", "desired login name, will fail if
     * already in use.")
     * @apidoc.param #param("string", "password")
     * @apidoc.param #param("string", "firstName")
     * @apidoc.param #param("string", "lastName")
     * @apidoc.param #param_desc("string", "email", "User's e-mail address.")
     * @apidoc.returntype #return_int_success()
     */
    public int create(User loggedInUser, String login, String password,
                   String firstName, String lastName, String email) throws FaultException {

        // If we didn't get a value for pamAuth, default to no
        return create(loggedInUser, login, password, firstName, lastName,
                      email, 0);
    }

    /**
     * Creates a new user
     * @param loggedInUser The current user
     * @param login The login for the new user
     * @param password The password for the new user
     * @param firstName The first name of the new user
     * @param lastName The last name of the new user
     * @param email The email address for the new user
     * @param usePamAuth Should this user authenticate via PAM?
     * @return Returns 1 if successful (exception otherwise)
     * @throws FaultException A FaultException is thrown if the loggedInUser doesn't have
     * permissions to create new users in thier org.
     *
     * @apidoc.doc Create a new user.
     * @apidoc.param #session_key()
     * @apidoc.param #param_desc("string", "login", "desired login name,
     * will fail if already in use.")
     * @apidoc.param #param("string", "password")
     * @apidoc.param #param("string", "firstName")
     * @apidoc.param #param("string", "lastName")
     * @apidoc.param #param_desc("string", "email", "User's e-mail address.")
     * @apidoc.param #param_desc("int", "usePamAuth", "1 if you wish to use PAM
     * authentication for this user, 0 otherwise.")
     * @apidoc.returntype #return_int_success()
     */
    public int create(User loggedInUser, String login, String password,
                      String firstName, String lastName, String email, Integer usePamAuth)
                      throws FaultException {
        //Logged in user must be an org admin and we must be on a sat to do this.
        ensureOrgAdmin(loggedInUser);
        ensurePasswordOrPamAuth(usePamAuth, password);

        boolean pamAuth = BooleanUtils.toBoolean(
                usePamAuth, Integer.valueOf(1), Integer.valueOf(0));

        if (pamAuth) {
            password = CryptHelper.getRandomPasswordForPamAuth();
        }

        CreateUserCommand command = new CreateUserCommand();
        command.setUsePamAuthentication(pamAuth);
        command.setLogin(login);
        command.setPassword(password);
        command.setFirstNames(firstName);
        command.setLastName(lastName);
        command.setEmail(email);
        command.setOrg(loggedInUser.getOrg());
        command.setCompany(loggedInUser.getCompany());

        //Validate the user to be
        ValidatorError[] errors = command.validate();
        if (errors.length > 0) {
            StringBuilder errorString = new StringBuilder();
            LocalizationService ls = LocalizationService.getInstance();
            //Build a sane error message here
            for (int i = 0; i < errors.length; i++) {
                ValidatorError err = errors[i];
                errorString.append(ls.getMessage(err.getKey(), err.getValues()));
                if (i != errors.length - 1) {
                    errorString.append(" :: ");
                }
            }
            //Throw a BadParameterException with our message string
            throw new BadParameterException(errorString.toString());
        }

        command.storeNewUser();
        return 1;
    }

    /**
     * Deletes a user
     * @param loggedInUser The current user
     * @param login The login for the user you would like to delete
     * @return Returns 1 if successful (exception otherwise)
     * @throws FaultException A FaultException is thrown if the user doesn't have access
     * to lookup the user corresponding to login or if the user does not exist.
     *
     * @apidoc.doc Delete a user.
     * @apidoc.param #session_key()
     * @apidoc.param #param_desc("string", "login", "User login name to delete.")
     * @apidoc.returntype #return_int_success()
     */
    public int delete(User loggedInUser, String login) throws FaultException {
        ensureOrgAdmin(loggedInUser);
        User target = XmlRpcUserHelper.getInstance().lookupTargetUser(loggedInUser, login);

        try {
            UserManager.deleteUser(loggedInUser, target.getId());
        }
        catch (DeleteSatAdminException e) {
            throw new DeleteUserException("user.cannot.delete.last.sat.admin");
        }

        return 1;
    }

    /**
     * Disable a user
     * @param loggedInUser The current user
     * @param login The login for the user you would like to disable
     * @return Returns 1 if successful (exception otherwise)
     * @throws FaultException A FaultException is thrown if the user doesn't have access
     * to lookup the user corresponding to login or if the user does not exist.
     *
     * @apidoc.doc Disable a user.
     * @apidoc.param #session_key()
     * @apidoc.param #param_desc("string", "login", "User login name to disable.")
     * @apidoc.returntype #return_int_success()
     */
    public int disable(User loggedInUser, String login) throws FaultException {
        ensureOrgAdmin(loggedInUser);

        User target = XmlRpcUserHelper.getInstance().lookupTargetUser(loggedInUser, login);
        UserManager.disableUser(loggedInUser, target);

        return 1;
    }

    /**
     * Enable a user
     * @param loggedInUser The current user
     * @param login The login for the user you would like to enable
     * @return Returns 1 if successful (exception otherwise)
     * @throws FaultException A FaultException is thrown if the user doesn't have access
     * to lookup the user corresponding to login or if the user does not exist.
     *
     * @apidoc.doc Enable a user.
     * @apidoc.param #session_key()
     * @apidoc.param #param_desc("string", "login", "User login name to enable.")
     * @apidoc.returntype #return_int_success()
     */
    public int enable(User loggedInUser, String login) throws FaultException {
        ensureOrgAdmin(loggedInUser);

        User target = XmlRpcUserHelper.getInstance().lookupTargetUser(loggedInUser, login);
        UserManager.enableUser(loggedInUser, target);

        return 1;
    }

    /**
     * Toggles whether or not a user users pamAuthentication or the basic db auth.
     * @param loggedInUser The current user
     * @param login The login for the user you would like to change
     * @param val The value you would like to set this to (1 = true, 0 = false)
     * @return Returns 1 if successful (exception otherwise)
     * @throws FaultException A FaultException is thrown if the user doesn't have access
     * to lookup the user corresponding to login or if the user does not exist.
     *
     * @apidoc.doc Toggles whether or not a user uses PAM authentication or
     * basic #product() authentication.
     * @apidoc.param #session_key()
     * @apidoc.param #param_desc("string", "login", "User's login name.")
     * @apidoc.param #param("int", "val")
     *   #options()
     *     #item("1 to enable PAM authentication")
     *     #item("0 to disable.")
     *   #options_end()
     * @apidoc.returntype #return_int_success()
     */
    public int usePamAuthentication(User loggedInUser, String login, Integer val)
        throws FaultException {
        // Only org admins can use this method.
        ensureOrgAdmin(loggedInUser);
        User target = XmlRpcUserHelper.getInstance().lookupTargetUser(loggedInUser, login);

        if (val.equals(1)) {
            target.setUsePamAuthentication(true);
        }
        else {
            target.setUsePamAuthentication(false);
        }

        UserManager.storeUser(target);

        return 1;
    }


    private void ensurePasswordOrPamAuth(Integer usePamAuth, String password)
        throws FaultException {
        if (!BooleanUtils.toBoolean(usePamAuth, Integer.valueOf(1), Integer.valueOf(0)) &&
                StringUtils.isEmpty(password)) {
            throw new FaultException(-501, "passwordRequiredOrUsePam",
                    "Password is required if not using PAM authentication");
        }
    }

    private void prepareAttributeUpdate(String attrName, UpdateUserCommand cmd,
            String value) {
        String methodName = StringUtil.beanify("set_" + attrName);
        Object[] params = {value};
        MethodUtil.callMethod(cmd, methodName, params);
    }

    /**
     * Add ServerGroup to the list of Default System groups. The ServerGroup
     * <strong>MUST</strong> exist otherwise a IllegalArgumentException is
     * thrown.
     * @param loggedInUser The current user
     * in user.
     * @param login The login for the user whose Default ServerGroup list will
     * be affected.
     * @param name name of ServerGroup.
     * @return Returns 1 if successful (exception otherwise)
     *
     * @apidoc.doc Add system group to user's list of default system groups.
     * @apidoc.param #session_key()
     * @apidoc.param #param_desc("string", "login", "User's login name.")
     * @apidoc.param #param_desc("string", "name", "server group name")
     * @apidoc.returntype #return_int_success()
     */
    public int addDefaultSystemGroup(User loggedInUser, String login, String name) {
        List<String> ids = new LinkedList<>();
        ids.add(name);
        return addDefaultSystemGroups(loggedInUser, login, ids);
    }

    /**
     * Add ServerGroups to the list of Default System groups. The ServerGroups
     * <strong>MUST</strong> exist otherwise a IllegalArgumentException is
     * thrown.
     * @param loggedInUser The current user
     * in user.
     * @param login The login for the user whose Default ServerGroup list will
     * be affected.
     * @param sgNames names of ServerGroups.
     * @return Returns 1 if successful (exception otherwise)
     *
     * @apidoc.doc Add system groups to user's list of default system groups.
     * @apidoc.param #session_key()
     * @apidoc.param #param_desc("string", "login", "User's login name.")
     * @apidoc.param #array_single_desc("string", "sgNames", "server group names")
     * @apidoc.returntype #return_int_success()
     */
    public int addDefaultSystemGroups(User loggedInUser, String login, List<String> sgNames) {

        User target = XmlRpcUserHelper.getInstance().lookupTargetUser(
                loggedInUser, login);

        if (sgNames == null || sgNames.isEmpty()) {
            throw new IllegalArgumentException("no servergroup names supplied");
        }

        List<ManagedServerGroup> groups = ServerGroupFactory.listManagedGroups(target.getOrg());
        Map<String, ServerGroup> groupMap = groups.stream().collect(Collectors.toMap(sg -> sg.getName(), sg -> sg));

        // Doing full check of all supplied names, if one is bad
        // throw an exception, prior to altering the DefaultSystemGroup Set.
        for (String name : sgNames) {
            ServerGroup sg = groupMap.get(name);
            if (sg == null) {
                throw new LookupServerGroupException(name);
            }
        }

        // now for the real reason we're in this method.
        Set<Long> defaults = target.getDefaultSystemGroupIds();
        for (String sgName : sgNames) {
            ServerGroup sg = groupMap.get(sgName);
            if (sg != null) {
                // not a simple add to the groups.  Needs to call
                // UserManager as DataSource is being used.
                defaults.add(sg.getId());
            }
        }

        UserManager.setDefaultSystemGroupIds(target, defaults);
        UserManager.storeUser(target);

        return 1;
    }

    /**
     * Remove ServerGroup from the list of Default System groups. The
     * ServerGroup <strong>MUST</strong> exist otherwise a
     * IllegalArgumentException is thrown.
     * @param loggedInUser The current user
     * in user.
     * @param login The login for the user whose Default ServerGroup list will
     * be affected.
     * @param sgName Name of ServerGroup.
     * @return Returns 1 if successful (exception otherwise)
     *
     * @apidoc.doc Remove a system group from user's list of default system groups.
     * @apidoc.param #session_key()
     * @apidoc.param #param_desc("string", "login", "User's login name.")
     * @apidoc.param #param_desc("string", "sgName", "server group name")
     * @apidoc.returntype #return_int_success()
     */
    public int removeDefaultSystemGroup(User loggedInUser, String login, String sgName) {
        List<String> names = new LinkedList<>();
        names.add(sgName);
        return removeDefaultSystemGroups(loggedInUser, login, names);
    }

    /**
     * Remove ServerGroups from the list of Default System groups. The
     * ServerGroups <strong>MUST</strong> exist otherwise a
     * IllegalArgumentException is thrown.
     * @param loggedInUser The current user
     * in user.
     * @param login The login for the user whose Default ServerGroup list will
     * be affected.
     * @param sgNames Names of ServerGroups.
     * @return Returns 1 if successful (exception otherwise)
     *
     * @apidoc.doc Remove system groups from a user's list of default system groups.
     * @apidoc.param #session_key()
     * @apidoc.param #param_desc("string", "login", "User's login name.")
     * @apidoc.param #array_single_desc("string", "sgNames", "server group names")
     * @apidoc.returntype #return_int_success()
     */
    public int removeDefaultSystemGroups(User loggedInUser, String login, List<String> sgNames) {

        User target = XmlRpcUserHelper.getInstance().lookupTargetUser(
                loggedInUser, login);

        if (sgNames == null || sgNames.isEmpty()) {
            throw new IllegalArgumentException("no servergroup names supplied");
        }

        List<ManagedServerGroup> groups = ServerGroupFactory.listManagedGroups(target.getOrg());
        Map<String, ServerGroup> groupMap = groups.stream().collect(Collectors.toMap(sg -> sg.getName(), sg -> sg));

        // Doing full check of all supplied names, if one is bad
        // throw an exception, prior to altering the DefaultSystemGroup Set.
        for (String name : sgNames) {
            ServerGroup sg = groupMap.get(name);
            if (sg == null) {
                throw new LookupServerGroupException(name);
            }
        }

        // now for the real reason we're in this method.
        Set<Long> defaults = target.getDefaultSystemGroupIds();
        for (String sgNameIn : sgNames) {
            ServerGroup sg = groupMap.get(sgNameIn);
            if (sg != null) {
                // not a simple remove to the groups.  Needs to call
                // UserManager as DataSource is being used.
                defaults.remove(sg.getId());
            }
        }

        UserManager.setDefaultSystemGroupIds(target, defaults);
        UserManager.storeUser(target);

        return 1;
    }

    /**
     * Returns default system groups for the given login.
     * @param loggedInUser The current user
     * in user.
     * @param login The login for the user whose Default ServerGroup list is
     * sought.
     * @return default system groups for the given login
     *
     * @apidoc.doc Returns a user's list of default system groups.
     * @apidoc.param #session_key()
     * @apidoc.param #param_desc("string", "login", "User's login name.")
     * @apidoc.returntype
     *   #return_array_begin()
     *     #struct_begin("system group")
     *       #prop("int", "id")
     *       #prop("string", "name")
     *       #prop("string", "description")
     *       #prop("int", "system_count")
     *       #prop_desc("int", "org_id", "Organization ID for this system group.")
     *     #struct_end()
     *   #array_end()
     */
    @ReadOnly
    public Object[] listDefaultSystemGroups(User loggedInUser, String login) {
        User target = XmlRpcUserHelper.getInstance().lookupTargetUser(
                loggedInUser, login);
        Set<Long> ids =  target.getDefaultSystemGroupIds();


        List<ServerGroup> sgs = new ArrayList<>(ids.size());
        for (Long id : ids) {
            sgs.add(ServerGroupFactory.lookupByIdAndOrg(id, target.getOrg()));
        }
        return sgs.toArray();
    }

    /**
     * Returns the ServerGroups that the user can administer.
     * @param loggedInUser The current user
     * in user.
     * @param login The login for the user whose ServerGroups are sought.
     * @return the ServerGroups that the user can administer.
     * @throws FaultException A FaultException is thrown if the user doesn't
     * have access to lookup the user corresponding to login or if the user
     * does not exist.
     *
     * @apidoc.doc Returns the system groups that a user can administer.
     * @apidoc.param #session_key()
     * @apidoc.param #param_desc("string", "login", "User's login name.")
     * @apidoc.returntype
     *   #return_array_begin()
     *     #struct_begin("system group")
     *       #prop("int", "id")
     *       #prop("string", "name")
     *       #prop("string", "description")
     *       #prop("int", "system_count")
     *       #prop_desc("int", "org_id", "Organization ID for this system group.")
     *     #struct_end()
     *   #array_end()
     */
    @ReadOnly
    public Object[] listAssignedSystemGroups(User loggedInUser, String login)
        throws FaultException {
        User target = XmlRpcUserHelper.getInstance().lookupTargetUser(
                    loggedInUser, login);
        List<ServerGroup> groups = ServerGroupFactory.listAdministeredServerGroups(target);
        return groups.toArray();
    }

    /**
     * remove system group association from a user
     * @param loggedInUser The current user
     * @param login the user's login that we want to remove the association from
     * @param sgNames list of system group names to remove
     * @param setDefault if true the default group will be removed from the users's
     *      group defaults
     * @return 1 on success
     *
     * @apidoc.doc Remove system groups from a user's list of assigned system groups.
     * @apidoc.param #session_key()
     * @apidoc.param #param_desc("string", "login", "User's login name.")
     * @apidoc.param #array_single_desc("string", "sgNames", "server group names")
     * @apidoc.param #param_desc("boolean", "setDefault", "Should system groups also be
     * removed from the user's list of default system groups.")
     * @apidoc.returntype #return_int_success()
     */
    public int removeAssignedSystemGroups(User loggedInUser,
            String login, List<String> sgNames, Boolean setDefault) {
        ensureUserRole(loggedInUser, RoleFactory.ORG_ADMIN);

        if (setDefault) {
            removeDefaultSystemGroups(loggedInUser, login, sgNames);
         }

        User user = UserManager.lookupUser(loggedInUser, login);

        // Iterate once to lookup the server groups and avoid removing some when
        // an exception will only be thrown later:
        List<ManagedServerGroup> groups = new LinkedList<>();
        for (String name : sgNames) {
            ManagedServerGroup sg = null;
            try {
                sg = serverGroupManager.lookup(name, user);
            }
            catch (LookupException e) {
                throw new InvalidServerGroupException();
            }
            groups.add(sg);
        }

        for (ManagedServerGroup sg : groups) {
            UserManager.revokeServerGroupPermission(user, sg.getId());
        }

        return 1;
    }

    /**
     * remove system group association from a user
     * @param loggedInUser The current user
     * @param login the user's login that we want to remove the association from
     * @param sgName  system group name to remove
     * @param setDefault if true the default group will be removed from the users's
     *      group defaults
     * @return 1 on success
     *
     * @apidoc.doc Remove system group from the user's list of assigned system groups.
     * @apidoc.param #session_key()
     * @apidoc.param #param_desc("string", "login", "User's login name.")
     * @apidoc.param #param_desc("string", "sgName", "server group name")
     * @apidoc.param #param_desc("boolean", "setDefault", "Should system group also
     * be removed from the user's list of default system groups.")
     * @apidoc.returntype #return_int_success()
     */
    public int removeAssignedSystemGroup(User loggedInUser, String login, String sgName, Boolean setDefault) {
            return removeAssignedSystemGroups(loggedInUser, login, List.of(sgName), setDefault);
    }



    /**
     * Add to the user's list of assigned system groups.
     *
     * @param loggedInUser The current user
     * @param login User to modify.
     * @param sgName Server group Name.
     * @param setDefault True to also add group to the user's default system groups.
     * @return Returns 1 if successful (exception thrown otherwise)
     *
     * @apidoc.doc Add system group to user's list of assigned system groups.
     * @apidoc.param #session_key()
     * @apidoc.param #param_desc("string", "login", "User's login name.")
     * @apidoc.param #param("string", "sgName", "server group name")
     * @apidoc.param #param_desc("boolean", "setDefault", "Should system group also be
     * added to user's list of default system groups.")
     * @apidoc.returntype #return_int_success()
     */
    public int addAssignedSystemGroup(User loggedInUser, String login, String sgName,
            Boolean setDefault) {
        List<String> names = new LinkedList<>();
        names.add(sgName);
        return addAssignedSystemGroups(loggedInUser, login, names, setDefault);
    }

    /**
     * Add to the user's list of assigned system groups.
     *
     * @param loggedInUser The current user
     * @param login User to modify.
     * @param sgNames List of server group Names.
     * @param setDefault True to also add groups to the user's default system groups.
     * @return Returns 1 if successful (exception thrown otherwise)
     *
     * @apidoc.doc Add system groups to user's list of assigned system groups.
     * @apidoc.param #session_key()
     * @apidoc.param #param_desc("string", "login", "User's login name.")
     * @apidoc.param #array_single_desc("string", "sgNames", "server group names")
     * @apidoc.param #param_desc("boolean", "setDefault", "Should system groups also be
     * added to user's list of default system groups.")
     * @apidoc.returntype #return_int_success()
     */
    public int addAssignedSystemGroups(User loggedInUser, String login, List<String> sgNames,
            Boolean setDefault) {

        User targetUser = XmlRpcUserHelper.getInstance().lookupTargetUser(
                loggedInUser, login);

        if (sgNames == null || sgNames.size() < 1) {
            throw new IllegalArgumentException("no servergroup names supplied");
        }


        // Iterate once just to make sure all the server groups exist. Done to
        // prevent adding a bunch of valid groups and then throwing an exception
        // when coming across one that doesn't exist.
        List<ManagedServerGroup> groups = new LinkedList<>();
        for (Object sgNameIn : sgNames) {
            String serverGroupName = (String) sgNameIn;

            // Make sure the server group exists:
            ManagedServerGroup group;
            try {
                group = serverGroupManager.lookup(serverGroupName, loggedInUser);
            }
            catch (LookupException e) {
                throw new InvalidServerGroupException();
            }
            groups.add(group);
        }

        List<Long> groupIds = groups.stream().map(ManagedServerGroup::getId).collect(Collectors.toList());
        UserManager.grantServerGroupPermission(targetUser.getId(), groupIds);

        // Follow up with a call to addDefaultSystemGroups if setDefault is true:
        if (setDefault) {
            addDefaultSystemGroups(loggedInUser, login, sgNames);
        }

        return 1;
    }

    /**
     * Return the current value of the createDefaultSystemGroup settnig
     * @param loggedInUser The current user
     * Must be org_admin.
     * @return Returns 1 if successful (exception otherwise)
     *
     * @apidoc.doc Returns the current value of the CreateDefaultSystemGroup setting.
     * If True this will cause there to be a system group created (with the same name
     * as the user) every time a new user is created, with the user automatically given
     * permission to that system group and the system group being set as the default
     * group for the user (so every time the user registers a system it will be
     * placed in that system group by default). This can be useful if different
     * users will administer different groups of servers in the same organization.
     * Can only be called by an org_admin.
     * @apidoc.param #session_key()
     * @apidoc.returntype #return_int_success()
     */
    @ReadOnly
    public boolean getCreateDefaultSystemGroup(User loggedInUser) {
        //Logged in user must be an org admin.
        ensureOrgAdmin(loggedInUser);

        return loggedInUser.getOrg().getOrgConfig().isCreateDefaultSg();
    }

    /**
     * Return the current value of the createDefaultSystemGroup settnig
     * @param loggedInUser The current user
     * Must be org_admin.
     * @param createDefaultSystemGroup The value to set
     * @return Returns 1 if successful (exception otherwise)
     *
     * @apidoc.doc Sets the value of the createDefaultSystemGroup setting.
     * If True this will cause there to be a system group created (with the same name
     * as the user) every time a new user is created, with the user automatically given
     * permission to that system group and the system group being set as the default
     * group for the user (so every time the user registers a system it will be
     * placed in that system group by default). This can be useful if different
     * users will administer different groups of servers in the same organization.
     * Can only be called by an org_admin.
     * @apidoc.param #session_key()
     * @apidoc.param #param_desc("boolean", "createDefaultSystemGroup",
     * "true if we should automatically create system groups, false otherwise.")
     * @apidoc.returntype #return_int_success()
     */
    public int setCreateDefaultSystemGroup(User loggedInUser,
            Boolean createDefaultSystemGroup) {
        //Logged in user must be an org admin.
        ensureOrgAdmin(loggedInUser);

        loggedInUser.getOrg().getOrgConfig().setCreateDefaultSg(createDefaultSystemGroup);
        return 1;

    }

    /**
     * @param loggedInUser The current user
     * @param login User to modify.
     * @param readOnly readOnly flag to set
     * @return 1 (should always succeed)
     * @apidoc.doc Sets whether the target user should have only read-only API access or
     * standard full scale access.
     * @apidoc.param #session_key()
     * @apidoc.param #param_desc("string", "login", "User's login name.")
     * @apidoc.param #param_desc("boolean", "readOnly", "Sets whether the target user should
     * have only read-only API access or standard full scale access.")
     * @apidoc.returntype #return_int_success()
     */
    public int setReadOnly(User loggedInUser, String login, Boolean readOnly) {
        //Logged in user must be an org admin.
        ensureOrgAdmin(loggedInUser);

        User targetUser = XmlRpcUserHelper.getInstance().lookupTargetUser(
                loggedInUser, login);

        if (!targetUser.isReadOnly()) {
            if (readOnly && targetUser.hasRole(RoleFactory.ORG_ADMIN) &&
                    targetUser.getOrg().numActiveOrgAdmins() < 2) {
                throw new InvalidOperationException("error.readonly_org_admin",
                        targetUser.getOrg().getName());
            }
            if (readOnly && targetUser.hasRole(RoleFactory.SAT_ADMIN) &&
                    SatManager.getActiveSatAdmins().size() < 2) {
                throw new InvalidOperationException("error.readonly_sat_admin");
            }
        }
        targetUser.setReadOnly(readOnly);
        return 1;
    }

    /**
     * @param loggedInUser The current user
     * @param login User to modify
     * @param value value to enable/disable errata mail notifications
     * @return Returns 1 if successful (exception thrown otherwise)
     * @apidoc.doc Enables/disables errata mail notifications for a specific user.
     * @apidoc.param #session_key()
     * @apidoc.param #param_desc("string", "login", "User's login name.")
     * @apidoc.param #param_desc("boolean", "value", "True for enabling
     * errata notifications, False for disabling")
     * @apidoc.returntype #return_int_success()
     */
    public int setErrataNotifications(User loggedInUser, String login, Boolean value) {
        //Logged in user must be an org admin.
        ensureOrgAdmin(loggedInUser);

        User targetUser = XmlRpcUserHelper.getInstance().lookupTargetUser(
                loggedInUser, login);
        targetUser.setEmailNotify(BooleanUtils.toIntegerObject(value));
        return 1;
    }

}
