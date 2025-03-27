/*
 * Copyright (c) 2025 SUSE LLC
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
import com.suse.oval.cpe.Cpe;
import com.suse.oval.cpe.CpeBuilder;
import com.suse.oval.ovaltypes.BaseCriteria;
import com.suse.oval.ovaltypes.CriterionType;
import com.suse.oval.ovaltypes.DefinitionClassEnum;
import com.suse.oval.ovaltypes.DefinitionType;

import java.util.Collections;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class UbuntuVulnerablePackageExtractor extends CriteriaTreeBasedExtractor {
    private static final Pattern AFFECTED_PACKAGE_REGEX =
            Pattern.compile("(?<packageName>\\S+) package in .* is affected and may need fixing.");
    private static final Pattern PATCHED_PACKAGE_REGEX =
            Pattern.compile("(?<packageName>\\S+) package in .*, is related to the CVE in some way and has " +
                    "been fixed \\(note: '(?<evr>\\S+)'\\).");
    protected UbuntuVulnerablePackageExtractor(DefinitionType definitionIn) {
        super(definitionIn);
    }

    @Override
    protected List<ProductVulnerablePackages> extractItem(BaseCriteria criteria) {
        CriterionType criterionType = (CriterionType) criteria;

        Matcher affectedPackageMatcher = AFFECTED_PACKAGE_REGEX.matcher(criterionType.getComment());
        Matcher patchedPackageMatcher = PATCHED_PACKAGE_REGEX.matcher(criterionType.getComment());

        VulnerablePackage vulnerablePackage;
        if (affectedPackageMatcher.matches()) {
            vulnerablePackage = extractAffectedPackage(affectedPackageMatcher);
        }
        else if (patchedPackageMatcher.matches()) {
            vulnerablePackage = extractPatchedPackage(patchedPackageMatcher);
        }
        else {
            return Collections.emptyList();
        }

        ProductVulnerablePackages productVulnerablePackages = new ProductVulnerablePackages();
        productVulnerablePackages.setProductCpe(deriveCpe().asString());
        productVulnerablePackages.setVulnerablePackages(List.of(vulnerablePackage));
        productVulnerablePackages.setSingleCve(definition.getSingleCve().orElseThrow());

        return List.of(productVulnerablePackages);
    }

    private Cpe deriveCpe() {
        String osVersion = definition.getOsVersion();

        return new CpeBuilder()
                .withVendor("canonical")
                .withProduct("ubuntu_linux")
                .withVersion(osVersion)
                .build();
    }

    private VulnerablePackage extractPatchedPackage(Matcher patchedPackageMatcherIn) {
        if (patchedPackageMatcherIn.groupCount() != 2) {
            throw new IllegalStateException();
        }

        String packageName = patchedPackageMatcherIn.group("packageName");
        String evr = patchedPackageMatcherIn.group("evr");

        VulnerablePackage vulnerablePackage = new VulnerablePackage();
        vulnerablePackage.setName(packageName);
        vulnerablePackage.setFixVersion(evr);

        return vulnerablePackage;
    }

    private VulnerablePackage extractAffectedPackage(Matcher affectedPackageMatcherIn) {
        if (affectedPackageMatcherIn.groupCount() != 1) {
            throw new IllegalStateException();
        }

        String packageName = affectedPackageMatcherIn.group("packageName");

        VulnerablePackage vulnerablePackage = new VulnerablePackage();
        vulnerablePackage.setName(packageName);
        vulnerablePackage.setFixVersion(null);

        return vulnerablePackage;
    }

    @Override
    protected boolean test(BaseCriteria criteria) {
        if (!(criteria instanceof CriterionType criterionType)) {
            return false;
        }

        return AFFECTED_PACKAGE_REGEX.asMatchPredicate().test(criterionType.getComment()) ||
                PATCHED_PACKAGE_REGEX.asMatchPredicate().test(criterionType.getComment());
    }

    @Override
    public boolean isValidDefinition(DefinitionType definitionIn) {

        boolean isDebianOVAL = definitionIn.getOsFamily() == OsFamily.UBUNTU;
        boolean isVulnerabilityDefinition = definitionIn.getDefinitionClass() == DefinitionClassEnum.VULNERABILITY;

        return super.isValidDefinition(definitionIn) && isDebianOVAL && isVulnerabilityDefinition;
    }
}
