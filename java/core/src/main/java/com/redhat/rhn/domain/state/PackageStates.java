/*
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

import java.util.Arrays;
import java.util.Optional;

/**
 * Available package states, matches the content of DB table "susePackageStateType".
 */
public enum PackageStates {

    INSTALLED(0), REMOVED(1), PURGED(2);

    /** This ID corresponds to the id column in the database */
    private final int ID;

    PackageStates(int id) {
        ID = id;
    }

    /**
     * Return the id.
     *
     * @return the id
     */
    public int id() {
        return ID;
    }

    /**
     * Get enum value for a given ID.
     *
     * @param id the ID
     * @return enum value or empty if the given id is invalid
     */
    public static Optional<PackageStates> byId(int id) {
        return Arrays.asList(PackageStates.values()).stream()
                .filter(state -> state.ID == id).findFirst();
    }

    /**
     * Indicate for a given state if a version constraint is required.
     *
     * @param packageState the package state
     * @return true if a version constraint is required, otherwise false
     */
    public static boolean requiresVersionConstraint(PackageStates packageState) {
        switch (packageState) {
            case INSTALLED: return true;
            default: return false;
        }
    }
}
