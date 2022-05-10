/*
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
package com.redhat.rhn.frontend.xmlrpc.image.profile;

import com.redhat.rhn.common.conf.Config;
import com.redhat.rhn.common.conf.ConfigDefaults;
import com.redhat.rhn.domain.image.DockerfileProfile;
import com.redhat.rhn.domain.image.ImageProfile;
import com.redhat.rhn.domain.image.ImageProfileFactory;
import com.redhat.rhn.domain.image.ImageStore;
import com.redhat.rhn.domain.image.ImageStoreFactory;
import com.redhat.rhn.domain.image.KiwiProfile;
import com.redhat.rhn.domain.image.ProfileCustomDataValue;
import com.redhat.rhn.domain.org.CustomDataKey;
import com.redhat.rhn.domain.token.ActivationKey;
import com.redhat.rhn.domain.token.ActivationKeyFactory;
import com.redhat.rhn.domain.user.User;
import com.redhat.rhn.frontend.xmlrpc.BaseHandler;
import com.redhat.rhn.frontend.xmlrpc.InvalidParameterException;
import com.redhat.rhn.frontend.xmlrpc.NoSuchImageProfileException;
import com.redhat.rhn.frontend.xmlrpc.NoSuchImageStoreException;

import com.suse.manager.api.ReadOnly;

import org.apache.commons.lang3.StringUtils;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.NoSuchElementException;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * ImageProfileHandler
 * @apidoc.namespace image.profile
 * @apidoc.doc Provides methods to access and modify image profiles.
 */
public class ImageProfileHandler extends BaseHandler {

    /**
     * List available Image Profile Types
     * @param loggedInUser The current user
     * @return Array of ImageProfileType strings
     *
     * @apidoc.doc List available image store types
     * @apidoc.param #session_key()
     * @apidoc.returntype #array_single("string", "the list of image profile types")
     */
    @ReadOnly
    public List<String> listImageProfileTypes(User loggedInUser) {
        ensureImageAdmin(loggedInUser);
        List<String> imageTypes = new ArrayList<>();
        imageTypes.add(ImageProfile.TYPE_DOCKERFILE);
        if (Config.get().getBoolean(ConfigDefaults.KIWI_OS_IMAGE_BUILDING_ENABLED)) {
            imageTypes.add(ImageProfile.TYPE_KIWI);
        }
        return imageTypes;
    }

    /**
     * List all configured image profiles visible for the logged in user
     * @param loggedInUser The current User
     * @return Array of ImageProfile Objects
     *
     * @apidoc.doc List available image profiles
     * @apidoc.param #session_key()
     * @apidoc.returntype #return_array_begin() $ImageProfileSerializer #array_end()
     */
    @ReadOnly
    public List<ImageProfile> listImageProfiles(User loggedInUser) {
        ensureImageAdmin(loggedInUser);
        return ImageProfileFactory.listImageProfiles(loggedInUser.getOrg());
    }

    /**
     * Get Image Profile Details
     * @param loggedInUser The Current User
     * @param label the Image Profile Label
     * @return ImageProfile Object
     *
     * @apidoc.doc Get details of an image profile
     * @apidoc.param #session_key()
     * @apidoc.param #param("string", "label")
     * @apidoc.returntype $ImageProfileSerializer
     */
    @ReadOnly
    public ImageProfile getDetails(User loggedInUser, String label) {
        ensureImageAdmin(loggedInUser);
        return getValidImageProfile(loggedInUser, label);
    }

