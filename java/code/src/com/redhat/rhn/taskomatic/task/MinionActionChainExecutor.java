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

import com.redhat.rhn.common.hibernate.HibernateFactory;
import com.redhat.rhn.domain.action.ActionChain;
import com.redhat.rhn.domain.action.ActionChainEntry;
import com.redhat.rhn.domain.action.ActionChainFactory;
import com.suse.manager.webui.services.SaltServerActionService;

import org.apache.log4j.Logger;
import org.quartz.JobExecutionContext;

/**
 * Execute SUSE Manager actions via Salt.
 */
public class MinionActionChainExecutor extends RhnJavaJob {

    private static final Logger LOG = Logger.getLogger(MinionActionChainExecutor.class);

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
        long actionChainId = Long.parseLong((String)context.getJobDetail()
                .getJobDataMap().get("actionchain_id"));

        ActionChain actionChain = ActionChainFactory
                .getActionChain(actionChainId)
                .orElse(null);

        if (actionChain == null) {
            LOG.error("Action chain not found id=" + actionChainId);
            return;
        }

        long serverActionsCount = countServerActions(actionChain);
        if (serverActionsCount == 0) {
            LOG.warn("Waiting " + ACTION_DATABASE_GRACE_TIME + "ms for the Tomcat transaction to complete.");
            // give a second chance, just in case this was scheduled immediately
            // and the scheduling transaction did not have the time to commit
            try {
                Thread.sleep(ACTION_DATABASE_GRACE_TIME);
            }
            catch (InterruptedException e) {
                // never happens
            }
            HibernateFactory.getSession().clear();
        }

        // TODO: At this point, the AC has been already removed from the DB
        // calculate offset between scheduled time of
        // actions and (now) to avoid execution in case it
        // was schedule > MAXIMUM_TIMEDELTA_FOR_SCHEDULED_ACTIONS

        log.info("Executing action chain: " + actionChainId);

        saltServerActionService.executeActionChain(actionChainId);

        if (log.isDebugEnabled()) {
            long duration = System.currentTimeMillis() - start;
            log.debug("Total duration was: " + duration + " ms");
        }
    }

    private long countServerActions(ActionChain actionChain) {
        return actionChain.getEntries().stream()
                .map(ActionChainEntry::getAction)
                .flatMap(action -> action.getServerActions().stream())
                .count();
    }
}
