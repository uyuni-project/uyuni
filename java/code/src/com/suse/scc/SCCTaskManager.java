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

package com.suse.scc;

import com.redhat.rhn.common.conf.Config;
import com.redhat.rhn.common.conf.ConfigDefaults;
import com.redhat.rhn.domain.credentials.Credentials;
import com.redhat.rhn.domain.credentials.CredentialsFactory;
import com.redhat.rhn.domain.credentials.SCCCredentials;
import com.redhat.rhn.domain.scc.SCCCachingFactory;
import com.redhat.rhn.domain.scc.SCCRegCacheItem;
import com.redhat.rhn.manager.content.ContentSyncManager;

import com.suse.manager.model.hub.HubFactory;
import com.suse.manager.model.hub.IssHub;
import com.suse.scc.client.SCCConfig;
import com.suse.scc.client.SCCConfigBuilder;
import com.suse.scc.client.SCCWebClient;
import com.suse.scc.model.SCCUpdateSystemItem;
import com.suse.scc.model.SCCVirtualizationHostJson;
import com.suse.scc.proxy.SCCProxyFactory;
import com.suse.scc.proxy.SCCProxyRecord;
import com.suse.scc.proxy.SccProxyStatus;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.net.URI;
import java.net.URISyntaxException;
import java.time.Duration;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.ThreadLocalRandom;

public class SCCTaskManager {
    protected Logger log = LogManager.getLogger(getClass());

    private final HubFactory hubFactory;
    private final SCCProxyFactory sccProxyFactory;

    /**
     * Default constructor
     */
    public SCCTaskManager() {
        this(new HubFactory(), new SCCProxyFactory());
    }

    /**
     * Constructor
     *
     * @param hubFactoryIn      the hub factory
     * @param sccProxyFactoryIn the proxy factory
     */
    public SCCTaskManager(HubFactory hubFactoryIn, SCCProxyFactory sccProxyFactoryIn) {
        hubFactory = hubFactoryIn;
        sccProxyFactory = sccProxyFactoryIn;
    }

    private void waitRandomTimeWithinMinutes(int minutes) {
        int waitTimeSec = ThreadLocalRandom.current().nextInt(0, minutes * 60);
        if (log.isDebugEnabled()) {
            // no waiting when debug is on
            waitTimeSec = 1;
        }
        try {
            Thread.sleep(Duration.ofSeconds(waitTimeSec).toMillis());
        }
        catch (InterruptedException e) {
            log.debug("Sleep interrupted", e);
            Thread.currentThread().interrupt();
        }
    }

    /**
     * get scc credentials
     */
    protected Optional<SCCCredentials> getSCCCredentials() {
        Optional<IssHub> optHub = hubFactory.lookupIssHub();
        return optHub.flatMap(this::getSCCCredentialsPeripheralCase).or(this::getSCCCredentialsStandardCase);
    }

    protected Optional<SCCCredentials> getSCCCredentialsStandardCase() {
        List<SCCCredentials> credentials = CredentialsFactory.listSCCCredentials();
        Optional<SCCCredentials> optPrimCred = credentials.stream()
                .filter(SCCCredentials::isPrimary)
                .findFirst();

        if (log.isDebugEnabled() && optPrimCred.isEmpty()) {
            // We cannot update SCC without credentials
            // Standard Uyuni case
            log.debug("No SCC Credentials - skipping forwarding registration");
        }

        return optPrimCred;
    }

    protected Optional<SCCCredentials> getSCCCredentialsPeripheralCase(IssHub hub) {
        List<SCCCredentials> credentials = CredentialsFactory.listSCCCredentials();
        Optional<SCCCredentials> optHubCredential = credentials.stream()
                .filter(c -> hub.getFqdn().equals(c.getUrl()))
                .findFirst();

        if (optHubCredential.isEmpty()) {
            log.warn("No Hub SCC Credentials for {} - skipping forwarding registration", hub.getFqdn());
        }

        return optHubCredential;
    }

    /**
     * get scc url
     */
    protected String getSCCUrl() {
        Optional<IssHub> optHub = hubFactory.lookupIssHub();
        return optHub.map(this::getUrlPeripheralCase).orElse(getUrlStandardCase());
    }

    protected String getUrlStandardCase() {
        return Config.get().getString(ConfigDefaults.SCC_URL);
    }

    protected String getUrlPeripheralCase(IssHub hub) {
        return "https://%s/rhn/hub/scc".formatted(hub.getFqdn());
    }

    /**
     * true if acting as SCC proxy
     */
    protected boolean isSCCProxy() {
        return hubFactory.isISSHub();
    }

    /**
     * create SCCSystemRegistrationManager
     */
    protected SCCSystemRegistrationManager getSCCSystemRegistrationManager() {
        return getSCCSystemRegistrationManager(getSCCUrl());
    }

    protected SCCSystemRegistrationManager getSCCSystemRegistrationManager(String sccUrl) {
        URI sccURI = null;
        try {
            sccURI = new URI(sccUrl);
        }
        catch (URISyntaxException e) {
            log.error("Unable to define a valid URI for the SCC url", e);
        }

        SCCConfig sccConfig = new SCCConfigBuilder()
                .setUrl(sccURI)
                .setUsername("")
                .setPassword("")
                .setUuid(ContentSyncManager.getUUID())
                .createSCCConfig();

        SCCWebClient sccClient = new SCCWebClient(sccConfig);
        return new SCCSystemRegistrationManager(sccClient, sccProxyFactory);
    }

    /**
     * Execute SCC related tasks like insert, update and delete system in SCC
     *
     * @param minutes               number of minutes within whom task execution is done
     * @param nextLastSeenUpdateRun timestamp of next "last seen update" run
     * @return true if updateLastSeen has been performed
     */
    public boolean executeSCCTasksWithinRandomMinutes(int minutes, LocalDateTime nextLastSeenUpdateRun) {
        if (minutes > 0) {
            waitRandomTimeWithinMinutes(minutes);
        }
        return executeSCCTasks(nextLastSeenUpdateRun);
    }

