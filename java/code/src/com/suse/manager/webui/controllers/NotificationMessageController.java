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

import com.redhat.rhn.common.security.CSRFTokenValidator;
import com.redhat.rhn.domain.notification.UserNotificationFactory;
import com.redhat.rhn.domain.channel.ChannelFactory;
import com.redhat.rhn.domain.notification.UserNotification;
import com.redhat.rhn.domain.user.User;
import com.redhat.rhn.frontend.struts.RhnHelper;
import com.redhat.rhn.taskomatic.TaskomaticApi;
import com.redhat.rhn.taskomatic.TaskomaticApiException;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.suse.manager.reactor.messaging.RegisterMinionEventMessage;
import com.suse.manager.reactor.messaging.RegisterMinionEventMessageAction;
import com.suse.manager.webui.services.impl.SaltService;
import com.suse.manager.webui.utils.gson.JSONNotificationMessage;
import com.suse.manager.webui.websocket.Notification;

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
        Map<String, Object> data = new HashMap<>();
        data.put("csrf_token", CSRFTokenValidator.getToken(request.session().raw()));
        return new ModelAndView(data, "notification-messages/list.jade");
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
     * Update the read status of the message
     *
     * @param request the request
     * @param response the response
     * @param user the user
     * @return JSON result of the API call
     */
    public static String updateMessageStatus(Request request, Response response, User user) {
        Map<String, Object> map = GSON.fromJson(request.body(), Map.class);
        Long messageId = Double.valueOf(map.get("messageId").toString()).longValue();
        boolean isRead = (boolean) map.get("isRead");

        Optional<UserNotification> un = UserNotificationFactory.lookupByUserAndMessageId(messageId, user);

        if (un.isPresent()) {
            UserNotificationFactory.updateStatus(un.get(), isRead);
        }
        Notification.spreadUpdate();

        Map<String, String> data = new HashMap<>();
        data.put("message", "Message status updated");
        response.type("application/json");
        return GSON.toJson(data);
    }

    /**
     * Mark all {@link NotificationMessage}s as read in one shot
     *
     * @param request the request
     * @param response the response
     * @param user the user
     * @return JSON result of the API call
     */
    public static String markAllAsRead(Request request, Response response, User user) {
        for (UserNotification un : UserNotificationFactory.listUnreadByUser(user)) {
            UserNotificationFactory.updateStatus(un, true);
        }
        Notification.spreadUpdate();
        Map<String, String> data = new HashMap<>();
        data.put("message", "All messages marked as read succesfully");
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

        RegisterMinionEventMessageAction action = new RegisterMinionEventMessageAction(SaltService.INSTANCE);
        action.doExecute(new RegisterMinionEventMessage(minionId));

        Map<String, String> data = new HashMap<>();
        data.put("message", "Onboarding restarted.");
        response.type("application/json");
        return GSON.toJson(data);
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

        String resultMessage = "Reposync restarted";

        TaskomaticApi taskomatic = new TaskomaticApi();
        try {
            taskomatic.scheduleSingleRepoSync(ChannelFactory.lookupById(channelId), user);
        }
        catch (TaskomaticApiException e) {
            resultMessage = "Failed to restart reposync: " + e.getMessage();
        }

        Map<String, String> data = new HashMap<>();
        data.put("message", resultMessage);
        response.type("application/json");
        return GSON.toJson(data);
    }

    /**
     * Convert a list of {@link UserNotification} to a {@link JSONNotificationMessage}
     *
     * @param list of UserNotification
     * @param user the current user
     * @return a list of JSONNotificationMessages
     */
    public static List<JSONNotificationMessage> getJSONNotificationMessages(List<UserNotification> list, User user) {
        return list.stream()
                .map(un -> new JSONNotificationMessage(un.getMessage(), un.getRead()))
                .collect(Collectors.toList());
    }
}
