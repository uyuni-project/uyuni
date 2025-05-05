/*
 * Copyright (c) 2017--2025 SUSE LLC
 *
 * This software is licensed to you under the GNU General Public License,
 * version 2 (GPLv2). There is NO WARRANTY for this software, express or
 * implied, including the implied warranties of MERCHANTABILITY or FITNESS
 * FOR A PARTICULAR PURPOSE. You should have received a copy of GPLv2
 * along with this software; if not, see
 * http://www.gnu.org/licenses/old-licenses/gpl-2.0.txt.
 */

package com.redhat.rhn.domain.notification.test;

import static org.junit.jupiter.api.Assertions.assertEquals;

import com.redhat.rhn.common.hibernate.HibernateFactory;
import com.redhat.rhn.common.messaging.test.MockMail;
import com.redhat.rhn.domain.access.AccessGroupFactory;
import com.redhat.rhn.domain.notification.NotificationMessage;
import com.redhat.rhn.domain.notification.UserNotification;
import com.redhat.rhn.domain.notification.UserNotificationFactory;
import com.redhat.rhn.domain.notification.types.ChannelSyncFinished;
import com.redhat.rhn.domain.notification.types.OnboardingFailed;
import com.redhat.rhn.domain.notification.types.StateApplyFailed;
import com.redhat.rhn.testing.BaseTestCaseWithUser;
import com.redhat.rhn.testing.TestUtils;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.Date;
import java.util.List;
import java.util.Set;


public class NotificationFactoryTest extends BaseTestCaseWithUser {

    private MockMail mailer;
    @Override
    @BeforeEach
    public void setUp() throws Exception {
        super.setUp();
        mailer = new MockMail();
    }

    @Test
    public final void testVisibilityForNoRoles() {
        mailer.setExpectedSendCount(1);
        UserNotificationFactory.setMailer(mailer);
        assertEquals(0, UserNotificationFactory.unreadUserNotificationsSize(user));
        NotificationMessage msg = UserNotificationFactory.createNotificationMessage(new OnboardingFailed("minion1"));
        UserNotificationFactory.storeNotificationMessageFor(msg);

        assertEquals(1, UserNotificationFactory.unreadUserNotificationsSize(user));
        assertEquals(1, UserNotificationFactory.listUnreadByUser(user).size());
        assertEquals(1, UserNotificationFactory.listAllByUser(user).size());
        mailer.verify();
        assertContains(mailer.getBody(), "minion1");
    }

    @Test
    public final void testEmailContentStripHTML() {
        mailer.setExpectedSendCount(1);
        UserNotificationFactory.setMailer(mailer);
        assertEquals(0, UserNotificationFactory.unreadUserNotificationsSize(user));
        NotificationMessage msg = UserNotificationFactory.createNotificationMessage(
                new StateApplyFailed("minion1", 10000010000L, 42L));
        UserNotificationFactory.storeNotificationMessageFor(msg);

        assertEquals(1, UserNotificationFactory.unreadUserNotificationsSize(user));
        assertEquals(1, UserNotificationFactory.listUnreadByUser(user).size());
        assertEquals(1, UserNotificationFactory.listAllByUser(user).size());
        mailer.verify();
        assertContains(mailer.getBody(), "minion1");
    }

    @Test
    public final void testUserOptOutForMail() {
        mailer.setExpectedSendCount(0);
        UserNotificationFactory.setMailer(mailer);
        user.setEmailNotify(0);

        assertEquals(0, UserNotificationFactory.unreadUserNotificationsSize(user));
        NotificationMessage msg = UserNotificationFactory.createNotificationMessage(new OnboardingFailed("minion1"));
        UserNotificationFactory.storeNotificationMessageFor(msg);

        assertEquals(1, UserNotificationFactory.unreadUserNotificationsSize(user));
        assertEquals(1, UserNotificationFactory.listUnreadByUser(user).size());
        assertEquals(1, UserNotificationFactory.listAllByUser(user).size());
        mailer.verify();
    }

