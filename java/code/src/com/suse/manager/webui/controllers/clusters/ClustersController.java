/**
 * Copyright (c) 2020 SUSE LLC
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

package com.suse.manager.webui.controllers.clusters;

import static com.suse.manager.webui.controllers.clusters.mappers.ResponseMappers.toClusterNodeResponse;
import static com.suse.manager.webui.controllers.clusters.mappers.ResponseMappers.toClusterResponse;
import static com.suse.manager.webui.controllers.clusters.mappers.ResponseMappers.toServerResponse;
import static com.suse.manager.webui.utils.SparkApplicationHelper.json;
import static com.suse.manager.webui.utils.SparkApplicationHelper.withClusterAdmin;
import static com.suse.manager.webui.utils.SparkApplicationHelper.withCsrfToken;
import static com.suse.manager.webui.utils.SparkApplicationHelper.withRolesTemplate;
import static com.suse.manager.webui.utils.SparkApplicationHelper.withUser;
import static com.suse.manager.webui.utils.SparkApplicationHelper.withUserPreferences;
import static spark.Spark.delete;
import static spark.Spark.get;
import static spark.Spark.halt;
import static spark.Spark.post;

import com.redhat.rhn.common.hibernate.HibernateFactory;
import com.redhat.rhn.common.hibernate.LookupException;
import com.redhat.rhn.common.security.PermissionException;
import com.redhat.rhn.domain.action.ActionFactory;
import com.redhat.rhn.domain.action.ActionType;
import com.redhat.rhn.domain.formula.FormulaFactory;
import com.redhat.rhn.domain.server.MinionServerFactory;
import com.redhat.rhn.domain.user.User;
import com.redhat.rhn.manager.formula.FormulaManager;
import com.suse.manager.clusters.ClusterFactory;
import com.suse.manager.clusters.ClusterManager;
import com.suse.manager.clusters.ClusterProvider;
import com.suse.manager.model.clusters.Cluster;
import com.suse.manager.reactor.utils.LocalDateTimeISOAdapter;
import com.suse.manager.reactor.utils.OptionalTypeAdapterFactory;
import com.suse.manager.webui.controllers.MinionController;
import com.suse.manager.webui.controllers.clusters.mappers.ResponseMappers;
import com.suse.manager.webui.controllers.clusters.response.ClusterNodeResponse;
import com.suse.manager.webui.controllers.clusters.response.ClusterProviderResponse;
import com.suse.manager.webui.controllers.clusters.response.ClusterResponse;
import com.suse.manager.webui.controllers.clusters.response.ServerResponse;
import com.suse.manager.webui.services.impl.SaltService;
import com.suse.manager.webui.services.impl.runner.MgrUtilRunner;
import com.suse.manager.webui.utils.FlashScopeHelper;
import com.suse.manager.webui.utils.MinionActionUtils;
import com.suse.manager.webui.utils.gson.ResultJson;
import com.suse.manager.webui.utils.gson.ScheduledRequestJson;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonParseException;
import com.google.gson.reflect.TypeToken;
import com.suse.salt.netapi.calls.RunnerCall;
import org.apache.commons.lang3.StringUtils;
import org.apache.http.HttpStatus;
import org.apache.log4j.Logger;

import java.io.IOException;
import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

import spark.ModelAndView;
import spark.Request;
import spark.Response;
import spark.template.jade.JadeTemplateEngine;

/**
 * Controller for clusters UI.
 */
public class ClustersController {

    private static final Logger LOG = Logger.getLogger(ClustersController.class);

    private static final Pattern LABEL_REGEX = Pattern.compile("^[a-zA-Z_][a-zA-Z0-9_]+$");

    private static final Gson GSON = new GsonBuilder()
            .registerTypeAdapter(LocalDateTime.class, new LocalDateTimeISOAdapter())
            .registerTypeAdapterFactory(new OptionalTypeAdapterFactory())
            .serializeNulls()
            .create();

