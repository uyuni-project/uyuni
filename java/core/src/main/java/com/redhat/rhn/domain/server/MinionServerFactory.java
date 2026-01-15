/*
 * Copyright (c) 2016 SUSE LLC
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

import static java.util.Collections.emptyList;
import static java.util.stream.Collectors.toList;

import com.redhat.rhn.common.hibernate.HibernateFactory;
import com.redhat.rhn.domain.action.Action;
import com.redhat.rhn.domain.action.ActionFactory;
import com.redhat.rhn.domain.action.ActionStatus;
import com.redhat.rhn.domain.action.server.ServerAction;
import com.redhat.rhn.domain.org.Org;
import com.redhat.rhn.domain.user.User;
import com.redhat.rhn.manager.entitlement.EntitlementManager;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.hibernate.Session;
import org.hibernate.type.StandardBasicTypes;

import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import jakarta.persistence.NoResultException;
import jakarta.persistence.Tuple;

/**
 * MinionFactory - the singleton class used to fetch and store
 * com.redhat.rhn.domain.server.MinionServer objects from the database.
 */
public class MinionServerFactory extends HibernateFactory {

    private static final Logger LOG = LogManager.getLogger(MinionServerFactory.class);

    /**
     * Lookup all Servers that belong to an org
     * @param org the org to search for
     * @return the Server found
     */
    public static List<MinionServer> lookupByOrg(Org org) {
        return getSession().createQuery("""
                FROM MinionServer
                WHERE org = :org
                """, MinionServer.class)
                .setParameter("org", org)
                .getResultList();
    }

    /**
     * Lookup all Minion Servers that can be managed by the given user
     * @param user the user servers are visible to
     * @return stream of MinionServer found
     */
    public static Stream<MinionServer> lookupVisibleToUser(User user) {
        return user.getServers().stream().flatMap(
                s -> s.asMinionServer().stream()
        );
    }

    @Override
    protected Logger getLogger() {
        return LOG;
    }

    /**
     * Find a registered minion by matching the machineId in the suseMinionInfo table.
     *
     * @param machineId minion machine_id from the grains
     * @return server corresponding to the given machine_id
     */
    public static Optional<MinionServer> findByMachineId(String machineId) {
        Optional<MinionServer> minion;
        try {
            minion =  getSession().createQuery("""
                                                   FROM MinionServer
                                                   WHERE machineId = :machineId
                                                   """, MinionServer.class)
                    .setParameter("machineId", machineId, StandardBasicTypes.STRING)
                    .uniqueResultOptional();
        }
        catch (NoResultException e) {
            minion = Optional.empty();
        }
        return minion;
    }

    /**
     * Find a registered minion by matching the minionId in the suseMinionInfo table.
     *
     * @param minionId minion machine_id from the grains
     * @return server corresponding to the given machine_id
     */
    public static Optional<MinionServer> findByMinionId(String minionId) {
        Optional<MinionServer> minion;
        try {
            minion = getSession().createQuery("""
                    FROM MinionServer WHERE minionId = :minionId
                    """, MinionServer.class)
                    .setParameter("minionId", minionId)
                    .uniqueResultOptional();
        }
        catch (NoResultException e) {
            minion = Optional.empty();
        }
        return minion;
    }

    /**
     * List all minions.
     *
     * @return a list of all minions
     */
    public static List<MinionServer> listMinions() {
        return getSession().createQuery("FROM MinionServer", MinionServer.class)
                .getResultList();
    }

    /**
     * Lookup minions by their id.
     *
     * @param id the id to lookup
     * @return the minion found
     */
    public static Optional<MinionServer> lookupById(Long id) {
        return Optional.ofNullable(getSession().find(MinionServer.class, id));
    }

    /**
     * Lookup multiple minions by id.
     * @param ids minion ids
     * @return the minions found
     */
    public static Stream<MinionServer> lookupByIds(List<Long> ids) {
        return findMinionsByServerIds(ids).stream();
    }

