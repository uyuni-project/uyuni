/*
 * Copyright (c) 2025 SUSE LLC
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


import java.io.Serial;
import java.io.Serializable;

import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GenerationType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.SequenceGenerator;
import jakarta.persistence.Table;

/**
 * PackageName
 */
@Entity
@Table(name = "rhnPackageNevra")
public class PackageNevra implements Serializable {

    @Serial
    private static final long serialVersionUID = 5428733207958125122L;

    @Id
	@GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "pkgnevra_seq")
	@SequenceGenerator(name = "pkgnevra_seq", sequenceName = "rhn_pkgnevra_id_seq", allocationSize = 1)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "name_id")
    private PackageName name;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "evr_id")
    private PackageEvr evr;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "package_arch_id")
    private PackageArch arch;

    /**
     * @return Returns the arch.
     */
    public PackageArch getArch() {
        return arch;
    }

    /**
     * @param archIn The arch to set.
     */
    public void setArch(PackageArch archIn) {
        this.arch = archIn;
    }

    /**
     * @return Returns the evr.
     */
    public PackageEvr getEvr() {
        return evr;
    }

    /**
     * @param evrIn The evr to set.
     */
    public void setEvr(PackageEvr evrIn) {
        this.evr = evrIn;
    }

    /**
     * @return Returns the id.
     */
    public Long getId() {
        return id;
    }

    /**
     * @param idIn The id to set.
     */
    protected void setId(Long idIn) {
        this.id = idIn;
    }

    /**
     * @return Returns the name.
     */
    public PackageName getName() {
        return name;
    }

    /**
     * @param nameIn The name to set.
     */
    public void setName(PackageName nameIn) {
        this.name = nameIn;
    }

}
