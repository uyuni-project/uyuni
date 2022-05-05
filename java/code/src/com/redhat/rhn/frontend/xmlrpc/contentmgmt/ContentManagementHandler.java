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

package com.redhat.rhn.frontend.xmlrpc.contentmgmt;

import static com.redhat.rhn.common.util.StringUtil.nullIfEmpty;
import static java.util.Optional.empty;
import static java.util.Optional.of;
import static java.util.Optional.ofNullable;

import com.redhat.rhn.common.hibernate.LookupException;
import com.redhat.rhn.common.validator.ValidatorException;
import com.redhat.rhn.domain.channel.Channel;
import com.redhat.rhn.domain.contentmgmt.ContentEnvironment;
import com.redhat.rhn.domain.contentmgmt.ContentFilter;
import com.redhat.rhn.domain.contentmgmt.ContentManagementException;
import com.redhat.rhn.domain.contentmgmt.ContentProject;
import com.redhat.rhn.domain.contentmgmt.ContentProjectFilter;
import com.redhat.rhn.domain.contentmgmt.FilterCriteria;
import com.redhat.rhn.domain.contentmgmt.ProjectSource;
import com.redhat.rhn.domain.contentmgmt.ProjectSource.Type;
import com.redhat.rhn.domain.contentmgmt.modulemd.ModulemdApiException;
import com.redhat.rhn.domain.contentmgmt.validation.ContentProjectValidator;
import com.redhat.rhn.domain.contentmgmt.validation.ContentValidationMessage;
import com.redhat.rhn.domain.user.User;
import com.redhat.rhn.frontend.xmlrpc.BaseHandler;
import com.redhat.rhn.frontend.xmlrpc.ContentManagementFaultException;
import com.redhat.rhn.frontend.xmlrpc.ContentValidationFaultException;
import com.redhat.rhn.frontend.xmlrpc.EntityExistsFaultException;
import com.redhat.rhn.frontend.xmlrpc.EntityNotExistsFaultException;
import com.redhat.rhn.frontend.xmlrpc.InvalidArgsException;
import com.redhat.rhn.frontend.xmlrpc.ValidationException;
import com.redhat.rhn.manager.EntityExistsException;
import com.redhat.rhn.manager.EntityNotExistsException;
import com.redhat.rhn.manager.channel.ChannelManager;
import com.redhat.rhn.manager.contentmgmt.ContentManager;
import com.redhat.rhn.manager.contentmgmt.FilterTemplateManager;

import org.apache.commons.lang3.StringUtils;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

/**
 * Content Management XMLRPC handler
 *
 * @xmlrpc.namespace contentmanagement
 * @xmlrpc.doc Provides methods to access and modify Content Lifecycle Management related entities
 * (Projects, Environments, Filters, Sources).
 */
public class ContentManagementHandler extends BaseHandler {

    private final ContentManager contentManager;
    private final FilterTemplateManager filterTemplateManager = new FilterTemplateManager();

    /**
     * Initialize a handler specifying a content manager instance.
     */
    public ContentManagementHandler() {
        contentManager = new ContentManager();
    }

    /**
     * Initialize a handler specifying a content manager instance. Mainly used for testing.
     *
     * @param contentManagerIn the content manager instance
     */
    public ContentManagementHandler(ContentManager contentManagerIn) {
        contentManager = contentManagerIn;
    }

    /**
     * List Content Projects visible to user
     *
     * @param loggedInUser the logged in user
     * @return the list of Content Projects visible to user
     *
     * @xmlrpc.doc List Content Projects visible to user
     * @xmlrpc.param #session_key()
     * @xmlrpc.returntype
     * #array_begin()
     * $ContentProjectSerializer
     * #array_end()
     */
    public List<ContentProject> listProjects(User loggedInUser) {
        return ContentManager.listProjects(loggedInUser);
    }

    /**
     * Look up Content Project with given label
     *
     * @param loggedInUser the logged in user
     * @param label the Content Project label
     * @throws EntityNotExistsFaultException when the Content Project does not exist
     * @return the Content Project with given label
     *
     * @xmlrpc.doc Look up Content Project with given label
     * @xmlrpc.param #session_key()
     * @xmlrpc.param #param_desc("string", "label", "Content Project label")
     * @xmlrpc.returntype $ContentProjectSerializer
     */
    public ContentProject lookupProject(User loggedInUser, String label) {
        return ContentManager.lookupProject(label, loggedInUser)
                .orElseThrow(() -> new EntityNotExistsFaultException(label));
    }

