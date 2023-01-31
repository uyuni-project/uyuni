/*
 * Copyright (c) 2023 SUSE LLC
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
package com.redhat.rhn.manager.image;

import com.redhat.rhn.common.security.PermissionException;
import com.redhat.rhn.common.validator.ValidatorException;
import com.redhat.rhn.domain.image.ImageStore;
import com.redhat.rhn.domain.image.ImageStoreFactory;
import com.redhat.rhn.domain.image.ImageSyncFactory;
import com.redhat.rhn.domain.image.ImageSyncProject;
import com.redhat.rhn.domain.image.ImageSyncSource;
import com.redhat.rhn.domain.role.RoleFactory;
import com.redhat.rhn.domain.user.User;
import com.redhat.rhn.manager.EntityExistsException;
import com.redhat.rhn.manager.EntityNotExistsException;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.util.Collections;
import java.util.List;
import java.util.Optional;

/**
 * Image Sync functionality
 */
public class ImageSyncManager {
    private static final Logger LOG = LogManager.getLogger(ImageSyncManager.class);
    private ImageSyncFactory syncFactory;

    /**
     * Standard Constructor
     */
    public ImageSyncManager() {
        syncFactory = new ImageSyncFactory();
    }

    /**
     * Create a ImageSync Project
     * @param name the project name
     * @param destStoreId the id of the destination image store
     * @param scoped should the images be stored with a scope
     * @param user the user
     * @return the new {@link ImageSyncProject}
     */
    public ImageSyncProject createProject(String name, Long destStoreId, Boolean scoped, User user) {
        ensureImageAdmin(user);
        Optional<ImageStore> optStore = ImageStoreFactory.lookupByIdAndOrg(destStoreId, user.getOrg());
        if (optStore.isEmpty()) {
            throw new EntityNotExistsException(ImageStore.class, destStoreId);
        }
        ImageStore store = optStore.filter(s -> s.getStoreType().equals(ImageStoreFactory.TYPE_REGISTRY))
                .orElseThrow(() -> new ValidatorException("Image Store is not a container registry"));
        syncFactory.lookupProjectByNameAndUser(name, user).ifPresent(p -> {
            throw new EntityExistsException(p);
        });
        ImageSyncProject prj = new ImageSyncProject(name, user.getOrg(), store, scoped);
        syncFactory.save(prj);
        return prj;
    }

    /**
     * Create a ImageSync Project
     * @param name the project name
     * @param destStoreLabel the label of the destination image store
     * @param scoped should the images be stored with a scope
     * @param user the user
     * @return the new {@link ImageSyncProject}
     */
    public ImageSyncProject createProject(String name, String destStoreLabel, Boolean scoped, User user) {
        ensureImageAdmin(user);
        ImageStore store = ImageStoreFactory.lookupBylabelAndOrg(destStoreLabel, user.getOrg())
                .orElseThrow(() -> new EntityNotExistsException(ImageStore.class, destStoreLabel));
        return createProject(name, store.getId(), scoped, user);
    }

    /**
     * List Image Sync Projects for the given user
     * @param user the user
     * @return list of {@link ImageSyncProject}
     */
    public List<ImageSyncProject> listProjects(User user) {
        ensureImageAdmin(user);
        return syncFactory.listProjectsByUser(user);
    }

    /**
     * Lookup a project by name and uer
     * @param name the project name
     * @param user the user
     * @return Optional of {@link ImageSyncProject}
     */
    public Optional<ImageSyncProject> lookupProject(String name, User user) {
        ensureImageAdmin(user);
        return syncFactory.lookupProjectByNameAndUser(name, user);
    }

    /**
     * Lookup a project by name and uer
     * @param id the project id
     * @param user the user
     * @return Optional of {@link ImageSyncProject}
     */
    public Optional<ImageSyncProject> lookupProject(Long id, User user) {
        ensureImageAdmin(user);
        return syncFactory.lookupProjectByIdAndUser(id, user);
    }

