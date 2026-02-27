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

import com.redhat.rhn.domain.access.AccessGroup;
import com.redhat.rhn.domain.access.Namespace;
import com.redhat.rhn.domain.user.User;

import static io.swagger.v3.oas.annotations.media.Schema.RequiredMode.NOT_REQUIRED;
import static io.swagger.v3.oas.annotations.media.Schema.RequiredMode.REQUIRED;

import com.suse.manager.api.ApiResponseWrapper;
import com.suse.manager.api.docs.ApiEndpointDoc;

import java.util.List;
import java.util.Set;

import io.swagger.models.HttpMethod;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.enums.ParameterIn;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.tags.Tag;

/**
 * API Contract for AccessHandler.
 */
@Tag(name = "access", description = "Provides methods to manage Role-Based Access Control")
public interface AccessHandlerApi {

    @ApiEndpointDoc(
        summary = "Create a new role.",
        requestClass = CreateRoleRequest.class,
        responseClass = AccessGroupResponse.class
    )
    AccessGroup createRole(User loggedInUser, String label, String description, List<String> permissionsFrom);

    @ApiEndpointDoc(
        summary = "List existing roles.",
        method = HttpMethod.GET,
        responseClass = AccessGroupListResponse.class
    )
    List<AccessGroup> listRoles(User loggedInUser);

    @ApiEndpointDoc(
        summary = "List available namespaces.",
        method = HttpMethod.GET,
        responseClass = NamespaceListResponse.class
    )
    List<Namespace> listNamespaces(User loggedInUser);

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

    @ApiEndpointDoc(
        summary = "List permissions granted by a role.",
        method = HttpMethod.GET,
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

    @ApiEndpointDoc(
        summary = "Grant access to the given namespace for the specified role.\n"+
                    "Returns the expanded list of namespaces granted by the call.",
        requestDescription = "Access grant data",
        requestClass = GrantAccessRequest.class,
        isIntegerResponse = true
    )
    int grantAccess(User loggedInUser, String label, List<String> namespaces);

    @ApiEndpointDoc(
        summary = "Revoke access to the given namespace for the specified role.\n"+
                    "Returns the expanded list of namespaces revoked by the call.",
        requestDescription = "Access revocation data with modes",
        requestClass = RevokeAccessRequest.class,
        isIntegerResponse = true
    )
    int revokeAccess(User loggedInUser, String label, List<String> namespaces, List<String> modes);

    @Schema(name = "RevokeAccessRequest")
    interface RevokeAccessRequest {
        @Schema(description = "the unique label of the role", requiredMode = REQUIRED)
        String getLabel();

        @Schema(description = "the list of namespaces to revoke access to", requiredMode = REQUIRED)
        List<String> getNamespaces();

        @Schema(description = "the access modes (R for read/view, W for write/modify)", requiredMode = NOT_REQUIRED)
        List<String> getModes();
    }

    @Schema(name = "CreateRoleRequest")
    interface CreateRoleRequest {
        @Schema(description = "the unique label of the new role", requiredMode = REQUIRED)
        String getLabel();

        @Schema(description = "the description of the new role", requiredMode = REQUIRED)
        String getDescription();

        @Schema(description = "the list of roles to inherit permissions from", requiredMode = NOT_REQUIRED)
        List<String> getPermissionsFrom();
    }

    @Schema(name = "AccessGroup", description = "access group")
    interface AccessGroupDoc {
        @Schema(requiredMode = REQUIRED)
        String getLabel();

        @Schema(requiredMode = REQUIRED)
        String getDescription();
    }

    @Schema(name = "Namespace", description = "namespace")
    interface NamespaceDoc {
        @Schema(description = "namespace identifier", requiredMode = REQUIRED)
        String getNamespace();

        @Schema(name = "access_mode", description = "access mode (R/W)", requiredMode = REQUIRED)
        String getAccessMode();

        @Schema(description = "description of the namespace", requiredMode = REQUIRED)
        String getDescription();
    }

    @Schema(name = "GrantAccessRequest")
    interface GrantAccessRequest {
        @Schema(description = "the unique label of the role", requiredMode = REQUIRED)
        String getLabel();

        @Schema(description = "the list of namespaces to grant access to", requiredMode = REQUIRED)
        List<String> getNamespaces();

        @Schema(description = "the access modes (R for read/view, W for write/modify)", requiredMode = NOT_REQUIRED)
        List<String> getModes();
    }

    @Schema(name = "ApiResponseAccessGroup")
    interface AccessGroupResponse extends ApiResponseWrapper<AccessGroupDoc> {}

    @Schema(name = "ApiResponseAccessGroupList")
    interface AccessGroupListResponse extends ApiResponseWrapper<List<AccessGroupDoc>> {}

    @Schema(name = "ApiResponseNamespaceList")
    interface NamespaceListResponse extends ApiResponseWrapper<List<NamespaceDoc>> {}
}
