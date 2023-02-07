/*
 * Copyright (c) 2009--2012 Red Hat, Inc.
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

import com.redhat.rhn.domain.BaseDomainHelper;
import com.redhat.rhn.manager.rhnpackage.PackageManager;

import org.apache.commons.lang3.builder.EqualsBuilder;
import org.apache.commons.lang3.builder.HashCodeBuilder;

import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.IdClass;
import javax.persistence.Inheritance;
import javax.persistence.InheritanceType;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;

/**
 * PackageProperty
 */
@Entity
@Inheritance(strategy = InheritanceType.TABLE_PER_CLASS)
@IdClass(PackagePropertyKey.class)
public class PackageProperty extends BaseDomainHelper {

    @Id
    @ManyToOne
    @JoinColumn(name = "package_id")
    private Package pack;

    @Id
    @ManyToOne
    @JoinColumn(name = "capability_id")
    private PackageCapability capability;

    private Long sense;

    /**
     * @return Returns the pack.
     */
    public Package getPack() {
        return pack;
    }

    /**
     * @param packIn The pack to set.
     */
    public void setPack(Package packIn) {
        this.pack = packIn;
    }

    /**
     * @return Returns the capability.
     */
    public PackageCapability getCapability() {
        return capability;
    }

    /**
     * @param capabilityIn The capability to set.
     */
    public void setCapability(PackageCapability capabilityIn) {
        this.capability = capabilityIn;
    }

    /**
     * @return Returns the sense.
     */
    public Long getSense() {
        return sense;
    }

    /**
     * @param senseIn The sense to set.
     */
    public void setSense(Long senseIn) {
        this.sense = senseIn;
    }

    /**
     *
     * {@inheritDoc}
     */
    @Override
    public int hashCode() {
        HashCodeBuilder hash = new HashCodeBuilder();
        hash.append(this.getSense());
        hash.append(this.getCapability());
        hash.append(this.getPack());
        return hash.toHashCode();
    }

    /**
     *
     * {@inheritDoc}
     */
    @Override
    public boolean equals(Object obj) {
        if (!(obj instanceof PackageProperty)) {
            return false;
        }
        PackageProperty prop = (PackageProperty) obj;
        EqualsBuilder eq = new EqualsBuilder();
        eq.append(this.getSense(), prop.getSense());
        eq.append(this.getPack(), prop.getPack());
        eq.append(this.getCapability(), prop.getCapability());
        return eq.isEquals();
    }

    @Override
    public String toString() {
        if (capability.getVersion() != null) {
            return capability.getName() + "  " + PackageManager.getDependencyModifier(sense, capability.getVersion());
        }

        return capability.getName();
    }
}
