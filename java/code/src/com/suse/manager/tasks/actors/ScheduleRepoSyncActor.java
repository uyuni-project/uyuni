package com.suse.manager.tasks.actors;

import static akka.actor.typed.javadsl.Behaviors.receive;
import static akka.actor.typed.javadsl.Behaviors.same;
import static akka.actor.typed.javadsl.Behaviors.setup;

import com.redhat.rhn.domain.channel.Channel;
import com.redhat.rhn.domain.org.Org;
import com.redhat.rhn.domain.user.User;
import com.redhat.rhn.domain.user.UserFactory;
import com.redhat.rhn.manager.channel.ChannelManager;
import com.redhat.rhn.taskomatic.TaskomaticApi;
import com.redhat.rhn.taskomatic.TaskomaticApiException;

import com.suse.manager.tasks.Actor;
import com.suse.manager.tasks.Command;
import org.apache.log4j.Logger;

import java.util.List;
import java.util.stream.Collectors;

import akka.actor.typed.Behavior;

public class ScheduleRepoSyncActor implements Actor {

    private final static Logger LOG = Logger.getLogger(ScheduleRepoSyncActor.class);

    public static class Message implements Command {
        private final List<String> channelLabels;
        private final Long userId;

        public Message(List<String> channelLabels, Long userId) {
            this.channelLabels = channelLabels;
            this.userId = userId;
        }
    }

    public Behavior<Command> create() {
        return setup(context -> receive(Command.class)
                .onMessage(Message.class, message -> onMessage(message))
                .build());
    }

    private Behavior<Command> onMessage(Message message) {
        execute(message);
        return same();
    }

    public void execute(Message event) {
        if (LOG.isDebugEnabled()) {
            LOG.debug("Scheduling repo sync for channels: " + event.channelLabels);
        }
        scheduleRepoSync(event.channelLabels, event.userId);
    }

    /**
     * Schedule an immediate reposync via the Taskomatic API.
     *
     * @param channelLabels labels of the channel to sync
     * @param userId id of user requesting the sync
     */
    private void scheduleRepoSync(List<String> channelLabels, Long userId) {
        User user = UserFactory.lookupById(userId);
        if (user != null && !channelLabels.isEmpty()) {
            Org org = user.getOrg();

            List<Channel> channels = channelLabels.stream()
                    .map(label -> ChannelManager.lookupByLabel(org, label))
                    .collect(Collectors.toList());

            try {
                new TaskomaticApi().scheduleSingleRepoSync(channels);
            }
            catch (TaskomaticApiException e) {
                LOG.error("Could not schedule repository synchronization for: " +
                        channels.toString());
                LOG.error(e);
            }
        }
    }
}
