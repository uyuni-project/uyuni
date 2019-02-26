/**
 * Copyright (c) 2019 SUSE LLC
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
import javax.persistence.OneToOne;

import static java.util.Optional.of;

/**
 * Content Environment Target targeting software Channel
 */
@Entity
@DiscriminatorValue("software")
public class SoftwareEnvironmentTarget extends EnvironmentTarget {

    private Channel channel;

    /**
     * Standard constructor
     */
    public SoftwareEnvironmentTarget() {
    }

    /**
     * Standard constructor
     *
     * @param envIn the environment
     * @param channelIn the channel
     */
    public SoftwareEnvironmentTarget(ContentEnvironment envIn, Channel channelIn) {
        super(envIn);
        this.channel = channelIn;
    }

    @Override
    public Optional<SoftwareEnvironmentTarget> asSoftwareTarget() {
        return of(this);
    }

    /**
     * Gets the channel.
     *
     * @return channel
     */
    @OneToOne
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
     * {@inheritDoc}
     */
    @Override
    public String toString() {
        return super.toStringBuilder()
                .append("channel", channel)
                .toString();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }

        if (o == null || getClass() != o.getClass()) {
            return false;
        }

        SoftwareEnvironmentTarget that = (SoftwareEnvironmentTarget) o;

        return new EqualsBuilder()
                .append(channel, that.channel)
                .append(getContentEnvironment(), that.getContentEnvironment())
                .isEquals();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public int hashCode() {
        return new HashCodeBuilder(17, 37)
                .append(channel)
                .append(getContentEnvironment())
                .toHashCode();
    }
}
