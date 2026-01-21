/*
 * Copyright (c) 2018--2025 SUSE LLC
 *
 * This software is licensed to you under the GNU General Public License,
 * version 2 (GPLv2). There is NO WARRANTY for this software, express or
 * implied, including the implied warranties of MERCHANTABILITY or FITNESS
 * FOR A PARTICULAR PURPOSE. You should have received a copy of GPLv2
 * along with this software; if not, see
 * http://www.gnu.org/licenses/old-licenses/gpl-2.0.txt.
 */

package com.suse.manager.webui.controllers;

import com.google.gson.Gson;
import com.google.gson.JsonObject;
import com.google.gson.JsonParseException;
import com.redhat.rhn.common.db.datasource.ModeFactory;
import com.redhat.rhn.common.db.datasource.SelectMode;
import com.redhat.rhn.domain.action.Action;
import com.redhat.rhn.domain.action.ActionFactory;
import com.redhat.rhn.domain.action.scap.ScapAction;
import com.redhat.rhn.domain.action.script.ScriptActionDetails;
import com.redhat.rhn.domain.action.script.ScriptRunAction;
import com.redhat.rhn.domain.action.salt.ApplyStatesAction;
import com.redhat.rhn.domain.audit.*;
import com.redhat.rhn.domain.role.RoleFactory;
import com.redhat.rhn.domain.server.Server;
import com.redhat.rhn.domain.user.User;

import com.redhat.rhn.frontend.dto.XccdfRuleResultDto;
import com.redhat.rhn.manager.action.ActionManager;
import com.redhat.rhn.manager.audit.ScapManager;
import com.redhat.rhn.manager.audit.scap.xml.BenchMark;
import com.redhat.rhn.manager.system.SystemManager;
import com.redhat.rhn.taskomatic.TaskomaticApi;
import com.redhat.rhn.taskomatic.TaskomaticApiException;
import com.suse.manager.webui.controllers.utils.RequestUtil;

import com.suse.manager.webui.utils.gson.AuditScanScheduleJson;
import com.suse.manager.webui.utils.gson.ResultJson;
import com.suse.manager.webui.utils.gson.ScapPolicyComplianceSummary;
import com.suse.manager.webui.utils.gson.ScapPolicyJson;
import com.suse.manager.webui.utils.gson.ScapPolicyScanHistory;

import com.suse.utils.Json;

import com.google.gson.reflect.TypeToken;

import org.apache.commons.fileupload2.core.DiskFileItem;
import org.apache.commons.fileupload2.core.FileUploadException;
import org.apache.commons.io.IOUtils;
import org.apache.commons.text.StringEscapeUtils;
import org.apache.http.HttpStatus;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import spark.ModelAndView;
import spark.Request;
import spark.Response;
import spark.Spark;
import spark.template.jade.JadeTemplateEngine;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.*;
import java.util.stream.Collectors;

import static com.suse.manager.webui.utils.SparkApplicationHelper.*;
import static spark.Spark.delete;
import static spark.Spark.get;
import static spark.Spark.post;

public class ScapAuditController {
    private static final Logger LOG = LogManager.getLogger(ScapAuditController.class);
    private static final Gson GSON = Json.GSON;
    private final TaskomaticApi taskomaticApi = new TaskomaticApi();
    private static final String TAILORING_FILES_DIR = "/srv/susemanager/scap/tailoring-files/";
    private static final String SCAP_CONTENT_DIR = "/srv/susemanager/scap/ssg/content";
    private static final String REMEDIATION_ACTION_PREFIX = "SCAP Remediation: ";


    /**
     * Invoked from Router. Initialize routes for SCAP audit Views.
     *
     * @param jade the Jade engine to use to render the pages
     */
    public void initRoutes(JadeTemplateEngine jade) {
        get("/manager/audit/scap/tailoring-files",
                withUserPreferences(withCsrfToken(withUser(this::listTailoringFilesView))), jade);
        get("/manager/audit/scap/tailoring-file/create",
                withUserPreferences(withCsrfToken(withUser(this::createTailoringFileView))), jade);
        get("/manager/audit/scap/tailoring-file/edit/:id",
                withUserPreferences(withCsrfToken(withUser(this::updateTailoringFileView))), jade);

        post("/manager/api/audit/scap/tailoring-file/create", withUser(this::createTailoringFile));
        post("/manager/api/audit/scap/tailoring-file/update", withUser(this::updateTailoringFile));
        post("/manager/api/audit/scap/tailoring-file/delete", withUser(this::deleteTailoringFile));

        // SCAP Policies routes
        get("/manager/audit/scap/policies",
                withUserPreferences(withCsrfToken(withUser(this::listScapPoliciesView))), jade);
        get("/manager/audit/scap/policy/create",
                withUserPreferences(withCsrfToken(withUser(this::createScapPolicyView))), jade);
        get("/manager/audit/scap/policy/edit/:id",
                withUserPreferences(withCsrfToken(withUser(this::editScapPolicyView))), jade);
        get("/manager/audit/scap/policy/details/:id",
                withUserPreferences(withCsrfToken(withUser(this::detailScapPolicyView))), jade);
        get("/manager/api/audit/profiles/list/:type/:name", withUser(ScapAuditController::getProfileList));
        get("/manager/api/audit/scap/policy/view/:id", asJson(withUser(this::getScapPolicyDetails)));
        get("/manager/api/audit/scap/policy/:id/scan-history", asJson(withUser(this::getPolicyScanHistory)));

        post("/manager/api/audit/scap/policy/create", withUser(this::createScapPolicy));
        post("/manager/api/audit/scap/policy/update", withUser(this::updateScapPolicy));
        post("/manager/api/audit/scap/policy/delete", withUser(this::deleteScapPolicy));

        // SCAP Content routes
        get("/manager/audit/scap/content",
                withUserPreferences(withCsrfToken(withUser(this::listScapContentView))), jade);
        get("/manager/audit/scap/content/create",
                withUserPreferences(withCsrfToken(withUser(this::createScapContentView))), jade);
        get("/manager/audit/scap/content/edit/:id",
                withUserPreferences(withCsrfToken(withUser(this::updateScapContentView))), jade);

        post("/manager/api/audit/scap/content/create", withUser(this::createScapContent));
        post("/manager/api/audit/scap/content/update", withUser(this::updateScapContent));
        post("/manager/api/audit/scap/content/delete", withUser(this::deleteScapContent));

        // Scap scans schedule

        get("/manager/systems/details/schedule-scap-scan",
          withCsrfToken(withDocsLocale(withUserAndServer(this::scheduleAuditScanView))),
          jade);
        post("manager/api/audit/schedule/create", withUser(this::scheduleAuditScan));

        //rules

        get("/manager/audit/scap/scan/rule-result-details/:sid/:rrid",
          withCsrfToken(withDocsLocale(withUserAndServer(ScapAuditController::createRuleResultView))), jade);

        // Custom remediation routes
        get("/manager/api/audit/scap/custom-remediation/:identifier/:benchmarkId",
                withUser(this::getCustomRemediation));
        post("/manager/api/audit/scap/custom-remediation",
                withUser(this::saveCustomRemediation));
        delete("/manager/api/audit/scap/custom-remediation/:identifier/:benchmarkId/:scriptType",
                withUser(this::deleteCustomRemediation));
        post("/manager/api/audit/scap/scan/rule-apply-remediation",
                withUser(this::applyRemediation));

    }
    /**
     * Processes a GET request to get a list of all Tailoring files
     *
     * @param req the request object
     * @param res the response object
     * @param user the authorized user
     * @return the result JSON object
     */
    public ModelAndView listTailoringFilesView(Request req, Response res, User user) {
        Map<String, Object> data = new HashMap<>();
        List<TailoringFile> tailoringFiles = ScapFactory.listTailoringFiles(user.getOrg());
        List<JsonObject> collect = tailoringFiles.stream().map(this::convertToTailoringFileJson).collect(Collectors.toList());

        //data.put("tailoringFiles", Json.GSON.toJson(tailoringFiles.));
        data.put("tailoringFiles", collect);
        data.put("tailoringFilesName", Json.GSON.toJson(tailoringFiles.stream()
          .map(TailoringFile::getDisplayFileName).collect(Collectors.toList())));
        return new ModelAndView(data, "templates/audit/list-tailoring-files.jade");
    }



