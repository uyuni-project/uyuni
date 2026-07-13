/*
 * Copyright (c) 2017--2026 SUSE LLC
 * Copyright (c) 2009--2017 Red Hat, Inc.
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
package com.redhat.rhn.domain.action;

import static java.util.stream.Collectors.toSet;

import com.redhat.rhn.common.db.datasource.CallableMode;
import com.redhat.rhn.common.db.datasource.DataResult;
import com.redhat.rhn.common.db.datasource.ModeFactory;
import com.redhat.rhn.common.db.datasource.Row;
import com.redhat.rhn.common.db.datasource.SelectMode;
import com.redhat.rhn.common.hibernate.HibernateFactory;
import com.redhat.rhn.common.hibernate.HibernateRuntimeException;
import com.redhat.rhn.domain.action.config.ConfigAction;
import com.redhat.rhn.domain.action.config.ConfigRevisionAction;
import com.redhat.rhn.domain.action.config.ConfigRevisionActionResult;
import com.redhat.rhn.domain.action.rhnpackage.PackageAction;
import com.redhat.rhn.domain.action.rhnpackage.PackageActionDetails;
import com.redhat.rhn.domain.action.salt.ApplyStatesActionDetails;
import com.redhat.rhn.domain.action.script.ScriptActionDetails;
import com.redhat.rhn.domain.action.server.ServerAction;
import com.redhat.rhn.domain.config.ConfigRevision;
import com.redhat.rhn.domain.rhnpackage.PackageEvr;
import com.redhat.rhn.domain.rhnpackage.PackageEvrFactory;
import com.redhat.rhn.domain.rhnset.RhnSet;
import com.redhat.rhn.domain.server.MinionServer;
import com.redhat.rhn.domain.server.MinionServerFactory;
import com.redhat.rhn.domain.server.Server;
import com.redhat.rhn.domain.server.ServerFactory;
import com.redhat.rhn.domain.server.ServerHistoryEvent;
import com.redhat.rhn.domain.user.User;
import com.redhat.rhn.domain.user.legacy.UserImpl;
import com.redhat.rhn.manager.rhnset.RhnSetManager;
import com.redhat.rhn.manager.system.SystemManager;
import com.redhat.rhn.taskomatic.TaskomaticApi;
import com.redhat.rhn.taskomatic.TaskomaticApiException;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.hibernate.HibernateException;
import org.hibernate.Session;

import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.Stream;

/**
 * ActionFactory - the singleton class used to fetch and store
 * com.redhat.rhn.domain.action.Action objects from the
 * database.
 */
public class ActionFactory extends HibernateFactory {

    private static ActionFactory singleton = new ActionFactory();
    private static final Logger LOG = LogManager.getLogger(ActionFactory.class);
    private static Set<String> actionArchTypes;
    private static final TaskomaticApi TASKOMATIC_API = new TaskomaticApi();

    private ActionFactory() {
        super();
        setupActionArchTypes();
    }

    private void setupActionArchTypes() {
        synchronized (this) {
            try {
                List<ActionArchType> types = getSession()
                        .createQuery("FROM com.redhat.rhn.domain.action.ActionArchType", ActionArchType.class)
                        .setCacheable(true).list();

                // don't cache the entire ActionArchType bean to avoid
                // any LazyInitializatoinException latter
                actionArchTypes = types.stream()
                        .map(type -> toActionArchTypeKey(type.getActionType().getId(), type.getActionStyle()))
                        .collect(toSet());
            }
            catch (HibernateException he) {
                LOG.error("Error loading ActionArchTypes from DB", he);
                throw new HibernateRuntimeException("Error loading ActionArchTypes from db");
            }
        }
    }

    private static String toActionArchTypeKey(Integer id, String actionStyle) {
        return id + "_" + actionStyle;
    }

