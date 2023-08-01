/*
 * Copyright (c) 2013 SUSE LLC
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
package com.redhat.rhn.manager.audit;

/**
 * Enum representing different patch status values.
 *
 */
public enum PatchStatus {

    // Values sorted by seriousness
    AFFECTED_PATCH_UNAVAILABLE("Affected, patch is unavailable", 0),
    AFFECTED_PATCH_INAPPLICABLE("Affected, patch available in unassigned channel", 1),
    AFFECTED_PARTIAL_PATCH_APPLICABLE("Affected, partial patch available in assigned channel", 2),
    AFFECTED_FULL_PATCH_APPLICABLE("Affected, full patch available in assigned channel", 3),
    NOT_AFFECTED("Not affected", 4),
    PATCHED("Patched", 5),
    AFFECTED_PATCH_INAPPLICABLE_SUCCESSOR_PRODUCT("Affected, patch available in a Product Migration target", 6);

    /**
     * The lower the more severe
     */
    private int rank;
    private String description;

    /**
     * Private constructor.
     * @param rankIn
     * @param descriptionIn
     */
    PatchStatus(String descriptionIn, int rankIn) {
        this.description = descriptionIn;
        this.rank = rankIn;
    }

    /**
     * Get the description.
     * @return the description
     */
    public String getDescription() {
        return description;
    }

    /**
     * Get the rank
     * @return the rank
     */
    public int getRank() {
        return rank;
    }
}
