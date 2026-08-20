/*
 * Copyright (c) 2026 SUSE LLC
 *
 * This software is licensed to you under the GNU General Public License,
 * version 2 (GPLv2). There is NO WARRANTY for this software, express or
 * implied, including the implied warranties of MERCHANTABILITY or FITNESS
 * FOR A PARTICULAR PURPOSE. You should have received a copy of GPLv2
 * along with this software; if not, see
 * http://www.gnu.org/licenses/old-licenses/gpl-2.0.txt.
 */
package com.redhat.rhn.frontend.xmlrpc.contentmgmt;

import com.redhat.rhn.domain.contentmgmt.ContentEnvironment;
import com.redhat.rhn.domain.contentmgmt.ContentEnvironmentDiff;
import com.redhat.rhn.domain.contentmgmt.ContentFilter;
import com.redhat.rhn.domain.contentmgmt.ContentProject;
import com.redhat.rhn.domain.contentmgmt.ContentProjectFilter;
import com.redhat.rhn.domain.contentmgmt.ProjectSource;
import com.redhat.rhn.domain.contentmgmt.modulemd.ModulemdApiException;
import com.redhat.rhn.domain.user.User;

import com.suse.manager.api.ApiResponseWrapper;
import com.suse.manager.api.docs.ApiEndpointDoc;
import com.suse.manager.api.docs.LegacyDocResponse;

import com.fasterxml.jackson.annotation.JsonPropertyOrder;

import java.util.Date;
import java.util.List;
import java.util.Map;

import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.enums.ParameterIn;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.tags.Tag;
import spark.route.HttpMethod;

/**
 * API contract for {@link ContentManagementHandler}.
 */
@Tag(name = "contentmanagement", description = "Provides methods to access and modify Content Lifecycle Management " +
        "related entities (Projects, Environments, Filters, Sources).")
public interface ContentManagementHandlerApi {

    /**
     * Lists the Content Projects of the user organization.
     *
     * @param loggedInUser the current user
     * @return the Content Projects
     */
    @ApiEndpointDoc(
        summary = "List all Content Projects visible to user",
        method = HttpMethod.get,
        responseClass = ContentProjectListResponse.class,
        legacyDocResponse = @LegacyDocResponse(name = "content project information")
    )
    List<ContentProject> listProjects(@Parameter(hidden = true) User loggedInUser);

    /**
     * Looks up a Content Project.
     *
     * @param loggedInUser the current user
     * @param projectLabel the Content Project label
     * @return the Content Project
     */
    @ApiEndpointDoc(
        summary = "Look up Content Project with given label",
        method = HttpMethod.get,
        responseClass = ContentProjectResponse.class,
        legacyDocResponse = @LegacyDocResponse(name = "content project information")
    )
    ContentProject lookupProject(
        @Parameter(hidden = true) User loggedInUser,
        @Parameter(name = "projectLabel", description = "Content Project label",
                in = ParameterIn.QUERY, required = true) String projectLabel);

    /**
     * Creates a Content Project.
     *
     * @param loggedInUser the current user
     * @param projectLabel the Content Project label
     * @param name the Content Project name
     * @param description the Content Project description
     * @return the created Content Project
     */
    @ApiEndpointDoc(
        summary = "Create Content Project",
        requestClass = CreateProjectRequest.class,
        responseClass = ContentProjectResponse.class,
        legacyDocResponse = @LegacyDocResponse(name = "content project information")
    )
    ContentProject createProject(User loggedInUser, String projectLabel, String name, String description);

    /**
     * Updates a Content Project.
     *
     * @param loggedInUser the current user
     * @param projectLabel the Content Project label
     * @param props the properties to update
     * @return the updated Content Project
     */
    @ApiEndpointDoc(
        summary = "Update Content Project with given label",
        requestClass = UpdateProjectRequest.class,
        responseClass = ContentProjectResponse.class,
        legacyDocResponse = @LegacyDocResponse(name = "content project information")
    )
    ContentProject updateProject(User loggedInUser, String projectLabel, Map<String, Object> props);

    /**
     * Removes a Content Project.
     *
     * @param loggedInUser the current user
     * @param projectLabel the Content Project label
     * @return 1 on success
     */
    @ApiEndpointDoc(
        summary = "Remove Content Project",
        requestClass = ProjectLabelRequest.class,
        isIntegerResponse = true
    )
    int removeProject(User loggedInUser, String projectLabel);

    /**
     * Lists the Environments of a Content Project.
     *
     * @param loggedInUser the current user
     * @param projectLabel the Content Project label
     * @return the Environments in their order
     */
    @ApiEndpointDoc(
        summary = "List Environments in a Content Project with the respect to their ordering",
        method = HttpMethod.get,
        responseClass = ContentEnvironmentListResponse.class,
        legacyDocResponse = @LegacyDocResponse(name = "content environment information")
    )
    List<ContentEnvironment> listProjectEnvironments(
        @Parameter(hidden = true) User loggedInUser,
        @Parameter(name = "projectLabel", description = "Content Project label",
                in = ParameterIn.QUERY, required = true) String projectLabel);

