package com.suse.manager.tasks.actors;

import static akka.actor.typed.javadsl.Behaviors.receive;
import static akka.actor.typed.javadsl.Behaviors.same;
import static akka.actor.typed.javadsl.Behaviors.setup;
import static com.redhat.rhn.frontend.events.TransactionHelper.handlingTransaction;
import static com.suse.manager.reactor.SaltReactor.THREAD_POOL_SIZE;

import com.redhat.rhn.domain.action.Action;
import com.redhat.rhn.domain.action.ActionFactory;
import com.redhat.rhn.domain.action.server.ServerAction;
import com.redhat.rhn.domain.server.ServerFactory;

import com.suse.manager.tasks.Actor;
import com.suse.manager.tasks.Command;
import com.suse.manager.webui.utils.salt.custom.ScheduleMetadata;
import com.suse.salt.netapi.event.BatchStartedEvent;
import org.apache.log4j.Logger;

import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;

import akka.actor.typed.Behavior;

public class BatchStartedActor implements Actor {

    private final static Logger LOG = Logger.getLogger(BatchStartedActor.class);

    @Override
    public int getMaxParallelWorkers() {
        return THREAD_POOL_SIZE;
    }

    public static class Message implements Command {
        private BatchStartedEvent batchStartedEvent;

        public Message(BatchStartedEvent batchStartedEvent) {
            this.batchStartedEvent = batchStartedEvent;
        }
    }

    public Behavior<Command> create() {
        return setup(context -> receive(Command.class)
                .onMessage(Message.class, message -> onMessage(message))
                .build());
    }

    private Behavior<Command> onMessage(Message message) {
        handlingTransaction(() -> execute(message));
        return same();
    }

    public void execute(Message msg) {
        BatchStartedEvent.Data eventData = msg.batchStartedEvent.getData();
        List<String> downMinions = eventData.getDownMinions();

        if (!downMinions.isEmpty()) {
            Optional<Long> actionId = eventData.getMetadata(ScheduleMetadata.class).map(
                    ScheduleMetadata::getSumaActionId);
            actionId.filter(id -> id > 0).ifPresent(id -> handleBatchStartedAction(id, downMinions));
        }
    }

    /**
     * Update the action properly based on the event results from Salt.
     *
     * @param actionId the ID of the Action to handle
     * @param minionId the ID of the Minion who performed the action
     */
    private static void handleBatchStartedAction(long actionId, List<String> minionIds) {
        Optional<Action> action = Optional.ofNullable(ActionFactory.lookupById(actionId));
        if (action.isPresent()) {
            if (LOG.isDebugEnabled()) {
                LOG.debug("Matched salt job with action (id=" + actionId + ")");
            }
            Map<String, Long> minionServerIds = ServerFactory.findServerIdsByMinionIds(minionIds);

            Set<ServerAction> serverActions = action.get().getServerActions();

            minionServerIds.entrySet().stream().forEach(entry -> {
                Optional<ServerAction> serverAction = serverActions.stream()
                        .filter(sa -> sa.getServerId().equals(entry.getValue()))
                        .findFirst();

                serverAction.ifPresent(sa -> handleServerAction(sa, entry.getKey()));
            });
        }
        else {
            LOG.warn("Action referenced from Salt job was not found: " + actionId);
        }
    }

    /**
     * Update a given server action properly for a given minion.
     *
     * @param serverAction the server action
     * @param minionServer the minion who performed the server action
     */
    private static void handleServerAction(ServerAction sa, String minionId) {
        if (LOG.isDebugEnabled()) {
            LOG.debug("Marking server action as failed for server: " + minionId);
        }
        sa.fail("Minion is down");
        ActionFactory.save(sa);
    }
}
