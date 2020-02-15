package com.suse.manager.tasks.actors;

import static akka.actor.typed.javadsl.Behaviors.receive;
import static akka.actor.typed.javadsl.Behaviors.same;
import static akka.actor.typed.javadsl.Behaviors.setup;
import static com.redhat.rhn.frontend.events.TransactionHelper.handlingTransaction;

import com.redhat.rhn.common.hibernate.HibernateFactory;
import com.redhat.rhn.domain.user.User;
import com.redhat.rhn.domain.user.UserFactory;
import com.redhat.rhn.manager.ssm.SsmOperationManager;
import com.redhat.rhn.manager.system.SystemManager;

import com.suse.manager.tasks.Actor;
import com.suse.manager.tasks.Command;
import org.apache.log4j.Logger;

import java.util.List;

import akka.actor.typed.ActorRef;
import akka.actor.typed.Behavior;

public class SsmDeleteServersActor implements Actor {

    private final static Logger LOG = Logger.getLogger(SsmDeleteServersActor.class);
    public static final String OPERATION_NAME = "ssm.server.delete.operationname";

    public static class Message implements Command {
        private final Long userId;
        private final List<Long> sids;
        private final SystemManager.ServerCleanupType serverCleanupType;

        public Message(Long userId, List<Long> sids, SystemManager.ServerCleanupType serverCleanupType) {
            this.userId = userId;
            this.sids = sids;
            this.serverCleanupType = serverCleanupType;
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

    private void execute(Message event) {
        User user = UserFactory.lookupById(event.userId);

        long operationId = SsmOperationManager.createOperation(user,
                OPERATION_NAME, null);

        SsmOperationManager.associateServersWithOperation(operationId,
                user.getId(), event.sids);
        HibernateFactory.commitTransaction();
        try {
            for (Long sid : event.sids) {
                SystemManager.deleteServerAndCleanup(user,
                        sid,
                        event.serverCleanupType
                );
                // commit after each deletion to prevent deadlocks with
                // system registration
                HibernateFactory.commitTransaction();
            }
        }
        finally {
            // Complete the action
            SsmOperationManager.completeOperation(user, operationId);
        }
    }
}
