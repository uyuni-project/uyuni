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
package com.redhat.rhn.frontend.xmlrpc.formula;

import com.redhat.rhn.domain.dto.FormulaData;
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
 * API contract for {@link FormulaHandler}.
 */
@Tag(name = "formula", description = "Provides methods to access and modify formulas.")
public interface FormulaHandlerApi {

    /**
     * Lists the formulas currently installed.
     *
     * @param loggedInUser the current user
     * @return the installed formulas
     */
    @ApiEndpointDoc(
        summary = "Return the list of formulas currently installed.",
        method = HttpMethod.get,
        responseClass = FormulaListResponse.class,
        responseDescription = "the list of formulas"
    )
    List<String> listFormulas(@Parameter(hidden = true) User loggedInUser);

    /**
     * Lists the formulas of a server group.
     *
     * @param loggedInUser the current user
     * @param systemGroupId the system group id
     * @return the formulas of the group
     */
    @ApiEndpointDoc(
        summary = "Return the list of formulas a server group has.",
        method = HttpMethod.get,
        responseClass = FormulaListResponse.class,
        responseDescription = "the list of formulas"
    )
    List<String> getFormulasByGroupId(
        @Parameter(hidden = true) User loggedInUser,
        @Parameter(name = "systemGroupId", in = ParameterIn.QUERY, required = true) Integer systemGroupId);

    /**
     * Lists the formulas directly applied to a server.
     *
     * @param loggedInUser the current user
     * @param sid the system id
     * @return the formulas of the server
     */
    @ApiEndpointDoc(
        summary = "Return the list of formulas directly applied to a server.",
        method = HttpMethod.get,
        responseClass = FormulaListResponse.class,
        responseDescription = "the list of formulas"
    )
    List<String> getFormulasByServerId(
        @Parameter(hidden = true) User loggedInUser,
        @Parameter(name = "sid", description = "the system ID", in = ParameterIn.QUERY, required = true) Integer sid);

    /**
     * Lists the formulas a server and all its groups have.
     *
     * @param loggedInUser the current user
     * @param sid the system id
     * @return the formulas of the server and its groups
     */
    @ApiEndpointDoc(
        summary = "Return the list of formulas a server and all his groups have.",
        method = HttpMethod.get,
        responseClass = FormulaListResponse.class,
        responseDescription = "the list of formulas"
    )
    List<String> getCombinedFormulasByServerId(
        @Parameter(hidden = true) User loggedInUser,
        @Parameter(name = "sid", description = "the system ID", in = ParameterIn.QUERY, required = true) Integer sid);

    /**
     * Sets the formulas of a server group.
     *
     * @param loggedInUser the current user
     * @param systemGroupId the system group id
     * @param formulas the formulas to set
     * @return 1 on success
     */
    @ApiEndpointDoc(
        summary = "Set the formulas of a server group.",
        requestClass = SetGroupFormulasRequest.class,
        isIntegerResponse = true
    )
    int setFormulasOfGroup(User loggedInUser, Integer systemGroupId, List<String> formulas);

    /**
     * Sets the formulas of a server.
     *
     * @param loggedInUser the current user
     * @param systemId the system id
     * @param formulas the formulas to set
     * @return 1 on success
     */
    @ApiEndpointDoc(
        summary = "Set the formulas of a server.",
        requestClass = SetServerFormulasRequest.class,
        isIntegerResponse = true
    )
    int setFormulasOfServer(User loggedInUser, Integer systemId, List<String> formulas);

    /**
     * Returns the saved data of a formula for a server.
     *
     * @param loggedInUser the current user
     * @param systemId the system id
     * @param formulaName the formula name
     * @return the saved formula data
     */
    @ApiEndpointDoc(
        summary = "Get the saved data for the specific formula against specific server",
        method = HttpMethod.get,
        responseClass = FormulaDataResponse.class,
        legacyDocResponse = @LegacyDocResponse(type = "struct", name = "the saved formula data")
    )
    Map<String, Object> getSystemFormulaData(
        @Parameter(hidden = true) User loggedInUser,
        @Parameter(name = "systemId", description = "the system ID", in = ParameterIn.QUERY,
                required = true) Integer systemId,
        @Parameter(name = "formulaName", in = ParameterIn.QUERY, required = true) String formulaName);

    /**
     * Returns the saved data of a formula for the given servers.
     *
     * @param loggedInUser the current user
     * @param formulaName the formula name
     * @param sids the system ids
     * @return the saved formula data of each server
     */
    @ApiEndpointDoc(
        summary = "Return the list of formulas a server and all his groups have.",
        method = HttpMethod.get,
        responseClass = FormulaDataListResponse.class,
        legacyDocResponse = @LegacyDocResponse(name = "formula data")
    )
    List<FormulaData> getCombinedFormulaDataByServerIds(
        @Parameter(hidden = true) User loggedInUser,
        @Parameter(name = "formulaName", in = ParameterIn.QUERY, required = true) String formulaName,
        @Parameter(name = "sids", description = "the list of system IDs", in = ParameterIn.QUERY,
                required = true) List<Integer> sids);

    /**
     * Returns the saved data of a formula for a server group.
     *
     * @param loggedInUser the current user
     * @param groupId the system group id
     * @param formulaName the formula name
     * @return the saved formula data
     */
    @ApiEndpointDoc(
        summary = "Get the saved data for the specific formula against specific group",
        method = HttpMethod.get,
        responseClass = FormulaDataResponse.class,
        legacyDocResponse = @LegacyDocResponse(type = "struct", name = "the saved formula data")
    )
    Map<String, Object> getGroupFormulaData(
        @Parameter(hidden = true) User loggedInUser,
        @Parameter(name = "groupId", in = ParameterIn.QUERY, required = true) Integer groupId,
        @Parameter(name = "formulaName", in = ParameterIn.QUERY, required = true) String formulaName);

