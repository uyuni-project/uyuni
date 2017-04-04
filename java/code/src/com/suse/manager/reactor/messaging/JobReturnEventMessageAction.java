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
import com.redhat.rhn.domain.action.server.ServerAction;
import com.redhat.rhn.domain.server.MinionServerFactory;
import com.redhat.rhn.frontend.events.AbstractDatabaseAction;
import com.redhat.rhn.manager.action.ActionManager;
import com.redhat.rhn.taskomatic.TaskomaticApiException;
import com.redhat.rhn.domain.server.MinionServer;

import com.suse.manager.utils.SaltUtils;
import com.suse.manager.webui.services.SaltServerActionService;
import com.suse.manager.webui.utils.salt.custom.ScheduleMetadata;
import com.suse.salt.netapi.event.JobReturnEvent;

import org.apache.log4j.Logger;

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


                // FIXME: This is a hack and should not be considered the final solution
                if (action.get().getActionType().equals(ActionFactory.TYPE_DIST_UPGRADE) &&
                        function.equals("test.ping")) {
                    SaltServerActionService.INSTANCE.execute(action.get(), false, false,
                            Optional.empty());
                }
                else {
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
                                SaltUtils.INSTANCE.updateServerAction(sa,
                                        jobReturnEvent.getData().getRetcode(),
                                        jobReturnEvent.getData().isSuccess(),
                                        jobReturnEvent.getJobId(), jobResult.get(),
                                        jobReturnEvent.getData().getFun());
                            }
                            catch (Exception e) {
                                sa.setStatus(ActionFactory.STATUS_FAILED);
                                sa.setResultMsg("An unexpected error has occured. " +
                                        "Please check the server logs.");
                                // We don't actually want to catch any exceptions here
                                throw e;
                            }
                            finally {
                                ActionFactory.save(sa);
                            }
                        });
                    });
                }
            }
            else {
                LOG.warn("Action referenced from Salt job was not found: " + id);
            }
        });

        // Schedule a package list refresh if either requested or detected as necessary
        if (
            forcePackageListRefresh(jobReturnEvent) ||
            SaltUtils.INSTANCE.shouldRefreshPackageList(function, jobResult)
        ) {
            MinionServerFactory
                    .findByMinionId(jobReturnEvent.getMinionId())
                    .ifPresent(minionServer -> {
                try {
                    ActionManager.schedulePackageRefresh(minionServer.getOrg(),
                        minionServer);
                }
                catch (TaskomaticApiException e) {
                    LOG.error("Could not schedule package refresh for minion: " +
                        minionServer.getMinionId());
                    throw new RuntimeException(e);
                }
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
