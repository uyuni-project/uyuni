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

package com.redhat.rhn.frontend.xmlrpc.cluster;

import com.redhat.rhn.common.hibernate.HibernateFactory;
import com.redhat.rhn.domain.action.ActionFactory;
import com.redhat.rhn.domain.server.MinionServer;
import com.redhat.rhn.domain.server.MinionServerFactory;
import com.redhat.rhn.domain.user.User;
import com.redhat.rhn.frontend.xmlrpc.BaseHandler;
import com.redhat.rhn.frontend.xmlrpc.NoSuchClusterException;
import com.redhat.rhn.frontend.xmlrpc.NoSuchSystemException;
import com.redhat.rhn.taskomatic.TaskomaticApiException;
import com.suse.manager.clusters.ClusterFactory;
import com.suse.manager.clusters.ClusterManager;
import com.suse.manager.clusters.ClusterNode;
import com.suse.manager.model.clusters.Cluster;

import org.apache.log4j.Logger;

import java.io.IOException;
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;
import java.util.stream.Stream;

/**
 * ClusterHandler
 * @xmlrpc.namespace cluster
 * @xmlrpc.doc Provides methods to access and modify clusters.
 */
public class ClusterHandler extends BaseHandler {

    private static final Logger LOG = Logger.getLogger(ClusterHandler.class);
    private final ClusterManager clusterManager;

    /**
     * @param clusterManagerIn
     */
    public ClusterHandler(ClusterManager clusterManagerIn) {
        this.clusterManager = clusterManagerIn;
    }

    /**
     * List all clusters for the user's org
     *
     * @param loggedInUser The current user
     * @return Returns an array of cluster names
     *
     * @xmlrpc.doc List all clusters for the user's org
     * @xmlrpc.param #param("string", "sessionKey")
     * @xmlrpc.returntype
     *      #return_array_begin()
     *          #struct_begin("cluster_id")
     *                 #prop("string", "cluster_name")
     *          #struct_end()
     *      #array_end()
     */
    public Map<Long, String> listClusters(User loggedInUser) {
        Map<Long, String> ret = new HashMap<>();
        ClusterFactory.findClustersByOrg(loggedInUser.getOrg().getId())
                .forEach(cluster -> {
            ret.put(cluster.getId(), cluster.getName());
        });
        return ret;
    }

    /**
     * Add a cluster.
     *
     * @param loggedInUser The current user
     * @param name of the cluster
     * @param label label for the cluster
     * @param description description for the cluster
     * @param managementNodeId the system id of the management node
     * @param provider the cluster provider (e.g. "caasp")
     * @param managementSettings the settings for the node management
     * @return Returns a 1 if successful, exception otherwise
     *
     * @xmlrpc.doc Add a cluster
     * @xmlrpc.param #param("string", "sessionKey")
     * @xmlrpc.param #param("string", "name")
     * @xmlrpc.param #param("string", "description")
     * @xmlrpc.param #param("integer", "management node id")
     * @xmlrpc.param #param("string", "cluster provider")
     * @xmlrpc.param struct containing the values for each field in the form
     * @xmlrpc.returntype #return_int_success()
     */
    public Integer addCluster(User loggedInUser, String name, String label, String description,
                              Integer managementNodeId, String provider, Map<String, Object> managementSettings) {
        ensureClusterAdmin(loggedInUser);
        Cluster ret = null;
        try {
            clusterManager.addCluster(name, label, description,
                    managementNodeId, provider, managementSettings, loggedInUser);
            return 1;
        }
        catch (IOException e) {
            LOG.error(e);
            return -1;
        }
        catch (TaskomaticApiException e) {
            LOG.error(e);
            throw new com.redhat.rhn.frontend.xmlrpc.TaskomaticApiException(e.getMessage());
        }
    }

    /**
     * Delete cluster given the ID.
     *
     * @param loggedInUser The current user
     * @param clusterId    The id of the cluster
     * @return Returns a 1 if successful, exception otherwise
     *
     * @xmlrpc.doc Delete a cluster given the ID
     * @xmlrpc.param #param("string", "sessionKey")
     * @xmlrpc.param #param("int", "clusterId")
     * @xmlrpc.returntype #return_int_success()
     */
    public Integer deleteCluster(User loggedInUser, Integer clusterId) throws NoSuchClusterException {
        ensureClusterAdmin(loggedInUser);
        Optional<Cluster> ret = ClusterFactory.findClusterByIdAndOrg(clusterId, loggedInUser.getOrg());
        if (ret.isPresent()) {
            clusterManager.deleteCluster(ret.get(), loggedInUser);
            return 1;
        }
        else {
            throw new NoSuchClusterException("Cluster not found");
        }
    }

