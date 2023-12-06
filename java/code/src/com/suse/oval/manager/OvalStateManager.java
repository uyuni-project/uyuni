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

package com.suse.oval.manager;

import com.suse.oval.ovaltypes.StateType;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * A cache for {@link StateType} to access OVAL states quickly
 */
public class OvalStateManager {
    private final Map<String, StateType> statesMap = new HashMap<>();

    /**
     * Standard constructor
     *
     * @param states the states to store and lookup later
     * */
    public OvalStateManager(List<StateType> states) {
        for (StateType state : states) {
            statesMap.put(state.getId(), state);
        }
    }

    /**
     * Looks up an OVAL state with an id of {@code stateId} or throws an exception if none is found.
     *
     * @param stateId the id of state to lookup
     * @return the state
     * */
    public StateType get(String stateId) {
        StateType state = statesMap.get(stateId);
        if (state == null) {
            throw new IllegalArgumentException("The state id is invalid: " + stateId);
        }
        return state;
    }

    /**
     * Check if an OVAL state with an id of {@code stateId} exists
     *
     * @param stateId the state id to check if exists
     * @return whether a state with {@code stateId} exist or not
     * */
    protected boolean exists(String stateId) {
        return statesMap.containsKey(stateId);
    }
}
