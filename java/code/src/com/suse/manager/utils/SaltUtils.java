/*
 * Copyright (c) 2016--2021 SUSE LLC
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

package com.suse.manager.utils;

import static com.suse.manager.webui.services.SaltConstants.SALT_CP_PUSH_ROOT_PATH;
import static com.suse.manager.webui.services.SaltConstants.SALT_FILE_GENERATION_TEMP_PATH;
import static com.suse.manager.webui.services.SaltConstants.SCRIPTS_DIR;
import static com.suse.manager.webui.services.SaltConstants.SUMA_STATE_FILES_ROOT_PATH;

import com.redhat.rhn.common.hibernate.HibernateFactory;
import com.redhat.rhn.common.localization.LocalizationService;
import com.redhat.rhn.common.messaging.MessageQueue;
import com.redhat.rhn.domain.action.Action;
import com.redhat.rhn.domain.action.ActionFactory;
import com.redhat.rhn.domain.action.ActionStatus;
import com.redhat.rhn.domain.action.ActionType;
import com.redhat.rhn.domain.action.channel.SubscribeChannelsAction;
import com.redhat.rhn.domain.action.config.ConfigRevisionActionResult;
import com.redhat.rhn.domain.action.config.ConfigVerifyAction;
import com.redhat.rhn.domain.action.dup.DistUpgradeAction;
import com.redhat.rhn.domain.action.dup.DistUpgradeActionDetails;
import com.redhat.rhn.domain.action.dup.DistUpgradeChannelTask;
import com.redhat.rhn.domain.action.salt.ApplyStatesAction;
import com.redhat.rhn.domain.action.salt.ApplyStatesActionResult;
import com.redhat.rhn.domain.action.salt.build.ImageBuildAction;
import com.redhat.rhn.domain.action.salt.build.ImageBuildActionDetails;
import com.redhat.rhn.domain.action.salt.inspect.ImageInspectAction;
import com.redhat.rhn.domain.action.salt.inspect.ImageInspectActionDetails;
import com.redhat.rhn.domain.action.scap.ScapAction;
import com.redhat.rhn.domain.action.script.ScriptResult;
import com.redhat.rhn.domain.action.script.ScriptRunAction;
import com.redhat.rhn.domain.action.server.ServerAction;
import com.redhat.rhn.domain.action.virtualization.BaseVirtualizationGuestAction;
import com.redhat.rhn.domain.action.virtualization.BaseVirtualizationNetworkAction;
import com.redhat.rhn.domain.action.virtualization.BaseVirtualizationPoolAction;
import com.redhat.rhn.domain.channel.AccessTokenFactory;
import com.redhat.rhn.domain.channel.Channel;
import com.redhat.rhn.domain.config.ConfigRevision;
import com.redhat.rhn.domain.image.ImageFile;
import com.redhat.rhn.domain.image.ImageInfo;
import com.redhat.rhn.domain.image.ImageInfoFactory;
import com.redhat.rhn.domain.image.ImagePackage;
import com.redhat.rhn.domain.image.ImageProfile;
import com.redhat.rhn.domain.image.ImageProfileFactory;
import com.redhat.rhn.domain.image.ImageRepoDigest;
import com.redhat.rhn.domain.image.OSImageStoreUtils;
import com.redhat.rhn.domain.notification.NotificationMessage;
import com.redhat.rhn.domain.notification.UserNotificationFactory;
import com.redhat.rhn.domain.notification.types.StateApplyFailed;
import com.redhat.rhn.domain.product.SUSEProduct;
import com.redhat.rhn.domain.product.SUSEProductFactory;
import com.redhat.rhn.domain.product.Tuple2;
import com.redhat.rhn.domain.rhnpackage.PackageArch;
import com.redhat.rhn.domain.rhnpackage.PackageEvr;
import com.redhat.rhn.domain.rhnpackage.PackageEvrFactory;
import com.redhat.rhn.domain.rhnpackage.PackageFactory;
import com.redhat.rhn.domain.rhnpackage.PackageName;
import com.redhat.rhn.domain.rhnpackage.PackageType;
import com.redhat.rhn.domain.server.InstalledPackage;
import com.redhat.rhn.domain.server.InstalledProduct;
import com.redhat.rhn.domain.server.MinionServer;
import com.redhat.rhn.domain.server.Server;
import com.redhat.rhn.domain.server.ServerFactory;
import com.redhat.rhn.domain.user.User;
import com.redhat.rhn.frontend.action.common.BadParameterException;
import com.redhat.rhn.manager.action.ActionManager;
import com.redhat.rhn.manager.audit.ScapManager;
import com.redhat.rhn.manager.errata.ErrataManager;
import com.redhat.rhn.manager.rhnpackage.PackageManager;
import com.redhat.rhn.manager.system.SystemManager;
import com.redhat.rhn.taskomatic.TaskomaticApi;
import com.redhat.rhn.taskomatic.TaskomaticApiException;

import com.suse.manager.reactor.hardware.CpuArchUtil;
import com.suse.manager.reactor.hardware.HardwareMapper;
import com.suse.manager.reactor.messaging.ApplyStatesEventMessage;
import com.suse.manager.reactor.messaging.ChannelsChangedEventMessage;
import com.suse.manager.reactor.utils.RhelUtils;
import com.suse.manager.reactor.utils.ValueMap;
import com.suse.manager.saltboot.SaltbootUtils;
import com.suse.manager.webui.controllers.bootstrap.BootstrapError;
import com.suse.manager.webui.controllers.bootstrap.SaltBootstrapError;
import com.suse.manager.webui.controllers.utils.ContactMethodUtil;
import com.suse.manager.webui.services.SaltStateGeneratorService;
import com.suse.manager.webui.services.iface.SaltApi;
import com.suse.manager.webui.services.iface.SystemQuery;
import com.suse.manager.webui.services.impl.runner.MgrUtilRunner;
import com.suse.manager.webui.services.pillar.MinionPillarManager;
import com.suse.manager.webui.utils.YamlHelper;
import com.suse.manager.webui.utils.salt.custom.DistUpgradeDryRunSlsResult;
import com.suse.manager.webui.utils.salt.custom.DistUpgradeOldSlsResult;
import com.suse.manager.webui.utils.salt.custom.DistUpgradeSlsResult;
import com.suse.manager.webui.utils.salt.custom.FilesDiffResult;
import com.suse.manager.webui.utils.salt.custom.FilesDiffResult.DirectoryResult;
import com.suse.manager.webui.utils.salt.custom.FilesDiffResult.FileResult;
import com.suse.manager.webui.utils.salt.custom.FilesDiffResult.SymLinkResult;
import com.suse.manager.webui.utils.salt.custom.HwProfileUpdateSlsResult;
import com.suse.manager.webui.utils.salt.custom.ImageChecksum;
import com.suse.manager.webui.utils.salt.custom.ImageInspectSlsResult;
import com.suse.manager.webui.utils.salt.custom.ImagesProfileUpdateSlsResult;
import com.suse.manager.webui.utils.salt.custom.KernelLiveVersionInfo;
import com.suse.manager.webui.utils.salt.custom.OSImageBuildImageInfoResult;
import com.suse.manager.webui.utils.salt.custom.OSImageBuildSlsResult;
import com.suse.manager.webui.utils.salt.custom.OSImageInspectSlsResult;
import com.suse.manager.webui.utils.salt.custom.PkgProfileUpdateSlsResult;
import com.suse.manager.webui.utils.salt.custom.RetOpt;
import com.suse.manager.webui.utils.salt.custom.SystemInfo;
import com.suse.manager.webui.websocket.VirtNotifications;
import com.suse.salt.netapi.calls.modules.Openscap;
import com.suse.salt.netapi.calls.modules.Pkg;
import com.suse.salt.netapi.calls.modules.Pkg.Info;
import com.suse.salt.netapi.calls.modules.State;
import com.suse.salt.netapi.calls.modules.Zypper.ProductInfo;
import com.suse.salt.netapi.datatypes.target.MinionList;
import com.suse.salt.netapi.errors.SaltError;
import com.suse.salt.netapi.parser.JsonParser;
import com.suse.salt.netapi.results.Change;
import com.suse.salt.netapi.results.CmdResult;
import com.suse.salt.netapi.results.ModuleRun;
import com.suse.salt.netapi.results.Ret;
import com.suse.salt.netapi.results.SSHResult;
import com.suse.salt.netapi.results.StateApplyResult;
import com.suse.salt.netapi.utils.Xor;
import com.suse.utils.Json;
import com.suse.utils.Opt;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParseException;
import com.google.gson.JsonSyntaxException;
import com.google.gson.reflect.TypeToken;

import org.apache.commons.lang3.StringEscapeUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.io.File;
import java.io.FileInputStream;
import java.io.InputStream;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.Duration;
import java.time.Instant;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;
import java.util.function.Function;
import java.util.stream.Collectors;
import java.util.stream.Stream;

/**
 * SaltUtils
 */
public class SaltUtils {

    /** Package-affecting Salt state module names. */
    private static final List<String> PKG_STATE_MODULES = Arrays.asList(
        "pkg.group_installed", "pkg.installed", "pkg.latest", "pkg.patch_installed",
        "pkg.purged", "pkg.removed", "pkg.uptodate", "product.all_installed"
    );

    /** Package-affecting Salt execution module names. */
    private static final List<String> PKG_EXECUTION_MODULES = Arrays.asList(
        "pkg.group_install", "pkg.install", "pkg.purge", "pkg.remove", "pkg.upgrade"
    );

    private static final Logger LOG = LogManager.getLogger(SaltUtils.class);
    private static final TaskomaticApi TASKOMATIC_API = new TaskomaticApi();

    private Path scriptsDir = Paths.get(SUMA_STATE_FILES_ROOT_PATH, SCRIPTS_DIR);

    private final SystemQuery systemQuery;
    private final SaltApi saltApi;

    private String xccdfResumeXsl = "/usr/share/susemanager/scap/xccdf-resume.xslt.in";

    // SUSE OS family as defined in Salt grains
    private static final String OS_FAMILY_SUSE = "Suse";

    private static final LocalizationService LOCALIZATION = LocalizationService.getInstance();

    /**
     * Enumerates results of handlePackageChanges().
     */
    public enum PackageChangeOutcome {
        /**
         * Changed packages have been persisted in the database.
         */
        DONE,
        /**
         * A separate full refresh is necessary.
         */
        NEEDS_REFRESHING
    }

    /**
     * Constructor for testing purposes.
     *
     * @param systemQueryIn
     * @param saltApiIn
     */
    public SaltUtils(SystemQuery systemQueryIn, SaltApi saltApiIn) {
        this.saltApi = saltApiIn;
        this.systemQuery = systemQueryIn;
    }

    /**
     * Figure out if the list of packages has changed based on the result of a Salt call
     * given as JsonElement. This information is used to decide if we should trigger a
     * package list refresh.
     *
     * @param function the Salt function that was used
     * @param callResult the result of the call
     * @return true if installed packages have changed or unparsable json, otherwise false
     */
    public boolean shouldRefreshPackageList(Optional<Xor<String[], String>> function,
            Optional<JsonElement> callResult) {
        List<String> functions = function.map(x -> x.fold(Arrays::asList, List::of)).orElseGet(ArrayList::new);
        if (functions.stream().anyMatch(PKG_EXECUTION_MODULES::contains)) {
            return true;
        }
        if (functions.contains("state.apply")) {
            return Opt.fold(
                callResult.flatMap(SaltUtils::jsonEventToStateApplyResults),
                () -> false,
                results -> results.entrySet().stream()
                    .anyMatch(result -> extractFunction(result.getKey())
                        .map(fn -> fn.equals("mgrcompat.module_run") ?
                            result.getValue().getName()
                                    .map(x -> x.fold(Arrays::asList, List::of))
                                    .orElseGet(ArrayList::new)
                                    .stream().anyMatch(PKG_EXECUTION_MODULES::contains) :
                            PKG_STATE_MODULES.contains(fn)
                        ).orElse(false) &&
                        !result.getValue().getChanges().isEmpty()
                    ));
        }

        return false;
    }

