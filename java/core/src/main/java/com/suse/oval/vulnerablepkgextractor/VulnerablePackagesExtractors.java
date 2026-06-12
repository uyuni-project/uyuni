/*
 * Copyright (c) 2023 SUSE LLC
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

package com.suse.oval.vulnerablepkgextractor;

import com.suse.oval.OsFamily;
import com.suse.oval.manager.OVALResourcesCache;
import com.suse.oval.ovaltypes.DefinitionClassEnum;
import com.suse.oval.ovaltypes.DefinitionType;
import com.suse.oval.vulnerablepkgextractor.redhat.RedHatVulnerablePackageExtractorFromPatchDefinition;
import com.suse.oval.vulnerablepkgextractor.redhat.RedHatVulnerablePackageExtractorFromVulnerabilityDefinition;

import java.util.Optional;

/**
 * A factory for {@link VulnerablePackagesExtractor}
 * */
public class VulnerablePackagesExtractors {

    private VulnerablePackagesExtractors() {
    }
    /**
     * Create and returns a {@link VulnerablePackagesExtractor} instance based on the given {@code osFamily} argument
     *
     * @param definition the definition to extract vulnerable packages from
     * @param osFamily the os family
     * @param ovalResourcesCache a helper class to lookup OVAL resources efficiently
     * @return a vulnerable package extractor instance
     * */
    public static Optional<VulnerablePackagesExtractor> create(DefinitionType definition, OsFamily osFamily,
                                                                 OVALResourcesCache ovalResourcesCache) {
        switch (osFamily) {
            case LEAP,
                 SUSE_LINUX_ENTERPRISE_SERVER, SUSE_LINUX_ENTERPRISE_DESKTOP, SUSE_LINUX_ENTERPRISE_MICRO,
                 SUSE_LIBERTY_LINUX:
                return Optional.of(new SUSEVulnerablePackageExtractor(definition, ovalResourcesCache));
            case DEBIAN:
                return Optional.of(new DebianVulnerablePackagesExtractor(definition));
            case REDHAT_ENTERPRISE_LINUX, ALMA_LINUX:
                if (definition.getDefinitionClass() == DefinitionClassEnum.VULNERABILITY) {
                    return Optional.of(new RedHatVulnerablePackageExtractorFromVulnerabilityDefinition(definition));
                }
                else if (definition.getDefinitionClass() == DefinitionClassEnum.PATCH) {
                    if (definition.getCves().isEmpty()) {
                        // If a patch definition has no CVEs (e.g. bugfix or enhancement errata),
                        // it does not contain vulnerability mappings, so we cleanly return empty.
                        return Optional.empty();
                    }
                    return Optional.of(new RedHatVulnerablePackageExtractorFromPatchDefinition(definition));
                }
                else {
                    throw new IllegalArgumentException(
                            "Only VULNERABILITY and PATCH definitions are allowed for RedHat OVALs");
                }
            case UBUNTU:
                if (definition.getDefinitionClass() == DefinitionClassEnum.VULNERABILITY) {
                    return Optional.of(new UbuntuVulnerablePackageExtractor(definition));
                }
                else if (definition.getDefinitionClass() == DefinitionClassEnum.INVENTORY) {
                    // Inventory is a valid definition class for Ubuntu OVALs,
                    // but it doesn't contain any vulnerable package information,
                    // so we return an empty Optional here.
                    return Optional.empty();
                }
                else {
                    throw new IllegalArgumentException(
                            "Only VULNERABILITY and INVENTORY definitions are allowed for Ubuntu OVALs");
                }
            default:
                throw new IllegalArgumentException(
                        "Cannot find any vulnerable packages extractor implementation for " + osFamily);
        }
    }
}
