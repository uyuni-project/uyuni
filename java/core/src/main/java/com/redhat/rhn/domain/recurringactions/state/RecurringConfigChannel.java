/*
 * Copyright (c) 2023 SUSE LLC
 *
 * This software is licensed to you under the GNU General Public License,
 * version 2 (GPLv2). There is NO WARRANTY for this software, express or
 * implied, including the implied warranties of MERCHANTABILITY or FITNESS
 * FOR A PARTICULAR PURPOSE. You should have received a copy of GPLv2
 * along with this software; if not, see
 * http://www.gnu.org/licenses/old-licenses/gpl-2.0.txt.
 *
 * SPDX-License-Identifier: GPL-2.0-only
 *
 * Red Hat trademarks are not licensed under GPLv2. No permission is
 * granted to use or replicate Red Hat trademarks that are incorporated
 * in this software or its documentation.
 */

package com.redhat.rhn.domain.recurringactions.state;

import com.redhat.rhn.common.hibernate.LookupException;
import com.redhat.rhn.domain.config.ConfigChannel;
import com.redhat.rhn.domain.config.ConfigChannelType;
import com.redhat.rhn.domain.config.ConfigurationFactory;
import com.redhat.rhn.domain.user.User;

import com.suse.manager.webui.services.ConfigChannelSaltManager;

import javax.persistence.DiscriminatorValue;
import javax.persistence.Entity;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.Transient;

/**
 * Recurring State configuration for config channel implementation
 */

@Entity
@DiscriminatorValue("CONFCHAN")
public class RecurringConfigChannel extends RecurringStateConfig {

    private ConfigChannel configChannel;

    /**
     * Standard constructor
     */
    public RecurringConfigChannel() {
    }

    /**
     * Constructor
     *
     * @param configChannelIn the config channel
     * @param positionIn the position
     */
    public RecurringConfigChannel(ConfigChannel configChannelIn, Long positionIn) {
        super(positionIn);
        this.configChannel = configChannelIn;
    }

    /**
     * Constructor
     *
     * @param channelLabelIn the label of the config channel
     * @param userIn the user
     * @param positionIn the position
     */
    public RecurringConfigChannel(String channelLabelIn, User userIn, Long positionIn) {
        super(positionIn);
        ConfigChannel channel = ConfigurationFactory.lookupConfigChannelByLabel(
                channelLabelIn, userIn.getOrg(), ConfigChannelType.state());
        if (channel != null) {
            this.configChannel = channel;
        }
        else {
            throw new LookupException("Config state channel with label: " + channelLabelIn + " does not exist!");
        }
    }

    @Override
    @Transient
    public String getStateName() {
        return ConfigChannelSaltManager.getInstance().getChannelStateName(this.configChannel);
    }

    /**
     * Gets the Config Channel
     *
     * @return the Config Channel
     */
    @ManyToOne(targetEntity = ConfigChannel.class)
    @JoinColumn(name = "confchan_id")
    public ConfigChannel getConfigChannel() {
        return configChannel;
    }

    /**
     * Sets the Config Channel
     *
     * @param configChannelIn the Config Channel
     */
    public void setConfigChannel(ConfigChannel configChannelIn) {
        this.configChannel = configChannelIn;
    }
}
