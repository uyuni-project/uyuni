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
package com.suse.manager.webui.services;

import com.redhat.rhn.domain.config.ConfigChannel;
import com.redhat.rhn.domain.org.Org;
import com.redhat.rhn.domain.server.MinionServer;
import com.redhat.rhn.domain.server.ServerGroup;
import com.redhat.rhn.domain.state.OrgStateRevision;
import com.redhat.rhn.domain.state.PackageState;
import com.redhat.rhn.domain.state.ServerGroupStateRevision;
import com.redhat.rhn.domain.state.ServerStateRevision;
import com.redhat.rhn.domain.state.StateFactory;
import com.redhat.rhn.domain.state.StateRevision;
import com.redhat.rhn.domain.user.User;
import com.redhat.rhn.manager.configuration.SaltConfigurable;

import java.util.Collections;
import java.util.HashSet;
import java.util.List;
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
     * @param cloneConfigChannels if config channels should be copied
     * @return a new {@link ServerStateRevision} instance with
     * cloned/copied dependencies
     */
    public ServerStateRevision cloneLatest(MinionServer server, User user,
                                           boolean clonePackageStates,
                                           boolean cloneConfigChannels) {
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

        if (cloneConfigChannels) {
            // config channels can be simply added to the collections because
            // they don't hold any reference to the state revision
            cloneConfigChannels(newRevision, StateFactory.latestConfigChannels(server));
        }

        return newRevision;
    }

    /**
     * Clone the latest state revision for the given configurable.
     * @param entity the configurable entity
     * @param user the user
     * @param clonePackageStates if package states should be copied
     * @param cloneConfigChannels if config channels should be copied
     * @return a new {@link StateRevision} instance with
     * cloned/copied dependencies
     */
    public StateRevision cloneLatest(SaltConfigurable entity, User user, boolean clonePackageStates,
            boolean cloneConfigChannels) {
        StateRevision revision;

        if (entity instanceof MinionServer) {
            revision = this.cloneLatest((MinionServer) entity, user, clonePackageStates, cloneConfigChannels);
        }
        else if (entity instanceof ServerGroup) {
            revision = this.cloneLatest((ServerGroup) entity, user, clonePackageStates, cloneConfigChannels);
        }
        else if (entity instanceof Org) {
            revision = this.cloneLatest((Org) entity, user, clonePackageStates, cloneConfigChannels);
        }
        else {
            revision = null;
        }

        return revision;
    }

    /**
     * Get the latest state revision for the given configurable.
     *
     * @param <T>    the actual type of the state revision
     * @param entity the configurable entity
     * @return the {@link StateRevision} instance
     */
    public <T extends StateRevision> Optional<T> getLatest(SaltConfigurable entity) {
        Optional<T> revision;

        if (entity instanceof MinionServer) {
            revision = (Optional<T>) StateFactory.latestStateRevision((MinionServer) entity);
        }
        else if (entity instanceof ServerGroup) {
            revision = (Optional<T>) StateFactory.latestStateRevision((ServerGroup) entity);
        }
        else if (entity instanceof Org) {
            revision = (Optional<T>) StateFactory.latestStateRevision((Org) entity);
        }
        else {
            revision = Optional.empty();
        }

        return revision;
    }

    /**
     * Clone the latest state revision for the given server group.
     * @param group the server group.
     * @param user the user
     * @param clonePackageStates if package states should be copied
     * @param cloneConfigChannels if config channels should be copied
     * @return a new {@link OrgStateRevision} instance with
     * cloned/copied dependencies
     */
    public ServerGroupStateRevision cloneLatest(ServerGroup group, User user,
                                        boolean clonePackageStates,
                                        boolean cloneConfigChannels) {
        ServerGroupStateRevision newRevision = new ServerGroupStateRevision();
        newRevision.setGroup(group);
        newRevision.setCreator(user);

        if (clonePackageStates) {
            Optional<Set<PackageState>> latestStates = StateFactory
                    .latestPackageStates(group);
            clonePackageStates(latestStates, newRevision);
        }

        if (cloneConfigChannels) {
            cloneConfigChannels(newRevision, StateFactory.latestConfigChannels(group));
        }
        return newRevision;
    }

    /**
     * Clone the latest state revision for the given organization.
     * @param org the organization
     * @param user the user
     * @param clonePackageStates if package states should be copied
     * @param cloneConfigChannels if config channels should be copied
     * @return a new {@link OrgStateRevision} instance with
     * cloned/copied dependencies
     */
    public OrgStateRevision cloneLatest(Org org, User user,
                                            boolean clonePackageStates,
                                            boolean cloneConfigChannels) {
        OrgStateRevision newRevision = new OrgStateRevision();
        newRevision.setOrg(org);
        newRevision.setCreator(user);

        if (clonePackageStates) {
            Optional<Set<PackageState>> latestStates = StateFactory
                    .latestPackageStates(org);
            clonePackageStates(latestStates, newRevision);
        }

        if (cloneConfigChannels) {
            cloneConfigChannels(newRevision, StateFactory.latestConfigChannels(org));
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

    private void cloneConfigChannels(StateRevision newRevision,
            Optional<List<ConfigChannel>> latestChannels) {
        newRevision.getConfigChannels()
                .addAll(latestChannels.orElse(Collections.emptyList()));
    }
}
