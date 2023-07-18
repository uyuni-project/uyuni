package com.suse.oval.vulnerablepkgextractor;

import com.suse.oval.OVALCachingFactory;
import com.suse.oval.db.*;
import com.suse.oval.ovaltypes.*;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

public class SUSEVulnerablePackageExtractor extends AbstractVulnerablePackagesExtractor {

    public SUSEVulnerablePackageExtractor(OVALDefinition vulnerabilityDefinition) {
        super(vulnerabilityDefinition);
    }

    @Override
    protected List<ProductVulnerablePackages> extractItem(BaseCriteria criteria) {
        CriteriaType criteriaType = (CriteriaType) criteria;

        BaseCriteria productCriteriaRootNode = criteriaType.getChildren().get(0);
        BaseCriteria packageCriteriaRootNode = criteriaType.getChildren().get(1);

        List<CriterionType> productCriterions = collectCriterions(productCriteriaRootNode);

        List<String> products = new ArrayList<>();
        for (CriterionType productCriterion : productCriterions) {
            String comment = productCriterion.getComment();
            String product = comment.replace(" is installed", "");
            products.add(product);
        }

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

            OVALPackageTest ovalPackageTest = OVALCachingFactory.lookupPackageTestById(testId);

            Optional<OVALPackageState> ovalPackageStateOpt = ovalPackageTest.getPackageState();
            OVALPackageObject ovalPackageObject = ovalPackageTest.getPackageObject();

            String packageName = ovalPackageObject.getPackageName();

            VulnerablePackage vulnerablePackage = new VulnerablePackage();
            vulnerablePackage.setName(packageName);

            if (ovalPackageStateOpt.isEmpty()) {
                throw new IllegalStateException("Found an empty state");
            }

            if (comment.endsWith("is installed")) {
                String evr = ovalPackageStateOpt
                        .flatMap(OVALPackageState::getPackageEvrState)
                        .map(OVALPackageEvrStateEntity::getEvr).orElse("");
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
        for (String product : products) {
            ProductVulnerablePackages vulnerableProduct = new ProductVulnerablePackages();
            // TODO: needs to be refactored to better imply that the title of SUSE definitions is the CVE
            vulnerableProduct.setCve(vulnerabilityDefinition.getTitle());
            // TODO: Cpe should be different between products
            vulnerableProduct.setProductCpe("cpe:/o:opensuse:leap:15.4");
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

        boolean allProductsAffected = true;
        // TODO: We're now storing cpe instead of the full product name in the databse
/*        for (CriterionType productCriterion : productCriterions) {
            String comment = productCriterion.getComment();
            String product = comment.replace(" is installed", "");

            if (vulnerabilityDefinition.getAffectedPlatforms().stream().map(OVALPlatform::getCpe).noneMatch(product::equals)) {
                allProductsAffected = false;
                break;
            }
        }*/

        return allProductsAffected;
    }
}
