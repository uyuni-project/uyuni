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

import com.redhat.rhn.common.conf.Config;
import com.redhat.rhn.common.conf.ConfigDefaults;
import com.redhat.rhn.domain.credentials.Credentials;
import com.redhat.rhn.domain.credentials.CredentialsFactory;
import com.redhat.rhn.domain.scc.SCCCachingFactory;
import com.redhat.rhn.domain.scc.SCCRegCacheItem;
import com.redhat.rhn.manager.content.ContentSyncManager;

import com.suse.scc.SCCSystemRegistrationManager;
import com.suse.scc.client.SCCClient;
import com.suse.scc.client.SCCConfig;
import com.suse.scc.client.SCCWebClient;

import org.quartz.JobExecutionContext;
import org.quartz.JobExecutionException;

import java.net.URI;
import java.net.URISyntaxException;
import java.util.List;
import java.util.concurrent.ThreadLocalRandom;


public class ForwardRegistrationTask extends RhnJavaJob {

    @Override
    public String getConfigNamespace() {
        return "forward_registration";
    }

    @Override
    public void execute(JobExecutionContext arg0) throws JobExecutionException {
        if (!ConfigDefaults.get().isForwardRegistrationEnabled()) {
            log.debug("Forwarding registrations disabled");
            return;
        }
        try {
            if (Config.get().getString(ContentSyncManager.RESOURCE_PATH) == null) {
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
                URI url = new URI(Config.get().getString(ConfigDefaults.SCC_URL));
                String uuid = ContentSyncManager.getUUID();
                SCCCachingFactory.initNewSystemsToForward();
                List<SCCRegCacheItem> forwardRegistration = SCCCachingFactory.findSystemsToForwardRegistration();
                log.debug(forwardRegistration.size() + " RegCacheItems found to forward");
                List<SCCRegCacheItem> deregister = SCCCachingFactory.listDeregisterItems();
                log.debug(deregister.size() + " RegCacheItems found to delete");
                List<Credentials> credentials = CredentialsFactory.lookupSCCCredentials();
                SCCConfig sccConfig = new SCCConfig(url, "", "", uuid);
                SCCClient sccClient = new SCCWebClient(sccConfig);
                SCCSystemRegistrationManager sccRegManager = new SCCSystemRegistrationManager(sccClient);
                sccRegManager.deregister(deregister, false);
                credentials.stream()
                    .filter(Credentials::isPrimarySCCCredential)
                    .findFirst()
                    .ifPresent(primaryCredentials -> sccRegManager.register(forwardRegistration, primaryCredentials));
            }
        }
        catch (URISyntaxException e) {
           log.error(e);
        }
    }

}
