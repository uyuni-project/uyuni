/**
 * Copyright (c) 2017 SUSE LLC
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
import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import com.redhat.rhn.domain.channel.Channel;
import com.redhat.rhn.domain.image.DockerfileProfile;
import com.redhat.rhn.domain.image.ImageProfile;
import com.redhat.rhn.domain.image.ImageProfileFactory;
import com.redhat.rhn.domain.image.ImageStore;
import com.redhat.rhn.domain.image.ImageStoreFactory;
import com.redhat.rhn.domain.role.Role;
import com.redhat.rhn.domain.role.RoleFactory;
import com.redhat.rhn.domain.token.ActivationKey;
import com.redhat.rhn.domain.token.ActivationKeyFactory;
import com.redhat.rhn.domain.token.Token;
import com.redhat.rhn.domain.user.User;
import com.redhat.rhn.manager.token.ActivationKeyManager;
import com.suse.manager.webui.utils.gson.ImageProfileCreateRequest;
import com.suse.manager.webui.utils.gson.JsonResult;
import org.apache.commons.lang.StringUtils;
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
 * Spark controller class for image profile pages and API endpoints.
 */
public class ImageProfileController {

    private static final Gson GSON = new Gson();
    private static final Role ADMIN_ROLE = RoleFactory.IMAGE_ADMIN;

    private ImageProfileController() { }

    /**
     * Returns a view to list image profiles
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
        return new ModelAndView(data, "content_management/list-profiles.jade");
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
        Map<String, Object> data = new HashMap<>();
        data.put("activation_keys", getActivationKeys(user));

        return new ModelAndView(data, "content_management/edit-profile.jade");
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
        Long profileId = Long.parseLong(req.params("id"));
        Optional<ImageProfile> profile =
                ImageProfileFactory.lookupByIdAndOrg(profileId, user.getOrg());
        if (!profile.isPresent()) {
            res.redirect("/rhn/manager/cm/imageprofiles/create");
        }

        Map<String, Object> data = new HashMap<>();
        data.put("profile_id", profileId);
        data.put("activation_keys", getActivationKeys(user));
        return new ModelAndView(data, "content_management/edit-profile.jade");
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

        JsonResult result =
                ImageProfileFactory.lookupByIdAndOrg(id, user.getOrg()).map(profile -> {
                    ImageProfileFactory.delete(profile);
                    return new JsonResult(true,
                            Collections.singletonList("delete_success"));
                }).orElseGet(() -> new JsonResult(false,
                        Collections.singletonList("not_found")));

        return json(res, result);
    }

    /**
     * Processes a GET request to get the channel information for a specific token
     *
     * @param req the request object
     * @param res the response object
     * @param user the authorized user
     * @return the result JSON object
     */
    public static Object getChannels(Request req, Response res, User user) {
        String key = req.params("token");

        JsonObject channelsJson = getChannelsJson(key);
        return json(res, channelsJson);
    }

    /**
     * Processes a GET request to get a single image profile object
     *
     * @param req the request object
     * @param res the response object
     * @param user the authorized user
     * @return the result JSON object
     */
    public static Object getSingle(Request req, Response res, User user) {
        Long profileId = Long.parseLong(req.params("id"));

        Optional<ImageProfile> profile =
                ImageProfileFactory.lookupByIdAndOrg(profileId, user.getOrg());

        // TODO: Use a TypeAdapter?
        return profile.map(p -> {
            JsonObject json = new JsonObject();
            json.addProperty("profile_id", p.getProfileId());
            json.addProperty("image_type", p.getImageType());
            json.addProperty("label", p.getLabel());

            if (p.getToken() != null) {
                ActivationKey ak = ActivationKeyFactory.lookupByToken(p.getToken());
                JsonObject akJson = new JsonObject();
                akJson.addProperty("id", ak.getId());
                akJson.addProperty("name", ak.getKey());

                JsonObject channelsJson = getChannelsJson(ak.getKey());

                akJson.add("channels", GSON.toJsonTree(channelsJson));
                json.add("activation_key", GSON.toJsonTree(akJson));
            }

            if (p instanceof DockerfileProfile) {
                DockerfileProfile dp = (DockerfileProfile) p;
                json.addProperty("path", dp.getPath());
                json.addProperty("store", dp.getTargetStore().getLabel());
            }

            return json(res, new JsonResult(true, json));
        }).orElseGet(() -> json(res, new JsonResult(false, "not_found")));
    }

    /**
     * Processes a GET request to get a list of all image profile objects
     *
     * @param req the request object
     * @param res the response object
     * @param user the authorized user
     * @return the result JSON object
     */
    public static Object list(Request req, Response res, User user) {
        List<JsonObject> result =
                getJsonList(ImageProfileFactory.listImageProfiles(user.getOrg()));
        return json(res, result);
    }

