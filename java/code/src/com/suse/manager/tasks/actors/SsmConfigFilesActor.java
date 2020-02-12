package com.suse.manager.tasks.actors;

import static akka.actor.typed.javadsl.Behaviors.receive;
import static akka.actor.typed.javadsl.Behaviors.same;
import static akka.actor.typed.javadsl.Behaviors.setup;
import static com.redhat.rhn.frontend.events.TransactionHelper.handlingTransaction;

import com.redhat.rhn.domain.action.ActionChainFactory;
import com.redhat.rhn.domain.action.ActionType;
import com.redhat.rhn.domain.user.User;
import com.redhat.rhn.domain.user.UserFactory;
import com.redhat.rhn.manager.action.ActionChainManager;

import com.suse.manager.tasks.Actor;
import com.suse.manager.tasks.Command;
import org.apache.commons.lang3.builder.ToStringBuilder;
import org.apache.log4j.Logger;

import java.util.Collection;
import java.util.Date;
import java.util.Map;
import java.util.Optional;

import akka.actor.typed.Behavior;

public class SsmConfigFilesActor implements Actor {

    private final static Logger LOG = Logger.getLogger(SsmConfigFilesActor.class);

    public static class Message implements Command {
        private final Collection<Long> systemIds;
        private final Map<Long, Collection<Long>> revisionMappings;
        private final Long userId;
        private final ActionType type;
        private final Date earliest;
        private final Optional<Long> actionChainId;

        public Message(Collection<Long> systemIds, Map<Long, Collection<Long>> revisionMappings, Long userId, ActionType type, Date earliest, Optional<Long> actionChainId) {
            this.systemIds = systemIds;
            this.revisionMappings = revisionMappings;
            this.userId = userId;
            this.type = type;
            this.earliest = earliest;
            this.actionChainId = actionChainId;
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

    private void execute(Message message) {
        User user = UserFactory.lookupById(message.userId);
        var actionChain = message.actionChainId.map(id -> ActionChainFactory.getActionChain(user, id)).orElse(null);

        try {
            ActionChainManager.createConfigActions(
                    user,
                    message.revisionMappings,
                    message.systemIds,
                    message.type,
                    message.earliest,
                    actionChain);
        }
        catch (Exception e) {
            LOG.error("Error scheduling configuration files deployment for event " +
                    ToStringBuilder.reflectionToString(message), e);
        }
    }
}