    /**
     * Looks up a Content Environment.
     *
     * @param loggedInUser the current user
     * @param projectLabel the Content Project label
     * @param envLabel the Content Environment label
     * @return the Content Environment
     */
    @ApiEndpointDoc(
        summary = "Look up Content Environment based on Content Project and Content Environment label",
        method = HttpMethod.get,
        responseClass = ContentEnvironmentResponse.class,
        legacyDocResponse = @LegacyDocResponse(name = "content environment information")
    )
    ContentEnvironment lookupEnvironment(
        @Parameter(hidden = true) User loggedInUser,
        @Parameter(name = "projectLabel", description = "Content Project label",
                in = ParameterIn.QUERY, required = true) String projectLabel,
        @Parameter(name = "envLabel", description = "Content Environment label",
                in = ParameterIn.QUERY, required = true) String envLabel);

    /**
     * Lists the differences of a Content Environment.
     *
     * @param loggedInUser the current user
     * @param projectLabel the Content Project label
     * @param envLabel the Content Environment label
     * @return the Content Environment differences
     */
    @ApiEndpointDoc(
        summary = "List the difference of a Content Environment based on Content Project and Content " +
                "Environment label",
        method = HttpMethod.get,
        responseClass = ContentEnvironmentDiffListResponse.class,
        legacyDocResponse = @LegacyDocResponse(name = "content environment difference information")
    )
    List<ContentEnvironmentDiff> listEnvironmentDifference(
        @Parameter(hidden = true) User loggedInUser,
        @Parameter(name = "projectLabel", description = "Content Project label",
                in = ParameterIn.QUERY, required = true) String projectLabel,
        @Parameter(name = "envLabel", description = "Content Environment label",
                in = ParameterIn.QUERY, required = true) String envLabel);

    /**
     * Creates a Content Environment.
     *
     * @param loggedInUser the current user
     * @param projectLabel the Content Project label
     * @param predecessorLabel the predecessor Environment label
     * @param envLabel the new Content Environment label
     * @param name the new Content Environment name
     * @param description the new Content Environment description
     * @return the created Content Environment
     */
    @ApiEndpointDoc(
        summary = "Create a Content Environment and appends it behind given Content Environment",
        requestClass = CreateEnvironmentRequest.class,
        responseClass = ContentEnvironmentResponse.class,
        legacyDocResponse = @LegacyDocResponse(name = "content environment information")
    )
    ContentEnvironment createEnvironment(User loggedInUser, String projectLabel, String predecessorLabel,
                                         String envLabel, String name, String description);

    /**
     * Updates a Content Environment.
     *
     * @param loggedInUser the current user
     * @param projectLabel the Content Project label
     * @param envLabel the Content Environment label
     * @param props the properties to update
     * @return the updated Content Environment
     */
    @ApiEndpointDoc(
        summary = "Update Content Environment with given label",
        requestClass = UpdateEnvironmentRequest.class,
        responseClass = ContentEnvironmentResponse.class,
        legacyDocResponse = @LegacyDocResponse(name = "content environment information")
    )
    ContentEnvironment updateEnvironment(User loggedInUser, String projectLabel, String envLabel,
                                         Map<String, Object> props);

    /**
     * Removes a Content Environment.
     *
     * @param loggedInUser the current user
     * @param projectLabel the Content Project label
     * @param envLabel the Content Environment label
     * @return 1 on success
     */
    @ApiEndpointDoc(
        summary = "Remove a Content Environment",
        requestClass = EnvironmentLabelRequest.class,
        isIntegerResponse = true
    )
    int removeEnvironment(User loggedInUser, String projectLabel, String envLabel);

    /**
     * Lists the Sources of a Content Project.
     *
     * @param loggedInUser the current user
     * @param projectLabel the Content Project label
     * @return the Project Sources
     */
    @ApiEndpointDoc(
        summary = "List all Content Project Sources",
        method = HttpMethod.get,
        responseClass = ProjectSourceListResponse.class,
        legacyDocResponse = @LegacyDocResponse(name = "content project source information")
    )
    List<ProjectSource> listProjectSources(
        @Parameter(hidden = true) User loggedInUser,
        @Parameter(name = "projectLabel", description = "Content Project label",
                in = ParameterIn.QUERY, required = true) String projectLabel);

    /**
     * Looks up a Project Source.
     *
     * @param loggedInUser the current user
     * @param projectLabel the Content Project label
     * @param sourceType the Project Source type
     * @param sourceLabel the Project Source label
     * @return the Project Source
     */
    @ApiEndpointDoc(
        summary = "Look up Content Project Source",
        method = HttpMethod.get,
        responseClass = ProjectSourceResponse.class,
        legacyDocResponse = @LegacyDocResponse(name = "content project source information")
    )
    ProjectSource lookupSource(
        @Parameter(hidden = true) User loggedInUser,
        @Parameter(name = "projectLabel", description = "Content Project label",
                in = ParameterIn.QUERY, required = true) String projectLabel,
        @Parameter(name = "sourceType", description = "Project Source type, e.g. 'software'",
                in = ParameterIn.QUERY, required = true) String sourceType,
        @Parameter(name = "sourceLabel", description = "Project Source label",
                in = ParameterIn.QUERY, required = true) String sourceLabel);

