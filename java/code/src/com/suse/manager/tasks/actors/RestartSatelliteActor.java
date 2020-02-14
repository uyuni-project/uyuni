package com.suse.manager.tasks.actors;

import static akka.actor.typed.javadsl.Behaviors.receive;
import static akka.actor.typed.javadsl.Behaviors.same;
import static akka.actor.typed.javadsl.Behaviors.setup;

import com.redhat.rhn.common.localization.LocalizationService;
import com.redhat.rhn.common.validator.ValidatorError;
import com.redhat.rhn.domain.user.User;
import com.redhat.rhn.domain.user.UserFactory;
import com.redhat.rhn.manager.satellite.RestartCommand;

import com.suse.manager.tasks.Command;
import org.apache.log4j.Logger;

import akka.actor.typed.Behavior;

public class RestartSatelliteActor {

    private final static Logger LOG = Logger.getLogger(RestartSatelliteActor.class);

    public static class Message implements Command {
        private final Long userId;

        public Message(Long userId) {
            this.userId = userId;
        }
    }

    public Behavior<Command> create() {
        return setup(context -> receive(Command.class)
                .onMessage(Message.class, message -> onMessage(message))
                .build());
    }

    private Behavior<Command> onMessage(Message message) {
        User user = UserFactory.lookupById(message.userId);
        RestartCommand rc = new RestartCommand(user);

        // This is a pretty intrusive action so we want to log it.
        LOG.warn("Restarting satellite.");
        ValidatorError[] errors = rc.storeConfiguration();
        if (errors != null) {
            for (int i = 0; i < errors.length; i++) {
                ValidatorError error = errors[i];
                LOG.error("Error trying to restart the satellite: " +
                        LocalizationService.getInstance()
                                .getMessage(error.getKey(), LocalizationService.getUserLocale()));
            }
        }

        return same();
    }
}
