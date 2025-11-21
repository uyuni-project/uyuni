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
package com.redhat.rhn.domain.action.rhnpackage;

import com.redhat.rhn.domain.BaseDomainHelper;
import com.redhat.rhn.domain.action.Action;
import com.redhat.rhn.domain.rhnpackage.PackageArch;
import com.redhat.rhn.domain.rhnpackage.PackageEvr;
import com.redhat.rhn.domain.rhnpackage.PackageName;

import org.apache.commons.lang3.builder.EqualsBuilder;

import java.util.HashSet;
import java.util.Set;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.SequenceGenerator;
import javax.persistence.Table;
import javax.persistence.Transient;

/**
 * PackageActionDetails
 */
@Entity
@Table(name = "rhnActionPackage")
public class PackageActionDetails extends BaseDomainHelper {

    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "package_action_seq")
    @SequenceGenerator(name = "package_action_seq", sequenceName = "RHN_ACT_P_ID_SEQ", allocationSize = 1)
    @Column(name = "id")
    private Long packageId;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "name_id")
    private PackageName packageName;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "evr_id")
    private PackageEvr evr;

    @Column(name = "parameter")
    private String parameter;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "package_arch_id")
    private PackageArch arch;

    @Transient
    private Set<PackageActionResult> results = new HashSet<>();

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "action_id", nullable = false)
    private Action parentAction;

    /**
     * @param resultsIn The results to set.
     */
    public void setResults(Set<PackageActionResult> resultsIn) {
        this.results = resultsIn;
    }

    /**
     * @return Returns the results.
     */
    public Set<PackageActionResult> getResults() {
        return results;
    }

    /**
     * @param r The result to add.
     */
    public void addResult(PackageActionResult r) {
        r.setDetails(this);
        results.add(r);
    }
    /**
     * @return Returns the arch.
     */
    public PackageArch getArch() {
        return arch;
    }

    /**
     * @param a The arch to set.
     */
    public void setArch(PackageArch a) {
        this.arch = a;
    }

    /**
     * @return Returns the evr.
     */
    public PackageEvr getEvr() {
        return evr;
    }

    /**
     * @param e The evr to set.
     */
    public void setEvr(PackageEvr e) {
        this.evr = e;
    }

    /**
     * @return Returns the id.
     */
    public Long getPackageId() {
        return packageId;
    }

    /**
     * @param i The id to set.
     */
    public void setPackageId(Long i) {
        this.packageId = i;
    }

    /**
     * @return Returns the packageName.
     */
    public PackageName getPackageName() {
        return packageName;
    }

    /**
     * @param n The packageName to set.
     */
    public void setPackageName(PackageName n) {
        this.packageName = n;
    }

    /**
     * @return Returns the parameter.
     */
    public String getParameter() {
        return parameter;
    }

    /**
     * @param p The parameter to set.
     */
    public void setParameter(String p) {
        this.parameter = p;
    }

    /**
     * Gets the parent Action associated with this ServerAction record
     * @return Returns the parentAction.
     */
    public Action getParentAction() {
        return parentAction;
    }

    /**
     * Sets the parent Action associated with this ServerAction record
     * @param parentActionIn The parentAction to set.
     */
    public void setParentAction(Action parentActionIn) {
        this.parentAction = parentActionIn;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public boolean equals(final Object other) {
        if (!(other instanceof PackageActionDetails castOther)) {
            return false;
        }
        return new EqualsBuilder().append((getParentAction() == null ? null :
                                       getParentAction().getId()),
                                       (castOther.getParentAction() == null ? null :
                                       castOther.getParentAction().getId()))
                                  .append(packageId, castOther.getPackageId())
                                  .append(parameter, castOther.getParameter())
                                  .append(packageName, castOther.getPackageName())
                                  .isEquals();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public int hashCode() {
        int result = 37 * (getParentAction() == null ? 0 :
                          (getParentAction().getId() == null ? 0 :
                           getParentAction().getId().intValue()));
        result += 37 * (packageId == null ? 0 : packageId.intValue());
        result += 37 * (parameter == null ? 0 : parameter.hashCode());
        result += 37 * (packageName == null ? 0 : packageName.hashCode());
        return result;
    }
}
