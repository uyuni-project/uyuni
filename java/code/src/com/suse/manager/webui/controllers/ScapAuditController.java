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
import org.apache.commons.fileupload.FileItem;
import org.apache.commons.io.IOUtils;
import org.apache.http.HttpStatus;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import spark.ModelAndView;
import spark.Request;
import spark.Response;
import spark.Spark;
import spark.template.jade.JadeTemplateEngine;

import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

import static com.suse.manager.webui.utils.SparkApplicationHelper.json;
import static com.suse.manager.webui.utils.SparkApplicationHelper.withCsrfToken;
import static com.suse.manager.webui.utils.SparkApplicationHelper.withImageAdmin;
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

        post("/manager/api/audit/scap/tailoring-file/create", withUser(this::uploadTailoringFile));
        post("/manager/api/audit/scap/tailoring-file/delete", withImageAdmin(this::deleteTailoringFile));
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
        data.put("tailoringFilesName", Json.GSON.toJson(tailoringFiles.stream().map(s->s.getName()).collect(Collectors.toList())));
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

    /**
     * Upload the Tailoring file
     *
     * @param request the request
     * @param response the response
     * @param user the user
     * @return the json response
     */
    public String uploadTailoringFile(Request request, Response response, User user) {
        try {
            List<FileItem> items = RequestUtil.parseMultipartRequest(request);

            String nameParam = RequestUtil.findStringParam(items, "name")
                    .orElseThrow(() ->
                            new IllegalArgumentException("Name parameter missing"));
            FileItem tailoringFileParam = RequestUtil.findFileItem(items, "tailoring_file")
                    .orElseThrow(() ->
                            new IllegalArgumentException("Tailoring_file parameter missing"));
            try (InputStream tailoringFileIn = tailoringFileParam.getInputStream(); FileOutputStream tailoringFileOut = new FileOutputStream("/srv/www/htdocs/pub/scap/tailoring-files/"+tailoringFileParam.getName())) {
                IOUtils.copy(tailoringFileIn, tailoringFileOut);
                TailoringFile tailoringFile = new TailoringFile(nameParam, tailoringFileParam.getName());
                tailoringFile.setOrg(user.getOrg());
                ScapFactory.saveTailoringFile(tailoringFile);
                return json(response, ResultJson.success());
            }
        }
        catch (IllegalArgumentException e) {
            return json(response, HttpStatus.SC_BAD_REQUEST,
                    ResultJson.error(e.getMessage()));
        }
        catch (IOException e) {
            LOG.error("Could not upload the trailoring file", e);
            return json(response, HttpStatus.SC_INTERNAL_SERVER_ERROR,
                    ResultJson.error(e.getMessage()));
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

        data.put("tailoringFileName",tailoringFile.map(TailoringFile::getFileName).orElse(null));
        return new ModelAndView(data, "templates/audit/create-tailoring-file.jade");
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
            return json(res, ResultJson.error("not_found"));
        }

        tailoringFiles.forEach(ScapFactory::deleteTailoringFile);
        return json(res, ResultJson.success(tailoringFiles.size()));
    }

}