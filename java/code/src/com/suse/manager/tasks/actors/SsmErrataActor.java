package com.suse.manager.tasks.actors;

import static akka.actor.typed.javadsl.Behaviors.receive;
import static akka.actor.typed.javadsl.Behaviors.same;
import static akka.actor.typed.javadsl.Behaviors.setup;
import static com.redhat.rhn.frontend.events.TransactionHelper.handlingTransaction;

import com.redhat.rhn.domain.action.ActionChain;
import com.redhat.rhn.domain.action.ActionChainFactory;
import com.redhat.rhn.domain.user.User;
import com.redhat.rhn.domain.user.UserFactory;
import com.redhat.rhn.manager.errata.ErrataManager;
import com.redhat.rhn.manager.rhnset.RhnSetDecl;
import com.redhat.rhn.manager.ssm.SsmOperationManager;

import com.suse.manager.tasks.Actor;
import com.suse.manager.tasks.Command;
import org.apache.log4j.Logger;

import java.util.Date;
import java.util.List;
import java.util.Optional;

import akka.actor.typed.ActorRef;
import akka.actor.typed.Behavior;

public class SsmErrataActor implements Actor {

    private final static Logger LOG = Logger.getLogger(SsmErrataActor.class);

    public static class Message implements Command {
        private final Long userId;
        private final Date earliest;
        private final Optional<Long> actionChainId;
        private final List<Long> errataIds;
        private final List<Long> serverIds;

        public Message(Long userId, Date earliest, Optional<Long> actionChainId, List<Long> errataIds, List<Long> serverIds) {
            this.userId = userId;
            this.earliest = earliest;
            this.actionChainId = actionChainId;
            this.errataIds = errataIds;
            this.serverIds = serverIds;
        }
    }

    public Behavior<Command> create(ActorRef<Command> guardian) {
        return setup(context -> receive(Command.class)
                .onMessage(Message.class, message -> onMessage(message))
                .build());
    }

    private Behavior<Command> onMessage(Message message) {
        handlingTransaction(() -> execute(message));
        return same();
    }

    public void execute(Message event) {
        LOG.debug("Scheduling errata in SSM.");

        User user = UserFactory.lookupById(event.userId);
        ActionChain actionChain = ActionChainFactory.getActionChain(user, event.actionChainId.orElse(null));

        try {
            ErrataManager.applyErrata(user, event.errataIds, event.earliest,
                    actionChain, event.serverIds);
        }
        catch (Exception e) {
            LOG.error("Error scheduling SSM errata for event: " + event, e);
        }
        finally {
            SsmOperationManager.completeOperation(user, SsmOperationManager
                    .createOperation(user, "ssm.package.remove.operationname",
                            RhnSetDecl.SYSTEMS.getLabel()));
        }
    }
}
