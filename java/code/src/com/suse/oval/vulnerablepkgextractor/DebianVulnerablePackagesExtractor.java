package com.suse.oval.vulnerablepkgextractor;

import com.suse.oval.cpe.Cpe;
import com.suse.oval.cpe.CpeBuilder;
import com.suse.oval.ovaltypes.BaseCriteria;
import com.suse.oval.ovaltypes.CriterionType;
import com.suse.oval.ovaltypes.DefinitionType;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.util.Collections;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class DebianVulnerablePackagesExtractor extends CriteriaTreeBasedExtractor {
    private static Logger LOG = LogManager.getLogger(DebianVulnerablePackagesExtractor.class);
    private static final Pattern DEBIAN_PACKAGE_REGEX = Pattern
            .compile("(?<packageName>\\w+) DPKG is earlier than (?<evr>.*)");

    public DebianVulnerablePackagesExtractor(DefinitionType vulnerabilityDefinition) {
        super(vulnerabilityDefinition);
    }

    @Override
    protected List<ProductVulnerablePackages> extractItem(BaseCriteria criteria) {
        CriterionType criterionType = (CriterionType) criteria;

        Matcher matcher = DEBIAN_PACKAGE_REGEX.matcher(criterionType.getComment());

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
        productVulnerablePackages.setProduct(deriveCpe().asString());
        productVulnerablePackages.setVulnerablePackages(List.of(vulnerablePackage));
        productVulnerablePackages.setCve(definition.getCve());

        return List.of(productVulnerablePackages);
    }

    public Cpe deriveCpe() {
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
}
