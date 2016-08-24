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
import java.util.Arrays;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

import javax.transaction.NotSupportedException;

import org.apache.http.HttpStatus;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.redhat.rhn.domain.formula.FormulaFactory;
import com.redhat.rhn.domain.org.OrgFactory;
import com.redhat.rhn.domain.server.MinionServer;
import com.redhat.rhn.domain.server.MinionServerFactory;
import com.redhat.rhn.domain.server.Server;
import com.redhat.rhn.domain.server.ServerFactory;
import com.redhat.rhn.domain.server.ServerGroupFactory;
import com.redhat.rhn.domain.user.User;
import com.redhat.rhn.manager.token.ActivationKeyManager;
import com.suse.manager.webui.services.impl.SaltService;
import com.suse.manager.webui.utils.FlashScopeHelper;

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
            .serializeNulls()
            .create();

    private MinionController() {}

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
     * @return the ModelAndView object to render the page
     */
    public static ModelAndView serverGroupFormula(Request request, Response response, User user) {
        String serverGroupId = request.queryParams("sgid");
        Map<String, Object> data = new HashMap<>();
        data.put("groupId", serverGroupId);
        data.put("groupName", ServerGroupFactory.lookupByIdAndOrg(Long.valueOf(serverGroupId),
                user.getOrg()).getName());
        data.put("info", FlashScopeHelper.flash(request));
        data.put("formula_id", request.params("formula_id"));
        return new ModelAndView(data, "groups/formula.jade");
    }

    /**
     * Return the JSON data to render a formula page of a server group.
     * @param request the http request
     * @param response the http response
     * @param user the current user
     * @return the JSON data
     */
    public static String serverGroupFormulaData(Request request, Response response, User user) {
    	Long groupId = new Long(request.params("sgid"));
    	int formula_id = Integer.parseInt(request.params("formula_id"));
    	List<String> formulas = FormulaFactory.getFormulasByGroupId(groupId);

        String form_data;
    	if (formulas.isEmpty()) {
    		form_data = "null";
    	}
    	else {
        	Map<String, Object> map = new HashMap<>();
        	map.put("formula_list", formulas);

        	if (formula_id >= 0 && formula_id < formulas.size()) {
        		String formula_name = formulas.get(formula_id);
        		map.put("formula_name", formula_name);

        		Optional<Map<String, Object>> layout = FormulaFactory.getFormulaLayoutByName(formula_name);
        		if (layout.isPresent())
        			map.put("layout", layout.get());
        		Optional<Map<String, Object>> values = FormulaFactory.getGroupFormulaValuesByNameAndGroupId(formula_name, groupId);
        		map.put("group_data", values.orElse(new HashMap<String, Object>()));
        	}
        	form_data = GSON.toJson(map);
    	}
        response.type("application/json");
		return form_data;
    }

    /**
     * Save the server groups formula data
     * @param request the http request
     * @param response the http response
     * @param user the current user
     * @return
     */
    public static String serverGroupSaveFormula(Request request, Response response, User user) {
    	// Get data from request
		Map<String, Object> map = GSON.fromJson(request.body(), Map.class);
    	Long groupId = Long.valueOf((String) map.get("groupId"));
    	String formulaName = (String) map.get("formula_name");
    	Map<String, Object> formData = (Map<String, Object>) map.get("content");

    	// Save data
    	try {
    		FormulaFactory.saveGroupFormulaData(formData, groupId, formulaName);
    	} catch (IOException e) {
    		return errorResponse(response,
                    Arrays.asList("Error while saving formula data: " + e.getMessage()));
    	}

    	// Return answer
        FlashScopeHelper.flash(request, "Formula saved!");
    	Map<String, String> data = new HashMap<>();
    	data.put("url", (String) map.get("url"));
        response.type("application/json");
        return GSON.toJson(data);
	}

    /**
     * Handler for the server group formula selection page.
     *
     * @param request the request object
     * @param response the response object
     * @param user the current user
     * @return the ModelAndView object to render the page
     */
    public static ModelAndView serverGroupFormulas(Request request, Response response, User user) {
        String serverGroupId = request.queryParams("sgid");
        Map<String, Object> data = new HashMap<>();
        data.put("groupId", serverGroupId);
        data.put("groupName", ServerGroupFactory.lookupByIdAndOrg(Long.valueOf(serverGroupId),
                user.getOrg()).getName());
        data.put("info", FlashScopeHelper.flash(request));
        return new ModelAndView(data, "groups/formulas.jade");
    }

    /**
     * Return the JSON data to render the server groups formula selection page.
     * @param request the http request
     * @param response the http response
     * @param user the current user
     * @return the JSON data
     */
    public static String serverGroupFormulasData(Request request, Response response, User user) {
    	Map<String, Object> data = new HashMap<>();

        List<String> server_formulas = FormulaFactory.getFormulasByGroupId(Long.valueOf(request.params("sgid")));
		data.put("selected", server_formulas);
		data.put("formulas", FormulaFactory.listFormulas());

		response.type("application/json");
		return GSON.toJson(data);
    }

    /**
     * Save the selected formulas of the server group.
     * @param request the http request
     * @param response the http response
     * @param user the current user
     * @return
     */
    public static String serverGroupFormulasApply(Request request, Response response, User user) {
    	// Get data from request
		Map<String, Object> map = GSON.fromJson(request.body(), Map.class);
    	Long groupId = Long.valueOf((String) map.get("groupId"));
    	List<String> selectedFormulas = (List<String>) map.get("selected");

    	try {
			FormulaFactory.saveServerGroupFormulas(groupId, selectedFormulas, user.getOrg());
    	}
    	catch (IOException e) {
    		return errorResponse(response,
                    Arrays.asList("Error while saving formula data: " + e.getMessage()));
    	}
        response.type("application/json");
        return GSON.toJson("");
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
        data.put("info", FlashScopeHelper.flash(request));
        data.put("formula_id", request.params("formula_id"));
        return new ModelAndView(data, "minion/formula.jade");
    }

    /**
     * Return the JSON data to render the minions formula edit page.
     * @param request the http request
     * @param response the http response
     * @param user the current user
     * @return the JSON data
     */
    public static String minionFormulaData(Request request, Response response, User user) {
    	// Find formulas of server groups
    	Long serverId = new Long(request.params("sid"));
    	int formula_id = Integer.parseInt(request.params("formula_id"));
    	List<String> formulas = FormulaFactory.getFormulasByServerId(serverId);

        String form_data;
    	if (formulas.isEmpty()) {
    		form_data = "null";
    	}
    	else {
        	Map<String, Object> map = new HashMap<>();
        	map.put("formula_list", formulas);

        	if (formula_id >= 0 && formula_id < formulas.size()) {
        		String formula_name = formulas.get(formula_id);
        		map.put("formula_name", formula_name);

        		Optional<Map<String, Object>> layout = FormulaFactory.getFormulaLayoutByName(formula_name);
        		if (layout.isPresent())
        			map.put("layout", layout.get());

        		Optional<Map<String, Object>> system_data = FormulaFactory.getFormulaValuesByNameAndServerId(formula_name, serverId);
        		map.put("system_data", system_data.orElse(new HashMap<String, Object>()));

        		Optional<Map<String, Object>> group_data = FormulaFactory.getGroupFormulaValuesByNameAndServerId(formula_name, serverId);
        		map.put("group_data", group_data.orElse(new HashMap<String, Object>()));
        	}
        	form_data = GSON.toJson(map);
    	}

        response.type("application/json");
		return form_data;
    }

    /**
     * Save the minions formula data
     * @param request the http request
     * @param response the http response
     * @param user the current user
     * @return
     */
    public static String minionSaveFormula(Request request, Response response, User user) {
    	// Get data from request
		Map<String, Object> map = GSON.fromJson(request.body(), Map.class);
    	Long serverId = Long.valueOf((String) map.get("serverId"));
    	String formulaName = (String) map.get("formula_name");
    	Map<String, Object> formData = (Map<String, Object>) map.get("content");

    	// Save data
    	try {
    		FormulaFactory.saveServerFormulaData(formData, serverId, formulaName);
    	} catch (IOException | NotSupportedException e) {
    		return errorResponse(response,
                    Arrays.asList("Error while saving formula data: " + e.getMessage()));
    	}

    	// Return answer
        response.type("application/json");
        return GSON.toJson("");
	}

    private static String errorResponse(Response response, List<String> errs) {
        response.type("application/json");
        response.status(HttpStatus.SC_BAD_REQUEST);
        return GSON.toJson(errs);
    }
}
