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
import static com.suse.manager.webui.utils.SparkApplicationHelper.withOrgAdmin;
import static com.suse.manager.webui.utils.SparkApplicationHelper.withUser;
import static com.suse.manager.webui.utils.SparkApplicationHelper.withUserPreferences;
import static spark.Spark.get;

import com.redhat.rhn.domain.org.OrgFactory;
import com.redhat.rhn.domain.server.MinionServer;
import com.redhat.rhn.domain.server.MinionServerFactory;
import com.redhat.rhn.domain.server.Server;
import com.redhat.rhn.domain.server.ServerFactory;
import com.redhat.rhn.domain.server.ServerGroup;
import com.redhat.rhn.domain.server.ServerGroupFactory;
import com.redhat.rhn.domain.server.ServerPath;
import com.redhat.rhn.domain.token.ActivationKey;
import com.redhat.rhn.domain.user.User;
import com.redhat.rhn.frontend.dto.SystemOverview;
import com.redhat.rhn.frontend.struts.ActionChainHelper;
import com.redhat.rhn.frontend.xmlrpc.system.SystemHandler;
import com.redhat.rhn.manager.ssm.SsmManager;
import com.redhat.rhn.manager.token.ActivationKeyManager;

import com.suse.manager.utils.MinionServerUtils;
import com.suse.manager.webui.services.impl.SaltService;
import com.suse.manager.webui.utils.gson.SimpleMinionJson;
import com.suse.utils.Json;

import java.util.Arrays;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

import spark.ModelAndView;
import spark.Request;
import spark.Response;
import spark.template.jade.JadeTemplateEngine;

/**
 * Controller class providing backend code for the minions page.
 */
public class MinionController {

    // Reference to the SaltService instance
    private static final SaltService SALT_SERVICE = SaltService.INSTANCE;

    private MinionController() { }

    /**
     * Invoked from Router. Initializes routes.
     *
     * @param jade jade engine to use to render pages
     */
    public static void initRoutes(JadeTemplateEngine jade) {
        initSystemRoutes(jade);
        initStatesRoutes(jade);
        initSSMRoutes(jade);
    }

    private static void initSystemRoutes(JadeTemplateEngine jade) {
        get("/manager/systems/keys",
                withUserPreferences(withCsrfToken(withUser(MinionController::list))),
                jade);
        get("/manager/systems/bootstrap",
                withCsrfToken(withOrgAdmin(MinionController::bootstrap)),
                jade);
        get("/manager/systems/cmd",
                withCsrfToken(MinionController::cmd),
                jade);
        get("/manager/systems/:id",
                MinionController::show);
    }

    private static void initStatesRoutes(JadeTemplateEngine jade) {
        get("/manager/systems/details/packages",
                withCsrfToken(MinionController::packageStates),
                jade);
        get("/manager/systems/details/custom",
                withCsrfToken(MinionController::minionCustomStates),
                jade);
        get("/manager/systems/details/highstate",
                withCsrfToken(withUser(MinionController::highstate)),
                jade);
        get("/manager/systems/details/states/schedules",
                withCsrfToken(withUser(MinionController::recurringStates)),
                jade);
        get("/manager/multiorg/details/custom",
                withCsrfToken(MinionController::orgCustomStates),
                jade);
        get("/manager/multiorg/details/recurring-states",
                withCsrfToken(withUser(MinionController::orgRecurringStates)),
                jade);
        get("/manager/yourorg/custom",
                withCsrfToken(withUser(MinionController::yourOrgConfigChannels)),
                jade);
        get("/manager/yourorg/recurring-states",
                withCsrfToken(withUser(MinionController::yourOrgRecurringStates)),
                jade);
        get("/manager/groups/details/custom",
                withCsrfToken(withUser(MinionController::serverGroupConfigChannels)),
                jade);
        get("/manager/groups/details/highstate",
                withCsrfToken(withUser(MinionController::serverGroupHighstate)), jade);
        get("/manager/groups/details/states/schedules",
                withCsrfToken(withUser(MinionController::serverGroupRecurringStates)),
                jade);
    }

