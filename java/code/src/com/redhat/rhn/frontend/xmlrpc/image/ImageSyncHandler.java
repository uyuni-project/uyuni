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
import com.redhat.rhn.domain.image.ImageSyncProject;
import com.redhat.rhn.domain.image.ImageSyncSource;
import com.redhat.rhn.domain.user.User;
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
     * @param destStoreId the id of the target image store
     * @param scoped store the image scoped
     * @return the created project
     *
     * @apidoc.doc Create an Image Sync Project
     * @apidoc.param #session_key()
     * @apidoc.param #param("string", "name", "The project name")
     * @apidoc.param #param("int", "destStoreId", "The destination image store id")
     * @apidoc.param #param("boolean", "scoped",
     *   "Images at DESTINATION are prefix using the full source image path as scope")
     * @apidoc.returntype $ImageSyncProjectSerializer
     */
    public ImageSyncProject createProject(User loggedInUser, String name, Integer destStoreId, Boolean scoped) {
        ensureImageAdmin(loggedInUser);
        try {
            return syncManager.createProject(name, destStoreId.longValue(), scoped, loggedInUser);
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
     * @param destStoreLabel the label of the target image store
     * @param scoped store the image scoped
     * @return the created project
     *
     * @apidoc.doc Create an Image Sync Project
     * @apidoc.param #session_key()
     * @apidoc.param #param("string", "name", "The project name")
     * @apidoc.param #param("string", "destStoreLabel", "The destination image store label")
     * @apidoc.param #param("boolean", "scoped",
     *   "Images at DESTINATION are prefix using the full source image path as scope")
     * @apidoc.returntype $ImageSyncProjectSerializer
     */
    public ImageSyncProject createProject(User loggedInUser, String name, String destStoreLabel, Boolean scoped) {
        ensureImageAdmin(loggedInUser);
        try {
            return syncManager.createProject(name, destStoreLabel, scoped, loggedInUser);
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
     * @apidoc.returntype #return_array_begin() $ImageSyncProjectSerializer #array_end()
     */
    @ReadOnly
    public List<ImageSyncProject> listProjects(User loggedInUser) {
        ensureImageAdmin(loggedInUser);
        return syncManager.listProjects(loggedInUser);
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
     * @apidoc.returntype $ImageSyncProject
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
     * @apidoc.returntype $ImageSyncProject
     */
    @ReadOnly
    public ImageSyncProject getProjectDetails(User loggedInUser, String projectName) {
        ensureImageAdmin(loggedInUser);
        Optional<ImageSyncProject> opt = syncManager.lookupProject(projectName, loggedInUser);
        return opt.orElseThrow(() -> new EntityNotExistsFaultException(projectName));
    }

    /**
     * Create an Image Sync Source in a project
     * @param loggedInUser the user
     * @param projectId the project id
     * @param storeId the image store id
     * @param repository the repository
     * @param tags list of tags to sync
     * @param tagsRegex regular expression for tags to sync
     * @return the newly created source
     *
     * @apidoc.doc Create an Image Sync Source
     * @apidoc.param #session_key()
     * @apidoc.param #param("int", "projectId", "The project id")
     * @apidoc.param #param("int", "storeId", "The source image store id")
     * @apidoc.param #param("string", "repository", "the repo to sync")
     * @apidoc.param #array_single_desc("string", "tags", "list of explicit tags to sync")
     * @apidoc.param #param("string", "tagsRegex", "regular expression for tags to sync")
     * @apidoc.returntype $ImageSyncSourceSerializer
     */
    public ImageSyncSource createSource(User loggedInUser, Integer projectId, Integer storeId, String repository,
                                        List<String> tags, String tagsRegex) {
        ensureImageAdmin(loggedInUser);
        try {
            return syncManager.createSource(projectId.longValue(), storeId.longValue(), repository, tags, tagsRegex,
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
     * Create an Image Sync Source in a project
     * @param loggedInUser the user
     * @param projectId the project id
     * @param storeId the image store id
     * @param repository the repository
     * @param tags list of tags to sync
     * @return the newly created source
     *
     * @apidoc.doc Create an Image Sync Source
     * @apidoc.param #session_key()
     * @apidoc.param #param("int", "projectId", "The project id")
     * @apidoc.param #param("int", "storeId", "The source image store id")
     * @apidoc.param #param("string", "repository", "the repo to sync")
     * @apidoc.param #array_single_desc("string", "tags", "list of explicit tags to sync - empty list to sync all")
     * @apidoc.returntype $ImageSyncSourceSerializer
     */
    public ImageSyncSource createSource(User loggedInUser, Integer projectId, Integer storeId, String repository,
                                        List<String> tags) {
        return createSource(loggedInUser, projectId, storeId, repository, tags, null);
    }

    /**
     * Create an Image Sync Source in a project
     * @param loggedInUser the user
     * @param projectId the project id
     * @param storeId the image store id
     * @param repository the repository
     * @param tagsRegex regular expression for tags to sync
     * @return the newly created source
     *
     * @apidoc.doc Create an Image Sync Source
     * @apidoc.param #session_key()
     * @apidoc.param #param("int", "projectId", "The project id")
     * @apidoc.param #param("int", "storeId", "The source image store id")
     * @apidoc.param #param("string", "repository", "the repo to sync")
     * @apidoc.param #param("string", "tagsRegex", "regular expression for tags to sync")
     * @apidoc.returntype $ImageSyncSourceSerializer
     */
    public ImageSyncSource createSource(User loggedInUser, Integer projectId, Integer storeId, String repository,
                                        String tagsRegex) {
        return createSource(loggedInUser, projectId, storeId, repository, null, tagsRegex);
    }

    /**
     * Create an Image Sync Source in a project
     * @param loggedInUser the user
     * @param projectName the project name
     * @param storeLabel the image store label
     * @param repository the repository
     * @param tags list of tags to sync
     * @param tagsRegex regular expression for tags to sync
     * @return the newly created source
     *
     * @apidoc.doc Create an Image Sync Source
     * @apidoc.param #session_key()
     * @apidoc.param #param("string", "projectName", "The project name")
     * @apidoc.param #param("string", "storeLabel", "The source image store label")
     * @apidoc.param #param("string", "repository", "the repo to sync")
     * @apidoc.param #array_single_desc("string", "tags", "list of explicit tags to sync")
     * @apidoc.param #param("string", "tagsRegex", "regular expression for tags to sync")
     * @apidoc.returntype $ImageSyncSourceSerializer
     */
    public ImageSyncSource createSource(User loggedInUser, String projectName, String storeLabel, String repository,
                                        List<String> tags, String tagsRegex) {
        ensureImageAdmin(loggedInUser);
        try {
            return syncManager.createSource(projectName, storeLabel, repository, tags, tagsRegex,
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
     * Create an Image Sync Source in a project
     * @param loggedInUser the user
     * @param projectName the project name
     * @param storeLabel the image store label
     * @param repository the repository
     * @param tags list of tags to sync
     * @return the newly created source
     *
     * @apidoc.doc Create an Image Sync Source
     * @apidoc.param #session_key()
     * @apidoc.param #param("string", "projectName", "The project name")
     * @apidoc.param #param("string", "storeLabel", "The source image store label")
     * @apidoc.param #param("string", "repository", "the repo to sync")
     * @apidoc.param #array_single_desc("string", "tags", "list of explicit tags to sync - empty list to sync all")
     * @apidoc.returntype $ImageSyncSourceSerializer
     */
    public ImageSyncSource createSource(User loggedInUser, String projectName, String storeLabel, String repository,
                                        List<String> tags) {
        return createSource(loggedInUser, projectName, storeLabel, repository, tags, null);
    }

    /**
     * Create an Image Sync Source in a project
     * @param loggedInUser the user
     * @param projectName the project name
     * @param storeLabel the image store label
     * @param repository the repository
     * @param tagsRegex regular expression for tags to sync
     * @return the newly created source
     *
     * @apidoc.doc Create an Image Sync Source
     * @apidoc.param #session_key()
     * @apidoc.param #param("string", "projectName", "The project name")
     * @apidoc.param #param("string", "storeLabel", "The source image store label")
     * @apidoc.param #param("string", "repository", "the repo to sync")
     * @apidoc.param #param("string", "tagsRegex", "regular expression for tags to sync")
     * @apidoc.returntype $ImageSyncSourceSerializer
     */
    public ImageSyncSource createSource(User loggedInUser, String projectName, String storeLabel, String repository,
                                        String tagsRegex) {
        return createSource(loggedInUser, projectName, storeLabel, repository, null, tagsRegex);
    }
}
