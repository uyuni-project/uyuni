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
import com.redhat.rhn.domain.user.User;
import com.redhat.rhn.manager.entitlement.EntitlementManager;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.hibernate.Criteria;
import org.hibernate.Session;
import org.hibernate.criterion.CriteriaSpecification;
import org.hibernate.criterion.Projections;
import org.hibernate.criterion.Restrictions;
import org.hibernate.query.Query;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import javax.persistence.criteria.CriteriaBuilder;
import javax.persistence.criteria.CriteriaQuery;
import javax.persistence.criteria.Join;
import javax.persistence.criteria.JoinType;
import javax.persistence.criteria.Predicate;
import javax.persistence.criteria.Root;

/**
 * MinionFactory - the singleton class used to fetch and store
 * com.redhat.rhn.domain.server.MinionServer objects from the database.
 */
public class MinionServerFactory extends HibernateFactory {

    private static Logger log = LogManager.getLogger(MinionServerFactory.class);

    /**
     * Lookup all Servers that belong to an org
     * @param orgId the org id to search for
     * @return the Server found
     */
    public static List<MinionServer> lookupByOrg(Long orgId) {
        return HibernateFactory.getSession()
                .createCriteria(MinionServer.class)
                .setResultTransformer(CriteriaSpecification.DISTINCT_ROOT_ENTITY)
                .add(Restrictions.eq("org.id", orgId))
                .list();
    }

    /**
     * Lookup all Minion Servers that can be managed by the given user
     * @param user the user servers are visible to
     * @return stream of MinionServer found
     */
    public static Stream<MinionServer> lookupVisibleToUser(User user) {
        return user.getServers().stream().flatMap(
                s -> s.asMinionServer().map(Stream::of).orElseGet(Stream::empty)
        );
    }

    @Override
    protected Logger getLogger() {
        return log;
    }

    /**
     * Find a registered minion by matching the machineId in the suseMinionInfo table.
     *
     * @param machineId minion machine_id from the grains
     * @return server corresponding to the given machine_id
     */
    public static Optional<MinionServer> findByMachineId(String machineId) {
        Session session = getSession();
        Criteria criteria = session.createCriteria(MinionServer.class);
        criteria.add(Restrictions.eq("machineId", machineId));
        return Optional.ofNullable((MinionServer) criteria.uniqueResult());
    }

    /**
     * Find a registered minion by matching the minionId in the suseMinionInfo table.
     *
     * @param minionId minion machine_id from the grains
     * @return server corresponding to the given machine_id
     */
    public static Optional<MinionServer> findByMinionId(String minionId) {
        Session session = getSession();
        Criteria criteria = session.createCriteria(MinionServer.class);
        criteria.add(Restrictions.eq("minionId", minionId));
        return Optional.ofNullable((MinionServer) criteria.uniqueResult());
    }

    /**
     * List all minions.
     *
     * @return a list of all minions
     */
    @SuppressWarnings("unchecked")
    public static List<MinionServer> listMinions() {
        return getSession().createCriteria(MinionServer.class)
                .setResultTransformer(CriteriaSpecification.DISTINCT_ROOT_ENTITY)
                .list();
    }

    /**
     * Find all minion ids that belong to an organization.
     *
     * @param orgId the organization id
     * @return a list of minions ids belonging to the given organization
     */
    public static List<String> findMinionIdsByOrgId(Long orgId) {
        return getSession().createCriteria(MinionServer.class)
                .setProjection(Projections.property("minionId"))
                .setResultTransformer(CriteriaSpecification.DISTINCT_ROOT_ENTITY)
                .add(Restrictions.eq("org.id", orgId))
                .list();
    }

    /**
     * Lookup minions by their id.
     *
     * @param id the id to lookup
     * @return the minion found
     */
    public static Optional<MinionServer> lookupById(Long id) {
        return Optional.ofNullable(getSession().get(MinionServer.class, id));
    }

    /**
     * Lookup multiple minions by id.
     * @param ids minion ids
     * @return the minions found
     */
    public static Stream<MinionServer> lookupByIds(List<Long> ids) {
        return ServerFactory.lookupByIds(ids).stream().flatMap(server ->
           server.asMinionServer().map(Stream::of).orElseGet(Stream::empty)
        );
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
        else {
            return HibernateFactory.getSession().createCriteria(MinionServer.class)
                    .setResultTransformer(CriteriaSpecification.DISTINCT_ROOT_ENTITY)
                    .add(Restrictions.in("minionId", minionIds))
                    .list();
        }
    }

