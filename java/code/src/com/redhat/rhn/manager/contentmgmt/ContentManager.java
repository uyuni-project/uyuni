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

package com.redhat.rhn.manager.contentmgmt;

import com.redhat.rhn.common.hibernate.LookupException;
import com.redhat.rhn.common.security.PermissionException;
import com.redhat.rhn.domain.channel.Channel;
import com.redhat.rhn.domain.contentmgmt.ContentEnvironment;
import com.redhat.rhn.domain.contentmgmt.ContentProject;
import com.redhat.rhn.domain.contentmgmt.ContentProjectFactory;
import com.redhat.rhn.domain.contentmgmt.ProjectSource;
import com.redhat.rhn.domain.contentmgmt.ProjectSource.Type;
import com.redhat.rhn.domain.contentmgmt.SoftwareProjectSource;
import com.redhat.rhn.domain.user.User;
import com.redhat.rhn.manager.EntityExistsException;
import com.redhat.rhn.manager.EntityNotExistsException;
import com.redhat.rhn.manager.channel.ChannelManager;

import java.util.List;
import java.util.Optional;

import static com.redhat.rhn.domain.contentmgmt.ProjectSource.Type.SW_CHANNEL;
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
     * @throws EntityExistsException if a project with given label already exists
     * @throws PermissionException if given user does not have required role
     * @return the created Content Project
     */
    public static ContentProject createProject(String label, String name, String description, User user) {
        ensureOrgAdmin(user);
        lookupProject(label, user).ifPresent(cp -> {
            throw new EntityExistsException(cp);
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
     * @throws EntityNotExistsException - if Content Project with given label is not found
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
                .orElseThrow(() -> new EntityNotExistsException(label));
    }

    /**
     * Remove Content Project
     *
     * @param label - the label
     * @param user - the user
     * @throws PermissionException if given user does not have required role
     * @throws EntityNotExistsException - if Content Project with given label is not found
     * @return the number of objects affected
     */
    public static int removeProject(String label, User user) {
        ensureOrgAdmin(user);
        return lookupProject(label, user)
                .map(cp -> ContentProjectFactory.remove(cp))
                .orElseThrow(() -> new EntityNotExistsException(label));
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
     * @throws EntityNotExistsException - if Content Project with given label or Content Environment in the Project
     * is not found
     * @throws EntityExistsException - if Environment with given parameters already exists
     * @throws PermissionException if given user does not have required role
     * @return the created Content Environment
     */
    public static ContentEnvironment createEnvironment(String projectLabel, Optional<String> predecessorLabel,
            String label, String name, String description, User user) {
        ensureOrgAdmin(user);
        lookupEnvironment(label, projectLabel, user).ifPresent(e -> { throw new EntityExistsException(e); });
        return lookupProject(projectLabel, user)
                .map(cp -> {
                    ContentEnvironment newEnv = new ContentEnvironment(label, name, description, cp);
                    Optional<ContentEnvironment> predecessor = predecessorLabel.map(pl ->
                            ContentProjectFactory.lookupEnvironmentByLabelAndProject(pl, cp)
                                    .orElseThrow(() -> new EntityNotExistsException(ContentEnvironment.class, label)));
                    ContentProjectFactory.insertEnvironment(newEnv, predecessor);
                    return newEnv;
                }).orElseThrow(() -> new EntityNotExistsException(ContentProject.class, projectLabel));
    }

    /**
     * List Environments in a Content Project with the respect to their ordering
     *
     * @param projectLabel - the Content Project label
     * @param user - the user
     * @throws EntityNotExistsException - if Content Project with given label is not found
     * @return the List of Content Environments with respect to their ordering
     */
    public static List<ContentEnvironment> listProjectEnvironments(String projectLabel, User user) {
        return lookupProject(projectLabel, user)
                .map(cp -> ContentProjectFactory.listProjectEnvironments(cp))
                .orElseThrow(() -> new EntityNotExistsException(projectLabel));
    }

    /**
     * Look up Content Environment based on its label, Content Project label and User
     *
     * @param envLabel - the Content Environment label
     * @param projectLabel - the Content Project label
     * @param user - the user
     * @throws EntityNotExistsException - if Content Project with given label is not found
     * @return the optional of matching Content Environment
     */
    public static Optional<ContentEnvironment> lookupEnvironment(String envLabel, String projectLabel, User user) {
        return lookupProject(projectLabel, user)
                .map(cp -> ContentProjectFactory.lookupEnvironmentByLabelAndProject(envLabel, cp))
                .orElseThrow(() -> new EntityNotExistsException(projectLabel));
    }

    /**
     * Update Content Environment
     *
     * @param envLabel - the Environment label
     * @param projectLabel - the Content Project label
     * @param newName - new name
     * @param newDescription - new description
     * @param user - the user
     * @throws EntityNotExistsException - if Project or Environment is not found
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
                .orElseThrow(() -> new EntityNotExistsException(envLabel));
    }

    /**
     * Remove a Content Environment
     *
     * @param envLabel - the Content Environment label
     * @param projectLabel - the Content Project label
     * @param user - the user
     * @throws PermissionException if given user does not have required role
     * @throws EntityNotExistsException - if Project or Environment is not found
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
                .orElseThrow(() -> new EntityNotExistsException(envLabel));
    }

    /**
     * Create and attach a Source to given Project.
     * If the Source is already attached to the Project, this is a no-op.
     *
     * @param projectLabel - the Project label
     * @param sourceType - the Source Type (e.g. SW_CHANNEL)
     * @param sourceLabel - the Source label (e.g. SoftwareChannel label)
     * @param position - the position of the Source (Optional)
     * @param user the user
     * @throws EntityNotExistsException when either the Project or the Source is not found
     * @throws EntityExistsException when Source with given parameters is already attached
     * @throws java.lang.IllegalArgumentException if the sourceType is unsupported
     * @return the created or existing Source
     */
    public static ProjectSource attachSource(String projectLabel, Type sourceType, String sourceLabel,
            Optional<Integer> position, User user) {
        ensureOrgAdmin(user);

        lookupProjectSource(projectLabel, sourceType, sourceLabel, user)
                .ifPresent(s -> { throw new EntityExistsException(s); });

        if (sourceType == SW_CHANNEL) {
            ContentProject project = lookupProject(projectLabel, user)
                    .orElseThrow(() -> new EntityNotExistsException(ContentProject.class, projectLabel));
            Channel channel = getChannel(sourceLabel, user);
            SoftwareProjectSource source = new SoftwareProjectSource(project, channel);
            project.addSource(source, position);
            ContentProjectFactory.save(project);
            ContentProjectFactory.save(source);
            return source;
        }
        else {
            throw new IllegalArgumentException("Unsupported source type " + sourceType);
        }
    }

    private static Channel getChannel(String sourceLabel, User user) {
        try {
            return ChannelManager.lookupByLabelAndUser(sourceLabel, user);
        }
        catch (LookupException e) {
            throw new EntityNotExistsException(Channel.class, e);
        }
    }

    /**
     * Detach a Source from given Project
     *
     * @param projectLabel - the Project label
     * @param sourceType - the Source Type (e.g. SW_CHANNEL)
     * @param sourceLabel - the Source label (e.g. SoftwareChannel label)
     * @param user the user
     * @throws EntityNotExistsException when either the Project or the Source is not found
     * @return number of Sources detached
     */
    public static int detachSource(String projectLabel, Type sourceType, String sourceLabel, User user) {
        ensureOrgAdmin(user);
        ProjectSource source = lookupProjectSource(projectLabel, sourceType, sourceLabel, user)
                .orElseThrow(() -> new EntityNotExistsException(ProjectSource.class, sourceLabel));
        source.setState(ProjectSource.State.DETACHED);
        return 1;
    }

    /**
     * Look up Source
     *
     * @param projectLabel the Project label
     * @param sourceType the Source type
     * @param sourceLabel  the Source label
     * @param user the User
     * @throws EntityNotExistsException if Project with given label cannot be found
     * @return Optional with matching Source
     */
    public static Optional<? extends ProjectSource> lookupProjectSource(String projectLabel, Type sourceType,
            String sourceLabel, User user) {
        ContentProject project = lookupProject(projectLabel, user)
                .orElseThrow(() -> new EntityNotExistsException(projectLabel));
        return ContentProjectFactory.lookupProjectSource(project, sourceType, sourceLabel, user);
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
