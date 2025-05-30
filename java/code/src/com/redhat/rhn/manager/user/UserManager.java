/*
 * Copyright (c) 2009--2014 Red Hat, Inc.
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
package com.redhat.rhn.manager.user;

import static java.util.Optional.of;
import static java.util.stream.Collectors.toList;

import com.redhat.rhn.GlobalInstanceHolder;
import com.redhat.rhn.common.db.datasource.CallableMode;
import com.redhat.rhn.common.db.datasource.DataResult;
import com.redhat.rhn.common.db.datasource.ModeFactory;
import com.redhat.rhn.common.db.datasource.Row;
import com.redhat.rhn.common.db.datasource.SelectMode;
import com.redhat.rhn.common.db.datasource.WriteMode;
import com.redhat.rhn.common.hibernate.LookupException;
import com.redhat.rhn.common.localization.LocalizationService;
import com.redhat.rhn.common.security.PermissionException;
import com.redhat.rhn.common.security.user.StateChangeException;
import com.redhat.rhn.domain.access.AccessGroup;
import com.redhat.rhn.domain.access.Namespace;
import com.redhat.rhn.domain.channel.Channel;
import com.redhat.rhn.domain.common.RhnConfiguration;
import com.redhat.rhn.domain.common.RhnConfigurationFactory;
import com.redhat.rhn.domain.org.Org;
import com.redhat.rhn.domain.org.OrgFactory;
import com.redhat.rhn.domain.role.Role;
import com.redhat.rhn.domain.role.RoleFactory;
import com.redhat.rhn.domain.server.ManagedServerGroup;
import com.redhat.rhn.domain.server.Server;
import com.redhat.rhn.domain.server.ServerGroup;
import com.redhat.rhn.domain.user.Address;
import com.redhat.rhn.domain.user.RhnTimeZone;
import com.redhat.rhn.domain.user.User;
import com.redhat.rhn.domain.user.UserFactory;
import com.redhat.rhn.domain.user.UserServerPreference;
import com.redhat.rhn.domain.user.legacy.UserImpl;
import com.redhat.rhn.frontend.dto.ChannelPerms;
import com.redhat.rhn.frontend.dto.SystemGroupOverview;
import com.redhat.rhn.frontend.dto.SystemSearchResult;
import com.redhat.rhn.frontend.dto.UserOverview;
import com.redhat.rhn.frontend.dto.VisibleSystems;
import com.redhat.rhn.frontend.listview.PageControl;
import com.redhat.rhn.frontend.taglibs.list.decorators.PageSizeDecorator;
import com.redhat.rhn.manager.BaseManager;
import com.redhat.rhn.manager.SatManager;
import com.redhat.rhn.manager.channel.ChannelManager;
import com.redhat.rhn.manager.system.ServerGroupManager;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import javax.security.auth.login.LoginException;

/**
 * UserManager - the singleton class used to provide Business Operations
 * on Users where those operations interact with other top tier Business Objects.
 *
 * Operations that require the User make changes to
 */
public class UserManager extends BaseManager {

    private static Logger log = LogManager.getLogger(UserManager.class);
    private static final String ORG_ADMIN_LABEL = "org_admin";

    private static final ServerGroupManager SERVER_GROUP_MANAGER = GlobalInstanceHolder.SERVER_GROUP_MANAGER;

    private UserManager() {
    }

    /**
     * Returns a list of roles that are assignable by a given user
     * i.e. the list of roles that the passed in user can assign
     * @param user the user for whom the check is being done
     * @return the list of roles assignable by this user.
     */
    public static Set<Role> listRolesAssignableBy(User user) {
        Set<Role> assignable = new LinkedHashSet<>();
        if (user.hasRole(RoleFactory.SAT_ADMIN)) {
            assignable.add(RoleFactory.SAT_ADMIN);
        }
        if (user.hasRole(RoleFactory.ORG_ADMIN)) {
            assignable.add(RoleFactory.ORG_ADMIN);
        }
        return assignable;
    }
    /**
     * Verifies that a given org has access to a given package.
     * @param org The org in question
     * @param packageId The id of the package in question
     * @return Returns true if the org has access to the package, false otherwise.
     */
    public static boolean verifyPackageAccess(Org org, Long packageId) {
        SelectMode m = ModeFactory.getMode("Package_queries", "package_available_to_user");
        Map<String, Object> params = new HashMap<>();
        params.put("pid", packageId);
        params.put("org_id", org.getId());
        DataResult dr = m.execute(params);
        /*
         * Ok... this query will result in returning a single row containing '1' if the
         * org has access to this channel. If the org *does not* have access to the given
         * package (org the package doesn't exist), nothing will be returned from the query
         * and we will end up with an empty DataResult object.
         */
        return (!dr.isEmpty());
    }

    /**
     * Verifies that a given org has access to a given list of packages.
     * @param org The org in question
     * @param packageIds The ids of the packages in question
     * @return Returns true if the org has access to the package, false otherwise.
     */
    public static boolean verifyPackagesAccess(Org org, List<Long> packageIds) {
        SelectMode m = ModeFactory.getMode("Package_queries", "packages_available_to_user");
        Map<String, Object> params = new HashMap<>();
        params.put("org_id", org.getId());
        DataResult<Row> dr = m.execute(params, packageIds);
        /*
         * Ok... this query will result in returning a single row containing '1' if the
         * org has access to this channel. If the org *does not* have access to the given
         * package (org the package doesn't exist), nothing will be returned from the query
         * and we will end up with an empty DataResult object.
         */
        return (!dr.isEmpty());
    }

