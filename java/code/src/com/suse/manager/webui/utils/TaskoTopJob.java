/**
 * Copyright (c) 2015 SUSE LLC
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

import static java.util.stream.Collectors.toList;

import com.redhat.rhn.domain.channel.Channel;
import com.redhat.rhn.domain.channel.ChannelFactory;
import com.redhat.rhn.domain.user.User;
import com.redhat.rhn.taskomatic.TaskoFactory;
import com.redhat.rhn.taskomatic.TaskoRun;

import org.apache.commons.lang.builder.EqualsBuilder;
import org.apache.commons.lang.builder.HashCodeBuilder;
import org.apache.commons.lang.builder.ToStringBuilder;

import java.util.Date;
import java.util.LinkedList;
import java.util.List;

/**
 * A Taskomatic Job Object.
 */
public class TaskoTopJob {

    private Long id;
    private String name;
    private Date startTime;
    private Date endTime;
    private List<String> data;

    /**
     * Constructor.
     */
    public TaskoTopJob() { }

    /**
     * 
     * @param idIn
     * @param nameIn
     * @param startTimeIn
     * @param endTimeIn
     * @param dataIn
     */
    public TaskoTopJob(Long idIn, String nameIn, Date startTimeIn, Date endTimeIn, List<String> dataIn) {
        id = idIn;
        name = nameIn;
        startTime = startTimeIn;
        endTime = endTimeIn;
        data = dataIn;
    }

    /**
     * Generate a TaskoTopJob object from a TaskoRun source
     * @param taskoRun the source object
     * @return a TaskoTopJob object with the taskoRun source values
     */
    public TaskoTopJob generateFromTaskoRun(TaskoRun taskoRun, User user) {
        return new TaskoTopJob(
                taskoRun.getId(),
                taskoRun.getTemplate().getTask().getName(),
                taskoRun.getStartTime(),
                taskoRun.getEndTime(),
                formatChannelsData(TaskoFactory.lookupScheduleById(taskoRun.getScheduleId()).getData(), user));
    }

    /**
     * Decode data assuming that if there is any value it contains channel ids,
     * then extract the channel names
     * @param dataIn the blob data
     * @return a List of String of channel names
     */
    public List<String>formatChannelsData (byte[] dataIn, User user) {
        List<Long> channelIds = new LinkedList<Long>();
        if (dataIn != null && dataIn.length > 0) {
            for (byte b : dataIn) {
                channelIds.add(Byte.toUnsignedLong(b));
            }
        }

        return channelIds.stream()
                .distinct()
                .map(id -> {
                    Channel c = ChannelFactory.lookupByIdAndUser(id, user);
                    return c != null ? c.getName() : null;
                })
                .collect(toList()).stream()
                .filter(c -> c != null)
                .sorted((c1, c2) -> c1.compareToIgnoreCase(c2))
                .collect(toList());
    }

    /**
     * @return Returns the id.
     */
    public Long getId() {
        return id;
    }

    /**
     * @param id The id to set.
     */
    public void setId(Long id) {
        this.id = id;
    }

    /**
     * @return Returns the name.
     */
    public String getName() {
        return name;
    }

    /**
     * @param name The name to set.
     */
    public void setName(String name) {
        this.name = name;
    }

    /**
     * @return Returns the startTime.
     */
    public Date getStartTime() {
        return startTime;
    }

    /**
     * @param startTime The startTime to set.
     */
    public void setStartTime(Date startTime) {
        this.startTime = startTime;
    }

    /**
     * @return Returns the endTime.
     */
    public Date getEndTime() {
        return endTime;
    }

    /**
     * @param endTime The endTime to set.
     */
    public void setEndTime(Date endTime) {
        this.endTime = endTime;
    }

    /**
     * @return Returns the data.
     */
    public List<String> getData() {
        return data;
    }

    /**
     * @param data The data to set.
     */
    public void setData(List<String> data) {
        this.data = data;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public boolean equals(Object other) {
        if (!(other instanceof TaskoTopJob)) {
            return false;
        }
        TaskoTopJob otherJob = (TaskoTopJob) other;
        return new EqualsBuilder()
            .append(getId(), otherJob.getId())
            .append(getName(), otherJob.getName())
            .append(getStartTime(), otherJob.getStartTime())
            .isEquals();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public int hashCode() {
        return new HashCodeBuilder()
            .append(getId())
            .append(getName())
            .append(getStartTime())
            .append(getData())
            .toHashCode();
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
        .append("Data", getData())
        .toString();
    }
}
