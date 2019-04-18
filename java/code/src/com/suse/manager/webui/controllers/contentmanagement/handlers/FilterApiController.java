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
import static spark.Spark.get;
import static spark.Spark.post;
import static spark.Spark.put;

import com.redhat.rhn.domain.contentmgmt.ContentFilter;
import com.redhat.rhn.domain.contentmgmt.ContentManagementException;
import com.redhat.rhn.domain.contentmgmt.ContentProject;
import com.redhat.rhn.domain.contentmgmt.FilterCriteria;
import com.redhat.rhn.domain.user.User;
import com.redhat.rhn.manager.contentmgmt.ContentManager;

import com.suse.manager.webui.controllers.contentmanagement.request.FilterRequest;
import com.suse.manager.webui.controllers.contentmanagement.request.ProjectFiltersUpdateRequest;
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
 * Spark controller ContentManagement Filter Api.
 */
public class FilterApiController {

    private static final Gson GSON = Json.GSON;

    private FilterApiController() {
    }

    /** Init routes for ContentManagement Filter Api.*/
    public static void initRoutes() {

        put("/manager/contentmanagement/api/projects/:projectId/filters",
                withUser(FilterApiController::updateFiltersOfProject));

        get("/manager/contentmanagement/api/filters",
                withUser(FilterApiController::getContentFilters));

        post("/manager/contentmanagement/api/filters",
                withUser(FilterApiController::createContentFilter));

        put("/manager/contentmanagement/api/filters/:filterId",
                withUser(FilterApiController::updateContentFilter));

        delete("/manager/contentmanagement/api/filters/:filterId",
                withUser(FilterApiController::removeContentFilter));
    }

    /**
     * Return the project JSON with the result of updating the filters of a project.
     * @param req the http request
     * @param res the http response
     * @param user the current user
     * @return the JSON data
     */
    public static String updateFiltersOfProject(Request req, Response res, User user) {
        ProjectFiltersUpdateRequest projectFiltersUpdateRequest = FilterHandler.getProjectFiltersRequest(req);
        String projectLabel = projectFiltersUpdateRequest.getProjectLabel();
        List<Long> filtersIdToUpdate =  projectFiltersUpdateRequest.getFiltersIds();

        ContentProject dbContentProject = ContentManager.lookupProject(
                projectFiltersUpdateRequest.getProjectLabel(), user
        ).get();


        List<Long> filterIdsToDetach = dbContentProject.getProjectFilters()
                .stream()
                .map(filter -> filter.getFilter().getId())
                .filter(filterId -> !filtersIdToUpdate.contains(filterId))
                .collect(Collectors.toList());
        filterIdsToDetach.forEach(filterId -> ContentManager.detachFilter(
                projectLabel,
                filterId,
                user
        ));

        List<Long> filterIdsToAttach = filtersIdToUpdate
                .stream()
                .filter(filterId ->
                        !dbContentProject.getProjectFilters()
                                .stream()
                                .filter(filter -> filter.getId() == filterId)
                                .findFirst()
                                .isPresent()
                )
                .collect(Collectors.toList());
        filterIdsToAttach
                .forEach(filterId ->
                        ContentManager.attachFilter(
                                projectLabel,
                                filterId,
                                user
                        ));

        return ControllerApiUtils.fullProjectJsonResponse(res, projectLabel, user);
    }

    /**
     * Return the JSON with all the available filters.
     * @param req the http request
     * @param res the http response
     * @param user the current user
     * @return the JSON data
     */
    public static String getContentFilters(Request req, Response res, User user) {
        return ControllerApiUtils.listFiltersJsonResponse(res, user);
    }

    /**
     * Return the JSON with the result of the creation of a filter.
     * @param req the http request
     * @param res the http response
     * @param user the current user
     * @return the JSON data
     */
    public static String createContentFilter(Request req, Response res, User user) {
        FilterRequest createFilterRequest = FilterHandler.getFilterRequest(req);

        HashMap<String, String> requestErrors = FilterHandler.validateFilterRequest(createFilterRequest);
        if (!requestErrors.isEmpty()) {
            return json(GSON, res, HttpStatus.SC_BAD_REQUEST, ResultJson.error(Arrays.asList(""), requestErrors));
        }

        FilterCriteria filterCriteria = new FilterCriteria(
                FilterCriteria.Matcher.CONTAINS,
                "name",
                createFilterRequest.getCriteria());
        ContentManager.createFilter(
                createFilterRequest.getName(),
                createFilterRequest.getDeny() ? ContentFilter.Rule.DENY : ContentFilter.Rule.ALLOW,
                ContentFilter.EntityType.lookupByLabel(createFilterRequest.getType()),
                filterCriteria,
                user
        );

        return ControllerApiUtils.listFiltersJsonResponse(res, user);
    }

    /**
     * Return the JSON with the result of updating a content project environemnt.
     * @param req the http request
     * @param res the http response
     * @param user the current user
     * @return the JSON data
     */
    public static String updateContentFilter(Request req, Response res, User user) {
        FilterRequest updateFilterRequest = FilterHandler.getFilterRequest(req);

        HashMap<String, String> requestErrors = FilterHandler.validateFilterRequest(updateFilterRequest);
        if (!requestErrors.isEmpty()) {
            return json(GSON, res, HttpStatus.SC_BAD_REQUEST, ResultJson.error(Arrays.asList(""), requestErrors));
        }

        FilterCriteria filterCriteria = new FilterCriteria(
                FilterCriteria.Matcher.CONTAINS,
                "name",
                updateFilterRequest.getCriteria());
        ContentManager.updateFilter(
                Long.parseLong(req.params("filterId")),
                Optional.ofNullable(updateFilterRequest.getName()),
                Optional.of(updateFilterRequest.getDeny() ? ContentFilter.Rule.DENY : ContentFilter.Rule.ALLOW),
                Optional.of(filterCriteria),
                user
        );

        return ControllerApiUtils.listFiltersJsonResponse(res, user);
    }

    /**
     * Return the JSON with the result of removing a content project environemnt.
     * @param req the http request
     * @param res the http response
     * @param user the current user
     * @return the JSON data
     */
    public static String removeContentFilter(Request req, Response res, User user) {
        try {
            ContentManager.removeFilter(Long.parseLong(req.params("filterId")), user);
        }
        catch (ContentManagementException error) {
            return json(GSON, res, HttpStatus.SC_BAD_REQUEST, ResultJson.error(error.getMessage()));
        }

        return ControllerApiUtils.listFiltersJsonResponse(res, user);
    }

}
