package com.suse.manager.tasks.actors;

import static akka.actor.typed.javadsl.Behaviors.receive;
import static akka.actor.typed.javadsl.Behaviors.same;
import static akka.actor.typed.javadsl.Behaviors.setup;

import com.suse.manager.tasks.Command;
import org.apache.commons.lang3.builder.ReflectionToStringBuilder;
import org.apache.log4j.Logger;

import akka.actor.typed.ActorRef;
import akka.actor.typed.Behavior;
import akka.actor.typed.javadsl.ActorContext;

public class GuardianActor {

    private final static Logger LOG = Logger.getLogger(GuardianActor.class);

    public Behavior<Command> create() {
        return setup(context -> buildBehavior(context));
    }

    private Behavior<Command> buildBehavior(ActorContext<Command> context) {
        var restartSatellite = context.spawn(new RestartSatelliteActor().create(), "restartSatellite");
        var traceBack = context.spawn(new TraceBackActor().create(), "traceBack");
        return receive(Command.class)
                .onMessage(RestartSatelliteActor.Message.class, message -> tellChild(restartSatellite, message))
                .onMessage(TraceBackActor.Message.class, message -> tellChild(traceBack, message))
                .build();
    }

    public Behavior<Command> tellChild(ActorRef<Command> a, Command c) {
        LOG.debug("telling " + a + " to do " + ReflectionToStringBuilder.toString(c));
        a.tell(c);
        return same();
    }
}