    /**
     * Execute SCC related tasks like insert, update and delete system in SCC
     */
    protected boolean executeSCCTasks(LocalDateTime nextLastSeenUpdateRun) {
        boolean updateLastSeenRun = executeSCCTasksAsServer(nextLastSeenUpdateRun);
        if (isSCCProxy()) {
            executeSCCTasksAsProxy();
        }
        return updateLastSeenRun;
    }

    /**
     * Execute normal server tasks: direct to SCC
     */
    protected boolean executeSCCTasksAsServer(LocalDateTime nextLastSeenUpdateRun) {
        SCCCachingFactory.initNewSystemsToForward();

        boolean updateLastSeenRun = getSCCCredentials().isPresent() &&
                (LocalDateTime.now().isAfter(nextLastSeenUpdateRun));

        getSCCCredentials().ifPresent(sccPrimaryCredentials -> {
            SCCSystemRegistrationManager sccRegManager = getSCCSystemRegistrationManager();
            List<SCCRegCacheItem> forwardRegistration = SCCCachingFactory.findSystemsToForwardRegistration();
            log.debug("{} RegCacheItems found to forward", forwardRegistration.size());

            List<SCCRegCacheItem> deregister = SCCCachingFactory.listDeregisterItems();
            log.debug("{} RegCacheItems found to delete", deregister.size());

            List<SCCVirtualizationHostJson> virtHosts = SCCCachingFactory.listVirtualizationHosts();
            log.debug("{} VirtHosts found to send", virtHosts.size());

            sccRegManager.deregister(deregister, false);
            sccRegManager.register(forwardRegistration, sccPrimaryCredentials);
            sccRegManager.virtualInfo(virtHosts, sccPrimaryCredentials);

            if (updateLastSeenRun) {
                List<SCCUpdateSystemItem> updateLastSeenItems =
                        SCCCachingFactory.listUpdateLastSeenItems(sccPrimaryCredentials);
                sccRegManager.updateLastSeen(updateLastSeenItems, sccPrimaryCredentials);
            }
        });

        return updateLastSeenRun;
    }

    /**
     * Execute tasks acting as proxy for peripherals
     */
    protected void executeSCCTasksAsProxy() {
        getSCCCredentials().ifPresent(sccPrimaryCredentials -> {
            SCCSystemRegistrationManager sccRegManager = getSCCSystemRegistrationManager();

            List<SCCProxyRecord> proxyForwardRegistration = sccProxyFactory.findSystemsToForwardRegistration();
            log.debug("{} ProxyRecords found to forward", proxyForwardRegistration.size());

            List<SCCProxyRecord> proxyDeregister = sccProxyFactory.listDeregisterItems();
            log.debug("{} ProxyRecords found to delete", proxyDeregister.size());

            List<SCCProxyRecord> proxyVirtHosts = sccProxyFactory.findVirtualizationHosts();
            log.debug("{} VirtHosts ProxyRecords found to send", proxyVirtHosts.size());

            sccRegManager.proxyDeregister(proxyDeregister, sccPrimaryCredentials, false);
            sccRegManager.proxyRegister(proxyForwardRegistration, sccPrimaryCredentials);
            sccRegManager.proxyVirtualInfo(proxyVirtHosts, sccPrimaryCredentials);

            //the updates are sent by the peripherals, we don't want to hold the information here
            List<SCCProxyRecord> proxyUpdateLastSeen = sccProxyFactory.listUpdateLastSeenItems();
            log.debug("{} Proxy systems to update last seen", proxyUpdateLastSeen.size());
            sccRegManager.proxyUpdateLastSeen(proxyUpdateLastSeen, sccPrimaryCredentials);
        });
    }

    /**
     * cleans up stuff when a peripheral is deregistered
     *
     * @param peripheralFqdn the fqdn of the peripheral that is giong to be deregistered
     */
    public void cleanupSccProxyWhenDeregisteringPeripheral(String peripheralFqdn) {
        //set scc proxy entries of this peripheral as if they applied to be deregistered
        sccProxyFactory.deregisterProxyEntriesForPeripheral(peripheralFqdn);

        //update proxy entries deregistering
        if (isSCCProxy()) {
            executeSCCTasksAsProxy();
        }
    }

    /**
     * deregister all proxy records
     * @param credentialsAboutToDelete the credentials about to be deleted
     */
    public void cleanupSccWhenDeletingPrimaryCredentials(Credentials credentialsAboutToDelete) {
        // Check for systems registered under this credentials and start delete requests
        List<SCCRegCacheItem> itemList = SCCCachingFactory.listRegItemsByCredentials(credentialsAboutToDelete);
        SCCSystemRegistrationManager sccRegManager = getSCCSystemRegistrationManager(getUrlStandardCase());
        sccRegManager.deregister(itemList, true);
    }

    /**
     * deregister all proxy records
     */
    public void cleanupSccProxyWhenDeletingPrimaryCredentials() {
        List<SCCProxyRecord> proxyDeregister = sccProxyFactory.lookupByStatusAndRetry(SccProxyStatus.SCC_CREATED);
        log.debug("{} ProxyRecords found to force deregister", proxyDeregister.size());

        getSCCCredentials().ifPresent(sccPrimaryCredentials -> {
            SCCSystemRegistrationManager sccRegManager = getSCCSystemRegistrationManager();
            sccRegManager.proxyForceDeregister(proxyDeregister, sccPrimaryCredentials);
        });

        sccProxyFactory.setReregisterProxyEntries();
    }
}
