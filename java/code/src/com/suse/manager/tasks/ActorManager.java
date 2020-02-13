package com.suse.manager.tasks;

import com.redhat.rhn.common.hibernate.HibernateFactory;

import com.suse.manager.tasks.actors.DeferredManagerActor;
import com.suse.manager.tasks.actors.GuardianActor;
import org.hibernate.Transaction;

import java.util.*;

import akka.actor.typed.ActorSystem;

public class ActorManager {
  private static ActorSystem<Command> actorSystem = ActorSystem.create(new GuardianActor().create(), "guardian");

  public static void stop(){
    actorSystem.terminate();
  }


  public static void tell(Command c) {
    actorSystem.tell(c);
  }

  public static void tellDeferred(Transaction transaction) {
    actorSystem.tell(new DeferredManagerActor.TellMessages(transaction.hashCode(), actorSystem));

  }

  public static void defer(Command c) {
    var transaction = HibernateFactory.getSession().getTransaction();
    actorSystem.tell(new DeferredManagerActor.RegisterMessage(transaction.hashCode(),c));
  }

  public static void clearDeferred(Transaction transaction) {
    actorSystem.tell(new DeferredManagerActor.ClearMessages(transaction.hashCode()));
  }
}
