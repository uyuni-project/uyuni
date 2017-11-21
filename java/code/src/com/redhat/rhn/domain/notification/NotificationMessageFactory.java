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

import com.redhat.rhn.domain.image.ImageInfo;
import com.suse.manager.webui.websocket.Notification;
import org.apache.log4j.Logger;
import org.hibernate.Session;

import javax.persistence.criteria.CriteriaBuilder;
import javax.persistence.criteria.CriteriaQuery;
import javax.persistence.criteria.Root;
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
        return new NotificationMessage();
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

    public static List<NotificationMessage> listAll() {
        CriteriaBuilder builder = getSession().getCriteriaBuilder();
        CriteriaQuery<NotificationMessage> criteria = builder.createQuery(NotificationMessage.class);
        return getSession().createQuery(criteria).getResultList();
    }

    /**
     * List notification messages by read status.
     * @return List of notification messages.
     */
    public static List<NotificationMessage> byRead(boolean isRead) {
        CriteriaBuilder builder = getSession().getCriteriaBuilder();
        CriteriaQuery<NotificationMessage> query = builder.createQuery(NotificationMessage.class);

        Root<NotificationMessage> root = query.from(NotificationMessage.class);
        query.where(builder.equal(root.get("isRead"), isRead));

        return getSession().createQuery(query).list();
    }

    /**
     * Lookup unread NotificationMessages .
     * @return List of not yet read notification messages.
     */
    public static List<NotificationMessage> listUnread() {
        return byRead(false);
    }

    public static Optional<NotificationMessage> lookupById(Long messageId) {
        CriteriaBuilder builder = getSession().getCriteriaBuilder();
        CriteriaQuery<NotificationMessage> query = builder.createQuery(NotificationMessage.class);

        Root<NotificationMessage> root = query.from(NotificationMessage.class);
        query.where(builder.equal(root.get("id"), messageId));

        return getSession().createQuery(query).uniqueResultOptional();
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
     * @param label The unique label of the type.
     * @return A sought for NotificationMessageType or null
     */
    static Optional<NotificationMessageType> lookupNotificationMessageTypeByLabel(String label) {
        CriteriaBuilder builder = getSession().getCriteriaBuilder();
        CriteriaQuery<NotificationMessageType> criteria = builder.createQuery(NotificationMessageType.class);
        Root<NotificationMessageType> root = criteria.from(NotificationMessageType.class);
        criteria.where(builder.equal(root.get("label"), label));
        return getSession().createQuery(criteria).uniqueResultOptional();
    }

    @Override
    protected Logger getLogger() {
        return log;
    }
}