    /**
     * Remove an action for an rhnset of system ids with the given label
     * @param actionId the action to remove
     * @param setLabel the set label to pull the ids from
     * @param user the user witht he set
     * @return the number of failed systems to remove an action for.
     * @throws TaskomaticApiException if there was a Taskomatic error
     * (typically: Taskomatic is down)
     */
    public static int removeActionForSystemSet(long actionId, String setLabel, User user)
        throws TaskomaticApiException {

        RhnSet set = RhnSetManager.findByLabel(user.getId(), setLabel, null);
        Set<Server> involvedMinions = MinionServerFactory
                    .lookupByIds(new ArrayList<>(set.getElementValues()))
                    .collect(toSet());
        Set<Server> involvedSystems = new HashSet<>(ServerFactory.lookupByIds(
                new ArrayList<>(set.getElementValues())));
        Action action = ActionFactory.lookupById(actionId);

        TASKOMATIC_API.deleteScheduledActions(Collections.singletonMap(action, involvedMinions));

        return involvedSystems.stream().map(Server::getId).mapToInt(sid -> {
            try {
                removeActionForSystem(actionId, sid);
                SystemManager.updateSystemOverview(sid);
                return 0;
            }
            catch (Exception e) {
                return 1;
            }
        }).sum();
    }

    /**
     * Remove pending action for system
     * @param serverId the server id
     */
    public static void cancelPendingForSystem(Long serverId) {
        SelectMode pending = ModeFactory.getMode("System_queries", "system_pending_actions");
        Map<String, Long> params = new HashMap<>();
        params.put("sid", serverId);
        DataResult<Row> dr = pending.execute(params);

        for (Row action : dr) {
            removeActionForSystem((Long) action.get("id"), serverId);
        }
        SystemManager.updateSystemOverview(serverId);
    }

    /**
     * Remove the system from the passed in Action.
     * @param actionId to process
     * @param sid to remove from Action
     */
    public static void removeActionForSystem(Number actionId, Number sid) {
        CallableMode mode = ModeFactory.getCallableMode("System_queries", "delete_action_for_system");
        Map<String, Object> params = new HashMap<>();
        params.put("action_id", actionId);
        params.put("server_id",  sid);
        mode.execute(params, new HashMap<>());
    }


    /**
     * Get the Logger for the derived class so log messages
     * show up on the correct class
     */
    @Override
    protected Logger getLogger() {
        return LOG;
    }

    /**
     * Create a ConfigRevisionAction for the given server and add it to the parent action.
     * @param revision The config revision to add to the action.
     * @param server The server for the action
     * @param parent The parent action
     */
    public static void addConfigRevisionToAction(ConfigRevision revision, Server server, ConfigAction parent) {
        ConfigRevisionAction cra = new ConfigRevisionAction();
        cra.setConfigRevision(revision);
        cra.setCreated(new Date());
        cra.setModified(new Date());
        cra.setServer(server);
        parent.addConfigRevisionAction(cra);
    }

    /**
     * Creates a ScriptActionDetails which contains an arbitrary script to be
     * run by a ScriptRunAction.
     * @param username Username of script
     * @param groupname Group script runs as
     * @param script Script contents
     * @param timeout script timeout
     * @return ScriptActionDetails containing script to be run by ScriptRunAction
     */
    public static ScriptActionDetails createScriptActionDetails(String username,
            String groupname, Long timeout, String script) {
        ScriptActionDetails sad = new ScriptActionDetails();
        sad.setUsername(username);
        sad.setGroupname(groupname);
        sad.setTimeout(timeout);
        sad.setScript(script.getBytes(StandardCharsets.UTF_8));

        return sad;
    }

    /**
     * Creates, saves and returns a new Action
     * @param actionTypeEnum the type of Action we want to create
     * @param schedulerUser the user who created this action
     * @param actionName the action name
     * @param earliestAction the earliest execution date
     * @return a saved Action
     */
    public static Action createAndSaveAction(ActionTypeEnum actionTypeEnum, User schedulerUser, String actionName,
                                             Date earliestAction) {
        /*
            We have to re-lookup the type here, because most likely a static final variable
            was passed in.  If we use this and the .reload() gets called below
            if we try to save a new action the instance of the type in the cache
            will be different from the final static variable
            sometimes hibernate is no fun
        */
        ActionType lookedUpType = lookupActionTypeByLabel(actionTypeEnum.getLabel());
        Action action = new ActionBuilder()
                .ofType(lookedUpType)
                .withSchedulerUser(schedulerUser)
                .withName(actionName)
                .withEarliest(earliestAction)
                .build();
        save(action);
        HibernateFactory.getSession().flush();
        return action;
    }

