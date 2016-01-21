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

import com.redhat.rhn.domain.action.Action;
import com.redhat.rhn.domain.action.ActionFactory;
import com.redhat.rhn.domain.action.errata.ErrataAction;
import com.redhat.rhn.domain.errata.Errata;
import com.redhat.rhn.domain.server.MinionServerFactory;
import com.redhat.rhn.domain.server.MinionServer;
import com.redhat.rhn.domain.server.ServerFactory;
import com.redhat.rhn.manager.entitlement.EntitlementManager;

import com.suse.manager.webui.services.impl.SaltAPIService;
import com.suse.saltstack.netapi.datatypes.target.MinionList;

import com.suse.saltstack.netapi.datatypes.target.Target;
import org.apache.log4j.Logger;

import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.Stream;

/**
 * Takes {@link Action} objects to be executed via salt.
 */
public enum SaltServerActionService {

    /* Singleton instance of this class */
    INSTANCE;

    /* Logger for this class */
    private static final Logger LOG = Logger.getLogger(SaltServerActionService.class);

    /**
     * Execute a given {@link Action} via salt.
     *
     * @param actionIn the action to execute
     */
    public void execute(Action actionIn) {
        if (actionIn.getActionType().equals(ActionFactory.TYPE_ERRATA)) {
            ErrataAction errataAction = (ErrataAction) actionIn;
            List<MinionServer> minions = actionIn.getServerActions().stream()
                    .flatMap(action ->
                            action.getServer().asMinionServer()
                                    .map(Stream::of)
                                    .orElse(Stream.empty()))
                    .filter(minion -> minion.hasEntitlement(EntitlementManager.SALTSTACK))
                    .collect(Collectors.toList());

            Set<Long> serverIds = minions.stream()
                    .map(MinionServer::getId)
                    .collect(Collectors.toSet());
            Set<Long> errataIds = errataAction.getErrata().stream()
                    .map(Errata::getId)
                    .collect(Collectors.toSet());
            Map<Long, Map<Long, Set<String>>> errataNames = ServerFactory
                    .listErrataNamesForServers(serverIds, errataIds);

            minions.forEach(minion -> {
                Target<?> target = new MinionList(minion.getMinionId());
                LOG.debug("Scheduling errata action for: ");
                Set<String> patches = errataNames.get(minion.getId()).entrySet().stream()
                        .map(Map.Entry::getValue)
                        .flatMap(Set::stream)
                        .collect(Collectors.toSet());
                LocalDateTime earliestAction = actionIn.getEarliestAction().toInstant()
                        .atZone(ZoneId.of("UTC")).toLocalDateTime();
                Map<String, Long> metadata = new HashMap<>();
                metadata.put("suma-action-id", actionIn.getId());
                SaltAPIService.INSTANCE.schedulePatchInstallation(
                        "scheduled-action-" + actionIn.getId(), target, patches,
                        earliestAction, metadata);
            });
        }
    }
}