    /**
     * Get a concatenated stream of namespaces granted direct (to the user)
     * or indirect (via access groups) access.
     * @param user the user
     * @return the stream of permitted namespaces
     */
    public static Stream<Namespace> getPermittedNamespaces(User user) {
        return Stream.concat(user.getNamespaces().stream(),
                user.getAccessGroups().stream().flatMap(g -> g.getNamespaces().stream()));
    }

    /**
     * Ensures that the current user is permitted access to a namespace with the specified access mode
     * @param user the user
     * @param namespace the namespace
     * @param mode the access mode (view/modify)
     * @throws PermissionException if the user is not permitted to access the specified namespace
     */
    public static void ensureRoleBasedAccess(User user, String namespace, Namespace.AccessMode mode) {
        if (!verifyRoleBasedAccess(user, namespace, mode)) {
            if (user == null) {
                log.debug("Access restricted for unauthenticated user to namespace '{}' [{}]", namespace, mode);
            }
            else {
                log.debug("Access restricted for user '{}' to namespace '{}' [{}]",
                        user.getLogin(), namespace, mode);
            }
            throw new PermissionException("You don't have the necessary permissions to access this resource.");
        }
    }

    /**
     * Returns {@code true} if the current user is permitted access to a namespace with the specified access mode
     * @param user the user
     * @param namespace the namespace string
     * @param mode the access mode (view/modify)
     * @return {@code true} if the user is permitted access to the specified namespace
     */
    private static boolean verifyRoleBasedAccess(User user, String namespace, Namespace.AccessMode mode) {
        if (user == null) {
            return false;
        }

        if (user.hasRole(RoleFactory.SAT_ADMIN)) {
            return true;
        }

        // Search through a concatenated stream of namespaces granted direct (to the user)
        // and indirect (via access groups) access.
        return getPermittedNamespaces(user)
                .filter(ns -> namespace.equals(ns.getNamespace()))
                .anyMatch(ns -> mode == null || ns.getAccessMode().equals(mode));
    }

    /**
     * Verifies that the passed in user has admin over the passed in channel.
     * @param user The user to check.
     * @param channel The channel to check.
     * @return Returns true if the user has admin access to this channel, false otherwise.
     */
    public static boolean verifyChannelAdmin(User user, Channel channel) {
       return verifyChannelAdmin(user.getId(), channel);
    }

    /**
     * Verifies that the passed in user id has admin over the passed in channel.
     * @param userId The user id to check.
     * @param channel The channel to check.
     * @return Returns true if the user id has admin access to this channel
     */
    public static boolean verifyChannelAdmin(Long userId, Channel channel) {
        return !ChannelManager.verifyChannelRole(userId, channel.getId(), "manage").isPresent();
    }

    /**
     * Verifies that the passed in user has subscribe access to passed in channel.
     * @param user The user to check.
     * @param channel The channel to check.
     * @return Returns true if the user has subscribe access to this channel,
     *     false otherwise.
     */
    public static boolean verifyChannelSubscribable(User user, Channel channel) {
        return !ChannelManager.verifyChannelRole(user.getId(), channel.getId(), "subscribe").isPresent();
    }

    /**
     * Enables a user.
     * @param enabledBy The user doing the enabling
     * @param userToEnable The user to enable
     */
    public static void enableUser(User enabledBy, User userToEnable) {
        //Make sure both users are in the same org
        if (!userToEnable.getOrg().equals(enabledBy.getOrg())) {
            throw new StateChangeException("userenable.error.sameorg");
        }

        //Make sure user we're trying to enable is disabled
        if (!userToEnable.isDisabled()) {
            return;
        }

        //Make sure enabledBy is an OrgAdmin
        if (!enabledBy.hasRole(RoleFactory.ORG_ADMIN)) {
            throw new StateChangeException("userenable.error.orgadmin");
        }

        //If we make it here everything must be ok
        UserFactory.getInstance().enable(userToEnable, enabledBy);
    }

    /**
     * Disables userToDisable.
     * @param disabledBy The user doing the disabling
     * @param userToDisable The user to disable
     */
    public static void disableUser(User disabledBy, User userToDisable) {
        //Are both users in same org?
        if (!userToDisable.getOrg().equals(disabledBy.getOrg())) {
            throw new StateChangeException("userdisable.error.sameorg");
        }

        //Make sure user we're trying to disable is currently enabled
        if (userToDisable.isDisabled()) {
            return;
        }
        if (!userToDisable.isReadOnly()) {
            if (userToDisable.hasRole(RoleFactory.ORG_ADMIN) &&
                userToDisable.getOrg().numActiveOrgAdmins() < 2) {

                // Is user is the last active orgadmin in org on a satellite?
                // bugzilla: 173542 removed org admin restriction for hosted.
                throw new StateChangeException("userdisable.error.onlyuser");
            }
        }

        //If not deleting self, make sure userToDisable is a normal user and
        //disabledBy is an org admin
        if (!userToDisable.equals(disabledBy)) {
            //Normal users can't disable other users
            if (!disabledBy.hasRole(RoleFactory.ORG_ADMIN)) {
                throw new StateChangeException("userdisable.error.otheruser");
            }

            //Can't disable other org admins
            if (userToDisable.hasRole(RoleFactory.ORG_ADMIN)) {
                throw new StateChangeException("userdisable.error.orgadmin");
            }
        }

        //If we get here things must be ok :)
        UserFactory.getInstance().disable(userToDisable, disabledBy);
    }