    /**
     * Attaches a Source to a Content Project.
     *
     * @param loggedInUser the current user
     * @param projectLabel the Content Project label
     * @param sourceType the Project Source type
     * @param sourceLabel the Project Source label
     * @param sourcePosition the Project Source position
     * @return the attached Project Source
     */
    @ApiEndpointDoc(
        summary = "Attach a Source to a Project",
        requestClass = AttachSourceRequest.class,
        responseClass = ProjectSourceResponse.class,
        legacyDocResponse = @LegacyDocResponse(name = "content project source information")
    )
    ProjectSource attachSource(User loggedInUser, String projectLabel, String sourceType, String sourceLabel,
                               int sourcePosition);

    /**
     * Detaches a Source from a Content Project.
     *
     * @param loggedInUser the current user
     * @param projectLabel the Content Project label
     * @param sourceType the Project Source type
     * @param sourceLabel the Project Source label
     * @return 1 on success
     */
    @ApiEndpointDoc(
        summary = "Detach a Source from a Project",
        requestClass = DetachSourceRequest.class,
        isIntegerResponse = true
    )
    int detachSource(User loggedInUser, String projectLabel, String sourceType, String sourceLabel);

    /**
     * Lists the Content Filters of the user organization.
     *
     * @param loggedInUser the current user
     * @return the Content Filters
     */
    @ApiEndpointDoc(
        summary = "List all Content Filters visible to given user",
        method = HttpMethod.get,
        responseClass = ContentFilterListResponse.class,
        legacyDocResponse = @LegacyDocResponse(name = "content filter information")
    )
    List<ContentFilter> listFilters(@Parameter(hidden = true) User loggedInUser);

    /**
     * Looks up a Content Filter.
     *
     * @param loggedInUser the current user
     * @param filterId the Filter id
     * @return the Content Filter
     */
    @ApiEndpointDoc(
        summary = "Lookup a Content Filter by id",
        method = HttpMethod.get,
        responseClass = ContentFilterResponse.class,
        legacyDocResponse = @LegacyDocResponse(name = "content filter information")
    )
    ContentFilter lookupFilter(
        @Parameter(hidden = true) User loggedInUser,
        @Parameter(name = "filterId", description = "Filter ID",
                in = ParameterIn.QUERY, required = true) Integer filterId);

    /**
     * Lists the available filter criteria.
     *
     * @param loggedInUser the current user
     * @return the available filter criteria
     */
    @ApiEndpointDoc(
        summary = "List of available filter criteria",
        method = HttpMethod.get,
        responseClass = FilterCriteriaListResponse.class,
        legacyDocResponse = @LegacyDocResponse(name = "Filter Criteria")
    )
    List<Map<String, String>> listFilterCriteria(@Parameter(hidden = true) User loggedInUser);

    /**
     * Creates a Content Filter.
     *
     * @param loggedInUser the current user
     * @param name the Filter name
     * @param rule the Filter rule
     * @param entityType the Filter entity type
     * @param criteria the Filter criteria
     * @return the created Content Filter
     */
    @ApiEndpointDoc(
        summary = "Create a Content Filter",
        requestClass = CreateFilterRequest.class,
        responseClass = ContentFilterResponse.class,
        legacyDocResponse = @LegacyDocResponse(name = "content filter information")
    )
    ContentFilter createFilter(User loggedInUser, String name, String rule, String entityType,
                               Map<String, Object> criteria);

    /**
     * Creates AppStream filters for a modular channel.
     *
     * @param loggedInUser the current user
     * @param prefix the Filter name prefix
     * @param channelLabel the modular Channel label
     * @param projectLabel the Project label
     * @return the created Content Filters
     */
    @ApiEndpointDoc(
        summary = "Create AppStream filters for the given channel and attach them to the given project",
        requestClass = CreateAppStreamFiltersRequest.class,
        responseClass = ContentFilterListResponse.class,
        legacyDocResponse = @LegacyDocResponse(name = "content filter information")
    )
    List<ContentFilter> createAppStreamFilters(User loggedInUser, String prefix, String channelLabel,
                                               String projectLabel) throws ModulemdApiException;

    /**
     * Updates a Content Filter.
     *
     * @param loggedInUser the current user
     * @param filterId the Filter id
     * @param name the new Filter name
     * @param rule the new Filter rule
     * @param criteria the new Filter criteria
     * @return the updated Content Filter
     */
    @ApiEndpointDoc(
        summary = "Update a Content Filter",
        requestClass = UpdateFilterRequest.class,
        responseClass = ContentFilterResponse.class,
        legacyDocResponse = @LegacyDocResponse(name = "content filter information")
    )
    ContentFilter updateFilter(User loggedInUser, Integer filterId, String name, String rule,
                               Map<String, Object> criteria);

    /**
     * Removes a Content Filter.
     *
     * @param loggedInUser the current user
     * @param filterId the Filter id
     * @return 1 on success
     */
    @ApiEndpointDoc(
        summary = "Remove a Content Filter",
        requestClass = FilterIdRequest.class,
        isIntegerResponse = true
    )
    int removeFilter(User loggedInUser, Integer filterId);

