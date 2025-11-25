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
 * SPDX-License-Identifier: GPL-2.0-only
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
        return query.uniqueResultOptional();
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
        return query.uniqueResultOptional();
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
        return query.uniqueResultOptional();
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
    public static StateRevisionsUsage latestStateRevisionsByConfigChannel(ConfigChannel configChannelIn) {

        List<Long[]> idList = getSession().createNativeQuery("""
            SELECT srcc.state_revision_id AS state_revision_id, srvrev.entity_id AS server_id,
                    grprev.entity_id AS group_id, orgrev.entity_id AS org_id
                    FROM rhnconfigchannel cc
                    INNER JOIN suseStateRevisionConfigChannel srcc ON cc.id=srcc.config_channel_id
                    LEFT JOIN (
                        SELECT ssr.state_revision_id AS state_revision_id, ssr.server_id AS entity_id
                        FROM suseServerStateRevision ssr
                        WHERE ssr.state_revision_id =
                            (SELECT max(ssrmax.state_revision_id) FROM suseServerStateRevision ssrmax
                            WHERE ssrmax.server_id=ssr.server_id)
                    ) srvrev ON srvrev.state_revision_id = srcc.state_revision_id
                    LEFT JOIN (
                        SELECT sgsr.state_revision_id AS state_revision_id, sgsr.group_id AS entity_id
                        FROM suseServerGroupStateRevision sgsr
                        WHERE sgsr.state_revision_id =
                            (SELECT max(sgsrmax.state_revision_id) FROM suseServerGroupStateRevision sgsrmax
                            WHERE sgsrmax.group_id=sgsr.group_id)
                    ) grprev ON grprev.state_revision_id = srcc.state_revision_id
                    LEFT JOIN (
                        SELECT osr.state_revision_id AS state_revision_id, osr.org_id AS entity_id
                        FROM suseOrgStateRevision osr
                        WHERE osr.state_revision_id =
                            (SELECT max(osrmax.state_revision_id) FROM suseOrgStateRevision osrmax
                            WHERE osrmax.org_id=osr.org_id)
                    ) orgrev ON orgrev.state_revision_id=srcc.state_revision_id
                    WHERE cc.org_id = :orgId AND cc.id = :channelId
                    AND (srvrev.entity_id IS NOT NULL OR grprev.entity_id IS NOT NULL OR orgrev.entity_id IS NOT NULL)
                   """)
                .setParameter("orgId", configChannelIn.getOrgId())
                .setParameter("channelId", configChannelIn.getId())
                .addScalar("state_revision_id", StandardBasicTypes.LONG)
                .addScalar("server_id", StandardBasicTypes.LONG)
                .addScalar("group_id", StandardBasicTypes.LONG)
                .addScalar("org_id", StandardBasicTypes.LONG)
                .list();

        StateRevisionsUsage usage = new StateRevisionsUsage();
        for (Object[] ids : idList) {
            Long stateId = (Long)ids[0];

            if (ids[1] != null) {
                ServerStateRevision rev =
                        getSession().find(ServerStateRevision.class, stateId);
                usage.getServerStateRevisions().add(rev);
            }
            else if (ids[2] != null) {
                ServerGroupStateRevision rev =
                        getSession().find(ServerGroupStateRevision.class, stateId);
                usage.getServerGroupStateRevisions().add(rev);
            }
            else if (ids[3] != null) {
                OrgStateRevision rev = getSession().find(OrgStateRevision.class, stateId);
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
        return getSession().createNativeQuery("""
                        SELECT latest.group_id FROM
                            (SELECT max(gsr.state_revision_id) state_revision_id, gsr.group_id
                            FROM suseServerGroupStateRevision gsr GROUP BY gsr.group_id) latest
                        JOIN susestaterevisionconfigchannel cc ON latest.state_revision_id = cc.state_revision_id
                        WHERE cc.config_channel_id = :channelId
                        """)
                .setParameter("channelId", channel.getId())
                .addScalar("group_id", StandardBasicTypes.LONG)
                .list();
    }
}
