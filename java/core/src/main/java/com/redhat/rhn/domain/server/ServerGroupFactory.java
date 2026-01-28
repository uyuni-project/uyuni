/*
 * Copyright (c) 2009--2014 Red Hat, Inc.
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
package com.redhat.rhn.domain.server;

import static java.util.stream.Collectors.toList;

import com.redhat.rhn.common.db.datasource.DataResult;
import com.redhat.rhn.common.db.datasource.ModeFactory;
import com.redhat.rhn.common.db.datasource.Row;
import com.redhat.rhn.common.db.datasource.SelectMode;
import com.redhat.rhn.common.hibernate.DuplicateObjectException;
import com.redhat.rhn.common.hibernate.HibernateFactory;
import com.redhat.rhn.domain.dto.SystemGroupID;
import com.redhat.rhn.domain.entitlement.Entitlement;
import com.redhat.rhn.domain.formula.FormulaFactory;
import com.redhat.rhn.domain.org.Org;
import com.redhat.rhn.domain.org.usergroup.UserGroupImpl;
import com.redhat.rhn.domain.org.usergroup.UserGroupMembers;
import com.redhat.rhn.domain.user.User;
import com.redhat.rhn.domain.user.legacy.UserImpl;

import com.suse.manager.webui.services.iface.SaltApi;
import com.suse.salt.netapi.datatypes.target.MinionList;
import com.suse.utils.Opt;

import org.apache.commons.lang3.StringUtils;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.hibernate.Session;
import org.hibernate.type.StandardBasicTypes;

import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.stream.Collectors;

import jakarta.persistence.Tuple;
import jakarta.persistence.criteria.CriteriaBuilder;
import jakarta.persistence.criteria.CriteriaQuery;
import jakarta.persistence.criteria.Root;

/**
 *
 * ServerGroupFactory
 */
public class ServerGroupFactory extends HibernateFactory {

    public static final String NULL_DESCRIPTION = "none";
    public static final ServerGroupFactory SINGLETON = new ServerGroupFactory();
    private static Logger log = LogManager.getLogger(ServerGroupFactory.class);

    @Override
    protected Logger getLogger() {
        return log;
    }

    /**
     * Returns the ServerGroups that the user can administer.
     * @param user User whose ServerGroups are sought.
     * @return the ServerGroups that the user can administer.
     */
    public static List<ServerGroup> listAdministeredServerGroups(User user) {
        Session session = HibernateFactory.getSession();
        return session.createNativeQuery(
                        """
                                SELECT sg.* FROM rhnServerGroup sg, rhnUserManagedServerGroups umsg
                                WHERE umsg.server_group_id = sg.id
                                AND sg.group_type IS NULL
                                AND umsg.user_id = :uid
                                """, ServerGroup.class)
                .addSynchronizedEntityClass(ServerGroup.class)
                .addSynchronizedEntityClass(UserImpl.class)
                .addSynchronizedEntityClass(UserGroupMembers.class)
                .setParameter("uid", user.getId())
                .list();
    }

    /**
     * Returns the ServerGroup that matches a given base entitlement and is
     * compatible to a given server.
     * @param serverId the Id of the server
     * @param baseEnt the base entitlement
     * @return the serverGroup
     */
    public static Optional<ServerGroup> findCompatibleServerGroupForBaseEntitlement(
            Long serverId, Entitlement baseEnt) {
        Session session = HibernateFactory.getSession();
        return session.createNativeQuery(
                        """
                                SELECT sg.* FROM rhnServerGroupType sgt
                                JOIN rhnServerGroup sg ON (sg.group_type = sgt.id)
                                JOIN rhnServer s ON (s.org_id = sg.org_id)
                                JOIN rhnServerServerGroupArchCompat ssgac
                                    ON (ssgac.server_arch_id = s.server_arch_id AND ssgac.server_group_type = sgt.id)
                                WHERE s.id = :sid
                                AND sgt.label = :entitlement_label
                                AND NOT EXISTS
                                    (SELECT 1 FROM rhnServerGroupMembers sgm
                                        WHERE sgm.server_group_id = sg.id
                                        AND sgm.server_id = s.id)
                                """, ServerGroup.class)
                .addSynchronizedEntityClass(ServerGroupType.class)
                .addSynchronizedEntityClass(ServerGroup.class)
                .addSynchronizedEntityClass(Server.class)
                .setParameter("sid", serverId)
                .setParameter("entitlement_label", baseEnt.getLabel())
                .uniqueResultOptional();
    }

