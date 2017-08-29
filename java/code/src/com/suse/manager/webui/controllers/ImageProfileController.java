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
import com.google.gson.JsonObject;
import com.google.gson.JsonParseException;
import com.redhat.rhn.domain.image.DockerfileProfile;
import com.redhat.rhn.domain.image.ImageProfile;
import com.redhat.rhn.domain.image.ImageProfileFactory;
import com.redhat.rhn.domain.image.ImageStore;
import com.redhat.rhn.domain.image.ImageStoreFactory;
import com.redhat.rhn.domain.image.ProfileCustomDataValue;
import com.redhat.rhn.domain.org.CustomDataKey;
import com.redhat.rhn.domain.org.Org;
import com.redhat.rhn.domain.role.Role;
import com.redhat.rhn.domain.role.RoleFactory;
import com.redhat.rhn.domain.token.ActivationKey;
import com.redhat.rhn.domain.token.ActivationKeyFactory;
import com.redhat.rhn.domain.token.Token;
import com.redhat.rhn.domain.user.User;
import com.redhat.rhn.manager.token.ActivationKeyManager;
import com.suse.manager.webui.utils.gson.ChannelsJson;
import com.suse.manager.webui.utils.gson.ImageProfileCreateRequest;
import com.suse.manager.webui.utils.gson.ImageProfileJson;
import com.suse.manager.webui.utils.gson.JsonResult;
import com.suse.utils.Json;
import org.apache.commons.lang.StringUtils;
import org.apache.http.HttpStatus;
import spark.ModelAndView;
import spark.Request;
import spark.Response;
import spark.Spark;

import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;

import static com.suse.manager.webui.utils.SparkApplicationHelper.json;

/**
 * Spark controller class for image profile pages and API endpoints.
 */
public class ImageProfileController {

    private static final Gson GSON = Json.GSON;
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
        data.put("isAdmin", user.hasRole(ADMIN_ROLE));
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
        data.put("activationKeys", getActivationKeys(user));
        data.put("customDataKeys", getCustomDataKeys(user.getOrg()));
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
        Long profileId;
        try {
            profileId = Long.parseLong(req.params("id"));
        }
        catch (NumberFormatException e) {
            Spark.halt(HttpStatus.SC_NOT_FOUND);
            return null;
        }

        Optional<ImageProfile> profile =
                ImageProfileFactory.lookupByIdAndOrg(profileId, user.getOrg());
        if (!profile.isPresent()) {
            res.redirect("/rhn/manager/cm/imageprofiles/create");
        }

        Map<String, Object> data = new HashMap<>();
        data.put("profileId", profileId);
        data.put("activationKeys", getActivationKeys(user));
        data.put("customDataKeys", getCustomDataKeys(user.getOrg()));
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
        List<Long> ids;
        try {
            ids = Arrays.asList(GSON.fromJson(req.body(), Long[].class));
        }
        catch (JsonParseException e) {
            Spark.halt(HttpStatus.SC_BAD_REQUEST);
            return null;
        }

        List<ImageProfile> profiles =
                ImageProfileFactory.lookupByIdsAndOrg(ids, user.getOrg());
        if (profiles.size() < ids.size()) {
            return json(res, new JsonResult<>(false, "not_found"));
        }

        profiles.forEach(ImageProfileFactory::delete);
        return json(res, new JsonResult<>(true, profiles.size()));
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

        ActivationKey ak = ActivationKeyFactory.lookupByKey(key);
        ChannelsJson channelsJson = getChannelsJson(ak);
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
        Long profileId;
        try {
            profileId = Long.parseLong(req.params("id"));
        }
        catch (NumberFormatException e) {
            Spark.halt(HttpStatus.SC_NOT_FOUND);
            return null;
        }

        Optional<ImageProfile> profile =
                ImageProfileFactory.lookupByIdAndOrg(profileId, user.getOrg());