    /**
     * Handles package updates by applying delta information or scheduling a full
     * refresh if necessary.
     *
     * @param function salt function
     * @param callResult salt result
     * @param server server to update
     * @return an outcome
     */
    public PackageChangeOutcome handlePackageChanges(Optional<Xor<String[], String>> function,
            JsonElement callResult, Server server) {
        final PackageChangeOutcome outcome;

        List<String> functions = function.map(x -> x.fold(Arrays::asList, List::of)).orElseGet(List::of);

        if (functions.isEmpty()) {
            LOG.error("NULL function for: {}{}", server.getName(), callResult);
            throw new BadParameterException("function must not be NULL");
        }

        if (functions.stream().anyMatch(PKG_STATE_MODULES::contains)) {
            Map<String, Change<Xor<String, List<Pkg.Info>>>> delta = Json.GSON.fromJson(
                callResult,
                new TypeToken<Map<String, Change<Xor<String, List<Pkg.Info>>>>>() { }
                .getType()
            );
            ErrataManager.insertErrataCacheTask(server);
            outcome = applyChangesFromStateModule(delta, server);
        }
        else if (functions.contains("state.apply")) {
            Map<String, JsonElement> apply = Json.GSON.fromJson(
                callResult, new TypeToken<Map<String, JsonElement>>() { }.getType());
            ErrataManager.insertErrataCacheTask(server);
            outcome = applyChangesFromStateApply(apply, server);
        }
        else {
            outcome = PackageChangeOutcome.DONE;
        }

        return outcome;
    }

    /**
     * Extract salt function/module information from state apply string
     * @param value state apply string
     * @return salt function / module info
     */
    public static Optional<String> extractFunction(String value) {
        String[] split = value.split("_\\|-");
        if (split.length == 4) {
            String module = split[0];
            String function = split[3];
            return Optional.of(module + "." + function);
        }
        else {
            LOG.error("Could not parse Salt function call: {}", value);
            return Optional.empty();
        }
    }

    /**
     * applies the package changes to the server or schedules a package list
     * refresh if not enough data is present
     * @param changes map of packages changes
     * @param server server to update
     * @return an outcome
     */
    public static PackageChangeOutcome applyChangesFromStateModule(
            Map<String, Change<Xor<String, List<Pkg.Info>>>> changes,
            Server server) {
        boolean fullRefreshNeeded = changes.entrySet().stream().anyMatch(
            e ->
                e.getKey().endsWith("-release") ||
                // Live patching requires refresh to fetch the updated LP version
                e.getKey().startsWith("kernel-livepatch-") ||
                (e.getValue().getNewValue().isLeft() &&
                 e.getValue().getOldValue().isLeft())
        );

        if (fullRefreshNeeded) {
            return PackageChangeOutcome.NEEDS_REFRESHING;
        }
        else {
            HibernateFactory.doWithoutAutoFlushing(() -> applyDeltaPackageInfo(changes, server));
            return PackageChangeOutcome.DONE;
        }
    }


    /**
     * Applies a package changeset to a server.
     *
     * @param changes the changes
     * @param server the server
     */
    private static void applyDeltaPackageInfo(
            Map<String, Change<Xor<String, List<Pkg.Info>>>> changes, Server server) {
        // normalise salts type madness
        Map<String, Change<List<Pkg.Info>>> collect = changes.entrySet().stream()
                .collect(
                Collectors.toMap(
                        Map.Entry::getKey,
                        e -> e.getValue().map(
                                xor -> xor.getOrElse(Collections::emptyList))
                )
        );
        Map<String, InstalledPackage> currentPackages = server.getPackages().stream()
                .collect(Collectors.toMap(
                        SaltUtils::packageToKey,
                        Function.identity()
                ));
        collect.entrySet().stream().forEach(e -> {
            String name = e.getKey();
            Change<List<Info>> change = e.getValue();

            // Sometimes Salt lists the same NEVRA twice, only with different install timestamps.
            // Use a merge function is to ignore these duplicate entries.
            Map<String, Info> newPackages = change.getNewValue().stream()
                    .collect(Collectors.toMap(info -> packageToKey(name, info), Function.identity(), (a, b) -> a));

            change.getOldValue().stream().forEach(info -> {
                String key = packageToKey(name, info);
                if (!newPackages.containsKey(key)) {
                    Optional.ofNullable(currentPackages.get(key))
                    .ifPresent(ip -> server.getPackages().remove(ip));
                }
            });

            List<InstalledPackage> packagesToAdd = newPackages.values().stream()
                    .map(info -> Optional.ofNullable(currentPackages.get(packageToKey(name, info))))
                    .filter(Optional::isPresent).map(Optional::get).collect(Collectors.toList());

            Map<String, Tuple2<String, Info>> packagesToCreate = newPackages.values().stream()
                    .filter(info -> !currentPackages.containsKey(packageToKey(name, info)))
                    .collect(Collectors.toMap(info -> packageToKey(name, info), info -> new Tuple2(name, info)));

            packagesToAdd.addAll(createPackagesFromSalt(packagesToCreate, server));
            server.getPackages().addAll(packagesToAdd);
        });
        SystemManager.updateSystemOverview(server.getId());
    }

    /**
     * Extracts and applies package delta information and updates the server with it.
     *
     * @param apply map of state apply results
     * @param server server to update
     * @return an outcome
     */
    public static PackageChangeOutcome applyChangesFromStateApply(
            Map<String, JsonElement> apply, Server server) {
        List<StateApplyResult<JsonElement>> collect =
                apply.entrySet().stream()
                        .flatMap(e -> extractFunction(e.getKey()).<Stream<StateApplyResult<JsonElement>>>map(fn -> {
                    if (fn.equals("mgrcompat.module_run")) {
                        StateApplyResult<JsonElement> ap = Json.GSON.fromJson(
                                e.getValue(),
                                new TypeToken<StateApplyResult<JsonElement>>() {
                                }.getType()
                        );
                        if (
                            ap.getName()
                                    .map(x -> x.fold(Arrays::asList, List::of))
                                    .orElseGet(ArrayList::new)
                                    .stream().anyMatch(PKG_EXECUTION_MODULES::contains)
                        ) {
                            return Stream.of(ap);
                        }
                        else {
                            return Stream.empty();
                        }
                    }
                    else if (PKG_STATE_MODULES.contains(fn)) {
                        return Stream.of(
                                Json.GSON.<StateApplyResult<JsonElement>>fromJson(e.getValue(),
                                        new TypeToken<StateApplyResult<JsonElement>>() {
                                        }.getType()
                                )
                        );
                    }
                    else {
                        return Stream.empty();
                    }
                }).orElseGet(Stream::empty))
                // we sort by run order process multiple package changing states right
                .sorted(Comparator.comparingInt(StateApplyResult::getRunNum))
                .collect(Collectors.toList());
        for (StateApplyResult<JsonElement> value : collect) {
              Map<String, Change<Xor<String, List<Pkg.Info>>>> delta =
                      extractPackageDelta(value.getChanges());

            if (applyChangesFromStateModule(delta, server) ==
                    PackageChangeOutcome.NEEDS_REFRESHING) {
                return PackageChangeOutcome.NEEDS_REFRESHING;
            }
        }
        return PackageChangeOutcome.DONE;
    }

    private static Map<String, Change<Xor<String, List<Info>>>> extractPackageDelta(
            JsonElement json) {
        if (json.getAsJsonObject().has("ret")) {
            return
            Json.GSON.<Ret<Map<String, Change<Xor<String, List<Pkg.Info>>>>>>fromJson(
                json,
                new TypeToken<Ret<Map<String, Change<Xor<String, List<Pkg.Info>>>>>>() { }
                        .getType()
            ).getRet();
        }
        else {
            return Json.GSON.fromJson(
                json,
                new TypeToken<Map<String, Change<Xor<String, List<Pkg.Info>>>>>() { }
                    .getType()
            );
        }
    }

