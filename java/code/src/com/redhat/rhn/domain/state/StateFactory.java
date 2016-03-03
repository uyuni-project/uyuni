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
import com.redhat.rhn.domain.org.Org;
import com.redhat.rhn.domain.server.Server;

import com.redhat.rhn.domain.server.ServerGroup;
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
     * Lookup the latest set of custom {@link CustomState} objects for a given server.
     *
     * @param org the organization
     * @return the latest custom states for this server
     */
    public static Optional<Set<CustomState>> latestCustomStates(Org org) {
        Optional<OrgStateRevision> revision = latestRevision(OrgStateRevision.class,
                "org", org);
        return revision.map(OrgStateRevision::getCustomStates);
    }

    /**
     * Lookup the latest set of custom {@link CustomState} objects for a given server.
     *
     * @param group the server group
     * @return the latest custom states for this server
     */
    public static Optional<Set<CustomState>> latestCustomStates(ServerGroup group) {
        Optional<ServerGroupStateRevision> revision = latestRevision(
                ServerGroupStateRevision.class, "group", group);
        return revision.map(ServerGroupStateRevision::getCustomStates);
    }

    /**
     * Lookup the latest set of custom {@link CustomState} objects for a given server.
     *
     * @param server the server
     * @return the latest custom states for this server
     */
    public static Optional<Set<CustomState>> latestCustomStates(Server server) {
        Optional<ServerStateRevision> revision = latestRevision(ServerStateRevision.class,
                "server", server);
        return revision.map(ServerStateRevision::getCustomStates);
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
     * Get a {@link CustomState} object from the db by name
     * @param name the name of the state to get
     * @return an {@link Optional} containing a {@link CustomState} object
     */
    public static Optional<CustomState> getCustomStateByName(String name) {
        CustomState state = (CustomState)getSession().createCriteria(CustomState.class)
                .add(Restrictions.eq("stateName", name))
                .uniqueResult();
        return Optional.ofNullable(state);
    }

}
