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

import org.apache.commons.codec.binary.Base64;
import org.apache.commons.lang3.builder.EqualsBuilder;
import org.apache.commons.lang3.builder.HashCodeBuilder;

import javax.persistence.Column;
import javax.persistence.MappedSuperclass;
import javax.persistence.Transient;

@MappedSuperclass
public abstract class PasswordBasedCredentials extends BaseCredentials {

    private String username;
    private String encodedPassword;

    /**
     * Return the decoded password.
     * @return the password
     */
    @Transient
    public String getPassword() {
        if (this.encodedPassword != null) {
            return new String(Base64.decodeBase64(this.encodedPassword.getBytes()));
        }
        return null;
    }

    @Column(name = "password")
    protected String getEncodedPassword() {
        return this.encodedPassword;
    }

    public void setEncodedPassword(String encodedPasswordIn) {
        this.encodedPassword = encodedPasswordIn;
    }

    /**
     * Set the password after encoding it to Base64.
     * @param password the password to set
     */
    public void setPassword(String password) {
        if (password != null) {
            this.encodedPassword = new String(Base64.encodeBase64(password.getBytes()));
        }
        else {
            this.encodedPassword = null;
        }
    }

    /**
     * Return the username
     * @return username
     */
    @Column(name = "username")
    public String getUsername() {
        return username;
    }

    /**
     * Set the username.
     * @param usernameIn username
     */
    public void setUsername(String usernameIn) {
        this.username = usernameIn;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }

        if (!(o instanceof PasswordBasedCredentials)) {
            return false;
        }

        PasswordBasedCredentials that = (PasswordBasedCredentials) o;

        return new EqualsBuilder()
            .appendSuper(super.equals(o))
            .append(username, that.username)
            .append(encodedPassword, that.encodedPassword)
            .isEquals();
    }

    @Override
    public int hashCode() {
        return new HashCodeBuilder(17, 37)
            .appendSuper(super.hashCode())
            .append(username)
            .append(encodedPassword)
            .toHashCode();
    }
}
