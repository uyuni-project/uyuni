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

import com.redhat.rhn.common.hibernate.HibernateFactory;
import org.apache.log4j.Logger;
import org.hibernate.Criteria;
import org.hibernate.Session;
import org.hibernate.criterion.Restrictions;

import java.util.List;
import java.util.Optional;

/**
 * MinionFactory - the singleton class used to fetch and store
 * com.redhat.rhn.domain.server.MinionServer objects from the database.
 */
public class MinionFactory extends HibernateFactory {

    private static Logger log = Logger.getLogger(MinionFactory.class);

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
     * Conversion of server objects into optionals of type MinionServer.
     *
     * @param server a server object
     * @return optional of MinionServer
     */
    public static Optional<MinionServer> asMinionServer(Server server) {
       if (server instanceof MinionServer) {
          return Optional.of((MinionServer) server);
       }
       else {
          return Optional.empty();
       }
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
     * Lookup minions by their id.
     *
     * @param id the id to lookup
     * @return the minion found
     */
    public static Optional<MinionServer> lookupById(Long id) {
        return Optional.ofNullable((MinionServer) getSession().get(MinionServer.class, id));
    }
}