    private static ClusterManager clusterManager = ClusterManager.instance();

    private ClustersController() { }

    /**
     * Called from Router. Initializes Spark routes.
     * @param jade the Jade engine to use to render the pages
     */
    public static void initRoutes(JadeTemplateEngine jade) {
        get("/manager/clusters",
                withCsrfToken(withUserPreferences(withRolesTemplate(ClustersController::showList))), jade);
        get("/manager/clusters/add",
                withCsrfToken(withClusterAdmin(
                        withUserPreferences(withRolesTemplate(ClustersController::showAddCluster)))), jade);
        get("/manager/cluster/:id",
                withCsrfToken(withUserPreferences(withRolesTemplate(ClustersController::showCluster))), jade);
        get("/manager/cluster/:id/join",
                withCsrfToken(withClusterAdmin(
                        withUserPreferences(withRolesTemplate(ClustersController::showJoinCluster)))), jade);
        post("/manager/cluster/:id/remove",
                withCsrfToken(withClusterAdmin(
                        withUserPreferences(withRolesTemplate(ClustersController::showRemoveNode)))), jade);
        get("/manager/cluster/:id/upgrade",
                withCsrfToken(withClusterAdmin(
                        withUserPreferences(withRolesTemplate(ClustersController::showClusterUpgrade)))), jade);

        get("/manager/api/cluster/:id/nodes",
                withUser(ClustersController::listNodes));
        get("/manager/api/cluster/:id/nodes-to-join",
                withUser(ClustersController::listNodesToJoin));
        post("/manager/api/cluster/:id/refresh-group-nodes",
                withClusterAdmin(ClustersController::refreshGroupNodes));
        get("/manager/api/cluster/:id/formula/:formula/data",
                withUser(ClustersController::getFormulaData));
        post("/manager/api/cluster/:id/formula/:formula/data",
                withClusterAdmin(ClustersController::saveFormulaData));
        post("/manager/api/cluster/:id/join",
                withClusterAdmin(ClustersController::joinNode));
        post("/manager/api/cluster/:id/remove-node",
                withClusterAdmin(ClustersController::removeNode));
        post("/manager/api/cluster/:id/upgrade",
                withClusterAdmin(ClustersController::upgradeCluster));
        get("/manager/api/cluster/:id",
                withUser(ClustersController::getClusterProps));
        post("/manager/api/cluster/:id",
                withClusterAdmin(ClustersController::updateCluster));
        delete("/manager/api/cluster/:id",
                withClusterAdmin(ClustersController::deleteCluster));

        post("/manager/api/cluster/provider/:provider/formula/:formula/form",
                withUser(ClustersController::providerFormulaForm));
        get("/manager/api/cluster/provider/:provider/management-nodes",
                withUser(ClustersController::providerManagementNodes));

        post("/manager/api/cluster/new/add",
                withClusterAdmin(ClustersController::addCluster));

        get("/manager/api/cluster-runner",
                withUser(ClustersController::asyncRunner));

    }

    private static Object asyncRunner(Request request, Response response, User user) {
        RunnerCall<String> call =
                new RunnerCall<>("stest.stdout_print", Optional.empty(),
                        new TypeToken<>() { });
        String jid = SaltService.INSTANCE_SALT_SERVICE.callAsync(call);
        return jid;
    }

    private static Object getClusterProps(Request request, Response response, User user) {
        Cluster cluster = getCluster(request, user);
        return json(response,
                ResultJson.success(toClusterResponse(cluster,
                        getClusterProvider(cluster.getProvider()))));

    }

    private static class UpdateClusterRequest {
        private String name;
        private String description;
        /**
         * @return name to get
         */
        public String getName() {
            return name;
        }

        /**
         * @return description to get
         */
        public String getDescription() {
            return description;
        }
    }

