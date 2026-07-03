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
package com.redhat.rhn.frontend.dto;

public class PackageMetadataFactory {
    private PackageMetadataFactory() {
        //empty constructor
    }

    /**
     * Creates a PackageMetadata object from a pkgDiffComparison index
     *
     * @param pkgDiffComparison the comparison index
     * @param systemIn          PackageListItem for the current system
     * @param otherIn           PackageListItem for the profile or other system
     * @return the created PackageMetadata
     */
    public static PackageMetadata createFromPkgDiffComparison(int pkgDiffComparison, PackageListItem systemIn, PackageListItem otherIn) {
        return switch (pkgDiffComparison) {
            case -2 -> new PackageMetadataOtherOnly(systemIn, otherIn, null);
            case -1 -> new PackageMetadataOtherNewer(systemIn, otherIn, null);
            case 1 -> new PackageMetadataThisNewer(systemIn, otherIn, null);
            case 2 -> new PackageMetadataThisOnly(systemIn, otherIn, null);
            default -> new PackageMetadataNoDiff(systemIn, otherIn, null);
        };
    }
}
