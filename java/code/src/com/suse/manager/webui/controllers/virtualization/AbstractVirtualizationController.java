/*
 * Copyright (c) 2020 SUSE LLC
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
package com.suse.manager.webui.controllers.virtualization;


import static com.suse.manager.webui.utils.SparkApplicationHelper.json;

import com.redhat.rhn.common.hibernate.LookupException;
import com.redhat.rhn.domain.action.Action;
import com.redhat.rhn.domain.server.Server;
import com.redhat.rhn.domain.user.User;
import com.redhat.rhn.manager.system.SystemManager;
import com.redhat.rhn.taskomatic.TaskomaticApiException;

import com.suse.manager.reactor.utils.LocalDateTimeISOAdapter;
import com.suse.manager.reactor.utils.OptionalTypeAdapterFactory;
import com.suse.manager.virtualization.VirtualizationActionHelper;
import com.suse.manager.webui.controllers.ECMAScriptDateAdapter;
import com.suse.manager.webui.errors.NotFoundException;
import com.suse.manager.webui.services.iface.VirtManager;
import com.suse.manager.webui.utils.gson.ScheduledRequestJson;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.reflect.TypeToken;

import org.apache.http.HttpStatus;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.time.LocalDateTime;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.function.BiFunction;
import java.util.function.Function;
import java.util.function.Supplier;
import java.util.stream.Collectors;

import spark.ModelAndView;
import spark.Request;
import spark.Response;
import spark.Spark;

/**
 * Base controller class offering convenience functions for other virtualization-related controllers
 */
public abstract class AbstractVirtualizationController {
    private static final Logger LOG = LogManager.getLogger(AbstractVirtualizationController.class);

    protected static final Gson GSON = new GsonBuilder()
            .registerTypeAdapter(Date.class, new ECMAScriptDateAdapter())
            .registerTypeAdapter(LocalDateTime.class, new LocalDateTimeISOAdapter())
            .registerTypeAdapterFactory(new OptionalTypeAdapterFactory())
            .serializeNulls()
            .create();

    protected final String jadeTemplatesPath;
    protected final VirtManager virtManager;

    /**
     * @param virtManagerIn instance to manage virtualization
     * @param jadeTemplatesPathIn the path to the folder containing the Jade templates
     */
    protected AbstractVirtualizationController(VirtManager virtManagerIn, String jadeTemplatesPathIn) {
        this.virtManager = virtManagerIn;
        this.jadeTemplatesPath = jadeTemplatesPathIn;
    }

    /**
     * Extract the server ID from the request. The Server ID should be in the sid Spark parameter.
     *
     * @param request the Spark request
     * @return the server Id
     *
     * @throws NotFoundException if the server id has an invalid format or if the sid parameter can't be found
     */
    protected long getServerId(Request request) throws NotFoundException {
        try {
            return Long.parseLong(request.params("sid"));
        }
        catch (NumberFormatException e) {
            LOG.error("Invalid server id: {}", request.params("sid"), e);
            throw Spark.halt(HttpStatus.SC_NOT_FOUND, "Invalid server id: " + request.params("sid"));
        }
    }

    /**
     * Get the server from the request. The Server ID should be in the sid Spark parameter.
     *
     * @param request the Spark request
     * @return the server Id
     *
     * @throws NotFoundException if the server can't be found in the request or in the database
     */
    protected Server getServer(Request request, User user) throws NotFoundException {
        Server server;
        long serverId = getServerId(request);

        try {
            server = SystemManager.lookupByIdAndUser(serverId, user);
        }
        catch (LookupException e) {
            throw new NotFoundException();
        }
        return server;
    }

    /**
     * Displays a page server-related virtual page
     *
     * @param template the name to the Jade template of the page
     * @param modelExtender provides additional properties to pass to the Jade template
     * @return the ModelAndView object to render the page
     */
    protected ModelAndView renderPage(String template, Supplier<Map<String, Object>> modelExtender) {
        Map<String, Object> data = new HashMap<>();
        if (modelExtender != null) {
            data.putAll(modelExtender.get());
        }

        /* For the rest of the template */

        return new ModelAndView(data, String.format("%s/%s.jade", jadeTemplatesPath, template));
    }

    protected <T extends ScheduledRequestJson> String action(Request request, Response response, User user,
                          Server host,
                          BiFunction<T, String, Action> actionCreator,
                          Function<T, List<String>> actionKeysGetter,
                          Class<T> jsonClass) {

        T data;
        try {
            data = GSON.fromJson(request.body(), jsonClass);

            List<String> actionKeys = actionKeysGetter.apply(data);
            if (!actionKeys.isEmpty()) {
                Map<String, String> actionsResults = actionKeys.stream().collect(
                        Collectors.toMap(Function.identity(),
                                key -> scheduleAction(key, user, host, actionCreator, data)
                        ));
                return json(response, actionsResults, new TypeToken<>() { });
            }

            String result = scheduleAction(null, user, host, actionCreator, data);
            return json(response, result);
        }
        catch (Exception e) {
            LOG.error("Bad Request", e);
            throw Spark.halt(HttpStatus.SC_BAD_REQUEST);
        }
    }

    private <T extends ScheduledRequestJson> String scheduleAction(String key, User user, Server host,
                                  BiFunction<T, String, Action> actionCreator,
                                  T data) {
        String status = "Failed";
        try {
            status = String.valueOf(VirtualizationActionHelper.scheduleAction(key, user, host, actionCreator, data));
        }
        catch (TaskomaticApiException e) {
            LOG.error("Could not schedule virtualization action:", e);
        }
        return status;
    }
}
