/*
 * Copyright (c) 2024--2026 SUSE LLC
 *
 * This software is licensed to you under the GNU General Public License,
 * version 2 (GPLv2). There is NO WARRANTY for this software, express or
 * implied, including the implied warranties of MERCHANTABILITY or FITNESS
 * FOR A PARTICULAR PURPOSE. You should have received a copy of GPLv2
 * along with this software; if not, see
 * http://www.gnu.org/licenses/old-licenses/gpl-2.0.txt.
 */
package com.suse.manager.model.attestation;

import com.redhat.rhn.domain.BaseDomainHelper;
import com.redhat.rhn.domain.action.Action;
import com.redhat.rhn.domain.server.Server;

import org.apache.commons.lang3.builder.EqualsBuilder;
import org.apache.commons.lang3.builder.HashCodeBuilder;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.type.SqlTypes;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;

import jakarta.persistence.Column;
import jakarta.persistence.Convert;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.OneToMany;
import jakarta.persistence.SequenceGenerator;
import jakarta.persistence.Table;

@Entity
@Table(name = "suseServerCoCoAttestationReport")
public class ServerCoCoAttestationReport extends BaseDomainHelper implements Serializable {
    private static final long serialVersionUID = 8161461482693316376L;

    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "server_cocoatt_report_seq")
    @SequenceGenerator(
            name = "server_cocoatt_report_seq", sequenceName = "suse_srvcocoatt_rep_id_seq", allocationSize = 1
    )
    private Long id;

    @ManyToOne
    @JoinColumn(name = "server_id")
    private Server server;

    @ManyToOne
    @JoinColumn(name = "action_id")
    private Action action;

    @Column(name = "env_type")
    @Convert(converter = CoCoEnvironmentTypeConverter.class)
    private CoCoEnvironmentType environmentType;

    @Column(name = "status")
    @Enumerated(EnumType.STRING)
    private CoCoReportStatus status;

    @JdbcTypeCode(SqlTypes.JSON)
    @Column(columnDefinition = "jsonb", name = "config_data")
    private Map<String, Object> configData = new TreeMap<>();

    @JdbcTypeCode(SqlTypes.JSON)
    @Column(columnDefinition = "jsonb", name = "in_data")
    private Map<String, Object> inData = new TreeMap<>();

    @JdbcTypeCode(SqlTypes.JSON)
    @Column(columnDefinition = "jsonb", name = "out_data")
    private Map<String, Object> outData = new TreeMap<>();

    @OneToMany(fetch = FetchType.LAZY, mappedBy = "report")
    private List<CoCoAttestationResult> results = new ArrayList<>();

    /**
     * @return return the ID
     */
    public Long getId() {
        return id;
    }

    /**
     * @return return the server
     */
    public Server getServer() {
        return server;
    }

    public Action getAction() {
        return action;
    }

    /**
     * @return return the selected environment type
     */
    public CoCoEnvironmentType getEnvironmentType() {
        return environmentType;
    }

    public CoCoReportStatus getStatus() {
        return status;
    }

    public Map<String, Object> getConfigData() {
        return configData;
    }

    public Map<String, Object> getInData() {
        return inData;
    }

    public Map<String, Object> getOutData() {
        return outData;
    }

    public List<CoCoAttestationResult> getResults() {
        return results;
    }

    /**
     * @param idIn set the id
     */
    public void setId(Long idIn) {
        id = idIn;
    }

    /*
     * Better set the ID directly (?)
     * But this method is required for Hibernate
     */
    protected void setServer(Server serverIn) {
        server = serverIn;
    }

    /**
     * @param actionIn the action id
     */
    public void setAction(Action actionIn) {
        action = actionIn;
    }

    /**
     * @param environmentTypeIn set the environment type
     */
    public void setEnvironmentType(CoCoEnvironmentType environmentTypeIn) {
        environmentType = environmentTypeIn;
    }

    /**
     * @param statusIn the status to set
     */
    public void setStatus(CoCoReportStatus statusIn) {
        status = statusIn;
    }

    /**
     * @param configDataIn the config data to set
     */
    public void setConfigData(Map<String, Object> configDataIn) {
        configData = configDataIn;
    }

    /**
     * @param inDataIn the input data to set
     */
    public void setInData(Map<String, Object> inDataIn) {
        inData = inDataIn;
    }

    /**
     * @param outDataIn the output data to set
     */
    public void setOutData(Map<String, Object> outDataIn) {
        outData = outDataIn;
    }

    /**
     * @param resultsIn the results to set
     */
    public void setResults(List<CoCoAttestationResult> resultsIn) {
        results = resultsIn;
    }

    /**
     * @param resultIn the results to add
     */
    public void addResults(CoCoAttestationResult resultIn) {
        results.add(resultIn);
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }
        ServerCoCoAttestationReport that = (ServerCoCoAttestationReport) o;
        return new EqualsBuilder()
                .append(outData, that.outData)
                .append(inData, that.inData)
                .append(status, that.status)
                .append(environmentType, that.environmentType)
                .append(action, that.action)
                .append(server, that.server)
                .isEquals();
    }

    @Override
    public int hashCode() {
        return new HashCodeBuilder(17, 37)
                .append(server)
                .append(action)
                .append(environmentType)
                .append(outData)
                .append(inData)
                .append(status)
                .toHashCode();
    }

    @Override
    public String toString() {
        return "ServerCoCoAttestationReport{" +
                "server=" + server +
                ", action=" + action +
                ", environmentType=" + environmentType +
                ", status=" + status +
                '}';
    }
}
