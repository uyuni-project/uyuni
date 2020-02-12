package com.suse.manager.tasks.actors;

import static akka.actor.typed.javadsl.Behaviors.receive;
import static akka.actor.typed.javadsl.Behaviors.same;
import static akka.actor.typed.javadsl.Behaviors.setup;
import static com.redhat.rhn.frontend.events.TransactionHelper.handlingTransaction;

import com.redhat.rhn.domain.action.Action;
import com.redhat.rhn.domain.action.ActionChainFactory;
import com.redhat.rhn.domain.user.User;
import com.redhat.rhn.domain.user.UserFactory;
import com.redhat.rhn.frontend.action.SetLabels;
import com.redhat.rhn.frontend.dto.EssentialServerDto;
import com.redhat.rhn.frontend.dto.PackageListItem;
import com.redhat.rhn.manager.action.ActionChainManager;
import com.redhat.rhn.manager.system.SystemManager;
import com.redhat.rhn.taskomatic.TaskomaticApiException;

import com.suse.manager.tasks.Actor;
import com.suse.manager.tasks.Command;
import org.apache.log4j.Logger;

import java.util.ArrayList;
import java.util.Date;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;

import akka.actor.typed.Behavior;

public class SsmInstallPackagesActor implements Actor {

    private final static Logger LOG = Logger.getLogger(SsmInstallPackagesActor.class);

    public static class Message implements Command {
        private final long userId;
        private final Date earliest;
        private final Optional<Long> actionChainId;
        private final Set<String> packages;
        private final Long channelId;

        public Message(Long userId, Date earliest, Optional<Long> actionChainId, Set<String> packages, Long channelId) {
            this.userId = userId;
            this.earliest = earliest;
            this.actionChainId = actionChainId;
            this.packages = packages;
            this.channelId = channelId;
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
        SsmPackagesActor.execute(user, "ssm.package.install.operationname", () -> doSchedule(message, user));
    }


    protected List<Long> getAffectedServers(Message msg, User u) {
        Long channelId = msg.channelId;

        List<EssentialServerDto> servers = SystemManager.systemsSubscribedToChannelInSet(
                channelId, u, SetLabels.SYSTEM_LIST);

        // Create one action for all servers to which the packages are installed
        List<Long> serverIds = new LinkedList<Long>();
        for (EssentialServerDto dto : servers) {
            serverIds.add(dto.getId());
        }
        return serverIds;
    }

    protected List<Action> doSchedule(Message msg, User user) {

        Set<String> data = msg.packages;
        // Convert the package list to domain objects
        List<PackageListItem> pkgListItems = new ArrayList<PackageListItem>(data.size());
        for (String key : data) {
            pkgListItems.add(PackageListItem.parse(key));
        }

        // Convert to list of maps
        List<Map<String, Long>> packageListData = PackageListItem
                .toKeyMaps(pkgListItems);

        var actionChain = msg.actionChainId.map(id -> ActionChainFactory.getActionChain(user, id)).orElse(null);
        try {
            return ActionChainManager.schedulePackageInstalls(user, getAffectedServers(msg, user), packageListData, msg.earliest, actionChain);
        }
        catch (TaskomaticApiException e) {
            throw new RuntimeException(e);
        }
    }
}