    /**
     * List all the minions by there minionIds
     * @param minionIds set of minion ids
     * @return list of minions
     */
    public static List<MinionServer> lookupByMinionIds(Set<String> minionIds) {
        //NOTE: this is needed since empty sets produce invalid sql statemensts
        if (minionIds.isEmpty()) {
            return emptyList();
        }
        return getSession().createQuery("""
                    FROM MinionServer
                    WHERE minionId IN (:minions)
                    """, MinionServer.class)
                .setParameterList("minions", minionIds, StandardBasicTypes.STRING)
                .getResultList();
    }

    /**
     * List all the SSH minion ids and their contact methods.
     * @return list of SSH minions
     */
    public static List<MinionServer> listSSHMinions() {
        List<ContactMethod> contacts = getSession().createNativeQuery("""
                                      SELECT * from suseServerContactMethod
                                      WHERE label IN (:labels)
                                      """, ContactMethod.class)
                .setParameterList("labels", List.of("ssh-push", "ssh-push-tunnel"), StandardBasicTypes.STRING)
                .getResultList();
        return getSession().createQuery("""
                FROM MinionServer
                WHERE contactMethod IN (:contacts)
                """, MinionServer.class)
                .setParameterList("contacts", contacts)
                .getResultList();
    }

   /**
    * Find all the serverActions that involve a traditional client, given an Action Id.
    *
    * @param actionId the Action Id
    * @return a list of server actions
    */
   public static List<ServerAction> findTradClientServerActions(long actionId) {
       return HibernateFactory.getSession()
               .createQuery("""
                       SELECT sa
                       FROM   ServerAction AS sa
                       JOIN   sa.server AS s
                       WHERE  type(s) != com.redhat.rhn.domain.server.MinionServer
                       AND    action_id = :id
                """, ServerAction.class)
               .setParameter("id", actionId)
               .getResultList();
   }

    /**
     * Retrieve a summary of all the minions involved in one Action.
     *
     * @param actionId the Action id
     * @return a list minion summaries of the minions involved in the given Action
     */
    public static List<MinionSummary> findAllMinionSummaries(Long actionId) {
        return findMinionSummariesInStatus(actionId, ActionFactory.ALL_STATUSES);
    }

    /**
     * Retrieve a summary of the minions involved in one Action that are in the queued status.
     *
     * @param actionId the Action id
     * @return a list minion summaries of the minions involved in the given Action with the queued status
     */
    public static List<MinionSummary> findQueuedMinionSummaries(Long actionId) {
        return findMinionSummariesInStatus(actionId, List.of(ActionFactory.STATUS_QUEUED));
    }

    /**
     * Retrieve a summary of the minions involved in one Action that are in one of the specified statuses
     *
     * @param actionId the Action id
     * @param allowedStatues the list of status to filter the minions
     * @return a list minion summaries of the minions involved in the given Action in one of the specified statuses
     */
    private static List<MinionSummary> findMinionSummariesInStatus(Long actionId, List<ActionStatus> allowedStatues) {
        Session session = HibernateFactory.getSession();

        return session.createQuery("""
                        SELECT new com.redhat.rhn.domain.server.MinionSummary(
                                   sa.server.id,
                                   s.minionId,
                                   s.digitalServerId,
                                   s.machineId,
                                   c.label,
                                   s.os
                                   )
                        FROM   ServerAction AS sa
                        JOIN   sa.server AS s
                        JOIN   s.contactMethod AS c
                        WHERE  type(s) = com.redhat.rhn.domain.server.MinionServer
                        AND    sa.parentAction.id = :id
                        AND    sa.status IN (:allowedStatues)
                        """, MinionSummary.class)
                       .setParameter("id", actionId)
                       .setParameter("allowedStatues", allowedStatues)
                       .getResultList();
    }

    /**
     * Find all minions by their server ids.
     *
     * @param serverIds the list of server ids
     * @return a list of minions
     */
    public static List<MinionServer> findMinionsByServerIds(List<Long> serverIds) {
        return serverIds.isEmpty() ? emptyList() :
                ServerFactory.lookupByServerIds(MinionServer.class, serverIds, """
                        FROM com.redhat.rhn.domain.server.MinionServer AS s WHERE s.id IN (:serverIds)
                        """);
    }

