/*
 * Copyright (c) 2019 SUSE LLC
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
package com.suse.manager.webui.controllers.channels;

import static com.redhat.rhn.common.hibernate.HibernateFactory.doWithoutAutoFlushing;
import static com.suse.manager.webui.controllers.channels.ChannelsUtils.generateChannelJson;
import static com.suse.manager.webui.controllers.channels.ChannelsUtils.getPossibleBaseChannels;
import static com.suse.manager.webui.utils.SparkApplicationHelper.json;
import static com.suse.manager.webui.utils.SparkApplicationHelper.withUser;
import static spark.Spark.get;

import com.redhat.rhn.domain.channel.Channel;
import com.redhat.rhn.domain.contentmgmt.ContentProjectFactory;
import com.redhat.rhn.domain.user.User;
import com.redhat.rhn.frontend.dto.ChannelTreeNode;
import com.redhat.rhn.manager.channel.ChannelManager;

import com.suse.manager.webui.utils.gson.ChannelsJson;
import com.suse.manager.webui.utils.gson.ResultJson;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

import spark.Request;
import spark.Response;

/**
 * Spark controller for Channels Api.
 */
public class ChannelsApiController {

    private ChannelsApiController() {
    }

    /** Invoked from Router. Init routes for ContentManagement Api.*/
    public static void initRoutes() {
        get("/manager/api/channels",
                withUser(ChannelsApiController::getAllChannels));
        get("/manager/api/channels/modular",
                withUser(ChannelsApiController::getModularChannels));
        get("/manager/api/channels/owned",
                withUser(ChannelsApiController::getOwnedChannels));
    }

    /**
     * Get all the existing channels for a user
     *
     * @param req the request
     * @param res the response
     * @param user the current user
     * @return the json response
     */
    public static String getAllChannels(Request req, Response res, User user) {
        return doWithoutAutoFlushing(() -> {
            Boolean filterClm = Boolean.parseBoolean(req.queryParams("filterClm"));
            Set<Long> filterOutIds = new HashSet<>();
            if (filterClm) {
                // filtering Content Lifecycle Management target channels
                filterOutIds.addAll(ContentProjectFactory.listSoftwareEnvironmentTarget().stream()
                        .map(tgt -> tgt.getChannel().getId())
                        .collect(Collectors.toSet()));
            }

            List<ChannelsJson> jsonChannelsFiltered =
                    getPossibleBaseChannels(user).stream()
                            .map(base -> generateChannelJson(base, user))
                            .filter(c -> !filterOutIds.contains(c.getBase().getId()))
                            .collect(Collectors.toList()
            );
            jsonChannelsFiltered.sort(Comparator.comparing(cIn -> cIn.getBase().getId()));

            return json(res, ResultJson.success(jsonChannelsFiltered));
        });
    }

    /**
     * Get existing modular channels for a user
     *
     * @param req the request
     * @param res the response
     * @param user the current user
     * @return the json response
     */
    public static String getModularChannels(Request req, Response res, User user) {
        List<ChannelsJson.ChannelJson> jsonChannels = user.getOrg().getAccessibleChannels().stream()
                .distinct()
                .filter(Channel::isModular)
                .map(ChannelsJson.ChannelJson::new)
                .collect(Collectors.toList());

        return json(res, ResultJson.success(jsonChannels));
    }


    /**
     * Get channels owned by a user
     *
     * @param req the request
     * @param res the response
     * @param user the current user
     * @return the json response
     */
    public static String getOwnedChannels(Request req, Response res, User user) {
        List<ChannelTreeNode> channels = new ArrayList<>(ChannelManager.ownedChannelsTree(user));
        return json(res, ResultJson.success(channels));
    }
}
