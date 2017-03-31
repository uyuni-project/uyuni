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

import com.suse.manager.webui.services.SaltServerActionService;

import org.quartz.JobExecutionContext;

import java.math.BigDecimal;
import java.time.Duration;
import java.time.ZoneId;
import java.time.ZonedDateTime;

/**
 * Execute SUSE Manager actions via Salt.
 */
public class MinionActionExecutor extends RhnJavaJob {

    private static final int ACTION_DATABASE_GRACE_TIME = 10000;
    private static final long MAXIMUM_TIMEDELTA_FOR_SCHEDULED_ACTIONS = 10; // minutes

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

        long actionId = context.getJobDetail()
                .getJobDataMap().getLongValueFromString("action_id");
        boolean forcePackageListRefresh = context.getJobDetail()
                .getJobDataMap().getBooleanValue("force_pkg_list_refresh");
        boolean isStagingJob =
                context.getJobDetail().getJobDataMap().getBooleanValue("staging_job");
        Long stagingJobMinionServerId = null;
        if (isStagingJob) {
            stagingJobMinionServerId = context.getJobDetail().getJobDataMap()
                    .getLong("staging_job_minion_server_id");
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
                .toMinutes();
        if (timeDelta >= MAXIMUM_TIMEDELTA_FOR_SCHEDULED_ACTIONS) {
            log.warn("Scheduled action " + action.getId() +
                    " was scheduled to be executed more than " +
                    MAXIMUM_TIMEDELTA_FOR_SCHEDULED_ACTIONS +
                    " minute(s) ago. Skipping it.");
            return;
        }

        log.info("Executing action: " + actionId);
        SaltServerActionService.INSTANCE.execute(action, forcePackageListRefresh,
                isStagingJob, stagingJobMinionServerId);

        if (log.isDebugEnabled()) {
            long duration = System.currentTimeMillis() - start;
            log.debug("Total duration was: " + duration + " ms");
        }
    }
}
