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
package com.redhat.rhn.frontend.xmlrpc.access;

import static io.swagger.v3.oas.annotations.media.Schema.RequiredMode.NOT_REQUIRED;
import static io.swagger.v3.oas.annotations.media.Schema.RequiredMode.REQUIRED;

import com.redhat.rhn.domain.access.AccessGroup;
import com.redhat.rhn.domain.access.Namespace;
import com.redhat.rhn.domain.user.User;

import com.suse.manager.api.ApiResponseWrapper;
import com.suse.manager.api.docs.ApiEndpointDoc;

import com.fasterxml.jackson.annotation.JsonPropertyOrder;

import java.util.List;
import java.util.Set;

import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.enums.ParameterIn;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.tags.Tag;
import spark.route.HttpMethod;

/**
 * API contract for {@link AccessHandler}.
 */
@Tag(name = "access", description = "Provides methods to manage Role-Based Access Control")
public interface AccessHandlerApi {

    /**
     * Creates a new role.
     *
     * @param loggedInUser current user
     * @param label role label
     * @param description role description
     * @param permissionsFrom roles to inherit permissions from
     * @return created access group
     */
    @ApiEndpointDoc(
        summary = "Create a new role.",
        requestClass = CreateRoleRequest.class,
        responseClass = AccessGroupResponse.class
    )
    AccessGroup createRole(User loggedInUser, String label, String description, List<String> permissionsFrom);

    /**
     * Lists existing roles.
     *
     * @param loggedInUser current user
     * @return existing roles
     */
    @ApiEndpointDoc(
        summary = "List existing roles.",
        method = HttpMethod.get,
        responseClass = AccessGroupListResponse.class
    )
    List<AccessGroup> listRoles(User loggedInUser);

    /**
     * Lists available namespaces.
     *
     * @param loggedInUser current user
     * @return available namespaces
     */
    @ApiEndpointDoc(
        summary = "List available namespaces.",
        method = HttpMethod.get,
        responseClass = NamespaceListResponse.class
    )
    List<Namespace> listNamespaces(User loggedInUser);

    /**
     * Deletes a role.
     *
     * @param loggedInUser current user
     * @param label role label
     * @return {@code 1} on success
     */
    @ApiEndpointDoc(summary = "Delete a role.", isIntegerResponse = true)
    int deleteRole(
        @Parameter(hidden = true) User loggedInUser,
        @Parameter(
            name = "label",
            description = "the unique label of the new role",
            in = ParameterIn.QUERY,
            required = true
        ) String label
    );

    /**
     * Lists permissions granted by a role.
     *
     * @param loggedInUser current user
     * @param label role label
     * @return granted permissions
     */
    @ApiEndpointDoc(
        summary = "List permissions granted by a role.",
        method = HttpMethod.get,
        responseClass = NamespaceListResponse.class
    )
    Set<Namespace> listPermissions(
        @Parameter(hidden = true) User loggedInUser,
        @Parameter(
            name = "label",
            description = "the unique label of the role",
            in = ParameterIn.QUERY,
            required = true
        ) String label
    );

    /**
     * Grants access to namespaces for a role.
     *
     * @param loggedInUser current user
     * @param label role label
     * @param namespaces namespaces to grant
     * @return {@code 1} on success
     */
    @ApiEndpointDoc(
        summary = "Grant access to the given namespace for the specified role.\n" +
                "Returns the expanded list of namespaces granted by the call.",
        requestDescription = "Access grant data",
        requestClass = GrantAccessRequest.class,
        isIntegerResponse = true
    )
    int grantAccess(User loggedInUser, String label, List<String> namespaces);

    /**
     * Revokes access to namespaces for a role.
     *
     * @param loggedInUser current user
     * @param label role label
     * @param namespaces namespaces to revoke
     * @param modes access modes to revoke
     * @return {@code 1} on success
     */
    @ApiEndpointDoc(
        summary = "Revoke access to the given namespace for the specified role.\n" +
                "Returns the expanded list of namespaces revoked by the call.",
        requestDescription = "Access revocation data with modes",
        requestClass = RevokeAccessRequest.class,
        isIntegerResponse = true
    )
    int revokeAccess(User loggedInUser, String label, List<String> namespaces, List<String> modes);

    @Schema(name = "RevokeAccessRequest")
    @JsonPropertyOrder({"label", "namespaces", "modes"})
    interface RevokeAccessRequest {

        /**
         * @return role label
         */
        @Schema(description = "the unique label of the role", requiredMode = REQUIRED)
        String getLabel();

        /**
         * @return namespaces to revoke
         */
        @Schema(description = "the list of namespaces to revoke access to", requiredMode = REQUIRED)
        List<String> getNamespaces();

        /**
         * @return access modes to revoke
         */
        @Schema(description = "the access modes (R for read/view, W for write/modify)", requiredMode = NOT_REQUIRED)
        List<String> getModes();
    }

    @Schema(name = "CreateRoleRequest")
    @JsonPropertyOrder({"label", "description", "permissionsFrom"})
    interface CreateRoleRequest {

        /**
         * @return new role label
         */
        @Schema(description = "the unique label of the new role", requiredMode = REQUIRED)
        String getLabel();

        /**
         * @return new role description
         */
        @Schema(description = "the description of the new role", requiredMode = REQUIRED)
        String getDescription();

        /**
         * @return roles to inherit permissions from
         */
        @Schema(description = "the list of roles to inherit permissions from", requiredMode = NOT_REQUIRED)
        List<String> getPermissionsFrom();
    }

    @Schema(name = "AccessGroup", description = "access group")
    @JsonPropertyOrder({"label", "description"})
    interface AccessGroupDoc {

        /**
         * @return access group label
         */
        @Schema(requiredMode = REQUIRED)
        String getLabel();

        /**
         * @return access group description
         */
        @Schema(requiredMode = REQUIRED)
        String getDescription();
    }

    @Schema(name = "Namespace", description = "namespace")
    @JsonPropertyOrder({"namespace", "accessMode", "description"})
    interface NamespaceDoc {

        /**
         * @return namespace identifier
         */
        @Schema(description = "namespace identifier", requiredMode = REQUIRED)
        String getNamespace();

        /**
         * @return access mode
         */
        @Schema(name = "access_mode", description = "access mode (R/W)", requiredMode = REQUIRED)
        String getAccessMode();

        /**
         * @return namespace description
         */
        @Schema(description = "description of the namespace", requiredMode = REQUIRED)
        String getDescription();
    }

    @Schema(name = "GrantAccessRequest")
    @JsonPropertyOrder({"label", "namespaces", "modes"})
    interface GrantAccessRequest {

        /**
         * @return role label
         */
        @Schema(description = "the unique label of the role", requiredMode = REQUIRED)
        String getLabel();

        /**
         * @return namespaces to grant
         */
        @Schema(description = "the list of namespaces to grant access to", requiredMode = REQUIRED)
        List<String> getNamespaces();

        /**
         * @return access modes to grant
         */
        @Schema(description = "the access modes (R for read/view, W for write/modify)", requiredMode = NOT_REQUIRED)
        List<String> getModes();
    }

    @Schema(name = "ApiResponseAccessGroup")
    interface AccessGroupResponse extends ApiResponseWrapper<AccessGroupDoc> { }

    @Schema(name = "ApiResponseAccessGroupList")
    interface AccessGroupListResponse extends ApiResponseWrapper<List<AccessGroupDoc>> { }

    @Schema(name = "ApiResponseNamespaceList")
    interface NamespaceListResponse extends ApiResponseWrapper<List<NamespaceDoc>> { }
}
