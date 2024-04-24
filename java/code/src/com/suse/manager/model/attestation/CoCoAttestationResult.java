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
package com.suse.manager.model.attestation;

import org.apache.commons.lang3.builder.EqualsBuilder;
import org.apache.commons.lang3.builder.HashCodeBuilder;

import java.util.Date;
import java.util.Optional;

import javax.persistence.Column;
import javax.persistence.Convert;
import javax.persistence.Entity;
import javax.persistence.EnumType;
import javax.persistence.Enumerated;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.SequenceGenerator;
import javax.persistence.Table;
import javax.persistence.Transient;

@Entity
@Table(name = "suseCoCoAttestationResult")
public class CoCoAttestationResult {
    private Long id;
    private ServerCoCoAttestationReport report;
    private CoCoResultType resultType;
    private CoCoAttestationStatus status;
    private String description;
    private String details;
    private String processOutput;
    private Date attested;

    /**
     * @return return the ID
     */
    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "cocoatt_result_seq")
    @SequenceGenerator(name = "cocoatt_result_seq", sequenceName = "suse_cocoatt_res_id_seq",
            allocationSize = 1)
    public Long getId() {
        return id;
    }

    /**
     * @return return the server
     */
    @ManyToOne
    @JoinColumn(name = "report_id")
    public ServerCoCoAttestationReport getReport() {
        return report;
    }

    /**
     * @return return the selected environment type
     */
    @Column(name = "result_type")
    @Convert(converter = CoCoResultTypeConverter.class)
    public CoCoResultType getResultType() {
        return resultType;
    }

    /**
     * @return returns the status
     */
    @Column(name = "status")
    @Enumerated(EnumType.STRING)
    public CoCoAttestationStatus getStatus() {
        return status;
    }

    /**
     * @return returns the description
     */
    @Column(name = "description")
    public String getDescription() {
        return description;
    }

    /**
     * @return return the details if available
     */
    @Column(name = "details")
    protected String getDetails() {
        return details;
    }

    /**
     * @return return the process output if available
     */
    @Column(name = "process_output")
    protected String getProcessOutput() {
        return processOutput;
    }

    /**
     * @return the time this result was attested
     */
    @Column(name = "attested")
    public Date getAttested() {
        return attested;
    }

    /**
     * @return return the details if available
     */
    @Transient
    public Optional<String> getDetailsOpt() {
        return Optional.ofNullable(details);
    }

    /**
     * @return return the details if available
     */
    @Transient
    public Optional<String> getProcessOutputOpt() {
        return Optional.ofNullable(processOutput);
    }

    /**
     * @param idIn set the id
     */
    public void setId(Long idIn) {
        id = idIn;
    }

    /**
     * @param reportIn the report to set
     */
    public void setReport(ServerCoCoAttestationReport reportIn) {
        report = reportIn;
    }

    /**
     * @param resultTypeIn set the result type
     */
    public void setResultType(CoCoResultType resultTypeIn) {
        resultType = resultTypeIn;
    }

    /**
     * @param statusIn the status to set
     */
    public void setStatus(CoCoAttestationStatus statusIn) {
        status = statusIn;
    }

    /**
     * @param descriptionIn the description to set
     */
    public void setDescription(String descriptionIn) {
        description = descriptionIn;
    }

    /**
     * @param detailsIn the output data to set
     */
    public void setDetails(String detailsIn) {
        details = detailsIn;
    }

    public void setProcessOutput(String processOutputIn) {
        this.processOutput = processOutputIn;
    }

    /**
     * @param attestedIn the time to set
     */
    public void setAttested(Date attestedIn) {
        attested = attestedIn;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }
        CoCoAttestationResult that = (CoCoAttestationResult) o;
        return new EqualsBuilder()
                .append(description, that.description)
                .append(status, that.status)
                .append(resultType, that.resultType)
                .append(report, that.report)
                .isEquals();
    }

    @Override
    public int hashCode() {
        return new HashCodeBuilder(17, 37)
                .append(report)
                .append(resultType)
                .append(description)
                .append(status)
                .toHashCode();
    }

    @Override
    public String toString() {
        return "ServerCoCoAttestationReport{" +
                "report_id=" + report.getId() +
                ", resultType=" + resultType +
                ", status=" + status +
                ", description=" + description +
                '}';
    }
}
