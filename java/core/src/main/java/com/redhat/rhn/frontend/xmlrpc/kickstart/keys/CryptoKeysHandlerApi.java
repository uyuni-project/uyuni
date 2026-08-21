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
package com.redhat.rhn.frontend.xmlrpc.kickstart.keys;

import static io.swagger.v3.oas.annotations.media.Schema.RequiredMode.REQUIRED;

import com.redhat.rhn.domain.kickstart.crypto.CryptoKey;
import com.redhat.rhn.domain.user.User;
import com.redhat.rhn.frontend.dto.CryptoKeyDto;

import com.suse.manager.api.ApiResponseWrapper;
import com.suse.manager.api.docs.ApiEndpointDoc;
import com.suse.manager.api.docs.LegacyDocResponse;

import com.fasterxml.jackson.annotation.JsonPropertyOrder;

import java.util.List;

import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.enums.ParameterIn;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.tags.Tag;
import spark.route.HttpMethod;

/**
 * API contract for {@link CryptoKeysHandler}.
 */
@Tag(name = "kickstart.keys", description = "Provides methods to manipulate kickstart keys.")
public interface CryptoKeysHandlerApi {

    /**
     * Lists all keys associated with the org of the logged in user.
     *
     * @param loggedInUser the current user
     * @return a list of keys
     */
    @ApiEndpointDoc(
        summary = "list all keys for the org associated with the user logged into the given session",
        method = HttpMethod.get,
        responseClass = KeyListResponse.class,
        legacyDocResponse = @LegacyDocResponse(name = "key")
    )
    List<CryptoKeyDto> listAllKeys(User loggedInUser);

    /**
     * Creates a new key with the given parameters.
     *
     * @param loggedInUser the current user
     * @param description description of the key
     * @param type type of key being created
     * @param content contents of the key itself
     * @return 1 on success
     */
    @ApiEndpointDoc(
        summary = "creates a new key with the given parameters",
        requestClass = CreateKeyRequest.class,
        isIntegerResponse = true
    )
    int create(User loggedInUser, String description, String type, String content);

    /**
     * Deletes the key identified by the given description.
     *
     * @param loggedInUser the current user
     * @param description description of the key
     * @return 1 on success
     */
    @ApiEndpointDoc(
        summary = "deletes the key identified by the given parameters",
        requestClass = DeleteKeyRequest.class,
        isIntegerResponse = true
    )
    int delete(User loggedInUser, String description);

    /**
     * Updates type and content of the key identified by the description.
     *
     * @param loggedInUser the current user
     * @param description description of the key used for identification
     * @param type type of key being created
     * @param content contents of the key itself
     * @return 1 on success
     */
    @ApiEndpointDoc(
        summary = "Updates type and content of the key identified by the description",
        requestClass = UpdateKeyRequest.class,
        isIntegerResponse = true
    )
    int update(User loggedInUser, String description, String type, String content);

    /**
     * Returns all of the data associated with the given key.
     *
     * @param loggedInUser the current user
     * @param description identifies the key
     * @return the key details
     */
    @ApiEndpointDoc(
        summary = "returns all the data associated with the given key",
        method = HttpMethod.get,
        responseClass = KeyDetailsResponse.class
    )
    CryptoKey getDetails(
        @Parameter(hidden = true) User loggedInUser,
        @Parameter(
            name = "description",
            description = "identifies the key",
            in = ParameterIn.QUERY,
            required = true
        ) String description
    );

    @Schema(name = "KeyListItem", description = "key")
    @JsonPropertyOrder({"description", "type"})
    interface KeyDoc {

        /**
         * @return the key description
         */
        @Schema(requiredMode = REQUIRED)
        String getDescription();

        /**
         * @return the key type
         */
        @Schema(requiredMode = REQUIRED)
        String getType();
    }

    @Schema(name = "Key", description = "key")
    @JsonPropertyOrder({"description", "type", "content"})
    interface KeyDetailsDoc {

        /**
         * @return the key description
         */
        @Schema(requiredMode = REQUIRED)
        String getDescription();

        /**
         * @return the key type
         */
        @Schema(requiredMode = REQUIRED)
        String getType();

        /**
         * @return the key contents
         */
        @Schema(requiredMode = REQUIRED)
        String getContent();
    }

    @Schema(name = "CreateKeyRequest")
    @JsonPropertyOrder({"description", "type", "content"})
    interface CreateKeyRequest {

        /**
         * @return the key description
         */
        @Schema(requiredMode = REQUIRED)
        String getDescription();

        /**
         * @return the key type
         */
        @Schema(description = "valid values are GPG or SSL", requiredMode = REQUIRED)
        String getType();

        /**
         * @return the key contents
         */
        @Schema(requiredMode = REQUIRED)
        String getContent();
    }

    @Schema(name = "DeleteKeyRequest")
    interface DeleteKeyRequest {

        /**
         * @return the key description
         */
        @Schema(requiredMode = REQUIRED)
        String getDescription();
    }

    @Schema(name = "UpdateKeyRequest")
    @JsonPropertyOrder({"description", "type", "content"})
    interface UpdateKeyRequest {

        /**
         * @return the key description
         */
        @Schema(requiredMode = REQUIRED)
        String getDescription();

        /**
         * @return the key type
         */
        @Schema(description = "valid values are GPG or SSL", requiredMode = REQUIRED)
        String getType();

        /**
         * @return the key contents
         */
        @Schema(requiredMode = REQUIRED)
        String getContent();
    }

    @Schema(name = "ApiResponseKeyList")
    interface KeyListResponse extends ApiResponseWrapper<List<KeyDoc>> { }

    @Schema(name = "ApiResponseKeyDetails")
    interface KeyDetailsResponse extends ApiResponseWrapper<KeyDetailsDoc> { }
}
