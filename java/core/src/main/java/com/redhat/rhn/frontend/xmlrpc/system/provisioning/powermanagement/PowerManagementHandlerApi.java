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
package com.redhat.rhn.frontend.xmlrpc.system.provisioning.powermanagement;

import com.redhat.rhn.domain.user.User;

import com.suse.manager.api.ApiResponseWrapper;
import com.suse.manager.api.docs.ApiEndpointDoc;
import com.suse.manager.api.docs.LegacyDocResponse;

import com.fasterxml.jackson.annotation.JsonPropertyOrder;

import java.util.List;
import java.util.Map;

import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.enums.ParameterIn;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.tags.Tag;
import spark.route.HttpMethod;

/**
 * API contract for {@link PowerManagementHandler}.
 */
@Tag(name = "system.provisioning.powermanagement",
    description = "Provides methods to access and modify power management for systems.")
public interface PowerManagementHandlerApi {

    /**
     * Returns the list of available power management types.
     *
     * @param loggedInUser the current user
     * @return the available power management types
     */
    @ApiEndpointDoc(
        summary = "Return a list of available power management types",
        method = HttpMethod.get,
        responseClass = PowerTypeListResponse.class,
        legacyDocResponse = @LegacyDocResponse(name = "power management types")
    )
    List<String> listTypes(User loggedInUser);

    /**
     * Returns the current power management settings of the given system.
     *
     * @param loggedInUser the current user
     * @param sid the system id
     * @return the power management settings
     */
    @ApiEndpointDoc(
        summary = "Get current power management settings of the given system",
        method = HttpMethod.get,
        responseClass = PowerManagementParametersResponse.class,
        legacyDocResponse = @LegacyDocResponse(name = "powerManagementParameters")
    )
    Map<String, String> getDetails(
        @Parameter(hidden = true) User loggedInUser,
        @Parameter(name = "sid", in = ParameterIn.QUERY, required = true) Integer sid);

    /**
     * Returns the current power management settings of the given system.
     *
     * @param loggedInUser the current user
     * @param name the system name
     * @return the power management settings
     */
    @ApiEndpointDoc(
        summary = "Get current power management settings of the given system",
        method = HttpMethod.get,
        responseClass = PowerManagementParametersResponse.class,
        legacyDocResponse = @LegacyDocResponse(name = "powerManagementParameters")
    )
    Map<String, String> getDetails(
        @Parameter(hidden = true) User loggedInUser,
        @Parameter(name = "name", in = ParameterIn.QUERY, required = true) String name);

    /**
     * Sets the power management settings of the given system.
     *
     * @param loggedInUser the current user
     * @param sid the system id
     * @param data the power management settings
     * @return 1 on success
     */
    @ApiEndpointDoc(
        summary = "Get current power management settings of the given system",
        requestClass = SetDetailsBySidRequest.class,
        isIntegerResponse = true
    )
    int setDetails(User loggedInUser, Integer sid, Map<String, String> data);

    /**
     * Sets the power management settings of the given system.
     *
     * @param loggedInUser the current user
     * @param name the system name
     * @param data the power management settings
     * @return 1 on success
     */
    @ApiEndpointDoc(
        summary = "Get current power management settings of the given system",
        requestClass = SetDetailsByNameRequest.class,
        isIntegerResponse = true
    )
    int setDetails(User loggedInUser, String name, Map<String, String> data);

    /**
     * Executes the power management action 'powerOn'.
     *
     * @param loggedInUser the current user
     * @param sid the system id
     * @return 1 on success
     */
    @ApiEndpointDoc(
        summary = "Execute power management action 'powerOn'",
        requestClass = SidRequest.class,
        isIntegerResponse = true
    )
    int powerOn(User loggedInUser, Integer sid);

    /**
     * Executes the power management action 'powerOn'.
     *
     * @param loggedInUser the current user
     * @param name the system name
     * @return 1 on success
     */
    @ApiEndpointDoc(
        summary = "Execute power management action 'powerOn'",
        requestClass = NameRequest.class,
        isIntegerResponse = true
    )
    int powerOn(User loggedInUser, String name);

    /**
     * Executes the power management action 'powerOff'.
     *
     * @param loggedInUser the current user
     * @param sid the system id
     * @return 1 on success
     */
    @ApiEndpointDoc(
        summary = "Execute power management action 'powerOff'",
        requestClass = SidRequest.class,
        isIntegerResponse = true
    )
    int powerOff(User loggedInUser, Integer sid);

    /**
     * Executes the power management action 'powerOff'.
     *
     * @param loggedInUser the current user
     * @param name the system name
     * @return 1 on success
     */
    @ApiEndpointDoc(
        summary = "Execute power management action 'powerOff'",
        requestClass = NameRequest.class,
        isIntegerResponse = true
    )
    int powerOff(User loggedInUser, String name);

    /**
     * Executes the power management action 'Reboot'.
     *
     * @param loggedInUser the current user
     * @param sid the system id
     * @return 1 on success
     */
    @ApiEndpointDoc(
        summary = "Execute power management action 'Reboot'",
        requestClass = SidRequest.class,
        isIntegerResponse = true
    )
    int reboot(User loggedInUser, Integer sid);

