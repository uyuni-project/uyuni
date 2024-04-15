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
package com.redhat.rhn.frontend.xmlrpc.systemgroup;

import static java.util.stream.Collectors.toList;

import com.redhat.rhn.FaultException;
import com.redhat.rhn.common.hibernate.LookupException;
import com.redhat.rhn.domain.common.SatConfigFactory;
import com.redhat.rhn.domain.config.ConfigChannel;
import com.redhat.rhn.domain.formula.Formula;
import com.redhat.rhn.domain.formula.FormulaFactory;
import com.redhat.rhn.domain.role.RoleFactory;
import com.redhat.rhn.domain.server.ManagedServerGroup;
import com.redhat.rhn.domain.server.Server;
import com.redhat.rhn.domain.server.ServerGroup;
import com.redhat.rhn.domain.server.ServerGroupFactory;
import com.redhat.rhn.domain.state.StateFactory;
import com.redhat.rhn.domain.user.User;
import com.redhat.rhn.domain.user.UserFactory;
import com.redhat.rhn.frontend.dto.SystemOverview;
import com.redhat.rhn.frontend.xmlrpc.BaseHandler;
import com.redhat.rhn.frontend.xmlrpc.LookupServerGroupException;
import com.redhat.rhn.frontend.xmlrpc.NoSuchChannelException;
import com.redhat.rhn.frontend.xmlrpc.ServerGroupAccessChangeException;
import com.redhat.rhn.frontend.xmlrpc.TaskomaticApiException;
import com.redhat.rhn.frontend.xmlrpc.system.XmlRpcSystemHelper;
import com.redhat.rhn.manager.configuration.ConfigurationManager;
import com.redhat.rhn.manager.errata.ErrataManager;
import com.redhat.rhn.manager.system.ServerGroupManager;
import com.redhat.rhn.manager.system.SystemManager;

import com.suse.manager.api.ReadOnly;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

/**
 * ServerGroupHandler
 * @apidoc.namespace systemgroup
 * @apidoc.doc Provides methods to access and modify system groups.
 */
public class ServerGroupHandler extends BaseHandler {

    private final XmlRpcSystemHelper xmlRpcSystemHelper;
    private final ServerGroupManager serverGroupManager;

    /**
     * @param xmlRpcSystemHelperIn XmlRpcSystemHelper
     * @param serverGroupManagerIn
     */
    public ServerGroupHandler(XmlRpcSystemHelper xmlRpcSystemHelperIn, ServerGroupManager serverGroupManagerIn) {
        xmlRpcSystemHelper = xmlRpcSystemHelperIn;
        serverGroupManager = serverGroupManagerIn;
    }

    /**
     * Given a systemGroupName this call returns the list of users
     * who can administer the group. One has to be a SystemGroupAdmin
     * or an Org Admin to obtain this list..
     * @param loggedInUser The current user
     * @param systemGroupName the name of the system group
     * @return a list of users who can administer this system group.
     *
     * @apidoc.doc Returns the list of users who can administer the given group.
     * Caller must be a system group admin or an organization administrator.
     * @apidoc.param #session_key()
     * @apidoc.param #param("string", "systemGroupName")
     * @apidoc.returntype
     *  #return_array_begin()
     *      $UserSerializer
     *   #array_end()
     */
    @ReadOnly
    public List<User> listAdministrators(User loggedInUser, String systemGroupName) {
        ManagedServerGroup sg = serverGroupManager.lookup(systemGroupName, loggedInUser);
        return serverGroupManager.listAdministrators(sg, loggedInUser);
    }