    /**
     * Update a given server action based on data from the corresponding job return event.
     *
     * @param serverAction the server action to update
     * @param retcode return code
     * @param success if the action was successful
     * @param jid salt job id for the action
     * @param jsonResult the result of the action as json
     * @param function salt function used for the action
     */
    public void updateServerAction(ServerAction serverAction, long retcode,
            boolean success, String jid, JsonElement jsonResult, Optional<Xor<String[], String>> function) {
        serverAction.setCompletionTime(new Date());

        // Set the result code defaulting to 0
        serverAction.setResultCode(retcode);

        // If the State was not executed due 'require' statement
        // we directly set the action to FAILED.
        if (jsonResult == null && function.isEmpty()) {
            serverAction.setStatus(ActionFactory.STATUS_FAILED);
            serverAction.setResultMsg("Prerequisite failed");
            return;
        }

        // Determine the final status of the action
        if (actionFailed(function, jsonResult, success, retcode)) {
            serverAction.setStatus(ActionFactory.STATUS_FAILED);
            // check if the minion is locked (blackout mode)
            String output = getJsonResultWithPrettyPrint(jsonResult);
            if (output.startsWith("\'ERROR") && output.contains("Minion in blackout mode")) {
                serverAction.setResultMsg(output);
                return;
            }
        }
        else {
            serverAction.setStatus(ActionFactory.STATUS_COMPLETED);
        }

        Action action = HibernateFactory.unproxy(serverAction.getParentAction());
        if (action.getActionType().equals(ActionFactory.TYPE_APPLY_STATES)) {
            handleStateApplyData(serverAction, jsonResult, retcode, success);
        }
        else if (action.getActionType().equals(ActionFactory.TYPE_SCRIPT_RUN)) {
            Map<String, StateApplyResult<CmdResult>> stateApplyResult = Json.GSON.fromJson(jsonResult,
                    new TypeToken<Map<String, StateApplyResult<CmdResult>>>() { }.getType());
            CmdResult result = stateApplyResult.entrySet().stream()
                    .findFirst().map(e -> e.getValue().getChanges())
                    .orElseGet(CmdResult::new);
            ScriptRunAction scriptAction = (ScriptRunAction) action;
            ScriptResult scriptResult = Optional.ofNullable(
                    scriptAction.getScriptActionDetails().getResults())
                    .orElse(Collections.emptySet())
                    .stream()
                    .filter(res -> serverAction.getServerId().equals(res.getServerId()))
                    .findFirst()
                    .orElse(new ScriptResult());

            scriptAction.getScriptActionDetails().addResult(scriptResult);
            scriptResult.setActionScriptId(scriptAction.getScriptActionDetails().getId());
            scriptResult.setServerId(serverAction.getServerId());
            scriptResult.setReturnCode(retcode);

            // Start and end dates
            Date startDate = action.getCreated().before(action.getEarliestAction()) ?
                    action.getEarliestAction() : action.getCreated();
            scriptResult.setStartDate(startDate);
            scriptResult.setStopDate(serverAction.getCompletionTime());

            // Depending on the status show stdout or stderr in the output
            if (serverAction.getStatus().equals(ActionFactory.STATUS_FAILED)) {
                serverAction.setResultMsg("Failed to execute script. [jid=" + jid + "]");
            }
            else {
                serverAction.setResultMsg("Script executed successfully. [jid=" +
                        jid + "]");
            }
            scriptResult.setOutput(printStdMessages(result.getStderr(), result.getStdout()).getBytes());
        }
        else if (action.getActionType().equals(ActionFactory.TYPE_IMAGE_BUILD)) {
            handleImageBuildData(serverAction, jsonResult);
        }
        else if (action.getActionType().equals(ActionFactory.TYPE_IMAGE_INSPECT)) {
            handleImageInspectData(serverAction, jsonResult);
        }
        else if (action.getActionType().equals(ActionFactory.TYPE_PACKAGES_REFRESH_LIST)) {
            if (serverAction.getStatus().equals(ActionFactory.STATUS_FAILED)) {
                serverAction.setResultMsg("Failure");
            }
            else {
                serverAction.setResultMsg("Success");
            }
            serverAction.getServer().asMinionServer()
                    .ifPresent(minionServer -> handlePackageProfileUpdate(minionServer, Json.GSON.fromJson(jsonResult,
                    PkgProfileUpdateSlsResult.class)));
        }
        else if (action.getActionType().equals(ActionFactory.TYPE_PACKAGES_LOCK)) {
            handlePackageLockData(serverAction, jsonResult, action);
        }
        else if (action.getActionType().equals(ActionFactory.TYPE_HARDWARE_REFRESH_LIST)) {
            if (serverAction.getStatus().equals(ActionFactory.STATUS_FAILED)) {
                serverAction.setResultMsg("Failure");
            }
            else {
                serverAction.setResultMsg("Success");
            }
            serverAction.getServer().asMinionServer()
                    .ifPresent(minionServer -> handleHardwareProfileUpdate(minionServer, Json.GSON.fromJson(jsonResult,
                    HwProfileUpdateSlsResult.class), serverAction));
        }
        else if (action.getActionType().equals(ActionFactory.TYPE_DIST_UPGRADE)) {
            DistUpgradeAction dupAction = (DistUpgradeAction) action;
            DistUpgradeActionDetails actionDetails = dupAction.getDetails();
            if (actionDetails.isDryRun()) {
                Map<Boolean, List<Channel>> collect = actionDetails.getChannelTasks()
                        .stream().collect(Collectors.partitioningBy(
                                ct -> ct.getTask() == DistUpgradeChannelTask.SUBSCRIBE,
                                Collectors.mapping(DistUpgradeChannelTask::getChannel,
                                        Collectors.toList())
                        ));
                List<Channel> subbed = collect.get(true);
                List<Channel> unsubbed = collect.get(false);
                Set<Channel> currentChannels = serverAction.getServer().getChannels();
                currentChannels.removeAll(subbed);
                currentChannels.addAll(unsubbed);
                ServerFactory.save(serverAction.getServer());
                MessageQueue.publish(
                        new ChannelsChangedEventMessage(serverAction.getServerId()));
                MessageQueue.publish(
                        new ApplyStatesEventMessage(serverAction.getServerId(), false,
                                ApplyStatesEventMessage.CHANNELS));
                String message = parseDryRunMessage(jsonResult);
                serverAction.setResultMsg(message);
            }
            else {
                String message = parseMigrationMessage(jsonResult);
                serverAction.setResultMsg(message);

                // Make sure grains are updated after dist upgrade
                serverAction.getServer().asMinionServer().ifPresent(minionServer -> {
                    MinionList minionTarget = new MinionList(minionServer.getMinionId());
                    saltApi.syncGrains(minionTarget);
                });
            }

        }
        else if (action.getActionType().equals(ActionFactory.TYPE_SCAP_XCCDF_EVAL)) {
            handleScapXccdfEval(serverAction, jsonResult, action);
        }
        else if (action.getActionType().equals(ActionFactory.TYPE_CONFIGFILES_DIFF)) {
            handleFilesDiff(jsonResult, action);
            serverAction.setResultMsg(LocalizationService.getInstance().getMessage("configfiles.diffed"));
            /**
             * For comparison we are simply using file.managed state in dry-run mode, Salt doesn't return
             * 'result' attribute(actionFailed method check this attribute) when File(File, Dir, Symlink)
             * already exist on the system and action is considered as Failed even though there was no error.
             */
            serverAction.setStatus(ActionFactory.STATUS_COMPLETED);
        }
        else if (action.getActionType().equals(ActionFactory.TYPE_CONFIGFILES_DEPLOY)) {
            if (serverAction.getStatus().equals(ActionFactory.STATUS_COMPLETED)) {
                serverAction.setResultMsg(LocalizationService.getInstance().getMessage("configfiles.deployed"));
            }
            else {
                serverAction.setResultMsg(getJsonResultWithPrettyPrint(jsonResult));
            }
        }
        else if (action.getActionType().equals(ActionFactory.TYPE_SUBSCRIBE_CHANNELS)) {
            handleSubscribeChannels(serverAction, jsonResult, action);
        }
        else if (action instanceof BaseVirtualizationGuestAction) {
            // Tell VirtNotifications that we got a change, passing actionId
            VirtNotifications.spreadActionUpdate(action);
            // Dump the whole message since the failure could be anywhere in the chain
            serverAction.setResultMsg(getJsonResultWithPrettyPrint(jsonResult));
        }
        else if (action instanceof BaseVirtualizationPoolAction || action instanceof BaseVirtualizationNetworkAction) {
            // Tell VirtNotifications that we got a pool action change, passing action
            VirtNotifications.spreadActionUpdate(action);
            // Intentionally don't get only the comment since the changes value could be interesting
            serverAction.setResultMsg(getJsonResultWithPrettyPrint(jsonResult));
        }
        else {
           serverAction.setResultMsg(getJsonResultWithPrettyPrint(jsonResult));
        }
    }

    private void handleStateApplyData(ServerAction serverAction, JsonElement jsonResult, long retcode,
            boolean success) {
        ApplyStatesAction applyStatesAction =
                (ApplyStatesAction)HibernateFactory.unproxy(serverAction.getParentAction());

        // Revisit the action status if test=true
        if (applyStatesAction.getDetails().isTest() && success && retcode == 0) {
            serverAction.setStatus(ActionFactory.STATUS_COMPLETED);
        }

        ApplyStatesActionResult statesResult = Optional.ofNullable(
                applyStatesAction.getDetails().getResults())
                .orElse(Collections.emptySet())
                .stream()
                .filter(result ->
                        serverAction.getServerId().equals(result.getServerId()))
                .findFirst()
                .orElse(new ApplyStatesActionResult());
        applyStatesAction.getDetails().addResult(statesResult);
        statesResult.setActionApplyStatesId(applyStatesAction.getDetails().getId());
        statesResult.setServerId(serverAction.getServerId());
        statesResult.setReturnCode(retcode);

        // Set the output to the result
        statesResult.setOutput(getJsonResultWithPrettyPrint(jsonResult).getBytes());

        // Create the result message depending on the action status
        String states = applyStatesAction.getDetails().getMods().isEmpty() ?
                "highstate" : applyStatesAction.getDetails().getMods().toString();
        String message = "Successfully applied state(s): " + states;
        if (serverAction.getStatus().equals(ActionFactory.STATUS_FAILED)) {
            message = "Failed to apply state(s): " + states;

            NotificationMessage nm = UserNotificationFactory.createNotificationMessage(
                    new StateApplyFailed(serverAction.getServer().getName(),
                            serverAction.getServerId(), serverAction.getParentAction().getId()));

            Set<User> admins = new HashSet<>(ServerFactory.listAdministrators(serverAction.getServer()));
            // TODO: are also org admins and the creator part of this list?
            UserNotificationFactory.storeForUsers(nm, admins);
        }
        if (applyStatesAction.getDetails().isTest()) {
            message += " (test-mode)";
        }
        serverAction.setResultMsg(message);

        serverAction.getServer().asMinionServer().ifPresent(minion -> {
            if (jsonResult.isJsonObject()) {
                updateSystemInfo(jsonResult, minion);
            }
        });
    }

    private void handlePackageLockData(ServerAction serverAction, JsonElement jsonResult, Action action) {
        if (serverAction.getStatus().equals(ActionFactory.STATUS_FAILED)) {
            String msg = "Error while changing the lock status";
            jsonEventToStateApplyResults(jsonResult).ifPresentOrElse(
                    r -> {
                        if (r.containsKey("pkg_|-pkg_locked_|-pkg_locked_|-held")) {
                            serverAction.setResultMsg(msg + ":\n" +
                                    r.get("pkg_|-pkg_locked_|-pkg_locked_|-held").getComment());
                        }
                        else {
                            serverAction.setResultMsg(msg);
                        }
                    },
                    () -> serverAction.setResultMsg(msg));
            serverAction.getServer().asMinionServer()
                    .ifPresent(minionServer -> PackageManager.syncLockedPackages(minionServer.getId(), action.getId()));
        }
        else {
            String msg = "Successfully changed lock status";
            jsonEventToStateApplyResults(jsonResult).ifPresentOrElse(
                    r -> serverAction.setResultMsg(msg + ":\n" +
                            r.get("pkg_|-pkg_locked_|-pkg_locked_|-held").getComment()),
                    () -> serverAction.setResultMsg(msg));
            serverAction.getServer().asMinionServer().ifPresent(minionServer -> {
                PackageManager.updateLockedPackages(minionServer.getId(), action.getId());
                PackageManager.updateUnlockedPackages(minionServer.getId(), action.getId());
            });
        }
    }

    /**
     * Return the path where scripts from Remote Commands Actions are stored.
     * @param scriptActionId the ID of the ScriptAction
     * @return a Path object to the storage.
     */
    public Path getScriptPath(Long scriptActionId) {
        return scriptsDir.resolve("script_" + scriptActionId + ".sh");
    }

    /**
     * Get the raw json result with pretty-print. If json result will be longer than 1024, it will
     * be trimmed.
     * @param jsonResult json result with pretty print
     */
    private String  getJsonResultWithPrettyPrint(JsonElement jsonResult) {
        return YamlHelper.INSTANCE.dump(Json.GSON.fromJson(jsonResult, Object.class));
    }

    private void handleSubscribeChannels(ServerAction serverAction, JsonElement jsonResult, Action action) {
        if (serverAction.getStatus().equals(ActionFactory.STATUS_COMPLETED)) {
            serverAction.setResultMsg("Successfully applied state: " + ApplyStatesEventMessage.CHANNELS);
            SubscribeChannelsAction sca = (SubscribeChannelsAction)action;

            // if successful update channels in db and trigger pillar refresh
            SystemManager.updateServerChannels(
                    action.getSchedulerUser(),
                    serverAction.getServer(),
                    Optional.ofNullable(sca.getDetails().getBaseChannel()),
                    sca.getDetails().getChannels());
        }
        else {
            //set the token as invalid
            SubscribeChannelsAction sca = (SubscribeChannelsAction)action;
            sca.getDetails().getAccessTokens().forEach(token -> {
                token.setValid(false);
                AccessTokenFactory.save(token);
            });

            serverAction.setResultMsg("Failed to apply state: " + ApplyStatesEventMessage.CHANNELS + ".\n" +
                    getJsonResultWithPrettyPrint(jsonResult));
        }
    }

