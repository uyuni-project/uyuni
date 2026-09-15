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

package com.redhat.rhn.manager.recurringactions;

import com.redhat.rhn.domain.config.ConfigChannel;
import com.redhat.rhn.domain.recurringactions.RecurringActionFactory;
import com.redhat.rhn.domain.recurringactions.state.InternalState;
import com.redhat.rhn.domain.recurringactions.state.RecurringConfigChannel;
import com.redhat.rhn.domain.recurringactions.state.RecurringInternalState;
import com.redhat.rhn.domain.recurringactions.state.RecurringStateConfig;
import com.redhat.rhn.domain.user.User;
import com.redhat.rhn.manager.configuration.ConfigurationManager;

import java.util.Optional;

/**
 * Factory class that creates {@link RecurringStateConfig} instances
 */
public class StateConfigFactory {
    private final ConfigurationManager configManager;

    /**
     * Initialize a new instance with the default {@link ConfigurationManager}
     */
    public StateConfigFactory() {
        configManager = ConfigurationManager.getInstance();
    }

    /**
     * Initialize a new instance with a specified {@link ConfigurationManager}
     * @param configManagerIn the {@link ConfigurationManager} instance
     */
    public StateConfigFactory(ConfigurationManager configManagerIn) {
        this.configManager = configManagerIn;
    }

    /**
     * Get a {@link RecurringStateConfig} instance for an internal state in the specified position
     * @param state the internal state instance
     * @param position the position that the state will be applied in
     * @return the state config instance
     */
    public RecurringStateConfig getRecurringState(InternalState state, long position) {
        return new RecurringInternalState(state, position);
    }

    /**
     * Get a {@link RecurringStateConfig} instance for a config channel state in the specified position
     * @param configChannel the config channel instance
     * @param position the position that the state will be applied in
     * @return the state config instance
     */
    public RecurringStateConfig getRecurringState(ConfigChannel configChannel, long position) {
        return new RecurringConfigChannel(configChannel, position);
    }

    /**
     * Get a {@link RecurringStateConfig} instance for either a config channel or an internal state, depending on the
     * state name provided.
     * <p>The state name is looked up in multiple pools of custom states in the following order:
     * <ol>
     *     <li>Internal states</li>
     *     <li>Config channels</li>
     * </ol>
     * If none found, a {@link java.util.NoSuchElementException} is thrown.
     * @param user the user initiating the operation
     * @param stateName the name of the state. may be an internal state, or a config channel label
     * @param position the position that the state will be applied in
     * @return the state config instance
     * @throws java.util.NoSuchElementException if the state cannot be found
     */
    public RecurringStateConfig getRecurringState(User user, String stateName, long position) {
        //TODO: Prevent creating config channels with reserved internal state names?
        return RecurringActionFactory.lookupInternalStateByName(stateName)
                .map(state -> (RecurringStateConfig) new RecurringInternalState(state, position))
                .or(() -> Optional.ofNullable(configManager.lookupGlobalConfigChannel(user, stateName))
                        .map((channel) -> this.getRecurringState(channel, position)))
                .orElseThrow();
    }
}
