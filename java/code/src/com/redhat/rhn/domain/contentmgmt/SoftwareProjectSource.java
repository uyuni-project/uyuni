package com.redhat.rhn.domain.contentmgmt;

import com.redhat.rhn.domain.channel.Channel;
import org.apache.commons.lang3.builder.EqualsBuilder;
import org.apache.commons.lang3.builder.HashCodeBuilder;
import org.apache.commons.lang3.builder.ToStringBuilder;
import org.hibernate.annotations.Cascade;
import org.hibernate.annotations.CascadeType;

import javax.persistence.DiscriminatorValue;
import javax.persistence.Entity;
import javax.persistence.JoinColumn;
import javax.persistence.OneToOne;

/**
 * Software Project Source
 */
@Entity
@DiscriminatorValue("software")
public class SoftwareProjectSource extends ProjectSource {

    private Channel channel;

    /**
     * Standard constructor
     */
    public SoftwareProjectSource() {
    }

    /**
     * Standard constructor
     */
    public SoftwareProjectSource(Channel channel) {
        this.channel = channel;
    }

    @Override
    public void publish() {
        // todo clone channel and apply filters here
    }

    /**
     * Get the channel
     *
     * @return the channel
     */
    @OneToOne
    @JoinColumn(name = "channel_id")
    public Channel getChannel() {
        return channel;
    }

    /**
     * Sets the channel.
     *
     * @param channelIn - the channel
     */
    public void setChannel(Channel channelIn) {
        channel = channelIn;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;

        if (o == null || getClass() != o.getClass()) return false;

        SoftwareProjectSource that = (SoftwareProjectSource) o;

        return new EqualsBuilder()
                .append(channel, that.channel)
                .append(getContentProject(), that.getContentProject())
                .isEquals();
    }

    @Override
    public int hashCode() {
        return new HashCodeBuilder(17, 37)
                .append(channel)
                .append(getContentProject())
                .toHashCode();
    }

    @Override
    public String toString() {
        return super.toStringBuilder()
                .append("channel", channel)
                .toString();
    }
}
