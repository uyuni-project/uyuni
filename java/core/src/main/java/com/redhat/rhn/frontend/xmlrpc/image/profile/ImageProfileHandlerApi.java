/*
 * Copyright (c) 2026 SUSE LLC
 *
 * This software is licensed to you under the GNU General Public License,
 * version 2 (GPLv2). There is NO WARRANTY for this software, express or
 * implied, including the implied warranties of MERCHANTABILITY or FITNESS
 * FOR A PARTICULAR PURPOSE. You should have received a copy of GPLv2
 * along with this software; if not, see
 * http://www.gnu.org/licenses/old-licenses/gpl-2.0.txt.
 */
package com.redhat.rhn.frontend.xmlrpc.image.profile;

import com.redhat.rhn.domain.image.ImageProfile;
import com.redhat.rhn.domain.user.User;

import com.suse.manager.api.ApiResponseWrapper;
import com.suse.manager.api.docs.ApiEndpointDoc;
import com.suse.manager.api.docs.LegacyDocResponse;

import com.fasterxml.jackson.annotation.JsonPropertyOrder;

import java.util.List;
import java.util.Map;

import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.enums.ParameterIn;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.tags.Tag;
import spark.route.HttpMethod;

/**
 * API contract for {@link ImageProfileHandler}.
 */
@Tag(name = "image.profile", description = "Provides methods to access and modify image profiles.")
public interface ImageProfileHandlerApi {

    /**
     * List available image profile types.
     *
     * @param loggedInUser the current user
     * @return the list of image profile types
     */
    @ApiEndpointDoc(
        summary = "List available image store types",
        method = HttpMethod.get,
        responseClass = ImageProfileTypeListResponse.class,
        responseDescription = "the list of image profile types"
    )
    List<String> listImageProfileTypes(User loggedInUser);

    /**
     * List available image profiles.
     *
     * @param loggedInUser the current user
     * @return the list of image profiles
     */
    @ApiEndpointDoc(
        summary = "List available image profiles",
        method = HttpMethod.get,
        responseClass = ImageProfileListResponse.class
    )
    List<ImageProfile> listImageProfiles(User loggedInUser);

    /**
     * Get details of an image profile.
     *
     * @param loggedInUser the current user
     * @param label the image profile label
     * @return the image profile details
     */
    @ApiEndpointDoc(
        summary = "Get details of an image profile",
        method = HttpMethod.get,
        responseClass = ImageProfileResponse.class
    )
    ImageProfile getDetails(
        @Parameter(hidden = true) User loggedInUser,
        @Parameter(name = "label", in = ParameterIn.QUERY, required = true) String label);

    /**
     * Create a new image profile.
     *
     * @param loggedInUser the current user
     * @param label the label
     * @param type the profile type label
     * @param storeLabel the image store label
     * @param path the path or git uri to the source
     * @param activationKey the activation key which defines the channels
     * @param kiwiOptions command line options passed to Kiwi
     * @return 1 on success
     */
    @ApiEndpointDoc(
        summary = "Create a new image profile",
        requestClass = CreateRequest.class,
        isIntegerResponse = true
    )
    int create(User loggedInUser, String label, String type, String storeLabel, String path,
               String activationKey, String kiwiOptions);

    /**
     * Delete an image profile.
     *
     * @param loggedInUser the current user
     * @param label the image profile label
     * @return 1 on success
     */
    @ApiEndpointDoc(
        summary = "Delete an image profile",
        requestClass = LabelRequest.class,
        isIntegerResponse = true
    )
    int delete(User loggedInUser, String label);

    /**
     * Set details of an image profile.
     *
     * @param loggedInUser the current user
     * @param label the image profile label
     * @param details a map containing the new details
     * @return 1 on success
     */
    @ApiEndpointDoc(
        summary = "Set details of an image profile",
        requestClass = SetDetailsRequest.class,
        isIntegerResponse = true
    )
    int setDetails(User loggedInUser, String label, Map details);

    /**
     * Get the custom data values defined for the image profile.
     *
     * @param loggedInUser the current user
     * @param label the image profile label
     * @return the map of custom labels to custom values
     */
    @ApiEndpointDoc(
        summary = "Get the custom data values defined for the image profile",
        method = HttpMethod.get,
        responseClass = CustomValuesResponse.class,
        legacyDocResponse = @LegacyDocResponse(name = "the map of custom labels to custom values")
    )
    Map<String, String> getCustomValues(
        @Parameter(hidden = true) User loggedInUser,
        @Parameter(name = "label", in = ParameterIn.QUERY, required = true) String label);

    /**
     * Set custom values for the specified image profile.
     *
     * @param loggedInUser the current user
     * @param label the image profile label
     * @param values the map of custom labels to custom values
     * @return 1 on success
     */
    @ApiEndpointDoc(
        summary = "Set custom values for the specified image profile",
        requestClass = SetCustomValuesRequest.class,
        isIntegerResponse = true
    )
    int setCustomValues(User loggedInUser, String label, Map<String, String> values);

    /**
     * Delete the custom values defined for the specified image profile.
     *
     * @param loggedInUser the current user
     * @param label the image profile label
     * @param keys the custom data keys
     * @return 1 on success
     */
    @ApiEndpointDoc(
        summary = "Delete the custom values defined for the specified image profile. " +
                  "(Note: Attempt to delete values of non-existing keys throws exception. Attempt to " +
                  "delete value of existing key which has assigned no values doesn't throw exception.)",
        requestClass = DeleteCustomValuesRequest.class,
        isIntegerResponse = true
    )
    int deleteCustomValues(User loggedInUser, String label, List<String> keys);