    /**
     * Returns a view to display form to upload tailoring file
     *
     * @param req the request object
     * @param res the response object
     * @param user the authorized user
     * @return the model and view
     */
    public ModelAndView createTailoringFileView(Request req, Response res, User user) {
        Map<String, Object> data = new HashMap<>();
        return new ModelAndView(data, "templates/audit/create-tailoring-file.jade");
    }

    /**
     * Create a new Tailoring file
     *
     * @param request the request
     * @param response the response
     * @param user the user
     * @return the json response
     */
    public String createTailoringFile(Request request, Response response, User user) {
        try {
            List<DiskFileItem> items = RequestUtil.parseMultipartRequest(request);

            String nameParam = RequestUtil.findStringParam(items, "name")
                    .orElseThrow(() ->
                            new IllegalArgumentException("Name parameter missing"));
            Optional<String> descriptionParam = RequestUtil.findStringParam(items, "description");
            DiskFileItem tailoringFileParam = RequestUtil.findFileItem(items, "tailoring_file")
                    .orElseThrow(() ->
                            new IllegalArgumentException("Tailoring_file parameter missing"));

            ensureDirectoryExists(TAILORING_FILES_DIR);

            // Step 1: Create database entry with original filename (temporary)
            String originalFilename = tailoringFileParam.getName();
            TailoringFile tailoringFile = new TailoringFile(nameParam, originalFilename);
            tailoringFile.setOrg(user.getOrg());
            descriptionParam.ifPresent(tailoringFile::setDescription);
            ScapFactory.saveTailoringFile(tailoringFile);

            // Step 2: Generate unique filename using org ID, name, and original filename
            String uniqueFilename = generateUniqueFileName(
                user.getOrg().getId(),
                nameParam,
                originalFilename
            );

            // Step 3: Save file with unique filename
            saveFileToDirectory(tailoringFileParam, TAILORING_FILES_DIR, uniqueFilename);

            // Step 4: Update database with unique filename
            tailoringFile.setFileName(uniqueFilename);
            ScapFactory.saveTailoringFile(tailoringFile);

            return result(response, ResultJson.success());
        }
        catch (Exception e) {
            return handleTailoringFileException(response, e, "creating");
        }
    }
    /**
     * Returns a view to display update form of tailoring file
     *
     * @param req the request object
     * @param res the response object
     * @param user the authorized user
     * @return the model and view
     */
    public ModelAndView updateTailoringFileView(Request req, Response res, User user) {
        Integer tailoringFileId  = Integer.parseInt(req.params("id"));

        Optional<TailoringFile> tailoringFile =
                ScapFactory.lookupTailoringFileByIdAndOrg(tailoringFileId, user.getOrg());
        if (tailoringFile.isEmpty()) {
            res.redirect("/rhn/manager/audit/scap/tailoring-file/create");
        }
        Map<String, Object> data = new HashMap<>();
        data.put("name", tailoringFile.map(TailoringFile::getName).orElse(null));
        data.put("id", tailoringFileId);
        data.put("description", tailoringFile.map(TailoringFile::getDescription).orElse(null));
        data.put("tailoringFileName", tailoringFile
            .map(TailoringFile::getDisplayFileName)
            .orElse(null));
        return new ModelAndView(data, "templates/audit/create-tailoring-file.jade");
    }
    /**
     * Update an existing Tailoring file
     *
     * @param request the request
     * @param response the response
     * @param user the user
     * @return the json response
     */
    public String updateTailoringFile(Request request, Response response, User user) {
        try {
            List<DiskFileItem> items = RequestUtil.parseMultipartRequest(request);

            String nameParam = RequestUtil.findStringParam(items, "name")
                    .orElseThrow(() ->
                            new IllegalArgumentException("Name parameter missing"));
            Optional<String> descriptionParam = RequestUtil.findStringParam(items, "description");
            String idParam = RequestUtil.findStringParam(items, "id")
                    .orElseThrow(() ->
                            new IllegalArgumentException("ID parameter missing"));

            Integer tailoringFileId = Integer.parseInt(idParam);
            Optional<TailoringFile> existingFile =
                    ScapFactory.lookupTailoringFileByIdAndOrg(tailoringFileId, user.getOrg());

            if (existingFile.isEmpty()) {
                return notFound(response, "Tailoring file not found");
            }
            TailoringFile tailoringFile = existingFile.get();
            tailoringFile.setName(nameParam);
            descriptionParam.ifPresent(tailoringFile::setDescription);

            // Check if a new file was uploaded
            Optional<DiskFileItem> tailoringFileParam = RequestUtil.findFileItem(items, "tailoring_file");
            if (tailoringFileParam.isPresent() && tailoringFileParam.get().getSize() > 0) {
                ensureDirectoryExists(TAILORING_FILES_DIR);

                // Delete old file if it exists (using the unique filename from DB)
                File oldFile = new File(TAILORING_FILES_DIR, tailoringFile.getFileName());
                if (oldFile.exists()) {
                    oldFile.delete();
                }

                // Generate unique filename for the new file using org ID, name, and original filename
                String originalFilename = tailoringFileParam.get().getName();
                String uniqueFilename = generateUniqueFileName(
                    user.getOrg().getId(),
                    nameParam,
                    originalFilename
                );

                // Save new file with unique filename
                saveFileToDirectory(tailoringFileParam.get(), TAILORING_FILES_DIR, uniqueFilename);
                tailoringFile.setFileName(uniqueFilename);
            }

            ScapFactory.saveTailoringFile(tailoringFile);
            return result(response, ResultJson.success());
        }
        catch (Exception e) {
            return handleTailoringFileException(response, e, "updating");
        }
    }

    /**
     * Processes a DELETE request
     *
     * @param req the request object
     * @param res the response object
     * @param user the authorized user
     * @return the result JSON object
     */
    public Object deleteTailoringFile(Request req, Response res, User user) {
        List<Long> ids;
        try {
            ids = Arrays.asList(GSON.fromJson(req.body(), Long[].class));
        }
        catch (JsonParseException e) {
            Spark.halt(HttpStatus.SC_BAD_REQUEST);
            return null;
        }

        List<TailoringFile> tailoringFiles =
                ScapFactory.lookupTailoringFilesByIds(ids, user.getOrg());
        if (tailoringFiles.size() < ids.size()) {
            return result(res, ResultJson.error("not_found"));
        }

        tailoringFiles.forEach(ScapFactory::deleteTailoringFile);
        return result(res, ResultJson.success(tailoringFiles.size()));
    }

