/*
 * Copyright (c) 2025 SUSE LLC
 * Copyright (c) 2009--2010 Red Hat, Inc.
 *
 * This software is licensed to you under the GNU General Public License,
 * version 2 (GPLv2). There is NO WARRANTY for this software, express or
 * implied, including the implied warranties of MERCHANTABILITY or FITNESS
 * FOR A PARTICULAR PURPOSE. You should have received a copy of GPLv2
 * along with this software; if not, see
 * http://www.gnu.org/licenses/old-licenses/gpl-2.0.txt.
 *
 * SPDX-License-Identifier: GPL-2.0-only
 *
 * Red Hat trademarks are not licensed under GPLv2. No permission is
 * granted to use or replicate Red Hat trademarks that are incorporated
 * in this software or its documentation.
 */
package com.redhat.rhn.domain.action.kickstart;

import com.redhat.rhn.domain.action.Action;

import javax.persistence.CascadeType;
import javax.persistence.DiscriminatorValue;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.OneToOne;


/**
 * KickstartGuestAction
 */
@Entity
@DiscriminatorValue("-4")
public class KickstartGuestAction extends Action {

    @OneToOne(mappedBy = "parentAction", fetch = FetchType.LAZY, cascade = CascadeType.ALL)
    private KickstartGuestActionDetails kickstartGuestActionDetails;

    /**
     * Get the detail record associated with this KickstartGuestAction
     *
     * @return Returns the kickstartGuestActionDetails.
     */
    public KickstartGuestActionDetails getKickstartGuestActionDetails() {
        return kickstartGuestActionDetails;
    }

    /**
     * Set the detail record associated with this KickstartGuestAction
     * @param kickstartGuestActionDetailsIn The kickstartGuestActionDetails to set.
     */
    public void setKickstartGuestActionDetails(
            KickstartGuestActionDetails kickstartGuestActionDetailsIn) {
        this.kickstartGuestActionDetails = kickstartGuestActionDetailsIn;
    }
}
