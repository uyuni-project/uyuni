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
package com.suse.manager.webui.controllers.appstreams;

import static com.suse.manager.webui.utils.SparkApplicationHelper.json;
import static com.suse.manager.webui.utils.SparkApplicationHelper.withCsrfToken;
import static com.suse.manager.webui.utils.SparkApplicationHelper.withDocsLocale;
import static com.suse.manager.webui.utils.SparkApplicationHelper.withUser;
import static com.suse.manager.webui.utils.SparkApplicationHelper.withUserAndServer;
import static spark.Spark.get;

import com.redhat.rhn.domain.channel.AppStreamModule;
import com.redhat.rhn.domain.channel.Channel;
import com.redhat.rhn.domain.rhnpackage.Package;
import com.redhat.rhn.domain.server.Server;
import com.redhat.rhn.domain.user.User;
import com.redhat.rhn.manager.appstreams.AppStreamsManager;

import com.suse.manager.webui.controllers.appstreams.response.ChannelModulesResponse;
import com.suse.manager.webui.controllers.contentmanagement.handlers.ControllerApiUtils;
import com.suse.manager.webui.utils.gson.ResultJson;

import com.google.gson.Gson;

import org.apache.http.HttpStatus;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import spark.ModelAndView;
import spark.Request;
import spark.Response;
import spark.Spark;
import spark.template.jade.JadeTemplateEngine;

/**
 * Controller class providing the backend for App Streams support.
 */
public class AppStreamsController {

    private AppStreamsController() { }

    private static final Gson GSON = ControllerApiUtils.GSON;
    private static final Logger LOG = LogManager.getLogger(AppStreamsController.class);

    /**
     * Invoked from Router. Initialize routes for Appstreams Views.
     *
     * @param jade the template engine
     */
    public static void initRoutes(JadeTemplateEngine jade) {
        get("/manager/systems/details/appstreams",
                withCsrfToken(withDocsLocale(withUserAndServer(AppStreamsController::appstreams))),
                jade);
        get("/manager/api/appstreams/:channelId/:nsvca/packages",
                withUser(AppStreamsController::getPackagesInModule));
    }

    /**
     * Handler for the app streams page.
     *
     * @param request the request object
     * @param response the response object
     * @param user the current user
     * @param server the server
     * @return the ModelAndView object to render the page
     */
    public static ModelAndView appstreams(Request request, Response response, User user, Server server) {
        Map<String, Object> data = new HashMap<>();
        var channelsModules =
            server
                .getChannels()
                .stream()
                .sorted(Comparator.comparing(Channel::getLabel))
                .map(it -> new ChannelModulesResponse(
                    it.getId(),
                    it.getLabel(),
                    AppStreamsManager.listChannelModules(it.getId()),
                    server
                ))
                .collect(Collectors.toList());
        data.put("channelsModules", GSON.toJson(channelsModules));
        return new ModelAndView(data, "templates/minion/appstreams.jade");
    }

    /**
     * Return the JSON with all the packages that are part of a module
     *
     * @param req the http request
     * @param res the http response
     * @param user the current user
     * @return the JSON data
     */
    public static String getPackagesInModule(Request req, Response res, User user) {
        try {
            Long channelId = Long.parseLong(req.params("channelId"));
            String nsvca = req.params("nsvca");
            String[] nsvcaParts = nsvca.split(":");

            AppStreamModule module = AppStreamsManager.findModule(
                channelId, nsvcaParts[0], nsvcaParts[1], nsvcaParts[2], nsvcaParts[3], nsvcaParts[4]
            );

            if (module == null) {
                throw Spark.halt(HttpStatus.SC_NOT_FOUND);
            }

            Map<String, List<String>> artifacts = new HashMap<>();
            List<String> sortedPackages = module.getArtifacts()
                    .stream()
                    .map(Package::getNevraWithEpoch)
                    .sorted()
                    .collect(Collectors.toList());
            artifacts.put("packages", sortedPackages);
            return json(res, ResultJson.success(artifacts));
        }
        catch (NumberFormatException e) {
            LOG.error(e.getMessage(), e);
            throw Spark.halt(HttpStatus.SC_BAD_REQUEST);
        }
    }
}

