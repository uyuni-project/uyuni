/*
 * Copyright (c) 2014 SUSE LLC
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

import static java.util.Collections.singleton;
import static java.util.Collections.singletonList;

import com.redhat.rhn.common.hibernate.HibernateFactory;
import com.redhat.rhn.domain.action.Action;
import com.redhat.rhn.domain.action.ActionChain;
import com.redhat.rhn.domain.action.ActionChainFactory;
import com.redhat.rhn.domain.action.ActionFactory;
import com.redhat.rhn.domain.action.ActionType;
import com.redhat.rhn.domain.action.appstream.AppStreamAction;
import com.redhat.rhn.domain.action.appstream.AppStreamActionDetails;
import com.redhat.rhn.domain.action.channel.SubscribeChannelsAction;
import com.redhat.rhn.domain.action.channel.SubscribeChannelsActionDetails;
import com.redhat.rhn.domain.action.config.ConfigAction;
import com.redhat.rhn.domain.action.rhnpackage.PackageAction;
import com.redhat.rhn.domain.action.salt.ApplyStatesAction;
import com.redhat.rhn.domain.action.salt.ApplyStatesActionDetails;
import com.redhat.rhn.domain.action.salt.PlaybookAction;
import com.redhat.rhn.domain.action.salt.PlaybookActionDetails;
import com.redhat.rhn.domain.action.salt.build.ImageBuildAction;
import com.redhat.rhn.domain.action.salt.build.ImageBuildActionDetails;
import com.redhat.rhn.domain.action.script.ScriptActionDetails;
import com.redhat.rhn.domain.action.script.ScriptRunAction;
import com.redhat.rhn.domain.channel.Channel;
import com.redhat.rhn.domain.image.ImageInfo;
import com.redhat.rhn.domain.image.ImageInfoFactory;
import com.redhat.rhn.domain.image.ImageProfile;
import com.redhat.rhn.domain.org.OrgFactory;
import com.redhat.rhn.domain.server.Server;
import com.redhat.rhn.domain.server.ServerFactory;
import com.redhat.rhn.domain.user.User;
import com.redhat.rhn.manager.errata.ErrataManager;
import com.redhat.rhn.manager.system.SystemManager;
import com.redhat.rhn.taskomatic.TaskomaticApi;
import com.redhat.rhn.taskomatic.TaskomaticApiException;

import com.suse.manager.utils.MinionServerUtils;

import org.apache.commons.io.FileUtils;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Date;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * An ActionManager companion to deal with Action Chains.
 *
 * Methods in this class are intended to replace similar methods in
 * ActionManager adding Action Chains support. It was decided to keep
 * this class separate to avoid adding significant complexity to ActionManager.
 * @author Silvio Moioli {@literal <smoioli@suse.de>}
 */
public class ActionChainManager {
    private static TaskomaticApi taskomaticApi = new TaskomaticApi();

    /**
     * Utility class constructor.
     */
    private ActionChainManager() {
    }

    /**
     * Set the {@link TaskomaticApi} instance to use. Only needed for unit tests.
     * @param taskomaticApiIn the {@link TaskomaticApi}
     */
    public static void setTaskomaticApi(TaskomaticApi taskomaticApiIn) {
        taskomaticApi = taskomaticApiIn;
    }

    /**
     * Schedules s package installation on a server.
     * @param user the user scheduling actions
     * @param server the server
     * @param packages the packages
     * @param earliest the earliest execution date
     * @param actionChain the action chain or null
     * @return scheduled action
     * @throws TaskomaticApiException if there was a Taskomatic error
     * (typically: Taskomatic is down)
     * @see com.redhat.rhn.manager.action.ActionManager#schedulePackageInstall
     */
    public static PackageAction schedulePackageInstall(User user, Server server,
            List<Map<String, Long>> packages, Date earliest, ActionChain actionChain)
        throws TaskomaticApiException {
        return schedulePackageActionByOs(user, server, packages, earliest, actionChain,
                null, ActionFactory.TYPE_PACKAGES_UPDATE);
    }

