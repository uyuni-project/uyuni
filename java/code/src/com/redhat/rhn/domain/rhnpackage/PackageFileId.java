/*
 * Copyright (c) 2024 SUSE LLC
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

import java.io.Serializable;
import java.util.Objects;

import javax.persistence.Embeddable;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.MappedSuperclass;

@MappedSuperclass
@Embeddable
public class PackageFileId implements Serializable {

    @ManyToOne
    @JoinColumn(name = "package_id", insertable = false, updatable = false)
    private Package pack; // The package_id field
    @ManyToOne
    @JoinColumn(name = "capability_id", insertable = false, updatable = false)
    private PackageCapability capability; // The capability_id field

    /**
     * Default Constructor.
     */
    public PackageFileId() {

    }

    /**
     * Constructor with parameters.
     * @param packIn package
     * @param capabilityIn package capability
     */
    public PackageFileId(Package packIn, PackageCapability capabilityIn) {
        pack = packIn;
        capability = capabilityIn;
    }

    // Getters and setters
    public Package getPack() {
        return pack;
    }

    public void setPack(Package packIn) {
        this.pack = packIn;
    }

    public PackageCapability getCapability() {
        return capability;
    }

    public void setCapability(PackageCapability capabilityIn) {
        this.capability = capabilityIn;
    }

    // Override equals and hashCode for composite keys (essential for Hibernate)
    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }
        PackageFileId that = (PackageFileId) o;
        return Objects.equals(pack, that.pack) && Objects.equals(capability, that.capability);
    }

    @Override
    public int hashCode() {
        return Objects.hash(pack, capability);
    }
}
