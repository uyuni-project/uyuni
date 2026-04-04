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
 * Red Hat trademarks are not licensed under GPLv2. No permission is
 * granted to use or replicate Red Hat trademarks that are incorporated
 * in this software or its documentation.
 */
package com.redhat.rhn.domain.kickstart;

import com.redhat.rhn.domain.BaseDomainHelper;
import com.redhat.rhn.domain.rhnpackage.PackageName;

import org.apache.commons.lang3.builder.HashCodeBuilder;

import java.io.Serializable;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.IdClass;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;

/**
 * KickstartPackage
 */
@Entity
@Table(name = "rhnKickstartPackage")
@IdClass(KickstartPackageId.class)
public class KickstartPackage extends BaseDomainHelper implements Serializable, Comparable<KickstartPackage> {

    private static final long serialVersionUID = 1L;

    @Column(nullable = false)
    private Long position;

    @Id
    @ManyToOne(targetEntity = KickstartData.class)
    @JoinColumn(name = "kickstart_id")
    private KickstartData ksData;

    @Id
    @ManyToOne(targetEntity = PackageName.class)
    @JoinColumn(name = "package_name_id")
    private PackageName packageName;

    /**
     *
     */
    public KickstartPackage() {
        super();
    }

    /**
     * @param ksDataIn identifies kickstart
     * @param packageNameIn PackageName to be associated
     */
    public KickstartPackage(KickstartData ksDataIn, PackageName packageNameIn) {
        super();
        this.ksData = ksDataIn;
        this.packageName = packageNameIn;
        this.position = 0L;
    }


    /**
     * @param ksDataIn identifies kickstart
     * @param packageNameIn PackageName to be associated
     * @param posIn position the package Name is in the kickstart package list
     */
    public KickstartPackage(KickstartData ksDataIn, PackageName packageNameIn, Long posIn) {
        this(ksDataIn, packageNameIn);
        this.position = posIn;
    }


    /**
     * @return Returns the position.
     */
    public Long getPosition() {
        return position;
    }

    /**
     * @param positionIn The position to set.
     */
    public void setPosition(Long positionIn) {
        this.position = positionIn;
    }

    /**
     * @return Returns the ksdata.
     */
    public KickstartData getKsData() {
        return ksData;
    }

    /**
     * @param ksdata The ksdata to set.
     */
    public void setKsData(KickstartData ksdata) {
        this.ksData = ksdata;
    }

    /**
     * @return Returns the packageName.
     */
    public PackageName getPackageName() {
        return packageName;
    }

    /**
     * @param pn The packageName to set.
     */
    public void setPackageName(PackageName pn) {
        this.packageName = pn;
    }

    /**
     * @param that KickstartPackage to be compared
     * @return -1,0,1 for sort algo
     */
    @Override
    public int compareTo(KickstartPackage that) {

        final int equal = 0;

        if (this.equals(that)) {
            return equal;
        }

        int comparism = this.getKsData().getLabel().compareTo(that.getKsData().getLabel());
        if (equal != comparism) {
            return comparism;
        }

        comparism = this.getPosition().compareTo(that.getPosition());
        if (equal != comparism) {
            return comparism;
        }
        return this.getPackageName().compareTo(that.getPackageName());
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public boolean equals(final Object other) {
        if (!(other instanceof KickstartPackage that)) {
            return false;
        }
        return this.hashCode() == that.hashCode();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public int hashCode() {
        return new HashCodeBuilder()
            .append(getKsData().getId())
            .append(getPosition())
            .append(getPackageName())
            .toHashCode();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public String toString() {
        return "{ " + this.getKsData().getId().toString() +
               ", " + this.getPosition().toString() +
               ", " + this.getPackageName().getName() + " }";
    }

    /**
     * Produce a clone of a kickstartPackage object
     * @param data The new kickstart data
     * @return the clone
     */
    public KickstartPackage deepCopy(KickstartData data) {
        KickstartPackage kp = new KickstartPackage();
        kp.setKsData(data);
        kp.setPackageName(this.getPackageName());
        kp.setPosition(this.getPosition());
        return kp;
    }
}
