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
import java.util.List;
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

    @Column(name = "created")
    private Date created;

    @Column(name = "prereq_status")
    @Enumerated(EnumType.STRING)
    private ProgressStatus prerequisiteStatus;

    @Column(name = "prereq_at")
    private Date prerequisiteAt;

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
     * @param transactionalApply true when the entries represent an apply-then-complete flow
     * @return transactional progress entries in execution order
     */
    public List<ProgressEntry> getProgressEntries(boolean transactionalApply) {
        return List.of(
                new ProgressEntry(transactionalApply ? ProgressStep.TRANSACTIONAL_APPLY :
                        ProgressStep.PREREQUISITES, prerequisiteStatus, prerequisiteAt),
                new ProgressEntry(transactionalApply ? ProgressStep.TRANSACTIONAL_REBOOT :
                        ProgressStep.REBOOT, rebootStatus, rebootAt),
                new ProgressEntry(transactionalApply ? ProgressStep.ACTION_FINALIZATION :
                        ProgressStep.ACTION_EXECUTION, afterRebootStatus, afterRebootStatusAt)
        );
    }

    /**
     * @return when the action started waiting for reboot, or null if reboot is not pending
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
     * @param bootTimeMillis minion boot time in milliseconds
     * @return true if the observed boot happened after the action started waiting
     */
    public boolean canContinueAfter(long bootTimeMillis) {
        Date pendingSince = getRebootPendingSince();
        return pendingSince != null && bootTimeMillis > pendingSince.getTime();
    }

    /**
     * Record that prerequisites were applied.
     *
     * @param rebootRequiredIn whether a reboot is required before continuing
     */
    public void recordPrerequisitesApplied(boolean rebootRequiredIn) {
        prerequisiteStatus = ProgressStatus.COMPLETED;
        prerequisiteAt = new Date();
        rebootRequired = rebootRequiredIn;
        rebootStatus = rebootRequiredIn ? ProgressStatus.PENDING : ProgressStatus.NOT_NEEDED;
        rebootAt = rebootRequiredIn ? null : prerequisiteAt;
        afterRebootStatus = ProgressStatus.PENDING;
        afterRebootStatusAt = null;
    }

    /**
     * Record that prerequisites failed.
     */
    public void recordPrerequisitesFailed() {
        Date now = new Date();
        prerequisiteStatus = ProgressStatus.FAILED;
        prerequisiteAt = now;
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
        if (isWaitingForReboot()) {
            rebootStatus = ProgressStatus.COMPLETED;
            rebootAt = now;
        }
        afterRebootStatus = ProgressStatus.SCHEDULED;
        afterRebootStatusAt = now;
    }

    /**
     * Record that the transactional apply step completed.
     *
     * @param rebootRequiredIn whether a reboot is required to complete the action
     */
    public void recordTransactionalApplyCompleted(boolean rebootRequiredIn) {
        prerequisiteStatus = ProgressStatus.COMPLETED;
        prerequisiteAt = new Date();
        rebootRequired = rebootRequiredIn;
        rebootStatus = rebootRequiredIn ? ProgressStatus.PENDING : ProgressStatus.NOT_NEEDED;
        rebootAt = rebootRequiredIn ? null : prerequisiteAt;
        afterRebootStatus = rebootRequiredIn ? ProgressStatus.PENDING : ProgressStatus.COMPLETED;
        afterRebootStatusAt = rebootRequiredIn ? null : prerequisiteAt;
    }

    /**
     * Record that a pending transactional apply action does not require a reboot anymore.
     *
     * @param completionTime when the pending transaction check completed
     */
    public void recordTransactionalApplyNoRebootNeeded(Date completionTime) {
        rebootRequired = false;
        rebootStatus = ProgressStatus.NOT_NEEDED;
        rebootAt = completionTime;
        afterRebootStatus = ProgressStatus.COMPLETED;
        afterRebootStatusAt = completionTime;
    }

    /**
     * Record that the transactional apply step failed.
     */
    public void recordTransactionalApplyFailed() {
        Date now = new Date();
        prerequisiteStatus = ProgressStatus.FAILED;
        prerequisiteAt = now;
        rebootRequired = false;
        rebootStatus = ProgressStatus.NOT_NEEDED;
        rebootAt = now;
        afterRebootStatus = ProgressStatus.NOT_NEEDED;
        afterRebootStatusAt = now;
    }

    /**
     * Record that a pending transactional apply action was completed after reboot.
     */
    public void recordTransactionalApplyFinalized() {
        Date now = new Date();
        if (isWaitingForReboot()) {
            rebootStatus = ProgressStatus.COMPLETED;
            rebootAt = now;
        }
        afterRebootStatus = ProgressStatus.COMPLETED;
        afterRebootStatusAt = now;
    }

    /**
     * Record that scheduling the after-reboot state failed.
     */
    public void recordAfterRebootFailed() {
        Date now = new Date();
        if (isWaitingForReboot()) {
            rebootStatus = ProgressStatus.COMPLETED;
            rebootAt = now;
        }
        afterRebootStatus = ProgressStatus.FAILED;
        afterRebootStatusAt = now;
    }

    /**
     * Transactional action progress status.
     */
    public enum ProgressStatus {
        COMPLETED("completed", true),
        FAILED("failed", true),
        NOT_NEEDED("notNeeded", false),
        PENDING("pending", false),
        SCHEDULED("scheduled", true);

        private final String key;
        private final boolean timestamped;

        ProgressStatus(String keyIn, boolean timestampedIn) {
            key = keyIn;
            timestamped = timestampedIn;
        }

        public String getKey() {
            return key;
        }

        public boolean isTimestamped() {
            return timestamped;
        }
    }

    /**
     * Transactional action progress step.
     */
    public enum ProgressStep {
        PREREQUISITES("prerequisites"),
        REBOOT("reboot"),
        ACTION_EXECUTION("execution"),
        TRANSACTIONAL_APPLY("apply"),
        TRANSACTIONAL_REBOOT("applyReboot"),
        ACTION_FINALIZATION("finalization");

        private final String key;

        ProgressStep(String keyIn) {
            key = keyIn;
        }

        public String getKey() {
            return key;
        }
    }

    /**
     * Transactional action progress entry.
     */
    public static class ProgressEntry {

        private final ProgressStep step;
        private final ProgressStatus status;
        private final Date date;

        /**
         * @param stepIn progress step
         * @param statusIn progress status
         * @param dateIn when the step reached the current status
         */
        public ProgressEntry(ProgressStep stepIn, ProgressStatus statusIn, Date dateIn) {
            step = stepIn;
            status = statusIn;
            date = dateIn;
        }

        public ProgressStep getStep() {
            return step;
        }

        public ProgressStatus getStatus() {
            return status;
        }

        public Date getDate() {
            return date;
        }

        public boolean isTimestamped() {
            return status.isTimestamped() && date != null;
        }
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
