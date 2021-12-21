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
package com.redhat.rhn.domain.state;

import com.redhat.rhn.common.hibernate.HibernateFactory;
import com.redhat.rhn.domain.config.ConfigChannel;
import com.redhat.rhn.domain.org.Org;
import com.redhat.rhn.domain.server.MinionServer;
import com.redhat.rhn.domain.server.ServerGroup;

import org.apache.log4j.Logger;
import org.hibernate.criterion.DetachedCriteria;
import org.hibernate.criterion.Projections;
import org.hibernate.criterion.Property;
import org.hibernate.criterion.Restrictions;

import java.util.LinkedList;
import java.util.List;
import java.util.Optional;
import java.util.Set;

/**
 * Factory class for working with states.
 */
public class StateFactory extends HibernateFactory {

    private static Logger log = Logger.getLogger(StateFactory.class);
    private static StateFactory singleton = new StateFactory();

    /**
     * Holds the result of {@link StateFactory#latestStateRevisionsByConfigChannel}.
     */
    public static class StateRevisionsUsage {
        private List<ServerStateRevision> serverStateRevisions = new LinkedList<>();
        private List<ServerGroupStateRevision> serverGroupStateRevisions
                = new LinkedList<>();
        private List<OrgStateRevision> orgStateRevisions = new LinkedList<>();

        /**
         * No arg constructor.
         */
        public StateRevisionsUsage() { }

        /**
         * @return the server state revisions
         */
        public List<ServerStateRevision> getServerStateRevisions() {
            return serverStateRevisions;
        }

        /**
         * @return the server group state revisions
         */
        public List<ServerGroupStateRevision> getServerGroupStateRevisions() {
            return serverGroupStateRevisions;
        }

        /**
         * @return the org state revisions
         */
        public List<OrgStateRevision> getOrgStateRevisions() {
            return orgStateRevisions;
        }
    }

    private StateFactory() {
    }

    /**
     * Save a {@link StateRevision}.
     *
     * @param stateRevision the state revision to save
     */
    public static void save(StateRevision stateRevision) {
        singleton.saveObject(stateRevision);
    }

    /**
     * Lookup the latest set of {@link PackageState} objects for a given server.
     *
     * @param server the server
     * @return the latest package states for this server
     */
    public static Optional<Set<PackageState>> latestPackageStates(MinionServer server) {
        Optional<ServerStateRevision> revision = latestRevision(ServerStateRevision.class,
                "server", server);
        return revision.map(ServerStateRevision::getPackageStates);
    }

    /**
     * Lookup the latest set of {@link PackageState} objects for a given server group.
     *
     * @param group the server group
     * @return the latest package states for this server group
     */
    public static Optional<Set<PackageState>> latestPackageStates(ServerGroup group) {
        Optional<ServerGroupStateRevision> revision = latestRevision(
                ServerGroupStateRevision.class, "group", group);
        return revision.map(ServerGroupStateRevision::getPackageStates);
    }

    /**
     * Lookup the latest set of {@link PackageState} objects for a given organization.
     *
     * @param org the organization
     * @return the latest package states for this organization
     */
    public static Optional<Set<PackageState>> latestPackageStates(Org org) {
        Optional<OrgStateRevision> revision = latestRevision(OrgStateRevision.class,
                "org", org);
        return revision.map(OrgStateRevision::getPackageStates);
    }

    /**
     * Lookup the latest state revision of an org.
     * @param org the org
     * @return the optional {@link OrgStateRevision}
     */
    public static Optional<OrgStateRevision> latestStateRevision(Org org) {
        return latestRevision(OrgStateRevision.class, "org", org);
    }

    /**
     * Lookup the latest state revision of an org.
     * @param group the server group
     * @return the optional {@link OrgStateRevision}
     */
    public static Optional<ServerGroupStateRevision> latestStateRevision(
            ServerGroup group) {
        return latestRevision(ServerGroupStateRevision.class, "group", group);
    }

