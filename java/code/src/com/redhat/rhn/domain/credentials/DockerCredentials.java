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
package com.redhat.rhn.domain.credentials;

import org.apache.commons.lang.builder.EqualsBuilder;
import org.apache.commons.lang.builder.HashCodeBuilder;

import java.util.Optional;

/**
 * DockerCredentials
 */
public class DockerCredentials extends Credentials {

    private String email;
    /**
     * Constructs a DockerCredentials instance
     */
    public DockerCredentials() {
        super();
    }
    /**
     * @return the email
     */
    public String getEmail() {
        return email;
    }
    /**
     * @param emailIn the email to set
     */
    public void setEmail(String emailIn) {
        this.email = emailIn;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public int hashCode() {
        return new HashCodeBuilder()
                .appendSuper(super.hashCode())
                .append(getEmail())
                .toHashCode();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public boolean equals(Object other) {
        if (!(other instanceof DockerCredentials)) {
            return false;
        }
        DockerCredentials otherCreds = (DockerCredentials) other;
        return new EqualsBuilder()
                .appendSuper(super.equals(otherCreds))
                .append(getEmail(), otherCreds.getEmail())
                .isEquals();
    }

    /**
     * Converts a credential into a docker credential if it is one.
     *
     * @return optional of DockerCredentials
     */
    public Optional<DockerCredentials> asDockerCredentials() {
        return Optional.of(this);
    }
}
