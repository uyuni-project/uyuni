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

package com.suse.oval.db;

import com.redhat.rhn.domain.errata.Cve;

import org.apache.commons.lang3.builder.EqualsBuilder;
import org.apache.commons.lang3.builder.HashCodeBuilder;

import java.io.Serializable;

public class OVALPlatformVulnerablePackageKey implements Serializable {
    private OVALPlatform platform;
    private Cve cve;
    private OVALVulnerablePackage vulnerablePackage;

    public OVALPlatform getPlatform() {
        return platform;
    }

    public void setPlatform(OVALPlatform platformIn) {
        this.platform = platformIn;
    }

    public Cve getCve() {
        return cve;
    }

    public void setCve(Cve cveIn) {
        this.cve = cveIn;
    }

    public OVALVulnerablePackage getVulnerablePackage() {
        return vulnerablePackage;
    }

    public void setVulnerablePackage(OVALVulnerablePackage vulnerablePackageIn) {
        this.vulnerablePackage = vulnerablePackageIn;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public int hashCode() {
        HashCodeBuilder builder = new HashCodeBuilder()
                .append(platform.getId())
                .append(cve.getId())
                .append(vulnerablePackage.getId());
        return builder.toHashCode();

    }

    /**
     * {@inheritDoc}
     */
    @Override
    public boolean equals(Object other) {
        if (other instanceof OVALPlatformVulnerablePackageKey) {
            OVALPlatformVulnerablePackageKey otherKey = (OVALPlatformVulnerablePackageKey) other;
            return new EqualsBuilder()
                    .append(this.getPlatform(), otherKey.getPlatform())
                    .append(this.getCve(), otherKey.getCve())
                    .append(this.getVulnerablePackage(), otherKey.getVulnerablePackage())
                    .isEquals();
        }
        else {
            return false;
        }
    }
}
