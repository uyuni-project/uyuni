/*
 * Copyright (c) 2025 SUSE LLC
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
package com.redhat.rhn.manager.action;

import static com.suse.manager.utils.MinionServerUtils.isMinionServer;
import static java.util.Collections.singletonList;
import static java.util.stream.Collectors.toList;
import static java.util.stream.Collectors.toMap;
import static java.util.stream.Collectors.toSet;
import static java.util.stream.Stream.concat;

import com.redhat.rhn.common.conf.ConfigDefaults;
import com.redhat.rhn.common.db.datasource.DataResult;
import com.redhat.rhn.common.db.datasource.ModeFactory;
import com.redhat.rhn.common.db.datasource.Row;
import com.redhat.rhn.common.db.datasource.SelectMode;
import com.redhat.rhn.common.db.datasource.WriteMode;
import com.redhat.rhn.common.hibernate.LookupException;
import com.redhat.rhn.common.localization.LocalizationService;
import com.redhat.rhn.domain.action.Action;
import com.redhat.rhn.domain.action.ActionChain;
import com.redhat.rhn.domain.action.ActionChainFactory;
import com.redhat.rhn.domain.action.ActionFactory;
import com.redhat.rhn.domain.action.ActionType;
import com.redhat.rhn.domain.action.ansible.InventoryAction;
import com.redhat.rhn.domain.action.ansible.InventoryActionDetails;
import com.redhat.rhn.domain.action.config.ConfigAction;
import com.redhat.rhn.domain.action.config.ConfigUploadAction;
import com.redhat.rhn.domain.action.dup.DistUpgradeAction;
import com.redhat.rhn.domain.action.dup.DistUpgradeActionDetails;
import com.redhat.rhn.domain.action.errata.ErrataAction;
import com.redhat.rhn.domain.action.kickstart.KickstartAction;
import com.redhat.rhn.domain.action.kickstart.KickstartActionDetails;
import com.redhat.rhn.domain.action.kickstart.KickstartGuestAction;
import com.redhat.rhn.domain.action.kickstart.KickstartGuestActionDetails;
import com.redhat.rhn.domain.action.rhnpackage.PackageAction;
import com.redhat.rhn.domain.action.salt.ApplyStatesAction;
import com.redhat.rhn.domain.action.salt.ApplyStatesActionDetails;
import com.redhat.rhn.domain.action.salt.build.ImageBuildAction;
import com.redhat.rhn.domain.action.salt.build.ImageBuildActionDetails;
import com.redhat.rhn.domain.action.salt.inspect.ImageInspectAction;
import com.redhat.rhn.domain.action.salt.inspect.ImageInspectActionDetails;
import com.redhat.rhn.domain.action.scap.ScapAction;
import com.redhat.rhn.domain.action.scap.ScapActionDetails;
import com.redhat.rhn.domain.action.script.ScriptActionDetails;
import com.redhat.rhn.domain.action.script.ScriptRunAction;
import com.redhat.rhn.domain.action.server.ServerAction;
import com.redhat.rhn.domain.action.supportdata.SupportDataAction;
import com.redhat.rhn.domain.action.supportdata.SupportDataActionDetails;
import com.redhat.rhn.domain.action.supportdata.UploadGeoType;
import com.redhat.rhn.domain.common.FileList;
import com.redhat.rhn.domain.config.ConfigChannel;
import com.redhat.rhn.domain.config.ConfigFileName;
import com.redhat.rhn.domain.config.ConfigRevision;
import com.redhat.rhn.domain.config.ConfigurationFactory;
import com.redhat.rhn.domain.errata.Errata;
import com.redhat.rhn.domain.image.ImageProfile;
import com.redhat.rhn.domain.image.ImageStore;
import com.redhat.rhn.domain.kickstart.KickstartData;
import com.redhat.rhn.domain.kickstart.KickstartFactory;
import com.redhat.rhn.domain.org.Org;
import com.redhat.rhn.domain.org.OrgFactory;
import com.redhat.rhn.domain.rhnpackage.Package;
import com.redhat.rhn.domain.rhnpackage.PackageDelta;
import com.redhat.rhn.domain.rhnpackage.PackageFactory;
import com.redhat.rhn.domain.rhnset.RhnSet;
import com.redhat.rhn.domain.rhnset.RhnSetElement;
import com.redhat.rhn.domain.role.RoleFactory;
import com.redhat.rhn.domain.server.MinionServer;
import com.redhat.rhn.domain.server.MinionServerFactory;
import com.redhat.rhn.domain.server.Server;
import com.redhat.rhn.domain.server.ServerFactory;
import com.redhat.rhn.domain.user.User;
import com.redhat.rhn.frontend.dto.ActionedSystem;
import com.redhat.rhn.frontend.dto.PackageMetadata;
import com.redhat.rhn.frontend.dto.ScheduledAction;
import com.redhat.rhn.frontend.dto.SystemPendingEventDto;
import com.redhat.rhn.frontend.listview.PageControl;
import com.redhat.rhn.frontend.xmlrpc.InvalidActionTypeException;
import com.redhat.rhn.manager.BaseManager;
import com.redhat.rhn.manager.MissingCapabilityException;
import com.redhat.rhn.manager.MissingEntitlementException;
import com.redhat.rhn.manager.configuration.ConfigurationManager;
import com.redhat.rhn.manager.entitlement.EntitlementManager;
import com.redhat.rhn.manager.errata.ErrataManager;
import com.redhat.rhn.manager.kickstart.ProvisionVirtualInstanceCommand;
import com.redhat.rhn.manager.kickstart.cobbler.CobblerVirtualSystemCommand;
import com.redhat.rhn.manager.kickstart.cobbler.CobblerXMLRPCHelper;
import com.redhat.rhn.manager.system.SystemManager;
import com.redhat.rhn.taskomatic.TaskomaticApi;
import com.redhat.rhn.taskomatic.TaskomaticApiException;

import com.suse.manager.reactor.messaging.ApplyStatesEventMessage;
import com.suse.manager.utils.MinionServerUtils;
import com.suse.manager.webui.controllers.utils.ContactMethodUtil;
import com.suse.manager.webui.services.pillar.MinionPillarManager;

import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.tuple.ImmutablePair;
import org.apache.commons.lang3.tuple.Pair;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.cobbler.Profile;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Calendar;
import java.util.Collection;
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.Set;

/**
 * ActionManager - the singleton class used to provide Business Operations
 * on Actions where those operations interact with other top tier Business Objects.
 */
public class ActionManager extends BaseManager {
    private static Logger log = LogManager.getLogger(ActionManager.class);

    // List of package names that we want to make sure we don't
    // remove when doing a package sync.  Never remove running kernel for instance.
    private static final String[] PACKAGES_NOT_REMOVABLE = {"kernel"};

    private static TaskomaticApi taskomaticApi = new TaskomaticApi();

    private ActionManager() {
    }


    /**
     * Set the {@link TaskomaticApi} instance to use. Only needed for unit tests.
     *
     * @param taskomaticApiIn the {@link TaskomaticApi}
     */
    public static void setTaskomaticApi(TaskomaticApi taskomaticApiIn) {
        taskomaticApi = taskomaticApiIn;
    }

    /**
     * Mark action as failed for specified system
     *
     * @param loggedInUser The user making the request.
     * @param serverId     server id
     * @param actionId     The id of the Action to be set as failed
     * @param message      Message from user, reason of this fail
     * @return int 1 if succeed
     */
    public static int failSystemAction(User loggedInUser, Long serverId, Long actionId, String message) {
        Action action = ActionFactory.lookupByUserAndId(loggedInUser, actionId);
        Server server = SystemManager.lookupByIdAndUser(serverId, loggedInUser);
        if (action == null) {
            throw new LookupException("Could not find action " + actionId + " on system " + serverId);
        }
        ServerAction serverAction = ActionFactory.getServerActionForServerAndAction(server,
                action);
        if (serverAction == null) {
            throw new LookupException("Could not find action " + actionId + " on system " + serverId);
        }
        Date now = Calendar.getInstance().getTime();
        if (serverAction.isStatusQueued() ||
                serverAction.isStatusPickedUp()) {
            serverAction.setStatusFailed();
            serverAction.setResultMsg(message);
            serverAction.setCompletionTime(now);
        }
        else {
            throw new IllegalStateException("Action " + actionId +
                    " must be in Pending state on " + "server " + serverId);
        }
        SystemManager.updateSystemOverview(serverId);
        return 1;
    }

    /**
     * Retrieve the specified Action, assuming that the User making the request has the required permissions.
     *
     * @param user The user making the lookup request.
     * @param aid  The id of the Action to lookup.
     * @return the specified Action.
     * @throws com.redhat.rhn.common.hibernate.LookupException if the Action can't be looked up.
     */
    public static Action lookupAction(User user, Long aid) {
        Action returnedAction;
        if (aid == null) {
            return null;
        }

        returnedAction = ActionFactory.lookupByUserAndId(user, aid);

        //TODO: put this in the hibernate lookup query
        SelectMode m = ModeFactory.getMode("Action_queries", "visible_to_user");
        Map<String, Object> params = new HashMap<>();
        params.put("user_id", user.getId());
        params.put("org_id", user.getOrg().getId());
        params.put("aid", aid);
        params.put("include_orphans", user.hasRole(RoleFactory.ORG_ADMIN) ? "Y" : "N");
        if (m.execute(params).isEmpty()) {
            returnedAction = null;
        }

        if (returnedAction == null) {
            LocalizationService ls = LocalizationService.getInstance();
            throw new LookupException("Could not find action with id: " + aid,
                    ls.getMessage("lookup.jsp.title.action"),
                    ls.getMessage("lookup.jsp.reason1.action"),
                    ls.getMessage("lookup.jsp.reason2.action"));
        }

        return returnedAction;
    }


    /**
     * Lookup the last completed Action on a Server given the user, action type and server.
     * This is useful especially in cases where we want to find the last deployed config action
     *
     * @param user   the user doing the search (needed for permission checking)
     * @param type   the action type of the action to be queried.
     * @param server the server whose latest completed action is desired.
     * @return the Action found or null if none exists
     */
    public static Action lookupLastCompletedAction(User user, ActionType type, Server server) {
        // TODO: check on user visibility ??
        return ActionFactory.lookupLastCompletedAction(user, type, server);
    }

    /**
     * Deletes the action set with the given label.
     *
     * @param user  User associated with the set of actions.
     * @param label Action label to be updated.
     */
    public static void deleteActions(User user, String label) {
        WriteMode m = ModeFactory.getWriteMode("Action_queries", "delete_actions");
        Map<String, Object> params = new HashMap<>();
        params.put("user_id", user.getId());
        params.put("org_id", user.getOrg().getId());
        params.put("label", label);
        m.executeUpdate(params);
    }

