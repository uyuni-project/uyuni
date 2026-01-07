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

import com.redhat.rhn.domain.audit.*;
import com.redhat.rhn.domain.server.Server;
import com.redhat.rhn.domain.user.User;

import com.redhat.rhn.frontend.dto.XccdfRuleResultDto;
import com.redhat.rhn.manager.audit.ScapManager;
import com.redhat.rhn.manager.audit.scap.xml.BenchMark;
import com.suse.manager.webui.controllers.utils.RequestUtil;
import com.suse.manager.webui.utils.gson.ResultJson;
import com.suse.manager.webui.utils.gson.ScapPolicyJson;
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
import static spark.Spark.get;
import static spark.Spark.post;

public class ScapAuditController {
    private static final Logger LOG = LogManager.getLogger(ScapAuditController.class);
    private static final Gson GSON = Json.GSON;
    private static final String TAILORING_FILES_DIR = "/srv/susemanager/scap/tailoring-files/";
    private static final String SCAP_CONTENT_STANDARD_DIR = "/srv/susemanager/scap/ssg/content";


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
        //post("manager/api/audit/schedule/create", withUser(ScapAuditController::scheduleAuditScan));

        //rules

        get("/manager/audit/scap/scan/rule-result-details/:sid/:rrid",
          withCsrfToken(withDocsLocale(withUserAndServer(ScapAuditController::createRuleResultView))), jade);


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
        
        // Fetch all SCAP policies for the user's organization
        List<ScapPolicy> scapPolicies = ScapFactory.listScapPolicies(user.getOrg());
        
        // Convert policies to JSON objects with all necessary fields
        List<JsonObject> scapPoliciesJson = scapPolicies.stream()
                .map(policy -> {
                    JsonObject json = new JsonObject();
                    json.addProperty("id", policy.getId());
                    json.addProperty("policyName", policy.getPolicyName());
                    json.addProperty("dataStreamName", policy.getDataStreamName());
                    json.addProperty("xccdfProfileId", policy.getXccdfProfileId());
                    
                    // Add tailoring file information if present
                    if (policy.getTailoringFile() != null) {
                        json.addProperty("tailoringFileName", policy.getTailoringFile().getName());
                    } else {
                        json.addProperty("tailoringFileName", "");
                    }
                    
                    // Add tailoring profile ID if present
                    if (policy.getTailoringProfileId() != null) {
                        json.addProperty("tailoringFileProfileId", policy.getTailoringProfileId());
                    } else {
                        json.addProperty("tailoringFileProfileId", "");
                    }
                    
                    return json;
                })
                .collect(Collectors.toList());
        
        data.put("scapPolicies", scapPoliciesJson);
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
        JsonObject policyData = new JsonObject();
        policyData.addProperty("id", policy.getId());
        policyData.addProperty("policyName", policy.getPolicyName());
        
        if (policy.getDescription() != null) {
            policyData.addProperty("description", policy.getDescription());
        }
        
        policyData.addProperty("dataStreamName", policy.getDataStreamName());
        policyData.addProperty("xccdfProfileId", policy.getXccdfProfileId());
        
        if (policy.getTailoringFile() != null) {
            policyData.addProperty("tailoringFile", policy.getTailoringFile().getId());
            policyData.addProperty("tailoringFileName", policy.getTailoringFile().getFileName());
        }
        
        if (policy.getTailoringProfileId() != null) {
            policyData.addProperty("tailoringProfileId", policy.getTailoringProfileId());
        }
        
        if (policy.getAdvancedArgs() != null) {
            policyData.addProperty("advancedArgs", policy.getAdvancedArgs());
        }
        