    // TODO: Consider moving these generic file utilities to a shared utility class
    //       for reuse across other controllers

    /**
     * Ensures the provided directory exists
     * @param directoryIn the directory to check
     * @throws IOException if directory creation fails
     */
    private void ensureDirectoryExists(String directoryIn) throws IOException {
        File directory = new File(directoryIn);
        if (!directory.exists()) {
            LOG.info("Creating the requested files directory: {}", directoryIn);
            if (!directory.mkdirs()) {
                throw new IOException("Failed to create directory: " + directoryIn);
            }
        }
    }

    /**
     * Saves a file to the tailoring files directory
     * @param fileItem the file to save
     * @param directory the directory to save the file to
     * @throws IOException if file saving fails
     */
    private void saveFileToDirectory(DiskFileItem fileItem, String directory) throws IOException {
        File outputFile = new File(directory, fileItem.getName());
        try (InputStream in = fileItem.getInputStream();
             FileOutputStream out = new FileOutputStream(outputFile)) {
            IOUtils.copy(in, out);
        }
    }

    /**
     * Saves a file to the specified directory with a custom filename
     * @param fileItem the file to save
     * @param directory the directory to save the file to
     * @param customFilename the custom filename to use
     * @throws IOException if file saving fails
     */
    private void saveFileToDirectory(DiskFileItem fileItem, String directory, String customFilename) throws IOException {
        File outputFile = new File(directory, customFilename);
        try (InputStream in = fileItem.getInputStream();
             FileOutputStream out = new FileOutputStream(outputFile)) {
            IOUtils.copy(in, out);
        }
    }

    /**
     * Handles exceptions for tailoring file operations
     * @param response the response object
     * @param e the exception
     * @param operation the operation being performed (e.g., "creating", "updating")
     * @return the error response
     */
    private String handleTailoringFileException(Response response, Exception e, String operation) {
        if (e instanceof IllegalArgumentException || e instanceof FileUploadException) {
            LOG.error("Invalid request when {} tailoring file", operation, e);
            return badRequest(response, e.getMessage());
        }
        else if (e instanceof IOException) {
            LOG.error("Could not {} tailoring file", operation, e);
            return internalServerError(response, e.getMessage());
        }
        else {
            LOG.error("Unexpected error when {} tailoring file", operation, e);
            return internalServerError(response, "An unexpected error occurred.");
        }
    }

    /**
     * Returns a view to display list of SCAP policies
     *
     * @param req the request object
     * @param res the response object
     * @param user the authorized user
     * @return the model and view
     */
    public ModelAndView listScapPoliciesView(Request req, Response res, User user) {
        Map<String, Object> data = new HashMap<>();

        // Use new query that includes compliance stats
        SelectMode m = ModeFactory.getMode("scap_queries", "all_policies_compliance");
        Map<String, Object> params = new HashMap<>();
        params.put("user_id", user.getId());
        params.put("org_id", user.getOrg().getId());
        
        List<ScapPolicyComplianceSummary> policies = m.execute(params);
        data.put("scapPolicies", GSON.toJson(policies));

        return new ModelAndView(data, "templates/audit/list-scap-policies.jade");
    }

    /**
     * Returns a view to display form to create SCAP policy
     *
     * @param req the request object
     * @param res the response object
     * @param user the authorized user
     * @return the model and view
     */
    public ModelAndView createScapPolicyView(Request req, Response res, User user) {
        Map<String, Object> data = new HashMap<>();

        // Set default values for create mode
        data.put("policyData", "null");
        data.put("isEditMode", false);
        data.put("isReadOnly", false);

        // Use shared method to get SCAP data streams
        data.put("scapDataStreams", GSON.toJson(getScapDataStreams()));

        // Get tailoring files for the user's organization
        List<TailoringFile> tailoringFiles = ScapFactory.listTailoringFiles(user.getOrg());
        List<JsonObject> tailoringFilesJson = tailoringFiles.stream()
                .map(this::convertToTailoringFileJson)
                .collect(Collectors.toList());
        data.put("tailoringFiles", tailoringFilesJson);

        return new ModelAndView(data, "templates/audit/create-scap-policy.jade");
    }

    /**
     * Returns a view to display form to edit SCAP policy
     *
     * @param req the request object
     * @param res the response object
     * @param user the authorized user
     * @return the model and view
     */
    public ModelAndView editScapPolicyView(Request req, Response res, User user) {
        Map<String, Object> data = new HashMap<>();
        Integer policyId = Integer.parseInt(req.params("id"));

        // Fetch the policy
        Optional<ScapPolicy> policyOpt = ScapFactory.lookupScapPolicyByIdAndOrg(policyId, user.getOrg());
        if (policyOpt.isEmpty()) {
            res.redirect("/rhn/manager/audit/scap/policies");
            return null;
        }

        ScapPolicy policy = policyOpt.get();

        // Prepare policy data for the form
        Map<String, Object> policyData = new HashMap<>();
        policyData.put("id", policy.getId());
        policyData.put("policyName", policy.getPolicyName());
        policyData.put("description", policy.getDescription());
        policyData.put("dataStreamName", policy.getDataStreamName());
        policyData.put("xccdfProfileId", policy.getXccdfProfileId());
        policyData.put("tailoringProfileId", policy.getTailoringProfileId());
        policyData.put("ovalFiles", policy.getOvalFiles());
        policyData.put("advancedArgs", policy.getAdvancedArgs());
        policyData.put("fetchRemoteResources", policy.getFetchRemoteResources());

        if (policy.getTailoringFile() != null) {
            policyData.put("tailoringFile", policy.getTailoringFile().getId());
            policyData.put("tailoringFileName", policy.getTailoringFile().getFileName());
        }

        data.put("policyData", GSON.toJson(policyData));
        data.put("isEditMode", true);
        data.put("isReadOnly", false);

        // Use shared method to get SCAP data streams
        data.put("scapDataStreams", GSON.toJson(getScapDataStreams()));

        // Get tailoring files for the user's organization
        List<TailoringFile> tailoringFiles = ScapFactory.listTailoringFiles(user.getOrg());
        List<JsonObject> tailoringFilesJson = tailoringFiles.stream()
                .map(this::convertToTailoringFileJson)
                .collect(Collectors.toList());
        data.put("tailoringFiles", tailoringFilesJson);

        return new ModelAndView(data, "templates/audit/create-scap-policy.jade");
    }

