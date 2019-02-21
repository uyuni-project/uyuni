/**
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

import com.redhat.rhn.common.conf.ConfigDefaults;
import com.redhat.rhn.common.db.datasource.DataResult;
import com.redhat.rhn.common.db.datasource.ModeFactory;
import com.redhat.rhn.common.db.datasource.SelectMode;
import com.redhat.rhn.common.db.datasource.WriteMode;
import com.redhat.rhn.common.hibernate.LookupException;
import com.redhat.rhn.common.localization.LocalizationService;
import com.redhat.rhn.domain.action.Action;
import com.redhat.rhn.domain.action.ActionFactory;
import com.redhat.rhn.domain.action.ActionType;
import com.redhat.rhn.domain.action.config.ConfigAction;
import com.redhat.rhn.domain.action.config.ConfigUploadAction;
import com.redhat.rhn.domain.action.dup.DistUpgradeAction;
import com.redhat.rhn.domain.action.dup.DistUpgradeActionDetails;
import com.redhat.rhn.domain.action.errata.ErrataAction;
import com.redhat.rhn.domain.action.image.DeployImageAction;
import com.redhat.rhn.domain.action.image.DeployImageActionDetails;
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
import com.redhat.rhn.domain.common.FileList;
import com.redhat.rhn.domain.config.ConfigChannel;
import com.redhat.rhn.domain.config.ConfigFileName;
import com.redhat.rhn.domain.config.ConfigRevision;
import com.redhat.rhn.domain.config.ConfigurationFactory;
import com.redhat.rhn.domain.errata.Errata;
import com.redhat.rhn.domain.image.ImageProfile;
import com.redhat.rhn.domain.image.ImageStore;
import com.redhat.rhn.domain.image.ProxyConfig;
import com.redhat.rhn.domain.kickstart.KickstartData;
import com.redhat.rhn.domain.kickstart.KickstartFactory;
import com.redhat.rhn.domain.org.Org;
import com.redhat.rhn.domain.org.OrgFactory;
import com.redhat.rhn.domain.rhnpackage.Package;
import com.redhat.rhn.domain.rhnpackage.PackageDelta;
import com.redhat.rhn.domain.rhnpackage.PackageFactory;
import com.redhat.rhn.domain.rhnset.RhnSet;
import com.redhat.rhn.domain.rhnset.RhnSetElement;
import com.redhat.rhn.domain.server.MinionServer;
import com.redhat.rhn.domain.server.Server;
import com.redhat.rhn.domain.user.User;
import com.redhat.rhn.frontend.dto.PackageMetadata;
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

import com.suse.utils.Opt;
import org.apache.commons.codec.binary.Base64;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.tuple.ImmutablePair;
import org.apache.commons.lang3.tuple.Pair;
import org.apache.log4j.Logger;
import org.cobbler.Profile;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Calendar;
import java.util.Collection;
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;

import static com.suse.manager.utils.MinionServerUtils.isMinionServer;
import static java.util.Collections.singletonList;
import static java.util.stream.Collectors.toList;
import static java.util.stream.Collectors.toMap;
import static java.util.stream.Collectors.toSet;
import static java.util.stream.Stream.concat;

/**
 * ActionManager - the singleton class used to provide Business Operations
 * on Actions where those operations interact with other top tier Business
 * Objects.
 */
public class ActionManager extends BaseManager {
    private static Logger log = Logger.getLogger(ActionManager.class);

    // List of package names that we want to make sure we dont
    // remove when doing a package sync.  Never remove running kernel
    // for instance.
    public static final String[] PACKAGES_NOT_REMOVABLE = {"kernel"};

    /**
     * This was extracted to a constant from the
     * {@link #scheduleAction(User, Server, ActionType, String, Date)} method. At the time
     * it was in there, there was a comment "hmm 10?". Not sure what the hesitation is
     * but I wanted to retain that comment with regard to this value.
     */
    private static final Long REMAINING_TRIES = 10L;

    private static TaskomaticApi taskomaticApi = new TaskomaticApi();

    private ActionManager() {
    }


    /**
     * Set the {@link TaskomaticApi} instance to use. Only needed for unit tests.
     * @param taskomaticApiIn the {@link TaskomaticApi}
     */
    public static void setTaskomaticApi(TaskomaticApi taskomaticApiIn) {
        taskomaticApi = taskomaticApiIn;
    }

    /**
     * Removes a list of actions.
     * @param actionIds actions to remove
     * @return int the number of failed action removals
     */
    public static int removeActions(List actionIds) {
        int failed = 0;
        for (Iterator ids = actionIds.iterator(); ids.hasNext();) {
            Long actionId = (Long) ids.next();
            failed += ActionFactory.removeAction(actionId);
        }
        return failed;
    }

    /**
     * Mark action as failed for specified system
     * @param loggedInUser The user making the request.
     * @param serverId server id
     * @param actionId The id of the Action to be set as failed
     * @param message Message from user, reason of this fail
     * @return int 1 if succeed
     */
    public static int failSystemAction(User loggedInUser, Long serverId, Long actionId,
                                       String message) {
        Action action = ActionFactory.lookupByUserAndId(loggedInUser, actionId);
        Server server = SystemManager.lookupByIdAndUser(serverId, loggedInUser);
        ServerAction serverAction = ActionFactory.getServerActionForServerAndAction(server,
                action);
        if (action == null || serverAction == null) {
            throw new LookupException("Could not find action " + actionId + " on system " +
                    serverId);
        }
        Date now = Calendar.getInstance().getTime();
        if (serverAction.getStatus().equals(ActionFactory.STATUS_QUEUED) ||
                serverAction.getStatus().equals((ActionFactory.STATUS_PICKEDUP))) {
            serverAction.setStatus(ActionFactory.STATUS_FAILED);
            serverAction.setResultMsg(message);
            serverAction.setCompletionTime(now);
        }
        else {
            throw new IllegalStateException("Action " + actionId +
                    " must be in Pending state on " + "server " + serverId);
        }
        return 1;
    }

    /**
     * Retreive the specified Action, assuming that the User making the request
     * has the required permissions.
     * @param user The user making the lookup request.
     * @param aid The id of the Action to lookup.
     * @return the specified Action.
     * @throws com.redhat.rhn.common.hibernate.LookupException if the Action
     * can't be looked up.
     */
    public static Action lookupAction(User user, Long aid) {
        Action returnedAction = null;
        if (aid == null) {
            return null;
        }

        returnedAction = ActionFactory.lookupByUserAndId(user, aid);

        //TODO: put this in the hibernate lookup query
        SelectMode m = ModeFactory.getMode("Action_queries", "visible_to_user");
        Map<String, Object> params = new HashMap<String, Object>();
        params.put("user_id", user.getId());
        params.put("aid", aid);
        if (m.execute(params).size() < 1) {
            returnedAction = null;
        }

        if (returnedAction == null) {
            LocalizationService ls = LocalizationService.getInstance();
            LookupException e =
                    new LookupException("Could not find action with id: " + aid);
            e.setLocalizedTitle(ls.getMessage("lookup.jsp.title.action"));
            e.setLocalizedReason1(ls.getMessage("lookup.jsp.reason1.action"));
            e.setLocalizedReason2(ls.getMessage("lookup.jsp.reason2.action"));
            throw e;
        }

        return returnedAction;
    }



    /**
     * Lookup the last completed Action on a Server
     *  given the user, action type and server.
     * This is useful especially in cases where we want to
     * find the last deployed config action ...
     *
     * @param user the user doing the search (needed for permssion checking)
     * @param type the action type of the action to be queried.
     * @param server the server who's latest completed action is desired.
     * @return the Action found or null if none exists
     */
    public static Action lookupLastCompletedAction(User user,
            ActionType type,
            Server server) {
        // TODO: check on user visibility ??

        return ActionFactory.lookupLastCompletedAction(user, type, server);
    }

    /**
     * Deletes the action set with the given label.
     * @param user User associated with the set of actions.
     * @param label Action label to be updated.
     */
    public static void deleteActions(User user, String label) {
        WriteMode m = ModeFactory.getWriteMode("Action_queries",
                "delete_actions");
        Map<String, Object> params = new HashMap<String, Object>();
        params.put("user_id", user.getId());
        params.put("org_id", user.getOrg().getId());
        params.put("label", label);
        m.executeUpdate(params);
    }

    /**
     * Deletes the action set by id and type.
     * @param id Action ID
     * @param type Action Type
     */
    public static void deleteActionsByIdAndType(Long id, Integer type) {
        WriteMode m = ModeFactory.getWriteMode("Action_queries",
                "delete_actions_by_id_and_type");
        Map params = new HashMap();
        params.put("id", id);
        params.put("action_type", type);
        m.executeUpdate(params);
    }

    /**
     * Archives the action set with the given label.
     * @param user User associated with the set of actions.
     * @param label Action label to be updated.
     */
    public static void archiveActions(User user, String label) {
        WriteMode m = ModeFactory.getWriteMode("Action_queries",
                "archive_actions");
        Map<String, Object> params = new HashMap<String, Object>();
        params.put("user_id", user.getId());
        params.put("org_id", user.getOrg().getId());
        params.put("label", label);
        m.executeUpdate(params);
    }

