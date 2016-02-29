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

import com.redhat.rhn.domain.server.Server;
import com.redhat.rhn.domain.state.PackageState;
import com.redhat.rhn.domain.state.ServerStateRevision;
import com.redhat.rhn.domain.state.StateFactory;
import com.redhat.rhn.domain.user.User;

import java.util.Collections;
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
            Set<PackageState> packageCopies = StateFactory
                    .latestPackageStates(server)
                    .map(packageStates -> packageStates.stream()
                            .map(p -> new PackageState(p.getName(),
                                    p.getEvr(),
                                    p.getArch(),
                                    newRevision,
                                    p.getPackageStateTypeId(),
                                    p.getVersionConstraintId())
                            ).collect(Collectors.toSet()))
                    .orElse(Collections.emptySet());
            newRevision.setPackageStates(packageCopies);
        }

        if (cloneCustomStates) {
            // custom states can be simply added to the collections because
            // they don't hold any reference to the state revision
            newRevision.getCustomStates().addAll(
                    StateFactory.latestCustomtates(server)
                            .orElse(Collections.emptySet()));
        }

        return newRevision;
    }

}
