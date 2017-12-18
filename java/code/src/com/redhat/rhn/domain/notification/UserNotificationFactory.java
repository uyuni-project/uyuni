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
import com.redhat.rhn.domain.notification.types.NotificationData;
import com.redhat.rhn.domain.org.OrgFactory;
import com.redhat.rhn.domain.role.Role;
import com.redhat.rhn.domain.user.User;
import com.redhat.rhn.domain.user.UserFactory;

import com.suse.manager.webui.websocket.Notification;

import org.apache.log4j.Logger;

import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.Set;

import javax.persistence.criteria.CriteriaBuilder;
import javax.persistence.criteria.CriteriaQuery;
import javax.persistence.criteria.Root;


/**
 * NotificationMessageFactory
 */
public class UserNotificationFactory extends HibernateFactory {

    private static UserNotificationFactory singleton = new UserNotificationFactory();
    private static Logger log = Logger.getLogger(UserNotificationFactory.class);

    private UserNotificationFactory() {
        super();
    }

    /**
     * Create new {@link UserNotification}.
     *
     * @param userIn the user of the notification
     * @param messageIn the message
     * @return new UserNotification
     */
    public static UserNotification create(User userIn, NotificationMessage messageIn) {
        UserNotification userNotification =
                new UserNotification(userIn, messageIn);
        return userNotification;
    }

    /**
     * Store {@link UserNotification} to the database.
     *
     * @param userNotificationIn userNotification
     */
    public static void store(UserNotification userNotificationIn) {
        singleton.saveObject(userNotificationIn);
    }

    /**
     * Create new {@link NotificationMessage}.
     *
     * @return new notificationMessage
     */
    public static NotificationMessage createNotificationMessage(NotificationData notification) {
        NotificationMessage notificationMessage = new NotificationMessage(notification);
        return notificationMessage;
    }

    /**
     * Store {@link NotificationMessage} to the database.
     *
     * @param notificationMessageIn notificationMessage
     * @param rolesIn the user roles the message is visible for
     */
    public static void storeNotificationMessageFor(NotificationMessage notificationMessageIn, Set<Role> rolesIn) {
        // save first the message to get the 'id' auto generated
        // because it is referenced by the UserNotification object
        singleton.saveObject(notificationMessageIn);

        // only users in the current Org
        // do not create notifications for non active users
        // only users with one role in the roles
        UserFactory.getInstance().findAllUsers(OrgFactory.getSatelliteOrg()).stream()
        .filter(user -> !user.isDisabled())
        .filter(user -> !Collections.disjoint(user.getRoles(), rolesIn))
        .forEach(user -> UserNotificationFactory.store(new UserNotification(user, notificationMessageIn)));

        // Update Notification WebSocket Sessions right now
        Notification.spreadUpdate();
    }

    /**
     * Update {@link UserNotification} in the database, set it as read.
     *
     * @param userNotificationIn the userNotification
     * @param isReadIn flag status to set if the message is read or not
     */
    public static void updateStatus(UserNotification userNotificationIn, boolean isReadIn) {
        userNotificationIn.setRead(isReadIn);
        singleton.saveObject(userNotificationIn);
    }

    /**
     * Lookup for all {@link UserNotification}.
     *
     * @param userIn the user
     * @return List of read and unread UserNotifications
     */
    public static List<UserNotification> listAllByUser(User userIn) {
        CriteriaBuilder builder = getSession().getCriteriaBuilder();
        CriteriaQuery<UserNotification> criteria = builder.createQuery(UserNotification.class);
        Root<UserNotification> root = criteria.from(UserNotification.class);
        criteria.where(builder.equal(root.get("userId"), userIn.getId()));

        return getSession().createQuery(criteria).getResultList();
    }

    /**
     * Lookup for unread {@link UserNotification}.
     *
     * @param userIn the user
     * @return List of unread UserNotification only
     */
    public static List<UserNotification> listUnreadByUser(User userIn) {
        CriteriaBuilder builder = getSession().getCriteriaBuilder();
        CriteriaQuery<UserNotification> criteria = builder.createQuery(UserNotification.class);
        Root<UserNotification> root = criteria.from(UserNotification.class);
        criteria.where(
                builder.equal(root.get("userId"), userIn.getId()),
                builder.isFalse(root.get("read")));

        return getSession().createQuery(criteria).getResultList();
    }

    /**
     * Get the count of unread messages
     *
     * @param userIn the user
     * @return the unread messages size count
     */
    public static Long unreadUserNotificationsSize(User userIn) {
        CriteriaBuilder builder = getSession().getCriteriaBuilder();
        CriteriaQuery<Long> criteria = builder.createQuery(Long.class);
        Root<UserNotification> root = criteria.from(UserNotification.class);
        CriteriaQuery<Long> count = criteria.select(builder.count(root));

        criteria.where(
                builder.equal(root.get("userId"), userIn.getId()),
                builder.isFalse(root.get("read")));

        return getSession().createQuery(count).getSingleResult();
    }

    /**
     * Lookup for a single {@link UserNotification} by its id
     *
     * @param messageIdIn the id of the message
     * @param userIn the user
     * @return the Optional wrapper for the UserNotification
     */
    public static Optional<UserNotification> lookupByUserAndMessageId(Long messageIdIn, User userIn) {
        CriteriaBuilder builder = getSession().getCriteriaBuilder();
        CriteriaQuery<UserNotification> query = builder.createQuery(UserNotification.class);

        Root<UserNotification> root = query.from(UserNotification.class);
        query.where(
                builder.equal(root.get("userId"), userIn.getId()),
                builder.equal(root.get("messageId"), messageIdIn));

        return getSession().createQuery(query).uniqueResultOptional();
    }

    @Override
    protected Logger getLogger() {
        return log;
    }
}