    /**
     * Returns a view to display SCAP policy details in readonly mode
     *
     * @param req the request object
     * @param res the response object
     * @param user the authorized user
     * @return the model and view
     */
    public ModelAndView detailScapPolicyView(Request req, Response res, User user) {
        Map<String, Object> data = new HashMap<>();
        Integer policyId = Integer.parseInt(req.params("id"));

        // Fetch the policy
        Optional<ScapPolicy> policyOpt = ScapFactory.lookupScapPolicyByIdAndOrg(policyId, user.getOrg());
        if (policyOpt.isEmpty()) {
            res.redirect("/rhn/manager/audit/scap/policies");
            return null;
        }

        ScapPolicy policy = policyOpt.get();

        // Prepare policy data for readonly view
        Map<String, Object> policyData = new HashMap<>();
        policyData.put("id", policy.getId());
        policyData.put("policyName", policy.getPolicyName());
        policyData.put("description", policy.getDescription());
        policyData.put("dataStreamName", policy.getDataStreamName());
        policyData.put("xccdfProfileId", policy.getXccdfProfileId());
        policyData.put("tailoringProfileId", policy.getTailoringProfileId());
        policyData.put("ovalFiles", policy.getOvalFiles());
        policyData.put("advancedArgs", policy.getAdvancedArgs());
        policyData.put("fetchRemoteResources", policy.getFetchRemoteResources());

        if (policy.getTailoringFile() != null) {
            policyData.put("tailoringFile", policy.getTailoringFile().getId());
            policyData.put("tailoringFileName", policy.getTailoringFile().getDisplayFileName());
        }

        data.put("policyData", GSON.toJson(policyData));
        data.put("isEditMode", false);
        data.put("isReadOnly", true);

        // Use shared method to get SCAP data streams
        data.put("scapDataStreams", GSON.toJson(getScapDataStreams()));

        // Get tailoring files for the user's organization
        List<TailoringFile> tailoringFiles = ScapFactory.listTailoringFiles(user.getOrg());
        List<JsonObject> tailoringFilesJson = tailoringFiles.stream()
                .map(this::convertToTailoringFileJson)
                .collect(Collectors.toList());
        data.put("tailoringFiles", tailoringFilesJson);
        data.put("policyId", policyId);

        return new ModelAndView(data, "templates/audit/scap-policy-details.jade");
    }

    /**
     * API endpoint to get SCAP policy details as JSON
     * @param req the request
     * @param res the response
     * @param user the user
     * @return JSON string with policy details
     */
    public String getScapPolicyDetails(Request req, Response res, User user) {
        Integer policyId = Integer.parseInt(req.params("id"));

        Optional<ScapPolicy> policyOpt = ScapFactory.lookupScapPolicyByIdAndOrg(policyId, user.getOrg());
        if (policyOpt.isEmpty()) {
            return notFound(res, "Policy not found");
        }

        ScapPolicy policy = policyOpt.get();

        // Create a simple DTO with only the fields we need
        Map<String, Object> policyData = new HashMap<>();
        policyData.put("id", policy.getId());
        policyData.put("policyName", policy.getPolicyName());
        policyData.put("description", policy.getDescription());
        policyData.put("dataStreamName", policy.getDataStreamName());
        policyData.put("xccdfProfileId", policy.getXccdfProfileId());
        policyData.put("tailoringProfileId", policy.getTailoringProfileId());
        policyData.put("ovalFiles", policy.getOvalFiles());
        policyData.put("advancedArgs", policy.getAdvancedArgs());
        policyData.put("fetchRemoteResources", policy.getFetchRemoteResources());

        if (policy.getTailoringFile() != null) {
            policyData.put("tailoringFile", policy.getTailoringFile().getFileName());
            policyData.put("tailoringFileName", policy.getTailoringFile().getDisplayFileName());
        }

        return json(res, policyData);
    }

    /**
     * API endpoint to get scan history for a specific policy
     * @param req the request
     * @param res the response
     * @param user the user
     * @return JSON string with scan history
     */
    public String getPolicyScanHistory(Request req, Response res, User user) {
        Integer policyId = Integer.parseInt(req.params("id"));
        
        SelectMode m = ModeFactory.getMode("scap_queries", "policy_scan_history");
        Map<String, Object> params = new HashMap<>();
        params.put("user_id", user.getId());
        params.put("policy_id", policyId);
        
        List<ScapPolicyScanHistory> history = m.execute(params);
        return json(res, history, new TypeToken<>() {});
    }

    /**
     * Schedules SCAP audit scan for specified systems
     * @param req the request
     * @param res the response
     * @param user the user
     * @return JSON with action ID
     */
    public String scheduleAuditScan(Request req, Response res, User user) {
        try {
            // Parse request body into DTO
            AuditScanScheduleJson reqData = GSON.fromJson(req.body(), AuditScanScheduleJson.class);

            // Validate required fields
            String validationError = reqData.validate();
            if (validationError != null) {
                res.status(HttpStatus.SC_BAD_REQUEST);
                return result(res, ResultJson.error(validationError));
            }

            // Parse earliest date
            Date earliest = reqData.getEarliest()
                    .map(ldt -> Date.from(ldt.atZone(java.time.ZoneId.systemDefault()).toInstant()))
                    .orElse(new Date());

            // Schedule the SCAP action
            ScapAction action = ActionManager.scheduleXccdfEval(
                    user,
                    reqData.getIds(),
                    reqData.getDataStreamName(),
                    reqData.buildOscapParameters(),
                    reqData.getOvalFiles(), // OVAL files
                    earliest,
                    reqData.getPolicyId() // Link to policy if provided
            );

            // Return action ID
            return json(res, action.getId());

        } catch (TaskomaticApiException e) {
            res.status(HttpStatus.SC_INTERNAL_SERVER_ERROR);
            return result(res, ResultJson.error("Failed to schedule SCAP scan: Taskomatic is down"));
        } catch (Exception e) {
            res.status(HttpStatus.SC_INTERNAL_SERVER_ERROR);
            return result(res, ResultJson.error("Failed to schedule SCAP scan: " + e.getMessage()));
        }
    }

    /**
     * Returns a view to display list of SCAP content
     *
     * @param req the request object
     * @param res the response object
     * @param user the authorized user
     * @return the model and view
     */
    public ModelAndView listScapContentView(Request req, Response res, User user) {
        Map<String, Object> data = new HashMap<>();
        List<ScapContent> scapContentList = ScapFactory.listScapContent();
        data.put("scapContent", GSON.toJson(scapContentList));
        return new ModelAndView(data, "templates/audit/list-scap-content.jade");
    }

    /**
     * Returns a view to display form to create SCAP content
     *
     * @param req the request object
     * @param res the response object
     * @param user the authorized user
     * @return the model and view
     */
    public ModelAndView createScapContentView(Request req, Response res, User user) {
        Map<String, Object> data = new HashMap<>();
        data.put("scapContentData", GSON.toJson(new HashMap<>()));
        return new ModelAndView(data, "templates/audit/create-scap-content.jade");
    }

    /**
     * Returns a view to display form to edit SCAP content
     *
     * @param req the request object
     * @param res the response object
     * @param user the authorized user
     * @return the model and view
     */
    public ModelAndView updateScapContentView(Request req, Response res, User user) {
        Map<String, Object> data = new HashMap<>();
        Integer id = Integer.parseInt(req.params("id"));
        Optional<ScapContent> scapContent = ScapFactory.lookupScapContentById(Long.valueOf(id));

        if (scapContent.isPresent()) {
            Map<String, Object> contentData = new HashMap<>();
            contentData.put("id", scapContent.get().getId());
            contentData.put("name", scapContent.get().getName());
            contentData.put("description", scapContent.get().getDescription());
            contentData.put("dataStreamFileName", scapContent.get().getDataStreamFileName());
            contentData.put("xccdfFileName", scapContent.get().getXccdfFileName());
            data.put("scapContentData", GSON.toJson(contentData));
        } else {
            data.put("scapContentData", GSON.toJson(new HashMap<>()));
        }

        return new ModelAndView(data, "templates/audit/create-scap-content.jade");
    }

