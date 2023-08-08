package com.suse.oval.vulnerablepkgextractor;

import com.suse.oval.ovaltypes.CriteriaType;
import com.suse.oval.ovaltypes.DefinitionType;

import java.util.List;
import java.util.Objects;

/**
 * This interface responsible for the extraction of vulnerable packages and their fix versions from an OVAL vulnerability definition.
 * The extraction process depends on the way the vulnerability criteria tree is structured. Therefore, the implementations
 * differ depending on the source of the OVAL definition (SUSE, Ubuntu, etc.)
 */
public interface VulnerablePackagesExtractor {
    List<ProductVulnerablePackages> extract();
}
