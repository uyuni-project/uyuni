/*
 * Copyright (c) 2025 SUSE LLC
 *
 * This software is licensed to you under the GNU General Public License,
 * version 2 (GPLv2). There is NO WARRANTY for this software, express or
 * implied, including the implied warranties of MERCHANTABILITY or FITNESS
 * FOR A PARTICULAR PURPOSE. You should have received a copy of GPLv2
 * along with this software; if not, see
 * http://www.gnu.org/licenses/old-licenses/gpl-2.0.txt.
 */

package com.redhat.rhn.domain.contentmgmt;

import com.redhat.rhn.domain.Labeled;

import java.util.Arrays;

public enum DiffAction implements Labeled {
    ADD("+"),
    DELETE("-"),
    FILTER("x");

    private final String label;

    DiffAction(String labelIn) {
        label = labelIn;
    }

    @Override
    public String getLabel() {
        return label;
    }

    /**
     * @param labelIn the label
     * @return returns the enum type for the given value
     */
    public static DiffAction fromValue(String labelIn) {
        return Arrays.stream(DiffAction.values())
                .filter(e -> e.getLabel().equals(labelIn))
                .findFirst()
                .orElseThrow(() -> new IllegalArgumentException("Invalid DiffAction label " + labelIn));
    }
}
