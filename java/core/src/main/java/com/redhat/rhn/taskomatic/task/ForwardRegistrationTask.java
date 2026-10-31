/*
 * Copyright (c) 2021--2025 SUSE LLC
 *
 * This software is licensed to you under the GNU General Public License,
 * version 2 (GPLv2). There is NO WARRANTY for this software, express or
 * implied, including the implied warranties of MERCHANTABILITY or FITNESS
 * FOR A PARTICULAR PURPOSE. You should have received a copy of GPLv2
 * along with this software; if not, see
 * http://www.gnu.org/licenses/old-licenses/gpl-2.0.txt.
 *
 * SPDX-License-Identifier: GPL-2.0-only
 */
package com.redhat.rhn.taskomatic.task;

import com.redhat.rhn.GlobalInstanceHolder;
import com.redhat.rhn.common.conf.Config;
import com.redhat.rhn.common.conf.ConfigDefaults;
import com.redhat.rhn.domain.notification.NotificationMessage;
import com.redhat.rhn.domain.notification.UserNotificationFactory;
import com.redhat.rhn.domain.notification.types.NotificationType;
import com.redhat.rhn.domain.notification.types.SCCOptOutWarning;
import com.redhat.rhn.domain.role.RoleFactory;
import com.redhat.rhn.manager.content.ContentSyncManager;

import com.suse.scc.SCCTaskManager;

import org.apache.commons.lang3.time.DateUtils;
import org.quartz.JobExecutionContext;

import java.time.LocalDateTime;
import java.util.Date;
import java.util.concurrent.ThreadLocalRandom;

public class ForwardRegistrationTask extends RhnJavaJob {

    // initialize first run between 30 minutes and 3 hours from now
    protected static LocalDateTime nextLastSeenUpdateRun = LocalDateTime.now().plusMinutes(
            ThreadLocalRandom.current().nextInt(30, 3 * 60));

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

            SCCTaskManager sccTaskManager = new SCCTaskManager();
            boolean updateLastSeenUpdateRun =
                    sccTaskManager.executeSCCTasksWithinRandomMinutes(15, nextLastSeenUpdateRun);
            if (updateLastSeenUpdateRun) {
                updateLastSeenUpdateRun();
            }
        }
    }

    private void updateLastSeenUpdateRun() {
        // next run in 22 - 26 hours
        synchronized (this) {
            nextLastSeenUpdateRun = nextLastSeenUpdateRun.plusMinutes(
                    ThreadLocalRandom.current().nextInt(22 * 60, 26 * 60));
        }
    }
}