    /**
     * Creates a new SCAP content
     *
     * @param req the request object
     * @param res the response object
     * @param user the authorized user
     * @return the result as JSON
     */
    public String createScapContent(Request req, Response res, User user) {
        return handleScapContentUpload(req, res, user, null);
    }

    /**
     * Updates an existing SCAP content
     *
     * @param req the request object
     * @param res the response object
     * @param user the authorized user
     * @return the result as JSON
     */
    public String updateScapContent(Request req, Response res, User user) {
        try {
            List<DiskFileItem> items = RequestUtil.parseMultipartRequest(req);
            String idParam = RequestUtil.findStringParam(items, "id")
              .orElseThrow(() ->
                new IllegalArgumentException("ID parameter missing"));
            Optional<ScapContent> scapContent = ScapFactory.lookupScapContentById(Long.valueOf(idParam));

            if (scapContent.isEmpty()) {
                return result(res, ResultJson.error("SCAP content not found"));
            }

            return handleScapContentUpload(req, res, user, scapContent.get());
        } catch (FileUploadException e) {
            throw new RuntimeException(e);
        }
    }

    /**
     * Deletes SCAP content
     *
     * @param req the request object
     * @param res the response object
     * @param user the authorized user
     * @return the result as JSON
     */
    public String deleteScapContent(Request req, Response res, User user) {
        List<Integer> ids = GSON.fromJson(req.body(), new TypeToken<List<Integer>>() { }.getType());
        List<ScapContent> scapContentList = ScapFactory.lookupScapContentByIds(
                ids.stream().map(Long::valueOf).collect(Collectors.toList()));

        scapContentList.forEach(content -> {
            try {
                // Delete DataStream file
                Path dsFilePath = Paths.get(SCAP_CONTENT_DIR, content.getDataStreamFileName());
                Files.deleteIfExists(dsFilePath);
                // Delete XCCDF file
                Path xccdfFilePath = Paths.get(SCAP_CONTENT_DIR, content.getXccdfFileName());
                Files.deleteIfExists(xccdfFilePath);
            } catch (IOException e) {
                LOG.error("Error deleting SCAP content files for: {}", content.getName(), e);
            }
            ScapFactory.deleteScapContent(content);
        });

        return result(res, ResultJson.success(scapContentList.size()));
    }

    /**
     * Handles SCAP content file upload for create and update operations
     *
     * @param req the request object
     * @param res the response object
     * @param user the user
     * @param existingContent existing SCAP content or null for create
     * @return the result as JSON
     */
    private String handleScapContentUpload(Request req, Response res, User user, ScapContent existingContent) {
        try {
            List<DiskFileItem> items = RequestUtil.parseMultipartRequest(req);
            String name = RequestUtil.findStringParam(items, "name")
              .orElseThrow(() ->
                new IllegalArgumentException("Name parameter missing"));
            Optional<String> description = RequestUtil.findStringParam(items, "description");

            // Get file items
            Optional<DiskFileItem> scapFileOpt = RequestUtil.findFileItem(items, "scapFile");
            Optional<DiskFileItem> xccdfFileOpt = RequestUtil.findFileItem(items, "xccdfFile");

            // For create operation, both files are required
            if (existingContent == null) {
                if (!scapFileOpt.isPresent()) {
                    return result(res, ResultJson.error("DataStream file is required"));
                }
                if (!xccdfFileOpt.isPresent()) {
                    return result(res, ResultJson.error("XCCDF file is required"));
                }
            }

            ScapContent scapContent = existingContent != null ? existingContent : new ScapContent();
            scapContent.setName(name.trim());
            description.ifPresent(scapContent::setDescription);

            ensureDirectoryExists(SCAP_CONTENT_DIR);

            // Process DataStream file if provided
            if (scapFileOpt.isPresent()) {
                DiskFileItem scapFile = scapFileOpt.get();
                String dsFileName = scapFile.getName();

                // Validate DataStream filename
                if (!dsFileName.endsWith("-ds.xml")) {
                    return result(res, ResultJson.error(
                        "DataStream file must end with '-ds.xml'"));
                }

                saveFileToDirectory(scapFile, SCAP_CONTENT_DIR);
                scapContent.setDataStreamFileName(dsFileName);
            }

            // Process XCCDF file if provided
            if (xccdfFileOpt.isPresent()) {
                DiskFileItem xccdfFile = xccdfFileOpt.get();
                String xccdfFileName = xccdfFile.getName();

                // Validate XCCDF filename
                if (!xccdfFileName.endsWith("-xccdf.xml")) {
                    return result(res, ResultJson.error(
                        "XCCDF file must end with '-xccdf.xml'"));
                }

                saveFileToDirectory(xccdfFile, SCAP_CONTENT_DIR);
                scapContent.setXccdfFileName(xccdfFileName);
            }

            // Validate that both files share the same base name (if both are being set)
            if (scapFileOpt.isPresent() && xccdfFileOpt.isPresent()) {
                String dsBaseName = scapContent.getDataStreamFileName().replace("-ds.xml", "");
                String xccdfBaseName = scapContent.getXccdfFileName().replace("-xccdf.xml", "");

                if (!dsBaseName.equals(xccdfBaseName)) {
                    return result(res, ResultJson.error(
                        "DataStream and XCCDF files must share the same base name. " +
                        "DataStream: '" + dsBaseName + "', XCCDF: '" + xccdfBaseName + "'"));
                }
            }

            ScapFactory.saveScapContent(scapContent);
            return result(res, ResultJson.success());

        } catch (Exception e) {
            LOG.error("Error handling SCAP content upload", e);
            return handleTailoringFileException(res, e, "creating");
        }
    }

    /**
     * Processes a GET request to get a list of scap configurations
     *
     * @param request  the request object
     * @param response  the response object
     * @param user the authorized user
     * @param server the server
     * @return the result JSON object
     */
    private ModelAndView scheduleAuditScanView(Request request, Response response, User user, Server server) {

        Map<String, Object> data = new HashMap<>();
        data.put("scapDataStreams", Json.GSON.toJson(getScapDataStreams()));
        List<TailoringFile> tailoringFiles = ScapFactory.listTailoringFiles(user.getOrg());
        List<JsonObject> collect = tailoringFiles.stream().map(this::convertToTailoringFileJson).collect(Collectors.toList());
        data.put("tailoringFiles", collect);

        List<ScapPolicy> scapPolicies = ScapFactory.listScapPolicies(user.getOrg());
        List<JsonObject> scapPoliciesJson = scapPolicies.stream()
          .map(policy -> {
              JsonObject json = new JsonObject();
              json.addProperty("id", policy.getId());
              json.addProperty("policyName", policy.getPolicyName());
              return json;
          })
          .collect(Collectors.toList());
        data.put("scapPolicies", scapPoliciesJson);

        return new ModelAndView(data, "templates/minion/schedule-scap-scan.jade");
    }

