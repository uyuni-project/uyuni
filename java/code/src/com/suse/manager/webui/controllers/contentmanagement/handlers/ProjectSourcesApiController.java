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

import static com.suse.manager.webui.utils.SparkApplicationHelper.withUser;
import static com.suse.utils.Opt.stream;
import static spark.Spark.put;

import com.redhat.rhn.domain.contentmgmt.ContentProject;
import com.redhat.rhn.domain.contentmgmt.ProjectSource;
import com.redhat.rhn.domain.user.User;
import com.redhat.rhn.manager.contentmgmt.ContentManager;

import com.suse.manager.webui.controllers.contentmanagement.request.ProjectSourcesRequest;
import com.suse.utils.Json;

import com.google.gson.Gson;

import java.util.Collections;
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
        put("/manager/contentmanagement/api/projects/:projectId/softwaresources",
                withUser(ProjectSourcesApiController::updateContentSoftwareSources));
    }

    /**
     * Return the JSON with the result of updating the content project with a list of sources.
     * @param req the http request
     * @param res the http response
     * @param user the current user
     * @return the JSON data
     */
    public static String updateContentSoftwareSources(Request req, Response res, User user) {
        ProjectSourcesRequest createSourceRequest = ProjectSourcesHandler.getSourcesRequest(req);
        String projectLabel = createSourceRequest.getProjectLabel();

        ContentProject dbContentProject = ContentManager.lookupProject(
                createSourceRequest.getProjectLabel(), user
        ).get();

        List<String> sourceLabelsToDetach = dbContentProject.getSources().stream()
                .flatMap(source -> stream(source.asSoftwareSource()))
                .map(softwareSource -> softwareSource.getChannel().getLabel())
                .collect(Collectors.toList());
        sourceLabelsToDetach.forEach(sourceLabel -> ContentManager.detachSource(
                projectLabel,
                ProjectSource.Type.SW_CHANNEL,
                sourceLabel,
                user
        ));

        List<String> sourceLabelsToAttach = createSourceRequest.getSoftwareSources()
                .stream()
                .map(source -> source.getLabel())
                .collect(Collectors.toList());
        Collections.reverse(sourceLabelsToAttach);
        sourceLabelsToAttach.forEach(sourceLabel -> ContentManager.attachSource(
                projectLabel,
                ProjectSource.Type.SW_CHANNEL,
                sourceLabel,
                Optional.of(0),
                user
        ));

        return ControllerUtils.fullProjectJson(res, projectLabel, user);
    }

}
