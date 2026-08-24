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
package com.redhat.rhn.frontend.xmlrpc.system.scap;

import com.redhat.rhn.domain.audit.ScapContent;
import com.redhat.rhn.domain.audit.ScapPolicy;
import com.redhat.rhn.domain.audit.TailoringFile;
import com.redhat.rhn.domain.audit.XccdfTestResult;
import com.redhat.rhn.domain.user.User;
import com.redhat.rhn.frontend.dto.XccdfRuleResultDto;
import com.redhat.rhn.frontend.dto.XccdfTestResultDto;
import com.redhat.rhn.frontend.xmlrpc.TaskomaticApiException;

import com.suse.manager.api.ApiResponseWrapper;
import com.suse.manager.api.docs.ApiEndpointDoc;
import com.suse.manager.api.docs.LegacyDocResponse;

import com.fasterxml.jackson.annotation.JsonPropertyOrder;

import java.util.Date;
import java.util.List;
import java.util.Map;

import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.enums.ParameterIn;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.tags.Tag;
import spark.route.HttpMethod;

/**
 * API contract for {@link SystemScapHandler}.
 */
@Tag(name = "system.scap",
    description = "Provides methods to schedule SCAP scans and access the results.")
public interface SystemScapHandlerApi {

    /**
     * Lists the finished OpenSCAP scans of a system.
     *
     * @param loggedInUser the current user
     * @param sid the system id
     * @return the finished scans of the system
     */
    @ApiEndpointDoc(
        summary = "Return a list of finished OpenSCAP scans for a given system.",
        method = HttpMethod.get,
        responseClass = XccdfScanListResponse.class,
        legacyDocResponse = @LegacyDocResponse(name = "OpenSCAP XCCDF Scan")
    )
    List<XccdfTestResultDto> listXccdfScans(
        @Parameter(hidden = true) User loggedInUser,
        @Parameter(name = "sid", in = ParameterIn.QUERY, required = true) Integer sid);

    /**
     * Returns the details of an OpenSCAP scan.
     *
     * @param loggedInUser the current user
     * @param xid the id of the XCCDF scan
     * @return the details of the scan
     */
    @ApiEndpointDoc(
        summary = "Get details of given OpenSCAP XCCDF scan.",
        method = HttpMethod.get,
        responseClass = XccdfScanDetailsResponse.class,
        legacyDocResponse = @LegacyDocResponse(name = "OpenSCAP XCCDF Scan")
    )
    XccdfTestResult getXccdfScanDetails(
        @Parameter(hidden = true) User loggedInUser,
        @Parameter(name = "xid", in = ParameterIn.QUERY, required = true,
                description = "ID of XCCDF scan.") Integer xid);

    /**
     * Lists the rule results of an OpenSCAP scan.
     *
     * @param loggedInUser the current user
     * @param xid the id of the XCCDF scan
     * @return the rule results of the scan
     */
    @ApiEndpointDoc(
        summary = "Return a full list of RuleResults for given OpenSCAP XCCDF scan.",
        method = HttpMethod.get,
        responseClass = XccdfRuleResultListResponse.class,
        legacyDocResponse = @LegacyDocResponse(name = "OpenSCAP XCCDF RuleResult")
    )
    List<XccdfRuleResultDto> getXccdfScanRuleResults(
        @Parameter(hidden = true) User loggedInUser,
        @Parameter(name = "xid", in = ParameterIn.QUERY, required = true,
                description = "ID of XCCDF scan.") Integer xid);

    /**
     * Deletes an OpenSCAP scan.
     *
     * @param loggedInUser the current user
     * @param xid the id of the XCCDF scan
     * @return whether the scan was deleted
     */
    @ApiEndpointDoc(
        summary = "Delete OpenSCAP XCCDF Scan from the #product() database. Note that " +
            "only those SCAP Scans can be deleted which have passed their retention period.",
        requestClass = XidRequest.class,
        responseClass = BooleanResponse.class,
        responseDescription = "indicates success of the operation",
        legacyDocResponse = @LegacyDocResponse(name = "status")
    )
    Boolean deleteXccdfScan(User loggedInUser, Integer xid);