    /**
     * Returns the ServerGroup that matches a given addOn entitlement and is
     * compatible to a given base entitlement and a given server.
     * @param serverId the Id of the server
     * @param addOnEnt the addOn entitlement
     * @param baseEntId the Id of the base entitlement
     * @return the serverGroup
     */
    public static Optional<ServerGroup> findCompatibleServerGroupForAddonEntitlement(
            Long serverId, Entitlement addOnEnt, Long baseEntId) {
        Session session = HibernateFactory.getSession();
        return session.createNativeQuery(
                        """
                                SELECT sg.* FROM rhnServerGroupType sgt
                                JOIN rhnServerGroup sg ON (sg.group_type = sgt.id)
                                JOIN rhnSGTypeBaseAddonCompat sgtbac ON (sgtbac.addon_id = sgt.id)
                                JOIN rhnServer s ON (s.org_id = sg.org_id)
                                JOIN rhnServerServerGroupArchCompat ssgac
                                    ON (ssgac.server_arch_id = s.server_arch_id AND ssgac.server_group_type = sgt.id)
                                WHERE s.id = :sid
                                AND sgt.label = :addon_entitlement_label
                                AND sgtbac.base_id = :base_ent_id
                                AND NOT EXISTS
                                        (SELECT 1 FROM rhnServerGroupMembers sgm
                                            WHERE sgm.server_group_id = sg.id
                                            AND sgm.server_id = s.id)
                                """, ServerGroup.class)
                .addSynchronizedEntityClass(ServerGroupType.class)
                .addSynchronizedEntityClass(ServerGroup.class)
                .addSynchronizedEntityClass(Server.class)
                .setParameter("sid", serverId)
                .setParameter("addon_entitlement_label", addOnEnt.getLabel())
                .setParameter("base_ent_id", baseEntId)
                .uniqueResultOptional();
    }

    /**
     * Insert or update a ServerGroup
     * @param group the ServerGroup to save
     * @return the saved {@link ServerGroup} instance
     */
    public static ServerGroup save(ServerGroup group) {
        return SINGLETON.saveObject(group);
    }

    /**
     * Lookup a ServerGroup by ID and organization.
     * @param id Server group id
     * @param org Organization
     * @return Server group requested
     */
    public static ManagedServerGroup lookupByIdAndOrg(Long id, Org org) {
        Session session = HibernateFactory.getSession();
        return session.createQuery(
                    "FROM ManagedServerGroup AS s WHERE s.id = :id AND s.org = :org AND (s.groupType IS NULL)",
                        ManagedServerGroup.class)
                .setParameter("id", id)
                .setParameter("org", org)
                .uniqueResult();
    }

    /**
     * Lookup a ServerGroup by ID
     *
     * @param id the server group id
     * @return server group object
     */
    public static ManagedServerGroup lookupById(Long id) {
        CriteriaBuilder builder = HibernateFactory.getSession().getCriteriaBuilder();
        CriteriaQuery<ManagedServerGroup> query = builder.createQuery(ManagedServerGroup.class);
        Root<ManagedServerGroup> root = query.from(ManagedServerGroup.class);
        query.where(builder.equal(root.get("id"), id));
        return HibernateFactory.getSession().createQuery(query).getSingleResult();
    }

    /**
     * Lookup a ServerGroup by Name and organization.
     * @param name Server group name
     * @param org Organization
     * @return Server group requested
     */