    /**
     * Schedules s package removal on a server.
     * @param user the user scheduling actions
     * @param server the server
     * @param packages the packages
     * @param earliest the earliest execution date
     * @param actionChain the action chain or null
     * @return scheduled action
     * @throws TaskomaticApiException if there was a Taskomatic error
     * (typically: Taskomatic is down)
     * @see com.redhat.rhn.manager.action.ActionManager#schedulePackageRemoval
     */
    public static PackageAction schedulePackageRemoval(User user, Server server,
            List<Map<String, Long>> packages, Date earliest, ActionChain actionChain)
        throws TaskomaticApiException {
        return schedulePackageActionByOs(user, server, packages, earliest, actionChain,
                null, ActionFactory.TYPE_PACKAGES_REMOVE);
    }

    /**
     * Schedules package upgrade(s) for the given servers.
     * Note: package upgrade = package install
     * @param user the user scheduling actions
     * @param packageMaps maps system IDs to lists of "package maps"
     * @param earliestAction Date of earliest action to be executed
     * @param actionChain the action chain or null
     * @return scheduled actions
     * @throws TaskomaticApiException if there was a Taskomatic error
     * (typically: Taskomatic is down)
     * @see ActionManager#addPackageActionDetails(Action, List) for "package map"
     */
    public static List<Action> schedulePackageUpgrades(User user,
            Map<Long, List<Map<String, Long>>> packageMaps, Date earliestAction,
            ActionChain actionChain) throws TaskomaticApiException {


        if (actionChain != null) {
            List<Action> actions = new ArrayList<>();
            int sortOrder = ActionChainFactory.getNextSortOrderValue(actionChain);
            for (Long sid : packageMaps.keySet()) {
                Server server = SystemManager.lookupByIdAndUser(sid, user);
                actions.add(schedulePackageActionByOs(user, server, packageMaps.get(sid),
                        earliestAction, actionChain, sortOrder,
                        ActionFactory.TYPE_PACKAGES_UPDATE));
            }
            return actions;
        }
        else {
            Map<List<Map<String, Long>>, List<Long>> collect = packageMaps.entrySet()
                    .stream().collect(
                    Collectors.groupingBy(
                            Map.Entry::getValue,
                            Collectors.mapping(Map.Entry::getKey, Collectors.toList())
                    )
            );
            List<Action> actions = new ArrayList<>();
            for (Map.Entry<List<Map<String, Long>>, List<Long>> entry :collect.entrySet()) {
                List<Long> servers = entry.getValue();
                List<Map<String, Long>> packages = entry.getKey();
                actions.addAll(
                        schedulePackageActionsByOs(user, servers, packages,
                        earliestAction, null, ActionFactory.TYPE_PACKAGES_UPDATE)
                );
            }
            return actions;
        }
    }

    /**
     * Schedules a package upgrade for the given server.
     * @param user the user scheduling actions
     * @param server the server
     * @param packages a list of "package maps"
     * @param earliest the earliest execution date
     * @param actionChain the action chain or null
     * @return scheduled action
     * @throws TaskomaticApiException if there was a Taskomatic error
     * (typically: Taskomatic is down)
     * @see ActionManager#addPackageActionDetails(Action, List) for "package map"
     */
    public static PackageAction schedulePackageUpgrade(User user, Server server,
            List<Map<String, Long>> packages, Date earliest, ActionChain actionChain)
        throws TaskomaticApiException {
        return schedulePackageInstall(user, server, packages, earliest, actionChain);
    }

    /**
     * Schedules a package verification for the given server.
     * @param user the user scheduling actions
     * @param server the server
     * @param packages a list of "package maps"
     * @param earliest the earliest execution date
     * @param actionChain the action chain or null
     * @return scheduled action
     * @throws TaskomaticApiException if there was a Taskomatic error
     * (typically: Taskomatic is down)
     * @see com.redhat.rhn.manager.action.ActionManager#schedulePackageVerify
     * @see ActionManager#addPackageActionDetails(Action, List) for "package map"
     */
    public static PackageAction schedulePackageVerify(User user, Server server,
            List<Map<String, Long>> packages, Date earliest, ActionChain actionChain)
        throws TaskomaticApiException {
        return (PackageAction) schedulePackageAction(user, packages,
            ActionFactory.TYPE_PACKAGES_VERIFY, earliest, actionChain, null, server);
    }

