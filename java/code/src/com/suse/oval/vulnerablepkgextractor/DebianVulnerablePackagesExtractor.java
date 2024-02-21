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

/**
 * Vulnerable package extractor for OVALs from: <a href="https://www.debian.org/security/oval/">Debian OVAL</a>
 * */
public class DebianVulnerablePackagesExtractor extends CriteriaTreeBasedExtractor {
    private static final Pattern DEBIAN_PACKAGE_REGEX = Pattern
            .compile("(?<packageName>\\S+) DPKG is earlier than (?<evr>.*)");

    /**
     * Standard constructor
     *
     * @param vulnerabilityDefinition the vulnerability definition to extract vulnerable packages from
     * */
    public DebianVulnerablePackagesExtractor(DefinitionType vulnerabilityDefinition) {
        super(vulnerabilityDefinition);
    }

    @Override
    protected List<ProductVulnerablePackages> extractItem(BaseCriteria criteria) {
        CriterionType criterionType = (CriterionType) criteria;

        Matcher matcher = DEBIAN_PACKAGE_REGEX.matcher(criterionType.getComment());

        // Although we know the comment matches the regex (see test()), we need to call matches() otherwise
        // Matcher#group throws an exception
        matcher.matches();

        if (matcher.groupCount() != 2) {
            return Collections.emptyList();
        }

        String packageName = matcher.group("packageName");
        String evr = matcher.group("evr");

        VulnerablePackage vulnerablePackage = new VulnerablePackage();
        vulnerablePackage.setName(packageName);
        if ("0".equals(evr)) {
            // Affected/unpatched package
            vulnerablePackage.setFixVersion(null);
        }
        else {
            vulnerablePackage.setFixVersion(evr);
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
                .withVendor("debian")
                .withProduct("debian_linux")
                .withVersion(osVersion)
                .build();
    }

    @Override
    protected boolean test(BaseCriteria criteria) {
        if (!(criteria instanceof CriterionType)) {
            return false;
        }

        CriterionType criterionType = (CriterionType) criteria;
        return DEBIAN_PACKAGE_REGEX.asMatchPredicate().test(criterionType.getComment());
    }

    @Override
    public boolean isValidDefinition(DefinitionType definitionTypeIn) {
        boolean isDebianOVAL = definitionTypeIn.getOsFamily() == OsFamily.DEBIAN;
        boolean isVulnerabilityDefinition = definitionTypeIn.getDefinitionClass() == DefinitionClassEnum.VULNERABILITY;

        return super.isValidDefinition(definitionTypeIn) && isDebianOVAL && isVulnerabilityDefinition;
    }
}