    /**
     * Lists the Filters attached to a Content Project.
     *
     * @param loggedInUser the current user
     * @param projectLabel the Project label
     * @return the attached Filters
     */
    @ApiEndpointDoc(
        summary = "List all Filters associated with a Project",
        method = HttpMethod.get,
        responseClass = ContentProjectFilterListResponse.class,
        legacyDocResponse = @LegacyDocResponse(name = "assigned content filter information")
    )
    List<ContentProjectFilter> listProjectFilters(
        @Parameter(hidden = true) User loggedInUser,
        @Parameter(name = "projectLabel", description = "Project label",
                in = ParameterIn.QUERY, required = true) String projectLabel);

    /**
     * Attaches a Filter to a Content Project.
     *
     * @param loggedInUser the current user
     * @param projectLabel the Project label
     * @param filterId the Filter id to attach
     * @return the attached Content Filter
     */
    @ApiEndpointDoc(
        summary = "Attach a Filter to a Project",
        requestClass = ProjectFilterRequest.class,
        responseClass = ContentFilterResponse.class,
        legacyDocResponse = @LegacyDocResponse(name = "content filter information")
    )
    ContentFilter attachFilter(User loggedInUser, String projectLabel, Integer filterId);

    /**
     * Detaches a Filter from a Content Project.
     *
     * @param loggedInUser the current user
     * @param projectLabel the Project label
     * @param filterId the Filter id to detach
     * @return 1 on success
     */
    @ApiEndpointDoc(
        summary = "Detach a Filter from a Project",
        requestClass = DetachProjectFilterRequest.class,
        isIntegerResponse = true
    )
    int detachFilter(User loggedInUser, String projectLabel, Integer filterId);

    /**
     * Builds a Content Project.
     *
     * @param loggedInUser the current user
     * @param projectLabel the Project label
     * @param message the log message to assign to the build
     * @return 1 on success
     */
    @ApiEndpointDoc(
        summary = "Build a Project",
        requestClass = BuildProjectRequest.class,
        isIntegerResponse = true
    )
    int buildProject(User loggedInUser, String projectLabel, String message);

    /**
     * Promotes an Environment of a Content Project.
     *
     * @param loggedInUser the current user
     * @param projectLabel the Project label
     * @param envLabel the Environment label
     * @return 1 on success
     */
    @ApiEndpointDoc(
        summary = "Promote an Environment in a Project",
        requestClass = PromoteProjectRequest.class,
        isIntegerResponse = true
    )
    int promoteProject(User loggedInUser, String projectLabel, String envLabel);

    /**
     * Generates the differences of all Environments of a Content Project.
     *
     * @param loggedInUser the current user
     * @param projectLabel the Project label
     * @return 1 on success
     */
    @ApiEndpointDoc(
        summary = "Generate the difference of all the Environments in a Project",
        requestClass = ProjectDifferenceRequest.class,
        isIntegerResponse = true
    )
    int generateProjectDifference(User loggedInUser, String projectLabel);

    /**
     * Generates the differences of an Environment of a Content Project.
     *
     * @param loggedInUser the current user
     * @param projectLabel the Project label
     * @param environmentLabel the Environment label
     * @return 1 on success
     */
    @ApiEndpointDoc(
        summary = "Generate the difference of an Environment in a Project",
        requestClass = GenerateEnvironmentDifferenceRequest.class,
        isIntegerResponse = true
    )
    int generateEnvironmentDifference(User loggedInUser, String projectLabel, String environmentLabel);

    @Schema(name = "ContentProject")
    @JsonPropertyOrder({"id", "label", "name", "description", "lastBuildDate", "orgId", "firstEnvironment"})
    interface ContentProjectDoc {

        /**
         * @return the Content Project id
         */
        @Schema(requiredMode = Schema.RequiredMode.REQUIRED)
        Integer getId();

        /**
         * @return the Content Project label
         */
        @Schema(requiredMode = Schema.RequiredMode.REQUIRED)
        String getLabel();

        /**
         * @return the Content Project name
         */
        @Schema(requiredMode = Schema.RequiredMode.REQUIRED)
        String getName();

        /**
         * @return the Content Project description
         */
        @Schema(requiredMode = Schema.RequiredMode.REQUIRED)
        String getDescription();

        /**
         * @return the date of the last build
         */
        @LegacyDocResponse(type = "dateTime.iso8601")
        Date getLastBuildDate();

        /**
         * @return the organization id
         */
        @Schema(requiredMode = Schema.RequiredMode.REQUIRED)
        Integer getOrgId();

        /**
         * @return the label of the first Environment
         */
        String getFirstEnvironment();
    }

    @Schema(name = "ContentEnvironment")
    @JsonPropertyOrder({"id", "label", "name", "description", "version", "status", "lastBuildDate",
        "contentProjectLabel", "previousEnvironmentLabel", "nextEnvironmentLabel"})
    interface ContentEnvironmentDoc {

        /**
         * @return the Content Environment id
         */
        @Schema(requiredMode = Schema.RequiredMode.REQUIRED)
        Integer getId();

        /**
         * @return the Content Environment label
         */
        @Schema(requiredMode = Schema.RequiredMode.REQUIRED)
        String getLabel();