    /**
     * Schedules a generic package action on a single server.
     * @param user the user scheduling actions
     * @param packages a list of "package maps"
     * @param type the type
     * @param earliest the earliest execution date
     * @param actionChain the action chain or null
     * @param sortOrder the sort order or null
     * @param server the server
     * @return scheduled action
     * @throws TaskomaticApiException if there was a Taskomatic error
     * (typically: Taskomatic is down)
     * @see com.redhat.rhn.manager.action.ActionManager#schedulePackageAction
     * @see ActionManager#addPackageActionDetails(Action, List) for "package map"
     */
    private static Action schedulePackageAction(User user, List<Map<String, Long>> packages,
            ActionType type, Date earliest, ActionChain actionChain, Integer sortOrder,
            Server server)
        throws TaskomaticApiException {
        Set<Long> serverIds = new HashSet<>();
        serverIds.add(server.getId());
        return schedulePackageActions(user, packages, type, earliest, actionChain,
            sortOrder, serverIds).iterator().next();
    }

    /**
     * Schedules generic package actions on multiple servers.
     * @param user the user scheduling actions
     * @param packages a list of "package maps"
     * @param type the type
     * @param earliest the earliest execution date
     * @param actionChain the action chain or null
     * @param sortOrder the sort order or null
     * @param servers the servers involved
     * @return scheduled actions
     * @throws TaskomaticApiException if there was a Taskomatic error
     * (typically: Taskomatic is down)
     * @see com.redhat.rhn.manager.action.ActionManager#schedulePackageAction
     * @see ActionManager#addPackageActionDetails(Action, List) for "package map"
     */
    private static Set<Action> schedulePackageActions(User user,
            List<Map<String, Long>> packages, ActionType type, Date earliestAction,
            ActionChain actionChain, Integer sortOrder, Set<Long> serverIds)
        throws TaskomaticApiException {

        String name = ActionManager.getActionName(type);

        Set<Action> result = createActions(user, type, name, earliestAction,
            actionChain, sortOrder, serverIds);

        ActionManager.addPackageActionDetails(result, packages);

        taskoScheduleActions(result, user, actionChain);
        return result;
    }

    /**
     * Schedules AppStream action.
     * @param user the user scheduling the action
     * @param name the name of the action
     * @param details the details to be linked with the action
     * @param earliest the earliest execution date
     * @param actionChain the action chain or null
     * @param serverId the id of servers involved
     * @return id of the scheduled action
     * @throws TaskomaticApiException if there was a Taskomatic error
     */
    public static Long scheduleAppStreamAction(
        User user,
        String name,
        Set<AppStreamActionDetails> details,
        Date earliest,
        ActionChain actionChain,
        Long serverId
    ) throws TaskomaticApiException {
        var actions = createActions(
            user, ActionFactory.TYPE_APPSTREAM_CHANGE, name, earliest, actionChain, null, singleton(serverId)
        );
        AppStreamAction action = (AppStreamAction) actions.stream().findFirst()
                .orElseThrow(() -> new RuntimeException("Action scheduling result missing"));
        action.setDetails(details);
        ActionFactory.save(action);
        taskoScheduleActions(actions, user, actionChain);
        return action.getId();
    }

    /**
     * Schedules script actions for the given servers.
     * @param user the user scheduling actions
     * @param sids IDs of affected servers
     * @param script detail object of the script to run
     * @param name name of the script action
     * @param earliest the earliest execution date
     * @param actionChain the action chain or null
     * @return scheduled actions
     * @throws TaskomaticApiException if there was a Taskomatic error
     * (typically: Taskomatic is down)
     * @throws com.redhat.rhn.manager.MissingCapabilityException if any server
     *             in the list is missing script.run schedule fails
     * @throws com.redhat.rhn.manager.MissingEntitlementException if any server
     *             in the list is missing Provisioning schedule fails
     * @see com.redhat.rhn.manager.action.ActionManager#scheduleScriptRun
     * @see ActionManager#addPackageActionDetails(Action, List) for "package map"
     */
    public static Set<Action> scheduleScriptRuns(User user, List<Long> sids, String name,
            ScriptActionDetails script, Date earliest, ActionChain actionChain)
        throws TaskomaticApiException {

        ActionManager.checkScriptingOnServers(sids);

        Set<Long> sidSet = new HashSet<>();
        sidSet.addAll(sids);

        Set<Action> result = createActions(user, ActionFactory.TYPE_SCRIPT_RUN, name,
            earliest, actionChain, null, sidSet);
        for (Action action : result) {
            ScriptActionDetails actionScript = ActionFactory.createScriptActionDetails(
                    script.getUsername(), script.getGroupname(), script.getTimeout(),
                    script.getScriptContents());
            ((ScriptRunAction)action).setScriptActionDetails(actionScript);
            ActionFactory.save(action);
        }
        taskoScheduleActions(result, user, actionChain);
        return result;
    }

