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
package com.redhat.rhn.domain.action.supportdata;

import com.redhat.rhn.domain.action.ActionChild;

import org.hibernate.annotations.Type;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.Table;

/**
 * SupportDataActionDetails
 */
@Entity
@Table(name = "suseActionSupportDataDetails")
public class SupportDataActionDetails extends ActionChild {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column
    private Long id;

    @Column(name = "case_number", nullable = false)
    private String caseNumber;

    @Column
    private String parameter;

    @Column(name = "upload_geo")
    @Type(type = "com.redhat.rhn.domain.action.supportdata.UploadGeoEnumType")
    private UploadGeoType geoType;

    /**
     * @return Returns the id.
     */
    public Long getId() {
        return id;
    }

    /**
     * @param i The id to set.
     */
    public void setId(Long i) {
        this.id = i;
    }

    /**
     * @return Returns the case number.
     */
    public String getCaseNumber() {
        return caseNumber;
    }

    /**
     * @param n The case number to set.
     */
    public void setCaseNumber(String n) {
        caseNumber = n;
    }

    /**
     * @return Returns the parameters.
     */
    public String getParameter() {
        return parameter;
    }

    /**
     * @param p The parameter to set.
     */
    public void setParameter(String p) {
        parameter = p;
    }

    /**
     * @return Returns the Upload Geo
     */
    public UploadGeoType getGeoType() {
        return geoType;
    }

    /**
     * Set the Upload Geo
     * @param geoTypeIn the upload Geo
     */
    public void setGeoType(UploadGeoType geoTypeIn) {
        geoType = geoTypeIn;
    }
}
