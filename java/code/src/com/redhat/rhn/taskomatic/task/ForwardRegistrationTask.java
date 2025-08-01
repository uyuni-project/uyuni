/*
 * Copyright (c) 2021--2025 SUSE LLC
 *
 * This software is licensed to you under the GNU General Public License,
 * version 2 (GPLv2). There is NO WARRANTY for this software, express or
 * implied, including the implied warranties of MERCHANTABILITY or FITNESS
 * FOR A PARTICULAR PURPOSE. You should have received a copy of GPLv2
 * along with this software; if not, see
 * http://www.gnu.org/licenses/old-licenses/gpl-2.0.txt.
 */
package com.redhat.rhn.taskomatic.task;

import com.redhat.rhn.GlobalInstanceHolder;
import com.redhat.rhn.common.conf.Config;
import com.redhat.rhn.common.conf.ConfigDefaults;
import com.redhat.rhn.domain.credentials.CredentialsFactory;
import com.redhat.rhn.domain.credentials.SCCCredentials;
import com.redhat.rhn.domain.notification.NotificationMessage;
import com.redhat.rhn.domain.notification.UserNotificationFactory;
import com.redhat.rhn.domain.notification.types.NotificationType;
import com.redhat.rhn.domain.notification.types.SCCOptOutWarning;
import com.redhat.rhn.domain.role.RoleFactory;
import com.redhat.rhn.domain.scc.SCCCachingFactory;
import com.redhat.rhn.domain.scc.SCCRegCacheItem;
import com.redhat.rhn.manager.content.ContentSyncManager;

import com.suse.manager.model.hub.HubFactory;
import com.suse.manager.model.hub.IssHub;
import com.suse.scc.SCCSystemRegistrationManager;
import com.suse.scc.client.SCCClient;
import com.suse.scc.client.SCCConfig;
import com.suse.scc.client.SCCConfigBuilder;
import com.suse.scc.client.SCCWebClient;
import com.suse.scc.model.SCCUpdateSystemItem;
import com.suse.scc.model.SCCVirtualizationHostJson;
import com.suse.scc.proxy.SCCProxyFactory;
import com.suse.scc.proxy.SCCProxyRecord;

import org.apache.commons.lang3.time.DateUtils;
import org.quartz.JobExecutionContext;

import java.net.URI;
import java.net.URISyntaxException;
import java.time.Duration;
import java.time.LocalDateTime;
import java.util.Date;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.ThreadLocalRandom;

public class ForwardRegistrationTask extends RhnJavaJob {

    // initialize first run between 30 minutes and 3 hours from now
    protected static LocalDateTime nextLastSeenUpdateRun = LocalDateTime.now().plusMinutes(
            ThreadLocalRandom.current().nextInt(30, 3 * 60));

    //task setup
    protected Optional<SCCCredentials> taskConfigSccCredentials = Optional.empty();
    protected URI taskConfigSccUrl = null;
    protected boolean taskConfigIsSccProxy = false;

    @Override
    public String getConfigNamespace() {
        return "forward_registration";
    }

    @Override
    public void execute(JobExecutionContext arg0) {
        if (!ConfigDefaults.get().isForwardRegistrationEnabled()) {
            NotificationMessage lastNotification = UserNotificationFactory
                    .getLastNotificationMessageByType(NotificationType.SCC_OPT_OUT_WARNING);

            if (lastNotification == null || lastNotification.getCreated().before(DateUtils.addMonths(new Date(), -3))) {
                NotificationMessage notificationMessage =
                        UserNotificationFactory.createNotificationMessage(new SCCOptOutWarning());
                UserNotificationFactory.storeNotificationMessageFor(notificationMessage, RoleFactory.ORG_ADMIN);
            }

            if (GlobalInstanceHolder.PAYG_MANAGER.isPaygInstance() &&
                    GlobalInstanceHolder.PAYG_MANAGER.hasSCCCredentials()) {
                log.warn("SUSE Multi-Linux Manager PAYG instances must forward registration data to SCC when " +
                        "credentials are provided. Data will be sent independently of the configuration setting.");
            }
            else {
                log.debug("Forwarding registrations disabled");
                return;
            }
        }
        if (Config.get().getString(ContentSyncManager.RESOURCE_PATH) == null) {
            setupTaskConfiguration();

            taskConfigSccCredentials.ifPresent(credentials -> {
                waitRandomTimeWithinMinutes(15);
                executeSCCTasks(credentials);
            });
        }
    }

