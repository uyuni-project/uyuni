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
package com.redhat.rhn.frontend.xmlrpc.image.profile;

import com.redhat.rhn.domain.image.DockerfileProfile;
import com.redhat.rhn.domain.image.ImageProfile;
import com.redhat.rhn.domain.image.ImageProfileFactory;
import com.redhat.rhn.domain.image.ImageStore;
import com.redhat.rhn.domain.image.ImageStoreFactory;
import com.redhat.rhn.domain.image.ProfileCustomDataValue;
import com.redhat.rhn.domain.org.CustomDataKey;
import com.redhat.rhn.domain.token.ActivationKey;
import com.redhat.rhn.domain.token.ActivationKeyFactory;
import com.redhat.rhn.domain.user.User;
import com.redhat.rhn.frontend.xmlrpc.BaseHandler;
import com.redhat.rhn.frontend.xmlrpc.InvalidParameterException;
import com.redhat.rhn.frontend.xmlrpc.NoSuchImageProfileException;
import com.redhat.rhn.frontend.xmlrpc.NoSuchImageStoreException;
import org.apache.commons.lang.StringUtils;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.NoSuchElementException;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * ImageProfileHandler
 * @xmlrpc.namespace image.profile
 * @xmlrpc.doc Provides methods to access and modify image profiles.
 */
public class ImageProfileHandler extends BaseHandler {

    /**
     * List available Image Profile Types
     * @param loggedInUser The current user
     * @return Array of ImageProfileType strings
     *
     * @xmlrpc.doc List available Image Store Types
     * @xmlrpc.param #param("string", "sessionKey")
     * @xmlrpc.returntype #array_single("string", "imageProfileTypes")
     */
    public List<String> listImageProfileTypes(User loggedInUser) {
        ensureImageAdmin(loggedInUser);
        List<String> imageTypes = new ArrayList<>();
        imageTypes.add(ImageProfile.TYPE_DOCKERFILE);
        return imageTypes;
    }

