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
import com.redhat.rhn.domain.rhnpackage.Package;
import com.redhat.rhn.domain.rhnpackage.PackageName;
import com.redhat.rhn.domain.server.Server;

import com.redhat.rhn.domain.server.ServerGroup;
import com.redhat.rhn.domain.user.User;
import com.redhat.rhn.domain.user.UserFactory;

import com.suse.manager.webui.services.StateRevisionService;

import org.apache.log4j.Logger;
import org.hibernate.criterion.DetachedCriteria;
import org.hibernate.criterion.Projections;
import org.hibernate.criterion.Property;
import org.hibernate.criterion.Restrictions;

import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.function.Function;
import java.util.stream.Collectors;

/**
 * Factory class for working with states.
 */
public class StateFactory extends HibernateFactory {

    private static Logger log = Logger.getLogger(StateFactory.class);
    private static StateFactory singleton = new StateFactory();

    /**
     * Holds the result of {@link StateFactory#latestStateRevisionsByCustomState}.
     */
    public static class CustomStateRevisionsUsage {
        private List<ServerStateRevision> serverStateRevisions = new LinkedList<>();
        private List<ServerGroupStateRevision> serverGroupStateRevisions
                = new LinkedList<>();
        private List<OrgStateRevision> orgStateRevisions = new LinkedList<>();

        /**
         * No arg constructor.
         */
        public CustomStateRevisionsUsage() { }

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
     * Lookup the latest state revision of an org.
     * @param org the org
     * @return the optional {@link OrgStateRevision}
     */
    public static Optional<OrgStateRevision> latestStateRevision(Org org) {
        return latestRevision(OrgStateRevision.class, "org", org);
    }

    /**
     * Lookup the latest state revision of an org.
     * @param group the server group
     * @return the optional {@link OrgStateRevision}
     */
    public static Optional<ServerGroupStateRevision> latestStateRevision(
            ServerGroup group) {
        return latestRevision(ServerGroupStateRevision.class, "group", group);
    }

    /**
     * Lookup the latest state revision of a server.
     * @param server the server
     * @return the optional {@link OrgStateRevision}
     */
    public static Optional<ServerStateRevision> latestStateRevision(Server server) {
        return latestRevision(ServerStateRevision.class, "server", server);
    }

    /**
     * Lookup the latest set of custom {@link CustomState} objects for a given server.
     *
     * @param org the organization
     * @return the latest custom states for this server
     */
    public static Optional<Set<CustomState>> latestCustomStates(Org org) {
        Optional<OrgStateRevision> revision = latestRevision(
                OrgStateRevision.class, "org", org);
        return filterDeleted(revision);
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
        return filterDeleted(revision);
    }

    /**
     * Lookup the latest set of custom {@link CustomState} objects for a given server.
     *
     * @param server the server
     * @return the latest custom states for this server
     */
    public static Optional<Set<CustomState>> latestCustomStates(Server server) {
        Optional<ServerStateRevision> revision = latestRevision(
                ServerStateRevision.class, "server", server);
        return filterDeleted(revision);
    }

    private static Optional<Set<CustomState>> filterDeleted(
            Optional<? extends StateRevision> revision) {
        return revision.map(
                r -> r.getCustomStates().stream()
                        .filter(s -> !s.isDeleted())
                        .collect(Collectors.toSet())
        );
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
     * @param orgId the org id
     * @return an {@link Optional} containing a {@link CustomState} object
     */
    public static Optional<CustomState> getCustomStateByName(long orgId, String name) {
        CustomState state = (CustomState)getSession().createCriteria(CustomState.class)
                .add(Restrictions.eq("stateName", name))
                .add(Restrictions.eq("org.id", orgId))
                .add(Restrictions.eq("deleted", false))
                .uniqueResult();
        return Optional.ofNullable(state);
    }

    /**
     * Get a list of {@link CustomState} from the db by organization
     * @param orgId the org id
     * @return a list of {@link CustomState} that belong to the given org
     */
    public static List<CustomState> getCustomStatesByOrg(long orgId) {
        return (List<CustomState>)getSession().createCriteria(CustomState.class)
                .add(Restrictions.eq("org.id", orgId))
                .add(Restrictions.eq("deleted", false))
                .list();
    }

    /**
     * Mark custom state as deleted in the database. Does not actually remove it.
     * @param orgId the org id
     * @param name the custom state name
     */
    public static void removeCustomState(long orgId, String name) {
        Optional<CustomState> customState = getCustomStateByName(orgId, name);
        customState.ifPresent(state ->
                state.setDeleted(true)
        );
    }

    /**
     * Find latest state revisions where a custom state is used.
     * @param orgIdIn the org of the custom state
     * @param stateNameIn the name of the custom state
     * @return a {@link CustomStateRevisionsUsage} bean holding the latest
     * server/group/org revisions where the given custom state is used
     */
    public static CustomStateRevisionsUsage latestStateRevisionsByCustomState(
            long orgIdIn, String stateNameIn) {
        List<Object[]> idList = getSession().getNamedQuery("StateRevision.findStateUsage")
                .setLong("orgId", orgIdIn)
                .setString("stateName", stateNameIn)
                .list();

        CustomStateRevisionsUsage usage = new CustomStateRevisionsUsage();
        for (Object[] ids : idList) {
            Long stateId = (Long)ids[0];

            if (ids[1] != null) {
                ServerStateRevision rev = (ServerStateRevision)getSession()
                        .get(ServerStateRevision.class, stateId);
                usage.getServerStateRevisions().add(rev);
            }
            else if (ids[2] != null) {
                ServerGroupStateRevision rev = (ServerGroupStateRevision)getSession()
                        .get(ServerGroupStateRevision.class, stateId);
                usage.getServerGroupStateRevisions().add(rev);
            }
            else if (ids[3] != null) {
                OrgStateRevision rev = (OrgStateRevision)getSession()
                        .get(OrgStateRevision.class, stateId);
                usage.getOrgStateRevisions().add(rev);
            }
        }
        return usage;
    }

    /**
     * Create a new ServerStateRevision and add the given packages
     * to the new created state (installed, latest) if they are not yet
     * part of the state.
     *
     * @param server the minion
     * @param userId the creator of the state
     * @param pkgs list of packages to add to the state
     */
    public static void addPackagesToNewStateRevision(Server server, Optional<Long> userId,
            List<Package> pkgs) {
        User user = userId.map(UserFactory::lookupById).orElse(server.getCreator());

        ServerStateRevision state = StateRevisionService.INSTANCE
                .cloneLatest(server, user, true, true);

        Set<PackageState> pkgStates = state.getPackageStates();
        Map<PackageName, PackageState> lookup = pkgStates.stream()
                .collect(Collectors.toMap(PackageState::getName, Function.identity()));

        pkgs.stream()
            .filter(pkg -> !lookup.containsKey(pkg.getPackageName()))
            .map(pkg -> {
                PackageState packageState = new PackageState();
                packageState.setStateRevision(state);
                packageState.setName(pkg.getPackageName());
                packageState.setPackageState(PackageStates.INSTALLED);
                packageState.setVersionConstraint(VersionConstraints.LATEST);
                return packageState;
            })
            .forEach(state::addPackageState);
        StateFactory.save(state);
    }
}