    /**
     * Schedules an OpenSCAP scan on a list of systems.
     *
     * @param loggedInUser the current user
     * @param sids the system ids
     * @param xccdfPath the path to the xccdf document
     * @param oscapParams the additional parameters for the oscap tool
     * @return the id of the SCAP action created
     */
    @ApiEndpointDoc(
        summary = "Schedule OpenSCAP scan.",
        requestClass = ScheduleScanBySidsRequest.class,
        isIntegerResponse = true,
        responseDescription = "ID if SCAP action created",
        legacyDocResponse = @LegacyDocResponse(name = "id")
    )
    int scheduleXccdfScan(User loggedInUser, List sids, String xccdfPath, String oscapParams);

    /**
     * Schedules an OpenSCAP scan on a list of systems at a given time.
     *
     * @param loggedInUser the current user
     * @param sids the system ids
     * @param xccdfPath the path to the xccdf document
     * @param oscapParams the additional parameters for the oscap tool
     * @param date the date to schedule the action
     * @return the id of the SCAP action created
     */
    @ApiEndpointDoc(
        summary = "Schedule OpenSCAP scan.",
        requestClass = ScheduleScanBySidsAtDateRequest.class,
        isIntegerResponse = true,
        responseDescription = "ID if SCAP action created",
        legacyDocResponse = @LegacyDocResponse(name = "id")
    )
    int scheduleXccdfScan(User loggedInUser, List sids, String xccdfPath, String oscapParams, Date date);

    /**
     * Schedules an OpenSCAP scan on a list of systems, with OVAL files, at a given time.
     *
     * @param loggedInUser the current user
     * @param sids the system ids
     * @param xccdfPath the path to the xccdf document
     * @param oscapParams the additional parameters for the oscap tool
     * @param ovalFiles the additional OVAL files for the oscap tool
     * @param date the date to schedule the action
     * @return the id of the SCAP action created
     */
    @ApiEndpointDoc(
        summary = "Schedule OpenSCAP scan.",
        requestClass = ScheduleScanBySidsWithOvalRequest.class,
        isIntegerResponse = true,
        responseDescription = "ID if SCAP action created",
        legacyDocResponse = @LegacyDocResponse(name = "id")
    )
    int scheduleXccdfScan(User loggedInUser, List sids, String xccdfPath, String oscapParams,
            String ovalFiles, Date date);

    /**
     * Schedules an OpenSCAP scan on a single system.
     *
     * @param loggedInUser the current user
     * @param sid the system id
     * @param xccdfPath the path to the xccdf document
     * @param oscapParams the additional parameters for the oscap tool
     * @return the id of the SCAP action created
     */
    @ApiEndpointDoc(
        summary = "Schedule Scap XCCDF scan.",
        requestClass = ScheduleScanBySidRequest.class,
        isIntegerResponse = true,
        responseDescription = "ID of the scap action created",
        legacyDocResponse = @LegacyDocResponse(name = "id")
    )
    int scheduleXccdfScan(User loggedInUser, Integer sid, String xccdfPath, String oscapParams);

    /**
     * Schedules an OpenSCAP scan on a single system at a given time.
     *
     * @param loggedInUser the current user
     * @param sid the system id
     * @param xccdfPath the path to the xccdf document
     * @param oscapParams the additional parameters for the oscap tool
     * @param date the date to schedule the action
     * @return the id of the SCAP action created
     */
    @ApiEndpointDoc(
        summary = "Schedule Scap XCCDF scan.",
        requestClass = ScheduleScanBySidAtDateRequest.class,
        isIntegerResponse = true,
        responseDescription = "ID of the scap action created",
        legacyDocResponse = @LegacyDocResponse(name = "id")
    )
    int scheduleXccdfScan(User loggedInUser, Integer sid, String xccdfPath, String oscapParams, Date date);

    /**
     * Lists the available SCAP content.
     *
     * @param loggedInUser the current user
     * @return the available SCAP content
     */
    @ApiEndpointDoc(
        summary = "List all available SCAP content.",
        method = HttpMethod.get,
        responseClass = ScapContentListResponse.class,
        legacyDocResponse = @LegacyDocResponse(name = "SCAP content information")
    )
    List<ScapContent> listScapContent(User loggedInUser);

    /**
     * Lists the tailoring files of the user's organization.
     *
     * @param loggedInUser the current user
     * @return the tailoring files
     */
    @ApiEndpointDoc(
        summary = "List tailoring files available for the user's organization.",
        method = HttpMethod.get,
        responseClass = TailoringFileListResponse.class,
        legacyDocResponse = @LegacyDocResponse(name = "Tailoring file information")
    )
    List<TailoringFile> listTailoringFiles(User loggedInUser);