    private static Object updateCluster(Request request, Response response, User user) {
        Cluster cluster = getCluster(request, user);

        UpdateClusterRequest clusterRequest;
        try {
            clusterRequest = GSON.fromJson(request.body(), UpdateClusterRequest.class);
        }
        catch (JsonParseException e) {
            LOG.error("Error parsing request body", e);
            return json(response, HttpStatus.SC_BAD_REQUEST,
                    ResultJson.error("invalid_request_body"));
        }
        if (StringUtils.isBlank(clusterRequest.getName()) ||
                StringUtils.isBlank(clusterRequest.getDescription())) {
            return json(response, HttpStatus.SC_BAD_REQUEST,
                    ResultJson.error("Name and description are mandatory"));
        }
        try {
            clusterManager.update(cluster, clusterRequest.getName(), clusterRequest.getDescription());
            HibernateFactory.getSession().getTransaction().commit();
        }
        catch (Exception e) {
            LOG.error("Updating cluster failed", e);
            return json(response, HttpStatus.SC_INTERNAL_SERVER_ERROR,
                    ResultJson.error(e.getMessage()));
        }
        return json(response, ResultJson.success());
    }

    private static ModelAndView showRemoveNode(Request request, Response response, User user) {
        Cluster cluster = getCluster(request, user);
        List<String> nodes = Arrays.asList(request.queryParamsValues("nodes"));

        var clusterNodes = clusterManager.listClusterNodes(cluster);
        var clusterNodeHostnames = clusterNodes.stream()
                .map(n -> n.getHostname())
                .collect(Collectors.toList());
        if (!clusterNodeHostnames.containsAll(nodes)) {
            LOG.error("Not all nodes '" + nodes + "' are part of the cluster");
            halt(HttpStatus.SC_BAD_REQUEST);
        }
        var nodesToRemove = clusterNodes.stream()
                .filter(cn -> nodes.contains(cn.getHostname()))
                .collect(Collectors.toList());

        Map<String, Object> data = new HashMap<>();
        data.put("flashMessage", FlashScopeHelper.flash(request));
        data.put("contentCluster", GSON.toJson(
                toClusterResponse(cluster, getClusterProvider(cluster.getProvider()))));
        data.put("nodes", GSON.toJson(nodesToRemove.stream()
                .map(n -> toClusterNodeResponse(n, Optional.empty())).collect(Collectors.toList())));
        MinionController.addActionChains(user, data);
        return new ModelAndView(data, "controllers/clusters/templates/remove-node.jade");

    }

    private static Object saveFormulaData(Request request, Response response, User user) {
        Optional<Map<String, Object>> formulaData = parseJson(request, response);
        if (formulaData.isEmpty()) {
            return json(response, HttpStatus.SC_BAD_REQUEST,
                    ResultJson.error("request_error"));
        }
        Cluster cluster = getCluster(request, user);

        String formula = request.params("formula");
        if (StringUtils.isBlank(formula)) {
            return json(response, HttpStatus.SC_BAD_REQUEST,
                    ResultJson.error("Formula parameter is empty"));
        }
        try {
            clusterManager.saveFormulaData(cluster, formula, formulaData.get(), user);
        }
        catch (PermissionException | LookupException e) {
            LOG.error("Error saving formula", e);
            return json(response, HttpStatus.SC_FORBIDDEN,
                    ResultJson.error("No permission to save formula"));
        }
        catch (IOException | RuntimeException e) {
            LOG.error("Error saving formula", e);
            return json(response, HttpStatus.SC_INTERNAL_SERVER_ERROR,
                    ResultJson.error(e.getMessage()));
        }
        return json(response, ResultJson.success());
    }

    private static ModelAndView showJoinCluster(Request request, Response response, User user) {
        Cluster cluster = getCluster(request, user);
        Map<String, Object> data = new HashMap<>();
        data.put("flashMessage", FlashScopeHelper.flash(request));
        data.put("contentCluster", GSON.toJson(
                toClusterResponse(cluster, getClusterProvider(cluster.getProvider()))));
        MinionController.addActionChains(user, data);
        return new ModelAndView(data, "controllers/clusters/templates/join.jade");
    }