    /**
     * Create Content Project
     *
     * @param loggedInUser the logged in user
     * @param label the Content Project label
     * @param name the Content Project name
     * @param description the description
     * @throws EntityExistsFaultException when Project already exists
     * @throws ValidationException if validation violation occurs
     * @return the created Content Project
     *
     * @xmlrpc.doc Create Content Project
     * @xmlrpc.param #session_key()
     * @xmlrpc.param #param_desc("string", "label", "Content Project label")
     * @xmlrpc.param #param_desc("string", "name", "Content Project name")
     * @xmlrpc.param #param_desc("string", "description", "Content Project description")
     * @xmlrpc.returntype $ContentProjectSerializer
     */
    public ContentProject createProject(User loggedInUser, String label, String name, String description) {
        ensureOrgAdmin(loggedInUser);
        try {
            return contentManager.createProject(label, name, description, loggedInUser);
        }
        catch (EntityExistsException e) {
            throw new EntityExistsFaultException(e);
        }
        catch (ValidatorException e) {
            throw new ValidationException(e);
        }
    }

    /**
     * Update Content Project
     *
     * @param loggedInUser the logged in user
     * @param label the new label
     * @param props the map with the Content Project properties
     * @throws EntityNotExistsFaultException when Project does not exist
     * @throws ValidationException if validation violation occurs
     * @return the updated Content Project
     *
     * @xmlrpc.doc Update Content Project with given label
     * @xmlrpc.param #session_key()
     * @xmlrpc.param #param_desc("string", "label", "Content Project label")
     * @xmlrpc.param
     *  #struct_begin("data")
     *      #prop_desc("string", "name", "Content Project name")
     *      #prop_desc("string", "description", "Content Project description")
     *  #struct_end()
     * @xmlrpc.returntype $ContentProjectSerializer
     */
    public ContentProject updateProject(User loggedInUser, String label, Map<String, Object> props) {
        ensureOrgAdmin(loggedInUser);
        try {
            return contentManager.updateProject(label,
                    ofNullable((String) props.get("name")),
                    ofNullable((String) props.get("description")),
                    loggedInUser);
        }
        catch (EntityNotExistsException e) {
            throw new EntityNotExistsFaultException(e);
        }
        catch (ValidatorException e) {
            throw new ValidationException(e);
        }
    }

    /**
     * Remove Content Project
     *
     * @param loggedInUser the logged in user
     * @param label the label
     * @throws EntityNotExistsFaultException when Project does not exist
     * @return the number of removed objects
     *
     * @xmlrpc.doc Remove Content Project
     * @xmlrpc.param #session_key()
     * @xmlrpc.param #param_desc("string", "label", "Content Project label")
     * @xmlrpc.returntype #return_int_success()
     */
    public int removeProject(User loggedInUser, String label) {
        ensureOrgAdmin(loggedInUser);
        try {
            return contentManager.removeProject(label, loggedInUser);
        }
        catch (EntityNotExistsException e) {
            throw new EntityNotExistsFaultException(e);
        }
    }

    /**
     * List Environments in a Content Project with the respect to their ordering
     *
     * @param loggedInUser the logged in user
     * @param projectLabel the Content Project label
     * @throws EntityNotExistsFaultException when Project does not exist
     * @return the List of Content Environments with respect to their ordering
     *
     * @xmlrpc.doc List Environments in a Content Project with the respect to their ordering
     * @xmlrpc.param #session_key()
     * @xmlrpc.param #param_desc("string", "projectLabel", "Content Project label")
     * @xmlrpc.returntype
     * #array_begin()
     * $ContentEnvironmentSerializer
     * #array_end()
     */
    public List<ContentEnvironment> listProjectEnvironments(User loggedInUser, String projectLabel) {
        try {
            return ContentManager.listProjectEnvironments(projectLabel, loggedInUser);
        }
        catch (EntityNotExistsException e) {
            throw new EntityNotExistsFaultException(e);
        }
    }

    /**
     * Look up Content Environment based on Content Project and Content Environment label
     *
     * @param loggedInUser the logged in user
     * @param projectLabel the Content Project label
     * @param envLabel the Content Environment label
     * @throws EntityNotExistsException when Project does not exist
     * @return found Content Environment or null if no such environment exists
     *
     * @xmlrpc.doc Look up Content Environment based on Content Project and Content Environment label
     * @xmlrpc.param #session_key()
     * @xmlrpc.param #param_desc("string", "projectLabel", "Content Project label")
     * @xmlrpc.param #param_desc("string", "envLabel", "Content Environment label")
     * @xmlrpc.returntype $ContentEnvironmentSerializer
     */
    public ContentEnvironment lookupEnvironment(User loggedInUser, String projectLabel, String envLabel) {
        try {
            return ContentManager.lookupEnvironment(envLabel, projectLabel, loggedInUser)
                    .orElseThrow(() -> new EntityNotExistsFaultException(envLabel));
        }
        catch (EntityNotExistsException e) {
            throw new EntityNotExistsFaultException(e);
        }
    }

