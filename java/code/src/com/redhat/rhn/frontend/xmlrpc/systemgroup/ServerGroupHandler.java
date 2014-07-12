/**
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

import com.redhat.rhn.FaultException;
import com.redhat.rhn.common.conf.Config;
import com.redhat.rhn.common.conf.ConfigDefaults;
import com.redhat.rhn.common.db.WrappedSQLException;
import com.redhat.rhn.common.hibernate.LookupException;
import com.redhat.rhn.domain.role.RoleFactory;
import com.redhat.rhn.domain.server.ManagedServerGroup;
import com.redhat.rhn.domain.server.ServerGroup;
import com.redhat.rhn.domain.server.ServerGroupFactory;
import com.redhat.rhn.domain.user.User;
import com.redhat.rhn.domain.user.UserFactory;
import com.redhat.rhn.frontend.dto.SystemOverview;
import com.redhat.rhn.frontend.xmlrpc.BaseHandler;
import com.redhat.rhn.frontend.xmlrpc.LookupServerGroupException;
import com.redhat.rhn.frontend.xmlrpc.ServerGroupAccessChangeException;
import com.redhat.rhn.frontend.xmlrpc.ServerNotInGroupException;
import com.redhat.rhn.frontend.xmlrpc.system.XmlRpcSystemHelper;
import com.redhat.rhn.manager.errata.ErrataManager;
import com.redhat.rhn.manager.system.ServerGroupManager;
import com.redhat.rhn.manager.system.SystemManager;

import java.util.ArrayList;
import java.util.Date;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;


/**
 * ServerGroupHandler
 * @version $Rev$
 * @xmlrpc.namespace systemgroup
 * @xmlrpc.doc Provides methods to access and modify system groups.
 */
public class ServerGroupHandler extends BaseHandler {

    /**
     * Given a systemGroupName this call returns the list of users
     * who can administer the group. One has to be a SystemGroupAdmin
     * or an Org Admin to obtain this list..
     * @param loggedInUser The current user
     * @param systemGroupName the name of the system group
     * @return a list of users who can administer this system group.
     *
     * @xmlrpc.doc Returns the list of users who can administer the given group.
     * Caller must be a system group admin or an organization administrator.
     * @xmlrpc.param #session_key()
     * @xmlrpc.param  string systemGroupName
     * @xmlrpc.returntype
     *  #array()
     *      $UserSerializer
     *   #array_end()
     */
    public List listAdministrators(User loggedInUser, String systemGroupName) {
        ServerGroupManager manager = ServerGroupManager.getInstance();
        ManagedServerGroup sg = manager.lookup(systemGroupName, loggedInUser);
        return manager.listAdministrators(sg, loggedInUser);
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
     * @xmlrpc.doc Add or remove administrators to/from the given group. Satellite and
     * Organization administrators are granted access to groups within their organization
     * by default; therefore, users with those roles should not be included in the array
     * provided. Caller must be an organization administrator.
     *
     * @xmlrpc.param #session_key()
     * @xmlrpc.param #param("string", "systemGroupName")
     * @xmlrpc.param #array_single("string", "loginName - User's loginName")
     * @xmlrpc.param #param_desc("int", "add", "1 to add administrators, 0 to remove.")
     * @xmlrpc.returntype #return_int_success()
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
        ServerGroupManager manager = ServerGroupManager.getInstance();
        ManagedServerGroup group = manager.lookup(systemGroupName, loggedInUser);

        manager.associateOrDissociateAdminsByLoginName(group, loginNames,
                                                            add, loggedInUser);

        return 1;
    }


    /**
     * List the systems that are associated to the given system group.
     * @param loggedInUser The current user
     * @param systemGroupName the name of the system group
     * @return a list of systems associated to a given system group.
     *
     * @xmlrpc.doc Return a list of systems associated with this system group.
     * User must have access to this system group.

     * @xmlrpc.param #session_key()
     * @xmlrpc.param #param("string", "systemGroupName")
     * @xmlrpc.returntype
     *      #array()
     *          $ServerSerializer
     *      #array_end()
     */
    public List listSystems(User loggedInUser, String systemGroupName) {
        ServerGroupManager manager = ServerGroupManager.getInstance();
        ManagedServerGroup group = manager.lookup(systemGroupName, loggedInUser);
        return group.getServers();
    }

