/*
 * Copyright (c) 2026 SUSE LLC
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

import com.redhat.rhn.domain.notification.NotificationMessage;
import com.redhat.rhn.domain.notification.UserNotificationFactory;
import com.redhat.rhn.domain.notification.types.DiskCheck;
import com.redhat.rhn.domain.role.RoleFactory;

import com.suse.manager.utils.DBDiskCheckHelper;
import com.suse.manager.utils.DiskCheckHelper;
import com.suse.manager.utils.DiskCheckSeverity;

import org.quartz.JobExecutionContext;

/**
 * DiskCheckTask
 * Periodically checks server and database disk usage and creates
 * notifications when usage reaches the alert threshold.
 */
public class DiskCheckTask extends RhnJavaJob {

    @Override
    public String getConfigNamespace() {
        return "diskcheck-task";
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void execute(JobExecutionContext context) {
        final DiskCheckHelper diskCheckHelper = new DiskCheckHelper();
        DiskCheckSeverity diskCheckSeverity = diskCheckHelper.executeDiskCheck();

        if (diskCheckSeverity.compareTo(DiskCheckSeverity.ALERT) >= 0) {
            NotificationMessage notification = UserNotificationFactory.createNotificationMessage(
                new DiskCheck("system", diskCheckSeverity));
            UserNotificationFactory.storeNotificationMessageFor(notification, RoleFactory.ORG_ADMIN);
        }

        final DBDiskCheckHelper dbDiskCheckHelper = new DBDiskCheckHelper();
        DiskCheckSeverity dbDiskCheckSeverity = dbDiskCheckHelper.executeDiskCheck();
        if (dbDiskCheckSeverity.needsAttention()) {
            NotificationMessage notification = UserNotificationFactory.createNotificationMessage(
                new DiskCheck("database", dbDiskCheckSeverity));
            UserNotificationFactory.storeNotificationMessageFor(notification, RoleFactory.ORG_ADMIN);
        }
    }

}
