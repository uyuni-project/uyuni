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

import static com.redhat.rhn.domain.action.ActionFactory.STATUS_COMPLETED;
import static com.redhat.rhn.domain.action.ActionFactory.STATUS_FAILED;
import static com.suse.manager.webui.services.SaltConstants.SALT_FS_PREFIX;
import static com.suse.manager.webui.services.SaltConstants.SCRIPTS_DIR;
import static java.util.Collections.emptyMap;
import static java.util.Collections.singletonList;
import static java.util.Collections.singletonMap;
import static java.util.Optional.ofNullable;
import static java.util.stream.Collectors.toList;

import com.redhat.rhn.common.conf.Config;
import com.redhat.rhn.common.conf.ConfigDefaults;
import com.redhat.rhn.common.hibernate.HibernateFactory;
import com.redhat.rhn.domain.action.Action;
import com.redhat.rhn.domain.action.ActionChain;
import com.redhat.rhn.domain.action.ActionChainEntry;
import com.redhat.rhn.domain.action.ActionChainFactory;
import com.redhat.rhn.domain.action.ActionFactory;
import com.redhat.rhn.domain.action.ActionStatus;
import com.redhat.rhn.domain.action.ActionType;
import com.redhat.rhn.domain.action.channel.SubscribeChannelsAction;
import com.redhat.rhn.domain.action.channel.SubscribeChannelsActionDetails;
import com.redhat.rhn.domain.action.config.ConfigAction;
import com.redhat.rhn.domain.action.config.ConfigRevisionAction;
import com.redhat.rhn.domain.action.dup.DistUpgradeAction;
import com.redhat.rhn.domain.action.dup.DistUpgradeChannelTask;
import com.redhat.rhn.domain.action.errata.ErrataAction;
import com.redhat.rhn.domain.action.rhnpackage.PackageRemoveAction;
import com.redhat.rhn.domain.action.rhnpackage.PackageUpdateAction;
import com.redhat.rhn.domain.action.salt.ApplyStatesAction;
import com.redhat.rhn.domain.action.salt.ApplyStatesActionDetails;
import com.redhat.rhn.domain.action.salt.build.ImageBuildAction;
import com.redhat.rhn.domain.action.salt.build.ImageBuildActionDetails;
import com.redhat.rhn.domain.action.salt.inspect.ImageInspectAction;
import com.redhat.rhn.domain.action.salt.inspect.ImageInspectActionDetails;
import com.redhat.rhn.domain.action.scap.ScapAction;
import com.redhat.rhn.domain.action.scap.ScapActionDetails;
import com.redhat.rhn.domain.action.script.ScriptAction;
import com.redhat.rhn.domain.action.server.ServerAction;
import com.redhat.rhn.domain.channel.AccessToken;
import com.redhat.rhn.domain.channel.AccessTokenFactory;
import com.redhat.rhn.domain.channel.Channel;
import com.redhat.rhn.domain.config.ConfigRevision;
import com.redhat.rhn.domain.errata.Errata;
import com.redhat.rhn.domain.image.DockerfileProfile;
import com.redhat.rhn.domain.image.ImageProfile;
import com.redhat.rhn.domain.image.ImageProfileFactory;
import com.redhat.rhn.domain.image.ImageStore;
import com.redhat.rhn.domain.image.ImageStoreFactory;
import com.redhat.rhn.domain.image.KiwiProfile;
import com.redhat.rhn.domain.server.ErrataInfo;
import com.redhat.rhn.domain.server.MinionSummary;
import com.redhat.rhn.domain.server.MinionServer;
import com.redhat.rhn.domain.server.MinionServerFactory;
import com.redhat.rhn.domain.server.ServerFactory;
import com.redhat.rhn.domain.token.ActivationKey;
import com.redhat.rhn.domain.token.ActivationKeyFactory;
import com.redhat.rhn.domain.user.User;
import com.redhat.rhn.manager.action.ActionManager;
import com.redhat.rhn.taskomatic.TaskomaticApiException;
import com.suse.manager.reactor.messaging.ApplyStatesEventMessage;
import com.suse.manager.reactor.messaging.JobReturnEventMessageAction;
import com.suse.manager.utils.SaltUtils;
import com.suse.manager.webui.services.impl.SaltSSHService;
import com.suse.manager.webui.services.impl.SaltService;
import com.suse.manager.webui.utils.ElementCallJson;
import com.suse.manager.webui.utils.SaltModuleRun;
import com.suse.manager.webui.utils.SaltState;
import com.suse.manager.webui.utils.SaltSystemReboot;
import com.suse.manager.webui.utils.TokenBuilder;
import com.suse.manager.webui.utils.salt.MgrActionChains;
import com.suse.manager.webui.utils.salt.State;
import com.suse.manager.webui.utils.salt.custom.ScheduleMetadata;
import com.suse.salt.netapi.calls.LocalCall;
import com.suse.salt.netapi.calls.modules.State.ApplyResult;
import com.suse.salt.netapi.datatypes.target.MinionList;
import com.suse.salt.netapi.exception.SaltException;
import com.suse.salt.netapi.results.Result;
import com.suse.salt.netapi.results.Ret;
import com.suse.salt.netapi.results.StateApplyResult;
import com.suse.utils.Json;
import com.suse.utils.Opt;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonSyntaxException;
import com.google.gson.reflect.TypeToken;
import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.io.FileUtils;
import org.apache.commons.lang3.tuple.ImmutablePair;
import org.apache.commons.lang3.tuple.Pair;
import org.apache.log4j.Logger;
import org.hibernate.Hibernate;
import org.hibernate.proxy.HibernateProxy;
import org.jose4j.lang.JoseException;

