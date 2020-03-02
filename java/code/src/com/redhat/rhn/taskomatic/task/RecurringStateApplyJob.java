/**
 * Copyright (c) 2020 SUSE LLC
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

import com.redhat.rhn.domain.recurringactions.RecurringAction;
import com.redhat.rhn.domain.recurringactions.RecurringActionFactory;
import com.redhat.rhn.manager.action.ActionChainManager;
import com.redhat.rhn.taskomatic.TaskoXmlRpcHandler;
import com.redhat.rhn.taskomatic.TaskomaticApiException;

import org.quartz.JobExecutionContext;
import org.quartz.JobExecutionException;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

/**
 * Used to run a scheduled Recurring Highstate Apply action
 */
public class RecurringStateApplyJob extends RhnJavaJob {

    /**
     * Schedule highstate application.
     *
     * If the {@link RecurringAction} data is not found, clean the schedule.
     *
     * {@inheritDoc}
     */
    public void execute(JobExecutionContext context) throws JobExecutionException {
        String scheduleName = context.getJobDetail().getKey().getName();
        Optional<RecurringAction> recurringAction = RecurringActionFactory.lookupByJobName(scheduleName);

        recurringAction.ifPresentOrElse(
                action ->  {
                    if (action.isActive()) {
                        scheduleAction(context, action);
                    }
                    else {
                        log.debug(String.format("Action %s not active, skipping", action));
                    }
                },
                () -> cleanSchedule(scheduleName)
        );
    }

    private void scheduleAction(JobExecutionContext context, RecurringAction action) {
        List<Long> minionIds = action.computeMinions().stream()
                .map(m -> m.getId())
                .collect(Collectors.toList());
        try {
            ActionChainManager.scheduleApplyStates(action.getCreator(), minionIds,
                    Optional.of(action.isTestMode()), context.getFireTime(), null);
        }
        catch (TaskomaticApiException e) {
            log.error("Error scheduling states application for recurring action " + action, e);
        }
    }

    private void cleanSchedule(String scheduleName) {
        log.warn(String.format("Can't find a recurring action data for schedule '%s'. " +
                "Cleaning the schedule!", scheduleName));
        int result = new TaskoXmlRpcHandler().unscheduleBunch(null, scheduleName);
        if (result != 1) {
            log.error(String.format("Error cleaning schedule '%s'", scheduleName));
        }
    }
}
