/*
 * Copyright (c) 2025--2026 SUSE LLC
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
import com.redhat.rhn.domain.user.legacy.UserImpl;

import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.builder.EqualsBuilder;
import org.apache.commons.lang3.builder.HashCodeBuilder;

import jakarta.persistence.Column;
import jakarta.persistence.Convert;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.SequenceGenerator;
import jakarta.persistence.Table;

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

    @Column(length = 128)
    private String email;

    @Column(name = "email_uc", length = 128)
    private String emailUc;

    @Column(name = "alt_first_names", length = 128)
    private String altFirstNames;

    @Column(name = "alt_last_name", length = 128)
    private String altLastName;

    @Column(name = "address1", length = 128, nullable = false)
    private String address1 = StringUtils.SPACE;

    @Column(name = "address2", length = 128)
    private String address2;

    @Column(name = "address3", length = 128)
    private String address3;

    @Column(name = "address4", length = 128)
    private String address4;

    @Column(name = "city", length = 128, nullable = false)
    private String city = StringUtils.SPACE;

    @Column(length = 64)
    private String state;

    @Column(length = 64)
    private String zip;

    @Column(name = "country", length = 2, nullable = false)
    private String country = StringUtils.SPACE;

    @Column(length = 32)
    private String phone;

    @Column(length = 32)
    private String fax;

    @Column(length = 128)
    private String url;

    @Column(name = "is_po_box", length = 1)
    private String isPoBox = "0";

    @Column(name = "type", length = 1, nullable = false)
    @Convert(converter = AddressTypeConverter.class)
    private AddressType type = AddressType.ADDRESS_TYPE_MARKETING;

    @ManyToOne
    @JoinColumn(name = "web_user_id")
    private UserImpl user;

    // JPA requires default constructor
    protected AddressImpl() {
    }

    /**
     * Getter for identifier.
     *
     * @return identifier value
     */
    @Override
    public Long getId() {
        return id;
    }

    /**
     * Set the identifier.
     *
     * @param idIn identifier value
     */
    public void setId(Long idIn) {
        id = idIn;
    }

    /**
     * Getter for email.
     *
     * @return email address
     */
    public String getEmail() {
        return email;
    }

    /**
     * Setter for email.
     * Also updates the normalized lowercase value in {@code emailUc}.
     *
     * @param emailIn email address
     */
    public void setEmail(String emailIn) {
        email = emailIn;
        emailUc = emailIn != null ? emailIn.toLowerCase() : null;
    }

    /**
     * Getter for normalized email (lowercase).
     *
     * @return normalized email value
     */
    public String getEmailUc() {
        return emailUc;
    }

    /**
     * Getter for alternate first names.
     *
     * @return alternate first names
     */
    public String getAltFirstNames() {
        return altFirstNames;
    }

    /**
     * Setter for alternate first names.
     *
     * @param altFirstNamesIn alternate first names value
     */
    public void setAltFirstNames(String altFirstNamesIn) {
        altFirstNames = altFirstNamesIn;
    }

    /**
     * Getter for alternate last name.
     *
     * @return alternate last name
     */
    public String getAltLastName() {
        return altLastName;
    }

    /**
     * Setter for alternate last name.
     *
     * @param altLastNameIn alternate last name value
     */
    public void setAltLastName(String altLastNameIn) {
        altLastName = altLastNameIn;
    }

    /** {@inheritDoc} */
    @Override
    public String getAddress1() {
        return address1;
    }

    /** {@inheritDoc} */
    @Override
    public void setAddress1(String address1In) {
        address1 = address1In;
    }

    /** {@inheritDoc} */
    @Override
    public String getAddress2() {
        return address2;
    }

    /** {@inheritDoc} */
    @Override
    public void setAddress2(String address2In) {
        address2 = address2In;
    }

    /**
     * Getter for the third address line.
     *
     * @return third address line
     */
    public String getAddress3() {
        return address3;
    }

    /**
     * Setter for the third address line.
     *
     * @param address3In third address line value
     */
    public void setAddress3(String address3In) {
        address3 = address3In;
    }

    /**
     * Getter for the fourth address line.
     *
     * @return fourth address line
     */
    public String getAddress4() {
        return address4;
    }

    /**
     * Setter for the fourth address line.
     *
     * @param address4In fourth address line value
     */
    public void setAddress4(String address4In) {
        address4 = address4In;
    }

    /** {@inheritDoc} */
    @Override
    public String getCity() {
        return city;
    }

    /** {@inheritDoc} */
    @Override
    public void setCity(String cityIn) {
        city = cityIn;
    }

    /** {@inheritDoc} */
    @Override
    public String getState() {
        return state;
    }

    /** {@inheritDoc} */
    @Override
    public void setState(String stateIn) {
        state = stateIn;
    }

    /** {@inheritDoc} */
    @Override
    public String getZip() {
        return zip;
    }

    /** {@inheritDoc} */
    @Override
    public void setZip(String zipIn) {
        zip = zipIn;
    }

    /** {@inheritDoc} */
    @Override
    public String getCountry() {
        return country;
    }

    /** {@inheritDoc} */
    @Override
    public void setCountry(String countryIn) {
        country = countryIn;
    }

    /** {@inheritDoc} */
    @Override
    public String getPhone() {
        return phone;
    }

    /** {@inheritDoc} */
    @Override
    public void setPhone(String phoneIn) {
        phone = phoneIn;
    }

    /** {@inheritDoc} */
    @Override
    public String getFax() {
        return fax;
    }

    /** {@inheritDoc} */
    @Override
    public void setFax(String faxIn) {
        fax = faxIn;
    }

    /**
     * Getter for URL.
     *
     * @return URL value
     */
    public String getUrl() {
        return url;
    }

    /**
     * Setter for URL.
     *
     * @param urlIn URL value
     */
    public void setUrl(String urlIn) {
        url = urlIn;
    }

    /** {@inheritDoc} */
    @Override
    public String getIsPoBox() {
        return isPoBox;
    }

    /** {@inheritDoc} */
    @Override
    public void setIsPoBox(String isPoBoxIn) {
        isPoBox = isPoBoxIn;
    }

    /** {@inheritDoc} */
    @Override
    public AddressType getType() {
        return type;
    }

    /** {@inheritDoc} */
    @Override
    public void setType(AddressType typeIn) {
        type = typeIn;
    }

    /** {@inheritDoc} */
    @Override
    public User getUser() {
        return user;
    }

    /** {@inheritDoc} */
    @Override
    public void setUser(User userIn) {
        user = (UserImpl) userIn;
    }

    /**
     * Compare two addresses by the canonical address fields and type.
     *
     * @param other other object
     * @return {@code true} when equivalent
     */
    @Override
    public boolean equals(Object other) {
        if (!(other instanceof AddressImpl otherAddr)) {
            return false;
        }
        return new EqualsBuilder()
                .append(getAddress1(), otherAddr.getAddress1())
                .append(getAddress2(), otherAddr.getAddress2())
                .append(getCity(), otherAddr.getCity())
                .append(getState(), otherAddr.getState())
                .append(getZip(), otherAddr.getZip())
                .append(getCountry(), otherAddr.getCountry())
                .append(getType(), otherAddr.getType())
                .isEquals();
    }

    /**
     * Compute hash code based on canonical address fields and type.
     *
     * @return hash code
     */
    @Override
    public int hashCode() {
        return new HashCodeBuilder()
                .append(getAddress1())
                .append(getAddress2())
                .append(getCity())
                .append(getState())
                .append(getZip())
                .append(getCountry())
                .append(getType())
                .toHashCode();
    }

    /**
     * Output this object to a string
     * @return String value of AddressImpl object
     */
    @Override
    public String toString() {
        return "{ID: " + getId() + ", type: " + getType() + ", created: " + getCreated() +
                ", modified: " + getModified() + ", address1: " + getAddress1() +
                ", city: " + getCity() + ", country: " + getCountry() + "}";
    }

}
