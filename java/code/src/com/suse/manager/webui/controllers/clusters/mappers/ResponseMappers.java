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

package com.suse.manager.webui.controllers.clusters.mappers;

import com.redhat.rhn.domain.product.Tuple2;
import com.redhat.rhn.domain.server.ManagedServerGroup;
import com.redhat.rhn.domain.server.MinionServer;
import com.suse.manager.clusters.ClusterNode;
import com.suse.manager.model.clusters.Cluster;
import com.suse.manager.clusters.ClusterProvider;
import com.suse.manager.webui.controllers.clusters.response.ClusterNodeResponse;
import com.suse.manager.webui.controllers.clusters.response.ClusterResponse;
import com.suse.manager.webui.controllers.clusters.response.ClusterProviderResponse;
import com.suse.manager.webui.controllers.clusters.response.MessageResponse;
import com.suse.manager.webui.controllers.clusters.response.ServerGroupResponse;
import com.suse.manager.webui.controllers.clusters.response.ServerResponse;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

/**
 * Utility class to map backend entities into view response beans
 */
public class ResponseMappers {

    private ResponseMappers() { }

    /**
     * Maps a Cluster bean to a response view bean.
     * @param cluster cluster
     * @param provider cluster provider
     * @return a view bean
     */
    public static ClusterResponse toClusterResponse(Cluster cluster, ClusterProvider provider) {
        ClusterResponse response = new ClusterResponse();
        response.setId(cluster.getId());
        response.setName(cluster.getName());
        response.setLabel(cluster.getLabel());
        response.setDescription(cluster.getDescription());
        response.setProvider(toClusterProviderResponse(provider));
        response.setManagementNode(toServerResponse(cluster.getManagementNode()));
        response.setGroup(toServerGroupResponse(cluster.getGroup()));
        return response;
    }

    /**
     * Maps a MinionServer bean to a response view bean.
     * @param server minion
     * @return a view bean
     */
    public static ServerResponse toServerResponse(MinionServer server) {
       return toServerResponse(server, Optional.empty());
    }

    /**
     * Maps a MinionServer bean to a response view bean.
     * @param server minion
     * @param messages optional messages for the minion
     * @return a view bean
     */
    public static ServerResponse toServerResponse(MinionServer server,
                                                  Optional<List<Tuple2<String, String>>> messages) {
        ServerResponse response = new ServerResponse();
        response.setId(server.getId());
        response.setName(server.getName());
        messages.map(msgs -> msgs.stream()
                .map(ResponseMappers::toMessageResponse).collect(Collectors.toList()))
                .ifPresent(msgs -> response.setMessages(msgs));
        return response;
    }

    private static MessageResponse toMessageResponse(Tuple2<String, String> message) {
        return new MessageResponse(message.getA(), message.getB());
    }

    /**
     * Maps a ClusterProvider bean to a response view bean.
     * @param clusterProvider
     * @return a view bean
     */
    public static ClusterProviderResponse toClusterProviderResponse(ClusterProvider clusterProvider) {
        ClusterProviderResponse response = new ClusterProviderResponse();
        response.setLabel(clusterProvider.getLabel());
        response.setName(clusterProvider.getName());
        response.setDescription(clusterProvider.getDescription());
        return response;
    }

    /**
     * Maps a ManagedServerGroup bean to a response view bean.
     * @param group system group
     * @return a view bean
     */
    public static ServerGroupResponse toServerGroupResponse(ManagedServerGroup group) {
        ServerGroupResponse response = new ServerGroupResponse();
        response.setId(group.getId());
        response.setName(group.getName());
        return response;
    }

    /**
     * Maps a ClusterNode bean to a response view bean.
     * @param node system group
     * @param fieldsOpt fields to show
     * @return a view bean
     */
    public static ClusterNodeResponse toClusterNodeResponse(ClusterNode node, Optional<List<String>> fieldsOpt) {
        ClusterNodeResponse response = new ClusterNodeResponse();
        response.setHostname(node.getHostname());
        fieldsOpt.ifPresent(fields -> {
            for (var field : fields) {
                var value = node.getDetails().get(field);
                if (value != null) {
                    response.getDetails().put(field, value);
                }
            }
        });
        node.getServer().ifPresent(srv ->
                response.setServer(toServerResponse(srv)));
        return response;
    }

}
