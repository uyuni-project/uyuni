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
 * SPDX-License-Identifier: GPL-2.0-only
 *
 * Red Hat trademarks are not licensed under GPLv2. No permission is
 * granted to use or replicate Red Hat trademarks that are incorporated
 * in this software or its documentation.
 */
package com.redhat.rhn.domain.state;

import java.util.Arrays;
import java.util.Optional;

/**
 * Available package version constraints, matches the content of DB table
 * "suseVersionConstraintType".
 */
public enum VersionConstraints {

    LATEST(0), ANY(1);

    /** This ID corresponds to the id column in the database */
    private final int id;

    VersionConstraints(int idIn) {
        id = idIn;
    }

    /**
     * Return the id.
     *
     * @return the id
     */
    public int getId() {
        return id;
    }

    /**
     * Get enum value for a given ID.
     *
     * @param idIn the ID
     * @return enum value or empty if the given id is invalid
     */
    public static Optional<VersionConstraints> byId(int idIn) {
        return Arrays.asList(VersionConstraints.values()).stream()
                .filter(constraint -> constraint.id == idIn).findFirst();
    }
}