    /**
     * Create an Image Sync Source in a given project
     * @param projectId the project
     * @param storeId the source store id
     * @param repository the repository
     * @param tags exact tag list to sync
     * @param tagsRegex regular expression for tags to sync
     * @param user the user
     * @return the image sync source
     */
    public ImageSyncSource createSource(Long projectId, Long storeId, String repository, List<String> tags,
                                        String tagsRegex, User user) {
        ensureImageAdmin(user);
        Optional<ImageStore> optStore = ImageStoreFactory.lookupByIdAndOrg(storeId, user.getOrg());
        if (optStore.isEmpty()) {
            throw new EntityNotExistsException(ImageStore.class, storeId);
        }
        ImageStore store = optStore.filter(s -> s.getStoreType().equals(ImageStoreFactory.TYPE_REGISTRY))
                .orElseThrow(() -> new ValidatorException("Image Store is not a container registry"));
        Optional<ImageSyncProject> optProject = syncFactory.lookupProjectByIdAndUser(projectId, user);
        ImageSyncProject project = optProject
                .orElseThrow(() -> new EntityNotExistsException(ImageSyncProject.class, projectId));

        ImageSyncSource src = new ImageSyncSource(project, user.getOrg(), store, repository, tags, tagsRegex);
        syncFactory.save(src);
        return src;
    }

    /**
     * Create an Image Sync Source in a given project
     * @param projectId the project
     * @param storeId the source store id
     * @param repository the repository
     * @param tags exact tag list to sync - sync all if empty
     * @param user the user
     * @return the image sync source
     */
    public ImageSyncSource createSource(Long projectId, Long storeId, String repository, List<String> tags, User user) {
        return createSource(projectId, storeId, repository, tags, null, user);
    }

    /**
     * Create an Image Sync Source in a given project
     * @param projectId the project
     * @param storeId the source store id
     * @param repository the repository
     * @param tagsRegex regular expression for tags to sync
     * @param user the user
     * @return the image sync source
     */
    public ImageSyncSource createSource(Long projectId, Long storeId, String repository, String tagsRegex, User user) {
        return createSource(projectId, storeId, repository, Collections.emptyList(), tagsRegex, user);
    }

    /**
     * Create an Image Sync Source in a given project
     * @param projectName the project name
     * @param storeLabel the source store label
     * @param repository the repository
     * @param tags exact tag list to sync
     * @param tagsRegex regular expression for tags to sync
     * @param user the user
     * @return the image sync source
     */
    public ImageSyncSource createSource(String projectName, String storeLabel, String repository, List<String> tags,
                                        String tagsRegex, User user) {
        ensureImageAdmin(user);
        Optional<ImageStore> optStore = ImageStoreFactory.lookupBylabelAndOrg(storeLabel, user.getOrg());
        if (optStore.isEmpty()) {
            throw new EntityNotExistsException(ImageStore.class, storeLabel);
        }
        ImageStore store = optStore.filter(s -> s.getStoreType().equals(ImageStoreFactory.TYPE_REGISTRY))
                .orElseThrow(() -> new ValidatorException("Image Store is not a container registry"));
        Optional<ImageSyncProject> optProject = syncFactory.lookupProjectByNameAndUser(projectName, user);
        ImageSyncProject project = optProject
                .orElseThrow(() -> new EntityNotExistsException(ImageSyncProject.class, projectName));

        ImageSyncSource src = new ImageSyncSource(project, user.getOrg(), store, repository, tags, tagsRegex);
        syncFactory.save(src);
        return src;
    }

    /**
     * Create an Image Sync Source in a given project
     * @param projectName the project name
     * @param storeLabel the source store label
     * @param repository the repository
     * @param tags exact tag list to sync - sync all if empty
     * @param user the user
     * @return the image sync source
     */
    public ImageSyncSource createSource(String projectName, String storeLabel, String repository, List<String> tags, User user) {
        return createSource(projectName, storeLabel, repository, tags, null, user);
    }

    /**
     * Create an Image Sync Source in a given project
     * @param projectName the project name
     * @param storeLabel the source store label
     * @param repository the repository
     * @param tagsRegex regular expression for tags to sync
     * @param user the user
     * @return the image sync source
     */
    public ImageSyncSource createSource(String projectName, String storeLabel, String repository, String tagsRegex, User user) {
        return createSource(projectName, storeLabel, repository, Collections.emptyList(), tagsRegex, user);
    }

    /**
     * Lookup a Image Sync Source by ID and User
     * @param sourceId the source ID
     * @param user the calling user
     * @return optional {@link ImageSyncSource}
     */
    public Optional<ImageSyncSource> lookupSourceByIdAndUser(Long sourceId, User user) {
        ensureImageAdmin(user);
        return syncFactory.lookupSourceByIdAndUser(sourceId, user);
    }