    private static ModelAndView showClusterUpgrade(Request request, Response response, User user) {
        Cluster cluster = getCluster(request, user);
        Map<String, Object> data = new HashMap<>();
        data.put("flashMessage", FlashScopeHelper.flash(request));
        data.put("contentCluster", GSON.toJson(
                toClusterResponse(cluster, getClusterProvider(cluster.getProvider()))));
        MinionController.addActionChains(user, data);
        return new ModelAndView(data, "controllers/clusters/templates/upgrade.jade");
    }

    private static class AddClusterRequest {
        private String name;
        private String label;
        private String description;
        private String provider;
        private Long managementNodeId;
        private Map<String, Object> managementSettings;

        /**
         * @return name to get
         */
        public String getName() {
            return name;
        }

        /**
         * @return label to get
         */
        public String getLabel() {
            return label;
        }

        /**
         * @return description to get
         */
        public String getDescription() {
            return description;
        }

        /**
         * @return provider to get
         */
        public String getProvider() {
            return provider;
        }

        /**
         * @return managementNodeId to get
         */
        public Long getManagementNodeId() {
            return managementNodeId;
        }

        /**
         * @return managementSettings to get
         */
        public Map<String, Object> getManagementSettings() {
            return managementSettings;
        }
    }

    private static String addCluster(Request request, Response response, User user) {
        AddClusterRequest clusterRequest;
        try {
            clusterRequest = GSON.fromJson(request.body(), AddClusterRequest.class);
        }
        catch (JsonParseException e) {
            LOG.error("Error parsing request body", e);
            return json(response, HttpStatus.SC_BAD_REQUEST,
                    ResultJson.error("invalid_request_body"));
        }

        if (StringUtils.isBlank(clusterRequest.getName())) {
            return json(response, HttpStatus.SC_BAD_REQUEST,
                    ResultJson.error("Name is mandatory"));
        }
        if (StringUtils.isBlank(clusterRequest.getLabel())) {
            return json(response, HttpStatus.SC_BAD_REQUEST,
                    ResultJson.error("Label is mandatory"));
        }

        if (!LABEL_REGEX.matcher(clusterRequest.getLabel()).matches()) {
            return json(response, HttpStatus.SC_BAD_REQUEST,
                    ResultJson.error("Label format is wrong"));
        }
        if (StringUtils.isBlank(clusterRequest.getDescription())) {
            return json(response, HttpStatus.SC_BAD_REQUEST,
                    ResultJson.error("Description is mandatory"));
        }
        if (StringUtils.isBlank(clusterRequest.getProvider())) {
            return json(response, HttpStatus.SC_BAD_REQUEST,
                    ResultJson.error("Provider is mandatory"));
        }
        if (!validProvider(clusterRequest.getProvider())) {
            return json(response, HttpStatus.SC_BAD_REQUEST,
                    ResultJson.error("Invalid provider"));
        }
        if (clusterRequest.getManagementNodeId() == null) {
            return json(response, HttpStatus.SC_BAD_REQUEST,
                    ResultJson.error("Management node id is mandatory"));
        }
        if (clusterRequest.getManagementSettings() == null ||
                clusterRequest.getManagementSettings().isEmpty()) {
            return json(response, HttpStatus.SC_BAD_REQUEST,
                    ResultJson.error("Management settings are mandatory"));
        }

        Cluster cluster;
        try {
            cluster = clusterManager.addCluster(clusterRequest.getName(),
                    clusterRequest.getLabel(), clusterRequest.getDescription(),
                    clusterRequest.getManagementNodeId(), clusterRequest.getProvider(),
                    clusterRequest.getManagementSettings(), user);
            HibernateFactory.getSession().getTransaction().commit();
        }
        catch (Exception e) {
            LOG.error("Adding cluster failed", e);
            return json(response, HttpStatus.SC_INTERNAL_SERVER_ERROR, ResultJson.error(e.getMessage()));
        }
        FlashScopeHelper.flash(request, String.format("Cluster '%s' has been added successfully",
                clusterRequest.getName()));
        return json(response, ResultJson.success(cluster.getId()));
    }

