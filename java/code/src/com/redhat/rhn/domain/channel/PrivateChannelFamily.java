/*
 * Copyright (c) 2009--2014 Red Hat, Inc.
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

import com.redhat.rhn.domain.BaseDomainHelper;
import com.redhat.rhn.domain.org.Org;

import org.apache.commons.lang3.builder.EqualsBuilder;
import org.apache.commons.lang3.builder.HashCodeBuilder;

import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.IdClass;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.Table;

/**
 * PrivateChannelFamily - Class representation of the table rhnPrivateChannelFamily.
 */
@Entity
@Table(name = "rhnPrivateChannelFamily")
@IdClass(PrivateChannelFamilyId.class)
public class PrivateChannelFamily extends BaseDomainHelper {

    private static final long serialVersionUID = 1L;

    @Id
    @ManyToOne(targetEntity = ChannelFamily.class)
    @JoinColumn(name = "CHANNEL_FAMILY_ID")
    private ChannelFamily channelFamily;

    @Id
    @ManyToOne(targetEntity = Org.class)
    @JoinColumn(name = "ORG_ID")
    private Org org;

    /**
     * @return Returns the channelFamily.
     */
    public ChannelFamily getChannelFamily() {
        return channelFamily;
    }

    /**
     * @param channelFamilyIn The channelFamily to set.
     */
    public void setChannelFamily(ChannelFamily channelFamilyIn) {
        this.channelFamily = channelFamilyIn;
    }

    /**
     * @return Returns the org.
     */
    public Org getOrg() {
        return org;
    }

    /**
     * @param orgIn The org to set.
     */
    public void setOrg(Org orgIn) {
        this.org = orgIn;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public boolean equals(final Object other) {
        if (!(other instanceof PrivateChannelFamily castOther)) {
            return false;
        }
        return new EqualsBuilder().append(this.getChannelFamily(),
                castOther.getChannelFamily()).append(this.getOrg(),
                        castOther.getOrg()).isEquals();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public int hashCode() {
        return new HashCodeBuilder().append(this.getChannelFamily()).append(this.getOrg())
                .toHashCode();
    }

}
