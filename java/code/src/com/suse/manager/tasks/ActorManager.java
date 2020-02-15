package com.suse.manager.tasks;

import com.redhat.rhn.common.hibernate.HibernateFactory;

import com.suse.manager.tasks.actors.DeferringActor;
import com.suse.manager.tasks.actors.GuardianActor;
import com.typesafe.config.Config;
import com.typesafe.config.ConfigFactory;
import org.hibernate.Transaction;

import java.net.InetAddress;
import java.net.UnknownHostException;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

import akka.actor.typed.ActorSystem;

public class ActorManager {
  private static ActorSystem<Command> actorSystem;

  public static void start(Integer port, String role){
    if(actorSystem == null){
      synchronized (ActorManager.class){
        if(actorSystem == null){
          Map<String, Object> overrides = new HashMap<>();
          overrides.put("akka.remote.artery.canonical.port", port);
          try {
            overrides.put("akka.remote.artery.canonical.hostname", InetAddress.getLocalHost().getCanonicalHostName() );
          }
          catch (UnknownHostException e) {
            e.printStackTrace();
          }
          overrides.put("akka.cluster.roles", Collections.singletonList(role));
          Config config = ConfigFactory.parseMap(overrides).withFallback(ConfigFactory.load());
          actorSystem = ActorSystem.create(new GuardianActor().create(), "guardian", config);
        }
      }
    }
  }

  public static void stop(){
    actorSystem.terminate();
  }


  public static void tell(Command c) {
    if(actorSystem == null){
      throw new RuntimeException("Actor Manager not initialized. Call ActorManager.start() first.");
    }
    actorSystem.tell(c);
  }

  public static void tellDeferred(Transaction transaction) {
    actorSystem.tell(new DeferringActor.TellDeferredMessage(transaction.hashCode(), actorSystem));
  }

  public static void defer(Command c) {
    var transaction = HibernateFactory.getSession().getTransaction();
    actorSystem.tell(new DeferringActor.DeferCommandMessage(transaction.hashCode(),c));
  }

  public static void clearDeferred(Transaction transaction) {
    actorSystem.tell(new DeferringActor.ClearDeferredMessage(transaction.hashCode()));
  }
}