    /**
     * Returns the minion id of a given server.
     * @param serverId the id of the server
     * @return the minion id
     * @throws UnsupportedOperationException if the server is not a salt minion
     */
    public static String getMinionId(Long serverId) throws UnsupportedOperationException {
        return lookupById(serverId)
                .map(MinionServer::getMinionId)
                .orElseThrow(() -> new UnsupportedOperationException("Salt minion not found, id: " + serverId));
    }

    private static List<Long> findSidsByHwAddrs(Set<String> hwAddrs) {
        if (hwAddrs.isEmpty()) {
            return emptyList();
        }

        return getSession().createNativeQuery("""
                                      SELECT * from rhnServerNetInterface
                                      WHERE hw_addr IN (:hwaddr)
                                      """, NetworkInterface.class)
                .setParameterList("hwaddr", hwAddrs, StandardBasicTypes.STRING)
                .getResultList().stream()
                .map(x -> x.getServer().getId()).collect(Collectors.toList());
    }

    /**
     * Find empty profiles with a HW address matching some of given HW addresses.
     *
     * @param hwAddrs the set of HW addresses
     * @return the List of MinionServer with a HW address matching some of given HW addresses
     */
    public static List<MinionServer> findEmptyProfilesByHwAddrs(Set<String> hwAddrs) {
        List<Long> serverIds = findSidsByHwAddrs(hwAddrs);

        return lookupByIds(serverIds)
                .filter(s -> s.hasEntitlement(EntitlementManager.BOOTSTRAP))
                .collect(toList());
    }

    /**
     * Find Salt Minions by a HW address.
     *
     * @param hwAddrs the set of HW addresses
     * @return the List of MinionServer with a HW address matching some of given HW addresses
     */
    public static List<MinionServer> findMinionsByHwAddrs(Set<String> hwAddrs) {
        List<Long> serverIds = findSidsByHwAddrs(hwAddrs);

        return lookupByIds(serverIds)
                .filter(s -> s.hasEntitlement(EntitlementManager.SALT))
                .collect(toList());
    }

    /**
     * Find empty profiles by hostname
     *
     * @param hostname the hostname
     * @return the List of MinionServer matching given hostname
     */
    public static List<MinionServer> findEmptyProfilesByHostName(String hostname) {
        List<MinionServer> servers = getSession().createQuery("""
                FROM MinionServer
                WHERE hostname = :hostname
                """, MinionServer.class)
                .setParameter("hostname", hostname, StandardBasicTypes.STRING)
                .getResultList();

        return servers.stream()
                .filter(s -> s.hasEntitlement(EntitlementManager.BOOTSTRAP))
                .collect(toList());
    }

    /**
     * Returns a list of Ids of the minions that match the passed server Ids
     * @param serverIds the server Ids
     * @return the list of minion Ids
     */
    public static List<MinionIds> findMinionIdsByServerIds(List<Long> serverIds) {
        return getSession().createNativeQuery("""
                SELECT m.server_id, m.minion_id
                FROM   suseMinionInfo m
                WHERE  m.server_id IN (:serverIds)
                """, Tuple.class)
                .setParameterList("serverIds", serverIds)
                .addScalar("server_id", StandardBasicTypes.BIG_INTEGER)
                .addScalar("minion_id", StandardBasicTypes.STRING)
                .stream()
                .map(t -> new MinionIds(t.get(0, Number.class).longValue(), t.get(1, String.class)))
                .collect(Collectors.toList());
    }

    /**
     * findByosServers looks for BYOS servers in a SUMA PAYG scenario given an action
     * @param action action that is going to be carried out
     * @return a list of BYOS servers
     */
    public static List<MinionSummary> findByosServers(Action action) {
        List<MinionSummary> allMinions = MinionServerFactory.findQueuedMinionSummaries(action.getId());
        return allMinions.stream().filter(
                minionSummary -> MinionServerFactory.findByMinionId(minionSummary.getMinionId())
                .map(Server::isDeniedOnPayg)
                .orElse(false)).collect(Collectors.toList());
    }
}