    /**
     * Given a systemGroupName and a list of users
     * this call adds or removes them as system administrators
     * Note one needs to be  an Org Admin to perform this
     * operation..
     * @param loggedInUser The current user
     * @param systemGroupName the name of the system group
     * @param loginNames login names of users to be made admins..
     * @param add a boolean to associate  or dissociate admins from the group
     * @return 1 if the operation succeed 1 Exception other wise.
     *
     * @apidoc.doc Add or remove administrators to/from the given group. #product() and
     * Organization administrators are granted access to groups within their organization
     * by default; therefore, users with those roles should not be included in the array
     * provided. Caller must be an organization administrator.
     *
     * @apidoc.param #session_key()
     * @apidoc.param #param("string", "systemGroupName")
     * @apidoc.param #array_single_desc("string", "loginName", "User's loginName")
     * @apidoc.param #param_desc("int", "add", "1 to add administrators, 0 to remove.")
     * @apidoc.returntype #return_int_success()
     */
    public int addOrRemoveAdmins(User loggedInUser, String systemGroupName,
                                        List<String> loginNames, boolean add) {
        ensureSystemGroupAdmin(loggedInUser);

        // Check to see if any of the users provided are Satellite or Organization
        // admins.  If so, generate an exception.  These users are granted access
        // by default and their access may not be changed.
        String admins = null;
        for (String login : loginNames) {
            User user = UserFactory.lookupByLogin(login);
            if ((user != null) && ((user.hasRole(RoleFactory.SAT_ADMIN) ||
                (user.hasRole(RoleFactory.ORG_ADMIN))))) {
                if (admins == null) {
                    admins = new String(login);
                }
                else {
                    admins += ", " + login;
                }
            }
        }
        if (admins != null) {
            throw new ServerGroupAccessChangeException(admins);
        }
        ManagedServerGroup group = serverGroupManager.lookup(systemGroupName, loggedInUser);

        serverGroupManager.associateOrDissociateAdminsByLoginName(group, loginNames,
                                                            add, loggedInUser);

        return 1;
    }


    /**
     * List the systems that are associated to the given system group.
     * @param loggedInUser The current user
     * @param systemGroupName the name of the system group
     * @return a list of systems associated to a given system group.
     *
     * @apidoc.doc Return a list of systems associated with this system group.
     * User must have access to this system group.

     * @apidoc.param #session_key()
     * @apidoc.param #param("string", "systemGroupName")
     * @apidoc.returntype
     *      #return_array_begin()
     *          $ServerSerializer
     *      #array_end()
     */
    @ReadOnly
    public List<Server> listSystems(User loggedInUser, String systemGroupName) {
        ManagedServerGroup group = serverGroupManager.lookup(systemGroupName, loggedInUser);
        return group.getServers();
    }

    /**
     * List the systems that are associated to the given system group.
     * @param loggedInUser The current user
     * @param systemGroupName the name of the system group
     * @return a list of systems associated to a given system group.
     *
     * @apidoc.doc Return a list of systems associated with this system group.
     * User must have access to this system group.

     * @apidoc.param #session_key()
     * @apidoc.param #param("string", "systemGroupName")
     * @apidoc.returntype
     *      #return_array_begin()
     *          $SystemOverviewSerializer
     *      #array_end()
     */
    @ReadOnly
    public List<SystemOverview> listSystemsMinimal(User loggedInUser, String systemGroupName) {
        ManagedServerGroup group = serverGroupManager.lookup(systemGroupName, loggedInUser);
        return SystemManager.systemsInGroupShort(group.getId());
    }

    /**
     * Associates a list of servers to a given group
     * @param loggedInUser The current user
     * @param systemGroupName The name system group to whom you want to add servers
     * @param serverIds  a list of ids of the servers you wish to add to this group.
     * @param add should this server be associated or dissociated to this group.
     * @return Returns 1 if successful, exception otherwise
     *
     * @apidoc.doc Add/remove the given servers to a system group.
     * @apidoc.param #session_key()
     * @apidoc.param #param("string", "systemGroupName")
     * @apidoc.param #array_single("int", "serverIds")
     * @apidoc.param #param_desc("boolean", "add", "True to add to the group,
     *              False to remove.")
     * @apidoc.returntype #return_int_success()
     */
    public int addOrRemoveSystems(User loggedInUser, String systemGroupName,
            List serverIds, Boolean add) {

        ManagedServerGroup group = serverGroupManager.lookup(systemGroupName, loggedInUser);

        List servers = xmlRpcSystemHelper.lookupServers(loggedInUser, serverIds);

        if (add) {
            serverGroupManager.addServers(group, servers, loggedInUser);
        }
        else {
            serverGroupManager.removeServers(group, servers, loggedInUser);
        }
        return 1;
    }

