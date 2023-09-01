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

    public void setProductCpe(String productCpeIn) {
        this.productCpe = productCpeIn;
    }

    public List<VulnerablePackage> getVulnerablePackages() {
        return vulnerablePackages;
    }

    public void setVulnerablePackages(List<VulnerablePackage> vulnerablePackagesIn) {
        this.vulnerablePackages.clear();
        this.vulnerablePackages.addAll(vulnerablePackagesIn);
    }

    public List<String> getCves() {
        return cves;
    }

    public void setCves(List<String> cvesIn) {
        this.cves = cvesIn;
    }

    /**
     * Clears the list of relevant CVEs and adds a single CVE
     *
     * @param cve the cve to add
     */
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

    @Override
    public String toString() {
        StringBuilder stringBuilder = new StringBuilder();
        for (String cve : cves) {
            stringBuilder.append(cve).append("\n");
            stringBuilder.append(productCpe).append("   +   ").append(productUserFriendlyName).append("\n");
            stringBuilder.append("****************************************").append("\n");
            for (VulnerablePackage vulnerablePackage : vulnerablePackages) {
                stringBuilder.append(vulnerablePackage.getName())
                        .append(" ")
                        .append(vulnerablePackage.getFixVersion().orElse("none"))
                        .append("\n");
            }
            stringBuilder.append("////////////////////////////////////////").append("\n");
        }

        return stringBuilder.toString();
    }
}
