package com.suse.manager.tasks;

import java.io.Serializable;

public interface Command extends Serializable {
    default String routingHashString() { return this.hashCode() + ""; }
}
