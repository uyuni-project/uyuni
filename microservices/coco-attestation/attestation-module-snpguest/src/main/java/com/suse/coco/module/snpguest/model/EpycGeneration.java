/*
 * Copyright (c) 2024 SUSE LLC
 *
 * This software is licensed to you under the GNU General Public License,
 * version 2 (GPLv2). There is NO WARRANTY for this software, express or
 * implied, including the implied warranties of MERCHANTABILITY or FITNESS
 * FOR A PARTICULAR PURPOSE. You should have received a copy of GPLv2
 * along with this software; if not, see
 * http://www.gnu.org/licenses/old-licenses/gpl-2.0.txt.
 */

package com.suse.coco.module.snpguest.model;

import java.util.Arrays;

/**
 * EPYC CPU Model, used by SNPGuest to identify what certificates to use during verification.
 */
public enum EpycGeneration {
    UNKNOWN(0),
    MILAN(1),
    GENOA(2),
    BERGAMO(3),
    SIENA(4),
    TURIN(5);

    private final int value;

    EpycGeneration(int valueIn) {
        value = valueIn;
    }

    public int getValue() {
        return value;
    }

    /**
     * @param valueIn the value
     * @return returns the enum type for the given value
     */
    public static EpycGeneration fromValue(int valueIn) {
        return Arrays.stream(EpycGeneration.values())
                .filter(e -> e.getValue() == valueIn)
                .findFirst()
                .orElse(UNKNOWN);
    }

}
