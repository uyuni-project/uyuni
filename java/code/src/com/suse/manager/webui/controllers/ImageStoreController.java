/**
 * Copyright (c) 2015 SUSE LLC
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
package com.suse.manager.webui.controllers;

import static com.suse.manager.webui.utils.SparkApplicationHelper.json;

import com.redhat.rhn.domain.credentials.Credentials;
import com.redhat.rhn.domain.credentials.CredentialsFactory;
import com.redhat.rhn.domain.image.ImageStore;
import com.redhat.rhn.domain.image.ImageStoreFactory;
import com.redhat.rhn.domain.image.ImageStoreType;
import com.redhat.rhn.domain.image.OSImageStoreUtils;
import com.redhat.rhn.domain.role.Role;
import com.redhat.rhn.domain.role.RoleFactory;
import com.redhat.rhn.domain.user.User;
import com.suse.manager.webui.errors.NotFoundException;
import com.suse.manager.webui.utils.gson.ImageStoreCreateRequest;
import com.suse.manager.webui.utils.gson.ResultJson;
import com.suse.utils.Json;

import com.google.gson.Gson;
import com.google.gson.JsonObject;
import com.google.gson.JsonParseException;
import org.apache.http.HttpStatus;
import org.apache.log4j.Logger;

import java.util.Arrays;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;
import spark.ModelAndView;
import spark.Request;
import spark.Response;
import spark.Spark;

/**
 * Spark controller class for image store pages and API endpoints.
 */
public class ImageStoreController {

    private static final Gson GSON = Json.GSON;
    private static final Role ADMIN_ROLE = RoleFactory.IMAGE_ADMIN;
    private static Logger log = Logger.getLogger(ImageStoreController.class);

    private ImageStoreController() { }

    /**
     * Returns a view to list image stores
     *
     * @param req the request object
     * @param res the response object
     * @param user the authorized user
     * @return the model and view
     */
    public static ModelAndView listView(Request req, Response res, User user) {
        Map<String, Object> data = new HashMap<>();
        data.put("is_admin", user.hasRole(ADMIN_ROLE));
        return new ModelAndView(data, "templates/content_management/list-stores.jade");
    }

    /**
     * Returns a view to display create form
     *
     * @param req the request object
     * @param res the response object
     * @param user the authorized user
     * @return the model and view
     */
    public static ModelAndView createView(Request req, Response res, User user) {
        return new ModelAndView(new HashMap<String, Object>(),
                "templates/content_management/edit-store.jade");
    }

    /**
     * Returns a view to display update form
     *
     * @param req the request object
     * @param res the response object
     * @param user the authorized user
     * @return the model and view
     */
    public static ModelAndView updateView(Request req, Response res, User user) {
        Long storeId;
        try {
            storeId = Long.parseLong(req.params("id"));
        }
        catch (NumberFormatException e) {
            throw new NotFoundException();
        }

        Optional<ImageStore> store =
                ImageStoreFactory.lookupByIdAndOrg(storeId, user.getOrg());
        if (!store.isPresent()) {
            res.redirect("/rhn/manager/cm/imagestores/create");
        }
        else if (store.get().getStoreType().equals(ImageStoreFactory.TYPE_OS_IMAGE)) {
            log.warn("Updating OS Image store is not allowed.");
            throw Spark.halt(HttpStatus.SC_BAD_REQUEST);
        }


        Map<String, Object> data = new HashMap<>();
        data.put("store_id", storeId);
        return new ModelAndView(data, "templates/content_management/edit-store.jade");
    }

    /**
     * Processes a POST request to delete a list of image stores
     *
     * @param req the request object
     * @param res the response object
     * @param user the authorized user
     * @return the result JSON object
     */
    public static Object delete(Request req, Response res, User user) {
        List<Long> ids;
        try {
            ids = Arrays.asList(GSON.fromJson(req.body(), Long[].class));
        }
        catch (JsonParseException e) {
            throw Spark.halt(HttpStatus.SC_BAD_REQUEST);
        }

        List<ImageStore> stores = ImageStoreFactory.lookupByIdsAndOrg(ids, user.getOrg());
        if (stores.size() < ids.size()) {
            return json(res, ResultJson.error("not_found"));
        }

        stores.forEach(ImageStoreFactory::delete);
        return json(res, ResultJson.success(stores.size()));
    }

