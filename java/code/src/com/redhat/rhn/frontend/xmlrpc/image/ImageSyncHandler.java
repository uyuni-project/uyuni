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
package com.redhat.rhn.frontend.xmlrpc.image;

import com.redhat.rhn.common.validator.ValidatorException;
import com.redhat.rhn.domain.image.ImageSyncItem;
import com.redhat.rhn.domain.image.ImageSyncProject;
import com.redhat.rhn.domain.user.User;
import com.redhat.rhn.frontend.dto.ShortImageSyncProject;
import com.redhat.rhn.frontend.xmlrpc.BaseHandler;
import com.redhat.rhn.frontend.xmlrpc.EntityExistsFaultException;
import com.redhat.rhn.frontend.xmlrpc.EntityNotExistsFaultException;
import com.redhat.rhn.frontend.xmlrpc.ValidationException;
import com.redhat.rhn.manager.EntityExistsException;
import com.redhat.rhn.manager.EntityNotExistsException;
import com.redhat.rhn.manager.image.ImageSyncManager;

import com.suse.manager.api.ReadOnly;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

/**
 * ImageSyncHandler
 * @apidoc.namespace image.sync
 * @apidoc.doc Provides methods to create, read, update, execute and delete image sync projects
 */
public class ImageSyncHandler extends BaseHandler {

    private final ImageSyncManager syncManager;

    /**
     * Standard Constructor
     */
    public ImageSyncHandler() {
        syncManager = new ImageSyncManager();
    }

    /**
     * Initialize a handler specifying a ImageSync Manager instance. Mainly for testing
     * @param syncManagerIn the image sync manager
     */
    public ImageSyncHandler(ImageSyncManager syncManagerIn) {
        syncManager = syncManagerIn;
    }

    /**
     * Create an Image Sync Project
     * @param loggedInUser the user
     * @param name the project name
     * @param srcStoreId the id of the source image store
     * @param destStoreId the id of the destination image store
     * @param scoped store the image scoped
     * @return the created project
     *
     * @apidoc.doc Create an Image Sync Project
     * @apidoc.param #session_key()
     * @apidoc.param #param("string", "name", "The project name")
     * @apidoc.param #param("int", "srcStoreId", "The source image store id")
     * @apidoc.param #param("int", "destStoreId", "The destination image store id")
     * @apidoc.param #param("boolean", "scoped",
     *   "Images at DESTINATION are prefix using the full source image path as scope")
     * @apidoc.returntype $ImageSyncProjectSerializer
     */
    public ImageSyncProject createProject(User loggedInUser, String name, Integer srcStoreId, Integer destStoreId,
                                          Boolean scoped) {
        ensureImageAdmin(loggedInUser);
        try {
            return syncManager.createProject(name, srcStoreId.longValue(), destStoreId.longValue(), scoped,
                    loggedInUser);
        }
        catch (EntityExistsException e) {
            throw new EntityExistsFaultException(e);
        }
        catch (EntityNotExistsException e) {
            throw new EntityNotExistsFaultException(e);
        }
        catch (ValidatorException e) {
            throw new ValidationException(e);
        }
    }

    /**
     * Create an Image Sync Project
     * @param loggedInUser the user
     * @param name the project name
     * @param srcStoreLabel the label of the source image store
     * @param destStoreLabel the label of the target image store
     * @param scoped store the image scoped
     * @return the created project
     *
     * @apidoc.doc Create an Image Sync Project
     * @apidoc.param #session_key()
     * @apidoc.param #param("string", "name", "The project name")
     * @apidoc.param #param("string", "srcStoreLabel", "The source image store label")
     * @apidoc.param #param("string", "destStoreLabel", "The destination image store label")
     * @apidoc.param #param("boolean", "scoped", "Images at DESTINATION are prefix using the full source image path as scope")
     * @apidoc.returntype $ImageSyncProjectSerializer
     */
    public ImageSyncProject createProject(User loggedInUser, String name, String srcStoreLabel, String destStoreLabel,
                                          Boolean scoped) {
        ensureImageAdmin(loggedInUser);
        try {
            return syncManager.createProject(name, srcStoreLabel, destStoreLabel, scoped, loggedInUser);
        }
        catch (EntityExistsException e) {
            throw new EntityExistsFaultException(e);
        }
        catch (EntityNotExistsException e) {
            throw new EntityNotExistsFaultException(e);
        }
        catch (ValidatorException e) {
            throw new ValidationException(e);
        }
    }

