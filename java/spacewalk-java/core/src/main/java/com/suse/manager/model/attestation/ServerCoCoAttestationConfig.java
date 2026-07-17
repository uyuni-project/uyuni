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

import com.redhat.rhn.domain.server.Server;

import org.apache.commons.lang3.builder.EqualsBuilder;
import org.apache.commons.lang3.builder.HashCodeBuilder;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.type.SqlTypes;

import java.io.Serializable;
import java.util.Map;
import java.util.TreeMap;

import jakarta.persistence.Column;
import jakarta.persistence.Convert;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.SequenceGenerator;
import jakarta.persistence.Table;

@Entity
@Table(name = "suseServerCoCoAttestationConfig")
public class ServerCoCoAttestationConfig implements Serializable  {
    private Long id;
    private Server server;
    private boolean enabled;
    private CoCoEnvironmentType environmentType;
    private Map<String, Object> inData = new TreeMap<>();
    private boolean attestOnBoot;

    // Default empty constructor for hibernate
    protected ServerCoCoAttestationConfig() {
        this(false, null);
    }

    /**
     * Constructor
     *
     * @param enabledIn if attestation is enabled for this server
     * @param serverIn the server
     */
    public ServerCoCoAttestationConfig(boolean enabledIn, Server serverIn) {
        enabled = enabledIn;
        server = serverIn;
        environmentType = CoCoEnvironmentType.getDefault();
        attestOnBoot = false;
    }

    /**
     * @return return the ID
     */
    @Id
    @Column(name = "id")
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "suse_srvcocoatt_cnf_seq")
    @SequenceGenerator(
            name = "suse_srvcocoatt_cnf_seq", sequenceName = "suse_srvcocoatt_cnf_id_seq", allocationSize = 1
    )
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

    /**
     * @return return if enabled
     */
    @Column(name = "enabled")
    public boolean isEnabled() {
        return enabled;
    }

    /**
     * @return return the selected environment type
     */
    @Column(name = "env_type")
    @Convert(converter = CoCoEnvironmentTypeConverter.class)
    public CoCoEnvironmentType getEnvironmentType() {
        return environmentType;
    }

    /**
     * @return returns the input data
     */
    @JdbcTypeCode(SqlTypes.JSON)
    @Column(columnDefinition = "jsonb", name = "in_data")
    public Map<String, Object> getInData() {
        return inData;
    }

    /**
     * @return returns true attest on boot flag is set
     */
    @Column(name = "attest_on_boot")
    public boolean isAttestOnBoot() {
        return attestOnBoot;
    }

    /**
     * Use setServer() instead
     * @param idIn set the id
     */
    protected void setId(Long idIn) {
        id = idIn;
    }

    /**
     * @param serverIn the server object
     */
    public void setServer(Server serverIn) {
        server = serverIn;
    }

    /**
     * @param enabledIn set is enabled
     */
    public void setEnabled(boolean enabledIn) {
        enabled = enabledIn;
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
     * @param attestOnBootIn the value of attest on boot flag
     */
    public void setAttestOnBoot(boolean attestOnBootIn) {
        this.attestOnBoot = attestOnBootIn;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }
        ServerCoCoAttestationConfig that = (ServerCoCoAttestationConfig) o;
        return new EqualsBuilder()
                .append(enabled, that.enabled)
                .append(environmentType, that.environmentType)
                .append(server, that.server)
                .isEquals();
    }

    @Override
    public int hashCode() {
        return new HashCodeBuilder(17, 37)
                .append(server)
                .append(enabled)
                .append(environmentType)
                .toHashCode();
    }

    @Override
    public String toString() {
        return "ServerCoCoAttestationConfig{" +
                "server=" + server +
                ", enabled=" + enabled +
                ", environmentType=" + environmentType +
                '}';
    }
}
