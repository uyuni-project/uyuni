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

package com.suse.manager.clusters;

import static com.redhat.rhn.common.hibernate.HibernateFactory.getSession;

import com.redhat.rhn.common.localization.LocalizationService;
import com.redhat.rhn.domain.action.ActionFactory;
import com.redhat.rhn.domain.action.ActionType;
import com.redhat.rhn.domain.action.cluster.BaseClusterModifyNodesAction;
import com.redhat.rhn.domain.action.cluster.ClusterActionCommand;
import com.redhat.rhn.domain.action.cluster.ClusterGroupRefreshNodesAction;
import com.redhat.rhn.domain.formula.FormulaFactory;
import com.redhat.rhn.domain.product.Tuple2;
import com.redhat.rhn.domain.server.ManagedServerGroup;
import com.redhat.rhn.domain.server.MinionServer;
import com.redhat.rhn.domain.server.MinionServerFactory;
import com.redhat.rhn.domain.server.Server;
import com.redhat.rhn.domain.server.ServerFactory;
import com.redhat.rhn.domain.server.ServerGroupFactory;
import com.redhat.rhn.domain.user.User;
import com.redhat.rhn.manager.formula.FormulaManager;
import com.redhat.rhn.manager.formula.FormulaUtil;
import com.redhat.rhn.manager.rhnpackage.PackageManager;
import com.redhat.rhn.manager.system.ServerGroupManager;
import com.redhat.rhn.taskomatic.TaskomaticApiException;
import com.suse.manager.model.clusters.Cluster;
import com.suse.manager.reactor.utils.LocalDateTimeISOAdapter;
import com.suse.manager.reactor.utils.OptionalTypeAdapterFactory;
import com.suse.manager.reactor.utils.ValueMap;
import com.suse.manager.webui.services.iface.SystemQuery;
import com.suse.manager.webui.services.impl.SaltService;
import com.suse.salt.netapi.datatypes.target.MinionList;
import com.suse.utils.Opt;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import org.apache.commons.jexl2.Expression;
import org.apache.commons.jexl2.JexlContext;
import org.apache.commons.jexl2.JexlEngine;
import org.apache.commons.jexl2.JexlException;
import org.apache.commons.jexl2.MapContext;
import org.apache.log4j.Logger;

import java.io.IOException;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.function.Function;
import java.util.stream.Collectors;

public class ClusterManager {

    private static final Logger LOG = Logger.getLogger(ClusterManager.class);

    public static final Gson GSON = new GsonBuilder()
            .registerTypeAdapter(LocalDateTime.class, new LocalDateTimeISOAdapter())
            .registerTypeAdapterFactory(new OptionalTypeAdapterFactory())
            .serializeNulls()
            .create();


    private static volatile ClusterManager instance;
    private SystemQuery systemQuery;
    private ServerGroupManager serverGroupManager;
    private FormulaManager formulaManager;

    /**
     * @return the instance
     */
    public static ClusterManager instance() {
        if (instance == null) {
            synchronized (ClusterManager.class) {
                if (instance == null) {
                    instance = new ClusterManager();
                }
            }
        }
        return instance;
    }

    /**
     * No arg constructor.
     */
    public ClusterManager() {
        this.systemQuery = SaltService.INSTANCE;
        this.serverGroupManager = ServerGroupManager.getInstance();
        this.formulaManager = FormulaManager.getInstance();
    }

    /**
     * Get all installed cluster providers
     * @return list of cluster providers
     */
    public static List<ClusterProvider> findClusterProviders() {
        List<Map<String, Object>> providers = FormulaFactory.getClusterProvidersMetadata();
        return providers.stream().map(m -> {
            ClusterProvider p = new ClusterProvider();
            ValueMap v = new ValueMap(m);
            p.setLabel(v.getValueAsString("label"));
            p.setName(v.getValueAsString("name"));
            p.setDescription(v.getValueAsString("description"));
            return p;
        }).collect(Collectors.toList());
    }

    /**
     * Find cluster provider by label.
     * @param label the label
     * @return a cluster provider
     */
    public static Optional<ClusterProvider> findClusterProvider(String label) {
        return findClusterProviders().stream()
                .filter(provider -> label.equals(provider.getLabel()))
                .findFirst();
    }


