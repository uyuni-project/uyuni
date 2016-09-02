/**
 * Copyright (c) 2015 SUSE LLC
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

import java.io.IOException;
import java.lang.reflect.Type;
import java.util.Arrays;
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import javax.transaction.NotSupportedException;

import org.apache.http.HttpStatus;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonElement;
import com.google.gson.JsonPrimitive;
import com.google.gson.JsonSerializationContext;
import com.google.gson.JsonSerializer;
import com.redhat.rhn.common.hibernate.LookupException;
import com.redhat.rhn.common.security.PermissionException;
import com.redhat.rhn.domain.formula.FormulaFactory;
import com.redhat.rhn.domain.org.OrgFactory;
import com.redhat.rhn.domain.server.MinionServer;
import com.redhat.rhn.domain.server.MinionServerFactory;
import com.redhat.rhn.domain.server.Server;
import com.redhat.rhn.domain.server.ServerFactory;
import com.redhat.rhn.domain.server.ServerGroup;
import com.redhat.rhn.domain.server.ServerGroupFactory;
import com.redhat.rhn.domain.user.User;
import com.redhat.rhn.manager.system.ServerGroupManager;
import com.redhat.rhn.manager.system.SystemManager;
import com.redhat.rhn.manager.token.ActivationKeyManager;
import com.suse.manager.webui.services.impl.SaltService;
import com.suse.manager.webui.utils.gson.StateTargetType;

import spark.ModelAndView;
import spark.Request;
import spark.Response;

/**
 * Controller class providing backend code for the minions page.
 */
public class MinionController {

    // Reference to the SaltService instance
    private static final SaltService SALT_SERVICE = SaltService.INSTANCE;

    private static final Gson GSON = new GsonBuilder()
            .registerTypeAdapter(Date.class, new ECMAScriptDateAdapter())
            .registerTypeAdapter(Double.class,  new JsonSerializer<Double>() {
                @Override
                public JsonElement serialize(Double src, Type type,
                            JsonSerializationContext context) {
                        if (src % 1 == 0) {
                            return new JsonPrimitive(src.intValue());
                        }
                        else {
                            return new JsonPrimitive(src);
                        }
                    }
                })
            .serializeNulls()
            .create();

    private MinionController() { }

    /**
     * Displays a list of minions.
     *
     * @param request the request object
     * @param response the response object
     * @param user the user
     * @return the ModelAndView object to render the page
     */
    public static ModelAndView list(Request request, Response response, User user) {
        Map<String, Object> data = new HashMap<>();
        data.put("pageSize", user.getPageSize());
        return new ModelAndView(data, "minion/list.jade");
    }

    /**
     * Handler for accept minion url.
     *
     * @param request the request object
     * @param response the response object
     * @return dummy string to satisfy spark
     */
    public static Object accept(Request request, Response response) {
        SALT_SERVICE.acceptKey(request.params("id"));
        response.redirect("/rhn/manager/minions");
        return "";
    }

    /**
     * Handler for delete minion url.
     *
     * @param request the request object
     * @param response the response object
     * @return dummy string to satisfy spark
     */
    public static Object destroy(Request request, Response response) {
        SALT_SERVICE.deleteKey(request.params("id"));
        response.redirect("/rhn/manager/minions");
        return "";
    }

    /**
     * Handler for reject minion url.
     *
     * @param request the request object
     * @param response the response object
     * @return dummy string to satisfy spark
     */
    public static Object reject(Request request, Response response) {
        SALT_SERVICE.rejectKey(request.params("id"));
        response.redirect("/rhn/manager/minions");
        return "";
    }

    /**
     * Handler for the realtime remote commands page.
     *
     * @param request the request object
     * @param response the response object
     * @return the ModelAndView object to render the page
     */
    public static ModelAndView cmd(Request request, Response response) {
        request.session().removeAttribute(MinionsAPI.SALT_CMD_RUN_TARGETS);
        return new ModelAndView(new HashMap<>(), "minion/cmd.jade");
    }

    /**
     * Displays a single minion.
     *
     * @param request the request object
     * @param response the response object
     * @return nothing
     */
    public static String show(Request request, Response response) {
        String minionId = request.params("id");
        long id = MinionServerFactory.findByMinionId(minionId)
                .map(MinionServer::getId).orElse(-1L);
        response.redirect("/rhn/systems/details/Overview.do?sid=" + id);
        return "";
    }

