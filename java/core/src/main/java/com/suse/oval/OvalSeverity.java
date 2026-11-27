/*
 * Copyright (c) 2026 SUSE LLC
 *
 * This software is licensed to you under the GNU General Public License,
 * version 2 (GPLv2). There is NO WARRANTY for this software, express or
 * implied, including the implied warranties of MERCHANTABILITY or FITNESS
 * FOR A PARTICULAR PURPOSE. You should have received a copy of GPLv2
 * along with this software; if not, see
 * http://www.gnu.org/licenses/old-licenses/gpl-2.0.txt.
 *
 * SPDX-License-Identifier: GPL-2.0-only
 */

package com.suse.oval;

import java.util.Locale;

/**
 * CVE severity from the OVAL vulnerability definitions, normalized over the OVAL vendors.
 * The rank is used for sorting, higher means more severe.
 */
public enum OvalSeverity {
    CRITICAL(4),
    IMPORTANT(3),
    MODERATE(2),
    LOW(1),
    UNKNOWN(0);

    private final int rank;

    OvalSeverity(int rankIn) {
        rank = rankIn;
    }

    /**
     * @return the rank, higher means more severe
     */
    public int getRank() {
        return rank;
    }

    /**
     * Normalize the severity written by an OVAL vendor.
     * SUSE and Red Hat use Critical, Important, Moderate and Low. Ubuntu uses High, Medium and Negligible
     * in place of the last three. Anything else, like 'Not set' or 'Untriaged', is UNKNOWN.
     *
     * @param ovalSeverity the severity text from the OVAL file, may be null
     * @return the normalized severity
     */
    public static OvalSeverity fromOval(String ovalSeverity) {
        if (ovalSeverity == null) {
            return UNKNOWN;
        }
        return switch (ovalSeverity.trim().toLowerCase(Locale.ROOT)) {
            case "critical" -> CRITICAL;
            case "important", "high" -> IMPORTANT;
            case "moderate", "medium" -> MODERATE;
            case "low", "negligible" -> LOW;
            default -> UNKNOWN;
        };
    }

    /**
     * @return the value stored in the database, null for UNKNOWN
     */
    public String toDbValue() {
        return this == UNKNOWN ? null : name();
    }
}
