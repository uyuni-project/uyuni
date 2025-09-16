/*
 * Copyright (c) 2017 SUSE LLC
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

package com.redhat.rhn.manager.configuration;

import com.redhat.rhn.domain.Identifiable;
import com.redhat.rhn.domain.config.ConfigChannel;
import com.redhat.rhn.domain.user.User;

import java.util.List;

/**
 * Interface implemented by entities which can be subscribed to config channels using Salt
 */
public interface SaltConfigurable extends Identifiable {

    /**
     * Subscribes the configurable to the specified config channels.
     * @param channels channels to be subscribed
     * @param user user performing the action
     */
    void subscribeConfigChannels(List<ConfigChannel> channels, User user);

    /**
     * Unsubscribes the configurable from the specified config channels.
     * @param channels channels to be subscribed
     * @param user user performing the action
     */
    void unsubscribeConfigChannels(List<ConfigChannel> channels, User user);

    /**
     * Sets the config channels subscribed for the configurable.
     * @param channels channels to be subscribed
     * @param user user performing the action
     */
    void setConfigChannels(List<ConfigChannel> channels, User user);

    /**
     * Gets the display name of the configurable
     * @return the name
     */
    String getName();
}
