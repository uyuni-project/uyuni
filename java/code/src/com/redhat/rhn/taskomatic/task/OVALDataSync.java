/*
 * Copyright (c) 2024 SUSE LLC
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
import org.quartz.JobExecutionException;

/**
 * Updates OVAL data to power the CVE auditing feature.
 * */
public class OVALDataSync extends RhnJavaJob {
    @Override
    public String getConfigNamespace() {
        return "oval_data_sync";
    }

    @Override
    public void execute(JobExecutionContext jobExecutionContext) throws JobExecutionException {
        log.info("Syncing OVAL data");

        CVEAuditManagerOVAL.syncOVAL();

        log.info("Done syncing OVAL data");
    }
}
