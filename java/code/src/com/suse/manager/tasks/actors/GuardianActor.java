package com.suse.manager.tasks.actors;

import static akka.actor.typed.SupervisorStrategy.restart;
import static akka.actor.typed.javadsl.Behaviors.receive;
import static akka.actor.typed.javadsl.Behaviors.same;
import static akka.actor.typed.javadsl.Behaviors.setup;
import static java.util.function.Function.identity;
import static java.util.stream.Collectors.toMap;
import static java.util.stream.Collectors.toSet;

import com.suse.manager.tasks.Command;
import org.apache.commons.lang3.builder.ReflectionToStringBuilder;
import org.apache.log4j.Logger;
import org.jose4j.jwk.Use;
import org.reflections.Reflections;
import org.reflections.scanners.SubTypesScanner;

import java.lang.reflect.InvocationTargetException;
import java.util.Arrays;
import java.util.Map;
import java.util.function.Function;

import akka.actor.typed.ActorRef;
import akka.actor.typed.Behavior;
import akka.actor.typed.javadsl.ActorContext;
import akka.actor.typed.javadsl.BehaviorBuilder;
import akka.actor.typed.javadsl.Behaviors;
import akka.actor.typed.javadsl.PoolRouter;
import akka.actor.typed.javadsl.Routers;

public class GuardianActor {

    private final static Logger LOG = Logger.getLogger(GuardianActor.class);

    public Behavior<Command> create() {
        return setup(context -> buildBehavior(context));
    }

    private Behavior<Command> buildBehavior(ActorContext<Command> context) {
        var reflections = new Reflections(GuardianActor.class.getPackageName(), new SubTypesScanner(false));
        var allClasses = reflections.getSubTypesOf(Object.class);

        var actorClasses = allClasses.stream()
                .filter(c -> !c.equals(GuardianActor.class))
                .filter(c -> !c.isMemberClass())
                .collect(toSet());

        var actorMap = actorClasses.stream().collect(toMap(
                identity(),
                classToActor(context))
        );

        var basicReceiveBuilder = receive(Command.class);
        var receiveBuilder = actorMap.entrySet().stream().reduce(
                basicReceiveBuilder,
                this::addHandlers,
                (memo1, memo2) -> memo2
        );

        return receiveBuilder.build();
    }

    private BehaviorBuilder<Command> addHandlers(BehaviorBuilder<Command> behaviorBuilder, Map.Entry<Class<?>, ActorRef<Command>> entry) {
        var actorClass = entry.getKey();
        var messageClasses = Arrays.stream(actorClass.getNestMembers())
                .filter(nm -> Arrays.asList(nm.getInterfaces()).contains(Command.class))
                .map(c -> (Class<Command>) c)
                .collect(toSet());

        var actor = entry.getValue();

        var augmentedBehaviorBuilder = messageClasses.stream().reduce(
                behaviorBuilder,
                (memo, messageClass) -> memo.onMessage(messageClass, message -> tellChild(actor, message)),
                (memo1, memo2) -> memo2
        );
        return augmentedBehaviorBuilder;
    }

    private Function<Class<?>, ActorRef<Command>> classToActor(ActorContext<Command> context) {
        return clazz -> {
            try {
                var createMethod = clazz.getMethod("create");
                var instance = clazz.getConstructor().newInstance();
                var behavior = (Behavior<Command>) createMethod.invoke(instance);

                // create a PoolRouter, which is an actor that will spawn many concurrent actors
                var getMaxParallelWorkersMethod = clazz.getMethod("getMaxParallelWorkers");
                var maxParallelWorkers = (int) getMaxParallelWorkersMethod.invoke(instance);
                PoolRouter<Command> pool =
                        Routers.pool(
                                maxParallelWorkers,
                                // make sure the workers are restarted if they fail
                                Behaviors.supervise(behavior).onFailure(restart()));

                var actor = context.spawn(pool, clazz.getSimpleName() + "_pool");
                return actor;
            }
            catch (NoSuchMethodException | IllegalAccessException | InvocationTargetException | InstantiationException e) {
                LOG.error(e);
                throw new RuntimeException(e);
            }
        };
    }

    public Behavior<Command> tellChild(ActorRef<Command> a, Command c) {
        LOG.debug("telling " + a + " to do " + ReflectionToStringBuilder.toString(c));
        a.tell(c);
        return same();
    }
}
