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

public enum CoCoResultType {
    NONE(0, "Result Type not found"),
    SEV_SNP(1, "AMD Secure Encrypted Virtualization-Secure Nested Paging (SEV-SNP)"),
    SECURE_BOOT(2, "Secure Boot enabled"),
    AZURE_SEV_SNP(3, "AMD Secure Encrypted Virtualization-Secure Nested Paging (SEV-SNP) Azure attestation"),
    AZURE_SECURE_BOOT(4, "Secure Boot Azure attestation"),
    AZURE_DISK_ENCRYPTED(5, "Disks encrypted Azure attestation");

    private final int value;
    private final String description;

    CoCoResultType(int valueIn, String descriptionIn) {
        value = valueIn;
        description = descriptionIn;
    }

    public int getValue() {
        return value;
    }

    /**
     * @return returns a description for the result type
     */
    public String getTypeDescription() {
        return description;
    }

    /**
     * @param valueIn the value
     * @return returns the enum type for the given value
     */
    public static CoCoResultType fromValue(int valueIn) {
        return Arrays.stream(CoCoResultType.values())
                     .filter(e -> e.getValue() == valueIn)
                     .findFirst()
                     .orElseThrow(() -> new IllegalArgumentException("Invalid CoCoResultType value " + valueIn));
    }
}
