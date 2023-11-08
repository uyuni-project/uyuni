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

import com.redhat.rhn.domain.audit.ScapFactory;
import com.redhat.rhn.domain.audit.TailoringFile;
import com.redhat.rhn.domain.user.User;

import com.suse.manager.webui.controllers.utils.RequestUtil;
import com.suse.manager.webui.utils.gson.ResultJson;
import com.suse.utils.Json;

import org.apache.commons.fileupload2.core.DiskFileItem;
import org.apache.commons.fileupload2.core.FileUploadException;
import org.apache.commons.io.IOUtils;
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
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

import static com.suse.manager.webui.utils.SparkApplicationHelper.badRequest;
import static com.suse.manager.webui.utils.SparkApplicationHelper.internalServerError;
import static com.suse.manager.webui.utils.SparkApplicationHelper.notFound;
import static com.suse.manager.webui.utils.SparkApplicationHelper.result;
import static com.suse.manager.webui.utils.SparkApplicationHelper.withCsrfToken;
import static com.suse.manager.webui.utils.SparkApplicationHelper.withUser;
import static com.suse.manager.webui.utils.SparkApplicationHelper.withUserPreferences;

import static spark.Spark.get;
import static spark.Spark.post;

public class ScapAuditController {
    private static final Logger LOG = LogManager.getLogger(ScapAuditController.class);
    private static final Gson GSON = Json.GSON;

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
        List<JsonObject> collect = tailoringFiles.stream().map(this::getJsonObject).collect(Collectors.toList());

        //data.put("tailoringFiles", Json.GSON.toJson(tailoringFiles.));
        data.put("tailoringFiles", collect);
        data.put("tailoringFilesName", Json.GSON.toJson(tailoringFiles.stream()
          .map(s->s.getName()).collect(Collectors.toList())));
        return new ModelAndView(data, "templates/audit/list-tailoring-files.jade");
    }


    /**
     * Creates a JSON object for an {@link TailoringFile} instance
     *
     * @param file the TailoringFile instance
     * @return the JSON object
     */
    private  JsonObject getJsonObject(TailoringFile file) {
        JsonObject json = new JsonObject();
        json.addProperty("name", file.getName());
        json.addProperty("id", file.getId());
        json.addProperty("fileName", file.getFileName());
        return json;
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

    private static final String TAILORING_FILES_DIR = "/srv/susemanager/scap/tailoring-files/";

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

            ensureDirectoryExists();
            saveFileToDirectory(tailoringFileParam);
            TailoringFile tailoringFile = new TailoringFile(nameParam, tailoringFileParam.getName());
            tailoringFile.setOrg(user.getOrg());
            descriptionParam.ifPresent(tailoringFile::setDescription);
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
        if (!tailoringFile.isPresent()) {
            res.redirect("/rhn/manager/audit/scap/tailoring-file/create");
        }
        Map<String, Object> data = new HashMap<>();
        data.put("name", tailoringFile.map(TailoringFile::getName).orElse(null));
        data.put("id", tailoringFileId);
        data.put("description", tailoringFile.map(TailoringFile::getDescription).orElse(null));
        data.put("tailoringFileName", tailoringFile.map(TailoringFile::getFileName).orElse(null));
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

            if (!existingFile.isPresent()) {
                return notFound(response, "Tailoring file not found");
            }
            TailoringFile tailoringFile = existingFile.get();
            tailoringFile.setName(nameParam);
            descriptionParam.ifPresent(tailoringFile::setDescription);

            // Check if a new file was uploaded
            Optional<DiskFileItem> tailoringFileParam = RequestUtil.findFileItem(items, "tailoring_file");
            if (tailoringFileParam.isPresent() && tailoringFileParam.get().getSize() > 0) {
                ensureDirectoryExists();

                // Delete old file if it exists
                File oldFile = new File(TAILORING_FILES_DIR, tailoringFile.getFileName());
                if (oldFile.exists()) {
                    oldFile.delete();
                }

                saveFileToDirectory(tailoringFileParam.get());
                tailoringFile.setFileName(tailoringFileParam.get().getName());
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

    // ========== Private Helper Methods ==========
    // TODO: Consider moving these generic file utilities to a shared utility class
    //       for reuse across other controllers

    /**
     * Ensures the tailoring files directory exists
     * @throws IOException if directory creation fails
     */
    private void ensureDirectoryExists() throws IOException {
        File directory = new File(TAILORING_FILES_DIR);
        if (!directory.exists()) {
            LOG.info("Creating tailoring files directory: {}", TAILORING_FILES_DIR);
            if (!directory.mkdirs()) {
                throw new IOException("Failed to create directory: " + TAILORING_FILES_DIR);
            }
        }
    }

    /**
     * Saves a file to the tailoring files directory
     * @param fileItem the file to save
     * @throws IOException if file saving fails
     */
    private void saveFileToDirectory(DiskFileItem fileItem) throws IOException {
        File outputFile = new File(TAILORING_FILES_DIR, fileItem.getName());
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

}
