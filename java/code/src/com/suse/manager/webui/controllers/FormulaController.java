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

import static com.suse.manager.webui.utils.SparkApplicationHelper.withCsrfToken;
import static com.suse.manager.webui.utils.SparkApplicationHelper.withDocsLocale;
import static com.suse.manager.webui.utils.SparkApplicationHelper.withUser;
import static com.suse.manager.webui.utils.SparkApplicationHelper.withUserAndServer;
import static spark.Spark.get;
import static spark.Spark.post;

import com.redhat.rhn.common.hibernate.LookupException;
import com.redhat.rhn.common.security.PermissionException;
import com.redhat.rhn.common.validator.ValidatorException;
import com.redhat.rhn.domain.formula.FormulaFactory;
import com.redhat.rhn.domain.server.ManagedServerGroup;
import com.redhat.rhn.domain.server.MinionServer;
import com.redhat.rhn.domain.server.MinionServerFactory;
import com.redhat.rhn.domain.server.Server;
import com.redhat.rhn.domain.server.ServerFactory;
import com.redhat.rhn.domain.server.ServerGroupFactory;
import com.redhat.rhn.domain.user.User;
import com.redhat.rhn.manager.formula.FormulaUtil;

import com.suse.manager.webui.services.iface.SaltApi;
import com.suse.manager.webui.services.iface.SystemQuery;
import com.suse.manager.webui.utils.ViewHelper;
import com.suse.manager.webui.utils.gson.StateTargetType;
import com.suse.salt.netapi.datatypes.target.MinionList;
import com.suse.utils.Opt;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonElement;
import com.google.gson.JsonPrimitive;
import com.google.gson.JsonSerializationContext;
import com.google.gson.JsonSerializer;

import org.apache.http.HttpStatus;

import java.io.IOException;
import java.lang.reflect.Type;
import java.util.Arrays;
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

import spark.ModelAndView;
import spark.Request;
import spark.Response;
import spark.template.jade.JadeTemplateEngine;

/**
 * Controller class providing backend code for formulas pages and APIs.
 */
public class FormulaController {

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

    private final SystemQuery systemQuery;
    private final SaltApi saltApi;

    /**
     * @param systemQueryIn instance to use.
     * @param saltApiIn Salt API instance to use.
     */
    public FormulaController(SystemQuery systemQueryIn, SaltApi saltApiIn) {
        this.systemQuery = systemQueryIn;
        this.saltApi = saltApiIn;
    }

    /**
     * Invoked from Router. Initialize routes for Systems Views.
     *
     * @param jade the Jade engine to use to render the pages
     */
    public void initRoutes(JadeTemplateEngine jade) {
        get("/manager/groups/details/formulas",
                withCsrfToken(withUser(this::serverGroupFormulas)),
                jade);
        get("/manager/systems/details/formulas",
                withCsrfToken(withDocsLocale(withUserAndServer(this::minionFormulas))),
                jade);
        get("/manager/groups/details/formula/:formula_id",
                withCsrfToken(withUser(this::serverGroupFormula)),
                jade);
        get("/manager/systems/details/formula/:formula_id",
                withCsrfToken(withDocsLocale(withUserAndServer(this::minionFormula))),
                jade);

        // Formula API
        get("/manager/api/formulas/list/:targetType/:id",
                withUser(this::listSelectedFormulas));
        get("/manager/api/formulas/form/:targetType/:id/:formula_id",
                withUser(this::formulaData));
        post("/manager/api/formulas/select",
                withUser(this::saveSelectedFormulas));
        post("/manager/api/formulas/save",
                withUser(this::saveFormula));
    }

    /**
     * Handler for the server group formula page.
     *
     * @param request the request object
     * @param response the response object
     * @param user the current user
     * @return the ModelAndView object to render the page
     */
    public ModelAndView serverGroupFormula(Request request, Response response,
            User user) {
        String serverGroupId = request.queryParams("sgid");
        Map<String, Object> data = new HashMap<>();
        data.put("groupId", serverGroupId);
        data.put("groupName",
                ServerGroupFactory.lookupByIdAndOrg(Long.valueOf(serverGroupId),
                user.getOrg()).getName());
        data.put("formula_id", request.params("formula_id"));
        data.put("tabs",
                ViewHelper.getInstance().renderNavigationMenu(request, "/WEB-INF/nav/system_group_detail.xml"));
        return new ModelAndView(data, "templates/groups/formula.jade");
    }

