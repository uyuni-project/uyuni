/**
 * Copyright (c) 2016 SUSE
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

import com.redhat.rhn.domain.action.Action;

/**
 * SaltJobAction - Action representation of a job that is executed by salt.
 */
public class SaltJobAction extends Action {

    private SaltJobActionDetails details;

    /**
     * Return the details.
     * @return details
     */
    public SaltJobActionDetails getDetails() {
        return details;
    }

    /**
     * Set the details.
     * @param detailsIn details
     */
    public void setDetails(SaltJobActionDetails detailsIn) {
        detailsIn.setParentAction(this);
        this.details = detailsIn;
    }
}
