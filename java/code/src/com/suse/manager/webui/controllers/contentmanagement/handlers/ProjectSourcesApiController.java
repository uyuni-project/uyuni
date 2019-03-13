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
import com.redhat.rhn.domain.user.User;
import com.redhat.rhn.manager.contentmgmt.ContentManager;

import com.suse.manager.webui.controllers.contentmanagement.mappers.ResponseMappers;
import com.suse.manager.webui.controllers.contentmanagement.request.ProjectSourcesRequest;
import com.suse.manager.webui.utils.gson.ResultJson;
import com.suse.utils.Json;

import com.google.gson.Gson;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

import spark.Request;
import spark.Response;

/**
 * Spark controller class for content management sources API endpoints.
 */
public class ProjectSourcesApiController {

    private static final Gson GSON = Json.GSON;

    private ProjectSourcesApiController() {
    }

    /** Init routes for ContentManagement Sources Api.*/
    public static void initRoutes() {
        put("/manager/contentmanagement/api/projects/:projectId/sources",
                withUser(ProjectSourcesApiController::updateContentSources));

    }

    /**
     * Return the JSON with the result of updating the content project with a list of sources.
     * @param req the http request
     * @param res the http response
     * @param user the current user
     * @return the JSON data
     */
    public static String updateContentSources(Request req, Response res, User user) {
        ProjectSourcesRequest createSourceRequest = ProjectSourcesHandler.getSourcesRequest(req);
        String projectLabel = createSourceRequest.getProjectLabel();

        ContentProject dbContentProject = ContentManager.lookupProject(
                createSourceRequest.getProjectLabel(), user
        ).get();

        List<String> sourceLabelsToDetach = dbContentProject.getSources().stream()
                .map(source -> source.sourceLabel())
                .collect(Collectors.toList());
        sourceLabelsToDetach.forEach(sourceLabel -> ContentManager.detachSource(
                projectLabel,
                ProjectSource.Type.SW_CHANNEL,
                sourceLabel,
                user
        ));

        List<String> sourceLabelsToAttach = createSourceRequest.getSoftwareSources().stream()
                .map(source -> source.getLabel())
                .collect(Collectors.toList());
        int sourcePosition = 0;
        for (String sourceLabel: sourceLabelsToAttach) {
            ContentManager.attachSource(
                    projectLabel,
                    ProjectSource.Type.SW_CHANNEL,
                    sourceLabel,
                    Optional.of(sourcePosition),
                    user
            );
            sourcePosition++;
        }

        List<ContentEnvironment> dbContentEnvironments = ContentManager.listProjectEnvironments(projectLabel, user);

        return json(GSON, res, ResultJson.success(
                ResponseMappers.mapProjectFromDB(dbContentProject, dbContentEnvironments)
        ));
    }

}