    /**
     * Lists the nodes of a cluster.
     *
     * @param loggedInUser The current user
     * @param clusterId    The id of the cluster
     * @return Returns an array of node hostnames
     *
     * @xmlrpc.doc Lists the nodes of a cluster.
     * @xmlrpc.param #param("string", "sessionKey")
     * @xmlrpc.param #param("int", "clusterId")
     * @xmlrpc.returntype #array_single("string", "hostname")
     */
    public List<String> listClusterNodes(User loggedInUser, Integer clusterId) {
        return ClusterFactory.findClusterByIdAndOrg(clusterId, loggedInUser.getOrg())
                .map(cluster -> {
                    return clusterManager.listClusterNodes(cluster).stream()
                            .map(ClusterNode::getHostname).collect(Collectors.toList());
                }).orElseThrow(() -> new NoSuchClusterException("Cluster not found"));
    }

    /**
     * Schedule the execution of joining the node(s) to a cluster
     *
     * @param loggedInUser       The current user
     * @param clusterId          The id of the cluster that will have the new node onboard
     * @param targetNodesSystemIds The system IDs of the node that will join the cluster
     * @param formulaData        Map containing the values for each field in the form.
     * @param earliestOccurrence Earliest occurrence of the join action
     * @return Returns a 1 if successful, exception otherwise
     *
     * @xmlrpc.doc Schedule the execution of a node join the cluster
     * @xmlrpc.param #param("string", "sessionKey")
     * @xmlrpc.param #param("int", "clusterId")
     * @xmlrpc.param #array_single("integer", "systemIds", "system IDs of the target nodes")
     * @xmlrpc.param struct containing the values for each field in the form
     * @xmlrpc.param #param_desc("dateTime.iso8601", "earliestOccurrence", "earliest the action can run")
     * @xmlrpc.returntype #return_int_success()
     */
    public Integer scheduleJoinNodeToCluster(User loggedInUser, Integer clusterId, List<Integer> targetNodesSystemIds,
                                             Map<String, Object> formulaData,
                                             Date earliestOccurrence) {
        ensureClusterAdmin(loggedInUser);
        List<MinionServer> targetNodes = MinionServerFactory.lookupByIds(targetNodesSystemIds.stream()
                .map(Long::valueOf)
                .collect(Collectors.toList())).collect(Collectors.toList());
        // minions visible to user will be tested in getNodesAvailableForJoining

        Optional<Cluster> optCluster = ClusterFactory.findClusterByIdAndOrg(clusterId, loggedInUser.getOrg())
                .stream().findFirst();
        if (optCluster.isPresent()) {
            if (clusterManager.getNodesAvailableForJoining(optCluster.get(),
                    loggedInUser).keySet().containsAll(targetNodes)) {
                try {
                   clusterManager.modifyClusterNodes(ActionFactory.TYPE_CLUSTER_JOIN_NODE,
                                    optCluster.get(),
                                    targetNodes.stream().map(m -> m.getId())
                                            .collect(Collectors.toList()),
                                    formulaData, earliestOccurrence,
                                    loggedInUser);
                    HibernateFactory.getSession().getTransaction().commit();
                    return 1;
                }
                catch (TaskomaticApiException e) {
                    LOG.error(e);
                    throw new com.redhat.rhn.frontend.xmlrpc.TaskomaticApiException(e.getMessage());
                }
            }
            else {
                throw new NoSuchSystemException("Minion(s) not authorized to join");
            }
        }
        else {
            throw new NoSuchClusterException("Cluster not found");
        }
    }

