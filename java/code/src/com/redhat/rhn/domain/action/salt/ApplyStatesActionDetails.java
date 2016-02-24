/**
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

import java.util.Set;

/**
 * ApplyStatesActionDetails - Class representation of the table rhnActionApplyStates.
 */
public class ApplyStatesActionDetails extends ActionChild {

    private long id;
    private long actionId;
    private String states;
    private Set<ApplyStatesResult> results;

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
     * @return the states
     */
    public String getStates() {
        return states;
    }

    /**
     * @param statesIn the states to set
     */
    public void setStates(String statesIn) {
        this.states = statesIn;
    }

    /**
     * @return the results
     */
    public Set<ApplyStatesResult> getResults() {
        return results;
    }

    /**
     * @param resultsIn the results to set
     */
    public void setResults(Set<ApplyStatesResult> resultsIn) {
        this.results = resultsIn;
    }
}
