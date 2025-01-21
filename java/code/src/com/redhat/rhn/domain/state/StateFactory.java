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

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.hibernate.query.Query;
import org.hibernate.type.StandardBasicTypes;

import java.util.LinkedList;
import java.util.List;
import java.util.Optional;
import java.util.Set;

import javax.persistence.NoResultException;

/**
 * Factory class for working with states.
 */
public class StateFactory extends HibernateFactory {

    private static Logger log = LogManager.getLogger(StateFactory.class);
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
     * Lookup the latest state revision of an org.
     * @param org the org
     * @return the optional {@link OrgStateRevision}
     */
    public static Optional<OrgStateRevision> latestStateRevision(Org org) {
        String sql =
                """
                        SELECT DISTINCT ON (org_id) *, null as created, null as creator_id FROM
                        suseOrgStateRevision WHERE org_id = :org
                        ORDER BY org_id, state_revision_id desc limit 1;
                """;
        Query<OrgStateRevision> query = getSession().createNativeQuery(sql, OrgStateRevision.class);
        query.setParameter("org", org.getId(), StandardBasicTypes.LONG);
        try {
            return Optional.ofNullable(query.getSingleResult());
        }
        catch (NoResultException e) {
            return Optional.empty();
        }
    }

    /**
     * Lookup the latest state revision of an org.
     * @param group the server group
     * @return the optional {@link OrgStateRevision}
     */
    public static Optional<ServerGroupStateRevision> latestStateRevision(
            ServerGroup group) {
        String sql =
                """
                        SELECT DISTINCT ON (group_id) *, null as created, null as creator_id FROM
                        suseServerGroupStateRevision WHERE group_id = :group
                        ORDER BY group_id, state_revision_id desc limit 1;
                """;
        Query<ServerGroupStateRevision> query = getSession().createNativeQuery(sql, ServerGroupStateRevision.class);
        query.setParameter("group", group.getId(), StandardBasicTypes.LONG);
        try {
            return Optional.ofNullable(query.getSingleResult());
        }
        catch (NoResultException e) {
            return Optional.empty();
        }
    }

    /**
     * Lookup the latest state revision of a server.
     * @param server the server
     * @return the optional {@link OrgStateRevision}
     */
    public static Optional<ServerStateRevision> latestStateRevision(MinionServer server) {
        String sql =
                """
                        SELECT DISTINCT ON (server_id) *, null as created, null as creator_id FROM
                        suseServerStateRevision WHERE server_id = :server
                        ORDER BY server_id, state_revision_id desc limit 1;
                """;
        Query<ServerStateRevision> query = getSession().createNativeQuery(sql, ServerStateRevision.class);
        query.setParameter("server", server.getId(), StandardBasicTypes.LONG);
        try {
            return Optional.ofNullable(query.getSingleResult());
        }
        catch (NoResultException e) {
            return Optional.empty();
        }
    }

    /**
     * Lookup the latest set of {@link PackageState} objects for a given server.
     *
     * @param server the server
     * @return the latest package states for this server
     */
    public static Optional<Set<PackageState>> latestPackageStates(MinionServer server) {
        Optional<ServerStateRevision> servers = latestStateRevision(server);
        return servers.map(StateRevision::getPackageStates);
    }

    /**
     * Lookup the latest set of {@link PackageState} objects for a given server group.
     *
     * @param group the server group
     * @return the latest package states for this server group
     */
    public static Optional<Set<PackageState>> latestPackageStates(ServerGroup group) {
        Optional<ServerGroupStateRevision> groups = latestStateRevision(group);
        return groups.map(StateRevision::getPackageStates);
    }

    /**
     * Lookup the latest set of {@link PackageState} objects for a given organization.
     *
     * @param org the organization
     * @return the latest package states for this organization
     */
    public static Optional<Set<PackageState>> latestPackageStates(Org org) {
        Optional<OrgStateRevision> orgs = latestStateRevision(org);
        return orgs.map(StateRevision::getPackageStates);
    }



    /**
     * Lookup the latest set of {@link ConfigChannel} objects for a given server.
     *
     * @param server the server
     * @return the latest config channels for this server
     */
    public static Optional<List<ConfigChannel>> latestConfigChannels(MinionServer server) {
        Optional<ServerStateRevision> servers = latestStateRevision(server);
        return servers.map(StateRevision::getConfigChannels);
    }

    /**
     * Lookup the latest set of {@link ConfigChannel} objects for a given server group.
     *
     * @param group the server group
     * @return the latest config channels for this server
     */
    public static Optional<List<ConfigChannel>> latestConfigChannels(ServerGroup group) {
        Optional<ServerGroupStateRevision> groups = latestStateRevision(group);
        return groups.map(StateRevision::getConfigChannels);
    }

    /**
     * Lookup the latest set of {@link ConfigChannel} objects for a given org.
     *
     * @param org the organization
     * @return the latest config channels for this server
     */
    public static Optional<List<ConfigChannel>> latestConfigChannels(Org org) {
        Optional<OrgStateRevision> orgs = latestStateRevision(org);
        return orgs.map(StateRevision::getConfigChannels);
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
