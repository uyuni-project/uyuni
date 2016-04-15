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

import com.redhat.rhn.common.messaging.EventMessage;
import com.redhat.rhn.common.messaging.MessageQueue;
import com.redhat.rhn.domain.action.Action;
import com.redhat.rhn.domain.action.ActionFactory;
import com.redhat.rhn.domain.action.salt.ApplyStatesAction;
import com.redhat.rhn.domain.action.salt.ApplyStatesActionResult;
import com.redhat.rhn.domain.action.script.ScriptResult;
import com.redhat.rhn.domain.action.script.ScriptRunAction;
import com.redhat.rhn.domain.action.server.ServerAction;
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

import com.suse.manager.webui.services.impl.SaltAPIService;
import com.suse.manager.webui.utils.YamlHelper;
import com.suse.manager.webui.utils.salt.ScheduleMetadata;
import com.suse.manager.webui.utils.salt.Zypper.ProductInfo;
import com.suse.manager.webui.utils.salt.custom.PkgProfileUpdateSlsResult;
import com.suse.manager.webui.utils.salt.events.JobReturnEvent;
import com.suse.manager.webui.utils.salt.results.CmdExecCodeAllResult;
import com.suse.manager.webui.utils.salt.results.StateApplyResult;
import com.suse.salt.netapi.calls.modules.Pkg;
import com.suse.salt.netapi.datatypes.target.MinionList;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.reflect.TypeToken;

import org.apache.log4j.Logger;

import java.time.Duration;
import java.time.Instant;
import java.util.Arrays;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.Stream;

/**
 * Handler class for {@link JobReturnEventMessage}.
 */
public class JobReturnEventMessageAction extends AbstractDatabaseAction {

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
            if (LOG.isDebugEnabled()) {
                LOG.debug("Matched salt job with action (id=" + id + ")");
            }