    /**
     * Create a Content Environment and appends it behind given Content Environment
     *
     * @param loggedInUser the logged in user
     * @param projectLabel the Content Project label
     * @param predecessorLabel the Predecessor label
     * @param label the Content Environment Label
     * @param name the Content Environment name
     * @param description the Content Environment description
     * @throws EntityNotExistsFaultException when Project or predecessor Environment does not exist
     * @throws EntityExistsFaultException when Environment with given parameters already exists
     * @throws ValidationException if validation violation occurs
     * @return the created Content Environment
     *
     * @xmlrpc.doc Create a Content Environment and appends it behind given Content Environment
     * @xmlrpc.param #session_key()
     * @xmlrpc.param #param_desc("string", "projectLabel", "Content Project label")
     * @xmlrpc.param #param_desc("string", "predecessorLabel", "Predecessor Environment label")
     * @xmlrpc.param #param_desc("string", "label", "new Content Environment label")
     * @xmlrpc.param #param_desc("string", "name", "new Content Environment name")
     * @xmlrpc.param #param_desc("string", "description", "new Content Environment description")
     * @xmlrpc.returntype $ContentEnvironmentSerializer
     */
    public ContentEnvironment createEnvironment(User loggedInUser, String projectLabel, String predecessorLabel,
            String label, String name, String description) {
        ensureOrgAdmin(loggedInUser);
        try {
            return contentManager.createEnvironment(projectLabel, ofNullable(nullIfEmpty(predecessorLabel)), label,
                    name, description, true, loggedInUser);
        }
        catch (EntityNotExistsException e) {
            throw new EntityNotExistsFaultException(e);
        }
        catch (EntityExistsException e) {
            throw new EntityExistsFaultException(e);
        }
        catch (ValidatorException e) {
            throw new ValidationException(e);
        }
    }

    /**
     * Update Content Environment
     *
     * @param loggedInUser the logged in user
     * @param projectLabel the Content Project label
     * @param envLabel the Environment label
     * @param props the map with the Environment properties
     * @throws EntityNotExistsFaultException when the Environment does not exist
     * @throws ValidationException if validation violation occurs
     * @return the updated Environment
     *
     * @xmlrpc.doc Update Content Environment with given label
     * @xmlrpc.param #session_key()
     * @xmlrpc.param #param_desc("string", "projectLabel", "Content Project label")
     * @xmlrpc.param #param_desc("string", "envLabel", "Content Environment label")
     * @xmlrpc.param
     *  #struct_begin("data")
     *      #prop_desc("string", "name", "Content Environment name")
     *      #prop_desc("string", "description", "Content Environment description")
     *  #struct_end()
     * @xmlrpc.returntype $ContentEnvironmentSerializer
     */
    public ContentEnvironment updateEnvironment(User loggedInUser, String projectLabel, String envLabel,
            Map<String, Object> props) {
        ensureOrgAdmin(loggedInUser);
        try {
            return contentManager.updateEnvironment(envLabel,
                    projectLabel,
                    ofNullable((String) props.get("name")),
                    ofNullable((String) props.get("description")),
                    loggedInUser);
        }
        catch (EntityNotExistsException e) {
            throw new EntityNotExistsFaultException(e);
        }
        catch (ValidatorException e) {
            throw new ValidationException(e);
        }
    }

    /**
     * Remove a Content Environment
     *
     * @param loggedInUser the logged in user
     * @param projectLabel the Content Project label
     * @param envLabel the Content Environment label
     * @throws EntityNotExistsFaultException when Project does not exist
     * @return the number of removed objects
     *
     * @xmlrpc.doc Remove a Content Environment
     * @xmlrpc.param #session_key()
     * @xmlrpc.param #param_desc("string", "projectLabel", "Content Project label")
     * @xmlrpc.param #param_desc("string", "envLabel", "Content Environment label")
     * @xmlrpc.returntype #return_int_success()
     */
    public int removeEnvironment(User loggedInUser, String projectLabel, String envLabel) {
        ensureOrgAdmin(loggedInUser);
        try {
            return contentManager.removeEnvironment(envLabel, projectLabel, loggedInUser);
        }
        catch (EntityNotExistsException e) {
            throw new EntityNotExistsFaultException(e);
        }
    }

