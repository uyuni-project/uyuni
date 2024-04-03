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

import com.redhat.rhn.domain.BaseDomainHelper;
import com.redhat.rhn.domain.action.Action;
import com.redhat.rhn.domain.server.Server;

import org.apache.commons.lang3.builder.EqualsBuilder;
import org.apache.commons.lang3.builder.HashCodeBuilder;
import org.hibernate.annotations.Type;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;

import javax.persistence.Column;
import javax.persistence.Convert;
import javax.persistence.Entity;
import javax.persistence.EnumType;
import javax.persistence.Enumerated;
import javax.persistence.FetchType;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.OneToMany;
import javax.persistence.SequenceGenerator;
import javax.persistence.Table;

@Entity
@Table(name = "suseServerCoCoAttestationReport")
public class ServerCoCoAttestationReport extends BaseDomainHelper implements Serializable {
    private static final long serialVersionUID = 8161461482693316376L;
    private Long id;
    private Server server;
    private Action action;
    private CoCoEnvironmentType environmentType;
    private CoCoAttestationStatus status;
    private Map<String, Object> inData = new TreeMap<>();
    private Map<String, Object> outData = new TreeMap<>();
    private List<CoCoAttestationResult> results = new ArrayList<>();

    /**
     * @return return the ID
     */
    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "server_cocoatt_report_seq")
    @SequenceGenerator(name = "server_cocoatt_report_seq", sequenceName = "suse_srvcocoatt_rep_id_seq",
            allocationSize = 1)
    public Long getId() {
        return id;
    }

    /**
     * @return return the server
     */
    @ManyToOne
    @JoinColumn(name = "server_id")
    public Server getServer() {
        return server;
    }

    @ManyToOne
    public Action getAction() {
        return action;
    }

    /**
     * @return return the selected environment type
     */
    @Column(name = "env_type")
    @Convert(converter = CoCoEnvironmentTypeConverter.class)
    public CoCoEnvironmentType getEnvironmentType() {
        return environmentType;
    }

    @Column(name = "status")
    @Enumerated(EnumType.STRING)
    public CoCoAttestationStatus getStatus() {
        return status;
    }

    @Type(type = "json")
    @Column(columnDefinition = "jsonb", name = "in_data")
    public Map<String, Object> getInData() {
        return inData;
    }

    @Type(type = "json")
    @Column(columnDefinition = "jsonb", name = "out_data")
    public Map<String, Object> getOutData() {
        return outData;
    }

    @OneToMany(fetch = FetchType.LAZY, mappedBy = "report")
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
    public void setStatus(CoCoAttestationStatus statusIn) {
        status = statusIn;
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
