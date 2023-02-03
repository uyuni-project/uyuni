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
package com.suse.manager.webui.controllers.image;

import static com.suse.manager.webui.utils.SparkApplicationHelper.json;
import static com.suse.manager.webui.utils.SparkApplicationHelper.withImageAdmin;
import static spark.Spark.get;
import static spark.Spark.halt;
import static spark.Spark.post;

import com.redhat.rhn.domain.image.ImageStore;
import com.redhat.rhn.domain.image.ImageStoreFactory;
import com.redhat.rhn.domain.image.ImageSyncItem;
import com.redhat.rhn.domain.image.ImageSyncProject;
import com.redhat.rhn.domain.role.Role;
import com.redhat.rhn.domain.role.RoleFactory;
import com.redhat.rhn.domain.user.User;
import com.redhat.rhn.manager.image.ImageSyncManager;

import com.suse.manager.utils.skopeo.SkopeoCommandManager;
import com.suse.manager.utils.skopeo.beans.ImageTags;
import com.suse.manager.utils.skopeo.beans.RepositoryImageList;
import com.suse.manager.webui.controllers.image.request.ImageSyncProjectCreateRequest;
import com.suse.manager.webui.errors.NotFoundException;
import com.suse.manager.webui.utils.gson.ResultJson;
import com.suse.utils.Json;

import com.google.gson.Gson;
import com.google.gson.JsonObject;
import com.google.gson.JsonParseException;

import org.apache.commons.lang3.StringUtils;
import org.apache.http.HttpStatus;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

import spark.Request;
import spark.Response;
import spark.Spark;

/**
 * Spark controller class for image store pages and API endpoints.
 */
public class ImageManagementApiController {

    private static final Gson GSON = Json.GSON;
    private ImageSyncManager imageSyncManager;

    public ImageManagementApiController(ImageSyncManager imageSyncManagerIn) {
        this.imageSyncManager = imageSyncManagerIn;
    }

    /**
     * Invoked from Router. Initialize routes for Systems Views.
     */
    public static void initRoutes(ImageManagementApiController imageManagementApiController) {
        get("/manager/api/cm/imagestores/listimages/:id", withImageAdmin(ImageManagementApiController::getStoreImages));
        get("/manager/api/cm/imagestores/imagetags/:id", withImageAdmin(ImageManagementApiController::getImageTags));

        get("/manager/api/cm/imagesync", withImageAdmin(imageManagementApiController::list));
        get("/manager/api/cm/imagesync/find/:label", withImageAdmin(imageManagementApiController::getSingleByLabel));
        get("/manager/api/cm/imagesync/:id", withImageAdmin(imageManagementApiController::getSingle));
        post("/manager/api/cm/imagesync/create", withImageAdmin(imageManagementApiController::create));
        post("/manager/api/cm/imagesync/update/:id", withImageAdmin(imageManagementApiController::update));
        post("/manager/api/cm/imagesync/delete", withImageAdmin(imageManagementApiController::delete));
    }


    private static String getStoreImages(Request requestIn, Response responseIn, User userIn) {
        //FIXME get image information can be refactored to be handle in rout manager
        Long storeId;
        try {
            storeId = Long.parseLong(requestIn.params("id"));
        }
        catch (NumberFormatException e) {
            throw new NotFoundException();
        }

        String filter = requestIn.queryParams("q");

        //FIXME: user ImageSyncManager::listImagesInStore(...)
        Optional<ImageStore> store = ImageStoreFactory.lookupByIdAndOrg(storeId,
                userIn.getOrg());

        if (store.isEmpty()) {
            return json(responseIn, HttpStatus.SC_BAD_REQUEST, ResultJson.error("not_found"));
        }

        if (!ImageStoreFactory.TYPE_REGISTRY.equals(store.get().getStoreType())) {
            // FIXME pass message to message string
            return json(responseIn, HttpStatus.SC_BAD_REQUEST,
                    ResultJson.error("Store is not a container image registry"));
        }

        try {
            List<RepositoryImageList> images = SkopeoCommandManager.getStoreImages(store.get(), filter);
            List<String> listNames = images.stream().map(t -> t.getName()).collect(Collectors.toList());
            JsonObject json = new JsonObject();
            json.addProperty("size", listNames.size());
            json.addProperty("images", GSON.toJson(listNames));
            return json(responseIn, ResultJson.success(json));
        }
        catch (RuntimeException e) {
            return json(responseIn, HttpStatus.SC_INTERNAL_SERVER_ERROR, ResultJson.error(e.getMessage()));
        }
    }


