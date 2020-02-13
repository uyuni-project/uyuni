package com.suse.manager.tasks.actors;

import static akka.actor.typed.javadsl.Behaviors.receive;
import static akka.actor.typed.javadsl.Behaviors.same;
import static akka.actor.typed.javadsl.Behaviors.setup;
import static com.redhat.rhn.frontend.events.TransactionHelper.handlingTransaction;
import static com.suse.manager.reactor.SaltReactor.THREAD_POOL_SIZE;

import com.redhat.rhn.domain.server.MinionServer;
import com.redhat.rhn.domain.server.MinionServerFactory;
import com.redhat.rhn.domain.server.ServerFactory;
import com.redhat.rhn.domain.token.ActivationKey;
import com.redhat.rhn.domain.token.ActivationKeyFactory;

import com.suse.manager.reactor.messaging.RegistrationUtils;
import com.suse.manager.reactor.utils.ValueMap;
import com.suse.manager.tasks.Actor;
import com.suse.manager.tasks.Command;
import com.suse.manager.webui.services.impl.SaltService;
import com.suse.manager.webui.utils.salt.ImageDeployedEvent;
import org.apache.log4j.Logger;

import java.util.Optional;

import akka.actor.typed.Behavior;

public class ImageDeployedActor implements Actor {

    private final static Logger LOG = Logger.getLogger(ImageDeployedActor.class);

    @Override
    public int getMaxParallelWorkers() {
        return THREAD_POOL_SIZE;
    }

    public static class Message implements Command {
        private ImageDeployedEvent imageDeployedEvent;

        public Message(ImageDeployedEvent imageDeployedEvent) {
            this.imageDeployedEvent = imageDeployedEvent;
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
        var imageDeployedEvent = msg.imageDeployedEvent;
        LOG.info("Finishing minion registration for machine id " + imageDeployedEvent.getMachineId());

        if (!imageDeployedEvent.getMachineId().isPresent()) {
            LOG.warn("Machine id grain is not present in event data: " + imageDeployedEvent +
                    " . Skipping post image-deploy actions.");
            return;
        }

        Optional<MinionServer> minion = imageDeployedEvent.getMachineId().flatMap(MinionServerFactory::findByMachineId);
        if (!minion.isPresent()) {
            LOG.warn("Minion id '" + imageDeployedEvent.getMachineId() +
                    "' not found. Skipping post-image deploy actions.");
            return;
        }

        minion.ifPresent(m -> {
            LOG.info("System image of minion id '" + m.getId() + "' has changed. Re-applying activation key," +
                    " subscribing to channels and executing post-registration tasks.");
            ValueMap grains = imageDeployedEvent.getGrains();

            grains.getOptionalAsString("osarch").ifPresent(osarch -> {
                m.setServerArch(ServerFactory.lookupServerArchByLabel(osarch + "-redhat-linux"));
            });

            Optional<String> activationKeyLabel = grains
                    .getMap("susemanager")
                    .flatMap(suma -> suma.getOptionalAsString("activation_key"));
            Optional<ActivationKey> activationKey = activationKeyLabel.map(ActivationKeyFactory::lookupByKey);

            // we want to clear assigned channels first
            m.getChannels().clear();
            RegistrationUtils.subscribeMinionToChannels(SaltService.INSTANCE, m, grains, activationKey, activationKeyLabel);
            activationKey.ifPresent(ak -> RegistrationUtils.applyActivationKeyProperties(m, ak, grains));
            RegistrationUtils.finishRegistration(m, activationKey, Optional.empty(), false);
        });
    }
}
