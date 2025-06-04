/*
 * Copyright (c) 2025 SUSE LLC
 *
 * This software is licensed to you under the GNU General Public License,
 * version 2 (GPLv2). There is NO WARRANTY for this software, express or
 * implied, including the implied warranties of MERCHANTABILITY or FITNESS
 * FOR A PARTICULAR PURPOSE. You should have received a copy of GPLv2
 * along with this software; if not, see
 * http://www.gnu.org/licenses/old-licenses/gpl-2.0.txt.
 */

package com.redhat.rhn.domain.credentials;

import com.suse.manager.model.hub.IssPeripheral;

import org.apache.commons.lang3.builder.EqualsBuilder;
import org.apache.commons.lang3.builder.HashCodeBuilder;
import org.apache.commons.lang3.builder.ToStringBuilder;

import javax.persistence.Column;
import javax.persistence.DiscriminatorValue;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.OneToOne;
import javax.persistence.Transient;

@Entity
@DiscriminatorValue(CredentialsType.Label.HUB_SCC)
public class HubSCCCredentials extends PasswordBasedCredentials {


    private IssPeripheral issPeripheral;
    private String peripheralUrl;

    // No args constructor for hibernate
    protected HubSCCCredentials() {
    }

    // Default constructor filling the mandatory fields to be used in the CredentialFactory
    protected HubSCCCredentials(String usernameIn, String passwordIn, String peripheralUrlIn) {
        setUsername(usernameIn);
        setPassword(passwordIn);
        this.peripheralUrl = peripheralUrlIn;
    }

    @OneToOne(mappedBy = "mirrorCredentials", fetch = FetchType.LAZY)
    public IssPeripheral getIssPeripheral() {
        return issPeripheral;
    }

    public void setIssPeripheral(IssPeripheral issPeripheralIn) {
        this.issPeripheral = issPeripheralIn;
    }

    @Override
    @Transient
    public CredentialsType getType() {
        return CredentialsType.HUB_SCC;
    }

    @Column(name = "url")
    public String getPeripheralUrl() {
        return peripheralUrl;
    }

    public void setPeripheralUrl(String peripheralUrlIn) {
        this.peripheralUrl = peripheralUrlIn;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }

        if (!(o instanceof HubSCCCredentials that)) {
            return false;
        }

        return new EqualsBuilder()
            .appendSuper(super.equals(o))
            .append(getPeripheralUrl(), that.getPeripheralUrl())
            .isEquals();
    }

    @Override
    public int hashCode() {
        return new HashCodeBuilder(17, 37)
            .appendSuper(super.hashCode())
            .append(getPeripheralUrl())
            .toHashCode();
    }

    @Override
    public String toString() {
        return new ToStringBuilder(this)
            .append("id", getId())
            .append("type", CredentialsType.HUB_SCC)
            .append("user", getUser())
            .append("username", getUsername())
            .append("url", getPeripheralUrl())
            .toString();
    }
}
