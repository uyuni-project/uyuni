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
package com.redhat.rhn.manager.action;

import static java.time.ZonedDateTime.now;
import static java.time.temporal.ChronoUnit.SECONDS;

import com.redhat.rhn.common.conf.ConfigDefaults;
import com.redhat.rhn.domain.action.Action;
import com.redhat.rhn.domain.server.MinionServerFactory;
import com.redhat.rhn.domain.server.MinionSummary;
import com.redhat.rhn.domain.user.User;
import com.redhat.rhn.taskomatic.TaskomaticApi;
import com.redhat.rhn.taskomatic.TaskomaticApiException;

import org.apache.log4j.Logger;

import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * Utility class for Actions related to minions.
 */
public class MinionActionManager {

    private static Logger log = Logger.getLogger(MinionActionManager.class);
    private static TaskomaticApi taskomaticApi = new TaskomaticApi();

    private MinionActionManager() {
    }

    /**
     * Set the {@link TaskomaticApi} instance to use. Only needed for unit tests.
     * @param taskomaticApiIn the {@link TaskomaticApi}
     */
    public static void setTaskomaticApi(TaskomaticApi taskomaticApiIn) {
        taskomaticApi = taskomaticApiIn;
    }

    /**
     * Schedule staging jobs for minions, if:
     * - org has enabled staging content
     * - action is either:
     *   - package install/update
     *   - patch install
     *
     * Staging job will be scheduled per-minion and at a random point in the time in
     * the proper range.
     *
     * @param actions List of actions. related action already scheduled
     * @param user user that started the action
     * @throws TaskomaticApiException in case of failure of the scheduled staging job
     * @return A list containing the schedule time(s) for staging job(s)
     */
    public static Map<Long, Map<Long, ZonedDateTime>> scheduleStagingJobsForMinions(List<Action> actions, User user)
            throws TaskomaticApiException {
        Map<Long, Map<Long, ZonedDateTime>> scheduleActionsData = new HashMap<>();
        for (Action action: actions) {
            Map<Long, ZonedDateTime> scheduleActionData = computeStagingTimestamps(action, user);
            if (!scheduleActionData.isEmpty()) {
                scheduleActionsData.put(action.getId(), scheduleActionData);
            }
        }
        // Schedule the taskomatic actions
        if (!scheduleActionsData.isEmpty()) {
            taskomaticApi.scheduleStagingJobs(scheduleActionsData);
        }
        return scheduleActionsData;
    }
    /**
     * Prepare data to Schedule staging jobs for minions, if:
     * - org has enabled staging content
     * - action is either:
     *   - package install/update
     *   - patch install
     *
     * @param action related action already scheduled
     * @param user user that started the action
     * @return A Map containing the schedule time for each minion involved in this action
     */
    private static Map<Long, ZonedDateTime> computeStagingTimestamps(Action action, User user) {

            if (user.getOrg().getOrgConfig().isStagingContentEnabled()) {

             List<MinionSummary> minionSummaries = MinionServerFactory.findMinionSummaries(action.getId());

            ZonedDateTime earliestAction =
                    action.getEarliestAction().toInstant().atZone(ZoneId.systemDefault());

            if (earliestAction.isAfter(now()) && !minionSummaries.isEmpty()) {

                final float saltContentStagingAdvance =
                        ConfigDefaults.get().getSaltContentStagingAdvance();
                final float saltContentStagingWindow =
                        ConfigDefaults.get().getSaltContentStagingWindow();
                final ZonedDateTime now = now();

                ZonedDateTime potentialStagingWindowStartTime = earliestAction
                        .minus((long) (saltContentStagingAdvance * 3600), SECONDS);

                ZonedDateTime potentialStagingWindowEndTime = potentialStagingWindowStartTime
                        .plus((long) (saltContentStagingWindow * 3600), SECONDS);

                ZonedDateTime stagingWindowStartTime;
                if (now.isAfter(potentialStagingWindowStartTime) &&
                        potentialStagingWindowEndTime.isAfter(now)) {
                    log.warn("Scheduled staging window began before now: adjusting start to now (" + now + ")");
                    stagingWindowStartTime = now;
                }
                else {
                    stagingWindowStartTime = potentialStagingWindowStartTime;
                }

                ZonedDateTime stagingWindowEndTime;
                if (potentialStagingWindowEndTime.isAfter(earliestAction)) {
                    log.warn("Ignoring salt_content_staging_window parameter: expected staging window end time is " +
                            "after action execution!");
                    log.warn("Expected staging window end time: " + potentialStagingWindowEndTime);
                    log.warn("Adjusting  window end time to earliest action execution: " + earliestAction);
                    stagingWindowEndTime = earliestAction;
                }
                else {
                    stagingWindowEndTime = potentialStagingWindowEndTime;
                }

                boolean stagingWindowIsAlreadyEnded = stagingWindowEndTime.isBefore(now);
                boolean stagingWindowStartIsBeforeAction = stagingWindowStartTime.isBefore(earliestAction);

                if (!stagingWindowIsAlreadyEnded && stagingWindowStartIsBeforeAction &&
                        (stagingWindowEndTime.isBefore(earliestAction) ||
                                stagingWindowEndTime.isEqual(earliestAction))) {
                    Map<Long, ZonedDateTime>  scheduleActionData = minionSummaries.stream()
                            .collect(Collectors.toMap(MinionSummary::getServerId, s -> {
                                ZonedDateTime stagingTime = stagingWindowStartTime.plus(
                                                    (long) (SECONDS.between(stagingWindowStartTime,
                                                            stagingWindowEndTime) * Math.random()), SECONDS);
                                return stagingTime;
                            }));
                        if (log.isDebugEnabled()) {
                            scheduleActionData.forEach((id, stagingTime)-> log.info("Detected install/update action " +
                                    "(id=" + action.getId() + "): scheduling staging job for minion server id: " + id +
                                    " at " + stagingTime));
                        }
                    return scheduleActionData;
                }
            }
        }
        return Collections.EMPTY_MAP;
    }
}
