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
package com.redhat.rhn.taskomatic.task;

import com.redhat.rhn.GlobalInstanceHolder;
import com.redhat.rhn.common.conf.Config;
import com.redhat.rhn.common.conf.ConfigDefaults;
import com.redhat.rhn.domain.credentials.CredentialsFactory;
import com.redhat.rhn.domain.credentials.SCCCredentials;
import com.redhat.rhn.domain.scc.SCCCachingFactory;
import com.redhat.rhn.domain.scc.SCCRegCacheItem;
import com.redhat.rhn.manager.content.ContentSyncManager;

import com.suse.scc.SCCSystemRegistrationManager;
import com.suse.scc.client.SCCClient;
import com.suse.scc.client.SCCConfig;
import com.suse.scc.client.SCCWebClient;
import com.suse.scc.model.SCCVirtualizationHostJson;

import org.quartz.JobExecutionContext;

import java.net.URI;
import java.net.URISyntaxException;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.ThreadLocalRandom;


public class ForwardRegistrationTask extends RhnJavaJob {

    // initialize first run between 30 minutes and 3 hours from now
    private static LocalDateTime nextLastSeenUpdateRun = LocalDateTime.now().plusMinutes(
            ThreadLocalRandom.current().nextInt(30, 3 * 60));

    @Override
    public String getConfigNamespace() {
        return "forward_registration";
    }

    @Override
    public void execute(JobExecutionContext arg0) {
        if (!ConfigDefaults.get().isForwardRegistrationEnabled()) {
            if (GlobalInstanceHolder.PAYG_MANAGER.isPaygInstance() &&
                    GlobalInstanceHolder.PAYG_MANAGER.hasSCCCredentials()) {
                log.warn("SUSE Manager PAYG instances must forward registration data to SCC when " +
                        "credentials are provided. Data will be sent independently of the configuration setting.");
            }
            else {
                log.debug("Forwarding registrations disabled");
                return;
            }
        }
        if (Config.get().getString(ContentSyncManager.RESOURCE_PATH) == null) {

            List<SCCCredentials> credentials = CredentialsFactory.listSCCCredentials();
            Optional<SCCCredentials> optPrimCred = credentials.stream()
                    .filter(c -> c.isPrimary())
                    .findFirst();
            if (optPrimCred.isEmpty()) {
                // We cannot update SCC without credentials
                // Standard Uyuni case
                log.debug("No SCC Credentials - skipping forwarding registration");
                return;
            }
            int waitTime = ThreadLocalRandom.current().nextInt(0, 15 * 60);
            if (log.isDebugEnabled()) {
                // no waiting when debug is on
                waitTime = 1;
            }
            try {
                Thread.sleep(waitTime * 1000);
            }
            catch (InterruptedException e) {
                log.debug("Sleep interrupted", e);
            }
            optPrimCred.ifPresent(primaryCredentials -> executeSCCTasks(primaryCredentials));
        }
    }

    /*
     * Do SCC related tasks like insert, update and delete system in SCC
     */
    private void executeSCCTasks(SCCCredentials primaryCredentials) {
        try {
            URI url = new URI(Config.get().getString(ConfigDefaults.SCC_URL));
            String uuid = ContentSyncManager.getUUID();
            SCCCachingFactory.initNewSystemsToForward();
            SCCConfig sccConfig = new SCCConfig(url, "", "", uuid);
            SCCClient sccClient = new SCCWebClient(sccConfig);

            SCCSystemRegistrationManager sccRegManager = new SCCSystemRegistrationManager(sccClient);
            List<SCCRegCacheItem> forwardRegistration = SCCCachingFactory.findSystemsToForwardRegistration();
            log.debug("{} RegCacheItems found to forward", forwardRegistration.size());

            List<SCCRegCacheItem> deregister = SCCCachingFactory.listDeregisterItems();
            log.debug("{} RegCacheItems found to delete", deregister.size());

            List<SCCVirtualizationHostJson> virtHosts = SCCCachingFactory.listVirtualizationHosts();
            log.debug("{} VirtHosts found to send", virtHosts.size());

            sccRegManager.deregister(deregister, false);
            sccRegManager.register(forwardRegistration, primaryCredentials);
            sccRegManager.virtualInfo(virtHosts, primaryCredentials);
            if (LocalDateTime.now().isAfter(nextLastSeenUpdateRun)) {
                sccRegManager.updateLastSeen(primaryCredentials);
                // next run in 22 - 26 hours
                nextLastSeenUpdateRun = nextLastSeenUpdateRun.plusMinutes(
                        ThreadLocalRandom.current().nextInt(22 * 60, 26 * 60));
            }
        }
        catch (URISyntaxException e) {
            log.error(e.getMessage(), e);
        }
    }
}
