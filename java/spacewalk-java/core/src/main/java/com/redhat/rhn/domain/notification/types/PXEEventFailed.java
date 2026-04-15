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

package com.redhat.rhn.domain.notification.types;

import com.redhat.rhn.common.localization.LocalizationService;

public class PXEEventFailed implements NotificationData {

    private final String minionId;
    private final String details;

    /**
     * Constructor
     * @param minionIdIn Minion ID of failing minion
     * @param detailsIn Details about the failure
     */
    public PXEEventFailed(String minionIdIn, String detailsIn) {
        this.minionId = minionIdIn;
        this.details = detailsIn;
    }

    @Override
    public String getSummary() {
        return LocalizationService.getInstance().getMessage("notification.pxeeventfailed", minionId);
    }

    @Override
    public String getDetails() {
        return details;
    }
}
