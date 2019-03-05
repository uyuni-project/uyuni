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
package com.suse.manager.webui.utils.salt.custom;

import com.google.gson.annotations.SerializedName;

/**
 * Object representation of SUSE Manager metadata to be added to Salt jobs.
 */
public class ScheduleMetadata {

    public static final String SUMA_ACTION_ID = "suma-action-id";
    public static final String SUMA_FORCE_PGK_LIST_REFRESH = "suma-force-pkg-list-refresh";
    public static final String SUMA_ACTION_CHAIN = "suma-action-chain";
    public static final String SUMA_MINION_STARTUP = "suma-minion-startup";

    @SerializedName(SUMA_ACTION_ID)
    private long sumaActionId = 0L;

    @SerializedName(SUMA_FORCE_PGK_LIST_REFRESH)
    private boolean forcePackageListRefresh;

    @SerializedName(SUMA_ACTION_CHAIN)
    private boolean actionChain;

    @SerializedName(SUMA_MINION_STARTUP)
    private boolean minionStartup;

    /**
     * @param sumaActionIdIn the action id
     */
    public ScheduleMetadata(long sumaActionIdIn) {
        sumaActionId = sumaActionIdIn;
    }

    /**
     * @return the action id
     */
    public long getSumaActionId() {
        return sumaActionId;
    }

    /**
     * @return true if a package list refresh should be enforced afterwards
     */
    public boolean isForcePackageListRefresh() {
        return forcePackageListRefresh;
    }

    /**
     * @return actionChain to get
     */
    public boolean isActionChain() {
        return actionChain;
    }

    /**
     * @return true in case of minion startup
     */
    public boolean isMinionStartup() {
         return minionStartup;
    }
}