    /**
     * Returns a view to display create form
     *
     * @param req the request object
     * @param res the response object
     * @param user the authorized user
     * @return the model and view
     */
    public static ModelAndView createRuleResultView(Request req, Response res, User user, Server server) {

        String serverId = req.params("sid");
        Long ruleResultId = Long.parseLong(req.params("rrid"));
        XccdfRuleResultDto ruleResult = ScapManager.ruleResultById(ruleResultId);
        
        // Use both benchmark ID and rule identifier for correct lookup
        // benchmarkId from benchmark.identifier, ruleId from documentIdref
        Optional<XccdfRuleFix> xccdfRuleFix =
                ScapFactory.lookupRuleRemediation(ruleResult.getTestResult().getBenchmark().getIdentifier(),
                        ruleResult.getDocumentIdref());
        String remediation = xccdfRuleFix.map(fix -> fix.getRemediation())
          .orElse("No remediation available");

        Map<String, Object> data = new HashMap<>();
        data.put("identifier",ruleResult.getDocumentIdref());
        data.put("result", ruleResult.getLabel());
        data.put("parentScanUrl","/rhn/systems/details/audit/XccdfDetails.do?sid="+serverId+ "&xid="+ruleResultId );
        data.put("parentScanProfile",ruleResult.getTestResult().getIdentifier());
        data.put("remediation", StringEscapeUtils.escapeJson(remediation));
        data.put("profileId", "----");
        data.put("benchmarkId", ruleResult.getTestResult().getBenchmark().getIdentifier());
        return new ModelAndView(data, "templates/minion/rule-result-detail.jade");
    }

    /**
     * Creates a JSON object for an {@link TailoringFile} instance
     *
     * @param file the TailoringFile instance
     * @return the JSON object
     */
    private JsonObject convertToTailoringFileJson(TailoringFile file) {
        JsonObject json = new JsonObject();
        json.addProperty("name", file.getName());
        json.addProperty("id", file.getId());
        // Display the original filename to users, not the unique internal filename
        json.addProperty("fileName", file.getFileName());
        json.addProperty("displayfileName", file.getDisplayFileName());
        return json;
    }

    /**
     * Generates a unique filename using org ID, tailoring file name, and original filename
     * Format: {orgId}_{sanitizedName}_{originalFilename}
     * This approach is more portable for migration/replication scenarios than using database IDs
     *
     * @param orgId the organization ID
     * @param tailoringFileName the name of the tailoring file (user-provided)
     * @param originalFilename the original filename
     * @return the unique filename
     */
    private static String generateUniqueFileName(Long orgId, String tailoringFileName, String originalFilename) {
        // Sanitize the tailoring file name to make it filesystem-safe
        // Underscores are reserved as delimiters, so they're converted to hyphens
        String sanitizedName = tailoringFileName
            .replaceAll("[^a-zA-Z0-9-]", "-")  // Replace non-alphanumeric chars (including _) with hyphen
            .replaceAll("-{2,}", "-")           // Replace multiple hyphens with single hyphen
            .toLowerCase();

        return orgId + "_" + sanitizedName + "_" + originalFilename;
    }

    /**
     * Reads SCAP content files from the standard directory
     * @return List of SCAP Datastream (-ds.xml) file names
     */
    private List<String> getScapDataStreams() {
        File scapContentDir = new File(SCAP_CONTENT_DIR);
        if (!scapContentDir.exists() || !scapContentDir.isDirectory()) {
            LOG.warn("SCAP content directory does not exist: {}", SCAP_CONTENT_DIR);
            return Collections.emptyList();
        }

        File[] scapDatastreams = scapContentDir.listFiles((dir, name) -> name.endsWith("-ds.xml"));
        if (scapDatastreams == null) {
            return Collections.emptyList();
        }

        return Arrays.stream(scapDatastreams)
                .map(File::getName)
                .sorted()
                .collect(Collectors.toList());
    }
    /**
     * Processes a GET request to get a list of profiles from SCAP content
     *
     * @param req  the request object
     * @param res  the response object
     * @param user the authorized user
     * @return the result JSON object
     */
    public static Object getProfileList(Request req, Response res, User user) {
        String location = SCAP_CONTENT_DIR;
        String contentType = req.params("type");
        String contentName = req.params("name");
        
        if (contentType.equalsIgnoreCase("tailoringFile")) {
            location = TAILORING_FILES_DIR;
        }
        
        Path filePath;
        
        // If content name ends with -ds.xml, find the corresponding -xccdf.xml file
        if (contentName.endsWith("-ds.xml")) {
            String baseName = contentName.substring(0, contentName.length() - "-ds.xml".length());
            String xccdfFileName = baseName + "-xccdf.xml";
            filePath = Paths.get(location).resolve(xccdfFileName);
            
            // Verify the XCCDF file exists
            if (!Files.exists(filePath)) {
                LOG.error("Corresponding XCCDF file not found: {}", xccdfFileName);
                return json(res, ResultJson.error("XCCDF file not found for: " + contentName), new TypeToken<>() { });
            }
        } else {
            // For tailoring files or direct XCCDF references
            filePath = Paths.get(location).resolve(contentName);
        }
        
        try {
            BenchMark result = ScapManager.getProfileList(filePath.toFile());
            return json(res, result.getProfiles(), new TypeToken<>() { });
        } catch (IOException e) {
            LOG.error("Error reading profile list from: {}", filePath, e);
            throw new RuntimeException(e);
        }
    }

    /**
     * Helper method to set optional string field if not empty
     * @param value the value to check
     * @param setter the setter method to call if value is not empty
     */
    private static void setIfNotEmpty(String value, java.util.function.Consumer<String> setter) {
        Optional.ofNullable(value)
                .map(String::trim)
                .filter(s -> !s.isEmpty())
                .ifPresent(setter);
    }

    /**
     * Helper method to set field to trimmed value or null if empty
     * @param value the value to check
     * @param setter the setter method to call with trimmed value or null
     */
    private static void setOrNull(String value, java.util.function.Consumer<String> setter) {
        setter.accept(
                Optional.ofNullable(value)
                        .map(String::trim)
                        .filter(s -> !s.isEmpty())
                        .orElse(null)
        );
    }

    /**
     * Validates required policy fields
     * @param policyJson the JSON data to validate
     * @return error message if validation fails, null if valid
     */
    private static String validatePolicyFields(ScapPolicyJson policyJson) {
        if (policyJson.getPolicyName() == null || policyJson.getPolicyName().trim().isEmpty()) {
            return "Policy name is required";
        }
        if (policyJson.getScapContentId() == null) {
            return "SCAP Content ID is required";
        }
        if (policyJson.getXccdfProfileId() == null || policyJson.getXccdfProfileId().trim().isEmpty()) {
            return "XCCDF profile ID is required";
        }
        return null;
    }

    /**
     * Helper method to apply common policy fields from JSON to ScapPolicy entity
     * @param policyJson the JSON data
     * @param scapPolicy the policy entity to update
     * @param user the user for tailoring file lookup
     */
    private static void applyPolicyFields(ScapPolicyJson policyJson, ScapPolicy scapPolicy, User user) {
        // Set optional fields - use setOrNull to allow clearing fields (empty string becomes null)
        setOrNull(policyJson.getDescription(), scapPolicy::setDescription);
        setOrNull(policyJson.getTailoringProfileId(), scapPolicy::setTailoringProfileId);
        setOrNull(policyJson.getOvalFiles(), scapPolicy::setOvalFiles);
        setOrNull(policyJson.getAdvancedArgs(), scapPolicy::setAdvancedArgs);

        // Set optional tailoring file
        Optional.ofNullable(policyJson.getTailoringFile())
                .filter(id -> !id.isEmpty())
                .map(Integer::parseInt)
                .flatMap(id -> ScapFactory.lookupTailoringFileByIdAndOrg(id, user.getOrg()))
                .ifPresent(scapPolicy::setTailoringFile);

        // Set fetch remote resources flag
        Optional.ofNullable(policyJson.getFetchRemoteResources())
                .ifPresent(scapPolicy::setFetchRemoteResources);
    }

