/*
 * Copyright (c) 2026 SUSE LLC
 *
 * This software is licensed to you under the GNU General Public License,
 * version 2 (GPLv2). There is NO WARRANTY for this software, express or
 * implied, including the implied warranties of MERCHANTABILITY or FITNESS
 * FOR A PARTICULAR PURPOSE. You should have received a copy of GPLv2
 * along with this software; if not, see
 * http://www.gnu.org/licenses/old-licenses/gpl-2.0.txt.
 */
package com.suse.manager.api.test.contract;

import com.redhat.rhn.domain.notification.NotificationMessage;
import com.redhat.rhn.domain.notification.UserNotification;
import com.redhat.rhn.domain.notification.types.NotificationData;
import com.redhat.rhn.domain.notification.types.NotificationType;
import com.redhat.rhn.domain.user.User;
import com.redhat.rhn.frontend.xmlrpc.user.UserNotificationsHandler;

import org.jmock.Expectations;
import org.junit.jupiter.api.Test;

import java.util.Date;
import java.util.List;
import java.util.Map;

public class UserNotificationsHandlerContractTest extends BaseOpenApiTest {

    private static final List<Integer> NOTIFICATION_IDS = List.of(1, 2);

    @Override
    protected String getApiNamespace() {
        return "user.notifications";
    }

    @Override
    protected Class<UserNotificationsHandler> getHandlerClass() {
        return UserNotificationsHandler.class;
    }

    private UserNotificationsHandler handler() {
        return (UserNotificationsHandler) handlerMock;
    }

    /**
     * Builds a notification whose registered serializer produces every documented property.
     *
     * @return the notification
     */
    private UserNotification userNotification() {
        UserNotification notification = context.mock(UserNotification.class, "notification");
        NotificationMessage message = context.mock(NotificationMessage.class, "message");
        NotificationData data = context.mock(NotificationData.class, "data");

        context.checking(new Expectations() {{
            allowing(notification).getId();
            will(returnValue(1L));
            allowing(notification).getRead();
            will(returnValue(false));
            allowing(notification).getMessage();
            will(returnValue(message));
            allowing(message).getData();
            will(returnValue("{\"summary\":\"Onboarding failed\"}"));
            allowing(message).getNotificationData();
            will(returnValue(data));
            allowing(message).getType();
            will(returnValue(NotificationType.ONBOARDING_FAILED));
            allowing(message).getCreated();
            will(returnValue(new Date()));
            allowing(data).getSummary();
            will(returnValue("<strong>Onboarding failed</strong>"));
            allowing(data).getDetails();
            will(returnValue("The minion could not be onboarded."));
        }});

        return notification;
    }

    @Test
    public void testGetNotifications() throws Exception {
        context.checking(new Expectations() {{
            oneOf(handler()).getNotifications(with(mockUser), with(true));
            will(returnValue(List.of(userNotification())));
        }});

        validateApiContract("/user.notifications/getNotifications", "GET")
                .withParams(Map.of("unread", new String[]{"true"}))
                .onHandlerMethod("getNotifications", User.class, boolean.class);
    }

    @Test
    public void testSetNotificationsRead() throws Exception {
        context.checking(new Expectations() {{
            oneOf(handler()).setNotificationsRead(with(mockUser), with(NOTIFICATION_IDS));
            will(returnValue(1));
        }});

        validateApiContract("/user.notifications/setNotificationsRead", "POST")
                .withBody(Map.of("notifications", NOTIFICATION_IDS))
                .onHandlerMethod("setNotificationsRead", User.class, List.class);
    }

    @Test
    public void testSetAllNotificationsRead() throws Exception {
        context.checking(new Expectations() {{
            oneOf(handler()).setAllNotificationsRead(with(mockUser));
            will(returnValue(1));
        }});

        validateApiContract("/user.notifications/setAllNotificationsRead", "POST")
                .onHandlerMethod("setAllNotificationsRead", User.class);
    }

    @Test
    public void testDeleteNotifications() throws Exception {
        context.checking(new Expectations() {{
            oneOf(handler()).deleteNotifications(with(mockUser), with(NOTIFICATION_IDS));
            will(returnValue(1));
        }});

        validateApiContract("/user.notifications/deleteNotifications", "POST")
                .withBody(Map.of("notifications", NOTIFICATION_IDS))
                .onHandlerMethod("deleteNotifications", User.class, List.class);
    }
}
