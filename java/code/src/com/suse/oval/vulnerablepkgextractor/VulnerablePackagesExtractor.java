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

import com.suse.oval.ovaltypes.DefinitionType;

import java.util.List;

/**
 * A vulnerable package extractor is used to extract the list of vulnerable packages from an OVAL definition.
 * Vulnerable packages are packages that could expose a certain system to a CVE, in other words, these are packages
 * that if found on a system (without their patched version), then that system is considered vulnerable to the CVE.
 * <p>
 * Although OVAL is a standard specification, OVAL providers got a little bit creative in the way they structure their
 * OVAL, thus, we need a different implementation for each distribution.
 */
public interface VulnerablePackagesExtractor {
    /**
     * Extracts the list of vulnerable packages along the vulnerable product and CVE.
     *
     * @return a list of ProductVulnerablePackages. It's a list because an OVAL definition could encapsulate
     * vulnerability information for multiple products. In that case, each item in the returned list gives the list of
     * vulnerable packages for one product.
     * */
    List<ProductVulnerablePackages> extract();

    /**
     * Following the fail fast principle, we assert that the current extractor implementation is capable of extorting
     * from the given OVAL definition. This method to be called as soon as possible, preferably before any
     * exacting has started.
     *
     * @param definition the OVAL definition to assert we can extract from.
     * */
    void assertDefinitionIsValid(DefinitionType definition);
}
