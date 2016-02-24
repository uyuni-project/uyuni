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
import com.redhat.rhn.domain.action.script.ScriptResult;
import com.redhat.rhn.domain.action.script.ScriptRunAction;
import com.redhat.rhn.domain.action.server.ServerAction;
import com.redhat.rhn.domain.server.MinionServerFactory;
import com.redhat.rhn.frontend.events.AbstractDatabaseAction;
import com.redhat.rhn.domain.server.MinionServer;

import com.suse.manager.webui.services.impl.SaltAPIService;

import com.suse.salt.netapi.datatypes.target.MinionList;
import com.suse.salt.netapi.event.JobReturnEvent;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

import org.apache.log4j.Logger;

import java.util.Collections;
import java.util.Date;
import java.util.Map;
import java.util.Optional;

/**
 * Handler class for {@link JobReturnEventMessage}.
 */
public class JobReturnEventMessageAction extends AbstractDatabaseAction {

    /* Logger for this class */
    private static final Logger LOG = Logger.getLogger(JobReturnEventMessageAction.class);

    @Override
    public void doExecute(EventMessage msg) {
        JobReturnEventMessage jobReturnEventMessage = (JobReturnEventMessage) msg;
        JobReturnEvent jobReturnEvent = jobReturnEventMessage.getJobReturnEvent();

        // React according to the function the minion ran
        String function = (String) jobReturnEvent.getData().get("fun");

        if (LOG.isDebugEnabled()) {
            LOG.debug("Job return event for minion: " +
                    jobReturnEvent.getMinionId() + "/" + jobReturnEvent.getJobId() +
                    " (" + function + ")");
        }

        // Adjust action status if the job was scheduled by us
        getActionId(jobReturnEvent).ifPresent(id -> {
            if (LOG.isDebugEnabled()) {
                LOG.debug("Matched salt job with action (id=" + id + ")");
            }

            Action action = ActionFactory.lookupById(id);
            Optional<MinionServer> minionServerOpt = MinionServerFactory
                    .findByMinionId(jobReturnEvent.getMinionId());
            minionServerOpt.ifPresent(minionServer -> {
                Optional<ServerAction> serverAction = action.getServerActions().stream()
                        .filter(sa -> sa.getServer().equals(minionServer)).findFirst();

                // Delete schedule on the minion if we created it
                if (jobReturnEvent.getData().containsKey("schedule")) {
                    String scheduleName = (String) jobReturnEvent.getData().get("schedule");
                    if (LOG.isDebugEnabled()) {
                        LOG.debug("Deleting schedule '" + scheduleName +
                                "' from minion: " + minionServer.getMinionId());
                    }
                    SaltAPIService.INSTANCE.deleteSchedule(scheduleName,
                        new MinionList(jobReturnEvent.getMinionId()));
                }

                serverAction.ifPresent(sa -> {
                    if (LOG.isDebugEnabled()) {
                        LOG.debug("Updating action for server: " + minionServer.getId());
                    }
                    updateServerAction(sa, jobReturnEvent);
                    ActionFactory.save(sa);
                });
            });
        });

        if (packagesChanged(jobReturnEvent)) {
            MinionServerFactory
                    .findByMinionId(jobReturnEvent.getMinionId())
                    .ifPresent(minionServer -> {
                MessageQueue.publish(
                        new UpdatePackageProfileEventMessage(minionServer.getId()));
            });
        }

        // for all jobs, update minion last checkin
        MinionServerFactory
                .findByMinionId(jobReturnEvent.getMinionId())
                .ifPresent(minionServer -> {
                    MessageQueue.publish
                            (new CheckinEventMessage(minionServer.getId()));
                });
    }

    /**
     * Update a given server action based on data from the corresponding job return event.
     *
     * @param serverAction the server action to update
     * @param event the event to read the update data from
     */
    private void updateServerAction(ServerAction serverAction, JobReturnEvent event) {
        Map<String, Object> eventData = event.getData();
        serverAction.setCompletionTime(new Date());

        // Set the result code defaulting to 0
        long retcode = ((Double) eventData.getOrDefault("retcode", 0.0)).longValue();
        serverAction.setResultCode(retcode);

        // The final status of the action depends on "success" and "retcode"
        if ((Boolean) eventData.getOrDefault("success", false) && retcode == 0) {
            serverAction.setStatus(ActionFactory.STATUS_COMPLETED);
        }
        else {
            serverAction.setStatus(ActionFactory.STATUS_FAILED);
        }

        @SuppressWarnings("unchecked")
        Map<String, Object> eventDataReturnMap = (Map<String, Object>) eventData
                .getOrDefault("return", Collections.EMPTY_MAP);
        Action action = serverAction.getParentAction();
        if (action.getActionType().equals(ActionFactory.TYPE_SCRIPT_RUN)) {
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
                String stderr = (String) eventDataReturnMap.getOrDefault("stderr",
                        "stderr is not available.");
                scriptResult.setOutput(stderr.getBytes());
            }
            else {
                serverAction.setResultMsg("Script executed successfully. [jid=" +
                        event.getJobId() + "]");
                String stdout = (String) eventDataReturnMap.getOrDefault("stdout",
                        "stdout is not available.");
                scriptResult.setOutput(stdout.getBytes());
            }
        }
        else {
            // Pretty-print the whole return map (or whatever fits into 1024 characters)
            Gson gson = new GsonBuilder().setPrettyPrinting().create();
            String json = gson.toJson(eventDataReturnMap);
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
    @SuppressWarnings("unchecked")
    private Optional<Long> getActionId(JobReturnEvent event) {
        Map<String, Object> metadata = (Map<String, Object>) event.getData()
                .getOrDefault("metadata", Collections.EMPTY_MAP);
        Optional<Long> actionId = Optional.empty();
        Double actionIdDouble = (Double) metadata.get("suma-action-id");
        if (actionIdDouble != null) {
            actionId = Optional.ofNullable(actionIdDouble.longValue());
        }
        return actionId;
    }

    private boolean packagesChanged(JobReturnEvent event) {
        String function = (String) event.getData().get("fun");
        // Add more events that change packages here
        // This can also be further optimized by inspecting the event contents
        switch (function) {
            case "pkg.install": return true;
            case "pkg.remove": return true;
            case "state.apply": return true;
            default: return false;
        }
    }
}
