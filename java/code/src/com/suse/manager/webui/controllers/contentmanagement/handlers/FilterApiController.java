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
import static spark.Spark.delete;
import static spark.Spark.get;
import static spark.Spark.post;
import static spark.Spark.put;

import com.redhat.rhn.common.localization.LocalizationService;
import com.redhat.rhn.common.validator.ValidatorException;
import com.redhat.rhn.domain.channel.Channel;
import com.redhat.rhn.domain.contentmgmt.ContentFilter;
import com.redhat.rhn.domain.contentmgmt.ContentManagementException;
import com.redhat.rhn.domain.contentmgmt.ContentProject;
import com.redhat.rhn.domain.contentmgmt.FilterCriteria;
import com.redhat.rhn.domain.contentmgmt.modulemd.ModulemdApiException;
import com.redhat.rhn.domain.rhnpackage.PackageEvr;
import com.redhat.rhn.domain.rhnpackage.PackageEvrFactory;
import com.redhat.rhn.domain.user.User;
import com.redhat.rhn.manager.EntityExistsException;
import com.redhat.rhn.manager.channel.ChannelManager;
import com.redhat.rhn.manager.contentmgmt.ContentManager;
import com.redhat.rhn.manager.contentmgmt.FilterTemplateManager;

import com.suse.manager.webui.controllers.contentmanagement.request.FilterRequest;
import com.suse.manager.webui.controllers.contentmanagement.request.ProjectFiltersUpdateRequest;
import com.suse.manager.webui.utils.FlashScopeHelper;
import com.suse.manager.webui.utils.SparkApplicationHelper;
import com.suse.manager.webui.utils.gson.ResultJson;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;

import org.apache.commons.lang3.StringUtils;
import org.apache.http.HttpStatus;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.util.Arrays;
import java.util.Collections;
import java.util.LinkedList;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

import spark.Request;
import spark.Response;

/**
 * Spark controller ContentManagement Filter Api.
 */
public class FilterApiController {

    private static final Gson GSON = ControllerApiUtils.GSON;
    private static final ContentManager CONTENT_MGR = ControllerApiUtils.CONTENT_MGR;
    private static final FilterTemplateManager TEMPLATE_MGR = ControllerApiUtils.TEMPLATE_MGR;
    private static final LocalizationService LOC = LocalizationService.getInstance();
    private static final Logger LOG = LogManager.getLogger(FilterApiController.class);

    private FilterApiController() {
    }