    /**
     * Revokes permission from the given User to a list of ServerGroups.
     * @param userId Id of user who no longer needs permission
     * @param serverGroupIds the IDs of the server groups
     */
    public static void revokeServerGroupPermission(Long userId,
            List<Long> serverGroupIds) {
        boolean needsToUpdateServerPerms = executeRevokeServerGroupPermsQuery(userId, serverGroupIds);

        if (needsToUpdateServerPerms) {
            updateServerPermsForUser(userId);
        }
    }

    /**
     * Revokes permission from the given User to the ServerGroup whose id is sgid.
     * @param uid Id of user who no longer needs permission
     * @param sgid ServerGroup ID
     */
    public static void revokeServerGroupPermission(final Long uid,
            final long sgid) {
        revokeServerGroupPermission(uid, Arrays.asList(sgid));
    }

    /**
     * Revokes permission from the given User to the ServerGroup whose id is sgid.
     * @param usr User who no longer needs permission
     * @param sgid ServerGroup ID
     */
    public static void revokeServerGroupPermission(final User usr,
            final long sgid) {
        revokeServerGroupPermission(usr.getId(), sgid);
    }

    /**
     * Updates server group permission to an User given a list of ServerGroup IDs.
     * @param user the user
     * @param serverGroupIds the IDs of the server groups
     */
    @SuppressWarnings("unchecked")
    public static void updateServerGroupPermsForUser(User user, List<Long> serverGroupIds) {
        boolean needsToUpdateServerPerms = false;
        List<Long> userServerGroupIds = user.getAssociatedServerGroups()
                .stream().map(sg -> sg.getId()).collect(toList());

        List<Long> serverGroupIdsToRevoke = userServerGroupIds.stream()
                .filter(id -> !serverGroupIds.contains(id)).collect(toList());
        if (!serverGroupIdsToRevoke.isEmpty()) {
            needsToUpdateServerPerms = executeRevokeServerGroupPermsQuery(user.getId(), serverGroupIdsToRevoke);
        }

        List<Long> serverGroupIdsToGrant = serverGroupIds.stream()
                .filter(id -> !userServerGroupIds.contains(id)).collect(toList());
        if (!serverGroupIdsToGrant.isEmpty()) {
            needsToUpdateServerPerms |= executeGrantServerGroupPermsQuery(user.getId(), serverGroupIdsToGrant);
        }
        if (needsToUpdateServerPerms) {
            updateServerPermsForUser(user.getId());
        }
    }

    /**
     * Executes a query for updating server group permissions to an user, given a list of server groups IDs.
     * @param userId Id of the user who needs permissions
     * @param updateServerGroupPermsQuery the query to be executed
     * @param serverGroupIds IDs of the server groups
     * @return true if there any row was affected by the query. Otherwise, returns false
     */
    private static boolean executeUpdateServerGroupPermsQuery(Long userId, String updateServerGroupPermsQuery,
            List<Long> serverGroupIds) {
        Map<String, Object> params = new HashMap<>();
        params.put("user_id", userId);

        WriteMode writeMode = ModeFactory.getWriteMode("User_queries", updateServerGroupPermsQuery);
        return writeMode.executeUpdate(params, serverGroupIds) > 0;
    }

    /**
     * Executes a query for revoking server group permissions to an user, given a list of server groups IDs.
     * @param userId Id of the user who needs permissions
     * @param serverGroupIds IDs of the server groups
     * @return true if there any row was affected by the query. Otherwise, returns false
     */
    private static boolean executeRevokeServerGroupPermsQuery(Long userId, List<Long> serverGroupIds) {
        return executeUpdateServerGroupPermsQuery(userId, "revoke_server_group_permissions_to_user", serverGroupIds);
    }

    /**
     * Executes a query for granting server group permissions to an user, given a list of server groups IDs.
     * @param userId Id of the user who needs permissions
     * @param serverGroupIds IDs of the server groups
     * @return true if there any row was affected by the query. Otherwise, returns false
     */
    private static boolean executeGrantServerGroupPermsQuery(Long userId, List<Long> serverGroupIds) {
        return executeUpdateServerGroupPermsQuery(userId, "grant_server_group_permissions_to_user", serverGroupIds);
    }

    /**
     * Grants the given User permission to a list of ServerGroup.
     * @param userId the ID of the User
     * @param serverGroupIds the IDs of the server groups
     */
    public static void grantServerGroupPermission(Long userId, List<Long> serverGroupIds) {
        boolean needsToUpdateServerPerms = executeGrantServerGroupPermsQuery(userId, serverGroupIds);

        if (needsToUpdateServerPerms) {
            updateServerPermsForUser(userId);
        }
    }

