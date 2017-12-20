/**
 * Copyright (c) 2016 SUSE LLC
 *
 * This software is licensed to you under the GNU General Public License,
 * version 2 (GPLv2). There is NO WARRANTY for this software, express or
 * implied, including the implied warranties of MERCHANTABILITY or FITNESS
 * FOR A PARTICULAR PURPOSE. You should have received a copy of GPLv2
 * along with this software; if not, see
 * http://www.gnu.org/licenses/old-licenses/gpl-2.0.txt.
 *
 * Red Hat trademarks are not licensed under GPLv2. No permission is
 * granted to use or replicate Red Hat trademarks that are incorporated
 * in this software or its documentation.
 */
package com.suse.manager.webui.controllers;

import static spark.Spark.halt;

import java.util.Arrays;
import java.util.Date;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.regex.Pattern;

import com.suse.manager.webui.services.StaleSaltStateException;
import com.suse.manager.webui.services.SaltStateExistsException;
import com.suse.manager.webui.utils.SaltFileUtils;
import org.apache.commons.codec.digest.DigestUtils;
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
import com.suse.manager.webui.services.impl.SaltService;
import com.suse.manager.webui.utils.FlashScopeHelper;

/**
 * Controller class for the state catalog page.
 */
public class StateCatalogController {

    private static final Logger LOG = Logger.getLogger(StateCatalogController.class);

    private static final Gson GSON = new GsonBuilder()
            .registerTypeAdapter(Date.class, new ECMAScriptDateAdapter())
            .serializeNulls()
            .create();

    private static final Pattern NAME_PATTERN = Pattern.compile("[a-zA-Z0-9_]*");

    private StateCatalogController() { }

    /**
     * Show the list of states.
     * @param request the http request
     * @param response the http response
     * @param user the current user
     * @return the page that will show the list of states
     */
    public static ModelAndView list(Request request, Response response, User user) {
        Map<String, Object> data = new HashMap<>();
        data.put("info", FlashScopeHelper.flash(request));
        return new ModelAndView(data, "state-catalog/list.jade");
    }

    /**
     * Show the details page for an empty new state.
     * @param request the http request
     * @param response the http response
     * @param user the current user
     * @return the detail page
     */
    public static ModelAndView add(Request request, Response response, User user) {
        Map<String, Object> data = new HashMap<>();
        Map<String, String> stateData = new HashMap<>();
        stateData.put("action", "add");
        data.put("stateData", GSON.toJson(stateData));

        return new ModelAndView(data, "state-catalog/state.jade");
    }

    /**
     * Show the details page for an existing state and allow to edit it.
     * @param request the http request
     * @param response the http response
     * @param user the current user
     * @return the detail page
     */
    public static ModelAndView edit(Request request, Response response, User user) {
        String stateName = request.params("name");

        if (!exists(user, stateName)) {
            Spark.halt(HttpStatus.SC_NOT_FOUND); // TODO redirect to the default 404 page
            return null;
        }

        Map<String, Object> data = new HashMap<>();
        Map<String, Object> stateData = new HashMap<>();
        stateData.put("action", "edit");
        stateData.put("name", SaltFileUtils.stripExtension(stateName));
        Optional<String> content = SaltService.INSTANCE
                .getOrgStateContent(user.getOrg().getId(), stateName);

        stateData.put("content", content.orElse(""));
        stateData.put("checksum", DigestUtils.md5Hex(content.orElse("")));
        if (!content.isPresent()) {
            stateData.put("errors", Arrays.asList(
                    "'" + stateName + ".sls' was not found in " +
                    SaltService.INSTANCE
                            .getCustomStateBaseDir(user.getOrg().getId())
            ));
            LOG.warn("Content of '" + stateName + "' was not found on disk");
        }
        data.put("stateData", GSON.toJson(stateData));

        return new ModelAndView(data, "state-catalog/state.jade");
    }

    /**
     * Get the content of the state with the give name.
     * @param request the http request
     * @param response the http response
     * @param user the current user
     * @return the content of the state as a string
     */
    public static String content(Request request, Response response, User user) {
        String stateName = request.params("name");
        String content = SaltService.INSTANCE
                .getOrgStateContent(user.getOrg().getId(), stateName).orElse("");
        response.type("text/plain");
        return content;
    }

    private static boolean exists(User user, String stateName) {
        return SaltService.INSTANCE.orgStateExists(user.getOrg().getId(), stateName);
    }

    /**
     * Return the JSON data to render in the state list page.
     * @param request the http request
     * @param response the http response
     * @param user the current user
     * @return the JSON data
     */
    public static String data(Request request, Response response, User user) {
        List<String> data = SaltService.INSTANCE.getCatalogStates(user.getOrg().getId());
        data.replaceAll(e -> StringUtils.substringBeforeLast(e, "."));
        response.type("application/json");
        return GSON.toJson(data);
    }

    /**
     * Update an .sls file.
     * @param request the http request
     * @param response the http response
     * @param user the current user
     * @return the response JSON
     */
    public static String update(Request request, Response response, User user) {
        // check if name changed and if so do not allow overwriting
        String previousName = request.params("name");
        return save(request, response, user, previousName);
    }

    /**
     * Create an .sls file.
     * @param request the http request
     * @param response the http response
     * @param user the current user
     * @return the response JSON
     */
    public static String create(Request request, Response response, User user) {
        return save(request, response, user, null);
    }

    /**
     * Delete an .sls file.
     * @param request the http request
     * @param response the http response
     * @param user the current user
     * @return the response JSON
     */
    public static String delete(Request request, Response response, User user) {
        String name = request.params("name");
        try {
            SaltService.INSTANCE.deleteCustomState(user.getOrg().getId(), name);
        }
        catch (RuntimeException e) {
            LOG.error("Could not delete state " + name, e);
            halt(HttpStatus.SC_INTERNAL_SERVER_ERROR);
        }
        return ok(request, response, String.format("State '%s' deleted", name));
    }

    private static String save(Request request, Response response,
                               User user, String previousName) {
        Map<String, String> map = GSON.fromJson(request.body(), Map.class);
        String name = map.get("name");
        String content = map.get("content");
        String previousChecksum = map.get("checksum");
        List<String> errs = validateStateParams(name, content);
        if (!errs.isEmpty()) {
            return errorResponse(response, errs);
        }
        // TODO validate content?
        try {
            SaltService.INSTANCE.saveCustomState(user.getOrg().getId(), name,
                    content, previousName, previousChecksum);
        }
        catch (SaltStateExistsException e) {
            return errorResponse(response,
                    Arrays.asList("A state with the same name already exists"));
        }
        catch (StaleSaltStateException e) {
            return errorResponse(response,
                    Arrays.asList("Another user has changed this file. " +
                            "Please reload the page."));
        }
        catch (RuntimeException e) {
            LOG.error("Could not save state " + name, e);
            halt(HttpStatus.SC_INTERNAL_SERVER_ERROR);
        }
        return ok(request, response, String.format("State '%s' saved", name));
    }

    private static String ok(Request request, Response response, String message) {
        Map<String, String> json = new HashMap<>();
        json.put("url", "/rhn/manager/state-catalog");
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
        else if (name.length() > 250) {
            errs.add("Name is too long (max. 250 characters)");
        }

        if (StringUtils.isBlank(content)) {
            errs.add("Content is missing");
        }
        return errs;
    }

}