    public static ManagedServerGroup lookupByNameAndOrg(String name, Org org) {
        Session session = HibernateFactory.getSession();
        return session.createQuery(
                        "FROM ManagedServerGroup AS s WHERE s.name = :name AND s.org = :org AND (s.groupType IS NULL)",
                        ManagedServerGroup.class)
                .setParameter("name", name)
                .setParameter("org", org)
                .uniqueResult();
    }
    /**
     * Returns an EntitlementServerGroup for the given org
     * and servergroup type.
     * @param org the org to look at
     * @param typeIn the server group type to look at
     * @return the Server group requested.
     */
    public static EntitlementServerGroup lookupEntitled(Org org,
                                                    ServerGroupType typeIn) {
        if (typeIn == null) {
            String msg = "Invalid argument Null value  passed in for typeIn argument." +
                            " This method only looks up Entitled servergroups.";
            throw new IllegalArgumentException(msg);
        }
        Session session = HibernateFactory.getSession();
        return session.createQuery("FROM EntitlementServerGroup AS s WHERE s.groupType = :groupType AND s.org = :org",
                        EntitlementServerGroup.class)
                .setParameter("groupType", typeIn)
                .setParameter("org", org)
                .uniqueResult();
    }

    /**
     * Retrieves a specific group from the server groups for this org
     * @param ent The entitlement of the desired servergroup
     * @param org The org in which the server group belongs
     * @return Returns the server group if found, null otherwise
     */
    public static EntitlementServerGroup lookupEntitled(Entitlement ent, Org org) {
        Session session = HibernateFactory.getSession();
        return session.createQuery(
                        "FROM EntitlementServerGroup AS s WHERE s.groupType.label = :label AND s.org = :org",
                        EntitlementServerGroup.class)
                .setParameter("label", ent.getLabel())
                .setParameter("org", org)
                .uniqueResult();
    }

    /**
     * Remove an server group
     * @param saltApi the Salt API
     * @param group to remove
     */
    public static void remove(SaltApi saltApi, ServerGroup group) {
        if (group != null) {
            // remove cobbler and monitoring configuration, which is connected with formulas
            FormulaFactory.saveGroupFormulas(group, Collections.emptyList());

            List<String> minions = group.getServers().stream()
                    .map(Server::asMinionServer)
                    .flatMap(Opt::stream)
                    .map(MinionServer::getMinionId)
                    .collect(Collectors.toList());
            SINGLETON.removeObject(group);
            saltApi.refreshPillar(new MinionList(minions));
        }
    }

    /**
     * Creates a new ServerGroup object and
     * persists it to the database before returning it
     * @param name name of the server group (cant be null)
     * @param description description of servergroup (non-null)
     * @param org the org of the server group
     * @return the created server group.
     */
    public static ManagedServerGroup create(String name,
                                String description,
                                Org org) {
        if (StringUtils.isBlank(name)) {
            String msg = "ServerGroup create exception. " +
                           "Null value provided for the non null field -> 'name'.";
            throw new IllegalArgumentException(msg);
        }

        if (StringUtils.isBlank(description)) {
            description = NULL_DESCRIPTION;
        }


        if (org == null) {
            String msg = "ServerGroup create exception. " +
                    "Null value provided for the non null field -> 'org'.";
            throw new IllegalArgumentException(msg);
        }

        if (lookupByNameAndOrg(name, org) == null) {
            ManagedServerGroup sg = new ManagedServerGroup();
            sg.setName(name);
            sg.setDescription(description);
            sg.setOrg(org);
            return (ManagedServerGroup) save(sg);
        }
        String msg = "Duplicate server group requested to be created.." +
                                "Server Group with name -[" + name + "] and" +
                                " org - [" + org.getName() + "] already exists";

        throw new DuplicateObjectException(msg);
    }

    /**
     * Returns a list of ServerGroups that have NO administrators
     * @param org org of the current user.
     * @return the list of servergroups without any admins.
     */
    public static List<ServerGroup> listNoAdminGroups(Org org) {
        Session session = HibernateFactory.getSession();
        return session.createNativeQuery(
                        """
                                SELECT sg.* FROM rhnServerGroup sg
                                WHERE sg.group_type IS NULL
                                AND sg.org_id = :org_id
                                AND NOT EXISTS
                                    (SELECT 1 FROM rhnUserServerGroupPerms usgp WHERE usgp.server_group_id = sg.id)
                                """, ServerGroup.class)
                .addSynchronizedEntityClass(ServerGroup.class)
                .addSynchronizedEntityClass(ManagedServerGroup.class)
                .setParameter("org_id", org.getId())
                .list();
    }