    /**
     * Create a new Image Profile
     * @param loggedInUser the current User
     * @param label the label
     * @param type the profile type label
     * @param storeLabel the image store label
     * @param path the path or git uri to the source
     * @param activationKey the activation key which defines the channels
     * @param kiwiOptions command line options passed to Kiwi
     * @return 1 on success
     *
     * @apidoc.doc Create a new image profile
     * @apidoc.param #session_key()
     * @apidoc.param #param("string", "label")
     * @apidoc.param #param("string", "type")
     * @apidoc.param #param("string", "storeLabel")
     * @apidoc.param #param("string", "path")
     * @apidoc.param #param_desc("string", "activationKey", "optional")
     * @apidoc.param #param("string", "kiwiOptions")
     * @apidoc.returntype #return_int_success()
     */
    public int create(User loggedInUser, String label, String type, String storeLabel,
            String path, String activationKey, String kiwiOptions) {
        ensureImageAdmin(loggedInUser);

        if (StringUtils.isEmpty(label)) {
            throw new InvalidParameterException("Label cannot be empty.");
        }
        if (StringUtils.contains(label, ':')) {
            throw new InvalidParameterException("Label cannot contain colons (:).");
        }
        else if (ImageProfileFactory.lookupByLabelAndOrg(label, loggedInUser.getOrg()).isPresent()) {
            throw new InvalidParameterException("Image already exists.");
        }
        if (StringUtils.isEmpty(type)) {
            throw new InvalidParameterException("Type cannot be empty.");
        }
        if (StringUtils.isEmpty(storeLabel)) {
            throw new InvalidParameterException("Store label cannot be empty.");
        }
        if (StringUtils.isEmpty(path)) {
            throw new InvalidParameterException("Path cannot be empty.");
        }
        if (!listImageProfileTypes(loggedInUser).contains(type)) {
            throw new InvalidParameterException("Type does not exist.");
        }
        Optional<ActivationKey> ak = Optional.empty();
        if (StringUtils.isNotEmpty(activationKey)) {
            ak = Optional.ofNullable(ActivationKeyFactory.lookupByKey(activationKey));
            if (!ak.isPresent()) {
                throw new InvalidParameterException("Activation key does not exist.");
            }
            ak.ifPresent(akey -> {
                if (akey.getBaseChannel() == null) {
                    throw new InvalidParameterException(
                            "Activation key does not have any base channel associated " +
                                    "(do not use SUSE Manager default).");
                }
            });
        }
        else if (ImageProfile.TYPE_KIWI.equals(type)) {
            throw new InvalidParameterException("Activation key cannot be empty for Kiwi profiles.");
        }

        ImageStore store;
        try {
            store = ImageStoreFactory.lookupBylabelAndOrg(storeLabel,
                    loggedInUser.getOrg()).get();
        }
        catch (NoSuchElementException e) {
            throw new NoSuchImageStoreException();
        }

        ImageProfile profile;
        // Set type-specific properties
        if (ImageProfile.TYPE_DOCKERFILE.equals(type)) {
            DockerfileProfile dockerfileProfile = new DockerfileProfile();
            dockerfileProfile.setPath(path);
            profile = dockerfileProfile;
        }
        else if (ImageProfile.TYPE_KIWI.equals(type)) {
            KiwiProfile kiwiProfile = new KiwiProfile();
            kiwiProfile.setPath(path);
            kiwiProfile.setKiwiOptions(kiwiOptions);
            profile = kiwiProfile;
        }
        else {
            throw new UnsupportedOperationException();
        }

        if (!ImageProfileFactory.getStoreTypeForProfile(profile).equals(store.getStoreType())) {
            throw new InvalidParameterException(String.format("Invalid store for profile type: '%s'", type));
        }

        // Set common properties
        profile.setLabel(label);
        profile.setTargetStore(store);
        profile.setOrg(loggedInUser.getOrg());
        ak.ifPresent(akey -> profile.setToken(akey.getToken()));

        ImageProfileFactory.save(profile);

        return 1;
    }

    /**
     * Create a new image profile
     * @param loggedInUser the current User
     * @param label the label
     * @param type the profile type label
     * @param storeLabel the image store label
     * @param path the path or git uri to the source
     * @param activationKey the activation key which defines the channels
     * @return 1 on success
     *
     * @apidoc.doc Create a new image profile
     * @apidoc.param #session_key()
     * @apidoc.param #param("string", "label")
     * @apidoc.param #param("string", "type")
     * @apidoc.param #param("string", "storeLabel")
     * @apidoc.param #param("string", "path")
     * @apidoc.param #param_desc("string", "activationKey", "optional")
     * @apidoc.returntype #return_int_success()
     */
    public int create(User loggedInUser, String label, String type, String storeLabel,
            String path, String activationKey) {
        return create(loggedInUser, label, type, storeLabel, path, activationKey, "");
    }