            // Lookup the corresponding action
            Optional<Action> action = Optional.ofNullable(ActionFactory.lookupById(id));
            if (action.isPresent()) {
                Optional<MinionServer> minionServerOpt = MinionServerFactory
                        .findByMinionId(jobReturnEvent.getMinionId());
                minionServerOpt.ifPresent(minionServer -> {
                    Optional<ServerAction> serverAction = action.get().getServerActions()
                            .stream()
                            .filter(sa -> sa.getServer().equals(minionServer)).findFirst();

                    // Delete schedule on the minion if we created it
                    jobReturnEvent.getData().getSchedule().ifPresent(scheduleName -> {
                        if (LOG.isDebugEnabled()) {
                            LOG.debug("Deleting schedule '" + scheduleName +
                                    "' from minion: " + minionServer.getMinionId());
                        }
                        SaltAPIService.INSTANCE.deleteSchedule(scheduleName,
                                new MinionList(jobReturnEvent.getMinionId()));
                    });

                    serverAction.ifPresent(sa -> {
                        if (LOG.isDebugEnabled()) {
                            LOG.debug("Updating action for server: " +
                                    minionServer.getId());
                        }
                        updateServerAction(sa, jobReturnEvent);
                        ActionFactory.save(sa);
                    });
                });
            }
            else {
                LOG.warn("Action referenced from Salt job was not found: " + id);
            }
        });

        if (packagesChanged(jobReturnEvent)) {
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
            MessageQueue.publish(new CheckinEventMessage(minion.get().getId()));
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
     * @param event the event to read the update data from
     */
    private void updateServerAction(ServerAction serverAction, JobReturnEvent event) {
        JobReturnEvent.Data eventData = event.getData();
        serverAction.setCompletionTime(new Date());

        final long retcode = eventData.getRetcode();

        // Set the result code defaulting to 0
        serverAction.setResultCode(retcode);

        // The final status of the action depends on "success" and "retcode"
        if (eventData.isSuccess() && retcode == 0) {
            serverAction.setStatus(ActionFactory.STATUS_COMPLETED);
        }
        else {
            serverAction.setStatus(ActionFactory.STATUS_FAILED);
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
                    .dump(eventData.getResult()).getBytes());

            // Create the result message depending on the action status
            String states = applyStatesAction.getDetails().getStates() != null ?
                    applyStatesAction.getDetails().getStates() : "highstate";
            String message = "Successfully applied state(s): " + states;
            if (serverAction.getStatus().equals(ActionFactory.STATUS_FAILED)) {
                message = "Failed to apply state(s): " + states;
            }
            serverAction.setResultMsg(message);
        }
        else if (action.getActionType().equals(ActionFactory.TYPE_SCRIPT_RUN)) {
            CmdExecCodeAllResult result = eventData.getResult(CmdExecCodeAllResult.class);
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
                serverAction.setResultMsg("Failed to execute script. [jid=" +
                        event.getJobId() + "]");
            }
            else {
                serverAction.setResultMsg("Script executed successfully. [jid=" +
                        event.getJobId() + "]");
            }
            StringBuilder sb = new StringBuilder();
            sb.append("Stderr: '");
            sb.append(result.getStderr());
            sb.append("\n\n");
            sb.append("Stdout: '");
            sb.append(result.getStdout());
            sb.append("\n");
            scriptResult.setOutput(sb.toString().getBytes());
        }
        else if (action.getActionType().equals(ActionFactory.TYPE_PACKAGES_REFRESH_LIST)) {
            if (serverAction.getStatus().equals(ActionFactory.STATUS_FAILED)) {
                serverAction.setResultMsg("Failure");
            }
            else {
                serverAction.setResultMsg("Success");
            }
            handlePackageProfileUpdate(serverAction,
                    eventData.getResult(PkgProfileUpdateSlsResult.class));
        }
        else {
            // Pretty-print the whole return map (or whatever fits into 1024 characters)
            Object returnObject = eventData.getResult();
            Gson gson = new GsonBuilder().setPrettyPrinting().create();
            String json = gson.toJson(returnObject);
            serverAction.setResultMsg(json.length() > 1024 ?
                    json.substring(0, 1023) : json);
        }
    }

    /**
     * Find the action id corresponding to a given job return event in the job metadata.
     *
     * @param event the job return event
     * @return the corresponding action id or empty optional
     */
    private Optional<Long> getActionId(JobReturnEvent event) {
        return event.getData().getMetadata(ScheduleMetadata.class)
                .map(ScheduleMetadata::getSumaActionId);
    }

    /**
     * Figure out if this {@link JobReturnEvent} could possibly have changed the list of
     * installed packages, i.e. if we should trigger a package list refresh.
     *
     * @param event the job return event
     * @return true if installed packages could have changed
     */
    private boolean packagesChanged(JobReturnEvent event) {
        String function = event.getData().getFun();
        switch (function) {
            case "pkg.install": return true;
            case "pkg.remove": return true;
            case "state.apply":
                Map<String, StateApplyResult<Object>> results = event.getData().getResult(
                        new TypeToken<Map<String, StateApplyResult<Object>>>() { });
                for (StateApplyResult<Object> result : results.values()) {
                    if (packageChangingModules.contains(result.getName())) {
                        return true;
                    }
                }
                return false;
            default: return false;
        }
    }

    /**
     * Perform the actual update of the database based on given event data.
     *
     * @param serverAction the current server action
     * @param eventData event data
     */
    private void handlePackageProfileUpdate(ServerAction serverAction,
            PkgProfileUpdateSlsResult result) {
        serverAction.getServer().asMinionServer().ifPresent(server -> {
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

            Optional<List<ProductInfo>> productInfo = Optional.of(
                    result.getListProducts().getChanges().getRet());
            getInstalledProducts(productInfo).ifPresent(server::setInstalledProducts);

            ServerFactory.save(server);
            if (LOG.isDebugEnabled()) {
                long duration = Duration.between(start, Instant.now()).getSeconds();
                LOG.debug("Package profile updated for minion: " + server.getMinionId() +
                        " (" + duration + " seconds)");
            }

            // Trigger update of errata cache for this server
            ErrataManager.insertErrataCacheTask(server);
        });
    }

    /**
     * Create a {@link InstalledPackage} object from package name and info and return it.
     *
     * @param name name of the package
     * @param info package info from salt
     * @param server server this package will be added to
     * @return the InstalledPackage object
     */
    private InstalledPackage createPackageFromSalt(
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
    private Optional<Set<InstalledProduct>> getInstalledProducts(
            Optional<List<ProductInfo>> productsIn) {
        return productsIn.map(result ->
            result.stream().flatMap(saltProduct -> {
                String name = saltProduct.getName();
                String version = saltProduct.getVersion();
                String release = saltProduct.getRelease();
                String arch = saltProduct.getArch();
                boolean isbase = saltProduct.getIsbase();

                Optional<SUSEProduct> product = Optional.ofNullable(
                    SUSEProductFactory.findSUSEProduct(
                            name, version, release, arch, true
                    )
                );
                if (!product.isPresent()) {
                    LOG.info(String.format("No product match found for: %s %s %s %s",
                            name, version, release, arch));
                }
                return product.map(prod -> {
                    InstalledProduct prd = new InstalledProduct();
                    prd.setName(prod.getName());
                    prd.setVersion(prod.getVersion());
                    prd.setRelease(prod.getRelease());
                    prd.setArch(prod.getArch());
                    prd.setBaseproduct(isbase);
                    return Stream.of(prd);
                }).orElseGet(Stream::empty);
            }).collect(Collectors.toSet())
        );
    }
}
