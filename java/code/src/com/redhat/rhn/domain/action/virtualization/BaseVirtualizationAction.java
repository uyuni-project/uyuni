/**
 * Copyright (c) 2009--2010 Red Hat, Inc.
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
package com.redhat.rhn.domain.action.virtualization;

import com.redhat.rhn.domain.action.Action;

import java.util.Map;

/**
 * BaseVirtualizationAction - Base class representing virtualization actions
 * @version $Rev$
 */
public abstract class BaseVirtualizationAction extends Action {

    private String uuid;

    /**
     * Getter for uuid
     * @return String to get
     */
    public String getUuid() {
        return this.uuid;
    }

    /**
     * Setter for uuid
     * @param stringIn String to set uuid to
     */
    public void setUuid(String stringIn) {
        this.uuid = stringIn;
    }

    /**
     * Extract any required parameters from the provided context and call the
     * appropriate setters.
     *
     * @param context Map of strings
     */
    public void extractParameters(Map context) {
        // Most virtualization actions require no parameters, default implementation
        // therefore does nothing.
    }

}
