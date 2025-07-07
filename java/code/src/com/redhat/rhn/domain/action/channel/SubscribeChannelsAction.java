/*
 * Copyright (c) 2018 SUSE LLC
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

package com.redhat.rhn.domain.action.channel;

import com.redhat.rhn.domain.action.Action;
import com.redhat.rhn.domain.server.MinionServer;
import com.redhat.rhn.domain.server.MinionServerFactory;
import com.redhat.rhn.domain.server.MinionSummary;
import com.redhat.rhn.domain.server.ServerFactory;
import com.redhat.rhn.domain.server.ServerGroupFactory;
import com.redhat.rhn.manager.system.SystemManager;

import com.suse.manager.reactor.messaging.ApplyStatesEventMessage;
import com.suse.manager.webui.services.iface.SaltApi;
import com.suse.salt.netapi.calls.LocalCall;
import com.suse.salt.netapi.calls.modules.State;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

/**
 * SubscribeChannelsAction - Class representing a channel(s) subscription action
 */
public class SubscribeChannelsAction extends Action {

    private SubscribeChannelsActionDetails details;

    /**
     * @return the action details
     */
    public SubscribeChannelsActionDetails getDetails() {
        return details;
    }

    /**
     * @param actionDetails to set
     */
    public void setDetails(SubscribeChannelsActionDetails actionDetails) {
        this.details = actionDetails;
    }


    /**
     * @param minionSummaries a list of minion summaries of the minions involved in the given Action
     * @param saltApi
     * @param action action which has all the revisions
     * @return minion summaries grouped by local call
     */
    public static Map<LocalCall<?>, List<MinionSummary>> subscribeChannelsAction(
            List<MinionSummary> minionSummaries, SaltApi saltApi, SubscribeChannelsAction action) {
        SubscribeChannelsActionDetails actionDetails = action.getDetails();

        Map<LocalCall<?>, List<MinionSummary>> ret = new HashMap<>();
        SystemManager sysMgr = new SystemManager(ServerFactory.SINGLETON, ServerGroupFactory.SINGLETON, saltApi);

        List<MinionServer> minions = MinionServerFactory.lookupByMinionIds(
                minionSummaries.stream().map(MinionSummary::getMinionId).collect(Collectors.toSet()));

        minions.forEach(minion ->
                // change channels in DB and execult the ChannelsChangedEventMessageAction
                // which regenerate pillar and refresh Tokens but does not execute a "state.apply channels"
                sysMgr.updateServerChannels(
                        actionDetails.getParentAction().getSchedulerUser(),
                        minion,
                        Optional.ofNullable(actionDetails.getBaseChannel()),
                        actionDetails.getChannels())
        );
        ret.put(State.apply(List.of(ApplyStatesEventMessage.CHANNELS), Optional.empty()),
                minionSummaries);

        return ret;
    }
}