    /**
     * Deletes the action set by id and type.
     *
     * @param id   Action ID
     * @param type Action Type
     */
    public static void deleteActionsByIdAndType(Long id, Integer type) {
        WriteMode m = ModeFactory.getWriteMode("Action_queries", "delete_actions_by_id_and_type");
        Map<String, Object> params = new HashMap<>();
        params.put("id", id);
        params.put("action_type", type);
        m.executeUpdate(params);
    }

    /**
     * Archives the action set with the given label.
     *
     * @param user  User associated with the set of actions.
     * @param label Action label to be updated.
     */
    public static void archiveActions(User user, String label) {
        WriteMode m = ModeFactory.getWriteMode("Action_queries", "archive_actions");
        Map<String, Object> params = new HashMap<>();
        params.put("user_id", user.getId());
        params.put("org_id", user.getOrg().getId());
        params.put("label", label);
        m.executeUpdate(params);
    }

    /**
     * Cancels the server actions associated with a given action, and if
     * required deals with associated pending kickstart actions and minion jobs.
     * Actions themselves are not deleted, only the ServerActions associated with them.
     *
     * @param user    User requesting the action be cancelled.
     * @param actions List of actions to be cancelled.
     * @throws TaskomaticApiException if there was a Taskomatic error (typically: Taskomatic is down)
     */
    public static void cancelActions(User user, Collection<Action> actions) throws TaskomaticApiException {
        cancelActions(user, actions, Collections.emptySet());
    }

    /**
     * Cancels the server actions associated with a given action, and if
     * required deals with associated pending kickstart actions and minion jobs.
     * Actions themselves are not deleted, only the ServerActions associated with them.
     *
     * @param user      User requesting the action be cancelled.
     * @param actions   List of actions to be cancelled.
     * @param serverIds If specified, only cancel ServerActions for a subset of system IDs
     * @throws TaskomaticApiException if there was a Taskomatic error (typically: Taskomatic is down)
     */
    public static void cancelActions(User user, Collection<Action> actions, Collection<Long> serverIds)
            throws TaskomaticApiException {
        if (log.isDebugEnabled()) {
            String actionIds = actions.stream().map(Action::getId).collect(toList()).toString();
            log.debug("Cancelling actions: {} for user: {}", actionIds, user.getLogin());
        }

        // Can only cancel top level actions or actions that have failed prerequisite:
        boolean hasValidPrerequisite = actions.stream()
                .map(Action::getPrerequisite)
                .filter(Objects::nonNull)
                .flatMap(p -> p.getServerActions().stream())
                .filter(sa -> serverIds.isEmpty() || serverIds.contains(sa.getServerId()))
                .anyMatch(sa -> !sa.isStatusFailed());
        if (hasValidPrerequisite) {
            StringBuilder message = new StringBuilder();
            for (Action a : actions) {
                Action p = a.getPrerequisite();
                Long lastId = a.getId();
                do {
                    if (p != null) {
                        lastId = p.getId();
                    }
                    else {
                        break;
                    }
                    p = p.getPrerequisite();
                } while (p != null);
                message.append(String.format("To cancel the whole chain, please cancel Action %d%n", lastId));
            }
            throw new ActionIsChildException(message.toString());
        }

        Set<Action> actionsToDelete = concat(
                actions.stream(),
                actions.stream().flatMap(ActionFactory::lookupDependentActions)
        ).collect(toSet());

        Set<Server> servers = new HashSet<>();
        Set<ServerAction> serverActions = new HashSet<>();

        actionsToDelete.stream()
                .flatMap(a -> a.getServerActions().stream())
                .filter(sa -> sa.isStatusQueued() || sa.isStatusPickedUp())
                // if serverIds is not specified, do not filter at all
                // if it is, only ServerActions that have server ids in the specified set can pass
                .filter(sa -> serverIds.isEmpty() || serverIds.contains(sa.getServerId()))
                .forEach(sa -> {
                    serverActions.add(sa);
                    servers.add(sa.getServer());
                });

        // fail any Kickstart sessions for these actions and servers
        KickstartFactory.failKickstartSessions(actionsToDelete, servers);

        // cancel associated schedule in Taskomatic
        Map<Action, Set<Server>> actionMap = actionsToDelete.stream()
                .map(a -> new ImmutablePair<>(
                                a,
                                a.getServerActions()
                                        .stream()
                                        .filter(sa -> sa.isStatusQueued())
                                        .map(ServerAction::getServer)
                                        .filter(server -> isMinionServer(server) && servers.contains(server))
                                        .collect(toSet())
                        )
                )
                .filter(p -> !p.getRight().isEmpty())
                // select Actions that have no minions besides those in the specified set
                // (those that have any other minion should NOT be unscheduled!)
                .filter(e -> e.getKey().getServerActions().stream()
                        .map(ServerAction::getServer)
                        .filter(MinionServerUtils::isMinionServer)
                        .allMatch(s -> e.getValue().contains(s))
                )
                .collect(toMap(
                        Pair::getLeft,
                        Pair::getRight
                ));

        if (!actionMap.isEmpty()) {
            taskomaticApi.deleteScheduledActions(actionMap);
        }

        String cancellationMessage = "Canceled by " + user.getLogin();
        serverActions.forEach(sa -> {
            // Delete ServerActions from the database only if QUEUED
            if (sa.isStatusQueued()) {
                sa.getParentAction().getServerActions().remove(sa);
                ActionFactory.delete(sa);
            }
            // Set to FAILED if the state is PICKED_UP
            else if (sa.isStatusPickedUp()) {
                failSystemAction(user, sa.getServerId(), sa.getParentAction().getId(), cancellationMessage);
            }
            SystemManager.updateSystemOverview(sa.getServerId());
        });

        // run post-actions
        actionsToDelete.forEach(Action::onCancelAction);
    }

    /**
     * Deletes the archived actions
     *
     * @param user       User requesting the delete action
     * @param actionsIds List of action ids to be deleted
     */
    public static void deleteActionsById(User user, List actionsIds) {
        List<Action> actions = new ArrayList<>();
        for (Number actionsIdIn : (Iterable<Number>) actionsIds) {
            long actionId = actionsIdIn.longValue();
            Action action = ActionManager.lookupAction(user, actionId);
            if (action != null) {
                // check, whether the actions are archived
                if (action.getArchived() == 0) {
                    throw new InvalidActionTypeException(
                            "Archive following action before deletion: " + action.getId());
                }
                actions.add(action);
            }
        }
        // now, delete them
        for (Action action : actions) {
            deleteActionsByIdAndType(action.getId(), action.getActionType().getId());
            action.getServerActions().stream()
                    .map(sa -> sa.getServerId())
                    .forEach(sid -> SystemManager.updateSystemOverview(sid));
        }
    }

    /**
     * Creates an errata action with the specified Org
     *
     * @param user   the user that is scheduling the action
     * @param org    The org that needs the errata.
     * @param errata The errata pertaining to this action
     * @return The created action
     */
    public static ErrataAction createErrataAction(User user, Org org, Errata errata) {
        //<source>Patch Update: {0} - {1}</source>
        String actionName = LocalizationService.getInstance().getMessage("action.name",
                errata.getAdvisory(), errata.getSynopsis());

        ErrataAction aa = (ErrataAction) ActionFactory.createAction(ActionFactory.TYPE_ERRATA, user, actionName, org,
                new Date());

        aa.addErrata(errata);
        return aa;
    }

    /**
     * Create a Config Upload action. This is a much different action from the
     * other config actions (doesn't involve revisions).
     *
     * @param user      The scheduler for this config action.
     * @param filenames A set of config file name ids as Longs
     * @param server    The server for which to schedule this action.
     * @param channel   The config channel to which files will be uploaded.
     * @param earliest  The soonest time that this action could be executed.
     * @return The created upload action
     * @throws TaskomaticApiException if there was a Taskomatic error (typically: Taskomatic is down)
     */
    public static Action createConfigUploadAction(User user, Set<Long> filenames, Server server,
                                                  ConfigChannel channel, Date earliest)
            throws TaskomaticApiException {
        //TODO: right now, our general rule is that upload actions will
        //always upload into the sandbox for a system. If we ever wish to
        //make that a strict business rule, here is where we can verify that
        //the given channel is the sandbox for the given server.

        ConfigUploadAction a = (ConfigUploadAction) ActionFactory.createAction(ActionFactory.TYPE_CONFIGFILES_UPLOAD,
                user, earliest);

        //put a single row into rhnActionConfigChannel
        a.addConfigChannelAndServer(channel, server);
        //put a single row into rhnServerAction
        ActionFactory.addServerToAction(server.getId(), a);

        //now put a row into rhnActionConfigFileName for each path we have.
        for (Long cfnid : filenames) {
            /*
             * We are using ConfigurationFactory to lookup the config file name
             * instead of ConfigurationManager.  If we used ConfigurationManager,
             * then we couldn't have new file names because the user wouldn't
             * have access to them yet.
             */
            ConfigFileName name = ConfigurationFactory.lookupConfigFileNameById(cfnid);
            if (name != null) {
                a.addConfigFileName(name, server);
            }
        }

        //if this is a pointless action, don't do it.
        if (a.getConfigFileNameAssociations().isEmpty()) {
            return null;
        }

        ActionFactory.save(a);
        taskomaticApi.scheduleActionExecution(a);
        return a;
    }

    /**
     * Create a Config File Diff action.
     *
     * @param user      The user scheduling a diff action.
     * @param revisions A set of revision ids as Longs
     * @param serverIds A set of server ids as Longs
     * @return The created diff action
     * @throws TaskomaticApiException if there was a Taskomatic error (typically: Taskomatic is down)
     */
    public static Action createConfigDiffAction(User user,
                                                Collection<Long> revisions,
                                                Collection<Long> serverIds) throws TaskomaticApiException {
        //diff actions are non-destructive, so there is no point to schedule them for any
        //later than now.
        return createConfigAction(user, revisions, serverIds,
                ActionFactory.TYPE_CONFIGFILES_DIFF, new Date());
    }

