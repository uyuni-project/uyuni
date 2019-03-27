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

import static com.suse.manager.webui.utils.SparkApplicationHelper.json;
import static com.suse.manager.webui.utils.SparkApplicationHelper.withUser;
import static spark.Spark.delete;
import static spark.Spark.post;
import static spark.Spark.put;

import com.redhat.rhn.domain.user.User;
import com.redhat.rhn.manager.contentmgmt.ContentManager;

import com.suse.manager.webui.controllers.contentmanagement.request.EnvironmentRequest;
import com.suse.manager.webui.utils.gson.ResultJson;
import com.suse.utils.Json;

import com.google.gson.Gson;

import org.apache.http.HttpStatus;

import java.util.Arrays;
import java.util.HashMap;
import java.util.Optional;

import spark.Request;
import spark.Response;

/**
 * Spark controller ContentManagement Environment Api.
 */
public class EnvironmentApiController {

    private static final Gson GSON = Json.GSON;

    private EnvironmentApiController() {
    }

    /** Init routes for ContentManagement Environment Api.*/
    public static void initRoutes() {
        post("/manager/contentmanagement/api/projects/:projectId/environments",
                withUser(EnvironmentApiController::createContentEnvironemnt));

        put("/manager/contentmanagement/api/projects/:projectId/environments",
                withUser(EnvironmentApiController::updateContentEnvironemnt));

        delete("/manager/contentmanagement/api/projects/:projectId/environments",
                withUser(EnvironmentApiController::removeContentEnvironemnt));
    }

    /**
     * Return the JSON with the result of the creation of a content project environemnt.
     * @param req the http request
     * @param res the http response
     * @param user the current user
     * @return the JSON data
     */
    public static String createContentEnvironemnt(Request req, Response res, User user) {
        EnvironmentRequest createEnvironmentRequest = EnvironmentHandler.getEnvironmentRequest(req);

        HashMap<String, String> requestErrors = EnvironmentHandler.validateEnvironmentRequest(createEnvironmentRequest);
        if (!requestErrors.isEmpty()) {
            return json(GSON, res, HttpStatus.SC_BAD_REQUEST, ResultJson.error(Arrays.asList(""), requestErrors));
        }

        ContentManager.createEnvironment(
                createEnvironmentRequest.getProjectLabel(),
                Optional.ofNullable(createEnvironmentRequest.getPredecessorLabel()),
                createEnvironmentRequest.getLabel(),
                createEnvironmentRequest.getName(),
                createEnvironmentRequest.getDescription(),
                user
        );

        return ControllerUtils.fullProjectJson(res, createEnvironmentRequest.getProjectLabel(), user);
    }

    /**
     * Return the JSON with the result of updating a content project environemnt.
     * @param req the http request
     * @param res the http response
     * @param user the current user
     * @return the JSON data
     */
    public static String updateContentEnvironemnt(Request req, Response res, User user) {
        EnvironmentRequest updateEnvironmentRequest = EnvironmentHandler.getEnvironmentRequest(req);

        HashMap<String, String> requestErrors = EnvironmentHandler.validateEnvironmentRequest(updateEnvironmentRequest);
        if (!requestErrors.isEmpty()) {
            return json(GSON, res, HttpStatus.SC_BAD_REQUEST, ResultJson.error(Arrays.asList(""), requestErrors));
        }

        ContentManager.updateEnvironment(
                updateEnvironmentRequest.getLabel(),
                updateEnvironmentRequest.getProjectLabel(),
                Optional.ofNullable(updateEnvironmentRequest.getName()),
                Optional.ofNullable(updateEnvironmentRequest.getDescription()),
                user
        );

        return ControllerUtils.fullProjectJson(res, updateEnvironmentRequest.getProjectLabel(), user);

    }

    /**
     * Return the JSON with the result of removing a content project environemnt.
     * @param req the http request
     * @param res the http response
     * @param user the current user
     * @return the JSON data
     */
    public static String removeContentEnvironemnt(Request req, Response res, User user) {
        EnvironmentRequest removeEnvironmentRequest = EnvironmentHandler.getEnvironmentRequest(req);

        HashMap<String, String> requestErrors = EnvironmentHandler.validateEnvironmentRequest(removeEnvironmentRequest);
        if (!requestErrors.isEmpty()) {
            return json(GSON, res, HttpStatus.SC_BAD_REQUEST, ResultJson.error(Arrays.asList(""), requestErrors));
        }

        ContentManager.removeEnvironment(
                removeEnvironmentRequest.getLabel(),
                removeEnvironmentRequest.getProjectLabel(),
                user
        );

        return ControllerUtils.fullProjectJson(res, removeEnvironmentRequest.getProjectLabel(), user);
    }

}