    private String parseMigrationMessage(JsonElement jsonResult) {
        try {
            DistUpgradeSlsResult distUpgradeSlsResult = Json.GSON.fromJson(
                    jsonResult, DistUpgradeSlsResult.class);
            StateApplyResult<RetOpt<Map<String, Change<String>>>> spmig =
                    distUpgradeSlsResult.getSpmigration();
            String message = spmig.getComment();
            if (spmig.isResult()) {
                message = spmig.getChanges().getRetOpt().map(ret -> ret.entrySet().stream().map(entry -> {
                    StringBuilder sb = new StringBuilder();
                    sb.append(entry.getKey());
                    sb.append(":");
                    sb.append(entry.getValue().getOldValue());
                    sb.append("->");
                    sb.append(entry.getValue().getNewValue());
                    return sb.toString();
                }).collect(Collectors.joining(","))).orElse(spmig.getComment());
            }
            return message;
        }
        catch (JsonSyntaxException e) {
            try {
                DistUpgradeOldSlsResult distUpgradeSlsResult = Json.GSON.fromJson(
                        jsonResult, DistUpgradeOldSlsResult.class);
                return distUpgradeSlsResult.getSpmigration().getChanges()
                        .getRetOpt().map(ret -> {
                            if (ret.isResult()) {
                                return ret.getChanges().entrySet().stream()
                                        .map(entry -> entry.getKey() + ":" + entry.getValue().getOldValue() + "->" +
                                                entry.getValue().getNewValue()).collect(Collectors.joining(","));
                            }
                            else {
                                return ret.getComment();
                            }
                        }).orElse("");
            }
            catch (JsonSyntaxException ex) {
                LOG.error("Unable to parse migration result", ex);
            }
        }
        return "Unable to parse migration result";
    }

    private String parseDryRunMessage(JsonElement jsonResult) {
        try {
            DistUpgradeDryRunSlsResult distUpgradeSlsResult = Json.GSON.fromJson(
                    jsonResult, DistUpgradeDryRunSlsResult.class);
            return distUpgradeSlsResult.getSpmigration()
                    .getChanges().getRetOpt()
                    .orElse("") + " " + distUpgradeSlsResult
                    .getSpmigration().getComment();
        }
        catch (JsonSyntaxException e) {
            try {
                DistUpgradeOldSlsResult distUpgradeSlsResult = Json.GSON.fromJson(
                        jsonResult, DistUpgradeOldSlsResult.class);
                return distUpgradeSlsResult.getSpmigration()
                        .getChanges().getRetOpt().map(ModuleRun::getComment)
                        .orElse("") + " " + distUpgradeSlsResult
                        .getSpmigration().getComment();
            }
            catch (JsonSyntaxException ex) {
                LOG.error("Unable to parse dry run result", ex);
            }
        }
        return "Unable to parse dry run result";
    }

    /**
     * Set the results based on the result from SALT
     * @param jsonResult response from SALT master
     * @param action main action
     */
    private void handleFilesDiff(JsonElement jsonResult, Action action) {
        TypeToken<Map<String, FilesDiffResult>> typeToken = new TypeToken<>() {
        };
        Map<String, FilesDiffResult> results = Json.GSON.fromJson(jsonResult, typeToken.getType());
        Map<String, FilesDiffResult> diffResults = new HashMap<>();
        // We are only interested in results where files are different/new.
        results.values().stream().filter(fdr -> !fdr.isResult())
                .forEach(fdr -> diffResults.put(
                        fdr.getName()
                                .flatMap(x -> x.fold(arr -> Arrays.stream(arr).findFirst(), Optional::of))
                                .orElse(null),
                        fdr));

        ConfigVerifyAction configAction = (ConfigVerifyAction) action;
        configAction.getConfigRevisionActions().forEach(cra -> {
            ConfigRevision cr = cra.getConfigRevision();
            String fileName = cr.getConfigFile().getConfigFileName().getPath();
            FilesDiffResult mapFileResult = diffResults.get(fileName);
            boolean isNew = false;
            if (mapFileResult != null) {
                if (cr.isFile()) {
                    FileResult filePchanges = mapFileResult.getPChanges(FileResult.class);
                    isNew = filePchanges.getNewfile().isPresent();
                }
                else if (cr.isSymlink()) {
                    SymLinkResult symLinkPchanges = mapFileResult.getPChanges(SymLinkResult.class);
                    isNew = symLinkPchanges.getNewSymlink().isPresent();
                }
                else if (cr.isDirectory()) {
                    TypeToken<Map<String, DirectoryResult>> typeTokenD =
                            new TypeToken<>() {
                            };
                    DirectoryResult dirPchanges = mapFileResult.getPChanges(typeTokenD).get(fileName);
                    isNew = dirPchanges.getDirectory().isPresent();
                }
                if (isNew) {
                    cra.setFailureId(1L); // 1 is for missing file(Client does not have this file yet)
                }
                else {
                    ConfigRevisionActionResult cresult = new ConfigRevisionActionResult();
                    cresult.setConfigRevisionAction(cra);
                    String result = StringEscapeUtils
                            .unescapeJava(YamlHelper.INSTANCE.dump(mapFileResult.getPChanges()));
                    cresult.setResult(result.getBytes());
                    cresult.setCreated(new Date());
                    cresult.setModified(new Date());
                    cra.setConfigRevisionActionResult(cresult);
                    SystemManager.updateSystemOverview(cra.getServer());
                }
            }
        });
    }

    private void handleScapXccdfEval(ServerAction serverAction,
                                     JsonElement jsonResult, Action action) {
        ScapAction scapAction = (ScapAction)action;
        Openscap.OpenscapResult openscapResult;
        try {
            TypeToken<Map<String, StateApplyResult<Ret<Openscap.OpenscapResult>>>> typeToken =
                    new TypeToken<>() {
                    };
            Map<String, StateApplyResult<Ret<Openscap.OpenscapResult>>> stateResult = Json.GSON.fromJson(
                    jsonResult, typeToken.getType());
            openscapResult = stateResult.entrySet().stream().findFirst().map(e -> e.getValue().getChanges().getRet())
                    .orElseThrow(() -> new RuntimeException("missing scap result"));
        }
        catch (JsonSyntaxException e) {
            serverAction.setResultMsg("Error parsing minion response: " + jsonResult);
            serverAction.setStatus(ActionFactory.STATUS_FAILED);
            return;
        }
        if (openscapResult.isSuccess()) {
            serverAction.getServer().asMinionServer().ifPresent(
                    minion -> {
                        try {
                            Map<Boolean, String> moveRes = saltApi.storeMinionScapFiles(
                                    minion, openscapResult.getUploadDir(), action.getId());
                            moveRes.entrySet().stream().findFirst().ifPresent(moved -> {
                                if (moved.getKey()) {
                                    Path resultsFile = Paths.get(moved.getValue(),
                                            "results.xml");
                                    try (InputStream resultsFileIn =
                                                 new FileInputStream(
                                                         resultsFile.toFile())) {
                                        ScapManager.xccdfEval(
                                                minion, scapAction,
                                                openscapResult.getReturnCode(),
                                                openscapResult.getError(),
                                                resultsFileIn,
                                                new File(xccdfResumeXsl));
                                        serverAction.setResultMsg("Success");
                                    }
                                    catch (Exception e) {
                                        LOG.error("Error processing SCAP results file {}", resultsFile, e);
                                        serverAction.setStatus(ActionFactory.STATUS_FAILED);
                                        serverAction.setResultMsg(
                                                "Error processing SCAP results file " +
                                                        resultsFile + ": " +
                                                        e.getMessage());
                                    }
                                }
                                else {
                                    serverAction.setStatus(ActionFactory.STATUS_FAILED);
                                    serverAction.setResultMsg(
                                            "Could not store SCAP files on server: " +
                                                    moved.getValue());
                                }
                            });
                        }
                        catch (Exception e) {
                            serverAction.setStatus(ActionFactory.STATUS_FAILED);
                            serverAction.setResultMsg(
                                    "Error saving SCAP result: " + e.getMessage());
                        }
                    });
        }
        else {
            serverAction.setResultMsg(openscapResult.getError());
            serverAction.setStatus(ActionFactory.STATUS_FAILED);
        }
    }

    private void handleImageBuildLog(ImageInfo info, Action action) {
        MinionServer buildHost = info.getBuildServer();
        if (buildHost == null) {
            return;
        }

        Path srcPath = Path.of(SALT_CP_PUSH_ROOT_PATH + buildHost.getMinionId() +
                            "/files/image-build" + action.getId() + ".log");
        Path tmpPath = Path.of(SALT_FILE_GENERATION_TEMP_PATH + "/image-build" + action.getId() + ".log");

        try {
            // copy the log to a directory readable by tomcat
            saltApi.copyFile(srcPath, tmpPath)
                        .orElseThrow(() -> new RuntimeException("Can't copy the build log file"));

            String log = Files.readString(tmpPath);
            info.setBuildLog(log);
            saltApi.removeFile(srcPath);
            saltApi.removeFile(tmpPath);
        }
        catch (Exception e) {
            LOG.info("No build log for action {} {}", action.getId(), e);
        }
    }

    private void handleImageBuildData(ServerAction serverAction, JsonElement jsonResult) {
        Action action = serverAction.getParentAction();
        ImageBuildAction ba = (ImageBuildAction) action;
        ImageBuildActionDetails details = ba.getDetails();

        serverAction.setResultMsg(getJsonResultWithPrettyPrint(jsonResult));

        Optional<ImageInfo> infoOpt = ImageInfoFactory.lookupByBuildAction(ba);
        if (infoOpt.isEmpty()) {
            LOG.error("ImageInfo not found while performing: {} in handleImageBuildData", action.getName());
            return;
        }
        ImageInfo info = infoOpt.get();

        handleImageBuildLog(info, action);

        if (serverAction.getStatus().equals(ActionFactory.STATUS_COMPLETED)) {
            if (details == null) {
                LOG.error("Details not found while performing: {} in handleImageBuildData", action.getName());
                return;
            }
            Long imageProfileId = details.getImageProfileId();
            if (imageProfileId == null) { // It happens when the image profile is deleted during a build action
                LOG.error("Image Profile ID not found while performing: {} in handleImageBuildData", action.getName());
                return;
            }

            boolean isKiwiProfile = false;
            Optional<ImageProfile> profileOpt = ImageProfileFactory.lookupById(imageProfileId);
            if (profileOpt.isPresent()) {
                isKiwiProfile = profileOpt.get().asKiwiProfile().isPresent();
            }
            else {
                LOG.warn("Could not find any profile for profile ID {}", imageProfileId);
            }

            if (isKiwiProfile) {
                serverAction.getServer().asMinionServer().ifPresent(minionServer -> {
                    // Update the image info and download the built Kiwi image to SUSE Manager server
                    OSImageBuildImageInfoResult buildInfo =
                            Json.GSON.fromJson(jsonResult, OSImageBuildSlsResult.class)
                                    .getKiwiBuildInfo().getChanges().getRet();

                    info.setChecksum(ImageInfoFactory.convertChecksum(buildInfo.getImage().getChecksum()));
                    info.setName(buildInfo.getImage().getName());
                    info.setVersion(buildInfo.getImage().getVersion());

                    ImageInfoFactory.updateRevision(info);

                    List<List<Object>> files = new ArrayList<>();
                    String imageDir = info.getName() + "-" + info.getVersion() + "-" + info.getRevisionNumber() + "/";
                    if (!buildInfo.getBundles().isEmpty()) {
                        buildInfo.getBundles().forEach(bundle -> {
                            files.add(List.of(bundle.getFilepath(),
                                    imageDir + bundle.getFilename(), "bundle", bundle.getChecksum()));
                        });
                    }
                    else {
                        files.add(List.of(buildInfo.getImage().getFilepath(),
                                imageDir + buildInfo.getImage().getFilename(), "image",
                                buildInfo.getImage().getChecksum()));
                        buildInfo.getBootImage().ifPresent(f -> {
                            files.add(List.of(f.getKernel().getFilepath(),
                                    imageDir + f.getKernel().getFilename(), "kernel",
                                    f.getKernel().getChecksum()));
                            files.add(List.of(f.getInitrd().getFilepath(),
                                    imageDir + f.getInitrd().getFilename(), "initrd",
                                    f.getInitrd().getChecksum()));
                        });
                    }
                    files.stream().forEach(file -> {
                        String targetPath = OSImageStoreUtils.getOSImageStorePathForImage(info);
                        targetPath += info.getName() + "-" + info.getVersion() + "-" + info.getRevisionNumber() + "/";
                        MgrUtilRunner.ExecResult collectResult = systemQuery
                                .collectKiwiImage(minionServer, (String)file.get(0), targetPath)
                                .orElseThrow(() -> new RuntimeException("Failed to download image."));

                        if (collectResult.getReturnCode() != 0) {
                            serverAction.setStatus(ActionFactory.STATUS_FAILED);
                            serverAction.setResultMsg(StringUtils
                                    .left(printStdMessages(collectResult.getStderr(), collectResult.getStdout()),
                                            1024));
                        }
                        else {
                            ImageFile imagefile = new ImageFile();
                            imagefile.setFile((String)file.get(1));
                            imagefile.setType((String)file.get(2));
                            imagefile.setChecksum(ImageInfoFactory.convertChecksum(
                                    (ImageChecksum.Checksum)file.get(3)));
                            imagefile.setImageInfo(info);
                            info.getImageFiles().add(imagefile);
                        }
                    });
                });
            }
            else {
                ImageInfoFactory.updateRevision(info);
                if (info.getImageType().equals(ImageProfile.TYPE_DOCKERFILE)) {
                    ImageInfoFactory.obsoletePreviousRevisions(info);
                }
            }
        }
        if (serverAction.getStatus().equals(ActionFactory.STATUS_COMPLETED)) {
            // both building and uploading results succeeded
            info.setBuilt(true);

            try {
                ImageInfoFactory.scheduleInspect(info, Date.from(Instant.now()), action.getSchedulerUser());
            }
            catch (TaskomaticApiException e) {
                LOG.error("Could not schedule image inspection ", e);
            }
        }
        ImageInfoFactory.save(info);
    }