        /**
         * @return the Content Environment name
         */
        @Schema(requiredMode = Schema.RequiredMode.REQUIRED)
        String getName();

        /**
         * @return the Content Environment description
         */
        @Schema(requiredMode = Schema.RequiredMode.REQUIRED)
        String getDescription();

        /**
         * @return the Content Environment version
         */
        @Schema(requiredMode = Schema.RequiredMode.REQUIRED)
        Integer getVersion();

        /**
         * @return the Content Environment status
         */
        @Schema(requiredMode = Schema.RequiredMode.REQUIRED)
        String getStatus();

        /**
         * @return the date of the last build or promote
         */
        @Schema(description = "last build/promote date")
        @LegacyDocResponse(type = "dateTime.iso8601")
        Date getLastBuildDate();

        /**
         * @return the label of the Content Project
         */
        @Schema(requiredMode = Schema.RequiredMode.REQUIRED)
        String getContentProjectLabel();

        /**
         * @return the label of the previous Content Environment
         */
        String getPreviousEnvironmentLabel();

        /**
         * @return the label of the next Content Environment
         */
        String getNextEnvironmentLabel();
    }

    @Schema(name = "ContentEnvironmentDifference")
    @JsonPropertyOrder({"id", "type", "action", "name", "description"})
    interface ContentEnvironmentDiffDoc {

        /**
         * @return the difference id
         */
        @Schema(requiredMode = Schema.RequiredMode.REQUIRED)
        Integer getId();

        /**
         * @return the type of the changed entity
         */
        @Schema(requiredMode = Schema.RequiredMode.REQUIRED)
        String getType();

        /**
         * @return the action to be performed
         */
        @Schema(requiredMode = Schema.RequiredMode.REQUIRED)
        String getAction();

        /**
         * @return the name of the changed entity
         */
        @Schema(requiredMode = Schema.RequiredMode.REQUIRED)
        String getName();

        /**
         * @return the description of the change
         */
        @Schema(requiredMode = Schema.RequiredMode.REQUIRED)
        String getDescription();
    }

    @Schema(name = "ContentProjectSource")
    @JsonPropertyOrder({"contentProjectLabel", "type", "state", "channelLabel"})
    interface ProjectSourceDoc {

        /**
         * @return the label of the Content Project
         */
        @Schema(requiredMode = Schema.RequiredMode.REQUIRED)
        String getContentProjectLabel();

        /**
         * @return the Source type
         */
        @Schema(requiredMode = Schema.RequiredMode.REQUIRED)
        String getType();

        /**
         * @return the Source state
         */
        @Schema(requiredMode = Schema.RequiredMode.REQUIRED)
        String getState();

        /**
         * @return the label of the associated channel
         */
        @Schema(description = "(if type is SW_CHANNEL) the label of channel associated with the source")
        String getChannelLabel();
    }

    @Schema(name = "ContentFilterCriteria")
    @JsonPropertyOrder({"matcher", "field", "value"})
    interface FilterCriteriaDoc {

        /**
         * @return the matcher type of the filter
         */
        @Schema(description = "the matcher type of the filter (e.g. 'contains')",
                requiredMode = Schema.RequiredMode.REQUIRED)
        String getMatcher();

        /**
         * @return the entity field to match
         */
        @Schema(description = "the entity field to match (e.g. 'name'",
                requiredMode = Schema.RequiredMode.REQUIRED)
        String getField();

        /**
         * @return the field value to match
         */
        @Schema(description = "the field value to match (e.g. 'kernel')",
                requiredMode = Schema.RequiredMode.REQUIRED)
        String getValue();
    }

    @Schema(name = "ContentFilter")
    @JsonPropertyOrder({"id", "name", "orgId", "entityType", "rule", "criteria"})
    interface ContentFilterDoc {

        /**
         * @return the Filter id
         */
        @Schema(requiredMode = Schema.RequiredMode.REQUIRED)
        Integer getId();

        /**
         * @return the Filter name
         */
        @Schema(requiredMode = Schema.RequiredMode.REQUIRED)
        String getName();

        /**
         * @return the organization id
         */
        @Schema(requiredMode = Schema.RequiredMode.REQUIRED)
        Integer getOrgId();

        /**
         * @return the entity type of the Filter
         */
        @Schema(description = "entity type (e.g. 'package')", requiredMode = Schema.RequiredMode.REQUIRED)
        @LegacyDocResponse(type = "entityType")
        String getEntityType();

        /**
         * @return the rule of the Filter
         */
        @Schema(description = "rule (e.g. 'deny')", requiredMode = Schema.RequiredMode.REQUIRED)
        @LegacyDocResponse(type = "rule")
        String getRule();

        /**
         * @return the Filter criteria
         */
        @Schema(requiredMode = Schema.RequiredMode.REQUIRED)
        @LegacyDocResponse(name = "criteria")
        FilterCriteriaDoc getCriteria();
    }

    @Schema(name = "AssignedContentFilter")
    @JsonPropertyOrder({"state", "filter"})
    interface ContentProjectFilterDoc {

        /**
         * @return the state of the assigned Filter
         */
        @Schema(requiredMode = Schema.RequiredMode.REQUIRED)
        String getState();

