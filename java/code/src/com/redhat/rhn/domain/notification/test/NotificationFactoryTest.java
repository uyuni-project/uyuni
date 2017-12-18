/**
 * Copyright (c) 2017 SUSE LLC
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

package com.redhat.rhn.domain.notification.test;

import com.redhat.rhn.domain.notification.NotificationMessage;
import com.redhat.rhn.domain.notification.UserNotification;
import com.redhat.rhn.domain.notification.UserNotificationFactory;
import com.redhat.rhn.domain.notification.types.OnboardingFailed;
import com.redhat.rhn.domain.role.RoleFactory;
import com.redhat.rhn.testing.BaseTestCaseWithUser;

import java.util.Collections;
import java.util.List;


public class NotificationFactoryTest extends BaseTestCaseWithUser {


    public final void testVisibilityForNoRoles() {
        assertEquals(0, UserNotificationFactory.unreadUserNotificationsSize(user));
        NotificationMessage msg = UserNotificationFactory.createNotificationMessage(new OnboardingFailed("minion1"));
        UserNotificationFactory.storeNotificationMessageFor(msg, Collections.emptySet());

        assertEquals(1, UserNotificationFactory.unreadUserNotificationsSize(user));
        assertEquals(1, UserNotificationFactory.listUnreadByUser(user).size());
        assertEquals(1, UserNotificationFactory.listAllByUser(user).size());
    }

    public final void testVisibilityForWrongRoles() {
        assertEquals(0, UserNotificationFactory.unreadUserNotificationsSize(user));
        NotificationMessage msg = UserNotificationFactory.createNotificationMessage(new OnboardingFailed("minion1"));
        UserNotificationFactory.storeNotificationMessageFor(msg, Collections.singleton(RoleFactory.CHANNEL_ADMIN));

        assertEquals(0, UserNotificationFactory.unreadUserNotificationsSize(user));
        assertEquals(0, UserNotificationFactory.listUnreadByUser(user).size());
        assertEquals(0, UserNotificationFactory.listAllByUser(user).size());
    }

    public final void testUpdateReadFlag() {
        assertEquals(0, UserNotificationFactory.unreadUserNotificationsSize(user));
        NotificationMessage msg = UserNotificationFactory.createNotificationMessage(new OnboardingFailed("minion1"));
        UserNotificationFactory.storeNotificationMessageFor(msg, Collections.emptySet());

        List<UserNotification> unread = UserNotificationFactory.listUnreadByUser(user);

        assertEquals(1, UserNotificationFactory.unreadUserNotificationsSize(user));
        assertEquals(1, unread.size());
        assertEquals(1, UserNotificationFactory.listAllByUser(user).size());

        UserNotificationFactory.updateStatus(unread.get(0), true);

        assertEquals(0, UserNotificationFactory.unreadUserNotificationsSize(user));
        assertEquals(0, UserNotificationFactory.listUnreadByUser(user).size());
        assertEquals(1, UserNotificationFactory.listAllByUser(user).size());

        UserNotificationFactory.updateStatus(unread.get(0), false);

        assertEquals(1, UserNotificationFactory.unreadUserNotificationsSize(user));
        assertEquals(1, UserNotificationFactory.listUnreadByUser(user).size());
        assertEquals(1, UserNotificationFactory.listAllByUser(user).size());
    }

}