    /**
     * Schedule the execution of node(s) removal from a cluster.
     *
     * @param loggedInUser       The current user
     * @param clusterId          The id of the cluster that will have the nodes removed
     * @param targetNodesSystemIds The system IDs of the node that will be removed from the cluster
     * @param formulaData        Map containing the values for each field in the form.
     * @param earliestOccurrence Earliest occurrence of the removal action
     * @return Returns a 1 if successful, exception otherwise
     *
     * @xmlrpc.doc Schedule the execution of a node join the cluster
     * @xmlrpc.param #param("string", "sessionKey")
     * @xmlrpc.param #param("int", "clusterId")
     * @xmlrpc.param #array_single("integer", "systemIds", "system IDs of the target nodes")
     * @xmlrpc.param struct containing the values for each field in the form
     * @xmlrpc.param #param_desc("dateTime.iso8601", "earliestOccurrence", "earliest the action can run")
     * @xmlrpc.returntype #return_int_success()
     */
    public Integer scheduleRemoveNodeFromCluster(User loggedInUser, Integer clusterId,
                                                 List<Integer> targetNodesSystemIds, Map<String, Object> formulaData,
                                                 Date earliestOccurrence) {
        ensureClusterAdmin(loggedInUser);
        Optional<Cluster> optCluster = ClusterFactory.findClusterByIdAndOrg(clusterId, loggedInUser.getOrg());
        if (optCluster.isPresent()) {
            List<MinionServer> availableToUser = MinionServerFactory.lookupVisibleToUser(loggedInUser)
                    .collect(Collectors.toList());
            List<MinionServer> clusterNodes = clusterManager
                    .listClusterNodes(optCluster.get()).stream()
                    .map(clusterNode -> clusterNode.getServer())
                    .flatMap(o -> o.map(Stream::of).orElseGet(Stream::empty))
                    .collect(Collectors.toList());
            List<Long> targetNodesId = MinionServerFactory.lookupByIds(targetNodesSystemIds.stream()
                    .map(Long::valueOf)
                    .collect(Collectors.toList()))
                    .filter(server -> availableToUser.contains(server))
                    .filter(server -> clusterNodes.contains(server))
                    .map(server -> server.getId())
                    .collect(Collectors.toList());
            try {
                clusterManager.modifyClusterNodes(ActionFactory.TYPE_CLUSTER_REMOVE_NODE,
                                optCluster.get(),
                                targetNodesId, formulaData,
                                earliestOccurrence,
                                loggedInUser);
                HibernateFactory.getSession().getTransaction().commit();
                return 1;
            }
            catch (TaskomaticApiException e) {
                LOG.error(e);
                throw new com.redhat.rhn.frontend.xmlrpc.TaskomaticApiException(e.getMessage());
            }
        }
        else {
            throw new NoSuchClusterException("Cluster not found");
        }
    }

    /**
     * Schedule the execution of cluster upgrade.
     *
     * @param loggedInUser       The current user
     * @param clusterId          The id of the cluster that will be upgraded
     * @param formulaData        Map containing the values for each field in the form.
     * @param earliestOccurrence Earliest occurrence of the upgrade action
     * @return Returns a 1 if successful, exception otherwise
     *
     * @xmlrpc.doc Schedule the execution of a cluster upgrade
     * @xmlrpc.param #param("string", "sessionKey")
     * @xmlrpc.param #param("int", "clusterId")
     * @xmlrpc.param struct containing the values for each field in the form
     * @xmlrpc.param #param_desc("dateTime.iso8601", "earliestOccurrence", "earliest the action can run")
     * @xmlrpc.returntype #return_int_success()

     */
    public Integer scheduleUpgradeCluster(User loggedInUser, Integer clusterId, Map<String, Object> formulaData,
                                          Date earliestOccurrence) {
        ensureClusterAdmin(loggedInUser);
        Optional<Cluster> optCluster = ClusterFactory.findClusterByIdAndOrg(clusterId, loggedInUser.getOrg());
        if (optCluster.isPresent()) {
            try {
                clusterManager.modifyClusterNodes(ActionFactory.TYPE_CLUSTER_UPGRADE_CLUSTER,
                        optCluster.get(), Collections.emptyList(),
                        formulaData, earliestOccurrence, loggedInUser);
                HibernateFactory.getSession().getTransaction().commit();
                return 1;
            }
            catch (TaskomaticApiException e) {
                LOG.error(e);
                throw new com.redhat.rhn.frontend.xmlrpc.TaskomaticApiException(e.getMessage());
            }
        }
        else {
            throw new NoSuchClusterException("Cluster not found");
        }
    }
}
