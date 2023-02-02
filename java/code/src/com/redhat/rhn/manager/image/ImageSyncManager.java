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
import com.redhat.rhn.domain.image.ImageSyncItem;
import com.redhat.rhn.domain.image.ImageSyncProject;
import com.redhat.rhn.domain.role.RoleFactory;
import com.redhat.rhn.domain.user.User;
import com.redhat.rhn.manager.EntityExistsException;
import com.redhat.rhn.manager.EntityNotExistsException;

import com.suse.manager.utils.skopeo.SkopeoCommandManager;
import com.suse.manager.utils.skopeo.beans.ImageTags;
import com.suse.manager.utils.skopeo.beans.RepositoryImageList;

import org.apache.commons.lang3.StringUtils;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

/**
 * Image Sync functionality
 */
public class ImageSyncManager {
    private static final Logger LOG = LogManager.getLogger(ImageSyncManager.class);
    private final ImageSyncFactory syncFactory;

    /**
     * Standard Constructor
     */
    public ImageSyncManager() {
        syncFactory = new ImageSyncFactory();
    }

    /**
     * Create a ImageSync Project
     * @param name the project name
     * @param srcStoreId the id of the source image store
     * @param destStoreId the id of the destination image store
     * @param scoped should the images be stored with a scope
     * @param user the user
     * @return the new {@link ImageSyncProject}
     */
    public ImageSyncProject createProject(String name, Long srcStoreId, Long destStoreId, Boolean scoped, User user) {
        ensureImageAdmin(user);
        ImageStore destStore = ImageStoreFactory.lookupByIdAndOrg(destStoreId, user.getOrg())
                .orElseThrow(() -> new EntityNotExistsException(ImageStore.class, destStoreId));
        if (!destStore.getStoreType().equals(ImageStoreFactory.TYPE_REGISTRY)) {
            throw new ValidatorException("Destination Image Store is not a container registry");
        }
        ImageStore srcStore = ImageStoreFactory.lookupByIdAndOrg(srcStoreId, user.getOrg())
                .orElseThrow(() -> new EntityNotExistsException(ImageStore.class, srcStoreId));
        if (!srcStore.getStoreType().equals(ImageStoreFactory.TYPE_REGISTRY)) {
            throw new ValidatorException("Source Image Store is not a container registry");
        }
        syncFactory.lookupProjectByNameAndUser(name, user).ifPresent(p -> {
            throw new EntityExistsException(p);
        });
        ImageSyncProject prj = new ImageSyncProject(name, user.getOrg(), srcStore, destStore, scoped);
        syncFactory.save(prj);
        return prj;
    }

    /**
     * Create a ImageSync Project
     * @param name the project name
     * @param srcStoreLabel the label of the source image store
     * @param destStoreLabel the label of the destination image store
     * @param scoped should the images be stored with a scope
     * @param user the user
     * @return the new {@link ImageSyncProject}
     */
    public ImageSyncProject createProject(String name, String srcStoreLabel, String destStoreLabel, Boolean scoped,
                                          User user) {
        ensureImageAdmin(user);
        ImageStore destStore = ImageStoreFactory.lookupBylabelAndOrg(destStoreLabel, user.getOrg())
                .orElseThrow(() -> new EntityNotExistsException(ImageStore.class, destStoreLabel));
        ImageStore srcStore = ImageStoreFactory.lookupBylabelAndOrg(srcStoreLabel, user.getOrg())
                .orElseThrow(() -> new EntityNotExistsException(ImageStore.class, srcStoreLabel));
        return createProject(name, srcStore.getId(), destStore.getId(), scoped, user);
    }