    /**
     * Returns the admins of a given serverGroup.
     * @param sg the serverGroup to find the admins of
     * @return list of User objects that can administer the server group
     */
    public static List<User> listAdministrators(ServerGroup sg) {
        Session session = HibernateFactory.getSession();
        List<UserImpl> userImplList = session.createNativeQuery(
                        """
                                SELECT wc.* FROM web_contact wc
                                INNER JOIN rhnUserManagedServerGroups umsg ON wc.id = umsg.user_id
                                INNER JOIN rhnServerGroup SG ON SG.id = umsg.server_group_id
                                WHERE sg.id = :sgid AND sg.org_id = :org_id
                                """, UserImpl.class)
                .addSynchronizedEntityClass(UserImpl.class)
                .addSynchronizedEntityClass(ServerGroup.class)
                .addSynchronizedEntityClass(UserGroupImpl.class)
                .addSynchronizedEntityClass(UserGroupMembers.class)
                .setParameter("sgid", sg.getId())
                .setParameter("org_id", sg.getOrg().getId())
                .list();

        return userImplList.stream()
                .filter(Objects::nonNull)
                .map(User.class::cast)
                .collect(Collectors.toList());
    }

    /**
     * Returns the servers of a given serverGroup.
     * @param sg the serverGroup to find the servers of
     * @return list of Server objects that are part of
     *                      the server group
     */
    public static List<Server> listServers(ServerGroup sg) {
        Session session = HibernateFactory.getSession();
        List<Tuple> ids = session.createNativeQuery(
                        """
                                SELECT s.id FROM rhnServer s
                                INNER JOIN rhnServerGroupMembers sgm ON s.id = sgm.server_id
                                WHERE sgm.server_group_id = :sgid
                                AND s.org_id = :org_id
                                """, Tuple.class)
                .addSynchronizedEntityClass(Server.class)
                .addSynchronizedEntityClass(ServerGroup.class)
                .setParameter("sgid", sg.getId())
                .setParameter("org_id", sg.getOrg().getId())
                .addScalar("id", StandardBasicTypes.LONG)
                .list();

        List<Long> serverIds = ids.stream().map(t -> t.get(0, Long.class)).collect(toList());
        return ServerFactory.lookupByIds(serverIds);
    }

    /**
     * Returns the list of simple representations of the minions that belong to the passed serverGroup
     * @param serverGroup the serverGroup
     * @return the list of minion representations
     */
    @SuppressWarnings("unchecked")
    public static List<MinionIds> listMinionIdsForServerGroup(ServerGroup serverGroup) {
        Session session = HibernateFactory.getSession();
        List<Tuple> minionIds = session.createNativeQuery(
                        """
                                SELECT m.server_id, m.minion_id FROM suseMinionInfo m
                                JOIN rhnServer s ON m.server_id = s.id
                                JOIN rhnServerGroupMembers sgm ON m.server_id = sgm.server_id
                                WHERE sgm.server_group_id = :sgid
                                AND s.org_id = :org_id
                                """, Tuple.class)
                .addSynchronizedEntityClass(MinionServer.class)
                .addSynchronizedEntityClass(Server.class)
                .addSynchronizedEntityClass(ServerGroup.class)
                .setParameter("sgid", serverGroup.getId())
                .setParameter("org_id", serverGroup.getOrg().getId())
                .addScalar("server_id", StandardBasicTypes.LONG)
                .addScalar("minion_id", StandardBasicTypes.STRING)
                .list();

        return minionIds.stream()
                .map(row -> new MinionIds(row.get(0, Long.class), row.get(1, String.class)))
                .collect(toList());
    }

    /**
     * Returns the value listed by current members column
     * on the rhnServerGroup table. This was made as a query
     * instead of mapping because this column is only updated
     * by the stored procedures dealing with entitlements.
     * @param sg the server group to get the current members of
     * @return the value of the current_members column.
     */
    public static Long getCurrentMembers(ServerGroup sg) {
        Session session = HibernateFactory.getSession();
        Tuple members = session.createNativeQuery(
                        "SELECT current_members FROM rhnServerGroup WHERE id = :sgid", Tuple.class)
                .addSynchronizedEntityClass(ServerGroup.class)
                .setParameter("sgid", sg.getId())
                .addScalar("current_members", StandardBasicTypes.LONG)
                .uniqueResult();

        if (members == null) {
           return 0L;
        }
        return members.get(0, Long.class);
    }

