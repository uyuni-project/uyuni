/**
 * Copyright (c) 2009--2015 Red Hat, Inc.
 *
 * This software is licensed to you under the GNU General Public License,
 * version 2 (GPLv2). There is NO WARRANTY for this software, express or
 * implied, including the implied warranties of MERCHANTABILITY or FITNESS
 * FOR A PARTICULAR PURPOSE. You should have received a copy of GPLv2
 * along with this software; if not, see
 * http://www.gnu.org/licenses/old-licenses/gpl-2.0.txt.
 *
 * Red Hat trademarks are not licensed under GPLv2. No permission is
 * granted to use or replicate Red Hat trademarks that are incorporated
 * in this software or its documentation.
 */
package com.redhat.rhn.taskomatic.task;

import com.redhat.rhn.common.conf.Config;
import com.redhat.rhn.common.conf.ConfigDefaults;
import com.redhat.rhn.domain.channel.Channel;
import com.redhat.rhn.domain.channel.ChannelFactory;

import com.redhat.rhn.domain.channel.ContentSource;
import com.redhat.rhn.domain.notification.NotificationMessage;
import com.redhat.rhn.domain.notification.UserNotificationFactory;
import com.redhat.rhn.domain.notification.types.ChannelSyncFailed;
import com.redhat.rhn.domain.notification.types.ChannelSyncFinished;
import com.redhat.rhn.domain.role.RoleFactory;

import org.quartz.JobExecutionContext;
import org.quartz.JobExecutionException;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Optional;

/**
 * Used for syncing repos (like yum repos) to a channel.
 * This really just calls a python script.
 */
public class RepoSyncTask extends RhnJavaJob {

    /**
     * {@inheritDoc}
     */
    @Override
    public void execute(JobExecutionContext context) {
        List<Long> channelIds = getChannelIds(context.getJobDetail().getJobDataMap());

        String[] lparams = {"no-errata", "latest", "sync-kickstart", "fail"};
        List<String> ltrue = Arrays.asList("true", "1");
        List<String> params = new ArrayList<String>();
        for (String p : lparams) {
            if (context.getJobDetail().getJobDataMap().containsKey(p)) {
                if (ltrue.contains(context.getJobDetail().getJobDataMap().get(p).toString()
                        .toLowerCase().trim())) {
                    params.add("--" + p);
                }
            }
        }

        for (Long channelId : channelIds) {
            Channel channel = ChannelFactory.lookupById(channelId);
            if (channel != null) {
                log.info("Syncing repos for channel: " + channel.getName());

                try {
                    executeExtCmd(getSyncCommand(channel, params).toArray(new String[0]));
                }
                catch (JobExecutionException e) {
                    NotificationMessage notificationMessage = UserNotificationFactory.createNotificationMessage(
                            new ChannelSyncFailed(channel.getId(), channel.getName())
                    );
                    if (channel.getOrg() == null) {
                        UserNotificationFactory.storeNotificationMessageFor(notificationMessage,
                                Collections.singleton(RoleFactory.CHANNEL_ADMIN), Optional.empty());
                    }
                    else {
                        UserNotificationFactory.storeNotificationMessageFor(notificationMessage,
                                Collections.singleton(RoleFactory.CHANNEL_ADMIN), Optional.of(channel.getOrg()));
                    }


                    log.error(e.getMessage());
                }
                NotificationMessage notificationMessage = UserNotificationFactory.createNotificationMessage(
                        new ChannelSyncFinished(channel.getId(), channel.getName())
                );
                if (channel.getOrg() == null) {
                    UserNotificationFactory.storeNotificationMessageFor(notificationMessage,
                            Collections.singleton(RoleFactory.CHANNEL_ADMIN), Optional.empty());
                }
                else {
                    UserNotificationFactory.storeNotificationMessageFor(notificationMessage,
                            Collections.singleton(RoleFactory.CHANNEL_ADMIN), Optional.of(channel.getOrg()));
                }
            }
            else {
                log.error("No such channel with channel_id " + channelId);
            }
        }
    }

    /**
     * Gets the ids of channel(s) in a schedule/job data map.
     *
     * @param dataMap the data map
     * @return the channel ids
     */
    @SuppressWarnings("unchecked")
    public static List<Long> getChannelIds(final Map<String, Object> dataMap) {
        List<Long> result = new LinkedList<>();
        if (dataMap != null) {
            // bulk reposync case
            List<String> channelIds = (List<String>) dataMap.get("channel_ids");

            // single reposync case
            if (channelIds == null) {
                channelIds = new LinkedList<>();
                String channelId = (String) dataMap.get("channel_id");
                if (channelId != null) {
                    channelIds.add(channelId);
                }
            }

            // String -> Long
            for (String channelId : channelIds) {
                try {
                  result.add(Long.parseLong(channelId));
                }
                catch (NumberFormatException nfe) {
                    // there is a channel id but it is not valid
                }
            }
        }
        return result;
    }

    private static List<String> getSyncCommand(Channel c, List<String> params) {
        List<String> cmd = new ArrayList<String>();
        cmd.add(Config.get().getString(ConfigDefaults.SPACEWALK_REPOSYNC_PATH,
                "/usr/bin/spacewalk-repo-sync"));
        cmd.add("--channel");
        cmd.add(c.getLabel());
        cmd.add("--type");
        List<ContentSource> sources = ChannelFactory.lookupContentSources(c.getOrg(), c);
        if (sources.isEmpty()) {
            cmd.add("yum");
        }
        else {
            cmd.add(sources.get(0).getType().getLabel());
        }
        cmd.add("--non-interactive");
        cmd.addAll(params);
        return cmd;
    }
}