    /**
     * Schedules the application of states to minions
     * @param user the requesting user
     * @param sids list of system IDs
     * @param test for test mode
     * @param earliest earliest execution date
     * @param actionChain Action Chain instance
     * @return a set of Actions
     * @throws TaskomaticApiException if there was a Taskomatic error
     * (typically: Taskomatic is down)
     */
    public static Set<Action> scheduleApplyStates(User user, List<Long> sids, Optional<Boolean> test,
                                                   Date earliest, ActionChain actionChain)
            throws TaskomaticApiException {

        if (!sids.stream().map(ServerFactory::lookupById)
                .filter(server -> !MinionServerUtils.isMinionServer(server))
                .collect(Collectors.toList()).isEmpty()) {
            throw new IllegalArgumentException("Server ids include non minion servers.");
        }

        Set<Long> sidSet = new HashSet<>();
        sidSet.addAll(sids);

        String summary = "Apply highstate" + (test.isPresent() && test.get() ? " in test-mode" : "");
        Set<Action> result = createActions(user, ActionFactory.TYPE_APPLY_STATES, summary,
                earliest, actionChain, null, sidSet);
        for (Action action : result) {
            ApplyStatesActionDetails applyState = new ApplyStatesActionDetails();
            applyState.setActionId(action.getId());
            test.ifPresent(applyState::setTest);
            ((ApplyStatesAction)action).setDetails(applyState);
            ActionFactory.save(action);
        }
        taskoScheduleActions(result, user, actionChain);
        return result;
    }

    /**
     * Creates configuration actions for the given server IDs.
     * @param user the user scheduling actions
     * @param revisions a set of revision IDs
     * @param serverIds a set of server IDs
     * @param type the type of configuration action
     * @param earliest the earliest execution date
     * @param actionChain the action chain or null
     * @return scheduled actions
     * @throws TaskomaticApiException if there was a Taskomatic error
     * (typically: Taskomatic is down)
     */
    public static Set<Action> createConfigActions(User user, Collection<Long> revisions,
        Collection<Long> serverIds, ActionType type, Date earliest,
        ActionChain actionChain) throws TaskomaticApiException {

        List<Server> servers = SystemManager.hydrateServerFromIds(serverIds, user);
        return createConfigActionForServers(user, revisions, servers, type, earliest,
            actionChain);
    }

    /**
     * Creates configuration actions for the given servers.
     * @param user the user scheduling actions
     * @param revisions a set of revision IDs
     * @param servers a set of servers
     * @param type the type of configuration action
     * @param earliest the earliest execution date
     * @param actionChain the action chain or null
     * @return scheduled actions
     * @throws TaskomaticApiException if there was a Taskomatic error
     * (typically: Taskomatic is down)
     */
    public static Set<Action> createConfigActionForServers(User user,
        Collection<Long> revisions, Collection<Server> servers, ActionType type,
        Date earliest, ActionChain actionChain) throws TaskomaticApiException {
        Set<Action> result = new HashSet<>();
        if (actionChain == null) {
            Action action = ActionManager.createConfigActionForServers(user, revisions,
                servers, type, earliest);
            if (action != null) {
                result.add(action);
            }
        }
        else {
            for (Server server : servers) {
                ConfigAction action = ActionManager
                    .createConfigAction(user, type, earliest);
                ActionManager.checkConfigActionOnServer(type, server);
                ActionChainFactory.queueActionChainEntry(action, actionChain, server);
                ActionManager.addConfigurationRevisionsToAction(user, revisions, action,
                    server);
                ActionFactory.save(action);
                result.add(action);
            }
        }
        return result;
    }