    /**
     * Create a Config Action.
     *
     * @param user      The user scheduling the action.
     * @param revisions A set of revision ids as Longs
     * @param servers   A set of server objects
     * @param type      The type of config action
     * @param earliest  The earliest time this action could execute.
     * @return The created config action
     * @throws TaskomaticApiException if there was a Taskomatic error (typically: Taskomatic is down)
     */
    public static Action createConfigActionForServers(User user,
                                                      Collection<Long> revisions,
                                                      Collection<Server> servers,
                                                      ActionType type, Date earliest) throws TaskomaticApiException {
        ConfigAction a = (ConfigAction) ActionFactory.createAction(type, user, earliest);

        for (Server server : servers) {
            checkConfigActionOnServer(type, server);
            ActionFactory.addServerToAction(server.getId(), a);

            //now that we made a server action, we must make config revision actions
            //which depend on the server as well.
            addConfigurationRevisionsToAction(user, revisions, a, server);
        }
        Set<ServerAction> sa = a.getServerActions();
        if ((sa == null) || (sa.isEmpty())) {
            return null;
        }
        ActionFactory.save(a);
        taskomaticApi.scheduleActionExecution(a);
        return a;
    }

    /**
     * Adds configuration revisions to a ConfigurationAction object
     *
     * @param user         the user scheduling the action
     * @param revisions    a set of revision ids as Longs
     * @param configAction the action to add revisions to
     * @param server       a server object
     */
    public static void addConfigurationRevisionsToAction(User user, Collection<Long> revisions,
                                                         ConfigAction configAction, Server server) {
        for (Long revId : revisions) {
            try {
                ConfigRevision rev = ConfigurationManager.getInstance()
                        .lookupConfigRevision(user, revId);
                ActionFactory.addConfigRevisionToAction(rev, server, configAction);
            }
            catch (LookupException e) {
                log.error("Failed lookup for revision {}by user {}", revId, user.getId());
            }
        }
    }

    /**
     * Checks that a server can be the target of a ConfigAction
     *
     * @param type   type of ConfigAction
     * @param server a server object
     * @throws MissingCapabilityException if server does not have needed capabilities
     */
    public static void checkConfigActionOnServer(ActionType type, Server server) {
        if (ActionFactory.TYPE_CONFIGFILES_DEPLOY.equals(type) &&
                !SystemManager.clientCapable(server.getId(), SystemManager.CAP_CONFIGFILES_DEPLOY)) {
            throw new MissingCapabilityException(SystemManager.CAP_CONFIGFILES_DEPLOY, server);
        }
    }

    /**
     * Create a Config Action.
     *
     * @param user      The user scheduling the action.
     * @param revisions A set of revision ids as Longs
     * @param serverIds A set of server ids as Longs
     * @param type      The type of config action
     * @param earliest  The earliest time this action could execute.
     * @return The created config action
     * @throws TaskomaticApiException if there was a Taskomatic error (typically: Taskomatic is down)
     */
    public static Action createConfigAction(User user, Collection<Long> revisions,
                                            Collection<Long> serverIds, ActionType type, Date earliest)
            throws TaskomaticApiException {

        List<Server> servers = SystemManager.hydrateServerFromIds(serverIds, user);
        return createConfigActionForServers(user, revisions, servers, type, earliest);
    }

    /**
     * Reschedule the action so it can be attempted again.
     *
     * @param action Action to reschedule
     * @throws TaskomaticApiException if there was a Taskomatic error (typically: Taskomatic is down)
     */
    public static void rescheduleAction(Action action) throws TaskomaticApiException {
        rescheduleAction(action, false);
    }

    /**
     * Reschedule the action so it can be attempted again.
     *
     * @param action     Action to reschedule
     * @param onlyFailed reschedule only the ServerActions w/failed status
     * @throws TaskomaticApiException if there was a Taskomatic error (typically: Taskomatic is down)
     */
    public static void rescheduleAction(Action action, boolean onlyFailed)
            throws TaskomaticApiException {
        //5 was hardcoded from perl :/
        if (onlyFailed) {
            ActionFactory.rescheduleFailedServerActions(action, 5L);
        }
        else {
            ActionFactory.rescheduleAllServerActions(action, 5L);
        }
        taskomaticApi.scheduleActionExecution(action);
    }

    /**
     * Retrieve the list of unarchived scheduled actions for the current user
     *
     * @param user The user in question
     * @param pc   The details of which results to return
     * @param age  how many days old a system can be in order to count as a "recently" scheduled action
     * @return A list containing the pending actions for the user
     */
    public static DataResult<ScheduledAction> recentlyScheduledActions(User user, PageControl pc, long age) {
        SelectMode m = ModeFactory.getMode("Action_queries",
                "recently_scheduled_action_list");
        Map<String, Object> params = new HashMap<>();
        params.put("user_id", user.getId());
        params.put("org_id", user.getOrg().getId());
        params.put("age", age);

        if (pc != null) {
            return makeDataResult(params, params, pc, m);
        }

        DataResult<ScheduledAction> dr = m.execute(params);
        dr.setTotalSize(dr.size());
        return dr;
    }

    /**
     * Retrieve the list of all actions for a particular user.
     * This includes pending, completed, failed and archived actions.
     *
     * @param user The user in question
     * @param pc   The details of which results to return
     * @return A list containing the all actions for the user
     */
    public static DataResult<ScheduledAction> allActions(User user, PageControl pc) {
        return getActions(user, pc, "all_action_list");
    }

    /**
     * Retrieve the list of pending actions for a particular user
     *
     * @param user The user in question
     * @param pc   The details of which results to return
     * @return A list containing the pending actions for the user
     */
    public static DataResult<ScheduledAction> pendingActions(User user, PageControl pc) {
        return getActions(user, pc, "pending_action_list");
    }

    /**
     * Retrieve the list of pending actions for a particular user within the given set.
     *
     * @param user     The user in question
     * @param pc       The details of which results to return
     * @param setLabel Label of an RhnSet of actions IDs to limit the results to.
     * @return A list containing the pending actions for the user.
     */
    public static DataResult<ScheduledAction> pendingActionsInSet(User user, PageControl pc, String setLabel) {
        return getActions(user, pc, "pending_actions_in_set", setLabel);
    }

    /**
     * Retrieve the list of pending actions for a particular user within the given set.
     *
     * @param user     The user in question
     * @param pc       The details of which results to return
     * @param setLabel Label of an RhnSet of actions IDs to limit the results to.
     * @param sid      Server id
     * @return A list containing the pending actions for the user.
     */
    public static DataResult<SystemPendingEventDto> pendingActionsToDeleteInSet(User user, PageControl pc,
                                                                                String setLabel, Long sid) {
        SelectMode m = ModeFactory.getMode("System_queries", "pending_actions_to_delete_in_set");
        Map<String, Object> params = new HashMap<>();
        params.put("sid", sid);
        params.put("user_id", user.getId());
        params.put("set_label", setLabel);
        if (pc != null) {
            return makeDataResult(params, params, pc, m);
        }
        DataResult<SystemPendingEventDto> dr = m.execute(params);
        dr.setTotalSize(dr.size());
        dr.setElaborationParams(params);
        return dr;
    }

    /**
     * Retrieve the list of failed actions for a particular user
     *
     * @param user The user in question
     * @param pc   The details of which results to return
     * @return A list containing the pending actions for the user
     */
    public static DataResult<ScheduledAction> failedActions(User user, PageControl pc) {
        return getActions(user, pc, "failed_action_list");
    }

    /**
     * Retrieve the list of completed actions for a particular user
     *
     * @param user The user in question
     * @param pc   The details of which results to return
     * @return A list containing the pending actions for the user
     */
    public static DataResult<ScheduledAction> completedActions(User user, PageControl pc) {
        return getActions(user, pc, "completed_action_list");
    }

    /**
     * Retrieve the list of all completed actions for a particular user
     *
     * @param user The user in question
     * @param pc   The details of which results to return
     * @return A list containing the pending actions for the user
     */
    public static DataResult<ScheduledAction> allCompletedActions(User user, PageControl pc) {
        return getActions(user, pc, "completed_action_list", null, true);
    }

    /**
     * Retrieve the list of archived actions for a particular user
     *
     * @param user The user in question
     * @param pc   The details of which results to return
     * @return A list containing the pending actions for the user
     */
    public static DataResult<ScheduledAction> archivedActions(User user, PageControl pc) {
        return getActions(user, pc, "archived_action_list");
    }

    /**
     * Retrieve the list of all archived actions for a particular user
     *
     * @param user The user in question
     * @param pc   The details of which results to return
     * @return A list containing the pending actions for the user
     */
    public static DataResult<ScheduledAction> allArchivedActions(User user, PageControl pc) {
        return getActions(user, pc, "archived_action_list", null, true);
    }

    /**
     * Helper method that does the work of getting a specific DataResult for scheduled actions.
     *
     * @param user    The user in question
     * @param pc      The details of which results to return
     * @param mode    The mode
     * @param noLimit Return all actions without limiting the results
     * @return Returns a list containing the actions for the user
     */
    private static DataResult<ScheduledAction> getActions(User user, PageControl pc, String mode,
                                                          String setLabel, boolean noLimit) {
        SelectMode m = ModeFactory.getMode("Action_queries", mode);
        Map<String, Object> params = new HashMap<>();
        params.put("user_id", user.getId());
        params.put("org_id", user.getOrg().getId());
        params.put("include_orphans", user.hasRole(RoleFactory.ORG_ADMIN) ? "Y" : "N");
        if (setLabel != null) {
            params.put("set_label", setLabel);
        }
        if (pc != null) {
            return makeDataResult(params, params, pc, m);
        }
        if (!noLimit) {
            int limit = ConfigDefaults.get().getActionsDisplayLimit();
            if (limit > 0) {
                m.setMaxRows(limit);
            }
        }
        DataResult<ScheduledAction> dr = m.execute(params);
        dr.setTotalSize(dr.size());
        dr.setElaborationParams(params);
        return dr;
    }

    /**
     * Helper method that does the work of getting a specific DataResult for scheduled actions.
     *
     * @param user The user in question
     * @param pc   The details of which results to return
     * @param mode The mode
     * @return Returns a list containing the actions for the user
     */
    private static DataResult<ScheduledAction> getActions(User user, PageControl pc, String mode, String setLabel) {
        return getActions(user, pc, mode, setLabel, false);
    }

    /**
     * Helper method that does the work of getting a specific DataResult for scheduled actions.
     *
     * @param user The user in question
     * @param pc   The details of which results to return
     * @param mode The mode
     * @return Returns a list containing the actions for the user
     */
    private static DataResult<ScheduledAction> getActions(User user, PageControl pc, String mode) {
        return getActions(user, pc, mode, null);
    }

