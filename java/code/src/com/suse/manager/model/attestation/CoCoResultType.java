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

import com.redhat.rhn.common.localization.LocalizationService;

import java.util.Arrays;

public enum CoCoResultType {
    NONE(0),
    SEV_SNP(1),
    SECURE_BOOT(2),
    AZURE_SEV_SNP(3),
    AZURE_SECURE_BOOT(4),
    AZURE_DISK_ENCRYPTED(5);
    // ATTENTION: KEEP CoCoAttestationReport_queries.xml up to date !

    private final int value;
    private final String labelKey;
    private final String descriptionKey;

    CoCoResultType(int valueIn) {
        value = valueIn;
        labelKey = "coco.resultType." + name().toLowerCase() + ".label";
        descriptionKey = "coco.resultType." + name().toLowerCase() + ".description";
    }

    public int getValue() {
        return value;
    }

    /**
     * @return returns a description for the result type
     */
    public String getTypeLabel() {
        return LocalizationService.getInstance().getMessage(labelKey);
    }

    /**
     * @return returns a description for the result type
     */
    public String getTypeDescription() {
        return LocalizationService.getInstance().getMessage(descriptionKey);
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
