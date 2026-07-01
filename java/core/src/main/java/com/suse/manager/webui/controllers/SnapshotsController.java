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

import static com.suse.manager.webui.utils.SparkApplicationHelper.withCsrfToken;
import static com.suse.manager.webui.utils.SparkApplicationHelper.withDocsLocale;
import static com.suse.manager.webui.utils.SparkApplicationHelper.withUserAndServer;
import static spark.Spark.get;

import com.redhat.rhn.domain.server.Server;
import com.redhat.rhn.domain.user.User;

import com.google.gson.JsonArray;
import com.google.gson.JsonParser;

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

        data.put("snapshots", snapshots.toString());
        return new ModelAndView(data, "templates/minion/snapshots.jade");
    }
}
