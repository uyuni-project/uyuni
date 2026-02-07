/*
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

import com.redhat.rhn.common.hibernate.HibernateFactory;
import com.redhat.rhn.common.messaging.EventMessage;
import com.redhat.rhn.common.messaging.MessageAction;
import com.redhat.rhn.domain.action.salt.ApplyStatesAction;
import com.redhat.rhn.domain.server.Server;
import com.redhat.rhn.domain.server.ServerFactory;
import com.redhat.rhn.domain.user.User;
import com.redhat.rhn.domain.user.UserFactory;
import com.redhat.rhn.manager.action.ActionManager;
import com.redhat.rhn.manager.errata.ErrataManager;
import com.redhat.rhn.taskomatic.TaskomaticApi;
import com.redhat.rhn.taskomatic.TaskomaticApiException;

import com.suse.manager.webui.services.iface.SaltApi;
import com.suse.manager.webui.services.iface.SystemQuery;
import com.suse.manager.webui.services.pillar.MinionPillarManager;
import com.suse.salt.netapi.datatypes.target.MinionList;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.util.Collections;
import java.util.Date;

/**
 * Handle changes of channel assignments on minions: trigger a refresh of the errata cache,
 * regenerate pillar data and propagate the changes to the minion via state application.
 */
public class ChannelsChangedEventMessageAction implements MessageAction {

    private static Logger log = LogManager.getLogger(ChannelsChangedEventMessageAction.class);

    private final SaltApi saltApi;

    private static final TaskomaticApi TASKOMATIC_API = new TaskomaticApi();

    /**
     * Constructor taking a {@link SystemQuery} instance.
     *
     * @param saltApiIn Salt API instance to use
     */
    public ChannelsChangedEventMessageAction(SaltApi saltApiIn) {
        saltApi = saltApiIn;
    }

    @Override
    public void execute(EventMessage event) {
        ChannelsChangedEventMessage msg = (ChannelsChangedEventMessage) event;
        long serverId = msg.getServerId();

        Server s = ServerFactory.lookupById(serverId);
        if (s == null) {
            log.error("Server with id {} not found.", serverId);
            return;
        }
        s.asMinionServer().ifPresentOrElse(
                minion -> {
                    // This code acts only on salt minions

                    // Trigger update of the errata cache
                    ErrataManager.insertErrataCacheTask(minion);

                    // Regenerate the pillar data
                    MinionPillarManager.INSTANCE.generatePillar(minion);

                    // Commit the current transaction
                    HibernateFactory.commitTransaction();

                    // push the changed pillar data to the minion
                    saltApi.refreshPillar(new MinionList(minion.getMinionId()));

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
                            log.error("Could not schedule channels state application for system: {}", s.getId());
                        }
                    }
                },
                () -> log.error("Traditional Clients are not supported"));
    }
}
