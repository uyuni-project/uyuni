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
package com.suse.manager.webui.controllers;

import static com.suse.manager.webui.utils.SparkApplicationHelper.asJson;
import static com.suse.manager.webui.utils.SparkApplicationHelper.badRequest;
import static com.suse.manager.webui.utils.SparkApplicationHelper.internalServerError;
import static com.suse.manager.webui.utils.SparkApplicationHelper.notFound;
import static com.suse.manager.webui.utils.SparkApplicationHelper.success;
import static com.suse.manager.webui.utils.SparkApplicationHelper.withCsrfToken;
import static com.suse.manager.webui.utils.SparkApplicationHelper.withUser;
import static com.suse.manager.webui.utils.SparkApplicationHelper.withUserPreferences;
import static spark.Spark.get;
import static spark.Spark.post;

import com.redhat.rhn.common.localization.LocalizationService;
import com.redhat.rhn.domain.channel.Channel;
import com.redhat.rhn.domain.channel.ChannelFactory;
import com.redhat.rhn.domain.notification.NotificationMessage;
import com.redhat.rhn.domain.notification.UserNotification;
import com.redhat.rhn.domain.notification.UserNotificationFactory;
import com.redhat.rhn.domain.notification.types.ChannelSyncFailed;
import com.redhat.rhn.domain.notification.types.NotificationType;
import com.redhat.rhn.domain.notification.types.OnboardingFailed;
import com.redhat.rhn.domain.user.User;
import com.redhat.rhn.taskomatic.TaskomaticApi;
import com.redhat.rhn.taskomatic.TaskomaticApiException;

import com.suse.cloud.CloudPaygManager;
import com.suse.manager.attestation.AttestationManager;
import com.suse.manager.reactor.messaging.RegisterMinionEventMessage;
import com.suse.manager.reactor.messaging.RegisterMinionEventMessageAction;
import com.suse.manager.webui.services.iface.SaltApi;
import com.suse.manager.webui.services.iface.SystemQuery;
import com.suse.manager.webui.utils.gson.NotificationMessageJson;
import com.suse.manager.webui.utils.gson.NotificationTypeJson;
import com.suse.manager.webui.utils.gson.ResultJson;
import com.suse.manager.webui.websocket.Notification;
import com.suse.utils.Json;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.reflect.TypeToken;

import org.apache.commons.lang3.builder.EqualsBuilder;
import org.apache.commons.lang3.builder.HashCodeBuilder;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.util.Arrays;
import java.util.Comparator;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

import spark.ModelAndView;
import spark.Request;
import spark.Response;
import spark.template.jade.JadeTemplateEngine;

/**
 * Controller class providing backend code for the messages page.
 */
public class NotificationMessageController {

    private static final Logger LOGGER = LogManager.getLogger(NotificationMessageController.class);

    private static final LocalizationService LOCALIZER = LocalizationService.getInstance();

    private static final Gson GSON = new GsonBuilder()
            .registerTypeAdapter(Date.class, new UserLocalizationDateAdapter())
            .serializeNulls()
            .create();

    private final SaltApi saltApi;
    private final SystemQuery systemQuery;
    private final CloudPaygManager paygManager;
    private final AttestationManager attestationManager;

    /**
     * @param systemQueryIn instance for getting information from a system.
     * @param saltApiIn instance for getting information from a system.
     * @param paygMgrIn instance of {@link CloudPaygManager}
     * @param attMgrIn instance of {@link AttestationManager}
     */
    public NotificationMessageController(SystemQuery systemQueryIn, SaltApi saltApiIn, CloudPaygManager paygMgrIn,
                                         AttestationManager attMgrIn) {
        this.saltApi = saltApiIn;
        this.systemQuery = systemQueryIn;
        this.paygManager = paygMgrIn;
        this.attestationManager = attMgrIn;
    }

    /**
     * Invoked from Router. Initialize routes for Systems Views.
     *
     * @param notificationMessageController instance to register.
     * @param jade the Jade engine to use to render the pages
     */
    public static void initRoutes(JadeTemplateEngine jade,
                                  NotificationMessageController notificationMessageController) {
        get("/manager/notification-messages",
                withUserPreferences(withCsrfToken(withUser(notificationMessageController::getList))), jade);
        get("/manager/notification-messages/data-unread", asJson(withUser(notificationMessageController::dataUnread)));
        get("/manager/notification-messages/data-all", asJson(withUser(notificationMessageController::dataAll)));
        post("/manager/notification-messages/update-messages-status",
                asJson(withUser(notificationMessageController::updateMessagesStatus)));
        post("/manager/notification-messages/delete",
                asJson(withUser(notificationMessageController::delete)));
        post("/manager/notification-messages/retry/:notificationId",
                asJson(withUser(notificationMessageController::retry)));
    }

    /**
     * Displays a list of messages.
     *
     * @param request the request object
     * @param response the response object
     * @param user the user object
     * @return the ModelAndView object to render the page
     */
    public ModelAndView getList(Request request, Response response, User user) {
        var notificationTypes = Arrays.stream(NotificationType.values())
            .map(NotificationTypeJson::new)
            .sorted(Comparator.comparing(NotificationTypeJson::description))
            .toList();

        Map<Object, Object> dataMap = new HashMap<>();
        dataMap.put("notificationTypes", GSON.toJson(notificationTypes));

        return new ModelAndView(dataMap, "templates/notification-messages/list.jade");
    }


    /**
     * Returns JSON data from messages
     *
     * @param request the request
     * @param response the response
     * @param user the user
     * @return JSON result of the API call
     */
    public String dataUnread(Request request, Response response, User user) {
        var data = getJSONNotificationMessages(UserNotificationFactory.listUnreadByUser(user));
        return success(response, ResultJson.success(data));
    }