    /**
     * Lists the SCAP policies of the user's organization.
     *
     * @param loggedInUser the current user
     * @return the SCAP policies
     */
    @ApiEndpointDoc(
        summary = "List SCAP policies available for the user's organization.",
        method = HttpMethod.get,
        responseClass = ScapPolicyListResponse.class,
        legacyDocResponse = @LegacyDocResponse(name = "SCAP policy information")
    )
    List<ScapPolicy> listPolicies(User loggedInUser);

    /**
     * Schedules a SCAP scan based on an existing SCAP policy.
     *
     * @param loggedInUser the current user
     * @param sids the system ids
     * @param policyId the SCAP policy id
     * @param date the date to schedule the action
     * @return the id of the SCAP action created
     * @throws TaskomaticApiException if there was a Taskomatic error
     */
    @ApiEndpointDoc(
        summary = "Schedule SCAP scan based on an existing SCAP policy.",
        requestClass = ScheduleScanWithPolicyRequest.class,
        isIntegerResponse = true,
        responseDescription = "ID of SCAP action created",
        legacyDocResponse = @LegacyDocResponse(name = "id")
    )
    Long scheduleBetaXccdfScanWithPolicy(User loggedInUser, List<Integer> sids, Integer policyId, Date date)
            throws TaskomaticApiException;

    /**
     * Schedules a SCAP scan with custom parameters.
     *
     * @param loggedInUser the current user
     * @param sids the system ids
     * @param params the SCAP parameters
     * @param date the date to schedule the action
     * @return the id of the SCAP action created
     * @throws TaskomaticApiException if there was a Taskomatic error
     */
    @ApiEndpointDoc(
        summary = "Schedule SCAP scan with custom parameters.",
        requestClass = ScheduleScanCustomRequest.class,
        isIntegerResponse = true,
        responseDescription = "ID of SCAP action created",
        legacyDocResponse = @LegacyDocResponse(name = "id")
    )
    Long scheduleBetaXccdfScanCustom(User loggedInUser, List<Integer> sids, Map<String, Object> params, Date date)
            throws TaskomaticApiException;

    @Schema(name = "ApiResponseXccdfScanList")
    interface XccdfScanListResponse extends ApiResponseWrapper<List<XccdfTestResultDtoDoc>> { }

    @Schema(name = "ApiResponseXccdfScanDetails")
    interface XccdfScanDetailsResponse extends ApiResponseWrapper<XccdfTestResultDoc> { }

    @Schema(name = "ApiResponseXccdfRuleResultList")
    interface XccdfRuleResultListResponse extends ApiResponseWrapper<List<XccdfRuleResultDoc>> { }

    @Schema(name = "ApiResponseScapContentList")
    interface ScapContentListResponse extends ApiResponseWrapper<List<ScapContentDoc>> { }

    @Schema(name = "ApiResponseTailoringFileList")
    interface TailoringFileListResponse extends ApiResponseWrapper<List<TailoringFileDoc>> { }

    @Schema(name = "ApiResponseScapPolicyList")
    interface ScapPolicyListResponse extends ApiResponseWrapper<List<ScapPolicyDoc>> { }

    @Schema(name = "ApiResponseBoolean")
    interface BooleanResponse extends ApiResponseWrapper<Boolean> { }

    @Schema(name = "ScapScanIdRequest")
    interface XidRequest {

        /**
         * @return the id of the XCCDF scan
         */
        @Schema(description = "ID of XCCDF scan.", requiredMode = Schema.RequiredMode.REQUIRED)
        Integer getXid();
    }

    @Schema(name = "ScapScheduleScanBySidsRequest")
    @JsonPropertyOrder({"sids", "xccdfPath", "oscapParams"})
    interface ScheduleScanBySidsRequest {

        /**
         * @return the system ids
         */
        @Schema(requiredMode = Schema.RequiredMode.REQUIRED)
        List<Integer> getSids();

        /**
         * @return the path to the xccdf document
         */
        @Schema(description = "path to xccdf content on targeted systems.",
                requiredMode = Schema.RequiredMode.REQUIRED)
        String getXccdfPath();

