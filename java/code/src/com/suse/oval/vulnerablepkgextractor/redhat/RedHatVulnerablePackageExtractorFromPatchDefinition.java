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

package com.suse.oval.vulnerablepkgextractor.redhat;

import com.suse.oval.OsFamily;
import com.suse.oval.ovaltypes.Advisory;
import com.suse.oval.ovaltypes.BaseCriteria;
import com.suse.oval.ovaltypes.CriterionType;
import com.suse.oval.ovaltypes.DefinitionClassEnum;
import com.suse.oval.ovaltypes.DefinitionType;
import com.suse.oval.vulnerablepkgextractor.CriteriaTreeBasedExtractor;
import com.suse.oval.vulnerablepkgextractor.ProductVulnerablePackages;
import com.suse.oval.vulnerablepkgextractor.VulnerablePackage;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Vulnerable packages extractor for patch definitions
 * from: <a href="https://www.redhat.com/security/data/oval/v2/">RedHat OVAL</a>
 * */
public class RedHatVulnerablePackageExtractorFromPatchDefinition extends CriteriaTreeBasedExtractor {
    private static final Pattern REDHAT_PACKAGE_REGEX = Pattern
            .compile("(?<packageName>\\S+) is earlier than (?<evr>.*)");
    private static final Logger LOG = LogManager.getLogger(RedHatVulnerablePackageExtractorFromPatchDefinition.class);

    /**
     * Standard constructor
     *
     * @param patchDefinition the patch definition to extract vulnerable packages from
     * */
    public RedHatVulnerablePackageExtractorFromPatchDefinition(DefinitionType patchDefinition) {
        super(patchDefinition);
        assertDefinitionIsValid(patchDefinition);
    }

    @Override
    protected List<ProductVulnerablePackages> extractItem(BaseCriteria criteria) {
        CriterionType criterionType = (CriterionType) criteria;

        Matcher matcher = REDHAT_PACKAGE_REGEX.matcher(criterionType.getComment());

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
        vulnerablePackage.setFixVersion(evr);

        List<String> affectedCpeList = definition.getMetadata().getAdvisory().map(Advisory::getAffectedCpeList)
                .orElse(Collections.emptyList());
        if (affectedCpeList.isEmpty()) {
            LOG.warn("RedHat affected CPE list is not meant to be empty");
        }

        List<ProductVulnerablePackages> result = new ArrayList<>();
        for (String affectedCpe : affectedCpeList) {
            // o implies 'operating system', the OVAL could also describe applications
            if (!affectedCpe.startsWith("cpe:/o:")) {
                continue;
            }
            ProductVulnerablePackages productVulnerablePackages = new ProductVulnerablePackages();
            productVulnerablePackages.setProductCpe(affectedCpe);
            productVulnerablePackages.setVulnerablePackages(List.of(vulnerablePackage));
            productVulnerablePackages.setCves(definition.getCves());

            result.add(productVulnerablePackages);
        }

        return result;
    }

    @Override
    protected boolean test(BaseCriteria criteria) {
        if (!(criteria instanceof CriterionType)) {
            return false;
        }

        CriterionType criterionType = (CriterionType) criteria;
        return REDHAT_PACKAGE_REGEX.asMatchPredicate().test(criterionType.getComment());
    }

    @Override
    public void assertDefinitionIsValid(DefinitionType definitionIn) {
        assert definitionIn.getDefinitionClass() == DefinitionClassEnum.PATCH;
        assert definitionIn.getOsFamily() == OsFamily.REDHAT_ENTERPRISE_LINUX;

        assert !definitionIn.getCves().isEmpty();
    }
}
