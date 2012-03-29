/**
 * Copyright (c) 2011 Novell
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

package com.suse.pam;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;

/**
 * Enum class to represent the return values of the 'unix2_chkpwd' binary.
 */
public class PamReturnValue {

    /**
     * The actual return values of unix2_chkpwd as constants. Error messages
     * taken from the manpage.
     */
    public static final PamReturnValue PAM_SUCCESS = new PamReturnValue(0,
            "The password is correct.");
    public static final PamReturnValue PAM_FAILURE = new PamReturnValue(1,
            "unix2_chkpwd was inappropriately called from the command line " +
            "or the password is incorrect.");

    private static final PamReturnValue[] PRIVATE_VALUES = { PAM_SUCCESS,
            PAM_FAILURE };

    /**
     * The {@link List} of possible values.
     */
    public static final List VALUES = Collections.unmodifiableList(Arrays
            .asList(PRIVATE_VALUES));

    private final String description;
    private final int id;

    /**
     * Private constructor.
     *
     * @param idIn id
     * @param descriptionIn description
     */
    private PamReturnValue(int idIn, String descriptionIn) {
        this.id = idIn;
        this.description = descriptionIn;
    }

    /**
     * Return the {@link PamReturnValue} that matches a given id.
     *
     * @param id a valid Integer with a value between 0 and 1
     * @return the PamReturnValue matching the id
     * @throws IllegalArgumentException
     *              if the id is outside the range of possible return values
     */
    public static PamReturnValue fromId(int id) throws IllegalArgumentException {
        int maxId = VALUES.size() - 1;
        if (id > maxId || id < 0) {
            throw new IllegalArgumentException("id " + id +
                    " is not between 0 and " + maxId);
        }
        return (PamReturnValue) VALUES.get(id);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public boolean equals(final Object o) {
        if (this == o) {
            return true;
        }
        if (!(o instanceof PamReturnValue)) {
            return false;
        }
        final PamReturnValue pamReturnValue = (PamReturnValue) o;
        if (id != pamReturnValue.id) {
            return false;
        }
        return true;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public int hashCode() {
        return id;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public String toString() {
        return description;
    }
}
