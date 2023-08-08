package com.suse.oval.vulnerablepkgextractor;

import com.suse.oval.OsFamily;
import com.suse.oval.ovaltypes.DefinitionClassEnum;
import com.suse.oval.ovaltypes.DefinitionType;
import com.suse.oval.vulnerablepkgextractor.redhat.RedHatVulnerablePackageExtractorFromPatchDefinition;
import com.suse.oval.vulnerablepkgextractor.redhat.RedHatVulnerablePackageExtractorFromVulnerabilityDefinition;

public class VulnerablePackagesExtractors {

    public static VulnerablePackagesExtractor create(DefinitionType definition, OsFamily osFamily) {
        switch (osFamily) {
            case openSUSE_LEAP:
            case SUSE_LINUX_ENTERPRISE_SERVER:
            case SUSE_LINUX_ENTERPRISE_DESKTOP:
                return new SUSEVulnerablePackageExtractor(definition);
            case DEBIAN:
                return new DebianVulnerablePackagesExtractor(definition);
            case REDHAT_ENTERPRISE_LINUX:
                if (definition.getDefinitionClass() == DefinitionClassEnum.VULNERABILITY) {
                    return new RedHatVulnerablePackageExtractorFromVulnerabilityDefinition(definition);
                } else if (definition.getDefinitionClass() == DefinitionClassEnum.PATCH){
                    return new RedHatVulnerablePackageExtractorFromPatchDefinition(definition);
                }
            default:
                throw new IllegalArgumentException(
                        "Cannot find any vulnerable packages extractor implementation for " + osFamily);
        }
    }
}
