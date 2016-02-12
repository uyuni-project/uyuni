package com.suse.manager.reactor.messaging;

import com.redhat.rhn.common.messaging.EventMessage;
import com.redhat.rhn.domain.action.ActionFactory;
import com.redhat.rhn.domain.action.server.ServerAction;
import com.redhat.rhn.domain.server.MinionServer;
import com.redhat.rhn.domain.server.MinionServerFactory;
import com.redhat.rhn.frontend.events.AbstractDatabaseAction;
import com.suse.manager.webui.services.impl.SaltAPIService;
import com.suse.salt.netapi.calls.LocalCall;
import com.suse.salt.netapi.calls.modules.Status;
import com.suse.salt.netapi.datatypes.target.MinionList;
import com.suse.salt.netapi.datatypes.target.Target;
import com.suse.salt.netapi.exception.SaltException;
import org.apache.log4j.Logger;

import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

public class MinionStartEventMessageAction extends AbstractDatabaseAction {

    /* Logger for this class */
    private static final Logger LOG = Logger.getLogger(MinionStartEventMessageAction.class);

    @Override
    protected void doExecute(EventMessage msg) {
        MinionStartEventMessage startEvent = (MinionStartEventMessage) msg;

        Optional<MinionServer> minionOpt =
                MinionServerFactory.findByMinionId(startEvent.getMinionId());
        minionOpt.ifPresent(minion -> {

            Target<?> target = new MinionList(minion.getMinionId());
            // get uptime
            LocalCall<Float> uptimeCall = com.suse.manager.webui.utils.salt.Status.uptime();
            try {
                Map<String, Object> metadata = new HashMap<String, Object>();
                Map<String, Float> uptimes = SaltAPIService.INSTANCE.callSync(uptimeCall, target, metadata);
                if (uptimes.containsKey(minion.getMinionId())) {
                    Long uptime = uptimes.get(minion.getMinionId()).longValue();

                    Date bootTime = new Date(System.currentTimeMillis() - (uptime * 1000));
                    LOG.debug("set last boot for " +
                                    minion.getMinionId() + " to " + bootTime);
                    minion.setLastBoot(bootTime.getTime() / 1000);

                    // cleanup old reboot actions
                    List<ServerAction> serverActions = ActionFactory.listServerActionsForServer(minion);
                    int actionsChanged = 0;
                    for (ServerAction serverAction : serverActions) {
                        if (serverAction.getParentAction().getActionType().equals(ActionFactory.TYPE_REBOOT) &&
                                serverAction.getPickupTime() != null &&
                                serverAction.getStatus().equals(ActionFactory.STATUS_QUEUED) &&
                                bootTime.after(serverAction.getPickupTime())) {
                            serverAction.setStatus(ActionFactory.STATUS_COMPLETED);
                            ActionFactory.save(serverAction);
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
