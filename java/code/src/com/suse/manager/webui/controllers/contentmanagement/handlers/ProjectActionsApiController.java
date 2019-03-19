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
import static spark.Spark.put;

import com.redhat.rhn.domain.contentmgmt.ContentEnvironment;
import com.redhat.rhn.domain.contentmgmt.ContentProject;
import com.redhat.rhn.domain.contentmgmt.ProjectSource;
import com.redhat.rhn.domain.contentmgmt.SoftwareProjectSource;
import com.redhat.rhn.domain.user.User;
import com.redhat.rhn.manager.contentmgmt.ContentManager;

import com.suse.manager.webui.controllers.contentmanagement.mappers.ResponseMappers;
import com.suse.manager.webui.controllers.contentmanagement.request.ProjectLabelRequest;
import com.suse.manager.webui.controllers.contentmanagement.request.ProjectSourcesRequest;
import com.suse.manager.webui.utils.gson.ResultJson;
import com.suse.utils.Json;

import com.google.gson.Gson;

import org.apache.http.HttpStatus;

import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

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
        put("/manager/contentmanagement/api/projects/:projectId/build",
                withUser(ProjectActionsApiController::buildProject));
    }

    /**
     * Return the JSON with the result of updating the content project with a list of sources.
     * @param req the http request
     * @param res the http response
     * @param user the current user
     * @return the JSON data
     */
    public static String buildProject(Request req, Response res, User user) {
        ProjectLabelRequest projectLabelRequest = ProjectActionsHandler.getProjectLabelRequest(req);

        HashMap<String, String> requestErrors = ProjectActionsHandler.validateProjectLabelRequest(projectLabelRequest);
        if (!requestErrors.isEmpty()) {
            return json(GSON, res, HttpStatus.SC_BAD_REQUEST, ResultJson.error(Arrays.asList(""), requestErrors));
        }

        String projectLabel = projectLabelRequest.getProjectLabel();
        ContentProject dbContentProject = ContentManager.lookupProject(projectLabel, user).get();

//        ContentManager.

        // [LN] Todo centralize this logic for all api
        List<ContentEnvironment> dbContentEnvironments = ContentManager.listProjectEnvironments(projectLabel, user);

        return json(GSON, res, ResultJson.success(
                ResponseMappers.mapProjectFromDB(dbContentProject, dbContentEnvironments)
        ));
    }

}