    private void handleImageInspectData(ServerAction serverAction,
            JsonElement jsonResult) {
        Action action = serverAction.getParentAction();
        ImageInspectAction ia = (ImageInspectAction) action;
        ImageInspectActionDetails details = ia.getDetails();
        if (details == null) {
            LOG.warn("Details not found while performing: {} in handleImageInspectData", action.getName());
            return;
        }
        Long imageStoreId = details.getImageStoreId();
        if (imageStoreId == null) { // It happens when the store is deleted during an inspect action
            LOG.warn("Image Store ID not found while performing: {} in handleImageInspectData", action.getName());
            return;
        }
        ImageInfoFactory
                .lookupByInspectAction(ia)
                .ifPresent(imageInfo -> serverAction.getServer().asMinionServer()
                        .ifPresent(minionServer ->
                                handleImagePackageProfileUpdate(imageInfo, Json.GSON.fromJson(jsonResult,
                                                ImagesProfileUpdateSlsResult.class),
                                        serverAction)));
    }

    /**
     * Check if an action is failed based on the return event data. The status depends on
     * the "success" and "retcode" attributes as well as on the single states results in
     * case we are looking at the results of a state.apply.
     *
     * @return true if the action has failed, false otherwise
     */
    private static boolean actionFailed(Optional<Xor<String[], String>> function, JsonElement rawResult,
            boolean success, long retcode) {
        // For state.apply based actions verify the result of each state
        if (function.map(x -> x.fold(Arrays::asList, List::of).contains("state.apply")).orElse(false)) {
            return Opt.fold(
                SaltUtils.jsonEventToStateApplyResults(rawResult),
                () -> true,
                results -> results.values().stream().filter(
                    result -> !result.isResult()).findAny().isPresent());
        }
        return !(success && retcode == 0);
    }

    /**
     * Converts the json representation of an event to a map
     *
     * @param jsonResult json representation of an event
     */
    private static Optional<Map<String, StateApplyResult<Map<String, Object>>>>
    jsonEventToStateApplyResults(JsonElement jsonResult) {
        TypeToken<Map<String, StateApplyResult<Map<String, Object>>>> typeToken =
                new TypeToken<>() {
                };
        Optional<Map<String, StateApplyResult<Map<String, Object>>>> results;
        results = Optional.empty();
        try {
             results = Optional.ofNullable(
                Json.GSON.fromJson(jsonResult, typeToken.getType()));
        }
        catch (JsonSyntaxException e) {
            LOG.error("JSON syntax error while decoding into a StateApplyResult:");
            LOG.error(jsonResult.toString());
        }
        return results;
    }

    private void handleImagePackageProfileUpdate(ImageInfo imageInfo,
            ImagesProfileUpdateSlsResult result, ServerAction serverAction) {
        ActionStatus as = ActionFactory.STATUS_COMPLETED;
        serverAction.setResultMsg("Success");
        if (Optional.ofNullable(imageInfo.getProfile()).isEmpty() ||
                imageInfo.getProfile().asDockerfileProfile().isPresent()) {
            if (result.getDockerInspect().isResult()) {
                ImageInspectSlsResult iret = result.getDockerInspect().getChanges().getRet();
                imageInfo.setChecksum(ImageInfoFactory.convertChecksum(iret.getId()));
                imageInfo.getRepoDigests().addAll(
                        iret.getRepoDigests().stream().map(digest -> {
                            ImageRepoDigest repoDigest = new ImageRepoDigest();
                            repoDigest.setRepoDigest(digest);
                            repoDigest.setImageInfo(imageInfo);
                            return repoDigest;
                        }).collect(Collectors.toSet()));
            }
            else {
                serverAction.setResultMsg(result.getDockerInspect().getComment());
                as = ActionFactory.STATUS_FAILED;
            }

            if (result.getDockerSlsBuild().isResult()) {
                PkgProfileUpdateSlsResult ret =
                        result.getDockerSlsBuild().getChanges().getRet();

                Optional.of(ret.getInfoInstalled().getChanges().getRet())
                        .map(saltPkgs -> saltPkgs.entrySet().stream()
                                .flatMap(entry -> Opt.stream(entry.getValue().left())
                                        .map(info -> createImagePackageFromSalt(entry.getKey(), info, imageInfo)))
                                .collect(Collectors.toSet()));

                Optional.of(ret.getInfoInstalled().getChanges().getRet())
                        .map(saltPkgs -> saltPkgs.entrySet().stream()
                                .flatMap(entry -> Opt.stream(entry.getValue().right())
                                        .flatMap(Collection::stream)
                                        .map(info -> createImagePackageFromSalt(entry.getKey(), info, imageInfo)))
                                .collect(Collectors.toSet()));

                Optional.ofNullable(ret.getListProducts())
                        .map(products -> products.getChanges().getRet())
                        .map(SaltUtils::getInstalledProducts)
                        .ifPresent(imageInfo::setInstalledProducts);

                Optional<String> rhelReleaseFile =
                        Optional.ofNullable(ret.getRhelReleaseFile())
                                .map(StateApplyResult::getChanges)
                                .filter(res -> res.getStdout() != null)
                                .map(CmdResult::getStdout);
                Optional<String> centosReleaseFile =
                        Optional.ofNullable(ret.getCentosReleaseFile())
                                .map(StateApplyResult::getChanges)
                                .filter(res -> res.getStdout() != null)
                                .map(CmdResult::getStdout);
                Optional<String> alibabaReleaseFile =
                        Optional.ofNullable(ret.getAlibabaReleaseFile())
                                .map(StateApplyResult::getChanges)
                                .filter(res -> res.getStdout() != null)
                                .map(CmdResult::getStdout);
                Optional<String> oracleReleaseFile =
                        Optional.ofNullable(ret.getOracleReleaseFile())
                                .map(StateApplyResult::getChanges)
                                .filter(res -> res.getStdout() != null)
                                .map(CmdResult::getStdout);
                Optional<String> almaReleaseFile =
                        Optional.ofNullable(ret.getAlmaReleaseFile())
                                .map(StateApplyResult::getChanges)
                                .filter(res -> res.getStdout() != null)
                                .map(CmdResult::getStdout);
                Optional<String> amazonReleaseFile =
                        Optional.ofNullable(ret.getAmazonReleaseFile())
                                .map(StateApplyResult::getChanges)
                                .filter(res -> res.getStdout() != null)
                                .map(CmdResult::getStdout);
                Optional<String> rockyReleaseFile =
                        Optional.ofNullable(ret.getRockyReleaseFile())
                                .map(StateApplyResult::getChanges)
                                .filter(res -> res.getStdout() != null)
                                .map(CmdResult::getStdout);
                Optional<String> resReleasePkg =
                        Optional.ofNullable(ret.getWhatProvidesResReleasePkg())
                                .map(StateApplyResult::getChanges)
                                .filter(res -> res.getStdout() != null)
                                .map(CmdResult::getStdout);
                Optional<String> sllReleasePkg =
                        Optional.ofNullable(ret.getWhatProvidesSLLReleasePkg())
                                .map(StateApplyResult::getChanges)
                                .filter(res -> res.getStdout() != null)
                                .map(CmdResult::getStdout);
                if (rhelReleaseFile.isPresent() || centosReleaseFile.isPresent() ||
                        oracleReleaseFile.isPresent() || alibabaReleaseFile.isPresent() ||
                        almaReleaseFile.isPresent() || amazonReleaseFile.isPresent() ||
                        rockyReleaseFile.isPresent() || resReleasePkg.isPresent()) {
                    Set<InstalledProduct> products = getInstalledProductsForRhel(
                            imageInfo, resReleasePkg, sllReleasePkg,
                            rhelReleaseFile, centosReleaseFile, oracleReleaseFile, alibabaReleaseFile,
                            almaReleaseFile, amazonReleaseFile, rockyReleaseFile);
                    imageInfo.setInstalledProducts(products);
                }
            }
            else {
                // do not fail the action when no packages are returned
                serverAction.setResultMsg(result.getDockerSlsBuild().getComment());
            }
        }
        else {
            if (result.getKiwiInspect().isResult()) {
                Long instantNow = new Date().getTime() / 1000L;
                OSImageInspectSlsResult ret = result.getKiwiInspect().getChanges().getRet();
                List<OSImageInspectSlsResult.Package> packages = ret.getPackages();
                packages.forEach(pkg -> createImagePackageFromSalt(pkg.getName(), Optional.of(pkg.getEpoch()),
                        Optional.of(pkg.getRelease()), pkg.getVersion(), Optional.of(instantNow),
                        Optional.of(pkg.getArch()), imageInfo));
                if ("pxe".equals(ret.getImage().getType())) {
                    SaltStateGeneratorService.INSTANCE.generateOSImagePillar(ret.getImage(),
                            ret.getBootImage(), imageInfo);
                    if (ret.getBootImage().isPresent() && ret.getBundles().isEmpty()) {
                        SaltbootUtils.createSaltbootDistro(imageInfo, ret.getBootImage().get());
                    }
                }
            }
            else {
                serverAction.setResultMsg(result.getKiwiInspect().getComment());
                as = ActionFactory.STATUS_FAILED;
            }
        }

        serverAction.setStatus(as);
        if (as.equals(ActionFactory.STATUS_COMPLETED)) {
            imageInfo.setBuilt(true);
        }
        ImageInfoFactory.save(imageInfo);
        ErrataManager.insertErrataCacheTask(imageInfo);
    }

