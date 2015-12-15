/**
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
import com.redhat.rhn.domain.server.Server;

import org.apache.log4j.Logger;
import org.hibernate.criterion.DetachedCriteria;
import org.hibernate.criterion.Order;
import org.hibernate.criterion.Projections;
import org.hibernate.criterion.Property;
import org.hibernate.criterion.Restrictions;

import java.util.List;
import java.util.Optional;
import java.util.Set;

/**
 * Factory class for working with states.
 */
public class StateFactory extends HibernateFactory {

    private static Logger log = Logger.getLogger(StateFactory.class);
    private static StateFactory singleton = new StateFactory();

    private StateFactory() {
    }

    /**
     * Save a {@link PackageState}.
     *
     * @param packageState the package state to save
     */
    public static void save(PackageState packageState) {
        singleton.saveObject(packageState);
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
     * Lookup all package states.
     *
     * @return list of all package states
     */
    @SuppressWarnings("unchecked")
    public static List<PackageState> lookupPackageStates() {
        return getSession().createCriteria(PackageState.class)
                .list();
    }

    /**
     * Lookup all {@link ServerStateRevision} objects for a given server.
     *
     * @param server the server
     * @return the state revisions for this server, newest first
     */
    @SuppressWarnings("unchecked")
    public static List<ServerStateRevision> lookupServerStateRevisions(Server server) {
        return getSession().createCriteria(ServerStateRevision.class)
                .add(Restrictions.eq("server", server))
                .addOrder(Order.desc("created"))
                .list();
    }

    /**
     * Lookup the latest set of {@link PackageState} objects for a given server.
     *
     * @param server the server
     * @return the latest package states for this server
     */
    @SuppressWarnings("unchecked")
    public static Optional<Set<PackageState>> latestPackageStates(Server server) {
        DetachedCriteria maxQuery = DetachedCriteria.forClass(ServerStateRevision.class)
                .add(Restrictions.eq("server", server))
                .setProjection(Projections.max("created"));
        ServerStateRevision rev = (ServerStateRevision) getSession()
                .createCriteria(ServerStateRevision.class)
                .add(Restrictions.eq("server", server))
                .add(Property.forName("created").eq(maxQuery))
                .uniqueResult();
        return Optional.ofNullable(rev.getPackageStates());
    }

    /**
     * Clear all state revisions from the database.
     */
    public static void clearStateRevisions() {
        getSession().getNamedQuery("StateRevision.deleteAll").executeUpdate();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    protected Logger getLogger() {
        return log;
    }
}
