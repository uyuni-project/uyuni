package com.suse.manager.tasks.actors;

import static akka.actor.typed.javadsl.Behaviors.receive;
import static akka.actor.typed.javadsl.Behaviors.same;
import static akka.actor.typed.javadsl.Behaviors.setup;
import static com.redhat.rhn.frontend.events.TransactionHelper.handlingTransaction;

import com.redhat.rhn.domain.user.User;
import com.redhat.rhn.domain.user.UserFactory;
import com.redhat.rhn.frontend.action.channel.ssm.ChannelActionDAO;
import com.redhat.rhn.manager.ssm.SsmManager;
import com.redhat.rhn.manager.ssm.SsmOperationManager;

import com.suse.manager.tasks.Actor;
import com.suse.manager.tasks.Command;
import org.apache.log4j.Logger;

import java.util.Collection;

import akka.actor.typed.Behavior;

public class SsmChangeChannelSubscriptionsActor implements Actor {

    private final static Logger LOG = Logger.getLogger(SsmChangeChannelSubscriptionsActor.class);

    public static class Message implements Command {
        private final Long userId;
        private final Collection<ChannelActionDAO> changes;
        private final Long operationId;

        public Message(Long userId, Collection<ChannelActionDAO> changes, Long operationId) {
            this.userId = userId;
            this.changes = changes;
            this.operationId = operationId;
        }
    }

    public Behavior<Command> create() {
        return setup(context -> receive(Command.class)
                .onMessage(Message.class, message -> onMessage(message))
                .build());
    }

    private Behavior<Command> onMessage(Message message) {
        handlingTransaction(() -> execute(message),
                e -> { SsmOperationManager.completeOperation(UserFactory.lookupById(message.userId), message.operationId); }
        );
        return same();
    }

    private void execute(Message message) {
        User user = UserFactory.lookupById(message.userId);
        SsmManager.performChannelActions(user, message.changes);
        SsmOperationManager.completeOperation(user, message.operationId);
    }
}