    /**
     * Queries the cluster to get the all the nodes and tries to match
     * them with the registered systems.
     * @param cluster the cluster
     * @return a list of cluster nodes
     */
    public List<ClusterNode> listClusterNodes(Cluster cluster) {
        List<ClusterNode> result = new ArrayList<>();

        Optional<Map<String, Object>> settingsFormulaData = formulaManager
                .getClusterFormulaData(cluster, "settings");
        if (settingsFormulaData.isEmpty()) {
            throw new RuntimeException("No settings data found for cluster " + cluster.getLabel());
        }

        ClusterProviderParameters cpp =
                new ClusterProviderParameters(cluster.getProvider(), Optional.of(settingsFormulaData.get()));
        systemQuery.listClusterNodes(cluster.getManagementNode(), cpp).ifPresent(ret -> {
            ret.forEach((k, v) -> result.add(new ClusterNode(k, v)));
        });
        matchClusterNodes(result);
        return result;
    }

    /**
     * Get the nodes that can be joined to the given cluster. For each node it checks the preconditions for joining
     * a cluster required by the cluster provider.
     *
     * @param cluster the cluster
     * @param user the user
     * @return a Map with minion as keys and error/warning messages as values
     */
    public Map<MinionServer, List<Tuple2<String, String>>> getNodesAvailableForJoining(Cluster cluster, User user) {
        return MinionServerFactory.lookupVisibleToUser(user)
                .filter(server -> !ClusterFactory.listAllManagementNodes().contains(server))
                .filter(server -> !ClusterFactory.listAllClustersNodes().contains(server))
                .collect(Collectors.toMap(Function.identity(), minion -> checkJoinPreconditions(cluster, minion)));
    }

    private List<Tuple2<String, String>> checkJoinPreconditions(Cluster cluster, MinionServer minion) {
        Optional<List<String>> requiredPackages = FormulaFactory
                .getClusterProviderMetadata(cluster.getProvider(), "channels:required_packages", List.class)
                .map(m -> (List<String>)m);
        var msgs = new ArrayList<Tuple2<String, String>>();
        requiredPackages.ifPresent(pkgs ->
            pkgs.forEach(pkgName -> {
                boolean pkgInChannels =
                        Optional.ofNullable(PackageManager.lookupEvrIdByPackageName(minion.getId(), pkgName))
                        .map(res -> !res.isEmpty())
                        .orElse(false);
                if (!pkgInChannels) {
                    msgs.add(new Tuple2<>("warning",
                            LocalizationService.getInstance().getMessage(
                                    "cluster.join_precondition_package_name",
                                    pkgName)));
                }
            })
        );
        return msgs;
    }

    /**
     * Get the minion that can be used as management nodes for the given provider.
     * @param provider the provider
     * @return list of minion ids
     */
    public List<String> findManagementNodeByProvider(String provider) {
        Optional<String> value = FormulaFactory.getClusterProviderMetadata(provider,
                "management_node:match", String.class);
        // TODO optimize to match pillar data directly in json files to speed up lookup instead of calling salt
        if (value.isEmpty()) {
            LOG.error("No string value found for path cluster:management_node:match in the '" +
                    provider + "' cluster provider metadata");
            return Collections.emptyList();
        }
        String mgmtNodeTarget = value.get();
        return systemQuery.matchCompoundSync(mgmtNodeTarget);
    }


