package com.suse.oval.vulnerablepkgextractor;

import com.suse.oval.OsFamily;
import com.suse.oval.db.OVALDefinition;
import com.suse.oval.ovaltypes.DefinitionType;

public class VulnerablePackagesExtractors {

    public static AbstractVulnerablePackagesExtractor create(DefinitionType definition, OsFamily osFamily) {
        switch (osFamily) {
            case openSUSE_LEAP:
            case SUSE_LINUX_ENTERPRISE_SERVER:
            case SUSE_LINUX_ENTERPRISE_DESKTOP:
                return new SUSEVulnerablePackageExtractor(definition);
            case DEBIAN:
                return new DebianVulnerablePackagesExtractor(definition);
            default:
                throw new IllegalArgumentException(
                        "Cannot find any vulnerable packages extractor implementation for " + osFamily);
        }
    }
}
