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
package com.suse.manager.webui.controllers;

import static com.suse.manager.webui.utils.SparkApplicationHelper.asJson;
import static com.suse.manager.webui.utils.SparkApplicationHelper.withCsrfToken;
import static com.suse.manager.webui.utils.SparkApplicationHelper.withUser;
import static com.suse.manager.webui.utils.SparkApplicationHelper.withUserPreferences;
import static spark.Spark.get;
import static spark.Spark.post;

import com.redhat.rhn.domain.channel.Channel;
import com.redhat.rhn.domain.channel.ChannelFactory;
import com.redhat.rhn.domain.notification.UserNotification;
import com.redhat.rhn.domain.notification.UserNotificationFactory;
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
import com.suse.manager.webui.websocket.Notification;
import com.suse.utils.Json;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.reflect.TypeToken;

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
        post("/manager/notification-messages/retry-onboarding/:minionId",
                asJson(withUser(notificationMessageController::retryOnboarding)));
        post("/manager/notification-messages/retry-reposync/:channelId",
                asJson(withUser(notificationMessageController::retryReposync)));
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
        return new ModelAndView(new HashMap<>(), "templates/notification-messages/list.jade");
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
        Object data = getJSONNotificationMessages(UserNotificationFactory.listUnreadByUser(user), user);
        return GSON.toJson(data);
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
        Object data = getJSONNotificationMessages(UserNotificationFactory.listAllByUser(user), user);
        return GSON.toJson(data);
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
                .map(id -> UserNotificationFactory.lookupByUserAndMessageId(id, user))
                .flatMap(Optional::stream)
                .collect(Collectors.toList());

        UserNotificationFactory.delete(notifications);

        Notification.spreadUpdate(Notification.USER_NOTIFICATIONS);

        Map<String, String> data = new HashMap<>();
        data.put("severity", "success");
        if (messageIds.size() == 1) {
            data.put("text", "1 message deleted successfully");
        }
        else {
            data.put("text", messageIds.size() + " messages deleted successfully");
        }
        return GSON.toJson(data);
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
        Map<String, Object> map = GSON.fromJson(request.body(), Map.class);
        List<Long> messageIds = Json.GSON.fromJson(
                map.get("messageIds").toString(),
                new TypeToken<List<Long>>() { }.getType());
        boolean flagAsRead = (boolean) map.get("flagAsRead");

        messageIds.forEach(messageId -> {
            Optional<UserNotification> un = UserNotificationFactory.lookupByUserAndMessageId(messageId, user);
            if (un.isPresent()) {
                UserNotificationFactory.updateStatus(un.get(), flagAsRead);
            }
        });

        Notification.spreadUpdate(Notification.USER_NOTIFICATIONS);

        Map<String, String> data = new HashMap<>();
        data.put("severity", "success");
        if (messageIds.size() == 1) {
            data.put("text", "1 message read status updated successfully");
        }
        else {
            data.put("text", messageIds.size() + " messages status updated successfully");
        }
        return GSON.toJson(data);
    }

    /**
     * Re-trigger onboarding the minion
     *
     * @param request the request
     * @param response the response
     * @param user the user
     * @return JSON result of the API call
     */
    public String retryOnboarding(Request request, Response response, User user) {
        String minionId = request.params("minionId");

        String severity = "success";
        String resultMessage = "Onboarding restarted of the minioniId '%s'";

        RegisterMinionEventMessageAction action =
                new RegisterMinionEventMessageAction(systemQuery, saltApi, paygManager, attestationManager);
        action.execute(new RegisterMinionEventMessage(minionId, Optional.empty()));

        Map<String, String> data = new HashMap<>();
        data.put("severity", severity);
        data.put("text", String.format(resultMessage, minionId));
        return GSON.toJson(data);
    }

    /**
     * Get the Label of a channel
     *
     * @param channel the channel
     * @return Optional containing the String of the Channel Label or empty
     */
    private Optional<String> getChannelLabel(Channel channel) {
        if (channel == null) {
            return Optional.empty();
        }
        return Optional.ofNullable(channel.getLabel());
    }

    /**
     * Re-trigger channel reposync
     *
     * @param request the request
     * @param response the response
     * @param user the user
     * @return JSON result of the API call
     */
    public String retryReposync(Request request, Response response, User user) {
        Long channelId = Long.valueOf(request.params("channelId"));

        String resultMessage = "Reposync restarted for the channel '%s'";
        String severity = "success";

        Channel channel = ChannelFactory.lookupById(channelId);
        Optional<String> channelLabel = getChannelLabel(channel);

        if (channel != null) {
            TaskomaticApi taskomatic = new TaskomaticApi();
            try {
                taskomatic.scheduleSingleRepoSync(channel, user);
            }
            catch (TaskomaticApiException e) {
                severity = "error";
                resultMessage = "Failed to restart reposync for the channel '%s': " + e.getMessage();
            }
        }
        else {
            severity = "error";
            resultMessage = "Failed to restart reposync. Channel ID does not exist: %s";
        }

        Map<String, String> data = new HashMap<>();
        data.put("severity", severity);
        data.put("text", String.format(resultMessage, channelLabel.orElse(String.valueOf(channelId))));
        return GSON.toJson(data);
    }

    /**
     * Convert a list of {@link UserNotification} to a {@link NotificationMessageJson}
     *
     * @param list of UserNotification
     * @param user the current user
     * @return a list of JSONNotificationMessages
     */
    public List<NotificationMessageJson> getJSONNotificationMessages(List<UserNotification> list, User user) {
        return list.stream()
                .map(un -> new NotificationMessageJson(un.getMessage(), un.getRead()))
                .collect(Collectors.toList());
    }
}
