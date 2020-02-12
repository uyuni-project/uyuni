package com.suse.manager.tasks.actors;

import static akka.actor.typed.javadsl.Behaviors.receive;
import static akka.actor.typed.javadsl.Behaviors.same;
import static akka.actor.typed.javadsl.Behaviors.setup;
import static com.redhat.rhn.frontend.events.TransactionHelper.handlingTransaction;

import com.redhat.rhn.domain.action.Action;
import com.redhat.rhn.domain.action.ActionChainFactory;
import com.redhat.rhn.domain.user.User;
import com.redhat.rhn.domain.user.UserFactory;
import com.redhat.rhn.manager.action.ActionChainManager;
import com.redhat.rhn.taskomatic.TaskomaticApiException;

import com.suse.manager.tasks.Actor;
import com.suse.manager.tasks.Command;
import org.apache.log4j.Logger;

import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import akka.actor.typed.Behavior;

public class SsmUpgradePackagesActor implements Actor {

    private final static Logger LOG = Logger.getLogger(SsmUpgradePackagesActor.class);

    public static class Message implements Command {
        private final long userId;
        private final Date earliest;
        private final Optional<Long> actionChainId;
        public final Map<Long, List<Map<String, Long>>> sysPackageSet;

        public Message(Long userId, Date earliest, Optional<Long> actionChainId, Map<Long, List<Map<String, Long>>> sysPackageSet) {
            this.userId = userId;
            this.earliest = earliest;
            this.actionChainId = actionChainId;
            this.sysPackageSet = sysPackageSet;
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

    private void execute(Message message) {
        var user = UserFactory.lookupById(message.userId);
        SsmPackagesActor.execute(user, "ssm.package.upgrade.operationname", () -> doSchedule(message, user));
    }

    public List<Action> doSchedule(Message msg, User user) {

        Map<Long, List<Map<String, Long>>> packageListItems = msg.sysPackageSet;

        var actionChain = msg.actionChainId.map(id -> ActionChainFactory.getActionChain(user, id)).orElse(null);
        try {
            return ActionChainManager.schedulePackageUpgrades(user, packageListItems, msg.earliest, actionChain);
        }
        catch (TaskomaticApiException e) {
            throw new RuntimeException(e);
        }
    }
}