    private static String getImageTags(Request requestIn, Response responseIn, User userIn) {
        //FIXME get image information can be refactored to be handle in rout manager
        Long storeId;
        try {
            storeId = Long.parseLong(requestIn.params("id"));
        }
        catch (NumberFormatException e) {
            throw new NotFoundException();
        }
        Optional<ImageStore> store = ImageStoreFactory.lookupByIdAndOrg(storeId,
                userIn.getOrg());

        String image = requestIn.queryParams("image");
        //FIXME: use ImageSyncManager::listImagesInStore(...)

        if (store.isEmpty()) {
            return json(responseIn, HttpStatus.SC_BAD_REQUEST, ResultJson.error("not_found"));
        }
        // FIXME should be a different message
        if (StringUtils.isEmpty(image)) {
            return json(responseIn, HttpStatus.SC_BAD_REQUEST, ResultJson.error("not_found"));
        }

        if (!ImageStoreFactory.TYPE_REGISTRY.equals(store.get().getStoreType())) {
            // FIXME pass message to message string
            return json(responseIn, HttpStatus.SC_BAD_REQUEST,
                    ResultJson.error("Store is not a container image registry"));
        }

        try {
            ImageTags tags = SkopeoCommandManager.getImageTags(store.get(), image);
            JsonObject json = new JsonObject();
            json.addProperty("size", tags.getTags().size());
            json.addProperty("tags", GSON.toJson(tags.getTags()));
            return json(responseIn, ResultJson.success(json));
        }
        catch (RuntimeException e) {
            return json(responseIn, HttpStatus.SC_INTERNAL_SERVER_ERROR, ResultJson.error(e.getMessage()));
        }
    }

    /**
     * Processes a GET request to get a list of all image sync projects
     *
     * @param req the request object
     * @param res the response object
     * @param user the authorized user
     * @return the result JSON object
     */
    public Object list(Request req, Response res, User user) {
        List<ImageSyncProject> imageSyncProjects = imageSyncManager.listProjects(user);
        return json(res, getJsonList(imageSyncProjects));
    }

    /**
     * Processes a GET request to get a single image sync project given an id
     *
     * @param req the request object
     * @param res the response object
     * @param user the authorized user
     * @return the result JSON object
     */
    public Object getSingle(Request req, Response res, User user) {
        Long syncId;
        try {
            syncId = Long.parseLong(req.params("id"));
        }
        catch (NumberFormatException e) {
            throw new NotFoundException();
        }

        Optional<ImageSyncProject> syncProject = imageSyncManager.lookupProject(syncId, user);

        return syncProject.map(sync -> {
            JsonObject json = new JsonObject();
            json.addProperty("id", sync.getId());
            json.addProperty("label", sync.getName());
            json.addProperty("sourceRegistry", sync.getSrcStore().getLabel());
            json.addProperty("targetRegistry", sync.getDestinationImageStore().getLabel());

            if (sync.getSyncItems().size() > 0) {
                json.addProperty("image", sync.getSyncItems().get(0).getSrcRepository());
            }

            return json(res, ResultJson.success(json));
        }).orElseGet(() -> json(res, ResultJson.error("not_found")));
    }

    /**
     * Processes a GET request to get a single image sync project by a given label
     *
     * @param req the request object
     * @param res the response object
     * @param user the authorized user
     * @return the result JSON object
     */
    public Object getSingleByLabel(Request req, Response res, User user) {
        String label = req.params("label");
        Optional<ImageSyncProject> syncProject = imageSyncManager.lookupProject(label, user);

        return syncProject.map(sync -> {
            JsonObject json = new JsonObject();
            json.addProperty("id", sync.getId());
            json.addProperty("label", sync.getName());
            json.addProperty("sourceRegistry", sync.getSrcStore().getLabel());
            json.addProperty("targetRegistry", sync.getDestinationImageStore().getLabel());

            if (sync.getSyncItems().size() > 0) {
                json.addProperty("image", sync.getSyncItems().get(0).getSrcRepository());
            }

            return json(res, ResultJson.success(json));
        }).orElseGet(() -> json(res, ResultJson.error("not_found")));
    }

