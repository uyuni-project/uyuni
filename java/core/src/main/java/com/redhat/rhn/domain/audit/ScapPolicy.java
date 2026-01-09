package com.redhat.rhn.domain.audit;


import com.google.gson.annotations.Expose;
import com.redhat.rhn.domain.BaseDomainHelper;
import com.redhat.rhn.domain.org.Org;
import com.suse.manager.webui.utils.gson.ScapPolicyDetailJson;
import org.apache.commons.lang3.builder.EqualsBuilder;
import org.apache.commons.lang3.builder.HashCodeBuilder;

import javax.persistence.*;
import java.util.Objects;

@Entity
@Table(name = "suseScapPolicy")
@SqlResultSetMapping(
        name = "ScapPolicyDetailMapping",
        classes = @ConstructorResult(
                targetClass = ScapPolicyDetailJson.class,
                columns = {
                        @ColumnResult(name = "scapActionId", type = Long.class),
                        @ColumnResult(name = "scapPolicyId", type = Long.class),
                        @ColumnResult(name = "serverId", type = Long.class),
                        @ColumnResult(name = "serverName", type = String.class),
                        @ColumnResult(name = "pickupTime", type = java.time.LocalDateTime.class),
                        @ColumnResult(name = "Passed", type = Long.class),
                        @ColumnResult(name = "Failed", type = Long.class),
                        @ColumnResult(name = "Other", type = Long.class)
                }
        )
)
public class ScapPolicy extends BaseDomainHelper {

    // Unique identifier for the SCAP policy
    private Integer id;

    // The name of the SCAP policy
    @Expose
    private String policyName;

    // The description of the SCAP policy
    private String description;

    // The name of the data stream associated with the SCAP policy
    @Expose
    private String dataStreamName;

    // The XCCDF profile ID used by this SCAP policy
    private String xccdfProfileId;

    // Reference to the TailoringFile associated with the SCAP policy (optional)
    private TailoringFile tailoringFile;

    // The profile ID used for tailoring the SCAP policy
    private String tailoringProfileId;

    // Comma-separated list of OVAL files
    private String ovalFiles;

    // Advanced arguments for oscap command (e.g., --remediate, --skip-valid, --thin-results)
    private String advancedArgs;

    // Flag to indicate whether to fetch remote resources during SCAP scan
    private Boolean fetchRemoteResources = false;

    // The organization (Org) to which this policy belongs
    private Org org;

    /**
     * Default constructor for the ScapPolicy entity.
     * This is required by JPA for entity instantiation.
     */
    public ScapPolicy() {
        // No-op constructor
    }

    /**
     * Constructor for creating a ScapPolicy with the essential details.
     * @param policyName the name of the policy
     * @param dataStreamName the name of the data stream
     * @param xccdfProfileId the XCCDF profile ID
     */
    public ScapPolicy(String policyName, String dataStreamName, String xccdfProfileId) {
        this.policyName = policyName;
        this.dataStreamName = dataStreamName;
        this.xccdfProfileId = xccdfProfileId;
    }

    /**
     * Get the unique identifier for this SCAP policy.
     * @return the unique identifier (ID)
     */
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    public Integer getId() {
        return id;
    }

    public void setId(Integer idIn) {
        id = idIn;
    }

    /**
     * Get the name of this SCAP policy.
     * @return the name of the policy
     */
    @Column(name = "policy_name")
    public String getPolicyName() {
        return policyName;
    }

    /**
     * Sets the name of the SCAP policy.
     * @param policyName the policy name to set
     */
    public void setPolicyName(String policyName) {
        this.policyName = policyName;
    }

    /**
     * Get the description of this SCAP policy.
     * @return the description of the policy
     */
    @Column(name = "description")
    public String getDescription() {
        return description;
    }

    /**
     * Sets the description of the SCAP policy.
     * @param description the description to set
     */
    public void setDescription(String description) {
        this.description = description;
    }

    /**
     * Get the data stream name for this SCAP policy.
     * @return the data stream name
     */
    @Column(name = "data_stream_name")
    public String getDataStreamName() {
        return dataStreamName;
    }

    /**
     * Set the data stream name for this SCAP policy.
     * @param dataStreamName the data stream name to set
     */
    public void setDataStreamName(String dataStreamName) {
        this.dataStreamName = dataStreamName;
    }

    /**
     * Get the XCCDF profile ID for this SCAP policy.
     * @return the XCCDF profile ID
     */
    @Column(name = "xccdf_profile_id")
    public String getXccdfProfileId() {
        return xccdfProfileId;
    }

    /**
     * Set the XCCDF profile ID for this SCAP policy.
     * @param xccdfProfileId the XCCDF profile ID to set
     */
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

    /**
     * Set the TailoringFile for this SCAP policy.
     * @param tailoringFile the TailoringFile to associate with this policy
     */
    public void setTailoringFile(TailoringFile tailoringFile) {
        this.tailoringFile = tailoringFile;
    }

    /**
     * Get the tailoring profile ID used for customizing this SCAP policy.
     * @return the tailoring profile ID
     */
    @Column(name = "tailoring_profile_id")
    public String getTailoringProfileId() {
        return tailoringProfileId;
    }

    /**
     * Set the tailoring profile ID for this SCAP policy.
     * @param tailoringProfileId the tailoring profile ID to set
     */
    public void setTailoringProfileId(String tailoringProfileId) {
        this.tailoringProfileId = tailoringProfileId;
    }

    /**
     * Get the OVAL files (comma-separated).
     * @return the OVAL files
     */
    @Column(name = "oval_files")
    public String getOvalFiles() {
        return ovalFiles;
    }

    /**
     * Set the OVAL files.
     * @param ovalFiles the OVAL files to set (comma-separated)
     */
    public void setOvalFiles(String ovalFiles) {
        this.ovalFiles = ovalFiles;
    }

    /**
     * Get the advanced arguments for oscap command.
     * @return the advanced arguments
     */
    @Column(name = "advanced_args")
    public String getAdvancedArgs() {
        return advancedArgs;
    }

    /**
     * Set the advanced arguments for oscap command.
     * @param advancedArgsIn the advanced arguments to set
     */
    public void setAdvancedArgs(String advancedArgsIn) {
        this.advancedArgs = advancedArgsIn;
    }

    /**
     * Get whether to fetch remote resources during SCAP scan.
     * @return true if remote resources should be fetched, false otherwise
     */
    @Column(name = "fetch_remote_resources")
    public Boolean getFetchRemoteResources() {
        return fetchRemoteResources;
    }

    /**
     * Set whether to fetch remote resources during SCAP scan.
     * @param fetchRemoteResourcesIn true to fetch remote resources, false otherwise
     */
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

    /**
     * Set the organization (Org) for this SCAP policy.
     * @param org the Org to set
     */
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
        return policyName.equals(that.policyName) && dataStreamName.equals(that.dataStreamName) && xccdfProfileId.equals(that.xccdfProfileId);
    }

    /**
     * Generates a hash code for this SCAP policy based on policy name, data stream name,
     * and XCCDF profile ID. This is important for using this object in hash-based collections.
     * @return the hash code of this SCAP policy
     */
    @Override
    public int hashCode() {
        return Objects.hash(policyName, dataStreamName, xccdfProfileId);
    }

    /**
     * Returns a string representation of this SCAP policy.
     * @return the string representation
     */
    @Override
    public String toString() {
        return super.toString();
    }
}