    /**
     * Lookup an Action by the id, assuming that it is in the same Org as
     * the user doing the search.  This method ensures security around the
     * Action.
     * @param user the user doing the search
     * @param id of the Action to search for
     * @return the Action found
     */
    public static Action lookupByUserAndId(User user, Long id) {
        return getSession().createQuery("FROM Action AS a where a.id = :aid AND a.org.id = :orgId", Action.class)
                .setParameter("aid", id)
                .setParameter("orgId", user.getOrg().getId())
                .uniqueResult();
    }

    /**
     * Lookup the last completed Action on a Server
     *  given the user, action type and server.
     * This is useful especially in cases where we want to
     * find the last deployed config action ...
     *
     * @param user the user doing the search (needed for permssion checking)
     * @param typeEnum the action type of the action to be queried.
     * @param server the server whose latest completed action is desired.
     * @return the Action found or null if none exists
     */
    public static Action lookupLastCompletedAction(User user, ActionTypeEnum typeEnum, Server server) {
        ActionType type = ActionFactory.lookupActionTypeByEnum(typeEnum);
        return getSession().createNativeQuery("""
                SELECT *
                FROM   rhnAction a
                WHERE  a.id = (SELECT     MAX(rA.id)
                               FROM       rhnAction rA
                               inner join rhnServerAction rsa ON rsa.action_id = rA.id
                               inner join rhnActionStatus ras ON ras.id = rsa.status
                               inner join rhnUserServerPerms usp ON usp.server_id = rsa.server_id
                               WHERE      usp.user_id = :userId
                               AND        rsa.server_id = :serverId
                               AND        ras.name IN ('Completed', 'Failed')
                               AND        rA.action_type = :actionTypeId
                              )
                """, Action.class)
                .addSynchronizedEntityClass(Action.class)
                .addSynchronizedEntityClass(Server.class)
                .addSynchronizedEntityClass(ServerAction.class)
                .addSynchronizedEntityClass(ActionStatus.class)
                .addSynchronizedEntityClass(UserImpl.class)
                .setParameter("userId", user.getId())
                .setParameter("actionTypeId", type.getId())
                .setParameter("serverId", server.getId())
                .uniqueResult();
    }


    /**
     * Lookup a Action by their id
     * @param id the id to search for
     * @return the Action found
     */
    public static Action lookupById(Long id) {
        Session session = HibernateFactory.getSession();
        return session.find(Action.class, id);
    }

    /**
     * Helper method to get a ActionType by ActionTypeEnum object
     * @param actionTypeEnumIn the ActionTypeEnum to lookup
     * @return Returns the ActionType corresponding to actionTypeEnumIn
     */
    public static ActionType lookupActionTypeByEnum(ActionTypeEnum actionTypeEnumIn) {
        if (actionTypeEnumIn == null) {
            return null;
        }
        return lookupActionTypeByLabel(actionTypeEnumIn.getLabel());
    }

    /**
     * Checks if the action type is in maintenance mode only
     * @param actionTypeEnumIn
     * @return true if the action type exists and is in maintenance mode only
     */
    public static boolean isMaintenanceModeOnly(ActionTypeEnum actionTypeEnumIn) {
        if (null == actionTypeEnumIn) {
            return false;
        }

        ActionType actionType = lookupActionTypeByEnum(actionTypeEnumIn);

        if (null != actionType) {
            return actionType.isMaintenanceModeOnly();
        }
        return false;
    }

    /**
     * Helper method to get a ActionType by label
     * @param label the Action to lookup
     * @return Returns the ActionType corresponding to label
     */
    public static ActionType lookupActionTypeByLabel(String label) {
        if (label == null) {
            return null;
        }
        return singleton.lookupObjectByParam(ActionType.class, "label", label, true);
    }

