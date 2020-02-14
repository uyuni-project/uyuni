package com.suse.manager.tasks.actors;

import static akka.actor.typed.javadsl.Behaviors.receive;
import static akka.actor.typed.javadsl.Behaviors.same;
import static akka.actor.typed.javadsl.Behaviors.setup;

import com.suse.manager.tasks.Actor;
import com.suse.manager.tasks.Command;
import org.apache.log4j.Logger;

import java.io.Serializable;

import akka.actor.typed.Behavior;

public class HelloWorldActor implements Actor {

    private final static Logger LOG = Logger.getLogger(HelloWorldActor.class);

    public static class Message implements Command, Serializable {
        private final String message;

        public Message(String message) {
            this.message = message;
        }
    }

    @Override
    public boolean remote() {
        return true;
    }

    public Behavior<Command> create() {
        return setup(context -> {
            System.err.println(context.getSelf());
            LOG.error(context.getSelf());
            return receive(Command.class)
                .onMessage(Message.class, message -> onMessage(message))
                .build(); });
    }

    public Behavior<Command> onMessage(Message message) {
        System.err.println(message.message);
        LOG.error(message.message);
        return same();
    }
}
