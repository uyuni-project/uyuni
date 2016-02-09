package com.suse.manager.webui.controllers;

import static spark.Spark.halt;

import java.util.Arrays;
import java.util.Date;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.regex.Pattern;

import com.suse.manager.webui.services.subscriptionmatching.StateExistsException;
import org.apache.commons.lang.StringUtils;
import org.apache.http.HttpStatus;
import org.apache.log4j.Logger;

import spark.ModelAndView;
import spark.Request;
import spark.Response;
import spark.Spark;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.redhat.rhn.domain.user.User;
import com.suse.manager.webui.services.impl.SaltAPIService;
import com.suse.manager.webui.utils.FlashScopeHelper;

/**
 * Controller class for the state catalog page.
 */
public class StateCatalogController {

    private static Logger LOG = Logger.getLogger(StateCatalogController.class);

    private static final Gson GSON = new GsonBuilder()
            .registerTypeAdapter(Date.class, new ECMAScriptDateAdapter())
            .serializeNulls()
            .create();

    private static Pattern NAME_PATTERN = Pattern.compile("[a-zA-Z0-9_]*");

    public static ModelAndView show(Request request, Response response, User user) {
        Map<String, Object> data = new HashMap<>();
        data.put("info", FlashScopeHelper.flash(request));
        return new ModelAndView(data, "state_catalog/show.jade");
    }

    public static ModelAndView add(Request request, Response response, User user) {
        Map<String, Object> data = new HashMap<>();
        Map<String, String> stateData = new HashMap<>();
        stateData.put("action", "add");
        data.put("stateData", GSON.toJson(stateData));

        return new ModelAndView(data, "state_catalog/state.jade");
    }

    public static ModelAndView edit(Request request, Response response, User user) {
        String stateName = request.params("name");

        if (!exists(user, stateName)) {
            Spark.halt(HttpStatus.SC_NOT_FOUND); // TODO redirect to the default 404 page
            return null;
        }

        Map<String, Object> data = new HashMap<>();
        Map<String, String> stateData = new HashMap<>();
        stateData.put("action", "edit");
        stateData.put("name", StringUtils.removeEnd(stateName, ".sls"));
        stateData.put("content", SaltAPIService.INSTANCE
                .getOrgStateContent(user.getOrg().getId(), stateName).orElse(""));
        data.put("stateData", GSON.toJson(stateData));

        return new ModelAndView(data, "state_catalog/state.jade");
    }

    private static boolean exists(User user, String stateName) {
        return SaltAPIService.INSTANCE.orgStateExists(user.getOrg().getId(), stateName);
    }

    public static String data(Request request, Response response, User user) {
        List<String> data = SaltAPIService.INSTANCE.getOrgStates(user.getOrg().getId());
        data.replaceAll( e -> StringUtils.substringBeforeLast(e, "."));
        response.type("application/json");
        return GSON.toJson(data);
    }

    public static String update(Request request, Response response, User user) {
        // check if name changed and if so do not allow overwriting
        String previousName = request.params("name");
        return save(request, response, user, previousName);
    }

    public static String create(Request request, Response response, User user) {
        return save(request, response, user, null);
    }

    public static String delete(Request request, Response response, User user) {
        String name = request.params("name");
        try {
            SaltAPIService.INSTANCE.deleteOrgState(user.getOrg().getId(), name);
        } catch (RuntimeException e) {
            LOG.error("Could not delete state " + name, e);
            halt(HttpStatus.SC_INTERNAL_SERVER_ERROR);
        }
        return ok(request, response, String.format("State '%s' deleted", name));
    }

    private static String save(Request request, Response response, User user, String previousName) {
        Map<String, String> map = GSON.fromJson(request.body(), Map.class);
        String name = map.get("name");
        String content = map.get("content");
        String previousChecksum = map.get("prevChecksum");
        List<String> errs = validateStateParams(name, content);
        if (!errs.isEmpty()) {
            return errorResponse(response, errs);
        }
        // TODO validate content?

        try {
            SaltAPIService.INSTANCE.storeOrgState(user.getOrg().getId(), name, previousName, previousChecksum, content);
        } catch (StateExistsException e) {
            return errorResponse(response, Arrays.asList("A state with the same name already exists"));
        } catch (RuntimeException e) {
            LOG.error("Could not save state " + name, e);
            halt(HttpStatus.SC_INTERNAL_SERVER_ERROR);
        }
        return ok(request, response, String.format("State '%s' saved", name));
    }

    private static String ok(Request request, Response response, String message) {
        Map<String, String> json = new HashMap<>();
        json.put("url", "/rhn/manager/state_catalog");
        FlashScopeHelper.flash(request, message);
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
        // only allow [a..zA..Z0..9_] in name
        if (!NAME_PATTERN.matcher(name).matches()) {
            errs.add("Name contains illegal characters");
        }
        if (StringUtils.isBlank(name)) {
            errs.add("Name is missing");
        }
        if (StringUtils.isBlank(content)) {
            errs.add("Content is missing");
        }
        return errs;
    }

}