    /**
     * Creates a new system group.. User needs to be a System Group Admin
     * or an OrgAdmin to be able to create new  system groups.
     * @param loggedInUser The current user
     * @param name The name of the system group..
     *              Note duplicates names cannot be created
     *              and will be responded to with an exception.
     * @param description The description of a system group.
     * @return the name of the system group created.
     *
     * @apidoc.doc Create a new system group.
     * @apidoc.param #session_key()
     * @apidoc.param #param_desc("string", "name", "Name of the system group.")
     * @apidoc.param #param_desc("string", "description", "Description of the
     *                  system group.")
     * @apidoc.returntype $ManagedServerGroupSerializer
     */
    public ServerGroup create(User loggedInUser, String name, String description) {
        ensureSystemGroupAdmin(loggedInUser);
        return serverGroupManager.create(loggedInUser, name, description);
    }

    /**
     * Deletes a given system group - given the system group name
     * @param loggedInUser The current user
     * @param systemGroupName the name of the system group
     * @return 1 for success exception  other wise.
     *
     * @apidoc.doc Delete a system group.
     * @apidoc.param #session_key()
     * @apidoc.param #param("string", "systemGroupName")
     * @apidoc.returntype #return_int_success()
     */
    public int delete(User loggedInUser, String systemGroupName) {
        ensureSystemGroupAdmin(loggedInUser);
        ManagedServerGroup group = serverGroupManager.lookup(systemGroupName, loggedInUser);
        serverGroupManager.remove(loggedInUser, group);
        return 1;
    }

    /**
     * Updates a system group. User needs to be a System Group Admin
     * or an OrgAdmin to be able to create new  system groups.
     * @param loggedInUser The current user
     * @param systemGroupName The name of the system group that needs to updated..
     * @param description The description of the system group.
     * @return the updated system group.
     *
     * @apidoc.doc Update an existing system group.
     * @apidoc.param #session_key()
     * @apidoc.param #param("string", "systemGroupName")
     * @apidoc.param #param("string", "description")
     * @apidoc.returntype $ManagedServerGroupSerializer
     */
    public ServerGroup update(User loggedInUser,
                                String systemGroupName, String description) {
        ManagedServerGroup group = serverGroupManager.lookup(systemGroupName, loggedInUser);
        group.setDescription(description);
        ServerGroupFactory.save(group);
        return group;
    }



    /**
     * Returns a list of system groups that do not
     * have a NON-OrgAdmin administrator..
     * Org admins implicitly have access to all
     * system groups, so this call would not make
     * sense for those cases..
     * Note the caller must be an orgadmin to get this
     *  information..
     *
     * @param loggedInUser The current user
     * @return List of ServerGroups that do not have an associated admin.
     *
     * @apidoc.doc Returns a list of system groups that do not have an administrator.
     * (who is not an organization administrator, as they have implicit access to
     * system groups) Caller must be an organization administrator.
     * @apidoc.param #session_key()
     * @apidoc.returntype
     *      #return_array_begin()
     *          $ManagedServerGroupSerializer
     *      #array_end()
     */
    @ReadOnly
    public List<ServerGroup> listGroupsWithNoAssociatedAdmins(User loggedInUser) {
        ensureOrgAdmin(loggedInUser);
        return serverGroupManager.listNoAdminGroups(loggedInUser);
    }


    /**
     * List all groups accessible by the logged in user
     * @param loggedInUser The current user
     * @return a list of ServerGroup objects
     *
     * @apidoc.doc Retrieve a list of system groups that are accessible by the logged
     *      in user.
     * @apidoc.param #session_key()
     * @apidoc.returntype
     *      #return_array_begin()
     *          $ManagedServerGroupSerializer
     *      #array_end()
     */
    @ReadOnly
    public List<ManagedServerGroup> listAllGroups(User loggedInUser) {
        List<ManagedServerGroup> groups = ServerGroupFactory.listManagedGroups(
                loggedInUser.getOrg());
        List<ManagedServerGroup> toReturn = new ArrayList<>();
        for (ManagedServerGroup group : groups) {
            if (serverGroupManager.canAccess(loggedInUser, group)) {
                toReturn.add(group);
            }
        }
        return toReturn;
    }

