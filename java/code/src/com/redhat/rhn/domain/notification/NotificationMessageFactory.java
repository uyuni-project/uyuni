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

import org.apache.log4j.Logger;
import org.hibernate.Session;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

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
     * Create new {@link NotificationMessage}.
     *
     * @param typeIn the type of the message
     * @param descriptionIn the text message
     * @param readIn if the message is read
     * @return new empty notificationMessage
     */
    public static NotificationMessage createNotificationMessage(
            NotificationMessageType typeIn, String descriptionIn, boolean readIn) {
        NotificationMessage notificationMessage =
                new NotificationMessage(typeIn, descriptionIn, readIn);
        return notificationMessage;
    }

    /**
     * Create new {@link NotificationMessage}.
     *
     * @param typeIn the label type of the message
     * @param descriptionIn the text message
     * @param readIn if the message is read
     * @return new empty notificationMessage
     */
    public static NotificationMessage createNotificationMessage(
            String typeIn, String descriptionIn, boolean readIn) {
        NotificationMessage notificationMessage =
                new NotificationMessage(typeIn, descriptionIn, readIn);
        return notificationMessage;
    }

    /**
     * Store {@link NotificationMessage} to the database.
     * @param notificationMessage notificationMessage
     */
    public static void store(NotificationMessage notificationMessage) {
        singleton.saveObject(notificationMessage);
    }

    /**
     * Update {@link NotificationMessage} to the database, set it as read.
     * @param notificationMessage notificationMessage
     * @param isRead flag status to set if the message is read or not
     */
    public static void updateStatus(NotificationMessage notificationMessage, boolean isRead) {
        notificationMessage.setIsRead(isRead);
        singleton.saveObject(notificationMessage);
    }

    /**
     * Delete {@link NotificationMessage} from the database.
     * @param notificationMessage notificationMessage
     */
    public static void remove(NotificationMessage notificationMessage) {
        singleton.removeObject(notificationMessage);
    }

    /**
     * Lookup all NotificationMessages .
     * @return List of notification messages.
     */
    public static List<NotificationMessage> listAll() {
        return singleton.listObjectsByNamedQuery("NotificationMessage.listAllNotificationMessage", null);
    }

    /**
     * Lookup unread NotificationMessages .
     * @return List of not yet read notification messages.
     */
    public static List<NotificationMessage> listUnread() {
        return singleton.listObjectsByNamedQuery("NotificationMessage.listUnreadNotificationMessage", null);
    }

    public static Optional<NotificationMessage> lookupById(Long messageId) {
        Map<String, Long> qryParams = new HashMap<>();
        qryParams.put("message_id", messageId);
        return Optional.ofNullable(
                (NotificationMessage) singleton.lookupObjectByNamedQuery("NotificationMessage.lookupById", qryParams));
    }

    /**
     * Get the count of unread messages
     * @return the unread messages size count
     */
    public static int unreadMessagesSize() {
        return listUnread().size();
    }

    /**
     * Get the count of unread messages as a String
     * @return the unread messages size count String
     */
    public static String unreadMessagesSizeToString() {
        return String.valueOf(unreadMessagesSize());
    }

    /**
     * Used to look up NotificationMessageType.
     *
     * @param id The unique id of the type.
     * @return A sought for NotificationMessageType or null
     */
    static Optional<NotificationMessageType> lookupNotificationMessageTypeById(Long id) {
        Session session = HibernateFactory.getSession();
        return session.getNamedQuery("NotificationMessageType.lookupById")
                .setParameter("type_id", id)
                //Retrieve from cache if there
                .setCacheable(true)
                .uniqueResultOptional();
    }

    /**
     * Used to look up NotificationMessageType.
     *
     * @param label The unique label of the type.
     * @return A sought for NotificationMessageType or null
     */
    static NotificationMessageType lookupNotificationMessageTypeByLabel(String label) {
        Session session = HibernateFactory.getSession();
        return (NotificationMessageType) session.getNamedQuery("NotificationMessageType.lookupByLabel")
                .setParameter("type_label", label)
                //Retrieve from cache if there
                .setCacheable(true)
                .uniqueResult();
    }

    @Override
    protected Logger getLogger() {
        return log;
    }
}
