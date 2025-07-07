/*
 * Copyright (c) 2016--2024 SUSE LLC
 *
 * This software is licensed to you under the GNU General Public License,
 * version 2 (GPLv2). There is NO WARRANTY for this software, express or
 * implied, including the implied warranties of MERCHANTABILITY or FITNESS
 * FOR A PARTICULAR PURPOSE. You should have received a copy of GPLv2
 * along with this software; if not, see
 * http://www.gnu.org/licenses/old-licenses/gpl-2.0.txt.
 */
package com.suse.manager.webui.services;

import static com.redhat.rhn.common.hibernate.HibernateFactory.unproxy;
import static com.redhat.rhn.domain.action.ActionFactory.STATUS_COMPLETED;
import static com.redhat.rhn.domain.action.ActionFactory.STATUS_FAILED;
import static com.redhat.rhn.domain.action.ActionFactory.STATUS_QUEUED;
import static java.util.Collections.emptyMap;
import static java.util.Collections.singletonList;
import static java.util.Collections.singletonMap;
import static java.util.Optional.ofNullable;
import static java.util.stream.Collectors.partitioningBy;
import static java.util.stream.Collectors.toList;
import static java.util.stream.Collectors.toMap;

import com.redhat.rhn.GlobalInstanceHolder;
import com.redhat.rhn.common.RhnRuntimeException;
import com.redhat.rhn.common.hibernate.HibernateFactory;
import com.redhat.rhn.domain.action.Action;
import com.redhat.rhn.domain.action.ActionChain;
import com.redhat.rhn.domain.action.ActionChainEntry;
import com.redhat.rhn.domain.action.ActionChainFactory;
import com.redhat.rhn.domain.action.ActionFactory;
import com.redhat.rhn.domain.action.ActionStatus;
import com.redhat.rhn.domain.action.ActionType;
import com.redhat.rhn.domain.action.HardwareRefreshAction;
import com.redhat.rhn.domain.action.RebootAction;
import com.redhat.rhn.domain.action.ansible.InventoryAction;
import com.redhat.rhn.domain.action.ansible.PlaybookAction;
import com.redhat.rhn.domain.action.appstream.AppStreamAction;
import com.redhat.rhn.domain.action.appstream.AppStreamActionDetails;
import com.redhat.rhn.domain.action.channel.SubscribeChannelsAction;
import com.redhat.rhn.domain.action.config.ConfigAction;
import com.redhat.rhn.domain.action.dup.DistUpgradeAction;
import com.redhat.rhn.domain.action.errata.ErrataAction;
import com.redhat.rhn.domain.action.kickstart.KickstartAction;
import com.redhat.rhn.domain.action.kickstart.KickstartInitiateAction;
import com.redhat.rhn.domain.action.rhnpackage.PackageLockAction;
import com.redhat.rhn.domain.action.rhnpackage.PackageRefreshListAction;
import com.redhat.rhn.domain.action.rhnpackage.PackageRemoveAction;
import com.redhat.rhn.domain.action.rhnpackage.PackageUpdateAction;
import com.redhat.rhn.domain.action.salt.ApplyStatesAction;
import com.redhat.rhn.domain.action.salt.build.ImageBuildAction;
import com.redhat.rhn.domain.action.salt.inspect.ImageInspectAction;
import com.redhat.rhn.domain.action.scap.ScapAction;
import com.redhat.rhn.domain.action.script.ScriptAction;
import com.redhat.rhn.domain.action.script.ScriptRunAction;
import com.redhat.rhn.domain.action.server.ServerAction;
import com.redhat.rhn.domain.errata.Errata;
import com.redhat.rhn.domain.product.Tuple2;
import com.redhat.rhn.domain.server.ErrataInfo;
import com.redhat.rhn.domain.server.MinionServer;
import com.redhat.rhn.domain.server.MinionServerFactory;
import com.redhat.rhn.domain.server.MinionSummary;
import com.redhat.rhn.domain.server.Server;
import com.redhat.rhn.domain.server.ServerFactory;
import com.redhat.rhn.domain.user.User;
import com.redhat.rhn.manager.action.ActionManager;
import com.redhat.rhn.manager.system.SystemManager;
import com.redhat.rhn.taskomatic.TaskomaticApi;
import com.redhat.rhn.taskomatic.TaskomaticApiException;

import com.suse.manager.utils.SaltKeyUtils;
import com.suse.manager.utils.SaltUtils;
import com.suse.manager.webui.services.iface.SaltApi;
import com.suse.manager.webui.services.impl.SaltSSHService;
import com.suse.manager.webui.utils.SaltModuleRun;
import com.suse.manager.webui.utils.SaltState;
import com.suse.manager.webui.utils.SaltSystemReboot;
import com.suse.manager.webui.utils.salt.custom.MgrActionChains;
import com.suse.manager.webui.utils.salt.custom.ScheduleMetadata;
import com.suse.salt.netapi.calls.LocalAsyncResult;
import com.suse.salt.netapi.calls.LocalCall;
import com.suse.salt.netapi.calls.modules.State;
import com.suse.salt.netapi.calls.modules.State.ApplyResult;
import com.suse.salt.netapi.datatypes.target.MinionList;
import com.suse.salt.netapi.errors.GenericError;
import com.suse.salt.netapi.exception.SaltException;
import com.suse.salt.netapi.results.Result;
import com.suse.salt.netapi.results.Ret;
import com.suse.salt.netapi.results.StateApplyResult;
import com.suse.salt.netapi.utils.Xor;
import com.suse.utils.Json;
import com.suse.utils.Opt;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonPrimitive;
import com.google.gson.JsonSyntaxException;
import com.google.gson.reflect.TypeToken;

import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.lang3.tuple.ImmutablePair;
import org.apache.commons.lang3.tuple.Pair;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.time.DateTimeException;
import java.time.Duration;
import java.time.Instant;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.ZoneId;
import java.time.temporal.ChronoUnit;
import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.Date;
import java.util.Deque;
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
public class SaltServerActionService {

    /* Logger for this class */
    private static final Logger LOG = LogManager.getLogger(SaltServerActionService.class);
    public static final String PACKAGES_PKGINSTALL = "packages.pkginstall";
    private static final String PACKAGES_PKGDOWNLOAD = "packages.pkgdownload";
    public static final String PACKAGES_PATCHINSTALL = "packages.patchinstall";
    private static final String PACKAGES_PATCHDOWNLOAD = "packages.patchdownload";
    private static final String PARAM_PKGS = "param_pkgs";
    private static final String PARAM_PATCHES = "param_patches";
    private static final String SYSTEM_REBOOT = "system.reboot";
    private static final String COCOATTEST_REQUESTDATA = "cocoattest.requestdata";
    public static final String APPSTREAMS_CONFIGURE = "appstreams.configure";
    public static final String PARAM_APPSTREAMS_ENABLE = "param_appstreams_enable";
    public static final String PARAM_APPSTREAMS_DISABLE = "param_appstreams_disable";

    /** SLS pillar parameter name for the list of update stack patch names. */
    public static final String PARAM_UPDATE_STACK_PATCHES = "param_update_stack_patches";

    /** SLS pillar parameter name for the list of regular patch names. */
    public static final String PARAM_REGULAR_PATCHES = "param_regular_patches";
    public static final String ALLOW_VENDOR_CHANGE = "allow_vendor_change";


    private boolean commitTransaction = true;

    private SaltActionChainGeneratorService saltActionChainGeneratorService =
            SaltActionChainGeneratorService.INSTANCE;

    private SaltApi saltApi;
    private final SaltSSHService saltSSHService = GlobalInstanceHolder.SALT_API.getSaltSSHService();
    private SaltUtils saltUtils;
    private final SaltKeyUtils saltKeyUtils;
    private TaskomaticApi taskomaticApi = new TaskomaticApi();

    /**
     * @param saltApiIn instance for getting information from a system.
     * @param saltUtilsIn salt utils instance to use
     * @param saltKeyUtilsIn salt key utils instance to use
     */
    public SaltServerActionService(SaltApi saltApiIn, SaltUtils saltUtilsIn, SaltKeyUtils saltKeyUtilsIn) {
        this.saltApi = saltApiIn;
        this.saltUtils = saltUtilsIn;
        this.saltKeyUtils = saltKeyUtilsIn;
    }

    /**
     * For a given action return the salt call(s) that need to be executed for the minions involved.
     *
     * @param actionIn the action to be executed
     * @return map of Salt local call to list of targeted minion summaries
     */
    public Map<LocalCall<?>, List<MinionSummary>> callsForAction(Action actionIn) {
        List<MinionSummary> minionSummaries = MinionServerFactory.findAllMinionSummaries(actionIn.getId());
        return callsForAction(actionIn, minionSummaries);
    }

