/*
 * Copyright (c) 2026 SUSE LLC
 *
 * This software is licensed to you under the GNU General Public License,
 * version 2 (GPLv2). There is NO WARRANTY for this software, express or
 * implied, including the implied warranties of MERCHANTABILITY or FITNESS
 * FOR A PARTICULAR PURPOSE. You should have received a copy of GPLv2
 * along with this software; if not, see
 * http://www.gnu.org/licenses/old-licenses/gpl-2.0.txt.
 */

package com.suse.coco.module.pvattest.model;

import java.util.Arrays;

/**
 * IBM Z series Model
 */
public enum IbmZGeneration {
    UNKNOWN(0),
    IBM_Z16(6),
    IBM_Z17(7);

    private final int value;

    IbmZGeneration(int valueIn) {
        value = valueIn;
    }

    public int getValue() {
        return value;
    }

    /**
     * @param valueIn the value
     * @return returns the enum type for the given value
     */
    public static IbmZGeneration fromValue(int valueIn) {
        return Arrays.stream(IbmZGeneration.values())
                .filter(e -> e.getValue() == valueIn)
                .findFirst()
                .orElse(UNKNOWN);
    }

}
