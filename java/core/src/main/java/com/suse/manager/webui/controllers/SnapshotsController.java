/*
 * Copyright (c) 2026 SUSE LLC
 *
 * This software is licensed to you under the GNU General Public License,
 * version 2 (GPLv2). There is NO WARRANTY for this software, express or
 * implied, including the implied warranties of MERCHANTABILITY or FITNESS
 * FOR A PARTICULAR PURPOSE. You should have received a copy of GPLv2
 * along with this software; if not, see
 * http://www.gnu.org/licenses/old-licenses/gpl-2.0.txt.
 */
package com.suse.manager.webui.controllers;

import static com.suse.manager.webui.utils.SparkApplicationHelper.asJson;
import static com.suse.manager.webui.utils.SparkApplicationHelper.success;
import static com.suse.manager.webui.utils.SparkApplicationHelper.withCsrfToken;
import static com.suse.manager.webui.utils.SparkApplicationHelper.withDocsLocale;
import static com.suse.manager.webui.utils.SparkApplicationHelper.withUserAndServer;
import static spark.Spark.get;
import static spark.Spark.post;

import com.redhat.rhn.common.RhnRuntimeException;
import com.redhat.rhn.domain.action.Action;
import com.redhat.rhn.domain.server.Server;
import com.redhat.rhn.domain.user.User;
import com.redhat.rhn.manager.action.ActionManager;
import com.redhat.rhn.taskomatic.TaskomaticApiException;

import com.suse.manager.webui.utils.gson.ResultJson;

import com.google.gson.JsonArray;
import com.google.gson.JsonParser;

import java.util.Date;
import java.util.HashMap;
import java.util.Map;

import spark.ModelAndView;
import spark.Request;
import spark.Response;
import spark.template.jade.JadeTemplateEngine;

/**
 * Controller for the Btrfs Snapshots page under Software for transactional systems.
 */
public class SnapshotsController {

    private SnapshotsController() { }

    /**
     * Invoked from Router. Initialize routes for the Snapshots view.
     *
     * @param jade the template engine
     */
    public static void initRoutes(JadeTemplateEngine jade) {
        get("/manager/systems/details/snapshots",
                withCsrfToken(withDocsLocale(withUserAndServer(SnapshotsController::snapshots))),
                jade);
        post("/manager/api/systems/:sid/details/snapshots/refresh",
                asJson(withUserAndServer(SnapshotsController::refreshSnapshots)));
    }

    /**
     * Handler for the Btrfs snapshots page.
     *
     * @param request  the request object
     * @param response the response object
     * @param user     the current user
     * @param server   the server
     * @return the ModelAndView to render
     */
    public static ModelAndView snapshots(Request request, Response response, User user, Server server) {
        Map<String, Object> data = new HashMap<>();

        JsonArray snapshots = server.asMinionServer()
                .map(minion -> minion.getSnapshotDetails())
                .filter(details -> details != null)
                .map(details -> JsonParser.parseString(details).getAsJsonArray())
                .orElseGet(JsonArray::new);
        String snapshotUpdated = server.asMinionServer()
                .map(minion -> minion.getSnapshotUpdated())
                .map(date -> date.toInstant().toString())
                .orElse(null);

        data.put("snapshots", snapshots.toString());
        data.put("snapshotUpdated", snapshotUpdated);
        return new ModelAndView(data, "templates/minion/snapshots.jade");
    }

    /**
     * Schedule a Btrfs snapshot refresh action.
     *
     * @param request  the request object
     * @param response the response object
     * @param user     the current user
     * @param server   the server
     * @return action event redirect data
     */
    public static String refreshSnapshots(Request request, Response response, User user, Server server) {
        try {
            Action action = ActionManager.scheduleSnapshotRefreshAction(user, server, new Date());
            String redirectUrl = "/rhn/systems/details/history/Event.do?sid=" + server.getId() + "&aid=" +
                    action.getId();
            return success(response, ResultJson.success(Map.of("redirectUrl", redirectUrl)));
        }
        catch (TaskomaticApiException e) {
            throw new RhnRuntimeException("Unable to schedule snapshot refresh action", e);
        }
    }
}
