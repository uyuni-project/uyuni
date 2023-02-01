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
import static spark.Spark.post;

import com.redhat.rhn.domain.image.ImageStore;
import com.redhat.rhn.domain.image.ImageStoreFactory;
import com.redhat.rhn.domain.image.ImageSyncProject;
import com.redhat.rhn.domain.role.Role;
import com.redhat.rhn.domain.role.RoleFactory;
import com.redhat.rhn.domain.user.User;
import com.redhat.rhn.manager.image.ImageSyncManager;

import com.suse.manager.utils.skopeo.SkopeoCommandManager;
import com.suse.manager.utils.skopeo.beans.ImageTags;
import com.suse.manager.utils.skopeo.beans.RepositoryImageList;
import com.suse.manager.webui.errors.NotFoundException;
import com.suse.manager.webui.utils.gson.ResultJson;
import com.suse.utils.Json;

import com.google.gson.Gson;
import com.google.gson.JsonObject;

import org.apache.commons.lang3.StringUtils;
import org.apache.http.HttpStatus;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

import spark.Request;
import spark.Response;

/**
 * Spark controller class for image store pages and API endpoints.
 */
public class ImageManagementApiController {

    private static final Gson GSON = Json.GSON;
    private static final Role ADMIN_ROLE = RoleFactory.IMAGE_ADMIN;
    private static Logger log = LogManager.getLogger(ImageManagementApiController.class);

    private ImageManagementApiController() { }

    /**
     * Invoked from Router. Initialize routes for Systems Views.
     *
     */
    public static void initRoutes() {
        get("/manager/api/cm/imagestores/listimages/:id", withImageAdmin(ImageManagementApiController::getStoreImages));
        get("/manager/api/cm/imagestores/imagetags/:id", withImageAdmin(ImageManagementApiController::getImageTags));

        get("/manager/api/cm/imagesync", withImageAdmin(ImageManagementApiController::list));
        get("/manager/api/cm/imagesync/:id", withImageAdmin(ImageManagementApiController::getSingle));
        post("/manager/api/cm/imagesync/create", withImageAdmin(ImageManagementApiController::create));
        post("/manager/api/cm/imagesync/update/:id", withImageAdmin(ImageManagementApiController::update));
        post("/manager/api/cm/imagesync/delete", withImageAdmin(ImageManagementApiController::delete));
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
    public static Object list(Request req, Response res, User user) {
        // FIXME: don't create an instance here, pass it in as parameter
        List<ImageSyncProject> imageSyncProjects = new ImageSyncManager().listProjects(user);
        return json(res, getJsonList(imageSyncProjects));
    }

    private static Object getSingle(Request request1, Response response2, User user3) {
        return null;
    }

    private static Object create(Request request1, Response response2, User user3) {
        return null;
    }

    private static Object update(Request request1, Response response2, User user3) {
        return null;
    }

    private static Object delete(Request request1, Response response2, User user3) {
        return null;
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
            json.addProperty("target", project.getDestinationImageStore().getLabel());
            return json;
        }).collect(Collectors.toList());
    }
}