    /**
     *
     * @param loggedInUser The current user
     * @param systemGroupId Integer id of system group to look up
     * @return ServerGroup object
     * @throws FaultException A FaultException is thrown if the server group
     * corresponding to systemGroupId cannot be retrieved.
     *
     * @apidoc.doc Retrieve details of a ServerGroup based on it's id
     * @apidoc.param #session_key()
     * @apidoc.param #param("int", "systemGroupId")
     * @apidoc.returntype $ManagedServerGroupSerializer
     */
    @ReadOnly
    public ServerGroup getDetails(User loggedInUser, Integer systemGroupId)
        throws FaultException {

        return lookup(systemGroupId, loggedInUser);

    }

    /**
     *
     * @param loggedInUser The current user
     * @param systemGroupName Name of the system group to lookup
     * @return ServerGroup object
     * @throws FaultException A FaultException is thrown if the server group
     * corresponding to systemGroupName cannot be retrieved.
     *
     * @apidoc.doc Retrieve details of a ServerGroup based on it's name
     * @apidoc.param #session_key()
     * @apidoc.param #param("string", "systemGroupName")
     * @apidoc.returntype $ManagedServerGroupSerializer
     */
    @ReadOnly
    public ServerGroup getDetails(User loggedInUser, String systemGroupName)
        throws FaultException {
        return lookup(systemGroupName, loggedInUser);

    }

    private ServerGroup lookup(String name, User user) {
        ServerGroup sg;
        try {
            sg  = serverGroupManager.lookup(name, user);
        }
        catch (LookupException e) {
            throw new LookupServerGroupException(name);
        }
        if (sg == null) {
            throw new LookupServerGroupException(name);
        }
        return sg;
    }

    private ServerGroup lookup(Integer id, User user) {
        ServerGroup sg;
        try {
            sg  = serverGroupManager.lookup(id.longValue(), user);
        }
        catch (LookupException e) {
            throw new LookupServerGroupException(id);
        }
        if (sg == null) {
            throw new LookupServerGroupException(id);
        }
        return sg;
    }



    /**
     * Lists active systems in a server group using the default inactivity
     *      time (Currently 1 day)
     * @param loggedInUser The current user
     * @param systemGroupName the system group
     * @return List of system ids that are active
     *
     * @apidoc.doc Lists active systems within a server group
     * @apidoc.param #session_key()
     * @apidoc.param #param("string", "systemGroupName")
     * @apidoc.returntype #array_single("int", "server_id")
     */
    @ReadOnly
    public List<Long> listActiveSystemsInGroup(User loggedInUser, String systemGroupName) {
        return activeSystemsInGroup(loggedInUser, systemGroupName);
    }

    private List<Long> activeSystemsInGroup(User loggedInUser, String systemGroupName) {
        ServerGroup sg = lookup(systemGroupName, loggedInUser);
        Long threshold = SatConfigFactory.getSatConfigLongValue(SatConfigFactory.SYSTEM_CHECKIN_THRESHOLD, 1L);
        return serverGroupManager.listActiveServers(sg, threshold);
    }

    /**
     * Lists inactive systems in a server group using the specified time
     * @param loggedInUser The current user
     * @param systemGroupName the system group
     * @param daysInactive number of days a system has to not check in to be inactive
     * @return List of system ids that are active
     *
     * @apidoc.doc Lists inactive systems within a server group using a
     *          specified inactivity time.
     * @apidoc.param #session_key()
     * @apidoc.param #param("string", "systemGroupName")
     * @apidoc.param #param_desc("int", "daysInactive", "Number of days a system
     *           must not check in to be considered inactive.")
     * @apidoc.returntype #array_single("int", "server_id")
     */
    @ReadOnly
    public List<Long> listInactiveSystemsInGroup(User loggedInUser,
            String systemGroupName, Integer daysInactive) {
        ServerGroup sg = lookup(systemGroupName, loggedInUser);
        return serverGroupManager.listInactiveServers(sg,
                daysInactive.longValue());
    }

    /**
     * Lists inactive systems in a server group using the default inactivity
     *      time (Currently 1 day)
     * @param loggedInUser The current user
     * @param systemGroupName the system group
     * @return List of system ids that are active
     *
     * @apidoc.doc Lists inactive systems within a server group using the default
     *          1 day threshold.
     * @apidoc.param #session_key()
     * @apidoc.param #param("string", "systemGroupName")
     * @apidoc.returntype #array_single("int", "server_id")
     */
    @ReadOnly
    public List<Long> listInactiveSystemsInGroup(User loggedInUser,
            String systemGroupName) {
        Long threshold = SatConfigFactory.getSatConfigLongValue(SatConfigFactory.SYSTEM_CHECKIN_THRESHOLD, 1L);
        return listInactiveSystemsInGroup(loggedInUser, systemGroupName,
                threshold.intValue());
    }

