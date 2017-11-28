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
import com.redhat.rhn.domain.notification.NotificationMessage;
import com.redhat.rhn.domain.notification.NotificationMessageFactory;
import com.redhat.rhn.domain.user.User;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
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
        Object data = getJSONNotificationMessages(NotificationMessageFactory.listUnreadByUser(user), user);

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
        Object data = getJSONNotificationMessages(NotificationMessageFactory.listAllByUser(user), user);

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

        Optional<NotificationMessage> nm = NotificationMessageFactory.lookupById(messageId);

        if (nm.isPresent()) {
            NotificationMessageFactory.updateStatus(nm.get(), user, isRead);
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
        for (NotificationMessage nm : NotificationMessageFactory.listUnreadByUser(user)) {
            NotificationMessageFactory.updateStatus(nm, user,true);
        }
        Notification.spreadUpdate();
        Map<String, String> data = new HashMap<>();
        data.put("message", "All messages marked as read succesfully");
        response.type("application/json");
        return GSON.toJson(data);
    }

    /**
     * Convert a list of {@link NotificationMessage} to a {@link JSONNotificationMessage}
     *
     * @param list of NotificationMessages
     * @return a list of JSONNotificationMessages
     */
    public static List<JSONNotificationMessage> getJSONNotificationMessages(List<NotificationMessage> list, User user) {
        return list.stream()
                .map(nm -> new JSONNotificationMessage(nm, nm.getUsers().contains(user)))
                .collect(Collectors.toList());
    }
}