    /**
     * Delete an Image Profile
     * @param loggedInUser The current User
     * @param label the image profile label
     * @return 1 on success
     *
     * @apidoc.doc Delete an image profile
     * @apidoc.param #session_key()
     * @apidoc.param #param("string", "label")
     * @apidoc.returntype #return_int_success()
     */
    public int delete(User loggedInUser, String label) {
        ensureImageAdmin(loggedInUser);
        ImageProfileFactory.delete(getValidImageProfile(loggedInUser, label));
        return 1;
    }

    /**
     * Set details of an Image Profile
     * @param loggedInUser the current User
     * @param label the label
     * @param details A map containing the new details
     * @return 1 on success
     *
     * @apidoc.doc Set details of an image profile
     * @apidoc.param #session_key()
     * @apidoc.param #param("string", "label")
     * @apidoc.param
     *   #struct_begin("details")
     *     #prop("string", "storeLabel")
     *     #prop("string", "path")
     *     #prop_desc("string", "activationKey", "set empty string to unset")
     *   #struct_end()
     * @apidoc.returntype #return_int_success()
     */
    public int setDetails(User loggedInUser, String label, Map details) {
        ensureImageAdmin(loggedInUser);
        Set<String> validKeys = new HashSet<>();
        validKeys.add("storeLabel");
        validKeys.add("path");
        validKeys.add("activationKey");
        validateMap(validKeys, details);

        ImageProfile profile = getValidImageProfile(loggedInUser, label);

        if (details.containsKey("storeLabel")) {
            String storeLabel = (String) details.get("storeLabel");
            if (StringUtils.isEmpty(storeLabel)) {
                throw new InvalidParameterException("Store label cannot be empty.");
            }
            ImageStore store =
                    ImageStoreFactory.lookupBylabelAndOrg(storeLabel, loggedInUser.getOrg())
                            .orElseThrow(NoSuchImageStoreException::new);

            if (!ImageProfileFactory.getStoreTypeForProfile(profile).equals(store.getStoreType())) {
                throw new InvalidParameterException(String.format(
                        "Invalid store for profile type: '%s'", profile.getImageType()));
            }

            profile.setTargetStore(store);
        }
        if (details.containsKey("path")) {
            String path = (String) details.get("path");
            if (StringUtils.isEmpty(path)) {
                throw new InvalidParameterException("Path cannot be empty.");
            }

            switch (profile.getImageType()) {
                case ImageProfile.TYPE_DOCKERFILE:
                    profile.asDockerfileProfile().get().setPath(path);
                    break;
                case ImageProfile.TYPE_KIWI:
                    profile.asKiwiProfile().get().setPath(path);
                    break;
                default:
                    throw new InvalidParameterException("The type " + profile.getImageType() +
                            " doesn't support 'Path' property");
            }
        }
        if (details.containsKey("kiwiOptions")) {
            String kiwiOptions = (String) details.get("kiwiOptions");

            switch (profile.getImageType()) {
                case ImageProfile.TYPE_KIWI:
                    profile.asKiwiProfile().get().setKiwiOptions(kiwiOptions);
                    break;
                default:
                    throw new InvalidParameterException("The type " + profile.getImageType() +
                            " doesn't support 'kiwiOptions' property");
            }
        }
        if (details.containsKey("activationKey")) {
            String activationKey = (String) details.get("activationKey");
            ActivationKey ak = null;
            if (StringUtils.isNotEmpty(activationKey)) {
                ak = ActivationKeyFactory.lookupByKey(activationKey);
                if (ak == null) {
                    throw new InvalidParameterException("Activation key does not exist.");
                }
            }
            else if (profile.asKiwiProfile().isPresent()) {
                throw new InvalidParameterException("Activation key cannot be empty for Kiwi profiles.");
            }
            profile.setToken(ak != null ? ak.getToken() : null);
        }

        ImageProfileFactory.save(profile);
        return 1;
    }

    /**
     * Get the custom data values defined for the Image Profile
     * @param loggedInUser The current user
     * @param label the profile label
     * @return Returns a map containing the defined custom data values for the
     * given Image Profile.
     *
     * @apidoc.doc Get the custom data values defined for the image profile
     * @apidoc.param #session_key()
     * @apidoc.param #param("string", "label")
     * @apidoc.returntype
     *    #struct_begin("the map of custom labels to custom values")
     *      #prop("string", "custom info label")
     *      #prop("string", "value")
     *    #struct_end()
     */
    @ReadOnly
    public Map<String, String> getCustomValues(User loggedInUser, String label) {
        ensureImageAdmin(loggedInUser);
        return getValidImageProfile(loggedInUser, label).getCustomDataValues().stream()
                .collect(Collectors.toMap(a -> a.getKey().getLabel(), ProfileCustomDataValue::getValue));
    }

