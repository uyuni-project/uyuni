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
import static java.time.temporal.ChronoUnit.HOURS;
import static java.time.temporal.ChronoUnit.SECONDS;

import com.redhat.rhn.common.conf.ConfigDefaults;
import com.redhat.rhn.common.hibernate.HibernateFactory;
import com.redhat.rhn.domain.action.Action;
import com.redhat.rhn.domain.action.ActionFactory;
import com.redhat.rhn.domain.server.MinionServerFactory;
import com.redhat.rhn.domain.user.User;
import com.redhat.rhn.taskomatic.TaskomaticApi;
import com.redhat.rhn.taskomatic.TaskomaticApiException;

import org.apache.log4j.Logger;

import java.math.BigDecimal;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.util.Date;
import java.util.List;
import java.util.stream.Collectors;


/**
 * Utility class for Actions related to minions.
 *
 */
public class MinionActionManager {

    private static Logger log = Logger.getLogger(MinionActionManager.class);
    private static TaskomaticApi taskomaticApi = new TaskomaticApi();

    private MinionActionManager() {
    }

    /**
     * Schedule staging jobs for minions, if:
     * - org has enabled staging_content_enabled
     * - action is either:
     *   - package install/update
     *   - patch install
     *
     * @param action related action already scheduled
     * @param user user that started the action
     * @throws TaskomaticApiException in case of failure of the scheduled staging job
     * @return A list containing the schedule time(s) for staging job(s)
     */
    public static void scheduleStagingJobsForMinions(Action action, User user)
        throws TaskomaticApiException {

        if (user.getOrg().getOrgConfig().isStagingContentEnabled()) {

            List<Long> minionServerIds = (List<Long>) HibernateFactory.getSession()
                    .getNamedQuery("Action.findMinionIds")
                    .setParameter("id", action.getId()).getResultList().stream()
                    .map(i -> ((BigDecimal) i).longValue()).collect(Collectors.toList());

            ZonedDateTime earliestAction =
                    action.getEarliestAction().toInstant().atZone(ZoneId.systemDefault());

            if (earliestAction.isAfter(now()) && !minionServerIds.isEmpty()) {

                final long saltContentStagingAdvance =
                        ConfigDefaults.get().getSaltContentStagingAdvance();
                final long saltContentStagingWindow =
                        ConfigDefaults.get().getSaltContentStagingWindow();

                ZonedDateTime stagingWindowStartTime =
                        earliestAction.minus(saltContentStagingAdvance, HOURS);
                ZonedDateTime stagingWindowEndTime =
                        stagingWindowStartTime.plus(saltContentStagingWindow, HOURS);

                if (now().isAfter(stagingWindowStartTime)) {
                    stagingWindowStartTime = now();
                }

                if (stagingWindowEndTime.isAfter(earliestAction)) {
                    log.warn("Ignoring salt_content_staging_window parameter: " +
                            "expected staging window end time is after action execution!");
                    log.warn("Expected staging window end time: " + stagingWindowEndTime);
                    log.warn("Adjusting  window end time to earliest action execution: " +
                            earliestAction);
                    stagingWindowEndTime = earliestAction;
                }

                if (stagingWindowStartTime.isBefore(earliestAction) &&
                        (stagingWindowEndTime.isBefore(earliestAction) ||
                                stagingWindowEndTime.isEqual(earliestAction)) &&
                        (ActionFactory.TYPE_PACKAGES_UPDATE
                                .equals(action.getActionType()) ||
                                ActionFactory.TYPE_ERRATA.equals(action.getActionType()))) {
                    for (Long minionServerId : minionServerIds) {
                        ZonedDateTime stagingTime =
                                stagingWindowStartTime.plus(
                                        (long) (SECONDS.between(stagingWindowStartTime,
                                                stagingWindowEndTime) * Math.random()),
                                        SECONDS);
                        if (MinionServerFactory.lookupById(minionServerId).get()
                                .getContactMethod().getLabel().equals("default")) {
                            // schedule an action only for non ssh-minions
                            // ssh-minions don't have to call normal salt
                            // but rather SSHPush Taskomatic job

                            log.info("Detected install/update action (id=" +
                                    action.getId() + "): " +
                                    "scheduling staging job for minion server id: " +
                                    minionServerId + " at " + stagingTime);
                            taskomaticApi.scheduleStagingJob(action.getId(), minionServerId,
                                    Date.from(stagingTime.toInstant()));
                        }
                    }
                }
            }
        }
    }
}
