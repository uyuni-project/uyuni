package com.suse.oval.vulnerablepkgextractor.redhat;

import com.suse.oval.OsFamily;
import com.suse.oval.cpe.Cpe;
import com.suse.oval.cpe.CpeBuilder;
import com.suse.oval.ovaltypes.BaseCriteria;
import com.suse.oval.ovaltypes.CriterionType;
import com.suse.oval.ovaltypes.DefinitionClassEnum;
import com.suse.oval.ovaltypes.DefinitionType;
import com.suse.oval.vulnerablepkgextractor.CriteriaTreeBasedExtractor;
import com.suse.oval.vulnerablepkgextractor.ProductVulnerablePackages;
import com.suse.oval.vulnerablepkgextractor.VulnerablePackage;

import java.util.Collections;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class RedHatVulnerablePackageExtractorFromPatchDefinition extends CriteriaTreeBasedExtractor {
    private static final Pattern REDHAT_PACKAGE_REGEX = Pattern
            .compile("(?<packageName>\\S+) is earlier than (?<evr>.*)");

    public RedHatVulnerablePackageExtractorFromPatchDefinition(DefinitionType patchDefinition) {
        super(patchDefinition);
        assertDefinitionIsValid(patchDefinition);
    }

    @Override
    protected List<ProductVulnerablePackages> extractItem(BaseCriteria criteria) {
        CriterionType criterionType = (CriterionType) criteria;

        Matcher matcher = REDHAT_PACKAGE_REGEX.matcher(criterionType.getComment());

        // Although we know the comment matches the regex (see test()), we need to call matches() otherwise Matcher#group
        // throws an exception
        matcher.matches();

        if (matcher.groupCount() != 2) {
            return Collections.emptyList();
        }

        String packageName = matcher.group("packageName");
        String evr = matcher.group("evr");

        VulnerablePackage vulnerablePackage = new VulnerablePackage();
        vulnerablePackage.setName(packageName);
        vulnerablePackage.setFixVersion(evr);

        ProductVulnerablePackages productVulnerablePackages = new ProductVulnerablePackages();
        productVulnerablePackages.setProductCpe(deriveCpe().asString());
        productVulnerablePackages.setVulnerablePackages(List.of(vulnerablePackage));
        productVulnerablePackages.setSingleCve(definition.getSingleCve().orElseThrow());

        return List.of(productVulnerablePackages);
    }

    @Override
    protected boolean test(BaseCriteria criteria) {
        if (!(criteria instanceof CriterionType)) {
            return false;
        }

        CriterionType criterionType = (CriterionType) criteria;
        return REDHAT_PACKAGE_REGEX.asMatchPredicate().test(criterionType.getComment());
    }

    public Cpe deriveCpe() {
        String osVersion = definition.getOsVersion();

        return new CpeBuilder()
                .withVendor("redhat")
                .withProduct("enterprise_linux")
                .withVersion(osVersion)
                .build();
    }

    @Override
    public void assertDefinitionIsValid(DefinitionType definition) {
        assert definition.getDefinitionClass() == DefinitionClassEnum.PATCH;
        assert definition.getOsFamily() == OsFamily.REDHAT_ENTERPRISE_LINUX;
    }
}
