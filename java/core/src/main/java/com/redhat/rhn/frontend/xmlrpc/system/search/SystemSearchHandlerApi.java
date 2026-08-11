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
package com.redhat.rhn.frontend.xmlrpc.system.search;

import com.redhat.rhn.frontend.dto.SystemSearchResult;

import com.suse.manager.api.ApiResponseWrapper;
import com.suse.manager.api.docs.ApiEndpointDoc;
import com.suse.manager.api.docs.LegacyDocResponse;

import com.fasterxml.jackson.annotation.JsonPropertyOrder;

import java.util.Date;
import java.util.List;

import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.enums.ParameterIn;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.tags.Tag;
import spark.route.HttpMethod;

/**
 * API contract for {@link SystemSearchHandler}.
 */
@Tag(name = "system.search",
     description = "Provides methods to perform system search requests using the search server.")
public interface SystemSearchHandlerApi {

    /**
     * Lists the systems which match this ip.
     *
     * @param sessionKey the session key
     * @param searchTerm the term to search for
     * @return the matching systems
     */
    @ApiEndpointDoc(
        summary = "List the systems which match this ip.",
        method = HttpMethod.get,
        responseClass = SystemSearchResultListResponse.class,
        legacyDocResponse = @LegacyDocResponse(name = "system")
    )
    List<SystemSearchResult> ip(
        @Parameter(hidden = true) String sessionKey,
        @Parameter(name = "searchTerm", in = ParameterIn.QUERY, required = true) String searchTerm);

    /**
     * Lists the systems which match this hostname.
     *
     * @param sessionKey the session key
     * @param searchTerm the term to search for
     * @return the matching systems
     */
    @ApiEndpointDoc(
        summary = "List the systems which match this hostname",
        method = HttpMethod.get,
        responseClass = SystemSearchResultListResponse.class,
        legacyDocResponse = @LegacyDocResponse(name = "system")
    )
    List<SystemSearchResult> hostname(
        @Parameter(hidden = true) String sessionKey,
        @Parameter(name = "searchTerm", in = ParameterIn.QUERY, required = true) String searchTerm);

    /**
     * Lists the systems which match this device vendor_id.
     *
     * @param sessionKey the session key
     * @param searchTerm the term to search for
     * @return the matching systems
     */
    @ApiEndpointDoc(
        summary = "List the systems which match this device vendor_id",
        method = HttpMethod.get,
        responseClass = SystemSearchResultListResponse.class,
        legacyDocResponse = @LegacyDocResponse(name = "system")
    )
    List<SystemSearchResult> deviceVendorId(
        @Parameter(hidden = true) String sessionKey,
        @Parameter(name = "searchTerm", in = ParameterIn.QUERY, required = true) String searchTerm);

    /**
     * Lists the systems which match this device id.
     *
     * @param sessionKey the session key
     * @param searchTerm the term to search for
     * @return the matching systems
     */
    @ApiEndpointDoc(
        summary = "List the systems which match this device id",
        method = HttpMethod.get,
        responseClass = SystemSearchResultListResponse.class,
        legacyDocResponse = @LegacyDocResponse(name = "system")
    )
    List<SystemSearchResult> deviceId(
        @Parameter(hidden = true) String sessionKey,
        @Parameter(name = "searchTerm", in = ParameterIn.QUERY, required = true) String searchTerm);

    /**
     * Lists the systems which match this device driver.
     *
     * @param sessionKey the session key
     * @param searchTerm the term to search for
     * @return the matching systems
     */
    @ApiEndpointDoc(
        summary = "List the systems which match this device driver.",
        method = HttpMethod.get,
        responseClass = SystemSearchResultListResponse.class,
        legacyDocResponse = @LegacyDocResponse(name = "system")
    )
    List<SystemSearchResult> deviceDriver(
        @Parameter(hidden = true) String sessionKey,
        @Parameter(name = "searchTerm", in = ParameterIn.QUERY, required = true) String searchTerm);