    /**
     * List all configured image profiles visible for the logged in user
     * @param loggedInUser The current User
     * @return Array of ImageProfile Objects
     *
     * @xmlrpc.doc List available Image Profiles
     * @xmlrpc.param #param("string", "sessionKey")
     * @xmlrpc.returntype #array() $ImageProfileSerializer #array_end()
     */
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
     * @xmlrpc.doc Get details of an Image Profile
     * @xmlrpc.param #param("string", "sessionKey")
     * @xmlrpc.param #param("string", "label")
     * @xmlrpc.returntype $ImageProfileSerializer
     */
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
     * @return 1 on success
     *
     * @xmlrpc.doc Create a new Image Profile
     * @xmlrpc.param #param("string", "sessionKey")
     * @xmlrpc.param #param("string", "label")
     * @xmlrpc.param #param("string", "type")
     * @xmlrpc.param #param("string", "storeLabel")
     * @xmlrpc.param #param("string", "path")
     * @xmlrpc.param #param_desc("string", "activationKey", "Optional")
     * @xmlrpc.returntype #return_int_success()
     */
    public int create(User loggedInUser, String label, String type, String storeLabel,
            String path, String activationKey) {
        ensureImageAdmin(loggedInUser);

        if (StringUtils.isEmpty(label)) {
            throw new InvalidParameterException("Label cannot be empty.");
        }
        if (StringUtils.contains(label, ':')) {
            throw new InvalidParameterException("Label cannot contain colons (:).");
        }
        else if (ImageProfileFactory.lookupByLabel(label).isPresent()) {
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
        ActivationKey ak = null;
        if (StringUtils.isNotEmpty(activationKey)) {
            ak = ActivationKeyFactory.lookupByKey(activationKey);
            if (ak == null) {
                throw new InvalidParameterException("Activation key does not exist.");
            }
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
        if (ImageProfile.TYPE_DOCKERFILE.equals(type)) {

            DockerfileProfile dockerfileProfile = new DockerfileProfile();

            dockerfileProfile.setLabel(label);
            dockerfileProfile.setPath(path);
            dockerfileProfile.setTargetStore(store);
            dockerfileProfile.setOrg(loggedInUser.getOrg());
            dockerfileProfile.setToken(ak != null ? ak.getToken() : null);

            profile = dockerfileProfile;
        }
        else {
            throw new UnsupportedOperationException();
        }
        ImageProfileFactory.save(profile);

        return 1;
    }

    /**
     * Delete an Image Profile
     * @param loggedInUser The current User
     * @param label the image profile label
     * @return 1 on success
     *
     * @xmlrpc.doc Delete an Image Profile
     * @xmlrpc.param #param("string", "sessionKey")
     * @xmlrpc.param #param("string", "label")
     * @xmlrpc.returntype #return_int_success()
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
     * @xmlrpc.doc Set details of an Image Profile
     * @xmlrpc.param #param("string", "sessionKey")
     * @xmlrpc.param #param("string", "label")
     * @xmlrpc.param
     *   #struct("image profile details")
     *     #prop("string", "storeLabel")
     *     #prop("string", "path")
     *     #prop_desc("string", "activationKey", "set empty string to unset")
     *   #struct_end()
     * @xmlrpc.returntype #return_int_success()
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
            profile.setTargetStore(ImageStoreFactory
                    .lookupBylabelAndOrg(storeLabel,
                            loggedInUser.getOrg())
                    .orElseThrow(NoSuchImageStoreException::new));
        }
        if (details.containsKey("path")) {
            String path = (String) details.get("path");
            if (StringUtils.isEmpty(path)) {
                throw new InvalidParameterException("Path cannot be empty.");
            }
            profile.asDockerfileProfile()
                    .orElseThrow(() -> new InvalidParameterException("The type " +
                            profile.getImageType() + " doesn't support 'Path' property"))
                    .setPath(path);
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
     * @xmlrpc.doc Get the custom data values defined for the Image Profile.
     * @xmlrpc.param #param("string", "sessionKey")
     * @xmlrpc.param #param("string", "label")
     * @xmlrpc.returntype
     *    #struct("Map of custom labels to custom values")
     *      #prop("string", "custom info label")
     *      #prop("string", "value")
     *    #struct_end()
     */
    public Map<String, String> getCustomValues(User loggedInUser, String label) {
        ensureImageAdmin(loggedInUser);
        return getValidImageProfile(loggedInUser, label).getCustomDataValues().stream()
                .collect(Collectors.toMap(a -> a.getKey().getLabel(), a -> a.getValue()));
    }

    /**
     * Set custom values for the specified Image Profile.
     * @param loggedInUser The current user
     * @param label the profile label
     * @param values A map containing the new set of custom data values for this profile
     * @return Returns a 1 if successful, exception otherwise
     *
     * @xmlrpc.doc Set custom values for the specified Image Profile.
     * @xmlrpc.param #param("string", "sessionKey")
     * @xmlrpc.param #param("string", "label")
     * @xmlrpc.param
     *    #struct("Map of custom labels to custom values")
     *      #prop("string", "custom info label")
     *      #prop("string", "value")
     *    #struct_end()
     * @xmlrpc.returntype #return_int_success()
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
        });

        // Save changes
        ImageProfileFactory.save(profile);
        return 1;
    }

    /**
     * Delete the custom values defined for the specified Image Profile.
     * @param loggedInUser The current user
     * @param label the profile label
     * @param keys A list of custom data labels/keys to delete from the profile
     * @return Returns a 1 if successful, exception otherwise
     *
     * @xmlrpc.doc Delete the custom values defined for the specified Image Profile.<br/>
     * (Note: Attempt to delete values of non-existing keys throws exception. Attempt to
     * delete value of existing key which has assigned no values doesn't throw exception.)
     * @xmlrpc.param #param("string", "sessionKey")
     * @xmlrpc.param #param("string", "label")
     * @xmlrpc.param  #array_single("string", "customDataKeys")
     * @xmlrpc.returntype #return_int_success()
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
