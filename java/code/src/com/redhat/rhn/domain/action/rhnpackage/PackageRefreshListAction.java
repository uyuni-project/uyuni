/*
 * Copyright (c) 2009--2010 Red Hat, Inc.
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
package com.redhat.rhn.domain.action.rhnpackage;


import com.redhat.rhn.domain.server.MinionSummary;

import com.suse.manager.reactor.messaging.ApplyStatesEventMessage;
import com.suse.salt.netapi.calls.LocalCall;
import com.suse.salt.netapi.calls.modules.State;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

/**
 * PackageRefreshListAction
 */
public class PackageRefreshListAction extends PackageAction {

    /**
     * @param minionSummaries a list of minion summaries of the minions involved in the given Action
     * @return minion summaries grouped by local call
     */
    public static Map<LocalCall<?>, List<MinionSummary>> packagesRefreshListAction(
            List<MinionSummary> minionSummaries) {
        Map<LocalCall<?>, List<MinionSummary>> ret = new HashMap<>();
        ret.put(State.apply(List.of(ApplyStatesEventMessage.PACKAGES_PROFILE_UPDATE),
                Optional.empty()), minionSummaries);
        return ret;
    }
}
