package com.suse.manager.webui.controllers.contentmanagement.handlers;


import static com.suse.manager.webui.utils.SparkApplicationHelper.json;
import static com.suse.manager.webui.utils.SparkApplicationHelper.withUser;
import static spark.Spark.delete;
import static spark.Spark.post;
import static spark.Spark.put;

import com.redhat.rhn.domain.contentmgmt.ContentEnvironment;
import com.redhat.rhn.domain.contentmgmt.ContentManagementException;
import com.redhat.rhn.domain.contentmgmt.ContentManager;
import com.redhat.rhn.domain.contentmgmt.ContentProject;
import com.redhat.rhn.domain.user.User;

import com.suse.manager.webui.controllers.contentmanagement.mappers.ResponseMappers;
import com.suse.manager.webui.controllers.contentmanagement.request.ProjectPropertiesRequest;
import com.suse.manager.webui.controllers.contentmanagement.request.ProjectRequest;
import com.suse.manager.webui.utils.FlashScopeHelper;
import com.suse.manager.webui.utils.gson.ResultJson;
import com.suse.utils.Json;

import com.google.gson.Gson;

import org.apache.http.HttpStatus;

import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Optional;

import spark.Request;
import spark.Response;

/**
 * Spark controller class for content management pages and API endpoints.
 */
public class ProjectApiController {

    private static final Gson GSON = Json.GSON;

    private ProjectApiController() {
    }

    public static void initRoutes() {
        post("/manager/contentmanagement/api/projects",
                withUser(ProjectApiController::createContentProject));

        delete("/manager/contentmanagement/api/projects/:projectId",
                withUser(ProjectApiController::deleteContentProject));

        put("/manager/contentmanagement/api/projects/:projectId/properties",
                withUser(ProjectApiController::updateContentProjectProperties));

    }

    public static String createContentProject(Request req, Response res, User user) {
        ProjectRequest createProjectRequest = ProjectHandler.getProjectRequest(req);
        HashMap<String, String> requestErrors = ProjectHandler.validateProjectRequest(createProjectRequest);
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
        catch (ContentManagementException error) {
            return json(GSON, res, HttpStatus.SC_BAD_REQUEST, ResultJson.error(error.getMessage()));
        }

        FlashScopeHelper.flash(
                req,
                String.format("Project %s created successfully.", projectPropertiesRequest.getLabel())
        );

        List<ContentEnvironment> contentEnvironments = ContentManager.listProjectEnvironments(
                createdProject.getLabel(), user
        );

        return json(GSON, res, ResultJson.success(
                ResponseMappers.mapProjectFromDB(createdProject, contentEnvironments))
        );
    }

    public static String deleteContentProject(Request req, Response res, User user) {
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

    public static String updateContentProjectProperties(Request req, Response res, User user) {
        ProjectPropertiesRequest updateProjectPropertiesRequest = ProjectHandler.getProjectPropertiesRequest(req);

        HashMap<String, String> requestErrors = ProjectHandler.validateProjectPropertiesRequest(updateProjectPropertiesRequest);
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

        List<ContentEnvironment> contentEnvironments = ContentManager.listProjectEnvironments(
                updatedProject.getLabel(), user
        );

        return json(GSON, res, ResultJson.success(
                ResponseMappers.mapProjectFromDB(updatedProject, contentEnvironments))
        );
    }

}