    /**
     * Handler for the package management page.
     *
     * @param request the request object
     * @param response the response object
     * @return the ModelAndView object to render the page
     */
    public static ModelAndView packageStates(Request request, Response response) {
        String serverId = request.queryParams("sid");
        Map<String, Object> data = new HashMap<>();
        Server server = ServerFactory.lookupById(new Long(serverId));
        data.put("server", server);
        return new ModelAndView(data, "minion/packages.jade");
    }

    /**
     * Handler for the org states page.
     *
     * @param request the request object
     * @param response the response object
     * @return the ModelAndView object to render the page
     */
    public static ModelAndView orgCustomStates(Request request, Response response) {
        String orgId = request.queryParams("oid");
        Map<String, Object> data = new HashMap<>();
        data.put("orgId", orgId);
        data.put("orgName", OrgFactory.lookupById(new Long(orgId)).getName());
        return new ModelAndView(data, "org/custom.jade");
    }

    /**
     * Handler for the org states page.
     *
     * @param request the request object
     * @param response the response object
     * @param user the current user
     * @return the ModelAndView object to render the page
     */
    public static ModelAndView yourOrgCustomStates(Request request, Response response,
                                                   User user) {
        Map<String, Object> data = new HashMap<>();
        data.put("orgId", user.getOrg().getId());
        data.put("orgName", user.getOrg().getName());
        return new ModelAndView(data, "yourorg/custom.jade");
    }

    /**
     * Handler for the server group states page.
     *
     * @param request the request object
     * @param response the response object
     * @param user the current user
     * @return the ModelAndView object to render the page
     */
    public static ModelAndView serverGroupCustomStates(Request request, Response response,
                                                       User user) {
        String orgId = request.queryParams("sgid");
        Map<String, Object> data = new HashMap<>();
        data.put("groupId", orgId);
        data.put("groupName", ServerGroupFactory.lookupByIdAndOrg(new Long(orgId),
                user.getOrg()).getName());
        return new ModelAndView(data, "groups/custom.jade");
    }

    /**
     * Handler for the minion states page.
     *
     * @param request the request object
     * @param response the response object
     * @return the ModelAndView object to render the page
     */
    public static ModelAndView minionCustomStates(Request request, Response response) {
        String serverId = request.queryParams("sid");
        Map<String, Object> data = new HashMap<>();
        Server server = ServerFactory.lookupById(new Long(serverId));
        data.put("server", server);
        return new ModelAndView(data, "minion/custom.jade");
    }

    /**
     * Handler for the highstate page.
     *
     * @param request the request object
     * @param response the response object
     * @return the ModelAndView object to render the page
     */
    public static ModelAndView highstate(Request request, Response response) {
        String serverId = request.queryParams("sid");
        Map<String, Object> data = new HashMap<>();
        Server server = ServerFactory.lookupById(new Long(serverId));
        data.put("server", server);
        return new ModelAndView(data, "minion/highstate.jade");
    }

    /**
     * Handler for the bootstrapping page.
     *
     * @param request the request object
     * @param response the response object
     * @param user the current user
     * @return the ModelAndView object to render the page
     */
    public static ModelAndView bootstrap(Request request, Response response, User user) {
        Map<String, Object> data = new HashMap<>();
        ActivationKeyManager akm = ActivationKeyManager.getInstance();
        List<String> visibleBootstrapKeys = akm.findAll(user)
                .stream().map(ak -> "'" + ak.getKey() + "'")
                .collect(Collectors.toList());
        data.put("availableActivationKeys", visibleBootstrapKeys);
        return new ModelAndView(data, "minion/bootstrap.jade");
    }

    /**
     * Handler for the server group formula page.
     *
     * @param request the request object
     * @param response the response object
     * @param user the current user
     * @return the ModelAndView object to render the page
     */
    public static ModelAndView serverGroupFormula(Request request, Response response,
            User user) {
        String serverGroupId = request.queryParams("sgid");
        Map<String, Object> data = new HashMap<>();
        data.put("groupId", serverGroupId);
        data.put("groupName",
                ServerGroupFactory.lookupByIdAndOrg(Long.valueOf(serverGroupId),
                user.getOrg()).getName());
        data.put("formula_id", request.params("formula_id"));
        return new ModelAndView(data, "groups/formula.jade");
    }

