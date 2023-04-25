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
 * Red Hat trademarks are not licensed under GPLv2. No permission is
 * granted to use or replicate Red Hat trademarks that are incorporated
 * in this software or its documentation.
 */
package com.suse.manager.webui.utils.gson;

import com.redhat.rhn.common.localization.LocalizationService;
import com.redhat.rhn.domain.config.ConfigChannel;
import com.redhat.rhn.domain.recurringactions.state.InternalState;
import com.redhat.rhn.domain.recurringactions.state.RecurringConfigChannel;
import com.redhat.rhn.domain.recurringactions.state.RecurringInternalState;
import com.redhat.rhn.domain.recurringactions.state.RecurringStateConfig;

import java.util.Set;
import java.util.stream.Collectors;

/**
 * JSON representation of a state configuration.
 */
public class StateConfigJson extends ConfigChannelJson {

    /**
     * Instantiates a new unassigned state object from a config channel
     *
     * @param channelIn the channel
     */
    public StateConfigJson(ConfigChannel channelIn) {
        super();
        this.setId(channelIn.getId());
        this.setName(channelIn.getName());
        this.setLabel(channelIn.getLabel());
        this.setDescription(channelIn.getDescription());
        this.setType(channelIn.getConfigChannelType().getLabel());
        this.setPosition(null);
        this.setAssigned(false);
    }

    /**
     * Instantiates a new state object assigned in a specific position from a config channel
     *
     * @param channelIn the channel
     * @param positionIn the ordering of the channel
     */
    public StateConfigJson(ConfigChannel channelIn, int positionIn) {
        super(channelIn, positionIn);
    }

    /**
     * Instantiates a new unassigned state object from internal state
     *
     * @param stateIn the internal state
     */
    public StateConfigJson(InternalState stateIn) {
        this.setId(stateIn.getId());
        this.setName(stateIn.getName());
        this.setLabel(stateIn.getLabel());
        this.setDescription(getDescriptionString(stateIn.getName()));
        this.setType("internal_state");
        this.setPosition(null);
        this.setAssigned(false);
    }

    /**
     * Instantiates a new state object assigned in a specific position from an internal state
     *
     * @param stateIn the internal state
     * @param positionIn the ordering of the channel
     */
    public StateConfigJson(InternalState stateIn, int positionIn) {
        this(stateIn);
        this.setPosition(positionIn);
        this.setAssigned(true);
    }

    /**
     * Creates a set of {@link StateConfigJson} objects from a set of {@link RecurringStateConfig}
     * @param configIn set of states to be included in the resulting set
     * @return the set of {@link StateConfigJson} objects
     */
    public static Set<StateConfigJson> listOrderedStates(Set<RecurringStateConfig> configIn) {
        return configIn.stream().map(config -> {
            if (config instanceof RecurringInternalState) {
                return new StateConfigJson(
                        ((RecurringInternalState) config).getInternalState(), config.getPosition().intValue());
            }
            else {
                return new StateConfigJson(
                        ((RecurringConfigChannel) config).getConfigChannel(), config.getPosition().intValue());
            }
        }).collect(Collectors.toSet());
    }

    private static String getDescriptionString(String nameIn) {
        String name = nameIn.replace(".", "_");
        return LocalizationService.getInstance().getMessage("internal_state." + name);
    }
}