    private static void initSSMRoutes(JadeTemplateEngine jade) {
        get("/manager/systems/ssm/highstate",
                withCsrfToken(withUser(MinionController::ssmHighstate)), jade);
    }

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
        return new ModelAndView(data, "templates/minion/list.jade");
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
        return new ModelAndView(new HashMap<>(), "templates/minion/cmd.jade");
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
        Server server = ServerFactory.lookupById(Long.valueOf(serverId));
        data.put("server", server);
        return new ModelAndView(data, "templates/minion/packages.jade");
    }

    /**
     * Handler for the org recurring-states page.
     *
     * @param request the request object
     * @param response the response object
     * @param user the current user
     * @return the ModelAndView object to render the page
     */
    public static ModelAndView orgRecurringStates(Request request, Response response, User user) {
        Set<Long> systems = Arrays.stream(new SystemHandler().listSystems(user))
                .map(system -> ((SystemOverview) system).getId()).
                        collect(Collectors.toSet());
        List<Server> servers = ServerFactory.lookupByIdsAndOrg(systems, user.getOrg());
        List<SimpleMinionJson> minions = MinionServerUtils.filterSaltMinions(servers)
                .map(SimpleMinionJson::fromMinionServer)
                .collect(Collectors.toList());

        Map<String, Object> data = new HashMap<>();
        data.put("minions", Json.GSON.toJson(minions));
        data.put("orgId", user.getOrg().getId());
        data.put("orgName", user.getOrg().getName());
        return new ModelAndView(data, "templates/org/recurring-states.jade");
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
        data.put("orgName", OrgFactory.lookupById(Long.valueOf(orgId)).getName());
        return new ModelAndView(data, "templates/org/custom.jade");
    }

    /**
     * Handler for the org recurring-states page.
     *
     * @param request the request object
     * @param response the response object
     * @param user the current user
     * @return the ModelAndView object to render the page
     */
    public static ModelAndView yourOrgRecurringStates(Request request, Response response,
                                                     User user) {
        Set<Long> systems = Arrays.stream(new SystemHandler().listSystems(user))
                .map(system -> ((SystemOverview) system).getId()).
                collect(Collectors.toSet());
        List<Server> servers = ServerFactory.lookupByIdsAndOrg(systems, user.getOrg());
        List<SimpleMinionJson> minions = MinionServerUtils.filterSaltMinions(servers)
                .map(SimpleMinionJson::fromMinionServer)
                .collect(Collectors.toList());

        Map<String, Object> data = new HashMap<>();
        data.put("minions", Json.GSON.toJson(minions));
        data.put("orgId", user.getOrg().getId());
        data.put("orgName", user.getOrg().getName());
        return new ModelAndView(data, "templates/yourorg/recurring-states.jade");
    }

    /**
     * Handler for the org states page.
     *
     * @param request the request object
     * @param response the response object
     * @param user the current user
     * @return the ModelAndView object to render the page
     */
    public static ModelAndView yourOrgConfigChannels(Request request, Response response,
                                                   User user) {
        Map<String, Object> data = new HashMap<>();
        data.put("orgId", user.getOrg().getId());
        data.put("orgName", user.getOrg().getName());
        return new ModelAndView(data, "templates/yourorg/custom.jade");
    }

    /**
     * Handler for the server group states page.
     *
     * @param request the request object
     * @param response the response object
     * @param user the current user
     * @return the ModelAndView object to render the page
     */
    public static ModelAndView serverGroupConfigChannels(Request request, Response response,
                                                       User user) {
        String orgId = request.queryParams("sgid");
        Map<String, Object> data = new HashMap<>();
        data.put("groupId", orgId);
        data.put("groupName", ServerGroupFactory.lookupByIdAndOrg(Long.valueOf(orgId),
                user.getOrg()).getName());
        return new ModelAndView(data, "templates/groups/custom.jade");
    }

    /**
     * Handler for the server group recurring-states page.
     *
     * @param request the request object
     * @param response the response object
     * @param user the current user
     * @return the ModelAndView object to render the page
     */
    public static ModelAndView serverGroupRecurringStates(Request request, Response response,
                                                    User user) {
        String grpId = request.queryParams("sgid");

        ServerGroup group =
                ServerGroupFactory.lookupByIdAndOrg(Long.parseLong(grpId), user.getOrg());

        List<Server> groupServers = ServerGroupFactory.listServers(group);
        List<SimpleMinionJson> minions = MinionServerUtils.filterSaltMinions(groupServers)
                .map(SimpleMinionJson::fromMinionServer)
                .collect(Collectors.toList());

        Map<String, Object> data = new HashMap<>();
        data.put("groupId", grpId);
        data.put("groupName", ServerGroupFactory.lookupByIdAndOrg(Long.valueOf(grpId),
                user.getOrg()).getName());
        data.put("minions", Json.GSON.toJson(minions));
        return new ModelAndView(data, "templates/groups/recurring-states.jade");
    }

    /**
     * Handler for the server group highstate page.
     *
     * @param request the request object
     * @param response the response object
     * @param user the current user
     * @return the ModelAndView object to render the page
     */
    public static ModelAndView serverGroupHighstate(Request request, Response response,
            User user) {
        String grpId = request.queryParams("sgid");

        ServerGroup group =
                ServerGroupFactory.lookupByIdAndOrg(Long.parseLong(grpId), user.getOrg());

        List<Server> groupServers = ServerGroupFactory.listServers(group);
        List<SimpleMinionJson> minions = MinionServerUtils.filterSaltMinions(groupServers)
                .map(SimpleMinionJson::fromMinionServer)
                .collect(Collectors.toList());

        Map<String, Object> data = new HashMap<>();
        data.put("groupId", grpId);
        data.put("groupName", ServerGroupFactory.lookupByIdAndOrg(Long.valueOf(grpId),
                user.getOrg()).getName());
        data.put("minions", Json.GSON.toJson(minions));
        addActionChains(user, data);
        return new ModelAndView(data, "templates/groups/highstate.jade");
    }

    /**
     * Handler for the SSM highstate page.
     *
     * @param request the request object
     * @param response the response object
     * @param user the current user
     * @return the ModelAndView object to render the page
     */
    public static ModelAndView ssmHighstate(Request request, Response response, User user) {
        List<SimpleMinionJson> minions = MinionServerFactory
                .lookupByIds(SsmManager.listServerIds(user))
                .map(SimpleMinionJson::fromMinionServer).collect(Collectors.toList());

        Map<String, Object> data = new HashMap<>();
        data.put("minions", Json.GSON.toJson(minions));
        addActionChains(user, data);
        return new ModelAndView(data, "templates/ssm/highstate.jade");
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
        Server server = ServerFactory.lookupById(Long.valueOf(serverId));
        data.put("server", server);
        return new ModelAndView(data, "templates/minion/custom.jade");
    }

    /**
     * Handler for the recurring-states page.
     *
     * @param request the request object
     * @param response the response object
     * @param user the current user
     * @return the ModelAndView object to render the page
     */
    public static ModelAndView recurringStates(Request request, Response response, User user) {
        String serverId = request.queryParams("sid");
        Map<String, Object> data = new HashMap<>();
        Server server = ServerFactory.lookupById(Long.valueOf(serverId));
        if (MinionServerUtils.isMinionServer(server)) {
            data.put("server", server);
        }
        return new ModelAndView(data, "templates/minion/recurring-states.jade");
    }

    /**
     * Handler for the highstate page.
     *
     * @param request the request object
     * @param response the response object
     * @param user the current user
     * @return the ModelAndView object to render the page
     */
    public static ModelAndView highstate(Request request, Response response, User user) {
        String serverId = request.queryParams("sid");
        Map<String, Object> data = new HashMap<>();
        Server server = ServerFactory.lookupById(Long.valueOf(serverId));
        data.put("server", server);
        addActionChains(user, data);
        return new ModelAndView(data, "templates/minion/highstate.jade");
    }

    /**
     * Utility method for adding a list of action chains to the model.
     * @param user the user
     * @param model the model
     */
    public static void addActionChains(User user, Map<String, Object> model) {
        model.put("actionChains", ActionChainHelper.actionChainsJson(user));
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
        List<String> visibleBootstrapKeys = akm.findAllActive(user).stream()
                .map(ActivationKey::getKey)
                .collect(Collectors.toList());
        List<Map<String, Object>> proxies = ServerFactory.lookupProxiesByOrg(user)
                .stream()
                .map(proxy -> {
                    Map<String, Object> entry = new HashMap<>();
                    entry.put("id", proxy.getId());
                    entry.put("name", proxy.getName());
                    entry.put("hostname", proxy.getHostname());
                    List<String> path = proxy.getServerPaths().stream()
                            .sorted(Comparator.comparingLong(ServerPath::getPosition))
                            .map(ServerPath::getHostname)
                            .collect(Collectors.toList());
                    entry.put("path", path);
                    return entry; })
                .collect(Collectors.toList());
        data.put("availableActivationKeys", Json.GSON.toJson(visibleBootstrapKeys));
        data.put("proxies", Json.GSON.toJson(proxies));
        return new ModelAndView(data, "templates/minion/bootstrap.jade");
    }
}
