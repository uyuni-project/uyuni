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

import static com.suse.manager.webui.utils.SparkApplicationHelper.result;
import static com.suse.manager.webui.utils.SparkApplicationHelper.withCsrfToken;
import static com.suse.manager.webui.utils.SparkApplicationHelper.withDocsLocale;
import static com.suse.manager.webui.utils.SparkApplicationHelper.withUser;
import static com.suse.manager.webui.utils.SparkApplicationHelper.withUserAndServer;
import static com.suse.manager.webui.utils.gson.ResultJson.error;
import static com.suse.manager.webui.utils.gson.ResultJson.success;
import static spark.Spark.get;
import static spark.Spark.post;

import com.redhat.rhn.common.localization.LocalizationService;
import com.redhat.rhn.domain.action.ActionChainFactory;
import com.redhat.rhn.domain.channel.AppStream;
import com.redhat.rhn.domain.channel.Channel;
import com.redhat.rhn.domain.server.Server;
import com.redhat.rhn.domain.user.User;
import com.redhat.rhn.frontend.context.Context;
import com.redhat.rhn.frontend.struts.ActionChainHelper;
import com.redhat.rhn.manager.appstreams.AppStreamsManager;
import com.redhat.rhn.taskomatic.TaskomaticApiException;

import com.suse.manager.reactor.utils.LocalDateTimeISOAdapter;
import com.suse.manager.reactor.utils.OptionalTypeAdapterFactory;
import com.suse.manager.webui.controllers.appstreams.response.ChannelAppStreamsResponse;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.reflect.TypeToken;

import org.apache.http.HttpStatus;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.Date;
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

    private static final Gson GSON = new GsonBuilder()
            .registerTypeAdapter(LocalDateTime.class, new LocalDateTimeISOAdapter())
            .registerTypeAdapterFactory(new OptionalTypeAdapterFactory())
            .serializeNulls()
            .create();
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
        get("/manager/api/appstreams/:channelId/:appstream/packages",
                withUser(AppStreamsController::getPackagesInModule));
        post("/manager/api/appstreams/save",
                withUser(AppStreamsController::save));
    }

    /**
     * Saves the changes (enable/disable) to AppStreams.
     *
     * @param request  the HTTP request
     * @param response the HTTP response
     * @param user     the user performing the action
     * @return a JSON response indicating success or failure
     */
    public static String save(Request request, Response response, User user) {
        AppStreamsChangesJson params = GSON.fromJson(request.body(), AppStreamsChangesJson.class);
        try {
            Long actionId = AppStreamsManager.scheduleAppStreamsChanges(
                params.getSid(),
                params.getToEnable(),
                params.getToDisable(),
                user,
                params.getActionChainLabel(),
                params.getEarliest().map(AppStreamsController::getScheduleDate).orElse(new Date())
            );

            return result(response, success(params.getActionChainLabel()
                    .map(l -> ActionChainFactory.getActionChain(user, l).getId()).orElse(actionId)),
                    new TypeToken<>() { });
        }
        catch (TaskomaticApiException e) {
            return result(response, error(LocalizationService.getInstance().getMessage("taskscheduler.down")),
                    new TypeToken<>() { });
        }
    }

    private static Date getScheduleDate(LocalDateTime dateTime) {
        ZoneId zoneId = Context.getCurrentContext().getTimezone().toZoneId();
        return Date.from(dateTime.atZone(zoneId).toInstant());
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
        var channelsAppStreams =
            server
                .getChannels()
                .stream()
                .filter(Channel::isModular)
                .map(channel -> new ChannelAppStreamsResponse(
                    channel,
                    AppStreamsManager.listChannelAppStreams(channel.getId()),
                    server
                ))
                .collect(Collectors.toList());
        data.put("channelsAppStreams", GSON.toJson(channelsAppStreams));
        data.put("actionChains", ActionChainHelper.actionChainsJson(user));
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
            String appStreamParam = req.params("appstream");
            String[] appStreamParts = appStreamParam.split(":");

            AppStream appStream = AppStreamsManager.findAppStream(
                channelId, appStreamParts[0], appStreamParts[1]
            );

            if (appStream == null) {
                throw Spark.halt(HttpStatus.SC_NOT_FOUND);
            }

            Map<String, List<PackageJson>> artifacts = new HashMap<>();
            List<PackageJson> sortedPackages = appStream.getArtifacts()
                .stream()
                .map(PackageJson::new)
                .collect(Collectors.toList());
            artifacts.put("packages", sortedPackages);
            return result(res, success(artifacts), new TypeToken<>() { });
        }
        catch (NumberFormatException e) {
            LOG.error(e.getMessage(), e);
            throw Spark.halt(HttpStatus.SC_BAD_REQUEST);
        }
    }
}
