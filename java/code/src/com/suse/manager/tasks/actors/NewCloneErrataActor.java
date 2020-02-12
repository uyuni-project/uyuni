package com.suse.manager.tasks.actors;

import static akka.actor.typed.javadsl.Behaviors.receive;
import static akka.actor.typed.javadsl.Behaviors.same;
import static akka.actor.typed.javadsl.Behaviors.setup;
import static com.redhat.rhn.frontend.events.TransactionHelper.handlingTransaction;

import com.redhat.rhn.domain.channel.Channel;
import com.redhat.rhn.domain.channel.ChannelFactory;
import com.redhat.rhn.domain.errata.Errata;
import com.redhat.rhn.domain.errata.ErrataFactory;
import com.redhat.rhn.domain.user.UserFactory;
import com.redhat.rhn.manager.errata.AsyncErrataCloneCounter;
import com.redhat.rhn.manager.errata.ErrataManager;

import com.suse.manager.tasks.Actor;
import com.suse.manager.tasks.Command;
import org.apache.log4j.Logger;

import java.util.ArrayList;
import java.util.List;

import akka.actor.typed.Behavior;

public class NewCloneErrataActor implements Actor {

    private final static Logger LOG = Logger.getLogger(NewCloneErrataActor.class);

    public static class Message implements Command {
        private final Long channelId;
        private final Long errataId;
        private final Long userId;
        private final boolean inheritPackages;

        public Message(Long channelId, Long errataId, Long userId, boolean inheritPackages) {
            this.channelId = channelId;
            this.errataId = errataId;
            this.userId = userId;
            this.inheritPackages = inheritPackages;
        }
    }

    public Behavior<Command> create() {
        return setup(context -> receive(Command.class)
                .onMessage(Message.class, message -> onMessage(message))
                .build());
    }

    private Behavior<Command> onMessage(Message message) {
        handlingTransaction(() -> execute(message));
        return same();
    }

    public void execute(Message msg) {
        List<Errata> errata = new ArrayList<Errata>();
        Errata erratum = ErrataFactory.lookupById(msg.errataId);
        Channel channel = ChannelFactory.lookupById(msg.channelId);
        if (channel != null && erratum != null) {
            errata.add(erratum);
            ErrataManager.cloneErrataApi(channel, errata, UserFactory.lookupById(msg.userId), msg.inheritPackages);
        }
        AsyncErrataCloneCounter.getInstance().removeAsyncErrataCloneJob(msg.channelId);
    }
}
