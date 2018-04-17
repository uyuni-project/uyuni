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
import com.google.gson.JsonParseException;
import com.google.gson.JsonSyntaxException;

import com.redhat.rhn.common.messaging.EventMessage;
import com.redhat.rhn.domain.action.Action;
import com.redhat.rhn.domain.action.ActionFactory;
import com.redhat.rhn.domain.action.server.ServerAction;
import com.redhat.rhn.domain.server.MinionServerFactory;
import com.redhat.rhn.domain.server.VirtualInstance;
import com.redhat.rhn.frontend.events.AbstractDatabaseAction;
import com.redhat.rhn.domain.server.MinionServer;

import com.redhat.rhn.manager.action.ActionManager;
import com.redhat.rhn.taskomatic.TaskomaticApiException;

import com.suse.manager.reactor.hardware.CpuArchUtil;
import com.suse.manager.utils.SaltUtils;
import com.suse.manager.utils.SaltUtils.PackageChangeOutcome;
import com.suse.manager.webui.utils.salt.custom.ScheduleMetadata;
import com.suse.salt.netapi.event.JobReturnEvent;

import org.apache.log4j.Logger;

import java.util.Date;
import java.util.Optional;

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

    /* Logger for this class */
    private static final Logger LOG = Logger.getLogger(JobReturnEventMessageAction.class);

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

        // Prepare the job result as a json element
        Optional<JsonElement> jobResult = eventToJson(jobReturnEvent);

        // Adjust action status if the job was scheduled by us
        Optional<Long> actionId = getActionId(jobReturnEvent);
        actionId.ifPresent(id -> {

            // Lookup the corresponding action
            Optional<Action> action = Optional.ofNullable(ActionFactory.lookupById(id));
            if (action.isPresent()) {

                if (LOG.isDebugEnabled()) {
                    LOG.debug("Matched salt job with action (id=" + id + ")");
                }

                Optional<MinionServer> minionServerOpt = MinionServerFactory
                        .findByMinionId(jobReturnEvent.getMinionId());
                minionServerOpt.ifPresent(minionServer -> {
                    Optional<ServerAction> serverAction = action.get()
                            .getServerActions()
                            .stream()
                            .filter(sa -> sa.getServer().equals(minionServer)).findFirst();


                    serverAction.ifPresent(sa -> {
                        if (LOG.isDebugEnabled()) {
                            LOG.debug("Updating action for server: " +
                                    minionServer.getId());
                        }
                        try {
                            // Avoid setting reboot action to COMPLETED. We wait until next "minion/start/event"
                            if (action.get().getActionType().equals(ActionFactory.TYPE_REBOOT) &&
                                    jobReturnEvent.getData().isSuccess() &&
                                    jobReturnEvent.getData().getRetcode() == 0) {
                                return;
                            }
                            SaltUtils.INSTANCE.updateServerAction(sa,
                                    jobReturnEvent.getData().getRetcode(),
                                    jobReturnEvent.getData().isSuccess(),
                                    jobReturnEvent.getJobId(), jobResult.get(),
                                    jobReturnEvent.getData().getFun());
                            ActionFactory.save(sa);
                        }
                        catch (Exception e) {
                            LOG.error("Error processing Salt job return", e);
                            // DB exceptions cause the transaction to go into rollback-only
                            // state. We need to rollback this transaction first.
                            ActionFactory.rollbackTransaction();

                            sa.setCompletionTime(new Date());
                            sa.setStatus(ActionFactory.STATUS_FAILED);
                            sa.setResultCode(-1L);
                            sa.setResultMsg("An unexpected error has occured. " +
                                    "Please check the server logs.");

                            ActionFactory.save(sa);
                            // When we throw the exception again, the current transaction
                            // will be set to rollback-only, so we explicitly commit the
                            // transaction here
                            ActionFactory.commitTransaction();

                            // We don't actually want to catch any exceptions
                            throw e;
                        }
                    });
                });
            }
            else {
                LOG.warn("Action referenced from Salt job was not found: " + id);
            }
        });

        MinionServerFactory.findByMinionId(jobReturnEvent.getMinionId())
            .ifPresent(minionServer -> {
                jobResult.ifPresent(result -> {
                    try {
                        if (forcePackageListRefresh(jobReturnEvent) ||
                            SaltUtils.handlePackageChanges(function, result,
                                    minionServer) ==
                                PackageChangeOutcome.NEEDS_REFRESHING) {
                                ActionManager.schedulePackageRefresh(minionServer.getOrg(),
                                        minionServer);
                            }
                        }
                    catch (JsonParseException e) {
                        LOG.warn("Could not determine if packages changed " +
                                "in call to " + function +
                                " because of a parse error");
                        LOG.warn(e);
                    }
                    catch (TaskomaticApiException e) {
                        LOG.error(e);
                    }
                });
            });

        // For all jobs: update minion last checkin
        Optional<MinionServer> minion = MinionServerFactory.findByMinionId(
                jobReturnEvent.getMinionId());
        if (minion.isPresent()) {
            MinionServer m = minion.get();
            m.updateServerInfo();
            // for s390 update the host as well
            if (m.getCpu() != null &&
                CpuArchUtil.isS390(m.getCpu().getArch().getLabel())) {
                VirtualInstance virtInstance = m.getVirtualInstance();
                if (virtInstance != null && virtInstance.getHostSystem() != null) {
                    virtInstance.getHostSystem().updateServerInfo();
                }
            }
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
     * {@inheritDoc}
     */
    @Override
    public boolean canRunConcurrently() {
        return true;
    }
}