    /**
     * List the systems that are associated to the given system group.
     * @param loggedInUser The current user
     * @param systemGroupName the name of the system group
     * @return a list of systems associated to a given system group.
     *
     * @xmlrpc.doc Return a list of systems associated with this system group.
     * User must have access to this system group.

     * @xmlrpc.param #session_key()
     * @xmlrpc.param #param("string", "systemGroupName")
     * @xmlrpc.returntype
     *      #array()
     *          $SystemOverviewSerializer
     *      #array_end()
     */
    public List<SystemOverview>
            listSystemsMinimal(User loggedInUser, String systemGroupName) {
        ServerGroupManager manager = ServerGroupManager.getInstance();
        ManagedServerGroup group = manager.lookup(systemGroupName, loggedInUser);
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
     * @xmlrpc.doc Add/remove the given servers to a system group.
     * @xmlrpc.param #session_key()
     * @xmlrpc.param #param("string", "systemGroupName")
     * @xmlrpc.param #array_single("int", "serverId")
     * @xmlrpc.param #param_desc("boolean", "add", "True to add to the group,
     *              False to remove.")
     * @xmlrpc.returntype #return_int_success()
     */
    public int addOrRemoveSystems(User loggedInUser, String systemGroupName,
            List serverIds, Boolean add) {
        ServerGroupManager manager = ServerGroupManager.getInstance();
        ManagedServerGroup group = manager.lookup(systemGroupName, loggedInUser);

        List servers = new LinkedList();
        XmlRpcSystemHelper helper = XmlRpcSystemHelper.getInstance();
        for (Iterator itr = serverIds.iterator(); itr.hasNext();) {
            Number sid = (Number) itr.next();
            servers.add(helper.lookupServer(loggedInUser, sid));
        }
        if (add.booleanValue()) {
            manager.addServers(group, servers, loggedInUser);
        }
        else {
            try {
                manager.removeServers(group, servers, loggedInUser);
            }
            catch (WrappedSQLException e) {
                throw new ServerNotInGroupException();
            }
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
     * @xmlrpc.doc Create a new system group.
     * @xmlrpc.param #session_key()
     * @xmlrpc.param #param_desc("string", "name", "Name of the system group.")
     * @xmlrpc.param #param_desc("string", "description", "Description of the
     *                  system group.")
     * @xmlrpc.returntype $ManagedServerGroupSerializer
     */
    public ServerGroup create(User loggedInUser, String name, String description) {
        ensureSystemGroupAdmin(loggedInUser);
        ServerGroupManager manager = ServerGroupManager.getInstance();
        ManagedServerGroup sg = manager.create(loggedInUser, name, description);
        return sg;
    }

    /**
     * Deletes a given system group - given the system group name
     * @param loggedInUser The current user
     * @param systemGroupName the name of the system group
     * @return 1 for success exception  other wise.
     *
     * @xmlrpc.doc Delete a system group.
     * @xmlrpc.param #session_key()
     * @xmlrpc.param #param("string", "systemGroupName")
     * @xmlrpc.returntype #return_int_success()
     */
    public int delete(User loggedInUser, String systemGroupName) {
        ensureSystemGroupAdmin(loggedInUser);
        ServerGroupManager manager = ServerGroupManager.getInstance();
        ManagedServerGroup group = manager.lookup(systemGroupName, loggedInUser);
        manager.remove(loggedInUser, group);
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
     * @xmlrpc.doc Update an existing system group.
     * @xmlrpc.param #session_key()
     * @xmlrpc.param #param("string", "systemGroupName")
     * @xmlrpc.param #param("string", "description")
     * @xmlrpc.returntype $ManagedServerGroupSerializer
     */
    public ServerGroup update(User loggedInUser,
                                String systemGroupName, String description) {
        ServerGroupManager manager = ServerGroupManager.getInstance();
        ManagedServerGroup group = manager.lookup(systemGroupName, loggedInUser);
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
     * @xmlrpc.doc Returns a list of system groups that do not have an administrator.
     * (who is not an organization administrator, as they have implicit access to
     * system groups) Caller must be an organization administrator.
     * @xmlrpc.param #session_key()
     * @xmlrpc.returntype
     *      #array()
     *          $ManagedServerGroupSerializer
     *      #array_end()
     */
    public List listGroupsWithNoAssociatedAdmins(User loggedInUser) {
        ensureOrgAdmin(loggedInUser);
        ServerGroupManager manager = ServerGroupManager.getInstance();
        return manager.listNoAdminGroups(loggedInUser);
    }


    /**
     * List all groups accessible by the logged in user
     * @param loggedInUser The current user
     * @return a list of ServerGroup objects
     *
     * @xmlrpc.doc Retrieve a list of system groups that are accessible by the logged
     *      in user.
     * @xmlrpc.param #session_key()
     * @xmlrpc.returntype
     *      #array()
     *          $ManagedServerGroupSerializer
     *      #array_end()
     */
    public List<ManagedServerGroup> listAllGroups(User loggedInUser) {
        List<ManagedServerGroup> groups = ServerGroupFactory.listManagedGroups(
                loggedInUser.getOrg());
        List<ManagedServerGroup> toReturn = new ArrayList();
        ServerGroupManager sm = ServerGroupManager.getInstance();
        for (ManagedServerGroup group : groups) {
            if (sm.canAccess(loggedInUser, group)) {
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
     * @xmlrpc.doc Retrieve details of a ServerGroup based on it's id
     * @xmlrpc.param #session_key()
     * @xmlrpc.param #param("int", "systemGroupId")
     * @xmlrpc.returntype $ManagedServerGroupSerializer
     */
    public ServerGroup getDetails(User loggedInUser, Integer systemGroupId)
        throws FaultException {
        ServerGroup sg = lookup(systemGroupId, loggedInUser);

        return sg;

    }

    /**
     *
     * @param loggedInUser The current user
     * @param systemGroupName Name of the system group to lookup
     * @return ServerGroup object
     * @throws FaultException A FaultException is thrown if the server group
     * corresponding to systemGroupName cannot be retrieved.
     *
     * @xmlrpc.doc Retrieve details of a ServerGroup based on it's name
     * @xmlrpc.param #session_key()
     * @xmlrpc.param #param("string", "systemGroupName")
     * @xmlrpc.returntype $ManagedServerGroupSerializer
     */
    public ServerGroup getDetails(User loggedInUser, String systemGroupName)
        throws FaultException {
        ServerGroup sg = lookup(systemGroupName, loggedInUser);
        return sg;

    }

    private ServerGroup lookup(String name, User user) {
        ServerGroup sg;
        try {
            ServerGroupManager sm = ServerGroupManager.getInstance();
            sg  = sm.lookup(name, user);
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
            ServerGroupManager sm = ServerGroupManager.getInstance();
            sg  = sm.lookup(id.longValue(), user);
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
     * @xmlrpc.doc Lists active systems within a server group
     * @xmlrpc.param #session_key()
     * @xmlrpc.param #param("string", "systemGroupName")
     * @xmlrpc.returntype #array_single("int", "server_id")
     */
    public List<Long> listActiveSystemsInGroup(User loggedInUser, String systemGroupName) {
        return activeSystemsInGroup(loggedInUser, systemGroupName);
    }

    private List<Long> activeSystemsInGroup(User loggedInUser, String systemGroupName) {
        ServerGroup sg = lookup(systemGroupName, loggedInUser);
        Long threshold = new Long(Config.get().getInt(
                ConfigDefaults.SYSTEM_CHECKIN_THRESHOLD));
        return ServerGroupManager.getInstance().listActiveServers(sg, threshold);
    }

    /**
     * Lists inactive systems in a server group using the specified time
     * @param loggedInUser The current user
     * @param systemGroupName the system group
     * @param daysInactive number of days a system has to not check in to be inactive
     * @return List of system ids that are active
     *
     * @xmlrpc.doc Lists inactive systems within a server group using a
     *          specified inactivity time.
     * @xmlrpc.param #session_key()
     * @xmlrpc.param #param("string", "systemGroupName")
     * @xmlrpc.param #param_desc("int", "daysInactive", "Number of days a system
     *           must not check in to be considered inactive.")
     * @xmlrpc.returntype #array_single("int", "server_id")
     */
    public List<Long> listInactiveSystemsInGroup(User loggedInUser,
            String systemGroupName, Integer daysInactive) {
        ServerGroup sg = lookup(systemGroupName, loggedInUser);
        return ServerGroupManager.getInstance().listInactiveServers(sg,
                daysInactive.longValue());
    }

    /**
     * Lists inactive systems in a server group using the default inactivity
     *      time (Currently 1 day)
     * @param loggedInUser The current user
     * @param systemGroupName the system group
     * @return List of system ids that are active
     *
     * @xmlrpc.doc Lists inactive systems within a server group using the default
     *          1 day threshold.
     * @xmlrpc.param #session_key()
     * @xmlrpc.param #param("string", "systemGroupName")
     * @xmlrpc.returntype #array_single("int", "server_id")
     */
    public List<Long> listInactiveSystemsInGroup(User loggedInUser,
            String systemGroupName) {
        Long threshold = new Long(Config.get().getInt(
                ConfigDefaults.SYSTEM_CHECKIN_THRESHOLD));
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
     * @xmlrpc.doc Schedules an action to apply errata updates to active systems
     * from a group.
     * @xmlrpc.param #param("string", "sessionKey")
     * @xmlrpc.param #param("string", "systemGroupName")
     * @xmlrpc.param  #array_single("int", "errataId")
     * @xmlrpc.returntype #array_single("int", "actionId")
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
     * @xmlrpc.doc Schedules an action to apply errata updates to active systems
     * from a group at a given date/time.
     * @xmlrpc.param #param("string", "sessionKey")
     * @xmlrpc.param #param("string", "systemGroupName")
     * @xmlrpc.param #array_single("int", "errataId")
     * @xmlrpc.param dateTime.iso8601 earliestOccurrence
     * @xmlrpc.returntype #array_single("int", "actionId")
     */
    public List<Long> scheduleApplyErrataToActive(User loggedInUser, String systemGroupName,
                                List<Integer> errataIds, Date earliestOccurrence) {
        List<Long> systemIds = activeSystemsInGroup(loggedInUser, systemGroupName);
        return ErrataManager.applyErrataHelper(loggedInUser, systemIds, errataIds,
                earliestOccurrence);
    }
}
