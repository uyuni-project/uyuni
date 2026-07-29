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
package com.redhat.rhn.frontend.xmlrpc.kickstart.snippet;

import com.redhat.rhn.domain.kickstart.cobbler.CobblerSnippet;
import com.redhat.rhn.domain.user.User;

import com.suse.manager.api.ApiResponseWrapper;
import com.suse.manager.api.docs.ApiEndpointDoc;

import com.fasterxml.jackson.annotation.JsonPropertyOrder;

import java.util.List;

import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.tags.Tag;
import spark.route.HttpMethod;

/**
 * API contract for {@link SnippetHandler}.
 */
@Tag(name = "kickstart.snippet", description = "Provides methods to create kickstart files")
public interface SnippetHandlerApi {

    /**
     * Lists all cobbler snippets for the logged in user.
     *
     * @param loggedInUser the current user
     * @return the list of cobbler snippets
     */
    @ApiEndpointDoc(
        summary = "List all cobbler snippets for the logged in user",
        method = HttpMethod.get,
        responseClass = SnippetListResponse.class
    )
    List<CobblerSnippet> listAll(@Parameter(hidden = true) User loggedInUser);

    /**
     * Lists only the custom snippets for the logged in user.
     *
     * @param loggedInUser the current user
     * @return the list of custom cobbler snippets
     */
    @ApiEndpointDoc(
        summary = "List only custom snippets for the logged in user. These snippets are editable.",
        method = HttpMethod.get,
        responseClass = SnippetListResponse.class
    )
    List<CobblerSnippet> listCustom(@Parameter(hidden = true) User loggedInUser);

    /**
     * Lists only the pre-made default snippets for the logged in user.
     *
     * @param loggedInUser the current user
     * @return the list of default cobbler snippets
     */
    @ApiEndpointDoc(
        summary = "List only pre-made default snippets for the logged in user. These snippets are not editable.",
        method = HttpMethod.get,
        responseClass = SnippetListResponse.class
    )
    List<CobblerSnippet> listDefault(@Parameter(hidden = true) User loggedInUser);

    /**
     * Creates a snippet, or updates it when it already exists.
     *
     * @param loggedInUser the current user
     * @param name the name of the snippet
     * @param contents the contents of the snippet
     * @return the created or updated snippet
     */
    @ApiEndpointDoc(
        summary = "Will create a snippet with the given name and contents if it doesn't exist. " +
                  "If it does exist, the existing snippet will be updated.",
        requestClass = CreateOrUpdateRequest.class,
        responseClass = SnippetResponse.class
    )
    CobblerSnippet createOrUpdate(User loggedInUser, String name, String contents);

    /**
     * Deletes the specified snippet.
     *
     * @param loggedInUser the current user
     * @param name the name of the snippet
     * @return 1 on success, 0 if the snippet is not found
     */
    @ApiEndpointDoc(
        summary = "Delete the specified snippet. If the snippet is not found, 0 is returned.",
        requestClass = DeleteRequest.class,
        isIntegerResponse = true
    )
    int delete(User loggedInUser, String name);

    @Schema(name = "CreateOrUpdateSnippetRequest")
    @JsonPropertyOrder({"name", "contents"})
    interface CreateOrUpdateRequest {

        /**
         * @return the name of the snippet
         */
        @Schema(requiredMode = Schema.RequiredMode.REQUIRED)
        String getName();

        /**
         * @return the contents of the snippet
         */
        @Schema(requiredMode = Schema.RequiredMode.REQUIRED)
        String getContents();
    }

    @Schema(name = "DeleteSnippetRequest")
    interface DeleteRequest {

        /**
         * @return the name of the snippet
         */
        @Schema(requiredMode = Schema.RequiredMode.REQUIRED)
        String getName();
    }

    @Schema(name = "Snippet")
    @JsonPropertyOrder({"name", "contents", "fragment", "file"})
    interface SnippetDoc {

        /**
         * @return the name of the snippet
         */
        @Schema(requiredMode = Schema.RequiredMode.REQUIRED)
        String getName();

        /**
         * @return the contents of the snippet
         */
        @Schema(requiredMode = Schema.RequiredMode.REQUIRED)
        String getContents();

        /**
         * @return the string to include in a kickstart file that will generate this snippet
         */
        @Schema(description = "the string to include in a kickstart file that will generate this snippet",
                requiredMode = Schema.RequiredMode.REQUIRED)
        String getFragment();

        /**
         * @return the local path to the file containing this snippet
         */
        @Schema(description = "the local path to the file containing this snippet",
                requiredMode = Schema.RequiredMode.REQUIRED)
        String getFile();
    }

    @Schema(name = "ApiResponseSnippet")
    interface SnippetResponse extends ApiResponseWrapper<SnippetDoc> { }

    @Schema(name = "ApiResponseSnippetList")
    interface SnippetListResponse extends ApiResponseWrapper<List<SnippetDoc>> { }
}
