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

import com.redhat.rhn.common.conf.ConfigDefaults;
import com.redhat.rhn.common.hibernate.HibernateFactory;
import com.redhat.rhn.domain.notification.types.NotificationData;
import com.redhat.rhn.domain.org.Org;
import com.redhat.rhn.domain.role.Role;
import com.redhat.rhn.domain.user.User;
import com.redhat.rhn.domain.user.UserFactory;

import com.suse.manager.webui.websocket.Notification;

import org.apache.log4j.Logger;

import java.util.Collections;
import java.util.Date;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import javax.persistence.criteria.CriteriaBuilder;
import javax.persistence.criteria.CriteriaDelete;
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
     * Check if the given user notification is currently disabled
     *
     * @param userNotificationIn userNotification
     * @return boolean if notification type is disabled
     */
    public static boolean isNotificationTypeDisabled(UserNotification userNotificationIn) {
        List<String> disableNotificationsBy = ConfigDefaults.get().getNotificationsTypeDisabled();

        return disableNotificationsBy.contains(userNotificationIn.getMessage().getType().name());
    }

    /**
     * Store {@link UserNotification} to the database.
     *
     * @param userNotificationIn userNotification
     */
    public static void store(UserNotification userNotificationIn) {
        // We want to disable out the notifications defined on parameter: java.notifications_type_disabled
        // They are still added to the SuseNotificationTable but not associated with any user
        if (!isNotificationTypeDisabled(userNotificationIn)) {
            singleton.saveObject(userNotificationIn);
        }
    }

    /**
     * Remove {@link UserNotification} from the database.
     *
     * @param userNotificationIn userNotification
     */
    public static void remove(UserNotification userNotificationIn) {
        singleton.removeObject(userNotificationIn);
    }

    /**
     * Create new {@link NotificationMessage}.
     *
     * @param notification notification data
     * @return new notificationMessage
     */
    public static NotificationMessage createNotificationMessage(NotificationData notification) {
        NotificationMessage notificationMessage = new NotificationMessage(notification);
        return notificationMessage;
    }

    /**
     * Stores a notification visible for the specified users.
     *
     * @param notificationMessageIn notification to store
     * @param users user that should see the notification
     */
    public static void storeForUsers(NotificationMessage notificationMessageIn, Set<User> users) {
        // save first the message to get the 'id' auto generated
        // because it is referenced by the UserNotification object
        singleton.saveObject(notificationMessageIn);
        users.forEach(user -> UserNotificationFactory.store(new UserNotification(user, notificationMessageIn)));

        // Update Notification WebSocket Sessions right now
        Notification.spreadUpdate();
    }

    /**
     * Stores a notification visible for users that match both the given roles and org.
     *
     * @param notificationMessageIn notification to store
     * @param rolesIn roles to determin which users should see the notification.
     * @param org org users need to be in to see the notification.
     */
    public static void storeNotificationMessageFor(NotificationMessage notificationMessageIn,
        Set<Role> rolesIn, Optional<Org> org) {
        // only users in the current Org
        // do not create notifications for non active users
        // only users with one role in the roles
        Stream<User> allUsers = UserFactory.getInstance().findAllUsers(org).stream()
                .filter(user -> !user.isDisabled());

        if (rolesIn.isEmpty()) {
            storeForUsers(notificationMessageIn, allUsers.collect(Collectors.toSet()));
        }
        else {
            storeForUsers(
                    notificationMessageIn,
                    allUsers.filter(user -> !Collections.disjoint(user.getRoles(), rolesIn)).collect(Collectors.toSet())
            );
        }

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
    public static long unreadUserNotificationsSize(User userIn) {
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

    /**
     * List all notification messages from the database.
     *
     * @return list of all notifications from the database
     */
    public static List<NotificationMessage> listAllNotificationMessages() {
        CriteriaBuilder builder = getSession().getCriteriaBuilder();
        CriteriaQuery<NotificationMessage> criteria = builder.createQuery(NotificationMessage.class);
        criteria.from(NotificationMessage.class);
        return getSession().createQuery(criteria).getResultList();
    }

    /**
     * Delete all notification messages that were created before a given date.
     *
     * @param before all notifications created before this date will be deleted
     * @return int number of deleted notification messages
     */
    public static int deleteNotificationMessagesBefore(Date before) {
        CriteriaBuilder builder = getSession().getCriteriaBuilder();
        CriteriaDelete<NotificationMessage> delete = builder.createCriteriaDelete(NotificationMessage.class);
        Root<NotificationMessage> root = delete.from(NotificationMessage.class);
        delete.where(builder.lessThan(root.<Date>get("created"), before));
        return getSession().createQuery(delete).executeUpdate();
    }

    @Override
    protected Logger getLogger() {
        return log;
    }
}
