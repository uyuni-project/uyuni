package com.suse.manager.tasks.actors;

import static akka.actor.typed.javadsl.Behaviors.receive;
import static akka.actor.typed.javadsl.Behaviors.same;
import static akka.actor.typed.javadsl.Behaviors.setup;
import static com.redhat.rhn.frontend.events.TransactionHelper.handlingTransaction;

import com.redhat.rhn.domain.action.salt.ApplyStatesAction;
import com.redhat.rhn.domain.channel.AccessTokenFactory;
import com.redhat.rhn.domain.rhnpackage.Package;
import com.redhat.rhn.domain.rhnpackage.PackageFactory;
import com.redhat.rhn.domain.server.MinionServer;
import com.redhat.rhn.domain.server.Server;
import com.redhat.rhn.domain.server.ServerFactory;
import com.redhat.rhn.domain.state.StateFactory;
import com.redhat.rhn.domain.user.User;
import com.redhat.rhn.domain.user.UserFactory;
import com.redhat.rhn.manager.action.ActionManager;
import com.redhat.rhn.manager.errata.ErrataManager;
import com.redhat.rhn.taskomatic.TaskomaticApi;
import com.redhat.rhn.taskomatic.TaskomaticApiException;

import com.suse.manager.reactor.messaging.ApplyStatesEventMessage;
import com.suse.manager.tasks.Actor;
import com.suse.manager.tasks.Command;
import com.suse.manager.webui.services.SaltStateGeneratorService;
import org.apache.log4j.Logger;

import java.util.Collections;
import java.util.Date;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

import akka.actor.typed.Behavior;

public class ChannelsChangedActor implements Actor {

    private final static Logger LOG = Logger.getLogger(ChannelsChangedActor.class);

    private static final TaskomaticApi TASKOMATIC_API = new TaskomaticApi();

    public static class Message implements Command {
        private final long serverId;
        private final Long userId;
        private final List<Long> accessTokenIds;
        private final boolean scheduleApplyChannelsState;

        public Message(long serverId, Long userId, List<Long> accessTokenIds, boolean scheduleApplyChannelsState) {
            this.serverId = serverId;
            this.userId = userId;
            this.accessTokenIds = accessTokenIds;
            this.scheduleApplyChannelsState = scheduleApplyChannelsState;
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
        long serverId = msg.serverId;

        Server s = ServerFactory.lookupById(serverId);
        if (s == null) {
            LOG.error("Server with id " + serverId + " not found.");
            return;
        }
        List<Package> prodPkgs =
                PackageFactory.findMissingProductPackagesOnServer(serverId);
        Optional<MinionServer> optMinion = s.asMinionServer();
        optMinion.ifPresent(minion -> {
            // This code acts only on salt minions

            // Trigger update of the errata cache
            ErrataManager.insertErrataCacheTask(minion);

            // Regenerate the pillar data
            SaltStateGeneratorService.INSTANCE.generatePillar(minion,
                    true,
                    msg.accessTokenIds.stream()
                        .map(tokenId -> AccessTokenFactory.lookupById(tokenId)
                                .orElseThrow(() ->
                                        new RuntimeException(
                                                "AccessToken not found id=" + msg.serverId)))
                        .collect(Collectors.toList())
            );

            // add product packages to package state
            StateFactory.addPackagesToNewStateRevision(minion,
                    Optional.ofNullable(msg.userId), prodPkgs);

            if (msg.scheduleApplyChannelsState) {
                User user = UserFactory.lookupById(msg.userId);
                ApplyStatesAction action = ActionManager.scheduleApplyStates(user,
                        Collections.singletonList(minion.getId()),
                        Collections.singletonList(ApplyStatesEventMessage.CHANNELS),
                        new Date());
                try {
                    TASKOMATIC_API.scheduleActionExecution(action, false);
                }
                catch (TaskomaticApiException e) {
                    LOG.error("Could not schedule channels state application for system: " +
                            s.getId());
                }
            }

        });
        if (!optMinion.isPresent()) {
            try {
                // This code acts only on traditional systems
                if (msg.userId != null) {
                    User user = UserFactory.lookupById(msg.userId);
                    ActionManager.schedulePackageInstall(user, prodPkgs, s, new Date());
                }
                else if (s.getCreator() != null) {
                    ActionManager.schedulePackageInstall(s.getCreator(), prodPkgs, s,
                            new Date());
                }
            }
            catch (TaskomaticApiException e) {
                LOG.error("Could not schedule state application for system: " + s.getId());
                throw new RuntimeException(e);
            }
        }
    }
}
