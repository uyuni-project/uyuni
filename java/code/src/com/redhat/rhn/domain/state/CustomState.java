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
package com.redhat.rhn.domain.state;

import com.redhat.rhn.domain.org.Org;

/**
 * A Salt sls file from the state catalog of an organization.
 */
public class CustomState {

    private Long id;
    private Org org;
    private String stateName;
    private boolean deleted;

    /**
     * The id of state.
     * @return an id.
     */
    public Long getId() {
        return id;
    }

    /**
     * Set the id
     * @param idIn the id
     */
    public void setId(Long idIn) {
        this.id = idIn;
    }

    /**
     * Get the organization to which this state belongs.
     * @return the organization
     */
    public Org getOrg() {
        return org;
    }

    /**
     * Set the organization to which this state belongs.
     * @param orgIn the organization
     */
    public void setOrg(Org orgIn) {
        this.org = orgIn;
    }

    /**
     * Get the name of the state. It's the same as the
     * name of the .sls file.
     * @return the name
     */
    public String getStateName() {
        return stateName;
    }

    /**
     * Set the name of the state.
     * @param stateNameIn the name
     */
    public void setStateName(String stateNameIn) {
        this.stateName = stateNameIn;
    }

    /**
     * @return true if this state was deleted
     */
    public boolean isDeleted() {
        return deleted;
    }

    /**
     * Mark custom state as deleted.
     * @param deletedIn true if deleted
     */
    public void setDeleted(boolean deletedIn) {
        this.deleted = deletedIn;
    }
}
