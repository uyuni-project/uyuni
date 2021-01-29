/**
 * Copyright (c) 2021 SUSE LLC
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

package com.redhat.rhn.domain.action.errata;

import com.redhat.rhn.domain.action.Action;
import com.redhat.rhn.domain.action.ActionChild;

public class ActionPackageDetails extends ActionChild {
     private Long id;
     private boolean allowVendorChange = false;

    /**
     * Constructor
     */
    public ActionPackageDetails() {  }

    /**
     * Constructor
     * @param parentActionIn Action
     */
     public ActionPackageDetails(Action parentActionIn) {
            super.setParentAction(parentActionIn);
     }
    /**
     * Constructor
     * @param parentActionIn Action
     * @param allowVendorChangeIn boolean
     */
     public ActionPackageDetails(Action parentActionIn, boolean allowVendorChangeIn) {
         super.setParentAction(parentActionIn);
         this.allowVendorChange = allowVendorChangeIn;
     }

    /**
     * Return the ID.
     * @return id
     */
    public Long getId() {
        return id;
    }

    /**
     * Set the ID.
     * @param idIn id
     */
    public void setId(Long idIn) {
        this.id = idIn;
    }


    /**
     * @return Returns allow vendor change
     */
    public boolean getAllowVendorChange() {
        return allowVendorChange;
    }

    /**
     * @param allowVendorChangeIn Set allow vendor change flag.
     */
    public void setAllowVendorChange(boolean allowVendorChangeIn) {
        this.allowVendorChange = allowVendorChangeIn;
    }
}
