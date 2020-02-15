package com.suse.manager.tasks.actors;

import static akka.actor.typed.javadsl.Behaviors.receive;
import static akka.actor.typed.javadsl.Behaviors.same;
import static akka.actor.typed.javadsl.Behaviors.setup;
import static com.redhat.rhn.frontend.events.TransactionHelper.handlingTransaction;

import com.redhat.rhn.common.client.ClientCertificate;
import com.redhat.rhn.domain.server.MinionServerFactory;
import com.redhat.rhn.manager.system.SystemManager;

import com.suse.manager.tasks.Actor;
import com.suse.manager.tasks.Command;
import com.suse.manager.webui.services.impl.SaltService;
import com.suse.salt.netapi.calls.modules.Event;
import com.suse.salt.netapi.datatypes.target.MinionList;
import com.suse.salt.netapi.exception.SaltException;
import org.apache.log4j.Logger;

import java.util.HashMap;
import java.util.Map;

import akka.actor.typed.ActorRef;
import akka.actor.typed.Behavior;

public class SystemIdGenerateActor implements Actor {

    private final static Logger LOG = Logger.getLogger(SystemIdGenerateActor.class);

    private static final String EVENT_TAG = "suse/systemid/generated";

    public static class Message implements Command {
        private String minionId;

        public Message(String minionId) {
            this.minionId = minionId;
        }
    }

    public Behavior<Command> create(ActorRef<Command> guardian) {
        return setup(context -> receive(Command.class)
                .onMessage(Message.class, message -> onMessage(message))
                .build());
    }

    private Behavior<Command> onMessage(Message message) {
        handlingTransaction(() -> execute(message));
        return same();
    }

    public void execute(Message message) {
        var minionId = message.minionId;
        MinionServerFactory.findByMinionId(minionId).ifPresent(minion -> {
            try {
                ClientCertificate cert = SystemManager.createClientCertificate(minion);
                Map<String, Object> data = new HashMap<>();
                data.put("data", cert.toString());
                SaltService.INSTANCE.callAsync(Event.fire(data, EVENT_TAG), new MinionList(minionId));
            }
            catch (InstantiationException e) {
                LOG.warn(String.format("Unable to generate certificate: : %s", minionId));
            }
            catch (SaltException e) {
                LOG.warn(String.format("Unable to call event.fire for %s: %s", minionId, e.getMessage()));
            }
        });
    }
}
