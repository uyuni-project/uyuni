/*
 * Copyright (c) 2026 SUSE LLC
 *
 * This software is licensed to you under the GNU General Public License,
 * version 2 (GPLv2). There is NO WARRANTY for this software, express or
 * implied, including the implied warranties of MERCHANTABILITY or FITNESS
 * FOR A PARTICULAR PURPOSE. You should have received a copy of GPLv2
 * along with this software; if not, see
 * http://www.gnu.org/licenses/old-licenses/gpl-2.0.txt.
 */
package com.redhat.rhn.testing.building;

import com.redhat.rhn.domain.user.Address;
import com.redhat.rhn.domain.user.UserFactory;

public class AddressBuilder {

    private String address1 = "444 Castro";
    private String address2 = "#1";
    private String city = "Mountain View";
    private String state = "CA";
    private String zip = "94043";
    private String country = "US";
    private String phone = "650-555-1212";
    private String fax = "650-555-1212";

    /**
     * Sets address line 1.
     *
     * @param address1In the first address line
     * @return this builder instance
     */
    public AddressBuilder withAddress1(String address1In) {
        this.address1 = address1In;
        return this;
    }

    /**
     * Sets address line 2.
     *
     * @param address2In the second address line
     * @return this builder instance
     */
    public AddressBuilder withAddress2(String address2In) {
        this.address2 = address2In;
        return this;
    }

    /**
     * Sets city.
     *
     * @param cityIn the city
     * @return this builder instance
     */
    public AddressBuilder withCity(String cityIn) {
        this.city = cityIn;
        return this;
    }

    /**
     * Sets state.
     *
     * @param stateIn the state
     * @return this builder instance
     */
    public AddressBuilder withState(String stateIn) {
        this.state = stateIn;
        return this;
    }

    /**
     * Sets ZIP code.
     *
     * @param zipIn the ZIP code
     * @return this builder instance
     */
    public AddressBuilder withZip(String zipIn) {
        this.zip = zipIn;
        return this;
    }

    /**
     * Sets country.
     *
     * @param countryIn the country code
     * @return this builder instance
     */
    public AddressBuilder withCountry(String countryIn) {
        this.country = countryIn;
        return this;
    }

    /**
     * Sets phone number.
     *
     * @param phoneIn the phone number
     * @return this builder instance
     */
    public AddressBuilder withPhone(String phoneIn) {
        this.phone = phoneIn;
        return this;
    }

    /**
     * Sets fax number.
     *
     * @param faxIn the fax number
     * @return this builder instance
     */
    public AddressBuilder withFax(String faxIn) {
        this.fax = faxIn;
        return this;
    }

    public Address build() {
        Address address = UserFactory.createAddress();

        address.setAddress1(address1);
        address.setAddress2(address2);
        address.setCity(city);
        address.setState(state);
        address.setZip(zip);
        address.setCountry(country);
        address.setPhone(phone);
        address.setFax(fax);

        return address;
    }

}
