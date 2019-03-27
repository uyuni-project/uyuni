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

import com.redhat.rhn.domain.contentmgmt.ContentEnvironment;
import com.redhat.rhn.domain.contentmgmt.ContentProject;
import com.redhat.rhn.domain.contentmgmt.ProjectSource;
import com.redhat.rhn.domain.contentmgmt.ProjectSource.Type;
import com.redhat.rhn.domain.user.User;
import com.redhat.rhn.frontend.xmlrpc.BaseHandler;
import com.redhat.rhn.frontend.xmlrpc.EntityExistsFaultException;
import com.redhat.rhn.frontend.xmlrpc.EntityNotExistsFaultException;
import com.redhat.rhn.manager.EntityExistsException;
import com.redhat.rhn.manager.EntityNotExistsException;
import com.redhat.rhn.manager.contentmgmt.ContentManager;

import java.util.List;
import java.util.Map;
import java.util.Optional;

import static com.redhat.rhn.common.util.StringUtil.nullIfEmpty;
import static java.util.Optional.empty;
import static java.util.Optional.of;
import static java.util.Optional.ofNullable;

/**
 * Content Management XMLRPC handler
 */
public class ContentManagementHandler extends BaseHandler {

    /**
     * List Content Projects visible to user
     *
     * @param loggedInUser - the logged in user
     * @return the list of Content Projects visible to user
     *
     * @xmlrpc.doc Look up Content Project with given label
     * @xmlrpc.param #session_key()
     * @xmlrpc.returntype
     * #array()
     * $ContentProjectSerializer
     * #array_end()
     */
    public List<ContentProject> listProjects(User loggedInUser) {
        return ContentManager.listProjects(loggedInUser);
    }

    /**
     * Look up Content Project
     *
     * @param loggedInUser - the logged in user
     * @param label - the label
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
     * @param loggedInUser - the logged in user
     * @param label - the label
     * @param name - the name
     * @param description - the description
     * @throws EntityExistsFaultException when Project already exists
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
            return ContentManager.createProject(label, name, description, loggedInUser);
        }
        catch (EntityExistsException e) {
            throw new EntityExistsFaultException(e);
        }
    }

    /**
     * Update Content Project
     *
     * @param loggedInUser - the logged in user
     * @param label - the new label
     * @param props - the map with the Content Project properties
     * @throws EntityNotExistsFaultException when Project does not exist
     * @return the updated Content Project
     *
     * @xmlrpc.doc Update Content Project with given label
     * @xmlrpc.param #session_key()
     * @xmlrpc.param #param_desc("string", "label", "Content Project label")
     * @xmlrpc.param
     *  #struct("data")
     *      #prop_desc("string", "name", "Content Project name")
     *      #prop_desc("string", "description", ""Content Project description")
     *  #struct_end()
     * @xmlrpc.returntype $ContentProjectSerializer
     */
    public ContentProject updateProject(User loggedInUser, String label, Map<String, Object> props) {
        ensureOrgAdmin(loggedInUser);
        try {
            return ContentManager.updateProject(label,
                    ofNullable((String) props.get("name")),
                    ofNullable((String) props.get("description")),
                    loggedInUser);
        }
        catch (EntityNotExistsException e) {
            throw new EntityNotExistsFaultException(e);
        }
    }

    /**
     * Remove Content Project
     *
     * @param loggedInUser - the logged in user
     * @param label - the label
     * @throws EntityNotExistsFaultException when Project does not exist
     * @return the number of removed objects
     *
     * @xmlrpc.doc Remove Content Project
     * @xmlrpc.param #session_key()
     * @xmlrpc.param #param_desc("string", "label", "Content Project label")
     * @xmlrpc.returntype int - the number of removed objects
     */
    public int removeProject(User loggedInUser, String label) {
        ensureOrgAdmin(loggedInUser);
        try {
            return ContentManager.removeProject(label, loggedInUser);
        }
        catch (EntityNotExistsException e) {
            throw new EntityNotExistsFaultException(e);
        }
    }

    /**
     * List Environments in a Content Project with the respect to their ordering
     *
     * @param loggedInUser - the logged in user
     * @param projectLabel - the Content Project label
     * @throws EntityNotExistsFaultException when Project does not exist
     * @return the List of Content Environments with respect to their ordering
     *
     * @xmlrpc.doc List Environments in a Content Project with the respect to their ordering
     * @xmlrpc.param #session_key()
     * @xmlrpc.param #param_desc("string", "projectLabel", "Content Project label")
     * @xmlrpc.returntype
     * #array()
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
     * @param loggedInUser - the logged in user
     * @param projectLabel - the Content Project label
     * @param envLabel - the Content Environment label
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
        return ContentManager.lookupEnvironment(envLabel, projectLabel, loggedInUser)
                .orElseThrow(() -> new EntityNotExistsFaultException(envLabel));
    }

    /**
     * Create a Content Environment and appends it behind given Content Environment
     *
     * @param loggedInUser - the logged in user
     * @param projectLabel - the Content Project label
     * @param predecessorLabel - the Predecessor label
     * @param label - the Content Environment Label
     * @param name - the Content Environment name
     * @param description - the Content Environment description
     * @throws EntityNotExistsFaultException when Project or predecessor Environment does not exist
     * @throws EntityExistsFaultException when Environment with given parameters already exists
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
            return ContentManager.createEnvironment(projectLabel, ofNullable(nullIfEmpty(predecessorLabel)), label,
                    name, description, loggedInUser);
        }
        catch (EntityNotExistsException e) {
            throw new EntityNotExistsFaultException(e);
        }
        catch (EntityExistsException e) {
            throw new EntityExistsFaultException(e);
        }
    }

    /**
     * Update Content Environment
     *
     * @param loggedInUser - the logged in user
     * @param projectLabel - the Content Project label
     * @param envLabel - the Environment label
     * @param props - the map with the Environment properties
     * @throws EntityNotExistsFaultException when Project or predecessor Environment does not exist
     * @return the updated Environment
     *
     * @xmlrpc.doc Update Content Environment with given label
     * @xmlrpc.param #session_key()
     * @xmlrpc.param #param_desc("string", "projectLabel", "Content Project label")
     * @xmlrpc.param #param_desc("string", "envLabel", "Content Environment label")
     * @xmlrpc.param
     *  #struct("data")
     *      #prop_desc("string", "name", "Content Environment name")
     *      #prop_desc("string", "description", "Content Environment description")
     *  #struct_end()
     * @xmlrpc.returntype $ContentEnvironmentSerializer
     */
    public ContentEnvironment updateEnvironment(User loggedInUser, String projectLabel, String envLabel,
            Map<String, Object> props) {
        ensureOrgAdmin(loggedInUser);
        try {
            return ContentManager.updateEnvironment(envLabel,
                    projectLabel,
                    ofNullable((String) props.get("name")),
                    ofNullable((String) props.get("description")),
                    loggedInUser);
        }
        catch (EntityNotExistsException e) {
            throw new EntityNotExistsFaultException(e);
        }
    }