import java.io.IOException;
import java.nio.charset.Charset;
import java.nio.file.FileSystem;
import java.nio.file.FileSystems;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.attribute.FileAttribute;
import java.nio.file.attribute.PosixFilePermission;
import java.nio.file.attribute.PosixFilePermissions;
import java.nio.file.attribute.UserPrincipal;
import java.nio.file.attribute.UserPrincipalLookupService;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
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

    /* Singleton instance of this class */
    public static final SaltServerActionService INSTANCE = new SaltServerActionService();

    /* Logger for this class */
    private static final Logger LOG = Logger.getLogger(SaltServerActionService.class);
    public static final String PACKAGES_PKGINSTALL = "packages.pkginstall";
    private static final String PACKAGES_PKGDOWNLOAD = "packages.pkgdownload";
    public static final String PACKAGES_PATCHINSTALL = "packages.patchinstall";
    private static final String PACKAGES_PATCHDOWNLOAD = "packages.patchdownload";
    private static final String PACKAGES_PKGREMOVE = "packages.pkgremove";
    private static final String CONFIG_DEPLOY_FILES = "configuration.deploy_files";
    private static final String CONFIG_DIFF_FILES = "configuration.diff_files";
    private static final String PARAM_PKGS = "param_pkgs";
    private static final String PARAM_PATCHES = "param_patches";
    private static final String PARAM_FILES = "param_files";
    private static final String REMOTE_COMMANDS = "remotecommands";
    private static final String SYSTEM_REBOOT = "system.reboot";

    /** SLS pillar parameter name for the list of update stack patch names. */
    public static final String PARAM_UPDATE_STACK_PATCHES = "param_update_stack_patches";

    /** SLS pillar parameter name for the list of regular patch names. */
    public static final String PARAM_REGULAR_PATCHES = "param_regular_patches";

    private boolean commitTransaction = true;

    private SaltActionChainGeneratorService saltActionChainGeneratorService =
            SaltActionChainGeneratorService.INSTANCE;

    private SaltService saltService = SaltService.INSTANCE;
    private SaltSSHService saltSSHService = SaltService.INSTANCE.getSaltSSHService();
    private SaltUtils saltUtils = SaltUtils.INSTANCE;
    private boolean skipCommandScriptPerms;

    private Action unproxy(Action entity) {
        Hibernate.initialize(entity);
        if (entity instanceof HibernateProxy) {
            entity = (Action) ((HibernateProxy) entity).getHibernateLazyInitializer()
                    .getImplementation();
        }
        return entity;
    }

    /**
     * For a given action return the salt call(s) that need to be executed for the minions involved.
     *
     * @param actionIn the action to be executed
     * @return map of Salt local call to list of targeted minion summaries
     */
    public Map<LocalCall<?>, List<MinionSummary>> callsForAction(Action actionIn) {
        List<MinionSummary> minionSummaries = MinionServerFactory.findMinionSummaries(actionIn.getId());
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
            return new HashMap<>();
        }

        ActionType actionType = actionIn.getActionType();
        actionIn = unproxy(actionIn);
        if (ActionFactory.TYPE_ERRATA.equals(actionType)) {
            ErrataAction errataAction = (ErrataAction) actionIn;
            Set<Long> errataIds = errataAction.getErrata().stream()
                    .map(Errata::getId).collect(Collectors.toSet());
            return errataAction(minions, errataIds);
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
        else if (ActionFactory.TYPE_CONFIGFILES_DEPLOY.equals(actionType)) {
            return deployFiles(minions, (ConfigAction) actionIn);
        }
        else if (ActionFactory.TYPE_CONFIGFILES_DIFF.equals(actionType)) {
            return diffFiles(minions, (ConfigAction) actionIn);
        }
        else if (ActionFactory.TYPE_SCRIPT_RUN.equals(actionType)) {
            return remoteCommandAction(minions, (ScriptAction) actionIn);
        }
        else if (ActionFactory.TYPE_APPLY_STATES.equals(actionType)) {
            ApplyStatesActionDetails actionDetails = ((ApplyStatesAction) actionIn).getDetails();
            return applyStatesAction(minions, actionDetails.getMods(), actionDetails.isTest());
        }
        else if (ActionFactory.TYPE_IMAGE_INSPECT.equals(actionType)) {
            ImageInspectAction iia = (ImageInspectAction) actionIn;
            ImageInspectActionDetails details = iia.getDetails();
            ImageStore store = ImageStoreFactory.lookupById(
                    details.getImageStoreId()).get();
            return imageInspectAction(minions, details, store);
        }
        else if (ActionFactory.TYPE_IMAGE_BUILD.equals(actionType)) {
            ImageBuildAction imageBuildAction = (ImageBuildAction) actionIn;
            ImageBuildActionDetails details = imageBuildAction.getDetails();
            return ImageProfileFactory.lookupById(details.getImageProfileId()).map(
                    ip -> imageBuildAction(
                            minions,
                            Optional.ofNullable(details.getVersion()),
                            ip,
                            imageBuildAction.getSchedulerUser(),
                            imageBuildAction.getId())
            ).orElseGet(Collections::emptyMap);
        }
        else if (ActionFactory.TYPE_DIST_UPGRADE.equals(actionType)) {
            return distUpgradeAction((DistUpgradeAction) actionIn, minions);
        }
        else if (ActionFactory.TYPE_SCAP_XCCDF_EVAL.equals(actionType)) {
            ScapAction scapAction = (ScapAction)actionIn;
            return scapXccdfEvalAction(minions, scapAction.getScapActionDetails());
        }
        else if (ActionFactory.TYPE_SUBSCRIBE_CHANNELS.equals(actionType)) {
            SubscribeChannelsAction subscribeAction = (SubscribeChannelsAction)actionIn;
            return subscribeChanelsAction(minions, subscribeAction.getDetails());
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

    private Optional<ServerAction> serverActionFor(Action actionIn, MinionSummary minion) {
        return actionIn.getServerActions().stream()
                .filter(sa -> sa.getServerId().equals(minion.getServerId()))
                .findFirst();
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

        List<MinionSummary> allMinions = MinionServerFactory.findMinionSummaries(actionIn.getId());

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

        for (MinionServer sshMinion: sshPushMinions) {
            executeSSHAction(actionIn, sshMinion);
        }
    }

    private void executeForRegularMinions(Action actionIn, boolean forcePackageListRefresh,
            boolean isStagingJob, Optional<Long> stagingJobMinionServerId, List<MinionSummary> minionSummaries) {
        for (Map.Entry<LocalCall<?>, List<MinionSummary>> entry : callsForAction(actionIn, minionSummaries)
                .entrySet()) {
            LocalCall<?> call = entry.getKey();
            final List<MinionSummary> targetMinions;
            Map<Boolean, List<MinionSummary>> results;

            if (isStagingJob) {
                targetMinions = new ArrayList<>();
                stagingJobMinionServerId.ifPresent(
                        serverId -> MinionServerFactory.lookupById(serverId).ifPresent(
                                server -> targetMinions.add(new MinionSummary(server))));
                call = prepareStagingTargets(actionIn, targetMinions);
            }
            else {
                targetMinions = entry.getValue();
            }

            results = execute(actionIn, call, targetMinions, forcePackageListRefresh, isStagingJob);

            if (!isStagingJob) {
                List<Long> succeededServerIds = results.get(true).stream()
                        .map(MinionSummary::getServerId).collect(toList());
                if (!succeededServerIds.isEmpty()) {
                    ActionFactory.updateServerActions(actionIn, succeededServerIds, ActionFactory.STATUS_PICKED_UP);
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
                callAsyncActionChainStart(MgrActionChains.start(actionChain.getId()), targetMinions);

        results.get(false).forEach(minionSummary -> {
            LOG.warn("Failed to schedule action chain for minion: " +
                    minionSummary.getMinionId());
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
            Map<String, Result<Map<String, String>>> pendingResumeConf = saltService.callSync(
                    MgrActionChains.getPendingResume(),
                    new MinionList(sshMinions.stream().map(minion -> minion.getMinionId())
                            .collect(Collectors.toList())));

            List<MinionSummary> targetSSHMinions = sshMinions.stream()
                    .filter(sshMinion -> {
                        Optional<Map<String, String>> confValues = pendingResumeConf.get(sshMinion.getMinionId())
                                .fold(err -> {
                                        LOG.error("mgractionchains.get_pending_resume failed: " + err.fold(
                                                Object::toString,
                                                Object::toString,
                                                Object::toString,
                                                Object::toString
                                        ));
                                        return Optional.empty();
                                    },
                                    res -> Optional.of(res));
                        if (!confValues.isPresent() || confValues.get().isEmpty()) {
                            // all good, no action chain currently executing on the minion
                            return true;
                        }
                        // fail the action chain because concurrent execution is not possible
                        LOG.warn("Minion " + sshMinion.getMinionId() + " has an action chain execution in progress");
                        failActionChain(sshMinion.getMinionId(), Optional.of(actionChain.getId()), firstActionId,
                                Optional.of("An action chain execution is already in progress. " +
                                        "Concurrent action chain execution is not allowed. " +
                                        "If the execution became stale remove directory " +
                                        "/var/tmp/.root_XXXX_salt/minion.d manually."));
                        return false;
                    }).collect(Collectors.toList());

            if (targetSSHMinions.isEmpty()) {
                // do nothing, no targets
                return;
            }

            Map<String, Result<Map<String, ApplyResult>>> res = saltSSHService.callSyncSSH(call,
                    new MinionList(targetSSHMinions.stream().map(minion -> minion.getMinionId())
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
            sshMinions.forEach(minion -> {
                failActionChain(minion.getMinionId(), Optional.of(actionChain.getId()), firstActionId,
                        Optional.of("Error handling action chain execution: " + e.getMessage()));
            });
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
                    chunkResult.get("module_|-startssh_|-mgractionchains.start_|-run");

            if (stateApplyResult == null) {
                // if no start result, look for resume
                stateApplyResult =
                        chunkResult.get("module_|-resumessh_|-mgractionchains.resume_|-run");
            }

            if (stateApplyResult == null) {
                LOG.error("No action chain result for minion " + minionId);
                failActionChain(minionId, firstChunkActionId, Optional.of("No action chain result"));
            }
            else if (!stateApplyResult.isResult() && (stateApplyResult.getChanges() == null ||
                    (stateApplyResult.getChanges().isJsonObject()) &&
                            ((JsonObject)stateApplyResult.getChanges()).size() == 0)) {
                LOG.error("Error handling action chain execution: " + stateApplyResult.getComment());
                failActionChain(minionId, firstChunkActionId, Optional.of(stateApplyResult.getComment()));
            }
            else if (stateApplyResult.getChanges() != null) {
                // handle the result
                Map<String, StateApplyResult<Ret<JsonElement>>> actionChainResult = null;
                try {
                    Ret<Map<String, StateApplyResult<Ret<JsonElement>>>> actionChainRet =
                            Json.GSON.fromJson(stateApplyResult.getChanges(),
                                    new TypeToken<Ret<Map<String, StateApplyResult<Ret<JsonElement>>>>>() {
                                    }.getType());
                    actionChainResult = actionChainRet.getRet();
                }
                catch (JsonSyntaxException e) {
                    LOG.error("Unexpected response: " + stateApplyResult.getChanges(), e);
                    String msg = stateApplyResult.getChanges().toString();
                    if ((stateApplyResult.getChanges().isJsonObject()) &&
                            ((JsonObject)stateApplyResult.getChanges()).get("ret") != null) {
                        msg = ((JsonObject)stateApplyResult.getChanges()).get("ret").toString();
                    }
                    failActionChain(minionId, firstChunkActionId, Optional.of("Unexpected response: " + msg));
                    return false;
                }
                JobReturnEventMessageAction.handleActionChainResult(minionId, "", 0, true,
                        actionChainResult,
                        // skip reboot, needs special handling
                        stateResult -> SYSTEM_REBOOT.equals(stateResult.getName()));

                boolean refreshPkg = false;
                for (Map.Entry<String, StateApplyResult<Ret<JsonElement>>> entry : actionChainResult.entrySet()) {
                    String stateIdKey = entry.getKey();
                    StateApplyResult<Ret<JsonElement>> stateResult = entry.getValue();

                    Optional<SaltActionChainGeneratorService.ActionChainStateId> actionChainStateId =
                            SaltActionChainGeneratorService.parseActionChainStateId(stateIdKey);
                    if (actionChainStateId.isPresent()) {
                        SaltActionChainGeneratorService.ActionChainStateId stateId = actionChainStateId.get();
                        // only reboot needs special handling,
                        // for salt pkg update there's no need to split the sls in case of salt-ssh minions
                        if (SYSTEM_REBOOT.equals(stateResult.getName()) && stateResult.isResult()) {
                            Action rebootAction = ActionFactory.lookupById(stateId.getActionId());

                            if (rebootAction.getActionType().equals(ActionFactory.TYPE_REBOOT)) {
                                Optional<ServerAction> rebootServerAction =
                                        rebootAction.getServerActions().stream()
                                                .filter(sa -> sa.getServer().asMinionServer().isPresent() &&
                                                        sa.getServer().asMinionServer().get()
                                                                .getMinionId().equals(minionId))
                                                .findFirst();
                                if (rebootServerAction.isPresent()) {
                                    rebootServerAction.get().setStatus(ActionFactory.STATUS_PICKED_UP);
                                    rebootServerAction.get().setPickupTime(new Date());
                                }
                                else {
                                    LOG.error("Action of type " + SYSTEM_REBOOT +
                                            " found in action chain result but not in actions for minion " +
                                            minionId);
                                }
                            }
                        }

                        if (stateResult.isResult() &&
                                saltUtils.shouldRefreshPackageList(stateResult.getName(),
                                        Optional.of(stateResult.getChanges().getRet()))) {
                            refreshPkg = true;
                        }
                    }
                }
                if (refreshPkg) {
                    MinionServerFactory.findByMinionId(minionId).ifPresent(minion -> {
                        LOG.info("Scheduling a package profile update for minion " + minionId);
                        try {
                            Action pkgList = ActionManager
                                    .schedulePackageRefresh(minion.getOrg(), minion);
                            executeSSHAction(pkgList, minion);
                        }
                        catch (TaskomaticApiException e) {
                            LOG.error("Could not schedule package refresh for minion: " +
                                    minion.getMinionId(), e);
                        }
                    });
                }
                // update minion last checkin
                MinionServerFactory.findByMinionId(minionId).ifPresent(minion ->
                        minion.updateServerInfo());
            }
            else {
                LOG.error("'state.apply mgractionchains.startssh' was successful " +
                        "but not state apply changes are present");
                failActionChain(minionId, firstChunkActionId, Optional.of("Got null result."));
                return false;
            }
        }
        catch (Exception e) {
            LOG.error("Error handling action chain result for SSH minion " +
                    minionId, e);
            failActionChain(minionId, firstChunkActionId,
                    Optional.of("Error handling action chain result:" + e.getMessage()));
            return false;
        }
        return true;
    }

    /**
     * Set the action and dependent actions to failed
     * @param minionId the minion id
     * @param failedActionId the failed action id
     * @param message the message to set to the failed action
     */
    public static void failActionChain(String minionId, Optional<Long> failedActionId, Optional<String> message) {
        failActionChain(minionId, Optional.empty(), failedActionId, message);
    }

    private static void failActionChain(String minionId, Optional<Long> actionChainId, Optional<Long> failedActionId,
                                        Optional<String> message) {
        failedActionId.ifPresent(last ->
                JobReturnEventMessageAction.failDependentServerActions(last, minionId, message));
        MinionServerFactory.findByMinionId(minionId).ifPresent(minion -> {
            SaltActionChainGeneratorService.INSTANCE.removeActionChainSLSFilesForMinion(minion, actionChainId);
        });
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
                    List<MinionSummary> minions = MinionServerFactory.findMinionSummaries(actionIn.getId());

                    if (minions.isEmpty()) {
                        // When an Action Chain contains an Action which does not target
                        // any minion we don't generate any Salt call.
                        LOG.warn("No server actions for action id=" + actionIn.getId());
                        return;
                    }

                    // get Salt calls for this action
                    Map<LocalCall<?>, List<MinionSummary>> actionCalls = callsForAction(actionIn, minions);

                    // TODO how to handle staging jobs?

                    // Salt calls for each minion
                    Map<MinionSummary, List<LocalCall<?>>> callsPerMinion =
                            actionCalls.values().stream().flatMap(m -> m.stream())
                                .collect(Collectors
                                        .toMap(Function.identity(),
                                                m -> actionCalls.entrySet()
                                                        .stream()
                                                        .filter(e -> e.getValue().contains(m))
                                                        .map(e -> e.getKey())
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
                        .collect(Collectors.partitioningBy(minionSummary ->
                        minionSummary.isSshPush(), Collectors.toSet()));

        Set<MinionSummary> sshMinionIds = minionPartitions.get(true);
        Set<MinionSummary> regularMinionIds = minionPartitions.get(false);

        // convert local calls to salt state objects
        Map<MinionSummary, List<SaltState>> statesPerMinion = new HashMap<>();
        minionCalls.forEach((minion, serverActionCalls) -> {
            boolean isSshPush = minion.isSshPush();
            List<SaltState> states = serverActionCalls.stream()
                    .flatMap(saCalls -> {
                        ServerAction sa = saCalls.getKey();
                        List<LocalCall<?>> calls = saCalls.getValue();
                        return convertToState(actionChain.getId(), sa, calls, isSshPush).stream();
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
            extraFilerefs = Optional.of(fileRefsList.stream().collect(Collectors.joining(",")));

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

        // We have generated the sls files for the action chain execution.
        // Delete the action chain db entity as it's no longer needed.
        ActionChainFactory.delete(actionChain);
    }

    private List<SaltState> convertToState(long actionChainId, ServerAction serverAction,
                                           List<LocalCall<?>> calls, boolean isSshPush) {
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
                        if (isSshPush) {
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
                case "system.reboot":
                    Integer time = (Integer)kwargs.get("at_time");
                    return new SaltSystemReboot(stateId,
                            serverAction.getParentAction().getId(), time);
                default:
                    throw new RuntimeException("Salt module call" + fun + " can't be converted to a state.");
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
     * @return minion summaries grouped by local call
     */
    public Map<LocalCall<?>, List<MinionSummary>> errataAction(List<MinionSummary> minionSummaries,
            Set<Long> errataIds) {
        Set<Long> minionServerIds = minionSummaries.stream().map(MinionSummary::getServerId)
                .collect(Collectors.toSet());

        Map<Long, Map<Long, Set<ErrataInfo>>> errataInfos = ServerFactory
                .listErrataNamesForServers(minionServerIds, errataIds);

        // Group targeted minions by errata names
        Map<Set<ErrataInfo>, List<MinionSummary>> collect = minionSummaries.stream()
                .collect(Collectors.groupingBy(minionId -> errataInfos.get(minionId.getServerId())
                        .entrySet().stream()
                        .map(Map.Entry::getValue)
                        .flatMap(Set::stream)
                        .collect(Collectors.toSet())
        ));

        // Convert errata names to LocalCall objects of type State.apply
        return collect.entrySet().stream()
            .collect(Collectors.toMap(entry -> {
                Map<String, Object> params = new HashMap<>();
                params.put(PARAM_REGULAR_PATCHES,
                    entry.getKey().stream()
                        .filter(e -> !e.isUpdateStack())
                        .map(e -> e.getName())
                        .sorted()
                        .collect(Collectors.toList())
                );
                params.put(PARAM_UPDATE_STACK_PATCHES,
                    entry.getKey().stream()
                        .filter(e -> e.isUpdateStack())
                        .map(e -> e.getName())
                        .sorted()
                        .collect(Collectors.toList())
                );
                if (!entry.getKey().stream()
                        .filter(e -> e.includeSalt())
                        .collect(Collectors.toList()).isEmpty()) {
                    params.put("include_salt_upgrade", true);
                }
                return State.apply(
                        Arrays.asList(PACKAGES_PATCHINSTALL),
                        Optional.of(params)
                );
            },
            Map.Entry::getValue));
    }

    private Map<LocalCall<?>, List<MinionSummary>> packagesUpdateAction(
            List<MinionSummary> minionSummaries, PackageUpdateAction action) {
        Map<LocalCall<?>, List<MinionSummary>> ret = new HashMap<>();
        List<List<String>> pkgs = action
                .getDetails().stream().map(d -> Arrays.asList(d.getPackageName().getName(),
                        d.getArch().toUniversalArchString(), d.getEvr().toUniversalEvrString()))
                .collect(Collectors.toList());
        ret.put(State.apply(Arrays.asList(PACKAGES_PKGINSTALL),
                Optional.of(singletonMap(PARAM_PKGS, pkgs))), minionSummaries);
        return ret;
    }

    private Map<LocalCall<?>, List<MinionSummary>> packagesRemoveAction(
            List<MinionSummary> minionSummaries, PackageRemoveAction action) {
        Map<LocalCall<?>, List<MinionSummary>> ret = new HashMap<>();
        List<List<String>> pkgs = action
                .getDetails().stream().map(d -> Arrays.asList(d.getPackageName().getName(),
                        d.getArch().toUniversalArchString(), d.getEvr().toUniversalEvrString()))
                .collect(Collectors.toList());
        ret.put(State.apply(Arrays.asList(PACKAGES_PKGREMOVE),
                Optional.of(singletonMap(PARAM_PKGS, pkgs))), minionSummaries);
        return ret;
    }

    private Map<LocalCall<?>, List<MinionSummary>> packagesRefreshListAction(
            List<MinionSummary> minionSummaries) {
        Map<LocalCall<?>, List<MinionSummary>> ret = new HashMap<>();
        ret.put(State.apply(Arrays.asList(ApplyStatesEventMessage.PACKAGES_PROFILE_UPDATE),
                Optional.empty()), minionSummaries);
        return ret;
    }

    private Map<LocalCall<?>, List<MinionSummary>> hardwareRefreshListAction(
            List<MinionSummary> minionSummaries) {
        Map<LocalCall<?>, List<MinionSummary>> ret = new HashMap<>();

        // salt-ssh minions in the 'true' partition
        // regular minions in the 'false' partition
        Map<Boolean, List<MinionSummary>> partitionBySSHPush = minionSummaries.stream()
                .collect(Collectors.partitioningBy(MinionSummary::isSshPush));

        // Separate SSH push minions from regular minions to apply different states
        List<MinionSummary> sshPushMinions = partitionBySSHPush.get(true);
        List<MinionSummary> regularMinions = partitionBySSHPush.get(false);

        if (!sshPushMinions.isEmpty()) {
            ret.put(State.apply(Arrays.asList(
                    ApplyStatesEventMessage.HARDWARE_PROFILE_UPDATE),
                    Optional.empty()), minionSummaries);
        }
        if (!regularMinions.isEmpty()) {
            ret.put(State.apply(Arrays.asList(
                    ApplyStatesEventMessage.SYNC_CUSTOM_ALL,
                    ApplyStatesEventMessage.HARDWARE_PROFILE_UPDATE),
                    Optional.empty()), minionSummaries);
        }

        return ret;
    }

    private Map<LocalCall<?>, List<MinionSummary>> rebootAction(List<MinionSummary> minionSummaries) {
        Map<LocalCall<?>, List<MinionSummary>> ret = new HashMap<>();
        ret.put(com.suse.salt.netapi.calls.modules.System
                .reboot(Optional.of(3)), minionSummaries);
        return ret;
    }

    /**
     * Deploy files(files, directory, symlink) through state.apply
     *
     * @param minionSummaries a list of minion summaries of the minions involved in the given Action
     * @param action action which has all the revisions
     * @return minion summaries grouped by local call
     */
    private Map<LocalCall<?>, List<MinionSummary>> deployFiles(List<MinionSummary> minionSummaries,
            ConfigAction action) {
        Map<LocalCall<?>, List<MinionSummary>> ret = new HashMap<>();

        Map<Long, MinionSummary> targetMap = minionSummaries.stream().
                collect(Collectors.toMap(MinionSummary::getServerId, minionId-> minionId));

        Map<MinionSummary, Set<ConfigRevision>> serverConfigMap = action.getConfigRevisionActions()
                .stream()
                .filter(cra -> targetMap.containsKey(cra.getServer().getId()))
                .collect(Collectors.groupingBy(
                        cra -> targetMap.get(cra.getServer().getId()),
                        Collectors.mapping(ConfigRevisionAction::getConfigRevision, Collectors.toSet())));
        Map<Set<ConfigRevision>, Set<MinionSummary>> revsServersMap = serverConfigMap.entrySet()
                .stream()
                .collect(Collectors.groupingBy(entry -> entry.getValue(),
                        Collectors.mapping(entry -> entry.getKey(), Collectors.toSet())));
        revsServersMap.forEach((configRevisions, selectedServers) -> {
            List<Map<String, Object>> fileStates = configRevisions
                    .stream()
                    .map(revision -> ConfigChannelSaltManager.getInstance().getStateParameters(revision))
                    .collect(Collectors.toList());
            ret.put(State.apply(Arrays.asList(CONFIG_DEPLOY_FILES),
                    Optional.of(Collections.singletonMap(PARAM_FILES, fileStates))),
                    selectedServers.stream().collect(Collectors.toList()));
        });
        return ret;
    }

    /**
     * Deploy files(files, directory, symlink) through state.apply
     *
     * @param minionSummaries a list of minion summaries of the minions involved in the given Action
     * @param action action which has all the revisions
     * @return minion summaries grouped by local call
     */
    private Map<LocalCall<?>, List<MinionSummary>> diffFiles(List<MinionSummary> minionSummaries, ConfigAction action) {
        Map<LocalCall<?>, List<MinionSummary>> ret = new HashMap<>();
        List<Map<String, Object>> fileStates = action.getConfigRevisionActions().stream()
                .map(revAction -> revAction.getConfigRevision())
                .filter(revision -> revision.isFile() ||
                        revision.isDirectory() ||
                        revision.isSymlink())
                .map(revision -> ConfigChannelSaltManager.getInstance().getStateParameters(revision))
                .collect(Collectors.toList());
        ret.put(com.suse.salt.netapi.calls.modules.State.apply(
                Arrays.asList(CONFIG_DIFF_FILES),
                Optional.of(Collections.singletonMap(PARAM_FILES, fileStates)),
                Optional.of(true), Optional.of(true)), minionSummaries);
        return ret;
    }

    private Map<LocalCall<?>, List<MinionSummary>> remoteCommandAction(
            List<MinionSummary> minions, ScriptAction scriptAction) {
        String script = scriptAction.getScriptActionDetails().getScriptContents();

        Map<LocalCall<?>, List<MinionSummary>> ret = new HashMap<>();
        // write script to /srv/susemanager/salt/scripts/script_<action_id>.sh
        Path scriptFile = saltUtils.getScriptPath(scriptAction.getId());
        try {
            if (!Files.exists(scriptFile)) {
                // make sure parent dir exists
                if (!Files.exists(scriptFile.getParent())) {
                    FileAttribute<Set<PosixFilePermission>> dirAttributes =
                            PosixFilePermissions.asFileAttribute(PosixFilePermissions.fromString("rwxr-xr-x"));

                    Files.createDirectory(scriptFile.getParent(), dirAttributes);
                    // make sure correct user is set
                }
            }

            if (!skipCommandScriptPerms) {
                setFileOwner(scriptFile.getParent());
            }

            // In case of action retry, the files script files will be already created.
            if (!Files.exists(scriptFile)) {
                FileAttribute<Set<PosixFilePermission>> fileAttributes =
                        PosixFilePermissions.asFileAttribute(PosixFilePermissions.fromString("rw-r--r--"));
                Files.createFile(scriptFile, fileAttributes);
                FileUtils.writeStringToFile(scriptFile.toFile(),
                        script.replaceAll("\r\n", "\n"));
            }

            if (!skipCommandScriptPerms) {
                setFileOwner(scriptFile);
            }

            // state.apply remotecommands
            Map<String, Object> pillar = new HashMap<>();
            pillar.put("mgr_remote_cmd_script",
                    SALT_FS_PREFIX + SCRIPTS_DIR + "/" + scriptFile.getFileName());
            pillar.put("mgr_remote_cmd_runas",
                    scriptAction.getScriptActionDetails().getUsername());
            ret.put(State.apply(Arrays.asList(REMOTE_COMMANDS), Optional.of(pillar)), minions);
        }
        catch (IOException e) {
            String errorMsg = "Could not write script to file " + scriptFile + " - " + e;
            LOG.error(errorMsg);
            scriptAction.getServerActions().stream()
                    .filter(entry -> minions.contains(entry.getServer()))
                    .forEach(sa -> {
                        sa.fail("Error scheduling the action: " + errorMsg);
                        ActionFactory.save(sa);
            });
        }
        return ret;
    }

    private void setFileOwner(Path path) throws IOException {
        FileSystem fileSystem = FileSystems.getDefault();
        UserPrincipalLookupService service = fileSystem.getUserPrincipalLookupService();
        UserPrincipal tomcatUser = service.lookupPrincipalByName("tomcat");

        Files.setOwner(path, tomcatUser);
    }

    private Map<LocalCall<?>, List<MinionSummary>> applyStatesAction(
            List<MinionSummary> minionSummaries, List<String> mods, boolean test) {
        Map<LocalCall<?>, List<MinionSummary>> ret = new HashMap<>();
        ret.put(com.suse.salt.netapi.calls.modules.State.apply(mods, Optional.empty(), Optional.of(true),
                test ? Optional.of(test) : Optional.empty()), minionSummaries);
        return ret;
    }

    private Map<LocalCall<?>, List<MinionSummary>> subscribeChanelsAction(
            List<MinionSummary> minionSummaries, SubscribeChannelsActionDetails actionDetails) {
        Map<LocalCall<?>, List<MinionSummary>> ret = new HashMap<>();

        Stream<MinionServer> minions = MinionServerFactory.lookupByIds(
                minionSummaries.stream().map(MinionSummary::getServerId).collect(Collectors.toList()));

        minions.forEach(minion -> {
            List<AccessToken> tokens = new ArrayList<>();

            // generate access tokens
            Set<Channel> allChannels = new HashSet();
            allChannels.addAll(actionDetails.getChannels());
            if (actionDetails.getBaseChannel() != null) {
                allChannels.add(actionDetails.getBaseChannel());
            }

            List<AccessToken> newTokens = allChannels.stream()
                    .map(channel ->
                            AccessTokenFactory.generate(minion, Collections.singleton(channel))
                                    .orElseThrow(() ->
                                            new RuntimeException(
                                                    "Could not generate new channel access token for minion " +
                                                            minion.getMinionId() + " and channel " +
                                                            channel.getName())))
                    .collect(Collectors.toList());

            newTokens.forEach(newToken -> {
                // persist the access tokens to be activated by the Salt job return event
                // if the state.apply channels returns successfully
                newToken.setValid(false);
                actionDetails.getAccessTokens().add(newToken);
            });

            Map<String, Object> chanPillar = new HashMap<>();
            newTokens.forEach(accessToken ->
                accessToken.getChannels().forEach(chan -> {
                    Map<String, Object> chanProps =
                            SaltStateGeneratorService.getChannelPillarData(minion, accessToken, chan);
                    chanPillar.put(chan.getLabel(), chanProps);
                })
            );

            Map<String, Object> pillar = new HashMap<>();
            pillar.put("_mgr_channels_items_name", "mgr_channels_new");
            pillar.put("mgr_channels_new", chanPillar);

            ret.put(State.apply(Arrays.asList(ApplyStatesEventMessage.CHANNELS),
                    Optional.of(pillar)), Collections.singletonList(new MinionSummary(minion)));

        });
        if (commitTransaction) {
            // we must be sure that tokens and action Details are in the database
            // before we return and send the salt calls to update the minions.
            HibernateFactory.commitTransaction();
        }

        return ret;
    }

    private static Map<String, Object> dockerRegPillar(List<ImageStore> stores) {
        Map<String, Object> dockerRegistries = new HashMap<>();
        stores.forEach(store -> {
            Optional.ofNullable(store.getCreds())
                    .ifPresent(credentials -> {
                        Map<String, Object> reg = new HashMap<>();
                        reg.put("email", "tux@example.com");
                        reg.put("password", credentials.getPassword());
                        reg.put("username", credentials.getUsername());
                        dockerRegistries.put(store.getUri(), reg);
                    });
        });
        return dockerRegistries;
    }

    private Map<LocalCall<?>, List<MinionSummary>> imageInspectAction(
            List<MinionSummary> minions, ImageInspectActionDetails details, ImageStore store) {
        Map<String, Object> pillar = new HashMap<>();
        Map<LocalCall<?>, List<MinionSummary>> result = new HashMap<>();
        if (ImageStoreFactory.TYPE_OS_IMAGE.equals(store.getStoreType())) {
            pillar.put("build_id", "build" + details.getBuildActionId());
            LocalCall<Map<String, ApplyResult>> apply = State.apply(
                    Collections.singletonList("images.kiwi-image-inspect"),
                    Optional.of(pillar));
            result.put(apply, minions);
            return result;
        }
        else {
            pillar.put("imagename", store.getUri() + "/" + details.getName() + ":" + details.getVersion());
            LocalCall<Map<String, ApplyResult>> apply = State.apply(
                    Collections.singletonList("images.profileupdate"),
                    Optional.of(pillar));
            result.put(apply, minions);
            return result;
        }
    }

    private Map<LocalCall<?>, List<MinionSummary>> imageBuildAction(
            List<MinionSummary> minionSummaries, Optional<String> version,
            ImageProfile profile, User user, Long actionId) {
        List<ImageStore> imageStores = new LinkedList<>();
        imageStores.add(profile.getTargetStore());

        List<MinionServer> minions = MinionServerFactory.findMinionsByServerIds(
                minionSummaries.stream().map(MinionSummary::getServerId).collect(Collectors.toList()));

        //TODO: optimal scheduling would be to group by host and orgid
        Map<LocalCall<?>, List<MinionSummary>> ret = minions.stream().collect(
                Collectors.toMap(minion -> {

                    //TODO: refactor ActivationKeyHandler.listChannels to share this logic
                    TokenBuilder tokenBuilder = new TokenBuilder(minion.getOrg().getId());
                    tokenBuilder.useServerSecret();
                    tokenBuilder.setExpirationTimeMinutesInTheFuture(
                            Config.get().getInt(
                                    ConfigDefaults.TEMP_TOKEN_LIFETIME
                            )
                    );
                    if (profile.getToken() != null) {
                        tokenBuilder.onlyChannels(profile.getToken().getChannels()
                                .stream().map(Channel::getLabel)
                                .collect(Collectors.toSet()));
                    }
                    String t = "";
                    try {
                        t = tokenBuilder.getToken();
                    }
                    catch (JoseException e) {
                        e.printStackTrace();
                    }
                    final String token = t;

                    Map<String, Object> pillar = new HashMap<>();
                    String host = SaltStateGeneratorService.getChannelHost(minion);

                    profile.asDockerfileProfile().ifPresent(dockerfileProfile -> {
                        Map<String, Object> dockerRegistries = dockerRegPillar(imageStores);
                        pillar.put("docker-registries", dockerRegistries);

                        String repoPath = profile.getTargetStore().getUri() + "/" + profile.getLabel();
                        String tag = version.orElse("");
                        String certificate = "";
                        // salt 2016.11 dockerng require imagename while salt 2018.3 docker requires it separate
                        pillar.put("imagerepopath", repoPath);
                        pillar.put("imagetag", tag);
                        pillar.put("imagename", repoPath + ":" + tag);
                        pillar.put("builddir", dockerfileProfile.getPath());
                        try {
                            //TODO: maybe from the database
                            certificate = Files.readAllLines(
                                    Paths.get("/srv/www/htdocs/pub/RHN-ORG-TRUSTED-SSL-CERT"),
                                    Charset.defaultCharset()
                            ).stream().collect(Collectors.joining("\n\n"));
                        }
                        catch (IOException e) {
                            LOG.error("Could not read certificate", e);
                        }
                        pillar.put("cert", certificate);
                        String repocontent = "";
                        if (profile.getToken() != null) {
                            repocontent = profile.getToken().getChannels().stream().map(s ->
                            {
                                return "[susemanager:" + s.getLabel() + "]\n\n" +
                                        "name=" + s.getName() + "\n\n" +
                                        "enabled=1\n\n" +
                                        "autorefresh=1\n\n" +
                                        "baseurl=https://" + host +
                                        ":443/rhn/manager/download/" + s.getLabel() + "?" +
                                        token + "\n\n" +
                                        "type=rpm-md\n\n" +
                                        "gpgcheck=1\n\n" +
                                        "repo_gpgcheck=0\n\n" +
                                        "pkg_gpgcheck=1\n\n";
                            }).collect(Collectors.joining("\n\n"));

                        }
                        pillar.put("repo", repocontent);
                    });

                    profile.asKiwiProfile().ifPresent(kiwiProfile -> {
                        pillar.put("source", kiwiProfile.getPath());
                        pillar.put("build_id", "build" + actionId);
                        List<String> repos = new ArrayList<>();
                        final ActivationKey activationKey = ActivationKeyFactory.lookupByToken(profile.getToken());
                        Set<Channel> channels = activationKey.getChannels();
                        for (Channel channel: channels) {
                            repos.add("https://" + host +
                                    "/rhn/manager/download/" + channel.getLabel() + "?" +
                                    token);
                        }
                        pillar.put("kiwi_repositories", repos);
                        pillar.put("activation_key", activationKey.getKey());
                    });

                    String saltCall = "";
                    if (profile instanceof DockerfileProfile) {
                        saltCall = "images.docker";
                    }
                    else if (profile instanceof KiwiProfile) {
                        saltCall = "images.kiwi-image-build";
                    }

                    return State.apply(
                            Collections.singletonList(saltCall),
                            Optional.of(pillar)
                    );
                },
                m -> Collections.singletonList(new MinionSummary(m))
        ));

        return ret;
    }

    private Map<LocalCall<?>, List<MinionSummary>> distUpgradeAction(
            DistUpgradeAction action,
            List<MinionSummary> minionSummaries) {
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
                .sorted()
                .map(c -> "susemanager:" + c.getLabel())
                .collect(Collectors.toList()));

        LocalCall<Map<String, ApplyResult>> distUpgrade = State.apply(
                Collections.singletonList(ApplyStatesEventMessage.DISTUPGRADE),
                Optional.of(pillar)
                );
        Map<LocalCall<?>, List<MinionSummary>> ret = new HashMap<>();
        ret.put(distUpgrade, minionSummaries);

        return ret;
    }

    private Map<LocalCall<?>, List<MinionSummary>> scapXccdfEvalAction(
            List<MinionSummary> minionSummaries, ScapActionDetails scapActionDetails) {
        Map<LocalCall<?>, List<MinionSummary>> ret = new HashMap<>();
        String parameters = "eval " +
            scapActionDetails.getParametersContents() + " " + scapActionDetails.getPath();
        ret.put(State.apply(singletonList("scap"),
                Optional.of(singletonMap("mgr_scap_params", (Object)parameters))),
                minionSummaries);
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
                    .collect(Collectors.toList());
            call = State.apply(Arrays.asList(PACKAGES_PKGDOWNLOAD),
                    Optional.of(Collections.singletonMap(PARAM_PKGS, args)));
            LOG.info("Executing staging of packages");
        }
        if (actionIn.getActionType().equals(ActionFactory.TYPE_ERRATA)) {
            Set<Long> errataIds = ((ErrataAction) actionIn).getErrata().stream()
                    .map(e -> e.getId()).collect(Collectors.toSet());
            Map<Long, Map<Long, Set<ErrataInfo>>> errataNames = ServerFactory
                    .listErrataNamesForServers(minionSummaries.stream().map(MinionSummary::getServerId)
                            .collect(Collectors.toSet()), errataIds);
            List<String> errataArgs = errataNames.entrySet().stream()
                .flatMap(e -> e.getValue().entrySet().stream()
                     .flatMap(f -> f.getValue().stream()
                         .map(ErrataInfo::getName)
                     )
                )
                .collect(Collectors.toList());

            call = State.apply(Arrays.asList(PACKAGES_PATCHDOWNLOAD),
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
            LOG.debug("Executing action for: " +
                    minionIds.stream().collect(Collectors.joining(", ")));
        }

        try {
            Map<Boolean, List<MinionSummary>> result = new HashMap<>();

            ScheduleMetadata metadata = ScheduleMetadata.getMetadataForRegularMinionActions(
                    isStagingJob, forcePackageListRefresh, actionIn.getId());
            List<String> results = SaltService.INSTANCE
                    .callAsync(call, new MinionList(minionIds), Optional.of(metadata)).get().getMinions();

            result = minionSummaries.stream().collect(Collectors
                    .partitioningBy(minionId -> results.contains(minionId.getMinionId())));

            return result;
        }
        catch (SaltException ex) {
            LOG.debug("Failed to execute action: " + ex.getMessage());
            Map<Boolean, List<MinionSummary>> result = new HashMap<>();
            result.put(true, Collections.emptyList());
            result.put(false, minionSummaries);
            return result;
        }
    }

    /**
     * @param call the call
     * @param minionSummaries a set of minion summaries of the minions involved in the given Action
     * @return a map containing all minions partitioned by success
     */
    private Map<Boolean, ? extends Collection<MinionSummary>> callAsyncActionChainStart(LocalCall<?> call,
            Set<MinionSummary> minionSummaries) {
        List<String> minionIds = minionSummaries.stream().map(MinionSummary::getMinionId)
                .collect(Collectors.toList());

        if (LOG.isDebugEnabled()) {
            LOG.debug("Executing action chain for: " +
                    minionIds.stream().collect(Collectors.joining(", ")));
        }

        try {
            List<String> results = SaltService.INSTANCE
                    .callAsync(call, new MinionList(minionIds),
                            Optional.of(ScheduleMetadata.getDefaultMetadata().withActionChain())).get().getMinions();

            Map<Boolean, ? extends Collection<MinionSummary>> result = minionSummaries.stream().collect(Collectors
                    .partitioningBy(minion -> results.contains(minion.getMinionId())));

            return result;
        }
        catch (SaltException ex) {
            LOG.debug("Failed to execute action chain: " + ex.getMessage());
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
        Optional<ServerAction> serverAction = action.getServerActions().stream()
                .filter(sa -> sa.getServerId().equals(minion.getId()))
                .findFirst();
        if (serverAction.isPresent()) {
            ServerAction sa = serverAction.get();
            if (sa.getStatus().equals(STATUS_FAILED) ||
                    sa.getStatus().equals(STATUS_COMPLETED)) {
                LOG.info("Action '" + action.getName() + "' is completed or failed." +
                        " Skipping.");
                return;
            }

            if (prerequisiteInStatus(sa, ActionFactory.STATUS_QUEUED)) {
                LOG.info("Prerequisite of action '" + action.getName() + "' is still" +
                        " queued. Skipping executing of the action.");
                return;
            }

            if (prerequisiteInStatus(sa, ActionFactory.STATUS_FAILED)) {
                LOG.info("Failing action '" + action.getName() + "' as its prerequisite '" +
                        action.getPrerequisite().getName() + "' failed.");
                sa.fail(-100L, "Prerequisite failed.");
                return;
            }

            sa.setRemainingTries(sa.getRemainingTries() - 1);

            Map<LocalCall<?>, List<MinionSummary>> calls = callsForAction(action,
                    Arrays.asList(new MinionSummary(minion)));

            for (LocalCall<?> call : calls.keySet()) {
                Optional<JsonElement> result;
                // try-catch as we'd like to log the warning in case of exception
                try {
                    result = saltService.callSync(new ElementCallJson(call), minion.getMinionId());
                }
                catch (RuntimeException e) {
                    LOG.error("Error executing Salt call for action: " + action.getName() +
                            "on minion " + minion.getMinionId(), e);
                    sa.setStatus(STATUS_FAILED);
                    sa.setResultMsg("Error calling Salt: " + e.getMessage());
                    sa.setCompletionTime(new Date());
                    throw e;
                }

                if (!result.isPresent()) {
                    LOG.error("Action '" + action.getName() + "' failed. Got not result from Salt," +
                            " probablly minion is down or could not be contacted.");
                    sa.setStatus(STATUS_FAILED);
                    sa.setResultMsg("Minion is down or could not be contacted.");
                    sa.setCompletionTime(new Date());
                    return;
                }

                result.ifPresent(r -> {
                    if (LOG.isTraceEnabled()) {
                        LOG.trace("Salt call result: " + r);
                    }
                    String function = (String) call.getPayload().get("fun");

                    // reboot needs special handling in case of ssh push
                    if (action.getActionType().equals(ActionFactory.TYPE_REBOOT)) {
                        sa.setStatus(ActionFactory.STATUS_PICKED_UP);
                        sa.setPickupTime(new Date());
                    }
                    else {
                        saltUtils.updateServerAction(sa, 0L, true, "n/a",
                                r, function);
                    }

                    // Perform a "check-in" after every executed action
                    minion.updateServerInfo();

                    // Perform a package profile update in the end if necessary
                    if (saltUtils.shouldRefreshPackageList(function, result)) {
                        LOG.info("Scheduling a package profile update");
                        Action pkgList;
                        try {
                            pkgList = ActionManager.schedulePackageRefresh(minion.getOrg(), minion);
                            executeSSHAction(pkgList, minion);
                        }
                        catch (TaskomaticApiException e) {
                            LOG.error("Could not schedule package refresh for minion: " +
                                    minion.getMinionId());
                            LOG.error(e);
                        }
                    }
                });
            }
        }
        return;
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
                        .map(a -> a.stream());

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
     * @param saltServiceIn to set
     */
    public void setSaltService(SaltService saltServiceIn) {
        this.saltService = saltServiceIn;
    }

    /**
     * Only used in unit tests.
     * @param skipCommandScriptPermsIn to set
     */
    public void setSkipCommandScriptPerms(boolean skipCommandScriptPermsIn) {
        this.skipCommandScriptPerms = skipCommandScriptPermsIn;
    }

}
