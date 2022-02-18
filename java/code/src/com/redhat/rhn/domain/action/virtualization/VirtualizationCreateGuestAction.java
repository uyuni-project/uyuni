/*
 * Copyright (c) 2018 SUSE LLC
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

import com.suse.manager.virtualization.GuestCreateDetails;

import java.util.Objects;

/**
 * CreateAction - Class representing TYPE_VIRTUALIZATION_CREATE
 */
public class VirtualizationCreateGuestAction extends BaseVirtualizationGuestAction {

    private static final long serialVersionUID = 5911199267745279497L;

    private GuestCreateDetails details;

    /**
     * @return value of details
     */
    public GuestCreateDetails getDetails() {
        return details;
    }

    /**
     * @return the String representation of the details
     *
     * This function should only be used by hibernate.
     */
    public String getDetailsAsString() {
        return details != null ? details.toJson() : null;
    }

    /**
     * @param detailsIn value of details
     */
    public void setDetails(GuestCreateDetails detailsIn) {
        details = detailsIn;
    }

    /**
     * Set the details from its serialized value
     *
     * @param json the string representation of the details
     *
     * This function should only be used by hibernate.
     */
    public void setDetailsAsString(String json) {
        details = json != null ? GuestCreateDetails.parse(json) : null;
    }

    @Override
    public boolean equals(Object other) {
        boolean result = false;
        if (other instanceof VirtualizationCreateGuestAction) {
            VirtualizationCreateGuestAction otherAction = (VirtualizationCreateGuestAction)other;
            result = Objects.equals(getDetailsAsString(), otherAction.getDetailsAsString());
        }
        return result;
    }

    @Override
    public int hashCode() {
        return Objects.hash(getDetailsAsString());
    }
}
