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
import com.redhat.rhn.domain.action.dup.DistUpgradeAction;
import com.redhat.rhn.domain.action.dup.DistUpgradeChannelTask;
import com.redhat.rhn.domain.action.errata.ErrataAction;
import com.redhat.rhn.domain.action.rhnpackage.PackageRemoveAction;
import com.redhat.rhn.domain.action.rhnpackage.PackageUpdateAction;
import com.redhat.rhn.domain.action.salt.ApplyStatesAction;
import com.redhat.rhn.domain.action.scap.ScapAction;
import com.redhat.rhn.domain.action.scap.ScapActionDetails;
import com.redhat.rhn.domain.action.script.ScriptAction;
import com.redhat.rhn.domain.action.server.ServerAction;
import com.redhat.rhn.domain.channel.Channel;
import com.redhat.rhn.domain.errata.Errata;
import com.redhat.rhn.domain.server.MinionServer;
import com.redhat.rhn.domain.server.ServerFactory;
import com.redhat.rhn.manager.entitlement.EntitlementManager;

import com.suse.manager.reactor.messaging.ApplyStatesEventMessage;
import com.suse.manager.webui.services.impl.SaltService;
import com.suse.manager.webui.utils.MinionServerUtils;
import com.suse.manager.webui.utils.salt.custom.Openscap;
import com.suse.manager.webui.utils.salt.custom.ScheduleMetadata;
import com.suse.salt.netapi.calls.LocalCall;
import com.suse.salt.netapi.calls.modules.Cmd;
import com.suse.salt.netapi.calls.modules.Schedule;
import com.suse.salt.netapi.calls.modules.State;
import com.suse.salt.netapi.calls.modules.Test;
import com.suse.salt.netapi.datatypes.target.MinionList;
import com.suse.salt.netapi.exception.SaltException;
import com.suse.salt.netapi.results.Result;

