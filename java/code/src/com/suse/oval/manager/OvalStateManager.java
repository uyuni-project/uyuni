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
