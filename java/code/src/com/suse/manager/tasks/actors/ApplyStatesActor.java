package com.suse.manager.tasks.actors;

import static akka.actor.typed.javadsl.Behaviors.receive;
import static akka.actor.typed.javadsl.Behaviors.same;
import static akka.actor.typed.javadsl.Behaviors.setup;
import static com.redhat.rhn.frontend.events.TransactionHelper.handlingTransaction;

import com.redhat.rhn.domain.action.salt.ApplyStatesAction;
import com.redhat.rhn.domain.server.Server;
import com.redhat.rhn.domain.server.ServerFactory;
import com.redhat.rhn.domain.user.User;
import com.redhat.rhn.domain.user.UserFactory;
import com.redhat.rhn.manager.action.ActionManager;
import com.redhat.rhn.manager.entitlement.EntitlementManager;
import com.redhat.rhn.taskomatic.TaskomaticApi;
import com.redhat.rhn.taskomatic.TaskomaticApiException;

import com.suse.manager.tasks.Actor;
import com.suse.manager.tasks.Command;
import com.suse.manager.utils.MinionServerUtils;
import org.apache.log4j.Logger;

import java.util.Arrays;
import java.util.Date;
import java.util.List;

import akka.actor.typed.Behavior;

public class ApplyStatesActor implements Actor {

    private final static Logger LOG = Logger.getLogger(ApplyStatesActor.class);

    public static final String CERTIFICATE = "certs";
    public static final String PACKAGES = "packages";
    public static final String PACKAGES_PROFILE_UPDATE = "packages.profileupdate";
    public static final String HARDWARE_PROFILE_UPDATE = "hardware.profileupdate";
    public static final String CHANNELS = "channels";
    public static final String CHANNELS_DISABLE_LOCAL_REPOS = "channels.disablelocalrepos";
    public static final String SALT_MINION_SERVICE = "services.salt-minion";
    public static final String SYNC_CUSTOM_ALL = "util.synccustomall";
    public static final String SYNC_STATES = "util.syncstates";
    public static final String DISTUPGRADE = "distupgrade";
    public static final String SALTBOOT = "saltboot";
    public static final String SYSTEM_INFO = "util.systeminfo";
    private static final TaskomaticApi TASKOMATIC_API = new TaskomaticApi();


    public static class Message implements Command {
        private final long serverId;
        private final Long userId;
        private final boolean forcePackageListRefresh;
        private final List<String> stateNames;

        public Message(long serverId, Long userId, boolean forcePackageListRefresh, List<String> stateNames) {
            this.serverId = serverId;
            this.userId = userId;
            this.forcePackageListRefresh = forcePackageListRefresh;
            this.stateNames = stateNames;
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

    public void execute(Message applyStatesEvent) {
        Server server = ServerFactory.lookupById(applyStatesEvent.serverId);

        // Apply states only for salt systems
        if (server != null && server.hasEntitlement(EntitlementManager.SALT)) {
            LOG.debug("Schedule state.apply for " + server.getName() + ": " +
                    applyStatesEvent.stateNames);

            // The scheduling user can be null
            User scheduler = applyStatesEvent.userId != null ?
                    UserFactory.lookupById(applyStatesEvent.userId) : null;

            try {
                // Schedule a "state.apply" action to happen right now
                ApplyStatesAction action = ActionManager.scheduleApplyStates(
                        scheduler,
                        Arrays.asList(server.getId()),
                        applyStatesEvent.stateNames,
                        new Date());
                TASKOMATIC_API.scheduleActionExecution(action,
                        applyStatesEvent.forcePackageListRefresh);

                // For Salt SSH: simply schedule package profile update (no job metadata)
                if (MinionServerUtils.isSshPushMinion(server) &&
                        applyStatesEvent.forcePackageListRefresh) {
                    ActionManager.schedulePackageRefresh(server.getOrg(), server);
                }
            }
            catch (TaskomaticApiException e) {
                LOG.error("Could not schedule state application for system: " +
                        server.getId());
                throw new RuntimeException(e);
            }
        }
    }
}
