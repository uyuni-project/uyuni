package com.suse.oval.vulnerablepkgextractor;

import java.util.ArrayList;
import java.util.List;

/**
 * This class encapsulates information about a product, a cve and the packages that made the product vulnerable to the cve
 */
public class ProductVulnerablePackages {
    private String cve;
    private String product;
    private final List<VulnerablePackage> vulnerablePackages = new ArrayList<>();

    public ProductVulnerablePackages() {

    }

    public String getProduct() {
        return product;
    }

    public void setProduct(String product) {
        this.product = product;
    }

    public List<VulnerablePackage> getVulnerablePackages() {
        return vulnerablePackages;
    }

    public void setVulnerablePackages(List<VulnerablePackage> vulnerablePackages) {
        this.vulnerablePackages.clear();
        this.vulnerablePackages.addAll(vulnerablePackages);
    }

    public String getCve() {
        return cve;
    }

    public void setCve(String cve) {
        this.cve = cve;
    }

    @Override
    public String toString() {
        StringBuilder stringBuilder = new StringBuilder();
        stringBuilder.append(cve).append("\n");
        stringBuilder.append(product).append("\n");
        stringBuilder.append("****************************************").append("\n");
        for (VulnerablePackage vulnerablePackage : vulnerablePackages) {
            stringBuilder.append(vulnerablePackage.getName())
                    .append(" ")
                    .append(vulnerablePackage.getFixVersion().orElse("none"))
                    .append("\n");
        }
        stringBuilder.append("////////////////////////////////////////").append("\n");

        return stringBuilder.toString();
    }
}
