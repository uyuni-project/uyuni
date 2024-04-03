/*
 * Copyright (c) 2024 SUSE LLC
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

import java.util.Map;


public class ProductsPageMetadataJson {

    private final boolean issMaster;
    private final boolean refreshNeeded;
    private final boolean refreshRunning;
    private final boolean refreshFileLocked;
    private final boolean noToolsChannelSubscription;

    /**
     * Information needed for the products page
     * @param issMasterIn
     * @param refreshNeededIn
     * @param refreshRunningIn
     * @param refreshFileLockedIn
     * @param noToolsChannelSubscriptionIn
     */
    public ProductsPageMetadataJson(boolean issMasterIn, boolean refreshNeededIn, boolean refreshRunningIn,
                                    boolean refreshFileLockedIn, boolean noToolsChannelSubscriptionIn) {
        this.issMaster = issMasterIn;
        this.refreshNeeded = refreshNeededIn;
        this.refreshRunning = refreshRunningIn;
        this.refreshFileLocked = refreshFileLockedIn;
        this.noToolsChannelSubscription = noToolsChannelSubscriptionIn;
    }


    /**
     * @return map representation for use with jade
     */
    public Map<String, Boolean> toMap() {
        return Map.of(
            "issMaster", issMaster,
            "refreshNeeded", refreshNeeded,
            "refreshRunning", refreshRunning,
            "refreshFileLocked", refreshFileLocked,
            "noToolsChannelSubscription", noToolsChannelSubscription
        );
    }
}
