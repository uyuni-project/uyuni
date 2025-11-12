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
 * SPDX-License-Identifier: GPL-2.0-only
 *
 * Red Hat trademarks are not licensed under GPLv2. No permission is
 * granted to use or replicate Red Hat trademarks that are incorporated
 * in this software or its documentation.
 */

package com.redhat.rhn.taskomatic.task.payg;

import com.redhat.rhn.GlobalInstanceHolder;
import com.redhat.rhn.common.hibernate.HibernateFactory;
import com.redhat.rhn.common.hibernate.LookupException;
import com.redhat.rhn.common.util.FileLocks;
import com.redhat.rhn.domain.access.AccessGroupFactory;
import com.redhat.rhn.domain.cloudpayg.PaygSshData;
import com.redhat.rhn.domain.cloudpayg.PaygSshDataFactory;
import com.redhat.rhn.domain.credentials.CloudRMTCredentials;
import com.redhat.rhn.domain.notification.NotificationMessage;
import com.redhat.rhn.domain.notification.UserNotificationFactory;
import com.redhat.rhn.domain.notification.types.PaygAuthenticationUpdateFailed;
import com.redhat.rhn.manager.content.ContentSyncException;
import com.redhat.rhn.manager.content.ContentSyncManager;
import com.redhat.rhn.taskomatic.TaskomaticApi;
import com.redhat.rhn.taskomatic.task.RhnJavaJob;
import com.redhat.rhn.taskomatic.task.payg.beans.PaygInstanceInfo;

import com.suse.cloud.CloudPaygManager;
import com.suse.manager.admin.PaygAdminManager;

import com.jcraft.jsch.JSchException;

import org.apache.commons.collections.CollectionUtils;
import org.quartz.JobExecutionContext;

import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;

public class PaygUpdateAuthTask extends RhnJavaJob {

    private final PaygAuthDataProcessor paygDataProcessor = new PaygAuthDataProcessor();
    private PaygAuthDataExtractor paygDataExtractor = new PaygAuthDataExtractor();

    private CloudPaygManager cloudPaygManager = GlobalInstanceHolder.PAYG_MANAGER;

    private ContentSyncManager contentSyncManager = new ContentSyncManager();

    private FileLocks sccRefreshLock = FileLocks.SCC_REFRESH_LOCK;

    private static final String KEY_ID = "sshData_id";

    @Override
    public String getConfigNamespace() {
        return "payg";
    }

    private void manageLocalHostPayg() {
        if (cloudPaygManager.isPaygInstance()) {
            if (PaygSshDataFactory.lookupByHostname("localhost").isEmpty()) {
                PaygSshData paygSshData = PaygSshDataFactory.createPaygSshData();
                paygSshData.setHost("localhost");
                paygSshData.setDescription("SUSE Multi-Linux Manager PAYG");
                paygSshData.setUsername("root");
                PaygSshDataFactory.savePaygSshData(paygSshData);
                HibernateFactory.getSession().flush();
                HibernateFactory.commitTransaction();
            }
        }
        else {
            try {
                PaygAdminManager pam = new PaygAdminManager(new TaskomaticApi());
                pam.delete("localhost");
            }
            catch (LookupException e) {
                log.debug("No localhost PAYG instance found, noting to delete");
            }
        }
    }

    @Override
    public void execute(JobExecutionContext jobExecutionContext) {
        log.debug("Running PaygUpdateAuthTask");

        manageLocalHostPayg();

        List<PaygSshData> paygSshData;
        if (jobExecutionContext != null && jobExecutionContext.getJobDetail().getJobDataMap().containsKey(KEY_ID)) {
            int sshId = Integer.parseInt((String) jobExecutionContext.getJobDetail().getJobDataMap().get(KEY_ID));
            paygSshData = PaygSshDataFactory.lookupById(sshId).stream().collect(Collectors.toList());
        }
        else {
            paygSshData = PaygSshDataFactory.lookupPaygSshData();
        }

        if (CollectionUtils.isNotEmpty(paygSshData)) {
            sccRefreshLock.withTimeoutFileLock(() -> {
                paygSshData.forEach(this::updateInstanceData);

                // Call the content sync manager to refresh all repositories content sources and the authorizations
                try {
                    contentSyncManager.updateRepositoriesPayg();
                }
                catch (ContentSyncException ex) {
                    log.error("Unable to refresh repositories", ex);
                }
            }, 60);
        }
    }

    /**
     * Need for automatic tests
     * @param paygDataExtractorIn
     */
    public void setPaygDataExtractor(PaygAuthDataExtractor paygDataExtractorIn) {
        this.paygDataExtractor = paygDataExtractorIn;
    }

    /**
     * Needed for unit tests
     * @param mgrIn
     */
    public void setCloudPaygManager(CloudPaygManager mgrIn) {
        cloudPaygManager = mgrIn;
    }

    /**
     * Needed for unit tests
     * @param contentSyncManagerIn
     */
    public void setContentSyncManager(ContentSyncManager contentSyncManagerIn) {
        this.contentSyncManager = contentSyncManagerIn;
    }

    /**
     * Needed for unit testing
     * @param sccRefreshLockIn
     */
    public void setSccRefreshLock(FileLocks sccRefreshLockIn) {
        this.sccRefreshLock = sccRefreshLockIn;
    }

    private void updateInstanceData(PaygSshData instance) {
        try {
            PaygInstanceInfo paygData = paygDataExtractor.extractAuthData(instance);
            paygDataProcessor.processPaygInstanceData(instance, paygData);
            instance.setStatus(PaygSshData.Status.S);
            instance.setErrorMessage("");
            PaygSshDataFactory.savePaygSshData(instance);

        }
        catch (PaygDataExtractException | JSchException e) {
            log.error("error getting instance data ", e);
            saveError(instance,  e.getMessage());

        }
        catch (Exception e) {
            log.error("error processing instance data", e);
            // Error message will be empty because we don't want to show this error on UI
            saveError(instance, "");
        }
        finally {
            HibernateFactory.commitTransaction();
            HibernateFactory.closeSession();
        }
    }

    private boolean hasInstanceValidCreds(PaygSshData instance) {
        return Optional.ofNullable(instance.getCredentials())
                .flatMap(c -> c.castAs(CloudRMTCredentials.class))
                .map(CloudRMTCredentials::isValid)
                .orElse(false);
    }

    private void saveError(PaygSshData instance, String errorMessage) {
        // rollback any data changed by the process
        HibernateFactory.rollbackTransaction();
        // Save a special error to know that a problem happened
        if (instance.getStatus().equals(PaygSshData.Status.E) && hasInstanceValidCreds(instance)) {
            NotificationMessage notificationMessage = UserNotificationFactory.createNotificationMessage(
                    new PaygAuthenticationUpdateFailed(instance.getHost(), instance.getId()));
            UserNotificationFactory.storeNotificationMessageFor(notificationMessage,
                    Set.of(AccessGroupFactory.CHANNEL_ADMIN), Optional.empty());
            // was in error state before. At least second time failed to get the data
            // invalidate existing credentials
            paygDataProcessor.invalidateCredentials(instance);
        }

        instance.setStatus(PaygSshData.Status.E);
        instance.setErrorMessage(errorMessage);
        PaygSshDataFactory.savePaygSshData(instance);
    }
}
