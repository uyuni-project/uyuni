/**
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
import com.redhat.rhn.taskomatic.SCCSystemRegistry;

import org.quartz.JobExecutionContext;
import org.quartz.JobExecutionException;

import java.net.URI;
import java.net.URISyntaxException;
import java.util.List;


public class ForwardRegistrationTask extends RhnJavaJob {

    @Override
    public void execute(JobExecutionContext arg0) throws JobExecutionException {
        if (!ConfigDefaults.get().isForwardRegistrationEnabled()) {
            log.debug("Forwarding registrations disabled");
            return;
        }
        try {
            if (Config.get().getString(ContentSyncManager.RESOURCE_PATH) == null) {
                URI url = new URI(Config.get().getString(ConfigDefaults.SCC_URL));
                //TODO: find a better place to put getUUID
                String uuid = ContentSyncManager.getUUID();
                SCCCachingFactory.initNewSystemsToForward();
                List<SCCRegCacheItem> forwardRegistration = SCCCachingFactory.findSystemsToForwardRegistration();
                log.debug(forwardRegistration.size() + " RegCacheItems found to forward");
                List<SCCRegCacheItem> deregister = SCCCachingFactory.listDeregisterItems();
                log.debug(deregister.size() + " RegCacheItems found to delete");
                List<Credentials> credentials = CredentialsFactory.lookupSCCCredentials();
                SCCSystemRegistry sccSystemRegistry = new SCCSystemRegistry(url, uuid);
                credentials.stream().filter(c -> c.isPrimarySCCCredential()).findFirst().ifPresent(primaryCredentials -> {
                    sccSystemRegistry.deregister(deregister, false);
                    sccSystemRegistry.register(forwardRegistration, primaryCredentials);
                });
            }
        }
        catch (URISyntaxException e) {
           log.error(e);
        }
    }

}
