/**
 * Copyright (c) 2018 SUSE LLC
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

import com.redhat.rhn.domain.user.User;
import com.redhat.rhn.domain.user.UserFactory;
import com.suse.manager.webui.services.SaltServerActionService;

import org.quartz.JobExecutionContext;

import java.util.Optional;
import java.util.List;
import java.util.stream.Collectors;

/**
 * Execute SUSE Manager actions via Salt.
 */
public class MinionActionChainExecutor extends RhnJavaJob {

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
            log.debug("Start minion action chain executor");
        }

        // Measure time to calculate the total duration
        long start = System.currentTimeMillis();
        long actionChainId = Long.parseLong(((List<String>) context.getJobDetail()
                .getJobDataMap().get("actionchain_id")).get(0));
        List<Long> targetServers = ((List<String>) context.getJobDetail()
                .getJobDataMap().get("target_ids")).stream()
                        .map(target -> Long.parseLong(target))
                        .collect(Collectors.toList());
        User user = Optional.ofNullable(context.getJobDetail().getJobDataMap().get("user_id"))
                .map(id -> Long.parseLong(id.toString()))
                .map(userId -> UserFactory.lookupById(userId))
                .orElse(null);

        // TODO: At this point, the AC has been already removed from the DB
        // calculate offset between scheduled time of
        // actions and (now) to avoid execution in case it
        // was schedule > MAXIMUM_TIMEDELTA_FOR_SCHEDULED_ACTIONS

        log.info("Executing action chain: " + actionChainId);

        saltServerActionService.executeActionChain(user, actionChainId, targetServers);

        if (log.isDebugEnabled()) {
            long duration = System.currentTimeMillis() - start;
            log.debug("Total duration was: " + duration + " ms");
        }
    }

    /**
     * Needed only for unit tests.
     * @param saltServerActionServiceIn to set
     */
    public void setSaltServerActionService(SaltServerActionService saltServerActionServiceIn) {
        this.saltServerActionService = saltServerActionServiceIn;
    }
}