    /**
     * Adds an existing cluster to the db, creates a system group for it, adds the management node
     * to the group and  schedules a system group refresh to populate the group.
     * @param name name of the cluster
     * @param label label
     * @param description description
     * @param managementNodeId managmenet node
     * @param provider cluster provider label
     * @param managementSettings settings formula values
     * @param user the user
     * @return the new cluster object
     * @throws IOException if saving the formula values failed
     * @throws TaskomaticApiException if the system group refresh could not be schedules
     */
    public Cluster addCluster(String name, String label, String description, long managementNodeId,
                              String provider, Map<String, Object> managementSettings, User user)
            throws IOException, TaskomaticApiException {
        Server managementNode = ServerFactory.lookupById(managementNodeId);
        if (managementNode == null) {
            throw new RuntimeException("Server with id=" + managementNodeId + " not found");
        }
        if (managementNode.asMinionServer().isEmpty()) {
            throw new RuntimeException("Server with id=" + managementNodeId + " is not a minion");
        }

        // find out settings formula from provider metadata
        String settingsFormula = FormulaFactory.getClusterProviderMetadata(provider,
                "formulas:settings:name", String.class)
                .orElseThrow(() -> new RuntimeException(
                        "Missing key formulas:settings:name from metadata.yml of cluster provider " +
                        provider));

        // create corresponding group
        ManagedServerGroup group = serverGroupManager.create(user, getGroupName(name),
                getGroupDescription(label));

        // create cluster
        Cluster cluster = new Cluster();
        cluster.setName(name);
        cluster.setLabel(label);
        cluster.setDescription(description);
        cluster.setProvider(provider);
        cluster.setManagementNode(managementNode.asMinionServer().get());
        cluster.setGroup(group);
        cluster.setOrg(user.getOrg());
        getSession().save(cluster);

        // enable settings formula
        List<String> formulas = new ArrayList<>();
        formulas.addAll(FormulaFactory.getFormulasByGroupId(group.getId()));
        formulas.add(settingsFormula);
        FormulaFactory.saveGroupFormulas(group.getId(), formulas, user.getOrg());

        // save settings data
        Map<String, Object> settingsInNamespace = adjustNamespace(label, "settings", managementSettings);
        FormulaFactory.saveGroupFormulaData(settingsInNamespace, group.getId(), user.getOrg(), settingsFormula);

        // add management node to group
        serverGroupManager.addServers(group, Arrays.asList(managementNode), user);

        // schedule refreshing group nodes
        ClusterActionCommand<ClusterGroupRefreshNodesAction> clusterActionCommand =
                new ClusterActionCommand(Optional.of(user), user.getOrg(),
                        new Date(),
                        null,
                        ActionFactory.TYPE_CLUSTER_GROUP_REFRESH_NODES,
                        managementNode.asMinionServer().get(),
                        cluster,
                        cluster.getGroup().getName(),
                        null);
        clusterActionCommand.store();
        return cluster;
    }

    private Map<String, Object> adjustNamespace(String clusterLabel, String key, Map<String, Object> data) {
        return Collections.singletonMap("mgr_clusters",
                Collections.singletonMap(clusterLabel,
                        Collections.singletonMap(key, data)));
    }

    /**
     * Deletes a cluster from the db.
     * @param cluster the cluster
     * @param user the user
     */
    public void deleteCluster(Cluster cluster, User user) {
        // delete first cluster to avoid non null foreign key error
        getSession().delete(cluster);
        // delete corresponding group
        serverGroupManager.remove(user, cluster.getGroup());
    }

    /**
     * Save cluster formula data.
     * @param cluster the cluster
     * @param formulaKey the formula key used by the cluster provider
     * @param formData the formula data
     * @param user the user
     * @throws IOException if the data could not be saved
     */
    public void saveFormulaData(Cluster cluster, String formulaKey, Map<String, Object> formData,
                                User user) throws IOException {
        ManagedServerGroup group = cluster.getGroup();
        FormulaUtil.ensureUserHasPermissionsOnServerGroup(user, group);
        Optional<String> formulaName = FormulaFactory
                .getClusterProviderFormulaName(cluster.getProvider(), formulaKey);
        if (formulaName.isEmpty()) {
            throw new RuntimeException(
                    "Couldn't find formula with key " + formulaKey + " in cluster provider " + cluster.getProvider());
        }
        Map<String, Object> formDataInNamespace = adjustNamespace(cluster.getLabel(), formulaKey, formData);
        FormulaFactory.saveGroupFormulaData(formDataInNamespace, group.getId(), user.getOrg(), formulaName.get());
        List<String> minionIds = group.getServers().stream()
                .flatMap(s -> Opt.stream(s.asMinionServer()))
                .map(MinionServer::getMinionId).collect(Collectors.toList());
        systemQuery.refreshPillar(new MinionList(minionIds));
    }

    /**
     * Match cluster nodes against registered systems using the machine-id
     * @param clusterNodes cluster nodes
     */
    public void matchClusterNodes(List<ClusterNode> clusterNodes) {
        clusterNodes.forEach(node -> {
            Optional<MinionServer> server = Optional.ofNullable(node.getDetails().get("machine-id"))
                    .map(String.class::cast)
                    .flatMap(machineId -> MinionServerFactory.findByMachineId(machineId));
            node.setServer(server);
        });
    }