        /**
         * @return the assigned Content Filter
         */
        @Schema(requiredMode = Schema.RequiredMode.REQUIRED)
        @LegacyDocResponse(name = "content filter information")
        ContentFilterDoc getFilter();
    }

    @Schema(name = "FilterCriterion")
    @JsonPropertyOrder({"type", "matcher", "field"})
    interface FilterCriterionDoc {

        /**
         * @return the entity type the criterion applies to
         */
        @Schema(requiredMode = Schema.RequiredMode.REQUIRED)
        String getType();

        /**
         * @return the matcher type
         */
        @Schema(requiredMode = Schema.RequiredMode.REQUIRED)
        String getMatcher();

        /**
         * @return the entity field
         */
        @Schema(requiredMode = Schema.RequiredMode.REQUIRED)
        String getField();
    }

    @Schema(name = "ContentProjectPropertiesRequest")
    @JsonPropertyOrder({"name", "description"})
    interface ContentProjectPropsDoc {

        /**
         * @return the Content Project name
         */
        @Schema(description = "Content Project name")
        String getName();

        /**
         * @return the Content Project description
         */
        @Schema(description = "Content Project description")
        String getDescription();
    }

    @Schema(name = "ContentEnvironmentPropertiesRequest")
    @JsonPropertyOrder({"name", "description"})
    interface ContentEnvironmentPropsDoc {

        /**
         * @return the Content Environment name
         */
        @Schema(description = "Content Environment name")
        String getName();

        /**
         * @return the Content Environment description
         */
        @Schema(description = "Content Environment description")
        String getDescription();
    }

    @Schema(name = "ContentProjectDifferenceRequest")
    interface ProjectDifferenceRequest {

        /**
         * @return the Content Project label
         */
        @Schema(description = "Project label", requiredMode = Schema.RequiredMode.REQUIRED)
        String getProjectLabel();
    }

    @Schema(name = "ContentProjectLabelRequest")
    interface ProjectLabelRequest {

        /**
         * @return the Content Project label
         */
        @Schema(description = "Content Project label", requiredMode = Schema.RequiredMode.REQUIRED)
        String getProjectLabel();
    }

    @Schema(name = "ContentCreateProjectRequest")
    @JsonPropertyOrder({"projectLabel", "name", "description"})
    interface CreateProjectRequest {

        /**
         * @return the Content Project label
         */
        @Schema(description = "Content Project label", requiredMode = Schema.RequiredMode.REQUIRED)
        String getProjectLabel();

        /**
         * @return the Content Project name
         */
        @Schema(description = "Content Project name", requiredMode = Schema.RequiredMode.REQUIRED)
        String getName();

        /**
         * @return the Content Project description
         */
        @Schema(description = "Content Project description", requiredMode = Schema.RequiredMode.REQUIRED)
        String getDescription();
    }

    @Schema(name = "ContentUpdateProjectRequest")
    @JsonPropertyOrder({"projectLabel", "props"})
    interface UpdateProjectRequest {

        /**
         * @return the Content Project label
         */
        @Schema(description = "Content Project label", requiredMode = Schema.RequiredMode.REQUIRED)
        String getProjectLabel();

        /**
         * @return the properties to update
         */
        @Schema(requiredMode = Schema.RequiredMode.REQUIRED)
        @LegacyDocResponse(name = "props")
        ContentProjectPropsDoc getProps();
    }

    @Schema(name = "ContentPromoteProjectRequest")
    @JsonPropertyOrder({"projectLabel", "envLabel"})
    interface PromoteProjectRequest {

        /**
         * @return the Content Project label
         */
        @Schema(description = "Project label", requiredMode = Schema.RequiredMode.REQUIRED)
        String getProjectLabel();

        /**
         * @return the Content Environment label
         */
        @Schema(description = "Environment label", requiredMode = Schema.RequiredMode.REQUIRED)
        String getEnvLabel();
    }

    @Schema(name = "ContentEnvironmentLabelRequest")
    @JsonPropertyOrder({"projectLabel", "envLabel"})
    interface EnvironmentLabelRequest {

        /**
         * @return the Content Project label
         */
        @Schema(description = "Content Project label", requiredMode = Schema.RequiredMode.REQUIRED)
        String getProjectLabel();

        /**
         * @return the Content Environment label
         */
        @Schema(description = "Content Environment label", requiredMode = Schema.RequiredMode.REQUIRED)
        String getEnvLabel();
    }

    @Schema(name = "ContentCreateEnvironmentRequest")
    @JsonPropertyOrder({"projectLabel", "predecessorLabel", "envLabel", "name", "description"})
    interface CreateEnvironmentRequest {

        /**
         * @return the Content Project label
         */
        @Schema(description = "Content Project label", requiredMode = Schema.RequiredMode.REQUIRED)
        String getProjectLabel();

        /**
         * @return the predecessor Environment label
         */
        @Schema(description = "Predecessor Environment label", requiredMode = Schema.RequiredMode.REQUIRED)
        String getPredecessorLabel();

        /**
         * @return the new Content Environment label
         */
        @Schema(description = "new Content Environment label", requiredMode = Schema.RequiredMode.REQUIRED)
        String getEnvLabel();