    /**
     * Creates configuration actions from server-revision maps.
     * @param user the user scheduling actions
     * @param revisions maps servers to multiple revision IDs
     * @param serverIds a set of server IDs
     * @param type the type of configuration action
     * @param earliest the earliest execution date
     * @param actionChain the action chain or null
     * @return scheduled actions
     * @throws TaskomaticApiException if there was a Taskomatic error
     */
    public static Set<Action> createConfigActions(User user,
            Map<Long, Collection<Long>> revisions, Collection<Long> serverIds,
            ActionType type, Date earliest, ActionChain actionChain) throws TaskomaticApiException {

            List<Server> servers = SystemManager.hydrateServerFromIds(serverIds, user);
            return createConfigActionForServers(user, revisions, servers, type, earliest,
                actionChain);
    }

    /**
     * Creates configuration actions from server-revision maps.
     * @param user the user scheduling actions
     * @param revisions maps servers to multiple revision IDs
     * @param servers a set of server objects
     * @param type the type of configuration action
     * @param earliest the earliest execution date
     * @param actionChain the action chain or null
     * @return scheduled actions
     * @throws TaskomaticApiException if there was a Taskomatic error
     */
    public static Set<Action> createConfigActionForServers(User user,
        Map<Long, Collection<Long>> revisions, Collection<Server> servers,
        ActionType type, Date earliest, ActionChain actionChain) throws TaskomaticApiException {
        Set<Action> result = new HashSet<>();
        if (actionChain == null) {
            ConfigAction action = ActionManager.createConfigAction(user, type, earliest);
            ActionFactory.save(action);

            for (Server server : servers) {
                ActionManager.checkConfigActionOnServer(type, server);
                ActionFactory.addServerToAction(server.getId(), action);

                ActionManager.addConfigurationRevisionsToAction(user,
                    revisions.get(server.getId()), action, server);
                ActionFactory.save(action);
                result.add(action);
            }
            taskomaticApi.scheduleActionExecution(action);
        }
        else {
            int sortOrder = ActionChainFactory.getNextSortOrderValue(actionChain);
            for (Server server : servers) {
                ConfigAction action = ActionManager
                    .createConfigAction(user, type, earliest);
                ActionFactory.save(action);
                result.add(action);
                ActionManager.checkConfigActionOnServer(type, server);
                ActionChainFactory.queueActionChainEntry(action, actionChain, server,
                    sortOrder);
                ActionManager.addConfigurationRevisionsToAction(user,
                    revisions.get(server.getId()), action, server);
                ActionFactory.save(action);
                result.add(action);
            }
        }
        return result;
    }

    /**
     * Schedules a reboot action on a server.
     * @param user the user scheduling actions
     * @param server the affected server
     * @param earliest the earliest execution date
     * @param actionChain the action chain or null
     * @return scheduled actions
     * @throws TaskomaticApiException if there was a Taskomatic error
     * (typically: Taskomatic is down)
     */
    public static Action scheduleRebootAction(User user, Server server, Date earliest,
        ActionChain actionChain) throws TaskomaticApiException {
        Set<Long> serverIds = new HashSet<>();
        serverIds.add(server.getId());
        Set<Action> actions = scheduleRebootActions(user, serverIds, earliest, actionChain);
        return actions.iterator().next();
    }

    /**
     * Schedules one or more reboot actions on multiple servers.
     * @param user the user scheduling actions
     * @param serverIds the affected servers' IDs
     * @param earliest the earliest execution date
     * @param actionChain the action chain or null
     * @return scheduled actions
     * @throws TaskomaticApiException if there was a Taskomatic error
     * (typically: Taskomatic is down)
     */
    public static Set<Action> scheduleRebootActions(User user, Set<Long> serverIds,
        Date earliest, ActionChain actionChain) throws TaskomaticApiException {
        Set<Action> result = createActions(user, ActionFactory.TYPE_REBOOT,
            ActionFactory.TYPE_REBOOT.getName(), earliest, actionChain, null, serverIds);
        taskoScheduleActions(result, user, actionChain);
        return result;
    }

