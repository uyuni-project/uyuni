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

package com.redhat.rhn.domain.rhnpackage;

import org.apache.commons.lang3.builder.EqualsBuilder;
import org.apache.commons.lang3.builder.HashCodeBuilder;

import java.io.Serial;
import java.io.Serializable;

public class PackageFileId implements Serializable {

    @Serial
    private static final long serialVersionUID = 9171993460580644717L;

    private Package pack;

    private PackageCapability capability;

    /**
     * Constructor
     */
    public PackageFileId() {
    }

    /**
     * Constructor
     *
     * @param packIn       the input pack
     * @param capabilityIn the input capability
     */
    public PackageFileId(Package packIn, PackageCapability capabilityIn) {
        pack = packIn;
        capability = capabilityIn;
    }

    public Package getPack() {
        return pack;
    }

    public void setPack(Package packIn) {
        pack = packIn;
    }

    public PackageCapability getCapability() {
        return capability;
    }

    public void setCapability(PackageCapability capabilityIn) {
        capability = capabilityIn;
    }

    @Override
    public boolean equals(Object oIn) {
        if (!(oIn instanceof PackageFileId that)) {
            return false;
        }

        return new EqualsBuilder()
                .append(pack, that.pack)
                .append(capability, that.capability)
                .isEquals();
    }

    @Override
    public int hashCode() {
        return new HashCodeBuilder()
                .append(pack)
                .append(capability)
                .toHashCode();
    }

    @Override
    public String toString() {
        return "PackageFileId{" +
                "pack=" + pack +
                ", capability=" + capability +
                '}';
    }
}
