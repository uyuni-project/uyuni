package com.suse.manager.tasks.actors;

import static akka.actor.typed.javadsl.Behaviors.receive;
import static akka.actor.typed.javadsl.Behaviors.same;
import static akka.actor.typed.javadsl.Behaviors.setup;
import static com.redhat.rhn.frontend.events.TransactionHelper.handlingTransaction;

import com.redhat.rhn.domain.action.ActionChain;
import com.redhat.rhn.domain.action.ActionChainFactory;
import com.redhat.rhn.domain.user.User;
import com.redhat.rhn.domain.user.UserFactory;
import com.redhat.rhn.manager.action.ActionChainManager;
import com.redhat.rhn.manager.rhnset.RhnSetDecl;
import com.redhat.rhn.manager.ssm.SsmOperationManager;
import com.redhat.rhn.taskomatic.TaskomaticApiException;

import com.suse.manager.tasks.Actor;
import com.suse.manager.tasks.Command;
import org.apache.log4j.Logger;

import java.util.Date;
import java.util.Optional;
import java.util.Set;

import akka.actor.typed.Behavior;

public class SsmSystemRebootActor implements Actor {

    private final static Logger LOG = Logger.getLogger(SsmSystemRebootActor.class);

    public static class Message implements Command {
        private final Long userId;
        private final Date earliest;
        private final Optional<Long> actionChainId;
        private final Set<Long> serverIds;

        public Message(Long userId, Date earliest, Optional<Long> actionChainId, Set<Long> serverIds) {
            this.userId = userId;
            this.earliest = earliest;
            this.actionChainId = actionChainId;
            this.serverIds = serverIds;
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

    public void execute(Message event) {
        LOG.debug("Scheduling systems reboot in SSM.");
        User user = UserFactory.lookupById(event.userId);
        ActionChain actionChain = ActionChainFactory.getActionChain(user, event.actionChainId.orElse(null));

        try {
            ActionChainManager.scheduleRebootActions(user, event.serverIds, event.earliest, actionChain);
        }
        catch (TaskomaticApiException e) {
            LOG.error("Could not schedule reboot:");
            LOG.error(e);
            throw new RuntimeException(e);
        }
        finally {
            SsmOperationManager.completeOperation(user,
                    SsmOperationManager.createOperation(user, "ssm.misc.reboot.operationname",
                            RhnSetDecl.SSM_SYSTEMS_REBOOT.getLabel()));
        }
    }
}