    /**
     * List Image Sync Projects for the given user
     *
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
     * Create an Image Sync Item in a given project
     * @param projectId the project
     * @param repository the repository
     * @param tags exact tag list to sync
     * @param tagsRegex regular expression for tags to sync
     * @param user the user
     * @return the image sync item
     */
    public ImageSyncItem createSyncItem(Long projectId, String repository, List<String> tags, String tagsRegex,
                                        User user) {
        ensureImageAdmin(user);
        Optional<ImageSyncProject> optProject = syncFactory.lookupProjectByIdAndUser(projectId, user);
        ImageSyncProject project = optProject
                .orElseThrow(() -> new EntityNotExistsException(ImageSyncProject.class, projectId));

        ImageSyncItem src = new ImageSyncItem(project, user.getOrg(), repository, tags, tagsRegex);
        syncFactory.save(src);
        return src;
    }

    /**
     * Create an Image Sync Item in a given project
     * @param projectId the project
     * @param repository the repository
     * @param tags exact tag list to sync - sync all if empty
     * @param user the user
     * @return the image sync item
     */
    public ImageSyncItem createSyncItem(Long projectId, String repository, List<String> tags, User user) {
        return createSyncItem(projectId, repository, tags, null, user);
    }

    /**
     * Create an Image Sync Item in a given project
     * @param projectId the project
     * @param repository the repository
     * @param tagsRegex regular expression for tags to sync
     * @param user the user
     * @return the image sync item
     */
    public ImageSyncItem createSyncItem(Long projectId, String repository, String tagsRegex, User user) {
        return createSyncItem(projectId, repository, Collections.emptyList(), tagsRegex, user);
    }

    /**
     * Create an Image Sync Item in a given project
     * @param projectName the project name
     * @param repository the repository
     * @param tags exact tag list to sync
     * @param tagsRegex regular expression for tags to sync
     * @param user the user
     * @return the image sync item
     */
    public ImageSyncItem createSyncItem(String projectName, String repository, List<String> tags,
                                        String tagsRegex, User user) {
        ensureImageAdmin(user);
        Optional<ImageSyncProject> optProject = syncFactory.lookupProjectByNameAndUser(projectName, user);
        ImageSyncProject project = optProject
                .orElseThrow(() -> new EntityNotExistsException(ImageSyncProject.class, projectName));

        ImageSyncItem src = new ImageSyncItem(project, user.getOrg(), repository, tags, tagsRegex);
        syncFactory.save(src);
        return src;
    }

    /**
     * Create an Image Sync Item in a given project
     * @param projectName the project name
     * @param repository the repository
     * @param tags exact tag list to sync - sync all if empty
     * @param user the user
     * @return the image sync item
     */
    public ImageSyncItem createSyncItem(String projectName, String repository, List<String> tags, User user) {
        return createSyncItem(projectName, repository, tags, null, user);
    }

    /**
     * Create an Image Sync Item in a given project
     * @param projectName the project name
     * @param repository the repository
     * @param tagsRegex regular expression for tags to sync
     * @param user the user
     * @return the image sync item
     */
    public ImageSyncItem createSyncItem(String projectName, String repository, String tagsRegex, User user) {
        return createSyncItem(projectName, repository, Collections.emptyList(), tagsRegex, user);
    }

    /**
     * Lookup a Image Sync Item by ID and User
     * @param itemId the item ID
     * @param user the calling user
     * @return optional {@link ImageSyncItem}
     */
    public Optional<ImageSyncItem> lookupSyncItemByIdAndUser(Long itemId, User user) {
        ensureImageAdmin(user);
        return syncFactory.lookupSyncItemByIdAndUser(itemId, user);
    }

    /**
     * List all {@link ImageSyncItem} available for the User
     * @param user the user
     * @return list of Image Sync Items
     */
    public List<ImageSyncItem> listSyncItems(User user) {
        ensureImageAdmin(user);
        return syncFactory.listSyncItems(user);
    }

