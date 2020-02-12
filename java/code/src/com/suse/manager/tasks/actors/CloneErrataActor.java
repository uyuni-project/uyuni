package com.suse.manager.tasks.actors;

import static akka.actor.typed.javadsl.Behaviors.receive;
import static akka.actor.typed.javadsl.Behaviors.same;
import static akka.actor.typed.javadsl.Behaviors.setup;
import static com.redhat.rhn.frontend.events.TransactionHelper.handlingTransaction;

import com.redhat.rhn.domain.user.UserFactory;
import com.redhat.rhn.manager.errata.ErrataManager;

import com.suse.manager.tasks.Actor;
import com.suse.manager.tasks.Command;
import org.apache.log4j.Logger;

import java.util.Collection;

import akka.actor.typed.Behavior;

public class CloneErrataActor implements Actor {

    private final static Logger LOG = Logger.getLogger(CloneErrataActor.class);

    public static class Message implements Command {
        private final Long chanId;
        private final Collection<Long> errata;
        private final Long userId;
        private final boolean requestRepodataRegen;

        public Message(Long chanId, Collection<Long> errata, Long userId) {
            this(chanId, errata, userId, true);
        }
        public Message(Long chanId, Collection<Long> errata, Long userId, boolean requestRepodataRegen) {
            this.chanId = chanId;
            this.errata = errata;
            this.userId = userId;
            this.requestRepodataRegen = requestRepodataRegen;
        }
    }

    public Behavior<Command> create() {
        return setup(context -> receive(Command.class)
                .onMessage(Message.class, message -> onMessage(message))
                .build());
    }

    public Behavior<Command> onMessage(Message message) {
        handlingTransaction(() -> cloneErrata(message));
        return same();
    }

    public void cloneErrata(Message msg) {
        LOG.debug("Clone Errata " + msg.errata);
        ErrataManager.cloneErrata(msg.chanId, msg.errata, msg.requestRepodataRegen, UserFactory.lookupById(msg.userId));
    }
}
