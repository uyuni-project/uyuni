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
package com.suse.manager.webui.controllers.contentmanagement.handlers;

import com.redhat.rhn.domain.channel.Channel;
import com.redhat.rhn.domain.contentmgmt.modulemd.ModulemdApi;
import com.redhat.rhn.domain.user.User;
import com.redhat.rhn.manager.channel.ChannelManager;
import com.suse.manager.webui.utils.gson.ResultJson;
import org.apache.http.HttpStatus;
import spark.Request;
import spark.Response;
import spark.Spark;

import static com.suse.manager.webui.utils.SparkApplicationHelper.json;
import static com.suse.manager.webui.utils.SparkApplicationHelper.withUser;
import static spark.Spark.get;

/**
 * Spark controller ContentManagement AppStreams API
 */
public class AppStreamsApiController {

    private static ModulemdApi api = new ModulemdApi();

    private AppStreamsApiController() { }

    /** Init routes for ContentManagement AppStreams Api.*/
    public static void initRoutes() {
        get("/manager/api/contentmanagement/appstreams/:channelId",
                withUser(AppStreamsApiController::getModulesInChannel));
    }

    /**
     * Return the JSON with all the available module streams for a specified modular channel
     *
     * @param req the http request
     * @param res the http response
     * @param user the current user
     * @return the JSON data
     */
    public static String getModulesInChannel(Request req, Response res, User user) {
        try {
            Channel channel = ChannelManager.lookupByIdAndUser(Long.parseLong(req.params("channelId")), user);
            return json(res, ResultJson.success(api.getAllModulesInChannel(channel)));
        }
        catch (NumberFormatException e) {
            throw Spark.halt(HttpStatus.SC_BAD_REQUEST);
        }
    }

}