    /**
     * Helper method to get a ActionStatus by Name
     * @param name the name of the status we want to lookup.
     * @return Returns the ActionStatus corresponding to name
     */
    private static ActionStatus lookupActionStatusByName(String name) {
        return singleton.lookupObjectByParam(ActionStatus.class, "name", name, true);
    }

    /**
     * Helper method to get a ConfigRevisionActionResult by Action Config Revision Id
     * @param actionConfigRevisionId the id of the ActionConfigRevision for whom we want to lookup the result
     * @return The ConfigRevisionActionResult corresponding to the revison ID.
     */
    public static ConfigRevisionActionResult lookupConfigActionResult(Long actionConfigRevisionId) {
        return singleton.lookupObjectByParam(ConfigRevisionActionResult.class, "id", actionConfigRevisionId, true);
    }

    /**
     * Helper method to get a ConfigRevisionAction by Action Config Revision Id
     * @param id the id of the ActionConfigRevision for whom we want to lookup the result
     * @return The ConfigRevisionAction corresponding to the revison ID.
     */
    public static ConfigRevisionAction lookupConfigRevisionAction(Long id) {
        Session session = HibernateFactory.getSession();
        return session.find(ConfigRevisionAction.class, id);
    }

    /**
     * Helper method to get a {@link ApplyStatesActionDetails} by its action id.
     * @param actionId the id of the {@link ApplyStatesActionDetails}
     * @return the {@link ApplyStatesActionDetails} corresponding to the given action id.
     */
    public static ApplyStatesActionDetails lookupApplyStatesActionDetails(Long actionId) {
        Session session = HibernateFactory.getSession();
        return session.createQuery("FROM ApplyStatesActionDetails WHERE parentAction.id = :action_id",
                        ApplyStatesActionDetails.class)
                .setParameter("action_id", actionId)
                //Retrieve from cache if there
                .setCacheable(true)
                .uniqueResult();
    }

    /**
     * Insert or Update a Action.
     * @param actionIn Action to be stored in database.
     * @return action
     * @param <T> The type of the action
     */
    public static <T extends Action> T save(T actionIn) {
        /*
         * If we are trying to commit a package action, make sure
         * the packageEvr stored proc is called first so that
         * the foreign key constraint holds.
         */
        if (actionIn instanceof PackageAction action) {
            Set<PackageActionDetails> details = action.getDetails();
            for (PackageActionDetails detail : details) {
                PackageEvr evr = detail.getEvr();

                // It is possible to have a Package Action with only a package name
                if (evr != null) {
                    //commit each packageEvr
                    PackageEvr newEvr = PackageEvrFactory.lookupOrCreatePackageEvr(evr);
                    detail.setEvr(newEvr);
                }
            }
        }


        T action = singleton.saveObject(actionIn);
        if (action.getServerActions() != null) {
            action.getServerActions().stream()
                    .map(sa -> sa.getServerId())
                    .forEach(sid -> SystemManager.updateSystemOverview(sid));
        }
        return action;
    }

    /**
     * Remove a Action from the DB
     * @param actionIn Action to be removed from database.
     */
    public static void remove(Action actionIn) {
        singleton.removeObject(actionIn);
        actionIn.getServerActions().stream()
                .map(sa -> sa.getServerId())
                .forEach(sid -> SystemManager.updateSystemOverview(sid));
    }

    /**
     * Check the ActionType against the ActionArchType to see
     *
     * @param actionCheck the Action we want to see if the type matches against
     * @param actionStyle the String type we want to check
     * @return boolean if the passed in Action matches the actionStyle from
     *         the set of ActionArchTypes
     */
    public static boolean checkActionArchType(Action actionCheck, String actionStyle) {
        return actionArchTypes.contains(
                toActionArchTypeKey(actionCheck.getActionType().getId(), actionStyle));
    }

