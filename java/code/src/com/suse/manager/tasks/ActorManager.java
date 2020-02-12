package com.suse.manager.tasks;

import com.redhat.rhn.common.hibernate.HibernateFactory;

import com.suse.manager.tasks.actors.GuardianActor;
import org.hibernate.Transaction;

import java.util.*;

import akka.actor.typed.ActorSystem;

public class ActorManager {
  private static ActorSystem<Command> actorSystem = ActorSystem.create(new GuardianActor().create(), "guardian");
  private static Map<Transaction, Set<Command>> deferred = new HashMap<>();

  public static void stop(){
    actorSystem.terminate();
  }


  public static void tell(Command c) {
    actorSystem.tell(c);
  }

  public static void tellDeferred(Transaction transaction) {
    Optional.ofNullable(deferred.remove(transaction))
            .ifPresent(commands -> commands.forEach(c -> tell(c)));
  }

  public static void defer(Command c) {
    var transaction = HibernateFactory.getSession().getTransaction();
    var set = deferred.getOrDefault(transaction, new HashSet<Command>());
    set.add(c);
    deferred.put(transaction, set);
  }

  public static void clearDeferred(Transaction transaction) {
    deferred.remove(transaction);
  }
}