        if (policy.getFetchRemoteResources() != null) {
            policyData.addProperty("fetchRemoteResources", policy.getFetchRemoteResources());
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
        JsonObject policyData = new JsonObject();
        policyData.addProperty("id", policy.getId());
        policyData.addProperty("policyName", policy.getPolicyName());
        
        if (policy.getDescription() != null) {
            policyData.addProperty("description", policy.getDescription());
        }
        
        policyData.addProperty("dataStreamName", policy.getDataStreamName());
        policyData.addProperty("xccdfProfileId", policy.getXccdfProfileId());
        
        if (policy.getTailoringFile() != null) {
            policyData.addProperty("tailoringFile", policy.getTailoringFile().getId());
            policyData.addProperty("tailoringFileName", policy.getTailoringFile().getFileName());
        }
        
        if (policy.getTailoringProfileId() != null) {
            policyData.addProperty("tailoringProfileId", policy.getTailoringProfileId());
        }
        
        if (policy.getAdvancedArgs() != null) {
            policyData.addProperty("advancedArgs", policy.getAdvancedArgs());
        }
        
        if (policy.getFetchRemoteResources() != null) {
            policyData.addProperty("fetchRemoteResources", policy.getFetchRemoteResources());
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
        
        return new ModelAndView(data, "templates/audit/create-scap-policy.jade");
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
        List<ScapContent> scapContentList = ScapFactory.listScapContent(user.getOrg());
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
        Optional<ScapContent> scapContent = ScapFactory.lookupScapContentByIdAndOrg(id, user.getOrg());
        
        if (scapContent.isPresent()) {
            Map<String, Object> contentData = new HashMap<>();
            contentData.put("id", scapContent.get().getId());
            contentData.put("name", scapContent.get().getName());
            contentData.put("description", scapContent.get().getDescription());
            contentData.put("fileName", scapContent.get().getFileName());
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
            Optional<ScapContent> scapContent = ScapFactory.lookupScapContentByIdAndOrg(Integer.parseInt(idParam), user.getOrg());

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
                ids.stream().map(Long::valueOf).collect(Collectors.toList()), user.getOrg());
        
        scapContentList.forEach(content -> {
            try {
                Path filePath = Paths.get(SCAP_CONTENT_STANDARD_DIR, content.getFileName());
                Files.deleteIfExists(filePath);
            } catch (IOException e) {
                LOG.error("Error deleting SCAP content file: {}", content.getFileName(), e);
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
            DiskFileItem tailoringFileParam = RequestUtil.findFileItem(items, "scapFile")
              .orElseThrow(() ->
                new IllegalArgumentException("Tailoring_file parameter missing"));

            ScapContent scapContent = existingContent != null ? existingContent : new ScapContent();
            scapContent.setName(name.trim());
            description.ifPresent(scapContent::setDescription);

            if (existingContent == null) {
                scapContent.setOrg(user.getOrg());
            }
            ensureDirectoryExists(SCAP_CONTENT_STANDARD_DIR);
            saveFileToDirectory(tailoringFileParam, SCAP_CONTENT_STANDARD_DIR);
            scapContent.setFileName(tailoringFileParam.getName());

            ScapFactory.saveScapContent(scapContent);
            return result(res, ResultJson.success());

        } catch (Exception e) {
            LOG.error("Error handling SCAP content upload", e);
            return handleTailoringFileException(res, e, "creating");
        }
    }

    /**
     * Processes a GET request to get a list of all image store objects of a specific type
     *
     * @param request  the request object
     * @param response  the response object
     * @param user the authorized user
     * @param user the authorized user
     * @return the result JSON object
     */
    private ModelAndView scheduleAuditScanView(Request request, Response response, User user, Server server) {
        List<String> imageTypesDataFromTheServer = new ArrayList<>();
        imageTypesDataFromTheServer.add("dockerfile");

        Map<String, Object> data = new HashMap<>();
        data.put("activationKeys", new HashMap<>());
        data.put("customDataKeys",new HashMap<>());
        data.put("imageTypesDataFromTheServer", Json.GSON.toJson(imageTypesDataFromTheServer));
        
        // Use shared method to get SCAP data streams
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
        /*Optional<XccdfRuleFix> xccdfRuleFix =
                ScapFactory.lookupRuleRemediation(ruleResult.getTestResult().getIdentifier(),
                        ruleResult.getDocumentIdref());*/
        Optional<XccdfRuleFix> xccdfRuleFix = ScapFactory.lookupRuleRemediation(ruleResult.getDocumentIdref());
        String remediation = xccdfRuleFix.map(fix -> fix.getRemediation())
          .orElse("No remediation available");


        List<String> imageTypesDataFromTheServer = new ArrayList<>();
        imageTypesDataFromTheServer.add("dockerfile");

        Map<String, Object> data = new HashMap<>();
        data.put("identifier",ruleResult.getDocumentIdref());
        data.put("result", ruleResult.getLabel());
        data.put("parentScanUrl","/rhn/systems/details/audit/XccdfDetails.do?sid="+serverId+ "&xid="+ruleResultId );
        data.put("parentScanProfile",ruleResult.getTestResult().getIdentifier());
        data.put("remediation", StringEscapeUtils.escapeJson(remediation));
        data.put("profileId", "----");
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
        String sanitizedName = tailoringFileName
            .replaceAll("[^a-zA-Z0-9-_]", "_")  // Replace non-alphanumeric chars with underscore
            .replaceAll("_{2,}", "_")            // Replace multiple underscores with single
            .toLowerCase();
        
        return orgId + "_" + sanitizedName + "_" + originalFilename;
    }

    /**
     * Reads SCAP content files from the standard directory
     * @return List of SCAP XCCDF file names
     */
    private List<String> getScapDataStreams() {
        File scapContentDir = new File(SCAP_CONTENT_STANDARD_DIR);
        if (!scapContentDir.exists() || !scapContentDir.isDirectory()) {
            LOG.warn("SCAP content directory does not exist: {}", SCAP_CONTENT_STANDARD_DIR);
            return Collections.emptyList();
        }
        
        File[] xccdfFiles = scapContentDir.listFiles((dir, name) -> name.endsWith("-xccdf.xml"));
        if (xccdfFiles == null) {
            return Collections.emptyList();
        }
        
        return Arrays.stream(xccdfFiles)
                .map(File::getName)
                .sorted()
                .collect(Collectors.toList());
    }
    /**
     * Processes a GET request to get a list of all image store objects of a specific type
     *
     * @param req  the request object
     * @param res  the response object
     * @param user the authorized user
     * @return the result JSON object
     */
    public static Object getProfileList(Request req, Response res, User user) {
        String location = SCAP_CONTENT_STANDARD_DIR;
        String contentType = req.params("type");
        String contentName = req.params("name");
        if (contentType.equalsIgnoreCase("tailoringFile")) {
            location = TAILORING_FILES_DIR;
        }
        Path dataStream = Paths.get(location).resolve(contentName);
        try {
            BenchMark result = ScapManager.getProfileList(dataStream.toFile());
            return json(res, result.getProfiles());
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
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
            if (policyJson.getPolicyName() == null || policyJson.getPolicyName().trim().isEmpty()) {
                return json(res, ResultJson.error("Policy name is required"));
            }
            if (policyJson.getDataStreamName() == null || policyJson.getDataStreamName().trim().isEmpty()) {
                return json(res, ResultJson.error("Data stream name is required"));
            }
            if (policyJson.getXccdfProfileId() == null || policyJson.getXccdfProfileId().trim().isEmpty()) {
                return json(res, ResultJson.error("XCCDF profile ID is required"));
            }

            // Create new SCAP policy
            ScapPolicy scapPolicy = new ScapPolicy(
                policyJson.getPolicyName().trim(),
                policyJson.getDataStreamName().trim(),
                policyJson.getXccdfProfileId().trim()
            );
            scapPolicy.setOrg(user.getOrg());
            
            // Set optional description
            if (policyJson.getDescription() != null && !policyJson.getDescription().trim().isEmpty()) {
                scapPolicy.setDescription(policyJson.getDescription().trim());
            }

            // Set optional tailoring file
            if (policyJson.getTailoringFile() != null && !policyJson.getTailoringFile().isEmpty()) {
                Integer tailoringId = Integer.parseInt(policyJson.getTailoringFile());
                Optional<TailoringFile> tailoringFile = ScapFactory.lookupTailoringFileByIdAndOrg(tailoringId, user.getOrg());
                tailoringFile.ifPresent(scapPolicy::setTailoringFile);
            }

            // Set optional tailoring profile
            if (policyJson.getTailoringProfileId() != null && !policyJson.getTailoringProfileId().isEmpty()) {
                scapPolicy.setTailoringProfileId(policyJson.getTailoringProfileId());
            }

            // Set advanced arguments
            if (policyJson.getAdvancedArgs() != null && !policyJson.getAdvancedArgs().trim().isEmpty()) {
                scapPolicy.setAdvancedArgs(policyJson.getAdvancedArgs().trim());
            }

            // Set fetch remote resources flag
            if (policyJson.getFetchRemoteResources() != null) {
                scapPolicy.setFetchRemoteResources(policyJson.getFetchRemoteResources());
            }

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

            Optional<ScapPolicy> scapPolicyOpt = ScapFactory.lookupScapPolicyByIdAndOrg(policyJson.getId(), user.getOrg());

            if (!scapPolicyOpt.isPresent()) {
                return json(res, ResultJson.error("SCAP policy not found"));
            }

            ScapPolicy scapPolicy = scapPolicyOpt.get();

            // Update fields
            if (policyJson.getPolicyName() != null && !policyJson.getPolicyName().trim().isEmpty()) {
                scapPolicy.setPolicyName(policyJson.getPolicyName().trim());
            }

            if (policyJson.getDescription() != null) {
                scapPolicy.setDescription(policyJson.getDescription().trim().isEmpty() ? null : policyJson.getDescription().trim());
            }

            if (policyJson.getDataStreamName() != null && !policyJson.getDataStreamName().trim().isEmpty()) {
                scapPolicy.setDataStreamName(policyJson.getDataStreamName().trim());
            }

            if (policyJson.getXccdfProfileId() != null && !policyJson.getXccdfProfileId().trim().isEmpty()) {
                scapPolicy.setXccdfProfileId(policyJson.getXccdfProfileId().trim());
            }

            if (policyJson.getTailoringFile() != null && !policyJson.getTailoringFile().isEmpty()) {
                Integer tailoringId = Integer.parseInt(policyJson.getTailoringFile());
                Optional<TailoringFile> tailoringFile = ScapFactory.lookupTailoringFileByIdAndOrg(tailoringId, user.getOrg());
                tailoringFile.ifPresent(scapPolicy::setTailoringFile);
            } else {
                scapPolicy.setTailoringFile(null);
            }

            if (policyJson.getTailoringProfileId() != null) {
                scapPolicy.setTailoringProfileId(policyJson.getTailoringProfileId().isEmpty() ? null : policyJson.getTailoringProfileId());
            }

            if (policyJson.getAdvancedArgs() != null) {
                scapPolicy.setAdvancedArgs(policyJson.getAdvancedArgs().trim().isEmpty() ? null : policyJson.getAdvancedArgs().trim());
            }

            if (policyJson.getFetchRemoteResources() != null) {
                scapPolicy.setFetchRemoteResources(policyJson.getFetchRemoteResources());
            }

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
}