    /**
     * Returns the list of packages associated with a specific action.
     *
     * @param aid The action id for the action in question
     * @param pc  The details of which results to return
     * @return Return a list containing the packages for the action.
     */
    public static DataResult<Row> getPackageList(Long aid, PageControl pc) {
        SelectMode m = ModeFactory.getMode("Package_queries", "packages_associated_with_action");
        Map<String, Object> params = new HashMap<>();
        params.put("aid", aid);
        if (pc != null) {
            return makeDataResult(params, params, pc, m);
        }
        DataResult<Row> dr = m.execute(params);
        dr.setTotalSize(dr.size());
        return dr;
    }

    /**
     * Returns the list of errata associated with a specific action.
     *
     * @param aid The action id for the action in question
     * @return Return a list containing the errata for the action.
     */
    public static DataResult<Row> getErrataList(Long aid) {
        SelectMode m = ModeFactory.getMode("Errata_queries", "errata_associated_with_action");

        Map<String, Object> params = new HashMap<>();
        params.put("aid", aid);

        DataResult<Row> dr = m.execute(params);
        dr.setTotalSize(dr.size());
        return dr;
    }

    /**
     * Returns the list of details associated with a config file upload action.
     *
     * @param aid The action id for the action in question
     * @return Return a list containing the errata for the action.
     */
    public static DataResult<Row> getConfigFileUploadList(Long aid) {
        SelectMode m = ModeFactory.getMode("config_queries", "upload_action_status");

        Map<String, Object> params = new HashMap<>();
        params.put("aid", aid);

        DataResult<Row> dr = m.execute(params);
        dr.setTotalSize(dr.size());
        return dr;
    }

    /**
     * Returns the list of details associated with a config file deploy action.
     *
     * @param aid The action id for the action in question
     * @return Return a list containing the details for the action.
     */
    public static DataResult<Row> getConfigFileDeployList(Long aid) {
        SelectMode m = ModeFactory.getMode("config_queries", "config_action_revisions");

        Map<String, Object> params = new HashMap<>();
        params.put("aid", aid);

        DataResult<Row> dr = m.execute(params);
        dr.setTotalSize(dr.size());
        return dr;
    }

    /**
     * Returns the list of details associated with a config file diff action.
     *
     * @param aid The action id for the action in question
     * @return Return a list containing the details for the action.
     */
    public static DataResult<Row> getConfigFileDiffList(Long aid) {
        SelectMode m = ModeFactory.getMode("config_queries", "diff_action_revisions");

        Map<String, Object> params = new HashMap<>();
        params.put("aid", aid);

        DataResult<Row> dr = m.execute(params);
        dr.setTotalSize(dr.size());
        return dr;
    }

    /**
     * Retrieves the systems that have completed a given action
     *
     * @param user   The user in question.
     * @param action The Action.
     * @param pc     The PageControl.
     * @param mode   The DataSource mode to run
     * @return Returns list containing the completed systems.
     */
    private static DataResult<ActionedSystem> getActionSystems(User user, Action action, PageControl pc, String mode) {

        SelectMode m = ModeFactory.getMode("System_queries", mode);
        Map<String, Object> params = new HashMap<>();
        params.put("org_id", user.getOrg().getId());
        params.put("aid", action.getId());
        params.put("user_id", user.getId());
        if (pc != null) {
            return makeDataResult(params, params, pc, m);
        }
        DataResult<ActionedSystem> dr = m.execute(params);
        dr.setTotalSize(dr.size());
        return dr;
    }

    /**
     * Retrieves the systems that have completed a given action
     *
     * @param user   The user in question.
     * @param action The Action.
     * @param pc     The PageControl.
     * @return Returns list containing the completed systems.
     */
    public static DataResult<ActionedSystem> completedSystems(User user, Action action, PageControl pc) {
        return getActionSystems(user, action, pc, "systems_completed_action");
    }

    /**
     * Retrieves the systems that are in the process of completing a given action
     *
     * @param user   The user in question.
     * @param action The Action.
     * @param pc     The PageControl.
     * @return Returns list containing the completed systems.
     */
    public static DataResult<ActionedSystem> inProgressSystems(User user, Action action, PageControl pc) {
        return getActionSystems(user, action, pc, "systems_in_progress_action");
    }

    /**
     * Retrieves the systems that failed completing a given action
     *
     * @param user   The user in question.
     * @param action The Action.
     * @param pc     The PageControl.
     * @return Returns list containing the completed systems.
     */
    public static DataResult<ActionedSystem> failedSystems(User user, Action action, PageControl pc) {
        return getActionSystems(user, action, pc, "systems_failed_action");
    }

    /**
     * Schedules a package list refresh action for the given server.
     *
     * @param scheduler User scheduling the action.
     * @param server    Server for which the action affects.
     * @param earliest  The earliest time this action should be run.
     * @return The scheduled PackageAction
     * @throws TaskomaticApiException if there was a Taskomatic error (typically: Taskomatic is down)
     * @throws MissingEntitlementException if the server is not entitled
     */
    public static PackageAction schedulePackageRefresh(User scheduler, Server server, Date earliest)
            throws TaskomaticApiException {
        checkSaltOrManagementEntitlement(server.getId());

        Action action = schedulePackageAction(scheduler,
                null, ActionFactory.TYPE_PACKAGES_REFRESH_LIST, earliest, server);

        ActionFactory.save(action);
        return (PackageAction) action;
    }

    /**
     * Schedule a package list refresh without a user.
     *
     * @param user     the organization the server belongs to
     * @param server   the server
     * @param earliest The earliest time this action should be run.
     * @return the scheduled PackageRefreshListAction
     * @throws TaskomaticApiException if there was a Taskomatic error (typically: Taskomatic is down)
     */
    public static PackageAction schedulePackageRefresh(Optional<User> user, Server server, Date earliest)
            throws TaskomaticApiException {
        checkSaltOrManagementEntitlement(server.getId());

        Action action = ActionFactory.createAction(ActionFactory.TYPE_PACKAGES_REFRESH_LIST,
                user.orElse(null), server.getOrg(), earliest);

        ActionFactory.createAddServerAction(server, action);

        ActionFactory.save(action);
        taskomaticApi.scheduleActionExecution(action);
        return (PackageAction) action;
    }

    /**
     * Schedules a package runTransaction action.
     *
     * @param scheduler User scheduling the action.
     * @param server    Server for which the action affects.
     * @param pkgs      List of PackageMetadata's to be run.
     * @param earliest  The earliest time this action should be run.
     * @return The scheduled PackageAction
     * @throws TaskomaticApiException if there was a Taskomatic error (typically: Taskomatic is down)
     */
    public static PackageAction schedulePackageRunTransaction(User scheduler, Server server,
                                                              List<PackageMetadata> pkgs, Date earliest)
            throws TaskomaticApiException {

        if (pkgs == null || pkgs.isEmpty()) {
            return null;
        }

        Action action = ActionFactory.createAction(ActionFactory.TYPE_PACKAGES_RUNTRANSACTION, scheduler, new Date());
        ActionFactory.createAddServerAction(server, action);
        action.setEarliestAction(earliest);

        if (!SystemManager.clientCapable(server.getId(),
                "packages.runTransaction")) {
            // We need to schedule a hardware refresh to pull
            // in the packages.runTransaction capability
            Action hwrefresh =
                    scheduleHardwareRefreshAction(scheduler, server, earliest);
            ActionFactory.save(hwrefresh);
            taskomaticApi.scheduleActionExecution(hwrefresh);
            action.setPrerequisite(hwrefresh);
        }

        ActionFactory.save(action);
        taskomaticApi.scheduleActionExecution(action);

        PackageDelta pd = new PackageDelta();
        pd.setLabel("delta-" + System.currentTimeMillis());
        PackageFactory.save(pd);

        // this is SOOOO WRONG, we need to get rid of DataSource
        WriteMode m = ModeFactory.getWriteMode("Action_queries",
                "insert_package_delta_element");
        for (PackageMetadata pm : pkgs) {
            Map<String, Object> params = new HashMap<>();
            params.put("delta_id", pd.getId());
            if (pm.getComparisonAsInt() == PackageMetadata.KEY_THIS_ONLY) {
                handleKeyThisOnly(params, pm, m);
            }
            else if (pm.getComparisonAsInt() == PackageMetadata.KEY_OTHER_ONLY) {
                handleKeyOtherOnly(params, pm, m);
            }
            else if (pm.getComparisonAsInt() == PackageMetadata.KEY_THIS_NEWER ||
                    pm.getComparisonAsInt() == PackageMetadata.KEY_OTHER_NEWER) {
                handleKeyThisOrOtherNewer(params, pm, m);
            }
        }

        // this is SOOOO WRONG, we need to get rid of DataSource
        m = ModeFactory.getWriteMode("Action_queries",
                "insert_action_package_delta");
        Map<String, Object> params = new HashMap<>();
        params.put("action_id", action.getId());
        params.put("delta_id", pd.getId());
        m.executeUpdate(params);

        return (PackageAction) action;
    }

    private static void handleKeyThisOnly(Map<String, Object> params, PackageMetadata pm, WriteMode m) {
        log.debug("compare returned [KEY_THIS_ONLY]; deleting package from system");

        params.put("operation", ActionFactory.TXN_OPERATION_DELETE);
        params.put("n", pm.getName());
        params.put("v", pm.getSystem().getVersion());
        params.put("r", pm.getSystem().getRelease());
        String epoch = pm.getSystem().getEpoch();
        params.put("e", StringUtils.isEmpty(epoch) ? null : epoch);
        params.put("a", pm.getSystem().getArch() != null ? pm.getSystem().getArch() : "");
        m.executeUpdate(params);
    }

    private static void handleKeyOtherOnly(Map<String, Object> params, PackageMetadata pm, WriteMode m) {

        if (log.isDebugEnabled()) {
            log.debug("compare returned [KEY_OTHER_ONLY]; installing package to system: {}-{}",
                    pm.getName(), pm.getOtherEvr());
        }

        params.put("operation", ActionFactory.TXN_OPERATION_INSERT);
        params.put("n", pm.getName());
        params.put("v", pm.getOther().getVersion());
        params.put("r", pm.getOther().getRelease());
        String epoch = pm.getOther().getEpoch();
        params.put("e", StringUtils.isEmpty(epoch) ? null : epoch);
        params.put("a", pm.getOther().getArch() != null ? pm.getOther().getArch() : "");
        m.executeUpdate(params);
    }

