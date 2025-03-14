/*
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
package com.redhat.rhn.domain.action.ansible;

import com.redhat.rhn.domain.action.Action;

import org.apache.commons.lang3.builder.EqualsBuilder;
import org.apache.commons.lang3.builder.HashCodeBuilder;

/**
 * PlaybookAction - Action class representing the execution of an Ansible playbook
 */
public class PlaybookAction extends Action {

    private PlaybookActionDetails details;

    /**
     * Return the details.
     * @return details
     */
    public PlaybookActionDetails getDetails() {
        return details;
    }

    /**
     * Set the details.
     * @param detailsIn details
     */
    public void setDetails(PlaybookActionDetails detailsIn) {
        if (detailsIn != null) {
            detailsIn.setParentAction(this);
        }
        this.details = detailsIn;
    }

    @Override
    public boolean equals(Object oIn) {
        if (this == oIn) {
            return true;
        }
        if (!(oIn instanceof PlaybookAction that)) {
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
