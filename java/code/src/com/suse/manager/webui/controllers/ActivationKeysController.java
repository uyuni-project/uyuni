/*
 * Copyright (c) 2018 SUSE LLC
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

import static com.suse.manager.webui.controllers.channels.ChannelsUtils.generateChannelJson;
import static com.suse.manager.webui.controllers.channels.ChannelsUtils.getPossibleBaseChannels;
import static com.suse.manager.webui.utils.SparkApplicationHelper.badRequest;
import static com.suse.manager.webui.utils.SparkApplicationHelper.notFound;
import static com.suse.manager.webui.utils.SparkApplicationHelper.result;
import static com.suse.manager.webui.utils.SparkApplicationHelper.withUser;
import static com.suse.manager.webui.utils.gson.ResultJson.success;
import static spark.Spark.get;

import com.redhat.rhn.domain.channel.Channel;
import com.redhat.rhn.domain.channel.ChannelFactory;
import com.redhat.rhn.domain.token.ActivationKey;
import com.redhat.rhn.domain.token.ActivationKeyFactory;
import com.redhat.rhn.domain.user.User;

import com.suse.manager.webui.utils.gson.ChannelsJson;

import com.google.gson.reflect.TypeToken;

import java.util.LinkedList;
import java.util.List;
import java.util.function.Function;
import java.util.stream.Collectors;

import spark.Request;
import spark.Response;

/**
 * Controller class providing backend code for the systems page.
 */
public class ActivationKeysController {
    private ActivationKeysController() { }

    /**
     * Invoked from Router. Init routes for Activation keys views.
     */
    public static void initRoutes() {
        get("/manager/api/activation-keys/:tid/channels", withUser(ActivationKeysController::getChannels));
        get("/manager/api/activation-keys/base-channels",
                withUser(ActivationKeysController::getAccessibleBaseChannels));
        get("/manager/api/activation-keys/base-channels/:cid/child-channels",
                withUser(ActivationKeysController::getChildChannelsByBaseId));
    }

    private static String withActivationKey(Request request, Response response,
                                            User user, Function<ActivationKey, String> handler) {
        Long activationKeyId;
        try {
            activationKeyId = Long.parseLong(request.params("tid"));
        }
        catch (NumberFormatException e) {
            return badRequest(response, "invalid_activation_key_id");
        }
        ActivationKey activationKey = ActivationKeyFactory.lookupById(activationKeyId, user.getOrg());
        if (activationKey == null) {
            return badRequest(response, "activation_key_not_found");
        }
        return handler.apply(activationKey);
    }

    /**
     * Get the current channels of an activation key.
     *
     * @param request the http request
     * @param response the http response
     * @param user the user
     * @return the json response
     */
    public static String getChannels(Request request, Response response, User user) {
        return withActivationKey(request, response, user, (activationKey) -> result(response,
                success(ChannelsJson.fromChannelSet(activationKey.getChannels())), new TypeToken<>() { }));
    }

    /**
     * Get available base channels for a user.
     *
     * @param request the request
     * @param response the response
     * @param user the user
     * @return the json response
     */
    public static String getAccessibleBaseChannels(Request request, Response response, User user) {
        return result(response, success(
                getPossibleBaseChannels(user).stream()
                        .map(b -> {
                            ChannelsJson group = new ChannelsJson();
                            group.setBase(b);
                            return group;
                        })
                        .collect(Collectors.toList())
        ), new TypeToken<>() { });
    }

    private static String withChannel(Request request, Response response,
                                            User user, Function<Channel, String> handler) {
        Long channelId;
        try {
            channelId = Long.parseLong(request.params("cid"));
        }
        catch (NumberFormatException e) {
            return badRequest(response, "invalid_channel_id");
        }
        Channel channel = ChannelFactory.lookupById(channelId);
        if (channel == null) {
            return notFound(response, "channel_not_found");
        }
        return handler.apply(channel);
    }

    /**
     * Get the child channel set for a certain base channel
     *
     * @param request the request
     * @param response the response
     * @param user the current user
     * @return the json response
     */
    public static String getChildChannelsByBaseId(Request request, Response response, User user) {
        List<ChannelsJson> jsonChannels = new LinkedList<>();

        if (request.params("cid").equals("-1")) {
            getPossibleBaseChannels(user).forEach(base -> jsonChannels.add(generateChannelJson(base, user)));
            return result(response, success(jsonChannels), new TypeToken<>() { });
        }
        else {
            return withChannel(request, response, user, (base) -> {
                jsonChannels.add(generateChannelJson(base, user));
                return result(response, success(jsonChannels), new TypeToken<>() { });
            });
        }
    }

}
