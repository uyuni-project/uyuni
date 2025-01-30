/*
 * Copyright (c) 2024 SUSE LLC
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
package com.suse.manager.webui.controllers.activationkeys;

import static com.suse.manager.webui.utils.SparkApplicationHelper.result;
import static com.suse.manager.webui.utils.SparkApplicationHelper.withCsrfToken;
import static com.suse.manager.webui.utils.SparkApplicationHelper.withDocsLocale;
import static com.suse.manager.webui.utils.SparkApplicationHelper.withUser;
import static com.suse.manager.webui.utils.gson.ResultJson.success;
import static spark.Spark.get;
import static spark.Spark.post;

import com.redhat.rhn.domain.channel.Channel;
import com.redhat.rhn.domain.channel.ChannelFactory;
import com.redhat.rhn.domain.role.RoleFactory;
import com.redhat.rhn.domain.token.ActivationKey;
import com.redhat.rhn.domain.token.ActivationKeyFactory;
import com.redhat.rhn.domain.user.User;
import com.redhat.rhn.manager.appstreams.AppStreamsManager;
import com.redhat.rhn.manager.token.ActivationKeyManager;

import com.suse.manager.webui.controllers.appstreams.response.ChannelAppStreamsResponse;
import com.suse.manager.webui.utils.ViewHelper;
import com.suse.utils.Json;

import com.google.gson.reflect.TypeToken;

import org.apache.http.HttpStatus;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.stream.Collectors;

import spark.ModelAndView;
import spark.Request;
import spark.Response;
import spark.Spark;
import spark.template.jade.JadeTemplateEngine;

public class ActivationKeysViewsController {

    private ActivationKeysViewsController() { }

    /**
     * Invoked from Router. Init routes for Activation keys views.
     * @param jade JadeTemplateEngine
     */
    public static void initRoutes(JadeTemplateEngine jade) {
        get("/manager/activationkeys/appstreams",
            withCsrfToken(withDocsLocale(withUser(ActivationKeysViewsController::appstreams))), jade);
        post("/manager/api/activationkeys/appstreams/save",
                withUser(ActivationKeysViewsController::saveAppStreamsChanges));
    }

    private static ActivationKey getActivationKey(Request request, User user) {
        ActivationKey activationKey;
        try {
            Long activationKeyId = Long.parseLong(request.raw().getParameter("tid"));
            activationKey = ActivationKeyFactory.lookupById(activationKeyId, user.getOrg());
        }
        catch (NumberFormatException e) {
            throw Spark.halt(HttpStatus.SC_BAD_REQUEST, "Invalid activation key");
        }
        if (activationKey == null) {
            throw Spark.halt(HttpStatus.SC_NOT_FOUND);
        }
        return activationKey;
    }

    /**
     * Handler for the activation keys -> app streams page.
     *
     * @param request the request object
     * @param response the response object
     * @param user the current user
     * @return the ModelAndView object to render the page
     */
    public static ModelAndView appstreams(Request request, Response response, User user) {
        Map<String, Object> data = new HashMap<>();
        var activationKey = getActivationKey(request, user);
        var channelsAppStreams = getChannelAppStreams(activationKey);

        data.put("channelsAppStreams", Json.GSON.toJson(channelsAppStreams));
        data.put("note", activationKey.getNote());
        data.put("activationKeyId", activationKey.getId());
        data.put("isActivationKeyAdmin", user.hasRole(RoleFactory.ACTIVATION_KEY_ADMIN));
        data.put("tabs", ViewHelper.getInstance().renderNavigationMenu(request, "/WEB-INF/nav/activation_key.xml"));
        return new ModelAndView(data, "templates/activation_keys/appstreams.jade");
    }

    private static List<ChannelAppStreamsResponse> getChannelAppStreams(ActivationKey activationKey) {
        return activationKey.getChannels()
            .stream()
            .filter(Channel::isModular)
            .map(channel -> new ChannelAppStreamsResponse(
                channel,
                AppStreamsManager.listChannelAppStreams(channel.getId()),
                (module, stream) -> ActivationKeyManager.getInstance().hasAppStreamModuleEnabled(
                        activationKey, channel, module, stream
                )
            )).collect(Collectors.toList());
    }

    /**
     * Saves the changes (enable/disable) to AppStreams linked to a given activation key.
     *
     * @param request  the HTTP request
     * @param response the HTTP response
     * @param user     the user performing the action
     * @return a JSON response indicating success or failure
     */
    public static String saveAppStreamsChanges(Request request, Response response, User user) {
        var activationKey = getActivationKey(request, user);
        var params = Json.GSON.fromJson(request.body(), ActivationKeysAppStreamsChanges.class);
        params
            .getChannelIds()
            .stream()
            .map(channelId -> ChannelFactory.lookupByIdAndUser(channelId, user))
            .filter(Objects::nonNull)
            .forEach(channel ->
            ActivationKeyManager
                .getInstance()
                .saveChannelAppStreams(
                    activationKey,
                    channel,
                    params.getToInclude(channel.getId()),
                    params.getToRemove(channel.getId())
                )
        );

        return result(response, success(getChannelAppStreams(activationKey)), new TypeToken<>() { });
    }
}