    /**
     * Perform the actual update of the database based on given event data.
     *
     * @param server the minion server
     * @param result the result of the call as parsed from event data
     */
    private void handlePackageProfileUpdate(MinionServer server,
            PkgProfileUpdateSlsResult result) {
        Instant start = Instant.now();

        HibernateFactory.doWithoutAutoFlushing(() -> updatePackages(server, result));

        Optional.ofNullable(result.getListProducts())
                .map(products -> products.getChanges().getRet())
                .map(SaltUtils::getInstalledProducts)
                .ifPresent(server::setInstalledProducts);

        Optional<String> rhelReleaseFile =
                Optional.ofNullable(result.getRhelReleaseFile())
                .map(StateApplyResult::getChanges)
                .filter(ret -> ret.getStdout() != null)
                .map(CmdResult::getStdout);
        Optional<String> centosReleaseFile =
                Optional.ofNullable(result.getCentosReleaseFile())
                .map(StateApplyResult::getChanges)
                .filter(ret -> ret.getStdout() != null)
                .map(CmdResult::getStdout);
        Optional<String> oracleReleaseFile =
                Optional.ofNullable(result.getOracleReleaseFile())
                .map(StateApplyResult::getChanges)
                .filter(ret -> ret.getStdout() != null)
                .map(CmdResult::getStdout);
        Optional<String> alibabaReleaseFile =
                Optional.ofNullable(result.getAlibabaReleaseFile())
                .map(StateApplyResult::getChanges)
                .filter(ret -> ret.getStdout() != null)
                .map(CmdResult::getStdout);
        Optional<String> almaReleaseFile =
                Optional.ofNullable(result.getAlmaReleaseFile())
                .map(StateApplyResult::getChanges)
                .filter(ret -> ret.getStdout() != null)
                .map(CmdResult::getStdout);
        Optional<String> amazonReleaseFile =
                Optional.ofNullable(result.getAmazonReleaseFile())
                .map(StateApplyResult::getChanges)
                .filter(ret -> ret.getStdout() != null)
                .map(CmdResult::getStdout);
        Optional<String> rockyReleaseFile =
                Optional.ofNullable(result.getRockyReleaseFile())
                .map(StateApplyResult::getChanges)
                .filter(ret -> ret.getStdout() != null)
                .map(CmdResult::getStdout);
        Optional<String> resReleasePkg =
                Optional.ofNullable(result.getWhatProvidesResReleasePkg())
                .map(StateApplyResult::getChanges)
                .filter(ret -> ret.getStdout() != null)
                .map(CmdResult::getStdout);
        Optional<String> sllReleasePkg =
                Optional.ofNullable(result.getWhatProvidesSLLReleasePkg())
                .map(StateApplyResult::getChanges)
                .filter(ret -> ret.getStdout() != null)
                .map(CmdResult::getStdout);

        ValueMap grains = new ValueMap(result.getGrains());

        if (rhelReleaseFile.isPresent() || centosReleaseFile.isPresent() ||
                oracleReleaseFile.isPresent() || alibabaReleaseFile.isPresent() ||
                almaReleaseFile.isPresent() || amazonReleaseFile.isPresent() ||
                rockyReleaseFile.isPresent() || resReleasePkg.isPresent()) {
            Set<InstalledProduct> products = getInstalledProductsForRhel(
                    server, resReleasePkg, sllReleasePkg,
                    rhelReleaseFile, centosReleaseFile, oracleReleaseFile, alibabaReleaseFile,
                    almaReleaseFile, amazonReleaseFile, rockyReleaseFile);
            server.setInstalledProducts(products);
        }
        else if ("ubuntu".equalsIgnoreCase(grains.getValueAsString("os"))) {
            String osArch = grains.getValueAsString("osarch") + "-deb";
            String osVersion = grains.getValueAsString("osrelease");
            // Check if we have a product for the specific arch and version
            SUSEProduct ubuntuProduct = SUSEProductFactory.findSUSEProduct("ubuntu-client", osVersion, null, osArch,
                    false);
            if (ubuntuProduct != null) {
                InstalledProduct installedProduct = SUSEProductFactory.findInstalledProduct(ubuntuProduct)
                        .orElse(new InstalledProduct(ubuntuProduct));
                server.setInstalledProducts(Collections.singleton(installedProduct));
            }
        }
        else if ("debian".equalsIgnoreCase(grains.getValueAsString("os"))) {
            String osArch = grains.getValueAsString("osarch") + "-deb";
            String osVersion = grains.getValueAsString("osmajorrelease");
            // Check if we have a product for the specific arch and version
            SUSEProduct debianProduct = SUSEProductFactory.findSUSEProduct("debian-client", osVersion, null, osArch,
                    false);
            if (debianProduct != null) {
                InstalledProduct installedProduct = SUSEProductFactory.findInstalledProduct(debianProduct)
                        .orElse(new InstalledProduct(debianProduct));
                server.setInstalledProducts(Collections.singleton(installedProduct));
            }
        }

        // Update last boot time
        handleUptimeUpdate(server, result.getUpTime()
                .map(ut -> (Number)ut.getChanges().getRet().get("seconds"))
                .map(n -> n.longValue())
                .orElse(null));

        // Update live patching version
        server.setKernelLiveVersion(result.getKernelLiveVersionInfo()
                .map(klv -> klv.getChanges().getRet()).filter(Objects::nonNull)
                .map(KernelLiveVersionInfo::getKernelLiveVersion).orElse(null));

        // Update grains
        if (!result.getGrains().isEmpty()) {
            server.setOsFamily(grains.getValueAsString("os_family"));
            server.setRunningKernel(grains.getValueAsString("kernelrelease"));
            server.setOs(grains.getValueAsString("osfullname"));
            server.setCpe(grains.getValueAsString("cpe"));

            /** Release is set directly from grain information for SUSE systems only.
                RH systems require some parsing on the grains to get the correct release
                See RegisterMinionEventMessageAction#getOsRelease

                However, release can change only after product migration and SUMA supports this only on SUSE systems.
                Also, the getOsRelease method requires remote command execution and was therefore avoided for now.
                If we decide to support RedHat distro/SP upgrades in the future, this code has to be reviewed.
             */
            if (server.getOsFamily().equals(OS_FAMILY_SUSE)) {
                server.setRelease(grains.getValueAsString("osrelease"));
            }
            SystemManager.updateMgrServerInfo(server, grains);
        }

        ServerFactory.save(server);
        if (LOG.isDebugEnabled()) {
            long duration = Duration.between(start, Instant.now()).getSeconds();
            LOG.debug("Package profile updated for minion: {} ({} seconds)", server.getMinionId(), duration);
        }

        // Trigger update of errata cache for this server
        ErrataManager.insertErrataCacheTask(server);
    }

    /**
     * Updates a minion's packages with the result coming from Salt
     *
     * @param server a Server object corresponding to a minion
     * @param result the result from the package profile update state
     */
    private static void updatePackages(MinionServer server,
            PkgProfileUpdateSlsResult result) {
        Set<InstalledPackage> packages = server.getPackages();

        Map<String, InstalledPackage> oldPackageMap = packages.stream()
            .collect(Collectors.toMap(
                    SaltUtils::packageToKey,
                    Function.identity()
             ));

        Map<String, Map.Entry<String, Pkg.Info>> newPackageMap =
            result.getInfoInstalled().getChanges().getRet()
                .entrySet().stream()
                .flatMap(entry ->
                   entry.getValue().fold(Stream::of, Collection::stream)
                        .flatMap(x -> {
                           Map<String, Info> infoTuple = new HashMap<>();
                           infoTuple.put(entry.getKey(), x);
                           return infoTuple.entrySet().stream();
                        })
                )
                .collect(Collectors.toMap(
                        SaltUtils::packageToKey,
                        Function.identity(),
                        SaltUtils::resolveDuplicatePackage
                ));

        Collection<InstalledPackage> unchanged = oldPackageMap.entrySet().stream().filter(
            e -> newPackageMap.containsKey(e.getKey())
        ).map(Map.Entry::getValue).collect(Collectors.toList());
        packages.retainAll(unchanged);

        Map<String, Tuple2<String, Pkg.Info>> packagesToAdd = newPackageMap.entrySet().stream().filter(
                e -> !oldPackageMap.containsKey(e.getKey())
        ).collect(Collectors.toMap(Map.Entry::getKey, e -> new Tuple2(e.getValue().getKey(), e.getValue().getValue())));

        packages.addAll(createPackagesFromSalt(packagesToAdd, server));
        SystemManager.updateSystemOverview(server.getId());
    }

    private static Map.Entry<String, Info> resolveDuplicatePackage(Map.Entry<String, Info> firstEntry,
            Map.Entry<String, Info> secondEntry) {
        Info first = firstEntry.getValue();
        Info second = secondEntry.getValue();

        if (first.getInstallDateUnixTime().isEmpty() && second.getInstallDateUnixTime().isEmpty()) {
            LOG.warn("Got duplicate packages NEVRA and the install timestamp is missing." +
                    " Taking the first one. First:  {}, second: {}", first, second);
            return firstEntry;
        }

        // the later one wins
        if (first.getInstallDateUnixTime().get() > second.getInstallDateUnixTime().get()) {
            return firstEntry;
        }
        else {
            return secondEntry;
        }
    }

    /**
     * Create a list of {@link InstalledPackage} for a  {@link Server} given the package names and package information.
     *
     * @param packageInfoAndNameBySaltPackageKey a map that contains a package name and a package info, by the package
     * key produced by salt
     * @param server server this package will be added to
     * @return a list of {@link InstalledPackage}
     */
    private static List<InstalledPackage> createPackagesFromSalt(
            Map<String, Tuple2<String, Pkg.Info>> packageInfoAndNameBySaltPackageKey, Server server) {
        List<String> names = new ArrayList<>(packageInfoAndNameBySaltPackageKey.values().stream().map(Tuple2::getA)
                .collect(Collectors.toSet()));
        Collections.sort(names);

        Map<String, PackageName> packageNames = names.stream().collect(Collectors.toMap(Function.identity(),
                PackageFactory::lookupOrCreatePackageByName));


        Map<String, PackageEvr> packageEvrsBySaltPackageKey = packageInfoAndNameBySaltPackageKey.entrySet().stream()
                .collect(Collectors.toMap(Map.Entry::getKey,
                        e -> {
                            Pkg.Info pkgInfo = e.getValue().getB();
                            return parsePackageEvr(pkgInfo.getEpoch(), pkgInfo.getVersion().get(),
                                    pkgInfo.getRelease(), server.getPackageType());
                        }));

        return packageInfoAndNameBySaltPackageKey.entrySet().stream().map(e -> createInstalledPackage(
                packageNames.get(e.getValue().getA()),
                packageEvrsBySaltPackageKey.get(e.getKey()), e.getValue().getB(), server))
                .collect(Collectors.toList());
    }

    /**
     * Create a {@link InstalledPackage} object from package name, evr, package info and server and return it.
     *
     * @param packageName the package name
     * @param packageEvr the package evr
     * @param pkgInfo the package info
     * @param server server this package will be added to
     * @return the InstalledPackage object
     */
    private static InstalledPackage createInstalledPackage(PackageName packageName,
                                                           PackageEvr packageEvr,
                                                           Pkg.Info pkgInfo, Server server) {
        InstalledPackage pkg = new InstalledPackage();
        pkg.setEvr(packageEvr);
        pkg.setInstallTime(pkgInfo.getInstallDateUnixTime()
                .map(time -> new Date((time * 1000)))
                .orElse(null));
        pkg.setName(packageName);
        pkg.setServer(server);

        // Add -deb suffix to architectures for Debian systems
        String pkgArch = pkgInfo.getArchitecture().get();
        if (server.getPackageType() == PackageType.DEB) {
            pkgArch += "-deb";
        }
        pkg.setArch(PackageFactory.lookupPackageArchByLabel(pkgArch));
        return pkg;
    }

