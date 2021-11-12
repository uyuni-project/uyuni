/**
 * Copyright (c) 2014 SUSE LLC
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

package com.redhat.rhn.manager.content;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

/**
 * SUSE Manager system entitlements
 */
public enum SystemEntitlement {
    SM_ENT_MGM_S("enterprise_entitled",
                 "bootstrap_entitled"),
    SM_ENT_MGM_V("enterprise_entitled",
                 "bootstrap_entitled"),
    SM_ENT_MGM_Z("enterprise_entitled",
                 "bootstrap_entitled");

    private final List<String> entitlements;

    SystemEntitlement(String... entitlementsIn) {
        this.entitlements = Collections.unmodifiableList(
                new ArrayList<String>(Arrays.asList(entitlementsIn)));
    }

    /**
     * Get entitlements assigned to the product class.
     * @return List of entitlement flags.
     */
    public List<String> getEntitlements() {
        return this.entitlements;
    }

    /**
     * Return all entitlements in a consolidated list.
     * @return list of all entitlements
     */
    public static List<String> getAllEntitlements() {
       Set<String> entitlements = new HashSet<String>();
       for (SystemEntitlement value : SystemEntitlement.values()) {
          entitlements.addAll(value.getEntitlements());
       }
       return Collections.unmodifiableList(new ArrayList<String>(entitlements));
    }

    /**
     * Return all product classes that are bound to a given entitlement.
     * @param entitlement the entitlement
     * @return list of product classes
     */
    public static List<String> getProductClasses(String entitlement) {
        List<String> productClasses = new ArrayList<String>();
        for (SystemEntitlement value : SystemEntitlement.values()) {
            if (value.getEntitlements().contains(entitlement)) {
                productClasses.add(value.name());
            }
        }
        return productClasses;
    }
}