    /**
     * Returns the list of Entitlement ServerGroups  associated to a server.
     * @param org the Org to find the server groups of
     * @return list of EntitlementServerGroup objects that are associated to
     *                      the org.
     */
    public static List<EntitlementServerGroup> listEntitlementGroups(Org org) {
        Session session = HibernateFactory.getSession();
        return session.createQuery(
                        "FROM EntitlementServerGroup AS s WHERE s.org = :org AND (s.groupType IS NOT NULL)",
                        EntitlementServerGroup.class)
                .setParameter("org", org)
                .list();
    }

    /**
     * Returns the list of Managed ServerGroups  associated to a server.
     * @param org the org to find the server groups of
     * @return list of ManagedServerGroup objects that are associated to
     *                      the org.
     */
    public static List<ManagedServerGroup> listManagedGroups(Org org) {
        Session session = HibernateFactory.getSession();
        return session.createQuery(
                        "FROM ManagedServerGroup AS s WHERE s.org = :org AND (s.groupType IS NULL)",
                        ManagedServerGroup.class)
                .setParameter("org", org)
                .list();
    }

    /**
     * Returns a list of active server Ids associated to this servergroup
     * Here active implies that the system has checked in after
     * sysdate - threshold
     * @param sg the server group to check systems on
     * @param threshold the threshold to check on
     * @return the server ids
     */
    public static List<Long> listActiveServerIds(ServerGroup sg, Long threshold) {
        Session session = HibernateFactory.getSession();
        return session.createNativeQuery(
                        """
                                SELECT s.id FROM rhnServerGroupMembers sgm
                                INNER JOIN rhnServer s on sgm.server_id = s.id
                                INNER JOIN rhnServerInfo si on s.id = si.server_id
                                WHERE
                                si.checkin >= current_timestamp - numtodsinterval(:threshold, 'day') AND
                                sgm.server_group_id = :sgid
                                """, Long.class)
                .addSynchronizedEntityClass(Server.class)
                .addSynchronizedEntityClass(ServerInfo.class)
                .setParameter("sgid", sg.getId())
                .setParameter("threshold", threshold)
                .addScalar("id", StandardBasicTypes.LONG)
                .getResultList();
    }

    /**
     * Returns a list of Inactive server Ids associated to this servergroup
     * Here inactive implies that the system has checked in before
     * sysdate - threshold
     * @param sg the server group to check systems on
     * @param threshold the threshold to check on
     * @return the server ids
     */
    public static List<Long> listInactiveServerIds(ServerGroup sg, Long threshold) {
        Session session = HibernateFactory.getSession();
        return session.createNativeQuery(
                        """
                                SELECT s.id FROM rhnServerGroupMembers sgm
                                INNER JOIN rhnServer s ON sgm.server_id = s.id
                                INNER JOIN rhnServerInfo si ON s.id = si.server_id
                                WHERE
                                si.checkin < current_timestamp - numtodsinterval(:threshold, 'day') AND
                                sgm.server_group_id = :sgid
                                """, Long.class)
                .addSynchronizedEntityClass(Server.class)
                .addSynchronizedEntityClass(ServerInfo.class)
                .setParameter("sgid", sg.getId())
                .setParameter("threshold", threshold)
                .addScalar("id", StandardBasicTypes.LONG)
                .getResultList();
    }

    /**
     * Lookup the managed system groups a set of systems are member of, given their system IDs.
     *
     * @param systemIDs the system IDs
     * @return a map containing the managed system group information for each group the passed systems are member of
     */
    public Map<Long, List<SystemGroupID>> lookupManagedSystemGroupsForSystems(List<Long> systemIDs) {
        SelectMode mode = ModeFactory.getMode("SystemGroup_queries", "managed_system_groups_by_system");
        DataResult<Row> dr = mode.execute(systemIDs);
        return dr.stream()
                .collect(Collectors.groupingBy(m -> (Long) m.get("system_id"),
                        Collectors.mapping(
                                m -> new SystemGroupID((Long) m.get("group_id"), (String) m.get("group_name")),
                                Collectors.toList())));
    }
}
