/*
 * Copyright (c) 2024--2025 SUSE LLC
 *
 * This software is licensed to you under the GNU General Public License,
 * version 2 (GPLv2). There is NO WARRANTY for this software, express or
 * implied, including the implied warranties of MERCHANTABILITY or FITNESS
 * FOR A PARTICULAR PURPOSE. You should have received a copy of GPLv2
 * along with this software; if not, see
 * http://www.gnu.org/licenses/old-licenses/gpl-2.0.txt.
 */
package com.suse.manager.model.attestation;

import org.apache.commons.lang3.builder.EqualsBuilder;
import org.apache.commons.lang3.builder.HashCodeBuilder;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.type.SqlTypes;

import java.io.Serial;
import java.io.Serializable;
import java.util.Date;
import java.util.Map;
import java.util.Optional;
import java.util.TreeMap;

import jakarta.persistence.Column;
import jakarta.persistence.Convert;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.SequenceGenerator;
import jakarta.persistence.Table;
import jakarta.persistence.Transient;

@Entity
@Table(name = "suseCoCoAttestationResult")
public class CoCoAttestationResult implements Serializable {
    @Serial
    private static final long serialVersionUID = -8527665110758960151L;


    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "cocoatt_result_seq")
    @SequenceGenerator(name = "cocoatt_result_seq", sequenceName = "suse_cocoatt_res_id_seq", allocationSize = 1)
    private Long id;

    @ManyToOne
    @JoinColumn(name = "report_id")
    private ServerCoCoAttestationReport report;

    @Column(name = "result_type")
    @Convert(converter = CoCoResultTypeConverter.class)
    private CoCoResultType resultType;

    @Column(name = "env_type")
    @Convert(converter = CoCoEnvironmentTypeConverter.class)
    private CoCoEnvironmentType environmentType;

    @JdbcTypeCode(SqlTypes.JSON)
    @Column(columnDefinition = "jsonb", name = "in_data")
    private Map<String, Object> inData = new TreeMap<>();

    @Column(name = "status")
    @Enumerated(EnumType.STRING)
    private CoCoResultStatus status;

    @Column(name = "description")
    private String description;

    @Column(name = "details")
    private String details;

    @Column(name = "process_output")
    private String processOutput;

    @Column(name = "attested")
    private Date attested;

    /**
     * @return return the ID
     */
    public Long getId() {
        return id;
    }

    /**
     * @return return the server
     */
    public ServerCoCoAttestationReport getReport() {
        return report;
    }

    /**
     * @return return the selected result type
     */
    public CoCoResultType getResultType() {
        return resultType;
    }

    /**
     * @return return the selected environment type
     */
    public CoCoEnvironmentType getEnvironmentType() {
        return environmentType;
    }

    /**
     * @return returns the input data
     */
    public Map<String, Object> getInData() {
        return inData;
    }

    /**
     * @return returns the status
     */
    public CoCoResultStatus getStatus() {
        return status;
    }

    /**
     * @return returns the description
     */
    public String getDescription() {
        return description;
    }

    /**
     * @return return the details if available
     */
    protected String getDetails() {
        return details;
    }

    /**
     * @return return the process output if available
     */
    protected String getProcessOutput() {
        return processOutput;
    }

    /**
     * @return the time this result was attested
     */
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
     * @param environmentTypeIn set the environment type
     */
    public void setEnvironmentType(CoCoEnvironmentType environmentTypeIn) {
        environmentType = environmentTypeIn;
    }

    /**
     * @param inDataIn the input data to set
     */
    public void setInData(Map<String, Object> inDataIn) {
        inData = inDataIn;
    }

    /**
     * @param statusIn the status to set
     */
    public void setStatus(CoCoResultStatus statusIn) {
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