    /**
     * List Content Project Sources
     *
     * @param loggedInUser the logged in user
     * @param projectLabel the Project label
     * @throws EntityNotExistsFaultException when Project does not exist
     * @return list of Project Sources
     *
     * @xmlrpc.doc List Content Project Sources
     * @xmlrpc.param #session_key()
     * @xmlrpc.param #param_desc("string", "projectLabel", "Content Project label")
     * @xmlrpc.returntype
     * #array_begin()
     * $ContentProjectSourceSerializer
     * #array_end()
     */
    public List<ProjectSource> listProjectSources(User loggedInUser, String projectLabel) {
        return ContentManager.lookupProject(projectLabel, loggedInUser)
                .orElseThrow(() -> new EntityNotExistsFaultException(projectLabel))
                .getSources();
    }

    /**
     * Look up Content Project Source
     *
     * @param loggedInUser the logged in user
     * @param projectLabel the Project label
     * @param sourceType the Source type (e.g. "software")
     * @param sourceLabel the Source label (e.g. software channel label)
     * @throws EntityNotExistsFaultException if the Project or Project Source is not found
     * @return list of Project Sources
     *
     * @xmlrpc.doc Look up Content Project Source
     * @xmlrpc.param #session_key()
     * @xmlrpc.param #param_desc("string", "projectLabel", "Content Project label")
     * @xmlrpc.param #param_desc("string", "sourceType", "Project Source type, e.g. 'software'")
     * @xmlrpc.param #param_desc("string", "sourceLabel", "Project Source label")
     * @xmlrpc.returntype $ContentProjectSourceSerializer
     */
    public ProjectSource lookupSource(User loggedInUser, String projectLabel, String sourceType,
            String sourceLabel) {
        Type type = Type.lookupByLabel(sourceType);
        try {
            return ContentManager.lookupProjectSource(projectLabel, type, sourceLabel, loggedInUser)
                    .orElseThrow(() -> new EntityNotExistsFaultException(sourceLabel));
        }
        catch (EntityNotExistsException e) {
            throw new EntityNotExistsFaultException(e);
        }
    }

    /**
     * Attach a Source to a Project
     *
     * @param loggedInUser the logged in user
     * @param projectLabel the Project label
     * @param sourceType the Source type (e.g. "software")
     * @param sourceLabel the Source label (e.g. software channel label)
     * @param sourcePosition the Source position
     * @throws EntityExistsFaultException when Source already exists
     * @throws EntityNotExistsFaultException when used entities don't exist or are not accessible
     * @return the created Source
     *
     * @xmlrpc.doc Attach a Source to a Project
     * @xmlrpc.param #session_key()
     * @xmlrpc.param #param_desc("string", "projectLabel", "Content Project label")
     * @xmlrpc.param #param_desc("string", "sourceType", "Project Source type, e.g. 'software'")
     * @xmlrpc.param #param_desc("string", "sourceLabel", "Project Source label")
     * @xmlrpc.param #param_desc("int", "sourcePosition", "Project Source position")
     * @xmlrpc.returntype $ContentProjectSourceSerializer
     */
    public ProjectSource attachSource(User loggedInUser, String projectLabel, String sourceType, String sourceLabel,
            int sourcePosition) {
        return attachSource(loggedInUser, projectLabel, sourceType, sourceLabel, of(sourcePosition));
    }

    /**
     * Attach a Source to a Project
     *
     * @param loggedInUser the logged in user
     * @param projectLabel the Project label
     * @param sourceType the Source type (e.g. "software")
     * @param sourceLabel the Source label (e.g. software channel label)
     * @throws EntityExistsFaultException when Source already exists
     * @throws EntityNotExistsFaultException when used entities don't exist or are not accessible
     * @return the created Source
     *
     * @xmlrpc.doc Attach a Source to a Project
     * @xmlrpc.param #session_key()
     * @xmlrpc.param #param_desc("string", "projectLabel", "Content Project label")
     * @xmlrpc.param #param_desc("string", "sourceType", "Project Source type, e.g. 'software'")
     * @xmlrpc.param #param_desc("string", "sourceLabel", "Project Source label")
     * @xmlrpc.returntype $ContentProjectSourceSerializer
     */
    public ProjectSource attachSource(User loggedInUser, String projectLabel, String sourceType, String sourceLabel) {
        return attachSource(loggedInUser, projectLabel, sourceType, sourceLabel, empty());
    }

    // helper method
    private ProjectSource attachSource(User loggedInUser, String projectLabel, String sourceType, String sourceLabel,
            Optional<Integer> sourcePosition) {
        ensureOrgAdmin(loggedInUser);
        Type type = Type.lookupByLabel(sourceType);
        try {
            return contentManager.attachSource(projectLabel, type, sourceLabel, sourcePosition, loggedInUser);
        }
        catch (EntityNotExistsException e) {
            throw new EntityNotExistsFaultException(e);
        }
        catch (EntityExistsException e) {
            throw new EntityExistsFaultException(e);
        }
    }

