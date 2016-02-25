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
import org.hibernate.criterion.Projections;
import org.hibernate.criterion.Property;
import org.hibernate.criterion.Restrictions;

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
     * Save a {@link StateRevision}.
     *
     * @param stateRevision the state revision to save
     */
    public static void save(StateRevision stateRevision) {
        singleton.saveObject(stateRevision);
    }

    /**
     * Save a {@link CustomState}.
     * @param customState the salt state to save
     */
    public static void save(CustomState customState) {
        singleton.saveObject(customState);
    }

    /**
     * Lookup the latest set of {@link PackageState} objects for a given server.
     *
     * @param server the server
     * @return the latest package states for this server
     */
    public static Optional<Set<PackageState>> latestPackageStates(Server server) {
        ServerStateRevision revision = latestRevision(server);
        return Optional.ofNullable(revision).map(ServerStateRevision::getPackageStates);
    }

    private static ServerStateRevision latestRevision(Server server) {
        DetachedCriteria maxQuery = DetachedCriteria.forClass(ServerStateRevision.class)
                .add(Restrictions.eq("server", server))
                .setProjection(Projections.max("id"));
        return (ServerStateRevision) getSession()
                .createCriteria(ServerStateRevision.class)
                .add(Restrictions.eq("server", server))
                .add(Property.forName("id").eq(maxQuery))
                .uniqueResult();
    }

    /**
     * Lookup the latest set of custom {@link CustomState} objects for a given server.
     *
     * @param server the server
     * @return the latest custom states for this server
     */
    public static Optional<Set<CustomState>> latestCustomSaltStates(Server server) {
        ServerStateRevision revision = latestRevision(server);
        return Optional.ofNullable(revision).map(ServerStateRevision::getCustomStates);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    protected Logger getLogger() {
        return log;
    }

    /**
     * Get a {@link CustomState} object from the db by name
     * @param name the name of the state to get
     * @return a {@link CustomState} object
     */
    public static CustomState getCustomSaltStateByName(String name) {
        return (CustomState)getSession().createCriteria(CustomState.class)
                .add(Restrictions.eq("stateName", name))
                .uniqueResult();
    }
}
