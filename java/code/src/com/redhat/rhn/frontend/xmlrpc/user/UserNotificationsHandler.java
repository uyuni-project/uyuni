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

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.util.List;
import java.util.Objects;
import java.util.Optional;

public class UserNotificationsHandler extends BaseHandler {
    private static final Logger LOG = LogManager.getLogger(UserNotificationsHandler.class);

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
     * @apidoc.doc Set notifications of the given user as read
     * @apidoc.param #session_key()
     * @apidoc.param #param_desc("Collection", "notifications", "The target notification.")
     * @apidoc.returntype #return_int_success()
     */
    public int setNotificationsRead(User user, List<Integer> notifications) {
        notifications.stream()
                .map(Long::valueOf)
                .forEach(id -> {
                    Optional<UserNotification> optNotification = UserNotificationFactory.lookupByUserAndId(id, user);
                    optNotification.ifPresentOrElse(
                            notification -> UserNotificationFactory.updateStatus(notification, true),
                            () -> LOG.error("Notification ID {} not found", id));
                    });
        return 1;
    }

    /**
     * @param user The current user
     * @return Returns 1 if successful
     * @apidoc.doc Set all notifications from a user as read
     * @apidoc.param #session_key()
     * @apidoc.returntype #return_int_success()
     */
    public int setAllNotificationsRead(User user) {
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
     * @apidoc.param #array_single_desc("int", "notifications", "The list of notification IDs to delete.")
     * @apidoc.returntype #return_int_success()
     */
    public int deleteNotifications(User user, List<Integer> notifications) {

        List<UserNotification> collect = notifications.stream()
                .map(Long::valueOf)
                .map(id -> {
                    Optional<UserNotification> optNotification = UserNotificationFactory.lookupByUserAndId(id, user);
                    return optNotification.orElseGet(() -> {
                        LOG.error("Notification ID {} not found", id);
                        return null;
                    });
                })
                .filter(Objects::nonNull)
                .toList();
        int cnt = UserNotificationFactory.delete(collect);
        return cnt == notifications.size() ? 1 : 0;
    }

}
