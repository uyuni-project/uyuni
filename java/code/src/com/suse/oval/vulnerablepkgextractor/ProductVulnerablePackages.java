package com.suse.oval.vulnerablepkgextractor;

import java.util.ArrayList;
import java.util.List;

/**
 * This class encapsulates information about a product, a cve and the packages that made the product
 * vulnerable to the cve
 */
public class ProductVulnerablePackages {
    private List<String> cves = new ArrayList<>();
    private String productCpe;
    private final List<VulnerablePackage> vulnerablePackages = new ArrayList<>();
    private String productUserFriendlyName;

    /**
     *
     * */
    public ProductVulnerablePackages() {

    }

    public String getProductCpe() {
        return productCpe;
    }

    public void setProductCpe(String productCpe) {
        this.productCpe = productCpe;
    }

    public List<VulnerablePackage> getVulnerablePackages() {
        return vulnerablePackages;
    }

    public void setVulnerablePackages(List<VulnerablePackage> vulnerablePackages) {
        this.vulnerablePackages.clear();
        this.vulnerablePackages.addAll(vulnerablePackages);
    }

    public List<String> getCves() {
        return cves;
    }

    public void setCves(List<String> cves) {
        this.cves = cves;
    }

    public void setSingleCve(String cve) {
        this.cves.clear();
        cves.add(cve);
    }

    public void setProductUserFriendlyName(String product) {
        this.productUserFriendlyName = product;
    }

    public String getProductUserFriendlyName() {
        return productUserFriendlyName;
    }
}