    /**
     * Schedules an Errata update on a server.
     * @param user the user scheduling actions
     * @param servers the affected servers
     * @param errataIds a list of erratas IDs
     * @param earliest the earliest execution date
     * @param actionChain the action chain
     * @return list of scheduled action ids
     * @throws TaskomaticApiException if there was a Taskomatic error
     * (typically: Taskomatic is down)
     */
    public static List<Long> scheduleErrataUpdate(User user, List<Server> servers, List<Integer> errataIds,
                                              Date earliest, ActionChain actionChain) throws TaskomaticApiException  {

        // This won't actually apply the errata because we're passing in an action chain
        return ErrataManager.applyErrata(user,
                errataIds.stream().map(Integer::longValue).collect(Collectors.toList()),
                earliest,
                actionChain,
                servers.stream().map(Server::getId).collect(Collectors.toList()), true, false
        );

    }

    /**
     * Schedules an Errata update on a server.
     * @param user the user scheduling actions
     * @param servers the affected servers
     * @param errataIds a list of erratas IDs
     * @param earliest the earliest execution date
     * @param actionChain the action chain
     * @param onlyRelevant If true, InvalidErrataException is thrown if an errata
     * does not apply to a system.
     * @return list of scheduled action ids
     * @throws TaskomaticApiException if there was a Taskomatic error
     * (typically: Taskomatic is down)
     */
    public static List<Long> scheduleErrataUpdate(User user, List<Server> servers, List<Integer> errataIds,
                                              Date earliest, ActionChain actionChain, boolean onlyRelevant)
                                                      throws TaskomaticApiException  {

        // This won't actually apply the errata because we're passing in an action chain
        return ErrataManager.applyErrata(user,
                errataIds.stream().map(Integer::longValue).collect(Collectors.toList()),
                earliest,
                actionChain,
                servers.stream().map(Server::getId).collect(Collectors.toList()), onlyRelevant, false);
    }


    /**
     * Schedules one or more package installation actions on one or more servers.
     * @param user the user scheduling actions
     * @param serverIds the affected servers' IDs
     * @param packages a list of "package maps"
     * @param earliest the earliest execution date
     * @param actionChain the action chain or null
     * @return scheduled actions
     * @throws TaskomaticApiException if there was a Taskomatic error
     * (typically: Taskomatic is down)
     * @see ActionManager#addPackageActionDetails(Action, List) for "package map"
     */
    public static List<Action> schedulePackageInstalls(User user,
        Collection<Long> serverIds, List<Map<String, Long>> packages, Date earliest,
        ActionChain actionChain) throws TaskomaticApiException {

        return schedulePackageActionsByOs(user, serverIds, packages, earliest, actionChain,
                ActionFactory.TYPE_PACKAGES_UPDATE);
    }

    /**
     * Schedules one or more package removal actions on one or more servers.
     * @param user the user scheduling actions
     * @param serverIds the affected servers' IDs
     * @param packages a list of "package maps"
     * @param earliest the earliest execution date
     * @param actionChain the action chain or null
     * @return scheduled actions
     * @throws TaskomaticApiException if there was a Taskomatic error
     * (typically: Taskomatic is down)
     * @see ActionManager#addPackageActionDetails(Action, List) for "package map"
     */
    public static List<Action> schedulePackageRemovals(User user,
        Collection<Long> serverIds, List<Map<String, Long>> packages, Date earliest,
        ActionChain actionChain) throws TaskomaticApiException {
        return schedulePackageActionsByOs(user, serverIds, packages, earliest, actionChain,
                ActionFactory.TYPE_PACKAGES_REMOVE);
    }

    /**
     * Creates generic actions on multiple servers.
     * @param user the user scheduling actions
     * @param type the type
     * @param name the name
     * @param earliest the earliest execution date
     * @param actionChain the action chain or null
     * @param sortOrder the sort order or null
     * @param serverIds the affected servers' IDs
     * @return created actions
     * @see com.redhat.rhn.manager.action.ActionManager#scheduleAction
     */
    private static Set<Action> createActions(User user, ActionType type, String name,
            Date earliest, ActionChain actionChain, Integer sortOrder, Set<Long> serverIds) {
        Set<Action> result = new HashSet<>();

        if (actionChain == null) {
            Action action = ActionManager.createAction(user, type, name, earliest);
            ActionManager.scheduleForExecution(action, serverIds);
            result.add(action);
        }
        else {
            Integer nextSortOrder = sortOrder;
            if (sortOrder == null) {
                nextSortOrder = ActionChainFactory.getNextSortOrderValue(actionChain);
            }
            for (Long serverId : serverIds) {
                Action action = ActionManager.createAction(user, type, name, earliest);
                ActionChainFactory.queueActionChainEntry(action, actionChain, serverId,
                    nextSortOrder);
                result.add(action);
            }
        }
        return result;
    }