    /**
     * Processes a POST request to update an existing image profile
     *
     * @param req the request object
     * @param res the response object
     * @param user the authorized user
     * @return the result JSON object
     */
    public static Object update(Request req, Response res, User user) {
        ImageProfileCreateRequest reqData =
                GSON.fromJson(req.body(), ImageProfileCreateRequest.class);

        Long profileId = Long.parseLong(req.params("id"));
        Optional<ImageProfile> profile =
                ImageProfileFactory.lookupByIdAndOrg(profileId, user.getOrg());

        JsonResult result = profile.map(p -> {
            if (p instanceof DockerfileProfile) {
                DockerfileProfile dp = (DockerfileProfile) p;
                ImageStore store = ImageStoreFactory
                        .lookupBylabelAndOrg(reqData.getStoreLabel(), user.getOrg());

                dp.setLabel(reqData.getLabel());
                dp.setPath(reqData.getPath());
                dp.setTargetStore(store);
                dp.setToken(getToken(reqData.getActivationKey()));
            }

            ImageProfileFactory.save(p);

            return new JsonResult(true);
        }).orElseGet(() -> new JsonResult(false, "not_found"));

        return json(res, result);
    }

    /**
     * Processes a POST request to create a new image profile
     *
     * @param req the request object
     * @param res the response object
     * @param user the authorized user
     * @return the result JSON object
     */
    public static Object create(Request req, Response res, User user) {
        ImageProfileCreateRequest reqData =
                GSON.fromJson(req.body(), ImageProfileCreateRequest.class);

        ImageProfile profile;
        if ("dockerfile".equals(reqData.getImageType())) {
            ImageStore store = ImageStoreFactory
                    .lookupBylabelAndOrg(reqData.getStoreLabel(), user.getOrg());
            DockerfileProfile dockerfileProfile = new DockerfileProfile();

            dockerfileProfile.setLabel(reqData.getLabel());
            dockerfileProfile.setPath(reqData.getPath());
            dockerfileProfile.setTargetStore(store);
            dockerfileProfile.setOrg(user.getOrg());
            dockerfileProfile.setToken(getToken(reqData.getActivationKey()));

            profile = dockerfileProfile;
        }
        else {
            return json(res, new JsonResult(false, "invalid_type"));
        }

        ImageProfileFactory.save(profile);
        return json(res, new JsonResult(true));
    }

    /**
     * Creates a JSON object for an {@link ImageProfile} instance
     *
     * @param profile the image profile instance
     * @return the JSON object
     */
    private static JsonObject getJsonObject(ImageProfile profile) {
        JsonObject json = new JsonObject();
        json.addProperty("profile_id", profile.getProfileId());
        json.addProperty("label", profile.getLabel());
        json.addProperty("image_type", profile.getImageType());
        return json;
    }

    /**
     * Creates a list of JSON objects for a list of {@link ImageProfile} instances
     *
     * @param profileList the image profile list
     * @return the list of JSON objects
     */
    private static List<JsonObject> getJsonList(List<ImageProfile> profileList) {
        return profileList.stream().map(ImageProfileController::getJsonObject)
                .collect(Collectors.toList());
    }

    /**
     * Gets a list of activation keys available to the user, as a JSON string
     *
     * @param user the user
     * @return a JSON string of the list of activation keys
     */
    private static String getActivationKeys(User user) {
        ActivationKeyManager akm = ActivationKeyManager.getInstance();
        return GSON.toJson(akm.findAll(user).stream().map(ActivationKey::getKey)
                .collect(Collectors.toList()));
    }

    /**
     * Gets the Token instance from activation key string, or null, if a null or empty
     * string is provided.
     *
     * @param activationKey the key string, or null/empty string
     * @return the token instance, or null if no key is provided
     */
    private static Token getToken(String activationKey) {
        if (StringUtils.isEmpty(activationKey)) {
            return null;
        }

        return ActivationKeyFactory.lookupByKey(activationKey).getToken();
    }

    /**
     * Gets a complex JSON object containing channel information for a specific
     * activation key
     *
     * @param activationKey the activation key
     * @return the JSON object
     */
    private static JsonObject getChannelsJson(String activationKey) {
        Token token = getToken(activationKey);

        JsonObject channelsJson = new JsonObject();
        JsonArray childChannelsJson = new JsonArray();

        channelsJson.addProperty("activation_key", activationKey);

        for (Channel ch : token.getChannels()) {
            JsonObject chJson = new JsonObject();
            chJson.addProperty("id", ch.getId());
            chJson.addProperty("name", ch.getLabel());

            if (ch.isBaseChannel()) {
                channelsJson.add("base", GSON.toJsonTree(chJson));
            }
            else {
                childChannelsJson.add(chJson);
            }
        }
        channelsJson.add("children", childChannelsJson);

        return channelsJson;
    }
}
