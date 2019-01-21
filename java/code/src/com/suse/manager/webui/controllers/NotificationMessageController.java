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
package com.suse.manager.webui.controllers;

import com.redhat.rhn.domain.channel.Channel;
import com.redhat.rhn.domain.channel.ChannelFactory;
import com.redhat.rhn.domain.notification.UserNotification;
import com.redhat.rhn.domain.notification.UserNotificationFactory;
import com.redhat.rhn.domain.user.User;
import com.redhat.rhn.taskomatic.TaskomaticApi;
import com.redhat.rhn.taskomatic.TaskomaticApiException;

import com.suse.manager.reactor.messaging.RegisterMinionEventMessage;
import com.suse.manager.reactor.messaging.RegisterMinionEventMessageAction;
import com.suse.manager.webui.services.impl.SaltService;
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

/**
 * Controller class providing backend code for the messages page.
 */
public class NotificationMessageController {

    private static final Gson GSON = new GsonBuilder()
            .registerTypeAdapter(Date.class, new ECMAScriptDateAdapter())
            .serializeNulls()
            .create();

    private NotificationMessageController() { }

    /**
     * Displays a list of messages.
     *
     * @param request the request object
     * @param response the response object
     * @param user the user object
     * @return the ModelAndView object to render the page
     */
    public static ModelAndView getList(Request request, Response response, User user) {
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
    public static String dataUnread(Request request, Response response, User user) {
        Object data = getJSONNotificationMessages(UserNotificationFactory.listUnreadByUser(user), user);

        response.type("application/json");
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
    public static String dataAll(Request request, Response response, User user) {
        Object data = getJSONNotificationMessages(UserNotificationFactory.listAllByUser(user), user);

        response.type("application/json");
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
    public static String delete(Request request, Response response, User user) {
        List<Long> messageIds = Json.GSON.fromJson(request.body(), new TypeToken<List<Long>>() { }.getType());

        messageIds.forEach(messageId ->
                {
                    Optional<UserNotification> un = UserNotificationFactory.lookupByUserAndMessageId(messageId, user);
                    if (un.isPresent()) {
                        UserNotificationFactory.remove(un.get());
                    }
                });

        Notification.spreadUpdate();

        Map<String, String> data = new HashMap<>();
        data.put("severity", "success");
        if (messageIds.size() == 1) {
            data.put("text", "1 message deleted successfully");
        }
        else {
            data.put("text", messageIds.size() + " messages deleted successfully");
        }
        response.type("application/json");
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
    public static String updateMessagesStatus(Request request, Response response, User user) {
        Map<String, Object> map = GSON.fromJson(request.body(), Map.class);
        List<Long> messageIds = Json.GSON.fromJson(
                map.get("messageIds").toString(),
                new TypeToken<List<Long>>() { }.getType());
        boolean flagAsRead = (boolean) map.get("flagAsRead");

        messageIds.forEach(messageId ->
        {
            Optional<UserNotification> un = UserNotificationFactory.lookupByUserAndMessageId(messageId, user);
            if (un.isPresent()) {
                UserNotificationFactory.updateStatus(un.get(), flagAsRead);
            }
        });

        Notification.spreadUpdate();

        Map<String, String> data = new HashMap<>();
        data.put("severity", "success");
        if (messageIds.size() == 1) {
            data.put("text", "1 message read status updated successfully");
        }
        else {
            data.put("text", messageIds.size() + " messages status updated successfully");
        }
        response.type("application/json");
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
    public static String retryOnboarding(Request request, Response response, User user) {
        String minionId = request.params("minionId");

        String severity = "success";
        String resultMessage = "Onboarding restarted of the minioniId '%s'";

        RegisterMinionEventMessageAction action = new RegisterMinionEventMessageAction(SaltService.INSTANCE);
        action.execute(new RegisterMinionEventMessage(minionId));

        Map<String, String> data = new HashMap<>();
        data.put("severity", severity);
        data.put("text", String.format(resultMessage, minionId));
        response.type("application/json");
        return GSON.toJson(data);
    }

    /**
     * Get the Label of a channel
     *
     * @param channel the channel
     * @return Optional containing the String of the Channel Label or empty
     */
    private static Optional<String> getChannelLabel(Channel channel) {
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
    public static String retryReposync(Request request, Response response, User user) {
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
        response.type("application/json");
        return GSON.toJson(data);
    }

    /**
     * Convert a list of {@link UserNotification} to a {@link NotificationMessageJson}
     *
     * @param list of UserNotification
     * @param user the current user
     * @return a list of JSONNotificationMessages
     */
    public static List<NotificationMessageJson> getJSONNotificationMessages(List<UserNotification> list, User user) {
        return list.stream()
                .map(un -> new NotificationMessageJson(un.getMessage(), un.getRead()))
                .collect(Collectors.toList());
    }
}