    @Test
    public final void testUserDisabledNotification() {
        mailer.setExpectedSendCount(0);
        UserNotificationFactory.setMailer(mailer);
        user.setEmailNotify(0);

        assertEquals(0, UserNotificationFactory.unreadUserNotificationsSize(user));
        NotificationMessage msg = UserNotificationFactory.createNotificationMessage(
                new ChannelSyncFinished(1L, "dumma-channel"));
        UserNotificationFactory.storeNotificationMessageFor(msg);

        assertEquals(0, UserNotificationFactory.unreadUserNotificationsSize(user));
        assertEquals(0, UserNotificationFactory.listUnreadByUser(user).size());
        assertEquals(0, UserNotificationFactory.listAllByUser(user).size());
        mailer.verify();
    }

    @Test
    public final void testVisibilityForWrongRoles() {
        mailer.setExpectedSendCount(0);
        UserNotificationFactory.setMailer(mailer);
        assertEquals(0, UserNotificationFactory.unreadUserNotificationsSize(user));
        NotificationMessage msg = UserNotificationFactory.createNotificationMessage(new OnboardingFailed("minion1"));
        UserNotificationFactory.storeNotificationMessageFor(msg, Set.of(AccessGroupFactory.CHANNEL_ADMIN));

        assertEquals(0, UserNotificationFactory.unreadUserNotificationsSize(user));
        assertEquals(0, UserNotificationFactory.listUnreadByUser(user).size());
        assertEquals(0, UserNotificationFactory.listAllByUser(user).size());
        mailer.verify();
    }

    @Test
    public final void testUpdateReadFlag() {
        UserNotificationFactory.setMailer(mailer);
        assertEquals(0, UserNotificationFactory.unreadUserNotificationsSize(user));
        NotificationMessage msg = UserNotificationFactory.createNotificationMessage(new OnboardingFailed("minion1"));
        UserNotificationFactory.storeNotificationMessageFor(msg);

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

    @Test
    public final void testDeleteNotificationMessagesBefore() {
        UserNotificationFactory.setMailer(mailer);
        // Clean up all notifications that might be present
        if (!UserNotificationFactory.listAllNotificationMessages().isEmpty()) {
            UserNotificationFactory.deleteNotificationMessagesBefore(Date.from(Instant.now()));
        }
        assertEquals(0, UserNotificationFactory.listAllNotificationMessages().size());

        // Create a notification
        NotificationMessage msg = UserNotificationFactory.createNotificationMessage(new OnboardingFailed("minion1"));
        UserNotificationFactory.storeNotificationMessageFor(msg, Set.of(AccessGroupFactory.CHANNEL_ADMIN));

        msg = TestUtils.reload(msg);

        // Should not be deleted at creation date
        Date deleteBeforeDate = msg.getCreated();
        int result = UserNotificationFactory.deleteNotificationMessagesBefore(deleteBeforeDate);
        assertEquals(0, result);
        assertEquals(1, UserNotificationFactory.listAllNotificationMessages().size());

        // Should be deleted 1 second after creation date
        deleteBeforeDate = Date.from(msg.getCreated().toInstant().plus(1, ChronoUnit.SECONDS));
        result = UserNotificationFactory.deleteNotificationMessagesBefore(deleteBeforeDate);
        assertEquals(1, result);
        assertEquals(0, UserNotificationFactory.listAllNotificationMessages().size());
    }

    @Test
    public final void testDeleteNotificationMessages() {
        UserNotificationFactory.setMailer(mailer);
        // Clean up all notifications that might be present
        if (!UserNotificationFactory.listAllNotificationMessages().isEmpty()) {
            UserNotificationFactory.deleteNotificationMessagesBefore(Date.from(Instant.now()));
        }
        assertEquals(0, UserNotificationFactory.listAllNotificationMessages().size());

        // Create notifications
        NotificationMessage msg = UserNotificationFactory.createNotificationMessage(new OnboardingFailed("minion1"));
        UserNotificationFactory.storeNotificationMessageFor(msg);

        // There should be one present
        assertEquals(1, UserNotificationFactory.listAllNotificationMessages().size());
        List<UserNotification> unread = UserNotificationFactory.listUnreadByUser(user);
        assertEquals(1, unread.size());

        // Try deleting
        int result = UserNotificationFactory.delete(unread);
        assertEquals(1, result);

        HibernateFactory.getSession().flush();

        // Should be deleted
        assertEquals(0, UserNotificationFactory.listUnreadByUser(user).size());

        // Try deleting again
        int resultAfter = UserNotificationFactory.delete(unread);

        // Should not be deleted
        assertEquals(0, resultAfter);
    }
}
