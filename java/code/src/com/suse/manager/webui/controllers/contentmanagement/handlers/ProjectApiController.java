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

import com.redhat.rhn.domain.contentmgmt.ContentManagementException;
import com.redhat.rhn.domain.contentmgmt.ContentProject;
import com.redhat.rhn.domain.user.User;
import com.redhat.rhn.manager.EntityExistsException;
import com.redhat.rhn.manager.contentmgmt.ContentManager;

import com.suse.manager.webui.controllers.contentmanagement.request.NewProjectRequest;
import com.suse.manager.webui.controllers.contentmanagement.request.ProjectPropertiesRequest;
import com.suse.manager.webui.utils.FlashScopeHelper;
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
 * Spark controller ContentManagement Project Api.
 */
public class ProjectApiController {

    private static final Gson GSON = Json.GSON;

    private ProjectApiController() {
    }

    /** Init routes for ContentManagement Project Api.*/
    public static void initRoutes() {
        post("/manager/contentmanagement/api/projects",
                withUser(ProjectApiController::createContentProject));

        delete("/manager/contentmanagement/api/projects/:projectId",
                withUser(ProjectApiController::removeContentProject));

        put("/manager/contentmanagement/api/projects/:projectId/properties",
                withUser(ProjectApiController::updateContentProjectProperties));

    }

    /**
     * Return the JSON with the result of the creation of a content project.
     * @param req the http request
     * @param res the http response
     * @param user the current user
     * @return the JSON data
     */
    public static String createContentProject(Request req, Response res, User user) {
        NewProjectRequest createProjectRequest = ProjectHandler.getProjectRequest(req);
        HashMap<String, String> requestErrors = ProjectHandler.validateProjectRequest(createProjectRequest, user);
        if (!requestErrors.isEmpty()) {
            return json(GSON, res, HttpStatus.SC_BAD_REQUEST, ResultJson.error(Arrays.asList(""), requestErrors));
        }

        ProjectPropertiesRequest projectPropertiesRequest = createProjectRequest.getProperties();

        ContentProject createdProject;
        try {
            createdProject = ContentManager.createProject(
                    projectPropertiesRequest.getLabel(),
                    projectPropertiesRequest.getName(),
                    projectPropertiesRequest.getDescription(),
                    user
            );
        }
        catch (EntityExistsException error) {
            return json(GSON, res, HttpStatus.SC_BAD_REQUEST, ResultJson.error("Project already exists"));
        }
        catch (ContentManagementException error) {
            return json(GSON, res, HttpStatus.SC_BAD_REQUEST, ResultJson.error(error.getMessage()));
        }

        FlashScopeHelper.flash(
                req,
                String.format("Project %s created successfully.", projectPropertiesRequest.getLabel())
        );

        return ControllerApiUtils.fullProjectJsonResponse(res, createdProject.getLabel(), user);
    }

    /**
     * Return the JSON with the result of removing a content project.
     * @param req the http request
     * @param res the http response
     * @param user the current user
     * @return the JSON data
     */
    public static String removeContentProject(Request req, Response res, User user) {
        String projectLabel = req.params("projectId");

        int removingResult = ContentManager.removeProject(projectLabel, user);

        if (removingResult == 1) {
            String successMessage = String.format("Project %s deleted successfully.", projectLabel);
            FlashScopeHelper.flash(
                    req,
                    successMessage
            );
            return json(GSON, res, ResultJson.successMessage(successMessage));
        }

        return json(GSON, res, ResultJson.error());
    }

    /**
     * Return the JSON with the result of updating the properties of a content project.
     * @param req the http request
     * @param res the http response
     * @param user the current user
     * @return the JSON data
     */
    public static String updateContentProjectProperties(Request req, Response res, User user) {
        ProjectPropertiesRequest updateProjectPropertiesRequest = ProjectHandler.getProjectPropertiesRequest(req);

        HashMap<String, String> requestErrors = ProjectHandler.validateProjectPropertiesRequest(
                updateProjectPropertiesRequest, user
        );
        if (!requestErrors.isEmpty()) {
            return json(GSON, res, HttpStatus.SC_BAD_REQUEST, ResultJson.error(Arrays.asList(""), requestErrors));
        }

        ContentProject updatedProject;
        try {
            updatedProject = ContentManager.updateProject(
                    updateProjectPropertiesRequest.getLabel(),
                    Optional.ofNullable(updateProjectPropertiesRequest.getName()),
                    Optional.ofNullable(updateProjectPropertiesRequest.getDescription()),
                    user
            );
        }
        catch (ContentManagementException error) {
            return json(GSON, res, HttpStatus.SC_BAD_REQUEST, ResultJson.error(error.getMessage()));
        }

        return ControllerApiUtils.fullProjectJsonResponse(res, updatedProject.getLabel(), user);
    }

}
