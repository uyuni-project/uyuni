/**
 * Copyright (c) 2018 SUSE LLC
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

package com.redhat.rhn.domain.contentmgmt;

import com.redhat.rhn.domain.channel.Channel;
import org.apache.commons.lang3.builder.EqualsBuilder;
import org.apache.commons.lang3.builder.HashCodeBuilder;

import java.util.Optional;
import javax.persistence.DiscriminatorValue;
import javax.persistence.Entity;
import javax.persistence.JoinColumn;
import javax.persistence.OneToOne;

import static java.util.Optional.of;

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
    public SoftwareProjectSource() { }

    /**
     * Standard constructor
     *
     * @param channelIn the channel
     * @param projectIn the project
     */
    public SoftwareProjectSource(ContentProject projectIn, Channel channelIn) {
        super(projectIn);
        this.channel = channelIn;
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

    /**
     * Gets the source Label.
     *
     * @return source label
     */
    public String sourceLabel() {
        return this.channel.getLabel();
    }

    /**
     * Gets the source Type.
     *
     * @return source Type
     */
    public Type sourceType() {
        return Type.SW_CHANNEL;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Optional<SoftwareProjectSource> asSoftwareSource() {
        return of(this);
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }

        if (o == null || getClass() != o.getClass()) {
            return false;
        }

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
