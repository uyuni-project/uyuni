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
import com.google.gson.JsonParseException;
import com.redhat.rhn.common.db.datasource.ModeFactory;
import com.redhat.rhn.common.db.datasource.SelectMode;
import com.redhat.rhn.common.util.FileUtils;
import com.redhat.rhn.domain.action.Action;
import com.redhat.rhn.domain.action.ActionFactory;
import com.redhat.rhn.domain.action.scap.ScapAction;
import com.redhat.rhn.domain.action.script.ScriptActionDetails;
import com.redhat.rhn.domain.action.script.ScriptRunAction;
import com.redhat.rhn.domain.action.salt.ApplyStatesAction;
import com.redhat.rhn.domain.audit.*;
import com.redhat.rhn.domain.server.Server;
import com.redhat.rhn.domain.server.ServerFactory;
import com.redhat.rhn.domain.user.User;

import com.redhat.rhn.frontend.dto.XccdfRuleResultDto;
import com.redhat.rhn.manager.action.ActionManager;
import com.redhat.rhn.manager.audit.ScapManager;
import com.redhat.rhn.manager.audit.scap.xml.BenchMark;
import com.redhat.rhn.manager.audit.scap.xml.Profile;
import com.redhat.rhn.manager.system.SystemManager;
import com.redhat.rhn.taskomatic.TaskomaticApi;
import com.redhat.rhn.taskomatic.TaskomaticApiException;
import com.redhat.rhn.manager.ssm.SsmManager;
import com.suse.manager.webui.controllers.utils.MultipartRequestUtil;

import com.suse.manager.webui.utils.gson.AuditScanScheduleJson;
import com.suse.manager.webui.utils.gson.ResultJson;
import com.suse.manager.webui.utils.gson.TailoringFileJson;
import com.suse.manager.webui.utils.gson.ScapContentJson;
import com.suse.manager.webui.utils.gson.ScapPolicyResponseJson;
import com.suse.manager.webui.utils.gson.ScapPolicyComplianceSummary;
import com.suse.manager.webui.utils.gson.ScapPolicyJson;
import com.suse.manager.webui.utils.gson.ScapPolicyScanHistory;

import com.suse.utils.Json;

import com.google.gson.reflect.TypeToken;

import org.apache.commons.fileupload2.core.DiskFileItem;
import org.apache.commons.fileupload2.core.FileUploadException;

import org.apache.commons.text.StringEscapeUtils;
import org.apache.http.HttpStatus;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import spark.ModelAndView;
import spark.Request;
import spark.Response;
import spark.template.jade.JadeTemplateEngine;

import java.io.File;

import java.io.IOException;

import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.util.*;
import java.util.stream.Collectors;

