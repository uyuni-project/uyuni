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
 * Red Hat trademarks are not licensed under GPLv2. No permission is
 * granted to use or replicate Red Hat trademarks that are incorporated
 * in this software or its documentation.
 */
package com.suse.manager.webui.utils.gson;

/**
 * DTO for SCAP policy compliance summary data
 */
public class ScapPolicyComplianceSummary {
    private Integer id;
    private String policyName;
    private String scapContentName;
    private Long totalSystems = 0L;
    private Long compliantSystems = 0L;
    private Double compliancePercentage;

    /**
     * No-argument constructor required by database query framework
     */
    public ScapPolicyComplianceSummary() {
        // No-op constructor for framework
    }

    /**
     * Constructor for SQL result mapping
     * @param idIn the policy ID
     * @param policyNameIn the policy name
     * @param scapContentNameIn the SCAP content name
     * @param totalSystemsIn total number of systems scanned
     * @param compliantSystemsIn number of compliant systems
     */
    public ScapPolicyComplianceSummary(Integer idIn, String policyNameIn,
                                       String scapContentNameIn, Long totalSystemsIn, Long compliantSystemsIn) {
        this.id = idIn;
        this.policyName = policyNameIn;
        this.scapContentName = scapContentNameIn;
        this.totalSystems = totalSystemsIn != null ? totalSystemsIn : 0L;
        this.compliantSystems = compliantSystemsIn != null ? compliantSystemsIn : 0L;
        this.compliancePercentage = calculatePercentage();
    }

    private Double calculatePercentage() {
        if (totalSystems == 0) {
            return 0.0;
        }
        return Math.round((compliantSystems * 100.0 / totalSystems) * 100.0) / 100.0;
    }

    /**
     * @return the policy ID
     */
    public Integer getId() {
        return id;
    }

    /**
     * @return the policy name
     */
    public String getPolicyName() {
        return policyName;
    }

    /**
     * @return the SCAP content name
     */
    public String getScapContentName() {
        return scapContentName;
    }

    /**
     * @return total number of systems scanned with this policy
     */
    public Long getTotalSystems() {
        return totalSystems;
    }

    /**
     * @return number of compliant systems
     */
    public Long getCompliantSystems() {
        return compliantSystems;
    }

    /**
     * @return number of non-compliant systems
     */
    public Long getNonCompliantSystems() {
        return totalSystems - compliantSystems;
    }

    /**
     * @return compliance percentage (0-100)
     */
    public Double getCompliancePercentage() {
        return compliancePercentage;
    }

    /**
     * Set the policy ID
     * @param idIn the policy ID
     */
    public void setId(Integer idIn) {
        this.id = idIn;
    }

    /**
     * Set the policy name
     * @param policyNameIn the policy name
     */
    public void setPolicyName(String policyNameIn) {
        this.policyName = policyNameIn;
    }

    /**
     * Set the SCAP content name
     * @param scapContentNameIn the SCAP content name
     */
    public void setScapContentName(String scapContentNameIn) {
        this.scapContentName = scapContentNameIn;
    }

    /**
     * Set total systems
     * @param totalSystemsIn total systems count
     */
    public void setTotalSystems(Long totalSystemsIn) {
        this.totalSystems = totalSystemsIn != null ? totalSystemsIn : 0L;
        this.compliancePercentage = calculatePercentage();
    }

    /**
     * Set compliant systems
     * @param compliantSystemsIn compliant systems count
     */
    public void setCompliantSystems(Long compliantSystemsIn) {
        this.compliantSystems = compliantSystemsIn != null ? compliantSystemsIn : 0L;
        this.compliancePercentage = calculatePercentage();
    }
}
