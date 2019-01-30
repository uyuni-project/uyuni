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

package com.redhat.rhn.domain.contentmgmt;

import com.redhat.rhn.common.security.PermissionException;
import com.redhat.rhn.domain.user.User;

import java.util.List;
import java.util.Optional;

import static com.redhat.rhn.domain.role.RoleFactory.ORG_ADMIN;

/**
 * Content Management functionality
 */
public class ContentManager {

    // forbid instantiation
    private ContentManager() { }

    /**
     * Create a Content Project
     *
     * @param label - the label
     * @param name - the name
     * @param description - the description
     * @param user - the creator
     * @throws ContentManagementException if a project with given label already exists
     * @throws PermissionException if given user does not have required role
     * @return the created Content Project
     */
    public static ContentProject createProject(String label, String name, String description, User user) {
        ensureOrgAdmin(user);
        lookupProject(label, user).ifPresent(cp -> {
            throw new ContentManagementException("Content Project with label " + label + " already exists");
        });
        ContentProject contentProject = new ContentProject(label, name, description, user.getOrg());
        ContentProjectFactory.save(contentProject);
        return contentProject;
    }

    /**
     * List Projects visible to the user
     *
     * @param user the user
     * @return the Projects visible to the user
     */
    public static List<ContentProject> listProjects(User user) {
        return ContentProjectFactory.listProjects(user.getOrg());
    }

    /**
     * Look up Content Project by label
     *
     * @param label - the label
     * @param user - the user
     * @return Optional with matching Content Project
     */
    public static Optional<ContentProject> lookupProject(String label, User user) {
        return ContentProjectFactory.lookupProjectByLabelAndOrg(label, user.getOrg());
    }

    /**
     * Update Content Project
     *
     * @param label - the label for lookup
     * @param newName - new name
     * @param newDesc - new description
     * @param user - the user
     * @throws ContentManagementException - if Content Project with given label is not found
     * @throws PermissionException if given user does not have required role
     * @return the updated Content Project
     */
    public static ContentProject updateProject(String label, Optional<String> newName, Optional<String> newDesc,
            User user) {
        ensureOrgAdmin(user);
        return lookupProject(label, user)
                .map(cp -> {
                    newName.ifPresent(name -> cp.setName(name));
                    newDesc.ifPresent(desc -> cp.setDescription(desc));
                    return cp;
                })
                .orElseThrow(() ->
                        new ContentManagementException("Content Project with label " + label + " not found."));
    }

    /**
     * Remove Content Project
     *
     * @param label - the label
     * @param user - the user
     * @throws PermissionException if given user does not have required role
     * @return the number of objects affected
     */
    public static int removeProject(String label, User user) {
        ensureOrgAdmin(user);
        return lookupProject(label, user)
                .map(cp -> ContentProjectFactory.remove(cp))
                .orElse(0);
    }

    /**
     * Create Content Environment
     *
     * @param projectLabel - the Content Project label
     * @param predecessorLabel - the predecessor Environment label
     * @param label - the Environment label
     * @param name - the Environment name
     * @param description - the Environment description
     * @param user - the user performing the action
     * @throws ContentManagementException - if Content Project with given label or Content Environment in the Project
     * is not found
     * @throws PermissionException if given user does not have required role
     * @return the created Content Environment
     */
    public static ContentEnvironment createEnvironment(String projectLabel, Optional<String> predecessorLabel,
            String label, String name, String description, User user) {
        ensureOrgAdmin(user);
        return lookupProject(projectLabel, user)
                .map(cp -> {
                    ContentEnvironment newEnv = new ContentEnvironment(label, name, description, cp);
                    Optional<ContentEnvironment> predecessor = predecessorLabel.map(pl ->
                            ContentProjectFactory.lookupEnvironmentByLabelAndProject(pl, cp)
                                    .orElseThrow(() -> new ContentManagementException("Environment " + pl +
                                            " in Project " + label + " not found.")));
                    ContentProjectFactory.insertEnvironment(newEnv, predecessor);
                    return newEnv;
                }).orElseThrow(() ->
                        new ContentManagementException("Content Project with label " + label + " not found."));
    }

    /**
     * List Environments in a Content Project with the respect to their ordering
     *
     * @param projectLabel - the Content Project label
     * @param user - the user
     * @throws ContentManagementException - if Content Project with given label is not found
     * @return the List of Content Environments with respect to their ordering
     */
    public static List<ContentEnvironment> listProjectEnvironments(String projectLabel, User user) {
        return lookupProject(projectLabel, user)
                .map(cp -> ContentProjectFactory.listProjectEnvironments(cp))
                .orElseThrow(() -> new ContentManagementException("Content Project with label " + projectLabel +
                        " not found."));
    }

    /**
     * Look up Content Environment based on its label, Content Project label and Org
     *
     * @param envLabel - the Content Environment label
     * @param contentLabel - the Content Project label
     * @param user - the user
     * @return the optional of matching Content Environment
     */
    public static Optional<ContentEnvironment> lookupEnvironment(String envLabel, String contentLabel, User user) {
        return lookupProject(contentLabel, user)
                .flatMap(cp -> ContentProjectFactory.lookupEnvironmentByLabelAndProject(envLabel, cp));
    }

    /**
     * Update Content Environment
     *
     * @param envLabel - the Environment label
     * @param projectLabel - the Content Project label
     * @param newName - new name
     * @param newDescription - new description
     * @param user - the user
     * @throws ContentManagementException - if the Environment is not found
     * @throws PermissionException if given user does not have required role
     * @return the updated Environment
     */
    public static ContentEnvironment updateEnvironment(String envLabel, String projectLabel, Optional<String> newName,
            Optional<String> newDescription, User user) {
        ensureOrgAdmin(user);
        return lookupEnvironment(envLabel, projectLabel, user)
                .map(env -> {
                    newName.ifPresent(name -> env.setName(name));
                    newDescription.ifPresent(desc -> env.setDescription(desc));
                    return env;
                })
                .orElseThrow(() -> new ContentManagementException("Environment with label " + envLabel +
                        " in Project " + projectLabel + " not found."));
    }


    /**
     * Remove a Content Environment
     *
     * @param envLabel - the Content Environment label
     * @param projectLabel - the Content Project label
     * @param user - the user
     * @throws PermissionException if given user does not have required role
     * @return number of deleted objects
     */
    public static int removeEnvironment(String envLabel, String projectLabel, User user) {
        ensureOrgAdmin(user);
        return lookupEnvironment(envLabel, projectLabel, user)
                .map((env) -> {
                    ContentProjectFactory.removeEnvironment(env);
                    return 1;
                })
                .orElse(0);
    }

    /**
     * Ensures that given user has the Org admin role
     *
     * @param user the user
     * @throws com.redhat.rhn.common.security.PermissionException if the user does not have Org admin role
     */
    private static void ensureOrgAdmin(User user) {
        if (!user.hasRole(ORG_ADMIN)) {
            throw new PermissionException(ORG_ADMIN);
        }
    }
}
