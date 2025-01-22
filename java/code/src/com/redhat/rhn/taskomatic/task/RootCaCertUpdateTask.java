/*
 * Copyright (c) 2025 SUSE LLC
 *
 * This software is licensed to you under the GNU General Public License,
 * version 2 (GPLv2). There is NO WARRANTY for this software, express or
 * implied, including the implied warranties of MERCHANTABILITY or FITNESS
 * FOR A PARTICULAR PURPOSE. You should have received a copy of GPLv2
 * along with this software; if not, see
 * http://www.gnu.org/licenses/old-licenses/gpl-2.0.txt.
 */
package com.redhat.rhn.taskomatic.task;

import org.quartz.JobDataMap;
import org.quartz.JobExecutionContext;
import org.quartz.JobExecutionException;

/**
 * Taskomatic task that checks if new certificates have been added or updated
 * * and stores them accordingly in the trusted certificate path
 */
public class RootCaCertUpdateTask extends RhnJavaJob {

    private static final String ROOT_CA_KEY = "root_ca";

    @Override
    public String getConfigNamespace() {
        return "root-ca-cert-update";
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void execute(JobExecutionContext context) throws JobExecutionException {
        final JobDataMap jobDataMap = context.getJobDetail().getJobDataMap();
        final String rootCa = getRootCa(jobDataMap);

        //TODO: do something
        log.info("Root Ca is: {}", rootCa);

    }

    private String getRootCa(final JobDataMap jobDataMap) {
        String rootCa = "";
        if (jobDataMap.containsKey(ROOT_CA_KEY)) {
            try {
                rootCa = jobDataMap.getString(ROOT_CA_KEY);
            }
            catch (ClassCastException e) {
                rootCa = "";
            }
        }
        return rootCa;
    }
}
