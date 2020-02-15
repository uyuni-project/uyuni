package com.suse.manager.tasks.actors;

import static akka.actor.typed.javadsl.Behaviors.receive;
import static akka.actor.typed.javadsl.Behaviors.same;
import static akka.actor.typed.javadsl.Behaviors.setup;
import static com.redhat.rhn.frontend.events.TransactionHelper.handlingTransaction;

import com.redhat.rhn.common.validator.ValidatorError;
import com.redhat.rhn.domain.server.Server;
import com.redhat.rhn.domain.user.User;
import com.redhat.rhn.domain.user.UserFactory;
import com.redhat.rhn.manager.kickstart.cobbler.CobblerPowerCommand;
import com.redhat.rhn.manager.ssm.SsmOperationManager;
import com.redhat.rhn.manager.system.SystemManager;

import com.suse.manager.tasks.Actor;
import com.suse.manager.tasks.Command;
import org.apache.log4j.Logger;
import org.cobbler.XmlRpcException;

import java.util.List;

import akka.actor.typed.ActorRef;
import akka.actor.typed.Behavior;

public class SsmPowerManagementActor implements Actor {

    private final static Logger LOG = Logger.getLogger(SsmPowerManagementActor.class);

    public static class Message implements Command {
        /** The user id. */
        private final Long userId;

        /** Systems to apply the action to. */
        private final List<Long> sids;

        /** Power management operation kind. */
        private final CobblerPowerCommand.Operation operation;

        public Message(Long userId, List<Long> sids, CobblerPowerCommand.Operation operation) {
            this.userId = userId;
            this.sids = sids;
            this.operation = operation;
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
        Long userId = event.userId;
        User user = UserFactory.lookupById(userId);

        CobblerPowerCommand.Operation operation = event.operation;
        long operationId = SsmOperationManager.createOperation(user,
                "cobbler.powermanagement." + operation.toString().toLowerCase(), null);
        SsmOperationManager.associateServersWithOperation(operationId, userId, event.sids);

        try {
            for (Long sid : event.sids) {
                LOG.debug("Running operation " + operation.toString() + " on server " +
                        sid);
                Server server = SystemManager.lookupByIdAndUser(sid, user);

                ValidatorError error = null;
                try {
                    error = new CobblerPowerCommand(user, server, operation).store();
                }
                catch (XmlRpcException e) {
                    LOG.error(e);
                    error = new ValidatorError(
                            "ssm.provisioning.powermanagement.cobbler_error");
                }
                if (error != null) {
                    SsmOperationManager.addNoteToOperationOnServer(operationId,
                            server.getId(), error.getKey());
                }
            }
        }
        catch (Exception e) {
            LOG.error("Error in power management operations " + event, e);
        }
        finally {
            SsmOperationManager.completeOperation(user, operationId);
        }
    }
}