    /**
     * Schedule multiple actions via taskomatic API.
     * @param actions the actions
     * @param user the user scheduling actions
     * @param actionChain the action chain or null
     * @throws TaskomaticApiException if there was a Taskomatic error
     * (typically: Taskomatic is down)
     */
    private static void taskoScheduleActions(Set<Action> actions, User user, ActionChain actionChain)
        throws TaskomaticApiException {
        try {
            for (Action action : actions) {
                if (actionChain == null) {
                    if (ActionFactory.TYPE_SUBSCRIBE_CHANNELS.equals(action.getActionType())) {
                        // Subscribing to channels is handled by the MinionActionExecutor even
                        // for traditional clients. Also the user must be passed for traditional
                        // clients.
                        taskomaticApi.scheduleSubscribeChannels(user, (SubscribeChannelsAction)action);
                    }
                    else {
                        taskomaticApi.scheduleActionExecution(action);
                    }
                }
                if (ActionFactory.TYPE_PACKAGES_UPDATE.equals(action.getActionType())) {
                    MinionActionManager.scheduleStagingJobsForMinions(singletonList(action), user.getOrg());
                }
            }
        }
        catch (TaskomaticApiException e) {
            String cancellationMessage = "Taskomatic error, please check log files";
            // do not leave actions queued forever
            actions.stream().map(a -> HibernateFactory.reload(a)).forEach(a -> {
                a.getServerActions().stream().forEach(sa -> {
                    ActionManager.failSystemAction(user, sa.getServerId(), a.getId(), cancellationMessage);
                });
                ActionFactory.save(a);
            });

            throw e;
        }
    }

    /**
     * Schedules package actions.
     * @param user the user scheduling actions
     * @param server the server
     * @param packages the packages
     * @param earliest the earliest execution date
     * @param actionChain the action chain or null
     * @param sortOrder the sort order or null
     * @param linuxActionType the action type to apply to Linux servers
     * @return scheduled action
     * @throws TaskomaticApiException if there was a Taskomatic error
     * (typically: Taskomatic is down)
     */
    private static PackageAction schedulePackageActionByOs(User user, Server server,
        List<Map<String, Long>> packages, Date earliest, ActionChain actionChain,
            Integer sortOrder, ActionType linuxActionType) throws TaskomaticApiException {
        return (PackageAction) schedulePackageAction(user, packages, linuxActionType,
            earliest, actionChain, sortOrder, server);
    }

    /**
     * Schedules package actions.
     * @param user the user scheduling actions
     * @param serverIds the affected servers' IDs
     * @param packages the packages involved
     * @param earliest the earliest execution date
     * @param actionChain the action chain or null
     * @param linuxActionType the action type to apply to Linux servers
     * @return scheduled actions
     * @throws TaskomaticApiException if there was a Taskomatic error
     * (typically: Taskomatic is down)
     */
    private static List<Action> schedulePackageActionsByOs(User user,
            Collection<Long> serverIds, List<Map<String, Long>> packages, Date earliest,
            ActionChain actionChain, ActionType linuxActionType)
        throws TaskomaticApiException {

        List<Action> result = new LinkedList<>();
        Set<Long> rhelServers = new HashSet<>();
        rhelServers.addAll(ServerFactory.listLinuxSystems(serverIds));

        if (!rhelServers.isEmpty()) {
            result.addAll(schedulePackageActions(user, packages, linuxActionType, earliest,
                actionChain, null, rhelServers));
        }

        return result;
    }