    /**
     * Returns a key string that uniquely identifies an installed package (as a
     * Hibernated object)
     *
     * @param p the package
     * @return the key
     */
    public static String packageToKey(InstalledPackage p) {

        // name and EVR are never null due to DB constraints
        // see schema/spacewalk/common/tables/rhnServerPackage.sql

        String sb = p.getName().getName() +
                "-" +
                p.getEvr().toUniversalEvrString() +
                "." +
                Optional.ofNullable(p.getArch()).map(PackageArch::toUniversalArchString).orElse("unknown");
        return sb;
    }

    /**
     * Returns a key string that uniquely identifies an installed package (as
     * returned by Salt)
     *
     * @param name the package name
     * @param info the package info
     * @return the key
     */
    public static String packageToKey(String name, Pkg.Info info) {

        StringBuilder sb = new StringBuilder();

        sb.append(name);
        sb.append("-");
        sb.append(
                new PackageEvr(
                        info.getEpoch().orElse(null),
                        info.getVersion().get(),
                        info.getRelease().orElse("X"),
                        PackageType.RPM
                ).toUniversalEvrString()
        );
        sb.append(".");
        sb.append(info.getArchitecture().get());

        return sb.toString();
    }

    /**
     * Returns a key string that uniquely identifies an installed package (as
     * returned by Salt)
     *
     * @param entry the package
     * @return the key
     */
    private static String packageToKey(Map.Entry<String, Pkg.Info> entry) {
        return packageToKey(entry.getKey(), entry.getValue());
    }

    /**
     * Update the hardware profile for a minion in the database from incoming
     * event data.
     *
     * @param server the minion server
     * @param result the result of the call as parsed from event data
     * @param serverAction the server action
     */
    private static void handleHardwareProfileUpdate(MinionServer server,
            HwProfileUpdateSlsResult result, ServerAction serverAction) {
        Instant start = Instant.now();

        HardwareMapper hwMapper = new HardwareMapper(server,
                new ValueMap(result.getGrains()));
        hwMapper.mapCpuInfo(new ValueMap(result.getCpuInfo()));
        server.setRam(hwMapper.getTotalMemory());
        server.setSwap(hwMapper.getTotalSwapMemory());
        if (CpuArchUtil.isDmiCapable(hwMapper.getCpuArch())) {
            hwMapper.mapDmiInfo(
                    result.getSmbiosRecordsBios().orElse(Collections.emptyMap()),
                    result.getSmbiosRecordsSystem().orElse(Collections.emptyMap()),
                    result.getSmbiosRecordsBaseboard().orElse(Collections.emptyMap()),
                    result.getSmbiosRecordsChassis().orElse(Collections.emptyMap()));
        }
        hwMapper.mapDevices(result.getUdevdb());
        if (CpuArchUtil.isS390(hwMapper.getCpuArch())) {
            hwMapper.mapSysinfo(result.getMainframeSysinfo());
        }
        hwMapper.mapVirtualizationInfo(result.getSmbiosRecordsSystem());
        hwMapper.mapNetworkInfo(result.getNetworkInterfaces(), Optional.of(result.getNetworkIPs()),
                result.getNetworkModules(),
                Stream.concat(
                    Stream.concat(
                        result.getFqdns().stream(),
                        result.getDnsFqdns().stream()
                    ),
                    result.getCustomFqdns().stream()
                ).distinct().collect(Collectors.toList())
        );
        hwMapper.mapPaygInfo();

        // Let the action fail in case there is error messages
        if (!hwMapper.getErrors().isEmpty()) {
            serverAction.setStatus(ActionFactory.STATUS_FAILED);
            serverAction.setResultMsg("Hardware list could not be refreshed completely:\n" +
                    hwMapper.getErrors().stream().collect(Collectors.joining("\n")));
            serverAction.setResultCode(-1L);
        }

        if (LOG.isDebugEnabled()) {
            long duration = Duration.between(start, Instant.now()).getSeconds();
            LOG.debug("Hardware profile updated for minion: {} ({} seconds)", server.getMinionId(), duration);
        }
    }

    private static PackageEvr parsePackageEvr(Optional<String> epoch, String version, Optional<String> release,
                                              PackageType type) {
        switch (type) {
            case DEB:
                return PackageEvrFactory.lookupOrCreatePackageEvr(PackageEvr.parseDebian(version));
            case RPM:
                return PackageEvrFactory.lookupOrCreatePackageEvr(epoch.map(StringUtils::trimToNull).orElse(null),
                        version, release.orElse("0"), PackageType.RPM);
            default:
                throw new RuntimeException("unreachable");
        }
    }

    private static ImagePackage createImagePackageFromSalt(String name, Pkg.Info info, ImageInfo imageInfo) {
        return createImagePackageFromSalt(name, info.getEpoch(), info.getRelease(), info.getVersion().get(),
                info.getInstallDateUnixTime(), info.getArchitecture(), imageInfo);
    }

    private static ImagePackage createImagePackageFromSalt(String name, Optional<String> epoch,
            Optional<String> release, String version, Optional<Long> installDateUnixTime, Optional<String> architecture,
            ImageInfo imageInfo) {

        PackageType packageType = imageInfo.getPackageType();
        Optional<String> pkgArch = architecture.map(arch -> packageType == PackageType.DEB ? arch + "-deb" : arch);
        PackageEvr evr = parsePackageEvr(epoch, version, release, packageType);
        ImagePackage pkg = new ImagePackage();
        pkg.setEvr(evr);
        pkgArch.ifPresent(arch -> pkg.setArch(PackageFactory.lookupPackageArchByLabel(arch)));
        installDateUnixTime.ifPresent(udut -> pkg.setInstallTime(new Date(udut * 1000)));
        pkg.setName(PackageFactory.lookupOrCreatePackageByName(name));
        pkg.setImageInfo(imageInfo);
        ImageInfoFactory.save(pkg);
        return pkg;
    }

    /**
     * Convert a list of {@link ProductInfo} objects into a set of {@link InstalledProduct}
     * objects.
     *
     * @param productsIn list of products as received from Salt
     * @return set of installed products
     */
    private static Set<InstalledProduct> getInstalledProducts(
            List<ProductInfo> productsIn) {
        return productsIn.stream().flatMap(saltProduct -> {
            String name = saltProduct.getName();
            String version = saltProduct.getVersion();
            String release = saltProduct.getRelease();
            String arch = saltProduct.getArch();
            boolean isbase = saltProduct.getIsbase();

            // Find the corresponding SUSEProduct in the database, if any
            Optional<SUSEProduct> suseProduct = Optional.ofNullable(SUSEProductFactory
                    .findSUSEProduct(name, version, release, arch, true));
            if (!suseProduct.isPresent()) {
                LOG.warn(String.format("No product match found for: %s %s %s %s",
                        name, version, release, arch));
            }

            return Stream.of(SUSEProductFactory
                    .findInstalledProduct(name, version, release,
                            PackageFactory.lookupPackageArchByLabel(arch), isbase)
                    .orElseGet(() -> {
                        // Use installed product information from the client
                        InstalledProduct p = new InstalledProduct(name, version,
                                PackageFactory.lookupPackageArchByLabel(arch), release,
                                isbase);
                        ServerFactory.save(p);
                        return p;
                    }));
        }).collect(Collectors.toSet());
    }

    private static Set<InstalledProduct> getInstalledProductsForRhel(
           MinionServer server,
           Optional<String> resPackage,
           Optional<String> sllPackage,
           Optional<String> rhelReleaseFile,
           Optional<String> centosRelaseFile,
           Optional<String> oracleReleaseFile,
           Optional<String> alibabaReleaseFile,
           Optional<String> almaReleaseFile,
           Optional<String> amazonReleaseFile,
           Optional<String> rockyReleaseFile) {

        Optional<RhelUtils.RhelProduct> rhelProductInfo =
                RhelUtils.detectRhelProduct(server, resPackage, sllPackage,
                        rhelReleaseFile, centosRelaseFile, oracleReleaseFile,
                        alibabaReleaseFile, almaReleaseFile, amazonReleaseFile,
                        rockyReleaseFile);

        if (!rhelProductInfo.isPresent()) {
            LOG.warn("Could not determine RHEL product type for minion: {}", server.getMinionId());
            return Collections.emptySet();
        }

        LOG.debug("Detected minion {} as a RedHat compatible system: {} {} {} {}",
                server.getMinionId(),
                rhelProductInfo.get().getName(), rhelProductInfo.get().getVersion(),
                rhelProductInfo.get().getRelease(), server.getServerArch().getName());

        return rhelProductInfo.get().getAllSuseProducts().stream().map(product -> {
            String arch = server.getServerArch().getLabel().replace("-redhat-linux", "");

            InstalledProduct installedProduct = new InstalledProduct();
            installedProduct.setName(product.getName());
            installedProduct.setVersion(product.getVersion());
            installedProduct.setRelease(product.getRelease());
            installedProduct.setArch(PackageFactory.lookupPackageArchByLabel(arch));
            installedProduct.setBaseproduct(product.isBase());

            return installedProduct;
        }).collect(Collectors.toSet());
    }

    private static Set<InstalledProduct> getInstalledProductsForRhel(
            ImageInfo image,
            Optional<String> resPackage,
            Optional<String> sllPackage,
            Optional<String> rhelReleaseFile,
            Optional<String> centosReleaseFile,
            Optional<String> oracleReleaseFile,
            Optional<String> alibabaReleaseFile,
            Optional<String> almaReleaseFile,
            Optional<String> amazonReleaseFile,
            Optional<String> rockyReleaseFile) {

         Optional<RhelUtils.RhelProduct> rhelProductInfo =
                 RhelUtils.detectRhelProduct(image, resPackage, sllPackage,
                         rhelReleaseFile, centosReleaseFile, oracleReleaseFile,
                         alibabaReleaseFile, almaReleaseFile, amazonReleaseFile,
                         rockyReleaseFile);

         if (!rhelProductInfo.isPresent()) {
             LOG.warn("Could not determine RHEL product type for image: {} {}", image.getName(), image.getVersion());
             return Collections.emptySet();
         }

         LOG.debug("Detected image {}:{} as a RedHat compatible system: {} {} {} {}",
                 image.getName(), image.getVersion(),
                 rhelProductInfo.get().getName(), rhelProductInfo.get().getVersion(),
                 rhelProductInfo.get().getRelease(), image.getImageArch().getName());

         return rhelProductInfo.get().getAllSuseProducts().stream().map(product -> {
             String arch = image.getImageArch().getLabel().replace("-redhat-linux", "");

             InstalledProduct installedProduct = new InstalledProduct();
             installedProduct.setName(product.getName());
             installedProduct.setVersion(product.getVersion());
             installedProduct.setRelease(product.getRelease());
             installedProduct.setArch(PackageFactory.lookupPackageArchByLabel(arch));
             installedProduct.setBaseproduct(true);

             return installedProduct;
         }).collect(Collectors.toSet());
     }

    /**
     * Update the system info through grains and data returned by status.uptime
     *
     * @param jsonResult response from salt master against util.systeminfo state
     * @param minion the minion for which information should be updated
     */
    public void updateSystemInfo(JsonElement jsonResult, MinionServer minion) {
        SystemInfo systemInfo = Json.GSON.fromJson(jsonResult, SystemInfo.class);
        updateSystemInfo(systemInfo, minion);
    }


