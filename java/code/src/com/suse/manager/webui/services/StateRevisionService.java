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
package com.suse.manager.webui.services;

import com.redhat.rhn.domain.config.ConfigChannel;
import com.redhat.rhn.domain.org.Org;
import com.redhat.rhn.domain.server.Server;
import com.redhat.rhn.domain.server.ServerGroup;
import com.redhat.rhn.domain.state.CustomState;
import com.redhat.rhn.domain.state.OrgStateRevision;
import com.redhat.rhn.domain.state.PackageState;
import com.redhat.rhn.domain.state.ServerGroupStateRevision;
import com.redhat.rhn.domain.state.ServerStateRevision;
import com.redhat.rhn.domain.state.StateFactory;
import com.redhat.rhn.domain.state.StateRevision;
import com.redhat.rhn.domain.user.User;

import java.util.Collections;
import java.util.HashSet;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * Business logic for state revisions.
 */
public enum StateRevisionService {

    // Singleton instance of this class
    INSTANCE;

    /**
     * Clone the latest state revision for the given server.
     * @param server the sever
     * @param user the user
     * @param clonePackageStates if package states should be copied
     * @param cloneCustomStates if custom states should be copied
     * @return a new {@link ServerStateRevision} instance with
     * cloned/copied dependencies
     */
    public ServerStateRevision cloneLatest(Server server, User user,
                                           boolean clonePackageStates,
                                           boolean cloneCustomStates) {
        ServerStateRevision newRevision = new ServerStateRevision();
        newRevision.setServer(server);
        newRevision.setCreator(user);

        if (clonePackageStates) {
            // package states have to be cloned because they
            // hold a reference to the state revision
            Optional<Set<PackageState>> latestStates = StateFactory
                    .latestPackageStates(server);
            clonePackageStates(latestStates, newRevision);
        }

        if (cloneCustomStates) {
            // custom states can be simply added to the collections because
            // they don't hold any reference to the state revision
            cloneCustomStates(newRevision, StateFactory.latestCustomStates(server));
            cloneConfigChannels(newRevision, StateFactory.latestConfigChannels(server));
        }

        return newRevision;
    }

    /**
     * Clone the latest state revision for the given server group.
     * @param group the server group.
     * @param user the user
     * @param clonePackageStates if package states should be copied
     * @param cloneCustomStates if custom states should be copied
     * @return a new {@link OrgStateRevision} instance with
     * cloned/copied dependencies
     */
    public ServerGroupStateRevision cloneLatest(ServerGroup group, User user,
                                        boolean clonePackageStates,
                                        boolean cloneCustomStates) {
        ServerGroupStateRevision newRevision = new ServerGroupStateRevision();
        newRevision.setGroup(group);
        newRevision.setCreator(user);

        if (clonePackageStates) {
            Optional<Set<PackageState>> latestStates = StateFactory
                    .latestPackageStates(group);
            clonePackageStates(latestStates, newRevision);
        }

        if (cloneCustomStates) {
            cloneCustomStates(newRevision, StateFactory.latestCustomStates(group));
        }
        return newRevision;
    }

    /**
     * Clone the latest state revision for the given organization.
     * @param org the organization
     * @param user the user
     * @param clonePackageStates if package states should be copied
     * @param cloneCustomStates if custom states should be copied
     * @return a new {@link OrgStateRevision} instance with
     * cloned/copied dependencies
     */
    public OrgStateRevision cloneLatest(Org org, User user,
                                            boolean clonePackageStates,
                                            boolean cloneCustomStates) {
        OrgStateRevision newRevision = new OrgStateRevision();
        newRevision.setOrg(org);
        newRevision.setCreator(user);

        if (clonePackageStates) {
            Optional<Set<PackageState>> latestStates = StateFactory
                    .latestPackageStates(org);
            clonePackageStates(latestStates, newRevision);
        }

        if (cloneCustomStates) {
            cloneCustomStates(newRevision, StateFactory.latestCustomStates(org));
        }
        return newRevision;
    }

    private void clonePackageStates(
            Optional<Set<PackageState>> packageStatesOpt, StateRevision newRevision) {
        Set<PackageState> packageCopies = packageStatesOpt
            .map(packageStates -> packageStates.stream()
                    .map(p -> {
                        PackageState copy = new PackageState();
                        copy.setName(p.getName());
                        copy.setEvr(p.getEvr());
                        copy.setArch(p.getArch());
                        copy.setStateRevision(newRevision);
                        copy.setPackageState(p.getPackageState());
                        copy.setVersionConstraint(p.getVersionConstraint());
                        return copy;
                    }).collect(Collectors.toSet()))
            .orElseGet(HashSet::new);
        newRevision.setPackageStates(packageCopies);
    }

    private void cloneCustomStates(StateRevision newRevision,
                                   Optional<Set<CustomState>> latestStates)  {
        newRevision.getCustomStates()
                .addAll(latestStates.orElse(Collections.emptySet()));
    }

    private void cloneConfigChannels(StateRevision newRevision,
            Optional<Set<ConfigChannel>> latestChannels) {
        newRevision.getConfigChannels()
                .addAll(latestChannels.orElse(Collections.emptySet()));
    }
}
