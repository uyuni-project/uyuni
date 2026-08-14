/*
 * Copyright (c) 2026 SUSE LLC
 *
 * This software is licensed to you under the GNU General Public License,
 * version 2 (GPLv2). There is NO WARRANTY for this software, express or
 * implied, including the implied warranties of MERCHANTABILITY or FITNESS
 * FOR A PARTICULAR PURPOSE. You should have received a copy of GPLv2
 * along with this software; if not, see
 * http://www.gnu.org/licenses/old-licenses/gpl-2.0.txt.
 */
package com.redhat.rhn.domain.server;

import java.io.Serializable;
import java.util.Date;
import java.util.Objects;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.Id;
import jakarta.persistence.IdClass;
import jakarta.persistence.Table;

/**
 * Transactional action progress for a minion.
 */
@Entity
@Table(name = "suseTransactionalActionHistory")
@IdClass(MinionTransactionalActionHistoryId.class)
public class MinionTransactionalActionHistory implements Serializable {

    @Id
    @Column(name = "minion_server_id")
    private Long minionServerId;

    @Id
    @Column(name = "action_id")
    private Long actionId;

    @Column(name = "snapshot_refresh_action_id")
    private Long snapshotRefreshActionId;

    @Column(name = "created")
    private Date created;

    @Column(name = "prereq_status")
    @Enumerated(EnumType.STRING)
    private ProgressStatus prerequisiteStatus;

    @Column(name = "prereq_at")
    private Date prerequisiteAt;

    @Column(name = "prereq_result")
    private String prerequisiteResult;

    @Column(name = "reboot_required")
    private boolean rebootRequired;

    @Column(name = "reboot_status")
    @Enumerated(EnumType.STRING)
    private ProgressStatus rebootStatus;

    @Column(name = "reboot_at")
    private Date rebootAt;

    @Column(name = "after_reboot_status")
    @Enumerated(EnumType.STRING)
    private ProgressStatus afterRebootStatus;

    @Column(name = "after_reboot_status_at")
    private Date afterRebootStatusAt;

    /**
     * Default constructor required by Hibernate.
     */
    public MinionTransactionalActionHistory() {
    }

    /**
     * @param minionServerIdIn minion server id
     * @param actionIdIn action being tracked
     */
    public MinionTransactionalActionHistory(Long minionServerIdIn, Long actionIdIn) {
        minionServerId = minionServerIdIn;
        actionId = actionIdIn;
        created = new Date();
        prerequisiteStatus = ProgressStatus.PENDING;
        rebootStatus = ProgressStatus.PENDING;
        afterRebootStatus = ProgressStatus.PENDING;
    }

    /**
     * Create a new initialized transactional action history entry.
     *
     * @param minionServerIdIn minion server id
     * @param actionIdIn action being tracked
     * @return initialized history entry
     */
    public static MinionTransactionalActionHistory create(Long minionServerIdIn, Long actionIdIn) {
        return new MinionTransactionalActionHistory(minionServerIdIn, actionIdIn);
    }

    /**
     * @return minion server id
     */
    public Long getMinionServerId() {
        return minionServerId;
    }

    /**
     * @return tracked action id
     */
    public Long getActionId() {
        return actionId;
    }

    /**
     * @return snapshot refresh action scheduled to reconcile this transactional action
     */
    public Long getSnapshotRefreshActionId() {
        return snapshotRefreshActionId;
    }

    /**
     * @return when tracking started
     */
    public Date getCreated() {
        return created;
    }

    /**
     * @return prerequisite step status
     */
    public ProgressStatus getPrerequisiteStatus() {
        return prerequisiteStatus;
    }

    /**
     * @return when the prerequisite step reached the current status
     */
    public Date getPrerequisiteAt() {
        return prerequisiteAt;
    }

    /**
     * @return Salt result of the prerequisite state, when this action has one
     */
    public String getPrerequisiteResult() {
        return prerequisiteResult;
    }

    /**
     * @return whether this action requires a reboot before continuing
     */
    public boolean isRebootRequired() {
        return rebootRequired;
    }

    /**
     * @return reboot step status
     */
    public ProgressStatus getRebootStatus() {
        return rebootStatus;
    }

    /**
     * @return when the reboot step reached the current status
     */
    public Date getRebootAt() {
        return rebootAt;
    }

    /**
     * @return after-reboot step status
     */
    public ProgressStatus getAfterRebootStatus() {
        return afterRebootStatus;
    }

    /**
     * @return when the after-reboot step reached the current status
     */
    public Date getAfterRebootStatusAt() {
        return afterRebootStatusAt;
    }

    /**
     * @return when the transactional state finished before a pending reboot, or null if reboot is not pending
     */
    public Date getRebootPendingSince() {
        return ProgressStatus.PENDING.equals(rebootStatus) && rebootRequired ? prerequisiteAt : null;
    }

    /**
     * @return true when this action waits for a reboot before it can continue
     */
    public boolean isWaitingForReboot() {
        return rebootRequired &&
                ProgressStatus.PENDING.equals(rebootStatus) &&
                ProgressStatus.PENDING.equals(afterRebootStatus);
    }

    /**
     * Record that the transactional state was applied successfully.
     */
    public void recordTransactionalStateApplied() {
        recordTransactionalStateApplied(null);
    }

