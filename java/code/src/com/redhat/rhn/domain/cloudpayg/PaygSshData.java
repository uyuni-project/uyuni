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

import com.redhat.rhn.common.conf.Config;
import com.redhat.rhn.common.util.AESCryptException;
import com.redhat.rhn.common.util.CryptHelper;
import com.redhat.rhn.common.util.FileUtils;
import com.redhat.rhn.domain.BaseDomainHelper;
import com.redhat.rhn.domain.credentials.Credentials;
import com.redhat.rhn.domain.credentials.CredentialsFactory;

import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.builder.EqualsBuilder;
import org.apache.commons.lang3.builder.HashCodeBuilder;
import org.apache.commons.lang3.builder.ToStringBuilder;

import java.io.IOException;

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
    private String passwordEncrypted;
    private String keyEncrypted;
    private String keyPasswordEncrypted;

    private String bastionHost;
    private Integer bastionPort;
    private String bastionUsername;
    private String bastionPasswordEncrypted;
    private String bastionKeyEncrypted;
    private String bastionKeyPasswordEncrypted;

    private Status status;
    private String errorMessage;

    private Credentials credentials;
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
                       String bastionKeyIn, String bastionKeyPasswordIn) throws AESCryptException, IOException {
        this();
        this.description = descriptionIn;
        this.host = hostIn;
        this.port = portIn;
        this.username = usernameIn;
        setPassword(passwordIn);
        setKey(keyIn);
        setKeyPassword(keyPasswordIn);
        this.bastionHost = bastionHostIn;
        this.bastionPort = bastionPortIn;
        this.bastionUsername = bastionUsernameIn;
        setBastionPassword(bastionPasswordIn);
        setBastionKey(bastionKeyIn);
        setBastionKeyPassword(bastionKeyPasswordIn);
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
    public String getPasswordEncrypted() {
        return passwordEncrypted;
    }

    /**
     * @return the Password in clear text
     * @throws IOException when something went wrong
     * @throws AESCryptException when something went wrong
     */
    @Transient
    public String getPassword() throws IOException, AESCryptException {
        if (passwordEncrypted != null && passwordEncrypted.startsWith(CredentialsFactory.SALTED_MAGIC_B64)) {
            // password is encrypted
            String mpw = FileUtils.readFirstLineFromFile(Config.getDefaultMasterPasswordFile());
            return CryptHelper.aes256Decrypt(passwordEncrypted, mpw);
        }
        return passwordEncrypted;
    }

    /**
     * Set the Password and encrypt it
     * @param passwordIn the plain password
     * @throws IOException when something went wrong
     * @throws AESCryptException when something went wrong
     */
    public void setPassword(String passwordIn) throws IOException, AESCryptException {
        if (StringUtils.isEmpty(passwordIn)) {
            passwordEncrypted = passwordIn;
            return;
        }
        String mpw = FileUtils.readFirstLineFromFile(Config.getDefaultMasterPasswordFile());
        passwordEncrypted = CryptHelper.aes256Encrypt(passwordIn, mpw);

    }

    public void setPasswordEncrypted(String passwordEncryptedIn) {
        passwordEncrypted = passwordEncryptedIn;
    }

    @Column(name = "key")
    public String getKeyEncrypted() {
        return keyEncrypted;
    }

    /**
     * @return the Key in clear text
     * @throws IOException when something went wrong
     * @throws AESCryptException when something went wrong
     */
    @Transient
    public String getKey() throws IOException, AESCryptException {
        if (keyEncrypted != null && keyEncrypted.startsWith(CredentialsFactory.SALTED_MAGIC_B64)) {
            // password is encrypted
            String mpw = FileUtils.readFirstLineFromFile(Config.getDefaultMasterPasswordFile());
            return CryptHelper.aes256Decrypt(keyEncrypted, mpw);
        }
        return keyEncrypted;
    }

    /**
     * Set the Key and encrypt it
     * @param keyIn the plain password
     * @throws IOException when something went wrong
     * @throws AESCryptException when something went wrong
     */
    public void setKey(String keyIn) throws IOException, AESCryptException {
        if (StringUtils.isEmpty(keyIn)) {
            keyEncrypted = keyIn;
            return;
        }
        String mpw = FileUtils.readFirstLineFromFile(Config.getDefaultMasterPasswordFile());
        keyEncrypted = CryptHelper.aes256Encrypt(keyIn, mpw);
    }

    public void setKeyEncrypted(String keyEncryptedIn) {
        keyEncrypted = keyEncryptedIn;
    }

    @Column(name = "key_password")
    public String getKeyPasswordEncrypted() {
        return keyPasswordEncrypted;
    }

    /**
     * @return the Key Password in clear text
     * @throws IOException when something went wrong
     * @throws AESCryptException when something went wrong
     */
    @Transient
    public String getKeyPassword() throws IOException, AESCryptException {
        if (keyPasswordEncrypted != null && keyPasswordEncrypted.startsWith(CredentialsFactory.SALTED_MAGIC_B64)) {
            // password is encrypted
            String mpw = FileUtils.readFirstLineFromFile(Config.getDefaultMasterPasswordFile());
            return CryptHelper.aes256Decrypt(keyPasswordEncrypted, mpw);
        }
        return keyPasswordEncrypted;
    }

    /**
     * Set the Bastion Key Password and encrypt it
     * @param keyPasswordIn the plain password
     * @throws IOException when something went wrong
     * @throws AESCryptException when something went wrong
     */
    public void setKeyPassword(String keyPasswordIn) throws IOException, AESCryptException {
        if (StringUtils.isEmpty(keyPasswordIn)) {
            keyPasswordEncrypted = keyPasswordIn;
            return;
        }
        String mpw = FileUtils.readFirstLineFromFile(Config.getDefaultMasterPasswordFile());
        keyPasswordEncrypted = CryptHelper.aes256Encrypt(keyPasswordIn, mpw);
    }

    public void setKeyPasswordEncrypted(String keyPasswordEncryptedIn) {
        keyPasswordEncrypted = keyPasswordEncryptedIn;
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
    public String getBastionPasswordEncrypted() {
        return bastionPasswordEncrypted;
    }

    /**
     * @return the Bastion Password in clear text
     * @throws IOException when something went wrong
     * @throws AESCryptException when something went wrong
     */
    @Transient
    public String getBastionPassword() throws IOException, AESCryptException {
        if (bastionPasswordEncrypted != null &&
                bastionPasswordEncrypted.startsWith(CredentialsFactory.SALTED_MAGIC_B64)) {
            // password is encrypted
            String mpw = FileUtils.readFirstLineFromFile(Config.getDefaultMasterPasswordFile());
            return CryptHelper.aes256Decrypt(bastionPasswordEncrypted, mpw);
        }
        return bastionPasswordEncrypted;
    }

    /**
     * Set the Bastion Password and encrypt it
     * @param bastionPasswordIn the plain password
     * @throws IOException when something went wrong
     * @throws AESCryptException when something went wrong
     */
    public void setBastionPassword(String bastionPasswordIn) throws IOException, AESCryptException {
        if (StringUtils.isEmpty(bastionPasswordIn)) {
            bastionPasswordEncrypted = bastionPasswordIn;
            return;
        }
        String mpw = FileUtils.readFirstLineFromFile(Config.getDefaultMasterPasswordFile());
        bastionPasswordEncrypted = CryptHelper.aes256Encrypt(bastionPasswordIn, mpw);
    }

    public void setBastionPasswordEncrypted(String bastionPasswordEncryptedIn) {
        bastionPasswordEncrypted = bastionPasswordEncryptedIn;
    }

    @Column(name = "bastion_key")
    public String getBastionKeyEncrypted() {
        return bastionKeyEncrypted;
    }

    /**
     * @return the Bastion Key in clear text
     * @throws IOException when something went wrong
     * @throws AESCryptException when something went wrong
     */
    @Transient
    public String getBastionKey() throws IOException, AESCryptException {
        if (bastionKeyEncrypted != null && bastionKeyEncrypted.startsWith(CredentialsFactory.SALTED_MAGIC_B64)) {
            // password is encrypted
            String mpw = FileUtils.readFirstLineFromFile(Config.getDefaultMasterPasswordFile());
            return CryptHelper.aes256Decrypt(bastionKeyEncrypted, mpw);
        }
        return bastionKeyEncrypted;
    }

    /**
     * Set the Bastion Key Password and encrypt it
     * @param bastionKeyIn the plain password
     * @throws IOException when something went wrong
     * @throws AESCryptException when something went wrong
     */
    public void setBastionKey(String bastionKeyIn) throws IOException, AESCryptException {
        if (StringUtils.isEmpty(bastionKeyIn)) {
            bastionKeyEncrypted = bastionKeyIn;
            return;
        }
        String mpw = FileUtils.readFirstLineFromFile(Config.getDefaultMasterPasswordFile());
        bastionKeyEncrypted = CryptHelper.aes256Encrypt(bastionKeyIn, mpw);
    }

    public void setBastionKeyEncrypted(String bastionKeyEncryptedIn) {
        bastionKeyEncrypted = bastionKeyEncryptedIn;
    }

    @Column(name = "bastion_key_password")
    public String getBastionKeyPasswordEncrypted() {
        return bastionKeyPasswordEncrypted;
    }

    /**
     * @return the Bastion Key Password in clear text
     * @throws IOException when something went wrong
     * @throws AESCryptException when something went wrong
     */
    @Transient
    public String getBastionKeyPassword() throws IOException, AESCryptException {
        if (bastionKeyPasswordEncrypted != null &&
                bastionKeyPasswordEncrypted.startsWith(CredentialsFactory.SALTED_MAGIC_B64)) {
            // password is encrypted
            String mpw = FileUtils.readFirstLineFromFile(Config.getDefaultMasterPasswordFile());
            return CryptHelper.aes256Decrypt(bastionKeyPasswordEncrypted, mpw);
        }
        return bastionKeyPasswordEncrypted;
    }

    /**
     * Set the Bastion Key Password and encrypt it
     * @param bastionKeyPasswordIn the plain password
     * @throws IOException when something went wrong
     * @throws AESCryptException when something went wrong
     */
    public void setBastionKeyPassword(String bastionKeyPasswordIn) throws IOException, AESCryptException {
        if (StringUtils.isEmpty(bastionKeyPasswordIn)) {
            bastionKeyPasswordEncrypted = bastionKeyPasswordIn;
            return;
        }
        String mpw = FileUtils.readFirstLineFromFile(Config.getDefaultMasterPasswordFile());
        bastionKeyPasswordEncrypted = CryptHelper.aes256Encrypt(bastionKeyPasswordIn, mpw);
    }

    public void setBastionKeyPasswordEncrypted(String bastionKeyPasswordEncryptedIn) {
        bastionKeyPasswordEncrypted = bastionKeyPasswordEncryptedIn;
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

    @OneToOne(fetch = FetchType.LAZY, cascade = CascadeType.ALL, mappedBy = "paygSshData")
    public Credentials getCredentials() {
        return credentials;
    }

    public void setCredentials(Credentials credentialsIn) {
        this.credentials = credentialsIn;
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

    /**
     * {@inheritDoc}
     */
    @Override
    public String toString() {
        ToStringBuilder builder = new ToStringBuilder(this)
                .append(host)
                .append(port)
                .append(username);
         if (!StringUtils.isBlank(bastionHost)) {
             builder.append(bastionHost)
                     .append(bastionPort)
                     .append(bastionUsername);
         }
         return builder.append(status).toString();
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
                .append(passwordEncrypted, that.passwordEncrypted)
                .append(keyEncrypted, that.keyEncrypted)
                .append(keyPasswordEncrypted, that.keyPasswordEncrypted)
                .append(bastionHost, that.bastionHost)
                .append(bastionPort, that.bastionPort)
                .append(bastionUsername, that.bastionUsername)
                .append(bastionPasswordEncrypted, that.bastionPasswordEncrypted)
                .append(bastionKeyEncrypted, that.bastionKeyEncrypted)
                .append(bastionKeyPasswordEncrypted, that.bastionKeyPasswordEncrypted)
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
                .append(passwordEncrypted)
                .append(keyEncrypted)
                .append(keyPasswordEncrypted)
                .append(bastionHost)
                .append(bastionPort)
                .append(bastionUsername)
                .append(bastionPasswordEncrypted)
                .append(bastionKeyEncrypted)
                .append(bastionKeyPasswordEncrypted)
                .append(status)
                .append(errorMessage)
                .toHashCode();
    }
}
