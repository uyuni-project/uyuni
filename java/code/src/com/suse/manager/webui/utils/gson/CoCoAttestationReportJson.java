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

import com.redhat.rhn.common.localization.LocalizationService;
import com.redhat.rhn.domain.action.Action;
import com.redhat.rhn.domain.action.ActionType;
import com.redhat.rhn.domain.user.User;

import com.suse.manager.model.attestation.CoCoAttestationResult;
import com.suse.manager.model.attestation.CoCoAttestationStatus;
import com.suse.manager.model.attestation.ServerCoCoAttestationReport;

import java.util.Date;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.StringJoiner;
import java.util.stream.Collectors;

public class CoCoAttestationReportJson {
    private final long id;

    private final long systemId;

    private final String systemName;

    private final String environmentType;

    private final String environmentTypeLabel;

    private final String environmentTypeDescription;

    private final String status;

    private final String statusDescription;

    private final Date creationTime;

    private final Date modificationTime;

    private final Date attestationTime;

    private final Long actionId;

    private final String actionName;

    private final String actionScheduledBy;

    private final List<CoCoAttestationResultJson> results;

    /**
     * Default constructor
     * @param report the attestation report domain object
     */
    public CoCoAttestationReportJson(ServerCoCoAttestationReport report) {
        this.id = report.getId();
        this.systemId = report.getServer().getId();
        this.systemName = report.getServer().getName();
        this.environmentType = report.getEnvironmentType().name();
        this.environmentTypeLabel = report.getEnvironmentType().getLabel();
        this.environmentTypeDescription = report.getEnvironmentType().getDescription();
        this.status = report.getStatus().name();
        this.statusDescription = report.getStatus().getDescription();
        this.creationTime = report.getCreated();
        this.modificationTime = report.getModified();
        this.attestationTime = getAttestationTime(report.getResults());

        Optional<Action> action = Optional.ofNullable(report.getAction());
        this.actionId = action.map(Action::getId).orElse(null);
        this.actionName = action.map(Action::getActionType).map(ActionType::getLabel)
            .map(label -> LocalizationService.getInstance().getMessage(label)).orElse(null);
        this.actionScheduledBy = action.map(Action::getSchedulerUser).map(User::getLogin).orElse(null);

        this.results = report.getResults().stream().map(CoCoAttestationResultJson::new).collect(Collectors.toList());
    }

    public long getId() {
        return id;
    }

    public long getSystemId() {
        return systemId;
    }

    public String getSystemName() {
        return systemName;
    }

    public String getEnvironmentType() {
        return environmentType;
    }

    public String getEnvironmentTypeLabel() {
        return environmentTypeLabel;
    }

    public String getEnvironmentTypeDescription() {
        return environmentTypeDescription;
    }

    public String getStatus() {
        return status;
    }

    public String getStatusDescription() {
        return statusDescription;
    }

    public Date getCreationTime() {
        return creationTime;
    }

    public Date getModificationTime() {
        return modificationTime;
    }

    public Date getAttestationTime() {
        return attestationTime;
    }

    public Long getActionId() {
        return actionId;
    }

    public String getActionName() {
        return actionName;
    }

    public String getActionScheduledBy() {
        return actionScheduledBy;
    }

    public List<CoCoAttestationResultJson> getResults() {
        return results;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (!(o instanceof CoCoAttestationReportJson)) {
            return false;
        }
        CoCoAttestationReportJson that = (CoCoAttestationReportJson) o;
        return id == that.id;
    }

    @Override
    public int hashCode() {
        return Objects.hashCode(id);
    }

    @Override
    public String toString() {
        return new StringJoiner(", ", CoCoAttestationReportJson.class.getSimpleName() + "[", "]")
            .add("id=" + getId())
            .add("systemName='" + getSystemName() + "'")
            .add("systemName='" + getEnvironmentType() + "'")
            .add("status=" + getStatus())
            .add("creationTime=" + getCreationTime())
            .add("modificationTime=" + getModificationTime())
            .add("attestationTime=" + getAttestationTime())
            .toString();
    }

    private static Date getAttestationTime(List<CoCoAttestationResult> results) {
        if (results.stream().anyMatch(r -> r.getStatus() != CoCoAttestationStatus.SUCCEEDED)) {
            return null;
        }

        return results.stream()
            .map(r -> r.getAttested())
            .filter(Objects::nonNull)
            .max(Date::compareTo)
            .orElse(null);
    }
}