    /**
     * Cancels the server actions associated with a given action, and if
     * required deals with associated pending kickstart actions and minion
     * jobs.
     *
     * Actions themselves are not deleted, only the ServerActions associated
     * with them.
     *
     * @param user User requesting the action be cancelled.
     * @param actions List of actions to be cancelled.
     * @throws TaskomaticApiException if there was a Taskomatic error
     * (typically: Taskomatic is down)
     */
    public static void cancelActions(User user, Collection<Action> actions) throws TaskomaticApiException {
        cancelActions(user, actions, Optional.empty());
    }

    /**
     * Cancels the server actions associated with a given action, and if
     * required deals with associated pending kickstart actions and minion
     * jobs.
     *
     * Actions themselves are not deleted, only the ServerActions associated
     * with them.
     *
     * @param user User requesting the action be cancelled.
     * @param actions List of actions to be cancelled.
     * @param serverIds If specified, only cancel ServerActions for a subset of system IDs
     * @throws TaskomaticApiException if there was a Taskomatic error
     * (typically: Taskomatic is down)
     */
    public static void cancelActions(User user, Collection<Action> actions, Optional<Collection<Long>> serverIds)
        throws TaskomaticApiException {
        if (log.isDebugEnabled()) {
            String actionIds = actions.stream()
                    .map(Action::getId)
                    .collect(toList())
                    .toString();
            log.debug("Cancelling actions: " + actionIds + " for user: " + user.getLogin());
        }

        // Can only cancel top level actions:
        if (actions.stream().anyMatch(a -> a.getPrerequisite() != null)) {
            throw new ActionIsChildException();
        }

        Set<Action> actionsToDelete = concat(
            actions.stream(),
            actions.stream().flatMap(ActionFactory::lookupDependentActions)
        ).collect(toSet());

        Set<ServerAction> serverActions = actionsToDelete.stream()
                .flatMap(a -> a.getServerActions().stream())
                .filter(sa -> ActionFactory.STATUS_QUEUED.equals(sa.getStatus()) ||
                        ActionFactory.STATUS_PICKEDUP.equals(sa.getStatus()))
                .filter(Opt.fold(serverIds,
                        // if serverIds is not specified, do not filter at all
                        () -> (a -> true),
                        // if it is, only ServerActions that have server ids in the specified set can pass
                        s -> (a -> s.contains(a.getServerId()))
                ))
                .collect(toSet());

        Set<Server> servers = serverActions.stream()
                .map(ServerAction::getServer)
                .collect(toSet());

        // fail any Kickstart sessions for these actions and servers
        KickstartFactory.failKickstartSessions(actionsToDelete, servers);

        // cancel associated schedule in Taskomatic
        Map<Action, Set<Server>> actionMap = actionsToDelete.stream()
                .map(a -> new ImmutablePair<>(
                        a,
                        a.getServerActions().stream()
                            .filter(sa -> isMinionServer(sa.getServer()))
                            .filter(sa -> ActionFactory.STATUS_QUEUED.equals(sa.getStatus()))
                            .map(sa -> sa.getServer())
                            .collect(toSet())
                        )
                )
                .filter(p -> !p.getRight().isEmpty())
                .collect(toMap(
                    Pair::getLeft,
                    Pair::getRight
                ));

        if (!actionMap.isEmpty()) {
            taskomaticApi.deleteScheduledActions(actionMap);
        }

        serverActions.stream()
                .forEach(sa -> {
                    // Delete ServerActions from the database only if QUEUED
                    if (ActionFactory.STATUS_QUEUED.equals(sa.getStatus())) {
                        sa.getParentAction().getServerActions().remove(sa);
                        ActionFactory.delete(sa);
                    }
                    // Set to FAILED if the state is PICKED_UP
                    else if (ActionFactory.STATUS_PICKEDUP.equals(sa.getStatus())) {
                        failSystemAction(user, sa.getServerId(), sa.getParentAction().getId(),
                                "Canceled by " + user.getLogin());
                    }
                });

        // run post-actions
        actionsToDelete.stream()
                .forEach(a ->a.onCancelAction());
    }

    /**
     * Deletes the archived actions
     *
     * @param user User requesting the delete action
     * @param actionsIds List of action ids to be deleted
     */
    public static void deleteActionsById(User user, List actionsIds) {
        List<Action> actions = new ArrayList<Action>();
        for (Iterator<Number> ai = actionsIds.iterator(); ai.hasNext();) {
            long actionId = ai.next().longValue();
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
            ActionFactory.remove(action);
        }
    }

    /**
     * Adds a server to an action
     * @param sid The server id
     * @param action The parent action
     */
    public static void addServerToAction(Long sid, Action action) {
        ActionFactory.addServerToAction(sid, action);
    }

    /**
     * Adds a server to an action
     * @param server The server
     * @param action The parent action
     */
    public static void addServerToAction(Server server, Action action) {
        ActionFactory.addServerToAction(server, action);
    }

    /**
     * Creates an errata action with the specified Org
     * @return The created action
     * @param org The org that needs the errata.
     * @param errata The errata pertaining to this action
     */
    public static ErrataAction createErrataAction(Org org, Errata errata) {
        ErrataAction a = (ErrataAction) createErrataAction((User) null, errata);
        a.setOrg(org);
        return a;
    }

    /**
     * Creates an errata action
     * @return The created action
     * @param user The user scheduling errata
     * @param errata The errata pertaining to this action
     */
    public static Action createErrataAction(User user, Errata errata) {
        ErrataAction a = (ErrataAction)ActionFactory
                .createAction(ActionFactory.TYPE_ERRATA);
        if (user != null) {
            a.setSchedulerUser(user);
            a.setOrg(user.getOrg());
        }
        a.addErrata(errata);

        Object[] args = new Object[2];
        args[0] = errata.getAdvisory();
        args[1] = errata.getSynopsis();
        a.setName(LocalizationService.getInstance().getMessage("action.name", args));
        return a;
    }