    /**
     * Updates the server permissions to User.
     * @param userId the ID of the User
     */
    private static void updateServerPermsForUser(Long userId) {
        CallableMode m = ModeFactory.getCallableMode("User_queries",
                "update_perms_for_user");
        Map<String, Object> params = new HashMap<>();
        params.put("user_id", userId);
        m.execute(params, new HashMap<>());
    }

    /**
     * Grants the given User permission to the ServerGroup whose id is sgid.
     * @param usr User who needs permission
     * @param sgid ServerGroup ID
     */
    public static void grantServerGroupPermission(final User usr,
            final long sgid) {
        grantServerGroupPermission(usr.getId(), sgid);
    }

    /**
     * Grants the given User permission to the ServerGroup whose id is sgid.
     * @param uid Id of user who needs permission
     * @param sgid ServerGroup ID
     */
    public static void grantServerGroupPermission(final Long uid,
            final long sgid) {
        grantServerGroupPermission(uid, Arrays.asList(sgid));
     }

    /**
    * Add and remove the specified roles from the user.
    *
    * @param usr The User who's Roles you want to update
    * @param rolesToAdd List of role labels to add.
    * @param rolesToRemove List of role labels to remove.
    */
    public static void addRemoveUserRoles(User usr, List<String> rolesToAdd, List<String> rolesToRemove) {

        log.debug("UserManager.updateUserRolesFromRoleLabels()");

        if (!usr.isReadOnly()) {
            // Make sure last org admin isn't trying to remove his own org admin role:
            if (rolesToRemove.contains(ORG_ADMIN_LABEL)) {
                if (usr.getOrg().numActiveOrgAdmins() <= 1) {
                    LocalizationService ls = LocalizationService.getInstance();
                    throw new PermissionException("Last org admin",
                            ls.getMessage("permission.jsp.title.removerole"),
                            ls.getMessage("permission.jsp.summary.removerole", ls.getMessage(ORG_ADMIN_LABEL)));
                }
            }
        }

        List<String> toAdd = new ArrayList<>(rolesToAdd);
        List<String> toRemove = new ArrayList<>(rolesToRemove);

        // ORG admin role needs to be added last so that others don't get skipped
        if (toAdd.remove(ORG_ADMIN_LABEL)) {
            toAdd.add(ORG_ADMIN_LABEL);
        }

        for (String removeLabel : toRemove) {
            Role removeMe = RoleFactory.lookupByLabel(removeLabel);
            log.debug("Removing role: {}", removeMe.getName());
            usr.removePermanentRole(removeMe);
        }

        for (String addLabel : toAdd) {
            Role r = RoleFactory.lookupByLabel(addLabel);
            log.debug("Adding role: {}", r.getName());
            usr.addPermanentRole(r);
        }
    }

    /**
     * Create brand new personal user using the information found in the
     * User object.
     * @param user Filled out user to create.
     * @param org Org to associate with the user.
     * @param addr Address to associate with the user.
     * @return User Freshly created user.
     */
    public static User createUser(User user, Org org, Address addr) {
        /*
         * Ok, this is a bloody ugly hack, but since the pl/sql used by
         * UserFactory.saveNewUser() is shared and the use pam authentication seems to be
         * the only thing affected by it, we are going to work around it here.
         *
         * The Create_New_User function in the db creates an entry in rhnUserInfo with the
         * default values. This means that anything stored in User.personalInfo gets
         * reset. We need to be able to update the use_pam_authentication column in this
         * table, so save the value, save the user, then set the attribute back to what it
         * was before we called UserManager.createUser(). This will ensure that what was
         * selected on the form is what gets stored with the user (since hibernate will
         * then be taking care of the db values).
         *
         * We really need to a) divorce ourselves from www and oracle apps b) get rid of the
         * application/business logic stored in pl/sql functions in the db and c) clean up
         * the dirty hacks like this that are throughout our code. We shouldn't have to work
         * around the db in our code.
         */
        boolean usePam = user.getUsePamAuthentication(); //save what we got from the form
        org = OrgFactory.save(org);

        user = UserFactory.saveNewUser(user, addr, org.getId());

        user.setUsePamAuthentication(usePam); //set it back

        //Set default page size also :)
        user.setPageSize(PageSizeDecorator.getDefaultPageSize());
        storeUser(user); //save the user via hibernate

        return user;
    }

    /**
     * Get the set of default system groups for this user
     * @param usr User for which to get the default system groups.
     * @return groupSet Set of default system groups IDs for the user.
     */
    public static Set<Long> getDefaultSystemGroupIds(User usr) {
        SelectMode prefixMode = ModeFactory.getMode("User_queries",
                                                    "default_system_groups");

        Map<String, Object> params = new HashMap<>();
        params.put("user_id", usr.getId());
        DataResult<Row> dr = prefixMode.execute(params);

        return dr.stream().map(row -> (Long)row.get("system_group_id")).collect(Collectors.toSet());
    }

    /**
     * Set the defaultSystemGroups for the specified User.  This method first
     * deletes all current groups and then adds all of the specified groups.
     * The assumption is that we never add a lot of default system groups at
     * one time, so it is cheaper to just delete and re-add than to compute
     * the difference and commit just the changes.
     * @param usr User for which to set the default groups.
     * @param groups Set of groups to associate with the user.
     */
    public static void setDefaultSystemGroupIds(final User usr, final Set<Long> groups) {
        WriteMode m = ModeFactory.getWriteMode("User_queries",
                "delete_all_system_groups_for_user");
        Map<String, Object> params = new HashMap<>();
        params.put("user_id", usr.getId());
        m.executeUpdate(params);

        m = ModeFactory.getWriteMode("User_queries", "set_system_group");
        for (Long sgid : groups) {
            params.put("sgid", sgid);
            m.executeUpdate(params);
        }
    }

