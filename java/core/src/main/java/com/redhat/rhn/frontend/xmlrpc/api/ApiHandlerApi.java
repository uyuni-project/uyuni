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
package com.redhat.rhn.frontend.xmlrpc.api;

import static io.swagger.v3.oas.annotations.media.Schema.RequiredMode.REQUIRED;

import com.suse.manager.api.ApiResponseWrapper;
import com.suse.manager.api.docs.ApiEndpointDoc;
import com.suse.manager.api.docs.PublicApiEndpoint;

import io.swagger.models.HttpMethod;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.enums.ParameterIn;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.tags.Tag;

import java.util.List;
import java.util.Map;

/**
 * API Contract for ApiHandler.
 */
@Tag(name = "api", description = "Methods providing information about the API.")
public interface ApiHandlerApi {

    @PublicApiEndpoint
    @ApiEndpointDoc(
        summary = "Returns the server version.",
        method = HttpMethod.GET,
        responseClass = StringResponse.class
    )
    String systemVersion();

    @PublicApiEndpoint
    @ApiEndpointDoc(
        summary = "Returns the server product name.",
        method = HttpMethod.GET,
        responseClass = StringResponse.class
    )
    String productName();

    @PublicApiEndpoint
    @ApiEndpointDoc(
        summary = "Returns the version of the API.",
        method = HttpMethod.GET,
        responseClass = StringResponse.class
    )
    String getVersion();

    @PublicApiEndpoint
    @ApiEndpointDoc(
        summary = "Lists available API namespaces",
        method = HttpMethod.GET,
        responseClass = ApiNamespacesResponse.class
    )
    Map<String, String> getApiNamespaces();

    @PublicApiEndpoint
    @ApiEndpointDoc(
        summary = "Lists all available api calls grouped by namespace",
        method = HttpMethod.GET,
        responseClass = ApiCallListResponse.class
    )
    Map<String, Object> getApiCallList();

    @PublicApiEndpoint
    @ApiEndpointDoc(
        summary = "Lists all available api calls for the specified namespace",
        method = HttpMethod.GET,
        responseClass = ApiNamespaceCallListResponse.class
    )
    Map getApiNamespaceCallList(
        @Parameter(
            name = "namespace",
            description = "the namespace of interest",
            in = ParameterIn.QUERY,
            required = true
        ) String namespace
    );

    @Schema(name = "MethodInfo", description = "Information about an API method")
    interface MethodInfoDoc {
        @Schema(description = "method name", requiredMode = REQUIRED)
        String getName();

        @Schema(description = "method parameters", requiredMode = REQUIRED)
        List<String> getParameters();

        @Schema(description = "method exceptions", requiredMode = REQUIRED)
        List<String> getExceptions();

        @Schema(description = "method return type", requiredMode = REQUIRED)
        String getReturn();
    }

    @Schema(name = "ApiResponseString")
    interface StringResponse extends ApiResponseWrapper<String> {}

    @Schema(name = "ApiResponseApiNamespaces")
    interface ApiNamespacesResponse extends ApiResponseWrapper<Map<String, String>> {}

    @Schema(name = "ApiResponseApiCallList")
    interface ApiCallListResponse extends ApiResponseWrapper<Map<String, Map<String, MethodInfoDoc>>> {}

    @Schema(name = "ApiResponseApiNamespaceCallList")
    interface ApiNamespaceCallListResponse extends ApiResponseWrapper<Map<String, MethodInfoDoc>> {}
}
