/**
 * Copyright (c) 2016 SUSE LLC
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
package com.suse.manager.webui.services;

import com.redhat.rhn.domain.action.Action;
import com.redhat.rhn.domain.action.ActionFactory;
import com.redhat.rhn.domain.action.ActionType;
import com.redhat.rhn.domain.action.errata.ErrataAction;
import com.redhat.rhn.domain.action.rhnpackage.PackageRemoveAction;
import com.redhat.rhn.domain.action.rhnpackage.PackageUpdateAction;
import com.redhat.rhn.domain.action.salt.ApplyStatesAction;
import com.redhat.rhn.domain.action.script.ScriptAction;
import com.redhat.rhn.domain.action.server.ServerAction;
import com.redhat.rhn.domain.errata.Errata;
import com.redhat.rhn.domain.server.MinionServer;
import com.redhat.rhn.domain.server.ServerFactory;
import com.redhat.rhn.manager.entitlement.EntitlementManager;

import com.suse.manager.reactor.messaging.ApplyStatesEventMessage;
import com.suse.manager.webui.services.impl.SaltService;
import com.suse.manager.webui.utils.salt.custom.ScheduleMetadata;
import com.suse.salt.netapi.calls.LocalCall;
import com.suse.salt.netapi.calls.modules.Cmd;
import com.suse.salt.netapi.calls.modules.Schedule;
import com.suse.salt.netapi.calls.modules.State;
import com.suse.salt.netapi.datatypes.target.MinionList;
import com.suse.salt.netapi.exception.SaltException;
import com.suse.salt.netapi.results.Result;

import org.apache.log4j.Logger;

import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.util.Arrays;
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.Stream;

/**
 * Takes {@link Action} objects to be executed via salt.
 */
public enum SaltServerActionService {

    /* Singleton instance of this class */
    INSTANCE;

    /* Logger for this class */
    private static final Logger LOG = Logger.getLogger(SaltServerActionService.class);
    private static final String PACKAGES_PKGINSTALL = "packages.pkginstall";
    private static final String PACKAGES_PATCHINSTALL = "packages.patchinstall";
    private static final String PACKAGES_PKGREMOVE = "packages.pkgremove";
    private static final String PARAM_PKGS = "param_pkgs";

    /**
     * @param actionIn the action
     * @param call the call
     * @param minions minions to target
     * @param forcePackageListRefresh add metadata to force a package list refresh
     * @return a map containing all minions partitioned by success
     */
    private Map<Boolean, List<MinionServer>> schedule(Action actionIn, LocalCall<?> call,
            List<MinionServer> minions, boolean forcePackageListRefresh) {
        ZonedDateTime earliestAction = actionIn.getEarliestAction().toInstant()
                .atZone(ZoneId.systemDefault());

        // Prepare the metadata
        Map<String, Object> metadata = new HashMap<>();
        metadata.put(ScheduleMetadata.SUMA_ACTION_ID, actionIn.getId());
        if (forcePackageListRefresh) {
            metadata.put(ScheduleMetadata.SUMA_FORCE_PGK_LIST_REFRESH, true);
        }

        List<String> minionIds = minions
                .stream()
                .map(MinionServer::getMinionId)
                .collect(Collectors.toList());

        if (LOG.isDebugEnabled()) {
            LOG.debug("Scheduling action for: " +
                    minionIds.stream().collect(Collectors.joining(", ")));
        }

        try {
            ZonedDateTime now = ZonedDateTime.now();
            if (earliestAction.isBefore(now) || earliestAction.equals(now)) {
                LOG.debug("Action will be executed directly using callAsync()");
                final Map<String, Result<Boolean>> responding =
                    SaltService.INSTANCE.ping(new MinionList(minionIds));
                final List<String> present = minionIds.stream()
                        .filter(responding::containsKey)
                        .collect(Collectors.toList());
                LOG.debug(present.size() + " minions present out of " + minionIds.size());

                if (present.isEmpty()) {
                    Map<Boolean, List<MinionServer>> result = new HashMap<>();
                    result.put(true, Collections.emptyList());
                    result.put(false, minions);
                    return result;
                }
                else {
                    List<String> results = SaltService.INSTANCE
                            .callAsync(call.withMetadata(metadata), new MinionList(present))
                            .getMinions();
                    return minions.stream()
                            .collect(Collectors.partitioningBy(minion ->
                                    results.contains(minion.getMinionId()) &&
                                            present.contains(minion.getMinionId())
                            ));
                }
            }
            else {
                LOG.debug("Action will be scheduled for later using schedule()");
                Map<String, Result<Schedule.Result>> results = SaltService.INSTANCE
                        .schedule("scheduled-action-" + actionIn.getId(), call,
                                new MinionList(minionIds), earliestAction, metadata);
                return minions.stream()
                        .collect(Collectors.partitioningBy(minion ->
                                Optional.ofNullable(results.get(minion.getMinionId()))
                                        .flatMap(Result::result)
                                        .map(Schedule.Result::getResult)
                                        .orElse(false)
                        ));
            }
        }
        catch (SaltException ex) {
            LOG.debug("Failed to schedule action: " + ex.getMessage());
            Map<Boolean, List<MinionServer>> result = new HashMap<>();
            result.put(true, Collections.emptyList());
            result.put(false, minions);
            return result;
        }
    }