    private static boolean validProvider(String provider) {
        List<String> providers = clusterManager.findClusterProviders()
                .stream().map(ClusterProvider::getLabel).collect(Collectors.toList());
        return providers.contains(provider);
    }

    private static Object deleteCluster(Request request, Response response, User user) {
        Cluster cluster = getCluster(request, user);

        try {
            clusterManager.deleteCluster(cluster, user);
            HibernateFactory.getSession().getTransaction().commit();
        }
        catch (Exception e) {
            LOG.error("Deleting cluster failed", e);
            return json(response, HttpStatus.SC_INTERNAL_SERVER_ERROR, ResultJson.error(e.getMessage()));
        }
        FlashScopeHelper.flash(request, String.format("Cluster '%s' has been deleted successfully",
                cluster.getName()));
        return json(response, ResultJson.success());
    }

    public static class ModifyNodesRequest extends ScheduledRequestJson {
        private List<Long> serverIds;
        private Map<String, Object> formula;

        /**
         * @return serverId to get
         */
        public List<Long> getServerIds() {
            return serverIds;
        }

        /**
         * @return joinConfig to get
         */
        public Map<String, Object> getFormula() {
            return formula;
        }

    }

    private static Object joinNode(Request request, Response response, User user) {
        return modifyClusterNodes(ActionFactory.TYPE_CLUSTER_JOIN_NODE, request, response, user);
    }

    private static Object removeNode(Request request, Response response, User user) {
        return modifyClusterNodes(ActionFactory.TYPE_CLUSTER_REMOVE_NODE, request, response, user);
    }

    private static Object upgradeCluster(Request request, Response response, User user) {
        Cluster cluster = getCluster(request, user);
        ModifyNodesRequest nodesRequest;
        try {
            nodesRequest = GSON.fromJson(request.body(), ModifyNodesRequest.class);
        }
        catch (JsonParseException e) {
            return json(response, HttpStatus.SC_BAD_REQUEST,
                    ResultJson.error());
        }
        Date scheduleDate = MinionActionUtils.getScheduleDate(nodesRequest.getEarliest());
        long actionId;
        try {
            actionId = clusterManager.modifyClusterNodes(ActionFactory.TYPE_CLUSTER_UPGRADE_CLUSTER,
                    cluster, Collections.emptyList(),
                    new HashMap<>(), scheduleDate, user).getId();
            HibernateFactory.getSession().getTransaction().commit();
        }
        catch (Exception e) {
            LOG.error("Scheduling cluster upgrade failed", e);
            return json(response, HttpStatus.SC_INTERNAL_SERVER_ERROR,
                    ResultJson.error("Internal error " + e.getClass()));
        }
        return json(response, ResultJson.success(actionId));
    }

    private static Object modifyClusterNodes(ActionType actionType, Request request, Response response, User user) {
        Cluster cluster = getCluster(request, user);
        ModifyNodesRequest nodesRequest;
        try {
            nodesRequest = GSON.fromJson(request.body(), ModifyNodesRequest.class);
        }
        catch (JsonParseException e) {
            return json(response, HttpStatus.SC_BAD_REQUEST,
                    ResultJson.error());
        }
        Date scheduleDate = MinionActionUtils.getScheduleDate(nodesRequest.getEarliest());
        long actionId;
        try {
            actionId = clusterManager.modifyClusterNodes(actionType,
                    cluster, nodesRequest.getServerIds(),
                    nodesRequest.getFormula(), scheduleDate, user).getId();
            HibernateFactory.getSession().getTransaction().commit();
        }
        catch (Exception e) {
            LOG.error("Scheduling join or remove node to cluster failed", e);
            return json(response, HttpStatus.SC_INTERNAL_SERVER_ERROR,
                    ResultJson.error("Internal error " + e.getClass()));
        }
        return json(response, ResultJson.success(actionId));
    }

