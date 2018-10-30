/**
 * Copyright (c) 2018 SUSE LLC
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

import java.time.LocalDate;
import java.time.ZoneId;
import java.util.Date;

import org.quartz.JobExecutionContext;
import org.quartz.JobExecutionException;

import com.redhat.rhn.common.conf.ConfigDefaults;
import com.redhat.rhn.domain.notification.UserNotificationFactory;

/**
 * Cleanup of notification messages after a configurable lifetime.
 */
public class NotificationsCleanup extends RhnJavaJob {

    @Override
    public void execute(JobExecutionContext arg0In) throws JobExecutionException {
        if (log.isDebugEnabled()) {
            log.debug("start notifications cleanup");
        }

        // Measure time and calculate the total duration
        long start = System.currentTimeMillis();

        int lifetime = ConfigDefaults.get().getNotificationsLifetime();
        Date before = Date.from(LocalDate.now().atStartOfDay().minusDays(lifetime)
                .atZone(ZoneId.systemDefault()).toInstant());
        log.info("Deleting all notification messages created before: " + before);
        int deleted = UserNotificationFactory.deleteNotificationMessagesBefore(before);
        log.info("Notification messages deleted: " + deleted);

        if (log.isDebugEnabled()) {
            long duration = System.currentTimeMillis() - start;
            log.debug("Total duration was: " + duration + " ms");
        }
    }
}