    /**
     * Lists the systems which match the device description.
     *
     * @param sessionKey the session key
     * @param searchTerm the term to search for
     * @return the matching systems
     */
    @ApiEndpointDoc(
        summary = "List the systems which match the device description.",
        method = HttpMethod.get,
        responseClass = SystemSearchResultListResponse.class,
        legacyDocResponse = @LegacyDocResponse(name = "system")
    )
    List<SystemSearchResult> deviceDescription(
        @Parameter(hidden = true) String sessionKey,
        @Parameter(name = "searchTerm", in = ParameterIn.QUERY, required = true) String searchTerm);

    /**
     * Lists the systems which match this name or description.
     *
     * @param sessionKey the session key
     * @param searchTerm the term to search for
     * @return the matching systems
     */
    @ApiEndpointDoc(
        summary = "List the systems which match this name or description",
        method = HttpMethod.get,
        responseClass = SystemSearchResultListResponse.class,
        legacyDocResponse = @LegacyDocResponse(name = "system")
    )
    List<SystemSearchResult> nameAndDescription(
        @Parameter(hidden = true) String sessionKey,
        @Parameter(name = "searchTerm", in = ParameterIn.QUERY, required = true) String searchTerm);

    /**
     * Lists the systems which match this UUID.
     *
     * @param sessionKey the session key
     * @param searchTerm the term to search for
     * @return the matching systems
     */
    @ApiEndpointDoc(
        summary = "List the systems which match this UUID",
        method = HttpMethod.get,
        responseClass = SystemSearchResultListResponse.class,
        legacyDocResponse = @LegacyDocResponse(name = "system")
    )
    List<SystemSearchResult> uuid(
        @Parameter(hidden = true) String sessionKey,
        @Parameter(name = "searchTerm", in = ParameterIn.QUERY, required = true) String searchTerm);

    @Schema(name = "ApiResponseSystemSearchResultList")
    interface SystemSearchResultListResponse extends ApiResponseWrapper<List<SystemSearchResultDoc>> { }

    @Schema(name = "SystemSearchResult")
    @JsonPropertyOrder({"id", "name", "lastCheckin", "hostname", "uuid", "ip", "hwDescription",
        "hwDeviceId", "hwVendorId", "hwDriver"})
    interface SystemSearchResultDoc {

        /**
         * @return the system identifier
         */
        @Schema(requiredMode = Schema.RequiredMode.REQUIRED)
        Integer getId();

        /**
         * @return the system name
         */
        @Schema(requiredMode = Schema.RequiredMode.REQUIRED)
        String getName();

        /**
         * @return the last time the server successfully checked in
         */
        @Schema(name = "last_checkin", description = "last time server successfully checked in",
                requiredMode = Schema.RequiredMode.REQUIRED)
        Date getLastCheckin();

        /**
         * @return the system hostname
         */
        @Schema(requiredMode = Schema.RequiredMode.REQUIRED)
        String getHostname();

        /**
         * @return the system UUID
         */
        @Schema(requiredMode = Schema.RequiredMode.NOT_REQUIRED)
        String getUuid();

        /**
         * @return the system IP address
         */
        @Schema(requiredMode = Schema.RequiredMode.REQUIRED)
        String getIp();

        /**
         * @return the hardware description
         */
        @Schema(name = "hw_description", description = "HW description if not null",
                requiredMode = Schema.RequiredMode.NOT_REQUIRED)
        String getHwDescription();

        /**
         * @return the hardware device identifier
         */
        @Schema(name = "hw_device_id", description = "HW device id if not null",
                requiredMode = Schema.RequiredMode.NOT_REQUIRED)
        String getHwDeviceId();

        /**
         * @return the hardware vendor identifier
         */
        @Schema(name = "hw_vendor_id", description = "HW vendor id if not null",
                requiredMode = Schema.RequiredMode.NOT_REQUIRED)
        String getHwVendorId();

        /**
         * @return the hardware driver
         */
        @Schema(name = "hw_driver", description = "HW driver if not null",
                requiredMode = Schema.RequiredMode.NOT_REQUIRED)
        String getHwDriver();
    }
}
