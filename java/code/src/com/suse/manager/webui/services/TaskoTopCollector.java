/**
 * Copyright (c) 2016 SUSE LLC
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
package com.suse.manager.webui.services;

import com.redhat.rhn.domain.channel.ChannelFactory;
import com.redhat.rhn.domain.user.User;
import com.redhat.rhn.taskomatic.TaskoFactory;
import com.redhat.rhn.taskomatic.TaskoRun;

import com.suse.manager.webui.utils.TaskoTopJob;

import java.util.Date;
import java.util.LinkedList;
import java.util.List;
import java.util.concurrent.TimeUnit;

import static java.util.stream.Collectors.toList;

/**
 * Collect data for the TaskoTop web UI page
 */
public class TaskoTopCollector {

    /**
     * Gets UI-ready data.
     *
     * @return the data
     */
    public Object getData(User userIn) {
        List<TaskoTopJob> jobs = TaskoFactory.listUnfinishedRuns().stream()
                .map(t -> generateTaskoTopJobFromTaskoRun(
                        TaskoFactory.lookupRunById(t.getId()), userIn))
                .sorted((j1, j2) -> j2.getStartTime().compareTo(j1.getStartTime()))
                .collect(toList());

        Date limitTime = new Date(System.currentTimeMillis() - TimeUnit.MINUTES.toMillis(5));

        jobs.addAll(TaskoFactory.listRunsNewerThan(limitTime).stream()
                .filter(j -> j.getEndTime() != null)
                .map(t -> generateTaskoTopJobFromTaskoRun(
                        TaskoFactory.lookupRunById(t.getId()), userIn))
                .sorted((j1, j2) -> j2.getStartTime().compareTo(j1.getStartTime()))
                .sorted((j1, j2) -> j2.getEndTime().compareTo(j1.getEndTime()))
                .collect(toList()));

        return jobs;
    }

    /**
     * Decode data assuming that if there is any value it contains channel ids,
     * then extract the channel names
     * @param dataIn the blob data
     * @return a List of String of channel names
     */
    public static List<String>formatChannelsData (byte[] dataIn, User user) {
        List<String> channelIds = new LinkedList<String>();
        if (dataIn != null && dataIn.length > 0) {
            for (byte b : dataIn) {
                channelIds.add(String.valueOf(b));
            }
        }
        return channelIds.stream()
                .distinct()
                .filter(id -> ChannelFactory.lookupByIdAndUser(Long.valueOf(id), user) != null)
                .map(id -> ChannelFactory.lookupByIdAndUser(Long.valueOf(id), user).getName())
                .collect(toList());
    }

    /**
     * Generate a TaskoTopJob object from a TaskoRun source
     * @param taskoRun the source object
     * @return a TaskoTopJob object with the taskoRun source values
     */
    public static TaskoTopJob generateTaskoTopJobFromTaskoRun(TaskoRun taskoRun, User user) {
        return new TaskoTopJob(
                taskoRun.getId(),
                taskoRun.getTemplate().getTask().getName(),
                taskoRun.getStartTime(),
                taskoRun.getEndTime(),
                formatChannelsData(TaskoFactory.lookupScheduleById(taskoRun.getScheduleId()).getData(), user));
    }
}