import static com.suse.manager.webui.utils.SparkApplicationHelper.*;
import static spark.Spark.delete;
import static spark.Spark.get;
import static spark.Spark.halt;
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
        // Tailoring files routes
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

        get("/manager/api/audit/profiles/list/:type/:id", withUser(this::getProfileList));
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
        // SSM Schedule
        get("/manager/systems/ssm/audit/schedule-scap-scan",
                withCsrfToken(withDocsLocale(withUser(this::scheduleAuditScanSsmView))),
                jade);

        post("manager/api/audit/schedule/create", withUser(this::scheduleAuditScan));

        //rules

        get("/manager/audit/scap/scan/rule-result-details/:sid/:rrid",
          withCsrfToken(withDocsLocale(withUserAndServer(this::createRuleResultView))), jade);

        // Custom remediation routes
        get("/manager/api/audit/scap/custom-remediation/:identifier/:benchmarkId",
                withUser(this::getCustomRemediation));
        post("/manager/api/audit/scap/custom-remediation",
                withOrgAdmin(this::saveCustomRemediation));
        delete("/manager/api/audit/scap/custom-remediation/:identifier/:benchmarkId/:scriptType",
                withOrgAdmin(this::deleteCustomRemediation));
        post("/manager/api/audit/scap/scan/rule-apply-remediation",
                withOrgAdmin(this::applyRemediation));

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
        try {
            List<TailoringFile> tailoringFiles = ScapFactory.listTailoringFiles(user.getOrg());
            List<TailoringFileJson> tailoringFilesJsonObjects = tailoringFiles.stream()
                    .map(file -> new TailoringFileJson(
                        file.getId(),
                        file.getName(),
                        file.getFileName(),
                        file.getDisplayFileName(),
                        file.getDescription()
                    ))
                    .collect(Collectors.toList());

            Map<String, Object> data = new HashMap<>();
            data.put("tailoringFiles", tailoringFilesJsonObjects);

            return new ModelAndView(data, "templates/audit/list-tailoring-files.jade");
        } catch (Exception e) {
            LOG.error("Failed to load tailoring files view", e);
            throw halt(500, "Failed to load tailoring files view");
        }
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
        File writtenFile = null;

        try {
            List<DiskFileItem> items = MultipartRequestUtil.parseMultipartRequest(request);
            String rawName = MultipartRequestUtil.getRequiredString(items, "name");
            DiskFileItem fileItem = MultipartRequestUtil.getRequiredFileItem(items, "tailoring_file");
            Optional<String> description = MultipartRequestUtil.findStringParam(items, "description");
            // We do this FIRST. If it fails, the DB is never touched, so no "zombie" records.
            writtenFile = writeNewTailoringFile(user, rawName, fileItem);

            TailoringFile tailoringFile = new TailoringFile(rawName, writtenFile.getName());
            tailoringFile.setOrg(user.getOrg());
            description.ifPresent(tailoringFile::setDescription);
            ScapFactory.saveTailoringFile(tailoringFile);

            return result(response, ResultJson.success());

        } catch (Exception e) {
            // Rollback using FileUtils
            if (writtenFile != null) {
                FileUtils.deleteFile(writtenFile.toPath());
            }
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
        try {
        int tailoringFileId  = Integer.parseInt(req.params("id"));
        Optional<TailoringFile> fileOpt =
                ScapFactory.lookupTailoringFileByIdAndOrg(tailoringFileId, user.getOrg());
        if (fileOpt.isEmpty()) {
            res.redirect("/rhn/manager/audit/scap/tailoring-file/create");
            return null;
        }
        TailoringFile tailoringFile = fileOpt.get();
        Map<String, Object> jsData = new HashMap<>();
        jsData.put("name", tailoringFile.getName());
        jsData.put("id", tailoringFileId);
        jsData.put("description", tailoringFile.getDescription());
        jsData.put("tailoringFileName", tailoringFile.getDisplayFileName());
        jsData.put("isUpdate", true);

        Map<String, Object> model = new HashMap<>();
        model.put("tailoringFileDataJson", Json.GSON.toJson(jsData));
        return new ModelAndView(model, "templates/audit/create-tailoring-file.jade");
        } catch (Exception e) {
            LOG.error("Failed to load update tailoring file view", e);
            throw halt(500, "Failed to load update tailoring file view");
        }
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
        File newWrittenFile = null;
        File oldFileToDelete = null;

        try {
            // 1. Parse
            List<DiskFileItem> items = MultipartRequestUtil.parseMultipartRequest(request);
            Integer id = MultipartRequestUtil.getRequiredInt(items, "id");
            String name = MultipartRequestUtil.getRequiredString(items, "name");
            Optional<String> description = MultipartRequestUtil.findStringParam(items, "description");
            TailoringFile tailoringFile = ScapFactory.lookupTailoringFileByIdAndOrg(id, user.getOrg())
                    .orElseThrow(() -> new IllegalArgumentException("Tailoring file not found"));

            oldFileToDelete = new File(TAILORING_FILES_DIR, tailoringFile.getFileName());

            Optional<DiskFileItem> fileItem = MultipartRequestUtil.findFileItem(items, "tailoring_file");
            if (fileItem.isPresent()) {
                 newWrittenFile = writeNewTailoringFile(user, name, fileItem.get());
                 tailoringFile.setFileName(newWrittenFile.getName());
            }

            tailoringFile.setName(name);
            description.ifPresent(tailoringFile::setDescription);
            ScapFactory.saveTailoringFile(tailoringFile);

            if (newWrittenFile != null) {
                FileUtils.deleteFile(oldFileToDelete.toPath());
            }

            return result(response, ResultJson.success());

        } catch (Exception e) {
            // Rollback: Delete the new file if it was created
            if (newWrittenFile != null) {
                FileUtils.deleteFile(newWrittenFile.toPath());
            }
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
    public String deleteTailoringFile(Request req, Response res, User user) {
        try {
            List<Long> ids = Arrays.asList(GSON.fromJson(req.body(), Long[].class));
            List<TailoringFile> tailoringFiles =
                    ScapFactory.lookupTailoringFilesByIds(ids, user.getOrg());
            
            if (tailoringFiles.size() < ids.size()) {
                return json(res, HttpStatus.SC_NOT_FOUND,
                        ResultJson.error("One or more tailoring files not found"), new TypeToken<>() { });
            }

            int deletedCount = 0;
            for (TailoringFile file : tailoringFiles) {
                ScapFactory.deleteTailoringFile(file);
                Path filePath = Paths.get(TAILORING_FILES_DIR, file.getFileName());
                FileUtils.deleteFile(filePath);
                deletedCount++;
            }
            
            return result(res, ResultJson.success(deletedCount));
        }
        catch (JsonParseException e) {
            LOG.error("Invalid JSON in delete tailoring file request", e);
            return json(res, HttpStatus.SC_BAD_REQUEST,
                    ResultJson.error("Invalid request format"), new TypeToken<>() { });
        }
        catch (Exception e) {
            LOG.error("Error deleting tailoring files", e);
            return json(res, HttpStatus.SC_INTERNAL_SERVER_ERROR,
                    ResultJson.error("Failed to delete tailoring files: " + e.getMessage()), new TypeToken<>() { });
        }
    }

    /**
     * Write a new tailoring file to disk
     * @param user the user
     * @param rawName the raw name of the file
     * @param item the file item
     * @return the file
     * @throws Exception if the file cannot be written
     */
    private File writeNewTailoringFile(User user, String rawName, DiskFileItem item) throws Exception {
        Files.createDirectories(Paths.get(TAILORING_FILES_DIR));
        String safeName = sanitizeFileName(rawName);
        String uniqueFilename = generateUniqueFileName(
                user.getOrg().getId(), 
                safeName,
                item.getName()
        );

        Path safePath = FileUtils.validateCanonicalPath(TAILORING_FILES_DIR, uniqueFilename);
        try (var in = item.getInputStream()) {
            Files.copy(in, safePath, StandardCopyOption.REPLACE_EXISTING);
        }
        return safePath.toFile();
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
        try {
            SelectMode m = ModeFactory.getMode("scap_queries", "all_policies_compliance");
            Map<String, Object> params = new HashMap<>();
            params.put("user_id", user.getId());
            params.put("org_id", user.getOrg().getId());
            
            List<ScapPolicyComplianceSummary> policies = m.execute(params);
            Map<String, Object> data = new HashMap<>();
            data.put("scapPolicies", GSON.toJson(policies));

            return new ModelAndView(data, "templates/audit/list-scap-policies.jade");
        } catch (Exception e) {
            LOG.error("Failed to load SCAP policies view", e);
            throw halt(500, "Failed to load SCAP policies view");
        }
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
        try {
            // Pass null for policy to indicate "Create Mode"
            Map<String, Object> pageData = buildPolicyPageData(user, null, false);

            Map<String, Object> model = new HashMap<>();
            model.put("scapPolicyPageDataJson", GSON.toJson(pageData));

            return new ModelAndView(model, "templates/audit/create-scap-policy.jade");
        } catch (Exception e) {
            LOG.error("Failed to load create SCAP policy view", e);
            throw halt(500, "Failed to load create SCAP policy view");
        }
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
        try {
            Integer policyId = Integer.parseInt(req.params("id"));
            ScapPolicy policy = ScapFactory.lookupScapPolicyByIdAndOrg(policyId, user.getOrg())
                    .orElse(null);

            if (policy == null) {
                res.redirect("/rhn/manager/audit/scap/policies");
                return null;
            }

            // Reuse helper: policy exists, isReadOnly = false
            Map<String, Object> pageData = buildPolicyPageData(user, policy, false);

            Map<String, Object> model = new HashMap<>();
            model.put("scapPolicyPageDataJson", GSON.toJson(pageData));

            return new ModelAndView(model, "templates/audit/create-scap-policy.jade");
        } catch (Exception e) {
            LOG.error("Failed to load edit SCAP policy view", e);
            throw halt(500, "Failed to load edit SCAP policy view");
        }
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
        try {
            Integer policyId = Integer.parseInt(req.params("id"));
            ScapPolicy policy = ScapFactory.lookupScapPolicyByIdAndOrg(policyId, user.getOrg())
                    .orElse(null);

            if (policy == null) {
                res.redirect("/rhn/manager/audit/scap/policies");
                return null;
            }

            // Reuse helper: policy exists, isReadOnly = TRUE
            Map<String, Object> pageData = buildPolicyPageData(user, policy, true);

            Map<String, Object> model = new HashMap<>();
            model.put("scapPolicyPageDataJson", GSON.toJson(pageData));

            return new ModelAndView(model, "templates/audit/scap-policy-details.jade");
        } catch (Exception e) {
            LOG.error("Failed to load SCAP policy details view", e);
            throw halt(500, "Failed to load SCAP policy details view");
        }
    }

    /**
     * Shared helper to build the page data for Create/Edit/Detail views.
     *
     * @param user the authorized user
     * @param policy the policy to display, or null for create mode
     * @param isReadOnly whether the view should be in read-only mode
     * @return the page data map
     */
    private Map<String, Object> buildPolicyPageData(User user, ScapPolicy policy, boolean isReadOnly) {
        Map<String, Object> pageData = new HashMap<>();
        
        pageData.put("isEditMode", policy != null && !isReadOnly);
        pageData.put("isReadOnly", isReadOnly);
        pageData.put("scapContentList", getScapContentList(user));
        pageData.put("tailoringFiles", getTailoringFilesList(user));
        if (policy != null) {
            Map<String, Object> policyData = new HashMap<>();
            policyData.put("id", policy.getId());
            policyData.put("policyName", policy.getPolicyName());
            policyData.put("description", policy.getDescription());
            policyData.put("scapContentId", policy.getScapContent() != null ? policy.getScapContent().getId() : null);
            policyData.put("xccdfProfileId", policy.getXccdfProfileId());
            policyData.put("tailoringProfileId", policy.getTailoringProfileId());
            policyData.put("ovalFiles", policy.getOvalFiles());
            policyData.put("advancedArgs", policy.getAdvancedArgs());
            policyData.put("fetchRemoteResources", policy.getFetchRemoteResources());
            if (policy.getTailoringFile() != null) {
                policyData.put("tailoringFile", policy.getTailoringFile().getId());
                policyData.put("tailoringFileName", policy.getTailoringFile().getDisplayFileName());
            }
            pageData.put("policyData", policyData);
            pageData.put("policyId", policy.getId()); // Convenient top-level ID
        } else {
            // Create Mode
            pageData.put("policyData", null);
        }

        return pageData;
    }

    /**
     * API endpoint to get SCAP policy details as JSON
     * @param req the request
     * @param res the response
     * @param user the user
     * @return JSON string with policy details
     */
    /**
     * Helper method to retrieve profile title from a SCAP file
     * @param baseDirectory the base directory (SCAP_CONTENT_DIR or TAILORING_FILES_DIR)
     * @param fileName the file name
     * @param profileId the profile ID to look up
     * @return the profile title, or the profileId if not found
     */
    private String getProfileTitle(String baseDirectory, String fileName, String profileId) {
        try {
            Path safePath = FileUtils.validateCanonicalPath(baseDirectory, fileName);
            File targetFile = safePath.toFile();
            
            if (targetFile.exists()) {
                BenchMark benchmark = ScapManager.getProfileList(targetFile);
                return benchmark.getProfiles().stream()
                        .filter(p -> profileId.equals(p.getId()))
                        .map(Profile::getTitle)
                        .findFirst()
                        .orElse(profileId);
            }
        } catch (Exception e) {
            LOG.warn("Could not retrieve profile title for profile ID: {}", profileId, e);
        }
        return profileId;
    }

    public String getScapPolicyDetails(Request req, Response res, User user) {
        Integer policyId = Integer.parseInt(req.params("id"));

        ScapPolicy policy = ScapFactory.lookupScapPolicyByIdAndOrg(policyId, user.getOrg())
            .orElseThrow(() -> new IllegalArgumentException("Policy not found"));

        return json(res, convertPolicyToResponseDto(policy));
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
            ScapPolicyJson policyJson = GSON.fromJson(req.body(), ScapPolicyJson.class);

            String validationError = validatePolicyFields(policyJson);
            if (validationError != null) {
                return result(res, ResultJson.error(validationError));
            }

            ScapPolicy scapPolicy = new ScapPolicy();
            scapPolicy.setOrg(user.getOrg());

            updatePolicyFromDto(scapPolicy, policyJson, user);
            
            ScapFactory.saveScapPolicy(scapPolicy);

            return result(res, ResultJson.success(scapPolicy.getId()));
        } catch (IllegalArgumentException e) {
            return result(res, ResultJson.error(e.getMessage()));
        } catch (Exception e) {
            LOG.error("Error creating SCAP policy", e);
            return result(res, ResultJson.error("Failed to create SCAP policy: " + e.getMessage()));
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
            ScapPolicyJson policyJson = GSON.fromJson(req.body(), ScapPolicyJson.class);

            if (policyJson.getId() == null) {
                return result(res, ResultJson.error("Policy ID is required"));
            }

            String validationError = validatePolicyFields(policyJson);
            if (validationError != null) {
                return result(res, ResultJson.error(validationError));
            }

            ScapPolicy scapPolicy = ScapFactory.lookupScapPolicyByIdAndOrg(policyJson.getId(), user.getOrg())
                    .orElseThrow(() -> new IllegalArgumentException("SCAP policy not found"));

            updatePolicyFromDto(scapPolicy, policyJson, user);
            
            ScapFactory.saveScapPolicy(scapPolicy);

            return result(res, ResultJson.success(scapPolicy.getId()));
        } catch (IllegalArgumentException e) {
            return result(res, ResultJson.error(e.getMessage()));
        } catch (Exception e) {
            LOG.error("Error updating SCAP policy", e);
            return result(res, ResultJson.error("Failed to update SCAP policy: " + e.getMessage()));
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
            Integer[] policyIds = GSON.fromJson(req.body(), Integer[].class);
            List<Integer> ids = Arrays.asList(policyIds);

            List<ScapPolicy> scapPolicies = ScapFactory.lookupScapPoliciesByIds(ids, user.getOrg());

            if (scapPolicies.size() < ids.size()) {
                return result(res, ResultJson.error("One or more SCAP policies not found"));
            }

            int deletedCount = 0;
            for (ScapPolicy policy : scapPolicies) {
                ScapFactory.deleteScapPolicy(policy);
                deletedCount++;
            }

            return result(res, ResultJson.success(deletedCount));
        } catch (IllegalArgumentException e) {
            return result(res, ResultJson.error(e.getMessage()));
        } catch (Exception e) {
            LOG.error("Error deleting SCAP policies", e);
            return result(res, ResultJson.error("Failed to delete SCAP policies: " + e.getMessage()));
        }
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
     * Resolves the profile title from the SCAP content XML file.
     * @param dir the directory containing the SCAP content
     * @param fileName the filename of the SCAP content
     * @param profileId the profile ID
     * @return the profile title
     */
    private String resolveProfileTitle(String dir, String fileName, String profileId) {
        if (profileId == null || profileId.isEmpty() || fileName == null) {
            return null;
        }
        return getProfileTitle(dir, fileName, profileId);
    }
    /**
     * Converts a ScapPolicy entity to a response DTO
     * @param policy the ScapPolicy entity
     * @return the ScapPolicyResponseJson
     */
    private ScapPolicyResponseJson convertPolicyToResponseDto(ScapPolicy policy) {
        String xccdfFile = (policy.getScapContent() != null) ? policy.getScapContent().getXccdfFileName() : null;
        String xccdfTitle = resolveProfileTitle(SCAP_CONTENT_DIR, xccdfFile, policy.getXccdfProfileId());
        String tailoringFile = (policy.getTailoringFile() != null) ? policy.getTailoringFile().getFileName() : null;
        String tailoringTitle = resolveProfileTitle(TAILORING_FILES_DIR, tailoringFile, policy.getTailoringProfileId());
        return new ScapPolicyResponseJson(policy, xccdfTitle, tailoringTitle);
    }
    /**
     * Updates a ScapPolicy entity from the JSON DTO.
     * Centralizes all "DTO -> Entity" mapping logic.
     */
    private void updatePolicyFromDto(ScapPolicy policy, ScapPolicyJson json, User user) {
        // Basic fields - direct assignment (frontend ensures proper values)
        policy.setPolicyName(json.getPolicyName());
        policy.setDescription(json.getDescription());
        policy.setXccdfProfileId(json.getXccdfProfileId());
        policy.setOvalFiles(json.getOvalFiles());
        policy.setAdvancedArgs(json.getAdvancedArgs());
        policy.setTailoringProfileId(json.getTailoringProfileId());
        policy.setFetchRemoteResources(json.getFetchRemoteResources());

        // SCAP Content (Load from DB if provided)
        // If scapContentId is null, we keep the existing SCAP content.
        // SCAP content is required for a policy, so we don't allow clearing it via update.
        if (json.getScapContentId() != null) {
             ScapContent content = ScapFactory.lookupScapContentById(json.getScapContentId())
                    .orElseThrow(() -> new IllegalArgumentException(
                        "SCAP Content not found for ID: " + json.getScapContentId()));
             policy.setScapContent(content);
        }

        if (json.getTailoringFile() == null || json.getTailoringFile().isEmpty()) {
            policy.setTailoringFile(null);
        } else {
            int tfId = Integer.parseInt(json.getTailoringFile());
            TailoringFile tf = ScapFactory.lookupTailoringFileByIdAndOrg(tfId, user.getOrg())
                    .orElseThrow(() -> new IllegalArgumentException("Tailoring File not found"));
            policy.setTailoringFile(tf);
        }
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
            AuditScanScheduleJson reqData = GSON.fromJson(req.body(), AuditScanScheduleJson.class);

            String validationError = reqData.validate();
            if (validationError != null) {
                return result(res, ResultJson.error(validationError));
            }

            Date earliest = reqData.getEarliest()
                    .map(ldt -> Date.from(ldt.atZone(java.time.ZoneId.systemDefault()).toInstant()))
                    .orElse(new Date());

            // Resolve SCAP content filename
            String dataStreamName = reqData.getDataStreamName();
            if (reqData.getScapContentId() != null) {
                ScapContent content = ScapFactory.lookupScapContentById(reqData.getScapContentId())
                        .orElseThrow(() -> new IllegalArgumentException("SCAP content not found"));
                dataStreamName = content.getDataStreamFileName();
            }

            // Resolve tailoring file filename (if provided)
            String tailoringFileName = reqData.getTailoringFile();
            if (reqData.getTailoringFileId() != null) {
                TailoringFile tf = ScapFactory.lookupTailoringFileByIdAndOrg(
                        reqData.getTailoringFileId().intValue(), user.getOrg())
                        .orElseThrow(() -> new IllegalArgumentException("Tailoring file not found"));
                tailoringFileName = tf.getFileName();
            }

            ScapAction action = ActionManager.scheduleXccdfEval(
                    user,
                    reqData.getIds(),
                    dataStreamName,
                    reqData.buildOscapParameters(tailoringFileName),
                    reqData.getOvalFiles(),
                    earliest,
                    reqData.getPolicyId()
            );

            return json(res, action.getId());
        } catch (TaskomaticApiException e) {
            LOG.error("Taskomatic API error scheduling SCAP scan", e);
            return result(res, ResultJson.error("Failed to schedule SCAP scan: Taskomatic is down"));
        } catch (IllegalArgumentException e) {
            return result(res, ResultJson.error(e.getMessage()));
        } catch (Exception e) {
            LOG.error("Error scheduling SCAP scan", e);
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
        try {
            List<DiskFileItem> items = MultipartRequestUtil.parseMultipartRequest(req);
            return handleScapContentUpload(items, res, user, null);
        } catch (FileUploadException e) {
            throw new RuntimeException(e);
        }
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
            List<DiskFileItem> items = MultipartRequestUtil.parseMultipartRequest(req);
            String idParam = MultipartRequestUtil.findStringParam(items, "id")
              .orElseThrow(() ->
                new IllegalArgumentException("ID parameter missing"));
            Optional<ScapContent> scapContent = ScapFactory.lookupScapContentById(Long.valueOf(idParam));

            if (scapContent.isEmpty()) {
                return result(res, ResultJson.error("SCAP content not found"));
            }

            return handleScapContentUpload(items, res, user, scapContent.get());
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
        try {
            List<Integer> ids = GSON.fromJson(req.body(), new TypeToken<List<Integer>>() { }.getType());
            List<ScapContent> scapContentList = ScapFactory.lookupScapContentByIds(
                    ids.stream().map(Long::valueOf).collect(Collectors.toList()));

            if (scapContentList.size() < ids.size()) {
                return json(res, HttpStatus.SC_NOT_FOUND,
                        ResultJson.error("One or more SCAP content items not found"), new TypeToken<>() { });
            }

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
        catch (JsonParseException e) {
            LOG.error("Invalid JSON in delete SCAP content request", e);
            return json(res, HttpStatus.SC_BAD_REQUEST,
                    ResultJson.error("Invalid request format"), new TypeToken<>() { });
        }
        catch (Exception e) {
            LOG.error("Error deleting SCAP content", e);
            return json(res, HttpStatus.SC_INTERNAL_SERVER_ERROR,
                    ResultJson.error("Failed to delete SCAP content: " + e.getMessage()), new TypeToken<>() { });
        }
    }

    /**
     * Handles SCAP content file upload for create and update operations
     * @param items pre-parsed multipart request items
     * @param res the response object
     * @param user the authorized user
     * @param existingContent existing content for update, null for create
     * @return the result as JSON
     */
    private String handleScapContentUpload(List<DiskFileItem> items, Response res, User user, ScapContent existingContent) {
        File writtenDsFile = null;
        File writtenXccdfFile = null;

        try {
            String name = MultipartRequestUtil.getRequiredString(items, "name");
            Optional<String> description = MultipartRequestUtil.findStringParam(items, "description");
            Optional<DiskFileItem> scapItemOpt = MultipartRequestUtil.findFileItem(items, "scapFile");
            Optional<DiskFileItem> xccdfItemOpt = MultipartRequestUtil.findFileItem(items, "xccdfFile");

            if (existingContent == null) {
                if (scapItemOpt.isEmpty()) return result(res, ResultJson.error("DataStream file is required"));
                if (xccdfItemOpt.isEmpty()) return result(res, ResultJson.error("XCCDF file is required"));
            }

            if (scapItemOpt.isPresent() && !scapItemOpt.get().getName().toLowerCase().endsWith("-ds.xml")) {
                return result(res, ResultJson.error("DataStream file must end with '-ds.xml'"));
            }
            if (xccdfItemOpt.isPresent() && !xccdfItemOpt.get().getName().toLowerCase().endsWith("-xccdf.xml")) {
                return result(res, ResultJson.error("XCCDF file must end with '-xccdf.xml'"));
            }

            String dsName = scapItemOpt.map(DiskFileItem::getName)
                    .orElse(existingContent != null ? existingContent.getDataStreamFileName() : "");
            String xccdfName = xccdfItemOpt.map(DiskFileItem::getName)
                    .orElse(existingContent != null ? existingContent.getXccdfFileName() : "");
            if (!dsName.isEmpty() && !xccdfName.isEmpty()) {
                String dsBase = dsName.replace("-ds.xml", "");
                String xccdfBase = xccdfName.replace("-xccdf.xml", "");
                if (!dsBase.equals(xccdfBase)) {
                    return result(res, ResultJson.error(
                        "DataStream and XCCDF files must share the same base name. " +
                        "DataStream: '" + dsBase + "', XCCDF: '" + xccdfBase + "'"
                    ));
                }
            }

            ScapContent scapContent = existingContent != null ? existingContent : new ScapContent();
            scapContent.setName(name.trim());
            description.ifPresent(scapContent::setDescription);
            
            Files.createDirectories(Paths.get(SCAP_CONTENT_DIR));

            if (scapItemOpt.isPresent()) {
                Path targetPath = FileUtils.validateCanonicalPath(SCAP_CONTENT_DIR, dsName);
                Files.copy(scapItemOpt.get().getInputStream(), targetPath, StandardCopyOption.REPLACE_EXISTING);
                
                writtenDsFile = targetPath.toFile();
                scapContent.setDataStreamFileName(dsName);
            }

            if (xccdfItemOpt.isPresent()) {
                Path targetPath = FileUtils.validateCanonicalPath(SCAP_CONTENT_DIR, xccdfName);
                Files.copy(xccdfItemOpt.get().getInputStream(), targetPath, StandardCopyOption.REPLACE_EXISTING);
                
                writtenXccdfFile = targetPath.toFile();
                scapContent.setXccdfFileName(xccdfName);
            }

            ScapFactory.saveScapContent(scapContent);

            return result(res, ResultJson.success());

        } catch (Exception e) {
            LOG.error("Error handling SCAP content upload", e);
            
            if (writtenDsFile != null) FileUtils.deleteFile(writtenDsFile.toPath());
            if (writtenXccdfFile != null) FileUtils.deleteFile(writtenXccdfFile.toPath());
            
            return handleTailoringFileException(res, e, existingContent == null ? "creating" : "updating");
        }
    }

    /**
     * Builds common schedule page data (SCAP content, tailoring files, policies)
     * @param user the authorized user
     * @return map with common data for schedule scan pages
     */
    private Map<String, Object> buildSchedulePageData(User user) {
        Map<String, Object> data = new HashMap<>();
        
        // SCAP content list
        data.put("scapContentList", getScapContentList(user));
        
        // Tailoring files
        data.put("tailoringFiles", getTailoringFilesList(user));
        
        // SCAP policies (ID and name only for dropdown)
        List<ScapPolicy> scapPolicies = ScapFactory.listScapPolicies(user.getOrg());
        List<ScapPolicyJson> scapPoliciesDto = scapPolicies.stream()
            .map(policy -> {
                ScapPolicyJson dto = new ScapPolicyJson();
                dto.setId(policy.getId());
                dto.setPolicyName(policy.getPolicyName());
                return dto;
            })
            .collect(Collectors.toList());
        data.put("scapPolicies", scapPoliciesDto);
        
        return data;
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
        Map<String, Object> jsData = buildSchedulePageData(user);
        jsData.put("profileId", server.getId());
        jsData.put("serverId", server.getId());
        jsData.put("entityType", "server");

        Map<String, Object> data = new HashMap<>(); 
        data.put("scheduleDataJson", Json.GSON.toJson(jsData));

        return new ModelAndView(data, "templates/minion/schedule-scap-scan.jade");
    }

    /**
     * Processes a GET request to get a list of scap configurations for SSM
     *
     * @param request  the request object
     * @param response  the response object
     * @param user the authorized user
     * @return the result JSON object
     */
    private ModelAndView scheduleAuditScanSsmView(Request request, Response response, User user) {
        Map<String, Object> jsData = buildSchedulePageData(user);
        jsData.put("entityType", "ssm");
        
        // Get systems in SSM for minions list
        List<Long> sids = SsmManager.listServerIds(user);
        List<Server> systems = ServerFactory.lookupByIdsAndOrg(new HashSet<>(sids), user.getOrg());
        
        List<Map<String, Object>> minionList = systems.stream().map(s -> {
            Map<String, Object> m = new HashMap<>();
            m.put("id", s.getId());
            m.put("name", s.getName());
            return m;
        }).collect(Collectors.toList());

        Map<String, Object> data = new HashMap<>();
        data.put("scheduleDataJson", Json.GSON.toJson(jsData));
        data.put("minions", Json.GSON.toJson(minionList));
        data.put("tabs", com.suse.manager.webui.utils.ViewHelper.getInstance()
                .renderNavigationMenu(request, "/WEB-INF/nav/ssm.xml"));

        return new ModelAndView(data, "templates/ssm/schedule-scap-scan.jade");
    }

    /**
     * Returns a view to display create form
     *
     * @param req the request object
     * @param res the response object
     * @param user the authorized user
     * @return the model and view
     */
    public ModelAndView createRuleResultView(Request req, Response res, User user, Server server) {

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
        data.put("parentScanUrl","/rhn/systems/details/audit/XccdfDetails.do?sid="+serverId+ "&xid="+ruleResult.getTestResult().getId() );
        data.put("parentScanProfile",ruleResult.getTestResult().getIdentifier());
        data.put("remediation", StringEscapeUtils.escapeJson(remediation));
        data.put("benchmarkId", ruleResult.getTestResult().getBenchmark().getIdentifier());
        return new ModelAndView(data, "templates/minion/rule-result-detail.jade");
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
     * Returns a list of available SCAP content from the database
     * @return List of SCAP content DTOs
     */
    private List<ScapContentJson> getScapContentList(User user) {
        return ScapFactory.listScapContent()
                .stream()
                .map(content -> new ScapContentJson(
                    content.getId(),
                    content.getName(),
                    content.getDataStreamFileName(),
                    content.getXccdfFileName(),
                    content.getDescription()
                ))
                .sorted((a, b) -> a.getName().compareToIgnoreCase(b.getName()))
                .collect(Collectors.toList());
    }

    /**
     * Returns a list of available tailoring files from the database
     * @param user the authorized user
     * @return List of tailoring file DTOs
     */
    private List<TailoringFileJson> getTailoringFilesList(User user) {
        return ScapFactory.listTailoringFiles(user.getOrg())
                .stream()
                .map(file -> new TailoringFileJson(
                    file.getId(),
                    file.getName(),
                    file.getFileName(),
                    file.getDisplayFileName(),
                    file.getDescription()
                ))
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

    public String getProfileList(Request req, Response res, User user) {
        String type = req.params("type");
        String idStr = req.params("id");

        try {
            String fileName;
            String baseDirectory;
            if ("dataStream".equalsIgnoreCase(type)) {
                long id = Long.parseLong(idStr);
                ScapContent content = ScapFactory.lookupScapContentById(id)
                        .orElseThrow(() -> new IllegalArgumentException("SCAP content not found"));
                
                fileName = content.getXccdfFileName();
                baseDirectory = SCAP_CONTENT_DIR;

            } else if ("tailoringFile".equalsIgnoreCase(type)) {
                int id = Integer.parseInt(idStr);
                TailoringFile tailoring = ScapFactory.lookupTailoringFileByIdAndOrg(id, user.getOrg())
                        .orElseThrow(() -> new IllegalArgumentException("Tailoring file not found"));
                
                fileName = tailoring.getFileName();
                baseDirectory = TAILORING_FILES_DIR;

            } else {
                return json(res, HttpStatus.SC_BAD_REQUEST,
                        ResultJson.error("Invalid content type: " + type), new TypeToken<>() {});
            }
    
            Path safePath = FileUtils.validateCanonicalPath(baseDirectory, fileName);
            File targetFile = safePath.toFile();

            if (!targetFile.exists()) {
                LOG.error("Physical file missing: {}", targetFile.getAbsolutePath());
                return json(res, HttpStatus.SC_NOT_FOUND,
                        ResultJson.error("Content file missing on server"), new TypeToken<>() {});
            }

            BenchMark result = ScapManager.getProfileList(targetFile);
            return json(res, result.getProfiles(), new TypeToken<>() { });

        } catch (IllegalArgumentException e) {
            return json(res, HttpStatus.SC_NOT_FOUND,
                    ResultJson.error(e.getMessage()), new TypeToken<>() {});
        } catch (Exception e) {
            LOG.error("Error reading profile list", e);
            return json(res, HttpStatus.SC_INTERNAL_SERVER_ERROR,
                    ResultJson.error("Server error reading profiles"), new TypeToken<>() {});
        }
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

        CustomRemediationJson data;
        try {
            data = GSON.fromJson(request.body(), CustomRemediationJson.class);
        } catch (JsonParseException e) {
            return json(response, HttpStatus.SC_BAD_REQUEST,
                    ResultJson.error("Invalid JSON request body"), new TypeToken<>() { });
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
        try {
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
                return result(response, ResultJson.success("Custom remediation deleted successfully"));
            }

            return json(response, HttpStatus.SC_NOT_FOUND,
                    ResultJson.error("No custom remediation found"), new TypeToken<>() { });
        }
        catch (Exception e) {
            LOG.error("Error deleting custom remediation", e);
            return json(response, HttpStatus.SC_INTERNAL_SERVER_ERROR,
                    ResultJson.error("Failed to delete custom remediation: " + e.getMessage()), new TypeToken<>() { });
        }
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
    private Action createBashRemediationAction(ApplyRemediationJson body, User user, Server server) {
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
     * This creates an ApplyStatesAction that applies the scap_beta.remediation state
     * with the remediation content passed via pillar data.
     *
     * @param body the remediation request containing Salt state content
     * @param user the user scheduling the action
     * @param server the target server (must be a Salt minion)
     * @return the created and saved ApplyStatesAction
     */
    private Action createSaltRemediationAction(ApplyRemediationJson body, User user, Server server) {
        // Pass Salt state content via pillar data
        Map<String, Object> pillar = new HashMap<>();
        pillar.put("scap_remediation_state", body.getRemediationContent());
        // Schedule apply states action with scap_beta.remediation state module
        ApplyStatesAction action = ActionManager.scheduleApplyStates(
                user,
                Collections.singletonList(server.getId()),
                Collections.singletonList("scap_beta.remediation"),
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
    // Helper method to prevent Path Traversal (e.g., input "..\..\windows")
    private String sanitizeFileName(String input) {
        return input.replaceAll("[^a-zA-Z0-9._-]", "_");
    }
} 
