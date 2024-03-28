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
package com.suse.manager.webui.controllers.contentmanagement.handlers;

import static com.suse.manager.webui.utils.SparkApplicationHelper.result;
import static com.suse.manager.webui.utils.SparkApplicationHelper.withUser;
import static spark.Spark.get;

import com.redhat.rhn.common.localization.LocalizationService;
import com.redhat.rhn.domain.channel.Channel;
import com.redhat.rhn.domain.contentmgmt.modulemd.ModulemdApi;
import com.redhat.rhn.domain.contentmgmt.modulemd.ModulemdApiException;
import com.redhat.rhn.domain.user.User;
import com.redhat.rhn.manager.channel.ChannelManager;

import com.suse.manager.webui.utils.gson.ResultJson;

import com.google.gson.reflect.TypeToken;

import org.apache.http.HttpStatus;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import spark.Request;
import spark.Response;
import spark.Spark;

/**
 * Spark controller ContentManagement AppStreams API
 */
public class AppStreamsApiController {

    private static final Logger LOG = LogManager.getLogger(AppStreamsApiController.class);
    private static final ModulemdApi API = new ModulemdApi();
    private static final LocalizationService LOC = LocalizationService.getInstance();

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
            return result(res, ResultJson.success(API.getAllModulesInChannel(channel)), new TypeToken<>() { });
        }
        catch (NumberFormatException e) {
            LOG.error(e.getMessage(), e);
            throw Spark.halt(HttpStatus.SC_BAD_REQUEST);
        }
        catch (ModulemdApiException e) {
            LOG.error(e.getMessage(), e);
            return result(res, ResultJson.error(LOC.getMessage("contentmanagement.modules_error")),
                    new TypeToken<>() { });
        }
    }

}
