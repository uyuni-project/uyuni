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

package com.redhat.rhn.domain.notification;

import com.redhat.rhn.common.hibernate.HibernateFactory;
import com.redhat.rhn.domain.notification.NotificationMessage.NotificationMessageSeverity;

import com.redhat.rhn.domain.org.Org;
import com.redhat.rhn.domain.role.Role;
import com.redhat.rhn.domain.user.User;
import org.apache.log4j.Logger;

import javax.persistence.criteria.CriteriaBuilder;
import javax.persistence.criteria.CriteriaQuery;
import javax.persistence.criteria.Predicate;
import javax.persistence.criteria.Root;
import java.util.*;
import java.util.stream.Collectors;

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
     * @param severityIn the label type of the message
     * @param descriptionIn the text message
     * @return new empty notificationMessage
     */
    public static NotificationMessage createNotificationMessage(
            NotificationMessageSeverity severityIn, String descriptionIn) {
        NotificationMessage notificationMessage =
                new NotificationMessage(severityIn, descriptionIn);
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
     * Lookup for all {@link NotificationMessage}.
     *
     * @return List of read and unread messages
     */
    public static List<NotificationMessage> listAllByUser(User user) {
        CriteriaBuilder builder = getSession().getCriteriaBuilder();
        CriteriaQuery<NotificationMessage> query = builder.createQuery(NotificationMessage.class);
        query.from(NotificationMessage.class);

        return getSession().createQuery(query).getResultList().stream()
                .filter(e -> {
                    return !Collections.disjoint(e.getRoles(), user.getRoles());
                }).collect(Collectors.toList());
    }

    /**
     * List notification messages by read status.
     *
     * @param isRead the read flag criterion to filter messages
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
     * List notification messages by read status.
     *
     * @param isRead the read flag criterion to filter messages
     * @return List of notification messages.
     */
    public static List<NotificationMessage> byRead(boolean isRead, Org org) {
        CriteriaBuilder builder = getSession().getCriteriaBuilder();
        CriteriaQuery<NotificationMessage> query = builder.createQuery(NotificationMessage.class);

        Root<NotificationMessage> root = query.from(NotificationMessage.class);
        query.where(
            builder.and(
                builder.or(
                  builder.equal(root.get("org"), org),
                  builder.isNull(root.get("org"))
                ),
                builder.equal(root.get("isRead"), isRead)
            )
        );

        return getSession().createQuery(query).list();
    }

    /**
     * Lookup unread NotificationMessages .
     * @return List of not yet read notification messages.
     */
    public static List<NotificationMessage> listUnread() {
        return byRead(false);
    }

    public static List<NotificationMessage> listUnreadByUser(User user) {
        return byRead(false, user.getOrg()).stream().filter(e -> {
            return !Collections.disjoint(e.getRoles(), user.getRoles());
        }).collect(Collectors.toList());
    }

    /**
     * Lookup for a single {@link NotificationMessage} by its id
     *
     * @param messageId the id of the message
     * @return the Optional wrapper for the message
     */
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
    public static int unreadMessagesSize(User user) {
        return listUnreadByUser(user).size();
    }

    @Override
    protected Logger getLogger() {
        return log;
    }
}
