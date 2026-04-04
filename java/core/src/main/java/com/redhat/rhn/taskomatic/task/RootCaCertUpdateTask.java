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

import com.suse.utils.CertificateUtils;

import org.quartz.JobDataMap;
import org.quartz.JobExecutionContext;
import org.quartz.JobExecutionException;

import java.util.HashMap;
import java.util.Map;

/**
 * Taskomatic task that checks if new certificates have been added or updated
 * and saves them accordingly in the trusted certificate path.
 * After saving the certificates, the system configuration is refreshed.
 */
public class RootCaCertUpdateTask extends RhnJavaJob {

    private static final String MAP_KEY = "filename_to_root_ca_cert_map";

    @Override
    public String getConfigNamespace() {
        return "root-ca-cert-update";
    }

    private Map<String, String> getFilenameToRootCaCertMap(final JobDataMap jobDataMap) {
        Map<String, String> filenameToRootCaCertMap = new HashMap<>();

        if (jobDataMap.containsKey(MAP_KEY)) {
            try {
                filenameToRootCaCertMap = (Map<String, String>) jobDataMap.get(MAP_KEY);
            }
            catch (ClassCastException e) {
                //filenameToRootCaCertMap is already empty
                log.debug("error while extracting filename to root certificate map");
            }
        }
        return filenameToRootCaCertMap;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void execute(JobExecutionContext context) throws JobExecutionException {
        final JobDataMap jobDataMap = context.getJobDetail().getJobDataMap();
        Map<String, String> filenameToRootCaCertMap = getFilenameToRootCaCertMap(jobDataMap);

        CertificateUtils.saveAndUpdateCertificates(filenameToRootCaCertMap);
    }
}
