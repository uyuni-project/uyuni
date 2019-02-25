/**
 * Copyright (c) 2018 SUSE LLC
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
package com.suse.manager.webui.controllers.contentmanagement;

import static com.suse.manager.webui.utils.SparkApplicationHelper.json;
import static com.suse.manager.webui.utils.SparkApplicationHelper.withUser;
import static spark.Spark.post;
import static spark.Spark.put;
import static spark.Spark.delete;

import com.redhat.rhn.domain.contentmgmt.ContentEnvironment;
import com.redhat.rhn.domain.contentmgmt.ContentManager;
import com.redhat.rhn.domain.contentmgmt.ContentProject;
import com.redhat.rhn.domain.contentmgmt.ContentProjectFactory;
import com.redhat.rhn.domain.user.User;

import com.suse.manager.webui.controllers.contentmanagement.mappers.ResponseMappers;
import com.suse.manager.webui.controllers.contentmanagement.request.EnvironmentRequest;
import com.suse.manager.webui.controllers.contentmanagement.request.ProjectPropertiesRequest;
import com.suse.manager.webui.controllers.contentmanagement.request.ProjectRequest;
import com.suse.manager.webui.utils.FlashScopeHelper;
import com.suse.manager.webui.utils.gson.ResultJson;
import com.suse.utils.Json;

import com.google.gson.Gson;
import com.google.gson.JsonParseException;

import org.apache.http.HttpStatus;
import org.apache.log4j.Logger;

import java.util.List;
import java.util.Optional;

import spark.Request;
import spark.Response;
import spark.Spark;

/**
 * Spark controller class for content management pages and API endpoints.
 */
public class ContentManagementApiController {

    private static Logger log = Logger.getLogger(ContentManagementApiController.class);
    private static final Gson GSON = Json.GSON;

    private ContentManagementApiController() {
    }

    public static void initRoutes() {
        post("/manager/contentmanagement/api/projects",
                withUser(ContentManagementApiController::createContentProject));

        delete("/manager/contentmanagement/api/projects/:projectId",
                withUser(ContentManagementApiController::deleteContentProject));

        put("/manager/contentmanagement/api/projects/:projectId/properties",
                withUser(ContentManagementApiController::updateContentProjectProperties));

        post("/manager/contentmanagement/api/projects/:projectId/environments",
                withUser(ContentManagementApiController::createContentEnvironemnt));

        put("/manager/contentmanagement/api/projects/:projectId/environments",
                withUser(ContentManagementApiController::updateContentEnvironemnt));

        delete("/manager/contentmanagement/api/projects/:projectId/environments",
                withUser(ContentManagementApiController::removeContentEnvironemnt));
    }

    public static String createContentProject(Request req, Response res, User user) {
        ProjectRequest createProjectRequest;
        try {
            createProjectRequest = GSON.fromJson(req.body(), ProjectRequest.class);
        }
        catch (JsonParseException e) {
            throw Spark.halt(HttpStatus.SC_BAD_REQUEST);
        }

        ProjectPropertiesRequest projectPropertiesRequest = createProjectRequest.getProperties();

        ContentProject createdProject = ContentManager.createProject(
                projectPropertiesRequest.getLabel(),
                projectPropertiesRequest.getName(),
                projectPropertiesRequest.getDescription(),
                user
        );

        // TODO: better  handling error if createProject fails

        FlashScopeHelper.flash(
                req,
                String.format("Project %s created successfully.", projectPropertiesRequest.getLabel())
        );

        List<ContentEnvironment> contentEnvironments = ContentManager.listProjectEnvironments(createdProject.getLabel(), user);

        return json(GSON, res, ResultJson.success(ResponseMappers.mapProjectFromDB(createdProject, contentEnvironments)));
    }

    public static String deleteContentProject(Request req, Response res, User user) {
        String projectLabel = req.params("projectId");

        int removingResult = ContentManager.removeProject(
                projectLabel,
                user
        );

        if(removingResult == 1) {
            String successMessage = String.format("Project %s deleted successfully.", projectLabel);
            log.error(successMessage);
            FlashScopeHelper.flash(
                    req,
                    successMessage
            );
            return json(GSON, res, ResultJson.successMessage(successMessage));
        }

        return json(GSON, res, ResultJson.error());
    }

