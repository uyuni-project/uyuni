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

import com.redhat.rhn.manager.audit.CVEAuditManagerOVAL;

import org.quartz.JobExecutionContext;

import java.util.Date;

/**
 * Trigger the population of the suseCVEServerChannels table, which is
 * necessary for running CVE audit queries.
 *
 */
public class CVEServerChannels extends RhnJavaJob {

    @Override
    public String getConfigNamespace() {
        return "cve_server_channels";
    }

    /**
     * @param context the job execution context
     * @see org.quartz.Job#execute(org.quartz.JobExecutionContext)
     */
    @Override
    public void execute(JobExecutionContext context) {
        if (log.isDebugEnabled()) {
            log.debug("Finding relevant channels");
        }

        // Measure time and calculate the total duration
        Date start = new Date();
        CVEAuditManagerOVAL.populateCVEChannels();

        if (log.isDebugEnabled()) {
            long duration = new Date().getTime() - start.getTime();
            log.debug("Total duration was: {} ms", duration);
        }
    }
}
