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