        /**
         * @return the additional parameters for the oscap tool
         */
        @Schema(description = "additional parameters for oscap tool.",
                requiredMode = Schema.RequiredMode.REQUIRED)
        String getOscapParams();
    }

    @Schema(name = "ScapScheduleScanBySidsAtDateRequest")
    @JsonPropertyOrder({"sids", "xccdfPath", "oscapParams", "date"})
    interface ScheduleScanBySidsAtDateRequest {

        /**
         * @return the system ids
         */
        @Schema(requiredMode = Schema.RequiredMode.REQUIRED)
        List<Integer> getSids();

        /**
         * @return the path to the xccdf document
         */
        @Schema(description = "path to xccdf content on targeted systems.",
                requiredMode = Schema.RequiredMode.REQUIRED)
        String getXccdfPath();

        /**
         * @return the additional parameters for the oscap tool
         */
        @Schema(description = "additional parameters for oscap tool.",
                requiredMode = Schema.RequiredMode.REQUIRED)
        String getOscapParams();

        /**
         * @return the date to schedule the action
         */
        @Schema(description = "The date to schedule the action",
                requiredMode = Schema.RequiredMode.REQUIRED)
        @LegacyDocResponse(type = "dateTime.iso8601")
        Date getDate();
    }

    @Schema(name = "ScapScheduleScanBySidsWithOvalRequest")
    @JsonPropertyOrder({"sids", "xccdfPath", "oscapParams", "ovalFiles", "date"})
    interface ScheduleScanBySidsWithOvalRequest {

        /**
         * @return the system ids
         */
        @Schema(requiredMode = Schema.RequiredMode.REQUIRED)
        List<Integer> getSids();

        /**
         * @return the path to the xccdf document
         */
        @Schema(description = "Path to xccdf content on targeted systems.",
                requiredMode = Schema.RequiredMode.REQUIRED)
        String getXccdfPath();

        /**
         * @return the additional parameters for the oscap tool
         */
        @Schema(description = "Additional parameters for oscap tool.",
                requiredMode = Schema.RequiredMode.REQUIRED)
        String getOscapParams();

        /**
         * @return the additional OVAL files for the oscap tool
         */
        @Schema(description = "Additional OVAL files for oscap tool.",
                requiredMode = Schema.RequiredMode.REQUIRED)
        String getOvalFiles();

        /**
         * @return the date to schedule the action
         */
        @Schema(description = "The date to schedule the action",
                requiredMode = Schema.RequiredMode.REQUIRED)
        @LegacyDocResponse(type = "dateTime.iso8601")
        Date getDate();
    }

    @Schema(name = "ScapScheduleScanBySidRequest")
    @JsonPropertyOrder({"sid", "xccdfPath", "oscapParams"})
    interface ScheduleScanBySidRequest {

        /**
         * @return the system id
         */
        @Schema(requiredMode = Schema.RequiredMode.REQUIRED)
        Integer getSid();

        /**
         * @return the path to the xccdf document
         */
        @Schema(description = "Path to xccdf content on targeted systems.",
                requiredMode = Schema.RequiredMode.REQUIRED)
        String getXccdfPath();

        /**
         * @return the additional parameters for the oscap tool
         */
        @Schema(description = "Additional parameters for oscap tool.",
                requiredMode = Schema.RequiredMode.REQUIRED)
        String getOscapParams();
    }

    @Schema(name = "ScapScheduleScanBySidAtDateRequest")
    @JsonPropertyOrder({"sid", "xccdfPath", "oscapParams", "date"})
    interface ScheduleScanBySidAtDateRequest {

        /**
         * @return the system id
         */
        @Schema(requiredMode = Schema.RequiredMode.REQUIRED)
        Integer getSid();

        /**
         * @return the path to the xccdf document
         */
        @Schema(description = "Path to xccdf content on targeted systems.",
                requiredMode = Schema.RequiredMode.REQUIRED)
        String getXccdfPath();

        /**
         * @return the additional parameters for the oscap tool
         */
        @Schema(description = "Additional parameters for oscap tool.",
                requiredMode = Schema.RequiredMode.REQUIRED)
        String getOscapParams();

        /**
         * @return the date to schedule the action
         */
        @Schema(description = "The date to schedule the action",
                requiredMode = Schema.RequiredMode.REQUIRED)
        @LegacyDocResponse(type = "dateTime.iso8601")
        Date getDate();
    }

