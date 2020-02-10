package com.suse.manager.tasks;

import com.suse.manager.tasks.actors.GreeterMain;

import akka.actor.typed.ActorSystem;
public class ActorManager {
  private static ActorSystem<GreeterMain.SayHello> greeterMain;

  public static void start(){
    if (greeterMain == null) {
      greeterMain = ActorSystem.create(GreeterMain.create(), "helloakka");
      greeterMain.tell(new GreeterMain.SayHello("Charles"));
    }
  }

  public static void stop(){
    greeterMain.terminate();
  }
}
