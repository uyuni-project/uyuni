/*
 * Copyright (c) 2025 SUSE LLC
 *
 * This software is licensed to you under the GNU General Public License,
 * version 2 (GPLv2). There is NO WARRANTY for this software, express or
 * implied, including the implied warranties of MERCHANTABILITY or FITNESS
 * FOR A PARTICULAR PURPOSE. You should have received a copy of GPLv2
 * along with this software; if not, see
 * http://www.gnu.org/licenses/old-licenses/gpl-2.0.txt.
 *
 * SPDX-License-Identifier: GPL-2.0-only
 */
package com.redhat.rhn.taskomatic.task;

import com.suse.utils.CertificateUtils;

import org.quartz.JobDataMap;
import org.quartz.JobExecutionContext;
import org.quartz.JobExecutionException;

/**
 * Taskomatic task that import a GPG keys to the customer keyring
 * After saving the GPG key, the system configuration is refreshed.
 */
public class GpgImportTask extends RhnJavaJob {

    @Override
    public String getConfigNamespace() {
        return "gpg-update";
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void execute(JobExecutionContext context) throws JobExecutionException {
        final JobDataMap jobDataMap = context.getJobDetail().getJobDataMap();
        if (!jobDataMap.containsKey("gpg-key")) {
            log.error("No GPG key provided");
            return;
        }
        try {
            CertificateUtils.importGpgKey((String)jobDataMap.get("gpg-key"));
        }
        catch (Exception e) {
            log.error("Importing the GPG key failed", e);
        }
    }
}
