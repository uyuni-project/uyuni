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
package com.suse.manager.webui.services;

import com.redhat.rhn.domain.action.Action;
import com.redhat.rhn.domain.action.ActionFactory;
import com.redhat.rhn.domain.action.errata.ErrataAction;
import com.redhat.rhn.domain.action.server.ServerAction;
import com.redhat.rhn.manager.entitlement.EntitlementManager;

import com.suse.manager.webui.services.impl.SaltAPIService;
import com.suse.saltstack.netapi.datatypes.target.MinionList;

import org.apache.log4j.Logger;

import java.util.List;
import java.util.stream.Collectors;

/**
 * Takes {@link ServerAction} objects to be executed via salt.
 */
public enum SaltServerActionService {

    /* Singleton instance of this class */
    INSTANCE;

    /* Logger for this class */
    private static final Logger LOG = Logger.getLogger(SaltServerActionService.class);

    /* Prevent instantiation */
    SaltServerActionService() {
    }

    /**
     * Execute a given {@link Action} via salt.
     *
     * @param actionIn the action to execute
     */
    public void execute(Action actionIn) {
        if (actionIn.getActionType().equals(ActionFactory.TYPE_ERRATA)) {
            ErrataAction errataAction = (ErrataAction) actionIn;

            // FIXME: Store the minion ID in the database instead of using server name
            List<String> minionIds = actionIn.getServerActions().stream()
                    .filter(serverAction -> serverAction.getServer()
                            .hasEntitlement(EntitlementManager.SALTSTACK))
                    .map(serverAction -> serverAction.getServer().getName())
                    .collect(Collectors.toList());
            MinionList target = new MinionList(minionIds);

            LOG.debug("Scheduling errata action for: " + target.getTarget());
            SaltAPIService.INSTANCE.schedulePatchInstallation(target,
                    errataAction.getErrata(), null);
        }
    }
}
