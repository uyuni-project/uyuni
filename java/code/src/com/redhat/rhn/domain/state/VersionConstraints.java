/**
 * Copyright (c) 2015 SUSE LLC
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
package com.redhat.rhn.domain.state;

import java.util.Optional;

/**
 * Available package version constraints, matches the content of DB table
 * "suseVersionConstraintType".
 */
public enum VersionConstraints {

    LATEST(0), EQUAL(1);

    /** This ID corresponds to the id column in the database */
    public final int ID;

    private VersionConstraints(int id) {
        ID = id;
    }

    /**
     * Get enum value for a given ID.
     *
     * @param id the ID
     * @return enum value or empty if the given id is invalid
     */
    public static Optional<VersionConstraints> byId(int id) {
        switch (id) {
            case 0: return Optional.of(LATEST);
            case 1: return Optional.of(EQUAL);
            default: return Optional.empty();
        }
    }
}