    /**
     * Update a Image Sync Project
     * @param projectName the project name
     * @param user the calling user
     * @param newSourceStoreLabel the new source image store label or NULL when it should stay unchanged
     * @param newDestinationStoreLabel the new destination image store name or NULL when it should stay unchanged
     * @param newScope the new Scope or NULL when it should stay unchanged
     * @return the new {@link ImageSyncProject}
     */
    public ImageSyncProject updateProject(String projectName, User user, String newSourceStoreLabel,
                                          String newDestinationStoreLabel, Boolean newScope) {
        ensureImageAdmin(user);
        ImageSyncProject project = syncFactory.lookupProjectByNameAndUser(projectName, user)
                .orElseThrow(() -> new EntityNotExistsException(ImageSyncProject.class, projectName));
        if (StringUtils.isBlank(newSourceStoreLabel)) {
            Optional<ImageStore> optStore = ImageStoreFactory.lookupBylabelAndOrg(newSourceStoreLabel, user.getOrg());
            if (optStore.isEmpty()) {
                throw new EntityNotExistsException(ImageStore.class, newSourceStoreLabel);
            }
            ImageStore store = optStore.filter(s -> s.getStoreType().equals(ImageStoreFactory.TYPE_REGISTRY))
                    .orElseThrow(() -> new ValidatorException("Source Image Store is not a container registry"));
            project.setSrcStore(store);
        }
        if (StringUtils.isBlank(newDestinationStoreLabel)) {
            Optional<ImageStore> optStore =
                    ImageStoreFactory.lookupBylabelAndOrg(newDestinationStoreLabel, user.getOrg());
            if (optStore.isEmpty()) {
                throw new EntityNotExistsException(ImageStore.class, newDestinationStoreLabel);
            }
            ImageStore store = optStore.filter(s -> s.getStoreType().equals(ImageStoreFactory.TYPE_REGISTRY))
                    .orElseThrow(() -> new ValidatorException("Destination Image Store is not a container registry"));
            project.setDestinationImageStore(store);
        }
        if (newScope != null) {
            project.setScoped(newScope);
        }
        syncFactory.save(project);
        return project;
    }

    /**
     * Update a Image Sync Project
     * @param projectId the project ID
     * @param user the calling user
     * @param newSrcStoreId the new source image store ID or NULL when it should stay unchanged
     * @param newDestStoreId the new destination image store ID or NULL when it should stay unchanged
     * @param newScope the new Scope or NULL when it should stay unchanged
     * @return the new {@link ImageSyncProject}
     */
    public ImageSyncProject updateProject(Long projectId, User user, Long newSrcStoreId, Long newDestStoreId,
                                          Boolean newScope) {
        ensureImageAdmin(user);
        Optional<ImageSyncProject> optProject = syncFactory.lookupProjectByIdAndUser(projectId, user);
        ImageSyncProject project = optProject
                .orElseThrow(() -> new EntityNotExistsException(ImageSyncProject.class, projectId));
        if (newSrcStoreId != null) {
            Optional<ImageStore> optStore = ImageStoreFactory.lookupByIdAndOrg(newSrcStoreId, user.getOrg());
            if (optStore.isEmpty()) {
                throw new EntityNotExistsException(ImageStore.class, newSrcStoreId);
            }
            ImageStore store = optStore.filter(s -> s.getStoreType().equals(ImageStoreFactory.TYPE_REGISTRY))
                    .orElseThrow(() -> new ValidatorException("Source Image Store is not a container registry"));
            project.setSrcStore(store);
        }
        if (newDestStoreId != null) {
            Optional<ImageStore> optStore = ImageStoreFactory.lookupByIdAndOrg(newDestStoreId, user.getOrg());
            if (optStore.isEmpty()) {
                throw new EntityNotExistsException(ImageStore.class, newDestStoreId);
            }
            ImageStore store = optStore.filter(s -> s.getStoreType().equals(ImageStoreFactory.TYPE_REGISTRY))
                    .orElseThrow(() -> new ValidatorException("Destination Image Store is not a container registry"));
            project.setDestinationImageStore(store);
        }
        if (newScope != null) {
            project.setScoped(newScope);
        }
        syncFactory.save(project);
        return project;
    }

