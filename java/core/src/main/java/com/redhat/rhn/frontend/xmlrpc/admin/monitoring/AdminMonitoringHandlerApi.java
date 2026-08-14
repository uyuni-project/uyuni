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
package com.redhat.rhn.frontend.xmlrpc.admin.monitoring;

import static io.swagger.v3.oas.annotations.media.Schema.RequiredMode.REQUIRED;

import com.redhat.rhn.domain.user.User;

import com.suse.manager.api.ApiResponseWrapper;
import com.suse.manager.api.docs.ApiEndpointDoc;
import com.suse.manager.api.docs.LegacyDocResponse;

import com.fasterxml.jackson.annotation.JsonPropertyOrder;

import java.util.Map;

import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.tags.Tag;
import spark.route.HttpMethod;

/**
 * API contract for {@link AdminMonitoringHandler}.
 */
@Tag(name = "admin.monitoring", description = "Provides methods to manage the monitoring of the Uyuni server.")
public interface AdminMonitoringHandlerApi {

    /**
     * Enables monitoring.
     *
     * @param loggedInUser the current user
     * @return the status of each exporter
     */
    @ApiEndpointDoc(
        summary = "Enable monitoring.",
        responseClass = ExportersResponse.class,
        legacyDocResponse = @LegacyDocResponse(name = "Exporters")
    )
    Map<String, String> enable(User loggedInUser);

    /**
     * Disables monitoring.
     *
     * @param loggedInUser the current user
     * @return the status of each exporter
     */
    @ApiEndpointDoc(
        summary = "Disable monitoring.",
        responseClass = ExportersResponse.class,
        legacyDocResponse = @LegacyDocResponse(name = "Exporters")
    )
    Map<String, String> disable(User loggedInUser);

    /**
     * Returns the status of each Prometheus exporter.
     *
     * @param loggedInUser the current user
     * @return the status of each exporter
     */
    @ApiEndpointDoc(
        summary = "Get the status of each Prometheus exporter.",
        method = HttpMethod.get,
        responseClass = ExportersResponse.class,
        legacyDocResponse = @LegacyDocResponse(name = "Exporters")
    )
    Map<String, String> getStatus(User loggedInUser);

    @Schema(name = "Exporters", description = "Exporters")
    @JsonPropertyOrder({"node", "tomcat", "taskomatic", "postgres", "selfMonitoring"})
    interface ExportersDoc {

        /**
         * @return the status of the node exporter
         */
        @Schema(requiredMode = REQUIRED)
        String getNode();

        /**
         * @return the status of the tomcat exporter
         */
        @Schema(requiredMode = REQUIRED)
        String getTomcat();

        /**
         * @return the status of the taskomatic exporter
         */
        @Schema(requiredMode = REQUIRED)
        String getTaskomatic();

        /**
         * @return the status of the postgres exporter
         */
        @Schema(requiredMode = REQUIRED)
        String getPostgres();

        /**
         * @return the status of the self monitoring exporter
         */
        @Schema(name = "self_monitoring", requiredMode = REQUIRED)
        String getSelfMonitoring();
    }

    @Schema(name = "ApiResponseExporters")
    interface ExportersResponse extends ApiResponseWrapper<ExportersDoc> { }
}
