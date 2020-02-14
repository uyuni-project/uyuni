package com.suse.manager.tasks;

import com.suse.manager.tasks.actors.HelloWorldActor;

public class RemoteActorManager {
    public static void main(String[] argv) throws InterruptedException {
        ActorManager.start(25520, "only_remote_actors");
        ActorManager.tell(new HelloWorldActor.Message("Hello Local World"));
        Thread.sleep(1000000);
    }
}