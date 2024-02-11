/*
 * Copyright (c) 2021 SUSE LLC
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

package com.redhat.rhn.taskomatic.task.payg;

import com.redhat.rhn.domain.cloudpayg.CloudRmtHost;
import com.redhat.rhn.domain.cloudpayg.CloudRmtHostFactory;
import com.redhat.rhn.taskomatic.task.RhnJavaJob;

import org.quartz.JobExecutionContext;
import org.quartz.JobExecutionException;

import java.io.BufferedWriter;
import java.io.FileWriter;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;

public class PaygUpdateHostsTask extends RhnJavaJob {
    private static final String HOSTS = "/etc/hosts";
    private static final String RETAIN_COMMENT = " - retain comment as well\n";
    private static final String HOST_COMMENT_START = "# Added by Suma - Start";
    private static final String HOST_COMMENT_END = "# Added by Suma - End";

    private static final String CA_LOCATION_TEMPLATE = "/etc/pki/trust/anchors/registration_server_%s.pem";

    @Override
    public String getConfigNamespace() {
        return "payg";
    }

    @Override
    public void execute(JobExecutionContext jobExecutionContext) throws JobExecutionException {
        log.debug("Running CloudRmUpdateHostsTask");
        List<CloudRmtHost> hostToUpdate = CloudRmtHostFactory.lookupCloudRmtHostsToUpdate();
        if (!hostToUpdate.isEmpty()) {
            loadHttpsCertificates(hostToUpdate);
            updateHost(hostToUpdate);
        }
    }

    private void loadHttpsCertificates(List<CloudRmtHost> hostToUpdate) throws JobExecutionException {
        try {
            for (CloudRmtHost host : hostToUpdate) {
                String caFileName = String.format(CA_LOCATION_TEMPLATE, host.getIp());
                try (FileWriter fw = new FileWriter(caFileName, false)) {
                    fw.write(host.getSslCert());
                }
            }
        }
        catch (IOException e) {
            log.error("error when writing the hosts file", e);
        }
        finally {
            if (!hostToUpdate.isEmpty()) {
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
    }

    private void updateHost(List<CloudRmtHost> hostToUpdate) {

        try {
            List<String> newLines = new ArrayList<>();
            List<String> hostLines = Files.readAllLines(Paths.get(HOSTS));
            boolean commentStart = false;

            // remove all SUMA created lines from /etc/hosts
            for (String line : hostLines) {
                if (line.startsWith(HOST_COMMENT_START)) {
                    commentStart = true;
                }
                if (!commentStart) {
                    newLines.add(line);
                }
                if (line.startsWith(HOST_COMMENT_END)) {
                    commentStart = false;
                }
            }

            try (FileWriter fw = new FileWriter(HOSTS, false)) {
                try (BufferedWriter bw = new BufferedWriter(fw)) {
                    for (String nl : newLines) {
                        bw.write(nl);
                        bw.newLine();
                    }
                    bw.flush();
                    if (!hostToUpdate.isEmpty()) {
                        bw.write(HOST_COMMENT_START + RETAIN_COMMENT);
                        for (CloudRmtHost host : hostToUpdate) {
                            // search for already existing RMT Host definition
                            if (newLines.stream().anyMatch(l -> l.contains(host.getHost()))) {
                                // registercloudguest has added already this hostname.
                                // defining multiple IP addresses for the same name can result in problems.
                                // We write out only commented entries here
                                bw.write("# ");
                            }
                            bw.write(String.format("%s\t%s%n", host.getIp(), host.getHost()));
                        }
                        bw.write(HOST_COMMENT_END + RETAIN_COMMENT);
                    }
                    bw.flush();
                }
            }

        }
        catch (IOException e) {
            log.error("error when writing the hosts file", e);
        }
    }
}