    /**
     * Create a Config Upload action. This is a much different action from the
     * other config actions (doesn't involve revisions).
     * @param user The scheduler for this config action.
     * @param filenames A set of config file name ids as Longs
     * @param server The server for which to schedule this action.
     * @param channel The config channel to which files will be uploaded.
     * @param earliest The soonest time that this action could be executed.
     * @return The created upload action
     * @throws TaskomaticApiException if there was a Taskomatic error
     * (typically: Taskomatic is down)
     */
    public static Action createConfigUploadAction(User user, Set filenames, Server server,
            ConfigChannel channel, Date earliest)
        throws TaskomaticApiException {
        //TODO: right now, our general rule is that upload actions will
        //always upload into the sandbox for a system. If we ever wish to
        //make that a strict business rule, here is where we can verify that
        //the given channel is the sandbox for the given server.

        ConfigUploadAction a =
                (ConfigUploadAction)ActionFactory.createAction(
                        ActionFactory.TYPE_CONFIGFILES_UPLOAD, earliest);
        a.setOrg(user.getOrg());
        a.setSchedulerUser(user);
        a.setName(a.getActionType().getName());
        //put a single row into rhnActionConfigChannel
        a.addConfigChannelAndServer(channel, server);
        //put a single row into rhnServerAction
        addServerToAction(server.getId(), a);

        //now put a row into rhnActionConfigFileName for each path we have.
        Iterator i = filenames.iterator();
        while (i.hasNext()) {
            Long cfnid = (Long)i.next();
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
        if (a.getConfigFileNameAssociations().size() < 1) {
            return null;
        }

        ActionFactory.save(a);
        taskomaticApi.scheduleActionExecution(a);
        return a;
    }

    /**
     * Create a Config File Diff action.
     * @param user The user scheduling a diff action.
     * @param revisions A set of revision ids as Longs
     * @param serverIds A set of server ids as Longs
     * @return The created diff action
     * @throws TaskomaticApiException if there was a Taskomatic error
     * (typically: Taskomatic is down)
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
     * @param user The user scheduling the action.
     * @param revisions A set of revision ids as Longs
     * @param servers A set of server objects
     * @param type The type of config action
     * @param earliest The earliest time this action could execute.
     * @return The created config action
     * @throws TaskomaticApiException if there was a Taskomatic error
     * (typically: Taskomatic is down)
     */
    public static Action createConfigActionForServers(User user,
            Collection<Long> revisions,
            Collection<Server> servers,
            ActionType type, Date earliest) throws TaskomaticApiException {
        ConfigAction a = createConfigAction(user, type, earliest);
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
     * @param user the user scheduling the action
     * @param revisions a set of revision ids as Longs
     * @param configAction the action to add revisions to
     * @param server a server object
     */
    public static void addConfigurationRevisionsToAction(User user,
        Collection<Long> revisions, ConfigAction configAction, Server server) {
        for (Long revId : revisions) {
            try {
                ConfigRevision rev = ConfigurationManager.getInstance()
                    .lookupConfigRevision(user, revId);
                ActionFactory.addConfigRevisionToAction(rev, server, configAction);
            }
            catch (LookupException e) {
                log.error("Failed lookup for revision " + revId + "by user " +
                    user.getId());
            }
        }
    }

    /**
     * Checks that a server can be the target of a ConfigAction
     * @param type type of ConfigAction
     * @param server a server object
     * @throws MissingCapabilityException if server does not have needed capabilities
     */
    public static void checkConfigActionOnServer(ActionType type, Server server) {
        if (ActionFactory.TYPE_CONFIGFILES_DEPLOY.equals(type) &&
                !SystemManager.clientCapable(server.getId(),
                        SystemManager.CAP_CONFIGFILES_DEPLOY)) {
            throw new MissingCapabilityException(
                    SystemManager.CAP_CONFIGFILES_DEPLOY, server);
        }
    }

    /**
     * Returns a new ConfigAction object
     * @param user the user scheduling the action
     * @param type type of ConfigAction
     * @param earliest earliest action scheduling date
     * @return a ConfigAction
     */
    public static ConfigAction createConfigAction(User user, ActionType type,
        Date earliest) {
        ConfigAction a = (ConfigAction)ActionFactory.createAction(type, earliest);

        /** This is not localized, because the perl that prints this when the action is
         *  rescheduled doesn't do localization.  If the reschedule page ever get
         *  converted to java, we should pass in a LS key and then simply do the lookup
         *  on display
         */
        a.setName(a.getActionType().getName());
        a.setOrg(user.getOrg());
        a.setSchedulerUser(user);
        return a;
    }

    /**
     * Create a Config Action.
     * @param user The user scheduling the action.
     * @param revisions A set of revision ids as Longs
     * @param serverIds A set of server ids as Longs
     * @param type The type of config action
     * @param earliest The earliest time this action could execute.
     * @return The created config action
     * @throws TaskomaticApiException if there was a Taskomatic error
     * (typically: Taskomatic is down)
     */
    public static Action createConfigAction(User user, Collection<Long> revisions,
            Collection<Long> serverIds, ActionType type, Date earliest)
        throws TaskomaticApiException {

        List<Server> servers = SystemManager.hydrateServerFromIds(serverIds, user);
        return createConfigActionForServers(user, revisions, servers, type, earliest);
    }

    /**
     * Schedule deployment of an image to a vhost.
     *
     * @return The created action
     * @param user The user scheduling image deployment
     * @param imageUrl The URL of the image to be deployed
     * @param vcpus number of vcpus
     * @param memkb memory in Kb
     * @param bridge device
     * @param proxy proxy configuration
     */
    public static Action createDeployImageAction(User user, String imageUrl,
            Long vcpus, Long memkb, String bridge, ProxyConfig proxy) {
        DeployImageAction a = (DeployImageAction) ActionFactory
                .createAction(ActionFactory.TYPE_DEPLOY_IMAGE);
        if (user != null) {
            a.setSchedulerUser(user);
            a.setOrg(user.getOrg());
        }

        DeployImageActionDetails details = new DeployImageActionDetails();
        details.setParentAction(a);
        details.setVcpus(vcpus);
        details.setMemKb(memkb);
        details.setBridgeDevice(bridge);
        details.setDownloadUrl(imageUrl);
        if (proxy != null) {
            details.setProxyServer(proxy.getServer());
            details.setProxyUser(proxy.getUser());
            details.setProxyPass(new String(Base64.encodeBase64(
                    proxy.getPass().getBytes())));
        }
        a.setDetails(details);
        a.setName("Image Deployment: " + imageUrl);
        return a;
    }

    /**
     *
     * @param user   The user scheduling the action
     * @param server The server the action is being scheduled for
     * @param type   The type of the action
     *
     * @return The Action we have created
     *
     */
    public static Action createBaseAction(User user, Server server, ActionType type) {

        Action action =
                ActionFactory.createAction(type);

        action.setSchedulerUser(user);
        action.setOrg(user.getOrg());

        ServerAction sa = new ServerAction();
        sa.setStatus(ActionFactory.STATUS_QUEUED);
        sa.setRemainingTries(5L);
        sa.setServer(server);

        sa.setParentAction(action);
        action.addServerAction(sa);

        return action;
    }

    /**
     * Stores the action in the database through hibernate
     * @param actionIn The action to be stored
     * @return action
     */
    public static Action storeAction(Action actionIn) {
        return ActionFactory.save(actionIn);
    }

    /**
     * Reschedule the action so it can be attempted again.
     *
     * @param action Action to reschedule
     * @throws TaskomaticApiException if there was a Taskomatic error
     * (typically: Taskomatic is down)
     */
    public static void rescheduleAction(Action action) throws TaskomaticApiException {
        rescheduleAction(action, false);
    }

    /**
     * Reschedule the action so it can be attempted again.
     *
     * @param action Action to reschedule
     * @param onlyFailed reschedule only the ServerActions w/failed status
     * @throws TaskomaticApiException if there was a Taskomatic error
     * (typically: Taskomatic is down)
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
     * Retrieve the list of unarchived scheduled actions for the
     * current user
     * @param user The user in question
     * @param pc The details of which results to return
     * @param age how many days old a system can be in order to count as a "recently"
     * scheduled action
     * @return A list containing the pending actions for the user
     */
    public static DataResult recentlyScheduledActions(User user, PageControl pc,
            long age) {
        SelectMode m = ModeFactory.getMode("Action_queries",
                "recently_scheduled_action_list");
        Map<String, Object> params = new HashMap<String, Object>();
        params.put("user_id", user.getId());
        params.put("org_id", user.getOrg().getId());
        params.put("age", age);

        if (pc != null) {
            return makeDataResult(params, params, pc, m);
        }

        DataResult dr = m.execute(params);
        dr.setTotalSize(dr.size());
        return dr;
    }

    /**
     * Retrieve the list of all actions for a particular user.
     * This includes pending, completed, failed and archived actions.
     * @param user The user in question
     * @param pc The details of which results to return
     * @return A list containing the all actions for the user
     */
    public static DataResult allActions(User user, PageControl pc) {
        return getActions(user, pc, "all_action_list");
    }

    /**
     * Retrieve the list of pending actions for a particular user
     * @param user The user in question
     * @param pc The details of which results to return
     * @return A list containing the pending actions for the user
     */
    public static DataResult pendingActions(User user, PageControl pc) {
        return getActions(user, pc, "pending_action_list");
    }

    /**
     * Retrieve the list of pending actions for a particular user within the given set.
     *
     * @param user The user in question
     * @param pc The details of which results to return
     * @param setLabel Label of an RhnSet of actions IDs to limit the results to.
     * @return A list containing the pending actions for the user.
     */
    public static DataResult pendingActionsInSet(User user, PageControl pc,
            String setLabel) {

        return getActions(user, pc, "pending_actions_in_set", setLabel);
    }

    /**
     * Retrieve the list of pending actions for a particular user within the given set.
     *
     * @param user The user in question
     * @param pc The details of which results to return
     * @param setLabel Label of an RhnSet of actions IDs to limit the results to.
     * @param sid Server id
     * @return A list containing the pending actions for the user.
     */
    public static DataResult pendingActionsToDeleteInSet(User user, PageControl pc,
            String setLabel, Long sid) {
        SelectMode m = ModeFactory.getMode("System_queries",
                "pending_actions_to_delete_in_set");
        Map<String, Object> params = new HashMap<String, Object>();
        params.put("sid", sid);
        params.put("user_id", user.getId());
        params.put("set_label", setLabel);
        if (pc != null) {
            return makeDataResult(params, params, pc, m);
        }
        DataResult dr = m.execute(params);
        dr.setTotalSize(dr.size());
        dr.setElaborationParams(params);
        return dr;
    }

    /**
     * Retrieve the list of failed actions for a particular user
     * @param user The user in question
     * @param pc The details of which results to return
     * @return A list containing the pending actions for the user
     */
    public static DataResult failedActions(User user, PageControl pc) {
        return getActions(user, pc, "failed_action_list");
    }

    /**
     * Retrieve the list of completed actions for a particular user
     * @param user The user in question
     * @param pc The details of which results to return
     * @return A list containing the pending actions for the user
     */
    public static DataResult completedActions(User user, PageControl pc) {
        return getActions(user, pc, "completed_action_list");
    }

    /**
     * Retrieve the list of completed actions for a particular user
     * @param user The user in question
     * @param pc The details of which results to return
     * @return A list containing the pending actions for the user
     */
    public static DataResult archivedActions(User user, PageControl pc) {
        return getActions(user, pc, "archived_action_list");
    }

    /**
     * Helper method that does the work of getting a specific
     * DataResult for scheduled actions.
     * @param user The user in question
     * @param pc The details of which results to return
     * @param mode The mode
     * @return Returns a list containing the actions for the user
     */
    private static DataResult getActions(User user, PageControl pc, String mode,
            String setLabel) {
        SelectMode m = ModeFactory.getMode("Action_queries", mode);
        Map<String, Object> params = new HashMap<String, Object>();
        params.put("user_id", user.getId());
        params.put("org_id", user.getOrg().getId());
        if (setLabel != null) {
            params.put("set_label", setLabel);
        }
        if (pc != null) {
            return makeDataResult(params, params, pc, m);
        }
        int limit = ConfigDefaults.get().getActionsDisplayLimit();
        if (limit > 0) {
            m.setMaxRows(limit);
        }
        DataResult dr = m.execute(params);
        dr.setTotalSize(dr.size());
        dr.setElaborationParams(params);
        return dr;
    }

    /**
     * Helper method that does the work of getting a specific
     * DataResult for scheduled actions.
     * @param user The user in question
     * @param pc The details of which results to return
     * @param mode The mode
     * @return Returns a list containing the actions for the user
     */
    private static DataResult getActions(User user, PageControl pc, String mode) {
        return getActions(user, pc, mode, null);
    }

    /**
     * Returns the list of packages associated with a specific action.
     * @param aid The action id for the action in question
     * @param pc The details of which results to return
     * @return Return a list containing the packages for the action.
     */
    public static DataResult getPackageList(Long aid, PageControl pc) {
        SelectMode m = ModeFactory.getMode("Package_queries",
                "packages_associated_with_action");
        Map<String, Object> params = new HashMap<String, Object>();
        params.put("aid", aid);
        if (pc != null) {
            return makeDataResult(params, params, pc, m);
        }
        DataResult dr = m.execute(params);
        dr.setTotalSize(dr.size());
        return dr;
    }

    /**
     * Returns the list of errata associated with a specific action.
     * @param aid The action id for the action in question
     * @return Return a list containing the errata for the action.
     */
    public static DataResult getErrataList(Long aid) {
        SelectMode m = ModeFactory.getMode("Errata_queries",
                "errata_associated_with_action");

        Map<String, Object> params = new HashMap<String, Object>();
        params.put("aid", aid);

        DataResult dr = m.execute(params);
        dr.setTotalSize(dr.size());
        return dr;
    }

    /**
     * Returns the list of details associated with a config file upload action.
     * @param aid The action id for the action in question
     * @return Return a list containing the errata for the action.
     */
    public static DataResult getConfigFileUploadList(Long aid) {
        SelectMode m = ModeFactory.getMode("config_queries", "upload_action_status");

        Map<String, Object> params = new HashMap<String, Object>();
        params.put("aid", aid);

        DataResult dr = m.execute(params);
        dr.setTotalSize(dr.size());
        return dr;
    }

    /**
     * Returns the list of details associated with a config file deploy action.
     * @param aid The action id for the action in question
     * @return Return a list containing the details for the action.
     */
    public static DataResult getConfigFileDeployList(Long aid) {
        SelectMode m = ModeFactory.getMode("config_queries", "config_action_revisions");

        Map<String, Object> params = new HashMap<String, Object>();
        params.put("aid", aid);

        DataResult dr = m.execute(params);
        dr.setTotalSize(dr.size());
        return dr;
    }

    /**
     * Returns the list of details associated with a config file diff action.
     * @param aid The action id for the action in question
     * @return Return a list containing the details for the action.
     */
    public static DataResult getConfigFileDiffList(Long aid) {
        SelectMode m = ModeFactory.getMode("config_queries", "diff_action_revisions");

        Map<String, Object> params = new HashMap<String, Object>();
        params.put("aid", aid);

        DataResult dr = m.execute(params);
        dr.setTotalSize(dr.size());
        return dr;
    }

    /**
     * Retrieves the systems that have completed a given action
     * @param user The user in question.
     * @param action The Action.
     * @param pc The PageControl.
     * @param mode The DataSource mode to run
     * @return Returns list containing the completed systems.
     */
    private static DataResult getActionSystems(User user,
            Action action,
            PageControl pc,
            String mode) {

        SelectMode m = ModeFactory.getMode("System_queries", mode);
        Map<String, Object> params = new HashMap<String, Object>();
        params.put("org_id", user.getOrg().getId());
        params.put("aid", action.getId());
        params.put("user_id", user.getId());
        if (pc != null) {
            return makeDataResult(params, params, pc, m);
        }
        DataResult dr = m.execute(params);
        dr.setTotalSize(dr.size());
        return dr;
    }

    /**
     * Retrieves the systems that have completed a given action
     * @param user The user in question.
     * @param action The Action.
     * @param pc The PageControl.
     * @return Returns list containing the completed systems.
     */
    public static DataResult completedSystems(User user,
            Action action,
            PageControl pc) {

        return getActionSystems(user, action, pc, "systems_completed_action");
    }

    /**
     * Retrieves the systems that are in the process of completing
     * a given action
     * @param user The user in question.
     * @param action The Action.
     * @param pc The PageControl.
     * @return Returns list containing the completed systems.
     */
    public static DataResult inProgressSystems(User user,
            Action action,
            PageControl pc) {

        return getActionSystems(user, action, pc, "systems_in_progress_action");
    }

    /**
     * Retrieves the systems that failed completing
     * a given action
     * @param user The user in question.
     * @param action The Action.
     * @param pc The PageControl.
     * @return Returns list containing the completed systems.
     */
    public static DataResult failedSystems(User user,
            Action action,
            PageControl pc) {

        return getActionSystems(user, action, pc, "systems_failed_action");
    }

    /**
     * Schedules a package list refresh action for the given server.
     * @param scheduler User scheduling the action.
     * @param server Server for which the action affects.
     * @return The scheduled PackageAction
     * @throws TaskomaticApiException if there was a Taskomatic error
     * (typically: Taskomatic is down)
     */
    public static PackageAction schedulePackageRefresh(User scheduler, Server server)
        throws TaskomaticApiException {
        return (schedulePackageRefresh(scheduler, server, new Date()));
    }

    /**
     * Schedules a package list refresh action for the given server.
     * @param scheduler User scheduling the action.
     * @param server Server for which the action affects.
     * @param earliest The earliest time this action should be run.
     * @return The scheduled PackageAction
     * @throws TaskomaticApiException if there was a Taskomatic error
     * (typically: Taskomatic is down)
     * @throws MissingEntitlementException if the server is not entitled
     */
    public static PackageAction schedulePackageRefresh(User scheduler, Server server,
            Date earliest) throws TaskomaticApiException {
        checkSaltOrManagementEntitlement(server.getId());

        PackageAction pa = (PackageAction) schedulePackageAction(scheduler,
                (List) null, ActionFactory.TYPE_PACKAGES_REFRESH_LIST, earliest, server);
        storeAction(pa);
        return pa;
    }

    /**
     * Schedule a package list refresh without a user.
     *
     * @param schedulerOrg the organization the server belongs to
     * @param server the server
     * @return the scheduled PackageRefreshListAction
     * @throws TaskomaticApiException if there was a Taskomatic error
     * (typically: Taskomatic is down)
     */
    public static PackageAction schedulePackageRefresh(Org schedulerOrg, Server server)
        throws TaskomaticApiException {
        checkSaltOrManagementEntitlement(server.getId());

        Action action = ActionFactory.createAction(
                ActionFactory.TYPE_PACKAGES_REFRESH_LIST);
        action.setName(ActionFactory.TYPE_PACKAGES_REFRESH_LIST.getName());
        action.setOrg(schedulerOrg);
        action.setSchedulerUser(null);
        action.setEarliestAction(new Date());

        ServerAction sa = new ServerAction();
        sa.setStatus(ActionFactory.STATUS_QUEUED);
        sa.setRemainingTries(REMAINING_TRIES);
        sa.setServer(server);
        action.addServerAction(sa);
        sa.setParentAction(action);

        ActionFactory.save(action);
        taskomaticApi.scheduleActionExecution(action);
        return (PackageAction) action;
    }

    /**
     * Schedules a package runtransaction action.
     * @param scheduler User scheduling the action.
     * @param server Server for which the action affects.
     * @param pkgs List of PackageMetadata's to be run.
     * @param earliest The earliest time this action should be run.
     * @return The scheduled PackageAction
     * @throws TaskomaticApiException if there was a Taskomatic error
     * (typically: Taskomatic is down)
     */
    public static PackageAction schedulePackageRunTransaction(User scheduler,
            Server server, List pkgs, Date earliest) throws TaskomaticApiException {

        if (pkgs == null || pkgs.isEmpty()) {
            return null;
        }

        Action action = scheduleAction(scheduler, server,
                ActionFactory.TYPE_PACKAGES_RUNTRANSACTION,
                "Package Synchronization", new Date());
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
        for (Iterator itr = pkgs.iterator(); itr.hasNext();) {
            PackageMetadata pm = (PackageMetadata) itr.next();
            Map<String, Object> params = new HashMap<String, Object>();
            params.put("delta_id", pd.getId());
            if (pm.getComparisonAsInt() == PackageMetadata.KEY_THIS_ONLY) {

                if (log.isDebugEnabled()) {
                    log.debug("compare returned [KEY_THIS_ONLY]; " +
                            "deleting package from system");
                }

                params.put("operation", ActionFactory.TXN_OPERATION_DELETE);
                params.put("n", pm.getName());
                params.put("v", pm.getSystem().getVersion());
                params.put("r", pm.getSystem().getRelease());
                String epoch = pm.getSystem().getEpoch();
                params.put("e", "".equals(epoch) ? null : epoch);
                params.put("a", pm.getSystem().getArch() != null ?
                        pm.getSystem().getArch() : "");
                m.executeUpdate(params);
            }
            else if (pm.getComparisonAsInt() == PackageMetadata.KEY_OTHER_ONLY) {

                if (log.isDebugEnabled()) {
                    log.debug("compare returned [KEY_OTHER_ONLY]; " +
                            "installing package to system: " +
                            pm.getName() + "-" + pm.getOtherEvr());
                }

                params.put("operation", ActionFactory.TXN_OPERATION_INSERT);
                params.put("n", pm.getName());
                params.put("v", pm.getOther().getVersion());
                params.put("r", pm.getOther().getRelease());
                String epoch = pm.getOther().getEpoch();
                params.put("e", StringUtils.isEmpty(epoch) ? null : epoch);
                params.put("a", pm.getOther().getArch() != null ?
                        pm.getOther().getArch() : "");
                m.executeUpdate(params);

            }
            else if (pm.getComparisonAsInt() == PackageMetadata.KEY_THIS_NEWER ||
                    pm.getComparisonAsInt() == PackageMetadata.KEY_OTHER_NEWER) {

                if (log.isDebugEnabled()) {
                    log.debug("compare returned [KEY_THIS_NEWER OR KEY_OTHER_NEWER]; " +
                            "deleting package ["  + pm.getName() + "-" +
                            pm.getSystemEvr() + "] from system " +
                            "installing package ["  + pm.getName() + "-" +
                            pm.getOther().getEvr() + "] to system");
                }

                String epoch;
                if (isPackageRemovable(pm.getName())) {
                    params.put("operation", ActionFactory.TXN_OPERATION_DELETE);
                    params.put("n", pm.getName());
                    params.put("v", pm.getSystem().getVersion());
                    params.put("r", pm.getSystem().getRelease());
                    epoch = pm.getSystem().getEpoch();
                    params.put("e", epoch.equals("") ? null : epoch);
                    params.put("a", pm.getSystem().getArch() != null ?
                            pm.getOther().getArch() : "");
                    m.executeUpdate(params);
                }

                params.put("operation", ActionFactory.TXN_OPERATION_INSERT);
                params.put("n", pm.getName());
                params.put("v", pm.getOther().getVersion());
                params.put("r", pm.getOther().getRelease());
                epoch = pm.getOther().getEpoch();
                params.put("e", epoch.equals("") ? null : epoch);
                params.put("a", pm.getOther().getArch() != null ?
                        pm.getOther().getArch() : "");
                m.executeUpdate(params);
            }
        }

        // this is SOOOO WRONG, we need to get rid of DataSource
        m = ModeFactory.getWriteMode("Action_queries",
                "insert_action_package_delta");
        Map<String, Object> params = new HashMap<String, Object>();
        params.put("action_id", action.getId());
        params.put("delta_id", pd.getId());
        m.executeUpdate(params);

        return (PackageAction) action;
    }

    // Check if we want to delete the old package when installing  a
    // new rev of one.
    private static boolean isPackageRemovable(String name) {
        for (int i = 0; i < PACKAGES_NOT_REMOVABLE.length; i++) {
            log.debug("Checking: " + name + " for: " + PACKAGES_NOT_REMOVABLE[i]);
            if (name.equals(PACKAGES_NOT_REMOVABLE[i])) {
                return false;
            }
        }
        return true;
    }

    /**
     * Schedules one or more package removal actions for the given server.
     * @param scheduler User scheduling the action.
     * @param srvr Server for which the action affects.
     * @param pkgs The set of packages to be removed.
     * @param earliestAction Date of earliest action to be executed
     * @return Currently scheduled PackageAction
     * @throws TaskomaticApiException if there was a Taskomatic error
     * (typically: Taskomatic is down)
     */
    public static PackageAction schedulePackageRemoval(User scheduler,
            Server srvr, RhnSet pkgs, Date earliestAction) throws TaskomaticApiException {
        return (PackageAction) schedulePackageAction(scheduler, srvr, pkgs,
                ActionFactory.TYPE_PACKAGES_REMOVE, earliestAction);
    }

    /**
     * Schedules one or more package removal actions for the given server.
     * @param scheduler User scheduling the action.
     * @param srvr Server for which the action affects.
     * @param pkgs The list of packages to be removed.
     * @param earliestAction Date of earliest action to be executed
     * @return Currently scheduled PackageAction
     * @throws TaskomaticApiException if there was a Taskomatic error
     * (typically: Taskomatic is down)
     */
    public static PackageAction schedulePackageRemoval(User scheduler, Server srvr,
            List<Map<String, Long>> pkgs, Date earliestAction)
        throws TaskomaticApiException {
        return (PackageAction) schedulePackageAction(scheduler, pkgs,
                ActionFactory.TYPE_PACKAGES_REMOVE, earliestAction, srvr);
    }

    /**
     * Schedules one or more package installation actions for the given server.
     * @param scheduler User scheduling the action.
     * @param srvr Server for which the action affects.
     * @param pkgs The set of packages to be removed.
     * @param earliestAction Date of earliest action to be executed
     * @return Currently scheduled PackageAction
     * @throws TaskomaticApiException if there was a Taskomatic error
     * (typically: Taskomatic is down)
     */
    public static PackageAction schedulePackageInstall(User scheduler,
            Server srvr, List pkgs, Date earliestAction) throws TaskomaticApiException {
        return (PackageAction) schedulePackageAction(scheduler, pkgs,
                ActionFactory.TYPE_PACKAGES_UPDATE, earliestAction, srvr);
    }

    /**
     * Schedules one or more package verification actions for the given server.
     * @param scheduler User scheduling the action.
     * @param srvr Server for which the action affects.
     * @param pkgs The set of packages to be removed.
     * @param earliest Earliest occurrence of the script.
     * @return Currently scheduled PackageAction
     * @throws TaskomaticApiException if there was a Taskomatic error
     * (typically: Taskomatic is down)
     */
    public static PackageAction schedulePackageVerify(User scheduler,
            Server srvr, RhnSet pkgs, Date earliest) throws TaskomaticApiException {
        return (PackageAction) schedulePackageAction(scheduler, srvr, pkgs,
                ActionFactory.TYPE_PACKAGES_VERIFY, earliest);
    }

    /**
     * Schedules one or more package lock actions for the given server.
     * @param scheduler the scheduler
     * @param server the server
     * @param packages set of packages
     * @param earliest earliest occurrence of this action
     * @return Currently scheduled PackageAction
     * @throws TaskomaticApiException if there was a Taskomatic error
     * (typically: Taskomatic is down)
     */
    public static Action schedulePackageLock(User scheduler, Server server,
            Set<Package> packages, Date earliest)
        throws TaskomaticApiException {
        List<Map<String, Long>> packagesList = new ArrayList<Map<String, Long>>();
        for (Package pkg : packages) {
            Map<String, Long> pkgMeta = new HashMap<String, Long>();
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
            server
        );
    }

    /**
     * Schedules a script action for the given servers
     *
     * @param scheduler User scheduling the action.
     * @param sids Servers for which the action affects.
     * @param script The set of packages to be removed.
     * @param name Name of Script action.
     * @param earliest Earliest occurrence of the script.
     * @return Currently scheduled ScriptRunAction
     * @throws TaskomaticApiException if there was a Taskomatic error
     * (typically: Taskomatic is down)
     * @throws MissingCapabilityException if any server in the list is missing script.run;
     *             schedule fails
     */
    public static ScriptRunAction scheduleScriptRun(User scheduler, List<Long> sids,
            String name, ScriptActionDetails script, Date earliest)
        throws TaskomaticApiException {

        checkScriptingOnServers(sids);

        Set<Long> sidSet = new HashSet<Long>();
        sidSet.addAll(sids);
        ScriptRunAction sra = (ScriptRunAction) scheduleAction(scheduler,
                ActionFactory.TYPE_SCRIPT_RUN, name, earliest, sidSet);
        sra.setScriptActionDetails(script);
        ActionFactory.save(sra);
        taskomaticApi.scheduleActionExecution(sra);
        return sra;
    }

    /**
     * Checks that ScriptRunActions can be run on the servers with specified
     * IDs.
     * @param sids servers' ids
     * @throws MissingCapabilityException if scripts cannot be run
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
     * Creates a ScriptActionDetails which contains an arbitrary script to be
     * run by a ScriptRunAction.
     * @param username Username of script
     * @param groupname Group script runs as
     * @param script Script contents
     * @param timeout script timeout
     * @return ScriptActionDetails containing script to be run by ScriptRunAction
     */
    public static ScriptActionDetails createScript(String username,
            String groupname, Long timeout, String script) {

        return ActionFactory.createScriptActionDetails(username, groupname,
                timeout, script);
    }

    private static Action scheduleAction(User scheduler, ActionType type, String name,
            Date earliestAction, Set<Long> serverIds) {
        Action action = createAction(scheduler, type, name, earliestAction);
        scheduleForExecution(action, serverIds);

        return action;
    }

    /**
     * Schedules an action for execution on one or more servers (adding rows to
     * rhnServerAction)
     * @param action the action
     * @param serverIds server IDs
     */
    public static void scheduleForExecution(Action action, Set<Long> serverIds) {
        Map<String, Object> params = new HashMap<String, Object>();
        params.put("status_id", ActionFactory.STATUS_QUEUED.getId());
        params.put("tries", REMAINING_TRIES);
        params.put("parent_id", action.getId());

        WriteMode m = ModeFactory.getWriteMode("Action_queries", "insert_server_actions");
        List<Long> sidList = new ArrayList<Long>();
        sidList.addAll(serverIds);
        m.executeUpdate(params, sidList);
    }

    /**
     * Creates, saves and returns a new Action
     * @param user the user who created this action
     * @param type the action type
     * @param name the action name
     * @param earliestAction the earliest execution date
     * @return a saved Action
     */
    public static Action createAction(User user, ActionType type, String name,
        Date earliestAction) {
        /**
         * We have to relookup the type here, because most likely a static final variable
         *  was passed in.  If we use this and the .reload() gets called below
         *  if we try to save a new action the instace of the type in the cache
         *  will be different than the final static variable
         *  sometimes hibernate is no fun
         */
        ActionType lookedUpType = ActionFactory.lookupActionTypeByLabel(type.getLabel());
        Action action = createScheduledAction(user, lookedUpType, name, earliestAction);
        ActionFactory.save(action);
        ActionFactory.getSession().flush();
        return action;
    }

    private static Action scheduleAction(User scheduler, Server srvr,
            ActionType type, String name, Date earliestAction) {

        Action action = createScheduledAction(scheduler, type, name, earliestAction);

        ServerAction sa = new ServerAction();
        sa.setStatus(ActionFactory.STATUS_QUEUED);
        sa.setRemainingTries(REMAINING_TRIES);
        sa.setServer(srvr);

        action.addServerAction(sa);
        sa.setParentAction(action);

        return action;
    }

    private static Action createScheduledAction(User scheduler, ActionType type,
            String name, Date earliestAction) {
        Action pa = ActionFactory.createAction(type);
        pa.setName(name);
        pa.setOrg(scheduler.getOrg());
        pa.setSchedulerUser(scheduler);
        pa.setEarliestAction(earliestAction);
        return pa;
    }

    /**
     * Schedule a KickstartAction against a system
     * @param ksdata KickstartData to associate with this Action
     * @param scheduler User scheduling the action.
     * @param srvr Server for which the action affects.
     * @param earliestAction Date run the Action
     * @param appendString extra options to add to the action.
     * @param kickstartHost host that serves up the kickstart file.
     * @return Currently scheduled KickstartAction
     */
    public static KickstartAction scheduleKickstartAction(
            KickstartData ksdata, User scheduler, Server srvr,
            Date earliestAction, String appendString, String kickstartHost) {
        if (log.isDebugEnabled()) {
            log.debug("scheduleKickstartAction(KickstartData ksdata=" + ksdata +
                    ", User scheduler=" + scheduler + ", Server srvr=" + srvr +
                    ", Date earliestAction=" + earliestAction +
                    ", String appendString=" + appendString +
                    ", String kickstartHost=" + kickstartHost + ") - start");
        }

        return scheduleKickstartAction(ksdata.getPreserveFileLists(), scheduler, srvr,
                earliestAction, appendString, kickstartHost);

    }

    /**
     * Schedule a KickstartAction against a system
     * @param fileList file preservation lists to be included in the system records.
     * @param scheduler User scheduling the action.
     * @param srvr Server for which the action affects.
     * @param earliestAction Date run the Action
     * @param appendString extra options to add to the action.
     * @param kickstartHost host that serves up the kickstart file.
     * @return Currently scheduled KickstartAction
     */
    public static KickstartAction scheduleKickstartAction(
            Set<FileList> fileList, User scheduler, Server srvr,
            Date earliestAction, String appendString, String kickstartHost) {
        if (log.isDebugEnabled()) {
            log.debug("scheduleKickstartAction(" +
                    ", User scheduler=" + scheduler + ", Server srvr=" + srvr +
                    ", Date earliestAction=" + earliestAction +
                    ", String appendString=" + appendString +
                    ", String kickstartHost=" + kickstartHost + ") - start");
        }

        KickstartAction ksaction = (KickstartAction) scheduleAction(scheduler, srvr,
                ActionFactory.TYPE_KICKSTART_INITIATE,
                ActionFactory.TYPE_KICKSTART_INITIATE.getName(),
                earliestAction);
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
     * @param pcmd most information needed to create this action
     * @param ksSessionId Kickstart Session ID to associate with this action
     * @return Currently scheduled KickstartAction
     */
    public static KickstartGuestAction scheduleKickstartGuestAction(
            ProvisionVirtualInstanceCommand pcmd,
            Long ksSessionId
            ) {

        KickstartGuestAction ksAction = (KickstartGuestAction)
                scheduleAction(pcmd.getUser(),
                        pcmd.getHostServer(),
                        ActionFactory.TYPE_KICKSTART_INITIATE_GUEST,
                        ActionFactory.TYPE_KICKSTART_INITIATE_GUEST.getName(),
                        pcmd.getScheduleDate());
        KickstartGuestActionDetails kad = new KickstartGuestActionDetails();
        kad.setAppendString(pcmd.getExtraOptions());
        kad.setParentAction(ksAction);

        kad.setDiskGb(pcmd.getLocalStorageSize());
        kad.setMemMb(pcmd.getMemoryAllocation().longValue());
        kad.setDiskPath(pcmd.getFilePath());
        kad.setVcpus(Long.valueOf(pcmd.getVirtualCpus()));
        kad.setGuestName(pcmd.getGuestName());
        kad.setMacAddress(pcmd.getMacAddress());
        kad.setKickstartSessionId(ksSessionId);

        Profile cProfile = Profile.lookupById(CobblerXMLRPCHelper.getConnection(
                pcmd.getUser()), pcmd.getKsdata().getCobblerId());
        if (pcmd.getVirtBridge() == null) {
            kad.setVirtBridge(cProfile.getVirtBridge());
        }
        else {
            kad.setVirtBridge(pcmd.getVirtBridge());
        }

        CobblerVirtualSystemCommand vcmd = new CobblerVirtualSystemCommand(
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
     * @param scheduler User scheduling the action.
     * @param srvr Server for which the action affects.
     * @param earliestAction Date run the Action
     * @return Currently scheduled KickstartAction
     */
    public static Action scheduleRebootAction(User scheduler, Server srvr,
            Date earliestAction) {
        return scheduleAction(scheduler, srvr, ActionFactory.TYPE_REBOOT,
                ActionFactory.TYPE_REBOOT.getName(), earliestAction);
    }

    /**
     * Schedule a scheduleHardwareRefreshAction against a system
     * @param scheduler User scheduling the action.
     * @param srvr Server for which the action affects.
     * @param earliestAction Date run the Action
     * @return Currently scheduled KickstartAction
     * @throws MissingCapabilityException if scripts cannot be run
     */
    public static Action scheduleHardwareRefreshAction(User scheduler, Server srvr,
            Date earliestAction) {
        checkSaltOrManagementEntitlement(srvr.getId());
        return scheduleAction(scheduler, srvr, ActionFactory.TYPE_HARDWARE_REFRESH_LIST,
                ActionFactory.TYPE_HARDWARE_REFRESH_LIST.getName(), earliestAction);
    }

    /**
     * Schedule a scheduleHardwareRefreshAction against a system or systems
     * @param scheduler User scheduling the action.
     * @param earliestAction Date run the Action
     * @param serverIds server ids meant for the action
     * @return Currently scheduled KickstartAction
     * @throws MissingCapabilityException if scripts cannot be run
     */
    public static Action scheduleHardwareRefreshAction(User scheduler, Date earliestAction,
            Set<Long> serverIds) {
        for (Long sid : serverIds) {
            Server s = SystemManager.lookupByIdAndUser(sid, scheduler);
            checkSaltOrManagementEntitlement(sid);
        }
        return scheduleAction(scheduler, ActionFactory.TYPE_HARDWARE_REFRESH_LIST,
                ActionFactory.TYPE_HARDWARE_REFRESH_LIST.getName(), earliestAction,
                serverIds);
    }

    /**
     * Schedule a HardwareRefreshAction without a user.
     * @param schedulerOrg the org scheduling the action.
     * @param srvr Server for which the action affects.
     * @param earliestAction Date run the Action
     * @return Currently scheduled HardwareRefreshAction
     * @throws TaskomaticApiException if there was a Taskomatic error
     * (typically: Taskomatic is down)
     * @throws MissingCapabilityException if scripts cannot be run
     */
    public static Action scheduleHardwareRefreshAction(Org schedulerOrg, Server srvr,
            Date earliestAction)
        throws TaskomaticApiException {
        checkSaltOrManagementEntitlement(srvr.getId());

        Action action = ActionFactory
                .createAction(ActionFactory.TYPE_HARDWARE_REFRESH_LIST);
        action.setName(ActionFactory.TYPE_HARDWARE_REFRESH_LIST.getName());
        action.setOrg(schedulerOrg);
        action.setSchedulerUser(null);
        action.setEarliestAction(earliestAction);

        ServerAction sa = new ServerAction();
        sa.setStatus(ActionFactory.STATUS_QUEUED);
        sa.setRemainingTries(REMAINING_TRIES);
        sa.setServer(srvr);

        action.addServerAction(sa);
        sa.setParentAction(action);

        ActionFactory.save(action);

        taskomaticApi.scheduleActionExecution(action);
        return action;
    }

    private static void checkSaltOrManagementEntitlement(Long sid) {
        if (!SystemManager.hasEntitlement(sid, EntitlementManager.MANAGEMENT) &&
                !SystemManager.hasEntitlement(sid, EntitlementManager.SALT)) {
            log.error("Unable to run action on a system without either Salt or " +
                    "Management entitlement, id " + sid);
            throw new MissingEntitlementException(
                    EntitlementManager.MANAGEMENT.getHumanReadableLabel() + " or " +
                    EntitlementManager.SALT.getHumanReadableLabel()
            );
        }
    }

    /**
     * Schedules all Errata for the given system.
     * @param scheduler Person scheduling the action.
     * @param srvr Server whose errata is going to be scheduled.
     * @param earliest Earliest possible time action will occur.
     * @throws TaskomaticApiException if there was a Taskomatic error
     * (typically: Taskomatic is down)
     */
    public static void scheduleAllErrataUpdate(User scheduler, Server srvr,
            Date earliest) throws TaskomaticApiException {
        // Do not elaborate, we need only the IDs in here
        DataResult<Errata> errata = SystemManager.unscheduledErrata(scheduler,
                srvr.getId(), null);
        List<Long> errataIds = new ArrayList<Long>();
        for (Errata e : errata) {
            errataIds.add(e.getId());
        }
        List<Long> serverIds = Arrays.asList(srvr.getId());
        ErrataManager.applyErrata(scheduler, errataIds, earliest, serverIds);
    }

    /**
     * Schedules an install of a package
     * @param scheduler The user scheduling the action.
     * @param srvr The server that this action is for.
     * @param nameId nameId rhnPackage.name_id
     * @param evrId evrId of package
     * @param archId archId of package
     * @return The action that has been scheduled.
     * @throws TaskomaticApiException if there was a Taskomatic error
     * (typically: Taskomatic is down)
     */
    public static Action schedulePackageInstall(User scheduler, Server srvr,
            Long nameId, Long evrId, Long archId) throws TaskomaticApiException {
        List packages = new LinkedList();
        Map row = new HashMap();
        row.put("name_id", nameId);
        row.put("evr_id", evrId);
        row.put("arch_id", archId);
        packages.add(row);
        return schedulePackageInstall(scheduler, srvr, packages, new Date());
    }

    /**
     * Schedules install of a packages on multiple servers
     * @param scheduler The user scheduling the action.
     * @param pkgs Set of packages to install
     * @param server The server that this action is for.
     * @param earliestAction The earliest time that this action could happen.
     * @return The action that has been scheduled.
     * @throws TaskomaticApiException if there was a Taskomatic error
     * (typically: Taskomatic is down)
     */
    public static Action schedulePackageInstall(User scheduler, List<Package> pkgs,
            Server server, Date earliestAction)
        throws TaskomaticApiException {
        if (pkgs.isEmpty()) {
            return null;
        }
        List packages = new LinkedList();
        for (Package pkg : pkgs) {
            Map row = new HashMap();
            row.put("name_id", pkg.getPackageName().getId());
            row.put("evr_id", pkg.getPackageEvr().getId());
            row.put("arch_id", pkg.getPackageArch().getId());
            packages.add(row);
        }
        Set<Long> serverIds = new HashSet<Long>();
        serverIds.add(server.getId());

        return schedulePackageAction(scheduler, packages,
                ActionFactory.TYPE_PACKAGES_UPDATE, earliestAction, serverIds);
    }

    /**
     * Schedules a package action of the given type for the given server with the
     * packages given as a list.
     * @param scheduler The user scheduling the action.
     * @param pkgs A list of maps containing keys 'name_id', 'evr_id' and
     *             optional 'arch_id' with Long values.
     * @param type The type of the package action.  One of the static types found in
     *             ActionFactory
     * @param earliestAction The earliest time that this action could happen.
     * @param servers The server(s) that this action is for.
     * @return The action that has been scheduled.
     * @throws TaskomaticApiException if there was a Taskomatic error
     * (typically: Taskomatic is down)
     */
    public static Action schedulePackageAction(User scheduler,
            List pkgs,
            ActionType type,
            Date earliestAction,
            Server...servers) throws TaskomaticApiException {
        Set<Long> serverIds = new HashSet<Long>();
        for (Server s : servers) {
            serverIds.add(s.getId());
        }

        return schedulePackageAction(scheduler, pkgs, type, earliestAction, serverIds);
    }

    /**
     * Schedules a package action of the given type for the given server with the
     * packages given as a list.
     * @param scheduler The user scheduling the action.
     * @param pkgs A list of maps containing keys 'name_id', 'evr_id' and
     *             optional 'arch_id' with Long values.
     * @param type The type of the package action.  One of the static types found in
     *             ActionFactory
     * @param earliestAction The earliest time that this action could happen.
     * @param serverIds The server ids that this action is for.
     * @return The action that has been scheduled.
     * @throws TaskomaticApiException if there was a Taskomatic error
     * (typically: Taskomatic is down)
     */
    public static Action schedulePackageAction(User scheduler, List pkgs, ActionType type,
            Date earliestAction, Set<Long> serverIds)
        throws TaskomaticApiException {

        String name = getActionName(type);

        Action action = scheduleAction(scheduler, type, name, earliestAction, serverIds);
        ActionFactory.save(action);

        addPackageActionDetails(Arrays.asList(action), pkgs);
        taskomaticApi.scheduleActionExecution(action);
        if (ActionFactory.TYPE_PACKAGES_UPDATE.equals(type)) {
            MinionActionManager.scheduleStagingJobsForMinions(singletonList(action), scheduler);
        }

        return action;
    }

    /**
     * Adds package details to some Actions
     * @param actions the actions
     * @param packageMaps A list of maps containing keys 'name_id', 'evr_id' and
     *            optional 'arch_id' with Long values.
     */
    public static void addPackageActionDetails(Collection<Action> actions,
            List<Map<String, Long>> packageMaps) {
        if (packageMaps != null) {
            List<Map<String, Object>> paramList =
                actions.stream().flatMap(action -> {
                    String packageParameter = getPackageParameter(action);
                    return packageMaps.stream().map(packageMap -> {
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
     * Returns the pkg_parameter parameter to the schedule_action queries in
     * Action_queries.xml
     * @param action the action
     * @return a parameter value
     */
    private static String getPackageParameter(Action action) {
        if (action.getActionType().equals(ActionFactory.TYPE_PACKAGES_LOCK)) {
            return "lock";
        }
        return "upgrade";
    }

    /**
     * Returns a name string from an Action type
     * @param type the type
     * @return a name
     */
    public static String getActionName(ActionType type) {
        String name = "";
        if (type.equals(ActionFactory.TYPE_PACKAGES_REMOVE)) {
            name = "Package Removal";
        }
        else if (type.equals(ActionFactory.TYPE_PACKAGES_UPDATE)) {
            name = "Package Install/Upgrade";
        }
        else if (type.equals(ActionFactory.TYPE_PACKAGES_VERIFY)) {
            name = "Package Verify";
        }
        else if (type.equals(ActionFactory.TYPE_PACKAGES_REFRESH_LIST)) {
            name = "Package List Refresh";
        }
        else if (type.equals(ActionFactory.TYPE_PACKAGES_DELTA)) {
            name = "Package Synchronization";
        }
        else if (type.equals(ActionFactory.TYPE_PACKAGES_LOCK)) {
            name = "Lock packages";
        }
        return name;
    }

    /**
     * Schedules the appropriate package action
     * @param scheduler User scheduling the action.
     * @param srvr Server for which the action affects.
     * @param pkgs The set of packages to be removed.
     * @param type The Action Type
     * @param earliestAction Date of earliest action to be executed
     * @return scheduled Package Action
     * @throws TaskomaticApiException if there was a Taskomatic error
     * (typically: Taskomatic is down)
     */
    private static Action schedulePackageAction(User scheduler, Server srvr, RhnSet pkgs,
            ActionType type, Date earliestAction)
        throws TaskomaticApiException {

        List packages = new LinkedList();
        Iterator i = pkgs.getElements().iterator();
        while (i.hasNext()) {
            RhnSetElement rse = (RhnSetElement) i.next();
            Map row = new HashMap();
            row.put("name_id", rse.getElement());
            row.put("evr_id", rse.getElementTwo());
            row.put("arch_id", rse.getElementThree());
            // bugzilla: 191000, we forgot to populate the damn LinkedList :(
            packages.add(row);
        }
        return schedulePackageAction(scheduler, packages, type, earliestAction, srvr
                );
    }

    /**
     * Schedules Xccdf evaluation.
     * @param scheduler User scheduling the action.
     * @param srvr Server for which the action affects.
     * @param path Path for the Xccdf content.
     * @param parameters Additional parameters for oscap tool.
     * @param earliestAction Date of earliest action to be executed.
     * @return scheduled Scap Action
     * @throws TaskomaticApiException if there was a Taskomatic error
     * (typically: Taskomatic is down)
     */
    public static ScapAction scheduleXccdfEval(User scheduler, Server srvr, String path,
            String parameters, Date earliestAction) throws TaskomaticApiException {
        Set<Long> serverIds = new HashSet<Long>();
        serverIds.add(srvr.getId());
        return scheduleXccdfEval(scheduler, serverIds, path, parameters, earliestAction);
    }

    /**
     * Schedules Xccdf evaluation.
     * @param scheduler User scheduling the action.
     * @param serverIds Set of server identifiers for which the action affects.
     * @param path Path for the Xccdf content.
     * @param parameters Additional parameters for oscap tool.
     * @param earliestAction Date of earliest action to be executed.
     * @return scheduled Scap Action
     * @throws TaskomaticApiException if there was a Taskomatic error
     * (typically: Taskomatic is down)
     * @throws MissingCapabilityException if scripts cannot be run
     */
    public static ScapAction scheduleXccdfEval(User scheduler, Set<Long> serverIds,
            String path, String parameters, Date earliestAction)
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

        ScapActionDetails scapDetails = new ScapActionDetails(path, parameters);
        ScapAction action = (ScapAction) scheduleAction(scheduler,
                ActionFactory.TYPE_SCAP_XCCDF_EVAL,
                ActionFactory.TYPE_SCAP_XCCDF_EVAL.getName(),
                earliestAction, serverIds);
        action.setScapActionDetails(scapDetails);
        ActionFactory.save(action);
        taskomaticApi.scheduleActionExecution(action);
        return action;
    }

    /**
     * Schedule machine reboot.
     *
     * @param scheduler Logged in user
     * @param server Server, which is going to be rebooted
     * @param earliestAction Earliest date. If null, then date is current.
     * @return scheduled reboot action
     * @throws TaskomaticApiException if there was a Taskomatic error
     * (typically: Taskomatic is down)
     */
    public static Action scheduleReboot(User scheduler, Server server, Date earliestAction)
        throws TaskomaticApiException {
        Action action = ActionManager.scheduleAction(scheduler,
                                                     server,
                                                     ActionFactory.TYPE_REBOOT,
                                                     ActionFactory.TYPE_REBOOT.getName(),
                                                     (earliestAction == null ?
                                                      new Date() :
                                                      earliestAction));
        ActionFactory.save(action);
        taskomaticApi.scheduleActionExecution(action);
        return action;
    }

    /**
     * scheduleCertificateUpdate
     * @param scheduler Logged in user
     * @param server Server, to update the certificate for
     * @param earliestAction Earliest date. If null, use current date
     * @return scheduled certificate update action
     * @throws TaskomaticApiException if there was a Taskomatic error
     * (typically: Taskomatic is down)
     */
    public static Action scheduleCertificateUpdate(User scheduler, Server server,
            Date earliestAction)
        throws TaskomaticApiException {
        if (!SystemManager.clientCapable(server.getId(), "clientcert.update_client_cert")) {
            throw new MissingCapabilityException("spacewalk-client-cert", server);
        }

        Action action = ActionManager.scheduleAction(scheduler,
                        server,
                        ActionFactory.TYPE_CLIENTCERT_UPDATE_CLIENT_CERT,
                        ActionFactory.TYPE_CLIENTCERT_UPDATE_CLIENT_CERT.getName(),
                        (earliestAction == null ? new Date() : earliestAction));
        ActionFactory.save(action);
        taskomaticApi.scheduleActionExecution(action);
        return action;
    }

    /**
     * Schedule a distribution upgrade.
     *
     * @param scheduler user who scheduled this action
     * @param server server
     * @param details action details
     * @param earliestAction date of earliest action
     * @return the scheduled action
     * @throws TaskomaticApiException if there was a Taskomatic error
     * (typically: Taskomatic is down)
     */
    public static DistUpgradeAction scheduleDistUpgrade(User scheduler, Server server,
            DistUpgradeActionDetails details, Date earliestAction)
        throws TaskomaticApiException {
        // Construct the action name
        String name = ActionFactory.TYPE_DIST_UPGRADE.getName();
        if (details.isDryRun()) {
            name += " (Dry Run)";
        }

        // Schedule the main action
        DistUpgradeAction action = (DistUpgradeAction) scheduleAction(scheduler, server,
                ActionFactory.TYPE_DIST_UPGRADE, name, earliestAction);

        // Add the details and save
        action.setDetails(details);
        ActionFactory.save(action);
        taskomaticApi.scheduleActionExecution(action, !details.isDryRun());
        return action;
    }
    /**
     * Schedule an action for channel state on the specified list of servers
     * @param user User with permission to schedule an action
     * @param minionServers  servers where channel state should be applied to
     * @return Set of scheduled Actions
     * @throws TaskomaticApiException if there was a Taskomatic error
     * (typically: Taskomatic is down)
     */
    public static Action scheduleChannelState(User user, List<MinionServer> minionServers)
            throws TaskomaticApiException {
        List<String> states = Collections.singletonList("channels");
        List<Long> sids = minionServers.stream().map(ms->ms.getId()).collect(toList());
        Action action = scheduleApplyStates(user, sids, states, new Date());
        taskomaticApi.scheduleActionExecution(action);
        return action;
    }
    /**
     * Schedule application of the highstate.
     *
     * @param scheduler the user who is scheduling
     * @param sids list of server ids
     * @param earliest action will not be executed before this date
     * @param test run states in test-only mode
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
     * @param sids list of server ids
     * @param mods list of state modules to be applied
     * @param earliest action will not be executed before this date
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
     * @param sids list of server ids
     * @param mods list of state modules to be applied
     * @param earliest action will not be executed before this date
     * @param test run states in test-only mode
     * @return the action object
     */
    public static ApplyStatesAction scheduleApplyStates(User scheduler, List<Long> sids, List<String> mods,
            Date earliest, Optional<Boolean> test) {
        ApplyStatesAction action = (ApplyStatesAction) ActionFactory
                .createAction(ActionFactory.TYPE_APPLY_STATES, earliest);
        String states = mods.isEmpty() ? "highstate" : "states " + mods.toString();
        action.setName("Apply " + states);
        action.setOrg(scheduler != null ?
                scheduler.getOrg() : OrgFactory.getSatelliteOrg());
        action.setSchedulerUser(scheduler);

        ApplyStatesActionDetails actionDetails = new ApplyStatesActionDetails();
        actionDetails.setMods(mods);
        test.ifPresent(t -> actionDetails.setTest(t));
        action.setDetails(actionDetails);
        ActionFactory.save(action);

        scheduleForExecution(action, new HashSet<>(sids));
        return action;
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
        ImageBuildAction action = (ImageBuildAction) ActionFactory
                .createAction(ActionFactory.TYPE_IMAGE_BUILD, earliest);
        action.setName("Image Build " + profile.getLabel());
        action.setOrg(scheduler != null ?
                scheduler.getOrg() : OrgFactory.getSatelliteOrg());
        action.setSchedulerUser(scheduler);

        ImageBuildActionDetails actionDetails = new ImageBuildActionDetails();
        actionDetails.setVersion(version);
        actionDetails.setImageProfileId(profile.getProfileId());
        action.setDetails(actionDetails);
        ActionFactory.save(action);

        scheduleForExecution(action, new HashSet<>(sids));
        return action;
    }

    /**
     *
     * @param scheduler the scheduler
     * @param sids      the sids
     * @param buildActionId the build actionId
     * @param version   the image version
     * @param name      the image name
     * @param store     the image store
     * @param earliest  the earliest
     * @return the image inspect action
     */
    public static ImageInspectAction scheduleImageInspect(User scheduler, List<Long> sids,
                                                          Optional<Long> buildActionId, String version, String name,
                                                          ImageStore store, Date earliest) {
        ImageInspectAction action = (ImageInspectAction) ActionFactory
                .createAction(ActionFactory.TYPE_IMAGE_INSPECT, earliest);
        action.setName("Image Inspect " + store.getUri() + "/" + name + ":" + version);
        action.setOrg(scheduler != null ?
                scheduler.getOrg() : OrgFactory.getSatelliteOrg());
        action.setSchedulerUser(scheduler);

        ImageInspectActionDetails actionDetails = new ImageInspectActionDetails();
        actionDetails.setName(name);
        buildActionId.ifPresent(aid -> actionDetails.setBuildActionId(aid));
        actionDetails.setVersion(version);
        actionDetails.setImageStoreId(store.getId());
        action.setDetails(actionDetails);
        ActionFactory.save(action);

        scheduleForExecution(action, new HashSet<>(sids));
        return action;
    }
}
