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
 * Red Hat trademarks are not licensed under GPLv2. No permission is
 * granted to use or replicate Red Hat trademarks that are incorporated
 * in this software or its documentation.
 */

package com.redhat.rhn.domain.user;

/**
 * Address type enumeration representing the different types of addresses
 * that can be stored in the WEB_USER_SITE_INFO table.
 */
public enum AddressType {
    ADDRESS_TYPE_MARKETING("M", "MARKET"),
    ADDRESS_TYPE_BILLING("B", "BILL_TO"),
    ADDRESS_TYPE_SHIPPING("S", "SHIP_TO"),
    ADDRESS_TYPE_SERVICE("R", "SERVICE");

    private final String type;
    private final String description;

    AddressType(String typeIn, String descriptionIn) {
        type = typeIn;
        description = descriptionIn;
    }

    /**
     * Static method to get the AddressType enum value from the database code.
     *
     * @param code the single character database code
     * @return the corresponding AddressType, or null if not found
     */
    public static AddressType fromCode(String code) {
        for (AddressType addressType : AddressType.values()) {
            if (addressType.type.equals(code)) {
                return addressType;
            }
        }
        return null;
    }

    public String getDescription() {
        return description;
    }

    public String getType() {
        return type;
    }
}
