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
import com.redhat.rhn.domain.recurringactions.type.RecurringPlaybook;
import com.redhat.rhn.domain.recurringactions.type.RecurringScapPolicy;
import com.redhat.rhn.domain.recurringactions.type.RecurringState;
import com.redhat.rhn.manager.action.ActionChainManager;
import com.redhat.rhn.manager.action.ActionManager;
import com.redhat.rhn.manager.system.AnsibleManager;
import com.redhat.rhn.taskomatic.TaskoXmlRpcHandler;
import com.redhat.rhn.taskomatic.TaskomaticApi;
import com.redhat.rhn.taskomatic.TaskomaticApiException;

import com.suse.manager.maintenance.MaintenanceManager;

import org.quartz.JobExecutionContext;

import java.util.Comparator;
import java.util.HashSet;
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
            if (actionType instanceof RecurringHighstate highstateType) {
                ActionChainManager.scheduleApplyStates(action.getCreator(), minionIds,
                        Optional.of(highstateType.isTestMode()), context.getFireTime(), null);
            }
            else if (actionType instanceof RecurringState stateType) {
                Set<RecurringStateConfig> configs = stateType.getStateConfig();
                List<String> mods = configs.stream()
                        .sorted(Comparator.comparingLong(RecurringStateConfig::getPosition))
                        .map(RecurringStateConfig::getStateName)
                        .collect(Collectors.toList());
                Action a = ActionManager.scheduleApplyStates(action.getCreator(),
                        minionIds, mods,
                        Optional.of(Map.of("rec_id", action.getId().toString())),
                        context.getFireTime(),
                        Optional.of(stateType.isTestMode()),
                        true);
                ActionFactory.save(a);
                new TaskomaticApi().scheduleActionExecution(a);
            }
            else if (actionType instanceof RecurringPlaybook playbookType) {
                AnsibleManager.schedulePlaybook(
                        playbookType.getPlaybookPath(),
                        playbookType.getInventoryPath(),
                        minionIds.get(0),
                        playbookType.isTestMode(),
                        playbookType.isFlushCache(),
                        playbookType.getExtraVarsContents(),
                        context.getFireTime(),
                        Optional.empty(),
                        action.getCreator()
                );
            }
            else if (actionType instanceof RecurringScapPolicy scapPolicyType) {
                if (!action.getCreator().getBetaFeaturesEnabled()) {
                    log.warn("RecurringScapPolicy {} execution skipped because creator does not have beta features enabled",
                            action.getId());
                }
                else if (scapPolicyType.getScapPolicy() != null) {
                    // Build parameters for SCAP scan
                    String parameters = "--profile " + scapPolicyType.getScapPolicy().getXccdfProfileId();
                    if (scapPolicyType.getScapPolicy().getTailoringFile() != null &&
                            scapPolicyType.getScapPolicy().getTailoringProfileId() != null) {
                        parameters += " --tailoring-file " + scapPolicyType.getScapPolicy().getTailoringFile().getFileName() +
                                " --tailoring-profile-id " + scapPolicyType.getScapPolicy().getTailoringProfileId();
                    }
                    
                    ActionManager.scheduleXccdfEval(
                            action.getCreator(),
                            new HashSet<>(minionIds),
                            scapPolicyType.getScapPolicy().getDataStreamName(),
                            parameters,
                            null, // ovalFiles
                            context.getFireTime(),
                            scapPolicyType.getScapPolicy().getId(),
                            true // recurring=true
                    );
                }
                else {
                    log.warn("RecurringScapPolicy {} has no SCAP policy assigned, skipping execution",
                            action.getId());
                }
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