    /**
     * Update the minion connection path according to master/proxy hostname
     * @param minion the minion
     * @param master master/proxy hostname
     * @return true if the path has changed
     */
    public boolean updateMinionConnectionPath(MinionServer minion, String master) {
        boolean changed = minion.updateServerPaths(master);

        if (changed) {
            ServerFactory.save(minion);

            // Regenerate the pillar data
            MinionPillarManager.INSTANCE.generatePillar(minion);

            // push the changed pillar data to the minion
            saltApi.refreshPillar(new MinionList(minion.getMinionId()));

            ApplyStatesAction action = ActionManager.scheduleApplyStates(minion.getCreator(),
                    Collections.singletonList(minion.getId()),
                    Collections.singletonList(ApplyStatesEventMessage.CHANNELS),
                    new Date());
            try {
                TASKOMATIC_API.scheduleActionExecution(action, false);
            }
            catch (TaskomaticApiException e) {
                LOG.error("Could not schedule channels state application");
                LOG.error("Could not schedule channels refresh after proxy change. Old URLs remains on minion {}",
                        minion.getMinionId());
            }

        }
        return changed;
    }

    /**
     * Update the system info of the minion and set Reboot Actions to completed
     * @param systemInfo response from salt master against util.systeminfo state
     * @param minion  minion for which information should be updated
     */
    public void updateSystemInfo(SystemInfo systemInfo, MinionServer minion) {
        systemInfo.getKerneRelese().ifPresent(minion::setRunningKernel);
        systemInfo.getKernelLiveVersion().ifPresent(minion::setKernelLiveVersion);
        ServerFactory.save(minion);

        if (!ContactMethodUtil.isSSHPushContactMethod(minion.getContactMethod())) {
            systemInfo.getMaster().ifPresent(master -> updateMinionConnectionPath(minion, master));
        }

        //Update the uptime
        systemInfo.getUptimeSeconds().ifPresent(us-> handleUptimeUpdate(minion, us.longValue()));
    }

    /**
     * Handle the minion uptime update, that means:
     * - Set the time of the last boot according to the uptimeSeconds value and
     *   current time,
     * - cleanup old reboot actions.
     *
     * @param minion the minion
     * @param uptimeSeconds uptime time in seconds
     */
    public static void handleUptimeUpdate(MinionServer minion, Long uptimeSeconds) {
        if (uptimeSeconds == null) {
            return;
        }
        Date bootTime = new Date(
                System.currentTimeMillis() - (uptimeSeconds * 1000));
        LOG.debug("Set last boot for {} to {}", minion.getMinionId(), bootTime);
        minion.setLastBoot(bootTime.getTime() / 1000);

        // cleanup old reboot actions
        List<ServerAction> serverActions = ActionFactory
                .listServerActionsForServer(minion);
        int actionsChanged = 0;
        for (ServerAction sa : serverActions) {
            if (shouldCleanupAction(bootTime, sa)) {
                sa.setStatus(ActionFactory.STATUS_COMPLETED);
                sa.setCompletionTime(new Date());
                sa.setResultMsg("Reboot completed.");
                sa.setResultCode(0L);
                ActionFactory.save(sa);
                actionsChanged += 1;
            }
        }
        if (actionsChanged > 0) {
            LOG.debug("{} reboot actions set to completed", actionsChanged);
        }
    }

    private static boolean shouldCleanupAction(Date bootTime, ServerAction sa) {
        Action action = sa.getParentAction();
        boolean result = false;
        if (action.getActionType().equals(ActionFactory.TYPE_REBOOT)) {
            if (sa.getStatus().equals(ActionFactory.STATUS_PICKED_UP) && sa.getPickupTime() != null) {
                result = bootTime.after(sa.getPickupTime());
            }
            else if (sa.getStatus().equals(ActionFactory.STATUS_PICKED_UP) && sa.getPickupTime() == null) {
                result = bootTime.after(action.getEarliestAction());
            }
            else if (sa.getStatus().equals(ActionFactory.STATUS_QUEUED)) {
                if (action.getPrerequisite() != null) {
                    // queued reboot actions that do not complete in 12 hours will
                    // be cleaned up by MinionActionUtils.cleanupMinionActions()
                    result = false;
                }
                else {
                    result = bootTime.after(sa.getParentAction().getEarliestAction());
                }
            }
            if (LOG.isDebugEnabled()) {
                LOG.debug("shouldCleanupAction Server:{} Action: {} BootTime: {} PickupTime: {} EarliestAction {}" +
                        " Result: {}", sa.getServer().getId(), sa.getParentAction().getId(), bootTime,
                        sa.getPickupTime(), action.getEarliestAction(), result);
            }
        }
        return result;
    }

    /**
     * Check recursively if there's prerequisite action of the given type in the completed state
     * @param action the action for which to check prerequisites
     * @param prereqType action type to check
     * @param systemId system id
     * @return true if there's prerequisite action of the given type in the completed state
     */
    public static boolean prerequisiteIsCompleted(Action action, Optional<ActionType> prereqType, long systemId) {
        if (action == null) {
            return false;
        }
        if ((!prereqType.isPresent() || prereqType.get().equals(action.getActionType())) &&
                action.getServerActions().stream()
                        .filter(sa -> sa.getServer().getId() == systemId)
                        .filter(sa -> ActionFactory.STATUS_COMPLETED.equals(sa.getStatus()))
                        .findFirst().isPresent()) {
            return true;
        }
        return prerequisiteIsCompleted(action.getPrerequisite(), prereqType, systemId);
    }

    /**
     * @param xccdfResumeXslIn to set
     */
    public void setXccdfResumeXsl(String xccdfResumeXslIn) {
        this.xccdfResumeXsl = xccdfResumeXslIn;
    }


    /**
     * Returns the same provided UUID string but representing the first
     * fields as little-endian according to RFC 4122.
     *
     * @param uuidIn the uuid string to transform without dashes "-"
     * @return the same UUID with first fields represented as little-endian
     */
    public static String uuidToLittleEndian(String uuidIn) {
        UUID uuidOrig = UUID.fromString(uuidIn.replaceAll(
                "(\\w{8})(\\w{4})(\\w{4})(\\w{4})(\\w{12})",
                "$1-$2-$3-$4-$5"));

        ByteBuffer bb = ByteBuffer.wrap(new byte[16]);
        bb.putLong(uuidOrig.getMostSignificantBits());
        bb.putLong(uuidOrig.getLeastSignificantBits());
        ByteBuffer source = ByteBuffer.wrap(bb.array());
        ByteBuffer target = ByteBuffer.allocate(16)
            .order(ByteOrder.LITTLE_ENDIAN)
            .putInt(source.getInt())
            .putShort(source.getShort())
            .putShort(source.getShort())
            .order(ByteOrder.BIG_ENDIAN)
            .putLong(source.getLong());
        target.rewind();

        UUID uuidSwap = new UUID(target.getLong(), target.getLong());
        return uuidSwap.toString().replaceAll("-", "");
    }

    private static JsonElement parseJsonError(SaltError message) {
        return message.fold(
            functionNotAvailable -> null,
            moduleNotSupported -> null,
            jsonParsingError -> jsonParsingError.getJson(),
            genericError -> null,
            saltSshError -> {
                try {
                    // Try parsing the error message as JSON
                    return JsonParser.GSON.fromJson("{" + saltSshError.getMessage() + "}", JsonElement.class);
                }
                catch (JsonParseException ex) {
                    LOG.warn("Unable to parse SaltSSHError message \"{}\"as json: {}",
                        saltSshError.getMessage(), ex.getMessage());

                    // Parsing as json has failed. Set the whole message as the "result" field, so it can be reported
                    JsonObject result = new JsonObject();
                    result.addProperty("result", saltSshError.getMessage());
                    return result;
                }
            }
        );
    }

    private static String extractStandardMessage(JsonElement json, String key) {
        if (json == null || !json.isJsonObject() || !json.getAsJsonObject().has(key)) {
            return null;
        }

        final JsonElement element = json.getAsJsonObject().get(key);
        if (element.isJsonPrimitive()) {
            return StringUtils.trimToNull(element.getAsJsonPrimitive().getAsString());
        }
        else if (element.isJsonArray()) {
            StringBuilder msg = new StringBuilder();
            element.getAsJsonArray().forEach(elem -> msg.append(elem.getAsString()));
            return StringUtils.trimToNull(msg.toString());
        }

        return null;
    }

    /**
     * Decode a {@link SaltError} to a {@link BootstrapError}.
     *
     * @param saltErr the Salt err
     * @return The parsed information from the error
     */
    public static BootstrapError decodeSaltErr(SaltError saltErr) {
        // Create a generic main message
        String detailMessage = saltErr.fold(
            err -> LOCALIZATION.getMessage("bootstrap.minion.error.salt.functionnotavailable", err.getFunctionName()),
            err -> LOCALIZATION.getMessage("bootstrap.minion.error.salt.modulenotsupported", err.getModuleName()),
            err -> LOCALIZATION.getMessage("bootstrap.minion.error.salt.jsonparsingerror"),
            err -> err.getMessage(),
            err -> LOCALIZATION.getMessage("bootstrap.minion.error.salt.saltssherror")
        );

        JsonElement jsonElement = SaltUtils.parseJsonError(saltErr);
        return new SaltBootstrapError(LOCALIZATION.getMessage("bootstrap.minion.error.salt.execution", detailMessage),
            SaltUtils.extractStandardMessage(jsonElement, "stdout"),
            SaltUtils.extractStandardMessage(jsonElement, "stderr"),
            SaltUtils.extractStandardMessage(jsonElement, "result"));
    }


    /**
     * Decode a collection of {@link State.ApplyResult} to a {@link BootstrapError}.
     *
     * @param result a map containing the result for each state applied
     * @return The parsed information from the error
     */
    public static BootstrapError decodeBootstrapSSHResult(SSHResult<Map<String, State.ApplyResult>> result) {
        String message = LOCALIZATION.getMessage("bootstrap.minion.error.salt.applystates", result.getRetcode());

        String standardOuput = result.getStdout().orElse(null);
        String standardError = result.getStderr().orElse(null);
        String resultText = StringUtils.trimToNull(
            result.getReturn().stream()
                  .flatMap(map -> map.entrySet().stream())
                  .filter(entry -> !entry.getValue().isResult())
                  .map(fail -> fail.getKey() + ": " + fail.getValue().getComment())
                  .collect(Collectors.joining("\n"))
        );

        return new SaltBootstrapError(message, standardOuput, standardError, resultText);
    }

    /**
     * Only used for unit tests.
     * @param scriptsDirIn to set
     */
    public void setScriptsDir(Path scriptsDirIn) {
        scriptsDir = scriptsDirIn;
    }

    /**
     * Return the scripts directory.
     * @return scripts directory
     */
    public Path getScriptsDir() {
        return scriptsDir;
    }

    /**
     * Returns a combined, printable string from output channels stderr and stdout.
     *
     * @param stderr the stderr message
     * @param stdout the stdout message
     * @return the string
     */
    public static String printStdMessages(String stderr, String stdout) {
        StringBuilder sb = new StringBuilder();
        if (StringUtils.isNotEmpty(stderr)) {
            sb.append("stderr:\n\n");
            sb.append(stderr);
            sb.append("\n");
            if (StringUtils.isNotEmpty(stdout)) {
                sb.append("stdout:\n\n");
                sb.append(stdout);
                sb.append("\n");
            }
        }
        else {
            sb.append(stdout);
            sb.append("\n");
        }
        return sb.toString();
    }
}
