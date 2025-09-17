/*
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

import static com.suse.manager.webui.utils.SparkApplicationHelper.withCsrfToken;
import static com.suse.manager.webui.utils.SparkApplicationHelper.withRolesTemplate;
import static com.suse.manager.webui.utils.SparkApplicationHelper.withUserPreferences;
import static spark.Spark.get;

import com.redhat.rhn.domain.channel.Channel;
import com.redhat.rhn.domain.contentmgmt.ContentEnvironment;
import com.redhat.rhn.domain.contentmgmt.ContentFilter;
import com.redhat.rhn.domain.contentmgmt.ContentProject;
import com.redhat.rhn.domain.contentmgmt.ContentProjectFactory;
import com.redhat.rhn.domain.contentmgmt.SoftwareProjectSource;
import com.redhat.rhn.domain.user.User;
import com.redhat.rhn.manager.contentmgmt.ContentManager;

import com.suse.manager.webui.controllers.contentmanagement.handlers.ControllerApiUtils;
import com.suse.manager.webui.controllers.contentmanagement.mappers.ResponseMappers;
import com.suse.manager.webui.controllers.contentmanagement.response.FilterResponse;
import com.suse.manager.webui.utils.FlashScopeHelper;

import com.google.gson.Gson;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;

import spark.ModelAndView;
import spark.Request;
import spark.Response;
import spark.template.jade.JadeTemplateEngine;

/**
 * Spark controller class for content management pages.
 */
public class ContentManagementViewsController {

    private static final Gson GSON = ControllerApiUtils.GSON;

    private ContentManagementViewsController() {
    }


    /**
     * @param jade JadeTemplateEngine
     * Invoked from Router. Init routes for ContentManagement Views.
     */
    public static void initRoutes(JadeTemplateEngine jade) {
        // Projects
        get("/manager/contentmanagement/project",
                withCsrfToken(withRolesTemplate(ContentManagementViewsController::createProjectView)), jade);
        get("/manager/contentmanagement/project/:label",
                withCsrfToken(withRolesTemplate(ContentManagementViewsController::editProjectView)), jade);
        get("/manager/contentmanagement/projects",
                withUserPreferences(withRolesTemplate(ContentManagementViewsController::listProjectsView)), jade);

        get("/manager/contentmanagement/filters",
                withUserPreferences(withCsrfToken(withRolesTemplate(
                        ContentManagementViewsController::listFiltersView))), jade);
    }

    /**
     * Handler for the server create project page.
     *
     * @param req the request object
     * @param res the response object
     * @param user the current user
     * @return the ModelAndView object to render the page
     */
    public static ModelAndView createProjectView(Request req, Response res, User user) {
        Map<String, Object> data = new HashMap<>();
        return new ModelAndView(data, "controllers/contentmanagement/templates/create-project.jade");
    }

    /**
     * Handler for the server edit project page.
     *
     * @param req the request object
     * @param res the response object
     * @param user the current user
     * @return the ModelAndView object to render the page
     */
    public static ModelAndView editProjectView(Request req, Response res, User user) {

        String projectToEditLabel = req.params("label");

        Optional<ContentProject> projectToEdit = ContentManager.lookupProject(projectToEditLabel, user);

        Map<String, Object> data = new HashMap<>();
        projectToEdit.ifPresent(project -> {
            List<ContentEnvironment> contentEnvironments = ContentManager.listProjectEnvironments(
                    project.getLabel(), user
            );

            Set<SoftwareProjectSource> sourcesWithUnsyncedPatches =
                    ContentManager.listActiveSwSourcesWithUnsyncedPatches(user, project);
            Map<Long, Long> sourceTagetChannelIds = new HashMap<>();
            if (!contentEnvironments.isEmpty() && !sourcesWithUnsyncedPatches.isEmpty()) {
                ContentEnvironment first = contentEnvironments.get(0);
                sourcesWithUnsyncedPatches.forEach(swsource -> {
                    Channel sChan = swsource.getChannel();
                    Optional<Long> tgtChanId = ContentManager.lookupTargetByChannel(sChan, first, user)
                            .map(target -> target.getChannel().getId());
                    sourceTagetChannelIds.put(sChan.getId(), tgtChanId.orElse(null));
                });
            }

            data.put("projectToEdit", GSON.toJson(ResponseMappers.mapProjectFromDB(project, contentEnvironments,
                    sourcesWithUnsyncedPatches, sourceTagetChannelIds)));
        });
        if (!projectToEdit.isEmpty()) {
            data.put("wasFreshlyCreatedMessage", FlashScopeHelper.flash(req));
        }
        else {
            data.put("projectToEdit", GSON.toJson(null));
        }

        return new ModelAndView(data, "controllers/contentmanagement/templates/project.jade");
    }

    /**
     * Handler for the server listing projects page.
     *
     * @param req the request object
     * @param res the response object
     * @param user the current user
     * @return the ModelAndView object to render the page
     */
    public static ModelAndView listProjectsView(Request req, Response res, User user) {
        Map<String, Object> data = new HashMap<>();

        List<ContentProject> projects = ContentManager.listProjects(user);

        Map<ContentProject, List<ContentEnvironment>> environmentsByProject = projects.stream()
                .collect(Collectors.toMap(p -> p, p -> ContentManager.listProjectEnvironments(p.getLabel(), user)));

        data.put("flashMessage", FlashScopeHelper.flash(req));
        data.put("contentProjects", GSON.toJson(ResponseMappers.mapProjectListingFromDB(environmentsByProject)));

        return new ModelAndView(data, "controllers/contentmanagement/templates/list-projects.jade");
    }

    /**
     *
     * @param req the request object
     * @param res the response object
     * @param user the current user
     * @return the ModelAndView object to render the page
     */
    public static ModelAndView listFiltersView(Request req, Response res, User user) {
        Map<String, Object> data = new HashMap<>();

        Map<ContentFilter, List<ContentProject>> filtersWithProjects = ContentManager.listFilters(user)
                .stream()
                .collect(Collectors.toMap(p -> p, ContentProjectFactory::listFilterProjects));


        List<FilterResponse> filterResponse = ResponseMappers.mapFilterListingFromDB(filtersWithProjects);

        data.put("flashMessage", FlashScopeHelper.flash(req));
        data.put("contentFilters", GSON.toJson(filterResponse));

        return new ModelAndView(data, "controllers/contentmanagement/templates/list-filters.jade");
    }

}