    /**
     * Returns JSON data from messages
     *
     * @param request the request
     * @param response the response
     * @param user the user
     * @return JSON result of the API call
     */
    public String dataAll(Request request, Response response, User user) {
        var data = getJSONNotificationMessages(UserNotificationFactory.listAllByUser(user));
        return success(response, ResultJson.success(data));
    }

    /**
     * Delete a set of {@link UserNotification}
     *
     * @param request the request
     * @param response the response
     * @param user the user
     * @return JSON result of the API call
     */
    public String delete(Request request, Response response, User user) {
        List<Long> messageIds = Json.GSON.fromJson(request.body(), new TypeToken<List<Long>>() { }.getType());

        List<UserNotification> notifications = messageIds.stream()
            .flatMap(id -> UserNotificationFactory.lookupByUserAndMessageId(id, user).stream())
            .toList();

        UserNotificationFactory.delete(notifications);

        Notification.spreadUpdate(Notification.USER_NOTIFICATIONS);
        return success(response);
    }

    /**
     * Update the read status of the messages
     *
     * @param request the request
     * @param response the response
     * @param user the user
     * @return JSON result of the API call
     */
    public String updateMessagesStatus(Request request, Response response, User user) {
        UpdateMessageStatusRequest statusChange = GSON.fromJson(request.body(), UpdateMessageStatusRequest.class);

        statusChange.getMessageIds().stream()
            .flatMap(id -> UserNotificationFactory.lookupByUserAndMessageId(id, user).stream())
            .forEach(notification -> UserNotificationFactory.updateStatus(notification, statusChange.isFlagAsRead()));

        Notification.spreadUpdate(Notification.USER_NOTIFICATIONS);
        return success(response);
    }

    /**
     * Retries an actionable notification
     *
     * @param request the request
     * @param response the response
     * @param user the user
     * @return JSON result of the API call
     */
    public String retry(Request request, Response response, User user) {
        long notificationId = Long.parseLong(request.params("notificationId"));

        return UserNotificationFactory.lookupByUserAndMessageId(notificationId, user)
            .map(UserNotification::getMessage)
            .map(NotificationMessage::getNotificationData)
            .map(notificationData -> {
                if (notificationData instanceof OnboardingFailed onboardingFailed) {
                    return retryOnboarding(response, onboardingFailed.getMinionId());
                }

                if (notificationData instanceof ChannelSyncFailed channelSyncFailed) {
                    return retryReposync(response, user, channelSyncFailed.getChannelId());
                }

                String typeDescription = notificationData.getType().getDescription();
                String errorMessage = LOCALIZER.getMessage("notification.type_not_actionable", typeDescription);

                return badRequest(response, errorMessage);
            })
            .orElseGet(() -> notFound(response, LOCALIZER.getMessage("notification.not_found", notificationId)));
    }

    private String retryOnboarding(Response response, String minionId) {
        var action = new RegisterMinionEventMessageAction(systemQuery, saltApi, paygManager, attestationManager);
        action.execute(new RegisterMinionEventMessage(minionId, Optional.empty()));

        String resultMessage = LOCALIZER.getMessage("notification.onboardingfailed.action.success", minionId);
        return success(response, ResultJson.successMessage(resultMessage));
    }

    private String retryReposync(Response response, User user, long channelId) {
        Channel channel = ChannelFactory.lookupById(channelId);
        if (channel == null) {
            String message = LOCALIZER.getMessage("notification.channelsyncfailed.action.not_found", channelId);
            return notFound(response, message);
        }

        try {
            TaskomaticApi taskomatic = new TaskomaticApi();
            taskomatic.scheduleSingleRepoSync(channel, user);

            String message = LOCALIZER.getMessage("notification.channelsyncfailed.action.success", channel.getLabel());
            return success(response, ResultJson.successMessage(message));
        }
        catch (TaskomaticApiException ex) {
            LOGGER.error("Failed to restart reposync for the channel {}", channel.getLabel(), ex);
            String message = LOCALIZER.getMessage("notification.channelsyncfailed.action.failed",
                channel.getLabel(), ex.getMessage());
            return internalServerError(response, message);
        }
    }

    private List<NotificationMessageJson> getJSONNotificationMessages(List<UserNotification> list) {
        return list.stream()
                .map(un -> new NotificationMessageJson(un.getMessage(), un.getRead()))
                .collect(Collectors.toList());
    }

    private static class UpdateMessageStatusRequest {
        private List<Long> messageIds;
        private boolean flagAsRead;

        public List<Long> getMessageIds() {
            return messageIds;
        }

        public void setMessageIds(List<Long> messageIdsIn) {
            this.messageIds = messageIdsIn;
        }

        public boolean isFlagAsRead() {
            return flagAsRead;
        }

        public void setFlagAsRead(boolean flagAsReadIn) {
            this.flagAsRead = flagAsReadIn;
        }

        @Override
        public boolean equals(Object o) {
            if (this == o) {
                return true;
            }

            if (!(o instanceof UpdateMessageStatusRequest that)) {
                return false;
            }

            return new EqualsBuilder()
                .append(isFlagAsRead(), that.isFlagAsRead())
                .append(getMessageIds(), that.getMessageIds())
                .isEquals();
        }

        @Override
        public int hashCode() {
            return new HashCodeBuilder(17, 37)
                .append(getMessageIds())
                .append(isFlagAsRead())
                .toHashCode();
        }

        @Override
        public String toString() {
            final StringBuilder sb = new StringBuilder("UpdateMessageStatusRequest{");
            sb.append("messageIds=").append(messageIds);
            sb.append(", flagAsRead=").append(flagAsRead);
            sb.append('}');
            return sb.toString();
        }
    }
}
