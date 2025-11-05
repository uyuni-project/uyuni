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
import com.redhat.rhn.frontend.dto.ChannelTreeNode;
import com.redhat.rhn.frontend.struts.ActionChainHelper;
import com.redhat.rhn.manager.appstreams.AppStreamsManager;
import com.redhat.rhn.manager.channel.ChannelManager;
import com.redhat.rhn.taskomatic.TaskomaticApiException;

import com.suse.manager.reactor.utils.LocalDateTimeISOAdapter;
import com.suse.manager.reactor.utils.OptionalTypeAdapterFactory;
import com.suse.manager.webui.controllers.appstreams.response.ChannelAppStreamsJson;
import com.suse.manager.webui.controllers.appstreams.response.ChannelAppStreamsResponse;
import com.suse.manager.webui.controllers.appstreams.response.SsmAppStreamModuleResponse;
import com.suse.manager.webui.utils.ViewHelper;

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
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.TimeZone;
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
        get("/manager/systems/ssm/appstreams",
                withCsrfToken(withDocsLocale(withUser(AppStreamsController::ssmAppstreamsChannelSelection))), jade);
        get("/manager/systems/ssm/appstreams/configure/:channelId",
                withCsrfToken(withDocsLocale(withUser(AppStreamsController::ssmConfigureAppstreams))), jade);
        post("/manager/api/ssm/appstreams/save",
                withUser(AppStreamsController::ssmSave));
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

    /**
     * Saves the SSM changes to AppStreams.
     *
     * @param request  the HTTP request
     * @param response the HTTP response
     * @param user     the user performing the action
     * @return a JSON response indicating success or failure
     */
    public static String ssmSave(Request request, Response response, User user) {
        SsmAppStreamsChangesJson params = GSON.fromJson(request.body(), SsmAppStreamsChangesJson.class);

        try {
            var earliest = params.getEarliest().map(AppStreamsController::getScheduleDate).orElse(new Date());
            Long resultId = AppStreamsManager.scheduleSsmAppStreamsChanges(
                params.getChannelId(),
                params.getToEnable(),
                params.getToDisable(),
                user,
                params.getActionChainLabel(),
                earliest
            );

            return result(response, success(resultId), new TypeToken<>() { });
        }
        catch (TaskomaticApiException e) {
            var message = LocalizationService.getInstance().getMessage("taskscheduler.down");
            LOG.error(message, e);
            return result(response, error(message), new TypeToken<>() { });
        }
    }

    private static Date getScheduleDate(LocalDateTime dateTime) {
        ZoneId zoneId = Optional.ofNullable(Context.getCurrentContext().getTimezone())
                .orElse(TimeZone.getDefault()).toZoneId();
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
                    server::hasAppStreamModuleEnabled
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

    /**
     * Handler for the ssm appstreams page
     *
     * @param request the request
     * @param response the response
     * @param user the current user
     * @return the ModelAndView object to render the page
     */
    public static ModelAndView ssmConfigureAppstreams(Request request, Response response, User user) {
        var channel = getAppStreamChannelFromPath(user, request.params("channelId"));
        if (channel == null) {
            return ssmAppstreamsChannelSelection(request, response, user);
        }
        var ssmAppStreams = AppStreamsManager.listSsmChannelAppStreams(channel.getId(), user);
        Map<String, Set<SsmAppStreamModuleResponse>> modulesMap = new HashMap<>();
        for (SsmAppStreamModuleResponse ssmAppstream : ssmAppStreams) {
            modulesMap
                .computeIfAbsent(ssmAppstream.getName(), k -> new HashSet<>())
                .add(ssmAppstream);
        }
        Map<String, Object> channelAppStreamsData = new HashMap<>();
        channelAppStreamsData.put("channel", channel);
        channelAppStreamsData.put("appStreams", modulesMap);
        Map<String, Object> data = new HashMap<>();
        data.put("tabs", ViewHelper.getInstance().renderNavigationMenu(request, "/WEB-INF/nav/ssm.xml"));
        data.put("appstreams", GSON.toJson(channelAppStreamsData));
        data.put("actionChains", ActionChainHelper.actionChainsJson(user));
        return new ModelAndView(data, "templates/ssm/appstreams-configure.jade");
    }

    /**
     * Handler for the SSM AppStream channel selection page.
     * <p>
     * This page displays the list of modular channels associated with the SSM systems.
     *
     * @param request  the HTTP request
     * @param response the HTTP response
     * @param user     the current user
     * @return the ModelAndView object to render the page
     */
    public static ModelAndView ssmAppstreamsChannelSelection(Request request, Response response, User user) {
        List<ChannelTreeNode> channels = ChannelManager.getModularChannelsForSsm(user);
        String channelsJson = GSON.toJson(channels);
        Map<String, Object> data = new HashMap<>();
        data.put("tabs", ViewHelper.getInstance().renderNavigationMenu(request, "/WEB-INF/nav/ssm.xml"));
        data.put("channels", channelsJson);
        return new ModelAndView(data, "templates/ssm/appstreams-channel-selection.jade");
    }

    private static ChannelAppStreamsJson getAppStreamChannelFromPath(User user, String channelId) {
        if (channelId == null) {
            LOG.warn("Invalid request, no channelId parameter");
            return null;
        }

        try {
            var channelIdLong = Long.parseLong(channelId);
            List<ChannelTreeNode> channels = ChannelManager.getModularChannelsForSsm(user);
            return channels
                .stream()
                .filter(it -> it.isModular() && it.getId().equals(channelIdLong))
                .findFirst()
                .map(ch -> new ChannelAppStreamsJson(ch.getId(), ch.getChannelLabel(), ch.getName()))
                .orElse(null);
        }
        catch (NumberFormatException ex) {
            LOG.warn("Ignoring invalid parameter {} passed as channelId", channelId, ex);
            return null;
        }
    }
}
