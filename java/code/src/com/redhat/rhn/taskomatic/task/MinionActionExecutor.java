/*
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

import com.redhat.rhn.GlobalInstanceHolder;
import com.redhat.rhn.common.localization.LocalizationService;
import com.redhat.rhn.domain.action.Action;
import com.redhat.rhn.domain.action.ActionFactory;
import com.redhat.rhn.domain.action.channel.SubscribeChannelsAction;
import com.redhat.rhn.domain.action.server.ServerAction;
import com.redhat.rhn.domain.server.MinionServerFactory;
import com.redhat.rhn.domain.user.User;
import com.redhat.rhn.domain.user.UserFactory;
import com.redhat.rhn.manager.system.SystemManager;

import com.suse.manager.webui.services.SaltServerActionService;

import org.apache.commons.collections.CollectionUtils;
import org.quartz.JobExecutionContext;

import java.time.Duration;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.util.Date;
import java.util.List;
import java.util.Optional;

/**
 * Execute SUSE Manager actions via Salt.
 */
public class MinionActionExecutor extends RhnJavaJob {

    public static final int ACTION_DATABASE_GRACE_TIME = 600_000;
    public static final int ACTION_DATABASE_POLL_TIME = 100;
    public static final long MAXIMUM_TIMEDELTA_FOR_SCHEDULED_ACTIONS = 24; // hours
    private static final LocalizationService LOCALIZATION = LocalizationService.getInstance();

    private final SaltServerActionService saltServerActionService;

    /**
     * Default constructor.
     */
    public MinionActionExecutor() {
        this(GlobalInstanceHolder.SALT_SERVER_ACTION_SERVICE);
    }

    /**
     * Constructs an instance specifying the {@link SaltServerActionService}. Meant to be used only for unit test.
     * @param saltServerActionServiceIn the salt service
     */
    public MinionActionExecutor(SaltServerActionService saltServerActionServiceIn) {
        this.saltServerActionService = saltServerActionServiceIn;
    }

    @Override
    public int getDefaultRescheduleTime() {
        return 10;
    }

    @Override
    public String getConfigNamespace() {
        return "minion_action_executor";
    }

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
                .map(UserFactory::lookupById)
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

        // HACK: it is possible that this Taskomatic task triggered before the corresponding Action was really
        // COMMITted in the database. Wait for some minutes checking if it appears
        int waitedTime = 0;
        while (countQueuedServerActions(action) == 0 && waitedTime < ACTION_DATABASE_GRACE_TIME) {
            action = ActionFactory.lookupById(actionId);
            try {
                Thread.sleep(ACTION_DATABASE_POLL_TIME);
            }
            catch (InterruptedException e) {
                // never happens
                Thread.currentThread().interrupt();
            }
            waitedTime += ACTION_DATABASE_POLL_TIME;
        }

        if (action == null) {
            log.error("Action not found: {}", actionId);
            return;
        }

        if (countQueuedServerActions(action) == 0) {
            log.error("Action with id={} has no server with status QUEUED", actionId);
            return;
        }

        log.debug("Action {} found after: {}ms", actionId, waitedTime);

        // calculate offset between scheduled time of
        // actions and (now)
        ZonedDateTime earliestInstant = ZonedDateTime.ofInstant(action.getEarliestAction().toInstant(),
            ZoneId.systemDefault());

        long timeDelta = Duration.between(earliestInstant, ZonedDateTime.now()).toHours();
        if (timeDelta >= MAXIMUM_TIMEDELTA_FOR_SCHEDULED_ACTIONS) {
            log.warn("Scheduled action {} was scheduled to be executed more than {} hours ago. Skipping it.",
                    action.getId(), MAXIMUM_TIMEDELTA_FOR_SCHEDULED_ACTIONS);

            ActionFactory.rejectScheduledActions(List.of(actionId),
                LOCALIZATION.getMessage("task.action.rejection.reason", MAXIMUM_TIMEDELTA_FOR_SCHEDULED_ACTIONS));

            return;
        }

        log.info("Executing action: {}", actionId);

        if (ActionFactory.TYPE_SUBSCRIBE_CHANNELS.equals(action.getActionType())) {
            handleTraditionalClients(user, (SubscribeChannelsAction) action);
        }

        saltServerActionService.execute(action, forcePackageListRefresh,
                isStagingJob, Optional.ofNullable(stagingJobMinionServerId));

        if (log.isDebugEnabled()) {
            long duration = System.currentTimeMillis() - start;
            log.debug("Total duration was: {} ms", duration);
        }
    }

    // for traditional systems only the subscribe channels action will be handled here
    // all other actions are still handled like before
    private void handleTraditionalClients(User user, SubscribeChannelsAction sca) {
        List<ServerAction> serverActions = MinionServerFactory.findTradClientServerActions(sca.getId());

        serverActions.forEach(sa -> {
            SystemManager.updateServerChannels(user, sa.getServer(),
                    Optional.ofNullable(sca.getDetails().getBaseChannel()),
                    sca.getDetails().getChannels());
            sa.setStatus(ActionFactory.STATUS_COMPLETED);
            sa.setCompletionTime(new Date());
            sa.setResultCode(0L);
            sa.setResultMsg("Successfully changed channels");
        });
    }

    private long countQueuedServerActions(Action action) {
        if (action == null || CollectionUtils.isEmpty(action.getServerActions())) {
            return 0;
        }

        return action.getServerActions()
                     .stream()
                     .filter(serverAction -> ActionFactory.STATUS_QUEUED.equals(serverAction.getStatus()))
                     .count();
    }
}