    private static Optional<Map<String, Object>> parseJson(Request request, Response response) {
        Map<String, Object> jsonRequest;
        try {
            jsonRequest = GSON.fromJson(request.body(), new TypeToken<Map<String, Object>>() {
            }.getType());
        }
        catch (JsonParseException e) {
            LOG.error("Error parsing JSON body", e);
            return Optional.empty();
        }
        return Optional.ofNullable(jsonRequest);
    }

    private static String providerManagementNodes(Request request, Response response, User user) {
        String provider = request.params("provider");
        if (!validProvider(provider)) {
            return json(response, HttpStatus.SC_BAD_REQUEST,
                    ResultJson.error("Invalid provider"));
        }
        List<String> minionIds = clusterManager.findManagementNodeByProvider(provider);
        List<ServerResponse> data = minionIds.stream()
                .map(minionId -> MinionServerFactory.findByMinionId(minionId))
                .filter(minion -> minion.isPresent())
                .map(minion -> minion.get())
                .map(ResponseMappers::toServerResponse)
                .collect(Collectors.toList());
        return json(response, ResultJson.success(data));
    }

    private static ModelAndView showAddCluster(Request request, Response response, User user) {
        Map<String, Object> data = new HashMap<>();
        List<ClusterProviderResponse> providers =
                clusterManager.findClusterProviders().stream()
                        .map(ResponseMappers::toClusterProviderResponse)
                        .collect(Collectors.toList());
        data.put("flashMessage", FlashScopeHelper.flash(request));
        data.put("contentAdd", GSON.toJson(providers));
        MinionController.addActionChains(user, data);
        return new ModelAndView(data, "controllers/clusters/templates/add.jade");
    }

    private static Object listNodes(Request request, Response response, User user) {
        Cluster cluster = getCluster(request, user);
        Map<String, Object> data = new HashMap<>();
        Optional<List<String>> fields = clusterManager.getNodesListFields(cluster.getProvider());
        data.put("fields", fields.orElse(Collections.emptyList()));
        List<ClusterNodeResponse> nodes = clusterManager.listClusterNodes(cluster)
                .stream().map(node -> toClusterNodeResponse(node, fields))
                .collect(Collectors.toList());
        data.put("nodes", nodes);

        return json(response, ResultJson.success(data));
    }

    private static Object listNodesToJoin(Request request, Response response, User user) {
        Cluster cluster = getCluster(request, user);

        var servers = clusterManager.getNodesAvailableForJoining(cluster, user);
        List<ServerResponse> data = servers.entrySet().stream()
                .map(entry -> toServerResponse(entry.getKey(), Optional.ofNullable(entry.getValue())))
                .collect(Collectors.toList());

        return json(response, ResultJson.success(data));
    }

    /**
     * Show list of clusters.
     * @param request http request
     * @param response http response
     * @param user the user
     * @return ModelAndView for page
     */
    public static ModelAndView showList(Request request, Response response, User user) {
        Map<String, Object> data = new HashMap<>();
        List<ClusterResponse> clusters =
                ClusterFactory.findClustersByOrg(user.getOrg().getId()).stream()
                        .map(cluster -> toClusterResponse(cluster,
                                getClusterProvider(cluster.getProvider())))
                        .collect(Collectors.toList());
        data.put("flashMessage", FlashScopeHelper.flash(request));
        data.put("contentClusters", GSON.toJson(clusters));
        return new ModelAndView(data, "controllers/clusters/templates/list.jade");
    }