import com.suse.utils.Opt;
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
    private static final String PARAM_PATCHES = "param_patches";
    private static final String UPDATE_STACK_ERRATA_KEYWORD = "restart_suggested";

    private Map<Boolean, List<MinionServer>> scheduleLater(
            List<MinionServer> minions,
            Map<String, ?> metadata,
            ZonedDateTime earliestAction,
            long actionId,
            LocalCall<?> call
    ) {
        LOG.debug("Action will be scheduled for later using schedule()");

        List<String> minionIds = minions
                .stream()
                .map(MinionServer::getMinionId)
                .collect(Collectors.toList());

        try {
            Map<String, Result<Schedule.Result>> results = SaltService.INSTANCE
                    .schedule("scheduled-action-" + actionId, call,
                            new MinionList(minionIds), earliestAction, metadata);

            Map<Boolean, List<MinionServer>> result = minions.stream()
                    .collect(Collectors.partitioningBy(minion ->
                    Optional.ofNullable(results.get(minion.getMinionId()))
                            .flatMap(Result::result)
                            .map(Schedule.Result::getResult)
                            .orElse(false)
            ));

            if (LOG.isDebugEnabled()) {
                result.get(true).forEach(minionServer -> {
                    LOG.debug("Successfully scheduled action for minion: " +
                            minionServer.getMinionId());
                });
            }
            return result;
        }
        catch (SaltException ex) {
            LOG.debug("Failed to schedule action: " + ex.getMessage());
            Map<Boolean, List<MinionServer>> result = new HashMap<>();
            result.put(true, Collections.emptyList());
            result.put(false, minions);
            return result;
        }
    }

    private Map<Boolean, List<MinionServer>> scheduleNow(
            List<MinionServer> minions,
            Map<String, ?> metadata,
            LocalCall<?> call,
            Action actionIn
    ) {
        LOG.debug("Action will be executed directly using callAsync()");

        List<String> minionIds = minions
                .stream()
                .map(MinionServer::getMinionId)
                .collect(Collectors.toList());

        try {
            final Map<String, Result<Boolean>> responding =
                    SaltService.INSTANCE.ping(new MinionList(minionIds));
            final List<String> present = minionIds.stream()
                    .filter(responding::containsKey)
                    .collect(Collectors.toList());
            LOG.debug(present.size() + " minions present out of " + minionIds.size());
            Map<Boolean, List<MinionServer>> result = new HashMap<>();

            if (present.isEmpty()) {
                result.put(true, Collections.emptyList());
                result.put(false, minions);
                return result;
            }
            else {
                List<String> results = SaltService.INSTANCE
                        .callAsync(call.withMetadata(metadata), new MinionList(present))
                        .getMinions();

                result = minions.stream()
                        .collect(Collectors.partitioningBy(minion ->
                                results.contains(minion.getMinionId()) &&
                                        present.contains(minion.getMinionId())
                        ));
                result.get(true).forEach(minionServer -> {
                    serverActionFor(actionIn, minionServer).ifPresent(serverAction -> {
                        if (LOG.isDebugEnabled()) {
                            LOG.debug("Successfully scheduled action for minion: " +
                                    minionServer.getMinionId());
                        }
                        serverAction.setStatus(ActionFactory.STATUS_PICKED_UP);
                        ActionFactory.save(serverAction);
                    });
                });
                return result;
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

        ZonedDateTime now = ZonedDateTime.now();
        if (earliestAction.isAfter(now)) {
            return scheduleLater(minions, metadata, earliestAction, actionIn.getId(), call);
        }
        else {
            return scheduleNow(minions, metadata, call, actionIn);
        }
    }

    /**
     * For a given action and list of minion servers return the salt call(s) that need to be
     * executed grouped by the list of targeted minions.
     *
     * @param actionIn the action to be executed
     * @param minions the list of minions to target
     * @return map of Salt local call to list of targeted minion servers
     */
    public Map<LocalCall<?>, List<MinionServer>> callsForAction(Action actionIn,
            List<MinionServer> minions) {
        ActionType actionType = actionIn.getActionType();
        if (ActionFactory.TYPE_ERRATA.equals(actionType)) {
            ErrataAction errataAction = (ErrataAction) actionIn;
            Set<Long> errataIds = errataAction.getErrata().stream()
                    .map(Errata::getId).collect(Collectors.toSet());
            return errataAction(minions, errataIds, errataAction.getErrata().stream()
                    .anyMatch(m -> m.hasKeyword(UPDATE_STACK_ERRATA_KEYWORD)));
        }
        else if (ActionFactory.TYPE_PACKAGES_UPDATE.equals(actionType)) {
            return packagesUpdateAction(minions, (PackageUpdateAction) actionIn);
        }
        else if (ActionFactory.TYPE_PACKAGES_REMOVE.equals(actionType)) {
            return packagesRemoveAction(minions, (PackageRemoveAction) actionIn);
        }
        else if (ActionFactory.TYPE_PACKAGES_REFRESH_LIST.equals(actionType)) {
            return packagesRefreshListAction(minions);
        }
        else if (ActionFactory.TYPE_HARDWARE_REFRESH_LIST.equals(actionType)) {
            return hardwareRefreshListAction(minions);
        }
        else if (ActionFactory.TYPE_REBOOT.equals(actionType)) {
            return rebootAction(minions);
        }
        else if (ActionFactory.TYPE_SCRIPT_RUN.equals(actionType)) {
            ScriptAction scriptAction = (ScriptAction) actionIn;
            String script = scriptAction.getScriptActionDetails().getScriptContents();
            return remoteCommandAction(minions, script);
        }
        else if (ActionFactory.TYPE_APPLY_STATES.equals(actionType)) {
            ApplyStatesAction applyStatesAction = (ApplyStatesAction) actionIn;
            return applyStatesAction(minions, applyStatesAction.getDetails().getMods());
        }
        else if (ActionFactory.TYPE_DIST_UPGRADE.equals(actionType)) {
            return distUpgradeAction((DistUpgradeAction) actionIn, minions);
        }
        else if (ActionFactory.TYPE_SCAP_XCCDF_EVAL.equals(actionType)) {
            ScapAction scapAction = (ScapAction)actionIn;
            return scapXccdfEvalAction(minions, scapAction.getScapActionDetails());
        }
        else {
            if (LOG.isDebugEnabled()) {
                LOG.debug("Action type " +
                        (actionType != null ? actionType.getName() : "") +
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
                        .filter(m -> m.getContactMethod().getLabel().equals("default"))
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

            results.get(false).forEach(minionServer -> {
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

    /**
     * This function will return a map with list of minions grouped by the
     * salt netapi local call that executes what needs to be executed on
     * those minions for the given errata ids and minions.
     *
     * @param minions list of minions
     * @param errataIds list of errata ids
     * @param isUpdateStackErrata set erratas to be handled as update stack errata
     * @return minions grouped by local call
     */
    public Map<LocalCall<?>, List<MinionServer>> errataAction(List<MinionServer> minions,
            Set<Long> errataIds, boolean isUpdateStackErrata) {
        // To prevent issues with outdated Salt version installed on the minion,
        // update stack erratas (which include Salt package) are always installed
        // using Salt 'pkg.installed' state instead the new 'pkg.patch_installed'
        // which might be not available on the minion Salt version. (bsc#1049139)
        return isUpdateStackErrata ? errataPackageInstallAction(minions, errataIds) :
                errataPatchInstallAction(minions, errataIds);
    }

    private Map<LocalCall<?>, List<MinionServer>> errataPackageInstallAction(
            List<MinionServer> minions, Set<Long> errataIds) {
        Set<Long> minionIds = minions.stream()
                .map(MinionServer::getId)
                .collect(Collectors.toSet());
        Map<Long, Map<String, String>> packageNames =
                ServerFactory.listNewestPkgsForServerErrata(minionIds, errataIds);
        // Group minions by packages that need to be updated
        Map<Map<String, String>, List<MinionServer>> collect = minions.stream().collect(
                Collectors.groupingBy(a -> packageNames.get(a.getId()))
        );
        return collect.entrySet().stream().collect(Collectors.toMap(
                    m -> State.apply(
                        Collections.singletonList(PACKAGES_PKGINSTALL),
                        Optional.of(Collections.singletonMap(PARAM_PKGS,
                            m.getKey().entrySet().stream().collect(Collectors.toMap(
                                Map.Entry::getKey,
                                Map.Entry::getValue)))),
                        Optional.of(true)
                    ),
                    Map.Entry::getValue));
    }

    private Map<LocalCall<?>, List<MinionServer>> errataPatchInstallAction(
            List<MinionServer> minions, Set<Long> errataIds) {
        Set<Long> minionIds = minions.stream()
                .map(MinionServer::getId)
                .collect(Collectors.toSet());
        Map<Long, Map<Long, Set<String>>> errataNames = ServerFactory
                .listErrataNamesForServers(minionIds, errataIds);
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
                        Optional.of(Collections.singletonMap(PARAM_PATCHES,
                                entry.getKey())),
                        Optional.of(true)
                ),
                Map.Entry::getValue));
    }

    private Map<LocalCall<?>, List<MinionServer>> packagesUpdateAction(
            List<MinionServer> minions, PackageUpdateAction action) {
        Map<LocalCall<?>, List<MinionServer>> ret = new HashMap<>();
        Map<String, String> pkgs = action.getDetails().stream().collect(Collectors.toMap(
                d -> d.getPackageName().getName(), d -> d.getEvr().toString(), (a, b)-> a));
        ret.put(State.apply(Arrays.asList(PACKAGES_PKGINSTALL),
                Optional.of(Collections.singletonMap(PARAM_PKGS, pkgs)),
                Optional.of(true)), minions);
        return ret;
    }

    private Map<LocalCall<?>, List<MinionServer>> packagesRemoveAction(
            List<MinionServer> minions, PackageRemoveAction action) {
        Map<LocalCall<?>, List<MinionServer>> ret = new HashMap<>();
        Map<String, String> pkgs = action.getDetails().stream().collect(Collectors.toMap(
                d -> d.getPackageName().getName(), d -> d.getEvr().toString(), (a, b)-> a));
        ret.put(State.apply(Arrays.asList(PACKAGES_PKGREMOVE),
                Optional.of(Collections.singletonMap(PARAM_PKGS, pkgs)),
                Optional.of(true)), minions);
        return ret;
    }

    private Map<LocalCall<?>, List<MinionServer>> packagesRefreshListAction(
            List<MinionServer> minions) {
        Map<LocalCall<?>, List<MinionServer>> ret = new HashMap<>();
        ret.put(State.apply(Arrays.asList(ApplyStatesEventMessage.PACKAGES_PROFILE_UPDATE),
                Optional.empty(), Optional.of(true)), minions);
        return ret;
    }

    private Map<LocalCall<?>, List<MinionServer>> hardwareRefreshListAction(
            List<MinionServer> minions) {
        Map<LocalCall<?>, List<MinionServer>> ret = new HashMap<>();

        // salt-ssh minions in the 'true' partition
        // regular minions in the 'false' partition
        Map<Boolean, List<MinionServer>> partitionBySSHPush = minions.stream()
                .collect(Collectors.partitioningBy(MinionServerUtils::isSshPushMinion));

        // Separate SSH push minions from regular minions to apply different states
        List<MinionServer> sshPushMinions = partitionBySSHPush.get(true);
        List<MinionServer> regularMinions = partitionBySSHPush.get(false);

        if (!sshPushMinions.isEmpty()) {
            ret.put(State.apply(Arrays.asList(
                    ApplyStatesEventMessage.HARDWARE_PROFILE_UPDATE),
                    Optional.empty(), Optional.of(true)), sshPushMinions);
        }
        if (!regularMinions.isEmpty()) {
            ret.put(State.apply(Arrays.asList(ApplyStatesEventMessage.SYNC_CUSTOM_ALL,
                    ApplyStatesEventMessage.HARDWARE_PROFILE_UPDATE),
                    Optional.empty(), Optional.of(true)), regularMinions);
        }

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
            List<MinionServer> minions, List<String> mods) {
        Map<LocalCall<?>, List<MinionServer>> ret = new HashMap<>();
        ret.put(State.apply(mods, Optional.empty(), Optional.of(true)), minions);
        return ret;
    }

    private Map<LocalCall<?>, List<MinionServer>> distUpgradeAction(
            DistUpgradeAction action,
            List<MinionServer> minions) {
        ZonedDateTime now = ZonedDateTime.now();
        ZonedDateTime earliestAction = action.getEarliestAction().toInstant()
                .atZone(ZoneId.systemDefault());
        Map<LocalCall<?>, List<MinionServer>> ret = new HashMap<>();
        if (earliestAction.isAfter(now)) {
            // The function we call itself is irrelevant since its just the
            // JobReturnEvent that with the action_id we are looking for.
            // Test.ping was only chosen because its simple and fast
            ret.put(Test.ping(), minions);
        }
        else {
            Map<Boolean, List<Channel>> collect = action.getDetails().getChannelTasks()
                    .stream().collect(Collectors.partitioningBy(
                            ct -> ct.getTask() == DistUpgradeChannelTask.SUBSCRIBE,
                            Collectors.mapping(DistUpgradeChannelTask::getChannel,
                                    Collectors.toList())
                    ));

            List<Channel> subbed = collect.get(true);
            List<Channel> unsubbed = collect.get(false);

            action.getServerActions()
                    .stream()
                    .flatMap(s -> Opt.stream(s.getServer().asMinionServer()))
                    .forEach(minion -> {
                        Set<Channel> currentChannels = minion.getChannels();
                        currentChannels.removeAll(unsubbed);
                        currentChannels.addAll(subbed);
                        ServerFactory.save(minion);
                        SaltStateGeneratorService.INSTANCE.generatePillar(minion);
                    });

            Map<String, Object> pillar = new HashMap<>();
            Map<String, Object> susemanager = new HashMap<>();
            pillar.put("susemanager", susemanager);
            Map<String, Object> distupgrade = new HashMap<>();
            susemanager.put("distupgrade", distupgrade);
            distupgrade.put("dryrun", action.getDetails().isDryRun());
            distupgrade.put("channels", subbed.stream()
                    .map(c -> "susemanager:" + c.getLabel())
                    .collect(Collectors.toList()));

            LocalCall<Map<String, State.ApplyResult>> distUpgrade = State.apply(
                    Collections.singletonList(ApplyStatesEventMessage.DISTUPGRADE),
                    Optional.of(pillar),
                    Optional.of(true)
            );
            ret.put(distUpgrade, minions);
        }
        return ret;
    }

    private Map<LocalCall<?>, List<MinionServer>> scapXccdfEvalAction(
            List<MinionServer> minions, ScapActionDetails scapActionDetails) {
        Map<LocalCall<?>, List<MinionServer>> ret = new HashMap<>();
        String parameters = "eval " +
            scapActionDetails.getParametersContents() + " " + scapActionDetails.getPath();
        ret.put(Openscap.xccdf(parameters), minions);
        return ret;
    }

}
