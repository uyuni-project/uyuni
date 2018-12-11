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

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.Lob;
import javax.persistence.MapsId;
import javax.persistence.OneToOne;

@Entity
public class PackageRepodata {

    @Id
    @Column(name = "package_id")
    private Long id;

    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "package_id")
    @MapsId
    private Package thePackage; // sorry, package is a keyword in java

    @Lob
    @Column(name = "primary_xml")
    private byte[] primaryXml;

    @Lob
    @Column(name = "filelist")
    private byte[] filelist;

    @Lob
    @Column(name = "other")
    private byte[] other;

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
    public byte[] getPrimaryXml() {
        return primaryXml;
    }

    public String getPrimaryXmlAsString() {
        return new String(primaryXml); // TODO encoding
    }

    /**
     * @param primaryXmlIn to set
     */
    public void setPrimaryXml(byte[] primaryXmlIn) {
        this.primaryXml = primaryXmlIn;
    }

    /**
     * @return filelist to get
     */
    public byte[] getFilelist() {
        return filelist;
    }


    public String getFilelistXml() {
        return new String(filelist); // TODO encoding
    }
    /**
     * @param filelistIn to set
     */
    public void setFilelist(byte[] filelistIn) {
        this.filelist = filelistIn;
    }

    /**
     * @return other to get
     */
    public byte[] getOther() {
        return other;
    }

    /**
     * @param otherIn to set
     */
    public void setOther(byte[] otherIn) {
        this.other = otherIn;
    }


    public String getOtherXml() {
        return new String(other); // TODO encoding
    }
}
