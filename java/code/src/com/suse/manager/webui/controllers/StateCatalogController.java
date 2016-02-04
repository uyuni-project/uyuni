package com.suse.manager.webui.controllers;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.redhat.rhn.common.security.CSRFTokenValidator;
import com.redhat.rhn.domain.user.User;
import com.suse.manager.webui.services.SaltStateStorageManager;
import com.suse.manager.webui.services.impl.SaltAPIService;
import com.suse.manager.webui.utils.FlashScopeHelper;
import org.apache.commons.lang.StringUtils;
import org.apache.http.HttpStatus;
import spark.ModelAndView;
import spark.Request;
import spark.Response;

import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import static spark.Spark.halt;

/**
 * Created by matei on 2/1/16.
 */
public class StateCatalogController {

    private static final Gson GSON = new GsonBuilder()
            .registerTypeAdapter(Date.class, new ECMAScriptDateAdapter())
            .serializeNulls()
            .create();

    public static ModelAndView show(Request request, Response response, User user) {
        Map<String, Object> data = new HashMap<>();

        return new ModelAndView(data, "state_catalog/show.jade");
    }

    public static ModelAndView add(Request request, Response response, User user) {
        Map<String, Object> data = new HashMap<>();
        data.put("csrf_token", CSRFTokenValidator.getToken(request.session().raw()));
        Map<String, String> stateData = new HashMap<>();
        stateData.put("action", "add");
        data.put("stateData", GSON.toJson(stateData));

        return new ModelAndView(data, "state_catalog/state.jade");
    }

    public static ModelAndView edit(Request request, Response response, User user) {
        String stateName = request.params("name");

        Map<String, Object> data = new HashMap<>();
        data.put("csrf_token", CSRFTokenValidator.getToken(request.session().raw()));
        Map<String, String> stateData = new HashMap<>();
        stateData.put("action", "edit");
        stateData.put("name", stateName);
        stateData.put("content", SaltAPIService.INSTANCE
                .getOrgStateContent(user.getOrg().getId(), stateName).orElse(""));
        data.put("stateData", GSON.toJson(stateData));

        return new ModelAndView(data, "state_catalog/state.jade");
    }

    public static String data(Request request, Response response, User user) {

        List<String> data = SaltAPIService.INSTANCE.getOrgStates(user.getOrg().getId());

        response.type("application/json");
        return GSON.toJson(data);
    }

    public static String update(Request request, Response response, User user) {
        // TODO check if name changed and it already exists to avoid overwriting
        return save(request, response, user);
    }

    public static String create(Request request, Response response, User user) {
        // TODO check if name already exists to avoid overwriting
        return save(request, response, user);
    }

    public static String delete(Request request, Response response, User user) {
        String name = request.params("name");
        try {
            SaltAPIService.INSTANCE.deleteOrgState(user.getOrg().getId(), name);
        } catch (RuntimeException e) {
            halt(HttpStatus.SC_INTERNAL_SERVER_ERROR);
        }
        return ok(response, "State deleted");
    }

    public static String save(Request request, Response response, User user) {
        Map<String, String> map = GSON.fromJson(request.body(), Map.class);
        String name = map.get("name");
        String content = map.get("content");

        List<String> errs = validateStateParams(name, content);
        if (!errs.isEmpty()) {
            return errorResponse(response, errs);
        }

        try {
            SaltAPIService.INSTANCE.storeOrgState(user.getOrg().getId(), name, content);
        } catch (RuntimeException e) {
            halt(HttpStatus.SC_INTERNAL_SERVER_ERROR);
        }
        return ok(response, "State saved");
    }

    private static String ok(Response response, String message) {
        Map<String, String> json = new HashMap<>();
        json.put("url", "/rhn/manager/state_catalog");
        json.put("message", message); // TODO put message in flash scope
        response.type("application/json");
        return GSON.toJson(json);
    }

    private static String errorResponse(Response response, List<String> errs) {
        response.type("application/json");
        response.status(HttpStatus.SC_BAD_REQUEST);
        return GSON.toJson(errs);
    }

    private static List<String> validateStateParams(String name, String content) {
        List<String> errs = new LinkedList<>();
        // TODO check file name
        // TODO sanitize content
        if (StringUtils.isBlank(name)) {
            errs.add("Name is missing");
        }
        if (StringUtils.isBlank(content)) {
            errs.add("Content is missing");
        }
        return errs;
    }

}
