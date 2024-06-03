/*
 * Copyright (c) 2024 SUSE LLC
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
package com.redhat.rhn.frontend.xmlrpc.user;

import com.redhat.rhn.domain.notification.UserNotification;
import com.redhat.rhn.domain.notification.UserNotificationFactory;
import com.redhat.rhn.domain.user.User;
import com.redhat.rhn.frontend.xmlrpc.BaseHandler;

import com.suse.manager.api.ReadOnly;

import java.util.Collection;
import java.util.List;

public class UserNotificationsHandler extends BaseHandler {

    /**
     * @param user The current user
     * @param unread Unread notifications
     * @return Returns a list of notifications
     * @apidoc.doc Get all notifications from a user.
     * @apidoc.param #session_key()
     * @apidoc.param #param_desc("boolean", "unread", "Read notifications.")
     * @apidoc.returntype
     * #return_array_begin()
     *     $UserNotificationSerializer
     * #array_end()
     */
    @ReadOnly
    public List<UserNotification> getNotifications(User user, boolean unread) {
        List<UserNotification> notifications;

        if (unread) {
            notifications = UserNotificationFactory.listUnreadByUser(user);
        }
        else {
            notifications = UserNotificationFactory.listAllByUser(user);
        }
        return notifications;
    }

    /**
     * @return Returns 1 if successful
     * @param user The current user
     * @param notifications Notification list
     * @apidoc.doc Makes a notification raed
     * @apidoc.param #session_key()
     * @apidoc.param #param_desc("Collection", "notifications", "The target notification.")
     * @apidoc.returntype #return_int_success()
     */
    public int makeNotificationsRead(User user, Collection<Integer> notifications) {
        for (UserNotification notification : getNotifications(user, true)) {
            if (notifications.stream()
                    .map(Long::valueOf)
                    .anyMatch(l -> l.equals(notification.getId()))) {
                UserNotificationFactory.updateStatus(notification, true);
            }
        }
        return 1;
    }

    /**
     * @param user The current user
     * @return Returns 1 if successful
     * @apidoc.doc Makes all notifications from a user read
     * @apidoc.param #session_key()
     * @apidoc.returntype #return_int_success()
     */
    public int makeAllNotificationsRead(User user) {
        for (UserNotification notification : getNotifications(user, true)) {
            UserNotificationFactory.updateStatus(notification, true);
        }
        return 1;
    }

    /**
     * @param notifications The notifications to delete
     * @param user The current user
     * @return int number of deleted notifications
     * @apidoc.doc Deletes multiple notifications
     * @apidoc.param #session_key()
     * @apidoc.param #param_desc("collection", "notifications", "List of notifications.")
     * @apidoc.returntype #return_int_success()
     */
    public int deleteNotification(User user, Collection<UserNotification> notifications) {
        UserNotificationFactory.delete(notifications);
        return 1;
    }

}
