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
 * Available package states, matches the content of DB table: susePackageStates.
 */
public enum PackageStates {

    INSTALLED(0), REMOVED(1), PURGED(2);

    public final int ID;

    private PackageStates(int id) {
        ID = id;
    }

    /**
     *
     * @param id
     * @return
     */
    public static Optional<PackageStates> byId(int id) {
        switch (id) {
            case 0: return Optional.of(INSTALLED);
            case 1: return Optional.of(REMOVED);
            case 2: return Optional.of(PURGED);
            default: return Optional.empty();
        }
    }
}
