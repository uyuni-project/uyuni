/*
 * Copyright (c) 2025 SUSE LLC
 *
 * This software is licensed to you under the GNU General Public License,
 * version 2 (GPLv2). There is NO WARRANTY for this software, express or
 * implied, including the implied warranties of MERCHANTABILITY or FITNESS
 * FOR A PARTICULAR PURPOSE. You should have received a copy of GPLv2
 * along with this software; if not, see
 * http://www.gnu.org/licenses/old-licenses/gpl-2.0.txt.
 */

package com.redhat.rhn.domain.action.supportdata;

import com.redhat.rhn.domain.action.Action;

import org.apache.commons.lang3.builder.EqualsBuilder;
import org.apache.commons.lang3.builder.HashCodeBuilder;

/**
 * SupportDataAction - Class representing TYPE_SUPPORTDATA_GET
 */
public class SupportDataAction extends Action {

    private SupportDataActionDetails details;

    public SupportDataActionDetails getDetails() {
        return details;
    }

    /**
     * Sets the details for this SupportDataAction.
     *
     * @param detailsIn the Set of SupportDataActionDetails to be set
     */
    public void setDetails(SupportDataActionDetails detailsIn) {
        details = detailsIn;
    }

    @Override
    public boolean equals(Object oIn) {
        if (this == oIn) {
            return true;
        }
        if (!(oIn instanceof SupportDataAction that)) {
            return false;
        }
        return new EqualsBuilder().appendSuper(super.equals(oIn)).append(details, that.details).isEquals();
    }

    @Override
    public int hashCode() {
        return new HashCodeBuilder(17, 37)
                .appendSuper(super.hashCode())
                .append(details)
                .toHashCode();
    }
}
