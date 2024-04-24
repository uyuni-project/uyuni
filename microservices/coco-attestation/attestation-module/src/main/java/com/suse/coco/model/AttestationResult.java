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

package com.suse.coco.model;

import java.time.OffsetDateTime;
import java.util.Objects;
import java.util.StringJoiner;

/**
 * An attestation result that needs to be verified by an {@link com.suse.coco.module.AttestationModule}.
 */
public class AttestationResult {
    private long id;
    private long reportId;
    private int resultType;
    private AttestationStatus status;
    private String description;
    private String details;
    private String processOutput;
    private OffsetDateTime attested;

    public long getId() {
        return id;
    }

    public void setId(long idIn) {
        this.id = idIn;
    }

    public long getReportId() {
        return reportId;
    }

    public void setReportId(long reportIdIn) {
        this.reportId = reportIdIn;
    }

    public int getResultType() {
        return resultType;
    }

    public void setResultType(int resultTypeIn) {
        this.resultType = resultTypeIn;
    }

    public AttestationStatus getStatus() {
        return status;
    }

    public void setStatus(AttestationStatus statusIn) {
        this.status = statusIn;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String descriptionIn) {
        this.description = descriptionIn;
    }

    public String getDetails() {
        return details;
    }

    public void setDetails(String detailsIn) {
        this.details = detailsIn;
    }

    public String getProcessOutput() {
        return processOutput;
    }

    public void setProcessOutput(String processOutputIn) {
        this.processOutput = processOutputIn;
    }

    public OffsetDateTime getAttested() {
        return attested;
    }

    public void setAttested(OffsetDateTime attestedIn) {
        this.attested = attestedIn;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (!(o instanceof AttestationResult)) {
            return false;
        }
        AttestationResult that = (AttestationResult) o;
        return Objects.equals(reportId, that.reportId) && Objects.equals(resultType, that.resultType);
    }

    @Override
    public int hashCode() {
        return Objects.hash(reportId, resultType);
    }

    @Override
    public String toString() {
        return new StringJoiner(", ", AttestationResult.class.getSimpleName() + "[", "]")
            .add("id=" + id)
            .add("reportId=" + reportId)
            .add("resultType=" + resultType)
            .add("status='" + status + "'")
            .add("description='" + description + "'")
            .add("attested=" + attested)
            .toString();
    }
}
