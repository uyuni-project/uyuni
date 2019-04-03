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

package com.suse.manager.utils;

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
import com.redhat.rhn.domain.channel.AccessToken;
import com.redhat.rhn.domain.channel.Channel;
import com.redhat.rhn.domain.config.ConfigRevision;
import com.redhat.rhn.domain.image.ImageBuildHistory;
import com.redhat.rhn.domain.image.ImageInfo;
import com.redhat.rhn.domain.image.ImageInfoFactory;
import com.redhat.rhn.domain.image.ImagePackage;
import com.redhat.rhn.domain.image.ImageProfile;
import com.redhat.rhn.domain.image.ImageProfileFactory;
import com.redhat.rhn.domain.image.ImageRepoDigest;
import com.redhat.rhn.domain.image.OSImageStoreUtils;
import com.redhat.rhn.domain.product.SUSEProduct;
import com.redhat.rhn.domain.product.SUSEProductFactory;
import com.redhat.rhn.domain.rhnpackage.PackageEvr;
import com.redhat.rhn.domain.rhnpackage.PackageEvrFactory;
import com.redhat.rhn.domain.rhnpackage.PackageFactory;
import com.redhat.rhn.domain.server.InstalledPackage;
import com.redhat.rhn.domain.server.InstalledProduct;
import com.redhat.rhn.domain.server.MinionServer;
import com.redhat.rhn.domain.server.MinionServerFactory;
import com.redhat.rhn.domain.server.Server;
import com.redhat.rhn.domain.server.ServerFactory;
import com.redhat.rhn.manager.action.ActionManager;
import com.redhat.rhn.manager.audit.ScapManager;
import com.redhat.rhn.manager.errata.ErrataManager;
import com.redhat.rhn.manager.system.SystemManager;
import com.redhat.rhn.taskomatic.TaskomaticApi;
import com.redhat.rhn.taskomatic.TaskomaticApiException;
import com.suse.manager.reactor.hardware.CpuArchUtil;
import com.suse.manager.reactor.hardware.HardwareMapper;
import com.suse.manager.reactor.messaging.ApplyStatesEventMessage;
import com.suse.manager.reactor.messaging.ChannelsChangedEventMessage;
import com.suse.manager.reactor.utils.RhelUtils;
import com.suse.manager.reactor.utils.ValueMap;
import com.suse.manager.webui.services.SaltStateGeneratorService;
import com.suse.manager.webui.services.impl.SaltService;
import com.suse.manager.webui.services.impl.runner.MgrUtilRunner;
import com.suse.manager.webui.utils.YamlHelper;
import com.suse.manager.webui.utils.salt.custom.DistUpgradeDryRunSlsResult;
import com.suse.manager.webui.utils.salt.custom.DistUpgradeOldSlsResult;
import com.suse.manager.webui.utils.salt.custom.DistUpgradeSlsResult;
import com.suse.manager.webui.utils.salt.custom.FilesDiffResult;
import com.suse.manager.webui.utils.salt.custom.FilesDiffResult.DirectoryResult;
import com.suse.manager.webui.utils.salt.custom.FilesDiffResult.FileResult;
import com.suse.manager.webui.utils.salt.custom.FilesDiffResult.SymLinkResult;
import com.suse.manager.webui.utils.salt.custom.HwProfileUpdateSlsResult;
import com.suse.manager.webui.utils.salt.custom.ImageInspectSlsResult;
import com.suse.manager.webui.utils.salt.custom.ImagesProfileUpdateSlsResult;
import com.suse.manager.webui.utils.salt.custom.KernelLiveVersionInfo;
import com.suse.manager.webui.utils.salt.custom.OSImageBuildSlsResult;
import com.suse.manager.webui.utils.salt.custom.OSImageInspectSlsResult;
import com.suse.manager.webui.utils.salt.custom.Openscap;
import com.suse.manager.webui.utils.salt.custom.PkgProfileUpdateSlsResult;
import com.suse.manager.webui.utils.salt.custom.RetOpt;
import com.suse.manager.webui.utils.salt.custom.SystemInfo;
import com.suse.salt.netapi.calls.modules.Pkg;
import com.suse.salt.netapi.calls.modules.Pkg.Info;
import com.suse.salt.netapi.calls.modules.Zypper.ProductInfo;
import com.suse.salt.netapi.datatypes.target.MinionList;
import com.suse.salt.netapi.errors.JsonParsingError;
import com.suse.salt.netapi.errors.SaltError;
import com.suse.salt.netapi.results.Change;
import com.suse.salt.netapi.results.CmdResult;
import com.suse.salt.netapi.results.ModuleRun;
import com.suse.salt.netapi.results.Ret;
import com.suse.salt.netapi.results.StateApplyResult;
import com.suse.salt.netapi.utils.Xor;
import com.suse.utils.Json;
import com.suse.utils.Opt;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonElement;
import com.google.gson.JsonSyntaxException;
import com.google.gson.reflect.TypeToken;
import org.apache.commons.lang3.StringEscapeUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.log4j.Logger;

