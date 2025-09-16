/*
 * Copyright (c) 2021 SUSE LLC
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

package com.redhat.rhn.domain.cloudpayg;

import com.redhat.rhn.domain.BaseDomainHelper;
import com.redhat.rhn.domain.credentials.CloudCredentials;

import org.apache.commons.lang3.builder.EqualsBuilder;
import org.apache.commons.lang3.builder.HashCodeBuilder;

import javax.persistence.CascadeType;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.EnumType;
import javax.persistence.Enumerated;
import javax.persistence.FetchType;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.OneToOne;
import javax.persistence.SequenceGenerator;
import javax.persistence.Table;
import javax.persistence.Transient;

@Entity
@Table(name = "susePaygSshData")
public class PaygSshData extends BaseDomainHelper {
    private Long id;
    private String description;
    private String host;
    private Integer port;
    private String username;
    private String password;
    private String key;
    private String keyPassword;
    private String bastionHost;
    private Integer bastionPort;
    private String bastionUsername;
    private String bastionPassword;
    private String bastionKey;
    private String bastionKeyPassword;
    private Status status;
    private String errorMessage;
    private CloudRmtHost rmtHosts;

    /**
     * Status of the {@link PaygSshData}
     */
    public enum Status {
        P("Pending Processing"),
        E("Error"),
        S("Success");

        private final String label;

        Status(String labelIn) {
            this.label = labelIn;
        }

        /**
         * Return the label
         * @return the label
         */
        public String getLabel() {
            return label;
        }
    }

    /**
     * Standard constructor.
     */
    public PaygSshData() {
        status = Status.P;
    }

    /**
     * Constructor with all the fields for the pay ssh connection
     * @param descriptionIn
     * @param hostIn
     * @param portIn
     * @param usernameIn
     * @param passwordIn
     * @param keyIn
     * @param keyPasswordIn
     * @param bastionHostIn
     * @param bastionPortIn
     * @param bastionUsernameIn
     * @param bastionPasswordIn
     * @param bastionKeyIn
     * @param bastionKeyPasswordIn
     */
    public PaygSshData(String descriptionIn,
                       String hostIn, Integer portIn,
                       String usernameIn, String passwordIn,
                       String keyIn, String keyPasswordIn,
                       String bastionHostIn, Integer bastionPortIn,
                       String bastionUsernameIn, String bastionPasswordIn,
                       String bastionKeyIn, String bastionKeyPasswordIn) {
        this();
        this.description = descriptionIn;
        this.host = hostIn;
        this.port = portIn;
        this.username = usernameIn;
        this.password = passwordIn;
        this.key = keyIn;
        this.keyPassword = keyPasswordIn;
        this.bastionHost = bastionHostIn;
        this.bastionPort = bastionPortIn;
        this.bastionUsername = bastionUsernameIn;
        this.bastionPassword = bastionPasswordIn;
        this.bastionKey = bastionKeyIn;
        this.bastionKeyPassword = bastionKeyPasswordIn;
        this.status = Status.P;
    }

    /**
     * Gets the id.
     * @return the id
     */
    @Id
    @Column(name = "id")
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "susePaygSshData_seq")
    @SequenceGenerator(name = "susePaygSshData_seq", sequenceName = "susePaygSshData_id_seq",
        allocationSize = 1)
    public Long getId() {
        return id;
    }

    /**
     * Sets the id.
     * @param idIn the new id
     */
    public void setId(Long idIn) {
        id = idIn;
    }

    @Column(name = "description")
    public String getDescription() {
        return description;
    }

    public void setDescription(String descriptionIn) {
        this.description = descriptionIn;
    }

    @Column(name = "host")
    public String getHost() {
        return host;
    }

    public void setHost(String hostIn) {
        this.host = hostIn;
    }

    @Column(name = "port")
    public Integer getPort() {
        return port;
    }

    public void setPort(Integer portIn) {
        this.port = portIn;
    }

    @Column(name = "username")
    public String getUsername() {
        return username;
    }

    public void setUsername(String usernameIn) {
        this.username = usernameIn;
    }

    @Column(name = "password")
    public String getPassword() {
        return password;
    }

    public void setPassword(String passwordIn) {
        this.password = passwordIn;
    }

    @Column(name = "key")
    public String getKey() {
        return key;
    }

    public void setKey(String keyIn) {
        this.key = keyIn;
    }

    @Column(name = "key_password")
    public String getKeyPassword() {
        return keyPassword;
    }

    public void setKeyPassword(String keyPasswordIn) {
        this.keyPassword = keyPasswordIn;
    }

    @Column(name = "bastion_host")
    public String getBastionHost() {
        return bastionHost;
    }

    public void setBastionHost(String bastionHostIn) {
        this.bastionHost = bastionHostIn;
    }

    @Column(name = "bastion_port")
    public Integer getBastionPort() {
        return bastionPort;
    }

    public void setBastionPort(Integer bastionPortIn) {
        this.bastionPort = bastionPortIn;
    }

    @Column(name = "bastion_username")
    public String getBastionUsername() {
        return bastionUsername;
    }

    public void setBastionUsername(String bastionUsernameIn) {
        this.bastionUsername = bastionUsernameIn;
    }

    @Column(name = "bastion_password")
    public String getBastionPassword() {
        return bastionPassword;
    }

    public void setBastionPassword(String bastionPasswordIn) {
        this.bastionPassword = bastionPasswordIn;
    }

    @Column(name = "bastion_key")
    public String getBastionKey() {
        return bastionKey;
    }

    public void setBastionKey(String bastionKeyIn) {
        this.bastionKey = bastionKeyIn;
    }

    @Column(name = "bastion_key_password")
    public String getBastionKeyPassword() {
        return bastionKeyPassword;
    }

    public void setBastionKeyPassword(String bastionKeyPasswordIn) {
        this.bastionKeyPassword = bastionKeyPasswordIn;
    }

    @Column(name = "status")
    @Enumerated(EnumType.STRING)
    public Status getStatus() {
        return status;
    }

    public void setStatus(Status statusIn) {
        this.status = statusIn;
    }

    @Column(name = "error_message")
    public String getErrorMessage() {
        return errorMessage;
    }

    public void setErrorMessage(String errorMessageIn) {
        this.errorMessage = errorMessageIn;
    }

    @Transient
    public CloudCredentials getCredentials() {
        return PaygSshDataFactory.lookupCloudCredentials(this).orElse(null);
    }

    @OneToOne(fetch = FetchType.LAZY, mappedBy = "paygSshData", cascade = CascadeType.ALL)
    public CloudRmtHost getRmtHosts() {
        return rmtHosts;
    }

    public void setRmtHosts(CloudRmtHost rmtHostsIn) {
        this.rmtHosts = rmtHostsIn;
    }

    /**
     * Identifies a connection for SUSE Manager PAYG
     * @return true if this SSH data refers to a SUSE Manager PAYG connection.
     */
    @Transient
    public boolean isSUSEManagerPayg() {
        return "localhost".equals(host);
    }

    @Override
    public String toString() {
        return "PaygSshData{" +
                "id=" + id +
                ", description='" + description + '\'' +
                ", host='" + host + '\'' +
                ", port=" + port +
                ", username='" + username + '\'' +
                ", status=" + status +
                '}';
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }

        if (o == null || getClass() != o.getClass()) {
            return false;
        }

        PaygSshData that = (PaygSshData) o;

        return new EqualsBuilder().append(host, that.host)
            .append(port, that.port)
            .append(username, that.username)
            .append(password, that.password)
            .append(key, that.key)
            .append(keyPassword, that.keyPassword)
            .append(bastionHost, that.bastionHost)
            .append(bastionPort, that.bastionPort)
            .append(bastionUsername, that.bastionUsername)
            .append(bastionPassword, that.bastionPassword)
            .append(bastionKey, that.bastionKey)
            .append(bastionKeyPassword, that.bastionKeyPassword)
            .append(status, that.status)
            .append(errorMessage, that.errorMessage)
            .isEquals();
    }

    @Override
    public int hashCode() {
        return new HashCodeBuilder(17, 37)
            .append(host)
            .append(port)
            .append(username)
            .append(password)
            .append(key)
            .append(keyPassword)
            .append(bastionHost)
            .append(bastionPort)
            .append(bastionUsername)
            .append(bastionPassword)
            .append(bastionKey)
            .append(bastionKeyPassword)
            .append(status)
            .append(errorMessage)
            .toHashCode();
    }
}