    /**
     * Return the JSON data to render a group or minion's formula edit page.
     * @param request the http request
     * @param response the http response
     * @param user the current user
     * @return the JSON data
     */
    public static String formulaData(Request request, Response response, User user) {
        Long id = new Long(request.params("id"));
        StateTargetType type = StateTargetType.valueOf(request.params("targetType"));
        int formulaId = Integer.parseInt(request.params("formula_id"));

        response.type("application/json");
        List<String> formulas;
        switch (type) {
            case SERVER:
                if (!checkUserHasPermissionsOnServer(user, ServerFactory.lookupById(id))) {
                    return deniedResponse(response);
                }
                formulas = new LinkedList<>(FormulaFactory.getCombinedFormulasByServerId(id));
                break;
            case GROUP:
                if (!checkUserHasPermissionsOnServerGroup(user,
                            ServerGroupFactory.lookupByIdAndOrg(id, user.getOrg()))) {
                    return deniedResponse(response);
                }
                formulas = FormulaFactory.getFormulasByGroupId(id);
                break;
            default:
                return errorResponse(response, Arrays.asList("Invalid target type!"));
        }

        if (formulas.isEmpty()) {
            return "null";
        }

        Map<String, Object> map = new HashMap<>();
        map.put("formula_list", formulas);

        if (formulaId < 0 || formulaId >= formulas.size()) {
            return GSON.toJson(map);
        }

        String formulaName = formulas.get(formulaId);
        switch (type) {
            case SERVER:
                map.put("system_data", FormulaFactory.
                        getFormulaValuesByNameAndServerId(formulaName, id)
                        .orElseGet(Collections::emptyMap));
                map.put("group_data", FormulaFactory
                        .getGroupFormulaValuesByNameAndServerId(formulaName, id)
                        .orElseGet(Collections::emptyMap));
                break;
            case GROUP:
                map.put("system_data", Collections.emptyMap());
                map.put("group_data", FormulaFactory
                        .getGroupFormulaValuesByNameAndGroupId(formulaName, id)
                        .orElseGet(Collections::emptyMap));
                break;
            default:
                return errorResponse(response, Arrays.asList("Invalid target type!"));
        }
        map.put("formula_name", formulaName);
        map.put("layout", FormulaFactory
                .getFormulaLayoutByName(formulaName)
                .orElseGet(Collections::emptyMap));
        return GSON.toJson(map);
    }

    /**
     * Save formula data for group or server
     * @param request the http request
     * @param response the http response
     * @param user the current user
     * @return null if successful, else list of error messages
     */
    public static String saveFormula(Request request, Response response, User user) {
        // Get data from request
        Map<String, Object> map = GSON.fromJson(request.body(), Map.class);
        Long id = Long.valueOf((String) map.get("id"));
        String formulaName = (String) map.get("formula_name");
        StateTargetType type = StateTargetType.valueOf((String) map.get("type"));
        Map<String, Object> formData = (Map<String, Object>) map.get("content");

        response.type("application/json");

        try {
            switch (type) {
                case SERVER:
                    if (!checkUserHasPermissionsOnServer(user,
                            ServerFactory.lookupById(id))) {
                        return deniedResponse(response);
                    }
                    FormulaFactory.saveServerFormulaData(formData, id, formulaName);
                    break;
                case GROUP:
                    if (!checkUserHasPermissionsOnServerGroup(user,
                                ServerGroupFactory.lookupByIdAndOrg(id, user.getOrg()))) {
                        return deniedResponse(response);
                    }
                    FormulaFactory.saveGroupFormulaData(formData, id, formulaName);
                    break;
                default:
                    return errorResponse(response, Arrays.asList("Invalid target type!"));
            }
        }
        catch (IOException | NotSupportedException e) {
            return errorResponse(response,
                    Arrays.asList("Error while saving formula data: " +
                            e.getMessage()));
        }
        return GSON.toJson(Arrays.asList("Formula saved!"));
    }

    /**
     * Handler for the server group formula selection page.
     *
     * @param request the request object
     * @param response the response object
     * @param user the current user
     * @return the ModelAndView object to render the page
     */
    public static ModelAndView serverGroupFormulas(Request request, Response response,
            User user) {
        String serverGroupId = request.queryParams("sgid");
        Map<String, Object> data = new HashMap<>();
        data.put("groupId", serverGroupId);
        data.put("groupName",
                ServerGroupFactory.lookupByIdAndOrg(Long.valueOf(serverGroupId),
                user.getOrg()).getName());
        return new ModelAndView(data, "groups/formulas.jade");
    }

