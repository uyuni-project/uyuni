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

import org.apache.commons.lang3.builder.EqualsBuilder;
import org.apache.commons.lang3.builder.HashCodeBuilder;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.SequenceGenerator;
import javax.persistence.Table;

@Entity
@Table(name = "suseOVALPlatform")
public class OVALPlatform {
    private Long id;
    private String cpe;

    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "platform_id_seq")
    @SequenceGenerator(name = "platform_id_seq", sequenceName = "suse_oval_platform_id_seq",
            allocationSize = 1)
    public Long getId() {
        return id;
    }

    public void setId(Long idIn) {
        this.id = idIn;
    }

    @Column(name = "cpe")
    public String getCpe() {
        return cpe;
    }

    public void setCpe(String cpeIn) {
        this.cpe = cpeIn;
    }

    @Override
    public String toString() {
        return "OVALPlatform{" +
                "id=" + id +
                ", cpe='" + cpe + '\'' +
                '}';
    }


    /**
     * {@inheritDoc}
     */
    @Override
    public boolean equals(final Object other) {
        if (!(other instanceof OVALPlatform)) {
            return false;
        }
        OVALPlatform castOther = (OVALPlatform) other;
        return new EqualsBuilder()
                .append(id, castOther.id)
                .isEquals();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public int hashCode() {
        return new HashCodeBuilder()
                .append(id)
                .toHashCode();
    }
}