    /**
     * Schedules an action to apply errata updates to active systems from a group.
     * @param loggedInUser The current user
     * @param systemGroupName the system group
     * @param errataIds List of errata IDs to apply (as Integers)
     * @return list of action ids, exception thrown otherwise
     * @since 13.0
     *
     * @apidoc.doc Schedules an action to apply errata updates to active systems
     * from a group.
     * @apidoc.param #session_key()
     * @apidoc.param #param("string", "systemGroupName")
     * @apidoc.param  #array_single("int", "errataIds")
     * @apidoc.returntype #array_single("int", "actionId")
     */
    public List<Long> scheduleApplyErrataToActive(User loggedInUser, String systemGroupName,
                                                                    List errataIds) {
        return scheduleApplyErrataToActive(loggedInUser, systemGroupName, errataIds, null);
    }

    /**
     * Schedules an action to apply errata updates to active systems from a group
     * at a specified time.
     * @param loggedInUser The current user
     * @param systemGroupName the system group
     * @param errataIds List of errata IDs to apply (as Integers)
     * @param earliestOccurrence Earliest occurrence of the errata update
     * @return list of action ids, exception thrown otherwise
     * @since 13.0
     *
     * @apidoc.doc Schedules an action to apply errata updates to active systems
     * from a group at a given date/time.
     * @apidoc.param #session_key()
     * @apidoc.param #param("string", "systemGroupName")
     * @apidoc.param #array_single("int", "errataIds")
     * @apidoc.param #param("$date", "earliestOccurrence")
     * @apidoc.returntype #array_single("int", "actionId")
     */
    public List<Long> scheduleApplyErrataToActive(User loggedInUser, String systemGroupName,
                                List<Integer> errataIds, Date earliestOccurrence) {
        return scheduleApplyErrataToActive(loggedInUser, systemGroupName, errataIds, null, false);
    }

    /**
     * Schedules an action to apply errata updates to active systems from a group
     * at a specified time.
     * @param loggedInUser The current user
     * @param systemGroupName the system group
     * @param errataIds List of errata IDs to apply (as Integers)
     * @param earliestOccurrence Earliest occurrence of the errata update
     * @param onlyRelevant If true not all erratas are applied to all systems.
     *        Systems get only the erratas relevant for them.
     * @return list of action ids, exception thrown otherwise
     * @since 24
     *
     * @apidoc.doc Schedules an action to apply errata updates to active systems
     * from a group at a given date/time.
     * @apidoc.param #session_key()
     * @apidoc.param #param("string", "systemGroupName")
     * @apidoc.param #array_single("int", "errataIds")
     * @apidoc.param #param("$date", "earliestOccurrence")
     * @apidoc.param #param("boolean", "onlyRelevant")
     * @apidoc.returntype #array_single("int", "actionId")
     */
    public List<Long> scheduleApplyErrataToActive(User loggedInUser, String systemGroupName,
                                            List<Integer> errataIds, Date earliestOccurrence, Boolean onlyRelevant) {
        try {
            List<Long> systemIds = activeSystemsInGroup(loggedInUser, systemGroupName);
            List<Long> ids = errataIds.stream()
                .map(Integer::longValue)
                .collect(toList());
            return ErrataManager.applyErrataHelper(loggedInUser, systemIds, ids,
                    earliestOccurrence, onlyRelevant);
        }
        catch (com.redhat.rhn.taskomatic.TaskomaticApiException e) {
            throw new TaskomaticApiException(e.getMessage());
        }
    }

