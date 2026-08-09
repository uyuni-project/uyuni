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
package com.redhat.rhn.frontend.xmlrpc.image.store;

import com.redhat.rhn.domain.image.ImageStore;
import com.redhat.rhn.domain.image.ImageStoreType;
import com.redhat.rhn.domain.user.User;

import com.suse.manager.api.ApiResponseWrapper;
import com.suse.manager.api.docs.ApiEndpointDoc;

import com.fasterxml.jackson.annotation.JsonPropertyOrder;

import java.util.List;
import java.util.Map;

import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.enums.ParameterIn;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.tags.Tag;
import spark.route.HttpMethod;

/**
 * API contract for {@link ImageStoreHandler}.
 */
@Tag(name = "image.store", description = "Provides methods to access and modify image stores.")
public interface ImageStoreHandlerApi {

    /**
     * Create a new image store.
     *
     * @param loggedInUser the current user
     * @param label the label
     * @param uri the uri
     * @param storeType the store type
     * @param credentials optional credentials
     * @return 1 on success
     */
    @ApiEndpointDoc(
        summary = "Create a new image store",
        requestClass = CreateRequest.class,
        isIntegerResponse = true
    )
    int create(User loggedInUser, String label, String uri, String storeType, Map<String, String> credentials);

    /**
     * List available image store types.
     *
     * @param loggedInUser the current user
     * @return the list of available image store types
     */
    @ApiEndpointDoc(
        summary = "List available image store types",
        method = HttpMethod.get,
        responseClass = ImageStoreTypeListResponse.class
    )
    List<ImageStoreType> listImageStoreTypes(User loggedInUser);

    /**
     * List available image stores.
     *
     * @param loggedInUser the current user
     * @return the list of configured image stores
     */
    @ApiEndpointDoc(
        summary = "List available image stores",
        method = HttpMethod.get,
        responseClass = ImageStoreListResponse.class
    )
    List<ImageStore> listImageStores(User loggedInUser);

    /**
     * Get details of an image store.
     *
     * @param loggedInUser the current user
     * @param label the image store label
     * @return the image store details
     */
    @ApiEndpointDoc(
        summary = "Get details of an image store",
        method = HttpMethod.get,
        responseClass = ImageStoreResponse.class
    )
    ImageStore getDetails(
        @Parameter(hidden = true) User loggedInUser,
        @Parameter(
            name = "label",
            in = ParameterIn.QUERY,
            required = true
        ) String label);

    /**
     * Delete an image store.
     *
     * @param loggedInUser the current user
     * @param label the image store label
     * @return 1 on success
     */
    @ApiEndpointDoc(
        summary = "Delete an image store",
        requestClass = LabelRequest.class,
        isIntegerResponse = true
    )
    int delete(User loggedInUser, String label);

    /**
     * Set details of an image store.
     *
     * @param loggedInUser the current user
     * @param label the label
     * @param details a map containing the new details
     * @return 1 on success
     */
    @ApiEndpointDoc(
        summary = "Set details of an image store",
        requestClass = SetDetailsRequest.class,
        isIntegerResponse = true
    )
    int setDetails(User loggedInUser, String label, Map details);

    @Schema(name = "CreateImageStoreRequest")
    @JsonPropertyOrder({"label", "uri", "storeType", "credentials"})
    interface CreateRequest {

        /**
         * @return the label
         */
        @Schema(requiredMode = Schema.RequiredMode.REQUIRED)
        String getLabel();

        /**
         * @return the uri
         */
        @Schema(requiredMode = Schema.RequiredMode.REQUIRED)
        String getUri();

        /**
         * @return the store type
         */
        @Schema(requiredMode = Schema.RequiredMode.REQUIRED)
        String getStoreType();

        /**
         * @return the optional credentials
         */
        @Schema(description = "optional", requiredMode = Schema.RequiredMode.REQUIRED)
        CredentialsDoc getCredentials();
    }

    @Schema(name = "ImageStoreCredentials")
    @JsonPropertyOrder({"username", "password"})
    interface CredentialsDoc {

        /**
         * @return the username
         */
        @Schema(requiredMode = Schema.RequiredMode.REQUIRED)
        String getUsername();

        /**
         * @return the password
         */
        @Schema(requiredMode = Schema.RequiredMode.REQUIRED)
        String getPassword();
    }

    @Schema(name = "ImageStoreLabelRequest")
    interface LabelRequest {

        /**
         * @return the image store label
         */
        @Schema(requiredMode = Schema.RequiredMode.REQUIRED)
        String getLabel();
    }

    @Schema(name = "SetImageStoreDetailsRequest")
    @JsonPropertyOrder({"label", "details"})
    interface SetDetailsRequest {

        /**
         * @return the label
         */
        @Schema(requiredMode = Schema.RequiredMode.REQUIRED)
        String getLabel();

        /**
         * @return the new details
         */
        @Schema(description = "image store details", requiredMode = Schema.RequiredMode.REQUIRED)
        DetailsDoc getDetails();
    }

    @Schema(name = "ImageStoreDetails")
    @JsonPropertyOrder({"uri", "username", "password"})
    interface DetailsDoc {

        /**
         * @return the uri
         */
        @Schema(requiredMode = Schema.RequiredMode.REQUIRED)
        String getUri();

        /**
         * @return the username
         */
        @Schema(description = "pass empty string to unset credentials",
                requiredMode = Schema.RequiredMode.REQUIRED)
        String getUsername();

        /**
         * @return the password
         */
        @Schema(requiredMode = Schema.RequiredMode.REQUIRED)
        String getPassword();
    }

    @Schema(name = "ImageStoreTypeInformation")
    @JsonPropertyOrder({"id", "label", "name"})
    interface ImageStoreTypeDoc {

        /**
         * @return the id
         */
        @Schema(requiredMode = Schema.RequiredMode.REQUIRED)
        Integer getId();

        /**
         * @return the label
         */
        @Schema(requiredMode = Schema.RequiredMode.REQUIRED)
        String getLabel();

        /**
         * @return the name
         */
        @Schema(requiredMode = Schema.RequiredMode.REQUIRED)
        String getName();
    }

    @Schema(name = "ImageStoreInformation")
    @JsonPropertyOrder({"label", "uri", "storetype", "hasCredentials", "username"})
    interface ImageStoreDoc {

        /**
         * @return the label
         */
        @Schema(requiredMode = Schema.RequiredMode.REQUIRED)
        String getLabel();

        /**
         * @return the uri
         */
        @Schema(requiredMode = Schema.RequiredMode.REQUIRED)
        String getUri();

        /**
         * @return the store type
         */
        @Schema(requiredMode = Schema.RequiredMode.REQUIRED)
        String getStoretype();

        /**
         * @return whether the store has credentials
         */
        @Schema(requiredMode = Schema.RequiredMode.REQUIRED)
        Boolean getHasCredentials();

        /**
         * @return the username
         */
        @Schema(requiredMode = Schema.RequiredMode.REQUIRED)
        String getUsername();
    }

    @Schema(name = "ApiResponseImageStoreTypeList")
    interface ImageStoreTypeListResponse extends ApiResponseWrapper<List<ImageStoreTypeDoc>> { }

    @Schema(name = "ApiResponseImageStoreList")
    interface ImageStoreListResponse extends ApiResponseWrapper<List<ImageStoreDoc>> { }

    @Schema(name = "ApiResponseImageStore")
    interface ImageStoreResponse extends ApiResponseWrapper<ImageStoreDoc> { }
}
