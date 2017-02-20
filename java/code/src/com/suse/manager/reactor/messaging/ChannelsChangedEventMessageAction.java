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
package com.suse.manager.reactor.messaging;

import com.redhat.rhn.common.messaging.EventMessage;
import com.redhat.rhn.domain.rhnpackage.Package;
import com.redhat.rhn.domain.rhnpackage.PackageFactory;
import com.redhat.rhn.domain.server.Server;
import com.redhat.rhn.domain.server.ServerFactory;
import com.redhat.rhn.domain.state.StateFactory;
import com.redhat.rhn.domain.user.User;
import com.redhat.rhn.domain.user.UserFactory;
import com.redhat.rhn.frontend.events.AbstractDatabaseAction;
import com.redhat.rhn.manager.action.ActionManager;
import com.redhat.rhn.manager.errata.ErrataManager;

import com.suse.manager.webui.services.SaltStateGeneratorService;

import java.util.Date;
import java.util.List;
import java.util.Optional;

/**
 * Handle changes of channel assignments on minions: trigger a refresh of the errata cache,
 * regenerate pillar data and propagate the changes to the minion via state application.
 */
public class ChannelsChangedEventMessageAction extends AbstractDatabaseAction {

    @Override
    protected void doExecute(EventMessage event) {
        long serverId = ((ChannelsChangedEventMessage) event).getServerId();

        Server s = ServerFactory.lookupById(serverId);
        List<Package> prodPkgs =
                PackageFactory.findMissingProductPackagesOnServer(serverId);
        s.asMinionServer().ifPresent(minion -> {
            // This code acts only on salt minions

            // Trigger update of the errata cache
            ErrataManager.insertErrataCacheTask(minion);

            // Regenerate the pillar data
            SaltStateGeneratorService.INSTANCE.generatePillar(minion);

            // add product packages to package state
            StateFactory.addPackagesToNewStateRevision(minion,
                    Optional.ofNullable(event.getUserId()), prodPkgs);
        });
        if (!s.asMinionServer().isPresent()) {
            // This code acts only on traditional systems
            if (event.getUserId() != null) {
                User user = UserFactory.lookupById(event.getUserId());
                ActionManager.schedulePackageInstall(user, prodPkgs, s, new Date());
            }
            else if (s.getCreator() != null) {
                ActionManager.schedulePackageInstall(s.getCreator(), prodPkgs, s,
                        new Date());
            }
        }
    }
}
