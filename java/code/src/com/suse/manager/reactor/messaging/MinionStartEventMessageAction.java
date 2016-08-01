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
package com.suse.manager.reactor.messaging;

import com.redhat.rhn.common.messaging.EventMessage;
import com.redhat.rhn.domain.action.ActionFactory;
import com.redhat.rhn.domain.action.ActionType;
import com.redhat.rhn.domain.action.server.ServerAction;
import com.redhat.rhn.domain.server.MinionServer;
import com.redhat.rhn.domain.server.MinionServerFactory;
import com.redhat.rhn.frontend.events.AbstractDatabaseAction;
import com.suse.manager.webui.services.SaltService;
import com.suse.manager.webui.services.impl.SaltAPIService;
import com.suse.salt.netapi.calls.LocalCall;
import com.suse.salt.netapi.calls.modules.Status;
import com.suse.salt.netapi.datatypes.target.MinionList;
import com.suse.salt.netapi.datatypes.target.Target;
import com.suse.salt.netapi.exception.SaltException;
import com.suse.salt.netapi.results.Result;

import org.apache.log4j.Logger;

import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.Optional;

/**
 * Event message handler for {@link MinionStartEventMessage}.
 */
public class MinionStartEventMessageAction extends AbstractDatabaseAction {

    /* Logger for this class */
    private static final Logger LOG = Logger.getLogger(MinionStartEventMessageAction.class);

    // Reference to the SaltService instance
    private final SaltService SALT_SERVICE;

    /**
     * Default constructor.
     */
    public MinionStartEventMessageAction() {
        this(SaltAPIService.INSTANCE);
    }

    /**
     * Constructor taking a {@link SaltService} instance.
     *
     * @param saltService the salt service to use
     */
    public MinionStartEventMessageAction(SaltService saltService) {
        SALT_SERVICE = saltService;
    }

    @Override
    protected void doExecute(EventMessage msg) {
        MinionStartEventMessage startEvent = (MinionStartEventMessage) msg;

        // Update custom grains, modules and beacons on every minion restart
        SALT_SERVICE.syncGrains(startEvent.getMinionId());
        SALT_SERVICE.syncModules(startEvent.getMinionId());
        SALT_SERVICE.syncBeacons(startEvent.getMinionId());

        Optional<MinionServer> minionOpt =
                MinionServerFactory.findByMinionId(startEvent.getMinionId());
        minionOpt.ifPresent(minion -> {

            Target<?> target = new MinionList(minion.getMinionId());
            // get uptime
            LocalCall<Float> uptimeCall = Status.uptime();
            try {
                Map<String, Result<Float>> uptimes = SaltAPIService.INSTANCE
                        .callSync(uptimeCall, target);
                if (uptimes.containsKey(minion.getMinionId())) {
                    Long uptime = uptimes.get(minion.getMinionId())
                            .result().get().longValue();

                    Date bootTime = new Date(System.currentTimeMillis() - (uptime * 1000));
                    LOG.debug("set last boot for " +
                                    minion.getMinionId() + " to " + bootTime);
                    minion.setLastBoot(bootTime.getTime() / 1000);

                    // cleanup old reboot actions
                    @SuppressWarnings("unchecked")
                    List<ServerAction> serverActions = ActionFactory
                            .listServerActionsForServer(minion);
                    int actionsChanged = 0;
                    for (ServerAction sa : serverActions) {
                        ActionType actionType = sa.getParentAction().getActionType();
                        if (actionType.equals(ActionFactory.TYPE_REBOOT) &&
                                sa.getStatus().equals(ActionFactory.STATUS_QUEUED) &&
                                bootTime.after(sa.getParentAction().getEarliestAction())) {
                            sa.setStatus(ActionFactory.STATUS_COMPLETED);
                            sa.setResultMsg("Reboot completed.");
                            sa.setResultCode(0L);
                            ActionFactory.save(sa);
                            actionsChanged += 1;
                        }
                    }
                    if (actionsChanged > 0) {
                        LOG.debug(actionsChanged + " reboot actions set to completed");
                    }
                }
                else {
                    LOG.error("Can't get uptime for " + minion.getMinionId());
                }
            }
            catch (SaltException e) {
                LOG.error(e);
            }
        });

    }
}
