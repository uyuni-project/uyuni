/*
 * Copyright (c) 2018--2025 SUSE LLC
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

package com.redhat.rhn.domain.audit;

import com.redhat.rhn.domain.BaseDomainHelper;
import com.redhat.rhn.domain.org.Org;

import java.util.Objects;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;

/**
 * ScapPolicy entity representing a SCAP audit policy.
 */
@Entity
@Table(name = "suseScapPolicy")
public class ScapPolicy extends BaseDomainHelper {


    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    @Column(name = "policy_name")
    private String policyName;

    @Column(name = "description")
    private String description;

    @ManyToOne
    @JoinColumn(name = "scap_content_id", nullable = false)
    private ScapContent scapContent;

    @Column(name = "xccdf_profile_id")
    private String xccdfProfileId;

    @ManyToOne
    @JoinColumn(name = "tailoring_file")
    private TailoringFile tailoringFile;

    @Column(name = "tailoring_profile_id")
    private String tailoringProfileId;

    @Column(name = "oval_files")
    private String ovalFiles;

    @Column(name = "advanced_args")
    private String advancedArgs;

    @Column(name = "fetch_remote_resources", nullable = false)
    private boolean fetchRemoteResources = false;

    @ManyToOne
    @JoinColumn(name = "org_id")
    private Org org;

    /**
     * Default constructor for JPA.
     */
    public ScapPolicy() {
        // Default constructor
    }

    /**
     * @return the id
     */
    public Integer getId() {
        return id;
    }

    /**
     * @param idIn the id to set
     */
    public void setId(Integer idIn) {
        id = idIn;
    }

    /**
     * @return the policyName
     */
    public String getPolicyName() {
        return policyName;
    }

    /**
     * @param policyNameIn the policyName to set
     */
    public void setPolicyName(String policyNameIn) {
        this.policyName = policyNameIn;
    }

    /**
     * @return the description
     */
    public String getDescription() {
        return description;
    }

    /**
     * @param descriptionIn the description to set
     */
    public void setDescription(String descriptionIn) {
        this.description = descriptionIn;
    }

    /**
     * Get the SCAP content file associated with this policy.
     * @return the SCAP content
     */
    public ScapContent getScapContent() {
        return scapContent;
    }

    /**
     * @param scapContentIn the scapContent to set
     */
    public void setScapContent(ScapContent scapContentIn) {
        this.scapContent = scapContentIn;
    }

    /**
     * @return the dataStreamName
     */
    public String getDataStreamName() {
        return scapContent != null ? scapContent.getDataStreamFileName() : null;
    }

    /**
     * @return the xccdfProfileId
     */
    public String getXccdfProfileId() {
        return xccdfProfileId;
    }

    /**
     * @param xccdfProfileIdIn the xccdfProfileId to set
     */
    public void setXccdfProfileId(String xccdfProfileIdIn) {
        this.xccdfProfileId = xccdfProfileIdIn;
    }

    /**
     * Get the TailoringFile associated with this SCAP policy.
     * TailoringFile is optional, so it can be null.
     * @return the TailoringFile, or null if not set
     */
    public TailoringFile getTailoringFile() {
        return tailoringFile;
    }

    /**
     * @param tailoringFileIn the tailoringFile to set
     */
    public void setTailoringFile(TailoringFile tailoringFileIn) {
        this.tailoringFile = tailoringFileIn;
    }

    /**
     * @return the tailoringProfileId
     */
    public String getTailoringProfileId() {
        return tailoringProfileId;
    }

    /**
     * @param tailoringProfileIdIn the tailoringProfileId to set
     */
    public void setTailoringProfileId(String tailoringProfileIdIn) {
        this.tailoringProfileId = tailoringProfileIdIn;
    }

    /**
     * @return the ovalFiles
     */
    public String getOvalFiles() {
        return ovalFiles;
    }

    /**
     * @param ovalFilesIn the ovalFiles to set
     */
    public void setOvalFiles(String ovalFilesIn) {
        this.ovalFiles = ovalFilesIn;
    }

    /**
     * @return the advancedArgs
     */
    public String getAdvancedArgs() {
        return advancedArgs;
    }

    /**
     * @param advancedArgsIn the advancedArgsIn to set
     */
    public void setAdvancedArgs(String advancedArgsIn) {
        this.advancedArgs = advancedArgsIn;
    }

    /**
     * @return the fetchRemoteResources
     */
    public boolean isFetchRemoteResources() {
        return fetchRemoteResources;
    }

    /**
     * @param fetchRemoteResourcesIn the fetchRemoteResourcesIn to set
     */
    public void setFetchRemoteResources(boolean fetchRemoteResourcesIn) {
        this.fetchRemoteResources = fetchRemoteResourcesIn;
    }

    /**
     * Get the organization (Org) associated with this SCAP policy.
     * @return the organization
     */
    public Org getOrg() {
        return org;
    }

    /**
     * @param orgIn the org to set
     */
    public void setOrg(Org orgIn) {
        this.org = orgIn;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }
        ScapPolicy that = (ScapPolicy) o;
        return Objects.equals(policyName, that.policyName) &&
                Objects.equals(scapContent, that.scapContent) &&
                Objects.equals(xccdfProfileId, that.xccdfProfileId);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public int hashCode() {
        return Objects.hash(policyName, scapContent, xccdfProfileId);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public String toString() {
        return super.toString();
    }
}
