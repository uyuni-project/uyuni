/*
 * Copyright (c) 2023 SUSE LLC
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

package com.redhat.rhn.domain.credentials;

import com.redhat.rhn.domain.cloudpayg.PaygSshData;
import com.redhat.rhn.domain.cloudpayg.PaygSshDataFactory;

import org.apache.commons.codec.binary.Base64;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.builder.EqualsBuilder;
import org.apache.commons.lang3.builder.HashCodeBuilder;
import org.apache.commons.lang3.builder.ToStringBuilder;

import javax.persistence.Column;
import javax.persistence.DiscriminatorValue;
import javax.persistence.Entity;
import javax.persistence.Transient;

@Entity
@DiscriminatorValue(CredentialsType.Label.CLOUD_RMT)
public class CloudRMTCredentials extends RemoteCredentials implements CloudCredentials {

    private static final String INVALIDATED_PASSWORD = new String(Base64.encodeBase64("invalidated".getBytes()));
    private byte[] extraAuthData;
    private Long paygSshDataId;

    // No args constructor for hibernate
    protected CloudRMTCredentials() {
    }

    // Default constructor filling the mandatory fields to be used in the CredentialFactory
    protected CloudRMTCredentials(String usernameIn, String passwordIn, String urlIn) {
        this.setUsername(usernameIn);
        this.setPassword(passwordIn);
        this.setUrl(urlIn);
    }

    @Override
    @Transient
    public CredentialsType getType() {
        return CredentialsType.CLOUD_RMT;
    }

    @Column(name = "extra_auth")
    public byte[] getExtraAuthData() {
        return extraAuthData;
    }

    public void setExtraAuthData(byte[] extraAuthDataIn) {
        this.extraAuthData = extraAuthDataIn;
    }

    @Column(name = "payg_ssh_data_id")
    protected Long getPaygSshDataId() {
        return paygSshDataId;
    }

    protected void setPaygSshDataId(Long paygSshDataIdIn) {
        this.paygSshDataId = paygSshDataIdIn;
    }

    @Override
    @Transient
    public PaygSshData getPaygSshData() {
        if (paygSshDataId == null) {
            return null;
        }

        return PaygSshDataFactory.lookupById(paygSshDataId.intValue()).orElse(null);
    }

    @Override
    public void setPaygSshData(PaygSshData paygSshDataIn) {
        this.paygSshDataId = paygSshDataIn.getId();
    }

    /**
     * Credentials are considered as valid as soon as we have a user and a
     * password.
     *
     * @return true if we have a user and a password, else false
     */
    @Transient
    public boolean isComplete() {
        return !StringUtils.isEmpty(getUsername()) &&
                !StringUtils.isEmpty(getEncodedPassword());
    }

    /**
     * Marks the current credential as invalid
     */
    public void invalidate() {
        this.extraAuthData = "{}".getBytes();
        setEncodedPassword(INVALIDATED_PASSWORD);
    }

    /**
     * Check if this credential is valid
     * @return true if valid
     */
    @Transient
    @Override
    public boolean isValid() {
        return isComplete() && !INVALIDATED_PASSWORD.equals(getEncodedPassword());
    }

    /**
     * Check if these credentials are empty regarding username, password and
     * url.
     *
     * @return true if we have a user and a password, else false
     */
    @Transient
    public boolean isEmpty() {
        return StringUtils.isEmpty(getUsername()) &&
                StringUtils.isEmpty(getEncodedPassword()) &&
                StringUtils.isEmpty(getUrl());
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }

        if (!(o instanceof CloudRMTCredentials)) {
            return false;
        }

        CloudRMTCredentials that = (CloudRMTCredentials) o;

        return new EqualsBuilder()
            .appendSuper(super.equals(o))
            .append(extraAuthData, that.extraAuthData)
            .append(paygSshDataId, that.paygSshDataId)
            .isEquals();
    }

    @Override
    public int hashCode() {
        return new HashCodeBuilder(17, 37)
            .appendSuper(super.hashCode())
            .append(extraAuthData)
            .append(paygSshDataId)
            .toHashCode();
    }

    @Override
    public String toString() {
        return new ToStringBuilder(this)
            .append("id", getId())
            .append("type", CredentialsType.CLOUD_RMT)
            .append("user", getUser())
            .append("url", getUrl())
            .append("username", getUsername())
            .append("paygSshDataId", paygSshDataId)
            .toString();
    }
}