    public static String updateContentProjectProperties(Request req, Response res, User user) {
        ProjectPropertiesRequest updateProjectPropertiesRequest;
        try {
            updateProjectPropertiesRequest = GSON.fromJson(req.body(), ProjectPropertiesRequest.class);
        }
        catch (JsonParseException e) {
            throw Spark.halt(HttpStatus.SC_BAD_REQUEST);
        }

        ContentProject updatedProject = ContentManager.updateProject(
                updateProjectPropertiesRequest.getLabel(),
                Optional.ofNullable(updateProjectPropertiesRequest.getName()),
                Optional.ofNullable(updateProjectPropertiesRequest.getDescription()),
                user
        );

        // UPDATE  PROPERTIES WITH EMPTY NAME ? THERE IS SOME NASTY BUG

        // TODO: update sources

        List<ContentEnvironment> contentEnvironments = ContentManager.listProjectEnvironments(updatedProject.getLabel(), user);

        return json(GSON, res, ResultJson.success(ResponseMappers.mapProjectFromDB(updatedProject, contentEnvironments)));
    }

    public static String createContentEnvironemnt(Request req, Response res, User user) {
        EnvironmentRequest createEnvironmentRequest;
        try {
            createEnvironmentRequest = GSON.fromJson(req.body(), EnvironmentRequest.class);
        }
        catch (JsonParseException e) {
            throw Spark.halt(HttpStatus.SC_BAD_REQUEST);
        }

        ContentEnvironment createdEnvironment = ContentManager.createEnvironment(
                createEnvironmentRequest.getProjectLabel(),
                Optional.ofNullable(createEnvironmentRequest.getPredecessorLabel()),
                createEnvironmentRequest.getLabel(),
                createEnvironmentRequest.getName(),
                createEnvironmentRequest.getDescription(),
                user
        );
        ContentProject contentProject = createdEnvironment.getContentProject();
        List<ContentEnvironment> contentEnvironments = ContentManager.listProjectEnvironments(contentProject.getLabel(), user);

        return json(GSON, res, ResultJson.success(ResponseMappers.mapProjectFromDB(contentProject, contentEnvironments)));
    }

    public static String updateContentEnvironemnt(Request req, Response res, User user) {
        EnvironmentRequest updateEnvironmentRequest;
        try {
            updateEnvironmentRequest = GSON.fromJson(req.body(), EnvironmentRequest.class);
        }
        catch (JsonParseException e) {
            throw Spark.halt(HttpStatus.SC_BAD_REQUEST);
        }

        ContentEnvironment updatedEnvironment = ContentManager.updateEnvironment(
                updateEnvironmentRequest.getLabel(),
                updateEnvironmentRequest.getProjectLabel(),
                Optional.ofNullable(updateEnvironmentRequest.getName()),
                Optional.ofNullable(updateEnvironmentRequest.getDescription()),
                user
        );

        ContentProject contentProject = updatedEnvironment.getContentProject();
        List<ContentEnvironment> contentEnvironments = ContentManager.listProjectEnvironments(contentProject.getLabel(), user);

        return json(GSON, res, ResultJson.success(ResponseMappers.mapProjectFromDB(contentProject, contentEnvironments)));
    }

    public static String removeContentEnvironemnt(Request req, Response res, User user) {
        EnvironmentRequest removeEnvironmentRequest;
        try {
            removeEnvironmentRequest = GSON.fromJson(req.body(), EnvironmentRequest.class);
        }
        catch (JsonParseException e) {
            throw Spark.halt(HttpStatus.SC_BAD_REQUEST);
        }

        ContentManager.removeEnvironment(
                removeEnvironmentRequest.getLabel(),
                removeEnvironmentRequest.getProjectLabel(),
                user
        );

        Optional<ContentProject> updatedProject = ContentManager.lookupProject(
                removeEnvironmentRequest.getProjectLabel(),
                user
        );

        if(updatedProject.isPresent()) {
            List<ContentEnvironment> contentEnvironments = ContentManager.listProjectEnvironments(
                    updatedProject.get().getLabel(),
                    user
            );

            return json(GSON, res, ResultJson.success(ResponseMappers.mapProjectFromDB(
                    updatedProject.get(),
                    contentEnvironments))
            );
        }

        return json(GSON, res, ResultJson.error());
    }

}
