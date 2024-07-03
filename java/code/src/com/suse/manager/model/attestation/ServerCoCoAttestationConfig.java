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

import com.redhat.rhn.domain.server.Server;

import org.apache.commons.lang3.builder.EqualsBuilder;
import org.apache.commons.lang3.builder.HashCodeBuilder;

import javax.persistence.Column;
import javax.persistence.Convert;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.SequenceGenerator;
import javax.persistence.Table;

@Entity
@Table(name = "suseServerCoCoAttestationConfig")
public class ServerCoCoAttestationConfig {
    private Long id;
    private Server server;
    private boolean enabled;
    private CoCoEnvironmentType environmentType;
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
        environmentType = CoCoEnvironmentType.NONE;
        attestOnBoot = false;
    }

    /**
     * @return return the ID
     */
    @Id
    @Column(name = "id")
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "suse_srvcocoatt_cnf_seq")
    @SequenceGenerator(name = "suse_srvcocoatt_cnf_seq", sequenceName = "suse_srvcocoatt_cnf_id_seq",
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
