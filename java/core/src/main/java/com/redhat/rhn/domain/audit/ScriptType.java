/*
 * Copyright (c) 2025 SUSE LLC
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
package com.redhat.rhn.domain.audit;

/**
 * Enumeration of supported custom remediation script types.
 */
public enum ScriptType {
    BASH("bash"),
    SALT("salt");

    private final String value;

    ScriptType(String valueIn) {
        this.value = valueIn;
    }

    public String getValue() {
        return value;
    }

    /**
     * Parse a string into a ScriptType.
     * @param value rule string value
     * @return the matching ScriptType or null if not found
     */
    public static ScriptType fromValue(String value) {
        if (value == null) {
            return null;
        }
        for (ScriptType type : values()) {
            if (type.value.equalsIgnoreCase(value)) {
                return type;
            }
        }
        return null;
    }
    @Override
    public String toString() {
        return value;
    }
}
