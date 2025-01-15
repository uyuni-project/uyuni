/*
 * Copyright (c) 2009--2012 Red Hat, Inc.
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
package com.redhat.rhn.domain.channel;

import com.redhat.rhn.domain.common.ChecksumType;

import org.apache.commons.lang3.builder.EqualsBuilder;
import org.apache.commons.lang3.builder.HashCodeBuilder;

import java.util.Optional;

import javax.persistence.Entity;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.Table;
import javax.persistence.Transient;

/**
 * ClonedChannel
 */
@Entity
@Table(name = "rhnChannelCloned")
public class ClonedChannel extends Channel {

    private Channel original;

    /**
     * @return the original Channel the channel was cloned from
     */
    @ManyToOne
    @JoinColumn(name = "original_id")
    public Channel getOriginal() {
        return original;
    }

    public void setOriginal(Channel originalIn) {
        original = originalIn;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    @Transient
    public boolean isCloned() {
        return true;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    @Transient
    public Optional<ClonedChannel> asCloned() {
        return Optional.of(this);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    @Transient
    public ChecksumType getChecksumType() {
        // We can reach a StackOverflow here if the current channel is also the
        // original. Happening because of a Hibernate problem. See BZ 837913.
        if (super.getChecksumType() == null &&
                !super.getLabel().equals(getOriginal().getLabel())) {
            // if the checksum type is not set use the
            //checksum of original channel instead.
            setChecksumType(getOriginal().getChecksumType());
        }
        return super.getChecksumType();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public boolean equals(final Object other) {
        if (!(other instanceof ClonedChannel castOther)) {
            return false;
        }
        return new EqualsBuilder().append(getOriginal(), castOther.getOriginal()).isEquals();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public int hashCode() {
        return new HashCodeBuilder().append(getOriginal()).toHashCode();
    }

}
