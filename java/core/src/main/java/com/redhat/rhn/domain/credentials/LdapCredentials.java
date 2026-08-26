/*
 * Copyright (c) 2026 SUSE LLC
 *
 * This software is licensed to you under the GNU General Public License,
 * version 2 (GPLv2). There is NO WARRANTY for this software, express or
 * implied, including the implied warranties of MERCHANTABILITY or FITNESS
 * FOR A PARTICULAR PURPOSE. You should have received a copy of GPLv2
 * along with this software; if not, see
 * http://www.gnu.org/licenses/old-licenses/gpl-2.0.txt.
 */

package com.redhat.rhn.domain.credentials;

import org.apache.commons.lang3.builder.ToStringBuilder;

import jakarta.persistence.DiscriminatorValue;
import jakarta.persistence.Entity;
import jakarta.persistence.Transient;

/**
 * Service-account password used to bind to a directory server.
 *
 * <p>Only the password is stored here. The matching bind DN lives on the LDAP server record
 * because {@code suseCredentials.username} is too short to hold a directory DN.</p>
 */
@Entity
@DiscriminatorValue(CredentialsType.Label.LDAP)
public class LdapCredentials extends PasswordBasedCredentials {

    // No args constructor for hibernate
    protected LdapCredentials() {
    }

    // Default constructor filling the mandatory fields to be used in the CredentialsFactory
    protected LdapCredentials(String passwordIn) {
        setPassword(passwordIn);
    }

    @Override
    @Transient
    public CredentialsType getType() {
        return CredentialsType.LDAP;
    }

    @Override
    public String toString() {
        return new ToStringBuilder(this)
            .append("id", getId())
            .append("type", CredentialsType.LDAP)
            .toString();
    }
}
