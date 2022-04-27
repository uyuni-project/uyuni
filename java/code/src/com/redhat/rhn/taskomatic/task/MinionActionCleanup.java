/*
 * Copyright (c) 2013 SUSE LLC
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

import com.suse.manager.webui.utils.MinionActionUtils;

import org.quartz.JobExecutionContext;

import java.io.IOException;


/**
 * Finds and cleans up salt Actions for which we missed the JobReturnEvent.
 */
public class MinionActionCleanup extends RhnJavaJob {

    private final MinionActionUtils minionActionUtils = GlobalInstanceHolder.MINION_ACTION_UTILS;

    @Override
    public String getConfigNamespace() {
        return "minion_action_cleanup";
    }

    /**
     * @param context the job execution context
     * @see org.quartz.Job#execute(JobExecutionContext)
     */
    @Override
    public void execute(JobExecutionContext context) {
        if (log.isDebugEnabled()) {
            log.debug("start minion action cleanup");
        }

        // Measure time and calculate the total duration
        long start = System.currentTimeMillis();
        minionActionUtils.cleanupMinionActions();

        // Delete script files for script actions that have completed
        try {
            minionActionUtils.cleanupScriptActions();
        }
        catch (IOException e) {
            log.error("Could not cleanup script files: {}", e.getMessage(), e);
        }

        if (log.isDebugEnabled()) {
            long duration = System.currentTimeMillis() - start;
            log.debug("Total duration was: {} ms", duration);
        }
    }
}