import java.io.File;
import java.io.FileInputStream;
import java.io.InputStream;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.Duration;
import java.time.Instant;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.Date;
import java.util.HashMap;
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
        "pkg.purged", "pkg.removed", "pkg.uptodate"
    );

    /** Package-affecting Salt execution module names. */
    private static final List<String> PKG_EXECUTION_MODULES = Arrays.asList(
        "pkg.group_install", "pkg.install", "pkg.purge", "pkg.remove", "pkg.upgrade"
    );

    private static final Logger LOG = Logger.getLogger(SaltUtils.class);
    private static final TaskomaticApi TASKOMATIC_API = new TaskomaticApi();

    public static final SaltUtils INSTANCE = new SaltUtils();

    private Path scriptsDir = Paths.get(SUMA_STATE_FILES_ROOT_PATH, SCRIPTS_DIR);

    private SaltService saltService = SaltService.INSTANCE;

    private String xccdfResumeXsl = "/usr/share/susemanager/scap/xccdf-resume.xslt.in";

    // SUSE OS family as defined in Salt grains
    private static final String OS_FAMILY_SUSE = "Suse";

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
     */
    public SaltUtils() { }

    /**
     * Figure out if the list of packages has changed based on the result of a Salt call
     * given as JsonElement. This information is used to decide if we should trigger a
     * package list refresh.
     *
     * @param function the Salt function that was used
     * @param callResult the result of the call
     * @return true if installed packages have changed or unparsable json, otherwise false
     */
    public boolean shouldRefreshPackageList(String function,
            Optional<JsonElement> callResult) {
        if (PKG_EXECUTION_MODULES.contains(function)) {
            return true;
        }
        if (function.equals("state.apply")) {
            return Opt.fold(
                callResult.flatMap(SaltUtils::jsonEventToStateApplyResults),
                () -> false,
                results -> results.entrySet().stream()
                    .anyMatch(result -> extractFunction(result.getKey())
                        .map(fn -> fn.equals("module.run") ?
                            PKG_EXECUTION_MODULES.contains(result.getValue().getName()) :
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
    public static PackageChangeOutcome handlePackageChanges(String function,
            JsonElement callResult, Server server) {
        final PackageChangeOutcome outcome;

        if (PKG_STATE_MODULES.contains(function)) {
            Map<String, Change<Xor<String, List<Pkg.Info>>>> delta = Json.GSON.fromJson(
                callResult,
                new TypeToken<Map<String, Change<Xor<String, List<Pkg.Info>>>>>() { }
                .getType()
            );
            ErrataManager.insertErrataCacheTask(server);
            outcome = applyChangesFromStateModule(delta, server);
        }
        else if (function.equals("state.apply")) {
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
            LOG.error("Could not parse Salt function call: " + value);
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
                (e.getValue().getNewValue().isLeft() &&
                 e.getValue().getOldValue().isLeft())

        );
        if (fullRefreshNeeded) {
            return PackageChangeOutcome.NEEDS_REFRESHING;
        }
        else {
            HibernateFactory.doWithoutAutoFlushing(() -> {
                applyDeltaPackageInfo(changes, server);
            });
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
                        e -> e.getKey(),
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

            Map<String, Info> newPackages = change.getNewValue().stream()
                .collect(Collectors.toMap(
                    info -> packageToKey(name, info),
                    Function.identity()
                ));

            change.getOldValue().stream().forEach(info -> {
                String key = packageToKey(name, info);
                if (!newPackages.containsKey(key)) {
                    Optional.ofNullable(currentPackages.get(key))
                    .ifPresent(ip -> {
                        server.getPackages().remove(ip);
                    });
                }
            });

            newPackages.values().stream().forEach(info -> {
                server.getPackages().add(
                    Optional.ofNullable(
                        currentPackages.get(packageToKey(name, info)))
                        .orElseGet(
                                () -> createPackageFromSalt(name, info, server)
                        )
                );
            });
        });
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
                apply.entrySet().stream().flatMap(e -> {
            return extractFunction(e.getKey()).<Stream<StateApplyResult<JsonElement>>>
                    map(fn -> {
                if (fn.equals("module.run")) {
                    StateApplyResult<JsonElement> ap = Json.GSON.fromJson(
                            e.getValue(),
                            new TypeToken<StateApplyResult<JsonElement>>() {
                            }.getType()
                    );
                    if (PKG_EXECUTION_MODULES.contains(ap.getName())) {
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
            }).orElseGet(Stream::empty);
        })
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
            boolean success, String jid, JsonElement jsonResult, String function) {
        serverAction.setCompletionTime(new Date());

        // Set the result code defaulting to 0
        serverAction.setResultCode(retcode);

        // If the State was not executed due 'require' statement
        // we directly set the action to FAILED.
        if (jsonResult == null && function == null) {
            serverAction.setStatus(ActionFactory.STATUS_FAILED);
            serverAction.setResultMsg("Prerequisite failed");
            return;
        }

        // Determine the final status of the action
        if (actionFailed(function, jsonResult, success, retcode)) {
            serverAction.setStatus(ActionFactory.STATUS_FAILED);
        }
        else {
            serverAction.setStatus(ActionFactory.STATUS_COMPLETED);
        }

        Action action = HibernateFactory.unproxy(serverAction.getParentAction());

        if (action.getActionType().equals(ActionFactory.TYPE_APPLY_STATES)) {
            ApplyStatesAction applyStatesAction = (ApplyStatesAction) action;

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
            statesResult.setOutput(YamlHelper.INSTANCE
                    .dump(Json.GSON.fromJson(jsonResult, Object.class)).getBytes());

            // Create the result message depending on the action status
            String states = applyStatesAction.getDetails().getMods().isEmpty() ?
                    "highstate" : applyStatesAction.getDetails().getMods().toString();
            String message = "Successfully applied state(s): " + states;
            if (serverAction.getStatus().equals(ActionFactory.STATUS_FAILED)) {
                message = "Failed to apply state(s): " + states;
            }
            if (applyStatesAction.getDetails().isTest()) {
                message += " (test-mode)";
            }
            serverAction.setResultMsg(message);
        }
        else if (action.getActionType().equals(ActionFactory.TYPE_SCRIPT_RUN)) {
            Map<String, StateApplyResult<CmdResult>> stateApplyResult = Json.GSON.fromJson(jsonResult,
                    new TypeToken<Map<String, StateApplyResult<CmdResult>>>() { }.getType());
            CmdResult result = stateApplyResult.entrySet().stream()
                    .findFirst().map(e -> e.getValue().getChanges())
                    .orElseGet(() -> new CmdResult());
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
            serverAction.getServer().asMinionServer().ifPresent(minionServer -> {
                handlePackageProfileUpdate(minionServer, Json.GSON.fromJson(jsonResult,
                        PkgProfileUpdateSlsResult.class));
            });
        }
        else if (action.getActionType().equals(ActionFactory.TYPE_HARDWARE_REFRESH_LIST)) {
            if (serverAction.getStatus().equals(ActionFactory.STATUS_FAILED)) {
                serverAction.setResultMsg("Failure");
            }
            else {
                serverAction.setResultMsg("Success");
            }
            serverAction.getServer().asMinionServer().ifPresent(minionServer -> {
                handleHardwareProfileUpdate(minionServer, Json.GSON.fromJson(jsonResult,
                        HwProfileUpdateSlsResult.class), serverAction);
            });
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
                    saltService.syncGrains(minionTarget);
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
        else {
           serverAction.setResultMsg(getJsonResultWithPrettyPrint(jsonResult));
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
        Object returnObject = Json.GSON.fromJson(jsonResult, Object.class);
        Gson gson = new GsonBuilder().setPrettyPrinting().create();
        return gson.toJson(returnObject);
    }

    private void handleSubscribeChannels(ServerAction serverAction, JsonElement jsonResult, Action action) {
        if (serverAction.getStatus().equals(ActionFactory.STATUS_COMPLETED)) {
            serverAction.setResultMsg("Successfully applied state: " + ApplyStatesEventMessage.CHANNELS);
            SubscribeChannelsAction sca = (SubscribeChannelsAction)action;

            // activate the new tokens
            List<Long> newTokenIds = sca.getDetails().getAccessTokens()
                    .stream()
                    .filter(ac -> ac.getMinion() != null)
                    .filter(ac -> ac.getMinion().getId().equals(serverAction.getServer().getId()))
                    .map(AccessToken::getId)
                    .collect(Collectors.toList());

            // if successful update channels in db and trigger pillar refresh
            SystemManager.updateServerChannels(
                    action.getSchedulerUser(),
                    serverAction.getServer(),
                    Optional.ofNullable(sca.getDetails().getBaseChannel()),
                    sca.getDetails().getChannels(),
                    newTokenIds);
        }
        else {
            Object returnObject = Json.GSON.fromJson(jsonResult, Object.class);
            Gson gson = new GsonBuilder().setPrettyPrinting().create();
            String json = gson.toJson(returnObject);
            serverAction.setResultMsg("Failed to apply state: " + ApplyStatesEventMessage.CHANNELS);
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
                message = spmig.getChanges().getRetOpt().map(ret -> {
                    return ret.entrySet().stream().map(entry -> {
                        StringBuilder sb = new StringBuilder();
                        sb.append(entry.getKey());
                        sb.append(":");
                        sb.append(entry.getValue().getOldValue());
                        sb.append("->");
                        sb.append(entry.getValue().getNewValue());
                        return sb.toString();
                    }).collect(Collectors.joining(","));
                }).orElse(spmig.getComment());
            }
            return message;
        }
        catch (JsonSyntaxException e) {
            try {
                DistUpgradeOldSlsResult distUpgradeSlsResult = Json.GSON.fromJson(
                        jsonResult, DistUpgradeOldSlsResult.class);
                String message = distUpgradeSlsResult.getSpmigration().getChanges()
                        .getRetOpt().map(ret -> {
                            if (ret.isResult()) {
                                return ret.getChanges().entrySet().stream()
                                        .map(entry -> {
                                            StringBuilder sb = new StringBuilder();
                                            sb.append(entry.getKey());
                                            sb.append(":");
                                            sb.append(entry.getValue().getOldValue());
                                            sb.append("->");
                                            sb.append(entry.getValue().getNewValue());
                                            return sb.toString();
                                        }).collect(Collectors.joining(","));
                            }
                            else {
                                return ret.getComment();
                            }
                        }).orElse("");
                return message;
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
        TypeToken<Map<String, FilesDiffResult>> typeToken = new TypeToken<Map<String, FilesDiffResult>>() { };
        Map<String, FilesDiffResult> results = Json.GSON.fromJson(jsonResult, typeToken.getType());
        Map<String, FilesDiffResult> diffResults = new HashMap<>();
        // We are only interested in results where files are different/new.
        results.values().stream().filter(fdr -> !fdr.isResult()).forEach(fdr -> diffResults.put(fdr.getName(), fdr));

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
                            new TypeToken<Map<String, DirectoryResult>>() { };
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
                    new TypeToken<Map<String, StateApplyResult<Ret<Openscap.OpenscapResult>>>>() { };
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
                            Map<Boolean, String> moveRes = saltService.storeMinionScapFiles(
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
                                        LOG.error(
                                                "Error processing SCAP results file " +
                                                        resultsFile.toString(), e);
                                        serverAction.setStatus(ActionFactory.STATUS_FAILED);
                                        serverAction.setResultMsg(
                                                "Error processing SCAP results file " +
                                                        resultsFile.toString() + ": " +
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

    private void handleImageBuildData(ServerAction serverAction, JsonElement jsonResult) {
        Action action = serverAction.getParentAction();
        ImageBuildAction ba = (ImageBuildAction)action;
        ImageBuildActionDetails details = ba.getDetails();
        Optional<ImageInfo> infoOpt = ImageInfoFactory.lookupByBuildAction(ba);

        // Pretty-print the whole return map (or whatever fits into 1024 characters)
        Object returnObject = Json.GSON.fromJson(jsonResult, Object.class);
        Gson gson = new GsonBuilder().setPrettyPrinting().create();
        String json = gson.toJson(returnObject);
        serverAction.setResultMsg(json);

        if (serverAction.getStatus().equals(ActionFactory.STATUS_COMPLETED)) {
            Optional<ImageProfile> profileOpt =
                    ImageProfileFactory.lookupById(details.getImageProfileId());

            profileOpt.ifPresent(p -> p.asKiwiProfile().ifPresent(kiwiProfile -> {
                serverAction.getServer().asMinionServer().ifPresent(minionServer -> {
                    // Download the built Kiwi image to SUSE Manager server
                    OSImageInspectSlsResult.Bundle bundleInfo =
                            Json.GSON.fromJson(jsonResult, OSImageBuildSlsResult.class)
                                    .getKiwiBuildInfo().getChanges().getRet().getBundle();
                    infoOpt.ifPresent(info -> info.setChecksum(
                            ImageInfoFactory.convertChecksum(bundleInfo.getChecksum())));
                    MgrUtilRunner.ExecResult collectResult = saltService
                            .collectKiwiImage(minionServer, bundleInfo.getFilepath(),
                                    OSImageStoreUtils.getOsImageStorePath() + kiwiProfile.getTargetStore().getUri())
                            .orElseThrow(() -> new RuntimeException("Failed to download image."));

                    if (collectResult.getReturnCode() != 0) {
                        serverAction.setStatus(ActionFactory.STATUS_FAILED);
                        serverAction.setResultMsg(StringUtils
                                .left(printStdMessages(collectResult.getStderr(), collectResult.getStdout()), 1024));
                    }
                });
            }));
            ImageInspectAction iAction = ActionManager.scheduleImageInspect(
                    action.getSchedulerUser(),
                    action.getServerActions()
                            .stream()
                            .map(ServerAction::getServerId)
                            .collect(Collectors.toList()),
                    Optional.of(action.getId()),
                    details.getVersion(),
                    profileOpt.map(ImageProfile::getLabel).orElse(null),
                    profileOpt.map(ImageProfile::getTargetStore).orElse(null),
                    Date.from(Instant.now())
            );
            try {
                TASKOMATIC_API.scheduleActionExecution(iAction);
            }
            catch (TaskomaticApiException e) {
                LOG.error("Could not schedule image inspection");
                LOG.error(e);
            }

            infoOpt.ifPresent(info -> {
                info.setRevisionNumber(info.getRevisionNumber() + 1);
                info.setInspectAction(iAction);
                ImageInfoFactory.save(info);
            });
        }
    }

    private void handleImageInspectData(ServerAction serverAction,
            JsonElement jsonResult) {
        Action action = serverAction.getParentAction();
        ImageInspectAction ia = (ImageInspectAction) action;
        ImageInspectActionDetails details = ia.getDetails();
        ImageInfoFactory
                .lookupByName(details.getName(), details.getVersion(),
                        details.getImageStoreId())
                .ifPresent(imageInfo -> serverAction.getServer().asMinionServer()
                        .ifPresent(minionServer -> handleImagePackageProfileUpdate(
                                imageInfo, Json.GSON.fromJson(jsonResult,
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
    private static boolean actionFailed(String function, JsonElement rawResult,
            boolean success, long retcode) {
        // For state.apply based actions verify the result of each state
        if (function.equals("state.apply")) {
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
            new TypeToken<Map<String, StateApplyResult<Map<String, Object>>>>() { };
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

        if (imageInfo.getProfile().asDockerfileProfile().isPresent()) {
            if (result.getDockerInspect().isResult()) {
                ImageInspectSlsResult iret = result.getDockerInspect().getChanges().getRet();
                imageInfo.setChecksum(ImageInfoFactory.convertChecksum(iret.getId()));
                ImageBuildHistory history = new ImageBuildHistory();
                history.setImageInfo(imageInfo);
                // revision number was already incremented in handleImageBuildData()
                history.setRevisionNumber(imageInfo.getRevisionNumber());
                history.getRepoDigests().addAll(
                        iret.getRepoDigests().stream().map(digest -> {
                            ImageRepoDigest repoDigest = new ImageRepoDigest();
                            repoDigest.setRepoDigest(digest);
                            repoDigest.setBuildHistory(history);
                            return repoDigest;
                        }).collect(Collectors.toSet()));
                imageInfo.getBuildHistory().add(history);
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
                                        .flatMap(infoList -> infoList.stream())
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
                Optional<String> resReleasePkg =
                        Optional.ofNullable(ret.getWhatProvidesResReleasePkg())
                                .map(StateApplyResult::getChanges)
                                .filter(res -> res.getStdout() != null)
                                .map(CmdResult::getStdout);
                if (rhelReleaseFile.isPresent() || centosReleaseFile.isPresent() ||
                        resReleasePkg.isPresent()) {
                    Set<InstalledProduct> products = getInstalledProductsForRhel(
                            imageInfo, resReleasePkg,
                            rhelReleaseFile, centosReleaseFile);
                    imageInfo.setInstalledProducts(products);
                }
            }
            else {
                // do not fail the action when no packages are returned
                serverAction.setResultMsg(result.getDockerSlsBuild().getComment());
            }
        }

        if (imageInfo.getProfile().asKiwiProfile().isPresent()) {
            if (result.getKiwiInspect().isResult()) {
                Long instantNow = new Date().getTime() / 1000L;
                OSImageInspectSlsResult ret = result.getKiwiInspect().getChanges().getRet();
                List<OSImageInspectSlsResult.Package> packages = ret.getPackages();
                packages.forEach(pkg -> createImagePackageFromSalt(pkg.getName(), Optional.of(pkg.getEpoch()),
                        Optional.of(pkg.getRelease()), pkg.getVersion(), Optional.of(instantNow),
                        Optional.of(pkg.getArch()), imageInfo));
                if ("pxe".equals(ret.getImage().getType())) {
                    String storeDirectory = OSImageStoreUtils.getOSImageStoreURIForOrg(
                            serverAction.getParentAction().getOrg());
                    SaltStateGeneratorService.INSTANCE.generateOSImagePillar(ret.getImage(), ret.getBundle(),
                            ret.getBootImage(), storeDirectory);
                }
            }
            else {
                serverAction.setResultMsg(result.getKiwiInspect().getComment());
                as = ActionFactory.STATUS_FAILED;
            }
        }

        serverAction.setStatus(as);
        ImageInfoFactory.save(imageInfo);
        ErrataManager.insertErrataCacheTask(imageInfo);
    }

    /**
     * Perform the actual update of the database based on given event data.
     *
     * @param server the minion server
     * @param result the result of the call as parsed from event data
     */
    private static void handlePackageProfileUpdate(MinionServer server,
            PkgProfileUpdateSlsResult result) {
        Instant start = Instant.now();

        HibernateFactory.doWithoutAutoFlushing(() -> {
            updatePackages(server, result);
        });

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
        Optional<String> resReleasePkg =
                Optional.ofNullable(result.getWhatProvidesResReleasePkg())
                .map(StateApplyResult::getChanges)
                .filter(ret -> ret.getStdout() != null)
                .map(CmdResult::getStdout);
        if (rhelReleaseFile.isPresent() || centosReleaseFile.isPresent() ||
                resReleasePkg.isPresent()) {
            Set<InstalledProduct> products = getInstalledProductsForRhel(
                    server, resReleasePkg,
                    rhelReleaseFile, centosReleaseFile);
            server.setInstalledProducts(products);
        }
        else if ("ubuntu".equalsIgnoreCase((String) result.getGrains().get("os"))) {
            String osArch = result.getGrains().get("osarch") + "-deb";
            String osVersion = (String) result.getGrains().get("osrelease");
            // Check if we have a product for the specific arch and version
            SUSEProduct ubuntuProduct = SUSEProductFactory.findSUSEProduct("ubuntu-client", osVersion, null, osArch,
                    false);
            if (ubuntuProduct != null) {
                InstalledProduct installedProduct = SUSEProductFactory.findInstalledProduct(ubuntuProduct)
                        .orElse(new InstalledProduct(ubuntuProduct));
                server.setInstalledProducts(Collections.singleton(installedProduct));
            }
        }

        // Update live patching version
        server.setKernelLiveVersion(result.getKernelLiveVersionInfo()
                .map(klv -> klv.getChanges().getRet()).filter(Objects::nonNull)
                .map(KernelLiveVersionInfo::getKernelLiveVersion).orElse(null));

        // Update grains
        if (!result.getGrains().isEmpty()) {
            ValueMap grains = new ValueMap(result.getGrains());
            server.setOsFamily(grains.getValueAsString("os_family"));
            server.setRunningKernel(grains.getValueAsString("kernelrelease"));
            server.setOs(grains.getValueAsString("osfullname"));

            /** Release is set directly from grain information for SUSE systems only.
                RH systems require some parsing on the grains to get the correct release
                See RegisterMinionEventMessageAction#getOsRelease

                However, release can change only after SP migration and SUMA supports this only on SUSE systems.
                Also, the getOsRelease method requires remote command execution and was therefore avoided for now.
                If we decide to support RedHat distro/SP upgrades in the future, this code has to be reviewed.
             */
            if (server.getOsFamily().equals(OS_FAMILY_SUSE)) {
                server.setRelease(grains.getValueAsString("osrelease"));
            }
        }

        ServerFactory.save(server);
        if (LOG.isDebugEnabled()) {
            long duration = Duration.between(start, Instant.now()).getSeconds();
            LOG.debug("Package profile updated for minion: " + server.getMinionId() +
                    " (" + duration + " seconds)");
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
                        Function.identity()
                ));

        Collection<InstalledPackage> unchanged = oldPackageMap.entrySet().stream().filter(
            e -> newPackageMap.containsKey(e.getKey())
        ).map(Map.Entry::getValue).collect(Collectors.toList());
        packages.retainAll(unchanged);

        Collection<InstalledPackage> added = newPackageMap.entrySet().stream().filter(
           e -> !oldPackageMap.containsKey(e.getKey())
        ).map(
           e -> createPackageFromSalt(e.getValue().getKey(), e.getValue().getValue(),
                   server)
        ).collect(Collectors.toList());
        packages.addAll(added);
    }

    /**
     * Returns a key string that uniquely identifies an installed package (as a
     * Hibernated object)
     *
     * @param p the package
     * @return the key
     */
    public static String packageToKey(InstalledPackage p) {
        StringBuilder sb = new StringBuilder();

        // name and EVR are never null due to DB constraints
        // see schema/spacewalk/common/tables/rhnServerPackage.sql
        sb.append(p.getName().getName());
        sb.append("-");
        sb.append(p.getEvr().toString());
        sb.append(".");
        sb.append(Optional.ofNullable(p.getArch()).map(a -> a.getName()).orElse("unknown"));

        return sb.toString();
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
                        info.getRelease().orElse("0")
                ).toString()
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
    public static String packageToKey(Map.Entry<String, Pkg.Info> entry) {
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
        hwMapper.mapNetworkInfo(
                result.getNetworkInterfaces(),
                Optional.of(result.getNetworkIPs()),
                result.getNetworkModules());

        // Let the action fail in case there is error messages
        if (!hwMapper.getErrors().isEmpty()) {
            serverAction.setStatus(ActionFactory.STATUS_FAILED);
            serverAction.setResultMsg("Hardware list could not be refreshed completely:\n" +
                    hwMapper.getErrors().stream().collect(Collectors.joining("\n")));
            serverAction.setResultCode(-1L);
        }

        if (LOG.isDebugEnabled()) {
            long duration = Duration.between(start, Instant.now()).getSeconds();
            LOG.debug("Hardware profile updated for minion: " + server.getMinionId() +
                    " (" + duration + " seconds)");
        }
    }

    /**
     * Create a {@link InstalledPackage} object from package name and info and return it.
     *
     * @param name package name from salt
     * @param info package info from salt
     * @param server server this package will be added to
     * @return the InstalledPackage object
     */
    private static InstalledPackage createPackageFromSalt(String name, Pkg.Info info, Server server) {

        String serverArchTypeLabel = server.getServerArch().getArchType().getLabel();

        InstalledPackage pkg = new InstalledPackage();
        pkg.setEvr(parsePackageEvr(info.getEpoch(), info.getVersion().get(), info.getRelease(), serverArchTypeLabel));
        pkg.setInstallTime(new Date(info.getInstallDateUnixTime().get() * 1000));
        pkg.setName(PackageFactory.lookupOrCreatePackageByName(name));
        pkg.setServer(server);

        // Add -deb suffix to architectures for Debian systems
        String pkgArch = info.getArchitecture().get();
        if ("deb".equals(serverArchTypeLabel)) {
            pkgArch += "-deb";
        }
        pkg.setArch(PackageFactory.lookupPackageArchByLabel(pkgArch));

        return pkg;
    }

    private static PackageEvr parsePackageEvr(Optional<String> epoch, String version, Optional<String> release,
            String archTypeLabel) {

        if ("deb".equals(archTypeLabel)) {
            // We need additional parsing for deb package versions
            return PackageEvrFactory.lookupOrCreatePackageEvr(PackageUtils.parseDebianEvr(version));
        }

        return PackageEvrFactory.lookupOrCreatePackageEvr(epoch.map(StringUtils::trimToNull).orElse(null), version,
                release.orElse("0"));
    }

    private static ImagePackage createImagePackageFromSalt(String name, Pkg.Info info, ImageInfo imageInfo) {
        return createImagePackageFromSalt(name, info.getEpoch(), info.getRelease(), info.getVersion().get(),
                info.getInstallDateUnixTime(), info.getArchitecture(), imageInfo);
    }

    private static ImagePackage createImagePackageFromSalt(String name, Optional<String> epoch,
            Optional<String> release, String version, Optional<Long> installDateUnixTime, Optional<String> architecture,
            ImageInfo imageInfo) {

        String archType = imageInfo.getImageArch().getArchType().getLabel();
        Optional<String> pkgArch = architecture.map(arch -> archType.equals("deb") ? arch + "-deb" : arch);
        PackageEvr evr = parsePackageEvr(epoch, version, release, archType);
        return createImagePackageFromSalt(name, evr, installDateUnixTime, pkgArch, imageInfo);
    }

    private static ImagePackage createImagePackageFromSalt(String name, PackageEvr pkgEvr,
            Optional<Long> installDateUnixTime, Optional<String> architecture, ImageInfo imageInfo) {
        ImagePackage pkg = new ImagePackage();
        pkg.setEvr(pkgEvr);
        architecture.ifPresent(arch -> pkg.setArch(PackageFactory.lookupPackageArchByLabel(arch)));
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
           Optional<String> rhelReleaseFile,
           Optional<String> centosRelaseFile) {

        Optional<RhelUtils.RhelProduct> rhelProductInfo =
                RhelUtils.detectRhelProduct(server, resPackage,
                        rhelReleaseFile, centosRelaseFile);

        if (!rhelProductInfo.isPresent()) {
            LOG.warn("Could not determine RHEL product type for minion: " +
                    server.getMinionId());
            return Collections.emptySet();
        }

        LOG.debug(String.format(
                "Detected minion %s as a RedHat compatible system: %s %s %s %s",
                server.getMinionId(),
                rhelProductInfo.get().getName(), rhelProductInfo.get().getVersion(),
                rhelProductInfo.get().getRelease(), server.getServerArch().getName()));

        return rhelProductInfo.get().getSuseProduct().map(product -> {
            String arch = server.getServerArch().getLabel().replace("-redhat-linux", "");

            InstalledProduct installedProduct = new InstalledProduct();
            installedProduct.setName(product.getName());
            installedProduct.setVersion(product.getVersion());
            installedProduct.setRelease(product.getRelease());
            installedProduct.setArch(PackageFactory.lookupPackageArchByLabel(arch));
            installedProduct.setBaseproduct(true);

            return Collections.singleton(installedProduct);
        }).orElse(Collections.emptySet());
    }

    private static Set<InstalledProduct> getInstalledProductsForRhel(
            ImageInfo image,
            Optional<String> resPackage,
            Optional<String> rhelReleaseFile,
            Optional<String> centosRelaseFile) {

         Optional<RhelUtils.RhelProduct> rhelProductInfo =
                 RhelUtils.detectRhelProduct(image, resPackage,
                         rhelReleaseFile, centosRelaseFile);

         if (!rhelProductInfo.isPresent()) {
             LOG.warn("Could not determine RHEL product type for image: " +
                     image.getName() + " " + image.getVersion());
             return Collections.emptySet();
         }

         LOG.debug(String.format(
                 "Detected image %s:%s as a RedHat compatible system: %s %s %s %s",
                 image.getName(), image.getVersion(),
                 rhelProductInfo.get().getName(), rhelProductInfo.get().getVersion(),
                 rhelProductInfo.get().getRelease(), image.getImageArch().getName()));

         return rhelProductInfo.get().getSuseProduct().map(product -> {
             String arch = image.getImageArch().getLabel().replace("-redhat-linux", "");

             InstalledProduct installedProduct = new InstalledProduct();
             installedProduct.setName(product.getName());
             installedProduct.setVersion(product.getVersion());
             installedProduct.setRelease(product.getRelease());
             installedProduct.setArch(PackageFactory.lookupPackageArchByLabel(arch));
             installedProduct.setBaseproduct(true);

             return Collections.singleton(installedProduct);
         }).orElse(Collections.emptySet());
     }

    /**
     * Update the system info through grains and data returned by status.uptime
     * @param jsonResult response from salt master against util.systeminfo state
     * @param minionId ID of the minion for which information should be updated
     */
    public void updateSystemInfo(JsonElement jsonResult, String minionId) {
        Optional<MinionServer> minionServer = MinionServerFactory.findByMinionId(minionId);
        minionServer.ifPresent(minion -> {
            SystemInfo systemInfo = Json.GSON.fromJson(jsonResult, SystemInfo.class);
            updateSystemInfo(systemInfo, minion);
        });
    }

    /**
     * Update the system info of the minion
     * @param systemInfo response from salt master against util.systeminfo state
     * @param minion  minion for which information should be updated
     */
    public void updateSystemInfo(SystemInfo systemInfo, MinionServer minion) {
        systemInfo.getKerneRelese().ifPresent(kerneRelese -> {
            minion.setRunningKernel(kerneRelese);
            ServerFactory.save(minion);
        });
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
    public void handleUptimeUpdate(MinionServer minion, Long uptimeSeconds) {
        Date bootTime = new Date(
                System.currentTimeMillis() - (uptimeSeconds * 1000));
        LOG.debug("Set last boot for " + minion.getMinionId() + " to " + bootTime);
        minion.setLastBoot(bootTime.getTime() / 1000);

        // cleanup old reboot actions
        @SuppressWarnings("unchecked")
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
            LOG.debug(actionsChanged + " reboot actions set to completed");
        }
    }

    private boolean shouldCleanupAction(Date bootTime, ServerAction sa) {
        Action action = sa.getParentAction();
        if (action.getActionType().equals(ActionFactory.TYPE_REBOOT)) {
            if (sa.getStatus().equals(ActionFactory.STATUS_PICKED_UP) && sa.getPickupTime() != null) {
                return bootTime.after(sa.getPickupTime());
            }
            else if (sa.getStatus().equals(ActionFactory.STATUS_PICKED_UP) && sa.getPickupTime() == null) {
                return bootTime.after(action.getEarliestAction());
            }
            else if (sa.getStatus().equals(ActionFactory.STATUS_QUEUED)) {
                if (action.getPrerequisite() != null) {
                    // queued reboot actions that do not complete in 12 hours will
                    // be cleaned up by MinionActionUtils.cleanupMinionActions()
                    return false;
                }
                return bootTime.after(sa.getParentAction().getEarliestAction());
            }
        }
        return false;
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
     * For unit testing only.
     * @param saltServiceIn the {@link SaltService} to set
     */
    public void setSaltService(SaltService saltServiceIn) {
        this.saltService = saltServiceIn;
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

    /**
     * Decode the std message from the whole message
     *
     * @param message the message Object
     * @param key the json key of the message to decode (e.g.: sdterr, stdout)
     * @return the String decoded if it exists
     */
    public static Optional<String> decodeStdMessage(Object message, String key) {
        if (message instanceof JsonParsingError) {
            JsonElement json = ((JsonParsingError)message).getJson();
            if (json.isJsonObject() && json.getAsJsonObject().has(key)) {
                if (json.getAsJsonObject().get(key).isJsonPrimitive() &&
                    json.getAsJsonObject().get(key).getAsJsonPrimitive().isString()) {
                    return Optional.of(json.getAsJsonObject()
                            .get(key).getAsJsonPrimitive().getAsString());
                }
                else if (json.getAsJsonObject().get(key).isJsonArray()) {
                    StringBuilder msg = new StringBuilder();
                    json.getAsJsonObject().get(key).getAsJsonArray()
                            .forEach(elem -> msg.append(elem.getAsString()));
                    return Optional.of(msg.toString());
                }
            }
        }

        return Optional.empty();
    }

    /**
     * Decode a {@link SaltError} to a string error message.
     *
     * @param saltErr the Salt err
     * @return the error as a string
     */
    public static String decodeSaltErr(SaltError saltErr) {
        Optional<String> errorMessage = SaltUtils.decodeStdMessage(saltErr, "stderr");
        Optional<String> outMessage = !errorMessage.isPresent() ?
                SaltUtils.decodeStdMessage(saltErr, "stdout") : errorMessage;
        Optional<String> returnMessage = !outMessage.isPresent() ?
                SaltUtils.decodeStdMessage(saltErr, "return") : outMessage;
        return returnMessage.orElseGet(() -> saltErr.toString());
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
