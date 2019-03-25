/**
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

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.redhat.rhn.domain.channel.Channel;
import com.redhat.rhn.domain.channel.ChannelFactory;
import com.redhat.rhn.domain.token.ActivationKey;
import com.redhat.rhn.domain.token.ActivationKeyFactory;
import com.redhat.rhn.domain.user.User;
import com.redhat.rhn.manager.channel.ChannelManager;
import com.suse.manager.reactor.utils.LocalDateTimeISOAdapter;
import com.suse.manager.reactor.utils.OptionalTypeAdapterFactory;
import com.suse.manager.webui.utils.gson.ChannelsJson;
import com.suse.manager.webui.utils.gson.ResultJson;
import java.time.LocalDateTime;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;
import org.apache.http.HttpStatus;
import org.apache.log4j.Logger;
import spark.Request;
import spark.Response;

import static com.suse.manager.webui.utils.SparkApplicationHelper.json;
/**
 * Controller class providing backend code for the systems page.
 */
public class ActivationKeysController {
    private ActivationKeysController() { }
    // Logger for this class
    private static final Logger LOG = Logger.getLogger(ActivationKeysController.class);
    private static final Gson GSON = new GsonBuilder()
            .registerTypeAdapter(LocalDateTime.class, new LocalDateTimeISOAdapter())
            .registerTypeAdapterFactory(new OptionalTypeAdapterFactory())
            .serializeNulls()
            .create();

    private static String withActivationKey(Request request, Response response,
                                            User user, Function<ActivationKey, String> handler) {
        Long activationKeyId;
        try {
            activationKeyId = Long.parseLong(request.params("tid"));
        }
        catch (NumberFormatException e) {
            return json(response,
                    HttpStatus.SC_BAD_REQUEST,
                    ResultJson.error("invalid_activation_key_id"));
        }
        ActivationKey activationKey = ActivationKeyFactory.lookupById(activationKeyId, user.getOrg());
        if (activationKey == null) {
            return json(response,
                    HttpStatus.SC_NOT_FOUND,
                    ResultJson.error("activation_key_not_found"));
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
        return withActivationKey(request, response, user, (activationKey) -> {
            return json(response, ResultJson.success(ChannelsJson.fromChannelSet(activationKey.getChannels())));
        });
    }

    /**
     * Get a list of base {@link Channel} visible to the current user
     *
     * @param user the current user
     * @return the list of base Channel the user can access
     */
    public static List<Channel> getPossibleBaseChannels(User user) {
        return ChannelFactory.listSubscribableBaseChannels(user);
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
        return json(response, ResultJson.success(
                getPossibleBaseChannels(user).stream()
                        .map(b -> {
                            ChannelsJson group = new ChannelsJson();
                            group.setBase(b);
                            return group;
                        })
                        .collect(Collectors.toList())
        ));
    }

    private static String withChannel(Request request, Response response,
                                            User user, Function<Channel, String> handler) {
        Long channelId;
        try {
            channelId = Long.parseLong(request.params("cid"));
        }
        catch (NumberFormatException e) {
            return json(response,
                    HttpStatus.SC_BAD_REQUEST,
                    ResultJson.error("invalid_channel_id"));
        }
        Channel channel = ChannelFactory.lookupById(channelId);
        if (channel == null) {
            return json(response,
                    HttpStatus.SC_NOT_FOUND,
                    ResultJson.error("channel_not_found"));
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
        List<ChannelsJson> jsonChannels = new LinkedList<ChannelsJson>();

        if (request.params("cid").equals("-1")) {
            getPossibleBaseChannels(user).forEach(base -> jsonChannels.add(generateChannelJson(base, user)));
            return json(response, ResultJson.success(jsonChannels));
        }
        else {
            return withChannel(request, response, user, (base) -> {
                jsonChannels.add(generateChannelJson(base, user));
                return json(response, ResultJson.success(jsonChannels));
            });
        }
    }

    /**
     * Generate a {@link ChannelsJson} object with all accessible child channels for the given base channel
     *
     * @param base the base channel
     * @param user the current user
     * @return the ChannelsJson object containing all base:children channels
     */
    public static ChannelsJson generateChannelJson(Channel base, User user) {
        List<Channel> children = ChannelFactory.getAccessibleChildChannels(base, user);
        Map<Long, Boolean> recommendedFlags = ChannelManager.computeChannelRecommendedFlags(base, children.stream());
        ChannelsJson jsonChannel = new ChannelsJson();
        jsonChannel.setBase(base);
        jsonChannel.setChildrenWithRecommendedAndArch(children.stream(), recommendedFlags);
        return jsonChannel;
    }
}
