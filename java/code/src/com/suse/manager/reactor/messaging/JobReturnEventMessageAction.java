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
package com.suse.manager.reactor.messaging;

import com.google.gson.JsonElement;
import com.google.gson.JsonSyntaxException;

import com.redhat.rhn.common.messaging.EventMessage;
import com.redhat.rhn.common.messaging.MessageQueue;
import com.redhat.rhn.domain.action.Action;
import com.redhat.rhn.domain.action.ActionFactory;
import com.redhat.rhn.domain.action.dup.DistUpgradeAction;
import com.redhat.rhn.domain.action.dup.DistUpgradeActionDetails;
import com.redhat.rhn.domain.action.dup.DistUpgradeChannelTask;
import com.redhat.rhn.domain.action.salt.ApplyStatesAction;
import com.redhat.rhn.domain.action.salt.ApplyStatesActionResult;
import com.redhat.rhn.domain.action.script.ScriptResult;
import com.redhat.rhn.domain.action.script.ScriptRunAction;
import com.redhat.rhn.domain.action.server.ServerAction;
import com.redhat.rhn.domain.channel.Channel;
import com.redhat.rhn.domain.product.SUSEProduct;
import com.redhat.rhn.domain.product.SUSEProductFactory;
import com.redhat.rhn.domain.rhnpackage.PackageEvr;
import com.redhat.rhn.domain.rhnpackage.PackageEvrFactory;
import com.redhat.rhn.domain.rhnpackage.PackageFactory;
import com.redhat.rhn.domain.server.MinionServerFactory;
import com.redhat.rhn.domain.server.Server;
import com.redhat.rhn.domain.server.ServerFactory;
import com.redhat.rhn.frontend.events.AbstractDatabaseAction;
import com.redhat.rhn.manager.action.ActionManager;
import com.redhat.rhn.manager.errata.ErrataManager;
import com.redhat.rhn.domain.server.InstalledPackage;
import com.redhat.rhn.domain.server.InstalledProduct;
import com.redhat.rhn.domain.server.MinionServer;

import com.suse.manager.reactor.hardware.CpuArchUtil;
import com.suse.manager.reactor.hardware.HardwareMapper;
import com.suse.manager.reactor.utils.RhelUtils;
import com.suse.manager.reactor.utils.ValueMap;
import com.suse.manager.webui.services.SaltServerActionService;
import com.suse.manager.webui.services.impl.SaltService;
import com.suse.manager.webui.utils.YamlHelper;
import com.suse.manager.webui.utils.salt.custom.HwProfileUpdateSlsResult;
import com.suse.manager.webui.utils.salt.custom.PkgProfileUpdateSlsResult;
import com.suse.manager.webui.utils.salt.custom.ScheduleMetadata;
import com.suse.salt.netapi.calls.modules.Pkg;
import com.suse.salt.netapi.calls.modules.Zypper.ProductInfo;
import com.suse.salt.netapi.datatypes.target.MinionList;
import com.suse.salt.netapi.event.JobReturnEvent;
import com.suse.salt.netapi.results.CmdExecCodeAllResult;
import com.suse.salt.netapi.results.StateApplyResult;
import com.suse.utils.Opt;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.reflect.TypeToken;

import com.suse.utils.Json;
import org.apache.commons.lang.StringUtils;
import org.apache.log4j.Logger;

import java.time.Duration;
import java.time.Instant;
import java.util.Arrays;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.Collections;
import java.util.function.Predicate;
import java.util.stream.Collector;
import java.util.stream.Collectors;
import java.util.stream.Stream;

/**
 * Handler class for {@link JobReturnEventMessage}.
 */
public class JobReturnEventMessageAction extends AbstractDatabaseAction {


