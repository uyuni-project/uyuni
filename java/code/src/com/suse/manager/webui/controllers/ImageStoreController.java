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

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonObject;

import com.redhat.rhn.domain.credentials.Credentials;
import com.redhat.rhn.domain.credentials.CredentialsFactory;
import com.redhat.rhn.domain.credentials.CredentialsType;
import com.redhat.rhn.domain.credentials.DockerCredentials;
import com.redhat.rhn.domain.image.ImageStore;
import com.redhat.rhn.domain.image.ImageStoreFactory;
import com.redhat.rhn.domain.role.Role;
import com.redhat.rhn.domain.role.RoleFactory;
import com.redhat.rhn.domain.user.User;
import com.suse.manager.webui.utils.gson.DockerRegistryCreateRequest;
import com.suse.manager.webui.utils.gson.JsonResult;
import spark.ModelAndView;
import spark.Request;
import spark.Response;

import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

import static com.suse.manager.webui.utils.SparkApplicationHelper.json;

/**
 * Spark controller class for image store pages and API endpoints.
 */
public class ImageStoreController {

    private static final Gson GSON = new GsonBuilder().create();
    private static final Role ADMIN_ROLE = RoleFactory.IMAGE_ADMIN;

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
        data.put("pageSize", user.getPageSize());
        data.put("is_admin", user.hasRole(ADMIN_ROLE));
        return new ModelAndView(data, "content_management/list-stores.jade");
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
                "content_management/edit-store.jade");
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
        Long storeId = Long.parseLong(req.params("id"));
        Optional<ImageStore> store =
                ImageStoreFactory.lookupByIdAndOrg(storeId, user.getOrg());
        if (!store.isPresent()) {
            res.redirect("/rhn/manager/cm/imagestores/create");
        }

        Map<String, Object> data = new HashMap<>();
        data.put("store_id", storeId);
        return new ModelAndView(data, "content_management/edit-store.jade");
    }

    /**
     * Processes a DELETE request
     *
     * @param req the request object
     * @param res the response object
     * @param user the authorized user
     * @return the result JSON object
     */
    public static Object delete(Request req, Response res, User user) {
        Long id = Long.parseLong(req.params("id"));

        Optional<ImageStore> maybeStore =
                ImageStoreFactory.lookupByIdAndOrg(id, user.getOrg());
        JsonResult jsonResult = maybeStore.map(imageStore -> {
            ImageStoreFactory.delete(imageStore);
            return new JsonResult(true, Collections.singletonList("delete_success"));
        }).orElseGet(() -> new JsonResult(false, Collections.singletonList("not_found")));

        return json(res, jsonResult);
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
        Long storeId = Long.parseLong(req.params("id"));

        Optional<ImageStore> store = ImageStoreFactory.lookupByIdAndOrg(storeId,
                user.getOrg());

        return store.map(s -> {
            JsonObject json = new JsonObject();
            json.addProperty("id", s.getId());
            json.addProperty("label", s.getLabel());
            json.addProperty("uri", s.getUri());
            json.addProperty("store_type", s.getStoreType().getLabel());

            if (s.getCreds() != null && s.getCreds() instanceof DockerCredentials) {
                DockerCredentials dc = (DockerCredentials) s.getCreds();
                JsonObject creds = new JsonObject();
                creds.addProperty("email", dc.getEmail());
                creds.addProperty("username", dc.getUsername());
                creds.addProperty("password", dc.getPassword());
                json.add("credentials", creds);
            }

            return json(res, new JsonResult(true, json));
        }).orElseGet(() -> json(res, new JsonResult(false, "not_found")));
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
        List<ImageStore> imageStores =
                ImageStoreFactory.listByTypeLabelAndOrg(type, user.getOrg());

        return json(res, getJsonList(imageStores));
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
        List<ImageStore> imageStores = ImageStoreFactory.listImageStores(user.getOrg());
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
        DockerRegistryCreateRequest createRequest =
                GSON.fromJson(req.body(), DockerRegistryCreateRequest.class);

        Long storeId = Long.parseLong(req.params("id"));
        Optional<ImageStore> store =
                ImageStoreFactory.lookupByIdAndOrg(storeId, user.getOrg());

        JsonResult result = store.map(s -> {
            s.setLabel(createRequest.getLabel());
            s.setUri(createRequest.getUri());
            s.setOrg(user.getOrg());
            setStoreDockerCredentials(s, createRequest.getCredentials());

            ImageStoreFactory.save(s);

            return new JsonResult(true);
        }).orElseGet(() -> new JsonResult(false, "not_found"));

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
        DockerRegistryCreateRequest createRequest =
                GSON.fromJson(req.body(), DockerRegistryCreateRequest.class);

        ImageStore imageStore = new ImageStore();
        imageStore.setLabel(createRequest.getLabel());
        imageStore.setUri(createRequest.getUri());
        setStoreDockerCredentials(imageStore, createRequest.getCredentials());

        imageStore.setStoreType(ImageStoreFactory.lookupStoreTypeByLabel("dockerreg"));
        imageStore.setOrg(user.getOrg());

        ImageStoreFactory.save(imageStore);

        return json(res, new JsonResult(true));
    }

    /**
     * Creates a list of JSON objects for a list of {@link ImageStore} instances
     *
     * @param imageStoreList the image store list
     * @return the list of JSON objects
     */
    private static List<JsonObject> getJsonList(List<ImageStore> imageStoreList) {
        return imageStoreList.stream().map(imageStore -> {
            JsonObject json = new JsonObject();
            json.addProperty("id", imageStore.getId());
            json.addProperty("label", imageStore.getLabel());
            json.addProperty("uri", imageStore.getUri());
            json.addProperty("type", imageStore.getStoreType().getName());
            return json;
        }).collect(Collectors.toList());
    }

    private static void setStoreDockerCredentials(ImageStore store,
            DockerRegistryCreateRequest.CredentialsJson credentials) {
        if (credentials != null) {
            DockerCredentials dc = store.getCreds() != null ?
                    (DockerCredentials) store.getCreds() : new DockerCredentials();
            dc.setEmail(credentials.getEmail());
            dc.setUsername(credentials.getUsername());
            dc.setPassword(credentials.getPassword());
            CredentialsType docker =
                    CredentialsFactory.findCredentialsTypeByLabel(
                            Credentials.TYPE_REGISTRY);
            dc.setType(docker);

            store.setCreds(dc);
        }
        else if (store.getCreds() != null) {
            CredentialsFactory.removeCredentials(store.getCreds());
            store.setCreds(null);
        }
    }
}
