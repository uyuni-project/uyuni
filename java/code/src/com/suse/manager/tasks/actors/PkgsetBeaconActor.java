package com.suse.manager.tasks.actors;

import static akka.actor.typed.javadsl.Behaviors.receive;
import static akka.actor.typed.javadsl.Behaviors.same;
import static akka.actor.typed.javadsl.Behaviors.setup;
import static com.redhat.rhn.frontend.events.TransactionHelper.handlingTransaction;
import static com.suse.manager.reactor.SaltReactor.THREAD_POOL_SIZE;

import com.redhat.rhn.domain.server.MinionServerFactory;
import com.redhat.rhn.manager.action.ActionManager;
import com.redhat.rhn.taskomatic.TaskomaticApiException;

import com.suse.manager.tasks.Actor;
import com.suse.manager.tasks.Command;
import com.suse.salt.netapi.event.BeaconEvent;
import org.apache.log4j.Logger;

import akka.actor.typed.Behavior;

public class PkgsetBeaconActor implements Actor {

    private final static Logger LOG = Logger.getLogger(PkgsetBeaconActor.class);

    @Override
    public int getMaxParallelWorkers() {
        return THREAD_POOL_SIZE;
    }

    public static class Message implements Command {
        private final BeaconEvent beaconEvent;

        public Message(BeaconEvent beaconEvent) {
            this.beaconEvent = beaconEvent;
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
        MinionServerFactory.findByMinionId(msg.beaconEvent.getMinionId()).ifPresent(minionServer -> {
            try {
                ActionManager.schedulePackageRefresh(minionServer.getOrg(), minionServer);
            }
            catch (TaskomaticApiException e) {
                LOG.error("Could not schedule package refresh for minion: " +
                        minionServer.getMinionId());
                LOG.error(e);
            }
        });
    }
}
