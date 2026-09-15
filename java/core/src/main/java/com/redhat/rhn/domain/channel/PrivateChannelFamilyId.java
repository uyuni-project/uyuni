/*
 * Copyright (c) 2025 SUSE LLC
 *
 * This software is licensed to you under the GNU General Public License,
 * version 2 (GPLv2). There is NO WARRANTY for this software, express or
 * implied, including the implied warranties of MERCHANTABILITY or FITNESS
 * FOR A PARTICULAR PURPOSE. You should have received a copy of GPLv2
 * along with this software; if not, see
 * http://www.gnu.org/licenses/old-licenses/gpl-2.0.txt.
 *
 * SPDX-License-Identifier: GPL-2.0-only
 */

package com.redhat.rhn.domain.channel;

import com.redhat.rhn.domain.org.Org;

import org.apache.commons.lang3.builder.EqualsBuilder;
import org.apache.commons.lang3.builder.HashCodeBuilder;

import java.io.Serial;
import java.io.Serializable;


public class PrivateChannelFamilyId implements Serializable {

    @Serial
    private static final long serialVersionUID = 4038630301606491852L;

    private ChannelFamily channelFamily;

    private Org org;

    /**
     * Constructor
     */
    public PrivateChannelFamilyId() {
    }

    /**
     * Constructor
     *
     * @param channelFamilyIn the input channelFamily
     * @param orgIn           the input org
     */
    public PrivateChannelFamilyId(ChannelFamily channelFamilyIn, Org orgIn) {
        channelFamily = channelFamilyIn;
        org = orgIn;
    }

    public ChannelFamily getChannelFamily() {
        return channelFamily;
    }

    public void setChannelFamily(ChannelFamily channelFamilyIn) {
        channelFamily = channelFamilyIn;
    }

    public Org getOrg() {
        return org;
    }

    public void setOrg(Org orgIn) {
        org = orgIn;
    }

    @Override
    public boolean equals(Object oIn) {
        if (!(oIn instanceof PrivateChannelFamilyId that)) {
            return false;
        }

        return new EqualsBuilder()
                .append(channelFamily, that.channelFamily)
                .append(org, that.org)
                .isEquals();
    }

    @Override
    public int hashCode() {
        return new HashCodeBuilder()
                .append(channelFamily)
                .append(org)
                .toHashCode();
    }

    @Override
    public String toString() {
        return "PrivateChannelFamilyId{" +
                "channelFamily=" + channelFamily +
                ", org=" + org +
                '}';
    }
}