    /**
     * Processes a GET request to get a single image store object
     *
     * @param req the request object
     * @param res the response object
     * @param user the authorized user
     * @return the result JSON object
     */
    public static Object getSingle(Request req, Response res, User user) {
        Long storeId;
        try {
            storeId = Long.parseLong(req.params("id"));
        }
        catch (NumberFormatException e) {
            throw new NotFoundException();
        }

        Optional<ImageStore> store = ImageStoreFactory.lookupByIdAndOrg(storeId,
                user.getOrg());

        return store.map(s -> {
            JsonObject json = new JsonObject();
            json.addProperty("id", s.getId());
            json.addProperty("label", s.getLabel());
            json.addProperty("uri", s.getUri());
            json.addProperty("storeType", s.getStoreType().getLabel());

            if (s.getCreds() != null && s.getCreds().getType().getLabel().equals(
                    Credentials.TYPE_REGISTRY)) {
                Credentials dc = s.getCreds();
                json.addProperty("username", dc.getUsername());
                json.addProperty("password", dc.getPassword());
                json.addProperty("useCredentials", true);
            }
            else {
                json.addProperty("useCredentials", false);
            }

            return json(res, ResultJson.success(json));
        }).orElseGet(() -> json(res, ResultJson.error("not_found")));
    }

    /**
     * Processes a GET request to get a single image store object by label
     *
     * @param req the request object
     * @param res the response object
     * @param user the authorized user
     * @return the result JSON object
     */
    public static Object getSingleByLabel(Request req, Response res, User user) {
        String storeLabel = req.params("label");

        Optional<ImageStore> store = ImageStoreFactory.lookupBylabelAndOrg(storeLabel,
                user.getOrg());

        return store.map(s -> {
            String uriPrefix = "";
            if (ImageStoreFactory.TYPE_OS_IMAGE.equals(s.getStoreType())) {
                uriPrefix = OSImageStoreUtils.getOSImageStoreURI();
            }

            JsonObject json = new JsonObject();
            json.addProperty("id", s.getId());
            json.addProperty("label", s.getLabel());
            json.addProperty("uri", uriPrefix + s.getUri());
            json.addProperty("storeType", s.getStoreType().getLabel());

            if (s.getCreds() != null && s.getCreds().getType().getLabel().equals(
                    Credentials.TYPE_REGISTRY)) {
                Credentials dc = s.getCreds();
                json.addProperty("username", dc.getUsername());
                json.addProperty("password", dc.getPassword());
                json.addProperty("useCredentials", true);
            }
            else {
                json.addProperty("useCredentials", false);
            }

            return json(res, ResultJson.success(json));
        }).orElseGet(() -> json(res, ResultJson.error("not_found")));
    }

    /**
     * Processes a GET request to get a list of all image store objects of a specific type
     *
     * @param req the request object
     * @param res the response object
     * @param user the authorized user
     * @return the result JSON object
     */
    public static Object listAllWithType(Request req, Response res, User user) {
        String type = req.params("type");
        List<ImageStore> imageStores = ImageStoreFactory.listByTypeLabelAndOrg(type, user.getOrg());

        String uriPrefix = "";
        if (ImageStoreFactory.TYPE_OS_IMAGE.getLabel().equals(type)) {
            uriPrefix = OSImageStoreUtils.getOSImageStoreURI();
        }

        return json(res, getJsonList(imageStores, uriPrefix));
    }

    /**
     * Processes a GET request to get a list of all image store objects
     *
     * @param req the request object
     * @param res the response object
     * @param user the authorized user
     * @return the result JSON object
     */
    public static Object list(Request req, Response res, User user) {
        // Only list 'registry' stores
        List<ImageStore> imageStores = ImageStoreFactory.listImageStores(user.getOrg())
                .stream()
                .filter(s -> s.getStoreType().equals(ImageStoreFactory.TYPE_REGISTRY))
                .collect(Collectors.toList());

        return json(res, getJsonList(imageStores));
    }

