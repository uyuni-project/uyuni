/*
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
import static com.suse.manager.webui.utils.SparkApplicationHelper.withOrgAdmin;
import static com.suse.manager.webui.utils.SparkApplicationHelper.withUser;
import static com.suse.manager.webui.utils.SparkApplicationHelper.withUserAndServer;
import static com.suse.manager.webui.utils.SparkApplicationHelper.withUserPreferences;
import static spark.Spark.get;
import com.redhat.rhn.common.conf.Config;
//TEST
import com.redhat.rhn.common.conf.ConfigDefaults;
import com.redhat.rhn.common.db.datasource.DataResult;
import com.redhat.rhn.common.security.PermissionException;
import com.redhat.rhn.domain.org.OrgFactory;
import com.redhat.rhn.domain.role.RoleFactory;
import com.redhat.rhn.domain.server.MinionServer;
import com.redhat.rhn.domain.server.MinionServerFactory;
import com.redhat.rhn.domain.server.Server;
import com.redhat.rhn.domain.server.ServerFactory;
import com.redhat.rhn.domain.server.ServerGroup;
import com.redhat.rhn.domain.server.ServerGroupFactory;
import com.redhat.rhn.domain.server.ServerPath;
import com.redhat.rhn.domain.token.ActivationKey;
import com.redhat.rhn.domain.user.User;
import com.redhat.rhn.frontend.dto.ShortSystemInfo;
import com.redhat.rhn.frontend.struts.ActionChainHelper;
import com.redhat.rhn.manager.ssm.SsmManager;
import com.redhat.rhn.manager.system.SystemManager;
import com.redhat.rhn.manager.token.ActivationKeyManager;

import com.suse.manager.model.attestation.CoCoEnvironmentType;
import com.suse.manager.utils.MinionServerUtils;
import com.suse.manager.webui.utils.ViewHelper;
import com.suse.manager.webui.utils.gson.SimpleMinionJson;
import com.suse.utils.Json;

import java.util.Arrays;
import java.util.Comparator;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import spark.ModelAndView;
import spark.Request;
import spark.Response;
import spark.template.jade.JadeTemplateEngine;

/**
 * Controller class providing backend code for the minions page.
 */