    /**
     * Sets the formula form of a server.
     *
     * @param loggedInUser the current user
     * @param systemId the system id
     * @param formulaName the formula name
     * @param content the values for each field in the form
     * @return 1 on success
     */
    @ApiEndpointDoc(
        summary = "Set the formula form for the specified server.",
        requestClass = SetSystemFormulaDataRequest.class,
        isIntegerResponse = true
    )
    int setSystemFormulaData(User loggedInUser, Integer systemId, String formulaName, Map<String, Object> content);

    /**
     * Sets the formula form of a server group.
     *
     * @param loggedInUser the current user
     * @param groupId the system group id
     * @param formulaName the formula name
     * @param content the values for each field in the form
     * @return 1 on success
     */
    @ApiEndpointDoc(
        summary = "Set the formula form for the specified group.",
        requestClass = SetGroupFormulaDataRequest.class,
        isIntegerResponse = true
    )
    int setGroupFormulaData(User loggedInUser, Integer groupId, String formulaName, Map<String, Object> content);

    @Schema(name = "SetGroupFormulasRequest")
    @JsonPropertyOrder({"systemGroupId", "formulas"})
    interface SetGroupFormulasRequest {

        /**
         * @return the system group id
         */
        @Schema(requiredMode = Schema.RequiredMode.REQUIRED)
        Integer getSystemGroupId();

        /**
         * @return the formulas to set
         */
        @Schema(requiredMode = Schema.RequiredMode.REQUIRED)
        List<String> getFormulas();
    }

    @Schema(name = "SetServerFormulasRequest")
    @JsonPropertyOrder({"systemId", "formulas"})
    interface SetServerFormulasRequest {

        /**
         * @return the system id
         */
        @Schema(description = "the system ID", requiredMode = Schema.RequiredMode.REQUIRED)
        Integer getSystemId();

        /**
         * @return the formulas to set
         */
        @Schema(requiredMode = Schema.RequiredMode.REQUIRED)
        List<String> getFormulas();
    }

    @Schema(name = "SetSystemFormulaDataRequest")
    @JsonPropertyOrder({"systemId", "formulaName", "content"})
    interface SetSystemFormulaDataRequest {

        /**
         * @return the system id
         */
        @Schema(requiredMode = Schema.RequiredMode.REQUIRED)
        Integer getSystemId();

        /**
         * @return the formula name
         */
        @Schema(requiredMode = Schema.RequiredMode.REQUIRED)
        String getFormulaName();

        /**
         * @return the values for each field in the form
         */
        @Schema(description = "struct content with the values for each field in the form",
                requiredMode = Schema.RequiredMode.REQUIRED,
                additionalProperties = Schema.AdditionalPropertiesValue.TRUE)
        @LegacyDocResponse(type = "struct")
        Map<String, Object> getContent();
    }

    @Schema(name = "SetGroupFormulaDataRequest")
    @JsonPropertyOrder({"groupId", "formulaName", "content"})
    interface SetGroupFormulaDataRequest {

        /**
         * @return the system group id
         */
        @Schema(requiredMode = Schema.RequiredMode.REQUIRED)
        Integer getGroupId();

        /**
         * @return the formula name
         */
        @Schema(requiredMode = Schema.RequiredMode.REQUIRED)
        String getFormulaName();

        /**
         * @return the values for each field in the form
         */
        @Schema(description = "struct containing the values for each field in the form",
                requiredMode = Schema.RequiredMode.REQUIRED,
                additionalProperties = Schema.AdditionalPropertiesValue.TRUE)
        @LegacyDocResponse(type = "struct")
        Map<String, Object> getContent();
    }

    @Schema(name = "FormulaDataEntry")
    @JsonPropertyOrder({"systemId", "minionId", "formulaValues"})
    interface FormulaDataDoc {

        /**
         * @return the system id
         */
        @Schema(name = "system_id", requiredMode = Schema.RequiredMode.REQUIRED)
        Integer getSystemId();

        /**
         * @return the minion id
         */
        @Schema(name = "minion_id", requiredMode = Schema.RequiredMode.REQUIRED)
        String getMinionId();

        /**
         * @return the saved formula values
         */
        @Schema(name = "formula_values", description = "saved formula values",
                requiredMode = Schema.RequiredMode.REQUIRED,
                additionalProperties = Schema.AdditionalPropertiesValue.TRUE)
        @LegacyDocResponse(type = "struct")
        Map<String, Object> getFormulaValues();
    }

    @Schema(name = "ApiResponseFormulaList")
    interface FormulaListResponse extends ApiResponseWrapper<List<String>> { }

    @Schema(name = "ApiResponseFormulaData")
    interface FormulaDataResponse extends ApiResponseWrapper<Map<String, Object>> {

        /**
         * The saved formula data is keyed by the fields of the formula form, which differ per
         * formula, so the payload is documented as a free form struct.
         *
         * @return the saved formula data
         */
        @Override
        @Schema(description = "The payload result", requiredMode = Schema.RequiredMode.REQUIRED,
                additionalProperties = Schema.AdditionalPropertiesValue.TRUE)
        Map<String, Object> getResult();
    }

    @Schema(name = "ApiResponseFormulaDataList")
    interface FormulaDataListResponse extends ApiResponseWrapper<List<FormulaDataDoc>> { }
}
