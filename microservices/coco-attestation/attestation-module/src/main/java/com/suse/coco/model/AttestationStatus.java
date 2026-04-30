/*
 * Copyright (c) 2024 SUSE LLC
 *
 * This software is licensed to you under the GNU General Public License,
 * version 2 (GPLv2). There is NO WARRANTY for this software, express or
 * implied, including the implied warranties of MERCHANTABILITY or FITNESS
 * FOR A PARTICULAR PURPOSE. You should have received a copy of GPLv2
 * along with this software; if not, see
 * http://www.gnu.org/licenses/old-licenses/gpl-2.0.txt.
 */

package com.suse.coco.model;

import java.util.List;

public enum AttestationStatus {
    PENDING,
    SUCCEEDED,
    FAILED,
    REQUESTED,
    QUEUED,
    SUBMITTED;

    /**
     * Gets a list of status to be listened to
     *
     * @return the status list
     */
    public static List<AttestationStatus> statusToListenList() {
        return List.of(REQUESTED, PENDING);
    }

    /**
     * Checks if status is REQUESTED
     *
     * @return true if status is REQUESTED
     */
    public boolean isProcessingAttestationRequest() {
        return (this == REQUESTED);
    }

    /**
     * Checks if status is PENDING
     *
     * @return true if status is PENDING
     */
    public boolean isProcessingAttestationVerification() {
        return (this == PENDING);
    }

    /**
     * Gets next status, depending on success and current status
     *
     * @param success true if current operation succeeded
     * @return next status
     */
    public AttestationStatus getProcessingResultStatus(boolean success) {
        if (success && isProcessingAttestationRequest()) {
            return AttestationStatus.QUEUED;
        }
        if (success && isProcessingAttestationVerification()) {
            return AttestationStatus.SUCCEEDED;
        }

        return AttestationStatus.FAILED;
    }

}