    private static User performLoginActions(User user) {
        user.setLastLoggedIn(new Date());
        RhnConfigurationFactory factory = RhnConfigurationFactory.getSingleton();
        boolean keepTempRoles = factory.getBooleanConfiguration(RhnConfiguration.KEYS.EXTAUTH_KEEP_TEMPROLES)
                .getValue();
        if (!keepTempRoles) {
            // delete all temporary roles
            UserManager.resetTemporaryRoles(user, new HashSet<>());
        }
        // need to disable OAI_SYNC during login
        storeUser(user);
        return user;
    }


    /**
     * Login the user with the given username and password.
     * @param username User's login name
     * @param password User's unencrypted password.
     * @return Returns the user if login is successful, or null othewise.
     * @throws LoginException if login fails.  The message is a string resource key.
     */
    public static User loginUser(String username, String password) throws LoginException {
        if (username == null) {
            throw new LoginException("error.invalid_login");
        }

        String exceptionType = null;
        try {
            User user = UserFactory.lookupByLogin(username);
            if (!user.authenticate(password)) {
                exceptionType = "error.invalid_login";
            }
            else if (user.isDisabled()) {
                exceptionType = "account.disabled";
            }
            else if (user.isReadOnly()) { // KEEP LAST!!
                exceptionType = "error.user_readonly";
            }
            else {
                return performLoginActions(user);
            }
        }
        catch (LookupException le) {
            exceptionType = "error.invalid_login";
        }
        // invalid login/password; set timeout to baffle
        // brute force password guessing attacks (BZ 672163)
        try {
            Thread.sleep(2000);
        }
        catch (InterruptedException ie) {
            log.warn("Failed to set timeout: {}", ie.getMessage());
        }
        throw new LoginException(exceptionType);
    }

    /**
     * This method should be ONLY called when we need to authenticate read-only user
     * e.g. for purpose of the API calls
     * Login the user with the given username and password.
     * @param username User's login name
     * @param password User's unencrypted password.
     * @return Returns the user if login is successful, or null othewise.
     * @throws LoginException if login fails.  The message is a string resource key.
     */
    public static User loginReadOnlyUser(String username,
            String password) throws LoginException {
        try {
            loginUser(username, password);
        }
        catch (LoginException e) {
            // if exception type is error.user_readonly everything else went well
            // and we can safely log the read-only user
            if (!e.getMessage().equals("error.user_readonly")) {
                throw e;
            }
        }
        User user = UserFactory.lookupByLogin(username);
        user.authenticate(password);
        return performLoginActions(user);
    }

    /**
     * Updates the Users to the database
     * @param user User to store.
     */
    public static void storeUser(User user) {
        UserFactory.save(user);
    }


    /**
     * Deletes a User
     * @param loggedInUser The user doing the deleting
     * @param targetUid The id for the user we're deleting
     */
    public static void deleteUser(User loggedInUser, Long targetUid) {
        if (!loggedInUser.hasRole(RoleFactory.ORG_ADMIN)) {
            //Throw an exception with a nice error message so the user
            //knows what went wrong.
            LocalizationService ls = LocalizationService.getInstance();
            throw new PermissionException("Deleting a user requires an Org Admin.",
                    ls.getMessage("permission.jsp.title.deleteuser"),
                    ls.getMessage("permission.jsp.summary.deleteuser"));
        }

        // Do not allow deletion of the last Satellite Administrator:
        User toDelete = UserFactory.lookupById(loggedInUser, targetUid);
        // 1542556 - Check all remaining SW admins, not just active.
        if (toDelete.hasRole(RoleFactory.SAT_ADMIN)) {
            if (SatManager.getAllSatAdmins().size() == 1) {
                log.warn("Cannot delete the last SUSE Multi-Linux Manager Administrator");
                throw new DeleteSatAdminException(toDelete);
            }
        }

        UserFactory.deleteUser(targetUid);
    }

    /**
     * Retrieve the specified user, assuming that the User making the request
     * has the required permissions.
     * @param user The user making the lookup request.
     * @param uid The id of the user to lookup.
     * @return the specified user.
     * @throws com.redhat.rhn.common.hibernate.LookupException if the User
     * can't be looked up.
     */
    public static User lookupUser(User user, Long uid) {
        if (uid == null) {
            return null;
        }

        if (user.getId().equals(uid)) {
            return user;
        }

        LocalizationService ls = LocalizationService.getInstance();

        if (!user.hasRole(RoleFactory.ORG_ADMIN)) {
            //Throw an exception with a nice error message so the user
            //knows what went wrong.
            throw new PermissionException("Lookup user requires Org Admin",
                    ls.getMessage("permission.jsp.title.lookupuser"),
                    ls.getMessage("permission.jsp.summary.lookupuser"));
        }

        return UserFactory.lookupById(user, uid);
    }