        /**
         * @return the new Content Environment name
         */
        @Schema(description = "new Content Environment name", requiredMode = Schema.RequiredMode.REQUIRED)
        String getName();

        /**
         * @return the new Content Environment description
         */
        @Schema(description = "new Content Environment description", requiredMode = Schema.RequiredMode.REQUIRED)
        String getDescription();
    }

    @Schema(name = "ContentUpdateEnvironmentRequest")
    @JsonPropertyOrder({"projectLabel", "envLabel", "props"})
    interface UpdateEnvironmentRequest {

        /**
         * @return the Content Project label
         */
        @Schema(description = "Content Project label", requiredMode = Schema.RequiredMode.REQUIRED)
        String getProjectLabel();

        /**
         * @return the Content Environment label
         */
        @Schema(description = "Content Environment label", requiredMode = Schema.RequiredMode.REQUIRED)
        String getEnvLabel();

        /**
         * @return the properties to update
         */
        @Schema(requiredMode = Schema.RequiredMode.REQUIRED)
        @LegacyDocResponse(name = "props")
        ContentEnvironmentPropsDoc getProps();
    }

    @Schema(name = "ContentAttachSourceRequest")
    @JsonPropertyOrder({"projectLabel", "sourceType", "sourceLabel", "sourcePosition"})
    interface AttachSourceRequest {

        /**
         * @return the Content Project label
         */
        @Schema(description = "Content Project label", requiredMode = Schema.RequiredMode.REQUIRED)
        String getProjectLabel();

        /**
         * @return the Project Source type
         */
        @Schema(description = "Project Source type, e.g. 'software'", requiredMode = Schema.RequiredMode.REQUIRED)
        String getSourceType();

        /**
         * @return the Project Source label
         */
        @Schema(description = "Project Source label", requiredMode = Schema.RequiredMode.REQUIRED)
        String getSourceLabel();

        /**
         * @return the Project Source position
         */
        @Schema(description = "Project Source position")
        Integer getSourcePosition();
    }

    @Schema(name = "ContentDetachSourceRequest")
    @JsonPropertyOrder({"projectLabel", "sourceType", "sourceLabel"})
    interface DetachSourceRequest {

        /**
         * @return the Content Project label
         */
        @Schema(description = "Content Project label", requiredMode = Schema.RequiredMode.REQUIRED)
        String getProjectLabel();

        /**
         * @return the Project Source type
         */
        @Schema(description = "Project Source type, e.g. 'software'", requiredMode = Schema.RequiredMode.REQUIRED)
        String getSourceType();

        /**
         * @return the Project Source label
         */
        @Schema(description = "Project Source label", requiredMode = Schema.RequiredMode.REQUIRED)
        String getSourceLabel();
    }

    @Schema(name = "ContentFilterIdRequest")
    interface FilterIdRequest {

        /**
         * @return the Filter id
         */
        @Schema(description = "Filter ID", requiredMode = Schema.RequiredMode.REQUIRED)
        Integer getFilterId();
    }

    @Schema(name = "ContentCreateFilterRequest")
    @JsonPropertyOrder({"name", "rule", "entityType", "criteria"})
    interface CreateFilterRequest {

        /**
         * @return the Filter name
         */
        @Schema(description = "Filter name", requiredMode = Schema.RequiredMode.REQUIRED)
        String getName();

        /**
         * @return the Filter rule
         */
        @Schema(description = "Filter rule ('deny' or 'allow')", requiredMode = Schema.RequiredMode.REQUIRED)
        String getRule();

        /**
         * @return the Filter entity type
         */
        @Schema(description = "Filter entityType ('package' or 'erratum')",
                requiredMode = Schema.RequiredMode.REQUIRED)
        String getEntityType();

        /**
         * @return the Filter criteria
         */
        @Schema(requiredMode = Schema.RequiredMode.REQUIRED)
        @LegacyDocResponse(name = "criteria")
        CreateFilterCriteriaDoc getCriteria();
    }

    @Schema(name = "ContentCreateFilterCriteria")
    @JsonPropertyOrder({"matcher", "field", "value"})
    interface CreateFilterCriteriaDoc {

        /**
         * @return the matcher type of the filter
         */
        @Schema(description = "The matcher type of the filter (e.g. 'contains')",
                requiredMode = Schema.RequiredMode.REQUIRED)
        String getMatcher();

        /**
         * @return the entity field to match
         */
        @Schema(description = "The entity field to match (e.g. 'name'",
                requiredMode = Schema.RequiredMode.REQUIRED)
        String getField();

        /**
         * @return the field value to match
         */
        @Schema(description = "The field value to match (e.g. 'kernel')",
                requiredMode = Schema.RequiredMode.NOT_REQUIRED)
        String getValue();
    }

    @Schema(name = "ContentCreateAppStreamFiltersRequest")
    @JsonPropertyOrder({"prefix", "channelLabel", "projectLabel"})
    interface CreateAppStreamFiltersRequest {

        /**
         * @return the Filter name prefix
         */
        @Schema(description = "Filter name prefix", requiredMode = Schema.RequiredMode.REQUIRED)
        String getPrefix();

