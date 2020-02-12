package com.suse.manager.tasks;

public interface Actor {
    default int getMaxParallelWorkers() { return 1; }
}