    /**
     * Return the JSON data to render a group or minion's formula edit page.
     * @param request the http request
     * @param response the http response
     * @param user the current user
     * @return the JSON data
     */
    public String formulaData(Request request, Response response, User user) {
        Long id = Long.valueOf(request.params("id"));
        StateTargetType type = StateTargetType.valueOf(request.params("targetType"));
        int formulaId = Integer.parseInt(request.params("formula_id"));

        response.type("application/json");
        List<String> formulas;
        switch (type) {
            case SERVER:
                try {
                    FormulaUtil.ensureUserHasPermissionsOnServer(user, ServerFactory.lookupById(id));
                }
                catch (PermissionException e) {
                    return deniedResponse(response);
                }
                formulas = new LinkedList<>(FormulaFactory.getCombinedFormulasByServerId(id));
                break;
            case GROUP:
                try {
                    FormulaUtil.ensureUserHasPermissionsOnServerGroup(user,
                            ServerGroupFactory.lookupByIdAndOrg(id, user.getOrg()));
                }
                catch (PermissionException | LookupException e) {
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
                        getFormulaValuesByNameAndMinionId(formulaName, MinionServerFactory.getMinionId(id))
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
        map.put("metadata", FormulaFactory.getMetadata(formulaName));
        return GSON.toJson(map);
    }

    /**
     * Save formula data for group or server
     * @param request the http request
     * @param response the http response
     * @param user the current user
     * @return null if successful, else list of error messages
     */
    public String saveFormula(Request request, Response response, User user) {
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
                    Optional<MinionServer> minion = MinionServerFactory.lookupById(id);
                    try {
                        FormulaUtil.ensureUserHasPermissionsOnServer(user, minion.get());
                    }
                    catch (PermissionException e) {
                        return deniedResponse(response);
                    }
                    FormulaFactory.saveServerFormulaData(formData, MinionServerFactory.getMinionId(id), formulaName);
                    saltApi.refreshPillar(new MinionList(minion.get().getMinionId()));
                    break;
                case GROUP:
                    ManagedServerGroup group = ServerGroupFactory.lookupByIdAndOrg(id, user.getOrg());
                    try {
                        FormulaUtil.ensureUserHasPermissionsOnServerGroup(user, group);
                    }
                    catch (PermissionException | LookupException e) {
                        return deniedResponse(response);
                    }
                    FormulaFactory.saveGroupFormulaData(formData, id, user.getOrg(), formulaName);
                    List<String> minionIds = group.getServers().stream()
                            .flatMap(s -> Opt.stream(s.asMinionServer()))
                            .map(MinionServer::getMinionId).collect(Collectors.toList());
                    saltApi.refreshPillar(new MinionList(minionIds));
                    break;
                default:
                    return errorResponse(response, Arrays.asList("error_invalid_target")); //Invalid target type!
            }
        }
        catch (IOException | UnsupportedOperationException e) {
            return errorResponse(response,
                    Arrays.asList("Error while saving formula data: " +
                            e.getMessage()));
        }
        Map<String, Object> metadata = FormulaFactory.getMetadata(formulaName);
        if (Boolean.TRUE.equals(metadata.get("pillar_only"))) {
            return GSON.toJson(Arrays.asList("pillar_only_formula_saved"));
        }
        return GSON.toJson(Arrays.asList("formula_saved")); // Formula saved!
    }

