/*
 * Copyright (c) 2012--2023 SUSE LLC
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

import com.redhat.rhn.domain.user.User;

import java.util.Date;
import java.util.Optional;

/**
 * Common interface for all the credentials used and stored by Uyuni
 */
public interface Credentials {

    /**
     * Retrieves the ID of this credentials object.
     * @return id
     */
    Long getId();

    /**
     * Sets the ID of this credentials object.
     * @param idIn id
     */
    void setId(Long idIn);

    /**
     * Returns the type as defined by {@link CredentialsType}.
     * @return type
     */
    CredentialsType getType();

    /**
     * Retrieves the associated {@link User}.
     * @return user
     */
    User getUser();

    /**
     * Sets the associated {@link User}.
     * @param userIn user
     */
    void setUser(User userIn);

    /**
     * Retrieves the creation date for this credentials object.
     * @return the creation date
     */
    Date getCreated();

    /**
     * Sets the creation date for this credentials object.
     * @param createdIn the creation date
     */
    void setCreated(Date createdIn);

    /**
     * Retrieves the data when this credentials object was last modified
     * @return the modification date
     */
    Date getModified();

    /**
     * Sets the data when this credentials object was last modified
     * @param modifiedIn the modification date
     */
    void setModified(Date modifiedIn);

    /**
     * Check if this credential is valid
     * @return true if valid
     */
    default boolean isValid() {
        return true;
    }

    /**
     * Check if this credential is of type credentialType
     * @param credentialType type to check for
     * @return true if the type match, otherwise false
     */
    default boolean isTypeOf(Class<? extends Credentials> credentialType) {
        return credentialType.isInstance(this);
    }

    /**
     * Retrieves this credentials object constrained by the specified type
     * @param credentialsClass the expected credentials class
     * @return an optional containing the current instance converted to the given type, or empty if the types do not
     * match
     * @param <T> an implementation of {@link Credentials}
     */
    default <T extends Credentials> Optional<T> castAs(Class<T> credentialsClass) {
        if (this.isTypeOf(credentialsClass)) {
            return Optional.of(credentialsClass.cast(this));
        }

        return Optional.empty();
    }

}
