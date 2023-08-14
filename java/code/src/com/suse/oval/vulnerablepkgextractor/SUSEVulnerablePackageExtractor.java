package com.suse.oval.vulnerablepkgextractor;

import com.suse.oval.OsFamily;
import com.suse.oval.cpe.Cpe;
import com.suse.oval.cpe.CpeBuilder;
import com.suse.oval.manager.OVALLookupHelper;
import com.suse.oval.ovaltypes.BaseCriteria;
import com.suse.oval.ovaltypes.CriteriaType;
import com.suse.oval.ovaltypes.CriterionType;
import com.suse.oval.ovaltypes.DefinitionClassEnum;
import com.suse.oval.ovaltypes.DefinitionType;
import com.suse.oval.ovaltypes.EVRType;
import com.suse.oval.ovaltypes.LogicOperatorType;
import com.suse.oval.ovaltypes.ObjectType;
import com.suse.oval.ovaltypes.StateType;
import com.suse.oval.ovaltypes.TestType;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

public class SUSEVulnerablePackageExtractor extends CriteriaTreeBasedExtractor {
    private static final Pattern RELEASE_PACKAGE_REGEX = Pattern.compile(
            "^\\s*(?<releasePackage>[-a-zA-Z_]+)\\s*.*==(?<releasePackageVersion>[0-9.]+)\\s*$");
    private static Logger LOG = LogManager.getLogger(SUSEVulnerablePackageExtractor.class);
    private final OVALLookupHelper ovalLookupHelper;

    public SUSEVulnerablePackageExtractor(DefinitionType vulnerabilityDefinition, OVALLookupHelper ovalLookupHelper) {
        super(vulnerabilityDefinition);
        Objects.requireNonNull(ovalLookupHelper);

        this.ovalLookupHelper = ovalLookupHelper;
    }

    @Override
    protected List<ProductVulnerablePackages> extractItem(BaseCriteria criteria) {
        CriteriaType criteriaType = (CriteriaType) criteria;

        BaseCriteria productCriteriaRootNode = criteriaType.getChildren().get(0);
        BaseCriteria packageCriteriaRootNode = criteriaType.getChildren().get(1);

        List<CriterionType> productCriterions = collectCriterions(productCriteriaRootNode);

        List<CriterionType> filteredPackageCriterions = collectCriterions(packageCriteriaRootNode, 1)
                .stream()
                .filter(c ->
                        c.getComment().endsWith("is installed") ||
                        c.getComment().endsWith("is affected") ||
                        c.getComment().endsWith("is not affected"))
                .collect(Collectors.toList());

        List<VulnerablePackage> vulnerablePackages = new ArrayList<>();
        for (CriterionType packageCriterion : filteredPackageCriterions) {
            String comment = packageCriterion.getComment();
            String testId = packageCriterion.getTestRef();

            TestType packageTest = ovalLookupHelper.lookupTestById(testId)
                    .orElseThrow(() -> new IllegalStateException("Referenced package test is not found: " + testId));

            String objectId = packageTest.getObjectRef();
            String stateId = packageTest.getStateRef()
                    .orElseThrow(() -> new IllegalStateException("Unexpected empty package state in SUSE OVAL"));

            ObjectType packageObject = ovalLookupHelper.lookupObjectById(objectId)
                    .orElseThrow(() -> new IllegalStateException("Referenced package object not found: " + objectId));

            StateType packageState = ovalLookupHelper.lookupStateById(stateId)
                    .orElseThrow(() -> new IllegalStateException("Referenced package state not found: " + stateId));

            String packageName = packageObject.getPackageName();

            VulnerablePackage vulnerablePackage = new VulnerablePackage();
            vulnerablePackage.setName(packageName);

            if (comment.endsWith("is installed")) {
                String evr = packageState.getPackageEVR().map(EVRType::getValue).orElse("");
                vulnerablePackage.setFixVersion(evr);
            } else if (comment.endsWith("is affected")) {
                // Affected packages don't have a fix version yet.
                vulnerablePackage.setFixVersion(null);
            } else if (comment.endsWith("is not affected")) {
                // Package 'is not affected' implies that the vulnerability is too old that all supported products
                // have the fixed package version or that a fix was backported to vulnerable products. Hence, the fix version
                // is 0.
                vulnerablePackage.setFixVersion("0:0-0");
            }

            vulnerablePackages.add(vulnerablePackage);
        }

        List<ProductVulnerablePackages> result = new ArrayList<>();
        for (CriterionType productCriterion : productCriterions) {
            String comment = productCriterion.getComment();
            String productUserFriendlyName = comment.replace(" is installed", "");
            TestType productTest = ovalLookupHelper.lookupTestById(productCriterion.getTestRef()).orElseThrow();

            ProductVulnerablePackages vulnerableProduct = new ProductVulnerablePackages();
            vulnerableProduct.setSingleCve(definition.getSingleCve().orElseThrow());
            vulnerableProduct.setProductCpe(deriveCpe(productTest).asString());
            vulnerableProduct.setProductUserFriendlyName(productUserFriendlyName);
            vulnerableProduct.setVulnerablePackages(vulnerablePackages);

            result.add(vulnerableProduct);
        }

        return result;
    }