    /**
     * Record that the transactional state was applied successfully.
     *
     * @param prerequisiteResultIn Salt result to keep for prerequisite actions
     */
    public void recordTransactionalStateApplied(String prerequisiteResultIn) {
        prerequisiteStatus = ProgressStatus.COMPLETED;
        prerequisiteAt = new Date();
        prerequisiteResult = prerequisiteResultIn;
        rebootRequired = false;
        rebootStatus = ProgressStatus.PENDING;
        rebootAt = null;
        afterRebootStatus = ProgressStatus.PENDING;
        afterRebootStatusAt = null;
    }

    /**
     * Record the snapshot refresh action scheduled to reconcile this transactional action.
     *
     * @param snapshotRefreshActionIdIn snapshot refresh action id
     */
    public void recordSnapshotRefreshAction(Long snapshotRefreshActionIdIn) {
        snapshotRefreshActionId = snapshotRefreshActionIdIn;
    }

    /**
     * Record the action-specific reboot state determined from refreshed snapshot information.
     *
     * @param rebootRequiredIn whether this action needs a reboot
     * @param hasAfterRebootState whether this action has an after-reboot state to execute
     */
    public void recordSnapshotReconciliation(boolean rebootRequiredIn, boolean hasAfterRebootState) {
        Date now = new Date();
        rebootRequired = rebootRequiredIn;
        rebootStatus = rebootRequiredIn ? ProgressStatus.PENDING : ProgressStatus.NOT_NEEDED;
        rebootAt = null;
        afterRebootStatus = rebootRequiredIn || hasAfterRebootState ? ProgressStatus.PENDING : ProgressStatus.COMPLETED;
        afterRebootStatusAt = rebootRequiredIn || hasAfterRebootState ? null : now;
    }

    /**
     * Record snapshot reconciliation after a failed transactional apply.
     *
     * @param rebootRequiredIn whether this action needs a reboot
     * @param hasAfterRebootState whether this action has an after-reboot state to execute
     */
    public void recordFailedSnapshotReconciliation(boolean rebootRequiredIn, boolean hasAfterRebootState) {
        Date now = new Date();
        rebootRequired = rebootRequiredIn;
        rebootStatus = rebootRequiredIn ? ProgressStatus.PENDING : ProgressStatus.NOT_NEEDED;
        rebootAt = rebootRequiredIn ? null : now;
        afterRebootStatus = hasAfterRebootState ? ProgressStatus.FAILED : ProgressStatus.NOT_NEEDED;
        afterRebootStatusAt = now;
    }

    /**
     * Record that snapshot refresh reconciliation could not be scheduled.
     */
    public void recordSnapshotRefreshFailed() {
        Date now = new Date();
        rebootRequired = false;
        rebootStatus = ProgressStatus.FAILED;
        rebootAt = now;
        afterRebootStatus = ProgressStatus.FAILED;
        afterRebootStatusAt = now;
    }

    /**
     * Record that applying the transactional state failed.
     */
    public void recordTransactionalApplyFailed() {
        recordTransactionalApplyFailed(null);
    }

    /**
     * Record that applying the transactional state failed.
     *
     * @param prerequisiteResultIn Salt result to keep for prerequisite actions
     */
    public void recordTransactionalApplyFailed(String prerequisiteResultIn) {
        Date now = new Date();
        prerequisiteStatus = ProgressStatus.FAILED;
        prerequisiteAt = now;
        prerequisiteResult = prerequisiteResultIn;
        rebootRequired = false;
        rebootStatus = ProgressStatus.NOT_NEEDED;
        rebootAt = now;
        afterRebootStatus = ProgressStatus.NOT_NEEDED;
        afterRebootStatusAt = now;
    }

    /**
     * Record that the after-reboot state was scheduled.
     */
    public void recordAfterRebootScheduled() {
        Date now = new Date();
        completePendingReboot(now);
        afterRebootStatus = ProgressStatus.SCHEDULED;
        afterRebootStatusAt = now;
    }

    /**
     * Record that a pending transactional apply action was completed after reboot.
     */
    public void recordTransactionalApplyFinalized() {
        Date now = new Date();
        completePendingReboot(now);
        afterRebootStatus = ProgressStatus.COMPLETED;
        afterRebootStatusAt = now;
    }

    /**
     * Record that scheduling the after-reboot state failed.
     */
    public void recordAfterRebootFailed() {
        Date now = new Date();
        completePendingReboot(now);
        afterRebootStatus = ProgressStatus.FAILED;
        afterRebootStatusAt = now;
    }

    private void completePendingReboot(Date now) {
        if (isWaitingForReboot()) {
            rebootStatus = ProgressStatus.COMPLETED;
            rebootAt = now;
        }
    }

    /**
     * Transactional action progress status.
     */
    public enum ProgressStatus {
        COMPLETED,
        FAILED,
        NOT_NEEDED,
        PENDING,
        SCHEDULED
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (!(o instanceof MinionTransactionalActionHistory that)) {
            return false;
        }
        return Objects.equals(minionServerId, that.minionServerId) &&
                Objects.equals(actionId, that.actionId);
    }

    @Override
    public int hashCode() {
        return Objects.hash(minionServerId, actionId);
    }
}
