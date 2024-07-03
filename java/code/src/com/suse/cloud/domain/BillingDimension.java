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
//TEST
import java.util.Arrays;
import java.util.Objects;
//TEST
public enum BillingDimension {
    MANAGED_SYSTEMS,
    MONITORING;

    private final String label;

    /**
     * Default constructor, uses the enum name converted to lowercase as label
     */
    BillingDimension() {
        this(null);
    }

    /**
     * Constructor to explicitly specify a label
     * @param labelIn the label for this enum value
     */
    BillingDimension(String labelIn) {
        this.label = labelIn != null ? labelIn : this.name().toLowerCase();
    }

    public String getLabel() {
        return label;
    }

    /**
     * Retrieve the {@link BillingDimension} with the given label
     * @param label the label of the dimension
     * @return the enum value corresponding to the specified label
     */
    public static BillingDimension byLabel(String label) {
        return Arrays.stream(BillingDimension.values())
                     .filter(e -> Objects.equals(e.getLabel(), label))
                     .findFirst()
                     .orElseThrow(() -> new IllegalArgumentException("Invalid BillingDimension value " + label));
    }
}