    /**
     * Remove a Content Environment
     *
     * @param loggedInUser - the logged in user
     * @param projectLabel - the Content Project label
     * @param envLabel - the Content Environment label
     * @throws EntityNotExistsFaultException when Project does not exist
     * @return the number of removed objects
     *
     * @xmlrpc.doc Remove a Content Environment
     * @xmlrpc.param #session_key()
     * @xmlrpc.param #param_desc("string", "projectLabel", "Content Project label")
     * @xmlrpc.param #param_desc("string", "envLabel", "Content Environment label")
     * @xmlrpc.returntype int - the number of removed objects
     */
    public int removeEnvironment(User loggedInUser, String projectLabel, String envLabel) {
        ensureOrgAdmin(loggedInUser);
        return ContentManager.removeEnvironment(envLabel, projectLabel, loggedInUser);
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
     * #array()
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
     * @throws EntityNotExistsFaultException if the Project is not found
     * @return list of Project Sources
     *
     * @xmlrpc.doc Look up Content Project Source
     * @xmlrpc.param #session_key()
     * @xmlrpc.param #param_desc("string", "projectLabel", "Content Project label")
     * @xmlrpc.param #param_desc("string", "sourceType", "Project Source type, e.g. 'software'")
     * @xmlrpc.param #param_desc("string", "sourceLabel", "Project Source label")
     * @xmlrpc.returntype $ContentProjectSourceSerializer
     */
    public ProjectSource lookupProjectSource(User loggedInUser, String projectLabel, String sourceType,
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
     * @xmlrpc.param #param_desc("int", "sourcePosition", "Project Source label")
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
            return ContentManager.attachSource(projectLabel, type, sourceLabel, sourcePosition, loggedInUser);
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
     * @return the number of Sources removed
     *
     * @xmlrpc.doc Detach a Source from a Project
     * @xmlrpc.param #session_key()
     * @xmlrpc.param #param_desc("string", "projectLabel", "Content Project label")
     * @xmlrpc.param #param_desc("string", "sourceType", "Project Source type, e.g. 'software'")
     * @xmlrpc.param #param_desc("string", "sourceLabel", "Project Source label")
     * @xmlrpc.returntype int - the number of detached sources
     */
    public int detachSource(User loggedInUser, String projectLabel, String sourceType, String sourceLabel) {
        ensureOrgAdmin(loggedInUser);
        Type type = Type.lookupByLabel(sourceType);
        try {
            return ContentManager.detachSource(projectLabel, type, sourceLabel, loggedInUser);
        }
        catch (EntityNotExistsException e) {
            throw new EntityNotExistsFaultException(e);
        }
    }

    /**
     * Build a Project
     *
     * @param loggedInUser the user
     * @param projectLabel the Project label
     * @return 1 if successful
     *
     * @xmlrpc.doc Build a Project
     * @xmlrpc.param #param_desc("string", "sessionKey", "Session token, issued at login")
     * @xmlrpc.param #param_desc("string", "projectLabel" "Project label")
     * @xmlrpc.returntype #return_int_success()
     */
    public int buildProject(User loggedInUser, String projectLabel) {
        ensureOrgAdmin(loggedInUser);
        ContentManager.buildProject(projectLabel, empty(), true, loggedInUser);
        return 1;
    }

    /**
     * Build a Project
     *
     * @param loggedInUser the user
     * @param message the log message to be assigned to the build
     * @param projectLabel the Project label
     * @return 1 if successful
     *
     * @xmlrpc.doc Build a Project
     * @xmlrpc.param #param_desc("string", "sessionKey", "Session token, issued at login")
     * @xmlrpc.param #param_desc("string", "message" "Log message to be assigned to the build")
     * @xmlrpc.param #param_desc("string", "projectLabel" "Project label")
     * @xmlrpc.returntype #return_int_success()
     */
    public int buildProject(User loggedInUser, String projectLabel, String message) {
        ensureOrgAdmin(loggedInUser);
        ContentManager.buildProject(projectLabel, of(message), true, loggedInUser);
        return 1;
    }
}
