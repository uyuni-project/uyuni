/*
 * Copyright (c) 2009--2010 Red Hat, Inc.
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

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.Table;

@Entity
@Table(name = "rhnPackageProvides")

/**
 * PackageArch
 */
public class PackageProvides extends PackageProperty implements Serializable {

    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "package_id", insertable = false, updatable = false, nullable = false)
    private Package pack;

    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "capability_id", insertable = false, updatable = false, nullable = false)
    private PackageCapability capability;


    @Column(name = "sense", nullable = false)
    private Long sense;

    /**
     * Default Constructor.
     */
    public PackageProvides() {
    }

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

    public Long getSense() {
        return sense;
    }

    public void setSense(Long senseIn) {
        this.sense = senseIn;
    }
}
