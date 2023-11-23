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

import org.apache.commons.lang3.builder.EqualsBuilder;
import org.apache.commons.lang3.builder.HashCodeBuilder;
import org.apache.commons.lang3.builder.ToStringBuilder;

import javax.persistence.Column;
import javax.persistence.DiscriminatorValue;
import javax.persistence.Entity;
import javax.persistence.Transient;

@Entity
@DiscriminatorValue(CredentialsType.Label.RHUI)
public class RHUICredentials extends BaseCredentials implements CloudCredentials {

    protected RHUICredentials() {
    }

    private byte[] extraAuthData;
    private Long paygSshDataId;

    @Override
    @Transient
    public CredentialsType getType() {
        return CredentialsType.RHUI;
    }

    @Column(name = "extra_auth")
    @Override
    public byte[] getExtraAuthData() {
        return extraAuthData;
    }

    @Override
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
    @Transient
    public void setPaygSshData(PaygSshData paygSshDataIn) {
        this.paygSshDataId = paygSshDataIn != null ? paygSshDataIn.getId() : null;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }

        if (!(o instanceof RHUICredentials)) {
            return false;
        }

        RHUICredentials that = (RHUICredentials) o;

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
            .append("user", getUser())
            .append("type", getType())
            .append("paygSshDataId", paygSshDataId)
            .toString();
    }
}