    /**
     * Converts an event to json
     *
     * @param jobReturnEvent the return event
     */
    private static Optional<JsonElement> eventToJson(JobReturnEvent jobReturnEvent) {
        Optional<JsonElement> jsonResult = Optional.empty();
        try {
            jsonResult = Optional.ofNullable(
                jobReturnEvent.getData().getResult(JsonElement.class));
        }
        catch (JsonSyntaxException e) {
            LOG.error("JSON syntax error while decoding into a StateApplyResult:");
            LOG.error(jobReturnEvent.getData().getResult(JsonElement.class).toString());
        }
        return jsonResult;
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


    /* Logger for this class */
    private static final Logger LOG = Logger.getLogger(JobReturnEventMessageAction.class);

    /* List of Salt modules that could possibly change installed packages */
    private final List<String> packageChangingModules = Arrays.asList("pkg.install",
            "pkg.remove", "pkg_installed", "pkg_latest", "pkg_removed");

    @Override
    public void doExecute(EventMessage msg) {
        JobReturnEventMessage jobReturnEventMessage = (JobReturnEventMessage) msg;
        JobReturnEvent jobReturnEvent = jobReturnEventMessage.getJobReturnEvent();

        // React according to the function the minion ran
        String function = jobReturnEvent.getData().getFun();

        if (LOG.isDebugEnabled()) {
            LOG.debug("Job return event for minion: " +
                    jobReturnEvent.getMinionId() + "/" + jobReturnEvent.getJobId() +
                    " (" + function + ")");
        }

        // Adjust action status if the job was scheduled by us
        Optional<Long> actionId = getActionId(jobReturnEvent);
        actionId.ifPresent(id -> {

            // Lookup the corresponding action
            Optional<Action> action = Optional.ofNullable(ActionFactory.lookupById(id));
            if (action.isPresent()) {

                if (LOG.isDebugEnabled()) {
                    LOG.debug("Matched salt job with action (id=" + id + ")");
                }


                // FIXME: This is a hack and should not be considered the final solution
                if (action.get().getActionType().equals(ActionFactory.TYPE_DIST_UPGRADE) &&
                        function.equals("test.ping")) {
                    SaltServerActionService.INSTANCE.execute(action.get(), false);
                }
                else {
                    Optional<MinionServer> minionServerOpt = MinionServerFactory
                            .findByMinionId(jobReturnEvent.getMinionId());
                    minionServerOpt.ifPresent(minionServer -> {
                        Optional<ServerAction> serverAction = action.get().getServerActions()
                                .stream()
                                .filter(sa -> sa.getServer().equals(minionServer)).findFirst();


                        serverAction.ifPresent(sa -> {
                            if (LOG.isDebugEnabled()) {
                                LOG.debug("Updating action for server: " +
                                        minionServer.getId());
                            }
                            updateServerAction(sa,
                                    jobReturnEvent.getData().getRetcode(),
                                    jobReturnEvent.getData().isSuccess(),
                                    jobReturnEvent.getJobId(),
                                    jobReturnEvent.getData().getResult(JsonElement.class),
                                    jobReturnEvent.getData().getFun());
                            ActionFactory.save(sa);
                        });
                    });
                }

                // Delete schedule on the minion if we created it
                jobReturnEvent.getData().getSchedule().ifPresent(scheduleName -> {
                    if (LOG.isDebugEnabled()) {
                        LOG.debug("Deleting schedule '" + scheduleName +
                                "' from minion: " + jobReturnEvent.getMinionId());
                    }
                    SaltService.INSTANCE.deleteSchedule(scheduleName,
                            new MinionList(jobReturnEvent.getMinionId()));
                });
            }
            else {
                LOG.warn("Action referenced from Salt job was not found: " + id);
            }
        });

        // Schedule a package list refresh if either requested or detected as necessary
        if (
            forcePackageListRefresh(jobReturnEvent) ||
            shouldRefreshPackageList(jobReturnEvent)
        ) {
            MinionServerFactory
                    .findByMinionId(jobReturnEvent.getMinionId())
                    .ifPresent(minionServer -> {
                ActionManager.schedulePackageRefresh(minionServer.getOrg(), minionServer);
            });
        }

        // For all jobs: update minion last checkin
        Optional<MinionServer> minion = MinionServerFactory.findByMinionId(
                jobReturnEvent.getMinionId());
        if (minion.isPresent()) {
            minion.get().updateServerInfo();
        }
        else {
            // Or trigger registration if minion is not present
            MessageQueue.publish(new RegisterMinionEventMessage(
                    jobReturnEvent.getMinionId()));
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
    public static void updateServerAction(ServerAction serverAction, long retcode,
            boolean success, String jid, JsonElement jsonResult, String function) {
        serverAction.setCompletionTime(new Date());

        // Set the result code defaulting to 0
        serverAction.setResultCode(retcode);

        // Determine the final status of the action
        if (actionFailed(function, jsonResult, success, retcode)) {
            serverAction.setStatus(ActionFactory.STATUS_FAILED);
        }
        else {
            serverAction.setStatus(ActionFactory.STATUS_COMPLETED);
        }

        Action action = serverAction.getParentAction();
        if (action.getActionType().equals(ActionFactory.TYPE_APPLY_STATES)) {
            ApplyStatesAction applyStatesAction = (ApplyStatesAction) action;
            ApplyStatesActionResult statesResult = new ApplyStatesActionResult();
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
            serverAction.setResultMsg(message);
        }
        else if (action.getActionType().equals(ActionFactory.TYPE_SCRIPT_RUN)) {
            CmdExecCodeAllResult result = Json.GSON.fromJson(jsonResult,
                    CmdExecCodeAllResult.class);
            ScriptRunAction scriptAction = (ScriptRunAction) action;
            ScriptResult scriptResult = new ScriptResult();
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
            StringBuilder sb = new StringBuilder();
            if (StringUtils.isNotEmpty(result.getStderr())) {
                sb.append("stderr:\n\n");
                sb.append(result.getStderr());
                sb.append("\n");
            }
            if (StringUtils.isNotEmpty(result.getStdout())) {
                sb.append("stdout:\n\n");
                sb.append(result.getStdout());
                sb.append("\n");
            }
            scriptResult.setOutput(sb.toString().getBytes());
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
                MessageQueue.publish(new ChannelsChangedEventMessage(serverAction.getServerId()));
            }
            //TODO: add better result message once we know how dup state apply looks
            // Pretty-print the whole return map (or whatever fits into 1024 characters)
            Object returnObject = Json.GSON.fromJson(jsonResult, Object.class);
            Gson gson = new GsonBuilder().setPrettyPrinting().create();
            String json = gson.toJson(returnObject);
            serverAction.setResultMsg(json.length() > 1024 ?
                    json.substring(0, 1024) : json);
        }
        else {
            // Pretty-print the whole return map (or whatever fits into 1024 characters)
            Object returnObject = Json.GSON.fromJson(jsonResult, Object.class);
            Gson gson = new GsonBuilder().setPrettyPrinting().create();
            String json = gson.toJson(returnObject);
            serverAction.setResultMsg(json.length() > 1024 ?
                    json.substring(0, 1024) : json);
        }
    }

    /**
     * Find the action id corresponding to a given job return event in the job metadata.
     *
     * @param event the job return event
     * @return the corresponding action id or empty optional
     */
    private Optional<Long> getActionId(JobReturnEvent event) {
        return event.getData().getMetadata(ScheduleMetadata.class).map(
            ScheduleMetadata::getSumaActionId);
    }

    /**
     * Lookup the metadata to see if a package list refresh was requested.
     *
     * @param event the job return event
     * @return true if a package list refresh was requested, otherwise false
     */
    private boolean forcePackageListRefresh(JobReturnEvent event) {
        return event.getData().getMetadata(ScheduleMetadata.class)
                .map(ScheduleMetadata::isForcePackageListRefresh)
                .orElse(false);
    }

    /**
     * Figure out if the list of packages has changed based on a {@link JobReturnEvent}.
     * This information is used to decide if we should trigger a package list refresh.
     *
     * @param event the job return event
     * @return true if installed packages have changed or unparsable json, otherwise false
     */
    private boolean shouldRefreshPackageList(JobReturnEvent event) {
        String function = event.getData().getFun();
        switch (function) {
            case "pkg.upgrade": return true;
            case "pkg.install": return true;
            case "pkg.remove": return true;
            case "state.apply":
                Predicate<StateApplyResult<Map<String, Object>>> filterCondition = result ->
                    packageChangingModules.contains(
                        result.getName()) && !result.getChanges().isEmpty();

                Optional<Map<String, StateApplyResult<Map<String, Object>>>>
                resultsOptional = Opt.fold(
                    eventToJson(event),
                    Optional::empty,
                    rawResult -> jsonEventToStateApplyResults(rawResult));

                return Opt.fold(
                    resultsOptional,
                    () -> true,
                    results ->
                        results.values().stream().filter(
                            filterCondition).findAny().isPresent());
            default: return false;
        }
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
        boolean stateApplySuccess = true;
        if (function.equals("state.apply")) {
            return Opt.fold(
                jsonEventToStateApplyResults(rawResult),
                () -> true,
                results -> results.values().stream().filter(
                    result -> !result.isResult()).findAny().isPresent());
        }
        return !(success && retcode == 0 && stateApplySuccess);
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

        Optional.of(result.getInfoInstalled().getChanges().getRet())
            .map(saltPkgs -> saltPkgs.entrySet().stream().map(
                entry -> createPackageFromSalt(entry.getKey(), entry.getValue(), server)
            ).collect(Collectors.toSet())
        ).ifPresent(newPackages -> {
            Set<InstalledPackage> oldPackages = server.getPackages();
            oldPackages.addAll(newPackages);
            oldPackages.retainAll(newPackages);
        });

        Optional.ofNullable(result.getListProducts())
                .map(products -> products.getChanges().getRet())
                .map(JobReturnEventMessageAction::getInstalledProducts)
                .ifPresent(server::setInstalledProducts);

        Optional<String> rhelReleaseFile =
                Optional.ofNullable(result.getRhelReleaseFile())
                .map(content -> content.getChanges())
                .filter(ret -> ret.getStdout() != null)
                .map(ret -> ret.getStdout());
        Optional<String> centosReleaseFile =
                Optional.ofNullable(result.getCentosReleaseFile())
                .map(content -> content.getChanges())
                .filter(ret -> ret.getStdout() != null)
                .map(ret -> ret.getStdout());
        Optional<String> resReleasePkg =
                Optional.ofNullable(result.getWhatProvidesResReleasePkg())
                .map(content -> content.getChanges())
                .filter(ret -> ret.getStdout() != null)
                .map(ret -> ret.getStdout());
        if (rhelReleaseFile.isPresent() || centosReleaseFile.isPresent() ||
                resReleasePkg.isPresent()) {
            Set<InstalledProduct> products = JobReturnEventMessageAction
                    .getInstalledProductsForRhel(server, resReleasePkg,
                            rhelReleaseFile, centosReleaseFile);
            server.setInstalledProducts(products);
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
     * Update the hardware profile for a minion in the database from incoming event data.
     *
     * @param server the minion server
     * @param result the result of the call as parsed from event data
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
     * @param name name of the package
     * @param info package info from salt
     * @param server server this package will be added to
     * @return the InstalledPackage object
     */
    private static InstalledPackage createPackageFromSalt(
            String name, Pkg.Info info, Server server) {
        String epoch = info.getEpoch().orElse(null);
        String release = info.getRelease().orElse("0");
        String version = info.getVersion().get();
        PackageEvr evr = PackageEvrFactory
                .lookupOrCreatePackageEvr(epoch, version, release);

        InstalledPackage pkg = new InstalledPackage();
        pkg.setEvr(evr);
        pkg.setArch(PackageFactory.lookupPackageArchByLabel(info.getArchitecture().get()));
        pkg.setInstallTime(Date.from(info.getInstallDate().get().toInstant()));
        pkg.setName(PackageFactory.lookupOrCreatePackageByName(name));
        pkg.setServer(server);
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

            return Stream.of(
                    SUSEProductFactory.findInstalledProduct(name, version, release,
                            PackageFactory.lookupPackageArchByLabel(arch), isbase)
                    .orElse(
                            // Use installed product information from the client
                            new InstalledProduct(name, version,
                                    PackageFactory.lookupPackageArchByLabel(arch),
                                    release, isbase)));
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


    @Override
    public boolean canRunConcurrently() {
        return true;
    }

}
