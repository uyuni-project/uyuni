package com.suse.manager.tasks.actors;

import akka.actor.typed.ActorSystem;
import akka.actor.typed.Behavior;
import akka.actor.typed.javadsl.ActorContext;
import com.suse.manager.tasks.Actor;
import com.suse.manager.tasks.Command;
import org.apache.log4j.Logger;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Optional;

import static akka.actor.typed.javadsl.Behaviors.receive;
import static akka.actor.typed.javadsl.Behaviors.setup;

public class DeferredManagerActor implements Actor {

    private final static Logger LOG = Logger.getLogger(DeferredManagerActor.class);


    public static class RegisterMessage implements Command {
        private final Integer accumulatorKey;
        private final Command command;

        public RegisterMessage(Integer accumulatorKey, Command command) {
            this.accumulatorKey = accumulatorKey;
            this.command = command;
        }
    }

    public static class ClearMessages implements Command {
        private final Integer accumulatorKey;

        public ClearMessages(Integer accumulatorKey) {
            this.accumulatorKey = accumulatorKey;
        }
    }

    public static class TellMessages implements Command {
        private final Integer accumulatorKey;
        private final ActorSystem<Command> actorSystem;

        public TellMessages(Integer accumulatorKey, ActorSystem<Command> actorSystem) {
            this.accumulatorKey = accumulatorKey;
            this.actorSystem = actorSystem;
        }
    }

    public Behavior<Command> create() {
        return setup(context -> build(context, new HashMap<Integer, HashSet<Command>>()));
    }

    private Behavior<Command> build(ActorContext<Command> context, Map<Integer, HashSet<Command>> accumulator) {
        LOG.debug("Accumulator size: " + accumulator.size());
        return receive(Command.class)
                .onMessage(RegisterMessage.class, message -> onRegisterMessage(context, message, accumulator))
                .onMessage(ClearMessages.class, message -> onClearMessage(context, message, accumulator))
                .onMessage(TellMessages.class, message -> onTellMessage(context, message, accumulator))
                .build();
    }

    private Behavior<Command> onRegisterMessage(ActorContext<Command> context, RegisterMessage message, Map<Integer, HashSet<Command>> accumulator) {
        var set = accumulator.getOrDefault(message.accumulatorKey, new HashSet<Command>());
        set.add(message.command);
        accumulator.put(message.accumulatorKey, set);

        return build(context, accumulator);
    }

    private Behavior<Command> onClearMessage(ActorContext<Command> context, ClearMessages message, Map<Integer, HashSet<Command>> accumulator) {
        accumulator.remove(message.accumulatorKey);
     return build(context, accumulator);
    }

    private Behavior<Command> onTellMessage(ActorContext<Command> context, TellMessages message, Map<Integer, HashSet<Command>> accumulator) {
        Optional.ofNullable(accumulator.remove(message.accumulatorKey))
                .ifPresent(commands -> commands.forEach(c -> message.actorSystem.tell(c)));
        return build(context, accumulator);
    }
}