    /**
     * Return the JSON data to render a server groups formula selection page.
     * @param request the http request
     * @param response the http response
     * @param user the current user
     * @return the JSON data
     */
    public static String listSelectedFormulas(Request request, Response response,
            User user) {
        response.type("application/json");
        Long id = Long.valueOf(request.params("id"));
        StateTargetType type = StateTargetType.valueOf(request.params("targetType"));

        Map<String, Object> data = new HashMap<>();
        switch (type) {
            case SERVER:
                if (!checkUserHasPermissionsOnServer(user, ServerFactory.lookupById(id))) {
                    return deniedResponse(response);
                }
                data.put("selected",FormulaFactory.getFormulasByServerId(id));
                data.put("active", FormulaFactory.getCombinedFormulasByServerId(id));
                break;
            case GROUP:
                if (!checkUserHasPermissionsOnServerGroup(user,
                        ServerGroupFactory.lookupByIdAndOrg(id, user.getOrg()))) {
                    return deniedResponse(response);
                }
                data.put("selected", FormulaFactory.getFormulasByGroupId(id));
                break;
            default:
                return errorResponse(response, Arrays.asList("Invalid target type!"));
        }
        data.put("formulas", FormulaFactory.listFormulas());
        return GSON.toJson(data);
    }

    /**
     * Save the selected formulas of a server or group.
     * @param request the http request
     * @param response the http response
     * @param user the current user
     * @return null if successful, else list of error messages
     */
    public static String saveSelectedFormulas(Request request, Response response,
            User user) {
        // Get data from request
        Map<String, Object> map = GSON.fromJson(request.body(), Map.class);
        Long id = Long.valueOf((String) map.get("id"));
        StateTargetType type = StateTargetType.valueOf((String) map.get("type"));
        List<String> selectedFormulas = (List<String>) map.get("selected");

        response.type("application/json");

        try {
            switch (type) {
                case SERVER:
                    if (!checkUserHasPermissionsOnServer(user,
                            ServerFactory.lookupById(id))) {
                        return deniedResponse(response);
                    }
                    FormulaFactory.saveServerFormulas(id, selectedFormulas);
                    break;
                case GROUP:
                    if (!checkUserHasPermissionsOnServerGroup(user,
                            ServerGroupFactory.lookupByIdAndOrg(id, user.getOrg()))) {
                        return deniedResponse(response);
                    }
                    FormulaFactory.saveGroupFormulas(id, selectedFormulas, user.getOrg());
                    break;
                default:
                    return errorResponse(response, Arrays.asList("Invalid target type!"));
            }
        }
        catch (IOException | NotSupportedException e) {
            return errorResponse(response,
                    Arrays.asList("Error while saving formula data: " + e.getMessage()));
        }
        return GSON.toJson(Arrays.asList("Formulas saved!"));
    }

    /**
     * Handler for the minion formula page.
     *
     * @param request the request object
     * @param response the response object
     * @return the ModelAndView object to render the page
     */
    public static ModelAndView minionFormula(Request request, Response response) {
        Map<String, Object> data = new HashMap<>();
        data.put("server", ServerFactory.lookupById(new Long(request.queryParams("sid"))));
        data.put("formula_id", request.params("formula_id"));
        return new ModelAndView(data, "minion/formula.jade");
    }

    /**
    * Handler for the minion formula selection page.
    *
    * @param request the request object
    * @param response the response object
    * @param user the current user
    * @return the ModelAndView object to render the page
    */
   public static ModelAndView minionFormulas(Request request, Response response,
           User user) {
       Map<String, Object> data = new HashMap<>();
       data.put("server", ServerFactory.lookupById(new Long(request.queryParams("sid"))));
       return new ModelAndView(data, "minion/formulas.jade");
   }

    private static String errorResponse(Response response, List<String> errs) {
        response.type("application/json");
        response.status(HttpStatus.SC_BAD_REQUEST);
        return GSON.toJson(errs);
    }

    private static String deniedResponse(Response response) {
        response.type("application/json");
        response.status(HttpStatus.SC_FORBIDDEN);
        return GSON.toJson("['Permission denied!']");
    }

    private static boolean checkUserHasPermissionsOnServerGroup(User user,
            ServerGroup group) {
        try {
            ServerGroupManager.getInstance().validateAccessCredentials(user, group,
                    group.getName());
            ServerGroupManager.getInstance().validateAdminCredentials(user);
            return true;
        }
        catch (NullPointerException | PermissionException | LookupException e) {
            return false;
        }
    }

    private static boolean checkUserHasPermissionsOnServer(User user, Server server) {
        return SystemManager.isAvailableToUser(user, server.getId());
    }
}