    /**
     * Check role for the specified user.
     * @param uid The id of the user to lookup.
     * @param label Role to check.
     * @return {@code True} if the user has the specified role.
     */
    public static boolean hasRole(Long uid, Role label) {
        if (uid == null) {
            return false;
        }

        return UserFactory.lookupById(uid).hasRole(label);
    }

    /**
     * Check RBAC memberships of the specified user.
     * @param uid The id of the user to lookup.
     * @param group the access group to check.
     * @return {@code True} if the user is a member of the access group.
     */
    public static boolean isMemberOf(Long uid, AccessGroup group) {
        if (uid == null) {
            return false;
        }

        return UserFactory.lookupById(uid).isMemberOf(group);
    }

    /**
     * Retrieve the specified user, assuming that the User making the request
     * has the required permissions.
     * @param user The user making the lookup request
     * @param login The login of the user to lookup.
     * @return the specified user.
     */
    public static User lookupUser(User user, String login) {
        User returnedUser = null;
        if (login == null) {
            return null;
        }

        if (user.getLogin().equals(login)) {
            return user;
        }

        LocalizationService ls = LocalizationService.getInstance();

        if (!user.hasRole(RoleFactory.ORG_ADMIN)) {
            //Throw an exception with a nice error message so the user
            //knows what went wrong.
            throw new PermissionException("Lookup user requires Org Admin",
                    ls.getMessage("permission.jsp.title.lookupuser"),
                    ls.getMessage("permission.jsp.summary.lookupuser"));
        }

        returnedUser = UserFactory.lookupByLogin(user, login);
        return returnedUser;
    }

    /**
     * Retrieve the list of all users in the specified user's org.
     * @param user The user who's org to search for users.
     * @return A list of users.
     */
    public static List<UserImpl> usersInOrg(User user) {
        if (!user.hasRole(RoleFactory.ORG_ADMIN)) {
            throw getNoAdminError();
        }
        return UserFactory.getInstance().findAllUsers(of(user.getOrg()));
    }

    /**
     * Retrieve the list of all users in the specified user's org. Returns DataResult
     * containing the default objects specified in User_queries.xml
     * @param user The user who's org to search for users.
     * @param pc The details of which results to return.
     * @return A DataResult containing the specified number of users.
     */
    public static DataResult<UserOverview> usersInOrg(User user, PageControl pc) {
        SelectMode m = ModeFactory.getMode("User_queries", "users_in_org");
        return getUsersInOrg(user, pc, m);
    }

    /**
     * Retrieve the list of all users in the specified user's org. Returns DataResult
     * containing Map objects.
     * @param user The user who's org to search for users.
     * @param pc The details of which results to return.
     * @param clazz The class you want the returned DataResult to contain.
     * @return A DataResult containing the specified number of users.
     */
    public static DataResult<UserOverview> usersInOrg(User user,
                                            PageControl pc, Class clazz) {
        SelectMode m = ModeFactory.getMode("User_queries", "users_in_org", clazz);
        return getUsersInOrg(user, pc, m);
    }

    /**
     * Helper method for usersInOrg* methods
     * @param user The user who's org to search for users.
     * @param pc The details of which results to return.
     * @param m The select mode.
     * @return A list containing the specified number of users.
     */
    private static DataResult<UserOverview> getUsersInOrg(User user,
                                                PageControl pc, SelectMode m) {
        if (!user.hasRole(RoleFactory.ORG_ADMIN)) {
            throw getNoAdminError();
        }
        Map<String, Object> params = new HashMap<>();
        params.put("org_id", user.getOrg().getId());
        return makeDataResult(params, new HashMap<>(), pc, m);
    }

    /**
     * Retrieve the list of all active users in the specified user's org
     * @param user The user who's org to search for users.
     * @param pc The details of which results to return.
     * @return A list containing the specified number of users.
     */
    public static DataResult<UserOverview> activeInOrg(User user, PageControl pc) {
        if (!user.hasRole(RoleFactory.ORG_ADMIN)) {
            throw getNoAdminError();
        }
        SelectMode m = ModeFactory.getMode("User_queries", "active_in_org");
        Map<String, Object> params = new HashMap<>();
        params.put("org_id", user.getOrg().getId());
        return  makeDataResult(params, new HashMap<>(), pc, m);
    }

    /**
     * Retrieve the list of all active users in the specified user's org
     * @param user The user who's org to search for users.
     * @return A list containing the specified number of users.
     */
    public static DataResult<UserOverview> activeInOrg2(User user) {
        SelectMode m = ModeFactory.getMode("User_queries", "active_in_org");
        Map<String, Object> params = new HashMap<>();
        params.put("org_id", user.getOrg().getId());
        DataResult<UserOverview> dr = m.execute(params);
        dr.elaborate(new HashMap<>());
        return dr;
    }

    /**
     * Retrieve the list of all disabled users in the specified user's org
     * @param user The user who's org to search for users.
     * @param pc The details of which results to return.
     * @return A list containing the specified number of users.
     */
    public static DataResult<UserOverview> disabledInOrg(User user, PageControl pc) {
        if (!user.hasRole(RoleFactory.ORG_ADMIN)) {
            throw getNoAdminError();
        }
        SelectMode m = ModeFactory.getMode("User_queries", "disabled_in_org");
        Map<String, Object> params = new HashMap<>();
        params.put("org_id", user.getOrg().getId());
        return makeDataResult(params, new HashMap<>(), pc, m);
    }

