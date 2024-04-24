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
import java.util.List;

public enum CoCoEnvironmentType {
    NONE(0, List.of()),
    KVM_AMD_EPYC_MILAN(1, List.of(CoCoResultType.SEV_SNP, CoCoResultType.SECURE_BOOT)),
    KVM_AMD_EPYC_GENOA(2, List.of(CoCoResultType.SEV_SNP, CoCoResultType.SECURE_BOOT));
    // ATTENTION: KEEP CoCoAttestationReport_queries.xml up to date !

    private final long value;
    private final String labelKey;
    private final String descriptionKey;
    private final List<CoCoResultType> supportedResultTypes;

    CoCoEnvironmentType(long valueIn, List<CoCoResultType> supportedResultTypesIn) {
        value = valueIn;
        supportedResultTypes = supportedResultTypesIn;
        labelKey = "coco.environment." + name().toLowerCase() + ".label";
        descriptionKey = "coco.environment." + name().toLowerCase() + ".description";
    }

    public long getValue() {
        return value;
    }

    public String getLabel() {
        return LocalizationService.getInstance().getMessage(labelKey);
    }

    public String getDescription() {
        return LocalizationService.getInstance().getMessage(descriptionKey);
    }

    /**
     * @return returns the list of supported {@link CoCoResultType} for this environment
     */
    public List<CoCoResultType> getSupportedResultTypes() {
        return supportedResultTypes;
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

    /**
     * @return returns if a nonce value is required
     */
    public boolean isNonceRequired() {
        return List.of(KVM_AMD_EPYC_MILAN, KVM_AMD_EPYC_GENOA).contains(this);
    }
}
