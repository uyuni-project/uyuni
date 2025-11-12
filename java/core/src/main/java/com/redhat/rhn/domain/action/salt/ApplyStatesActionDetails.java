/*
 * Copyright (c) 2016--2025 SUSE LLC
 *
 * This software is licensed to you under the GNU General Public License,
 * version 2 (GPLv2). There is NO WARRANTY for this software, express or
 * implied, including the implied warranties of MERCHANTABILITY or FITNESS
 * FOR A PARTICULAR PURPOSE. You should have received a copy of GPLv2
 * along with this software; if not, see
 * http://www.gnu.org/licenses/old-licenses/gpl-2.0.txt.
 */
package com.redhat.rhn.domain.action.salt;

import com.redhat.rhn.domain.BaseDomainHelper;
import com.redhat.rhn.domain.action.Action;

import com.suse.utils.Json;

import org.hibernate.annotations.Type;
import org.hibernate.type.YesNoConverter;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;

import jakarta.persistence.CascadeType;
import jakarta.persistence.Column;
import jakarta.persistence.Convert;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.OneToMany;
import jakarta.persistence.Table;

/**
 * ApplyStatesActionDetails - Class representation of the table rhnActionApplyStates.
 */
@Entity
@Table(name = "rhnActionApplyStates")
public class ApplyStatesActionDetails extends BaseDomainHelper {

    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "RHN_ACT_APPLY_STATES_ID_SEQ")
    @SequenceGenerator(name = "RHN_ACT_APPLY_STATES_ID_SEQ", sequenceName = "RHN_ACT_APPLY_STATES_ID_SEQ",
            allocationSize = 1)
    private long id;

    @Column
    private String states;

    @Column
    private String pillars;

    @OneToMany(mappedBy = "actionApplyStatesId", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    private Set<ApplyStatesActionResult> results;

    @Column
    @Convert(converter = YesNoConverter.class)
    private boolean test = false;

    @Column
    @Convert(converter = YesNoConverter.class)
    private boolean direct = false;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "action_id", updatable = false, nullable = false, insertable = true)
    private Action parentAction;

    /**
     * @return the id
     */
    public long getId() {
        return id;
    }

    /**
     * @param idIn the id to set
     */
    protected void setId(long idIn) {
        this.id = idIn;
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

    /**
     * @return is a direct Call or not
     */
    public boolean isDirect() {
        return direct;
    }

    /**
     * @param directIn set to direct call or not
     */
    public void setDirect(boolean directIn) {
        direct = directIn;
    }

    /**
     * Gets the parent Action associated with this ServerAction record
     * @return Returns the parentAction.
     */
    public Action getParentAction() {
        return parentAction;
    }

    /**
     * Sets the parent Action associated with this ServerAction record
     * @param parentActionIn The parentAction to set.
     */
    public void setParentAction(Action parentActionIn) {
        this.parentAction = parentActionIn;
    }
}