    /**
     * For a given action return the salt call(s) that need to be executed for the targeted minions.
     *
     * @param actionIn the action to be executed
     * @param minions the list of minion summaries to target
     * @return map of Salt local call to list of targeted minion summaries
     */
    public Map<LocalCall<?>, List<MinionSummary>> callsForAction(Action actionIn, List<MinionSummary> minions) {
        if (minions.isEmpty()) {
            return Collections.emptyMap();
        }

        ActionType actionType = actionIn.getActionType();
        actionIn = unproxy(actionIn);
        if (ActionFactory.TYPE_ERRATA.equals(actionType)) {
            ErrataAction errataAction = (ErrataAction) actionIn;
            Set<Long> errataIds = errataAction.getErrata().stream()
                    .map(Errata::getId).collect(Collectors.toSet());
            return errataAction(minions, errataIds, errataAction.getDetails().getAllowVendorChange());
        }
        else if (ActionFactory.TYPE_PACKAGES_LOCK.equals(actionType)) {
            return PackageLockAction.packagesLockAction(minions, (PackageLockAction) actionIn);
        }
        else if (ActionFactory.TYPE_PACKAGES_UPDATE.equals(actionType)) {
            return PackageUpdateAction.packagesUpdateAction(minions, (PackageUpdateAction) actionIn);
        }
        else if (ActionFactory.TYPE_PACKAGES_REMOVE.equals(actionType)) {
            return PackageRemoveAction.packagesRemoveAction(minions, (PackageRemoveAction) actionIn);
        }
        else if (ActionFactory.TYPE_PACKAGES_REFRESH_LIST.equals(actionType)) {
            return PackageRefreshListAction.packagesRefreshListAction(minions);
        }
        else if (ActionFactory.TYPE_HARDWARE_REFRESH_LIST.equals(actionType)) {
            return HardwareRefreshAction.hardwareRefreshListAction(minions);
        }
        else if (ActionFactory.TYPE_REBOOT.equals(actionType)) {
            return RebootAction.rebootAction(minions);
        }
        else if (ActionFactory.TYPE_CONFIGFILES_DEPLOY.equals(actionType)) {
            return ConfigAction.deployFiles(minions, (ConfigAction) actionIn);
        }
        else if (ActionFactory.TYPE_CONFIGFILES_DIFF.equals(actionType)) {
            return ConfigAction.diffFiles(minions, (ConfigAction) actionIn);
        }
        else if (ActionFactory.TYPE_SCRIPT_RUN.equals(actionType)) {
            return ScriptRunAction.remoteCommandAction(minions, (ScriptAction) actionIn);
        }
        else if (ActionFactory.TYPE_APPLY_STATES.equals(actionType)) {
            return ApplyStatesAction.applyStatesAction(minions, (ApplyStatesAction) actionIn);
        }
        else if (ActionFactory.TYPE_IMAGE_INSPECT.equals(actionType)) {
            return ImageInspectAction.imageInspectAction(minions, (ImageInspectAction) actionIn);
        }
        else if (ActionFactory.TYPE_IMAGE_BUILD.equals(actionType)) {
            return ImageBuildAction.imageBuildAction(minions, (ImageBuildAction) actionIn);
        }
        else if (ActionFactory.TYPE_DIST_UPGRADE.equals(actionType)) {
            return DistUpgradeAction.distUpgradeAction(minions, (DistUpgradeAction) actionIn);
        }
        else if (ActionFactory.TYPE_SCAP_XCCDF_EVAL.equals(actionType)) {
            return ScapAction.scapXccdfEvalAction(minions, (ScapAction)actionIn);
        }
        else if (ActionFactory.TYPE_SUBSCRIBE_CHANNELS.equals(actionType)) {
            return SubscribeChannelsAction.subscribeChannelsAction(minions, saltApi, (SubscribeChannelsAction)actionIn);
        }
        else if (ActionFactory.TYPE_KICKSTART_INITIATE.equals(actionType)) {
            return KickstartInitiateAction.autoinstallInitAction(minions, (KickstartInitiateAction)actionIn);
        }
        else if (ActionFactory.TYPE_PLAYBOOK.equals(actionType)) {
            return PlaybookAction.playbookAction(minions, (PlaybookAction) actionIn);
        }
        else if (ActionFactory.TYPE_INVENTORY.equals(actionType)) {
            return InventoryAction.inventoryAction(minions, (InventoryAction) actionIn);
        }
        else if (ActionFactory.TYPE_COCO_ATTESTATION.equals(actionType)) {
            return cocoAttestationAction(minions);
        }
        else if (ActionFactory.TYPE_APPSTREAM_CONFIGURE.equals(actionType)) {
            return appStreamAction(minions, (AppStreamAction) actionIn);
        }
        else {
            if (LOG.isDebugEnabled()) {
                LOG.debug("Action type {} is not supported with Salt", actionType != null ? actionType.getName() : "");
            }
            return Collections.emptyMap();
        }
    }

    /**
     * Execute a given {@link Action} via salt.
     *
     * @param actionIn the action to execute
     * @param forcePackageListRefresh add metadata to force a package list
     * refresh
     * @param isStagingJob whether the action is a staging of packages
     * action
     * @param stagingJobMinionServerId if action is a staging action it will
     * contain involved minionId(s)
     */
    public void execute(Action actionIn, boolean forcePackageListRefresh,
            boolean isStagingJob, Optional<Long> stagingJobMinionServerId) {

        List<MinionSummary> allMinions = MinionServerFactory.findQueuedMinionSummaries(actionIn.getId());
        if (CollectionUtils.isEmpty(allMinions)) {
            LOG.warn("Unable to find any minion that have the action id={} in status QUEUED", actionIn.getId());
            return;
        }

        // split minions into regular and salt-ssh
        Map<Boolean, List<MinionSummary>> partitionBySSHPush = allMinions.stream()
                .collect(Collectors.partitioningBy(MinionSummary::isSshPush));

        // Separate SSH push minions from regular minions to apply different states
        List<MinionSummary> sshMinionSummaries = partitionBySSHPush.get(true);
        List<MinionSummary> regularMinionSummaries = partitionBySSHPush.get(false);

        if (!regularMinionSummaries.isEmpty()) {
            executeForRegularMinions(actionIn, forcePackageListRefresh, isStagingJob, stagingJobMinionServerId,
                    regularMinionSummaries);
        }

        List<MinionServer> sshPushMinions = MinionServerFactory.findMinionsByServerIds(
                sshMinionSummaries.stream().map(MinionSummary::getServerId).collect(Collectors.toList()));

        if (!sshPushMinions.isEmpty()) {
            for (MinionServer sshMinion : sshPushMinions) {
                try {
                    taskomaticApi.scheduleSSHActionExecution(actionIn, sshMinion, forcePackageListRefresh);
                }
                catch (TaskomaticApiException e) {
                    LOG.error("Couldn't schedule SSH action id={} minion={}",
                            actionIn.getId(), sshMinion.getMinionId(), e);
                }
            }
        }
    }

    private void executeForRegularMinions(Action actionIn, boolean forcePackageListRefresh,
            boolean isStagingJob, Optional<Long> stagingJobMinionServerId, List<MinionSummary> minionSummaries) {
        Map<LocalCall<?>, List<MinionSummary>> localCalls = new HashMap<>();
        try {
             localCalls = callsForAction(actionIn, minionSummaries);
        }
        catch (RuntimeException e) {
            LOG.error("Failed to prepare salt calls: ", e);
            List<Long> failedServerIds  = minionSummaries.stream()
                    .map(MinionSummary::getServerId).toList();
            if (!failedServerIds.isEmpty()) {
                actionIn.getServerActions().stream()
                        .filter(sa -> failedServerIds.contains(sa.getServer().getId()))
                        .forEach(sa -> {
                            sa.setStatus(STATUS_FAILED);
                            sa.setResultMsg("Error preparing salt call: " + e.getMessage());
                            sa.setCompletionTime(new Date());
                        });
            }
            return;
        }
        for (Map.Entry<LocalCall<?>, List<MinionSummary>> entry : localCalls.entrySet()) {
            LocalCall<?> call = entry.getKey();
            final List<MinionSummary> targetMinions;
            Map<Boolean, List<MinionSummary>> results;

            if (isStagingJob) {
                targetMinions = new ArrayList<>();
                stagingJobMinionServerId.flatMap(MinionServerFactory::lookupById)
                        .ifPresent(server -> targetMinions.add(new MinionSummary(server)));
                call = prepareStagingTargets(actionIn, targetMinions);
            }
            else {
                targetMinions = entry.getValue();
            }

            LOG.debug("Executing action {} for {} minions.", actionIn.getId(), targetMinions.size());
            results = execute(actionIn, call, targetMinions, forcePackageListRefresh, isStagingJob);
            LOG.debug(
                "Finished action {}. Picked up for {} minions and failed for {} minions.",
                actionIn.getId(),
                results.get(true).size(),
                results.get(false).size()
            );

            if (!isStagingJob) {
                List<Long> succeededServerIds = results.get(true).stream()
                        .map(MinionSummary::getServerId).collect(toList());
                if (!succeededServerIds.isEmpty()) {
                    ActionFactory.updateServerActionsPickedUp(actionIn, succeededServerIds);
                }
                List<Long> failedServerIds  = results.get(false).stream()
                        .map(MinionSummary::getServerId).collect(toList());
                if (!failedServerIds.isEmpty()) {
                    ActionFactory.updateServerActions(actionIn, failedServerIds, ActionFactory.STATUS_FAILED);
                }
            }
        }
    }