    /**
     * Schedules the action to refresh the system group that belongs to a cluster.
     * @param cluster the cluster
     * @param user the user
     * @return the action
     * @throws TaskomaticApiException is the action could not be scheduled
     */
    public ClusterGroupRefreshNodesAction refreshGroup(Cluster cluster, User user) throws TaskomaticApiException {
        ClusterActionCommand<ClusterGroupRefreshNodesAction> cmd =
                new ClusterActionCommand(Optional.of(user), user.getOrg(),
                        new Date(),
                        null,
                        ActionFactory.TYPE_CLUSTER_GROUP_REFRESH_NODES, cluster.getManagementNode(),
                        cluster,
                        cluster.getGroup().getName(),
                        null);
        cmd.store();
        return cmd.getAction();

    }

    /**
     * Schedules an action to join or remove a node from a cluster.
     * @param actionType the action type (join/remove)
     * @param cluster the cluster
     * @param serverIds the server id (optional for remove)
     * @param formulaData the formula data for joining/removing
     * @param earliest the date of execution
     * @param user the user
     * @return the action
     * @throws TaskomaticApiException if the action could not be scheduled
     */
    public BaseClusterModifyNodesAction modifyClusterNodes(ActionType actionType, Cluster cluster,
                                                           List<Long> serverIds, Map<String, Object> formulaData,
                                                           Date earliest, User user) throws TaskomaticApiException {
        ClusterActionCommand<BaseClusterModifyNodesAction> clusterActionCommand =
                new ClusterActionCommand<>(Optional.of(user), user.getOrg(),
                        earliest,
                        null,
                        actionType,
                        cluster.getManagementNode(),
                        cluster,
                        cluster.getGroup().getName(),
                        nodeAction -> {
                            nodeAction.getServerIds().addAll(serverIds);
                            nodeAction.setJsonParams(GSON.toJson(formulaData));
                        });
        clusterActionCommand.store();
        return clusterActionCommand.getAction();
    }

    /**
     * Deserialize action params stored as JSON.
     * @param jsonParams json string
     * @return json as Map
     */
    public Map<String, Object> deserializeJsonParams(String jsonParams) {
        return GSON.fromJson(jsonParams, Map.class);
    }

    /**
     * Evaluate a JEXL expression.
     * @param expr expression
     * @param ctx context for the expression
     * @return evaluation result
     */
    public Object evalExpression(String expr, Map<String, Object> ctx) {
        JexlEngine jexl = new JexlEngine();

        // Create an expression
        Expression jexlExpr = jexl.createExpression(expr);

        // Create a context and add data
        JexlContext jc = new MapContext(ctx);
        try {
            return jexlExpr.evaluate(jc);
        }
        catch (JexlException e) {
            LOG.error("Error evaluating expression: " + expr, e);
            throw new RuntimeException(e);
        }
    }

    /**
     * Creates the initial data for a cluster provider formula. It evaluates the entries from
     * the provider metadata, from formulas:[formula]:data (if present) using the provided context
     * and creates initial data for the form.
     *
     * It checks if "cluster" or "node" are present in the supplied context and contain an id and
     * tries to find the corresponding cluster or server entities in the db.
     *
     * If "cluster" can be resolved it also adds the cluster settings data to the context with
     * the name "cluster_settings".
     *
     * @param provider cluster provider label
     * @param formulaKey formula key
     * @param context context for evaluation
     * @return the initial data for the formula form
     */
    public Map<String, Object> initialFormulaData(String provider, String formulaKey, Map<String, Object> context) {
        Map<String, Object> initialData = new HashMap<>();

        Optional<Map<String, Object>> formulaData = FormulaFactory
                .getClusterProviderMetadata(provider, "formulas:" + formulaKey + ":data", Map.class)
                .map(m -> (Map<String, Object>)m);
        if (formulaData.isPresent()) {
            Map<String, Object> ctx = new HashMap<>();
            Optional.ofNullable(context.get("nodes"))
                    .filter(List.class::isInstance)
                    .map(List.class::cast)
                    .filter(nodeIds -> nodeIds.stream().anyMatch(Number.class::isInstance))
                    .map(nodeIds -> (List<Number>)nodeIds)
                    .map(nodeIds -> nodeIds.stream()
                            .map(id -> MinionServerFactory.lookupById(((Number)id).longValue()))
                            .filter(Optional::isPresent)
                            .map(Optional::get)
                            .collect(Collectors.toList())
                    )
                    .ifPresentOrElse(minions ->
                            ctx.put("nodes", minions),
                            () -> LOG.error("Could not find minions ids: " + context.get("nodes")));

            Optional.ofNullable(context.get("cluster"))
                    .filter(Number.class::isInstance)
                    .map(Number.class::cast)
                    .map(Number::longValue)
                    .flatMap(ClusterFactory::findClusterById)
                    .ifPresentOrElse(cluster -> {
                                ctx.put("cluster", cluster);
                                formulaManager.getClusterFormulaData(cluster, "settings")
                                        .ifPresent(data -> ctx.put("cluster_settings", data));
                            },
                            () -> LOG.error("Could not find cluster id: " + context.get("cluster")));

            Map<String, Object> otherValues = context.entrySet().stream()
                    .filter(e -> !e.getKey().equals("nodes") &&
                            !e.getKey().equals("cluster") &&
                            !e.getKey().equals("cluster_settings"))
                    .collect(Collectors.toMap(Map.Entry::getKey, Map.Entry::getValue));
            ctx.putAll(otherValues);

            evalFormInitialData(initialData, "", formulaData.get(), ctx);

            initialData.putAll(otherValues);
        }
        else {
            initialData.putAll(context);
        }
        return initialData;
    }

