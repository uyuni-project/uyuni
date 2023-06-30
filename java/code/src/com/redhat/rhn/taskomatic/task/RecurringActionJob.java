/*
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

import com.redhat.rhn.domain.action.Action;
import com.redhat.rhn.domain.action.ActionFactory;
import com.redhat.rhn.domain.recurringactions.RecurringAction;
import com.redhat.rhn.domain.recurringactions.RecurringActionFactory;
import com.redhat.rhn.domain.recurringactions.state.RecurringStateConfig;
import com.redhat.rhn.domain.recurringactions.type.RecurringActionType;
import com.redhat.rhn.domain.recurringactions.type.RecurringHighstate;
import com.redhat.rhn.domain.recurringactions.type.RecurringState;
import com.redhat.rhn.manager.action.ActionChainManager;
import com.redhat.rhn.manager.action.ActionManager;
import com.redhat.rhn.taskomatic.TaskoXmlRpcHandler;
import com.redhat.rhn.taskomatic.TaskomaticApi;
import com.redhat.rhn.taskomatic.TaskomaticApiException;

import com.suse.manager.maintenance.MaintenanceManager;

import org.quartz.JobExecutionContext;

import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * Used to schedule actions from Recurring action schedules
 */
public class RecurringActionJob extends RhnJavaJob {

    private static MaintenanceManager maintenanceManager = new MaintenanceManager();

    @Override
    public String getConfigNamespace() {
        return "recurring_state_apply";
    }

    /**
     * Schedule highstate application.
     *
     * If the {@link RecurringAction} data is not found, clean the schedule.
     *
     * {@inheritDoc}
     */
    @Override
    public void execute(JobExecutionContext context) {
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
        List<Long> minionIds = maintenanceManager.systemIdsMaintenanceMode(action.computeMinions());

        try {
            RecurringActionType actionType = action.getRecurringActionType();
            if (actionType instanceof RecurringHighstate) {
                ActionChainManager.scheduleApplyStates(action.getCreator(), minionIds,
                        Optional.of(((RecurringHighstate) actionType).isTestMode()), context.getFireTime(), null);
            }
            else if (actionType instanceof RecurringState) {
                Set<RecurringStateConfig> configs = ((RecurringState) action.getRecurringActionType()).getStateConfig();
                List<String> mods = configs.stream().map(RecurringStateConfig::getStateName)
                        .collect(Collectors.toList());
                Action a = ActionManager.scheduleApplyStates(action.getCreator(),
                        minionIds, mods,
                        Optional.of(Map.of("rec_id", action.getId().toString())),
                        context.getFireTime(),
                        Optional.of(((RecurringState) action.getRecurringActionType()).isTestMode()),
                        true);
                ActionFactory.save(a);
                new TaskomaticApi().scheduleActionExecution(a);
            }
        }
        catch (TaskomaticApiException e) {
            log.error("Error scheduling states application for recurring action {}", action, e);
        }
    }

    private void cleanSchedule(String scheduleName) {
        log.warn("Can't find a recurring action data for schedule '{}'. Cleaning the schedule!", scheduleName);
        int result = new TaskoXmlRpcHandler().unscheduleBunch(null, scheduleName);
        if (result != 1) {
            log.error("Error cleaning schedule '{}'", scheduleName);
        }
    }
}
