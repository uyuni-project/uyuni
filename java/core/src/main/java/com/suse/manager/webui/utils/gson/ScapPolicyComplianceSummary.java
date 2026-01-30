package com.suse.manager.webui.utils.gson;

import java.time.LocalDateTime;

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
     * @param id the policy ID
     * @param policyName the policy name
     * @param scapContentName the SCAP content name
     * @param totalSystems total number of systems scanned
     * @param compliantSystems number of compliant systems
     */
    public ScapPolicyComplianceSummary(Integer id, String policyName,
                                       String scapContentName, Long totalSystems, Long compliantSystems) {
        this.id = id;
        this.policyName = policyName;
        this.scapContentName = scapContentName;
        this.totalSystems = totalSystems != null ? totalSystems : 0L;
        this.compliantSystems = compliantSystems != null ? compliantSystems : 0L;
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
     * @param id the policy ID
     */
    public void setId(Integer id) {
        this.id = id;
    }
    
    /**
     * Set the policy name
     * @param policyName the policy name
     */
    public void setPolicyName(String policyName) {
        this.policyName = policyName;
    }
    
    /**
     * Set the SCAP content name
     * @param scapContentName the SCAP content name
     */
    public void setScapContentName(String scapContentName) {
        this.scapContentName = scapContentName;
    }
    
    /**
     * Set total systems
     * @param totalSystems total systems count
     */
    public void setTotalSystems(Long totalSystems) {
        this.totalSystems = totalSystems != null ? totalSystems : 0L;
        this.compliancePercentage = calculatePercentage();
    }
    
    /**
     * Set compliant systems
     * @param compliantSystems compliant systems count
     */
    public void setCompliantSystems(Long compliantSystems) {
        this.compliantSystems = compliantSystems != null ? compliantSystems : 0L;
        this.compliancePercentage = calculatePercentage();
    }
}