    /**
     * List all Configuration Channels assigned to a system group
     * @param loggedInUser the user
     * @param systemGroupName the group name
     * @return list of Config Channels
     * @since 25
     *
     * @apidoc.doc List all Configuration Channels assigned to a system group
     * @apidoc.param #session_key()
     * @apidoc.param #param("string", "systemGroupName")
     * @apidoc.returntype
     * #return_array_begin()
     * $ConfigChannelSerializer
     * #array_end()
     */
    @ReadOnly
    public List<ConfigChannel> listAssignedConfigChannels(User loggedInUser, String systemGroupName) {
        ServerGroup group = Optional.ofNullable(
                ServerGroupFactory.lookupByNameAndOrg(systemGroupName, loggedInUser.getOrg())
                ).orElseThrow(() -> new LookupServerGroupException(systemGroupName));

        return StateFactory.latestConfigChannels(group).orElseGet(Collections::emptyList);
    }

    /**
     * Subscribe given config channels to a system group
     * @param loggedInUser the user
     * @param systemGroupName the group name
     * @param configChannelLabels list of config channel labels to subscribe
     * @return 1 on success
     *
     * @apidoc.doc Subscribe given config channels to a system group
     * @apidoc.param #session_key()
     * @apidoc.param #param("string", "systemGroupName")
     * @apidoc.param #array_single("string", "configChannelLabels")
     * @apidoc.returntype 1 on success, exception on failure
     */
    public int subscribeConfigChannel(User loggedInUser, String systemGroupName,
            List<String> configChannelLabels) {

        ServerGroup group = Optional.ofNullable(
                ServerGroupFactory.lookupByNameAndOrg(systemGroupName, loggedInUser.getOrg())
                ).orElseThrow(() -> new LookupServerGroupException(systemGroupName));
        ConfigurationManager manager = ConfigurationManager.getInstance();

        List<ConfigChannel> channels = configChannelLabels.stream()
            .map(l -> Optional.ofNullable(manager.lookupGlobalConfigChannel(loggedInUser, l))
                    .orElseThrow(() -> new NoSuchChannelException(l)))
            .collect(Collectors.toList());

        group.subscribeConfigChannels(channels, loggedInUser);
        return 1;
    }

    /**
     * Unsubscribe given config channels to a system group
     * @param loggedInUser the user
     * @param systemGroupName the group name
     * @param configChannelLabels list of config channel labels to subscribe
     * @return 1 on success
     *
     * @apidoc.doc Unsubscribe given config channels to a system group
     * @apidoc.param #session_key()
     * @apidoc.param #param("string", "systemGroupName")
     * @apidoc.param #array_single("string", "configChannelLabels")
     * @apidoc.returntype 1 on success, exception on failure
     */
    public int unsubscribeConfigChannel(User loggedInUser, String systemGroupName,
            List<String> configChannelLabels) {

        ServerGroup group = Optional.ofNullable(
                ServerGroupFactory.lookupByNameAndOrg(systemGroupName, loggedInUser.getOrg())
                ).orElseThrow(() -> new LookupServerGroupException(systemGroupName));
        ConfigurationManager manager = ConfigurationManager.getInstance();

        List<ConfigChannel> channels = configChannelLabels.stream()
            .map(l -> Optional.ofNullable(manager.lookupGlobalConfigChannel(loggedInUser, l))
                    .orElseThrow(() -> new NoSuchChannelException(l)))
            .collect(Collectors.toList());

        group.unsubscribeConfigChannels(channels, loggedInUser);
        return 1;
    }

    /**
     * List all Formulas assigned to a system group
     * @param loggedInUser the user
     * @param systemGroupName the group name
     * @return list of Formulas
     * @since 25
     *
     * @apidoc.doc List all Configuration Channels assigned to a system group
     * @apidoc.param #session_key()
     * @apidoc.param #param("string", "systemGroupName")
     * @apidoc.returntype
     * #return_array_begin()
     * $FormulaSerializer
     * #array_end()
     */
    @ReadOnly
    public List<Formula> listAssignedFormuals(User loggedInUser, String systemGroupName) {
        ServerGroup group = Optional.ofNullable(
                ServerGroupFactory.lookupByNameAndOrg(systemGroupName, loggedInUser.getOrg())
                ).orElseThrow(() -> new LookupServerGroupException(systemGroupName));

        List<Formula> formulas = FormulaFactory.listFormulas();
        List<String> assigned = FormulaFactory.getFormulasByGroup(group);
        return formulas.stream().filter(f -> assigned.contains(f.getName())).collect(Collectors.toList());
    }
}
