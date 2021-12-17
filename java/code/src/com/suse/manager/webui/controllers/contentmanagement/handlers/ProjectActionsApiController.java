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

import static com.suse.manager.webui.utils.SparkApplicationHelper.json;
import static com.suse.manager.webui.utils.SparkApplicationHelper.withUser;
import static spark.Spark.get;
import static spark.Spark.post;

import com.redhat.rhn.domain.user.User;
import com.redhat.rhn.manager.contentmgmt.ContentManager;

import com.suse.manager.webui.controllers.contentmanagement.request.ProjectBuildRequest;
import com.suse.manager.webui.controllers.contentmanagement.request.ProjectPromoteRequest;
import com.suse.manager.webui.utils.gson.ResultJson;

import com.google.gson.Gson;

import org.apache.http.HttpStatus;

import java.util.List;
import java.util.Optional;

import spark.Request;
import spark.Response;

/**
 * Spark controller class for content management actions API endpoints.
 */
public class ProjectActionsApiController {

    private static final Gson GSON = ControllerApiUtils.GSON;
    private static final ContentManager CONTENT_MGR = ControllerApiUtils.CONTENT_MGR;

    private ProjectActionsApiController() {
    }

    /** Init routes for ContentManagement Sources Api.*/
    public static void initRoutes() {
        get("/manager/api/contentmanagement/projects/:projectId",
                withUser(ProjectActionsApiController::project));
        post("/manager/api/contentmanagement/projects/:projectId/build",
                withUser(ProjectActionsApiController::buildProject));
        post("/manager/api/contentmanagement/projects/:projectId/promote",
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
        return ControllerApiUtils.fullProjectJsonResponse(res, projectLabel, user);
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
        List<String> requestErrors = ProjectActionsHandler.validateProjectBuildRequest(projectLabelRequest);
        if (!requestErrors.isEmpty()) {
            return json(GSON, res, HttpStatus.SC_BAD_REQUEST, ResultJson.error(requestErrors));
        }

        String projectLabel = projectLabelRequest.getProjectLabel();
        CONTENT_MGR.buildProject(projectLabel, Optional.ofNullable(projectLabelRequest.getMessage()), true, user);

        return ControllerApiUtils.fullProjectJsonResponse(res, projectLabel, user);
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
        List<String> requestErrors = ProjectActionsHandler.validateProjectPromoteRequest(projectPromoteReq);
        if (!requestErrors.isEmpty()) {
            return json(GSON, res, HttpStatus.SC_BAD_REQUEST, ResultJson.error(requestErrors));
        }

        String projectLabel = projectPromoteReq.getProjectLabel();
        CONTENT_MGR.promoteProject(projectLabel, projectPromoteReq.getEnvironmentPromoteLabel(), true, user);

        return ControllerApiUtils.fullProjectJsonResponse(res, projectLabel, user);
    }



}