    @Schema(name = "ScapScheduleScanWithPolicyRequest")
    @JsonPropertyOrder({"sids", "policyId", "date"})
    interface ScheduleScanWithPolicyRequest {

        /**
         * @return the system ids
         */
        @Schema(requiredMode = Schema.RequiredMode.REQUIRED)
        List<Integer> getSids();

        /**
         * @return the SCAP policy id
         */
        @Schema(description = "SCAP policy ID", requiredMode = Schema.RequiredMode.REQUIRED)
        Integer getPolicyId();

        /**
         * @return the date to schedule the action
         */
        @Schema(description = "The date to schedule the action",
                requiredMode = Schema.RequiredMode.REQUIRED)
        @LegacyDocResponse(type = "dateTime.iso8601")
        Date getDate();
    }

    @Schema(name = "ScapScheduleScanCustomRequest")
    @JsonPropertyOrder({"sids", "params", "date"})
    interface ScheduleScanCustomRequest {

        /**
         * @return the system ids
         */
        @Schema(requiredMode = Schema.RequiredMode.REQUIRED)
        List<Integer> getSids();

        /**
         * @return the SCAP parameters
         */
        @Schema(requiredMode = Schema.RequiredMode.REQUIRED)
        ScapScanParamsDoc getParams();

        /**
         * @return the date to schedule the action
         */
        @Schema(description = "The date to schedule the action",
                requiredMode = Schema.RequiredMode.REQUIRED)
        @LegacyDocResponse(type = "dateTime.iso8601")
        Date getDate();
    }

    @Schema(name = "ScapScanCustomParams")
    @JsonPropertyOrder({"scapContentId", "xccdfProfileId", "tailoringFileId", "tailoringProfileId", "ovalFiles",
        "advancedArgs", "fetchRemoteResources"})
    interface ScapScanParamsDoc {

        /**
         * @return the SCAP content id
         */
        @Schema(description = "SCAP content ID (required)", requiredMode = Schema.RequiredMode.REQUIRED)
        Integer getScapContentId();

        /**
         * @return the XCCDF profile id
         */
        @Schema(description = "XCCDF profile ID (required)", requiredMode = Schema.RequiredMode.REQUIRED)
        String getXccdfProfileId();

        /**
         * @return the tailoring file id
         */
        @Schema(description = "Tailoring file ID (optional)")
        Integer getTailoringFileId();

        /**
         * @return the tailoring profile id
         */
        @Schema(description = "Tailoring profile ID (optional)")
        String getTailoringProfileId();

        /**
         * @return the OVAL files
         */
        @Schema(description = "OVAL files (optional)")
        String getOvalFiles();

        /**
         * @return the advanced arguments
         */
        @Schema(description = "Advanced arguments (optional)")
        String getAdvancedArgs();

        /**
         * @return whether remote resources are fetched
         */
        @Schema(description = "Fetch remote resources (optional, default false)")
        Boolean getFetchRemoteResources();
    }

    @Schema(name = "OpenScapXccdfScanSummary", description = "OpenSCAP XCCDF Scan")
    @JsonPropertyOrder({"xid", "profile", "path", "ovalfiles", "completed"})
    interface XccdfTestResultDtoDoc {

        /**
         * @return the XCCDF TestResult id
         */
        @Schema(description = "XCCDF TestResult ID", requiredMode = Schema.RequiredMode.REQUIRED)
        Integer getXid();

        /**
         * @return the XCCDF profile
         */
        @Schema(description = "XCCDF Profile", requiredMode = Schema.RequiredMode.REQUIRED)
        String getProfile();

        /**
         * @return the path to the XCCDF document
         */
        @Schema(description = "path to XCCDF document", requiredMode = Schema.RequiredMode.REQUIRED)
        String getPath();

        /**
         * @return the optional OVAL files
         */
        @Schema(description = "optional OVAL files", requiredMode = Schema.RequiredMode.REQUIRED)
        String getOvalfiles();

        /**
         * @return the scan completion time
         */
        @Schema(description = "scan completion time", requiredMode = Schema.RequiredMode.REQUIRED)
        @LegacyDocResponse(type = "dateTime.iso8601")
        Date getCompleted();
    }