    /**
     * Call Salt to start the execution of the given action chain.
     *
     * @param actionChain the action chain to execute
     * @param targetMinions a list containing target minions
     */
    private void startActionChainExecution(ActionChain actionChain, Set<MinionSummary> targetMinions) {
        // prepare the start action chain call
        Map<Boolean, ? extends Collection<MinionSummary>> results =
                callAsyncActionChainStart(actionChain, targetMinions);

        results.get(false).forEach(minionSummary -> {
            LOG.warn("Failed to schedule action chain for minion: {}", minionSummary.getMinionId());
            Optional<Long> firstActionId = actionChain.getEntries().stream()
                    .sorted(Comparator.comparingInt(ActionChainEntry::getSortOrder))
                    .map(ActionChainEntry::getAction)
                    .map(Action::getId)
                    .findFirst();
            failActionChain(minionSummary.getMinionId(), Optional.of(actionChain.getId()), firstActionId,
                    Optional.of("Got empty result."));
        });
    }

    private void startSSHActionChain(ActionChain actionChain, Set<MinionSummary> sshMinions,
                                     Optional<String> extraFilerefs) {
        // use a state to start the action chain in order to trick salt-ssh into including the
        // mgractionchains custom module in the thin-dir tarball
        LocalCall<Map<String, ApplyResult>> call =
                State.apply(singletonList("actionchains.startssh"),
                        Optional.of(singletonMap("actionchain_id", actionChain.getId())));

        Optional<Long> firstActionId = actionChain.getEntries().stream()
                .sorted(Comparator.comparingInt(ActionChainEntry::getSortOrder))
                .map(ActionChainEntry::getAction)
                .map(Action::getId)
                .findFirst();

        // start the action chain synchronously
        try {
            // first check if there's an action chain with a reboot already executing
            Map<String, Result<Map<String, String>>> pendingResumeConf = saltApi.getPendingResume(
                    sshMinions.stream().map(MinionSummary::getMinionId)
                            .collect(Collectors.toList())
            );
            List<MinionSummary> targetSSHMinions = sshMinions.stream()
                    .filter(sshMinion -> {
                        Optional<Map<String, String>> confValues = pendingResumeConf.get(sshMinion.getMinionId())
                                .fold(err -> {
                                            LOG.error("mgractionchains.get_pending_resume failed: {}", err.fold(
                                                    Object::toString,
                                                    Object::toString,
                                                    Object::toString,
                                                    Object::toString,
                                                    Object::toString
                                            ));
                                        return Optional.empty();
                                    },
                                        Optional::of);
                        if (confValues.orElse(Collections.emptyMap()).isEmpty()) {
                            // all good, no action chain currently executing on the minion
                            return true;
                        }
                        // fail the action chain because concurrent execution is not possible
                        LOG.warn("Minion {} has an action chain execution in progress", sshMinion.getMinionId());
                        failActionChain(sshMinion.getMinionId(), Optional.of(actionChain.getId()), firstActionId,
                                Optional.of("An action chain execution is already in progress. " +
                                        "Concurrent action chain execution is not allowed. " +
                                        "If the execution became stale remove directory " +
                                        "/var/tmp/.root_XXXX_salt/minion.d manually."));
                        return false;
                    }).toList();

            if (targetSSHMinions.isEmpty()) {
                // do nothing, no targets
                return;
            }

            Map<String, Result<Map<String, ApplyResult>>> res = saltSSHService.callSyncSSH(call,
                    new MinionList(targetSSHMinions.stream().map(MinionSummary::getMinionId)
                            .collect(Collectors.toList())),
                    extraFilerefs);

            res.forEach((minionId, chunkResult) -> {
                if (chunkResult.result().isPresent()) {
                    handleActionChainSSHResult(firstActionId, minionId, chunkResult.result().get());
                }
                else {
                    String errMsg = chunkResult.error().map(saltErr -> saltErr.fold(
                            e ->  {
                                LOG.error(e);
                                return "Function " + e.getFunctionName() + " not available";
                            },
                            e ->  {
                                LOG.error(e);
                                return "Module " + e.getModuleName() + " not supported";
                            },
                            e ->  {
                                LOG.error(e);
                                return "Error parsing JSON: " + e.getJson();
                            },
                            e ->  {
                                LOG.error(e);
                                return "Salt error: " + e.getMessage();
                            },
                            e -> {
                                LOG.error(e);
                                return "Salt SSH error: " + e.getRetcode() + " " + e.getMessage();
                            }
                    )).orElse("Unknonw error");
                    // no result, fail the entire chain
                    failActionChain(minionId, Optional.of(actionChain.getId()), firstActionId,
                            Optional.of(errMsg));
                }
            });

        }
        catch (SaltException e) {
            LOG.error("Error handling action chain execution: ", e);
            // fail the entire chain
            sshMinions.forEach(minion -> failActionChain(minion.getMinionId(), Optional.of(actionChain.getId()),
                    firstActionId, Optional.of("Error handling action chain execution: " + e.getMessage())));
        }
    }