    private static PermissionException getNoAdminError() {
        //Throw an exception with a nice error message so the user
        //knows what went wrong.
        LocalizationService ls = LocalizationService.getInstance();
        return new PermissionException("User must be an Org Admin to access the user list",
                ls.getMessage("permission.jsp.title.userlist"),
                ls.getMessage("permission.jsp.summary.userlist"));
    }

    /**
     * Retrieve the list of Channels the user can subscribe to
     * @param user The user who's channels to search for.
     * @param pc The details of which results to return.
     * @return A list containing the specified number of channels.
     */
    public static DataResult<ChannelPerms> channelSubscriptions(User user, PageControl pc) {
        SelectMode m = ModeFactory.getMode("Channel_queries",
                                           "user_subscribe_perms");
        Map<String, Object> params = new HashMap<>();
        params.put("org_id", user.getOrg().getId());
        params.put("user_id", user.getId());
        return makeDataResult(params, new HashMap<>(), pc, m);
    }

    /**
     * Retrieve the list of Channels the user can manage
     * @param user The user who's channels to search for.
     * @param pc The details of which results to return.
     * @return A list containing the specified number of channels.
     */
    public static DataResult<ChannelPerms> channelManagement(User user, PageControl pc) {
        SelectMode m = ModeFactory.getMode("Channel_queries", "user_manage_perms");
        Map<String, Object> params = new HashMap<>();
        params.put("org_id", user.getOrg().getId());
        params.put("user_id", user.getId());
        return makeDataResult(params, new HashMap<>(), pc, m);
    }

    /**
     * Retrieve the list of systems visible to a particular user
     * @param user The user in question
     * @param pc The details of which results to return
     * @return A list containing the visible systems for the user
     */
    public static DataResult<VisibleSystems> visibleSystems(User user, PageControl pc) {
        SelectMode m = ModeFactory.getMode("System_queries", "visible_to_uid");
        Map<String, Object> params = new HashMap<>();
        params.put("formvar_uid", user.getId());
        if (pc != null) {
            return makeDataResult(params, params, pc, m);
        }
        DataResult<VisibleSystems> dr = m.execute(params);
        dr.setTotalSize(dr.size());
        return dr;
    }

    /**
     * Generic visibleSystems that returns all systems visible to a
     * particular user.
     * @param user The user in question
     * @return A list containing the visible systems for the user
     */
    public static DataResult<VisibleSystems> visibleSystems(User user) {
        return visibleSystems(user, null);
    }

    /**
     * Returns visible Systems as a SystemSearchResult Object
     * @param user the user we want
     * @param ids the list of desired system ids
     * @return DataResult of systems
     */
    public static DataResult<SystemSearchResult> visibleSystemsAsDtoFromList(User user,
            List<Long> ids) {

        if (ids.isEmpty()) {
            return null;
        }
        SelectMode m = ModeFactory.getMode("System_queries",
            "visible_to_user_from_sysid_list");

        Map<String, Object> params = new HashMap<>();
        params.put("user_id", user.getId());

        DataResult<SystemSearchResult> dr = m.execute(params, ids);
        dr.setElaborationParams(Collections.emptyMap());
        return dr;
    }

    /**
     * Returns the users in the given set
     * @param user The user
     * @param label The name of the set
     * @param pc Page Control
     * @return completed DataResult
     */
    public static DataResult<UserOverview> usersInSet(User user, String label, PageControl pc) {
        SelectMode m = ModeFactory.getMode("User_queries", "in_set");
        Map<String, Object> params = new HashMap<>();
        params.put("user_id", user.getId());
        params.put("set_label", label);
        return makeDataResult(params, new HashMap<>(), pc, m);
    }

    /**
     * can a system group be administered by user?
     * @param user User
     * @param group SystemGroup
     * @return true if user can administer system group
     */
    public static boolean canAdministerSystemGroup(User user, ManagedServerGroup group) {
        return (user != null && group != null && SERVER_GROUP_MANAGER.canAccess(user, group));
    }

    /**
     * Returns the System Groups associated with the given User
     * bounded by the values of the PageControl.
     * @param user User whose SystemGroups are sought.
     * @param pc Bounding PageControl
     * @return The DataResult of the SystemGroups.
     */
    public static DataResult<SystemGroupOverview> getSystemGroups(User user, PageControl pc) {
        SelectMode m = ModeFactory.getMode("SystemGroup_queries",
                                           "user_permissions", SystemGroupOverview.class);
        Map<String, Object> params = new HashMap<>();
        params.put("user_id", user.getId());
        params.put("org_id", user.getOrg().getId());
        DataResult<SystemGroupOverview> dr = m.execute(params);
        dr.setTotalSize(dr.size());
        if (pc != null && !dr.isEmpty()) {
                dr = dr.subList(pc.getStart() - 1, pc.getEnd());
                dr.elaborate(new HashMap<>());
        }
        return dr;
    }

    /**
     * Gets a timezone object based on id
     * @param id timezone id number
     * @return the timezone requested
     */
    public static RhnTimeZone getTimeZone(int id) {
        return UserFactory.getTimeZone(id);
    }