    /**
     * Show cluster details.
     * @param request http request
     * @param response http response
     * @param user the user
     * @return ModelAndView for page
     */
    public static ModelAndView showCluster(Request request, Response response, User user) {
        Cluster cluster = getCluster(request, user);

        Map<String, Object> data = new HashMap<>();
        data.put("flashMessage", FlashScopeHelper.flash(request));
        data.put("contentCluster",
                GSON.toJson(toClusterResponse(cluster, getClusterProvider(cluster.getProvider()))));
        return new ModelAndView(data, "controllers/clusters/templates/cluster.jade");
    }

    /**
     * Get cluster formula form.
     * @param request http request
     * @param response http response
     * @param user the user
     * @return the formula form as Json
     */
    public static String providerFormulaForm(Request request, Response response, User user) {
        String provider = request.params("provider");
        String formula = request.params("formula");

        if (StringUtils.isBlank(formula)) {
            return json(response, HttpStatus.SC_BAD_REQUEST,
                    ResultJson.error("Formula is mandatory"));

        }
        if (!validProvider(provider)) {
            return json(response, HttpStatus.SC_BAD_REQUEST,
                    ResultJson.error("Invalid provider"));
        }

        Map<String, Object> data = new HashMap<>();
        data.put("form",
                FormulaFactory.getClusterProviderFormulaLayout(provider, formula)
                        .orElseGet(Collections::emptyMap));

        Optional<Map<String, Object>> bodyJson = parseJson(request, response);
        if (bodyJson.isPresent()) {
            Map<String, Object> initialData = clusterManager.initialFormulaData(provider, formula, bodyJson.get());
            data.put("params", initialData);
        }

        return json(response, ResultJson.success(data));
    }

    /**
     * Get cluster formula data.
     * @param request http request
     * @param response http response
     * @param user the user
     * @return the formula form as Json
     */
    public static String getFormulaData(Request request, Response response, User user) {
        Cluster cluster = getCluster(request, user);

        String formula = request.params("formula");
        Optional<Map<String, Object>> data = FormulaManager.getInstance().getClusterFormulaData(cluster, formula);
        return json(response, ResultJson.success(data.orElse(Collections.emptyMap())));
    }

    /**
     * Schedules refreshing the system group of a cluster.
     * @param request http request
     * @param response http response
     * @param user the user
     * @return the outcome as Json
     */
    public static String refreshGroupNodes(Request request, Response response, User user) {
        Cluster cluster = getCluster(request, user);

        long actionId;
        try {
            actionId = clusterManager.refreshGroup(cluster, user).getId();
            HibernateFactory.getSession().getTransaction().commit();
        }
        catch (Exception e) {
            LOG.error("Scheduling remove node from cluster failed", e);
            return json(response, HttpStatus.SC_INTERNAL_SERVER_ERROR, ResultJson.error(e.getMessage()));
        }
        FlashScopeHelper.flash(request, "Scheduled action to refresh the system group associated with cluster");
        return json(response, ResultJson.success(actionId));
    }

    private static Long getId(Request request) {
        String idStr = request.params("id");
        Long id = null;
        try {
            id = Long.parseLong(idStr);
        }
        catch (Exception e) {
            LOG.error("Id '" + idStr + "' is not a number.");
            halt(HttpStatus.SC_BAD_REQUEST, "id is not a number");
        }
        return id;
    }

    private static Cluster getCluster(Request request, User user) {
        Long id = getId(request);
        Optional<Cluster> cluster = ClusterFactory.findClusterByIdAndOrg(id, user.getOrg());
        if (cluster.isEmpty()) {
            halt(HttpStatus.SC_NOT_FOUND, "Cluster " + id + " not found");
        }
        return cluster.get();
    }


    private static ClusterProvider getClusterProvider(String label) {
        Optional<ClusterProvider> provider = ClusterManager.findClusterProvider(label);
        if (provider.isEmpty()) {
            halt(HttpStatus.SC_NOT_FOUND, "Provider " + label + " not found");
        }
        return provider.get();
    }

}