    /**
     * Detach a Source from a Project
     *
     * @param loggedInUser the logged in user
     * @param projectLabel the Project label
     * @param sourceType the Source type (e.g. "software")
     * @param sourceLabel the Source label (e.g. software channel label)
     * @throws EntityNotExistsFaultException when used entities don't exist or are not accessible
     * @return 1 on success
     *
     * @xmlrpc.doc Detach a Source from a Project
     * @xmlrpc.param #session_key()
     * @xmlrpc.param #param_desc("string", "projectLabel", "Content Project label")
     * @xmlrpc.param #param_desc("string", "sourceType", "Project Source type, e.g. 'software'")
     * @xmlrpc.param #param_desc("string", "sourceLabel", "Project Source label")
     * @xmlrpc.returntype #return_int_success()
     */
    public int detachSource(User loggedInUser, String projectLabel, String sourceType, String sourceLabel) {
        ensureOrgAdmin(loggedInUser);
        Type type = Type.lookupByLabel(sourceType);
        try {
            contentManager.detachSource(projectLabel, type, sourceLabel, loggedInUser);
            return 1;
        }
        catch (EntityNotExistsException e) {
            throw new EntityNotExistsFaultException(e);
        }
    }

    /**
     * List {@link ContentFilter}s
     *
     * @param loggedInUser the logged in user
     * @return the list of {@link ContentFilter}s
     *
     * @xmlrpc.doc List all Content Filters visible to given user
     * @xmlrpc.param #session_key()
     * @xmlrpc.returntype
     * #array_begin()
     * $ContentFilterSerializer
     * #array_end()
     */
    public List<ContentFilter> listFilters(User loggedInUser) {
        return ContentManager.listFilters(loggedInUser);
    }

    /**
     * Lookup {@link ContentFilter} by id
     *
     * @param loggedInUser the logged in user
     * @param id the filter id
     * @throws EntityNotExistsFaultException if filter is not found
     * @return the matching {@link ContentFilter}
     *
     * @xmlrpc.doc Lookup a Content Filter by id
     * @xmlrpc.param #session_key()
     * @xmlrpc.param #param_desc("int", "id", "Filter id")
     * @xmlrpc.returntype $ContentFilterSerializer
     */
    public ContentFilter lookupFilter(User loggedInUser, Integer id) {
        return ContentManager.lookupFilterById(id.longValue(), loggedInUser)
                .orElseThrow(() -> new EntityNotExistsFaultException(id));
    }

    /**
     * Returns a list of available filter criteria
     *
     * @param loggedInUser the user
     * @return list of filter criteria
     *
     * @xmlrpc.doc List of available filter criteria
     * @xmlrpc.param #session_key()
     * @xmlrpc.returntype
     * #array_begin()
     * #struct_begin("Filter Criteria")
     * #prop("string", "type")
     * #prop("string", "matcher")
     * #prop("string", "field")
     * #struct_end()
     * #array_end()
     */
    public List<Map<String, String>> listFilterCriteria(User loggedInUser) {
        return FilterCriteria.listFilterCriteria();
    }