    private static void handleKeyThisOrOtherNewer(Map<String, Object> params, PackageMetadata pm, WriteMode m) {

        if (log.isDebugEnabled()) {
            log.debug("compare returned [KEY_THIS_NEWER OR KEY_OTHER_NEWER]; deleting package [{}-{}] " +
                            "from system installing package [{}-{}] to system",
                    pm.getName(), pm.getSystemEvr(), pm.getName(), pm.getOther().getEvr());
        }

        String epoch;
        if (isPackageRemovable(pm.getName())) {
            params.put("operation", ActionFactory.TXN_OPERATION_DELETE);
            params.put("n", pm.getName());
            params.put("v", pm.getSystem().getVersion());
            params.put("r", pm.getSystem().getRelease());
            epoch = pm.getSystem().getEpoch();
            params.put("e", StringUtils.isEmpty(epoch) ? null : epoch);
            params.put("a", pm.getSystem().getArch() != null ? pm.getOther().getArch() : "");
            m.executeUpdate(params);
        }

        params.put("operation", ActionFactory.TXN_OPERATION_INSERT);
        params.put("n", pm.getName());
        params.put("v", pm.getOther().getVersion());
        params.put("r", pm.getOther().getRelease());
        epoch = pm.getOther().getEpoch();
        params.put("e", StringUtils.isEmpty(epoch) ? null : epoch);
        params.put("a", pm.getOther().getArch() != null ? pm.getOther().getArch() : "");
        m.executeUpdate(params);
    }

    // Check if we want to delete the old package when installing  a
    // new rev of one.
    private static boolean isPackageRemovable(String name) {
        for (String sIn : PACKAGES_NOT_REMOVABLE) {
            log.debug("Checking: {} for: {}", name, sIn);
            if (name.equals(sIn)) {
                return false;
            }
        }
        return true;
    }

    /**
     * Schedules one or more package lock actions for the given server.
     *
     * @param scheduler the scheduler
     * @param servers   the servers
     * @param packages  set of packages
     * @param earliest  earliest occurrence of this action
     * @return Currently scheduled PackageAction
     * @throws TaskomaticApiException if there was a Taskomatic error (typically: Taskomatic is down)
     */
    public static Action schedulePackageLock(User scheduler, Set<Package> packages, Date earliest, Server... servers)
            throws TaskomaticApiException {
        List<Map<String, Long>> packagesList = new ArrayList<>();
        for (Package pkg : packages) {
            Map<String, Long> pkgMeta = new HashMap<>();
            pkgMeta.put("name_id", pkg.getPackageName().getId());
            pkgMeta.put("evr_id", pkg.getPackageEvr().getId());
            pkgMeta.put("arch_id", pkg.getPackageArch().getId());
            packagesList.add(pkgMeta);
        }

        return ActionManager.schedulePackageAction(
                scheduler,
                packagesList,
                ActionFactory.TYPE_PACKAGES_LOCK,
                earliest,
                servers
        );
    }

    /**
     * Schedules a script action for the given servers
     *
     * @param scheduler User scheduling the action.
     * @param sids      Servers for which the action affects.
     * @param script    The set of packages to be removed.
     * @param name      Name of Script action.
     * @param earliest  Earliest occurrence of the script.
     * @return Currently scheduled ScriptRunAction
     * @throws TaskomaticApiException if there was a Taskomatic error (typically: Taskomatic is down)
     * @throws MissingCapabilityException if any server in the list is missing script.run: schedule fails
     */
    public static ScriptRunAction scheduleScriptRun(User scheduler, List<Long> sids,
                                                    String name, ScriptActionDetails script, Date earliest)
            throws TaskomaticApiException {

        checkScriptingOnServers(sids);

        Set<Long> sidSet = new HashSet<>();
        sidSet.addAll(sids);

        ScriptRunAction sra = (ScriptRunAction) ActionFactory.createAndSaveAction(ActionFactory.TYPE_SCRIPT_RUN,
                scheduler, name, earliest);
        ActionFactory.scheduleForExecution(sra, sidSet);

        sra.setScriptActionDetails(script);
        ActionFactory.save(sra);
        taskomaticApi.scheduleActionExecution(sra);
        return sra;
    }

    /**
     * Checks that ScriptRunActions can be run on the servers with specified
     * IDs.
     *
     * @param sids servers' ids
     * @throws MissingCapabilityException  if scripts cannot be run
     * @throws MissingEntitlementException if the server is not entitled
     */
    public static void checkScriptingOnServers(List<Long> sids)
            throws MissingCapabilityException {
        for (Long sid : sids) {
            if (!SystemManager.clientCapable(sid, "script.run")) {
                throw new MissingCapabilityException("script.run", sid);
            }

            checkSaltOrManagementEntitlement(sid);
        }
    }

    /**
     * Schedule a KickstartAction against a system
     *
     * @param ksdata         KickstartData to associate with this Action
     * @param scheduler      User scheduling the action.
     * @param srvr           Server for which the action affects.
     * @param earliestAction Date run the Action
     * @param appendString   extra options to add to the action.
     * @param kickstartHost  host that serves up the kickstart file.
     * @return Currently scheduled KickstartAction
     */
    public static KickstartAction scheduleKickstartAction(KickstartData ksdata, User scheduler, Server srvr,
                                                          Date earliestAction, String appendString,
                                                          String kickstartHost) {
        if (log.isDebugEnabled()) {
            log.debug("scheduleKickstartAction(KickstartData ksdata={}, User scheduler={}, Server srvr={}, " +
                            "Date earliestAction={}, String appendString={}, String kickstartHost={}) - start",
                    ksdata, scheduler, srvr, earliestAction, appendString, kickstartHost);
        }

        return scheduleKickstartAction(ksdata.getPreserveFileLists(), scheduler, srvr,
                earliestAction, appendString, kickstartHost);

    }

    /**
     * Schedule a KickstartAction against a system
     *
     * @param fileList       file preservation lists to be included in the system records.
     * @param scheduler      User scheduling the action.
     * @param srvr           Server for which the action affects.
     * @param earliestAction Date run the Action
     * @param appendString   extra options to add to the action.
     * @param kickstartHost  host that serves up the kickstart file.
     * @return Currently scheduled KickstartAction
     */
    public static KickstartAction scheduleKickstartAction(Set<FileList> fileList, User scheduler, Server srvr,
                                                          Date earliestAction, String appendString,
                                                          String kickstartHost) {
        if (log.isDebugEnabled()) {
            log.debug("scheduleKickstartAction(, User scheduler={}, Server srvr={}, Date earliestAction={}, " +
                            "String appendString={}, String kickstartHost={}) - start",
                    scheduler, srvr, earliestAction, appendString, kickstartHost);
        }

        KickstartAction ksaction = (KickstartAction) ActionFactory.createAction(ActionFactory.TYPE_KICKSTART_INITIATE,
                scheduler,
                earliestAction);
        ActionFactory.createAddServerAction(srvr, ksaction);

        KickstartActionDetails kad = new KickstartActionDetails();
        kad.setAppendString(appendString);
        kad.setParentAction(ksaction);
        kad.setKickstartHost(kickstartHost);
        ksaction.setKickstartActionDetails(kad);
        if (fileList != null) {
            for (FileList list : fileList) {
                kad.addFileList(list);
            }
        }

        return ksaction;
    }


    /**
     * Schedule a KickstartGuestAction against a system
     *
     * @param pcmd        most information needed to create this action
     * @param ksSessionId Kickstart Session ID to associate with this action
     * @return Currently scheduled KickstartAction
     */
    public static KickstartGuestAction scheduleKickstartGuestAction(ProvisionVirtualInstanceCommand pcmd,
                                                                    Long ksSessionId) {

        KickstartGuestAction ksAction = (KickstartGuestAction)
                ActionFactory.createAction(ActionFactory.TYPE_KICKSTART_INITIATE_GUEST,
                        pcmd.getUser(),
                        pcmd.getScheduleDate());
        ActionFactory.createAddServerAction(pcmd.getHostServer(), ksAction);

        KickstartGuestActionDetails kad = new KickstartGuestActionDetails();
        kad.setAppendString(pcmd.getExtraOptions());
        kad.setParentAction(ksAction);

        kad.setDiskGb(pcmd.getLocalStorageSize());
        kad.setMemMb(pcmd.getMemoryAllocation());
        kad.setDiskPath(pcmd.getFilePath());
        kad.setVcpus(pcmd.getVirtualCpus());
        kad.setGuestName(pcmd.getGuestName());
        kad.setMacAddress(pcmd.getMacAddress());
        kad.setKickstartSessionId(ksSessionId);

        Profile cProfile = Profile.lookupById(CobblerXMLRPCHelper.getConnection(
                pcmd.getUser()), pcmd.getKsdata().getCobblerId());
        if (pcmd.getVirtBridge() == null) {
            kad.setVirtBridge(cProfile.getVirtBridge().orElse(""));
        }
        else {
            kad.setVirtBridge(pcmd.getVirtBridge());
        }

        CobblerVirtualSystemCommand vcmd = new CobblerVirtualSystemCommand(pcmd.getUser(),
                pcmd.getServer(), cProfile.getName(), pcmd.getGuestName(),
                pcmd.getKsdata());
        kad.setCobblerSystemName(vcmd.getCobblerSystemRecordName());

        String hostname = pcmd.getKickstartServerName();
        if (pcmd.getProxyHost() != null) {
            hostname = pcmd.getProxyHost();
        }
        kad.setKickstartHost(hostname);
        ksAction.setKickstartGuestActionDetails(kad);
        return ksAction;
    }

    /**
     * Schedule a scheduleRebootAction against a system
     *
     * @param scheduler      User scheduling the action.
     * @param srvr           Server for which the action affects.
     * @param earliestAction Date run the Action
     * @return Currently scheduled KickstartAction
     */
    public static Action scheduleRebootAction(User scheduler, Server srvr, Date earliestAction) {
        Action action = ActionFactory.createAction(ActionFactory.TYPE_REBOOT, scheduler, earliestAction);
        ActionFactory.createAddServerAction(srvr, action);
        return action;
    }

    /**
     * Schedule a scheduleHardwareRefreshAction against a system
     *
     * @param scheduler      User scheduling the action.
     * @param srvr           Server for which the action affects.
     * @param earliestAction Date run the Action
     * @return Currently scheduled KickstartAction
     * @throws MissingCapabilityException if scripts cannot be run
     */
    public static Action scheduleHardwareRefreshAction(User scheduler, Server srvr, Date earliestAction) {
        checkSaltOrManagementEntitlement(srvr.getId());
        Action action = ActionFactory.createAction(ActionFactory.TYPE_HARDWARE_REFRESH_LIST, scheduler, earliestAction);
        ActionFactory.createAddServerAction(srvr, action);
        return action;
    }

