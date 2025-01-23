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

import java.io.FileWriter;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
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

        saveAndUpdateCaCertificates(filenameToRootCaCertMap);
    }

    /**
     * Saves multiple root ca certificates, then updates trusted directory
     * This is a public entry point for any other taskomatic task
     * that needs to deal with ca certificates in the local filesystem
     *
     * @param filenameToRootCaCertMap maps filename to root ca certificate actual content
     * @throws JobExecutionException if there was an error
     */
    public void saveAndUpdateCaCertificates(Map<String, String> filenameToRootCaCertMap) throws JobExecutionException {
        if ((null == filenameToRootCaCertMap) || filenameToRootCaCertMap.isEmpty()) {
            return; // nothing to do
        }

        for (Map.Entry<String, String> pair : filenameToRootCaCertMap.entrySet()) {
            String fileName = pair.getKey();
            String rootCaCertContent = pair.getValue();

            if (fileName.isEmpty()) {
                continue;
            }

            if (rootCaCertContent.isEmpty()) {
                try {
                    removeCertificate(fileName);
                    log.info("CA certificate file: {} successfully removed", fileName);
                }
                catch (IOException e) {
                    log.error("error when removing CA certificate file {}: {}", fileName, e);
                }
            }
            else {
                try {
                    saveCertificate(fileName, rootCaCertContent);
                    log.info("CA certificate file: {} successfully written", fileName);
                }
                catch (IOException e) {
                    log.error("error when writing CA certificate file {}: {}", fileName, e);
                }
            }
        }

        updateCaCertificates();
    }

    private void removeCertificate(String fileName) throws IOException {
        String fullPathName = CertificateUtils.CERTS_PATH.resolve(fileName).toString();
        Files.delete(Path.of(fullPathName));
    }

    private void saveCertificate(String fileName, String rootCaCertContent) throws IOException {
        String fullPathName = CertificateUtils.CERTS_PATH.resolve(fileName).toString();
        try (FileWriter fw = new FileWriter(fullPathName, false)) {
            fw.write(rootCaCertContent);
        }
    }

    private void updateCaCertificates() throws JobExecutionException {
        try {
            String[] cmd = {"systemctl", "is-active", "--quiet", "ca-certificates.path"};
            executeExtCmd(cmd);
        }
        catch (Exception e) {
            log.debug("ca-certificates.path service is not active, we will call 'update-ca-certificates' tool");
            String[] cmd = {"/usr/share/rhn/certs/update-ca-cert-trust.sh"};
            executeExtCmd(cmd);
        }
    }
}
