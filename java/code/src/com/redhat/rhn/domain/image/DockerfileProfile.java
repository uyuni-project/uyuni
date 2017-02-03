/**
 * Copyright (c) 2017 SUSE LLC
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

import org.apache.commons.lang.builder.EqualsBuilder;
import org.apache.commons.lang.builder.HashCodeBuilder;

import java.util.Optional;

import javax.persistence.Column;
import javax.persistence.DiscriminatorValue;
import javax.persistence.Entity;
import javax.persistence.ManyToOne;
import javax.persistence.Table;

/**
 * Domain class for Dockerfile image profiles
 */
@Entity
@Table(name = "suseDockerfileProfile")
@DiscriminatorValue("dockerfile")
public class DockerfileProfile extends ImageProfile {

    private String path;
    private ImageStore store;

    /**
     * @return the path
     */
    @Column(name = "path")
    public String getPath() {
        return path;
    }

    /**
     * @return the store
     */
    @ManyToOne
    public ImageStore getStore() {
        return store;
    }

    /**
     * @param pathIn the path to set
     */
    public void setPath(String pathIn) {
        this.path = pathIn;
    }

    /**
     * @param storeIn the store to set
     */
    public void setStore(ImageStore storeIn) {
        this.store = storeIn;
    }

    /**
     * {@inheritDoc}
     */
    public boolean equals(final Object other) {
        if (!(other instanceof DockerfileProfile)) {
            return false;
        }
        DockerfileProfile castOther = (DockerfileProfile) other;
        return new EqualsBuilder()
                .appendSuper(super.equals(castOther))
                .append(path, castOther.path)
                .append(store, castOther.store)
                .isEquals();
    }

    /**
     * {@inheritDoc}
     */
    public int hashCode() {
        return new HashCodeBuilder()
                .appendSuper(super.hashCode())
                .append(path)
                .append(store)
                .toHashCode();
    }

    /**
     * Converts this profile to a DockerfileProfile if it is one.
     *
     * @return optional of DockerfileProfile
     */
    public Optional<DockerfileProfile> asDockerfileProfile() {
        return Optional.of(this);
    }
}
