/**
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

import static com.suse.manager.webui.controllers.channels.ChannelsUtils.generateChannelJson;
import static com.suse.manager.webui.controllers.channels.ChannelsUtils.getPossibleBaseChannels;
import static com.suse.manager.webui.utils.SparkApplicationHelper.json;
import static com.suse.manager.webui.utils.SparkApplicationHelper.withUser;
import static spark.Spark.get;

import com.redhat.rhn.domain.contentmgmt.ContentProjectFactory;
import com.redhat.rhn.domain.user.User;

import com.suse.manager.webui.utils.gson.ChannelsJson;
import com.suse.manager.webui.utils.gson.ResultJson;

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
        Boolean filterClm = Boolean.parseBoolean(req.queryParams("filterClm"));
        Set<Long> filterOutIds = new HashSet<>();
        if (filterClm) {
            // filtering Content Lifecycle Management target channels
            filterOutIds.addAll(ContentProjectFactory.listSoftwareEnvironmentTarget().stream()
                    .map(tgt -> tgt.getChannel().getId())
                    .collect(Collectors.toSet()));
        }

        List<ChannelsJson> jsonChannelsFiltered = getPossibleBaseChannels(user).stream()
                .map(base -> generateChannelJson(base, user))
                .filter(c -> !filterOutIds.contains(c.getBase().getId()))
                .collect(Collectors.toList());

        return json(res, ResultJson.success(jsonChannelsFiltered));
    }


}
