/**
 * Copyright (c) 2016 SUSE LLC
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
package com.suse.manager.webui.services;

import com.redhat.rhn.domain.notification.NotificationMessageFactory;
import com.redhat.rhn.domain.user.User;

import java.util.concurrent.TimeUnit;

/**
 * Collect data for the Message web UI page
 */
public class NotificationMessageCollector {

    // latest slice of time to collect notification messages is fixed to 60 minutes
    public static final long SLICE_TIME = TimeUnit.MINUTES.toMillis(60);

    /**
     * Gets UI-ready data.
     *
     * @param userIn the current user
     * @return the data
     */
    public Object getData(User userIn) {
        return NotificationMessageFactory.listNotificationMessage();
    }
}
