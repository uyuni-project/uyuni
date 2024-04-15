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

import com.suse.manager.model.attestation.CoCoAttestationResult;

import java.util.Date;
import java.util.Objects;
import java.util.StringJoiner;

public class CoCoAttestationResultJson {

    private final long id;

    private final String resultType;

    private final String resultTypeLabel;

    private final String status;

    private final String statusDescription;

    private final String description;

    private final String details;

    private final Date attestationTime;


    /**
     * Default constructor
     * @param result the attestation result domain object
     */
    public CoCoAttestationResultJson(CoCoAttestationResult result) {
        this.id = result.getId();
        this.resultType = result.getResultType().name();
        this.resultTypeLabel = result.getResultType().getTypeLabel();
        this.status = result.getStatus().name();
        this.statusDescription = result.getStatus().getDescription();
        this.description = result.getDescription();
        this.attestationTime = result.getAttested();
        this.details = result.getDetailsOpt().orElse(null);
    }

    public long getId() {
        return id;
    }

    public String getResultType() {
        return resultType;
    }

    public String getResultTypeLabel() {
        return resultTypeLabel;
    }

    public String getStatus() {
        return status;
    }

    public String getStatusDescription() {
        return statusDescription;
    }

    public String getDescription() {
        return description;
    }

    public String getDetails() {
        return details;
    }

    public Date getAttestationTime() {
        return attestationTime;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (!(o instanceof CoCoAttestationResultJson)) {
            return false;
        }
        CoCoAttestationResultJson that = (CoCoAttestationResultJson) o;
        return id == that.id;
    }

    @Override
    public int hashCode() {
        return Objects.hashCode(id);
    }

    @Override
    public String toString() {
        return new StringJoiner(", ", CoCoAttestationResultJson.class.getSimpleName() + "[", "]")
            .add("id=" + getId())
            .add("resultType='" + getResultType() + "'")
            .add("status='" + getStatus() + "'")
            .add("attestationTime=" + getAttestationTime())
            .toString();
    }
}
