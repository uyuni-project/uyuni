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
import com.redhat.rhn.domain.action.errata.ErrataAction;
import com.redhat.rhn.domain.action.salt.ApplyStatesAction;
import com.redhat.rhn.domain.action.script.ScriptAction;
import com.redhat.rhn.domain.action.server.ServerAction;
import com.redhat.rhn.domain.errata.Errata;
import com.redhat.rhn.domain.server.MinionServer;
import com.redhat.rhn.domain.server.ServerFactory;
import com.redhat.rhn.manager.entitlement.EntitlementManager;

import com.suse.manager.webui.services.impl.SaltAPIService;
import com.suse.salt.netapi.calls.LocalCall;
import com.suse.salt.netapi.calls.modules.Pkg;
import com.suse.salt.netapi.calls.modules.Schedule;
import com.suse.salt.netapi.calls.modules.State;
import com.suse.salt.netapi.datatypes.target.MinionList;
import com.suse.salt.netapi.datatypes.target.Target;
import com.suse.salt.netapi.exception.SaltException;

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
import java.util.function.Function;
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

    /**
     *
     * @param actionIn the action
     * @param call the call
     * @param minions minions to target
     * @return a map containing all minions partitioned by success
     */
    private Map<Boolean, List<MinionServer>> schedule(Action actionIn,
            LocalCall<?> call, List<MinionServer> minions) {
        ZonedDateTime earliestAction = actionIn.getEarliestAction().toInstant()
                .atZone(ZoneId.systemDefault());
        Map<String, Long> metadata = new HashMap<>();
        metadata.put("suma-action-id", actionIn.getId());
        Map<String, MinionServer> minionsById = minions
                .stream()
                .collect(Collectors.toMap(MinionServer::getMinionId, Function.identity()));
        Target<?> target = new MinionList(minionsById.keySet()
                .stream().collect(Collectors.toList()));

        if (LOG.isDebugEnabled()) {
            LOG.debug("Scheduling action for: " + target.getTarget());
        }

        try {
            ZonedDateTime now = ZonedDateTime.now();
            if (earliestAction.isBefore(now) || earliestAction.equals(now)) {
                LOG.debug("Action will be executed directly using callAsync()");
                List<String> results = SaltAPIService.INSTANCE
                        .callAsync(call, target, metadata)
                        .getMinions();
                return minions.stream()
                        .collect(Collectors.partitioningBy(minion ->
                                results.contains(minion.getMinionId())
                        ));
            }
            else {
                LOG.debug("Action will be scheduled for later using schedule()");
                Map<String, Schedule.Result> results = SaltAPIService.INSTANCE
                        .schedule("scheduled-action-" + actionIn.getId(), call, target,
                                earliestAction, metadata);
                return minions.stream()
                        .collect(Collectors.partitioningBy(minion ->
                                Optional.ofNullable(results.get(minion.getMinionId()))
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
        if (actionIn.getActionType().equals(ActionFactory.TYPE_ERRATA)) {
            return errataAction(minions, (ErrataAction) actionIn);
        }
        else if (actionIn.getActionType().equals(ActionFactory.TYPE_REBOOT)) {
            return rebootAction(minions);
        }
        else if (actionIn.getActionType().equals(ActionFactory.TYPE_SCRIPT_RUN)) {
            ScriptAction scriptAction = (ScriptAction) actionIn;
            String script = scriptAction.getScriptActionDetails().getScriptContents();
            return remoteCommandAction(minions, script);
        }
        else if (actionIn.getActionType().equals(ActionFactory.TYPE_APPLY_STATES)) {
            ApplyStatesAction applyStatesAction = (ApplyStatesAction) actionIn;
            String states = applyStatesAction.getDetails().getStates();
            return applyStatesAction(minions, states);
        }
        else {
            if (LOG.isDebugEnabled()) {
                LOG.debug("Action type " + actionIn.getActionType().getName() +
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
     */
    public void execute(Action actionIn) {
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
                    schedule(actionIn, call, targetMinions);

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
        // Convert errata names to LocalCall objects of type Pkg.install
        return collect.entrySet().stream().collect(Collectors.toMap(
                entry -> Pkg.install(true, entry.getKey()
                        .stream()
                        .map(patch -> "patch:" + patch)
                        .collect(Collectors.toList()))
                ,
                Map.Entry::getValue
        ));
    }

    private Map<LocalCall<?>, List<MinionServer>> rebootAction(List<MinionServer> minions) {
        Map<LocalCall<?>, List<MinionServer>> ret = new HashMap<>();
        ret.put(com.suse.manager.webui.utils.salt.System.reboot(Optional.empty()), minions);
        return ret;
    }

    private Map<LocalCall<?>, List<MinionServer>> remoteCommandAction(
            List<MinionServer> minions, String script) {
        Map<LocalCall<?>, List<MinionServer>> ret = new HashMap<>();
        // FIXME: This supports only bash at the moment
        ret.put(com.suse.manager.webui.utils.salt.Cmd.execCodeAll(
                "bash",
                // remove \r or bash will fail
                script.replaceAll("\r\n", "\n")), minions);
        return ret;
    }

    private Map<LocalCall<?>, List<MinionServer>> applyStatesAction(
            List<MinionServer> minions, String states) {
        Map<LocalCall<?>, List<MinionServer>> ret = new HashMap<>();
        ret.put(State.apply(Arrays.asList(states.split("\\s*,\\s*"))), minions);
        return ret;
    }
}
