/*
 * Copyright (c) 2024 SUSE LLC
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

package com.redhat.rhn.domain.channel;

import java.io.Serializable;
import java.util.Objects;

import javax.persistence.Embeddable;

@Embeddable
public class AppStreamApiKey implements Serializable {

    private Long id;
    private String rpm;

    /**
     * default constructor
     */
    public AppStreamApiKey() { }

    /**
     * constructor
     * @param idIn ID
     * @param rpmIn RPM
     */
    public AppStreamApiKey(Long idIn, String rpmIn) {
        this.id = idIn;
        this.rpm = rpmIn;
    }

    // Getters and Setters
    public Long getId() {
        return id;
    }

    public void setId(Long idIn) {
        this.id = idIn;
    }

    public String getRpm() {
        return rpm;
    }

    public void setRpm(String rpmIn) {
        this.rpm = rpmIn;
    }

    // Equals and HashCode methods
    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }
        AppStreamApiKey that = (AppStreamApiKey) o;
        return Objects.equals(id, that.id) && Objects.equals(rpm, that.rpm);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id, rpm);
    }
}