    /**
     * Create a {@link ContentFilter}
     *
     * @param loggedInUser the logged in user
     * @param name the Filter name
     * @param rule the Filter rule
     * @param entityType the Filter entity type
     * @param criteria the filter criteria
     * @throws InvalidArgsException when invalid criteria are passed
     * @return the created {@link ContentFilter}
     *
     * @xmlrpc.doc Create a Content Filter
     * #paragraph_end()
     * #paragraph()
     * The following filters are available (you can get the list in machine-readable format using
     * the listFilterCriteria() endpoint):
     * #paragraph_end()
     * #paragraph()
     * Package filtering:
     * #itemlist()
     *   #item("by name - field: name; matchers: contains or matches")
     *   #item("by name, epoch, version, release and architecture - field: nevr or nevra; matcher: equals")
     *  #itemlist_end()
     * #paragraph_end()
     * #paragraph()
     * Errata/Patch filtering:
     * #itemlist()
     *   #item("by advisory name - field: advisory_name; matcher: equals or matches")
     *   #item("by type - field: advisory_type (e.g. 'Security Advisory'); matcher: equals")
     *   #item("by synopsis - field: synopsis; matcher: equals, contains or matches")
     *   #item("by keyword - field: keyword; matcher: contains")
     *   #item("by date - field: issue_date; matcher: greater or greatereq")
     *   #item("by affected package name - field: package_name; matcher: contains_pkg_name or matches_pkg_name")
     *   #item("by affected package with version - field: package_nevr; matcher: contains_pkg_lt_evr,
     *   contains_pkg_le_evr, contains_pkg_eq_evr, contains_pkg_ge_evr or contains_pkg_gt_evr")
     * #itemlist_end()
     * #paragraph_end()
     * #paragraph()
     * Appstream module/stream filtering:
     * #itemlist()
     *   #item("by module name, stream - field: module_stream; matcher: equals; value: modulaneme:stream")
     * #itemlist_end()
     * Note: Only 'allow' rule is supported for appstream filters.
     * #paragraph_end()
     * #paragraph()
     *
     * Note: The 'matches' matcher works on Java regular expressions.
     *
     * @xmlrpc.param #session_key()
     * @xmlrpc.param #param_desc("string", "name", "Filter name")
     * @xmlrpc.param #param_desc("string", "rule", "Filter rule ('deny' or 'allow')")
     * @xmlrpc.param #param_desc("string", "entityType", "Filter entityType ('package' or 'erratum')")
     * @xmlrpc.param
     *  #struct_begin("criteria")
     *      #prop_desc("string", "matcher", "The matcher type of the filter (e.g. 'contains')")
     *      #prop_desc("string", "field", "The entity field to match (e.g. 'name'")
     *      #prop_desc("string", "value", "The field value to match (e.g. 'kernel')")
     *  #struct_end()
     * @xmlrpc.returntype $ContentFilterSerializer
     */
    public ContentFilter createFilter(User loggedInUser, String name, String rule, String entityType,
            Map<String, Object> criteria) {
        ensureOrgAdmin(loggedInUser);

        ContentFilter.Rule ruleObj = ContentFilter.Rule.lookupByLabel(rule);
        ContentFilter.EntityType entityTypeObj = ContentFilter.EntityType.lookupByLabel(entityType);
        FilterCriteria criteriaObj = createCriteria(criteria).orElseThrow(
                () -> new InvalidArgsException("criteria must be specified")
        );

        try {
            return contentManager.createFilter(name, ruleObj, entityTypeObj, criteriaObj, loggedInUser);
        }
        catch (IllegalArgumentException e) {
            throw new InvalidArgsException(e.getMessage());
        }
    }

    /**
     * Create new {@link ContentFilter}s for all AppStream modules with default streams
     *
     * @param loggedInUser the logged in user
     * @param prefix the filter name prefix
     * @param channelLabel label of the modular channel
     * @param projectLabel label of the Content Lifecycle Project
     * @throws EntityExistsFaultException when Filter already exist
     * @return List of created and successfully attached Filter
     *
     * @xmlrpc.doc Create Filters for AppStream Modular Channel and attach them to CLM Project
     * @xmlrpc.param #session_key()
     * @xmlrpc.param #param_desc("string", "prefix", "Filter name prefix")
     * @xmlrpc.param #param_desc("string", "channelLabel", "Modular Channel label")
     * @xmlrpc.param #param_desc("string", "projectLabel", "Project label")
     * @xmlrpc.returntype #array_begin() $ContentFilterSerializer #array_end()
     */
    public List<ContentFilter> createAppStreamFilters(User loggedInUser, String prefix,
            String channelLabel, String projectLabel) throws ModulemdApiException {
        ensureOrgAdmin(loggedInUser);

        try {
            Channel channel = ChannelManager.lookupByLabelAndUser(channelLabel, loggedInUser);

            List<ContentFilter> createdFilters = filterTemplateManager.createAppStreamFilters(
                    prefix, channel, loggedInUser);

            List<ContentFilter> attachedFilters = new ArrayList<>();

            for (ContentFilter createdFilter : createdFilters) {
                attachedFilters.add(contentManager.attachFilter(projectLabel, createdFilter.getId(), loggedInUser));
            }

            return attachedFilters;
        }
        catch (EntityExistsException e) {
            throw new EntityExistsFaultException(e);
        }
        catch (LookupException | EntityNotExistsException e) {
            throw new EntityNotExistsFaultException(e);
        }
    }

