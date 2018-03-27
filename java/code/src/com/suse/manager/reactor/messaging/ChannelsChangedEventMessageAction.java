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
import com.redhat.rhn.domain.action.salt.ApplyStatesAction;
import com.redhat.rhn.domain.channel.AccessTokenFactory;
import com.redhat.rhn.domain.rhnpackage.Package;
import com.redhat.rhn.domain.rhnpackage.PackageFactory;
import com.redhat.rhn.domain.server.MinionServer;
import com.redhat.rhn.domain.server.Server;
import com.redhat.rhn.domain.server.ServerFactory;
import com.redhat.rhn.domain.state.StateFactory;
import com.redhat.rhn.domain.user.User;
import com.redhat.rhn.domain.user.UserFactory;
import com.redhat.rhn.frontend.events.AbstractDatabaseAction;
import com.redhat.rhn.manager.action.ActionManager;
import com.redhat.rhn.manager.errata.ErrataManager;
import com.redhat.rhn.taskomatic.TaskomaticApi;
import com.redhat.rhn.taskomatic.TaskomaticApiException;

import com.suse.manager.webui.services.SaltStateGeneratorService;

import org.apache.log4j.Logger;

import java.util.Collections;
import java.util.Date;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

/**
 * Handle changes of channel assignments on minions: trigger a refresh of the errata cache,
 * regenerate pillar data and propagate the changes to the minion via state application.
 */
public class ChannelsChangedEventMessageAction extends AbstractDatabaseAction {

    private static Logger log = Logger.getLogger(ChannelsChangedEventMessageAction.class);

    private static final TaskomaticApi TASKOMATIC_API = new TaskomaticApi();

    @Override
    protected void doExecute(EventMessage event) {
        ChannelsChangedEventMessage msg = (ChannelsChangedEventMessage) event;
        long serverId = msg.getServerId();

        Server s = ServerFactory.lookupById(serverId);
        if (s == null) {
            log.error("Server with id " + serverId + " not found.");
            return;
        }
        List<Package> prodPkgs =
                PackageFactory.findMissingProductPackagesOnServer(serverId);
        Optional<MinionServer> optMinion = s.asMinionServer();
        optMinion.ifPresent(minion -> {
            // This code acts only on salt minions

            // Trigger update of the errata cache
            ErrataManager.insertErrataCacheTask(minion);

            // Regenerate the pillar data
            SaltStateGeneratorService.INSTANCE.generatePillar(minion,
                    true,
                    msg.getAccessTokenIds() != null ?
                            msg.getAccessTokenIds().stream()
                                    .map(tokenId -> AccessTokenFactory.lookupById(tokenId)
                                            .orElseThrow(() ->
                                                    new RuntimeException(
                                                            "AccessToken not found id=" + msg.getServerId())))
                                    .collect(Collectors.toList()) :
                            Collections.emptyList()
                    );

            // add product packages to package state
            StateFactory.addPackagesToNewStateRevision(minion,
                    Optional.ofNullable(event.getUserId()), prodPkgs);

            if (msg.isScheduleApplyChannelsState()) {
                User user = UserFactory.lookupById(event.getUserId());
                ApplyStatesAction action = ActionManager.scheduleApplyStates(user,
                        Collections.singletonList(minion.getId()),
                        Collections.singletonList(ApplyStatesEventMessage.CHANNELS),
                        new Date());
                try {
                    TASKOMATIC_API.scheduleActionExecution(action, false);
                }
                catch (TaskomaticApiException e) {
                    log.error("Could not schedule channels state application for system: " +
                            s.getId());
                }
            }

        });
        if (!optMinion.isPresent()) {
            try {
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
            catch (TaskomaticApiException e) {
                log.error("Could not schedule state application for system: " + s.getId());
                throw new RuntimeException(e);
            }
        }
    }
}