public class MinionController {

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
        initPTFRoutes(jade);
        initCoCoRoutes(jade);
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
                withCsrfToken(withDocsLocale(withUserAndServer(MinionController::packageStates))),
                jade);
        get("/manager/systems/details/custom",
                withCsrfToken(withDocsLocale(withUserAndServer(MinionController::minionCustomStates))),
                jade);
        get("/manager/systems/details/highstate",
                withCsrfToken(withDocsLocale(withUserAndServer(MinionController::highstate))),
                jade);
        get("/manager/systems/details/recurring-actions",
                withCsrfToken(withDocsLocale(withUserAndServer(MinionController::recurringActions))),
                jade);
        get("/manager/systems/details/proxy",
                withCsrfToken(withDocsLocale(withUserAndServer(MinionController::proxy))),
                jade);
        get("/manager/multiorg/details/custom",
                withCsrfToken(MinionController::orgCustomStates),
                jade);
        get("/manager/multiorg/recurring-actions",
                withCsrfToken(withUser(MinionController::orgRecurringActions)),
                jade);
        get("/manager/yourorg/custom",
                withCsrfToken(withDocsLocale(withUser(MinionController::yourOrgConfigChannels))),
                jade);
        get("/manager/yourorg/recurring-actions",
                withCsrfToken(withDocsLocale(withUser(MinionController::yourOrgRecurringActions))),
                jade);
        get("/manager/groups/details/custom",
                withCsrfToken(withUser(MinionController::serverGroupConfigChannels)),
                jade);
        get("/manager/groups/details/highstate",
                withCsrfToken(withUser(MinionController::serverGroupHighstate)), jade);
        get("/manager/groups/details/recurring-actions",
                withCsrfToken(withUser(MinionController::serverGroupRecurringActions)),
                jade);
    }

    private static void initSSMRoutes(JadeTemplateEngine jade) {
        get("/manager/systems/ssm/highstate",
                withCsrfToken(withDocsLocale(withUser(MinionController::ssmHighstate))), jade);
        get("/manager/systems/ssm/proxy",
                withCsrfToken(withDocsLocale(withUser(MinionController::ssmProxy))), jade);
        get("/manager/systems/ssm/coco/settings",
                withCsrfToken(withDocsLocale(withUser(MinionController::ssmCoCoSettings))), jade);
        get("/manager/systems/ssm/coco/schedule",
            withCsrfToken(withDocsLocale(withUser(MinionController::ssmCoCoSchedule))), jade);
    }

    private static void initPTFRoutes(JadeTemplateEngine jade) {
        get("/manager/systems/details/ptf/overview",
            withCsrfToken(withDocsLocale(withUserAndServer(MinionController::ptfOverview))), jade);
        get("/manager/systems/details/ptf/list",
            withCsrfToken(withDocsLocale(withUserAndServer(MinionController::ptfListRemove))), jade);
        get("/manager/systems/details/ptf/install",
            withCsrfToken(withDocsLocale(withUserAndServer(MinionController::ptfInstall))), jade);
    }

    private static void initCoCoRoutes(JadeTemplateEngine jade) {
        get("/manager/systems/details/coco/settings",
            withCsrfToken(withDocsLocale(withUserAndServer(MinionController::cocoSettings))), jade);
        get("/manager/systems/details/coco/list",
            withCsrfToken(withDocsLocale(withUserAndServer(MinionController::cocoListScans))), jade);
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
        if (Config.get().getBoolean(ConfigDefaults.WEB_DISABLE_REMOTE_COMMANDS_FROM_UI)) {
            throw new PermissionException("Remote command is disabled");
        }
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
     * @param user the current user
     * @param server the server
     * @return the ModelAndView object to render the page
     */
    public static ModelAndView packageStates(Request request, Response response, User user, Server server) {
        return new ModelAndView(new HashMap<String, Object>(), "templates/minion/packages.jade");
    }

    /**
     * Handler for the org recurring-actions page.
     *
     * @param request the request object
     * @param response the response object
     * @param user the current user
     * @return the ModelAndView object to render the page
     */
    public static ModelAndView orgRecurringActions(Request request, Response response, User user) {
        DataResult<ShortSystemInfo> dr = SystemManager.systemListShort(user, null);
        dr.elaborate();
        Set<Long> systems = Arrays.stream(dr.toArray())
                .map(system -> ((ShortSystemInfo) system).getId()).
                        collect(Collectors.toSet());
        List<Server> servers = ServerFactory.lookupByIdsAndOrg(systems, user.getOrg());
        List<SimpleMinionJson> minions = MinionServerUtils.filterSaltMinions(servers)
                .map(SimpleMinionJson::fromMinionServer)
                .collect(Collectors.toList());

        Map<String, Object> data = new HashMap<>();
        String orgId = request.queryParams("oid");
        data.put("minions", Json.GSON.toJson(minions));
        data.put("orgId", orgId);
        data.put("orgName", OrgFactory.lookupById(Long.valueOf(orgId)).getName());
        data.put("entityType", "ORG");
        data.put("is_org_admin", user.hasRole(RoleFactory.ORG_ADMIN));
        data.put("tabs",
                ViewHelper.getInstance().renderNavigationMenu(request, "/WEB-INF/nav/org_tabs.xml"));
        return new ModelAndView(data, "templates/org/recurring-actions.jade");
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
        data.put("tabs",
                ViewHelper.getInstance().renderNavigationMenu(request, "/WEB-INF/nav/org_tabs.xml"));
        return new ModelAndView(data, "templates/org/custom.jade");
    }

    /**
     * Handler for the org recurring-actions page.
     *
     * @param request the request object
     * @param response the response object
     * @param user the current user
     * @return the ModelAndView object to render the page
     */
    public static ModelAndView yourOrgRecurringActions(Request request, Response response,
                                                     User user) {
        DataResult<ShortSystemInfo> dr = SystemManager.systemListShort(user, null);
        dr.elaborate();
        Set<Long> systems = Arrays.stream(dr.toArray())
                .map(system -> ((ShortSystemInfo) system).getId()).
                collect(Collectors.toSet());
        List<Server> servers = ServerFactory.lookupByIdsAndOrg(systems, user.getOrg());
        List<SimpleMinionJson> minions = MinionServerUtils.filterSaltMinions(servers)
                .map(SimpleMinionJson::fromMinionServer)
                .collect(Collectors.toList());

        Map<String, Object> data = new HashMap<>();
        data.put("minions", Json.GSON.toJson(minions));
        data.put("orgId", user.getOrg().getId());
        data.put("orgName", user.getOrg().getName());
        data.put("is_org_admin", user.hasRole(RoleFactory.ORG_ADMIN));
        data.put("entityType", "ORG");
        return new ModelAndView(data, "templates/yourorg/recurring-actions.jade");
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
        data.put("tabs",
                ViewHelper.getInstance().renderNavigationMenu(request, "/WEB-INF/nav/system_group_detail.xml"));
        return new ModelAndView(data, "templates/groups/custom.jade");
    }

    /**
     * Handler for the server group recurring-actions page.
     *
     * @param request the request object
     * @param response the response object
     * @param user the current user
     * @return the ModelAndView object to render the page
     */
    public static ModelAndView serverGroupRecurringActions(Request request, Response response,
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
        data.put("entityType", "GROUP");
        data.put("tabs",
                ViewHelper.getInstance().renderNavigationMenu(request, "/WEB-INF/nav/system_group_detail.xml"));
        data.put("is_org_admin", user.hasRole(RoleFactory.ORG_ADMIN));
        return new ModelAndView(data, "templates/groups/recurring-actions.jade");
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

        List<SimpleMinionJson> minions = ServerGroupFactory.listMinionIdsForServerGroup(group).stream()
                .map(m -> new SimpleMinionJson(m.getServerId(), m.getMinionId())).collect(Collectors.toList());

        Map<String, Object> data = new HashMap<>();
        data.put("groupId", grpId);
        data.put("groupName", ServerGroupFactory.lookupByIdAndOrg(Long.valueOf(grpId),
                user.getOrg()).getName());
        data.put("entityType", "GROUP");
        data.put("minions", Json.GSON.toJson(minions));
        data.put("tabs",
                ViewHelper.getInstance().renderNavigationMenu(request, "/WEB-INF/nav/system_group_detail.xml"));
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
        List<SimpleMinionJson> minions =
                MinionServerFactory.findMinionIdsByServerIds(SsmManager.listServerIds(user)).stream()
                        .map(m -> new SimpleMinionJson(m.getServerId(), m.getMinionId())).collect(Collectors.toList());

        Map<String, Object> data = new HashMap<>();
        data.put("minions", Json.GSON.toJson(minions));
        data.put("entityType", "SSM");
        data.put("tabs", ViewHelper.getInstance().renderNavigationMenu(request, "/WEB-INF/nav/ssm.xml"));
        addActionChains(user, data);
        return new ModelAndView(data, "templates/ssm/highstate.jade");
    }

    /**
     * Handler for the minion states page.
     *
     * @param request the request object
     * @param response the response object
     * @param user the user
     * @param server the server
     * @return the ModelAndView object to render the page
     */
    public static ModelAndView minionCustomStates(Request request, Response response, User user, Server server) {
        return new ModelAndView(new HashMap<String, Object>(), "templates/minion/custom.jade");
    }

    /**
     * Handler for the recurring-actions page.
     *
     * @param request the request object
     * @param response the response object
     * @param user the current user
     * @param server the server
     * @return the ModelAndView object to render the page
     */
    public static ModelAndView recurringActions(Request request, Response response, User user, Server server) {
        Map<String, Object> data = new HashMap<>();
        data.put("entityType", "MINION");
        data.put("is_org_admin", user.hasRole(RoleFactory.ORG_ADMIN));
        return new ModelAndView(data, "templates/minion/recurring-actions.jade");
    }

    /**
     * Handler for the highstate page.
     *
     * @param request the request object
     * @param response the response object
     * @param user the current user
     * @param server the server
     * @return the ModelAndView object to render the page
     */
    public static ModelAndView highstate(Request request, Response response, User user, Server server) {
        Map<String, Object> data = new HashMap<>();
        data.put("entityType", "MINION");
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
     * Add the proxies configured for  the use to the bootstrap object model
     *
     * @param user the user
     * @param model the current bootstrap model
     */
    public static void addProxies(User user, Map<String, Object> model) {
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
        model.put("proxies", Json.GSON.toJson(proxies));
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
        data.put("availableActivationKeys", Json.GSON.toJson(visibleBootstrapKeys));
        addProxies(user, data);
        return new ModelAndView(data, "templates/minion/bootstrap.jade");
    }


    /**
     * Handler for the proxy page
     *
     * @param request the request object
     * @param response the response object
     * @param user the current user
     * @param server the current server
     * @return the ModelAndView object to render the page
     */
    public static ModelAndView proxy(Request request, Response response, User user, Server server) {
        Map<String, Object> data = new HashMap<>();
        data.put("entityType", "MINION");
        addProxies(user, data);

        data.put("currentProxy", Json.GSON.toJson(server.getFirstServerPath()
                .map(p -> p.getId().getProxyServer().getId()).orElseGet(() -> 0L)));

        return new ModelAndView(data, "templates/minion/proxy.jade");
    }

    /**
     * Handler for the ssm proxy page
     *
     * @param request the request object
     * @param response the response object
     * @param user the current user
     * @return the ModelAndView object to render the page
     */
    public static ModelAndView ssmProxy(Request request, Response response, User user) {
        Map<String, Object> data = new HashMap<>();
        data.put("entityType", "SSM");
        data.put("tabs", ViewHelper.getInstance().renderNavigationMenu(request, "/WEB-INF/nav/ssm.xml"));
        addProxies(user, data);
        List<SimpleMinionJson> minions =
                MinionServerFactory.lookupByIds(SsmManager.listServerIds(user))
                        .filter(m -> !m.isProxy())
                        .map(m -> new SimpleMinionJson(m.getId(), m.getMinionId())).collect(Collectors.toList());
        if (minions.size() == SsmManager.listServerIds(user).size()) {
            data.put("minions", Json.GSON.toJson(minions));
        }
        else {
            data.put("minions", "[]");
        }

        return new ModelAndView(data, "templates/ssm/proxy.jade");
    }

    /**
     * Handler for the overview PTFs page.
     *
     * @param request the request object
     * @param response the response object
     * @param user the current user
     * @param server the server
     * @return the ModelAndView object to render the page
     */
    public static ModelAndView ptfOverview(Request request, Response response, User user, Server server) {
        return new ModelAndView(new HashMap<>(), "templates/minion/ptf-overview.jade");
    }
    /**
     * Handler for the page to list and remove currently installed PTFs.
     *
     * @param request the request object
     * @param response the response object
     * @param user the current user
     * @param server the server
     * @return the ModelAndView object to render the page
     */
    public static ModelAndView ptfListRemove(Request request, Response response, User user, Server server) {
        Map<String, Object> data = new HashMap<>();
        addActionChains(user, data);
        return new ModelAndView(data, "templates/minion/ptf-list-remove.jade");
    }

    /**
     * Handler for the page to install PTFs.
     *
     * @param request the request object
     * @param response the response object
     * @param user the current user
     * @param server the server
     * @return the ModelAndView object to render the page
     */
    public static ModelAndView ptfInstall(Request request, Response response, User user, Server server) {
        Map<String, Object> data = new HashMap<>();
        addActionChains(user, data);
        return new ModelAndView(data, "templates/minion/ptf-install.jade");
    }

    /**
     * Handler for the page to list confidential computing settings.
     *
     * @param request the request object
     * @param response the response object
     * @param user the current user
     * @param server the server
     * @return the ModelAndView object to render the page
     */
    public static ModelAndView cocoSettings(Request request, Response response, User user, Server server) {
        Map<String, Object> data = new HashMap<>();
        addActionChains(user, data);
        addCoCoMetadata(data);
        return new ModelAndView(data, "templates/minion/coco-settings.jade");
    }

    /**
     * Handler for the page to list confidential computing attestation scans.
     *
     * @param request the request object
     * @param response the response object
     * @param user the current user
     * @param server the server
     * @return the ModelAndView object to render the page
     */
    public static ModelAndView cocoListScans(Request request, Response response, User user, Server server) {
        Map<String, Object> data = new HashMap<>();
        addActionChains(user, data);
        return new ModelAndView(data, "templates/minion/coco-scans-list.jade");
    }

    private static void addCoCoMetadata(Map<String, Object> data) {
        // Confidential computing environment types. Using linked hash map to keep the enum order
        Map<String, String> environmentMap = new LinkedHashMap<>();
        Stream.of(CoCoEnvironmentType.values())
            .forEach(e -> environmentMap.put(e.name(), e.getDescription()));

        data.put("availableEnvironmentTypes", Json.GSON.toJson(environmentMap));
    }

    /**
     * Handler for the ssm confidential computing settings page
     *
     * @param request the request object
     * @param response the response object
     * @param user the current user
     * @return the ModelAndView object to render the page
     */
    public static ModelAndView ssmCoCoSettings(Request request, Response response, User user) {
        Map<String, Object> data = new HashMap<>();

        data.put("entityType", "SSM");
        data.put("tabs", ViewHelper.getInstance().renderNavigationMenu(request, "/WEB-INF/nav/ssm.xml"));
        data.put("systemSupport", Json.GSON.toJson(
            MinionServerFactory.lookupByIds(SsmManager.listServerIds(user))
                .map(minionServer -> Map.of(
                    "id", minionServer.getId(),
                    "name", minionServer.getName(),
                    "cocoSupport", minionServer.doesOsSupportCoCoAttestation()
                ))
                .collect(Collectors.toList())
        ));
        addCoCoMetadata(data);

        return new ModelAndView(data, "templates/ssm/coco-ssm-settings.jade");
    }

    /**
     * Handler for the ssm confidential computing schedule page
     *
     * @param request the request object
     * @param response the response object
     * @param user the current user
     * @return the ModelAndView object to render the page
     */
    public static ModelAndView ssmCoCoSchedule(Request request, Response response, User user) {
        Map<String, Object> data = new HashMap<>();

        data.put("entityType", "SSM");
        data.put("tabs", ViewHelper.getInstance().renderNavigationMenu(request, "/WEB-INF/nav/ssm.xml"));
        data.put("systemSupport", Json.GSON.toJson(
            MinionServerFactory.lookupByIds(SsmManager.listServerIds(user))
                .map(minionServer -> Map.of(
                    "id", minionServer.getId(),
                    "name", minionServer.getName(),
                    "cocoSupport", minionServer.doesOsSupportCoCoAttestation()
                ))
                .collect(Collectors.toList())
        ));
        addActionChains(user, data);

        return new ModelAndView(data, "templates/ssm/coco-ssm-schedule.jade");
    }
}