    /**
     * Handle result of applying an action chain.
     * @param firstChunkActionId id of the first action in the chunk
     * @param minionId minion id
     * @param chunkResult result of applying the chunk
     * @return true if the result could be handled
     */
    public boolean handleActionChainSSHResult(Optional<Long> firstChunkActionId, String minionId,
                                              Map<String, ApplyResult> chunkResult) {
        try {
            // mgractionchains.start is executed via state.apply, get the actual output of the module
            // fist look for start result
            StateApplyResult<JsonElement> stateApplyResult =
                    chunkResult.get("mgrcompat_|-startssh_|-mgractionchains.start_|-module_run");

            if (stateApplyResult == null) {
                // if no start result, look for resume
                stateApplyResult =
                        chunkResult.get("mgrcompat_|-resumessh_|-mgractionchains.resume_|-module_run");
            }

            if (stateApplyResult == null) {
                LOG.error("No action chain result for minion {}", minionId);
                failActionChain(minionId, firstChunkActionId, Optional.of("No action chain result"));
            }
            else if (!stateApplyResult.isResult() && (stateApplyResult.getChanges() == null ||
                    (stateApplyResult.getChanges().isJsonObject()) &&
                            ((JsonObject)stateApplyResult.getChanges()).size() == 0)) {
                LOG.error("Error handling action chain execution: {}", stateApplyResult.getComment());
                failActionChain(minionId, firstChunkActionId, Optional.of(stateApplyResult.getComment()));
            }
            else if (stateApplyResult.getChanges() != null) {
                // handle the result
                Optional<Map<String, StateApplyResult<Ret<JsonElement>>>> optActionChainResult = getActionChainResult(
                        stateApplyResult.getChanges(), minionId, firstChunkActionId);
                if (optActionChainResult.isEmpty()) {
                    return false;
                }
                Map<String, StateApplyResult<Ret<JsonElement>>> actionChainResult = optActionChainResult.get();
                handleActionChainResult(minionId, "",
                        actionChainResult,
                        // skip reboot, needs special handling
                        stateResult ->
                                stateResult.getName().map(x -> x.fold(Arrays::asList, List::of)
                                        .contains(SYSTEM_REBOOT)).orElse(false));

                boolean refreshPkg = false;
                Optional<User> scheduler = Optional.empty();
                for (Map.Entry<String, StateApplyResult<Ret<JsonElement>>> entry : actionChainResult.entrySet()) {
                    String stateIdKey = entry.getKey();
                    StateApplyResult<Ret<JsonElement>> stateResult = entry.getValue();

                    Optional<SaltActionChainGeneratorService.ActionChainStateId> actionChainStateId =
                            SaltActionChainGeneratorService.parseActionChainStateId(stateIdKey);
                    if (actionChainStateId.isPresent()) {
                        SaltActionChainGeneratorService.ActionChainStateId stateId = actionChainStateId.get();
                        // only reboot needs special handling,
                        // for salt pkg update there's no need to split the sls in case of salt-ssh minions

                        Action action = ActionFactory.lookupById(stateId.getActionId());
                        if (stateResult.getName().map(x -> x.fold(Arrays::asList, List::of)
                                .contains(SYSTEM_REBOOT)).orElse(false) && stateResult.isResult() &&
                                action.getActionType().equals(ActionFactory.TYPE_REBOOT)) {

                            Optional<ServerAction> rebootServerAction =
                                    action.getServerActions().stream()
                                            .filter(sa -> sa.getServer().asMinionServer()
                                                    .map(m -> m.getMinionId().equals(minionId)).orElse(false))
                                            .findFirst();
                            rebootServerAction.ifPresentOrElse(
                                    ract -> {
                                        if (ract.getStatus().equals(ActionFactory.STATUS_QUEUED)) {
                                            setActionAsPickedUp(ract);
                                        }
                                    },
                                    () -> LOG.error("Action of type {} found in action chain result but not " +
                                            "in actions for minion {}", SYSTEM_REBOOT, minionId));
                        }

                        if (stateResult.isResult() &&
                                saltUtils.shouldRefreshPackageList(stateResult.getName(),
                                        Optional.of(stateResult.getChanges().getRet()))) {
                            scheduler = Optional.ofNullable(action.getSchedulerUser());
                            refreshPkg = true;
                        }
                    }
                }
                Optional<MinionServer> minionServer = MinionServerFactory.findByMinionId(minionId);
                if (refreshPkg) {
                    Optional<User> finalScheduler = scheduler;
                    minionServer.ifPresent(minion -> {
                        LOG.info("Scheduling a package profile update for minion {}", minionId);
                        try {
                            ActionManager.schedulePackageRefresh(finalScheduler, minion);
                        }
                        catch (TaskomaticApiException e) {
                            LOG.error("Could not schedule package refresh for minion: {}", minion.getMinionId(), e);
                        }
                    });
                }
                // update minion last checkin
                minionServer.ifPresent(Server::updateServerInfo);
            }
            else {
                LOG.error("'state.apply mgractionchains.startssh' was successful " +
                        "but not state apply changes are present");
                failActionChain(minionId, firstChunkActionId, Optional.of("Got null result."));
                return false;
            }
        }
        catch (Exception e) {
            LOG.error("Error handling action chain result for SSH minion {}", minionId, e);
            failActionChain(minionId, firstChunkActionId,
                    Optional.of("Error handling action chain result:" + e.getMessage()));
            return false;
        }
        return true;
    }

    private Optional<Map<String, StateApplyResult<Ret<JsonElement>>>> getActionChainResult(
            JsonElement stateChanges, String minionId, Optional<Long> firstChunkActionId) {
        Map<String, StateApplyResult<Ret<JsonElement>>> actionChainResult;
        try {
            Ret<Map<String, StateApplyResult<Ret<JsonElement>>>> actionChainRet =
                    Json.GSON.fromJson(stateChanges,
                            new TypeToken<Ret<Map<String, StateApplyResult<Ret<JsonElement>>>>>() {
                            }.getType());
            actionChainResult = actionChainRet.getRet();
        }
        catch (JsonSyntaxException e) {
            LOG.error("Unexpected response: {}", stateChanges, e);
            String msg = stateChanges.toString();
            if ((stateChanges.isJsonObject()) &&
                    ((JsonObject)stateChanges).get("ret") != null) {
                msg = ((JsonObject)stateChanges).get("ret").toString();
            }
            failActionChain(minionId, firstChunkActionId, Optional.of("Unexpected response: " + msg));
            return Optional.empty();
        }
        return Optional.of(actionChainResult);
    }

    /**
     * Set the action and dependent actions to failed
     * @param minionId the minion id
     * @param failedActionId the failed action id
     * @param message the message to set to the failed action
     */
    public void failActionChain(String minionId, Optional<Long> failedActionId, Optional<String> message) {
        failActionChain(minionId, Optional.empty(), failedActionId, message);
    }

    private void failActionChain(String minionId, Optional<Long> actionChainId, Optional<Long> failedActionId,
                                        Optional<String> message) {
        failedActionId.ifPresent(last ->
                failDependentServerActions(last, minionId, message));
        MinionServerFactory.findByMinionId(minionId).ifPresent(minion -> SaltActionChainGeneratorService.INSTANCE
                .removeActionChainSLSFilesForMinion(minion.getMachineId(), actionChainId));
    }

    /**
     * Prepare and execute the action chain.
     *
     * @param actionChainId id of the action chain to execute
     */
    public void executeActionChain(long actionChainId) {
        ActionChain actionChain = ActionChainFactory
                .getActionChain(actionChainId)
                .orElseThrow(() -> new RuntimeException("Action chain id=" + actionChainId + " not found in db"));

        // for each minion populate a list of ServerActions with the corresponding Salt call(s)
        Map<MinionSummary, List<Pair<ServerAction, List<LocalCall<?>>>>> minionCalls = new HashMap<>();

        actionChain.getEntries().stream()
                .sorted(Comparator.comparingInt(ActionChainEntry::getSortOrder))
                .map(ActionChainEntry::getAction)
                .forEach(actionIn -> {
                    List<MinionSummary> minions = MinionServerFactory.findAllMinionSummaries(actionIn.getId());

                    if (minions.isEmpty()) {
                        // When an Action Chain contains an Action which does not target
                        // any minion we don't generate any Salt call.
                        LOG.warn("No server actions for action id={}", actionIn.getId());
                        return;
                    }

                    // get Salt calls for this action
                    Map<LocalCall<?>, List<MinionSummary>> actionCalls = callsForAction(actionIn, minions);

                    // TODO how to handle staging jobs?

                    // Salt calls for each minion
                    Map<MinionSummary, List<LocalCall<?>>> callsPerMinion =
                            actionCalls.values().stream().flatMap(Collection::stream)
                                .collect(Collectors
                                        .toMap(Function.identity(),
                                                m -> actionCalls.entrySet()
                                                        .stream()
                                                        .filter(e -> e.getValue().contains(m))
                                                        .map(Map.Entry::getKey)
                                                        .collect(Collectors.toList())
                                ));

                    // append the Salt calls for this action to the list of calls of each minion
                    callsPerMinion.forEach((minion, calls) -> {
                        List<Pair<ServerAction, List<LocalCall<?>>>> currentCalls = minionCalls
                                .getOrDefault(minion, new ArrayList<>());
                        Optional<ServerAction> serverAction = actionIn.getServerActions().stream()
                                .filter(sa -> sa.getServer().getId().equals(minion.getServerId()))
                                .findFirst();
                        serverAction.ifPresent(sa -> {
                            Pair<ServerAction, List<LocalCall<?>>> serverActionCalls =
                                    new ImmutablePair<>(sa, calls);
                            currentCalls.add(serverActionCalls);
                        });
                        minionCalls.put(minion, currentCalls);
                    });

                });

        // split minions into regular and salt-ssh
        Map<Boolean, Set<MinionSummary>> minionPartitions =
                minionCalls.keySet().stream()
                        .collect(Collectors.partitioningBy(MinionSummary::isSshPush, Collectors.toSet()));

        Set<MinionSummary> sshMinionIds = minionPartitions.get(true);
        Set<MinionSummary> regularMinionIds = minionPartitions.get(false);

        // convert local calls to salt state objects
        Map<MinionSummary, List<SaltState>> statesPerMinion = new HashMap<>();
        minionCalls.forEach((minion, serverActionCalls) -> {
            List<SaltState> states = serverActionCalls.stream()
                    .flatMap(saCalls -> {
                        ServerAction sa = saCalls.getKey();
                        List<LocalCall<?>> calls = saCalls.getValue();
                        return convertToState(actionChain.getId(), sa, calls, minion).stream();
                    }).collect(Collectors.toList());

            statesPerMinion.put(minion, states);
        });

        // Compute the additional sls files to be included in the state tarball for ssh-push minions.
        // This is needed because we're using module.run + state.apply to apply the corresponding
        // action states and this breaks the introspection that Salt does in order to figure out
        // what files it needs to included in the state tarball.
        Optional<String> extraFilerefs = Optional.empty();
        if (!sshMinionIds.isEmpty()) {
            Map<MinionSummary, Integer> chunksPerMinion =
                    saltActionChainGeneratorService.getChunksPerMinion(statesPerMinion);

            // If there are highstate apply actions the corresponding top files must be generated
            // because we're applying highstates using state.top instead of state.apply.
            // This is due to module.run + state.apply highstate not working properly even if there's
            // a top.sls included in the state tarball.
            Map<MinionSummary, List<Long>> highstateActionPerMinion =
                    saltSSHService.findApplyHighstateActionsPerMinion(statesPerMinion);

            List<String> fileRefsList = new LinkedList<>();
            highstateActionPerMinion.forEach((minion, highstateActionIds) -> {
                for (Long highstateActionId: highstateActionIds) {
                    // generate a top files for each highstate apply action
                    Pair<String, List<String>> highstateTop = saltSSHService
                            .generateTopFile(actionChainId, highstateActionId);
                    fileRefsList.add(highstateTop.getKey());
                }
            });

            // collect additional files (e.g. salt://scripts/script_xxx.sh) referenced from the action
            // chain sls file
            String extraFilerefsStr = saltSSHService
                    .findStatesExtraFilerefs(actionChain.getId(), chunksPerMinion, statesPerMinion);
            fileRefsList.add(extraFilerefsStr);

            // join extra files and tops
            extraFilerefs = Optional.of(String.join(",", fileRefsList));

        }

        // render the action chain sls files
        for (Map.Entry<MinionSummary, List<SaltState>> entry: statesPerMinion.entrySet()) {
            saltActionChainGeneratorService
                    .createActionChainSLSFiles(actionChain, entry.getKey(), entry.getValue(),
                            entry.getKey().isSshPush() ? extraFilerefs : Optional.empty());
        }

        // start the execution
        if (!regularMinionIds.isEmpty()) {
            startActionChainExecution(actionChain, regularMinionIds);
        }

        if (!sshMinionIds.isEmpty()) {
            startSSHActionChain(actionChain, sshMinionIds, extraFilerefs);
        }
    }

