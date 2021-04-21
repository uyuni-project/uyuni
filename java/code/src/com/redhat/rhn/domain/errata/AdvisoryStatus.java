/**
 * Copyright (c) 2021 SUSE LLC
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
package com.redhat.rhn.domain.errata;

import java.util.Arrays;
import java.util.Optional;

/**
 * AdvisoryStatus
 */
public enum AdvisoryStatus {
    FINAL("final"),
    STABLE("stable"),
    TESTING("testing"),
    RETRACTED("retracted");

    private final String metadataValue;

    /**
     * Constructor
     * @param metadataValueIn metadata
     */
    AdvisoryStatus(String metadataValueIn) {
        this.metadataValue = metadataValueIn;
    }

    /**
     * @return advisory status as string
     */
    public String getMetadataValue() {
        return metadataValue;
    }

    /**
     * Map string to AdvisoryStatus
     * @param value as string value
     * @return AdvisoryStatus as Optional
     */
    public static Optional<AdvisoryStatus> fromMetadata(String value) {
        return Arrays.stream(values()).filter(e -> e.metadataValue.equals(value)).findFirst();
    }
}