    /**
     * Create subscribe channel actions for the given servers.
     * @param user the user scheduling the actions
     * @param serverIds the server ids
     * @param base the base channel to set
     * @param children the child channels to set
     * @param earliest the earliest execution date
     * @param actionChain the action chain or null
     * @return created and schedule actions
     * @throws TaskomaticApiException if there was a Taskomatic error
     * (typically: Taskomatic is down)
     */
    public static Set<Action> scheduleSubscribeChannelsAction(User user,
                                                              Set<Long> serverIds,
                                                              Optional<Channel> base,
                                                              Collection<Channel> children,
                                                              Date earliest,
                                                              ActionChain actionChain)
            throws TaskomaticApiException {
        Set<Action> result = createActions(user, ActionFactory.TYPE_SUBSCRIBE_CHANNELS, "Subscribe channels",
                earliest, actionChain, null, serverIds);
        for (Action action : result) {
            SubscribeChannelsActionDetails details = new SubscribeChannelsActionDetails();
            base.ifPresent(details::setBaseChannel);
            details.setChannels(new HashSet<>(children));
            details.setParentAction(action);
            ((SubscribeChannelsAction)action).setDetails(details);
            ActionFactory.save(action);
        }
        taskoScheduleActions(result, user, actionChain);
        return result;
    }

    /**
     * Schedule an Image Build
     * @param buildHostId The ID of the build host
     * @param version the tag/version of the resulting image
     * @param profile the profile
     * @param earliest earliest build
     * @param actionChain the action chain to add to or null
     * @param user the current user
     * @return the action ID
     * @throws TaskomaticApiException if there was a Taskomatic error
     * (typically: Taskomatic is down)
     */
    public static ImageBuildAction scheduleImageBuild(long buildHostId, String version, ImageProfile profile,
                                     Date earliest, ActionChain actionChain, User user) throws TaskomaticApiException {

        ImageInfo info = ImageInfoFactory.createImageInfo(buildHostId, version, profile);

        Set<Action> result = createActions(user, ActionFactory.TYPE_IMAGE_BUILD, "Image Build " + profile.getLabel(),
                earliest, actionChain, null, Collections.singleton(buildHostId));

        ImageBuildAction action = (ImageBuildAction)result.stream().findFirst()
                .orElseThrow(() -> new RuntimeException("Action scheduling result missing"));

        action.setName("Image Build " + profile.getLabel());
        action.setOrg(user != null ?
                user.getOrg() : OrgFactory.getSatelliteOrg());
        action.setSchedulerUser(user);

        ImageBuildActionDetails actionDetails = new ImageBuildActionDetails();
        actionDetails.setVersion(info.getVersion());
        actionDetails.setImageProfileId(profile.getProfileId());
        action.setDetails(actionDetails);
        ActionFactory.save(action);

        info.setBuildAction(action);

        ImageInfoFactory.save(info);

        taskoScheduleActions(result, user, actionChain);

        return action;
    }

    /**
     * Schedule execution of a playbook on an Ansible control node.
     *
     * @param scheduler the user who is scheduling
     * @param controlNodeId server ID of the Ansible control node
     * @param playbookPath path to the playbook file in the control node
     * @param inventoryPath path to the inventory file in the control node
     * @param actionChain the action chain to add to or null
     * @param earliest action will not be executed before this date
     * @param testMode true if the playbook shall be executed in test mode
     * @param flushCache true if --flush-cache flag is to be set
     * @return the action object
     */
    public static PlaybookAction scheduleExecutePlaybook(User scheduler, Long controlNodeId, String playbookPath,
            String inventoryPath, ActionChain actionChain, Date earliest, boolean testMode, boolean flushCache)
            throws TaskomaticApiException {
        String playbookName = FileUtils.getFile(playbookPath).getName();

        Set<Action> result = createActions(scheduler, ActionFactory.TYPE_PLAYBOOK,
                String.format("Execute playbook '%s'", playbookName), earliest, actionChain, null,
                singleton(controlNodeId));
        PlaybookAction action = (PlaybookAction) result.stream().findFirst()
                .orElseThrow(() -> new RuntimeException("Action scheduling result missing"));

        PlaybookActionDetails details = new PlaybookActionDetails();
        details.setPlaybookPath(playbookPath);
        details.setInventoryPath(inventoryPath);
        details.setTestMode(testMode);
        details.setFlushCache(flushCache);
        action.setDetails(details);
        ActionFactory.save(action);
        taskoScheduleActions(result, scheduler, actionChain);

        return action;
    }
}