    @Schema(name = "OpenScapXccdfScanDetails", description = "OpenSCAP XCCDF Scan")
    @JsonPropertyOrder({"xid", "sid", "actionId", "path", "ovalfiles", "oscapParameters", "testResult", "benchmark",
        "benchmarkVersion", "profile", "profileTitle", "startTime", "endTime", "errors", "deletable"})
    interface XccdfTestResultDoc {

        /**
         * @return the XCCDF TestResult id
         */
        @Schema(description = "XCCDF TestResult ID", requiredMode = Schema.RequiredMode.REQUIRED)
        Integer getXid();

        /**
         * @return the server id
         */
        @Schema(description = "serverId", requiredMode = Schema.RequiredMode.REQUIRED)
        Integer getSid();

        /**
         * @return the id of the parent action
         */
        @Schema(name = "action_id", description = "ID of the parent action",
                requiredMode = Schema.RequiredMode.REQUIRED)
        Integer getActionId();

        /**
         * @return the path to the XCCDF document
         */
        @Schema(description = "path to XCCDF document", requiredMode = Schema.RequiredMode.REQUIRED)
        String getPath();

        /**
         * @return the optional OVAL files
         */
        @Schema(description = "optional OVAL files", requiredMode = Schema.RequiredMode.REQUIRED)
        String getOvalfiles();

        /**
         * @return the oscap command-line arguments
         */
        @Schema(name = "oscap_parameters", description = "oscap command-line arguments",
                requiredMode = Schema.RequiredMode.REQUIRED)
        String getOscapParameters();

        /**
         * @return the identifier of the XCCDF TestResult
         */
        @Schema(name = "test_result", description = "identifier of XCCDF TestResult",
                requiredMode = Schema.RequiredMode.REQUIRED)
        String getTestResult();

        /**
         * @return the identifier of the XCCDF Benchmark
         */
        @Schema(description = "identifier of XCCDF Benchmark", requiredMode = Schema.RequiredMode.REQUIRED)
        String getBenchmark();

        /**
         * @return the version of the Benchmark
         */
        @Schema(name = "benchmark_version", description = "version of the Benchmark",
                requiredMode = Schema.RequiredMode.REQUIRED)
        String getBenchmarkVersion();

        /**
         * @return the identifier of the XCCDF Profile
         */
        @Schema(description = "identifier of XCCDF Profile", requiredMode = Schema.RequiredMode.REQUIRED)
        String getProfile();

        /**
         * @return the title of the XCCDF Profile
         */
        @Schema(name = "profile_title", description = "title of XCCDF Profile",
                requiredMode = Schema.RequiredMode.REQUIRED)
        String getProfileTitle();

        /**
         * @return the client machine time of the scan start
         */
        @Schema(name = "start_time", description = "client machine time of scan start",
                requiredMode = Schema.RequiredMode.REQUIRED)
        @LegacyDocResponse(type = "dateTime.iso8601")
        Date getStartTime();

        /**
         * @return the client machine time of the scan completion
         */
        @Schema(name = "end_time", description = "client machine time of scan completion",
                requiredMode = Schema.RequiredMode.REQUIRED)
        @LegacyDocResponse(type = "dateTime.iso8601")
        Date getEndTime();

        /**
         * @return the stderr output of the scan
         */
        @Schema(description = "stderr output of scan", requiredMode = Schema.RequiredMode.REQUIRED)
        String getErrors();

        /**
         * @return whether the scan can be deleted
         */
        @Schema(description = "indicates whether the scan can be deleted",
                requiredMode = Schema.RequiredMode.REQUIRED)
        Boolean getDeletable();
    }

    @Schema(name = "OpenScapXccdfRuleResult", description = "OpenSCAP XCCDF RuleResult")
    @JsonPropertyOrder({"idref", "result", "idents"})
    interface XccdfRuleResultDoc {

        /**
         * @return the idref from the XCCDF document
         */
        @Schema(description = "idref from XCCDF document", requiredMode = Schema.RequiredMode.REQUIRED)
        String getIdref();

        /**
         * @return the result of the evaluation
         */
        @Schema(description = "result of evaluation", requiredMode = Schema.RequiredMode.REQUIRED)
        String getResult();

