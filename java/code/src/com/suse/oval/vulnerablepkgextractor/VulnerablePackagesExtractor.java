package com.suse.oval.vulnerablepkgextractor;

import com.suse.oval.ovaltypes.CriteriaType;
import com.suse.oval.ovaltypes.DefinitionType;

import java.util.List;
import java.util.Objects;

/**
 * A vulnerable package extractor is used to extract the list of vulnerable packages from an OVAL definition.
 * Vulnerable packages are packages that could expose a certain system to a CVE, in other words, these are packages
 * that if found on a system (without their patched version), then that system is considered vulnerable to the CVE.
 * <p>
 * Although OVAL is a standard specification, OVAL providers got a little bit creative in the way they structure their
 * OVAL, thus, we need a different implementation for each distribution.
 */
public interface VulnerablePackagesExtractor {
    List<ProductVulnerablePackages> extract();

    void assertDefinitionIsValid(DefinitionType definition);
}
