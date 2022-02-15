/*
 * Copyright (c) 2016 SUSE LLC
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
package com.redhat.rhn.domain.action.salt;

import com.redhat.rhn.domain.action.ActionChild;

import com.suse.utils.Json;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * ApplyStatesActionDetails - Class representation of the table rhnActionApplyStates.
 */
public class ApplyStatesActionDetails extends ActionChild {

    private long id;
    private long actionId;
    private String states;
    private String pillars;
    private Set<ApplyStatesActionResult> results;
    private boolean test = false;

    /**
     * @return the id
     */
    public long getId() {
        return id;
    }

    /**
     * @param idIn the id to set
     */
    public void setId(long idIn) {
        this.id = idIn;
    }

    /**
     * @return the action id
     */
    public long getActionId() {
        return actionId;
    }

    /**
     * @param actionIdIn the action id to set
     */
    public void setActionId(long actionIdIn) {
        this.actionId = actionIdIn;
    }

    /**
     * Only for hibernate, please use getMods() instead.
     *
     * @return the states
     */
    public String getStates() {
        return states;
    }

    /**
     * Only for hibernate, please use setMods() instead.
     *
     * @param statesIn the states to set
     */
    public void setStates(String statesIn) {
        this.states = statesIn;
    }

    /**
     * Only for hibernate, please use getPillarsMap() instead.
     *
     * @return the pillars
     */
    public String getPillars() {
        return pillars;
    }

    /**
     * Only for hibernate, please use setPillarsMap() instead.
     *
     * @param pillarsIn the states to set
     */
    public void setPillars(String pillarsIn) {
        this.pillars = pillarsIn;
    }

    /**
     * Return the state modules as a list.
     *
     * @return state modules as list of strings
     */
    public List<String> getMods() {
        if (states != null) {
            return Arrays.asList(states.split(","));
        }
        return Collections.unmodifiableList(new ArrayList<>());
    }

    /**
     * Set the state modules given as a list.
     *
     * @param mods list of state modules
     */
    public void setMods(List<String> mods) {
        if (!mods.isEmpty()) {
            states = mods.stream().collect(Collectors.joining(","));
        }
        else {
            states = null;
        }
    }

    /**
     * Return the pillars as a Map.
     *
     * @return the pillars as a Optional Map
     */
    public Optional<Map<String, Object>> getPillarsMap() {
        if (pillars != null) {
            return Optional.of(Json.GSON.fromJson(pillars, Map.class));
        }
        return Optional.empty();
    }

    /**
     * Set the the pillars as a Map.
     *
     * @param op Optiona Map with pillars
     */
    public void setPillarsMap(Optional<Map<String, Object>> op) {
        op.ifPresentOrElse(
            p -> pillars = Json.GSON.toJson(p),
            () -> pillars = null);
    }

    /**
     * @return the results
     */
    public Set<ApplyStatesActionResult> getResults() {
        return results;
    }

    /**
     * @param resultsIn the results to set
     */
    public void setResults(Set<ApplyStatesActionResult> resultsIn) {
        this.results = resultsIn;
    }

    /**
     * Add {@link ApplyStatesActionResult} to the results set.
     *
     * @param resultIn ApplyStatesActionResult to add to the set
     */
    public void addResult(ApplyStatesActionResult resultIn) {
        if (results == null) {
            results = new HashSet<>();
        }
        resultIn.setParentScriptActionDetails(this);
        results.add(resultIn);
    }

    /**
     * Get the action result for a specific server.
     * @param serverId the server id.
     * @return an {@link Optional} wrapping the result of the given server or {@link Optional#empty()} if no there is
     * no result for it
     */
    public Optional<ApplyStatesActionResult> getResult(long serverId) {
        if (results == null || results.isEmpty()) {
            return Optional.empty();
        }

        return results.stream()
                      .filter(r -> r.getServerId() == serverId)
                      .findFirst();
    }

    /**
     * @return the value of test
     */
    public boolean isTest() {
        return test;
    }

    /**
     * @param testIn the value to set for test
     */
    public void setTest(boolean testIn) {
        test = testIn;
    }
}
