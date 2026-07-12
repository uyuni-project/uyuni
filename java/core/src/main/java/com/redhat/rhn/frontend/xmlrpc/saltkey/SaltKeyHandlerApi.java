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
package com.redhat.rhn.frontend.xmlrpc.saltkey;

import com.redhat.rhn.domain.user.User;

import com.suse.manager.api.ApiResponseWrapper;
import com.suse.manager.api.docs.ApiEndpointDoc;

import java.util.List;

import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.tags.Tag;
import spark.route.HttpMethod;

/**
 * API contract for {@link SaltKeyHandler}.
 */
@Tag(name = "saltkey", description = "Provides methods to manage salt keys")
public interface SaltKeyHandlerApi {

    /**
     * Lists the accepted salt keys.
     *
     * @param loggedInUser current user
     * @return the accepted salt key list
     */
    @ApiEndpointDoc(
        summary = "List accepted salt keys",
        method = HttpMethod.get,
        responseClass = SaltKeyListResponse.class,
        responseDescription = "Accepted salt key list"
    )
    List<String> acceptedList(User loggedInUser);

    /**
     * Lists the pending salt keys.
     *
     * @param loggedInUser current user
     * @return the pending salt key list
     */
    @ApiEndpointDoc(
        summary = "List pending salt keys",
        method = HttpMethod.get,
        responseClass = SaltKeyListResponse.class,
        responseDescription = "Pending salt key list"
    )
    List<String> pendingList(User loggedInUser);

    /**
     * Lists the rejected salt keys.
     *
     * @param loggedInUser current user
     * @return the rejected salt key list
     */
    @ApiEndpointDoc(
        summary = "List of rejected salt keys",
        method = HttpMethod.get,
        responseClass = SaltKeyListResponse.class,
        responseDescription = "Rejected salt key list"
    )
    List<String> rejectedList(User loggedInUser);

    /**
     * Lists the denied salt keys.
     *
     * @param loggedInUser current user
     * @return the denied salt key list
     */
    @ApiEndpointDoc(
        summary = "List of denied salt keys",
        method = HttpMethod.get,
        responseClass = SaltKeyListResponse.class,
        responseDescription = "Denied salt key list"
    )
    List<String> deniedList(User loggedInUser);

    /**
     * Accepts a minion key.
     *
     * @param loggedInUser current user
     * @param minionId the key identifier (minionId)
     * @return 1 on success
     */
    @ApiEndpointDoc(
        summary = "Accept a minion key",
        requestClass = SaltKeyActionRequest.class,
        isIntegerResponse = true
    )
    int accept(User loggedInUser, String minionId);

    /**
     * Rejects a minion key.
     *
     * @param loggedInUser current user
     * @param minionId the key identifier (minionId)
     * @return 1 on success
     */
    @ApiEndpointDoc(
        summary = "Reject a minion key",
        requestClass = SaltKeyActionRequest.class,
        isIntegerResponse = true
    )
    int reject(User loggedInUser, String minionId);

    /**
     * Deletes a minion key.
     *
     * @param loggedInUser current user
     * @param minionId the key identifier (minionId)
     * @return 1 on success
     */
    @ApiEndpointDoc(
        summary = "Delete a minion key",
        requestClass = SaltKeyActionRequest.class,
        isIntegerResponse = true
    )
    int delete(User loggedInUser, String minionId);

    @Schema(name = "SaltKeyActionRequest")
    interface SaltKeyActionRequest {

        /**
         * @return the key identifier (minionId)
         */
        @Schema(description = "the key identifier (minionId)", requiredMode = Schema.RequiredMode.REQUIRED)
        String getMinionId();
    }

    @Schema(name = "ApiResponseSaltKeyList")
    interface SaltKeyListResponse extends ApiResponseWrapper<List<String>> { }
}