        return profile.map(
                p -> json(res, new JsonResult(true, ImageProfileJson.fromImageProfile(p))))
                .orElseGet(() -> json(res, new JsonResult(false, "not_found")));
    }

    /**
     * Processes a GET request to get a single image profile object by label
     *
     * @param req the request object
     * @param res the response object
     * @param user the authorized user
     * @return the result JSON object
     */
    public static Object getSingleByLabel(Request req, Response res, User user) {
        String profileLabel = req.params("label");

        Optional<ImageProfile> profile =
                ImageProfileFactory.lookupByLabelAndOrg(profileLabel, user.getOrg());

        return profile.map(
                p -> json(res, new JsonResult(true, ImageProfileJson.fromImageProfile(p))))
                .orElseGet(() -> json(res, new JsonResult(false, "not_found")));
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
        ImageProfileCreateRequest reqData;
        try {
            reqData = GSON.fromJson(req.body(), ImageProfileCreateRequest.class);
        }
        catch (JsonParseException e) {
            Spark.halt(HttpStatus.SC_BAD_REQUEST);
            return null;
        }

        Long profileId = Long.parseLong(req.params("id"));
        Optional<ImageProfile> profile =
                ImageProfileFactory.lookupByIdAndOrg(profileId, user.getOrg());

        JsonResult result = profile.map(p -> {
            if (p instanceof DockerfileProfile) {
                DockerfileProfile dp = (DockerfileProfile) p;

                //Throw NoSuchElementException if not found
                ImageStore store = ImageStoreFactory
                        .lookupBylabelAndOrg(reqData.getImageStore(), user.getOrg()).get();

                dp.setLabel(reqData.getLabel());
                dp.setPath(reqData.getPath());
                dp.setTargetStore(store);
                dp.setToken(getToken(reqData.getActivationKey()));

            }

            ImageProfileFactory.save(p);
            updateCustomDataValues(p, reqData, user);

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
        ImageProfileCreateRequest reqData;
        try {
            reqData = GSON.fromJson(req.body(), ImageProfileCreateRequest.class);
        }
        catch (JsonParseException e) {
            Spark.halt(HttpStatus.SC_BAD_REQUEST);
            return null;
        }

        ImageProfile profile;
        if (ImageProfile.TYPE_DOCKERFILE.equals(reqData.getImageType())) {
            //Throw NoSuchElementException if not found
            ImageStore store = ImageStoreFactory
                    .lookupBylabelAndOrg(reqData.getImageStore(), user.getOrg()).get();

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
        updateCustomDataValues(profile, reqData, user);

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
        json.addProperty("profileId", profile.getProfileId());
        json.addProperty("label", profile.getLabel());
        json.addProperty("imageType", profile.getImageType());
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
     * @param req the request object
     * @param res the response object
     * @param user the authorized user
     * @return a JSON string of the list of activation keys
     */
    public static String getActivationKeys(Request req, Response res, User user) {
        return json(res, getActivationKeyNames(user));
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

    private static List<String> getActivationKeyNames(User user) {
        return ActivationKeyManager.getInstance()
                .findAll(user)
                .stream().map(ActivationKey::getKey)
                .collect(Collectors.toList());
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
    private static ChannelsJson getChannelsJson(ActivationKey activationKey) {
        Token token = activationKey.getToken();

        ChannelsJson json = ChannelsJson.fromChannelSet(token.getChannels());
        json.setActivationKey(activationKey.getKey());

        return json;
    }

    /**
     * Get a list of custom data keys for a specific organization, as a JSON array.
     *
     * @param org the organization
     * @return a JSON array of custom data keys
     */
    private static String getCustomDataKeys(Org org) {
        return org.getCustomDataKeys().stream().map(k -> {
            JsonObject obj = new JsonObject();
            obj.addProperty("id", k.getId());
            obj.addProperty("label", k.getLabel());
            obj.addProperty("description", k.getDescription());
            return obj;
        }).collect(Collectors.collectingAndThen(Collectors.toList(), GSON::toJson));
    }

    /**
     * Updates custom data value entries for a specific image profile
     *
     * @param profile the image profile
     * @param reqData the request data object
     * @param user the authorized user
     */
    private static void updateCustomDataValues(ImageProfile profile,
            ImageProfileCreateRequest reqData, User user) {

        if (profile.getCustomDataValues() == null) {
            profile.setCustomDataValues(new HashSet<>());
        }

        Set<ProfileCustomDataValue> oldVals = profile.getCustomDataValues();
        Set<CustomDataKey> orgKeys = user.getOrg().getCustomDataKeys();

        // Loop through the values in the request
        Set<ProfileCustomDataValue> newValues =
                reqData.getCustomData().entrySet().stream().map(reqVal -> {

                    // Find the key in organization keys, ignore if not found.
                    return orgKeys.stream()
                            .filter(orgKey -> orgKey.getLabel().equals(reqVal.getKey()))
                            .findFirst().map(k -> {

                                // Find if we already have an entry for this key-val pair,
                                ProfileCustomDataValue val = oldVals.stream()
                                        .filter(oldVal -> oldVal.getKey().getLabel()
                                                .equals(reqVal.getKey()))
                                        .findFirst().orElseGet(() -> {
                                            // otherwise create a new one.
                                            ProfileCustomDataValue newVal =
                                                    new ProfileCustomDataValue();
                                            newVal.setProfile(profile);
                                            newVal.setCreator(k.getCreator());
                                            newVal.setKey(k);
                                            return newVal;
                                        });

                                // Save the individual value entry
                                // (For inserts and updates)
                                val.setValue(reqVal.getValue());
                                ImageProfileFactory.save(val);

                                return val;
                            });
                }).filter(Optional::isPresent).map(Optional::get)
                        .collect(Collectors.toSet());

        // Remove deleted values
        oldVals.stream()
                .filter(oldVal -> !newValues.stream()
                        .filter(newVal -> oldVal.getId().equals(newVal.getId())).findAny()
                        .isPresent())
                .forEach(ImageProfileFactory::delete);
    }
}
