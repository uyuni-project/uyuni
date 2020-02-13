package com.suse.manager.tasks.actors;

import static akka.actor.typed.javadsl.Behaviors.receive;
import static akka.actor.typed.javadsl.Behaviors.same;
import static akka.actor.typed.javadsl.Behaviors.setup;
import static com.redhat.rhn.frontend.events.TransactionHelper.handlingTransaction;

import com.redhat.rhn.domain.server.MinionServerFactory;
import com.redhat.rhn.domain.server.VirtualInstanceFactory;
import com.redhat.rhn.manager.system.VirtualInstanceManager;

import com.suse.manager.tasks.Actor;
import com.suse.manager.tasks.Command;
import com.suse.manager.webui.utils.salt.custom.VirtpollerData;
import org.apache.log4j.Logger;

import akka.actor.typed.Behavior;

public class VirtpollerBeaconActor implements Actor {

    private final static Logger LOG = Logger.getLogger(VirtpollerBeaconActor.class);

    public static class Message implements Command {
        private final String minionId;
        private final VirtpollerData data;

        public Message(String minionId, VirtpollerData data) {
            this.minionId = minionId;
            this.data = data;
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

    public void execute(Message message) {
        MinionServerFactory.findByMinionId(message.minionId).ifPresent(minion -> {
            VirtualInstanceManager.updateHostVirtualInstance(minion,
                    VirtualInstanceFactory.getInstance().getFullyVirtType());
            VirtualInstanceManager.updateGuestsVirtualInstances(minion,
                    message.data.getPlan());
        });
    }
}