    /**
     * Set custom values for the specified Image Profile.
     * @param loggedInUser The current user
     * @param label the profile label
     * @param values A map containing the new set of custom data values for this profile
     * @return Returns a 1 if successful, exception otherwise
     *
     * @apidoc.doc Set custom values for the specified image profile
     * @apidoc.param #session_key()
     * @apidoc.param #param("string", "label")
     * @apidoc.param
     *    #struct_desc("values", "the map of custom labels to custom values")
     *      #prop("string", "custom info label")
     *      #prop("string", "value")
     *    #struct_end()
     * @apidoc.returntype #return_int_success()
     */
    public int setCustomValues(User loggedInUser, String label,
            Map<String, String> values) {
        ImageProfile profile = getValidImageProfile(loggedInUser, label);

        Set<CustomDataKey> orgKeys = loggedInUser.getOrg().getCustomDataKeys();
        values.forEach((key, val) -> {
            if (StringUtils.isEmpty(key)) {
                throw new InvalidParameterException("Key label cannot be empty.");
            }

            // Find the key in organization
            CustomDataKey orgKey = orgKeys.stream().filter(ok -> ok.getLabel().equals(key))
                    .findFirst().orElseThrow(() -> new InvalidParameterException(
                            "The key '" + key + "' doesn't exist."));

            // Find the key in profile, or create a new one
            if (profile.getCustomDataValues() == null) {
                profile.setCustomDataValues(new HashSet<>());
            }
            ProfileCustomDataValue profileVal = profile.getCustomDataValues().stream()
                    .filter(cdv -> cdv.getKey().getLabel().equals(key)).findFirst()
                    .orElseGet(() -> {
                        ProfileCustomDataValue newVal = new ProfileCustomDataValue();
                        newVal.setKey(orgKey);
                        newVal.setCreator(loggedInUser);
                        newVal.setProfile(profile);
                        profile.getCustomDataValues().add(newVal);
                        return newVal;
                    });

            // Create or update the key
            profileVal.setValue(val);
            profileVal.setLastModifier(loggedInUser);
            ImageProfileFactory.save(profileVal);
        });

        return 1;
    }

    /**
     * Delete the custom values defined for the specified Image Profile.
     * @param loggedInUser The current user
     * @param label the profile label
     * @param keys A list of custom data labels/keys to delete from the profile
     * @return Returns a 1 if successful, exception otherwise
     *
     * @apidoc.doc Delete the custom values defined for the specified image profile.<br/>
     * (Note: Attempt to delete values of non-existing keys throws exception. Attempt to
     * delete value of existing key which has assigned no values doesn't throw exception.)
     * @apidoc.param #session_key()
     * @apidoc.param #param("string", "label")
     * @apidoc.param  #array_single_desc("string", "keys", "the custom data keys")
     * @apidoc.returntype #return_int_success()
     */
    public int deleteCustomValues(User loggedInUser, String label, List<String> keys) {
        ImageProfile profile = getValidImageProfile(loggedInUser, label);
        Set<CustomDataKey> orgKeys = loggedInUser.getOrg().getCustomDataKeys();

        Set<ProfileCustomDataValue> values = profile.getCustomDataValues();
        keys.forEach(key -> {
            if (StringUtils.isEmpty(key)) {
                throw new InvalidParameterException("Key label cannot be empty.");
            }
            orgKeys.stream().filter(k -> key.equals(k.getLabel())).findFirst()
                    .orElseThrow(() -> new InvalidParameterException(
                            "The key '" + key + "' doesn't exist."));

            values.stream().filter(v -> key.equals(v.getKey().getLabel())).findFirst()
                    .ifPresent(values::remove);
        });

        return 1;
    }

    private ImageProfile getValidImageProfile(User user, String label) {
        if (StringUtils.isEmpty(label)) {
            throw new InvalidParameterException("Label cannot be empty.");
        }

        return ImageProfileFactory.lookupByLabelAndOrg(label, user.getOrg())
                .orElseThrow(NoSuchImageProfileException::new);
    }
}