    /**
     * Public for unit tests
     * @param out
     * @param dataKey
     * @param data
     * @param context
     */
    public void evalFormInitialData(Map out, String dataKey, Map<String, Object> data, Map<String, Object> context) {
            Map<String, Object> map = (Map<String, Object>)data;
            if ("edit-group".equals(map.get("$type"))) {
                String valueExpr = (String)map.get("$value");
                String itemName = (String)map.get("$item");
                Map<String, Object> prototype = (Map<String, Object>)map.get("$prototype");
                Object value = evalExpression(valueExpr, context);
                if (value instanceof Collection) {
                    List<Map<String, Object>> items = new LinkedList<>();
                    out.put(dataKey, items);
                    for (var item : ((Collection)value)) {
                        Map<String, Object> itemVal = new HashMap<>();
                        prototype.forEach((key, valExpr) -> {
                            Map<String, Object> valCtx = new HashMap<>();
                            valCtx.putAll(context);
                            valCtx.put(itemName, item);
                            if (valExpr instanceof String) {
                                Object val = evalExpression((String)valExpr, valCtx);
                                itemVal.put((String)key, val);
                            }
                            else if (valExpr instanceof Map) {
                                Map nestedVal = new HashMap();
                                evalFormInitialData(nestedVal, key, (Map<String, Object>)valExpr, valCtx);
                                itemVal.putAll(nestedVal);
                            }

                        });
                        items.add(itemVal);
                    }
                }
                else {
                    throw new RuntimeException("$value for edit-group doesn't evaluate to a collection");
                }
            }
            else {
                map.forEach((key, valExpr) -> {
                    if (valExpr instanceof String) {
                        Object val = evalExpression((String) valExpr, context);
                        out.put(key, val);
                    }
                    else if (valExpr instanceof Map) {
                        Map nestedVal = new HashMap();
                        evalFormInitialData(nestedVal, key, (Map<String, Object>)valExpr, context);
                        out.putAll(nestedVal);
                    }
                });
            }
    }

    /**
     * Get state hooks for cluster provider.
     * @param provider cluster provider label
     * @return state hooks as Map (if any)
     */
    public Optional<Map<String, List<String>>> getStateHooks(String provider) {
        return FormulaFactory.getClusterProviderMetadata(provider, "state_hooks", Map.class)
                .map(m -> (Map<String, List<String>>)m);

    }

    /**
     * Get the details fields to show for each cluster node.
     * @param provider cluster provider label
     * @return detail fields to show (if any)
     */
    public Optional<List<String>> getNodesListFields(String provider) {
        return FormulaFactory.getClusterProviderMetadata(provider, "ui:nodes_list:fields", List.class)
                .map(l -> (List<String>)l);
    }

    /**
     * Update cluster name and description.
     * @param cluster cluster
     * @param name new name
     * @param description new description
     */
    public void update(Cluster cluster, String name, String description) {
        cluster.setName(name);
        cluster.setDescription(description);
        getSession().save(cluster);
        cluster.getGroup().setName(getGroupName(name));
        cluster.getGroup().setDescription(getGroupDescription(name));
        ServerGroupFactory.save(cluster.getGroup());
    }

    private String getGroupName(String clusterName) {
        return "Cluster " + clusterName;
    }

    private String getGroupDescription(String clusterName) {
        return "Group for cluster " + clusterName;
    }
}
