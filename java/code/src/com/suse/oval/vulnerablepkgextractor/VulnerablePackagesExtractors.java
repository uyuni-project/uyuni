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
import com.suse.oval.manager.OVALLookupHelper;
import com.suse.oval.ovaltypes.DefinitionClassEnum;
import com.suse.oval.ovaltypes.DefinitionType;
import com.suse.oval.vulnerablepkgextractor.redhat.RedHatVulnerablePackageExtractorFromPatchDefinition;
import com.suse.oval.vulnerablepkgextractor.redhat.RedHatVulnerablePackageExtractorFromVulnerabilityDefinition;

/**
 * A factory for {@link VulnerablePackagesExtractor}
 * */
public class VulnerablePackagesExtractors {

    public static VulnerablePackagesExtractor create(DefinitionType definition, OsFamily osFamily,
                                                     OVALLookupHelper ovalLookupHelper) {
        switch (osFamily) {
            case openSUSE_LEAP:
            case SUSE_LINUX_ENTERPRISE_SERVER:
            case SUSE_LINUX_ENTERPRISE_DESKTOP:
                return new SUSEVulnerablePackageExtractor(definition, ovalLookupHelper);
            case DEBIAN:
                return new DebianVulnerablePackagesExtractor(definition);
            case REDHAT_ENTERPRISE_LINUX:
                if (definition.getDefinitionClass() == DefinitionClassEnum.VULNERABILITY) {
                    return new RedHatVulnerablePackageExtractorFromVulnerabilityDefinition(definition);
                } else if (definition.getDefinitionClass() == DefinitionClassEnum.PATCH) {
                    return new RedHatVulnerablePackageExtractorFromPatchDefinition(definition);
                }
            default:
                throw new IllegalArgumentException(
                        "Cannot find any vulnerable packages extractor implementation for " + osFamily);
        }
    }
}
