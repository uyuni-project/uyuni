/*
 * Copyright (c) 2012 SUSE LLC
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

import com.redhat.rhn.domain.BaseDomainHelper;
import com.redhat.rhn.domain.cloudpayg.PaygSshData;
import com.redhat.rhn.domain.user.User;

import org.apache.commons.codec.binary.Base64;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.builder.EqualsBuilder;
import org.apache.commons.lang3.builder.HashCodeBuilder;
import org.apache.commons.lang3.builder.ToStringBuilder;

/**
 * Credentials - Java representation of the table SUSECREDENTIALS.
 *
 * This table contains pairs of credentials used for communicating
 * with 3rd party systems, e.g. API usernames and keys.
 */
public class Credentials extends BaseDomainHelper {

    // Available type labels
    public static final String TYPE_SCC = "scc";
    public static final String TYPE_VIRT_HOST_MANAGER = "vhm";
    public static final String TYPE_REGISTRY = "registrycreds";
    public static final String TYPE_CLOUD_RMT = "cloudrmt";
    public static final String TYPE_REPORT_CREDS = "reportcreds";

    private Long id;
    private User user;
    private CredentialsType type;
    private String url;
    private byte[] extraAuthData;
    private String username;
    private String encodedPassword;

    private PaygSshData paygSshData;

    /**
     * Get the ID of this object.
     * @return id
     */
    public Long getId() {
        return this.id;
    }

    /**
     * Set the ID of this object.
     * @param idIn id
     */
    public void setId(Long idIn) {
        this.id = idIn;
    }

    /**
     * Get the associated {@link User}.
     * @return user
     */
    public User getUser() {
        return this.user;
    }

    /**
     * Set the associated {@link User}.
     * @param userIn user
     */
    public void setUser(User userIn) {
        this.user = userIn;
    }

    /**
     * Return the type.
     * @return type
     */
    public CredentialsType getType() {
        return type;
    }

    /**
     * Set the type.
     * @param typeIn type
     */
    public void setType(CredentialsType typeIn) {
        this.type = typeIn;
    }

    /**
     * Return the URL.
     * @return url
     */
    public String getUrl() {
        return url;
    }

    /**
     * Set the url.
     * @param urlIn url
     */
    public void setUrl(String urlIn) {
        this.url = urlIn;
    }

    /**
     * Return the username
     * @return username
     */
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

    /**
     * Return the encoded password.
     * @return the password
     */
    public String getEncodedPassword() {
        return encodedPassword;
    }

    /**
     * Set the password.
     * @param password the password to set
     */
    public void setEncodedPassword(String password) {
        this.encodedPassword = password;
    }

    /**
     * Return the decoded password.
     * @return the password
     */
    public String getPassword() {
        if (this.encodedPassword != null) {
            return new String(Base64.decodeBase64(this.encodedPassword.getBytes()));
        }
        return this.encodedPassword;
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

    public byte[] getExtraAuthData() {
        return extraAuthData;
    }

    public void setExtraAuthData(byte[] extraAuthDataIn) {
        this.extraAuthData = extraAuthDataIn;
    }

    public PaygSshData getPaygSshData() {
        return paygSshData;
    }

    public void setPaygSshData(PaygSshData paygSshDataIn) {
        this.paygSshData = paygSshDataIn;
    }

    /**
     * Credentials are considered as valid as soon as we have a user and a
     * password.
     *
     * @return true if we have a user and a password, else false
     */
    public boolean isComplete() {
        return !StringUtils.isEmpty(username) &&
                !StringUtils.isEmpty(encodedPassword);
    }

    /**
     * Check if these credentials are empty regarding username, password and
     * url.
     *
     * @return true if we have a user and a password, else false
     */
    public boolean isEmpty() {
        return StringUtils.isEmpty(username) &&
                StringUtils.isEmpty(encodedPassword) &&
                StringUtils.isEmpty(url);
    }

    /**
     * @return if this credential is the current primary scc credential which
     * is at the moment denoted by having the url field set.
     */
    public boolean isPrimarySCCCredential() {
        return type.getLabel().equals(TYPE_SCC) && url != null;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public boolean equals(Object other) {
        if (!(other instanceof Credentials)) {
            return false;
        }
        Credentials otherCredentials = (Credentials) other;
        return new EqualsBuilder()
            .append(getType(), otherCredentials.getType())
            .append(getUsername(), otherCredentials.getUsername())
            .append(getPassword(), otherCredentials.getPassword())
            .isEquals();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public int hashCode() {
        return new HashCodeBuilder()
            .append(getType())
            .append(getUsername())
            .append(getPassword())
            .toHashCode();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public String toString() {
        return new ToStringBuilder(this)
            .append("id", getId())
            .append("username", getUsername())
            .toString();
    }
}