    /**
     * Update a {@link ContentFilter}
     *
     * @param loggedInUser the logged in user
     * @param filterId the Filter id
     * @param name the Filter name
     * @param rule the Filter rule
     * @param criteria the filter criteria
     * @throws EntityNotExistsFaultException when Filter is not found
     * @return the updated {@link ContentFilter}
     *
     * @xmlrpc.doc Update a Content Filter
     * #paragraph_end()
     * #paragraph()
     * See also: createFilter(), listFilterCriteria()
     * @xmlrpc.param #session_key()
     * @xmlrpc.param #param_desc("int", "filterId", "Filter id")
     * @xmlrpc.param #param_desc("string", "name", "New filter name")
     * @xmlrpc.param #param_desc("string", "rule", "New filter rule ('deny' or 'allow')")
     * @xmlrpc.param
     *  #struct_begin("criteria")
     *      #prop_desc("string", "matcher", "The matcher type of the filter (e.g. 'contains')")
     *      #prop_desc("string", "field", "The entity field to match (e.g. 'name'")
     *      #prop_desc("string", "value", "The field value to match (e.g. 'kernel')")
     *  #struct_end()
     * @xmlrpc.returntype $ContentFilterSerializer
     */
    public ContentFilter updateFilter(User loggedInUser, Integer filterId, String name, String rule,
            Map<String, Object> criteria) {
        ensureOrgAdmin(loggedInUser);

        Optional<ContentFilter.Rule> ruleObj;
        if (rule.isEmpty()) {
            ruleObj = empty();
        }
        else {
            ruleObj = Optional.of(ContentFilter.Rule.lookupByLabel(rule));
        }
        Optional<FilterCriteria> criteriaObj = createCriteria(criteria);

        try {
            return contentManager.updateFilter(
                    filterId.longValue(),
                    ofNullable(name),
                    ruleObj,
                    criteriaObj,
                    loggedInUser);
        }
        catch (EntityNotExistsException e) {
            throw new EntityNotExistsFaultException(e);
        }
        catch (IllegalArgumentException e) {
            throw new InvalidArgsException(e.getMessage());
        }
    }

    private Optional<FilterCriteria> createCriteria(Map<String, Object> criteria) {
        if (criteria.isEmpty()) {
            return empty();
        }
        if (!criteria.containsKey("matcher") || !criteria.containsKey("field") ||
                !criteria.containsKey("value")) {
            throw new InvalidArgsException("Incomplete filter criteria");
        }
        return of(new FilterCriteria(
                FilterCriteria.Matcher.lookupByLabel((String) criteria.get("matcher")),
                (String) criteria.get("field"),
                (String) criteria.get("value")));
    }

    /**
     * Remove a {@link ContentFilter}
     *
     * @param loggedInUser the logged in user
     * @param filterId the filter id
     * @throws EntityNotExistsFaultException when Filter does not exist
     * @return 1 on success
     *
     * @xmlrpc.doc Remove a Content Filter
     * @xmlrpc.param #session_key()
     * @xmlrpc.param #param_desc("int", "id", "Filter id")
     * @xmlrpc.returntype #return_int_success()
     */
    public int removeFilter(User loggedInUser, Integer filterId) {
        ensureOrgAdmin(loggedInUser);
        try {
            contentManager.removeFilter(filterId.longValue(), loggedInUser);
            return 1;
        }
        catch (EntityNotExistsException e) {
            throw new EntityNotExistsFaultException(e);
        }
    }

    /**
     * List {@link ContentProject} filters
     *
     * @param loggedInUser the logged in user
     * @param projectLabel the Project label
     * @throws EntityNotExistsFaultException when Project is not found
     * @return the list of filters
     *
     * @xmlrpc.doc List all Filters associated with a Project
     * @xmlrpc.param #session_key()
     * @xmlrpc.param #param_desc("string", "projectLabel", "Project label")
     * @xmlrpc.returntype
     * #array_begin()
     * $ContentProjectFilterSerializer
     * #array_end()
     */
    public List<ContentProjectFilter> listProjectFilters(User loggedInUser, String projectLabel) {
        try {
            return lookupProject(loggedInUser, projectLabel).getProjectFilters();
        }
        catch (EntityNotExistsException e) {
            throw new EntityNotExistsFaultException(e);
        }
    }

    /**
     * Attach a {@link ContentFilter} to a {@link ContentProject}
     *
     * @param loggedInUser the logged in user
     * @param projectLabel the Project label
     * @param filterId the Filter id to attach
     * @throws EntityNotExistsException if the Project/Filter does not exist
     * @return the attached Filter
     *
     * @xmlrpc.doc Attach a Filter to a Project
     * @xmlrpc.param #session_key()
     * @xmlrpc.param #param_desc("string", "projectLabel", "Project label")
     * @xmlrpc.param #param_desc("int", "id", "Filter id to attach")
     * @xmlrpc.returntype $ContentFilterSerializer
     */
    public ContentFilter attachFilter(User loggedInUser, String projectLabel, Integer filterId) {
        ensureOrgAdmin(loggedInUser);
        try {
            return contentManager.attachFilter(projectLabel, filterId.longValue(), loggedInUser);
        }
        catch (EntityNotExistsException e) {
            throw new EntityExistsFaultException(e);
        }

    }

