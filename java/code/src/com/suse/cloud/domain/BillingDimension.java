/*
 * Copyright (c) 2023 SUSE LLC
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

package com.suse.cloud.domain;

import java.util.Arrays;

public enum BillingDimension {
    ENROLLED_SYSTEMS(1),
    MONITORING(2);

    private final int id;

    BillingDimension(int idIn) {
        this.id = idIn;
    }

    public int getId() {
        return id;
    }

    /**
     * Retrieve the {@link BillingDimension} with the given id
     * @param id the id of the dimension
     * @return the enum value corresponding to the specified id
     */
    public static BillingDimension byId(int id) {
        return Arrays.stream(BillingDimension.values())
                     .filter(e -> e.getId() == id)
                     .findFirst()
                     .orElse(null);
    }
}
