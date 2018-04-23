/**
 * Copyright (c) 2018 SUSE LLC
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
package com.redhat.rhn.domain.image;

import org.apache.commons.lang3.builder.EqualsBuilder;
import org.apache.commons.lang3.builder.HashCodeBuilder;

import javax.persistence.Column;
import javax.persistence.DiscriminatorValue;
import javax.persistence.Entity;
import javax.persistence.Table;
import java.util.Optional;

/**
 * Domain class for Kiwi image profiles
 */
@Entity
@Table(name = "suseKiwiProfile")
@DiscriminatorValue("kiwi")
public class KiwiProfile extends ImageProfile {

    private String path;

    /**
     * @return the path
     */
    @Column(name = "path")
    public String getPath() {
        return path;
    }

    /**
     * @param pathIn the path to set
     */
    public void setPath(String pathIn) {
        this.path = pathIn;
    }

    /**
     * {@inheritDoc}
     */
    public boolean equals(final Object other) {
        if (!(other instanceof KiwiProfile)) {
            return false;
        }
        KiwiProfile castOther = (KiwiProfile) other;
        return new EqualsBuilder()
                .appendSuper(super.equals(castOther))
                .append(path, castOther.path)
                .isEquals();
    }

    /**
     * {@inheritDoc}
     */
    public int hashCode() {
        return new HashCodeBuilder()
                .appendSuper(super.hashCode())
                .append(path)
                .toHashCode();
    }

    /**
     * Converts this profile to a KiwiProfile if it is one.
     *
     * @return optional of KiwiProfile
     */
    @Override
    public Optional<KiwiProfile> asKiwiProfile() {
        return Optional.of(this);
    }
}