    private Map<LocalCall<?>, List<MinionServer>> callsForAction(Action actionIn,
            List<MinionServer> minions) {
        ActionType actionType = actionIn.getActionType();
        if (actionType.equals(ActionFactory.TYPE_ERRATA)) {
            return errataAction(minions, (ErrataAction) actionIn);
        }
        else if (actionType.equals(ActionFactory.TYPE_PACKAGES_UPDATE)) {
            return packagesUpdateAction(minions, (PackageUpdateAction) actionIn);
        }
        else if (actionType.equals(ActionFactory.TYPE_PACKAGES_REMOVE)) {
            return packagesRemoveAction(minions, (PackageRemoveAction) actionIn);
        }
        else if (actionType.equals(ActionFactory.TYPE_PACKAGES_REFRESH_LIST)) {
            return packagesRefreshListAction(minions);
        }
        else if (actionType.equals(ActionFactory.TYPE_REBOOT)) {
            return rebootAction(minions);
        }
        else if (actionType.equals(ActionFactory.TYPE_SCRIPT_RUN)) {
            ScriptAction scriptAction = (ScriptAction) actionIn;
            String script = scriptAction.getScriptActionDetails().getScriptContents();
            return remoteCommandAction(minions, script);
        }
        else if (actionType.equals(ActionFactory.TYPE_APPLY_STATES)) {
            ApplyStatesAction applyStatesAction = (ApplyStatesAction) actionIn;
            Optional<String> states = Optional.ofNullable(
                    applyStatesAction.getDetails().getStates());
            return applyStatesAction(minions, states);
        }
        else {
            if (LOG.isDebugEnabled()) {
                LOG.debug("Action type " + actionType.getName() +
                        " is not supported with Salt");
            }
            return Collections.emptyMap();
        }
    }

    private Optional<ServerAction> serverActionFor(Action actionIn, MinionServer minion) {
        return actionIn.getServerActions().stream()
                .filter(sa -> sa.getServerId().equals(minion.getId()))
                .findFirst();
    }

    /**
     * Execute a given {@link Action} via salt.
     *
     * @param actionIn the action to execute
     * @param forcePackageListRefresh add metadata to force a package list refresh
     */
    public void execute(Action actionIn, boolean forcePackageListRefresh) {
        List<MinionServer> minions = Optional.ofNullable(actionIn.getServerActions())
                .map(serverActions -> serverActions.stream()
                        .flatMap(action ->
                                action.getServer().asMinionServer()
                                        .map(Stream::of)
                                        .orElse(Stream.empty()))
                        .filter(m -> m.hasEntitlement(EntitlementManager.SALT))
                        .collect(Collectors.toList())
                )
                .orElse(new LinkedList<>());

        // now prepare each call
        for (Map.Entry<LocalCall<?>, List<MinionServer>> entry :
                callsForAction(actionIn, minions).entrySet()) {
            final LocalCall<?> call = entry.getKey();
            final List<MinionServer> targetMinions = entry.getValue();

            Map<Boolean, List<MinionServer>> results =
                    schedule(actionIn, call, targetMinions, forcePackageListRefresh);

            results.get(true).stream().forEach(minionServer -> {
                serverActionFor(actionIn, minionServer).ifPresent(serverAction -> {
                    if (LOG.isDebugEnabled()) {
                        LOG.debug("Successfully scheduled action for minion: " +
                                minionServer.getMinionId());
                    }
                    ActionFactory.save(serverAction);
                });
            });

            results.get(false).stream().forEach(minionServer -> {
                serverActionFor(actionIn, minionServer).ifPresent(serverAction -> {
                    LOG.warn("Failed to schedule action for minion: " +
                            minionServer.getMinionId());
                    serverAction.setCompletionTime(new Date());
                    serverAction.setResultCode(-1L);
                    serverAction.setResultMsg("Failed to schedule action.");
                    serverAction.setStatus(ActionFactory.STATUS_FAILED);
                    ActionFactory.save(serverAction);
                });
            });
        }
    }

