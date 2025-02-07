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

import com.suse.manager.model.hub.IssHub;

import org.apache.commons.lang3.builder.EqualsBuilder;
import org.apache.commons.lang3.builder.HashCodeBuilder;
import org.apache.commons.lang3.builder.ToStringBuilder;

import javax.persistence.DiscriminatorValue;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.OneToOne;
import javax.persistence.Transient;

@Entity
@DiscriminatorValue(CredentialsType.Label.SCC)
public class SCCCredentials extends RemoteCredentials {

    private IssHub issHub;

    // No args constructor for hibernate
    protected SCCCredentials() {
    }

    // Default constructor filling the mandatory fields to be used in the CredentialFactory
    protected SCCCredentials(String usernameIn, String passwordIn) {
        setUsername(usernameIn);
        setPassword(passwordIn);
    }

    @Override
    @Transient
    public CredentialsType getType() {
        return CredentialsType.SCC;
    }

    @OneToOne(mappedBy = "mirrorCredentials", fetch = FetchType.LAZY)
    public IssHub getIssHub() {
        return issHub;
    }

    public void setIssHub(IssHub issHubIn) {
        this.issHub = issHubIn;
    }

    /**
     * @return if this credential is the current primary scc credential which
     * is at the moment denoted by having the url field set.
     */
    @Transient
    public boolean isPrimary() {
        return getUrl() != null;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }

        if (!(o instanceof SCCCredentials that)) {
            return false;
        }
        return new EqualsBuilder()
                .appendSuper(super.equals(o))
                .append(getIssHub(), that.getIssHub())
                .isEquals();
    }

    @Override
    public int hashCode() {
        return new HashCodeBuilder(17, 37)
                .appendSuper(super.hashCode())
                .append(issHub)
                .toHashCode();
    }

    @Override
    public String toString() {
        return new ToStringBuilder(this)
            .append("id", getId())
            .append("type", CredentialsType.SCC)
            .append("user", getUser())
            .append("username", getUsername())
            .append("url", getUrl())
            .toString();
    }
}
