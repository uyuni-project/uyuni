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
package com.suse.manager.webui.controllers.contentmanagement.handlers;

import static com.suse.manager.webui.utils.SparkApplicationHelper.json;
import static com.suse.manager.webui.utils.SparkApplicationHelper.withUser;
import static spark.Spark.delete;
import static spark.Spark.post;
import static spark.Spark.put;

import com.redhat.rhn.domain.contentmgmt.ContentEnvironment;
import com.redhat.rhn.domain.contentmgmt.ContentManager;
import com.redhat.rhn.domain.contentmgmt.ContentProject;
import com.redhat.rhn.domain.user.User;

import com.suse.manager.webui.controllers.contentmanagement.mappers.ResponseMappers;
import com.suse.manager.webui.controllers.contentmanagement.request.EnvironmentRequest;
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
public class EnvironmentApiController {

    private static final Gson GSON = Json.GSON;

    private EnvironmentApiController() {
    }

    public static void initRoutes() {
        post("/manager/contentmanagement/api/projects/:projectId/environments",
                withUser(EnvironmentApiController::createContentEnvironemnt));

        put("/manager/contentmanagement/api/projects/:projectId/environments",
                withUser(EnvironmentApiController::updateContentEnvironemnt));

        delete("/manager/contentmanagement/api/projects/:projectId/environments",
                withUser(EnvironmentApiController::removeContentEnvironemnt));
    }

    public static String createContentEnvironemnt(Request req, Response res, User user) {
        EnvironmentRequest createEnvironmentRequest = EnvironmentHandler.getEnvironmentRequest(req);

        HashMap<String, String> requestErrors = EnvironmentHandler.validateEnvironmentRequest(createEnvironmentRequest);
        if (!requestErrors.isEmpty()) {
            return json(GSON, res, HttpStatus.SC_BAD_REQUEST, ResultJson.error(Arrays.asList(""), requestErrors));
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
        EnvironmentRequest updateEnvironmentRequest = EnvironmentHandler.getEnvironmentRequest(req);

        HashMap<String, String> requestErrors = EnvironmentHandler.validateEnvironmentRequest(updateEnvironmentRequest);
        if (!requestErrors.isEmpty()) {
            return json(GSON, res, HttpStatus.SC_BAD_REQUEST, ResultJson.error(Arrays.asList(""), requestErrors));
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

        Optional<ContentProject> updatedProject = ContentManager.lookupProject(
                removeEnvironmentRequest.getProjectLabel(),
                user
        );


        return updatedProject.map(p -> {
            List<ContentEnvironment> contentEnvironments = ContentManager.listProjectEnvironments(
                    p.getLabel(),
                    user
            );
            return json(GSON, res, ResultJson.success(ResponseMappers.mapProjectFromDB(p, contentEnvironments)));
        }).orElseGet(() -> json(GSON, res, ResultJson.error()));
    }

}
