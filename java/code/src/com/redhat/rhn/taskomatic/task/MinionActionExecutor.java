/**
 * Copyright (c) 2017 SUSE LLC
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
package com.redhat.rhn.taskomatic.task;

import com.redhat.rhn.domain.action.Action;
import com.redhat.rhn.domain.action.ActionFactory;

import com.redhat.rhn.domain.action.ActionType;
import com.redhat.rhn.domain.action.channel.SubscribeChannelsAction;
import com.redhat.rhn.domain.action.server.ServerAction;
import com.redhat.rhn.domain.user.User;
import com.redhat.rhn.domain.user.UserFactory;
import com.redhat.rhn.manager.system.SystemManager;
import com.suse.manager.webui.services.SaltServerActionService;

import org.quartz.JobExecutionContext;

import java.time.Duration;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.util.Date;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

/**
 * Execute SUSE Manager actions via Salt.
 */
public class MinionActionExecutor extends RhnJavaJob {

    private static final int ACTION_DATABASE_GRACE_TIME = 10000;
    private static final long MAXIMUM_TIMEDELTA_FOR_SCHEDULED_ACTIONS = 24; // hours

    private SaltServerActionService saltServerActionService = SaltServerActionService.INSTANCE;

    /**
     * @param context the job execution context
     * @see org.quartz.Job#execute(JobExecutionContext)
     */
    @Override
    public void execute(JobExecutionContext context) {
        if (log.isDebugEnabled()) {
            log.debug("Start minion action executor");
        }

        // Measure time to calculate the total duration
        long start = System.currentTimeMillis();
        boolean forcePackageListRefresh = false;
        long actionId = context.getJobDetail()
                .getJobDataMap().getLongValueFromString("action_id");
        User user = Optional.ofNullable(context.getJobDetail().getJobDataMap().get("user_id"))
                .map(id -> Long.parseLong(id.toString()))
                .map(userId -> UserFactory.lookupById(userId))
                .orElse(null);

        boolean isStagingJob =
                context.getJobDetail().getJobDataMap().getBooleanValue("staging_job");
        Long stagingJobMinionServerId = null;
        if (isStagingJob) {
            stagingJobMinionServerId = context.getJobDetail().getJobDataMap()
                    .getLong("staging_job_minion_server_id");
        }
        else {
            forcePackageListRefresh = context.getJobDetail().getJobDataMap()
                    .getBooleanValue("force_pkg_list_refresh");
        }

        Action action = ActionFactory.lookupById(actionId);
        if (action == null) {
            // give a second chance, just in case this was scheduled immediately
            // and the scheduling transaction did not have the time to commit
            try {
                Thread.sleep(ACTION_DATABASE_GRACE_TIME);
            }
            catch (InterruptedException e) {
                // never happens
            }
            action = ActionFactory.lookupById(actionId);
        }

        if (action == null) {
            log.error("Action not found: " + actionId);
            return;
        }
        // calculate offset between scheduled time of
        // actions and (now)
        long timeDelta = Duration
                .between(ZonedDateTime.ofInstant(action.getEarliestAction().toInstant(),
                        ZoneId.systemDefault()), ZonedDateTime.now())
                .toHours();
        if (timeDelta >= MAXIMUM_TIMEDELTA_FOR_SCHEDULED_ACTIONS) {
            log.warn("Scheduled action " + action.getId() +
                    " was scheduled to be executed more than " +
                    MAXIMUM_TIMEDELTA_FOR_SCHEDULED_ACTIONS +
                    " hours ago. Skipping it.");
            return;
        }

        log.info("Executing action: " + actionId);
        handleTraditionalClients(user, action);
        saltServerActionService.execute(action, forcePackageListRefresh,
                isStagingJob, Optional.ofNullable(stagingJobMinionServerId));

        if (log.isDebugEnabled()) {
            long duration = System.currentTimeMillis() - start;
            log.debug("Total duration was: " + duration + " ms");
        }
    }

    // for traditional systems only the subscribe channels action will be handled here
    // all other actions are still handled like before
    private void handleTraditionalClients(User user, Action action) {
        List<ServerAction> serverActions = action.getServerActions().stream()
                // skip minions, they're handled by the job return event
                .filter(sa -> !sa.getServer().asMinionServer().isPresent())
                .collect(Collectors.toList());
        serverActions.forEach(sa -> {
            ActionType actionType = action.getActionType();
            if (ActionFactory.TYPE_SUBSCRIBE_CHANNELS.equals(actionType)) {
                SubscribeChannelsAction sca = (SubscribeChannelsAction)action;
                SystemManager.updateServerChannels(user, sa.getServer(),
                        Optional.ofNullable(sca.getDetails().getBaseChannel()),
                        sca.getDetails().getChannels(),
                        null);
            }
            else {
                log.warn("Action type " +
                        (actionType != null ? actionType.getName() : "") +
                        " is not supported by " + this.getClass().getSimpleName());
                return;
            }
            sa.setStatus(ActionFactory.STATUS_COMPLETED);
            sa.setCompletionTime(new Date());
            sa.setResultCode(0L);
            sa.setResultMsg("Successfully changed channels");
        });
    }

    /**
     * Needed only for unit tests.
     * @param saltServerActionServiceIn to set
     */
    public void setSaltServerActionService(SaltServerActionService saltServerActionServiceIn) {
        this.saltServerActionService = saltServerActionServiceIn;
    }
}