    private List<SaltState> convertToState(long actionChainId, ServerAction serverAction,
                                           List<LocalCall<?>> calls, MinionSummary minion) {
        String stateId = SaltActionChainGeneratorService.createStateId(actionChainId,
                serverAction.getParentAction().getId());

        return calls.stream().map(call -> {
            Map<String, Object> payload = call.getPayload();
            String fun = (String)payload.get("fun");
            Map<String, ?> kwargs = (Map<String, ?>)payload.get("kwarg");
            switch(fun) {
                case "state.apply":
                    List<String> mods = (List<String>)kwargs.get("mods");
                    if (CollectionUtils.isEmpty(mods)) {
                        if (minion.isSshPush()) {
                            // Apply highstate using a custom top.
                            // The custom top is needed because salt-ssh invokes
                            // "salt-call --local" and this needs a top file in order to apply the highstate
                            // and salt-ssh doesn't pack one automatically in the state tarball
                            return new SaltModuleRun(stateId,
                                    "state.top",
                                    serverAction.getParentAction().getId(),
                                    singletonMap("topfn",
                                            saltActionChainGeneratorService
                                                    .getActionChainTopPath(actionChainId,
                                                            serverAction.getParentAction().getId())),
                                    emptyMap());
                        }
                        else {
                            return new SaltModuleRun(stateId,
                                    "state.apply",
                                    serverAction.getParentAction().getId(),
                                    emptyMap(),
                                    createStateApplyKwargs(kwargs));
                        }

                    }
                    return new SaltModuleRun(stateId,
                            "state.apply",
                            serverAction.getParentAction().getId(),
                            !mods.isEmpty() ?
                                    singletonMap("mods", mods) : emptyMap(),
                            createStateApplyKwargs(kwargs));
                case SYSTEM_REBOOT:
                    Integer time = (Integer)kwargs.get("at_time");
                    return new SaltSystemReboot(stateId,
                            serverAction.getParentAction().getId(), time);
                case "transactional_update.reboot":
                    // this function will be excluded to the sls file by createActionChainSLSFiles
                    return new SaltSystemReboot(stateId,
                            serverAction.getParentAction().getId(), 0);
                default:
                    throw new RhnRuntimeException("Salt module call " + fun + " can't be converted to a state.");
            }
        }).collect(Collectors.toList());
    }

    private Map<String, Object> createStateApplyKwargs(Map<String, ?> kwargs) {
        Map<String, Object> applyKwargs = new HashMap<>();
        if (kwargs.get("pillar") != null) {
            applyKwargs.put("pillar", kwargs.get("pillar"));
        }
        applyKwargs.put("queue", true);
        return applyKwargs;
    }


    /**
     * This function will return a map with list of minions grouped by the
     * salt netapi local call that executes what needs to be executed on
     * those minions for the given errata ids and minions.
     *
     * @param minionSummaries list of minion summaries to target
     * @param errataIds list of errata ids
     * @param allowVendorChange true if vendor change allowed
     * @return minion summaries grouped by local call
     */
    public Map<LocalCall<?>, List<MinionSummary>> errataAction(List<MinionSummary> minionSummaries,
            Set<Long> errataIds, boolean allowVendorChange) {
        Map<Boolean, List<MinionSummary>> byUbuntu = minionSummaries.stream()
                .collect(partitioningBy(m -> m.getOs().equals("Ubuntu")));

        Map<LocalCall<Map<String, ApplyResult>>, List<MinionSummary>> ubuntuErrataInstallCalls =
                errataToPackageInstallCalls(byUbuntu.get(true), errataIds);

        Set<Long> minionServerIds = byUbuntu.get(false).stream()
                .map(MinionSummary::getServerId)
                .collect(Collectors.toSet());

        Map<Long, Map<Long, Set<ErrataInfo>>> errataInfos = ServerFactory
                .listErrataNamesForServers(minionServerIds, errataIds);

        // Group targeted minions by errata names
        Map<Set<ErrataInfo>, List<MinionSummary>> collect = byUbuntu.get(false).stream()
                .collect(Collectors.groupingBy(minionId -> errataInfos.get(minionId.getServerId())
                        .values().stream()
                        .flatMap(Set::stream)
                        .collect(Collectors.toSet())
        ));

        // Convert errata names to LocalCall objects of type State.apply
        Map<LocalCall<?>, List<MinionSummary>> patchableCalls = collect.entrySet().stream()
            .collect(Collectors.toMap(entry -> {
                Map<String, Object> params = new HashMap<>();
                params.put(PARAM_REGULAR_PATCHES,
                    entry.getKey().stream()
                        .filter(e -> !e.isUpdateStack())
                        .map(ErrataInfo::getName)
                        .sorted()
                        .collect(toList())
                );
                params.put(ALLOW_VENDOR_CHANGE, allowVendorChange);
                params.put(PARAM_UPDATE_STACK_PATCHES,
                    entry.getKey().stream()
                        .filter(ErrataInfo::isUpdateStack)
                        .map(ErrataInfo::getName)
                        .sorted()
                        .collect(toList())
                );
                if (entry.getKey().stream().anyMatch(ErrataInfo::includeSalt)) {
                    params.put("include_salt_upgrade", true);
                }
                return State.apply(
                        List.of(PACKAGES_PATCHINSTALL),
                        Optional.of(params)
                );
            },
            Map.Entry::getValue));
        patchableCalls.putAll(ubuntuErrataInstallCalls);
        return patchableCalls;
    }


    private Map<LocalCall<Map<String, ApplyResult>>, List<MinionSummary>> errataToPackageInstallCalls(
            List<MinionSummary> minions,
            Set<Long> errataIds) {
        Set<Long> minionIds = minions.stream()
                .map(MinionSummary::getServerId).collect(Collectors.toSet());
        Map<Long, Map<String, Tuple2<String, String>>> longMapMap =
                ServerFactory.listNewestPkgsForServerErrata(minionIds, errataIds);

        // group minions by packages that need to be updated
        Map<Map<String, Tuple2<String, String>>, List<MinionSummary>> nameArchVersionToMinions =
                minions.stream().collect(
                        Collectors.groupingBy(minion -> longMapMap.get(minion.getServerId()))
                );

        return nameArchVersionToMinions.entrySet().stream().collect(toMap(
                entry -> State.apply(
                        singletonList(PACKAGES_PKGINSTALL),
                        Optional.of(singletonMap(PARAM_PKGS,
                                entry.getKey().entrySet()
                                        .stream()
                                        .map(e -> List.of(
                                                e.getKey(),
                                                e.getValue().getA().replaceAll("-deb$", ""),
                                                e.getValue().getB().endsWith("-X") ?
                                                    e.getValue().getB().substring(0, e.getValue().getB().length() - 2) :
                                                    e.getValue().getB()))
                                        .collect(Collectors.toList())))
                ),
                Map.Entry::getValue
        ));
    }