    /** Init routes for ContentManagement Filter Api.*/
    public static void initRoutes() {

        put("/manager/api/contentmanagement/projects/:projectId/filters",
                withUser(FilterApiController::updateFiltersOfProject));

        get("/manager/api/contentmanagement/filters",
                withUser(FilterApiController::getContentFilters));

        post("/manager/api/contentmanagement/filters",
                withUser(FilterApiController::createContentFilter));

        put("/manager/api/contentmanagement/filters/:filterId",
                withUser(FilterApiController::updateContentFilter));

        delete("/manager/api/contentmanagement/filters/:filterId",
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
                .map(pf -> pf.getFilter().getId())
                .filter(filterId -> !filtersIdToUpdate.contains(filterId))
                .collect(Collectors.toList());
        filterIdsToDetach.forEach(filterId -> CONTENT_MGR.detachFilter(
                projectLabel,
                filterId,
                user
        ));

        List<Long> filterIdsToAttach = filtersIdToUpdate
                .stream()
                .filter(filterId ->
                        dbContentProject.getProjectFilters()
                                .stream()
                                .noneMatch(pf -> pf.getFilter().getId().equals(filterId))
                )
                .collect(Collectors.toList());
        filterIdsToAttach
                .forEach(filterId ->
                        CONTENT_MGR.attachFilter(
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

    private static String createFromTemplate(Request req, Response res, User user) {
        FilterRequest createFilterRequest = FilterHandler.getFilterRequest(req);

        String prefix = createFilterRequest.getPrefix();
        if (!StringUtils.endsWithAny(prefix, "-", "_")) {
            prefix += "-";
        }

        List<ContentFilter> createdFilters;
        try {
            switch (createFilterRequest.getTemplate()) {
            case "LivePatchingSystem":
            case "LivePatchingProduct":
                PackageEvr kernelEvr = PackageEvrFactory.lookupPackageEvrById(createFilterRequest.getKernelEvrId());
                createdFilters = TEMPLATE_MGR.createLivePatchFilters(prefix, kernelEvr, user);
                break;
            case "AppStreamsWithDefaults":
                Channel channel = ChannelManager.lookupByIdAndUser(createFilterRequest.getChannelId(), user);
                try {
                    createdFilters = TEMPLATE_MGR.createAppStreamFilters(prefix, channel, user);
                }
                catch (ModulemdApiException e) {
                    LOG.error(e.getMessage(), e);
                    return SparkApplicationHelper.json(GSON, res,
                            ResultJson.error(LOC.getMessage("contentmanagement.modules_error")),
                            new TypeToken<>() { });
                }
                break;
            default:
                return json(GSON, res, HttpStatus.SC_BAD_REQUEST, ResultJson.error(Collections.emptyList(),
                        Collections.singletonMap("invalid_template",
                                Collections.singletonList(LOC.getMessage("contentmanagement.invalid_template")))),
                        new TypeToken<>() { });
            }
        }
        catch (EntityExistsException error) {
            return json(GSON, res, HttpStatus.SC_BAD_REQUEST, ResultJson.error(Collections.emptyList(),
                    Collections.singletonMap("filter_name",
                            Collections.singletonList(LOC.getMessage("contentmanagement.filter_exists")))
            ), new TypeToken<>() { });
        }

        if (!StringUtils.isEmpty(createFilterRequest.getProjectLabel())) {
            for (ContentFilter createdFilter : createdFilters) {
                CONTENT_MGR.attachFilter(
                        createFilterRequest.getProjectLabel(),
                        createdFilter.getId(),
                        user
                );
            }
            FlashScopeHelper.flash(req, LOC.getMessage("contentmanagement.filter_created_template",
                    createdFilters.size()));
        }

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

        if (StringUtils.isNotEmpty(createFilterRequest.getTemplate())) {
            return createFromTemplate(req, res, user);
        }

        FilterCriteria filterCriteria = new FilterCriteria(
                FilterCriteria.Matcher.lookupByLabel(createFilterRequest.getMatcher()),
                createFilterRequest.getCriteriaKey(),
                StringUtils.trimToNull(createFilterRequest.getCriteriaValue()));


        ContentFilter createdFilter;
        try {
            createdFilter = CONTENT_MGR.createFilter(
                    createFilterRequest.getName(),
                    ContentFilter.Rule.lookupByLabel(createFilterRequest.getRule()),
                    ContentFilter.EntityType.lookupByLabel(createFilterRequest.getEntityType()),
                    filterCriteria,
                    user
            );
        }
        catch (EntityExistsException error) {
            return json(GSON, res, HttpStatus.SC_BAD_REQUEST, ResultJson.error(
                    new LinkedList<>(),
                    Collections.singletonMap("filter_name",
                            Arrays.asList(LOC.getMessage("contentmanagement.filter_exists")))
            ), new TypeToken<>() { });
        }
        catch (ValidatorException e) {
            return json(GSON, res, HttpStatus.SC_BAD_REQUEST,
                    ResultJson.error(ValidationUtils.convertValidationErrors(e),
                            ValidationUtils.convertFieldValidationErrors(e)));
        }

        if (!StringUtils.isEmpty(createFilterRequest.getProjectLabel())) {
            CONTENT_MGR.attachFilter(
                    createFilterRequest.getProjectLabel(),
                    createdFilter.getId(),
                    user
            );
            FlashScopeHelper.flash(req, LOC.getMessage("contentmanagement.filter_created", createdFilter.getName()));
        }

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

        FilterCriteria filterCriteria = new FilterCriteria(
                FilterCriteria.Matcher.lookupByLabel(updateFilterRequest.getMatcher()),
                updateFilterRequest.getCriteriaKey(),
                StringUtils.trimToNull(updateFilterRequest.getCriteriaValue()));
        try {
            CONTENT_MGR.updateFilter(
                    Long.parseLong(req.params("filterId")),
                    Optional.ofNullable(updateFilterRequest.getName()),
                    Optional.ofNullable(updateFilterRequest.getRule()).map(ContentFilter.Rule::lookupByLabel),
                    Optional.of(filterCriteria),
                    user
            );
        }
        catch (ValidatorException e) {
            return json(GSON, res, HttpStatus.SC_BAD_REQUEST,
                    ResultJson.error(ValidationUtils.convertValidationErrors(e),
                            ValidationUtils.convertFieldValidationErrors(e)));
        }

        if (!StringUtils.isEmpty(updateFilterRequest.getProjectLabel())) {
            FlashScopeHelper.flash(
                    req, LOC.getMessage("contentmanagement.filter_updated", updateFilterRequest.getName())
            );
        }

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
            CONTENT_MGR.removeFilter(Long.parseLong(req.params("filterId")), user);
        }
        catch (ContentManagementException error) {
            return json(GSON, res, HttpStatus.SC_BAD_REQUEST, ResultJson.error(error.getMessage()));
        }

        return ControllerApiUtils.listFiltersJsonResponse(res, user);
    }

}