    /**
     * List all the SSH minion ids and their contact methods.
     * @return map of SSH minion id and its contact method
     */
    public static List<MinionServer> listSSHMinions() {
        return HibernateFactory.getSession().createCriteria(MinionServer.class)
                .createAlias("contactMethod", "m")
                .add(Restrictions.in("m.label",
                        "ssh-push", "ssh-push-tunnel"))
                .list();
    }

   /**
    * Find all the serverActions that involve a traditional client, given an Action Id.
    *
    * @param actionId the Action Id
    * @return a list of server actions
    */
   @SuppressWarnings("unchecked")
   public static List<ServerAction> findTradClientServerActions(long actionId) {
       return HibernateFactory.getSession()
               .getNamedQuery("Action.findTradClientServerActions")
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

        Query<MinionSummary> query = session.createNamedQuery("Action.findMinionSummaries", MinionSummary.class)
                                            .setParameter("id", actionId)
                                            .setParameter("allowedStatues", allowedStatues);

        return query.getResultList();
    }

    /**
     * Find all minions by their server ids.
     *
     * @param serverIds the list of server ids
     * @return a list of minions
     */
    public static List<MinionServer> findMinionsByServerIds(List<Long> serverIds) {
        return !serverIds.isEmpty() ?
                ServerFactory.lookupByServerIds(serverIds, "Server.findMinionsByServerIds") : emptyList();
    }

    /**
     * Returns the minion id of a given server.
     * @param serverId the id of the server
     * @return the minion id
     * @throws UnsupportedOperationException if the server is not a salt minion
     */
    public static String getMinionId(Long serverId) throws UnsupportedOperationException {
        return ServerFactory.lookupById(serverId)
                .asMinionServer()
                .map(MinionServer::getMinionId)
                .orElseThrow(() -> new UnsupportedOperationException("Salt minion not found, id: " + serverId));
    }

    /**
     * Find empty profiles with a HW address matching some of given HW addresses.
     *
     * @param hwAddrs the set of HW addresses
     * @return the List of MinionServer with a HW address matching some of given HW addresses
     */
    public static List<MinionServer> findEmptyProfilesByHwAddrs(Set<String> hwAddrs) {
        if (hwAddrs.isEmpty()) {
            return emptyList();
        }

        CriteriaBuilder builder = getSession().getCriteriaBuilder();
        CriteriaQuery<MinionServer> query = builder.createQuery(MinionServer.class);
        Root<MinionServer> root = query.distinct(true).from(MinionServer.class);

        Join<MinionServer, NetworkInterface> nicJoin = root.join("networkInterfaces", JoinType.INNER);
        Predicate hwAddrPredicate = nicJoin.get("hwaddr").in(hwAddrs);

        query.where(hwAddrPredicate);

        return getSession().createQuery(query).stream()
                .filter(s -> s.hasEntitlement(EntitlementManager.BOOTSTRAP))
                .collect(toList());
    }

    /**
     * Find empty profiles by hostname
     *
     * @param hostname the hostname
     * @return the List of MinionServer matching given hostname
     */
    public static List<MinionServer> findEmptyProfilesByHostName(String hostname) {
        CriteriaBuilder builder = getSession().getCriteriaBuilder();
        CriteriaQuery<MinionServer> query = builder.createQuery(MinionServer.class);
        Root<MinionServer> root = query.from(MinionServer.class);
        query.where(builder.equal(root.get("hostname"), hostname));

        return getSession().createQuery(query).stream()
                .filter(s -> s.hasEntitlement(EntitlementManager.BOOTSTRAP))
                .collect(toList());
    }

    /**
     * Returns a list of Ids of the minions that match the passed server Ids
     * @param serverIds the server Ids
     * @return the list of minion Ids
     */
    public static List<MinionIds> findMinionIdsByServerIds(List<Long> serverIds) {
        List<Object[]> results = findByIds(serverIds, "Server.findSimpleMinionsByServerIds", "serverIds");
        return results.stream().map(row -> new MinionIds(((BigDecimal) row[0]).longValue(), row[1].toString()))
                .collect(toList());
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
                .map(server -> server.isDeniedOnPayg())
                .orElse(false)).collect(Collectors.toList());
    }
}