    /**
     * Update an Image Sync Item
     * @param itemId the item ID
     * @param user the calling user
     * @param newRepository the new Repository or NULL when it should be unchanged
     * @param newTags the new exact tag list or NULL when it should be unchanged
     * @param newTagsRegex the new tag regular expression or NULL when it should be unchanged
     * @return the new Image Sync Item
     */
    public ImageSyncItem updateSyncItem(Long itemId, User user, String newRepository, List<String> newTags,
                                        String newTagsRegex) {
        ensureImageAdmin(user);
        Optional<ImageSyncItem> optSource = syncFactory.lookupSyncItemByIdAndUser(itemId, user);
        ImageSyncItem src = optSource
                .orElseThrow(() -> new EntityNotExistsException(ImageSyncItem.class, itemId));
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
     * Delete an Image Sync Item by ID and user
     * @param itemId the item id
     * @param user the calling user
     */
    public void deleteSyncItem(Long itemId, User user) {
        ensureImageAdmin(user);
        Optional<ImageSyncItem> optSource = syncFactory.lookupSyncItemByIdAndUser(itemId, user);
        ImageSyncItem src = optSource
                .orElseThrow(() -> new EntityNotExistsException(ImageSyncItem.class, itemId));
        syncFactory.remove(src);
    }

    /**
     * List images in an Image store
     * @param storeId the store id
     * @param filter filter - provide an empty string to get all images
     * @param user the calling user
     * @return a list of images in the store
     */
    public List<String> listImagesInStore(Long storeId, String filter, User user) {
        ensureImageAdmin(user);
        ImageStore store = ImageStoreFactory.lookupByIdAndOrg(storeId, user.getOrg())
                .orElseThrow(() -> new EntityNotExistsException(ImageStore.class, storeId));
        return listImagesInStore(store, filter);
    }

    /**
     * List images in an Image store
     * @param storeLabel image store label
     * @param filter filter - provide an empty string to get all images
     * @param user the calling user
     * @return a list of images in the store
     */
    public List<String> listImagesInStore(String storeLabel, String filter, User user) {
        ensureImageAdmin(user);
        ImageStore store = ImageStoreFactory.lookupBylabelAndOrg(storeLabel, user.getOrg())
                .orElseThrow(() -> new EntityNotExistsException(ImageStore.class, storeLabel));
        return listImagesInStore(store, filter);
    }

    private List<String> listImagesInStore(ImageStore store, String filter) {
        if (!ImageStoreFactory.TYPE_REGISTRY.equals(store.getStoreType())) {
            throw new ValidatorException("Image Store is not a container registry");
        }

        List<RepositoryImageList> images = SkopeoCommandManager.getStoreImages(store, filter);
        return images.stream().map(t -> t.getName()).collect(Collectors.toList());
    }

    /**
     * List tags for a given image in a store
     * @param storeId the store id
     * @param image the image
     * @param user the calling user
     * @return list of tags
     */
    public List<String> listImageTagsInStore(Long storeId, String image, User user) {
        ensureImageAdmin(user);
        ImageStore store = ImageStoreFactory.lookupByIdAndOrg(storeId, user.getOrg())
                .orElseThrow(() -> new EntityNotExistsException(ImageStore.class, storeId));
        return listImageTagsInStore(store, image);
    }

    /**
     * List tags for a given image in a store
     * @param storeLabel the store label
     * @param image the image
     * @param user the calling user
     * @return list of tags
     */
    public List<String> listImageTagsInStore(String storeLabel, String image, User user) {
        ensureImageAdmin(user);
        ImageStore store = ImageStoreFactory.lookupBylabelAndOrg(storeLabel, user.getOrg())
                .orElseThrow(() -> new EntityNotExistsException(ImageStore.class, storeLabel));
        return listImageTagsInStore(store, image);
    }

    private List<String> listImageTagsInStore(ImageStore store, String image) {
        if (StringUtils.isBlank(image)) {
            throw new ValidatorException("Missing Image");
        }
        if (!ImageStoreFactory.TYPE_REGISTRY.equals(store.getStoreType())) {
            throw new ValidatorException("Image Store is not a container registry");
        }
        ImageTags tags = SkopeoCommandManager.getImageTags(store, image);
        return tags.getTags();
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