    @Override
    protected boolean test(BaseCriteria criteria) {
        if (!(criteria instanceof CriteriaType)) {
            return false;
        }
        CriteriaType criteriaType = (CriteriaType) criteria;

        boolean hasTwoChildren = criteriaType.getChildren().size() == 2;
        boolean hasOperatorAND = criteriaType.getOperator() == LogicOperatorType.AND;

        if (!(hasOperatorAND && hasTwoChildren)) {
            return false;
        }

        BaseCriteria productCriteriaRootNode = criteriaType.getChildren().get(0);
        List<CriterionType> productCriterions = collectCriterions(productCriteriaRootNode);

        if (productCriterions.isEmpty()) {
            return false;
        }

        String osProduct = definition.getOsFamily().fullname();

        // Making sure that the product criterions contain indeed product names
        return productCriterions.stream()
                .map(CriterionType::getComment)
                .anyMatch(comment -> comment.startsWith(osProduct));
    }

    public Cpe deriveCpe(TestType productTest) {
        OsFamily osProduct = definition.getOsFamily();
        if (osProduct == OsFamily.openSUSE_LEAP) {
            return deriveOpenSUSELeapCpe();
        } else {
            return deriveSUSEProductCpe(productTest);
        }
    }

    public Cpe deriveOpenSUSELeapCpe() {
        return new CpeBuilder()
                .withVendor("opensuse")
                .withProduct("leap")
                .withVersion(definition.getOsVersion())
                .build();
    }

    public Cpe deriveSUSEProductCpe(TestType productTest) {
        String testComment = productTest.getComment();
        String productPart = null;
        String versionPart = null;
        String updatePart = null;

        Matcher matcher = RELEASE_PACKAGE_REGEX.matcher(testComment);
        if (!matcher.matches()) {
            throw new IllegalStateException("Failed to derive CPE from OVAL test");
        }

        String releasePackage = matcher.group("releasePackage");
        String releasePackageVersion = matcher.group("releasePackageVersion");
        productPart = releasePackage.replace("-release", "").toLowerCase();

        if (releasePackageVersion.contains(".")) {
            if ("ses".equals(productPart) || productPart.contains("suse-manager")) {
                versionPart = releasePackageVersion;
            } else {
                int periodIndex = releasePackageVersion.indexOf('.');
                versionPart = releasePackageVersion.substring(0, periodIndex);
                String update = releasePackageVersion.substring(periodIndex + 1);
                updatePart = "sp" + update;
            }
        } else {
            versionPart = releasePackageVersion;
        }

        return new CpeBuilder()
                .withVendor("suse")
                .withProduct(productPart)
                .withVersion(versionPart)
                .withUpdate(updatePart)
                .build();
    }

    @Override
    public void assertDefinitionIsValid(DefinitionType definition) {
        super.assertDefinitionIsValid(definition);

        OsFamily osFamily = definition.getOsFamily();
        assert osFamily == OsFamily.openSUSE_LEAP ||
                osFamily == OsFamily.SUSE_LINUX_ENTERPRISE_SERVER ||
                osFamily == OsFamily.SUSE_LINUX_ENTERPRISE_DESKTOP;

        assert definition.getDefinitionClass() == DefinitionClassEnum.VULNERABILITY;
    }
}