    @Schema(name = "CreateImageProfileRequest")
    @JsonPropertyOrder({"label", "type", "storeLabel", "path", "activationKey", "kiwiOptions"})
    interface CreateRequest {

        /**
         * @return the label
         */
        @Schema(requiredMode = Schema.RequiredMode.REQUIRED)
        String getLabel();

        /**
         * @return the profile type label
         */
        @Schema(requiredMode = Schema.RequiredMode.REQUIRED)
        String getType();

        /**
         * @return the image store label
         */
        @Schema(requiredMode = Schema.RequiredMode.REQUIRED)
        String getStoreLabel();

        /**
         * @return the path or git uri to the source
         */
        @Schema(requiredMode = Schema.RequiredMode.REQUIRED)
        String getPath();

        /**
         * @return the activation key which defines the channels
         */
        @Schema(description = "optional", requiredMode = Schema.RequiredMode.REQUIRED)
        String getActivationKey();

        /**
         * @return the command line options passed to Kiwi
         */
        @Schema(requiredMode = Schema.RequiredMode.NOT_REQUIRED)
        String getKiwiOptions();
    }

    @Schema(name = "ImageProfileLabelRequest")
    interface LabelRequest {

        /**
         * @return the image profile label
         */
        @Schema(requiredMode = Schema.RequiredMode.REQUIRED)
        String getLabel();
    }

    @Schema(name = "SetImageProfileDetailsRequest")
    @JsonPropertyOrder({"label", "details"})
    interface SetDetailsRequest {

        /**
         * @return the image profile label
         */
        @Schema(requiredMode = Schema.RequiredMode.REQUIRED)
        String getLabel();

        /**
         * @return the new details
         */
        @Schema(requiredMode = Schema.RequiredMode.REQUIRED)
        DetailsDoc getDetails();
    }

    @Schema(name = "ImageProfileDetails")
    @JsonPropertyOrder({"storeLabel", "path", "activationKey"})
    interface DetailsDoc {

        /**
         * @return the image store label
         */
        @Schema(requiredMode = Schema.RequiredMode.REQUIRED)
        String getStoreLabel();

        /**
         * @return the path
         */
        @Schema(requiredMode = Schema.RequiredMode.REQUIRED)
        String getPath();

        /**
         * @return the activation key
         */
        @Schema(description = "set empty string to unset", requiredMode = Schema.RequiredMode.REQUIRED)
        String getActivationKey();
    }

    @Schema(name = "SetImageProfileCustomValuesRequest")
    @JsonPropertyOrder({"label", "values"})
    interface SetCustomValuesRequest {

        /**
         * @return the image profile label
         */
        @Schema(requiredMode = Schema.RequiredMode.REQUIRED)
        String getLabel();

        /**
         * @return the map of custom labels to custom values
         */
        @Schema(description = "the map of custom labels to custom values",
                requiredMode = Schema.RequiredMode.REQUIRED)
        CustomValuesDoc getValues();
    }

    @Schema(name = "DeleteImageProfileCustomValuesRequest")
    @JsonPropertyOrder({"label", "keys"})
    interface DeleteCustomValuesRequest {

        /**
         * @return the image profile label
         */
        @Schema(requiredMode = Schema.RequiredMode.REQUIRED)
        String getLabel();

        /**
         * @return the custom data keys
         */
        @Schema(description = "the custom data keys", requiredMode = Schema.RequiredMode.REQUIRED)
        List<String> getKeys();
    }

    @Schema(name = "ImageProfileCustomValues")
    @JsonPropertyOrder({"customInfoLabel", "value"})
    interface CustomValuesDoc {

        /**
         * @return the custom info label
         */
        @Schema(name = "custom info label", requiredMode = Schema.RequiredMode.REQUIRED)
        String getCustomInfoLabel();

        /**
         * @return the value
         */
        @Schema(requiredMode = Schema.RequiredMode.REQUIRED)
        String getValue();
    }

    @Schema(name = "ImageProfileInformation")
    @JsonPropertyOrder({"label", "imageType", "imageStore", "activationKey", "path"})
    interface ImageProfileDoc {

        /**
         * @return the label
         */
        @Schema(requiredMode = Schema.RequiredMode.REQUIRED)
        String getLabel();

        /**
         * @return the image type
         */
        @Schema(requiredMode = Schema.RequiredMode.REQUIRED)
        String getImageType();

        /**
         * @return the image store
         */
        @Schema(requiredMode = Schema.RequiredMode.REQUIRED)
        String getImageStore();

        /**
         * @return the activation key
         */
        @Schema(requiredMode = Schema.RequiredMode.REQUIRED)
        String getActivationKey();

        /**
         * @return the path
         */
        @Schema(description = "in case type support path", requiredMode = Schema.RequiredMode.REQUIRED)
        String getPath();
    }

    @Schema(name = "ApiResponseImageProfileTypeList")
    interface ImageProfileTypeListResponse extends ApiResponseWrapper<List<String>> { }

    @Schema(name = "ApiResponseImageProfileList")
    interface ImageProfileListResponse extends ApiResponseWrapper<List<ImageProfileDoc>> { }

    @Schema(name = "ApiResponseImageProfile")
    interface ImageProfileResponse extends ApiResponseWrapper<ImageProfileDoc> { }

    @Schema(name = "ApiResponseImageProfileCustomValues")
    interface CustomValuesResponse extends ApiResponseWrapper<CustomValuesDoc> { }
}