    /**
     * Schedule a scheduleHardwareRefreshAction against a system or systems
     *
     * @param scheduler      User scheduling the action.
     * @param earliestAction Date run the Action
     * @param serverIds      server ids meant for the action
     * @return Currently scheduled KickstartAction
     * @throws MissingCapabilityException if scripts cannot be run
     */
    public static Action scheduleHardwareRefreshAction(User scheduler, Date earliestAction, Set<Long> serverIds) {
        for (Long sid : serverIds) {
            Server s = SystemManager.lookupByIdAndUser(sid, scheduler);
            checkSaltOrManagementEntitlement(sid);
        }

        Action action = ActionFactory.createAndSaveAction(ActionFactory.TYPE_HARDWARE_REFRESH_LIST, scheduler,
                ActionFactory.TYPE_HARDWARE_REFRESH_LIST.getName(), earliestAction);
        ActionFactory.scheduleForExecution(action, serverIds);
        return action;
    }

    /**
     * Schedule a HardwareRefreshAction without a user.
     *
     * @param schedulerOrg   the org scheduling the action.
     * @param srvr           Server for which the action affects.
     * @param earliestAction Date run the Action
     * @return Currently scheduled HardwareRefreshAction
     * @throws TaskomaticApiException if there was a Taskomatic error (typically: Taskomatic is down)
     * @throws MissingCapabilityException if scripts cannot be run
     */
    public static Action scheduleHardwareRefreshAction(Org schedulerOrg, Server srvr, Date earliestAction)
            throws TaskomaticApiException {
        checkSaltOrManagementEntitlement(srvr.getId());

        Action action = ActionFactory.createAction(ActionFactory.TYPE_HARDWARE_REFRESH_LIST,
                null, schedulerOrg, earliestAction);

        ActionFactory.createAddServerAction(srvr, action);

        ActionFactory.save(action);

        taskomaticApi.scheduleActionExecution(action);
        return action;
    }

    private static void checkSaltOrManagementEntitlement(Long sid) {
        if (!SystemManager.hasEntitlement(sid, EntitlementManager.MANAGEMENT) &&
                !SystemManager.hasEntitlement(sid, EntitlementManager.SALT)) {
            log.error("Unable to run action on a system without either Salt or Management entitlement, id {}", sid);
            throw new MissingEntitlementException(
                    EntitlementManager.MANAGEMENT.getHumanReadableLabel() + " or " +
                            EntitlementManager.SALT.getHumanReadableLabel()
            );
        }
    }

    /**
     * Schedules all Errata for the given system.
     *
     * @param scheduler Person scheduling the action.
     * @param srvr      Server whose errata is going to be scheduled.
     * @param earliest  Earliest possible time action will occur.
     * @throws TaskomaticApiException if there was a Taskomatic error (typically: Taskomatic is down)
     */
    public static void scheduleAllErrataUpdate(User scheduler, Server srvr, Date earliest)
            throws TaskomaticApiException {
        // Do not elaborate, we need only the IDs in here
        DataResult<Errata> errata = SystemManager.unscheduledErrata(scheduler,
                srvr.getId(), null);
        List<Long> errataIds = new ArrayList<>();
        for (Errata e : errata) {
            errataIds.add(e.getId());
        }
        List<Long> serverIds = Arrays.asList(srvr.getId());
        ErrataManager.applyErrata(scheduler, errataIds, earliest, serverIds);
    }

    /**
     * Schedules a package action of the given type for the given server with the
     * packages given as a list.
     *
     * @param scheduler      The user scheduling the action.
     * @param pkgs           A list of maps containing keys 'name_id', 'evr_id' and
     *                       optional 'arch_id' with Long values.
     * @param type           The type of the package action.  One of the static types found in
     *                       ActionFactory
     * @param earliestAction The earliest time that this action could happen.
     * @param servers        The server(s) that this action is for.
     * @return The action that has been scheduled.
     * @throws TaskomaticApiException if there was a Taskomatic error (typically: Taskomatic is down)
     */
    public static Action schedulePackageAction(User scheduler, List<Map<String, Long>> pkgs, ActionType type,
                                               Date earliestAction, Server... servers) throws TaskomaticApiException {
        Set<Long> serverIds = new HashSet<>();
        for (Server s : servers) {
            serverIds.add(s.getId());
        }

        return schedulePackageAction(scheduler, pkgs, type, earliestAction, serverIds);
    }

    /**
     * Schedules a package action of the given type for the given server with the
     * packages given as a list.
     *
     * @param scheduler      The user scheduling the action.
     * @param pkgs           A list of maps containing keys 'name_id', 'evr_id' and
     *                       optional 'arch_id' with Long values.
     * @param type           The type of the package action.  One of the static types found in
     *                       ActionFactory
     * @param earliestAction The earliest time that this action could happen.
     * @param serverIds      The server ids that this action is for.
     * @return The action that has been scheduled.
     * @throws TaskomaticApiException if there was a Taskomatic error (typically: Taskomatic is down)
     */
    public static Action schedulePackageAction(User scheduler, List<Map<String, Long>> pkgs, ActionType type,
                                               Date earliestAction, Set<Long> serverIds)
            throws TaskomaticApiException {

        String name = type.getPackageActionName();

        Action action = ActionFactory.createAndSaveAction(type, scheduler, name, earliestAction);
        ActionFactory.scheduleForExecution(action, serverIds);

        ActionFactory.save(action);

        addPackageActionDetails(List.of(action), pkgs);
        taskomaticApi.scheduleActionExecution(action);
        if (ActionFactory.TYPE_PACKAGES_UPDATE.equals(type)) {
            MinionActionManager.scheduleStagingJobsForMinions(singletonList(action), scheduler.getOrg());
        }

        return action;
    }

    /**
     * Adds package details to some Actions
     *
     * @param actions     the actions
     * @param packageMaps A list of maps containing keys 'name_id', 'evr_id' and optional 'arch_id' with Long values.
     */
    public static void addPackageActionDetails(Collection<Action> actions, List<Map<String, Long>> packageMaps) {
        if (packageMaps != null) {
            List<Map<String, Long>> pkgMaps;

            if (actions.iterator().next().getActionType().equals(ActionFactory.TYPE_PACKAGES_REMOVE)) {
                // our packages.pkgremove state is handling duplicates
                pkgMaps = packageMaps;
            }
            else {
                long noarch = PackageFactory.lookupPackageArchByLabel("noarch").getId();
                long all = PackageFactory.lookupPackageArchByLabel("all-deb").getId();
                Map<String, Map<String, Long>> uPkgMap = new HashMap<>();
                // For other salt pkg states (pkg.installed), name + arch must be unique.
                for (Map<String, Long> p : packageMaps) {
                    long archId = p.getOrDefault("arch_id", noarch);
                    String name = String.valueOf(p.get("name_id"));
                    if (archId == noarch || archId == all) {
                        if (uPkgMap.keySet().stream().noneMatch(k -> k.startsWith(name + "."))) {
                            uPkgMap.put(name, p);
                        }
                    }
                    else if (!uPkgMap.containsKey(name)) {
                        String key = name + "." + p.get("arch_id");
                        uPkgMap.put(key, p);
                    }
                }
                pkgMaps = new ArrayList<>(uPkgMap.values());
            }
            List<Map<String, Object>> paramList =
                    actions.stream().flatMap(action -> {
                                String packageParameter = action.getPackageParameter();
                                return pkgMaps.stream().map(packageMap -> {
                                    Map<String, Object> params = new HashMap<>();
                                    params.put("action_id", action.getId());
                                    params.put("name_id", packageMap.get("name_id"));
                                    params.put("evr_id", packageMap.get("evr_id"));
                                    params.put("arch_id", packageMap.get("arch_id"));
                                    params.put("pkg_parameter", packageParameter);
                                    return params;
                                });
                            })
                            .collect(toList());

            ModeFactory.getWriteMode("Action_queries", "schedule_action")
                    .executeUpdates(paramList);
        }
    }

    /**
     * converts a RhnSet of packages to a List of Map(String, Long) structure
     *
     * @param pkgs The set of RhnSet packages to be converted.
     * @return a structure List of Map(String, Long) of packages
     */
    public static List<Map<String, Long>> convertPackagesFromRhnSetToListOfMaps(RhnSet pkgs) {
        List<Map<String, Long>> packages = new LinkedList<>();
        for (RhnSetElement rse : pkgs.getElements()) {
            Map<String, Long> row = new HashMap<>();
            row.put("name_id", rse.getElement());
            row.put("evr_id", rse.getElementTwo());
            row.put("arch_id", rse.getElementThree());
            // bugzilla: 191000, we forgot to populate the damn LinkedList :(
            packages.add(row);
        }

        return packages;
    }

    /**
     * Schedules Xccdf evaluation.
     *
     * @param scheduler      User scheduling the action.
     * @param srvr           Server for which the action affects.
     * @param path           Path for the Xccdf content.
     * @param parameters     Additional parameters for oscap tool.
     * @param earliestAction Date of earliest action to be executed.
     * @return scheduled Scap Action
     * @throws TaskomaticApiException if there was a Taskomatic error (typically: Taskomatic is down)
     */
    public static ScapAction scheduleXccdfEval(User scheduler, Server srvr, String path,
                                               String parameters, Date earliestAction) throws TaskomaticApiException {
        return scheduleXccdfEval(scheduler, srvr, path, parameters, null, earliestAction);
    }

    /**
     * Schedules Xccdf evaluation.
     *
     * @param scheduler      User scheduling the action.
     * @param srvr           Server for which the action affects.
     * @param path           Path for the Xccdf content.
     * @param parameters     Additional parameters for oscap tool.
     * @param ovalFiles      Optional OVAL files for oscap tool.
     * @param earliestAction Date of earliest action to be executed.
     * @return scheduled Scap Action
     * @throws TaskomaticApiException if there was a Taskomatic error (typically: Taskomatic is down)
     */
    public static ScapAction scheduleXccdfEval(User scheduler, Server srvr, String path,
                                               String parameters, String ovalFiles, Date earliestAction)
            throws TaskomaticApiException {
        Set<Long> serverIds = new HashSet<>();
        serverIds.add(srvr.getId());
        return scheduleXccdfEval(scheduler, serverIds, path, parameters, ovalFiles, earliestAction);
    }

