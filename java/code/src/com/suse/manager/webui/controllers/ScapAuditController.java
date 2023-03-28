package com.suse.manager.webui.controllers;

import com.google.gson.Gson;
import com.redhat.rhn.domain.audit.ScapFactory;
import com.redhat.rhn.domain.audit.TailoringFile;
import com.redhat.rhn.domain.user.User;
import com.suse.manager.webui.controllers.utils.RequestUtil;
import com.suse.manager.webui.utils.gson.ResultJson;
import com.suse.utils.Json;
import org.apache.commons.fileupload.FileItem;
import org.apache.commons.fileupload.FileUploadException;
import org.apache.http.HttpStatus;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.apache.commons.io.IOUtils;
import spark.ModelAndView;
import spark.Request;
import spark.Response;
import spark.template.jade.JadeTemplateEngine;

import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import static com.suse.manager.webui.utils.SparkApplicationHelper.json;
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
        get("/manager/audit/scap/create-tailoring-file",
                withUserPreferences(withCsrfToken(withUser(this::createTailoringFileView))), jade);
        post("/manager/api/audit/scap/tailoring-file/create", withUser(ScapAuditController::uploadTailoringFile));
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
        List<TailoringFile> tailoringFiles = ScapFactory.lookupAllTailoringFiles();
        data.put("tailoringFiles", Json.GSON.toJson(tailoringFiles));
        data.put("tailoringFilesName", Json.GSON.toJson(tailoringFiles.stream().map(s->s.getName()).collect(Collectors.toList())));
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
        List<String> imageTypesDataFromTheServer = new ArrayList<>();
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
    public static String uploadTailoringFile(Request request, Response response, User user) {
        try {
            List<FileItem> items = RequestUtil.parseMultipartRequest(request);

            String name = RequestUtil.findStringParam(items, "name")
                    .orElseThrow(() ->
                            new IllegalArgumentException("name param missing"));
            FileItem kubeconfig = RequestUtil.findFileItem(items, "module_kubeconfig")
                    .orElseThrow(() ->
                            new IllegalArgumentException("kubeconfig param missing"));
            try (InputStream kubeconfigIn = kubeconfig.getInputStream(); FileOutputStream kubeconfigOut = new FileOutputStream("/srv/www/htdocs/pub/scap/tailoring-files/"+kubeconfig.getName())) {
                IOUtils.copy(kubeconfigIn, kubeconfigOut);
                TailoringFile tailorFile = new TailoringFile(name, kubeconfig.getName());
                ScapFactory.saveTailoringFile(tailorFile);
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

}