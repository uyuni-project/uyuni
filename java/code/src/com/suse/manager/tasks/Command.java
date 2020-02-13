package com.suse.manager.tasks;

public interface Command {
    default String routingHashString() { return this.hashCode() + ""; }
}
