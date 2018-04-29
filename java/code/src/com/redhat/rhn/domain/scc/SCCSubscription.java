/**
 * Copyright (c) 2015 SUSE LLC
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
package com.redhat.rhn.domain.scc;

import com.redhat.rhn.domain.BaseDomainHelper;
import com.redhat.rhn.domain.credentials.Credentials;
import com.redhat.rhn.domain.product.SUSEProduct;

import org.apache.commons.lang.builder.EqualsBuilder;
import org.apache.commons.lang.builder.HashCodeBuilder;
import org.apache.commons.lang.builder.ToStringBuilder;

import java.util.Date;
import java.util.Set;

/**
 * This is a subscription representation in the DB from SCC.
 */
public class SCCSubscription extends BaseDomainHelper {

    private long id;
    private long sccId;
    private transient Credentials credentials;

    private String name;
    private Date startsAt;
    private Date expiresAt;
    private String status;
    private String regcode;
    private String type;
    private Long systemLimit;
    private Set<SUSEProduct> products;

    /**
     * @return the id
     */
    public long getId() {
        return id;
    }

    /**
     * @return the sccId
     */
    public long getSccId() {
        return sccId;
    }

    /**
     * Get the mirror credentials.
     * @return the credentials
     */
    public Credentials getCredentials() {
        return credentials;
    }

    /**
     * @return the regcode
     */
    public String getRegcode() {
        return regcode;
    }

    /**
     * @return the name
     */
    public String getName() {
        return name;
    }

    /**
     * @return the type
     */
    public String getType() {
        return type;
    }

    /**
     * @return the status
     */
    public String getStatus() {
        return status;
    }

    /**
     * @return the startsAt
     */
    public Date getStartsAt() {
        return startsAt;
    }

    /**
     * @return the expiresAt
     */
    public Date getExpiresAt() {
        return expiresAt;
    }

    /**
     * @return the systemLimit
     */
    public Long getSystemLimit() {
        return systemLimit;
    }

    /**
     * Get the SUSE Products
     * @return the SUSE Products
     */
    public Set<SUSEProduct> getProducts() {
        return products;
    }

    // Setters

    /**
     * @param idIn the id to set
     */
    public void setId(long idIn) {
        this.id = idIn;
    }

    /**
     * @param sccIdIn the sccId to set
     */
    public void setSccId(long sccIdIn) {
        this.sccId = sccIdIn;
    }

    /**
     * Set the mirror credentials this repo can be retrieved with.
     * @param credentialsIn the credentials to set
     */
    public void setCredentials(Credentials credentialsIn) {
        this.credentials = credentialsIn;
    }

    /**
     * @param regcodeIn the regcode to set
     */
    public void setRegcode(String regcodeIn) {
        this.regcode = regcodeIn;
    }

    /**
     * @param nameIn the name to set
     */
    public void setName(String nameIn) {
        this.name = nameIn;
    }

    /**
     * @param typeIn the type to set
     */
    public void setType(String typeIn) {
        this.type = typeIn;
    }

    /**
     * @param statusIn the status to set
     */
    public void setStatus(String statusIn) {
        this.status = statusIn;
    }

    /**
     * @param startsAtIn the startsAt to set
     */
    public void setStartsAt(Date startsAtIn) {
        this.startsAt = startsAtIn;
    }

    /**
     * @param expiresAtIn the expiresAt to set
     */
    public void setExpiresAt(Date expiresAtIn) {
        this.expiresAt = expiresAtIn;
    }

    /**
     * @param systemLimitIn the systemLimit to set
     */
    public void setSystemLimit(Long systemLimitIn) {
        this.systemLimit = systemLimitIn;
    }

    /**
     * Set the SUSE Products
     * @param productsIn the SUSE Products
     */
    public void setProducts(Set<SUSEProduct> productsIn) {
        this.products = productsIn;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public boolean equals(Object other) {
        if (!(other instanceof SCCSubscription)) {
            return false;
        }
        SCCSubscription otherSCCSubscription = (SCCSubscription) other;
        return new EqualsBuilder()
                .append(getSccId(), otherSCCSubscription.getSccId())
                .append(getRegcode(), otherSCCSubscription.getRegcode())
                .append(getCredentials(), otherSCCSubscription.getCredentials())
                .isEquals();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public int hashCode() {
        return new HashCodeBuilder()
                .append(getSccId())
                .append(getRegcode())
                .append(getCredentials())
                .toHashCode();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public String toString() {
        return new ToStringBuilder(this)
        .append("sccId", getSccId())
        .append("name", getName())
        .append("regcode", getRegcode())
        .toString();
    }
}
