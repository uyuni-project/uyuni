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

import com.fasterxml.jackson.annotation.JsonPropertyOrder;

import java.util.List;
import java.util.Map;

import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.enums.ParameterIn;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.tags.Tag;
import spark.route.HttpMethod;

/**
 * API contract for {@link ApiHandler}.
 */
@Tag(name = "api", description = "Methods providing information about the API.")
public interface ApiHandlerApi {

    /**
     * Returns the server version.
     *
     * @return server version
     */
    @PublicApiEndpoint
    @ApiEndpointDoc(
        summary = "Returns the server version.",
        method = HttpMethod.get,
        responseClass = StringResponse.class,
        responseDescription = "version"
    )
    String systemVersion();

    /**
     * Returns the server product name.
     *
     * @return product name
     */
    @PublicApiEndpoint
    @ApiEndpointDoc(
        summary = "Returns the server product name.",
        method = HttpMethod.get,
        responseClass = StringResponse.class
    )
    String productName();

    /**
     * Returns the API version.
     *
     * @return API version
     */
    @PublicApiEndpoint
    @ApiEndpointDoc(
        summary = "Returns the version of the API.",
        method = HttpMethod.get,
        responseClass = StringResponse.class
    )
    String getVersion();

    /**
     * Lists available API namespaces.
     *
     * @return namespace map
     */
    @PublicApiEndpoint
    @ApiEndpointDoc(
        summary = "Lists available API namespaces",
        method = HttpMethod.get,
        responseClass = ApiNamespacesResponse.class,
        legacyDocResponseClass = ApiNamespaceDoc.class,
        responseDescription = "namespace"
    )
    Map<String, String> getApiNamespaces();

    /**
     * Lists all available API calls grouped by namespace.
     *
     * @return call list by namespace
     */
    @PublicApiEndpoint
    @ApiEndpointDoc(
        summary = "Lists all available api calls grouped by namespace",
        method = HttpMethod.get,
        responseClass = ApiCallListResponse.class,
        legacyDocResponseClass = MethodInfoDoc.class,
        responseDescription = "method_info"
    )
    Map<String, Object> getApiCallList();

    /**
     * Lists all available API calls for the specified namespace.
     *
     * @param namespace namespace of interest
     * @return call list for the namespace
     */
    @PublicApiEndpoint
    @ApiEndpointDoc(
        summary = "Lists all available api calls for the specified namespace",
        method = HttpMethod.get,
        responseClass = ApiNamespaceCallListResponse.class,
        legacyDocResponseClass = MethodInfoDoc.class,
        responseDescription = "method_info"
    )
    Map getApiNamespaceCallList(
        @Parameter(
            name = "namespace",
            description = "the namespace of interest",
            in = ParameterIn.QUERY,
            required = true
        ) String namespace
    );

    @Schema(name = "method_info", description = "Information about an API method")
    @JsonPropertyOrder({"name", "parameters", "exceptions", "return"})
    interface MethodInfoDoc {

        /**
         * @return method name
         */
        @Schema(description = "method name", requiredMode = REQUIRED)
        String getName();

        /**
         * @return method parameters
         */
        @Schema(description = "method parameters", requiredMode = REQUIRED)
        List<String> getParameters();

        /**
         * @return method exceptions
         */
        @Schema(description = "method exceptions", requiredMode = REQUIRED)
        List<String> getExceptions();

        /**
         * @return method return type
         */
        @Schema(description = "method return type", requiredMode = REQUIRED)
        String getReturn();
    }

    @Schema(name = "namespace")
    @JsonPropertyOrder({"namespace", "handler"})
    interface ApiNamespaceDoc {

        /**
         * @return API namespace
         */
        @Schema(description = "API namespace", requiredMode = REQUIRED)
        String getNamespace();

        /**
         * @return API Handler
         */
        @Schema(description = "API Handler", requiredMode = REQUIRED)
        String getHandler();
    }

    @Schema(name = "ApiResponseString")
    interface StringResponse extends ApiResponseWrapper<String> { }

    @Schema(name = "ApiResponseApiNamespaces")
    interface ApiNamespacesResponse extends ApiResponseWrapper<Map<String, String>> { }

    @Schema(name = "ApiResponseApiCallList")
    interface ApiCallListResponse extends ApiResponseWrapper<Map<String, Map<String, MethodInfoDoc>>> { }

    @Schema(name = "ApiResponseApiNamespaceCallList")
    interface ApiNamespaceCallListResponse extends ApiResponseWrapper<Map<String, MethodInfoDoc>> { }
}
