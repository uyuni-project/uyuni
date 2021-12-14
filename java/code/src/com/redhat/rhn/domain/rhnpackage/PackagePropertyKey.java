/*
 * Copyright (c) 2016 SUSE LLC
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
package com.redhat.rhn.domain.rhnpackage;

import org.apache.commons.lang3.builder.EqualsBuilder;
import org.apache.commons.lang3.builder.HashCodeBuilder;

import java.io.Serializable;

/**
 * Primary key class for PackageProperty.
 */
public class PackagePropertyKey implements Serializable {

    private Package pack;
    private PackageCapability capability;

    /**
     * @return pack to get
     */
    public Package getPack() {
        return pack;
    }

    public void setPack(Package packIn) {
        this.pack = packIn;
    }

    /**
     * @return capability to get
     */
    public PackageCapability getCapability() {
        return capability;
    }

    public void setCapability(PackageCapability capabilityIn) {
        this.capability = capabilityIn;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }

        if (o == null || getClass() != o.getClass()) {
            return false;
        }

        PackagePropertyKey that = (PackagePropertyKey) o;

        return new EqualsBuilder()
                .append(pack, that.pack)
                .append(capability, that.capability)
                .isEquals();
    }

    @Override
    public int hashCode() {
        return new HashCodeBuilder(17, 37)
                .append(pack)
                .append(capability)
                .toHashCode();
    }
}
