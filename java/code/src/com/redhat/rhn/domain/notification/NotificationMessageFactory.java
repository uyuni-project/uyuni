/**
 * Copyright (c) 2012 SUSE LLC
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

package com.redhat.rhn.domain.notification;

import com.redhat.rhn.common.hibernate.HibernateFactory;

import com.suse.manager.webui.websocket.Notification;

import org.apache.log4j.Logger;

import java.util.Date;
import java.util.List;

/**
 * NotificationMessageFactory
 */
public class NotificationMessageFactory extends HibernateFactory {

    private static NotificationMessageFactory singleton = new NotificationMessageFactory();
    private static Logger log = Logger.getLogger(NotificationMessageFactory.class);

    private NotificationMessageFactory() {
        super();
    }

    /**
     * Create new empty {@link NotificationMessage}.
     * @return new empty notificationMessage
     */
    public static NotificationMessage createNotificationMessage() {
        NotificationMessage notificationMessage = new NotificationMessage();
        return notificationMessage;
    }

    /**
     * Create new empty {@link NotificationMessage}.
     * @param descriptionIn the text message
     * @param readIn if the message is read
     * @return new empty notificationMessage
     */
    public static NotificationMessage createNotificationMessage(
            String descriptionIn, boolean readIn) {
        NotificationMessage notificationMessage =
                new NotificationMessage(descriptionIn, readIn);
        return notificationMessage;
    }

    /**
     * Store {@link NotificationMessage} to the database.
     * @param notificationMessage notificationMessage
     */
    public static void storeNotificationMessage(NotificationMessage notificationMessage) {
        notificationMessage.setModified(new Date());
        singleton.saveObject(notificationMessage);

        // as soon as a message is stored --> notify all attached sessions via websocket
        Notification.notifyAll(unreadMessagesSizeToString());
    }

    /**
     * Update {@link NotificationMessage} to the database, set it as read.
     * @param notificationMessage notificationMessage
     */
    public static void updateNotificationMessageAsRead(NotificationMessage notificationMessage) {
        notificationMessage.setRead(true);
        notificationMessage.setModified(new Date());
        singleton.saveObject(notificationMessage);

        // as soon as a message is stored --> notify all attached sessions via websocket
        Notification.notifyAll(unreadMessagesSizeToString());
    }

    /**
     * Delete {@link NotificationMessage} from the database.
     * @param notificationMessage notificationMessage
     */
    public static void removeNotificationMessage(NotificationMessage notificationMessage) {
        singleton.removeObject(notificationMessage);
    }

    /**
     * Lookup all NotificationMessages .
     * @return List of notification messages.
     */
    public static List<NotificationMessage> listNotificationMessage() {
        return singleton.listObjectsByNamedQuery("NotificationMessage.listNotificationMessage", null);
    }

    /**
     * Lookup not read NotificationMessages .
     * @return List of not yet read notification messages.
     */
    public static List<NotificationMessage> listNotReadNotificationMessage() {
        return singleton.listObjectsByNamedQuery("NotificationMessage.listNotReadNotificationMessage", null);
    }

    /**
     * Get the count of unread messages
     * @return the unread messages size count
     */
    public static int unreadMessagesSize() {
        return listNotReadNotificationMessage().size();
    }

    /**
     * Get the count of unread messages as a String
     * @return the unread messages size count String
     */
    public static String unreadMessagesSizeToString() {
        return String.valueOf(unreadMessagesSize());
    }

    @Override
    protected Logger getLogger() {
        return log;
    }
}
