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
 */

package com.suse.oval.vulnerablepkgextractor;

import com.redhat.rhn.domain.rhnpackage.PackageEvr;

import com.suse.oval.OsFamily;
import com.suse.oval.cpe.Cpe;
import com.suse.oval.cpe.CpeBuilder;
import com.suse.oval.ovaltypes.BaseCriteria;
import com.suse.oval.ovaltypes.CriterionType;
import com.suse.oval.ovaltypes.DefinitionClassEnum;
import com.suse.oval.ovaltypes.DefinitionType;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.util.Collections;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class UbuntuVulnerablePackageExtractor extends CriteriaTreeBasedExtractor {

    private static final Logger LOG = LogManager.getLogger(UbuntuVulnerablePackageExtractor.class);


    private static final Pattern AFFECTED_PACKAGE_REGEX =
            Pattern.compile("(?<packageName>\\S+) package in .* is affected and may need fixing.");
    private static final Pattern AFFECTED_PACKAGE_REGEX_V2 =
            Pattern.compile("(?<packageName>\\S+) package in .* is affected and needs fixing.");
    private static final Pattern AFFECTED_PACKAGE_REGEX_V3 =
            Pattern.compile("(?<packageName>\\S+) package in .* is affected, " +
                    "but a decision has been made to defer addressing it.*");
    private static final Pattern PATCHED_PACKAGE_REGEX =
            Pattern.compile("(?<packageName>\\S+) package in .*, is related to the CVE in some way and has " +
                    "been fixed \\(note: '(?<evr>\\S+)'\\).");
    private static final Pattern PATCHED_PACKAGE_REGEX_V2 =
            Pattern.compile("(?<packageName>\\S+) package in .* was vulnerable but has " +
                    "been fixed \\(note: '(?<evr>\\S+)'\\).");

    // sqlite3 package in noble is affected and needs fixing
    // krb5 package in noble is affected, but a decision has been made to defer addressing it (note: '2025-04-17')

    protected UbuntuVulnerablePackageExtractor(DefinitionType definitionIn) {
        super(definitionIn);
    }

    @Override
    protected List<ProductVulnerablePackages> extractItem(BaseCriteria criteria) {
        CriterionType criterionType = (CriterionType) criteria;

        Matcher affectedPackageMatcher = AFFECTED_PACKAGE_REGEX.matcher(criterionType.getComment());
        Matcher affectedPackageMatcherV2 = AFFECTED_PACKAGE_REGEX_V2.matcher(criterionType.getComment());
        Matcher affectedPackageMatcherV3 = AFFECTED_PACKAGE_REGEX_V3.matcher(criterionType.getComment());
        Matcher patchedPackageMatcher = PATCHED_PACKAGE_REGEX.matcher(criterionType.getComment());
        Matcher patchedPackageMatcherV2 = PATCHED_PACKAGE_REGEX_V2.matcher(criterionType.getComment());

        VulnerablePackage vulnerablePackage;
        if (affectedPackageMatcher.matches()) {
            vulnerablePackage = extractAffectedPackage(affectedPackageMatcher);
        }
        else if (affectedPackageMatcherV2.matches()) {
            vulnerablePackage = extractAffectedPackage(affectedPackageMatcherV2);
        }
        else if (affectedPackageMatcherV3.matches()) {
            vulnerablePackage = extractAffectedPackage(affectedPackageMatcherV3);
        }
        else if (patchedPackageMatcher.matches()) {
            vulnerablePackage = extractPatchedPackage(patchedPackageMatcher);
        }
        else if (patchedPackageMatcherV2.matches()) {
            vulnerablePackage = extractPatchedPackage(patchedPackageMatcherV2);
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
        vulnerablePackage.setFixVersion(PackageEvr.parseDebian(evr));

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

        boolean matches =  AFFECTED_PACKAGE_REGEX.asMatchPredicate().test(criterionType.getComment()) ||
                AFFECTED_PACKAGE_REGEX_V2.asMatchPredicate().test(criterionType.getComment()) ||
                AFFECTED_PACKAGE_REGEX_V3.asMatchPredicate().test(criterionType.getComment()) ||
                PATCHED_PACKAGE_REGEX.asMatchPredicate().test(criterionType.getComment()) ||
                PATCHED_PACKAGE_REGEX_V2.asMatchPredicate().test(criterionType.getComment());
        if (!matches) {
            LOG.debug("Criteria didn't match any parsing regex: {}", criterionType.getComment());
        }
        return matches;
    }

    @Override
    public boolean isValidDefinition(DefinitionType definitionIn) {

        boolean isDebianOVAL = definitionIn.getOsFamily() == OsFamily.UBUNTU;
        boolean isVulnerabilityDefinition = definitionIn.getDefinitionClass() == DefinitionClassEnum.VULNERABILITY;

        return super.isValidDefinition(definitionIn) && isDebianOVAL && isVulnerabilityDefinition;
    }
}