        /**
         * @return the comma separated list of XCCDF idents
         */
        @Schema(description = "comma separated list of XCCDF idents",
                requiredMode = Schema.RequiredMode.REQUIRED)
        String getIdents();
    }

    @Schema(name = "ScapContentInfo", description = "SCAP content information")
    @JsonPropertyOrder({"id", "name", "description", "dataStreamFileName", "xccdfFileName"})
    interface ScapContentDoc {

        /**
         * @return the SCAP content id
         */
        @Schema(description = "SCAP content ID", requiredMode = Schema.RequiredMode.REQUIRED)
        Integer getId();

        /**
         * @return the SCAP content name
         */
        @Schema(description = "SCAP content name", requiredMode = Schema.RequiredMode.REQUIRED)
        String getName();

        /**
         * @return the SCAP content description
         */
        @Schema(description = "SCAP content description", requiredMode = Schema.RequiredMode.REQUIRED)
        String getDescription();

        /**
         * @return the DataStream file name
         */
        @Schema(description = "DataStream file name", requiredMode = Schema.RequiredMode.REQUIRED)
        String getDataStreamFileName();

        /**
         * @return the XCCDF file name
         */
        @Schema(description = "XCCDF file name", requiredMode = Schema.RequiredMode.REQUIRED)
        String getXccdfFileName();
    }

    @Schema(name = "ScapTailoringFileInfo", description = "Tailoring file information")
    @JsonPropertyOrder({"id", "name", "fileName", "orgId"})
    interface TailoringFileDoc {

        /**
         * @return the tailoring file id
         */
        @Schema(description = "Tailoring file ID", requiredMode = Schema.RequiredMode.REQUIRED)
        Integer getId();

        /**
         * @return the tailoring file name
         */
        @Schema(description = "Tailoring file name", requiredMode = Schema.RequiredMode.REQUIRED)
        String getName();

        /**
         * @return the file name on disk
         */
        @Schema(description = "File name on disk", requiredMode = Schema.RequiredMode.REQUIRED)
        String getFileName();

        /**
         * @return the organization id
         */
        @Schema(description = "Organization ID", requiredMode = Schema.RequiredMode.REQUIRED)
        Integer getOrgId();
    }

    @Schema(name = "ScapPolicyInfo", description = "SCAP policy information")
    @JsonPropertyOrder({"id", "policyName", "description", "scapContentId", "xccdfProfileId", "tailoringFileId",
        "tailoringProfileId", "ovalFiles", "advancedArgs", "fetchRemoteResources"})
    interface ScapPolicyDoc {

        /**
         * @return the policy id
         */
        @Schema(description = "Policy ID", requiredMode = Schema.RequiredMode.REQUIRED)
        Integer getId();

        /**
         * @return the policy name
         */
        @Schema(description = "Policy name", requiredMode = Schema.RequiredMode.REQUIRED)
        String getPolicyName();

        /**
         * @return the policy description
         */
        @Schema(description = "Policy description", requiredMode = Schema.RequiredMode.REQUIRED)
        String getDescription();

        /**
         * @return the SCAP content id
         */
        @Schema(description = "SCAP content ID", requiredMode = Schema.RequiredMode.REQUIRED)
        Integer getScapContentId();

        /**
         * @return the XCCDF profile id
         */
        @Schema(description = "XCCDF profile ID", requiredMode = Schema.RequiredMode.REQUIRED)
        String getXccdfProfileId();

        /**
         * @return the tailoring file id
         */
        @Schema(description = "Tailoring file ID (optional)", requiredMode = Schema.RequiredMode.REQUIRED)
        Integer getTailoringFileId();

        /**
         * @return the tailoring profile id
         */
        @Schema(description = "Tailoring profile ID (optional)", requiredMode = Schema.RequiredMode.REQUIRED)
        String getTailoringProfileId();

        /**
         * @return the OVAL files
         */
        @Schema(description = "OVAL files", requiredMode = Schema.RequiredMode.REQUIRED)
        String getOvalFiles();

        /**
         * @return the advanced arguments
         */
        @Schema(description = "Advanced arguments", requiredMode = Schema.RequiredMode.REQUIRED)
        String getAdvancedArgs();

        /**
         * @return whether remote resources are fetched
         */
        @Schema(description = "Fetch remote resources flag", requiredMode = Schema.RequiredMode.REQUIRED)
        Boolean getFetchRemoteResources();
    }
}
