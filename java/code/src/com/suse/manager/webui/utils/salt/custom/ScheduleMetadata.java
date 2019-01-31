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
    public static final String BATCH_MODE = "batch-mode";

    @SerializedName(SUMA_ACTION_ID)
    private Long sumaActionId = 0L;

    @SerializedName(SUMA_FORCE_PGK_LIST_REFRESH)
    private final boolean forcePackageListRefresh;

    @SerializedName(SUMA_ACTION_CHAIN)
    private final boolean actionChain;

    @SerializedName(BATCH_MODE)
    private final boolean batchMode;

    /**
     * Constructor for ScheduleMetadata
     * @param sumaActionIdIn the Id of the action
     * @param forcePackageListRefreshIn whether the schedule action should force a package list refresh
     * @param actionChainIn whether the schedule action is corresponds to an action chain
     * @param batchModeIn whether the schedule action is executed in batch mode
     */
    public ScheduleMetadata(Long sumaActionIdIn, boolean forcePackageListRefreshIn, boolean actionChainIn,
            boolean batchModeIn) {
        super();
        this.sumaActionId = sumaActionIdIn;
        this.forcePackageListRefresh = forcePackageListRefreshIn;
        this.actionChain = actionChainIn;
        this.batchMode = batchModeIn;
    }

    /**
     * Constructor for ScheduleMetadata
     * @param forcePackageListRefreshIn whether the schedule action should force a package list refresh
     * @param actionChainIn whether the schedule action is corresponds to an action chain
     * @param batchModeIn whether the schedule action is executed in batch mode
     */
    public ScheduleMetadata(boolean forcePackageListRefreshIn, boolean actionChainIn,
            boolean batchModeIn) {
        super();
        this.forcePackageListRefresh = forcePackageListRefreshIn;
        this.actionChain = actionChainIn;
        this.batchMode = batchModeIn;
    }

    /**
     * Returns a new instance of ScheduleMetadata with its default values.
     * @return the new instance of ScheduleMetadata
     */
    public static ScheduleMetadata getDefaultMetadata() {
        return new ScheduleMetadata(false, false, false);
    }

    @SerializedName(SUMA_MINION_STARTUP)
    private boolean minionStartup;

    /**
     * Returns a new instance of ScheduleMetadata for actions to be executed in regular minions.
     * @param isStagingJob whether this action corresponds to a staging job
     * @param forcePackageListRefresh whether the schedule action should force a package list refresh
     * @param actionId the Id of the action
     * @return the new instance of ScheduleMetadata
     */
    public static ScheduleMetadata getMetadataForRegularMinionActions(boolean isStagingJob,
            boolean forcePackageListRefresh, long actionId) {
        if (!isStagingJob) {
            return new ScheduleMetadata(actionId, forcePackageListRefresh, false, false);
        }
        return new ScheduleMetadata(forcePackageListRefresh, false, false);
    }

    /**
     * Sets the BatchMode flag in true
     * @return an instance of ScheduleMetadata with batchMode flag set in true
     */
    public ScheduleMetadata withBatchMode() {
        return new ScheduleMetadata(sumaActionId, forcePackageListRefresh, actionChain, true);
    }

    /**
     * Sets the actionChain flag in true
     * @return an instance of ScheduleMetadata with the actionChain flag set in true
     */
    public ScheduleMetadata withActionChain() {
        return new ScheduleMetadata(sumaActionId, forcePackageListRefresh, true, batchMode);
    }

    /**
     * @return the action id
     */
    public Long getSumaActionId() {
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

    /**
     * @return true if it's a batch call
     */
    public boolean isBatchMode() {
        return batchMode;
    }
}
