package com.suse.manager.webui.controllers;

import com.google.gson.Gson;
import com.google.gson.JsonObject;
import com.google.gson.JsonParseException;
import com.redhat.rhn.domain.audit.ScapFactory;
import com.redhat.rhn.domain.audit.ScapPolicy;
import com.redhat.rhn.domain.audit.TailoringFile;
import com.redhat.rhn.domain.user.User;
import com.redhat.rhn.frontend.context.Context;
import com.redhat.rhn.manager.audit.ScapManager;
import com.redhat.rhn.manager.audit.scap.xml.BenchMark;
import com.suse.manager.webui.controllers.utils.RequestUtil;
import com.suse.manager.webui.utils.ScapPolicyJson;
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

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.*;
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
    private static final String SCAP_CONTENT_STANDARD_DIR = "/srv/www/htdocs/pub/scap/ssg/content";

    /**
     * Invoked from Router. Initialize routes for SCAP audit Views.
     *
     * @param jade the Jade engine to use to render the pages
     */
    public void initRoutes(JadeTemplateEngine jade) {

        // Tailoring Files
        get("/manager/audit/scap/tailoring-files",
                withUserPreferences(withCsrfToken(withUser(this::listTailoringFilesView))), jade);
        get("/manager/audit/scap/tailoring-file/create",
                withUserPreferences(withCsrfToken(withUser(this::createTailoringFileView))), jade);
        get("/manager/audit/scap/tailoring-file/edit/:id",
                withUserPreferences(withCsrfToken(withUser(this::updateTailoringFileView))), jade);

        post("/manager/api/audit/scap/tailoring-file/create", withUser(this::uploadTailoringFile));
        post("/manager/api/audit/scap/tailoring-file/delete", withImageAdmin(this::deleteTailoringFile));

        // Scap Policies

        get("/manager/audit/scap/policies",
                withUserPreferences(withCsrfToken(withUser(this::listScapPoliciesView))), jade);
        get("/manager/audit/scap/create-scap-policy",
                withUserPreferences(withCsrfToken(withUser(this::createScapPolicyView))), jade);
        get("/manager/api/audit/scap/policy/edit/:id",
                withUserPreferences(withCsrfToken(withUser(this::updateScapPolicyView))), jade);
        get("/manager/api/audit/profiles/list/:type/:name", withUser(ScapAuditController::getProfileList));

        post("/manager/api/audit/scap/policy/create", withUser(this::createScapPolicy));
        post("/manager/api/audit/scap/policy/delete", withUser(this::deleteScapPolicy));

    }

    /**
     * Processes a GET request to get a list of all Tailoring files
     *
     * @param req  the request object
     * @param res  the response object
     * @param user the authorized user
     * @return the result JSON object
     */
    public ModelAndView listTailoringFilesView(Request req, Response res, User user) {
        Map<String, Object> data = new HashMap<>();
        List<TailoringFile> tailoringFiles = ScapFactory.listTailoringFiles(user.getOrg());
        List<JsonObject> collect = tailoringFiles.stream().map(this::convertToTailoringFileJson).collect(Collectors.toList());

        data.put("tailoringFiles", collect);
        data.put("tailoringFilesName", Json.GSON.toJson(tailoringFiles.stream().map(s -> s.getName()).collect(Collectors.toList())));
        return new ModelAndView(data, "templates/audit/list-tailoring-files.jade");
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
        json.addProperty("fileName", file.getFileName());
        return json;
    }

    /**
     * Creates a JSON object for an {@link TailoringFile} instance
     *
     * @param file the TailoringFile instance
     * @return the JSON object
     */
    private JsonObject convertToScapPolicyJson(TailoringFile file) {
        JsonObject json = new JsonObject();
        json.addProperty("name", file.getName());
        json.addProperty("id", file.getId());
        json.addProperty("fileName", file.getFileName());
        return json;
    }


    /**
     * Returns a view to display form to upload tailoring file
     *
     * @param req  the request object
     * @param res  the response object
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
     * @param request  the request
     * @param response the response
     * @param user     the user
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
            try (InputStream tailoringFileIn = tailoringFileParam.getInputStream(); FileOutputStream tailoringFileOut = new FileOutputStream("/srv/www/htdocs/pub/scap/tailoring-files/" + tailoringFileParam.getName())) {
                IOUtils.copy(tailoringFileIn, tailoringFileOut);
                TailoringFile tailoringFile = new TailoringFile(nameParam, tailoringFileParam.getName());
                tailoringFile.setOrg(user.getOrg());
                ScapFactory.saveTailoringFile(tailoringFile);
                return json(response, ResultJson.success());
            }
        } catch (IllegalArgumentException e) {
            return json(response, HttpStatus.SC_BAD_REQUEST,
                    ResultJson.error(e.getMessage()));
        } catch (IOException e) {
            LOG.error("Could not upload the trailoring file", e);
            return json(response, HttpStatus.SC_INTERNAL_SERVER_ERROR,
                    ResultJson.error(e.getMessage()));
        }
    }

    /**
     * Returns a view to display update form of tailoring file
     *
     * @param req  the request object
     * @param res  the response object
     * @param user the authorized user
     * @return the model and view
     */
    public ModelAndView updateTailoringFileView(Request req, Response res, User user) {
        Integer tailoringFileId = Integer.parseInt(req.params("id"));

        Optional<TailoringFile> tailoringFile =
                ScapFactory.lookupTailoringFileByIdAndOrg(tailoringFileId, user.getOrg());
        if (!tailoringFile.isPresent()) {
            res.redirect("/rhn/manager/audit/scap/tailoring-file/create");
        }

        Map<String, Object> data = new HashMap<>();
        data.put("name", tailoringFile.map(TailoringFile::getName).orElse(null));
        data.put("id", tailoringFileId);

        data.put("tailoringFileName", tailoringFile.map(TailoringFile::getFileName).orElse(null));
        return new ModelAndView(data, "templates/audit/create-tailoring-file.jade");
    }

    /**
     * Processes a DELETE request
     *
     * @param req  the request object
     * @param res  the response object
     * @param user the authorized user
     * @return the result JSON object
     */
    public Object deleteTailoringFile(Request req, Response res, User user) {
        List<Long> ids;
        try {
            ids = Arrays.asList(GSON.fromJson(req.body(), Long[].class));
        } catch (JsonParseException e) {
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

    /**
     * Processes a GET request to get a list of all the scap policies
     *
     * @param req  the request object
     * @param res  the response object
     * @param user the authorized user
     * @return the result JSON object
     */
    public ModelAndView listScapPoliciesView(Request req, Response res, User user) {
        Map<String, Object> data = new HashMap<>();
        List<ScapPolicy> scapPolicies = ScapFactory.listScapPolicies(user.getOrg());
        List<JsonObject> scapPoliciesJson = scapPolicies.stream()
                .map(policy -> {
                    JsonObject json = new JsonObject();
                    json.addProperty("id", policy.getId());
                    json.addProperty("policyName", policy.getPolicyName());
                    json.addProperty("dataStreamName", policy.getDataStreamName());
                    json.addProperty("xccdfProfileId", policy.getXccdfProfileId());
                    json.addProperty("tailoringFileName", policy.getTailoringFile().getName());
                    json.addProperty("tailoringFileProfileId", policy.getTailoringProfileId());
                    return json;
                })
                .collect(Collectors.toList());
        //data.put("tailoringFiles", Json.GSON.toJson(tailoringFiles.));
        data.put("scapPolicies", scapPoliciesJson);
        return new ModelAndView(data, "templates/audit/list-scap-policies.jade");
    }

    /**
     * Returns a view to display form to create the scap policy
     *
     * @param req  the request object
     * @param res  the response object
     * @param user the authorized user
     * @return the model and view
     */
    public ModelAndView createScapPolicyView(Request req, Response res, User user) {
        List<String> imageTypesDataFromTheServer = new ArrayList<>();

        final String SCAP_CONTENT_STANDARD_DIR = "/srv/www/htdocs/pub/scap/ssg/content";
        File scapContentDir = new File(SCAP_CONTENT_STANDARD_DIR);
        List<String> collect1 = Arrays.stream(scapContentDir.listFiles((dir, name) -> name.endsWith("-ds.xml")))
                .map(s -> s.getName().toUpperCase().split("-DS.XML")[0]).collect(Collectors.toList());
        List<String> collect2 = Arrays.stream(scapContentDir.listFiles((dir, name) -> name.endsWith("-xccdf.xml")))
                .map(s -> s.getName()).collect(Collectors.toList());

        Map<String, Object> data = new HashMap<>();
        data.put("scapDataStreams", Json.GSON.toJson(collect2));
        List<TailoringFile> tailoringFiles = ScapFactory.listTailoringFiles(user.getOrg());
        List<JsonObject> collect = tailoringFiles.stream().map(this::convertToTailoringFileJson).collect(Collectors.toList());
        data.put("tailoringFiles", collect);
        return new ModelAndView(data, "templates/audit/create-scap-policy.jade");
    }

    /**
     * Returns a view to display update form of Scap policy
     *
     * @param req  the request object
     * @param res  the response object
     * @param user the authorized user
     * @return the model and view
     */
    public ModelAndView updateScapPolicyView(Request req, Response res, User user) {
        Integer scapPolicyId = Integer.parseInt(req.params("id"));
        ScapPolicyJson scapPolicyJson = new ScapPolicyJson();
        Optional<ScapPolicy> scapPolicy =
                ScapFactory.lookupScapPolicyByIdAndOrg(scapPolicyId, user.getOrg());
        // Using Optional's ifPresent and orElse to handle the conversion or redirect
        scapPolicy.ifPresentOrElse(
                policy -> {
                    // Convert ScapPolicy to ScapPolicyJson and process it
                    scapPolicyJson.setPolicyName(policy.getPolicyName());
                    scapPolicyJson.setDataStreamName(policy.getDataStreamName());
                    scapPolicyJson.setXccdfProfileId(policy.getXccdfProfileId());
                    scapPolicyJson.setTailoringFile(policy.getTailoringFile().getFileName());
                    scapPolicyJson.setTailoringProfileId(policy.getTailoringProfileId());

                },
                () -> res.redirect("/rhn/manager/audit/scap/create-scap-policy") // Redirect if Optional is empty
        );
        // Start
        final String SCAP_CONTENT_STANDARD_DIR = "/srv/www/htdocs/pub/scap/ssg/content";
        File scapContentDir = new File(SCAP_CONTENT_STANDARD_DIR);


        List<String> collect2 = Arrays.stream(scapContentDir.listFiles((dir, name) -> name.endsWith("-xccdf.xml")))
                .map(s -> s.getName()).collect(Collectors.toList());
        Map<String, Object> data = new HashMap<>();
        data.put("scapDataStreams", Json.GSON.toJson(collect2));
        List<TailoringFile> tailoringFiles = ScapFactory.listTailoringFiles(user.getOrg());
        List<JsonObject> collect = tailoringFiles.stream().map(this::convertToTailoringFileJson).collect(Collectors.toList());
        data.put("tailoringFiles", collect);
        //end
        data.put("selectedPolicy", Json.GSON.toJson(scapPolicyJson));
        return new ModelAndView(data, "templates/audit/create-scap-policy.jade");
    }

    /**
     * Create the SCAP policy
     *
     * @param request  the request
     * @param response the response
     * @param user     the user
     * @return the json response
     */
    public String createScapPolicy(Request request, Response response, User user) {
        try {
            ScapPolicyJson reqData = Json.GSON.fromJson(request.body(), ScapPolicyJson.class);
            //ZoneId zoneId = Context.getCurrentContext().getTimezone().toZoneId();
            //Date earliest = Date.from(reqData.getEarliest().orElseGet(LocalDateTime::now).atZone(zoneId).toInstant());

            ZoneId zoneId = Context.getCurrentContext().getTimezone().toZoneId();
            Date earliest = Date.from(reqData.getEarliest().orElseGet(LocalDateTime::now).atZone(zoneId).toInstant());

            // ScapAction action = ActionManager.scheduleXccdfEval(user, reqData.getIds(),
            //       reqData.getDataStreamName(), params.toString(), ovalfiles, earliest);
            ScapPolicy sp = new ScapPolicy();
            sp.setPolicyName(reqData.getPolicyName());
            sp.setDataStreamName(reqData.getDataStreamName());
            sp.setXccdfProfileId(reqData.getXccdfProfileId());
            sp.setTailoringFile(ScapFactory.lookupAllTailoringFiles().get(0));
            sp.setTailoringProfileId(reqData.getTailoringProfileId());
            sp.setOrg(user.getOrg());
            ScapFactory.saveScapPolicy(sp);
            return json(response, ResultJson.success());
        } catch (JsonParseException e) {
            throw Spark.halt(HttpStatus.SC_BAD_REQUEST);
        }
    }

    /**
     * Processes a DELETE request for SCAP policy
     *
     * @param req  the request object
     * @param res  the response object
     * @param user the authorized user
     * @return the result JSON object
     */
    public Object deleteScapPolicy(Request req, Response res, User user) {
        List<Integer> ids;
        try {
            ids = Arrays.asList(GSON.fromJson(req.body(), Integer[].class));
        } catch (JsonParseException e) {
            Spark.halt(HttpStatus.SC_BAD_REQUEST);
            return null;
        }

        List<ScapPolicy> scapPolicies =
                ScapFactory.lookupScapPoliciesByIds(ids, user.getOrg());
        if (scapPolicies.size() < ids.size()) {
            return json(res, ResultJson.error("not_found"));
        }

        scapPolicies.forEach(ScapFactory::deleteScapPolicy);
        return json(res, ResultJson.success(scapPolicies.size()));
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
            location = "/srv/www/htdocs/pub/scap/tailoring-files";
        }
        Path dataStream = Paths.get(location).resolve(contentName);
        try {
            BenchMark result = ScapManager.getProfileList(dataStream.toFile());
            return json(res, result.getProfiles());
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

}