    /**
     * List Image Sync Projects available for the given User
     * @param loggedInUser the user
     * @return list of Image Sync Projects
     *
     * @apidoc.doc List available Image Sync Projects
     * @apidoc.param #session_key()
     * @apidoc.returntype #return_array_begin() $ShortImageSyncProjectSerializer #array_end()
     */
    @ReadOnly
    public List<ShortImageSyncProject> listProjects(User loggedInUser) {
        ensureImageAdmin(loggedInUser);
        return syncManager.listProjects(loggedInUser)
                .stream()
                .map(p -> new ShortImageSyncProject(p))
                .collect(Collectors.toList());
    }

    /**
     * Get Project Details
     * @param loggedInUser The Current User
     * @param projectId the Project id
     * @return ImageSyncProject Object
     *
     * @apidoc.doc Get details of an Image
     * @apidoc.param #session_key()
     * @apidoc.param #param("int", "projectId")
     * @apidoc.returntype $ImageSyncProjectSerializer
     */
    @ReadOnly
    public ImageSyncProject getProjectDetails(User loggedInUser, Integer projectId) {
        ensureImageAdmin(loggedInUser);
        Optional<ImageSyncProject> opt = syncManager.lookupProject(projectId.longValue(), loggedInUser);
        return opt.orElseThrow(() -> new EntityNotExistsFaultException(projectId));
    }

    /**
     * Get Project Details
     * @param loggedInUser The Current User
     * @param projectName the Project Name
     * @return ImageSyncProject Object
     *
     * @apidoc.doc Get details of an Image
     * @apidoc.param #session_key()
     * @apidoc.param #param("string", "projectName")
     * @apidoc.returntype $ImageSyncProjectSerializer
     */
    @ReadOnly
    public ImageSyncProject getProjectDetails(User loggedInUser, String projectName) {
        ensureImageAdmin(loggedInUser);
        Optional<ImageSyncProject> opt = syncManager.lookupProject(projectName, loggedInUser);
        return opt.orElseThrow(() -> new EntityNotExistsFaultException(projectName));
    }

    /**
     * Create an Image Sync Item in a project
     * @param loggedInUser the user
     * @param projectId the project id
     * @param repository the repository
     * @param tags list of tags to sync
     * @param tagsRegex regular expression for tags to sync
     * @return the newly created item
     *
     * @apidoc.doc Create an Image Sync Items
     * @apidoc.param #session_key()
     * @apidoc.param #param("int", "projectId", "The project id")
     * @apidoc.param #param("string", "repository", "the repo to sync")
     * @apidoc.param #array_single_desc("string", "tags", "list of explicit tags to sync")
     * @apidoc.param #param("string", "tagsRegex", "regular expression for tags to sync")
     * @apidoc.returntype $ImageSyncItemSerializer
     */
    public ImageSyncItem createSyncItem(User loggedInUser, Integer projectId, String repository,
                                        List<String> tags, String tagsRegex) {
        ensureImageAdmin(loggedInUser);
        try {
            return syncManager.createSyncItem(projectId.longValue(), repository, tags, tagsRegex, loggedInUser);
        }
        catch (EntityNotExistsException e) {
            throw new EntityNotExistsFaultException(e);
        }
        catch (ValidatorException e) {
            throw new ValidationException(e);
        }
    }

    /**
     * Create an Image Sync Item in a project
     * @param loggedInUser the user
     * @param projectId the project id
     * @param repository the repository
     * @param tags list of tags to sync
     * @return the newly created item
     *
     * @apidoc.doc Create an Image Sync Item
     * @apidoc.param #session_key()
     * @apidoc.param #param("int", "projectId", "The project id")
     * @apidoc.param #param("string", "repository", "the repo to sync")
     * @apidoc.param #array_single_desc("string", "tags", "list of explicit tags to sync - empty list to sync all")
     * @apidoc.returntype $ImageSyncItemSerializer
     */
    public ImageSyncItem createSyncItem(User loggedInUser, Integer projectId, String repository, List<String> tags) {
        return createSyncItem(loggedInUser, projectId, repository, tags, null);
    }

    /**
     * Create an Image Sync Item in a project
     * @param loggedInUser the user
     * @param projectId the project id
     * @param repository the repository
     * @param tagsRegex regular expression for tags to sync
     * @return the newly created item
     *
     * @apidoc.doc Create an Image Sync Item
     * @apidoc.param #session_key()
     * @apidoc.param #param("int", "projectId", "The project id")
     * @apidoc.param #param("string", "repository", "the repo to sync")
     * @apidoc.param #param("string", "tagsRegex", "regular expression for tags to sync")
     * @apidoc.returntype $ImageSyncItemSerializer
     */
    public ImageSyncItem createSyncItem(User loggedInUser, Integer projectId, String repository, String tagsRegex) {
        return createSyncItem(loggedInUser, projectId, repository, null, tagsRegex);
    }

