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

    public OvalStateManager(List<StateType> states) {
        for (StateType state : states) {
            statesMap.put(state.getId(), state);
        }
    }

    public StateType get(String stateId) {
        StateType state = statesMap.get(stateId);
        if (state == null) {
            throw new IllegalArgumentException("The state id is invalid: " + stateId);
        }
        return state;
    }

    public boolean exists(String stateId) {
        return statesMap.containsKey(stateId);
    }

    public void add(StateType state) {
        statesMap.put(state.getId(), state);
    }
}
