/*
 * Copyright (c) 2009--2014 Red Hat, Inc.
 *
 * This software is licensed to you under the GNU General Public License,
 * version 2 (GPLv2). There is NO WARRANTY for this software, express or
 * implied, including the implied warranties of MERCHANTABILITY or FITNESS
 * FOR A PARTICULAR PURPOSE. You should have received a copy of GPLv2
 * along with this software; if not, see
 * http://www.gnu.org/licenses/old-licenses/gpl-2.0.txt.
 *
 * SPDX-License-Identifier: GPL-2.0-only
 *
 * Red Hat trademarks are not licensed under GPLv2. No permission is
 * granted to use or replicate Red Hat trademarks that are incorporated
 * in this software or its documentation.
 */
package com.redhat.rhn.domain.kickstart;

import com.redhat.rhn.domain.BaseDomainHelper;
import com.redhat.rhn.domain.org.Org;
import com.redhat.rhn.manager.kickstart.IpAddress;

import org.apache.commons.lang3.builder.EqualsBuilder;
import org.apache.commons.lang3.builder.HashCodeBuilder;

import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.IdClass;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.Table;

/**
 * KickstartIpRange - Class representation of the table rhnkickstartiprange.
 */
@Entity
@Table(name = "rhnKickstartIpRange")
@IdClass(KickstartIpRangeId.class)
public class KickstartIpRange extends BaseDomainHelper {

    private static final long serialVersionUID = 1L;

    @Id
    @ManyToOne(targetEntity = KickstartData.class)
    @JoinColumn(name = "kickstart_id")
    private KickstartData ksdata;

    @Id
    @ManyToOne(targetEntity = Org.class)
    @JoinColumn(name = "org_id")
    private Org org;

    @Id
    private Long min;

    @Id
    private Long max;

    /**
     * Getter for kickstartId
     * @return KickstartData to get
    */
    public KickstartData getKsdata() {
        return this.ksdata;
    }

    /**
     * Setter for ksdata
     * @param ksdataIn to set
    */
    public void setKsdata(KickstartData ksdataIn) {
        this.ksdata = ksdataIn;
    }

    /**
     * Getter for org
     * @return Org to get
    */
    public Org getOrg() {
        return this.org;
    }

    /**
     * Setter for org
     * @param orgIn to set
    */
    public void setOrg(Org orgIn) {
        this.org = orgIn;
    }

    /**
     * Getter for min
     * @return Long to get
    */
    public Long getMin() {
        return this.min;
    }

    /**
     * Setter for min
     * @param minIn to set
    */
    public void setMin(Long minIn) {
        this.min = minIn;
    }

    /**
     * Getter for max
     * @return Long to get
    */
    public Long getMax() {
        return this.max;
    }

    /**
     * Setter for max
     * @param maxIn to set
    */
    public void setMax(Long maxIn) {
        this.max = maxIn;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public boolean equals(final Object other) {
        if (!(other instanceof KickstartIpRange castOther)) {
            return false;
        }
        return new EqualsBuilder().append(ksdata, castOther.ksdata)
                                  .append(org, castOther.org)
                                  .append(min, castOther.min)
                                  .append(max, castOther.max)
                                  .isEquals();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public int hashCode() {
        return new HashCodeBuilder().append(ksdata)
                                    .append(org)
                                    .append(min)
                                    .append(max)
                                    .toHashCode();
    }

    /**
     * gets the string representation of the max ip (i.e. "192.168.0.1")
     * @return the ip as a string
     */
    public String getMaxString() {
        IpAddress ip = new IpAddress(max);
        return ip.toString();
    }

    /**
     * gets the string representation of the min ip (i.e. "192.168.0.1")
     * @return the ip as a string
     */
    public String getMinString() {
        IpAddress ip = new IpAddress(min);
        return ip.toString();
    }

    /**
     * Sets the max ip
     * @param maxStr the max ip in string format ("192.168.0.1")
     */
    public void setMaxString(String maxStr) {
        IpAddress ip = new IpAddress(maxStr);
        max = ip.getNumber();
    }

    /**
     * Sets the min ip
     * @param minStr the min ip in string format ("192.168.0.1")
     */
    public void setMinString(String minStr) {
        IpAddress ip = new IpAddress(minStr);
        min = ip.getNumber();
    }

}
