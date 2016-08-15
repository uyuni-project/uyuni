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
package com.suse.manager.webui.utils;

import static java.util.Optional.ofNullable;
import static java.util.stream.Collectors.toList;

import com.redhat.rhn.domain.channel.Channel;
import com.redhat.rhn.domain.channel.ChannelFactory;
import com.redhat.rhn.domain.user.User;
import com.redhat.rhn.taskomatic.TaskoFactory;
import com.redhat.rhn.taskomatic.TaskoRun;
import com.redhat.rhn.taskomatic.task.RepoSyncTask;

import com.suse.utils.Opt;

import org.apache.commons.lang.builder.ToStringBuilder;

import java.util.Date;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

/**
 * A Taskomatic Job Object.
 */
public class TaskoTopJob {

    private Long id;
    private String name;
    private Date startTime;
    private Date endTime;
    private Long elapsedTime;
    private List<String> data;
    private String status;

    /**
     * Constructor.
     */
    public TaskoTopJob() { }

    /**
     * Constructor with all parameters.
     *
     * @param idIn the id in
     * @param nameIn the name in
     * @param startTimeIn the start time in
     * @param endTimeIn the end time in
     * @param elapsedTimeIn the elapsed time in
     * @param dataIn the data in
     * @param statusIn the status in
     */
    public TaskoTopJob(Long idIn, String nameIn, Date startTimeIn, Date endTimeIn,
            Long elapsedTimeIn, List<String> dataIn, String statusIn) {
        id = idIn;
        name = nameIn;
        startTime = startTimeIn;
        endTime = endTimeIn;
        elapsedTime = elapsedTimeIn;
        data = dataIn;
        status = statusIn;
    }

    /**
     * Constructor of a TaskoTopJob object build on a TaskoRun object.
     *
     * @param taskoRun the source object
     * @param user the current user, needed to get channels data
     */
    public TaskoTopJob(TaskoRun taskoRun, User user) {
        id = taskoRun.getId();
        name = taskoRun.getTemplate().getTask().getName();
        startTime = taskoRun.getStartTime();
        endTime = taskoRun.getEndTime();
        elapsedTime = taskoRun.getEndTime() != null ?
                (taskoRun.getEndTime().getTime() -
                        taskoRun.getStartTime().getTime()) / 1000 :
                (new Date(System.currentTimeMillis()).getTime() -
                        taskoRun.getStartTime().getTime()) / 1000;
        data = formatChannelsData(taskoRun.getScheduleId(), user);
        status = taskoRun.getStatus().toLowerCase();
    }

    /**
     * Decode data that contains channel ids,
     * then extract the channel names.
     *
     * @param scheduleId the id of the scheduled task
     * @param user the current user
     * @return a List of String of channel names
     */
    public List<String> formatChannelsData(Long scheduleId, User user) {
        Map<String, Object> map = TaskoFactory.lookupScheduleById(scheduleId).getDataMap();
        return ofNullable(map)
                .map(RepoSyncTask::getChannelIds)
                .orElseGet(LinkedList::new)
                .stream()
                .flatMap(cid -> Opt.stream(
                        ofNullable(ChannelFactory.lookupByIdAndUser(cid, user))))
                .map(Channel::getName)
                .collect(toList());
    }

    /**
     * @return Returns the id.
     */
    public Long getId() {
        return id;
    }

    /**
     * @param idIn The id to set.
     */
    public void setId(Long idIn) {
        this.id = idIn;
    }

    /**
     * @return Returns the name.
     */
    public String getName() {
        return name;
    }

    /**
     * @param nameIn The name to set.
     */
    public void setName(String nameIn) {
        this.name = nameIn;
    }

    /**
     * @return Returns the startTime.
     */
    public Date getStartTime() {
        return startTime;
    }

    /**
     * @param startTimeIn The startTime to set.
     */
    public void setStartTime(Date startTimeIn) {
        this.startTime = startTimeIn;
    }

    /**
     * @return Returns the endTime.
     */
    public Date getEndTime() {
        return endTime;
    }

    /**
     * @param endTimeIn The endTime to set.
     */
    public void setEndTime(Date endTimeIn) {
        this.endTime = endTimeIn;
    }

    /**
     * @return Returns the elapsedTime.
     */
    public Long getElapsedTime() {
        return elapsedTime;
    }

    /**
     * @param elapsedTimeIn The elapsedTime to set.
     */
    public void setElapsedTime(Long elapsedTimeIn) {
        this.elapsedTime = elapsedTimeIn;
    }

    /**
     * @return Returns the data.
     */
    public List<String> getData() {
        return data;
    }

    /**
     * @param dataIn The data to set.
     */
    public void setData(List<String> dataIn) {
        this.data = dataIn;
    }

    /**
     * @return Returns the status.
     */
    public String getStatus() {
        return status;
    }

    /**
     * @param statusIn The status to set.
     */
    public void setStatus(String statusIn) {
        this.status = statusIn;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public String toString() {
        return new ToStringBuilder(this)
        .append("Id", getId())
        .append("Name", getName())
        .append("StartTime", getStartTime())
        .append("EndTime", getEndTime())
        .append("ElapsedTime", getElapsedTime())
        .append("Data", getData())
        .append("Status", getStatus())
        .toString();
    }
}
