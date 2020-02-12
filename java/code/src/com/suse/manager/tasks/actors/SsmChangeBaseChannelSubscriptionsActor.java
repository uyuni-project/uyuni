package com.suse.manager.tasks.actors;

import static akka.actor.typed.javadsl.Behaviors.receive;
import static akka.actor.typed.javadsl.Behaviors.same;
import static akka.actor.typed.javadsl.Behaviors.setup;
import static com.redhat.rhn.frontend.events.TransactionHelper.handlingTransaction;

import com.redhat.rhn.common.hibernate.HibernateFactory;
import com.redhat.rhn.domain.server.Server;
import com.redhat.rhn.domain.server.ServerFactory;
import com.redhat.rhn.domain.user.User;
import com.redhat.rhn.domain.user.UserFactory;
import com.redhat.rhn.frontend.action.channel.ssm.ChannelActionDAO;
import com.redhat.rhn.manager.ssm.SsmOperationManager;
import com.redhat.rhn.manager.system.UpdateBaseChannelCommand;

import com.suse.manager.tasks.Actor;
import com.suse.manager.tasks.Command;
import org.apache.log4j.Logger;

import java.util.Collection;

import akka.actor.typed.Behavior;

public class SsmChangeBaseChannelSubscriptionsActor implements Actor {

    private final static Logger LOG = Logger.getLogger(SsmChangeBaseChannelSubscriptionsActor.class);

    public static class Message implements Command {
        private final Long userId;
        private final Collection<ChannelActionDAO> changes;
        private final Long opId;

        public Message(Long userId, Collection<ChannelActionDAO> changes, Long opId) {
            this.userId = userId;
            this.changes = changes;
            this.opId = opId;
        }
    }

    public Behavior<Command> create() {
        return setup(context -> receive(Command.class)
                .onMessage(Message.class, message -> onMessage(message))
                .build());
    }

    private Behavior<Command> onMessage(Message message) {
        handlingTransaction(() -> execute(message),
                e -> { SsmOperationManager.completeOperation(UserFactory.lookupById(message.userId), message.opId); }
        );
        return same();
    }

    private void execute(Message message) {
        User user = UserFactory.lookupById(message.userId);

        for (ChannelActionDAO server : message.changes) {
            Server s = ServerFactory.lookupById(server.getId());
            for (Long cid : server.getSubscribeChannelIds()) {
                UpdateBaseChannelCommand ubcc = new UpdateBaseChannelCommand(user,
                        s, cid);
                // don't care about the return value
                ubcc.store();
            }
            // commit after each server to keep this from blocking other operations
            HibernateFactory.commitTransaction();
        }
        SsmOperationManager.completeOperation(UserFactory.lookupById(message.userId), message.opId);
    }
}