    private Map<LocalCall<?>, List<MinionSummary>> cocoAttestationAction(List<MinionSummary> minionSummaries) {
        return Map.of(
                State.apply(Collections.singletonList(COCOATTEST_REQUESTDATA), Optional.empty()),
                minionSummaries
        );
    }

    private Map<LocalCall<?>, List<MinionSummary>> appStreamAction(
            List<MinionSummary> minionSummaries, AppStreamAction action) {
        Map<LocalCall<?>, List<MinionSummary>> ret = new HashMap<>();

        Map<Boolean, Set<AppStreamActionDetails>> details = action.getDetails().stream()
            .collect(Collectors.partitioningBy(AppStreamActionDetails::isEnable, Collectors.toSet()));

        var enableParams = details.get(true).stream()
            .map(d -> d.getStream() == null ?
                singletonList(d.getModuleName()) :
                Arrays.asList(d.getModuleName(), d.getStream()))
            .toList();
        var disableParams = details.get(false).stream().map(AppStreamActionDetails::getModuleName).toList();

        Optional<Map<String, Object>> params = Optional.of(Map.of(
            PARAM_APPSTREAMS_ENABLE, enableParams,
            PARAM_APPSTREAMS_DISABLE, disableParams
        ));
        ret.put(State.apply(List.of(APPSTREAMS_CONFIGURE), params), minionSummaries);
        return ret;
    }

    /**
     * Prepare to execute staging job via Salt
     * @param actionIn the action
     * @param minionSummaries a list of minion summaries of the minions involved in the given Action
     * @return a call with the impacted minions
     */
    private LocalCall<?> prepareStagingTargets(Action actionIn, List<MinionSummary> minionSummaries) {
        LocalCall<?> call = null;
        if (actionIn.getActionType().equals(ActionFactory.TYPE_PACKAGES_UPDATE)) {
            List<List<String>> args = ((PackageUpdateAction) actionIn)
                    .getDetails().stream().map(d -> Arrays.asList(d.getPackageName().getName(),
                            d.getArch().toUniversalArchString(), d.getEvr().toUniversalEvrString()))
                    .toList();
            call = State.apply(List.of(PACKAGES_PKGDOWNLOAD),
                    Optional.of(Collections.singletonMap(PARAM_PKGS, args)));
            LOG.info("Executing staging of packages");
        }
        if (actionIn.getActionType().equals(ActionFactory.TYPE_ERRATA)) {
            Set<Long> errataIds = ((ErrataAction) actionIn).getErrata().stream()
                    .map(Errata::getId).collect(Collectors.toSet());
            Map<Long, Map<Long, Set<ErrataInfo>>> errataNames = ServerFactory
                    .listErrataNamesForServers(minionSummaries.stream().map(MinionSummary::getServerId)
                            .collect(Collectors.toSet()), errataIds);
            List<String> errataArgs = errataNames.entrySet().stream()
                .flatMap(e -> e.getValue().entrySet().stream()
                     .flatMap(f -> f.getValue().stream()
                         .map(ErrataInfo::getName)
                     )
                )
                .toList();

            call = State.apply(List.of(PACKAGES_PATCHDOWNLOAD),
                    Optional.of(Collections.singletonMap(PARAM_PATCHES, errataArgs)));
            LOG.info("Executing staging of patches");
        }
        return call;
    }

    /**
     * @param actionIn the action
     * @param call the call
     * @param minionSummaries a list of minion summaries of the minions involved in the given Action
     * @param forcePackageListRefresh add metadata to force a package list refresh
     * @param isStagingJob if the job is a staging job
     * @return a map containing all minions partitioned by success
     */
    private Map<Boolean, List<MinionSummary>> execute(Action actionIn, LocalCall<?> call,
            List<MinionSummary> minionSummaries, boolean forcePackageListRefresh,
            boolean isStagingJob) {
        List<String> minionIds = minionSummaries.stream().map(MinionSummary::getMinionId).collect(Collectors.toList());

        if (LOG.isDebugEnabled()) {
            LOG.debug("Executing action for: {}", minionIds.stream().collect(Collectors.joining(", ")));
        }

        try {
            ScheduleMetadata metadata = ScheduleMetadata.getMetadataForRegularMinionActions(
                    isStagingJob, forcePackageListRefresh, actionIn.getId());
            List<String> results = Opt.fold(
                    saltApi.callAsync(call, new MinionList(minionIds), Optional.of(metadata)),
                    ArrayList::new,
                    LocalAsyncResult::getMinions);

            return minionSummaries.stream().collect(Collectors
                    .partitioningBy(minionId -> results.contains(minionId.getMinionId())));
        }
        catch (SaltException ex) {
            LOG.debug("Failed to execute action: {}", ex.getMessage());
            Map<Boolean, List<MinionSummary>> result = new HashMap<>();
            result.put(true, Collections.emptyList());
            result.put(false, minionSummaries);
            return result;
        }
    }

    /**
     * @param actionChain the actionChain
     * @param minionSummaries a set of minion summaries of the minions involved in the given Action
     * @return a map containing all minions partitioned by success
     */
    private Map<Boolean, Set<MinionSummary>> callAsyncActionChainStart(
            ActionChain actionChain,
            Set<MinionSummary> minionSummaries) {
        List<String> minionIds = minionSummaries.stream().map(MinionSummary::getMinionId)
                .collect(Collectors.toList());

        if (LOG.isDebugEnabled()) {
            LOG.debug("Executing action chain for: {}", String.join(", ", minionIds));
        }

        try {
            List<String> results = saltApi
                    .callAsync(MgrActionChains.start(actionChain.getId()), new MinionList(minionIds),
                            Optional.of(ScheduleMetadata.getDefaultMetadata().withActionChain(actionChain.getId())))
                    .map(LocalAsyncResult::getMinions)
                    .orElse(Collections.emptyList());

            return minionSummaries.stream()
                    .collect(Collectors.partitioningBy(
                            minion -> results.contains(minion.getMinionId()),
                            Collectors.toSet()
                    ));
        }
        catch (SaltException ex) {
            LOG.debug("Failed to execute action chain: {}", ex.getMessage());
            Map<Boolean, Set<MinionSummary>> result = new HashMap<>();
            result.put(true, Collections.emptySet());
            result.put(false, minionSummaries);
            return result;
        }
    }

    /**
     * Execute an action on an ssh-push minion.
     *
     * @param action the action to be executed
     * @param minion minion on which the action will be executed
     */
    public void executeSSHAction(Action action, MinionServer minion) {
        executeSSHAction(action, minion, false);
    }

