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

import com.redhat.rhn.common.hibernate.HibernateFactory;
import com.redhat.rhn.domain.org.Org;
import com.redhat.rhn.domain.server.MinionServer;
import com.redhat.rhn.domain.server.ServerGroup;
import com.redhat.rhn.domain.server.ServerGroupFactory;
import com.suse.manager.model.clusters.Cluster;

import org.apache.log4j.Logger;
import org.hibernate.Criteria;
import org.hibernate.criterion.Restrictions;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import javax.persistence.criteria.CriteriaBuilder;
import javax.persistence.criteria.CriteriaQuery;
import javax.persistence.criteria.Root;

/**
 * Factory class to work with clusters
 */
public class ClusterFactory extends HibernateFactory {

    private static ClusterFactory singleton = new ClusterFactory();
    private static final Logger LOG = Logger.getLogger(ClusterFactory.class);

    private ClusterFactory() {
        super();
    }

    @Override
    protected Logger getLogger() {
        return LOG;
    }

    /**
     * Get cluster by id.
     * @param id id of the cluster
     * @return the cluster if any
     */
    public static Optional<Cluster> findClusterById(long id) {
        return Optional.ofNullable(getSession().get(Cluster.class, id));
    }

    /**
     * Find the cluster that by id and organization
     * @param id id of the cluster
     * @param org the organization
     * @return the cluster if any
     */
    public static Optional<Cluster> findClusterByIdAndOrg(long id, Org org) {
        return Optional.ofNullable((Cluster) getSession().createCriteria(Cluster.class)
                .setResultTransformer(Criteria.DISTINCT_ROOT_ENTITY)
                .add(Restrictions.eq("org.id", org.getId()))
                .add(Restrictions.eq("id", id))
                .uniqueResult());
    }

    /**
     * Find the cluster that owns the given group.
     * @param groupId the group id
     * @return the cluster if any
     */
    public static Optional<Cluster> findClusterByGroupId(Long groupId) {
        return getSession().createNamedQuery("Clusters.findByGroup")
                .setParameter("groupId", groupId)
                .list().stream().findFirst();
    }

    /**
     * Get all clusters of the given org.
     * @param orgId the org
     * @return all clusters belonging to org
     */
    public static List<Cluster> findClustersByOrg(long orgId) {
        return getSession().createNamedQuery("Clusters.findByOrg")
                .setParameter("orgId", orgId)
                .list();
    }

    /**
     * Returns the list of all management nodes
     *
     * @return the list of management nodes
     */
    public static List<MinionServer> listAllManagementNodes() {
        CriteriaBuilder builder = getSession().getCriteriaBuilder();
        CriteriaQuery<MinionServer> criteria = builder.createQuery(MinionServer.class);
        Root<Cluster> root = criteria.from(Cluster.class);
        CriteriaQuery<MinionServer> managementNodes = criteria.select(root.get("managementNode"));
        return getSession().createQuery(managementNodes).getResultList();
    }

    /**
     * Returns the list of all servers that are part of a cluster
     *
     * @return the list of all servers that are part of a cluster
     */
    public static List<MinionServer> listAllClustersNodes() {
        List<MinionServer> ret = new ArrayList<>();
        CriteriaBuilder builder = getSession().getCriteriaBuilder();
        CriteriaQuery<ServerGroup> criteria = builder.createQuery(ServerGroup.class);
        Root<Cluster> root = criteria.from(Cluster.class);
        CriteriaQuery<ServerGroup> managementNodes = criteria.select(root.get("group"));
        List<ServerGroup> serverGroups = getSession().createQuery(managementNodes).getResultList();
        serverGroups.stream()
                .map(sg -> ServerGroupFactory.listServers(sg))
                .flatMap(List::stream)
                .map(s -> s.asMinionServer())
                .forEach(optMinion -> optMinion.ifPresent(minion -> ret.add(minion)));
        return ret;
    }
}

