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

package com.redhat.rhn.manager.audit;

import com.suse.oval.OsFamily;

import java.util.Optional;

public class OsReleasePair {
    private final String os;
    private final String osRelease;

    /**
     * Default Constructor
     * @param osIn the name of the OS
     * @param osReleaseIn the OS release version
     * */
    public OsReleasePair(String osIn, String osReleaseIn) {
        os = osIn;
        osRelease = osReleaseIn;
    }

    public String getOs() {
        return os;
    }

    public String getOsRelease() {
        return osRelease;
    }

    /**
     * Derives an {@link com.redhat.rhn.manager.audit.CVEAuditManagerOVAL.OVALProduct} object based on the server's
     * information, including the operating system name and release version. Used by the OVAL synchronization process
     * to identify the installed product and synchronize OVAL data for it.
     *
     * @return the information of the installed product to synchronize OVAL data for if the product is eligible for
     * OVAL synchronization and {@code Optional.empty} otherwise
     * (for example product maintainers don't produce OVAL data)
     * */
    public Optional<CVEAuditManagerOVAL.OVALProduct> toOVALProduct() {
        return OsFamily.fromOsName(os).flatMap(serverOsFamily -> {
            String serverOsRelease = getOsRelease();
            if (serverOsFamily == OsFamily.REDHAT_ENTERPRISE_LINUX ||
                    serverOsFamily == OsFamily.SUSE_LINUX_ENTERPRISE_SERVER ||
                    serverOsFamily == OsFamily.SUSE_LINUX_ENTERPRISE_DESKTOP) {
                // Removing the minor version part: 15.6 --> 15
                serverOsRelease = serverOsRelease.replaceFirst("\\..*$", "");
            }

            CVEAuditManagerOVAL.OVALProduct ovalProduct = null;
            if (serverOsFamily.isSupportedRelease(serverOsRelease)) {
                ovalProduct = new CVEAuditManagerOVAL.OVALProduct(serverOsFamily, serverOsRelease);
            }

            return Optional.ofNullable(ovalProduct);
        });
    }


}