    /**
     * Create an Image Sync Item in a project
     * @param loggedInUser the user
     * @param projectName the project name
     * @param repository the repository
     * @param tags list of tags to sync
     * @param tagsRegex regular expression for tags to sync
     * @return the newly created item
     *
     * @apidoc.doc Create an Image Sync Item
     * @apidoc.param #session_key()
     * @apidoc.param #param("string", "projectName", "The project name")
     * @apidoc.param #param("string", "repository", "the repo to sync")
     * @apidoc.param #array_single_desc("string", "tags", "list of explicit tags to sync")
     * @apidoc.param #param("string", "tagsRegex", "regular expression for tags to sync")
     * @apidoc.returntype $ImageSyncItemSerializer
     */
    public ImageSyncItem createSyncItem(User loggedInUser, String projectName, String repository,
                                        List<String> tags, String tagsRegex) {
        ensureImageAdmin(loggedInUser);
        try {
            return syncManager.createSyncItem(projectName, repository, tags, tagsRegex,
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
     * Create an Image Sync Item in a project
     * @param loggedInUser the user
     * @param projectName the project name
     * @param repository the repository
     * @param tags list of tags to sync
     * @return the newly created item
     *
     * @apidoc.doc Create an Image Sync Item
     * @apidoc.param #session_key()
     * @apidoc.param #param("string", "projectName", "The project name")
     * @apidoc.param #param("string", "repository", "the repo to sync")
     * @apidoc.param #array_single_desc("string", "tags", "list of explicit tags to sync - empty list to sync all")
     * @apidoc.returntype $ImageSyncItemSerializer
     */
    public ImageSyncItem createSyncItem(User loggedInUser, String projectName, String repository, List<String> tags) {
        return createSyncItem(loggedInUser, projectName, repository, tags, null);
    }

    /**
     * Create an Image Sync Item in a project
     * @param loggedInUser the user
     * @param projectName the project name
     * @param repository the repository
     * @param tagsRegex regular expression for tags to sync
     * @return the newly created item
     *
     * @apidoc.doc Create an Image Sync Item
     * @apidoc.param #session_key()
     * @apidoc.param #param("string", "projectName", "The project name")
     * @apidoc.param #param("string", "repository", "the repo to sync")
     * @apidoc.param #param("string", "tagsRegex", "regular expression for tags to sync")
     * @apidoc.returntype $ImageSyncItemSerializer
     */
    public ImageSyncItem createSyncItem(User loggedInUser, String projectName, String repository, String tagsRegex) {
        return createSyncItem(loggedInUser, projectName, repository, null, tagsRegex);
    }

    /**
     * List images from a given image store
     * @param loggedInUser the user
     * @param imageStoreLabel the store label
     * @param imageFilter an image filter
     *
     * @apidoc.doc List images from a given image store
     * @apidoc.param #session_key()
     * @apidoc.param #param("string", "imageStoreLabel", "The image store label")
     * @apidoc.param #param("string", "imageFilter", "filter images by the given string - keep empyt for all images")
     * @apidoc.returntype #array_single("string", "the list of images")
     * @return
     */
    public List<String> listImages(User loggedInUser, String imageStoreLabel, String imageFilter) {
        ensureImageAdmin(loggedInUser);
        try {
            return syncManager.listImagesInStore(imageStoreLabel, imageFilter, loggedInUser);
        }
        catch (EntityNotExistsException e) {
            throw new EntityNotExistsFaultException(e);
        }
        catch (ValidatorException e) {
            throw new ValidationException(e);
        }
    }

    /**
     * List tags from a given image in an image store
     * @param loggedInUser the user
     * @param imageStoreLabel the store label
     * @param image the image
     *
     * @apidoc.doc List tags from a given image in an image store
     * @apidoc.param #session_key()
     * @apidoc.param #param("string", "imageStoreLabel", "The image store label")
     * @apidoc.param #param("string", "image", "the images")
     * @apidoc.returntype #array_single("string", "the list of tags")
     * @return
     */
    public List<String> listImageTags(User loggedInUser, String imageStoreLabel, String image) {
        ensureImageAdmin(loggedInUser);
        try {
            return syncManager.listImageTagsInStore(imageStoreLabel, image, loggedInUser);
        }
        catch (EntityNotExistsException e) {
            throw new EntityNotExistsFaultException(e);
        }
        catch (ValidatorException e) {
            throw new ValidationException(e);
        }
    }

    /**
     * Delete an Image Sync Project
     * @param loggedInUser the user
     * @param projectName the project name
     * @return 1 on success
     *
     * @apidoc.doc Delete an image sync project.
     * @apidoc.param #session_key()
     * @apidoc.param #param("string", "projectName")
     * @apidoc.returntype #return_int_success()
     */
    public int deleteProject(User loggedInUser, String projectName) {
        ensureImageAdmin(loggedInUser);
        try {
            syncManager.deleteProject(projectName, loggedInUser);
        }
        catch (EntityNotExistsException e) {
            throw new EntityNotExistsFaultException(e);
        }
        return 1;
    }

    /**
     * Delete an Image Sync Item
     * @param loggedInUser the user
     * @param syncItemId the sync item id
     * @return 1 on success
     *
     * @apidoc.doc Delete an image sync item.
     * @apidoc.param #session_key()
     * @apidoc.param #param("int", "syncItemId")
     * @apidoc.returntype #return_int_success()
     */
    public int deleteSyncItem(User loggedInUser, Integer syncItemId) {
        ensureImageAdmin(loggedInUser);
        try {
            syncManager.deleteSyncItem(syncItemId.longValue(), loggedInUser);
        }
        catch (EntityNotExistsException e) {
            throw new EntityNotExistsFaultException(e);
        }
        return 1;
    }

    /**
     * Update an Image Store Project.
     * Set new values to empty when no change is requested.
     * @param loggedInUser the user
     * @param projectName the project name
     * @param newSourceStoreLabel new source image store label
     * @param newDestinationStoreLabel new destination image store label
     * @param newScope new scope
     * @return the new {@link ImageSyncProject}
     *
     * @apidoc.doc Update an Image Store Project. Set new values to empty when no change is requested.
     * @apidoc.param #session_key()
     * @apidoc.param #param("string", "projectName")
     * @apidoc.param #param("string", "newSourceStoreLabel")
     * @apidoc.param #param("string", "newDestinationStoreLabel")
     * @apidoc.param #param("boolean", "newScope")
     * @apidoc.returntype $ImageSyncProjectSerializer
     */
    public ImageSyncProject updateProject(User loggedInUser, String projectName, String newSourceStoreLabel,
                                          String newDestinationStoreLabel, Boolean newScope) {
        ensureImageAdmin(loggedInUser);
        try {
            return syncManager.updateProject(projectName, loggedInUser, newSourceStoreLabel, newDestinationStoreLabel,
                    newScope);
        }
        catch (EntityNotExistsException e) {
            throw new EntityNotExistsFaultException(e);
        }
    }

    /**
     * Update an Image Store Project.
     * Set new values to empty when no change is requested.
     * @param loggedInUser the user
     * @param projectName the project name
     * @param newSourceStoreLabel new source image store label
     * @param newDestinationStoreLabel new destination image store label
     * @return the new {@link ImageSyncProject}
     *
     * @apidoc.doc Update an Image Store Project. Set new values to empty when no change is requested.
     * @apidoc.param #session_key()
     * @apidoc.param #param("string", "projectName")
     * @apidoc.param #param("string", "newSourceStoreLabel")
     * @apidoc.param #param("string", "newDestinationStoreLabel")
     * @apidoc.returntype $ImageSyncProjectSerializer
     */
    public ImageSyncProject updateProject(User loggedInUser, String projectName, String newSourceStoreLabel,
                                          String newDestinationStoreLabel) {
        return updateProject(loggedInUser, projectName, newSourceStoreLabel, newDestinationStoreLabel, null);
    }

    /**
     * Update an Image Sync Item.
     * Set new values to empty when no change is requested.
     * @param loggedInUser the user
     * @param itemId the item id
     * @param newRepository new repository
     * @param newTags new tag list
     * @param newTagsRegex new tag regular expression
     * @return the new {@link ImageSyncItem}
     *
     * @apidoc.doc Update an Image Sync Item. Set new values to empty when no change is requested.
     * @apidoc.param #session_key()
     * @apidoc.param #param("int", "itemId")
     * @apidoc.param #param("string", "newRepository")
     * @apidoc.param #array_single("string", "newTags")
     * @apidoc.param #param("string", "newTagsRegex")
     * @apidoc.returntype $ImageSyncItemSerializer
     */
    public ImageSyncItem updateImageSyncItem(User loggedInUser, Integer itemId, String newRepository,
                                             List<String> newTags, String newTagsRegex) {
        ensureImageAdmin(loggedInUser);
        try {
            return syncManager.updateSyncItem(itemId.longValue(), loggedInUser, newRepository, newTags, newTagsRegex);
        }
        catch (EntityNotExistsException e) {
            throw new EntityNotExistsFaultException(e);
        }
    }
}
