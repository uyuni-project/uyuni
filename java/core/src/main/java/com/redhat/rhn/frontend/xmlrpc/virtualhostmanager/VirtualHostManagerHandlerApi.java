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
package com.redhat.rhn.frontend.xmlrpc.virtualhostmanager;

import static io.swagger.v3.oas.annotations.media.Schema.RequiredMode.REQUIRED;

import com.redhat.rhn.domain.server.virtualhostmanager.VirtualHostManager;
import com.redhat.rhn.domain.user.User;

import com.suse.manager.api.ApiResponseWrapper;
import com.suse.manager.api.docs.ApiEndpointDoc;
import com.suse.manager.api.docs.LegacyDocResponse;

import com.fasterxml.jackson.annotation.JsonPropertyOrder;

import java.util.Collection;
import java.util.List;
import java.util.Map;

import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.enums.ParameterIn;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.tags.Tag;
import spark.route.HttpMethod;

/**
 * API contract for {@link VirtualHostManagerHandler}.
 */
@Tag(name = "virtualhostmanager", description = "Provides the namespace for the Virtual Host Manager methods.")
public interface VirtualHostManagerHandlerApi {

    /**
     * Lists the Virtual Host Managers visible to the user.
     *
     * @param loggedInUser the current user
     * @return the visible Virtual Host Managers
     */
    @ApiEndpointDoc(
        summary = "Lists Virtual Host Managers visible to a user",
        method = HttpMethod.get,
        responseClass = VirtualHostManagerListResponse.class
    )
    List<VirtualHostManager> listVirtualHostManagers(User loggedInUser);

    /**
     * Creates a Virtual Host Manager.
     *
     * @param loggedInUser the current user
     * @param label the Virtual Host Manager label
     * @param moduleName the name of the Gatherer module
     * @param parameters additional parameters
     * @return 1 on success
     */
    @ApiEndpointDoc(
        summary = "Creates a Virtual Host Manager from given arguments",
        requestClass = CreateVirtualHostManagerRequest.class,
        isIntegerResponse = true
    )
    int create(User loggedInUser, String label, String moduleName, Map<String, String> parameters);

    /**
     * Deletes a Virtual Host Manager.
     *
     * @param loggedInUser the current user
     * @param label the Virtual Host Manager label
     * @return 1 on success
     */
    @ApiEndpointDoc(
        summary = "Deletes a Virtual Host Manager with a given label",
        requestClass = VirtualHostManagerLabelRequest.class,
        isIntegerResponse = true
    )
    int delete(User loggedInUser, String label);

    /**
     * Gets the details of a Virtual Host Manager.
     *
     * @param loggedInUser the current user
     * @param label the Virtual Host Manager label
     * @return the Virtual Host Manager
     */
    @ApiEndpointDoc(
        summary = "Gets details of a Virtual Host Manager with a given label",
        method = HttpMethod.get,
        responseClass = VirtualHostManagerResponse.class
    )
    VirtualHostManager getDetail(
        @Parameter(hidden = true) User loggedInUser,
        @Parameter(name = "label", description = "Virtual Host Manager label",
            in = ParameterIn.QUERY, required = true) String label
    );

    /**
     * Lists the available virtual-host-gatherer modules.
     *
     * @param loggedInUser the current user
     * @return the available module names
     */
    @ApiEndpointDoc(
        summary = "List all available modules from virtual-host-gatherer",
        method = HttpMethod.get,
        responseClass = ModuleNameListResponse.class,
        responseDescription = "moduleName"
    )
    Collection<String> listAvailableVirtualHostGathererModules(User loggedInUser);

    /**
     * Gets the parameters of a virtual-host-gatherer module.
     *
     * @param loggedInUser the current user
     * @param moduleName the name of the module
     * @return the module parameters
     */
    @ApiEndpointDoc(
        summary = "Get a list of parameters for a virtual-host-gatherer module.",
        method = HttpMethod.get,
        responseClass = ModuleParametersResponse.class,
        legacyDocResponse = @LegacyDocResponse(type = "map", name = "module_params")
    )
    Map<String, String> getModuleParameters(
        @Parameter(hidden = true) User loggedInUser,
        @Parameter(name = "moduleName", description = "The name of the module",
            in = ParameterIn.QUERY, required = true) String moduleName
    );

    @Schema(name = "CreateVirtualHostManagerRequest")
    @JsonPropertyOrder({"label", "moduleName", "parameters"})
    interface CreateVirtualHostManagerRequest {

        /**
         * @return the Virtual Host Manager label
         */
        @Schema(description = "Virtual Host Manager label", requiredMode = REQUIRED)
        String getLabel();

        /**
         * @return the name of the Gatherer module
         */
        @Schema(description = "the name of the Gatherer module", requiredMode = REQUIRED)
        String getModuleName();

        /**
         * @return the additional parameters
         */
        @LegacyDocResponse(type = "parameters")
        @Schema(description = "additional parameters (credentials, parameters for virtual-host-gatherer)",
                requiredMode = REQUIRED)
        Map<String, String> getParameters();
    }

    @Schema(name = "VirtualHostManagerLabelRequest")
    interface VirtualHostManagerLabelRequest {

        /**
         * @return the Virtual Host Manager label
         */
        @Schema(description = "Virtual Host Manager label", requiredMode = REQUIRED)
        String getLabel();
    }

    @Schema(name = "VirtualHostManager", description = "virtual host manager")
    @JsonPropertyOrder({"label", "orgId", "gathererModule", "configs"})
    interface VirtualHostManagerDoc {

        /**
         * @return the Virtual Host Manager label
         */
        @Schema(requiredMode = REQUIRED)
        String getLabel();

        /**
         * @return the organization identifier
         */
        @Schema(name = "org_id", requiredMode = REQUIRED)
        Integer getOrgId();

        /**
         * @return the Gatherer module name
         */
        @Schema(name = "gatherer_module", requiredMode = REQUIRED)
        String getGathererModule();

        /**
         * @return the Virtual Host Manager configuration
         */
        @Schema(requiredMode = REQUIRED)
        Map<String, String> getConfigs();
    }

    @Schema(name = "ApiResponseVirtualHostManagerList")
    interface VirtualHostManagerListResponse extends ApiResponseWrapper<List<VirtualHostManagerDoc>> { }

    @Schema(name = "ApiResponseVirtualHostManager")
    interface VirtualHostManagerResponse extends ApiResponseWrapper<VirtualHostManagerDoc> { }

    @Schema(name = "ApiResponseModuleNameList")
    interface ModuleNameListResponse extends ApiResponseWrapper<List<String>> { }

    @Schema(name = "ApiResponseModuleParameters")
    interface ModuleParametersResponse extends ApiResponseWrapper<Map<String, String>> { }
}