    /**
     * Execute an action on an ssh-push minion.
     *
     * @param action the action to be executed
     * @param minion minion on which the action will be executed
     * @param forcePkgRefresh set to true if a package list refresh should be scheduled at the end
     */
    public void executeSSHAction(Action action, MinionServer minion, boolean forcePkgRefresh) {
        Optional<ServerAction> serverAction = action.getServerActions().stream()
                .filter(sa -> sa.getServerId().equals(minion.getId()))
                .findFirst();
        serverAction.ifPresent(sa -> {
            if (List.of(STATUS_FAILED, STATUS_COMPLETED).contains(sa.getStatus())) {
                LOG.info("Action '{}' is completed or failed. Skipping.", action.getName());
                return;
            }

            if (prerequisiteInStatus(sa, ActionFactory.STATUS_QUEUED)) {
                LOG.info("Prerequisite of action '{}' is still queued. Skipping executing of the action.",
                        action.getName());
                return;
            }

            if (prerequisiteInStatus(sa, ActionFactory.STATUS_FAILED)) {
                LOG.info("Failing action '{}' as its prerequisite '{}' failed.", action.getName(),
                        action.getPrerequisite().getName());
                sa.fail(-100L, "Prerequisite failed.");
                return;
            }

            sa.setRemainingTries(sa.getRemainingTries() - 1);

            Map<LocalCall<?>, List<MinionSummary>> calls = new HashMap<>();
            try {
                calls = callsForAction(action, List.of(new MinionSummary(minion)));
            }
            catch (RuntimeException e) {
                sa.setStatus(STATUS_FAILED);
                sa.setResultMsg("Error preparing salt call: " + e.getMessage());
                sa.setCompletionTime(new Date());
                return;
            }

            for (LocalCall<?> call : calls.keySet()) {
                Optional<Result<JsonElement>> result;
                // try-catch as we'd like to log the warning in case of exception
                try {
                    result = saltApi.rawJsonCall(call, minion.getMinionId());
                }
                catch (RuntimeException e) {
                    LOG.error("Error executing Salt call for action: {} on minion {}",
                            action.getName(), minion.getMinionId(), e);
                    sa.setStatus(STATUS_FAILED);
                    sa.setResultMsg("Error calling Salt: " + e.getMessage());
                    sa.setCompletionTime(new Date());
                    return;
                }

                result.ifPresentOrElse(r -> {
                    LOG.trace("Salt call result: {}", r);

                    r.consume(error -> {
                        String errorString = error.toString();
                        if (sa.getRemainingTries() > 0 && errorString.contains("System is going down")) {
                            // SSH login is blocked when a reboot is ongoing. Reschedule this action later again
                            LOG.info("System is going down. Configure re-try in 3 minutes");
                            sa.setStatus(STATUS_QUEUED);
                            sa.setRemainingTries((sa.getRemainingTries() - 1L));
                            sa.setPickupTime(null);
                            sa.setCompletionTime(null);
                            action.setEarliestAction(Date.from(Instant.now().plus(3, ChronoUnit.MINUTES)));
                            ActionFactory.save(action);
                            // We commit as we need to take care that the new date is in DB when we
                            // call taskomatic to execute the action again.
                            HibernateFactory.commitTransaction();
                            try {
                                taskomaticApi.scheduleActionExecution(action);
                            }
                            catch (TaskomaticApiException e) {
                                LOG.error("Unable to reschedule failed Salt SSH Action: {}", errorString, e);
                                sa.setStatus(STATUS_FAILED);
                                sa.setResultMsg(errorString);
                                sa.setCompletionTime(new Date());
                            }
                        }
                        else {
                            sa.setStatus(STATUS_FAILED);
                            sa.setResultMsg(error.fold(
                                    e -> "function " + e.getFunctionName() + " not available.",
                                    e -> "module " + e.getModuleName() + " not supported.",
                                    e -> "error parsing json.",
                                    GenericError::getMessage,
                                    e -> "salt ssh error: " + e.getRetcode() + " " + e.getMessage()
                            ));
                            LOG.error(sa.getResultMsg());
                            sa.setCompletionTime(new Date());
                        }
                    }, jsonResult -> {
                        String function = (String) call.getPayload().get("fun");

                        /* bsc#1197591 ssh push reboot has an answer that is not a failure but the action needs to stay
                        *  in picked up, in this way SSHServiceDriver::getCandidates can schedule a reboot correctly
                        */
                        if (!action.getActionType().equals(ActionFactory.TYPE_REBOOT)) {
                            saltUtils.updateServerAction(sa, 0L, true, "n/a", jsonResult,
                                    Optional.of(Xor.right(function)), null);
                        }

                        else if (sa.getStatus().equals(ActionFactory.STATUS_QUEUED)) {
                            setActionAsPickedUp(sa);
                        }

                        // Perform a "check-in" after every executed action
                        minion.updateServerInfo();

                        // Perform a package profile update in the end if necessary
                        if (forcePkgRefresh || saltUtils.shouldRefreshPackageList(
                                Optional.of(Xor.right(function)), Optional.of(jsonResult))) {
                            LOG.info("Scheduling a package profile update");

                            try {
                                ActionManager.schedulePackageRefresh(
                                        Optional.ofNullable(action.getSchedulerUser()), minion);
                            }
                            catch (TaskomaticApiException e) {
                                LOG.error("Could not schedule package refresh for minion: {}", minion.getMinionId(), e);
                            }
                        }
                    });
                }, () -> {
                    LOG.error("Action '{}' failed. Got not result from Salt, probably minion is down or " +
                            "could not be contacted.", action.getName());
                    sa.setStatus(STATUS_FAILED);
                    sa.setResultMsg("Minion is down or could not be contacted.");
                    sa.setCompletionTime(new Date());
                });
            }
        });
    }

    /**
     * Checks whether the parent action of given server action contains a server action
     * that is in given state and is associated with the server of given server action.
     * @param serverAction server action
     * @param state state
     * @return true if there exists a server action in given state associated with the same
     * server as serverAction and parent action of serverAction
     */
    private boolean prerequisiteInStatus(ServerAction serverAction, ActionStatus state) {
        Optional<Stream<ServerAction>> prerequisites =
                ofNullable(serverAction.getParentAction())
                        .map(Action::getPrerequisite)
                        .map(Action::getServerActions)
                        .map(Collection::stream);

        return prerequisites
                .flatMap(serverActions ->
                        serverActions
                                .filter(s ->
                                        serverAction.getServer().equals(s.getServer()) &&
                                                state.equals(s.getStatus()))
                                .findAny())
                .isPresent();
    }

    /**
     * Set the given action to FAILED if not already in that state and also the dependent actions.
     * @param actionId the action id
     * @param minionId the minion id
     * @param message the result message to set in the server action
     */
    public void failDependentServerActions(long actionId, String minionId, Optional<String> message) {
        Optional<MinionServer> minion = MinionServerFactory.findByMinionId(
                minionId);
        if (minion.isPresent()) {
            // set first action to failed if not already in that state
            Action action = ActionFactory.lookupById(actionId);
            Optional.ofNullable(action)
                    .flatMap(firstAction -> firstAction.getServerActions().stream()
                            .filter(sa -> sa.getServerId().equals(minion.get().getId()))
                            .filter(sa -> !ActionFactory.STATUS_FAILED.equals(sa.getStatus()))
                            .filter(sa -> !ActionFactory.STATUS_COMPLETED.equals(sa.getStatus()))
                            .findFirst()).ifPresent(sa -> sa.fail(message.orElse("Prerequisite failed")));

            // walk dependent server actions recursively and set them to failed
            Deque<Long> actionIdsDependencies = new ArrayDeque<>();
            actionIdsDependencies.push(actionId);
            List<ServerAction> serverActions = Optional.ofNullable(action).
                    map(firstAction -> ActionFactory
                        .listServerActionsForServer(minion.get(),
                                Arrays.asList(ActionFactory.STATUS_QUEUED, ActionFactory.STATUS_PICKED_UP,
                                        ActionFactory.STATUS_FAILED), action.getCreated()))
                    .orElse(new ArrayList<>());

            while (!actionIdsDependencies.isEmpty()) {
                Long acId = actionIdsDependencies.pop();
                serverActions.stream()
                        .filter(s -> s.getParentAction().getPrerequisite() != null)
                        .filter(s -> s.getParentAction().getPrerequisite().getId().equals(acId))
                        .forEach(sa -> {
                            actionIdsDependencies.push(sa.getParentAction().getId());
                            sa.fail("Prerequisite failed");
                        });
            }
        }
    }

    /**
     * In action chains at this point the action is still queued so we have
     * to set it to picked up.
     * This could still lead to the race condition on when event processing is slow.
     *
     */
    private void setActionAsPickedUp(ServerAction sa) {
        sa.setStatus(ActionFactory.STATUS_PICKED_UP);
        sa.setPickupTime(new Date());
    }

    /**
     * Update the action properly based on the Job results from Salt.
     *
     * @param actionId the ID of the Action to handle
     * @param minionId the ID of the Minion who performed the action
     * @param retcode the retcode returned
     * @param success indicates if the job executed successfully
     * @param jobId the ID of the Salt job.
     * @param jsonResult the json results from the Salt job.
     * @param function the Salt function executed.
     * @param endTime end time when the action finished. If null, "now" is used
     */
    public void handleAction(long actionId, String minionId, int retcode, boolean success,
                             String jobId, JsonElement jsonResult,
                             Optional<Xor<String[], String>> function,
                             Date endTime) {
        // Lookup the corresponding action
        Optional<Action> action = Optional.ofNullable(ActionFactory.lookupById(actionId));
        if (action.isPresent()) {

            if (LOG.isDebugEnabled()) {
                LOG.debug("Matched salt job with action (id={})", actionId);
            }

            Optional<MinionServer> minionServerOpt = MinionServerFactory.findByMinionId(minionId);
            minionServerOpt.ifPresent(minionServer -> {
                Optional<ServerAction> serverAction = action.get()
                        .getServerActions()
                        .stream()
                        .filter(sa -> sa.getServer().equals(minionServer)).findFirst();


                serverAction.ifPresent(sa -> {
                    if (LOG.isDebugEnabled()) {
                        LOG.debug("Updating action for server: {}", minionServer.getId());
                    }
                    try {
                        if (action.get().getActionType().equals(
                                ActionFactory.TYPE_REBOOT) && success && retcode == 0) {
                            // Reboot has been scheduled so set reboot action to PICKED_UP.
                            // Wait until next "minion/start/event" to set it to COMPLETED.
                            if (sa.getStatus().equals(ActionFactory.STATUS_QUEUED)) {
                                setActionAsPickedUp(sa);
                            }
                            return;
                        }
                        else if (action.get().getActionType().equals(ActionFactory.TYPE_KICKSTART_INITIATE) &&
                                success) {
                            KickstartAction ksAction = (KickstartAction) action.get();
                            if (!ksAction.getKickstartActionDetails().getUpgrade()) {
                                // Delete salt key from master
                                saltKeyUtils.deleteSaltKey(ksAction.getSchedulerUser(), minionId);
                            }
                        }
                        saltUtils.updateServerAction(sa,
                                retcode,
                                success,
                                jobId,
                                jsonResult,
                                function,
                                endTime);
                        ActionFactory.save(sa);
                        SystemManager.updateSystemOverview(sa.getServer());
                    }
                    catch (Exception e) {
                        LOG.error("Error processing Salt job return", e);
                        // DB exceptions cause the transaction to go into rollback-only
                        // state. We need to rollback this transaction first.
                        HibernateFactory.rollbackTransaction();

                        sa.fail("An unexpected error has occurred. Please check the server logs.");

                        ActionFactory.save(sa);
                        // When we throw the exception again, the current transaction
                        // will be set to rollback-only, so we explicitly commit the
                        // transaction here
                        HibernateFactory.commitTransaction();

                        // We don't actually want to catch any exceptions
                        throw e;
                    }
                });
            });
        }
        else {
            LOG.warn("Action referenced from Salt job was not found: {}", actionId);
        }
    }

