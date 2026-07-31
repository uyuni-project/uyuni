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
     * Creates a PackageMetadata object from a package diff comparison
     *
     * @param packageDiffComparison the comparison index
     * @param systemIn              PackageListItem for the current system
     * @param otherIn               PackageListItem for the profile or other system
     * @return the created PackageMetadata
     */
    public static PackageMetadata createFromPackageDiffComparison(int packageDiffComparison,
                                                                  PackageListItem systemIn, PackageListItem otherIn) {
        return switch (packageDiffComparison) {
            case -2 -> new PackageMetadataOtherOnly(systemIn, otherIn, null);
            case -1 -> new PackageMetadataOtherNewer(systemIn, otherIn, null);
            case 1 -> new PackageMetadataThisNewer(systemIn, otherIn, null);
            case 2 -> new PackageMetadataThisOnly(systemIn, otherIn, null);
            default -> new PackageMetadataNoDiff(systemIn, otherIn, null);
        };
    }

    /**
     * Creates a PackageMetadata object from a package evr comparison
     *
     * @param packageEvrComparison the comparison index
     * @param systemIn             PackageListItem for the current system
     * @param otherIn              PackageListItem for the profile or other system
     * @param compareParamIn       The parameter to the comparison string.
     * @return the created PackageMetadata
     */
    public static PackageMetadata createFromPackageEvrComparison(int packageEvrComparison,
                                                                 PackageListItem systemIn, PackageListItem otherIn,
                                                                 String compareParamIn) {
        if (packageEvrComparison < 0) {
            return new PackageMetadataOtherNewer(systemIn, otherIn, compareParamIn);
        }

        if (packageEvrComparison > 0) {
            return new PackageMetadataThisNewer(systemIn, otherIn, compareParamIn);
        }

        return new PackageMetadataNoDiff(systemIn, otherIn, compareParamIn);
    }
}
