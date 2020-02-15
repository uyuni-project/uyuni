package com.suse.manager.tasks;

import akka.actor.typed.ActorRef;
import akka.actor.typed.Behavior;

public interface Actor {
    default int getMaxParallelWorkers() { return 1; }

    /**
     * If @{getMaxParallelWorkers} > 1, routing happens in round-robin way by default.
     *
     * Return true and implement {@link Command#routingHashString()} to use hashing routing instead:
     * {@link Command}s returning the same String will almost always go to the same Actor.
     */
    default boolean useHashRouting() { return false; }

    default boolean remote() { return false; }

    Behavior<Command> create(ActorRef<Command> guardian);
}
