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

import org.apache.commons.lang3.builder.ToStringBuilder;

import javax.persistence.DiscriminatorValue;
import javax.persistence.Entity;
import javax.persistence.Transient;

@Entity
@DiscriminatorValue(CredentialsType.Label.SCC)
public class SCCCredentials extends RemoteCredentials {

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

    /**
     * @return if this credential is the current primary scc credential which
     * is at the moment denoted by having the url field set.
     */
    @Transient
    public boolean isPrimary() {
        return getUrl() != null;
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