    /**
     * Handler for the server group formula selection page.
     *
     * @param request the request object
     * @param response the response object
     * @param user the current user
     * @return the ModelAndView object to render the page
     */
    public ModelAndView serverGroupFormulas(Request request, Response response,
            User user) {
        String serverGroupId = request.queryParams("sgid");
        Map<String, Object> data = new HashMap<>();
        data.put("groupId", serverGroupId);
        data.put("groupName",
                ServerGroupFactory.lookupByIdAndOrg(Long.valueOf(serverGroupId),
                user.getOrg()).getName());
        data.put("warning", FormulaFactory.getWarningMessageAccessFormulaFolders());
        data.put("tabs",
                ViewHelper.getInstance().renderNavigationMenu(request, "/WEB-INF/nav/system_group_detail.xml"));
        return new ModelAndView(data, "templates/groups/formulas.jade");
    }

    /**
     * Return the JSON data to render a server groups formula selection page.
     * @param request the http request
     * @param response the http response
     * @param user the current user
     * @return the JSON data
     */
    public String listSelectedFormulas(Request request, Response response,
            User user) {
        response.type("application/json");
        Long id = Long.valueOf(request.params("id"));
        StateTargetType type = StateTargetType.valueOf(request.params("targetType"));

        Map<String, Object> data = new HashMap<>();
        switch (type) {
            case SERVER:
                try {
                    FormulaUtil.ensureUserHasPermissionsOnServer(user, ServerFactory.lookupById(id));
                }
                catch (PermissionException e) {
                    return deniedResponse(response);
                }
                data.put("selected", FormulaFactory.getFormulasByMinionId(MinionServerFactory.getMinionId(id)));
                data.put("active", FormulaFactory.getCombinedFormulasByServerId(id));
                break;
            case GROUP:
                try {
                    FormulaUtil.ensureUserHasPermissionsOnServerGroup(user,
                            ServerGroupFactory.lookupByIdAndOrg(id, user.getOrg()));
                }
               catch (PermissionException | LookupException e) {
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
    public String saveSelectedFormulas(Request request, Response response,
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
                    try {
                        FormulaUtil.ensureUserHasPermissionsOnServer(user,
                                ServerFactory.lookupById(id));
                    }
                    catch (PermissionException e) {
                        return deniedResponse(response);
                    }
                    FormulaFactory.saveServerFormulas(MinionServerFactory.getMinionId(id), selectedFormulas);
                    break;
                case GROUP:
                    try {
                        FormulaUtil.ensureUserHasPermissionsOnServerGroup(user,
                                ServerGroupFactory.lookupByIdAndOrg(id, user.getOrg()));
                    }
                    catch (PermissionException | LookupException e) {
                        return deniedResponse(response);
                    }
                    FormulaFactory.saveGroupFormulas(id, selectedFormulas, user.getOrg());
                    break;
                default:
                    return errorResponse(response, Arrays.asList("error_invalid_target"));
            }
        }
        catch (IOException | ValidatorException | UnsupportedOperationException e) {
            return errorResponse(response, Arrays.asList("Error while saving formula data: " + e.getMessage()));
        }
        return GSON.toJson(Arrays.asList("formulas_saved"));
    }

    /**
     * Handler for the minion formula page.
     *
     * @param request the request object
     * @param response the response object
     * @param user the current user
     * @param server the current server
     * @return the ModelAndView object to render the page
     */
    public ModelAndView minionFormula(Request request, Response response, User user, Server server) {
        Map<String, Object> data = new HashMap<>();
        data.put("formula_id", request.params("formula_id"));
        return new ModelAndView(data, "templates/minion/formula.jade");
    }

    /**
    * Handler for the minion formula selection page.
    *
    * @param request the request object
    * @param response the response object
    * @param user the current user
     * @param server the current server
    * @return the ModelAndView object to render the page
    */
   public ModelAndView minionFormulas(Request request, Response response,
           User user, Server server) {
       Map<String, Object> data = new HashMap<>();
       data.put("warning", FormulaFactory.getWarningMessageAccessFormulaFolders());
       return new ModelAndView(data, "templates/minion/formulas.jade");
   }

    private String errorResponse(Response response, List<String> errs) {
        response.type("application/json");
        response.status(HttpStatus.SC_BAD_REQUEST);
        return GSON.toJson(errs);
    }

    private String deniedResponse(Response response) {
        response.type("application/json");
        response.status(HttpStatus.SC_FORBIDDEN);
        return GSON.toJson("['Permission denied!']");
    }
}
