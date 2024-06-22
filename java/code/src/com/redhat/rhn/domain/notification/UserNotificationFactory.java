/*
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

import com.redhat.rhn.common.conf.Config;
import com.redhat.rhn.common.conf.ConfigDefaults;
import com.redhat.rhn.common.hibernate.HibernateFactory;
import com.redhat.rhn.common.messaging.Mail;
import com.redhat.rhn.common.messaging.SmtpMail;
import com.redhat.rhn.domain.notification.types.NotificationData;
import com.redhat.rhn.domain.org.Org;
import com.redhat.rhn.domain.role.Role;
import com.redhat.rhn.domain.user.User;
import com.redhat.rhn.domain.user.UserFactory;

import com.suse.manager.utils.MailHelper;
import com.suse.manager.webui.websocket.Notification;

import org.apache.commons.lang3.StringUtils;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.util.Collection;
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

    private static Logger log = LogManager.getLogger(UserNotificationFactory.class);
    private static UserNotificationFactory singleton = new UserNotificationFactory();
    private static Mail mailer;

    private UserNotificationFactory() {
        super();
        try {
            configureMailer();
        }
        catch (Exception e) {
            log.error("Unable to configure the mailer: {}", e.getMessage(), e);
        }
    }

    private static void configureMailer() {
        String clazz = Config.get().getString("web.mailer_class");
        if (clazz == null) {
            mailer = new SmtpMail();
            return;
        }
        try {
            Class<? extends Mail> cobj = Class.forName(clazz).asSubclass(Mail.class);
            mailer = cobj.getDeclaredConstructor().newInstance();
        }
        catch (Exception | LinkageError e) {
            log.error("An exception was thrown while initializing custom mailer class", e);
            mailer = new SmtpMail();
        }
    }

    /**
     * For debugging set special mailer
     * @param mailerIn the mailer
     */
    public static void setMailer(Mail mailerIn) {
        mailer = mailerIn;
    }

    /**
     * Create new {@link UserNotification}.
     *
     * @param userIn the user of the notification
     * @param messageIn the message
     * @return new UserNotification
     */
    public static UserNotification create(User userIn, NotificationMessage messageIn) {
        return new UserNotification(userIn, messageIn);
    }

    /**
     * Check if the given user notification is currently disabled
     *
     * @param userNotificationIn userNotification
     * @return boolean if notification type is disabled
     */
    public static boolean isNotificationTypeDisabled(UserNotification userNotificationIn) {
        return isNotificationTypeDisabled(userNotificationIn.getMessage());
    }

    /**
     * Check if the given notification is currently disabled
     *
     * @param notificationIn the notification
     * @return boolean if notification type is disabled
     */
    public static boolean isNotificationTypeDisabled(NotificationMessage notificationIn) {
        List<String> disableNotificationsBy = ConfigDefaults.get().getNotificationsTypeDisabled();

        return disableNotificationsBy.contains(notificationIn.getType().name());
    }

    /**
     * Store {@link UserNotification} to the database.
     *
     * @param userNotificationIn userNotification
     */
    private static void store(UserNotification userNotificationIn) {
        singleton.saveObject(userNotificationIn);
    }

    /**
     * Create new {@link NotificationMessage}.
     *
     * @param notification notification data
     * @return new notificationMessage
     */
    public static NotificationMessage createNotificationMessage(NotificationData notification) {
        return new NotificationMessage(notification);
    }

    /**
     * Stores a notification visible for the specified users.
     * Send email to users which have email notifications requested
     *
     * @param notificationMessageIn notification to store
     * @param users user that should see the notification
     */
    public static void storeForUsers(NotificationMessage notificationMessageIn, Set<User> users) {
        // save first the message to get the 'id' auto generated
        // because it is referenced by the UserNotification object
        singleton.saveObject(notificationMessageIn);
        // We want to disable out the notifications defined on parameter: java.notifications_type_disabled
        // They are still added to the SuseNotificationTable but not associated with any user
        if (!isNotificationTypeDisabled(notificationMessageIn)) {
            String[] receipients = users.stream()
                                        .filter(user -> !user.isDisabled())
                                        .peek(user -> UserNotificationFactory.store(
                                                new UserNotification(user, notificationMessageIn)))
                                        .filter(user -> user.getEmailNotify() == 1)
                                        .map(User::getEmail)
                                        .toArray(String[]::new);
            if (receipients.length > 0 && mailer != null) {
                String subject = String.format("%s Notification from %s: %s",
                        MailHelper.PRODUCT_PREFIX,
                        ConfigDefaults.get().getHostname(),
                        notificationMessageIn.getTypeAsString());
                NotificationData data = notificationMessageIn.getNotificationData();
                String message = data.getSummary();
                if (!StringUtils.isBlank(data.getDetails())) {
                    message += "\n\n" + data.getDetails();
                }
                MailHelper.withMailer(mailer)
                        .sendEmail(receipients, subject, message.replaceAll("<[^>]*>", ""));
            }
        }
        // Update Notification WebSocket Sessions right now
        Notification.spreadUpdate(Notification.USER_NOTIFICATIONS);
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
        Notification.spreadUpdate(Notification.USER_NOTIFICATIONS);
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
        return unreadUserNotificationsSize(userIn.getId());
    }

    /**
     * Get the count of unread messages
     *
     * @param userIdIn the user id
     * @return the unread messages size count
     */
    public static long unreadUserNotificationsSize(long userIdIn) {
        CriteriaBuilder builder = getSession().getCriteriaBuilder();
        CriteriaQuery<Long> criteria = builder.createQuery(Long.class);
        Root<UserNotification> root = criteria.from(UserNotification.class);
        CriteriaQuery<Long> count = criteria.select(builder.count(root));

        criteria.where(
                builder.equal(root.get("userId"), userIdIn),
                builder.isFalse(root.get("read")));

        return getSession().createQuery(count).getSingleResult();
    }

    /**
     * Lookup for a single {@link UserNotification} by its message id
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
     * Lookup for a single {@link UserNotification} by its id
     *
     * @param notificationIdIn the id of the user notification
     * @param userIn the user
     * @return the Optional wrapper for the UserNotification
     */
    public static Optional<UserNotification> lookupByUserAndId(Long notificationIdIn, User userIn) {
        return getSession().createQuery("FROM UserNotification WHERE userId = :userid AND id = :id",
                UserNotification.class)
                .setParameter("userid", userIn.getId())
                .setParameter("id", notificationIdIn)
                .uniqueResultOptional();
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
        delete.where(builder.lessThan(root.get("created"), before));
        return getSession().createQuery(delete).executeUpdate();
    }

    /**
     * Deletes multiple notifications
     *
     * @param notifications the notifications to delete
     * @return int number of deleted notifications
     */
    public static int delete(Collection<UserNotification> notifications) {
        return delete(notifications, UserNotification.class);
    }

    @Override
    protected Logger getLogger() {
        return log;
    }
}