    /**
     * Recursively query the hierarchy of actions dependent on a given
     * parent. While recursive, only one query is executed per level in
     * the hierarchy, and action hierarchies tend to not be more than
     * two levels deep.
     *
     * @param parentAction Parent action.
     * @return Set of actions dependent on the given parent.
     */
    public static Stream<Action> lookupDependentActions(Action parentAction) {
        Session session = HibernateFactory.getSession();

        Set<Action> returnSet = new HashSet<>();
        List<Long> actionsAtHierarchyLevel = new LinkedList<>();
        actionsAtHierarchyLevel.add(parentAction.getId());
        do {
            List<Action> results = session
                    .createQuery("from Action a where a.prerequisite.id in (:action_ids)", Action.class)
                    .setParameterList("action_ids", actionsAtHierarchyLevel)
                    .list();
            returnSet.addAll(results);
            // Reset list of actions for the next hierarchy level:
            actionsAtHierarchyLevel = results.stream().map(Action::getId).collect(Collectors.toList());
        }
        while (!actionsAtHierarchyLevel.isEmpty());

        return returnSet.stream();
    }

    /**
     * Lookup a List of Action objects for a given Server.
     * @param user the user doing the search
     * @param serverIn you want to limit the list of Actions to
     * @return List of Action objects
     */
    public static List<Action> listActionsForServer(User user, Server serverIn) {
        return getSession().createQuery("""
                FROM  com.redhat.rhn.domain.action.Action AS a
                LEFT  JOIN FETCH a.serverActions AS sa
                WHERE a.org = :org
                AND   sa.server = :server
                """, Action.class)
                .setParameter("org", user.getOrg())
                .setParameter("server", serverIn)
                .list();
    }

    /**
     * Returns all pending actions that contain minions
     * @return list of pending minions that contain minions
     */
    public static List<Action> pendingMinionServerActions() {
        return getSession().createNativeQuery("""
                SELECT *
                FROM   rhnAction
                WHERE  id IN (SELECT     DISTINCT ac.id
                              FROM       rhnAction ac
                              INNER JOIN rhnServerAction sa on ac.id = sa.action_id
                              INNER JOIN suseMinionInfo mi on sa.server_id = mi.server_id
                              WHERE      sa.status in (0, 1))
                """, Action.class)
                .addSynchronizedEntityClass(Action.class)
                .addSynchronizedEntityClass(ServerAction.class)
                .addSynchronizedEntityClass(MinionServer.class)
                .list();
    }

    /**
     * @param aid history event id to look up for
     * @return history event
     */
    public static ServerHistoryEvent lookupHistoryEventById(Long aid) {
        Session session = HibernateFactory.getSession();
        return session.createQuery("FROM ServerHistoryEvent AS s WHERE s.id = :id", ServerHistoryEvent.class)
                .setParameter("id", aid)
                .uniqueResult();

    }

    /**
     * The constant representing the Action Status QUEUED
     */
    public static final ActionStatus STATUS_QUEUED =
            lookupActionStatusByName("Queued");

    /**
     * The constant representing the Action Status PICKED_UP
     */
    public static final ActionStatus STATUS_PICKED_UP =
            lookupActionStatusByName("Picked Up");

    /**
     * The constant representing the Action Status COMPLETED
     */
    public static final ActionStatus STATUS_COMPLETED =
            lookupActionStatusByName("Completed");

    /**
     * The constant representing the Action Status FAILED
     */
    public static final ActionStatus STATUS_FAILED =
            lookupActionStatusByName("Failed");

    /**
     * All the possible action statuses
     */
    public static final List<ActionStatus> ALL_STATUSES = List.of(STATUS_QUEUED, STATUS_PICKED_UP, STATUS_COMPLETED,
        STATUS_FAILED);

    /**
     * All the possible action statuses, but completed
     */
    public static final List<ActionStatus> ALL_STATUSES_BUT_COMPLETED = List.of(STATUS_QUEUED, STATUS_PICKED_UP,
            STATUS_FAILED);

    /**
     * All the pending action statuses
     */
    public static final List<ActionStatus> ALL_PENDING_STATUSES = List.of(STATUS_QUEUED, STATUS_PICKED_UP);

    public static final String TXN_OPERATION_INSERT = "insert";
    public static final String TXN_OPERATION_DELETE = "delete";
}

