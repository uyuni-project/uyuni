/*
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

import java.util.Optional;

import javax.persistence.Column;
import javax.persistence.DiscriminatorValue;
import javax.persistence.Entity;
import javax.persistence.Table;

/**
 * Domain class for Kiwi image profiles
 */
@Entity
@Table(name = "suseKiwiProfile")
@DiscriminatorValue("kiwi")
public class KiwiProfile extends ImageProfile {

    private String path;
    private String kiwiOptions;

    /**
     * @return the Kiwi options
     */
    @Column(name = "kiwi_options")
    public String getKiwiOptions() {
        if (kiwiOptions == null) {
            return "";
        }
        return kiwiOptions;
    }

    /**
     * @param kiwiOptionsIn the kiwi options to set
     */
    public void setKiwiOptions(String kiwiOptionsIn) {
        this.kiwiOptions = kiwiOptionsIn;
    }

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
    @Override
    public boolean equals(final Object other) {
        if (!(other instanceof KiwiProfile)) {
            return false;
        }
        KiwiProfile castOther = (KiwiProfile) other;
        return new EqualsBuilder()
                .appendSuper(super.equals(castOther))
                .append(path, castOther.path)
                .append(kiwiOptions, castOther.kiwiOptions)
                .isEquals();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public int hashCode() {
        return new HashCodeBuilder()
                .appendSuper(super.hashCode())
                .append(path)
                .append(kiwiOptions)
                .toHashCode();
    }

    /**
     * Converts this profile to a <code>KiwiProfile</code> if it is one.
     *
     * @return optional of <code>KiwiProfile</code>
     */
    @Override
    public Optional<KiwiProfile> asKiwiProfile() {
        return Optional.of(this);
    }
}