    /**
     * Lookup the latest state revision of a server.
     * @param server the server
     * @return the optional {@link OrgStateRevision}
     */
    public static Optional<ServerStateRevision> latestStateRevision(MinionServer server) {
        return latestRevision(ServerStateRevision.class, "server", server);
    }

    /**
     * Lookup the latest set of {@link ConfigChannel} objects for a given server.
     *
     * @param server the server
     * @return the latest config channels for this server
     */
    public static Optional<List<ConfigChannel>> latestConfigChannels(MinionServer server) {
        Optional<ServerStateRevision> revision = latestRevision(
                ServerStateRevision.class, "server", server);
        return Optional
                .ofNullable(revision.map(StateRevision::getConfigChannels).orElse(null));
    }

    /**
     * Lookup the latest set of {@link ConfigChannel} objects for a given server group.
     *
     * @param group the server group
     * @return the latest config channels for this server
     */
    public static Optional<List<ConfigChannel>> latestConfigChannels(ServerGroup group) {
        Optional<ServerGroupStateRevision> revision = latestRevision(
                ServerGroupStateRevision.class, "group", group);
        return Optional
                .ofNullable(revision.map(StateRevision::getConfigChannels).orElse(null));
    }

    /**
     * Lookup the latest set of {@link ConfigChannel} objects for a given org.
     *
     * @param org the organization
     * @return the latest config channels for this server
     */
    public static Optional<List<ConfigChannel>> latestConfigChannels(Org org) {
        Optional<OrgStateRevision> revision = latestRevision(
                OrgStateRevision.class, "org", org);
        return Optional
                .ofNullable(revision.map(StateRevision::getConfigChannels).orElse(null));
    }

    private static <T extends StateRevision> Optional<T> latestRevision(
            Class<T> revisionType, String field, Object bean) {
        DetachedCriteria maxQuery = DetachedCriteria.forClass(revisionType)
                .add(Restrictions.eq(field, bean))
                .setProjection(Projections.max("id"));
        T revision = (T) getSession()
                .createCriteria(revisionType)
                .add(Restrictions.eq(field, bean))
                .add(Property.forName("id").eq(maxQuery))
                .uniqueResult();
        return Optional.ofNullable(revision);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    protected Logger getLogger() {
        return log;
    }

    /**
     * Find latest state revisions where a config channel is used.
     * @param configChannelIn the config channel
     * @return a {@link StateRevisionsUsage} bean holding the latest
     * server/group/org revisions where the given config channel is used
     */
    public static StateRevisionsUsage latestStateRevisionsByConfigChannel(
            ConfigChannel configChannelIn) {
        List<Long[]> idList = getSession().getNamedQuery("StateRevision.findChannelUsage")
                .setParameter("orgId", configChannelIn.getOrgId())
                .setParameter("channelId", configChannelIn.getId())
                .list();

        StateRevisionsUsage usage = new StateRevisionsUsage();
        for (Object[] ids : idList) {
            Long stateId = (Long)ids[0];

            if (ids[1] != null) {
                ServerStateRevision rev =
                        getSession().get(ServerStateRevision.class, stateId);
                usage.getServerStateRevisions().add(rev);
            }
            else if (ids[2] != null) {
                ServerGroupStateRevision rev =
                        getSession().get(ServerGroupStateRevision.class, stateId);
                usage.getServerGroupStateRevisions().add(rev);
            }
            else if (ids[3] != null) {
                OrgStateRevision rev = getSession().get(OrgStateRevision.class, stateId);
                usage.getOrgStateRevisions().add(rev);
            }
        }
        return usage;
    }

    /**
     * List group ids where a given channel is assigned to
     * @param channel the channel
     * @return list of group ids
     */
    public static List<Long> listConfigChannelsSubscribedGroupIds(ConfigChannel channel) {
        return getSession().getNamedQuery("StateRevision.findGroupsAssignedToChannel")
                .setParameter("channelId", channel.getId())
                .list();
    }
}