    /**
     * Gets a timezone object based on olson name
     * @param olsonName timezone olson name
     * @return the timezone requested
     */
    public static RhnTimeZone getTimeZone(String olsonName) {
        return UserFactory.getTimeZone(olsonName);
    }

    /**
     * Gets the default timezone object
     * @return the default timezone object
     */
    public static RhnTimeZone getDefaultTimeZone() {
        return UserFactory.getDefaultTimeZone();
    }

    /**
     * Gets all timezone objects in the appropriate order
     * @return a list of ordered timezones
     */
    public static List lookupAllTimeZones() {
        return UserFactory.lookupAllTimeZones();
    }

    /**
     * Removes channel permissions from a user or nothing if they already have
     * no channel permissions.
     * @param user The user for which the perm is changing
     * @param cid The channel identifier
     * @param role The role the user is losing for this channel.
     */
    public static void removeChannelPerm(User user, Long cid, String role) {
        user.getOrg().removeChannelPermissions(user.getId(), cid, role);
    }

    /**
     * Adds channel permissions from a user.
     * Does nothing if the channel cannot be viewed by this user's org
     * @param user The user for which the perm is changing
     * @param cid The channel identifier
     * @param role The role the user is gaining for this channel.
     */
    public static void addChannelPerm(User user, Long cid, String role) {
        //first figure out if this channel is visible by the user's org
        boolean permittedAction = false;
        Iterator<Channel> channels = user.getOrg().getAccessibleChannels().iterator();
        while (!permittedAction && channels.hasNext()) {
            if ((channels.next()).getId().equals(cid)) {
                permittedAction = true;
            }
        }

        //now perform the action
        if (permittedAction) {
            user.getOrg().resetChannelPermissions(user.getId(), cid, role);
        }
    }
    /**
     * Method to determine whether a satellite has any users. Returns
     * true if satellite has one or more users, false otherwise.  Also
     * returns false if this method is called on a hosted installation.
     * @return true if satellite has one or more users, false otherwise.
     */
    public static boolean satelliteHasUsers() {
        return UserFactory.satelliteHasUsers();
    }

    /**
     * Returns the responsible user (the first orgadmin in the org)
     * @param org Org to search
     * @param r Org_admin role
     * @see com.redhat.rhn.domain.role.RoleFactory#ORG_ADMIN
     * @return User with the login and id populated.
     */
    public static User findResponsibleUser(Org org, Role r) {
        return UserFactory.findResponsibleUser(org.getId(), r);
    }

    /**
     * Looks up the value of a user's server preference.
     * @param user user to lookup the preference
     * @param server server that the preference corresponds to
     * @param preferenceName the name of the preference
     * @see com.redhat.rhn.domain.user.UserServerPreferenceId
     * @return true if no value is present, false if the value is present and equal to 0
     * otherwise
     */
    public static boolean lookupUserServerPreferenceValue(User user,
                                                          Server server,
                                                          String preferenceName) {
        UserFactory factory = UserFactory.getInstance();
        UserServerPreference pref = factory.
                                        lookupServerPreferenceByUserServerAndName(user,
                                                                     server,
                                                                     preferenceName);

        if (pref == null) {
            return true;
        }
        return !pref.getValue().equals("0");
    }

    /**
     * Sets a UserServerPreference to true or false
     * @param user User whose preference will be set
     * @param server Server we are setting the perference on
     * @param preferenceName the name of the preference
     * @see com.redhat.rhn.domain.user.UserServerPreferenceId
     * @param value true if the preference should be true, false otherwise
     */
    public static void setUserServerPreferenceValue(User user,
                                                    Server server,
                                                    String preferenceName,
                                                    boolean value) {
        UserFactory.getInstance().
                setUserServerPreferenceValue(user, server, preferenceName, value);
    }

    /**
     * set temporary roles to the current set
     * @param userIn affected user
     * @param temporaryRolesIn temporary roles set
     */
    public static void resetTemporaryRoles(User userIn, Set<Role> temporaryRolesIn) {

        Set<Role> currentRoles = userIn.getRoles();
        for (Role role : userIn.getOrg().getRoles()) {
            if (temporaryRolesIn.contains(role) && !currentRoles.contains(role)) {
                userIn.addTemporaryRole(role);
            }
            else if (!temporaryRolesIn.contains(role) && currentRoles.contains(role)) {
                userIn.removeTemporaryRole(role);
            }
        }
    }

    /**
     * serialize role names
     * @param rolesIn roles to put into string
     * @return roles string
     */
    public static String roleNames(Set<Role> rolesIn) {
        String roleNames = null;
        for (Role role : rolesIn) {
            roleNames = (roleNames == null) ? role.getName() :
                roleNames + ", " + role.getName();
        }
        if (roleNames == null) {
            return "(normal user)";
        }
        return roleNames;
    }

    /**
     * serialize role names
     * @param serverGroupsIn roles to put into string
     * @return roles string
     */
    public static String serverGroupsName(Set<ServerGroup> serverGroupsIn) {
        String serverGroupsName = null;
        for (ServerGroup sg : serverGroupsIn) {
            serverGroupsName = (serverGroupsName == null) ? sg.getName() :
                serverGroupsName + ", " + sg.getName();
        }
        return Objects.requireNonNullElse(serverGroupsName, "(none)");
    }
}
