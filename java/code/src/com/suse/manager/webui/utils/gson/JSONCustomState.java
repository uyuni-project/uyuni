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
package com.suse.manager.webui.utils.gson;

/**
 * JSON representation of a custom Salt state.
 */
public class JSONCustomState {

    private String name;

    private boolean assigned;

    /**
     * @param nameIn the name of the state
     * @param assignedIn if it's assigned or not
     */
    public JSONCustomState(String nameIn, boolean assignedIn) {
        this.name = nameIn;
        this.assigned = assignedIn;
    }

    /**
     * @return the name of the custom state
     */
    public String getName() {
        return name;
    }

    /**
     * @param nameIn the name of the custom state
     */
    public void setName(String nameIn) {
        this.name = nameIn;
    }

    /**
     * @return true if the state is assigned
     */
    public boolean isAssigned() {
        return assigned;
    }

    /**
     * @param assignedIn true if state is assigned
     */
    public void setAssigned(boolean assignedIn) {
        this.assigned = assignedIn;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }

        JSONCustomState that = (JSONCustomState) o;

        return name != null ? name.equals(that.name) : that.name == null;

    }

    @Override
    public int hashCode() {
        return name != null ? name.hashCode() : 0;
    }
}