    protected void setupTaskConfiguration() {
        HubFactory hubFactory = new HubFactory();
        Optional<IssHub> optHub = hubFactory.lookupIssHub();
        taskConfigSccCredentials = optHub.flatMap(this::getSCCCredentialsWhenPeripheral).or(this::getSCCCredentials);

        try {
            taskConfigSccUrl = new URI(optHub.map(h -> "https://%s/rhn/hub/scc".formatted(h.getFqdn()))
                    .orElse(Config.get().getString(ConfigDefaults.SCC_URL)));
        }
        catch (URISyntaxException e) {
            log.error("Unable to define a valid URI for the SCC url", e);
        }

        taskConfigIsSccProxy = optHub.isPresent();
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

    protected Optional<SCCCredentials> getSCCCredentials() {
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

    protected Optional<SCCCredentials> getSCCCredentialsWhenPeripheral(IssHub hub) {
        List<SCCCredentials> credentials = CredentialsFactory.listSCCCredentials();
        Optional<SCCCredentials> optHubCredential = credentials.stream()
                .filter(c -> hub.getFqdn().equals(c.getUrl()))
                .findFirst();

        if (optHubCredential.isEmpty()) {
            log.warn("No Hub SCC Credentials for {} - skipping forwarding registration", hub.getFqdn());
        }

        return optHubCredential;
    }

    // Do SCC related tasks like insert, update and delete system in SCC
    protected void executeSCCTasks(SCCCredentials credentialsIn) {
        String uuid = ContentSyncManager.getUUID();
        SCCCachingFactory.initNewSystemsToForward();
        SCCConfig sccConfig = new SCCConfigBuilder()
                .setUrl(taskConfigSccUrl)
                .setUsername("")
                .setPassword("")
                .setUuid(uuid)
                .createSCCConfig();
        SCCClient sccClient = new SCCWebClient(sccConfig);
        SCCProxyFactory sccProxyFactory = new SCCProxyFactory();

        SCCSystemRegistrationManager sccRegManager = new SCCSystemRegistrationManager(sccClient, sccProxyFactory);

        executeSCCTasksAsServer(sccRegManager, credentialsIn);
        if (taskConfigIsSccProxy) {
            executeSCCTasksAsProxy(sccRegManager, credentialsIn);
        }
    }

    // normal server tasks: direct to SCC
    protected void executeSCCTasksAsServer(SCCSystemRegistrationManager sccRegManager,
                                       SCCCredentials sccPrimaryOrProxyCredentials) {
        List<SCCRegCacheItem> forwardRegistration = SCCCachingFactory.findSystemsToForwardRegistration();
        log.debug("{} RegCacheItems found to forward", forwardRegistration.size());

        List<SCCRegCacheItem> deregister = SCCCachingFactory.listDeregisterItems();
        log.debug("{} RegCacheItems found to delete", deregister.size());

        List<SCCVirtualizationHostJson> virtHosts = SCCCachingFactory.listVirtualizationHosts();
        log.debug("{} VirtHosts found to send", virtHosts.size());

        sccRegManager.deregister(deregister, false);
        sccRegManager.register(forwardRegistration, sccPrimaryOrProxyCredentials);
        sccRegManager.virtualInfo(virtHosts, sccPrimaryOrProxyCredentials);
        if (LocalDateTime.now().isAfter(nextLastSeenUpdateRun)) {
            List<SCCUpdateSystemItem> updateLastSeenItems =
                    SCCCachingFactory.listUpdateLastSeenItems(sccPrimaryOrProxyCredentials);
            sccRegManager.updateLastSeen(updateLastSeenItems, sccPrimaryOrProxyCredentials);
            // next run in 22 - 26 hours
            synchronized (this) {
                nextLastSeenUpdateRun = nextLastSeenUpdateRun.plusMinutes(
                        ThreadLocalRandom.current().nextInt(22 * 60, 26 * 60));
            }
        }
    }

    // tasks acting as proxy for peripherals
    protected void executeSCCTasksAsProxy(SCCSystemRegistrationManager sccRegManager,
            SCCCredentials sccPrimaryOrProxyCredentials) {
        SCCProxyFactory sccProxyFactory = sccRegManager.getSccProxyFactory();

        List<SCCProxyRecord> proxyForwardRegistration = sccProxyFactory.findSystemsToForwardRegistration();
        log.debug("{} ProxyRecords found to forward", proxyForwardRegistration.size());

        List<SCCProxyRecord> proxyDeregister = sccProxyFactory.listDeregisterItems();
        log.debug("{} ProxyRecords found to delete", proxyDeregister.size());

        List<SCCProxyRecord> proxyVirtHosts = sccProxyFactory.findVirtualizationHosts();
        log.debug("{} VirtHosts ProxyRecords found to send", proxyVirtHosts.size());

        sccRegManager.proxyDeregister(proxyDeregister, false);
        sccRegManager.proxyRegister(proxyForwardRegistration, sccPrimaryOrProxyCredentials);
        sccRegManager.proxyVirtualInfo(proxyVirtHosts, sccPrimaryOrProxyCredentials);

        //the updates are sent by the peripherals, we don't want to hold the information here
        List<SCCProxyRecord> proxyUpdateLastSeen = sccProxyFactory.listUpdateLastSeenItems();
        log.debug("{} Proxy systems to update last seen", proxyUpdateLastSeen.size());
        sccRegManager.proxyUpdateLastSeen(proxyUpdateLastSeen, sccPrimaryOrProxyCredentials);
    }
}