    /**
     * Creates a new SCAP policy
     *
     * @param req the request object
     * @param res the response object
     * @param user the authorized user
     * @return the result as JSON
     */
    public String createScapPolicy(Request req, Response res, User user) {
        try {
            // Parse JSON request body
            ScapPolicyJson policyJson = GSON.fromJson(req.body(), ScapPolicyJson.class);

            // Validate required fields
            String validationError = validatePolicyFields(policyJson);
            if (validationError != null) {
                return json(res, ResultJson.error(validationError));
            }

            // Create new SCAP policy
            // Lookup SCAP Content
            // Lookup SCAP Content
            ScapContent content = ScapFactory.lookupScapContentById(policyJson.getScapContentId()).orElse(null);
            if (content == null) {
                return json(res, ResultJson.error("SCAP Content not found for ID: " + policyJson.getScapContentId()));
            }

            // Create new SCAP policy
            ScapPolicy scapPolicy = new ScapPolicy(
                policyJson.getPolicyName().trim(),
                content,
                policyJson.getXccdfProfileId().trim()
            );
            scapPolicy.setOrg(user.getOrg());

            // Apply common policy fields
            applyPolicyFields(policyJson, scapPolicy, user);

            // Save to database
            ScapFactory.saveScapPolicy(scapPolicy);

            return json(res, ResultJson.success(scapPolicy.getId()));
        }
        catch (Exception e) {
            LOG.error("Error creating SCAP policy", e);
            return json(res, ResultJson.error(e.getMessage()));
        }
    }

    /**
     * Updates an existing SCAP policy
     *
     * @param req the request object
     * @param res the response object
     * @param user the authorized user
     * @return the result as JSON
     */
    public String updateScapPolicy(Request req, Response res, User user) {
        try {
            // Parse JSON request body
            ScapPolicyJson policyJson = GSON.fromJson(req.body(), ScapPolicyJson.class);

            // Validate policy ID
            if (policyJson.getId() == null) {
                return json(res, ResultJson.error("Policy ID is required"));
            }

            // Validate required fields
            String validationError = validatePolicyFields(policyJson);
            if (validationError != null) {
                return json(res, ResultJson.error(validationError));
            }

            Optional<ScapPolicy> scapPolicyOpt = ScapFactory.lookupScapPolicyByIdAndOrg(policyJson.getId(), user.getOrg());

            if (!scapPolicyOpt.isPresent()) {
                return json(res, ResultJson.error("SCAP policy not found"));
            }

            ScapPolicy scapPolicy = scapPolicyOpt.get();

            // Update core fields
            setIfNotEmpty(policyJson.getPolicyName(), scapPolicy::setPolicyName);
            if (policyJson.getScapContentId() != null) {
                ScapContent content = ScapFactory.lookupScapContentById(policyJson.getScapContentId()).orElse(null);
                if (content != null) {
                    scapPolicy.setScapContent(content);
                }
            }
            setIfNotEmpty(policyJson.getXccdfProfileId(), scapPolicy::setXccdfProfileId);

            // Handle tailoring file - set to null if empty to allow clearing
            if (policyJson.getTailoringFile() == null || policyJson.getTailoringFile().isEmpty()) {
                scapPolicy.setTailoringFile(null);
            }

            // Apply common policy fields
            applyPolicyFields(policyJson, scapPolicy, user);

            // Save to database
            ScapFactory.saveScapPolicy(scapPolicy);

            return json(res, ResultJson.success(scapPolicy.getId()));
        }
        catch (Exception e) {
            LOG.error("Error updating SCAP policy", e);
            return json(res, ResultJson.error(e.getMessage()));
        }
    }

    /**
     * Deletes SCAP policies
     *
     * @param req the request object
     * @param res the response object
     * @param user the authorized user
     * @return the result as JSON
     */
    public String deleteScapPolicy(Request req, Response res, User user) {
        try {
            // Parse array of policy IDs from request body
            Integer[] policyIds = GSON.fromJson(req.body(), Integer[].class);
            List<Integer> ids = Arrays.asList(policyIds);

            // Lookup policies by IDs
            List<ScapPolicy> scapPolicies = ScapFactory.lookupScapPoliciesByIds(ids, user.getOrg());

            if (scapPolicies.size() < ids.size()) {
                return json(res, ResultJson.error("One or more SCAP policies not found"));
            }

            // Delete all policies
            scapPolicies.forEach(ScapFactory::deleteScapPolicy);

            return json(res, ResultJson.success(scapPolicies.size()));
        }
        catch (Exception e) {
            LOG.error("Error deleting SCAP policies", e);
            return json(res, ResultJson.error(e.getMessage()));
        }
    }

    /**
     * Get custom remediation for a rule.
     *
     * @param request the request
     * @param response the response
     * @param user the current user
     * @return JSON response
     */
    public String getCustomRemediation(Request request, Response response, User user) {
        String identifier = request.params(":identifier");
        String benchmarkId = request.params(":benchmarkId");

        Optional<XccdfRuleFixCustom> customOpt = ScapFactory.lookupCustomRemediationByIdentifier(
                identifier, benchmarkId, user.getOrg());

        if (customOpt.isPresent()) {
            XccdfRuleFixCustom custom = customOpt.get();
            Map<String, Object> data = new HashMap<>();
            data.put("identifier", identifier);
            data.put("benchmarkId", benchmarkId);
            data.put("customRemediationBash", custom.getCustomRemediationBash());
            data.put("customRemediationSalt", custom.getCustomRemediationSalt());
            data.put("created", custom.getCreated());
            data.put("modified", custom.getModified());

            return success(response, ResultJson.success(data));
        }

        return json(response, HttpStatus.SC_NOT_FOUND,
                ResultJson.error("No custom remediation found"), new TypeToken<>() { });
    }

    /**
     * Save custom remediation for a rule.
     *
     * @param request the request
     * @param response the response
     * @param user the current user
     * @return JSON response
     */
    public String saveCustomRemediation(Request request, Response response, User user) {
        if (!user.hasRole(RoleFactory.ORG_ADMIN)) {
            return json(response, HttpStatus.SC_FORBIDDEN,
                    ResultJson.error("Only organization administrators can save custom remediation"), new TypeToken<>() { });
        }

        CustomRemediationJson data;
        try {
            data = GSON.fromJson(request.body(), CustomRemediationJson.class);
        } catch (JsonParseException e) {
            return json(response, HttpStatus.SC_BAD_REQUEST,
                    ResultJson.error("Invalid JSON request body"));
        }

        String identifier = data.getIdentifier();
        String benchmarkId = data.getBenchmarkId();
        ScriptType scriptType = ScriptType.fromValue(data.getScriptType());
        String remediationContent = data.getRemediation();

        if (identifier == null || identifier.isEmpty()) {
            return json(response, HttpStatus.SC_BAD_REQUEST,
                    ResultJson.error("Rule identifier is required"), new TypeToken<>() { });
        }

        if (benchmarkId == null || benchmarkId.isEmpty()) {
            return json(response, HttpStatus.SC_BAD_REQUEST,
                    ResultJson.error("Benchmark ID is required"), new TypeToken<>() { });
        }

        if (scriptType == null) {
            return json(response, HttpStatus.SC_BAD_REQUEST,
                    ResultJson.error("Script type must be 'bash' or 'salt'"), new TypeToken<>() { });
        }

        if (remediationContent == null || remediationContent.isEmpty()) {
            return json(response, HttpStatus.SC_BAD_REQUEST,
                    ResultJson.error("Remediation content is required"), new TypeToken<>() { });
        }

        try {
            ScapFactory.saveCustomRemediation(identifier, benchmarkId, scriptType,
                    remediationContent, user.getOrg(), user);

            return success(response, ResultJson.success("Custom remediation saved successfully"));
        } catch (IllegalArgumentException e) {
            return json(response, HttpStatus.SC_BAD_REQUEST,
                    ResultJson.error(e.getMessage()), new TypeToken<>() { });
        } catch (Exception e) {
            return json(response, HttpStatus.SC_INTERNAL_SERVER_ERROR,
                    ResultJson.error("Failed to save custom remediation: " + e.getMessage()), new TypeToken<>() { });
        }
    }

