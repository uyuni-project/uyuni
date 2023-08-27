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

import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.IdClass;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.Table;

@Entity
@Table(name = "suseOVALPlatformVulnerablePackage")
@IdClass(OVALPlatformVulnerablePackageKey.class)
public class OVALPlatformVulnerablePackage {
    private OVALPlatform platform;
    private Cve cve;
    private OVALVulnerablePackage vulnerablePackage;

    @Id
    @ManyToOne
    @JoinColumn(name = "platform_id")
    public OVALPlatform getPlatform() {
        return platform;
    }

    public void setPlatform(OVALPlatform platformIn) {
        this.platform = platformIn;
    }

    @SuppressWarnings("JpaAttributeTypeInspection")
    @Id
    @ManyToOne
    @JoinColumn(name = "cve_id")
    public Cve getCve() {
        return cve;
    }

    public void setCve(Cve cveIn) {
        this.cve = cveIn;
    }

    @Id
    @ManyToOne
    @JoinColumn(name = "vulnerable_pkg_id")
    public OVALVulnerablePackage getVulnerablePackage() {
        return vulnerablePackage;
    }

    public void setVulnerablePackage(OVALVulnerablePackage vulnerablePackageIn) {
        this.vulnerablePackage = vulnerablePackageIn;
    }
}
