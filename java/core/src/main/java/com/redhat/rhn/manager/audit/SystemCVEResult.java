/*
 * Copyright (c) 2026 SUSE LLC
 *
 * This software is licensed to you under the GNU General Public License,
 * version 2 (GPLv2). There is NO WARRANTY for this software, express or
 * implied, including the implied warranties of MERCHANTABILITY or FITNESS
 * FOR A PARTICULAR PURPOSE. You should have received a copy of GPLv2
 * along with this software; if not, see
 * http://www.gnu.org/licenses/old-licenses/gpl-2.0.txt.
 *
 * SPDX-License-Identifier: GPL-2.0-only
 */

package com.redhat.rhn.manager.audit;

import com.redhat.rhn.frontend.dto.BaseTupleDto;

import com.suse.oval.OvalSeverity;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

import jakarta.persistence.Tuple;

/**
 * One CVE involving a package installed on a single system, with its patch status.
 */
public class SystemCVEResult extends BaseTupleDto {

    /**
     * An advisory fixing the CVE on the system, with the channel offering it.
     *
     * @param id the errata id
     * @param advisory the advisory name
     * @param channelLabel the label of the best channel offering the advisory
     * @param assigned true if that channel is assigned to the system
     */
    public record Errata(long id, String advisory, String channelLabel, boolean assigned) implements Serializable { }

    private Long id;
    private String cveName;
    private PatchStatus status;
    private OvalSeverity severity;
    private ArrayList<Errata> errata = new ArrayList<>();

    /**
     * Constructor used to read the rows of the query of {@link SystemCVEAuditManager}
     *
     * @param tuple JPA tuple with the columns id, cve_name, status and severity
     */
    public SystemCVEResult(Tuple tuple) {
        id = getTupleValue(tuple, "id", Number.class).map(Number::longValue).orElse(null);
        cveName = getTupleValue(tuple, "cve_name", String.class).orElse("");
        status = getTupleValue(tuple, "status", String.class).map(PatchStatus::valueOf).orElse(PatchStatus.UNKNOWN);
        severity = getTupleValue(tuple, "severity", String.class).map(OvalSeverity::valueOf)
                .orElse(OvalSeverity.UNKNOWN);
    }

    /**
     * @return the CVE id (rhnCVE.id)
     */
    @Override
    public Long getId() {
        return id;
    }

    /**
     * @return the CVE name, e.g. CVE-2024-1234
     */
    public String getCveName() {
        return cveName;
    }

    /**
     * @return the patch status of the CVE on the system
     */
    public PatchStatus getStatus() {
        return status;
    }

    /**
     * @return the highest OVAL severity of the CVE for the platforms of the system
     */
    public OvalSeverity getSeverity() {
        return severity;
    }

    /**
     * @return the advisories fixing the CVE on the system
     */
    public List<Errata> getErrata() {
        return errata;
    }

    /**
     * @param errataIn the advisories fixing the CVE on the system
     */
    public void setErrata(List<Errata> errataIn) {
        errata = new ArrayList<>(errataIn);
    }
}
