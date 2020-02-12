package com.suse.manager.tasks;

import akka.actor.typed.Behavior;

public interface Actor {
    default int getMaxParallelWorkers() { return 1; }

    Behavior<Command> create();
}
