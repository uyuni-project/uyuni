package com.suse.manager.tasks;

import com.suse.manager.tasks.actors.RestartSatelliteActor;

import akka.actor.typed.ActorSystem;
public class ActorManager {
  private static ActorSystem<Command> actorSystem;

  public static void start() {
    if (actorSystem == null) {
      actorSystem = ActorSystem.create(RestartSatelliteActor.create(), "helloakka");
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
