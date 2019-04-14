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
import static spark.Spark.get;
import static spark.Spark.post;

import com.redhat.rhn.domain.user.User;
import com.redhat.rhn.manager.contentmgmt.ContentManager;

import com.suse.manager.webui.controllers.contentmanagement.request.ProjectBuildRequest;
import com.suse.manager.webui.controllers.contentmanagement.request.ProjectPromoteRequest;
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
 * Spark controller class for content management actions API endpoints.
 */
public class ProjectActionsApiController {

    private static final Gson GSON = Json.GSON;

    private ProjectActionsApiController() {
    }

    /** Init routes for ContentManagement Sources Api.*/
    public static void initRoutes() {
        get("/manager/contentmanagement/api/projects/:projectId",
                withUser(ProjectActionsApiController::project));
        post("/manager/contentmanagement/api/projects/:projectId/build",
                withUser(ProjectActionsApiController::buildProject));
        post("/manager/contentmanagement/api/projects/:projectId/promote",
                withUser(ProjectActionsApiController::promoteProject));
    }

    /**
     * Return the JSON with the project updated result.
     * @param req the http request
     * @param res the http response
     * @param user the current user
     * @return the JSON data
     */
    public static String project(Request req, Response res, User user) {
        String projectLabel = req.params("projectId");
        return ControllerUtils.fullProjectJson(res, projectLabel, user);
    }

    /**
     * Return the JSON with the result of building the content project.
     * @param req the http request
     * @param res the http response
     * @param user the current user
     * @return the JSON data
     */
    public static String buildProject(Request req, Response res, User user) {
        ProjectBuildRequest projectLabelRequest = ProjectActionsHandler.getProjectBuildRequest(req);
        HashMap<String, String> requestErrors = ProjectActionsHandler.validateProjectBuildRequest(projectLabelRequest);
        if (!requestErrors.isEmpty()) {
            return json(GSON, res, HttpStatus.SC_BAD_REQUEST, ResultJson.error(Arrays.asList(""), requestErrors));
        }

        String projectLabel = projectLabelRequest.getProjectLabel();
        ContentManager.buildProject(projectLabel, Optional.ofNullable(projectLabelRequest.getMessage()), true, user);

        return ControllerUtils.fullProjectJson(res, projectLabel, user);
    }

    /**
     * Return the JSON with the result of promoting the content project.
     * @param req the http request
     * @param res the http response
     * @param user the current user
     * @return the JSON data
     */
    public static String promoteProject(Request req, Response res, User user) {
        ProjectPromoteRequest projectPromoteReq = ProjectActionsHandler.getProjectPromoteRequest(req);
        HashMap<String, String> requestErrors = ProjectActionsHandler.validateProjectPromoteRequest(projectPromoteReq);
        if (!requestErrors.isEmpty()) {
            return json(GSON, res, HttpStatus.SC_BAD_REQUEST, ResultJson.error(Arrays.asList(""), requestErrors));
        }

        String projectLabel = projectPromoteReq.getProjectLabel();
        ContentManager.promoteProject(projectLabel, projectPromoteReq.getEnvironmentPromoteLabel(), true, user);

        return ControllerUtils.fullProjectJson(res, projectLabel, user);
    }



}