    /**
     * Processes a POST request to create a new image sync project
     *
     * @param req the request object
     * @param res the response object
     * @param user the authorized user
     * @return the result JSON object
     */
    public Object create(Request req, Response res, User user) {
        ImageSyncProjectCreateRequest createRequest;
        try {
            createRequest = GSON.fromJson(req.body(), ImageSyncProjectCreateRequest.class);
        }
        catch (JsonParseException e) {
            throw Spark.halt(HttpStatus.SC_BAD_REQUEST);
        }

        ImageSyncProject syncProject = imageSyncManager.createProject(
                createRequest.getLabel(),
                createRequest.getSourceRegistry(),
                createRequest.getTargetRegistry(),
                true,
                user);

        imageSyncManager.createSyncItem(syncProject.getId(), createRequest.getImage(), new ArrayList<>(), user);
        return json(res, ResultJson.success());
    }

    /**
     * Processes a POST request to update an existing image sync project
     *
     * @param req the request object
     * @param res the response object
     * @param user the authorized user
     * @return the result JSON object
     */
    public Object update(Request req, Response res, User user) {
        ImageSyncProjectCreateRequest updateRequest;
        try {
            updateRequest = GSON.fromJson(req.body(), ImageSyncProjectCreateRequest.class);
        }
        catch (JsonParseException e) {
            throw Spark.halt(HttpStatus.SC_BAD_REQUEST);
        }

        // Lookup the project and update with the new values
        Long syncId = Long.parseLong(req.params("id"));
        Optional<ImageSyncProject> syncProject = imageSyncManager.lookupProject(syncId, user);
        if (syncProject.isPresent()) {

            Optional<ImageStore> source = ImageStoreFactory
                    .lookupBylabelAndOrg(updateRequest.getSourceRegistry(), user.getOrg());
            Optional<ImageStore> target = ImageStoreFactory
                    .lookupBylabelAndOrg(updateRequest.getTargetRegistry(), user.getOrg());

            imageSyncManager.updateProject(
                    syncId,
                    user,
                    source.get().getId(),
                    target.get().getId(),
                    null);

            List<ImageSyncItem> items = syncProject.get().getSyncItems();
            if (items.size() > 0) {
                imageSyncManager.updateSyncItem(items.get(0).getId(), user, updateRequest.getImage(),
                        new ArrayList<>(), null);
            }

            return json(res, ResultJson.success());
        }
        else {
            return json(res, ResultJson.error("not_found"));
        }
    }

    /**
     * Processes a POST request to delete a list of image sync projects
     *
     * @param req the request object
     * @param res the response object
     * @param user the authorized user
     * @return the result JSON object
     */
    public Object delete(Request req, Response res, User user) {
        List<Long> ids;
        try {
            ids = Arrays.asList(GSON.fromJson(req.body(), Long[].class));
        }
        catch (JsonParseException e) {
            throw halt(HttpStatus.SC_BAD_REQUEST);
        }

        List<ImageSyncProject> projects = ids.stream()
                .map(id -> imageSyncManager.lookupProject(id, user))
                .filter(Optional::isPresent)
                .map(Optional::get)
                .collect(Collectors.toList());

        if (projects.size() < ids.size()) {
            return json(res, ResultJson.error("not_found"));
        }

        projects.forEach(p -> imageSyncManager.deleteProject(p.getId(), user));
        return json(res, ResultJson.success(projects.size()));
    }

    /**
     * Creates a list of JSON objects for a list of {@link ImageSyncProject} instances
     *
     * @param imageSyncProjects the list of image sync projects
     * @return the list of JSON objects
     */
    private static List<JsonObject> getJsonList(List<ImageSyncProject> imageSyncProjects) {
        return imageSyncProjects.stream().map(project -> {
            JsonObject json = new JsonObject();
            json.addProperty("id", project.getId());
            json.addProperty("label", project.getName());
            json.addProperty("source", project.getSrcStore().getLabel());
            json.addProperty("target", project.getDestinationImageStore().getLabel());
            return json;
        }).collect(Collectors.toList());
    }
}