    private Map<LocalCall<?>, List<MinionServer>> errataAction(List<MinionServer> minions,
            ErrataAction errataAction) {
        Set<Long> serverIds = minions.stream()
                .map(MinionServer::getId)
                .collect(Collectors.toSet());
        Set<Long> errataIds = errataAction.getErrata().stream()
                .map(Errata::getId)
                .collect(Collectors.toSet());
        Map<Long, Map<Long, Set<String>>> errataNames = ServerFactory
                .listErrataNamesForServers(serverIds, errataIds);
        // Group targeted minions by errata names
        Map<Set<String>, List<MinionServer>> collect = minions.stream()
                .collect(Collectors.groupingBy(minion -> errataNames.get(minion.getId())
                        .entrySet().stream()
                        .map(Map.Entry::getValue)
                        .flatMap(Set::stream)
                        .collect(Collectors.toSet())
        ));
        // Convert errata names to LocalCall objects of type State.apply
        return collect.entrySet().stream()
                .collect(Collectors.toMap(entry -> State.apply(
                        Arrays.asList(PACKAGES_PATCHINSTALL),
                        Optional.of(Collections.singletonMap(PARAM_PKGS, entry.getKey()
                                .stream().collect(Collectors.toMap(
                                        patch -> "patch:" + patch,
                                        patch -> "")))),
                        Optional.of(true)
                ),
                Map.Entry::getValue));
    }

    private Map<LocalCall<?>, List<MinionServer>> packagesUpdateAction(
            List<MinionServer> minions, PackageUpdateAction action) {
        Map<LocalCall<?>, List<MinionServer>> ret = new HashMap<>();
        Map<String, String> pkgs = action.getDetails().stream().collect(Collectors.toMap(
                d -> d.getPackageName().getName(), d -> d.getEvr().toString()));
        ret.put(State.apply(Arrays.asList(PACKAGES_PKGINSTALL),
                Optional.of(Collections.singletonMap(PARAM_PKGS, pkgs)),
                Optional.of(true)), minions);
        return ret;
    }

    private Map<LocalCall<?>, List<MinionServer>> packagesRemoveAction(
            List<MinionServer> minions, PackageRemoveAction action) {
        Map<LocalCall<?>, List<MinionServer>> ret = new HashMap<>();
        Map<String, String> pkgs = action.getDetails().stream().collect(Collectors.toMap(
                d -> d.getPackageName().getName(), d -> d.getEvr().toString()));
        ret.put(State.apply(Arrays.asList(PACKAGES_PKGREMOVE),
                Optional.of(Collections.singletonMap(PARAM_PKGS, pkgs)),
                Optional.of(true)), minions);
        return ret;
    }

    private Map<LocalCall<?>, List<MinionServer>> packagesRefreshListAction(
            List<MinionServer> minions) {
        Map<LocalCall<?>, List<MinionServer>> ret = new HashMap<>();
        ret.put(State.apply(Arrays.asList(ApplyStatesEventMessage.PACKAGES_PROFILE_UPDATE)),
                minions);
        return ret;
    }

    private Map<LocalCall<?>, List<MinionServer>> rebootAction(List<MinionServer> minions) {
        Map<LocalCall<?>, List<MinionServer>> ret = new HashMap<>();
        ret.put(com.suse.salt.netapi.calls.modules.System
                .reboot(Optional.of(3)), minions);
        return ret;
    }

    private Map<LocalCall<?>, List<MinionServer>> remoteCommandAction(
            List<MinionServer> minions, String script) {
        Map<LocalCall<?>, List<MinionServer>> ret = new HashMap<>();
        // FIXME: This supports only bash at the moment
        ret.put(Cmd.execCodeAll(
                "bash",
                // remove \r or bash will fail
                script.replaceAll("\r\n", "\n")), minions);
        return ret;
    }

    private Map<LocalCall<?>, List<MinionServer>> applyStatesAction(
            List<MinionServer> minions, Optional<String> states) {
        Map<LocalCall<?>, List<MinionServer>> ret = new HashMap<>();
        if (states.isPresent()) {
            ret.put(State.apply(Arrays.asList(states.get().split("\\s*,\\s*"))), minions);
        }
        else {
            ret.put(State.apply(), minions);
        }
        return ret;
    }
}