        /**
         * @return the modular Channel label
         */
        @Schema(description = "Modular Channel label", requiredMode = Schema.RequiredMode.REQUIRED)
        String getChannelLabel();

        /**
         * @return the Project label
         */
        @Schema(description = "Project label", requiredMode = Schema.RequiredMode.REQUIRED)
        String getProjectLabel();
    }

    @Schema(name = "ContentUpdateFilterRequest")
    @JsonPropertyOrder({"filterId", "name", "rule", "criteria"})
    interface UpdateFilterRequest {

        /**
         * @return the Filter id
         */
        @Schema(description = "Filter ID", requiredMode = Schema.RequiredMode.REQUIRED)
        Integer getFilterId();

        /**
         * @return the new Filter name
         */
        @Schema(description = "New filter name", requiredMode = Schema.RequiredMode.REQUIRED)
        String getName();

        /**
         * @return the new Filter rule
         */
        @Schema(description = "New filter rule ('deny' or 'allow')", requiredMode = Schema.RequiredMode.REQUIRED)
        String getRule();

        /**
         * @return the new Filter criteria
         */
        @Schema(requiredMode = Schema.RequiredMode.REQUIRED)
        @LegacyDocResponse(name = "criteria")
        CreateFilterCriteriaDoc getCriteria();
    }

    @Schema(name = "ContentDetachProjectFilterRequest")
    @JsonPropertyOrder({"projectLabel", "filterId"})
    interface DetachProjectFilterRequest {

        /**
         * @return the Project label
         */
        @Schema(description = "Project label", requiredMode = Schema.RequiredMode.REQUIRED)
        String getProjectLabel();

        /**
         * @return the Filter id
         */
        @Schema(description = "filter ID to detach", requiredMode = Schema.RequiredMode.REQUIRED)
        Integer getFilterId();
    }

    @Schema(name = "ContentProjectFilterRequest")
    @JsonPropertyOrder({"projectLabel", "filterId"})
    interface ProjectFilterRequest {

        /**
         * @return the Project label
         */
        @Schema(description = "Project label", requiredMode = Schema.RequiredMode.REQUIRED)
        String getProjectLabel();

        /**
         * @return the Filter id
         */
        @Schema(description = "filter ID to attach", requiredMode = Schema.RequiredMode.REQUIRED)
        Integer getFilterId();
    }

    @Schema(name = "ContentBuildProjectRequest")
    @JsonPropertyOrder({"projectLabel", "message"})
    interface BuildProjectRequest {

        /**
         * @return the Project label
         */
        @Schema(description = "Project label", requiredMode = Schema.RequiredMode.REQUIRED)
        String getProjectLabel();

        /**
         * @return the log message to assign to the build
         */
        @Schema(description = "log message to be assigned to the build")
        String getMessage();
    }

    @Schema(name = "ContentGenerateEnvironmentDifferenceRequest")
    @JsonPropertyOrder({"projectLabel", "environmentLabel"})
    interface GenerateEnvironmentDifferenceRequest {

        /**
         * @return the Project label
         */
        @Schema(description = "Project label", requiredMode = Schema.RequiredMode.REQUIRED)
        String getProjectLabel();

        /**
         * @return the Environment label
         */
        @Schema(description = "Environment label", requiredMode = Schema.RequiredMode.REQUIRED)
        String getEnvironmentLabel();
    }

    @Schema(name = "ApiResponseContentProject")
    interface ContentProjectResponse extends ApiResponseWrapper<ContentProjectDoc> { }

    @Schema(name = "ApiResponseContentProjectList")
    interface ContentProjectListResponse extends ApiResponseWrapper<List<ContentProjectDoc>> { }

    @Schema(name = "ApiResponseContentEnvironment")
    interface ContentEnvironmentResponse extends ApiResponseWrapper<ContentEnvironmentDoc> { }

    @Schema(name = "ApiResponseContentEnvironmentList")
    interface ContentEnvironmentListResponse extends ApiResponseWrapper<List<ContentEnvironmentDoc>> { }

    @Schema(name = "ApiResponseContentEnvironmentDifferenceList")
    interface ContentEnvironmentDiffListResponse extends ApiResponseWrapper<List<ContentEnvironmentDiffDoc>> { }

    @Schema(name = "ApiResponseContentProjectSource")
    interface ProjectSourceResponse extends ApiResponseWrapper<ProjectSourceDoc> { }

    @Schema(name = "ApiResponseContentProjectSourceList")
    interface ProjectSourceListResponse extends ApiResponseWrapper<List<ProjectSourceDoc>> { }

    @Schema(name = "ApiResponseContentFilter")
    interface ContentFilterResponse extends ApiResponseWrapper<ContentFilterDoc> { }

    @Schema(name = "ApiResponseContentFilterList")
    interface ContentFilterListResponse extends ApiResponseWrapper<List<ContentFilterDoc>> { }

    @Schema(name = "ApiResponseAssignedContentFilterList")
    interface ContentProjectFilterListResponse extends ApiResponseWrapper<List<ContentProjectFilterDoc>> { }

    @Schema(name = "ApiResponseFilterCriterionList")
    interface FilterCriteriaListResponse extends ApiResponseWrapper<List<FilterCriterionDoc>> { }
}
