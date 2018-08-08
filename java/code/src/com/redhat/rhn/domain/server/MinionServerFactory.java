/**
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

import static java.util.stream.Collectors.toList;

import com.redhat.rhn.common.hibernate.HibernateFactory;
import com.redhat.rhn.domain.action.server.ServerAction;
import com.redhat.rhn.domain.user.User;

import org.apache.log4j.Logger;
import org.hibernate.Criteria;
import org.hibernate.Session;
import org.hibernate.criterion.Projections;
import org.hibernate.criterion.Restrictions;

import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Stream;

/**
 * MinionFactory - the singleton class used to fetch and store
 * com.redhat.rhn.domain.server.MinionServer objects from the database.
 */
public class MinionServerFactory extends HibernateFactory {

    private static Logger log = Logger.getLogger(MinionServerFactory.class);

    /**
     * Lookup all Servers that belong to an org
     * @param orgId the org id to search for
     * @return the Server found
     */
    public static List<MinionServer> lookupByOrg(Long orgId) {
        return (List<MinionServer>) HibernateFactory.getSession()
                .createCriteria(MinionServer.class)
                .setResultTransformer(Criteria.DISTINCT_ROOT_ENTITY)
                .add(Restrictions.eq("org.id", orgId))
                .list();
    }

    /**
     * Lookup all Servers that belong to an org
     * @param user the user servers are visible to
     * @return the Server found
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
                .setResultTransformer(Criteria.DISTINCT_ROOT_ENTITY)
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
                .setResultTransformer(Criteria.DISTINCT_ROOT_ENTITY)
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
        return Optional.ofNullable((MinionServer) getSession().get(MinionServer.class, id));
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
            return Collections.emptyList();
        }
        else {
            return ServerFactory.getSession().createCriteria(MinionServer.class)
                    .setResultTransformer(Criteria.DISTINCT_ROOT_ENTITY)
                    .add(Restrictions.in("minionId", minionIds))
                    .list();
        }
    }

    /**
     * List all the SSH minion ids and their contact methods.
     * @return map of SSH minion id and its contact method
     */
    public static List<MinionServer> listSSHMinions() {
        return ServerFactory.getSession().createCriteria(MinionServer.class)
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
     * Find all Ids of the regular minions involved in one Action.
     *
     * @param actionId the Action id
     * @return a list of regular minions ids involved in the given Action
     */
    @SuppressWarnings("unchecked")
    public static List<MinionIds> findRegularMinionIds(Long actionId) {
        return ((List<Object[]>) HibernateFactory.getSession()
                .getNamedQuery("Action.findRegularMinionIds")
                .setParameter("id", actionId)
                .getResultList()).stream()
                .map(row -> new MinionIds((Long)row[0], row[1].toString()))
                .collect(toList());
    }
}
