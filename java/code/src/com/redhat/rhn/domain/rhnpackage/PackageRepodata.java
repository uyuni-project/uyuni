/**
 * Copyright (c) 2018 SUSE LLC
 * <p>
 * This software is licensed to you under the GNU General Public License,
 * version 2 (GPLv2). There is NO WARRANTY for this software, express or
 * implied, including the implied warranties of MERCHANTABILITY or FITNESS
 * FOR A PARTICULAR PURPOSE. You should have received a copy of GPLv2
 * along with this software; if not, see
 * http://www.gnu.org/licenses/old-licenses/gpl-2.0.txt.
 * <p>
 * Red Hat trademarks are not licensed under GPLv2. No permission is
 * granted to use or replicate Red Hat trademarks that are incorporated
 * in this software or its documentation.
 */

package com.redhat.rhn.domain.rhnpackage;

import org.hibernate.annotations.Type;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.Lob;
import javax.persistence.MapsId;
import javax.persistence.OneToOne;
import javax.persistence.Table;

@Entity
@Table(name = "rhnPackageRepodata")
public class PackageRepodata {

    @Id
    @Column(name = "package_id")
    private Long id;

    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "package_id")
    @MapsId
    private Package thePackage; // package is a keyword in java

    @Type(type = "org.hibernate.type.TextType")
    @Column(name = "primary_xml")
    private String primaryXml;

    @Type(type = "org.hibernate.type.TextType")
    @Column(name = "filelist")
    private String filelistXml;

    @Type(type = "org.hibernate.type.TextType")
    @Column(name = "other")
    private String otherXml;

    /**
     * @return id to get
     */
    public Long getId() {
        return id;
    }

    /**
     * @param idIn to set
     */
    public void setId(Long idIn) {
        this.id = idIn;
    }

    /**
     * @return thePackage to get
     */
    public Package getThePackage() {
        return thePackage;
    }

    /**
     * @param thePackageIn to set
     */
    public void setThePackage(Package thePackageIn) {
        this.thePackage = thePackageIn;
    }

    /**
     * @return primaryXml to get
     */
    public String getPrimaryXml() {
        return primaryXml;
    }

    /**
     * @param primaryXmlIn to set
     */
    public void setPrimaryXml(String primaryXmlIn) {
        this.primaryXml = primaryXmlIn;
    }

    /**
     * @return filelist to get
     */
    public String getFilelistXml() {
        return filelistXml;
    }

    /**
     * @param filelistIn to set
     */
    public void setFilelistXml(String filelistIn) {
        this.filelistXml = filelistIn;
    }

    /**
     * @return other to get
     */
    public String getOtherXml() {
        return otherXml;
    }

    /**
     * @param otherIn to set
     */
    public void setOtherXml(String otherIn) {
        this.otherXml = otherIn;
    }
}
