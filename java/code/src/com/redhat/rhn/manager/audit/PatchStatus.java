/**
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
 * @version $Rev$
 */
public enum PatchStatus {

    // Values sorted by seriousness
    AFFECTED_PATCH_INAPPLICABLE("Affected, patch available in unassigned channel"),
    AFFECTED_PATCH_APPLICABLE("Affected, patch available in assigned channel"),
    NOT_AFFECTED("Not affected"),
    PATCHED("Patched");

    // Status description
    private String description;

    /**
     * Private constructor.
     * @param label
     * @param descriptionIn
     */
    PatchStatus(String descriptionIn) {
        this.description = descriptionIn;
    }

    /**
     * Get the description.
     * @return the description
     */
    public String getDescription() {
        return description;
    }
}