    /**
     * Processes a POST request to update an existing image store
     *
     * @param req the request object
     * @param res the response object
     * @param user the authorized user
     * @return the result JSON object
     */
    public static Object update(Request req, Response res, User user) {
        ImageStoreCreateRequest updateRequest;
        try {
            updateRequest = GSON.fromJson(req.body(), ImageStoreCreateRequest.class);
        }
        catch (JsonParseException e) {
            throw Spark.halt(HttpStatus.SC_BAD_REQUEST);
        }

        Long storeId = Long.parseLong(req.params("id"));
        Optional<ImageStore> store =
                ImageStoreFactory.lookupByIdAndOrg(storeId, user.getOrg());

        ResultJson result = store.map(s -> {

            if (s.getStoreType().equals(ImageStoreFactory.TYPE_OS_IMAGE)) {
                log.warn("Updating OS Image store is not allowed.");
                throw Spark.halt(HttpStatus.SC_BAD_REQUEST);
            }

            s.setLabel(updateRequest.getLabel());
            s.setUri(updateRequest.getUri());
            s.setOrg(user.getOrg());
            setStoreCredentials(s, updateRequest);

            ImageStoreFactory.save(s);

            return ResultJson.success();
        }).orElseGet(() -> ResultJson.error("not_found"));

        return json(res, result);
    }

    /**
     * Processes a POST request to create a new image store
     *
     * @param req the request object
     * @param res the response object
     * @param user the authorized user
     * @return the result JSON object
     */
    public static Object create(Request req, Response res, User user) {
        ImageStoreCreateRequest createRequest;
        try {
            createRequest = GSON.fromJson(req.body(), ImageStoreCreateRequest.class);
        }
        catch (JsonParseException e) {
            throw Spark.halt(HttpStatus.SC_BAD_REQUEST);
        }

        ImageStore imageStore = new ImageStore();
        imageStore.setLabel(createRequest.getLabel());
        imageStore.setUri(createRequest.getUri());
        setStoreCredentials(imageStore, createRequest);

        Optional<ImageStoreType> storeType = ImageStoreFactory.lookupStoreTypeByLabel(createRequest.getStoreType());

        if (!storeType.isPresent()) {
            log.warn("Invalid store type: " + createRequest.getStoreType());
            throw Spark.halt(HttpStatus.SC_BAD_REQUEST);
        }

        if (storeType.get().equals(ImageStoreFactory.TYPE_OS_IMAGE)) {
            log.warn("Creating OS Image store is not allowed.");
            throw Spark.halt(HttpStatus.SC_BAD_REQUEST);
        }

        imageStore.setStoreType(storeType.get());
        imageStore.setOrg(user.getOrg());

        ImageStoreFactory.save(imageStore);

        return json(res, ResultJson.success());
    }

    /**
     * Creates a list of JSON objects for a list of {@link ImageStore} instances
     *
     * @param imageStoreList the image store list
     * @return the list of JSON objects
     */
    private static List<JsonObject> getJsonList(List<ImageStore> imageStoreList) {
        return getJsonList(imageStoreList, "");
    }

    /**
     * Creates a list of JSON objects for a list of {@link ImageStore} instances
     *
     * @param imageStoreList the image store list
     * @return the list of JSON objects
     */
    private static List<JsonObject> getJsonList(List<ImageStore> imageStoreList, String uriPrefix) {
        return imageStoreList.stream().map(imageStore -> {
            JsonObject json = new JsonObject();
            json.addProperty("id", imageStore.getId());
            json.addProperty("label", imageStore.getLabel());
            json.addProperty("uri", uriPrefix + imageStore.getUri());
            json.addProperty("type", imageStore.getStoreType().getLabel());
            return json;
        }).collect(Collectors.toList());
    }

    private static void setStoreCredentials(ImageStore store, ImageStoreCreateRequest request) {
        if (request.isUseCredentials()) {
            Credentials dc = store.getCreds() != null ?
                    store.getCreds() : CredentialsFactory.createRegistryCredentials();
            dc.setUsername(request.getUsername());
            dc.setPassword(request.getPassword());
            dc.setModified(new Date());

            store.setCreds(dc);
        }
        else if (store.getCreds() != null) {
            CredentialsFactory.removeCredentials(store.getCreds());
            store.setCreds(null);
        }
    }
}
