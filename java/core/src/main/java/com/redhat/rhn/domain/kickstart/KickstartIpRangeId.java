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

package com.redhat.rhn.domain.kickstart;

import com.redhat.rhn.domain.org.Org;

import org.apache.commons.lang3.builder.EqualsBuilder;
import org.apache.commons.lang3.builder.HashCodeBuilder;

import java.io.Serial;
import java.io.Serializable;

public class KickstartIpRangeId implements Serializable {

    @Serial
    private static final long serialVersionUID = 8838755484611464865L;

    private KickstartData ksdata;

    private Org org;

    private long min;

    private long max;

    /**
     * Constructor
     */
    public KickstartIpRangeId() {
    }

    /**
     * Constructor
     *
     * @param ksdataIn the input ksdata
     * @param orgIn    the input org
     * @param minIn    the input min
     * @param maxIn    the input max
     */
    public KickstartIpRangeId(KickstartData ksdataIn, Org orgIn, long minIn, long maxIn) {
        ksdata = ksdataIn;
        org = orgIn;
        min = minIn;
        max = maxIn;
    }

    public KickstartData getKsdata() {
        return ksdata;
    }

    public void setKsdata(KickstartData ksdataIn) {
        ksdata = ksdataIn;
    }

    public Org getOrg() {
        return org;
    }

    public void setOrg(Org orgIn) {
        org = orgIn;
    }

    public long getMin() {
        return min;
    }

    public void setMin(long minIn) {
        min = minIn;
    }

    public long getMax() {
        return max;
    }

    public void setMax(long maxIn) {
        max = maxIn;
    }

    @Override
    public boolean equals(Object oIn) {
        if (!(oIn instanceof KickstartIpRangeId that)) {
            return false;
        }

        return new EqualsBuilder()
                .append(ksdata, that.ksdata)
                .append(org, that.org)
                .append(min, that.min)
                .append(max, that.max)
                .isEquals();
    }

    @Override
    public int hashCode() {
        return new HashCodeBuilder()
                .append(ksdata)
                .append(org)
                .append(min)
                .append(max)
                .toHashCode();
    }

    @Override
    public String toString() {
        return "KickstartIpRangeId{" +
                "ksdata=" + ksdata +
                ", org=" + org +
                ", min=" + min +
                ", max=" + max +
                '}';
    }
}
