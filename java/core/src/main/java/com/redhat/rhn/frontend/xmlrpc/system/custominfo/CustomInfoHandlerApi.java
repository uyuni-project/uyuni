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
package com.redhat.rhn.frontend.xmlrpc.system.custominfo;

import com.redhat.rhn.domain.user.User;

import com.suse.manager.api.ApiResponseWrapper;
import com.suse.manager.api.docs.ApiEndpointDoc;
import com.suse.manager.api.docs.LegacyDocResponse;

import com.fasterxml.jackson.annotation.JsonPropertyOrder;

import java.util.Date;
import java.util.List;

import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.tags.Tag;
import spark.route.HttpMethod;

/**
 * API contract for {@link CustomInfoHandler}.
 */
@Tag(name = "system.custominfo", description = "Provides methods to access and modify custom system information.")
public interface CustomInfoHandlerApi {

    /**
     * Create a new custom key.
     *
     * @param loggedInUser the current user
     * @param keyLabel the new key's label
     * @param keyDescription the new key's description
     * @return 1 on success
     */
    @ApiEndpointDoc(
        summary = "Create a new custom key",
        requestClass = CreateKeyRequest.class,
        isIntegerResponse = true
    )
    int createKey(User loggedInUser, String keyLabel, String keyDescription);

    /**
     * Delete an existing custom key and all systems' values for the key.
     *
     * @param loggedInUser the current user
     * @param keyLabel the key's label
     * @return 1 on success
     */
    @ApiEndpointDoc(
        summary = "Delete an existing custom key and all systems' values for the key.",
        requestClass = DeleteKeyRequest.class,
        isIntegerResponse = true
    )
    int deleteKey(User loggedInUser, String keyLabel);

    /**
     * List the custom information keys defined for the user's organization.
     *
     * @param loggedInUser the current user
     * @return the list of custom information keys
     */
    @ApiEndpointDoc(
        summary = "List the custom information keys defined for the user's organization.",
        method = HttpMethod.get,
        responseClass = CustomInfoListResponse.class,
        legacyDocResponse = @LegacyDocResponse(name = "custom info")
    )
    Object[] listAllKeys(@Parameter(hidden = true) User loggedInUser);

    /**
     * Update the description of a custom key.
     *
     * @param loggedInUser the current user
     * @param keyLabel the key to change
     * @param keyDescription the new key's description
     * @return 1 on success
     */
    @ApiEndpointDoc(
        summary = "Update description of a custom key",
        requestClass = UpdateKeyRequest.class,
        isIntegerResponse = true
    )
    int updateKey(User loggedInUser, String keyLabel, String keyDescription);

    @Schema(name = "CreateCustomKeyRequest")
    @JsonPropertyOrder({"keyLabel", "keyDescription"})
    interface CreateKeyRequest {

        /**
         * @return the new key's label
         */
        @Schema(description = "new key's label", requiredMode = Schema.RequiredMode.REQUIRED)
        String getKeyLabel();

        /**
         * @return the new key's description
         */
        @Schema(description = "new key's description", requiredMode = Schema.RequiredMode.REQUIRED)
        String getKeyDescription();
    }

    @Schema(name = "DeleteCustomKeyRequest")
    interface DeleteKeyRequest {

        /**
         * @return the new key's label
         */
        @Schema(description = "new key's label", requiredMode = Schema.RequiredMode.REQUIRED)
        String getKeyLabel();
    }

    @Schema(name = "UpdateCustomKeyRequest")
    @JsonPropertyOrder({"keyLabel", "keyDescription"})
    interface UpdateKeyRequest {

        /**
         * @return the key to change
         */
        @Schema(description = "key to change", requiredMode = Schema.RequiredMode.REQUIRED)
        String getKeyLabel();

        /**
         * @return the new key's description
         */
        @Schema(description = "new key's description", requiredMode = Schema.RequiredMode.REQUIRED)
        String getKeyDescription();
    }

    @Schema(name = "CustomInfoKey")
    @JsonPropertyOrder({"id", "label", "description", "systemCount", "lastModified"})
    interface CustomInfoKeyDoc {

        /**
         * @return the key id
         */
        Integer getId();

        /**
         * @return the key label
         */
        String getLabel();

        /**
         * @return the key description
         */
        String getDescription();

        /**
         * @return the number of systems carrying a value for the key
         */
        @Schema(name = "system_count")
        Integer getSystemCount();

        /**
         * @return the last modification date
         */
        @Schema(name = "last_modified")
        Date getLastModified();
    }

    @Schema(name = "ApiResponseCustomInfoKeyList")
    interface CustomInfoListResponse extends ApiResponseWrapper<List<CustomInfoKeyDoc>> { }
}
