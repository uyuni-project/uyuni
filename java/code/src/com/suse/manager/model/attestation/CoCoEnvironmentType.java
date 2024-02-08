/*
 * Copyright (c) 2024 SUSE LLC
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
package com.suse.manager.model.attestation;

import java.util.Arrays;

public enum CoCoEnvironmentType {
    NONE(0),
    KVM_AMD_EPYC_MILAN(1),
    KVM_AMD_EPYC_GENOA(2),
    AZURE(3);

    private final long value;

    CoCoEnvironmentType(long valueIn) {
        value = valueIn;
    }

    public long getValue() {
        return value;
    }

    /**
     * @param valueIn the value
     * @return returns the enum type for the given value
     */
    public static CoCoEnvironmentType fromValue(long valueIn) {
        return Arrays.stream(CoCoEnvironmentType.values())
                     .filter(e -> e.getValue() == valueIn)
                     .findFirst()
                     .orElseThrow(() -> new IllegalArgumentException("Invalid CoCoEnvironmentType value " + valueIn));
    }
}