    /**
     * Detach a {@link ContentFilter} from a {@link ContentProject}
     *
     * @param loggedInUser the logged in user
     * @param projectLabel the Project label
     * @param filterId the Filter id to detach
     * @throws EntityNotExistsException if the Project/Filter does not exist
     * @return 1 on success
     *
     * @xmlrpc.doc Detach a Filter from a Project
     * @xmlrpc.param #session_key()
     * @xmlrpc.param #param_desc("string", "projectLabel", "Project label")
     * @xmlrpc.param #param_desc("int", "id", "Filter id to detach")
     * @xmlrpc.returntype #return_int_success()
     */
    public int detachFilter(User loggedInUser, String projectLabel, Integer filterId) {
        ensureOrgAdmin(loggedInUser);
        try {
            contentManager.detachFilter(projectLabel, filterId.longValue(), loggedInUser);
        }
        catch (EntityNotExistsException e) {
            throw new EntityExistsFaultException(e);
        }
        return 1;
    }

    /**
     * Build a Project
     *
     * @param loggedInUser the user
     * @param projectLabel the Project label
     * @throws EntityNotExistsFaultException when Project does not exist
     * @throws ContentManagementFaultException on Content Management-related error
     * @return 1 if successful
     *
     * @xmlrpc.doc Build a Project
     * @xmlrpc.param #session_key()
     * @xmlrpc.param #param_desc("string", "projectLabel" "Project label")
     * @xmlrpc.returntype #return_int_success()
     */
    public int buildProject(User loggedInUser, String projectLabel) {
        ensureOrgAdmin(loggedInUser);

        // Validate the project for build
        ContentProject project = lookupProject(loggedInUser, projectLabel);
        validateContentProject(project);

        try {
            contentManager.buildProject(project, empty(), true, loggedInUser);
        }
        catch (ContentManagementException e) {
            throw new ContentManagementFaultException(e);
        }
        return 1;
    }

    /**
     * Build a Project
     *
     * @param loggedInUser the user
     * @param projectLabel the Project label
     * @param message the log message to be assigned to the build
     * @throws EntityNotExistsFaultException when Project does not exist
     * @throws ContentManagementFaultException on Content Management-related error
     * @return 1 if successful
     *
     * @xmlrpc.doc Build a Project
     * @xmlrpc.param #session_key()
     * @xmlrpc.param #param_desc("string", "projectLabel" "Project label")
     * @xmlrpc.param #param_desc("string", "message" "Log message to be assigned to the build")
     * @xmlrpc.returntype #return_int_success()
     */
    public int buildProject(User loggedInUser, String projectLabel, String message) {
        ensureOrgAdmin(loggedInUser);

        // Validate the project for build
        ContentProject project = lookupProject(loggedInUser, projectLabel);
        validateContentProject(project);

        try {
            contentManager.buildProject(project, of(message), true, loggedInUser);
        }
        catch (ContentManagementException e) {
            throw new ContentManagementFaultException(e);
        }
        return 1;
    }

    /**
     * Promote an Environment in a Project
     *
     * @param loggedInUser the user
     * @param projectLabel the Project label
     * @param envLabel the Environment label
     * @throws EntityNotExistsFaultException when Project does not exist
     * @throws ContentManagementFaultException on Content Management-related error
     * @return 1 if successful
     *
     * @xmlrpc.doc Promote an Environment in a Project
     * @xmlrpc.param #session_key()
     * @xmlrpc.param #param_desc("string", "projectLabel" "Project label")
     * @xmlrpc.param #param_desc("string", "envLabel" "Environment label")
     * @xmlrpc.returntype #return_int_success()
     */
    public int promoteProject(User loggedInUser, String projectLabel, String envLabel) {
        ensureOrgAdmin(loggedInUser);

        // Validate the project for promote
        ContentProject project = lookupProject(loggedInUser, projectLabel);
        validateContentProject(project);

        try {
            contentManager.promoteProject(projectLabel, envLabel, true, loggedInUser);
        }
        catch (EntityNotExistsException e) {
            throw new EntityExistsFaultException(e);
        }
        catch (ContentManagementException e) {
            throw new ContentManagementFaultException(e);
        }
        return 1;
    }

    /**
     * Validates a content project for build/promote
     *
     * The validation fails only in case of an error. Info and warning messages are ignored since the build/promote
     * operation can still be performed.
     *
     * @param project the {@link ContentProject} instance
     * @throws ContentValidationFaultException when validation fails with messages
     */
    private void validateContentProject(ContentProject project) throws ContentValidationFaultException {
        ContentProjectValidator projectValidator = new ContentProjectValidator(project,
                contentManager.getModulemdApi());

        // Join all error messages in a new line
        String validationError = projectValidator.validate().stream()
                .filter(m -> ContentValidationMessage.TYPE_ERROR.equals(m.getType()))
                .map(ContentValidationMessage::getMessage)
                .collect(Collectors.joining(System.lineSeparator()));

        if (StringUtils.isNotEmpty(validationError)) {
            throw new ContentValidationFaultException(validationError);
        }
    }
}
