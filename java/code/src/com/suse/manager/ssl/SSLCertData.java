/*
 * Copyright (c) 2022 SUSE LLC
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
package com.suse.manager.ssl;

import org.apache.commons.lang3.StringUtils;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Objects;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * Data contained in an SSL certificate, also used to generate them
 */
public class SSLCertData {
    private final String cn;
    private final List<String> cnames;
    private final String country;
    private final String state;
    private final String city;
    private final String org;
    private final String orgUnit;
    private final String email;

    /**
     * Create new data objet
     *
     * @param cnIn CN
     * @param cnamesIn list of cnames
     * @param countryIn the country
     * @param stateIn the state
     * @param cityIn the city
     * @param orgIn the organization
     * @param orgUnitIn the organization unit
     * @param emailIn the mail
     */
    public SSLCertData(String cnIn, List<String> cnamesIn, String countryIn, String stateIn, String cityIn,
                       String orgIn, String orgUnitIn, String emailIn) {
        cn = cnIn;
        cnames = cnamesIn;
        country = sanitizeValue(countryIn);
        state = sanitizeValue(stateIn);
        city = sanitizeValue(cityIn);
        org = sanitizeValue(orgIn);
        orgUnit = sanitizeValue(orgUnitIn);
        email = sanitizeValue(emailIn);
    }

    private String sanitizeValue(String value) {
        return value != null && value.isBlank() ? null : value;
    }

    /**
     * @return the values as parameters for the rhn-ssl-tool
     */
    public List<String> getRhnSslToolParams() {
        List<String> params = new ArrayList<>();

        if (cn != null) {
            params.addAll(List.of("--set-hostname", cn));
        }

        if (cnames != null) {
            params.addAll(cnames.stream()
                    .flatMap(cname -> List.of("--set-cname", cname).stream()).collect(Collectors.toList()));
        }

        if (country != null) {
            params.addAll(List.of("--set-country", country));
        }

        if (state != null) {
            params.addAll(List.of("--set-state", state));
        }

        if (city != null) {
            params.addAll(List.of("--set-city", city));
        }

        if (org != null) {
            params.addAll(List.of("--set-org", org));
        }

        if (orgUnit != null) {
            params.addAll(List.of("--set-org-unit", orgUnit));
        }

        if (email != null) {
            params.addAll(List.of("--set-email", email));
        }
        return params;
    }

    /**
     * @return value of cn
     */
    public String getCn() {
        return cn;
    }

    @Override
    public boolean equals(Object oIn) {
        if (this == oIn) {
            return true;
        }
        if (oIn == null || getClass() != oIn.getClass()) {
            return false;
        }
        SSLCertData that = (SSLCertData) oIn;
        return Objects.equals(cn, that.cn) &&
                Objects.equals(cnames, that.cnames) &&
                Objects.equals(country, that.country) &&
                Objects.equals(state, that.state) &&
                Objects.equals(city, that.city) &&
                Objects.equals(org, that.org) &&
                Objects.equals(orgUnit, that.orgUnit) &&
                Objects.equals(email, that.email);
    }

    @Override
    public int hashCode() {
        return Objects.hash(cn, cnames, country, state, city, org, orgUnit, email);
    }

    /**
     * calculate machine name
     *
     * @return machine name
     */
    public String getMachineName() {
        String[] hostnameParts = this.getCn().split("\\.");

        return hostnameParts.length > 2 ?
                StringUtils.join(hostnameParts, ".", 0, hostnameParts.length - 2) :
                this.getCn();
    }

    /**
     * Return the set of aggregates cn and cnames.
     *
     * @return all cnames and cn without duplicate.
     */
    public Set<String> getAllCnames() {
        Set<String> allCnames = new HashSet<>();
        if (cn != null) {
            allCnames.add(cn);
        }
        if (cnames != null) {
            allCnames.addAll(cnames);
        }
        return allCnames;
    }
}