    private boolean checkIfRebootRequired(StateApplyResult<Ret<JsonElement>> actionStateApply) {
        JsonElement ret = actionStateApply.getChanges().getRet();
        if (!ret.isJsonObject()) {
            return false;
        }

        if (ret.getAsJsonObject() == null) {
            return false;
        }

        JsonPrimitive prim = ret.getAsJsonObject().getAsJsonPrimitive("reboot_required");
        if (prim == null || !prim.isBoolean()) {
            return false;

        }
        return prim.getAsBoolean();
    }

    private long checkActionID(StateApplyResult<Ret<JsonElement>> actionStateApply) {
        Ret<JsonElement> changes = actionStateApply.getChanges();
        if (changes == null) {
            return 0;
        }

        JsonElement ret = changes.getRet();
        if (ret == null || !ret.isJsonObject()) {
            return 0;
        }

        JsonObject obj = ret.getAsJsonObject();
        if (obj == null) {
            return 0;
        }

        JsonPrimitive prim = obj.getAsJsonPrimitive("current_action_id");
        if (prim == null || !prim.isNumber()) {
            return 0;

        }
        return prim.getAsLong();
    }

    /**
     * Handle action chain Salt result.
     *
     * @param minionId the minion id
     * @param jobId the job id
     * @param actionChainResult job result
     * @param skipFunction function to check if a result should be skipped from handling
     */
    public void handleActionChainResult(
            String minionId, String jobId,
            Map<String, StateApplyResult<Ret<JsonElement>>> actionChainResult,
            Function<StateApplyResult<Ret<JsonElement>>, Boolean> skipFunction) {
        int chunk = 1;
        long retActionChainId = 0L;
        boolean actionChainFailed = false;
        List<Long> failedActionIds = new ArrayList<>();
        for (Map.Entry<String, StateApplyResult<Ret<JsonElement>>> entry : actionChainResult.entrySet()) {
            String key = entry.getKey();
            StateApplyResult<Ret<JsonElement>> actionStateApply = entry.getValue();

            Optional<SaltActionChainGeneratorService.ActionChainStateId> stateId =
                    SaltActionChainGeneratorService.parseActionChainStateId(key);
            if (stateId.isPresent()) {
                retActionChainId = stateId.get().getActionChainId();
                chunk = stateId.get().getChunk();
                long actionId = stateId.get().getActionId();
                if (Boolean.TRUE.equals(skipFunction.apply(actionStateApply))) {
                    continue; // skip this state from handling
                }

                if (!actionStateApply.isResult()) {
                    actionChainFailed = true;
                    failedActionIds.add(actionId);
                    // don't stop handling the result entries if there's a failed action
                    // the result entries are not returned in order
                }
                Date endTime = calculateStateApplyEndTime(actionStateApply.getStartTime(),
                        actionStateApply.getDuration());
                handleAction(actionId,
                        minionId,
                        actionStateApply.isResult() ? 0 : -1,
                        actionStateApply.isResult(),
                        jobId,
                        actionStateApply.getChanges().getRet(),
                        actionStateApply.getName(), endTime);
            }
            else if (key.contains("schedule_next_chunk")) {

                Optional<MinionServer> minionServerOpt = MinionServerFactory.findByMinionId(minionId);

                long actionId = checkActionID(actionStateApply);
                minionServerOpt.ifPresent(minionServer -> {

                        if (minionServer.doesOsSupportsTransactionalUpdate() &&
                                actionId != 0 && checkIfRebootRequired(actionStateApply)) {
                            /*
                             * Transactional update does not contain reboot in sls files, but apply a reboot using
                             * activate_transaction=True in transactional_update.sls . So it's required to parse
                             * the return to check if schedule_next_chunk contains reboot_required param,
                             * then we can suppose that the next action is a reboot.
                             * Then we need to pick up the action.
                             */

                            final Optional<Action> action  = Optional.ofNullable(ActionFactory.lookupById(actionId));
                            action.ifPresent(actionIn -> {
                                LOG.debug("Matched salt job with action (id={})", actionId);
                                Optional<ServerAction> serverAction = action.get()
                                        .getServerActions()
                                        .stream()
                                        .filter(sa -> sa.getServer().equals(minionServer)).findFirst();

                                serverAction.ifPresent(this::setActionAsPickedUp);
                            });
                        }
                });
            }
            else {
                LOG.warn("Could not find action id in action chain state key: {}", key);
            }
        }

        if (actionChainFailed) {
            failedActionIds.stream().min(Long::compare).ifPresent(firstFailedActionId ->
                    // Set rest of actions as FAILED due to failed prerequisite
                    failDependentServerActions(firstFailedActionId, minionId, Optional.empty())
            );
        }
        // Removing the generated SLS file
        SaltActionChainGeneratorService.INSTANCE.removeActionChainSLSFiles(
                retActionChainId, minionId, chunk, actionChainFailed);

        ActionChainFactory.getActionChain(retActionChainId).ifPresent(ac -> {
            // We need to reload server actions since saltssh will be in
            // the same db session from when the action was started and
            // won't see results of non ssh minions otherwise.
            ac.getEntries().stream()
                    .flatMap(ace -> ace.getAction().getServerActions().stream())
                    .forEach(HibernateFactory::reload);
            if (ac.isDone()) {
                ActionChainFactory.delete(ac);
            }
        });
    }

    private Date calculateStateApplyEndTime(String startTimeIn, double durationIn) {
        try {
            LocalDate now = LocalDate.now();
            LocalTime localTime = LocalTime.parse(startTimeIn);
            LocalDateTime endTime = LocalDateTime.of(now, localTime).plus(Duration.ofMillis((long) durationIn));
            return Date.from(endTime.atZone(ZoneId.systemDefault()).toInstant());
        }
        catch (DateTimeException | ArithmeticException e) {
            LOG.warn("Unable to parse date time", e);
        }
        return new Date();
    }

    /**
     * @param saltActionChainGeneratorServiceIn to set
     */
    public void setSaltActionChainGeneratorService(SaltActionChainGeneratorService
                                                           saltActionChainGeneratorServiceIn) {
        this.saltActionChainGeneratorService = saltActionChainGeneratorServiceIn;
    }

    /**
     * Whether to commit hibernate transaction or not. Default is commit.
     * Only used in unit tests.
     *
     * @param commitTransactionIn flag to set
     */
    public void setCommitTransaction(boolean commitTransactionIn) {
        this.commitTransaction = commitTransactionIn;
    }

    /**
     * Only used in unit tests.
     * @param saltUtilsIn to set
     */
    public void setSaltUtils(SaltUtils saltUtilsIn) {
        this.saltUtils = saltUtilsIn;
    }

    /**
     * Only used in unit tests.
     * @param saltApiIn to set
     */
    public void setSaltApi(SaltApi saltApiIn) {
        this.saltApi = saltApiIn;
    }

    /**
     * Only needed for unit test.
     * @param taskomaticApiIn to set
     */
    public void setTaskomaticApi(TaskomaticApi taskomaticApiIn) {
        this.taskomaticApi = taskomaticApiIn;
    }
}
