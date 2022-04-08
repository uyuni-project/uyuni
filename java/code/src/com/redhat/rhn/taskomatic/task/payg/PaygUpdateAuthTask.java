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

import com.redhat.rhn.common.hibernate.HibernateFactory;
import com.redhat.rhn.common.localization.LocalizationService;
import com.redhat.rhn.domain.cloudpayg.PaygSshData;
import com.redhat.rhn.domain.cloudpayg.PaygSshDataFactory;
import com.redhat.rhn.domain.notification.NotificationMessage;
import com.redhat.rhn.domain.notification.UserNotificationFactory;
import com.redhat.rhn.domain.notification.types.PaygAuthenticationUpdateFailed;
import com.redhat.rhn.domain.role.RoleFactory;
import com.redhat.rhn.taskomatic.task.RhnJavaJob;
import com.redhat.rhn.taskomatic.task.payg.beans.PaygInstanceInfo;

import com.jcraft.jsch.JSchException;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.quartz.JobExecutionContext;
import org.quartz.JobExecutionException;

import java.util.Collections;
import java.util.Optional;

public class PaygUpdateAuthTask extends RhnJavaJob {

    private final PaygAuthDataProcessor paygDataProcessor = new PaygAuthDataProcessor();
    private PaygAuthDataExtractor paygDataExtractor = new PaygAuthDataExtractor();

    private static final Logger LOG = LogManager.getLogger(PaygUpdateAuthTask.class);

    private static final String KEY_ID = "sshData_id";

    @Override
    public String getConfigNamespace() {
        return "payg";
    }

    @Override
    public void execute(JobExecutionContext jobExecutionContext) throws JobExecutionException {
        log.debug("Running PaygUpdateAuthTask");
        if (jobExecutionContext != null && jobExecutionContext.getJobDetail().getJobDataMap().containsKey(KEY_ID)) {
            Optional<PaygSshData> paygData = PaygSshDataFactory.lookupById(
                    Integer.parseInt((String) jobExecutionContext.getJobDetail().getJobDataMap().get(KEY_ID)));
            paygData.ifPresent(this::updateInstanceData);
        }
        else {
            PaygSshDataFactory.lookupPaygSshData()
                    .forEach(this::updateInstanceData);
        }
    }

    /**
     * Need for automatic tests
     * @param paygDataExtractorIn
     */
    public void setPaygDataExtractor(PaygAuthDataExtractor paygDataExtractorIn) {
        this.paygDataExtractor = paygDataExtractorIn;
    }

    private void updateInstanceData(PaygSshData instance) {
        PaygInstanceInfo paygData;
        LocalizationService ls = LocalizationService.getInstance();
        try {
            paygData = paygDataExtractor.extractAuthData(instance);
            paygDataProcessor.processPaygInstanceData(instance, paygData);
            instance.setStatus(PaygSshData.Status.S);
            instance.setErrorMessage("");
            PaygSshDataFactory.savePaygSshData(instance);

        }
        catch (PaygDataExtractException | JSchException e) {
            LOG.error("error getting instance data ", e);
            saveError(instance,  e.getMessage());

        }
        catch (Exception e) {
            LOG.error("error processing instance data", e);
            // Error message will be empty because we don't want to show this error on UI
            saveError(instance, "");
        }
        finally {
            HibernateFactory.commitTransaction();
            HibernateFactory.closeSession();
        }
    }

    private void saveError(PaygSshData instance, String errorMessage) {
        // rollback any data changed by the process
        HibernateFactory.rollbackTransaction();
        // Save a special error to know that a problem happened
        if (!instance.getStatus().equals(PaygSshData.Status.E)) {
            NotificationMessage notificationMessage = UserNotificationFactory.createNotificationMessage(
                    new PaygAuthenticationUpdateFailed(instance.getHost(), instance.getId()));
            UserNotificationFactory.storeNotificationMessageFor(notificationMessage,
                    Collections.singleton(RoleFactory.CHANNEL_ADMIN), Optional.empty());
        }
        instance.setStatus(PaygSshData.Status.E);
        instance.setErrorMessage(errorMessage);
        PaygSshDataFactory.savePaygSshData(instance);
    }
}
