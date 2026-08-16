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
package com.redhat.rhn.frontend.xmlrpc.system.monitoring;

import com.redhat.rhn.domain.dto.EndpointInfo;
import com.redhat.rhn.domain.user.User;

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
 * API contract for {@link SystemMonitoringHandler}.
 */
@Tag(name = "system.monitoring",
     description = "Provides methods to access information about managed systems, applications and formulas " +
             "which can be\nrelevant for Prometheus monitoring")
public interface SystemMonitoringHandlerApi {

    /**
     * Lists the monitoring endpoints of the given systems.
     *
     * @param loggedInUser the current user
     * @param sids the system ids
     * @return the monitoring endpoints
     */
    @ApiEndpointDoc(
        summary = "Get the list of monitoring endpoint details.",
        method = HttpMethod.get,
        responseClass = EndpointInfoListResponse.class,
        legacyDocResponse = @LegacyDocResponse(name = "endpoint info")
    )
    List<EndpointInfo> listEndpoints(
        @Parameter(hidden = true) User loggedInUser,
        @Parameter(name = "sids", in = ParameterIn.QUERY, required = true) List<Integer> sids);

    @Schema(name = "MonitoringEndpointInfo")
    @JsonPropertyOrder({"systemId", "endpointName", "exporterName", "module", "path", "port", "tlsEnabled"})
    interface EndpointInfoDoc {

        /**
         * @return the system id
         */
        @Schema(name = "system_id", requiredMode = Schema.RequiredMode.REQUIRED)
        Integer getSystemId();

        /**
         * @return the endpoint name
         */
        @Schema(name = "endpoint_name", requiredMode = Schema.RequiredMode.REQUIRED)
        String getEndpointName();

        /**
         * @return the exporter name
         */
        @Schema(name = "exporter_name", requiredMode = Schema.RequiredMode.REQUIRED)
        String getExporterName();

        /**
         * @return the module of the endpoint
         */
        @Schema(requiredMode = Schema.RequiredMode.REQUIRED)
        String getModule();

        /**
         * @return the path of the endpoint
         */
        @Schema(requiredMode = Schema.RequiredMode.REQUIRED)
        String getPath();

        /**
         * @return the port of the endpoint
         */
        @Schema(requiredMode = Schema.RequiredMode.REQUIRED)
        Integer getPort();

        /**
         * @return whether TLS is enabled on the endpoint
         */
        @Schema(name = "tls_enabled", requiredMode = Schema.RequiredMode.REQUIRED)
        @LegacyDocResponse(type = "bool")
        Boolean getTlsEnabled();
    }

    @Schema(name = "ApiResponseMonitoringEndpointInfoList")
    interface EndpointInfoListResponse extends ApiResponseWrapper<List<EndpointInfoDoc>> { }
}
