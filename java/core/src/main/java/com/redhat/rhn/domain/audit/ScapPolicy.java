package com.redhat.rhn.domain.audit;


import com.redhat.rhn.domain.BaseDomainHelper;
import com.redhat.rhn.domain.org.Org;

import javax.persistence.*;
import java.util.Objects;

@Entity
@Table(name = "suseScapPolicy")
public class ScapPolicy extends BaseDomainHelper {

    private Integer id;

    
    private String policyName;

    private String description;

    private ScapContent scapContent;

    private String xccdfProfileId;

    private TailoringFile tailoringFile;

    private String tailoringProfileId;

    private String ovalFiles;

    private String advancedArgs;

    private Boolean fetchRemoteResources = false;

    private Org org;

    public ScapPolicy() {
    }

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    public Integer getId() {
        return id;
    }

    public void setId(Integer idIn) {
        id = idIn;
    }

    @Column(name = "policy_name")
    public String getPolicyName() {
        return policyName;
    }

    public void setPolicyName(String policyName) {
        this.policyName = policyName;
    }

    @Column(name = "description")
    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    /**
     * Get the SCAP content file associated with this policy.
     * @return the SCAP content
     */
    @ManyToOne
    @JoinColumn(name = "scap_content_id", nullable = false)
    public ScapContent getScapContent() {
        return scapContent;
    }

    public void setScapContent(ScapContent scapContent) {
        this.scapContent = scapContent;
    }

    @Transient
    public String getDataStreamName() {
        return scapContent != null ? scapContent.getDataStreamFileName() : null;
    }

    @Column(name = "xccdf_profile_id")
    public String getXccdfProfileId() {
        return xccdfProfileId;
    }

    public void setXccdfProfileId(String xccdfProfileId) {
        this.xccdfProfileId = xccdfProfileId;
    }

    /**
     * Get the TailoringFile associated with this SCAP policy.
     * TailoringFile is optional, so it can be null.
     * @return the TailoringFile, or null if not set
     */
    @ManyToOne
    @JoinColumn(name = "tailoring_file") // Ensuring the TailoringFile relationship is properly joined
    public TailoringFile getTailoringFile() {
        return tailoringFile;
    }

    public void setTailoringFile(TailoringFile tailoringFile) {
        this.tailoringFile = tailoringFile;
    }

    @Column(name = "tailoring_profile_id")
    public String getTailoringProfileId() {
        return tailoringProfileId;
    }

    public void setTailoringProfileId(String tailoringProfileId) {
        this.tailoringProfileId = tailoringProfileId;
    }

    @Column(name = "oval_files")
    public String getOvalFiles() {
        return ovalFiles;
    }

    public void setOvalFiles(String ovalFiles) {
        this.ovalFiles = ovalFiles;
    }

    @Column(name = "advanced_args")
    public String getAdvancedArgs() {
        return advancedArgs;
    }

    public void setAdvancedArgs(String advancedArgsIn) {
        this.advancedArgs = advancedArgsIn;
    }
    @Column(name = "fetch_remote_resources")
    public Boolean getFetchRemoteResources() {
        return fetchRemoteResources;
    }

    public void setFetchRemoteResources(Boolean fetchRemoteResourcesIn) {
        this.fetchRemoteResources = fetchRemoteResourcesIn;
    }

    /**
     * Get the organization (Org) associated with this SCAP policy.
     * @return the organization
     */
    @ManyToOne
    @JoinColumn(name = "org_id") // Ensuring the Org relationship is properly joined
    public Org getOrg() {
        return org;
    }

    public void setOrg(Org org) {
        this.org = org;
    }

    /**
     * Compares this SCAP policy with another object for equality.
     * Two SCAP policies are considered equal if their policy name, data stream name,
     * and XCCDF profile ID are the same.
     * @param o the object to compare with
     * @return true if this SCAP policy is equal to the provided object
     */
    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        ScapPolicy that = (ScapPolicy) o;
        return policyName.equals(that.policyName) && scapContent.equals(that.scapContent) && xccdfProfileId.equals(that.xccdfProfileId);
    }

    @Override
    public int hashCode() {
        return Objects.hash(policyName, scapContent, xccdfProfileId);
    }

    @Override
    public String toString() {
        return super.toString();
    }
}