    /**
     * Executes the power management action 'Reboot'.
     *
     * @param loggedInUser the current user
     * @param name the system name
     * @return 1 on success
     */
    @ApiEndpointDoc(
        summary = "Execute power management action 'Reboot'",
        requestClass = NameRequest.class,
        isIntegerResponse = true
    )
    int reboot(User loggedInUser, String name);

    /**
     * Returns the power status of the given system.
     *
     * @param loggedInUser the current user
     * @param sid the system id
     * @return true when the power is on
     */
    @ApiEndpointDoc(
        summary = "Execute powermanagement actions",
        method = HttpMethod.get,
        responseClass = BooleanResponse.class,
        responseDescription = "True when power is on, otherwise False",
        legacyDocResponse = @LegacyDocResponse(type = "boolean", name = "status")
    )
    boolean getStatus(
        @Parameter(hidden = true) User loggedInUser,
        @Parameter(name = "sid", in = ParameterIn.QUERY, required = true) Integer sid);

    /**
     * Returns the power status of the given system.
     *
     * @param loggedInUser the current user
     * @param name the system name
     * @return true when the power is on
     */
    @ApiEndpointDoc(
        summary = "Execute powermanagement actions",
        method = HttpMethod.get,
        responseClass = BooleanResponse.class,
        responseDescription = "True when power is on, otherwise False",
        legacyDocResponse = @LegacyDocResponse(type = "boolean", name = "status")
    )
    boolean getStatus(
        @Parameter(hidden = true) User loggedInUser,
        @Parameter(name = "name", in = ParameterIn.QUERY, required = true) String name);

    @Schema(name = "ApiResponseBoolean")
    interface BooleanResponse extends ApiResponseWrapper<Boolean> { }

    @Schema(name = "ApiResponsePowerTypeList")
    interface PowerTypeListResponse extends ApiResponseWrapper<List<String>> { }

    @Schema(name = "ApiResponsePowerManagementParameters")
    interface PowerManagementParametersResponse extends ApiResponseWrapper<PowerManagementParametersDoc> { }

    @Schema(name = "PowerManagementSidRequest")
    interface SidRequest {

        /**
         * @return the system id
         */
        @Schema(requiredMode = Schema.RequiredMode.REQUIRED)
        Integer getSid();
    }

    @Schema(name = "PowerManagementNameRequest")
    interface NameRequest {

        /**
         * @return the system name
         */
        @Schema(requiredMode = Schema.RequiredMode.REQUIRED)
        String getName();
    }

    @Schema(name = "PowerManagementSetDetailsRequest")
    @JsonPropertyOrder({"sid", "data"})
    interface SetDetailsBySidRequest {

        /**
         * @return the system id
         */
        @Schema(requiredMode = Schema.RequiredMode.REQUIRED)
        Integer getSid();

        /**
         * @return the power management settings
         */
        @Schema(requiredMode = Schema.RequiredMode.REQUIRED)
        PowerManagementSettingsDoc getData();
    }

    @Schema(name = "PowerManagementSetDetailsByNameRequest")
    @JsonPropertyOrder({"name", "data"})
    interface SetDetailsByNameRequest {

        /**
         * @return the system name
         */
        @Schema(requiredMode = Schema.RequiredMode.REQUIRED)
        String getName();

        /**
         * @return the power management settings
         */
        @Schema(requiredMode = Schema.RequiredMode.REQUIRED)
        PowerManagementSettingsDoc getData();
    }

    @Schema(name = "PowerManagementSettings")
    @JsonPropertyOrder({"powerType", "powerAddress", "powerUsername", "powerPassword", "powerId"})
    interface PowerManagementSettingsDoc {

        /**
         * @return the power management type
         */
        @Schema(description = "Power management type", requiredMode = Schema.RequiredMode.REQUIRED)
        String getPowerType();

        /**
         * @return the IP address for power management
         */
        @Schema(description = "IP address for power management", requiredMode = Schema.RequiredMode.REQUIRED)
        String getPowerAddress();

        /**
         * @return the username
         */
        @Schema(description = "The Username", requiredMode = Schema.RequiredMode.REQUIRED)
        String getPowerUsername();

        /**
         * @return the password
         */
        @Schema(description = "The Password", requiredMode = Schema.RequiredMode.REQUIRED)
        String getPowerPassword();

        /**
         * @return the identifier
         */
        @Schema(description = "Identifier", requiredMode = Schema.RequiredMode.REQUIRED)
        String getPowerId();
    }

    @Schema(name = "PowerManagementParameters", description = "powerManagementParameters")
    @JsonPropertyOrder({"powerType", "powerAddress", "powerUsername", "powerPassword", "powerId"})
    interface PowerManagementParametersDoc {

        /**
         * @return the power management type
         */
        @Schema(description = "Power management type", requiredMode = Schema.RequiredMode.REQUIRED)
        String getPowerType();

        /**
         * @return the IP address for power management
         */
        @Schema(description = "IP address for power management", requiredMode = Schema.RequiredMode.REQUIRED)
        String getPowerAddress();

        /**
         * @return the username
         */
        @Schema(description = "The Username", requiredMode = Schema.RequiredMode.REQUIRED)
        String getPowerUsername();

        /**
         * @return the password
         */
        @Schema(description = "The Password", requiredMode = Schema.RequiredMode.REQUIRED)
        String getPowerPassword();

        /**
         * @return the identifier
         */
        @Schema(description = "Identifier", requiredMode = Schema.RequiredMode.REQUIRED)
        String getPowerId();
    }
}