    /**
     * Schedules Xccdf evaluation.
     *
     * @param scheduler      User scheduling the action.
     * @param serverIds      Set of server identifiers for which the action affects.
     * @param path           Path for the Xccdf content.
     * @param parameters     Additional parameters for oscap tool.
     * @param ovalFiles      Optional OVAL files for oscap tool.
     * @param earliestAction Date of earliest action to be executed.
     * @return scheduled Scap Action
     * @throws TaskomaticApiException if there was a Taskomatic error (typically: Taskomatic is down)
     * @throws MissingCapabilityException if scripts cannot be run
     */
    public static ScapAction scheduleXccdfEval(User scheduler, Set<Long> serverIds,
                                               String path, String parameters, String ovalFiles, Date earliestAction)
            throws TaskomaticApiException {
        if (serverIds.isEmpty()) {
            return null;
        }
        for (Long serverId : serverIds) {
            Server server = SystemManager.lookupByIdAndUser(serverId, scheduler);

            if (!SystemManager.clientCapable(serverId, SystemManager.CAP_SCAP)) {
                throw new MissingCapabilityException("OpenSCAP", server);
            }
            if (!SystemManager.hasEntitlement(serverId,
                    EntitlementManager.MANAGEMENT) &&
                    !SystemManager.hasEntitlement(serverId,
                            EntitlementManager.SALT)) {
                throw new MissingEntitlementException(
                        EntitlementManager.MANAGEMENT.getHumanReadableLabel() +
                                " or " +
                                EntitlementManager.SALT.getHumanReadableLabel()
                );
            }
        }

        ScapActionDetails scapDetails = new ScapActionDetails(path, parameters, ovalFiles);

        ScapAction action = (ScapAction) ActionFactory.createAndSaveAction(ActionFactory.TYPE_SCAP_XCCDF_EVAL,
                scheduler,
                ActionFactory.TYPE_SCAP_XCCDF_EVAL.getName(),
                earliestAction);
        ActionFactory.scheduleForExecution(action, serverIds);

        action.setScapActionDetails(scapDetails);
        ActionFactory.save(action);
        taskomaticApi.scheduleActionExecution(action);
        return action;
    }

    /**
     * Schedule machine reboot.
     *
     * @param scheduler      Logged in user
     * @param server         Server, which is going to be rebooted
     * @param earliestAction Earliest date. If null, then date is current.
     * @return scheduled reboot action
     * @throws TaskomaticApiException if there was a Taskomatic error (typically: Taskomatic is down)
     */
    public static Action scheduleReboot(User scheduler, Server server, Date earliestAction)
            throws TaskomaticApiException {
        Action action = ActionFactory.createAction(ActionFactory.TYPE_REBOOT,
                scheduler,
                earliestAction);
        ActionFactory.createAddServerAction(server, action);

        ActionFactory.save(action);
        taskomaticApi.scheduleActionExecution(action);
        return action;
    }

    /**
     * scheduleCertificateUpdate
     *
     * @param scheduler      Logged in user
     * @param server         Server, to update the certificate for
     * @param earliestAction Earliest date. If null, use current date
     * @return scheduled certificate update action
     * @throws TaskomaticApiException if there was a Taskomatic error (typically: Taskomatic is down)
     */
    public static Action scheduleCertificateUpdate(User scheduler, Server server,
                                                   Date earliestAction)
            throws TaskomaticApiException {
        if (!SystemManager.clientCapable(server.getId(), "clientcert.update_client_cert")) {
            throw new MissingCapabilityException("spacewalk-client-cert", server);
        }

        Action action = ActionFactory.createAction(ActionFactory.TYPE_CLIENTCERT_UPDATE_CLIENT_CERT,
                scheduler,
                (earliestAction == null ? new Date() : earliestAction));
        ActionFactory.createAddServerAction(server, action);

        ActionFactory.save(action);
        taskomaticApi.scheduleActionExecution(action);
        return action;
    }

    /**
     * Schedule a distribution upgrade.
     * @param scheduler user who scheduled this action
     * @param earliestAction date of earliest action
     * @param actionChain the action chain
     * @param dryRun true if it's a dry run to test the migration
     * @param detailsMap the action details map
     * @return the scheduled action
     * @throws TaskomaticApiException if there was a Taskomatic error
     *                                (typically: Taskomatic is down)
     */
    public static List<DistUpgradeAction> scheduleDistUpgrade(User scheduler, Date earliestAction,
                                                              ActionChain actionChain, boolean dryRun,
                                                              Map<Long, DistUpgradeActionDetails> detailsMap)
        throws TaskomaticApiException {
        ActionType actionType = ActionFactory.TYPE_DIST_UPGRADE;

        // Construct the action name
        String actionName = ActionFactory.TYPE_DIST_UPGRADE.getName();
        if (dryRun) {
            actionName += " (Dry Run)";
        }

        if (actionChain != null) {
            int sortOrder = ActionChainFactory.getNextSortOrderValue(actionChain);

            List<DistUpgradeAction> actionsList = new ArrayList<>();
            for (DistUpgradeActionDetails details : detailsMap.values()) {
                var action = (DistUpgradeAction) ActionFactory.createAndSaveAction(
                    actionType, scheduler, actionName, earliestAction
                );

                action.setDetailsMap(Map.of(details.getServer().getId(), details));
                ActionFactory.save(action);

                ActionChainFactory.queueActionChainEntry(action, actionChain, details.getServer().getId(), sortOrder);
                actionsList.add(action);
            }

            return actionsList;
        }

        // Schedule the main action
        var action = (DistUpgradeAction) ActionFactory.createAction(actionType, scheduler, actionName, earliestAction);
        detailsMap.values().stream()
            .map(details -> details.getServer())
            .forEach(server -> ActionFactory.createAddServerAction(server, action));

        // Add the details and save
        action.setDetailsMap(detailsMap);
        ActionFactory.save(action);

        taskomaticApi.scheduleActionExecution(action, !dryRun);
        return List.of(action);
    }

    /**
     * Schedule an action for channel state on the specified list of servers
     *
     * @param user          User with permission to schedule an action
     * @param minionServers servers where channel state should be applied to
     * @return Set of scheduled Actions
     * @throws TaskomaticApiException if there was a Taskomatic error (typically: Taskomatic is down)
     */
    public static Action scheduleChannelState(User user, List<MinionServer> minionServers)
            throws TaskomaticApiException {
        List<String> states = Collections.singletonList("channels");
        List<Long> sids = minionServers.stream().map(Server::getId).collect(toList());
        Action action = scheduleApplyStates(user, sids, states, new Date());
        taskomaticApi.scheduleActionExecution(action);
        return action;
    }

    /**
     * Schedule application of the highstate.
     *
     * @param scheduler the user who is scheduling
     * @param sids      list of server ids
     * @param earliest  action will not be executed before this date
     * @param test      run states in test-only mode
     * @return the action object
     */
    public static ApplyStatesAction scheduleApplyHighstate(User scheduler, List<Long> sids, Date earliest,
                                                           Optional<Boolean> test) {
        return scheduleApplyStates(scheduler, sids, new ArrayList<>(), earliest, test);
    }

    /**
     * Schedule state application given a list of state modules. Salt will apply the highstate if an empty list of
     * state modules is given.
     *
     * @param scheduler the user who is scheduling
     * @param sids      list of server ids
     * @param mods      list of state modules to be applied
     * @param earliest  action will not be executed before this date
     * @return the action object
     */
    public static ApplyStatesAction scheduleApplyStates(User scheduler, List<Long> sids, List<String> mods,
                                                        Date earliest) {
        return scheduleApplyStates(scheduler, sids, mods, earliest, Optional.empty());
    }

    /**
     * Schedule state application given a list of state modules. Salt will apply the
     * highstate if an empty list of state modules is given.
     *
     * @param scheduler the user who is scheduling
     * @param sids      list of server ids
     * @param mods      list of state modules to be applied
     * @param earliest  action will not be executed before this date
     * @param test      run states in test-only mode
     * @return the action object
     */
    public static ApplyStatesAction scheduleApplyStates(User scheduler, List<Long> sids, List<String> mods,
                                                        Date earliest, Optional<Boolean> test) {
        return scheduleApplyStates(scheduler, sids, mods, Optional.empty(), earliest, test, false);
    }

    /**
     * Schedule state application given a list of state modules. Salt will apply the
     * highstate if an empty list of state modules is given.
     *
     * @param scheduler the user who is scheduling
     * @param sids      list of server ids
     * @param mods      list of state modules to be applied
     * @param pillar    optional pillar map
     * @param earliest  action will not be executed before this date
     * @param test      run states in test-only mode
     * @return the action object
     */
    public static ApplyStatesAction scheduleApplyStates(User scheduler, List<Long> sids, List<String> mods,
                                                        Optional<Map<String, Object>> pillar,
                                                        Date earliest, Optional<Boolean> test) {
        return scheduleApplyStates(scheduler, sids, mods, pillar, earliest, test, false);
    }

    /**
     * Schedule state application given a list of state modules. Salt will apply the
     * highstate if an empty list of state modules is given.
     *
     * @param scheduler the user who is scheduling
     * @param sids      list of server ids
     * @param mods      list of state modules to be applied
     * @param pillar    optional pillar map
     * @param earliest  action will not be executed before this date
     * @param test      run states in test-only mode
     * @param recurring whether the state is being applied recurring
     * @return the action object
     */
    public static ApplyStatesAction scheduleApplyStates(User scheduler, List<Long> sids, List<String> mods,
                                                        Optional<Map<String, Object>> pillar, Date earliest,
                                                        Optional<Boolean> test, boolean recurring) {
        return scheduleApplyStates(scheduler, sids, mods, pillar, earliest, test, recurring, false);
    }

    /**
     * Schedule state application given a list of state modules. Salt will apply the
     * highstate if an empty list of state modules is given.
     *
     * @param scheduler the user who is scheduling
     * @param sids      list of server ids
     * @param mods      list of state modules to be applied
     * @param pillar    optional pillar map
     * @param earliest  action will not be executed before this date
     * @param test      run states in test-only mode
     * @param recurring whether the state is being applied recurring
     * @param direct    whenther the state should be executed as direct call
     * @return the action object
     */
    public static ApplyStatesAction scheduleApplyStates(User scheduler, List<Long> sids, List<String> mods,
                                                        Optional<Map<String, Object>> pillar, Date earliest,
                                                        Optional<Boolean> test, boolean recurring, boolean direct) {

        ApplyStatesAction action = (ApplyStatesAction) ActionFactory.createAction(ActionFactory.TYPE_APPLY_STATES,
                scheduler, defineStatesActionName(mods, recurring),
                scheduler != null ? scheduler.getOrg() : OrgFactory.getSatelliteOrg(), earliest);


        ApplyStatesActionDetails actionDetails = new ApplyStatesActionDetails();
        actionDetails.setMods(mods);
        actionDetails.setPillarsMap(pillar);
        test.ifPresent(actionDetails::setTest);
        actionDetails.setDirect(direct);
        action.setDetails(actionDetails);
        ActionFactory.save(action);

        ActionFactory.scheduleForExecution(action, new HashSet<>(sids));
        return action;
    }

