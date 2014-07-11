/**
 * Copyright (c) 2014 SUSE
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
import java.util.List;

/**
 * SUSE Manager system entitlements
 */
public enum SystemEntitlement {
    SM_ENT_MON_S("monitoring_entitled"),
    SM_ENT_PROV_S("provisioning_entitled"),
    SM_ENT_MGM_S("enterprise_entitled",
                 "bootstrap_entitled"),
    SM_ENT_MGM_V("virtualization_host_platform",
                 "enterprise_entitled",
                 "bootstrap_entitled"),
    SM_ENT_MON_V("monitoring_entitled"),
    SM_ENT_PROV_V("provisioning_entitled"),
    SM_ENT_MON_Z("monitoring_entitled"),
    SM_ENT_PROV_Z("provisioning_entitled"),
    SM_ENT_MGM_Z("enterprise_entitled",
                 "bootstrap_entitled");

    private final List<String> entitlements;

    SystemEntitlement(String... entitlements) {
        this.entitlements = Collections.unmodifiableList(
                new ArrayList<String>(Arrays.asList(entitlements)));
    }

    /**
     * Get entitlements assigned to the product class.
     * @return List of entitlement flags.
     */
    public List<String> getEntitlements() {
        return this.entitlements;
    }
}
