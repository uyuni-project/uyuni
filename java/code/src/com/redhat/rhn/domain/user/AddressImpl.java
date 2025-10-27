/*
 * Copyright (c) 2025 SUSE LLC
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
package com.redhat.rhn.domain.user;

import com.redhat.rhn.domain.BaseDomainHelper;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.SequenceGenerator;
import javax.persistence.Table;

/**
 * Class AddressImpl that implements Address
 */
@Entity
@Table(name = "web_user_site_info")
public class AddressImpl extends BaseDomainHelper implements Address {

    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "WEB_USER_SITE_INFO_SEQ")
    @SequenceGenerator(name = "WEB_USER_SITE_INFO_SEQ", sequenceName = "WEB_USER_SITE_INFO_ID_SEQ", allocationSize = 1)
    private Long id;
    @Column
    private String address1;
    @Column
    private String address2;
    @Column
    private String city;
    @Column
    private String state;
    @Column
    private String zip;
    @Column
    private String country;
    @Column
    private String phone;
    @Column
    private String fax;
    @Column(name = "is_po_box")
    private String isPoBox;
    @Column(name = "type")
    private String privType;

    /**
     * Protect the constructor
     */
    protected AddressImpl() {
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Long getId() {
        return this.id;
    }

    /**
     * Sets the database id for this address.  This
     * is the unique id for this address.
     * @param idIn the in id
     */
    protected void setId(Long idIn) {
        this.id = idIn;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public String getAddress1() {
        if (address1 == null) {
            address1 = " ";
        }
        return this.address1;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void setAddress1(String address1In) {
        this.address1 = address1In;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public String getAddress2() {
        if (address2 == null) {
            address2 = " ";
        }
        return this.address2;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void setAddress2(String address2In) {
        this.address2 = address2In;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public String getCity() {
        if (city == null) {
            city = " ";
        }
        return this.city;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void setCity(String cityIn) {
        this.city = cityIn;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public String getState() {
        if (state == null) {
            state = " ";
        }
        return this.state;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void setState(String stateIn) {
        this.state = stateIn;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public String getZip() {
        if (zip == null) {
            zip = " ";
        }
        return this.zip;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void setZip(String zipIn) {
        this.zip = zipIn;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public String getCountry() {
        if (country == null) {
            country = " ";
        }
        return this.country;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void setCountry(String countryIn) {
        this.country = countryIn;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public String getPhone() {
        if (phone == null) {
            phone = " ";
        }
        return this.phone;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void setPhone(String phoneIn) {
        this.phone = phoneIn;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public String getFax() {
        if (fax == null) {
            fax = " ";
        }
        return this.fax;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void setFax(String faxIn) {
        this.fax = faxIn;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public String getIsPoBox() {
        if (isPoBox == null) {
            isPoBox = "0";
        }
        return this.isPoBox;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void setIsPoBox(String isPoBoxIn) {
        this.isPoBox = isPoBoxIn;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public String getType() {
        if (privType == null || privType.isEmpty()) {
            return Address.TYPE_MARKETING;
        }
        return privType;
    }

    /**
     * Output this object to a string
     * @return String value of AddressImpl object
     */
    @Override
    public String toString() {
        return "{ID: " + getId() + ", type: " + getType() + ", created: " + getCreated() +
                ", modified: " + getModified() + ", address1: " + getAddress1() + "}";
    }

    // NOTE THIS IS LEGACY REMOVE LATER!!
    /**
     * Set the private type of this address
     * @param pt string to set
     */
    public void setPrivType(String pt) {
        privType = pt;
    }

    /**
     * Get the private type.
     * @return string type
     */
    public String getPrivType() {
        return privType;
    }
}