    /**
     * Delete custom remediation for a rule.
     *
     * @param request the request
     * @param response the response
     * @param user the current user
     * @return JSON response
     */
    public String deleteCustomRemediation(Request request, Response response, User user) {
        if (!user.hasRole(RoleFactory.ORG_ADMIN)) {
            return json(response, HttpStatus.SC_FORBIDDEN,
                    ResultJson.error("Only organization administrators can delete custom remediation"), new TypeToken<>() { });
        }

        String identifier = request.params(":identifier");
        String benchmarkId = request.params(":benchmarkId");
        ScriptType scriptType = ScriptType.fromValue(request.params(":scriptType"));

        if (scriptType == null) {
            return json(response, HttpStatus.SC_BAD_REQUEST,
                    ResultJson.error("Invalid script type"), new TypeToken<>() { });
        }

        boolean deleted = ScapFactory.deleteCustomRemediation(identifier, benchmarkId,
                scriptType, user.getOrg());

        if (deleted) {
            return success(response, ResultJson.success("Custom remediation deleted successfully"));
        }

        return json(response, HttpStatus.SC_NOT_FOUND,
                ResultJson.error("No custom remediation found"), new TypeToken<>() { });
    }
    
     /**
     * Apply remediation (bash or Salt) to a system.
     * @param request the HTTP request containing remediation details
     * @param response the HTTP response
     * @param user the current user
     * @return JSON response with action ID and status message
     */
    String applyRemediation(Request request, Response response, User user) {
        Action action;
        String successMessage;
        try {
            if (!user.hasRole(RoleFactory.ORG_ADMIN)) {
                return result(response, ResultJson.error("Only organization administrators can apply remediation"));
            }

            ApplyRemediationJson body = GSON.fromJson(request.body(), ApplyRemediationJson.class);
            String validationError = validateRemediationBody(body);
            if (validationError != null) {
                return result(response, ResultJson.error(validationError));
            }
            Server server = SystemManager.lookupByIdAndUser(body.getServerId(), user);
            if (server == null) {
                response.status(HttpStatus.SC_NOT_FOUND);
                return result(response, ResultJson.error("Server not found or access denied"));
            }

            ScriptType scriptType = ScriptType.fromValue(body.getScriptType());
            if (scriptType == ScriptType.BASH) {
                action = createBashRemediationAction(body, user, server);
                successMessage = "Bash remediation scheduled successfully";
            } else if (scriptType == ScriptType.SALT) {
                action = createSaltRemediationAction(body, user, server);
                successMessage = "Salt remediation scheduled successfully";
            } else {
                return result(response, ResultJson.error("Invalid script type"));
            }

            taskomaticApi.scheduleActionExecution(action);
            Map<String, Object> result = Map.of(
                "actionId", action.getId(),
                "message", successMessage
            );
            return result(response, ResultJson.success(result));
        } catch (JsonParseException e) {
            return result(response, ResultJson.error("Invalid JSON request body"));
        } catch (TaskomaticApiException e) {
            LOG.error("Failed to schedule remediation action", e);
            return result(response, ResultJson.error("Failed to schedule remediation: " + e.getMessage()));
        } catch (Exception e) {
            LOG.error("Error applying remediation", e);
            return result(response, ResultJson.error("Failed to apply remediation: " + e.getMessage()));
        }
    }
    
    /**
     * Validate the remediation request body.
     *
     * @param body the parsed request body
     * @return error message if validation fails, null otherwise
     */
    public String validateRemediationBody(ApplyRemediationJson body) {
        if (body.getRemediationContent() == null || body.getRemediationContent().trim().isEmpty()) {
            return "Remediation content cannot be empty";
        }
        if (body.getScriptType() == null || ScriptType.fromValue(body.getScriptType()) == null) {
            return "Invalid script type. Must be 'bash' or 'salt'";
        }
        return null;
    }

    /**
     * Create a Bash script action for remediation.
     * This creates a ScriptRunAction that executes the remediation script as root.
     *
     * @param body the remediation request containing script content
     * @param user the user scheduling the action
     * @param server the target server
     * @return the created and saved ScriptRunAction
     */
    Action createBashRemediationAction(ApplyRemediationJson body, User user, Server server) {
        final long scriptTimeout = 300L; // 5 minutes timeout for remediation scripts
        ScriptActionDetails scriptDetails = ActionFactory.createScriptActionDetails(
                "root", "root", scriptTimeout, body.getRemediationContent());
        ScriptRunAction action = (ScriptRunAction) ActionFactory.createAction(ActionFactory.TYPE_SCRIPT_RUN);
        action.setName(REMEDIATION_ACTION_PREFIX + body.getRuleIdentifier());
        action.setOrg(user.getOrg());
        action.setSchedulerUser(user);
        action.setEarliestAction(new Date());
        action.setScriptActionDetails(scriptDetails);
        ActionFactory.addServerToAction(server.getId(), action);
        ActionFactory.save(action);
        return action;
    }
    /**
     * Create a Salt state action for remediation.
     * This creates an ApplyStatesAction that applies the scap_remediation state
     * with the remediation content passed via pillar data.
     *
     * @param body the remediation request containing Salt state content
     * @param user the user scheduling the action
     * @param server the target server (must be a Salt minion)
     * @return the created and saved ApplyStatesAction
     */
    Action createSaltRemediationAction(ApplyRemediationJson body, User user, Server server) {
        // Pass Salt state content via pillar data
        Map<String, Object> pillar = new HashMap<>();
        pillar.put("scap_remediation_state", body.getRemediationContent());
        // Schedule apply states action with scap_remediation state module
        ApplyStatesAction action = ActionManager.scheduleApplyStates(
                user,
                Collections.singletonList(server.getId()),
                Collections.singletonList("scap_remediation"),
                Optional.of(pillar),
                new Date(),
                Optional.empty()
        );
        // Set custom action name to match Bash remediation naming
        action.setName(REMEDIATION_ACTION_PREFIX + body.getRuleIdentifier());
        // Note: scheduleApplyStates already saves the action, but we need to save again
        // after modifying the name to persist the change
        ActionFactory.save(action);
        return action;
    }
} 
