package com.suse.manager.tasks.actors;

import static akka.actor.typed.javadsl.Behaviors.receive;
import static akka.actor.typed.javadsl.Behaviors.same;
import static akka.actor.typed.javadsl.Behaviors.setup;
import static com.redhat.rhn.frontend.events.TransactionHelper.handlingTransaction;
import static java.util.stream.Collectors.toList;

import com.redhat.rhn.common.security.PermissionException;
import com.redhat.rhn.domain.channel.Channel;
import com.redhat.rhn.domain.channel.ChannelFactory;
import com.redhat.rhn.domain.contentmgmt.ContentFilter;
import com.redhat.rhn.domain.contentmgmt.ContentProjectFactory;
import com.redhat.rhn.domain.contentmgmt.EnvironmentTarget.Status;
import com.redhat.rhn.domain.contentmgmt.SoftwareEnvironmentTarget;
import com.redhat.rhn.domain.user.User;
import com.redhat.rhn.domain.user.UserFactory;
import com.redhat.rhn.manager.EntityNotExistsException;
import com.redhat.rhn.manager.contentmgmt.ContentManager;
import com.redhat.rhn.manager.user.UserManager;

import com.suse.manager.tasks.Actor;
import com.suse.manager.tasks.Command;
import org.apache.log4j.Logger;

import java.time.Duration;
import java.time.Instant;
import java.util.List;

import akka.actor.typed.Behavior;

public class AlignSoftwareTargetActor implements Actor {

    private final static Logger LOG = Logger.getLogger(AlignSoftwareTargetActor.class);

    public static class Message implements Command {
        private final Long sourceChannelId;
        private final Long targetId;
        private final List<Long> filterIds;
        private final Long userId;

        public Message(Long sourceChannelId, Long targetId, List<Long> filterIds, Long userId) {
            this.sourceChannelId = sourceChannelId;
            this.targetId = targetId;
            this.filterIds = filterIds;
            this.userId = userId;
        }
    }

    public Behavior<Command> create() {
        return setup(context -> receive(Command.class)
                .onMessage(Message.class, message -> onMessage(message))
                .build());
    }

    public Behavior<Command> onMessage(Message message) {
        handlingTransaction(() -> execute(message), this::exceptionHandler);
        return same();
    }

    public void execute(Message msg) {
        Channel sourceChannel = ChannelFactory.lookupById(msg.sourceChannelId);
        Long targetId = msg.targetId;
        SoftwareEnvironmentTarget target = ContentProjectFactory
                .lookupSwEnvironmentTargetById(targetId)
                .orElseThrow(() -> new EntityNotExistsException(targetId));
        Channel targetChannel = target.getChannel();
        List<ContentFilter> filters = msg.filterIds
                .stream()
                .map(id -> ContentProjectFactory.lookupFilterById(id).get())
                .collect(toList());
        User user = UserFactory.lookupById(msg.userId);

        try {
            if (!UserManager.verifyChannelAdmin(user, targetChannel)) {
                throw new PermissionException("User " + user.getLogin() + " has no permission for channel " +
                        targetChannel.getLabel());
            }

            LOG.info("Asynchronously aligning: " + msg);
            Instant start = Instant.now();
            ContentManager.alignEnvironmentTargetSync(filters, sourceChannel, targetChannel, user);
            target.setStatus(Status.GENERATING_REPODATA);
            LOG.info("Finished aligning " + msg + " in " + Duration.between(start, Instant.now()));
        }
        catch (Throwable t) {
            throw new AlignSoftwareTargetActor.AlignSoftwareTargetException(target, t);
        }
    }

    public void exceptionHandler(Exception e) {
        if (e instanceof AlignSoftwareTargetActor.AlignSoftwareTargetException) {
            LOG.error("Error aligning target " + ((AlignSoftwareTargetActor.AlignSoftwareTargetException) e).getTarget().getId(), e);
            AlignSoftwareTargetActor.AlignSoftwareTargetException exc = ((AlignSoftwareTargetActor.AlignSoftwareTargetException) e);
            exc.getTarget().setStatus(Status.FAILED);
            ContentProjectFactory.save(exc.getTarget());
        }
    }

    private class AlignSoftwareTargetException extends RuntimeException {
        private SoftwareEnvironmentTarget target;

        AlignSoftwareTargetException(SoftwareEnvironmentTarget targetIn, Throwable cause) {
            super(cause);
            this.target = targetIn;
        }

        /**
         * Gets the target.
         *
         * @return target
         */
        public SoftwareEnvironmentTarget getTarget() {
            return target;
        }
    }
}