    /**
     * Define apply states action name.
     *
     * @param mods      - the mods applied
     * @param recurring - whether the states are being applied recurring
     * @return - the name of the action
     */
    public static String defineStatesActionName(List<String> mods, boolean recurring) {
        StringBuilder statesDescription = new StringBuilder("Apply ");
        if (recurring) {
            statesDescription.append("recurring ");
        }
        statesDescription.append(mods.isEmpty() ? "highstate" : "states " + mods);
        return statesDescription.toString();
    }

    /**
     * Schedule image build
     *
     * @param scheduler the scheduler
     * @param sids      the sids
     * @param version   the version
     * @param profile   the profile
     * @param earliest  the earliest
     * @return the image build action
     */
    public static ImageBuildAction scheduleImageBuild(User scheduler, List<Long> sids,
                                                      String version, ImageProfile profile, Date earliest) {

        ImageBuildAction action = (ImageBuildAction) ActionFactory.createAction(ActionFactory.TYPE_IMAGE_BUILD,
                scheduler, "Image Build " + profile.getLabel(),
                scheduler != null ? scheduler.getOrg() : OrgFactory.getSatelliteOrg(), earliest);

        ImageBuildActionDetails actionDetails = new ImageBuildActionDetails();
        actionDetails.setVersion(version);
        actionDetails.setImageProfileId(profile.getProfileId());
        action.setDetails(actionDetails);
        ActionFactory.save(action);

        ActionFactory.scheduleForExecution(action, new HashSet<>(sids));
        return action;
    }

    /**
     *
     * @param scheduler     the scheduler
     * @param sids          the sids
     * @param buildActionId the build actionId
     * @param version       the image version
     * @param name          the image name
     * @param store         the image store
     * @param earliest      the earliest
     * @return the image inspect action
     */
    public static ImageInspectAction scheduleImageInspect(User scheduler, List<Long> sids,
                                                          Optional<Long> buildActionId, String version, String name,
                                                          ImageStore store, Date earliest) {

        String actionName = "Image Inspect " + store.getUri() + "/" + name + ":" + version;
        ImageInspectAction action = (ImageInspectAction) ActionFactory.createAction(ActionFactory.TYPE_IMAGE_INSPECT,
                scheduler, actionName,
                scheduler != null ? scheduler.getOrg() : OrgFactory.getSatelliteOrg(), earliest);

        ImageInspectActionDetails actionDetails = new ImageInspectActionDetails();
        actionDetails.setName(name);
        buildActionId.ifPresent(actionDetails::setBuildActionId);
        actionDetails.setVersion(version);
        actionDetails.setImageStoreId(store.getId());
        action.setDetails(actionDetails);
        ActionFactory.save(action);

        ActionFactory.scheduleForExecution(action, new HashSet<>(sids));
        return action;
    }

    /**
     * Connect given systems to another proxy.
     *
     * @param loggedInUser The current user
     * @param sysids       A list of systems ids
     * @param proxyId      Id of the proxy or 0 for direct connection to SUMA server
     * @return Returns a list of scheduled action ids
     *
     */
    public static List<Long> changeProxy(User loggedInUser, List<Long> sysids, Long proxyId)
            throws TaskomaticApiException {
        List<Long> visible = MinionServerFactory.lookupVisibleToUser(loggedInUser)
                .map(Server::getId).collect(toList());
        if (!visible.containsAll(sysids)) {
            sysids.removeAll(visible);
            throw new UnsupportedOperationException("Some System not available or not managed with Salt: " + sysids);
        }

        List<MinionServer> minions = sysids.stream().map(
                id -> SystemManager.lookupByIdAndUser(id, loggedInUser).asMinionServer().get()).collect(toList());

        List<Long> proxies = minions.stream().filter(Server::isProxy).map(Server::getId).collect(toList());
        if (!proxies.isEmpty()) {
            throw new UnsupportedOperationException("Some of the minions are proxies: " + proxies);
        }

        Optional<Server> proxy = Optional.empty();

        if (proxyId != 0) {
            proxy = Optional.of(SystemManager.lookupByIdAndUser(proxyId, loggedInUser));
            proxy.ifPresent(p -> {
                if (!p.isProxy()) {
                    throw new UnsupportedOperationException("The system is not a proxy: " + p.getId());
                }
            });
        }
        Optional<Long> proxyIdOpt = proxy.map(Server::getId);

        List<Long> sshIds = minions.stream()
                .filter(minion -> ContactMethodUtil.isSSHPushContactMethod(minion.getContactMethod()))
                .map(minion -> {
                    // handle SSH minions
                    minion.updateServerPaths(proxyIdOpt);
                    ServerFactory.save(minion);

                    MinionPillarManager.INSTANCE.generatePillar(minion);
                    return minion.getId();
                }).collect(toList());


        List<Long> normalIds = minions.stream()
                .filter(minion -> !ContactMethodUtil.isSSHPushContactMethod(minion.getContactMethod()))
                .map(Server::getId)
                .collect(toList());

        List<Long> ret = new ArrayList<>();
        if (!sshIds.isEmpty()) {
            // action for SSH minions - update channel configuration
            Action a = scheduleApplyStates(loggedInUser, sshIds,
                    Collections.singletonList(ApplyStatesEventMessage.CHANNELS),
                    new Date());
            a = ActionFactory.save(a);
            taskomaticApi.scheduleActionExecution(a);
            ret.add(a.getId());
        }

        if (!normalIds.isEmpty()) {
            // action for normal minions - update salt master, the channels will be updated after minion restart
            Map<String, Object> pillar = new HashMap<>();
            pillar.put("mgr_server", proxy.map(Server::getHostname).orElse(ConfigDefaults.get().getJavaHostname()));

            Action a = scheduleApplyStates(loggedInUser, normalIds,
                    Collections.singletonList(ApplyStatesEventMessage.SET_PROXY),
                    Optional.of(pillar), new Date(), Optional.empty());
            a = ActionFactory.save(a);
            taskomaticApi.scheduleActionExecution(a);
            ret.add(a.getId());
        }
        return ret;
    }

    /**
     * Schedule Action to get and upload supportdata from the defined system to SCC.
     *
     * @param scheduler     the scheduler of this action
     * @param sid           the system ID
     * @param caseNumber    the support case number
     * @param parameter     additional parameter for the tool which collect the data
     * @param uploadGeoType the uploadGeo Type
     * @param earliest      the date when this action should be executed
     * @return the action
     */
    public static Action scheduleSupportDataAction(User scheduler, long sid, String caseNumber, String parameter,
                                                   UploadGeoType uploadGeoType, Date earliest) {

        SupportDataAction action = (SupportDataAction) ActionFactory.createAction(ActionFactory.TYPE_SUPPORTDATA_GET,
                scheduler, "Get and Upload Support data",
                scheduler != null ? scheduler.getOrg() : OrgFactory.getSatelliteOrg(), earliest);

        SupportDataActionDetails actionDetails = new SupportDataActionDetails();
        actionDetails.setCaseNumber(caseNumber);
        actionDetails.setParameter(parameter);
        actionDetails.setGeoType(uploadGeoType);
        actionDetails.setParentAction(action);

        action.setDetails(actionDetails);
        ActionFactory.save(action);

        ActionFactory.scheduleForExecution(action, Set.of(sid));
        return action;
    }

    /**
     * Schedule an immediate Ansible inventory refresh without a user.
     *
     * @param server        the server
     * @param inventoryPath the Ansible inventory
     * @return the scheduled InventoryAction
     * @throws TaskomaticApiException if there was a Taskomatic error (typically: Taskomatic is down)
     */
    public static Action scheduleInventoryRefresh(Server server, String inventoryPath) throws TaskomaticApiException {
        return scheduleInventoryRefresh(Optional.empty(), server, inventoryPath, new Date());
    }

    /**
     * Schedule an Ansible inventory refresh.
     *
     * @param user          the user
     * @param server        the server
     * @param earliest      The earliest time this action should be run.
     * @param inventoryPath the Ansible inventory
     * @return the scheduled InventoryAction
     * @throws TaskomaticApiException if there was a Taskomatic error (typically: Taskomatic is down)
     */
    public static Action scheduleInventoryRefresh(Optional<User> user, Server server, String inventoryPath,
                                                  Date earliest) throws TaskomaticApiException {
        checkSaltOrManagementEntitlement(server.getId());

        InventoryAction action = (InventoryAction) ActionFactory.createAction(ActionFactory.TYPE_INVENTORY,
                user.orElse(null), server.getOrg(), earliest);

        InventoryActionDetails details = new InventoryActionDetails();
        details.setInventoryPath(inventoryPath);
        action.setDetails(details);

        ActionFactory.createAddServerAction(server, action);

        ActionFactory.save(action);
        taskomaticApi.scheduleActionExecution(action);
        return action;
    }

    /**
     * Schedule proxy.apply_proxy_config salt state.
     *
     * @param loggedInUser The current user
     * @param sysids       A list of systems ids
     * @param pillar       The pillar passed to the salt state
     * @return the scheduled action
     *
     */
    public static Action scheduleApplyProxyConfig(User loggedInUser, List<Long> sysids,
                                                  Optional<Map<String, Object>> pillar) throws TaskomaticApiException {
        Date earliestAction = new Date();
        Action action = scheduleApplyStates(loggedInUser, sysids, Collections.singletonList("proxy.apply_proxy_config"),
                pillar, earliestAction, Optional.empty());
        action = ActionFactory.save(action);
        taskomaticApi.scheduleActionExecution(action);
        return action;
    }

    /**
     * Schedule proxy.backup salt state.
     *
     * @param loggedInUser The current user
     * @param proxyIds     A list of systems ids of the proxies
     * @return the scheduled action
     *
     */
    public static Action scheduleProxyBackup(User loggedInUser, List<Long> proxyIds) throws TaskomaticApiException {
        Date earliestAction = new Date();
        Action action = scheduleApplyStates(loggedInUser, proxyIds, Collections.singletonList("proxy.backup"),
                earliestAction, Optional.empty());
        action = ActionFactory.save(action);
        taskomaticApi.scheduleActionExecution(action);
        return action;
    }
}
