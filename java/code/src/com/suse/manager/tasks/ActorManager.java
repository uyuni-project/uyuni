package com.suse.manager.tasks;

import com.suse.manager.tasks.actors.GuardianActor;

import akka.actor.typed.ActorSystem;
public class ActorManager {
  private static ActorSystem<Command> actorSystem;

  public static void start() {
    if (actorSystem == null) {
      actorSystem = ActorSystem.create(new GuardianActor().create(), "guardian");
    }
  }

  public static void stop(){
    actorSystem.terminate();
  }


  public static void tell(Command c) {
    start();
    actorSystem.tell(c);
  }
}
