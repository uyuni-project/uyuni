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

import com.redhat.rhn.GlobalInstanceHolder;
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

    private SaltApi saltApi = null;
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
     * @param saltApiIn SaltApi to set
     */
    public void setSaltApi(SaltApi saltApiIn) {
        this.saltApi = saltApiIn;
    }

    /**
     * @param minionSummaries a list of minion summaries of the minions involved in the given Action
     * @return minion summaries grouped by local call
     */
    @Override
    public Map<LocalCall<?>, List<MinionSummary>> getSaltCalls(List<MinionSummary> minionSummaries) {

        Map<LocalCall<?>, List<MinionSummary>> ret = new HashMap<>();
        SystemManager sysMgr = new SystemManager(ServerFactory.SINGLETON, ServerGroupFactory.SINGLETON,
                (null == saltApi) ? GlobalInstanceHolder.SALT_API : saltApi);

        List<MinionServer> minions = MinionServerFactory.lookupByMinionIds(
                minionSummaries.stream().map(MinionSummary::getMinionId).collect(Collectors.toSet()));

        minions.forEach(minion ->
                // change channels in DB and execult the ChannelsChangedEventMessageAction
                // which regenerate pillar and refresh Tokens but does not execute a "state.apply channels"
                sysMgr.updateServerChannels(
                        details.getParentAction().getSchedulerUser(),
                        minion,
                        Optional.ofNullable(details.getBaseChannel()),
                        details.getChannels())
        );
        ret.put(State.apply(List.of(ApplyStatesEventMessage.CHANNELS), Optional.empty()),
                minionSummaries);

        return ret;
    }
}
