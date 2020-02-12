package com.suse.manager.tasks.actors;

import static akka.actor.typed.javadsl.Behaviors.receive;
import static akka.actor.typed.javadsl.Behaviors.same;
import static akka.actor.typed.javadsl.Behaviors.setup;
import static com.redhat.rhn.frontend.events.TransactionHelper.handlingTransaction;

import com.redhat.rhn.manager.errata.cache.UpdateErrataCacheCommand;

import com.suse.manager.tasks.Actor;
import com.suse.manager.tasks.Command;
import org.apache.log4j.Logger;

import java.util.List;

import akka.actor.typed.Behavior;

public class UpdateErrataCacheActor implements Actor {

    private final static Logger LOG = Logger.getLogger(UpdateErrataCacheActor.class);
    public static final int TYPE_ORG = 1;
    public static final int TYPE_CHANNEL = 2;
    public static final int TYPE_CHANNEL_ERRATA = 3;

    public static class Message implements Command {
        private final int type;
        private final Long orgId;
        private final List<Long> channelIds;
        private final List<Long> packageIds;
        private final Long errataId;

        public Message(int type, Long orgId, List<Long> channelIds, List<Long> packageIds, Long errataId) {
            this.type = type;
            this.orgId = orgId;
            this.channelIds = channelIds;
            this.packageIds = packageIds;
            this.errataId = errataId;
        }
    }

    public Behavior<Command> create() {
        return setup(context -> receive(Command.class)
                .onMessage(Message.class, message -> onMessage(message))
                .build());
    }

    private Behavior<Command> onMessage(Message message) {
        handlingTransaction(() -> updateErrata(message));
        return same();
    }

    private void updateErrata(Message msg) {
        if (LOG.isDebugEnabled()) {
            LOG.debug("Updating errata cache, with type: " + msg.type);
        }

        UpdateErrataCacheCommand uecc = new UpdateErrataCacheCommand();

        if (msg.type == TYPE_ORG) {
            Long orgId = msg.orgId;
            if (orgId == null) {
                LOG.error("UpdateErrataCacheEvent was sent with a null org");
                return;
            }
            if (LOG.isDebugEnabled()) {
                LOG.debug("Updating errata cache for org [" + orgId + "]");
            }
            uecc.updateErrataCache(orgId);
            if (LOG.isDebugEnabled()) {
                LOG.debug("Finished updating errata cache for org [" +
                        orgId + "]");
            }
        }
        else if (msg.type == TYPE_CHANNEL) {
            for (Long cid : msg.channelIds) {
                if (LOG.isDebugEnabled()) {
                    LOG.debug("Updating errata cache for channel: " + cid);
                }
                uecc.updateErrataCacheForChannel(cid);
            }
        }
        else if (msg.type == TYPE_CHANNEL_ERRATA) {
            for (Long cid : msg.channelIds) {
                if (LOG.isDebugEnabled()) {
                    LOG.debug("Updating errata cache for channel: " + cid +
                            " and errata:" + msg.errataId);
                }
                if (msg.packageIds == null || msg.packageIds.size() == 0) {
                    uecc.updateErrataCacheForErrata(cid, msg.errataId);
                }
                else {
                    uecc.updateErrataCacheForErrata(cid, msg.errataId,
                            msg.packageIds);
                }
            }
        }
        else {
            throw new IllegalArgumentException("Unknown update type: " +
                    msg.type);
        }
    }
}