    /**
     * List all {@link ImageSyncSource} available for the User
     * @param user the user
     * @return list of Image Sync Sources
     */
    public List<ImageSyncSource> listSources(User user) {
        ensureImageAdmin(user);
        return syncFactory.listSources(user);
    }
    /**
     * Update a Image Sync Project
     * @param projectId the project ID
     * @param user the calling user
     * @param newStoreId the new destination image store ID or NULL when it should stay unchanged
     * @param newScope the new Scope or NULL when it should stay unchanged
     * @return
     */
    public ImageSyncProject updateProject(Long projectId, User user, Long newStoreId, Boolean newScope) {
        ensureImageAdmin(user);
        Optional<ImageSyncProject> optProject = syncFactory.lookupProjectByIdAndUser(projectId, user);
        ImageSyncProject project = optProject
                .orElseThrow(() -> new EntityNotExistsException(ImageSyncProject.class, projectId));
        if (newStoreId != null) {
            Optional<ImageStore> optStore = ImageStoreFactory.lookupByIdAndOrg(newStoreId, user.getOrg());
            if (optStore.isEmpty()) {
                throw new EntityNotExistsException(ImageStore.class, newStoreId);
            }
            ImageStore store = optStore.filter(s -> s.getStoreType().equals(ImageStoreFactory.TYPE_REGISTRY))
                    .orElseThrow(() -> new ValidatorException("Image Store is not a container registry"));
            project.setDestinationImageStore(store);
        }
        if (newScope != null) {
            project.setScoped(newScope);
        }
        syncFactory.save(project);
        return project;
    }

    /**
     * Update an Image Sync Source
     * @param sourceId the source ID
     * @param user the calling user
     * @param newStoreId the new Image Store ID or NULL when it should be unchanged
     * @param newRepository the new Repository or NULL when it should be unchanged
     * @param newTags the new exact tag list or NULL when it should be unchanged
     * @param newTagsRegex the new tag regular expression or NULL when it should be unchanged
     * @return the new Image Sync Source
     */
    public ImageSyncSource updateSource(Long sourceId, User user, Long newStoreId, String newRepository,
                                        List<String> newTags, String newTagsRegex) {
        ensureImageAdmin(user);
        Optional<ImageSyncSource> optSource = syncFactory.lookupSourceByIdAndUser(sourceId, user);
        ImageSyncSource src = optSource
                .orElseThrow(() -> new EntityNotExistsException(ImageSyncSource.class, sourceId));
        if (newStoreId != null) {
            ImageStore store = ImageStoreFactory.lookupByIdAndOrg(newStoreId, user.getOrg())
                    .orElseThrow(() -> new EntityNotExistsException(ImageStore.class, newStoreId));
            src.setSrcStore(store);
        }
        if (newRepository != null) {
            src.setSrcRepository(newRepository);
        }
        if (newTags != null) {
            src.setSrcTags(newTags);
        }
        if (newTagsRegex != null) {
            src.setSrcTagsRegexp(newTagsRegex);
        }
        syncFactory.save(src);
        return src;
    }

    /**
     * Delete a Image Sync Project by id and user
     * @param projectId the project ID
     * @param user the calling user
     */
    public void deleteProject(Long projectId, User user) {
        ensureImageAdmin(user);
        Optional<ImageSyncProject> optProject = syncFactory.lookupProjectByIdAndUser(projectId, user);
        ImageSyncProject project = optProject
                .orElseThrow(() -> new EntityNotExistsException(ImageSyncProject.class, projectId));
        syncFactory.remove(project);
    }

    /**
     * Delete a Image Sync Project by name and user
     * @param projectName the project name
     * @param user the calling user
     */
    public void deleteProject(String projectName, User user) {
        ensureImageAdmin(user);
        Optional<ImageSyncProject> optProject = syncFactory.lookupProjectByNameAndUser(projectName, user);
        ImageSyncProject project = optProject
                .orElseThrow(() -> new EntityNotExistsException(ImageSyncProject.class, projectName));
        syncFactory.remove(project);
    }

    /**
     * Delete an Image Sync Source by ID and user
     * @param sourceId the source id
     * @param user the calling user
     */
    public void deleteSource(Long sourceId, User user) {
        ensureImageAdmin(user);
        Optional<ImageSyncSource> optSource = syncFactory.lookupSourceByIdAndUser(sourceId, user);
        ImageSyncSource src = optSource
                .orElseThrow(() -> new EntityNotExistsException(ImageSyncSource.class, sourceId));
        syncFactory.remove(src);
    }

    /**
     * Ensures that given user has the Image admin role
     *
     * @param user the user
     * @throws com.redhat.rhn.common.security.PermissionException if the user does not have Image admin role
     */
    private static void ensureImageAdmin(User user) {
        if (!user.hasRole(RoleFactory.IMAGE_ADMIN)) {
            throw new PermissionException(RoleFactory.IMAGE_ADMIN);
        }
    }

}
