/*
 * Copyright (c) 2013--2015 Red Hat, Inc.
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

package com.redhat.rhn.domain.org;

import com.redhat.rhn.domain.BaseDomainHelper;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.hibernate.annotations.Type;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.MapsId;
import jakarta.persistence.OneToOne;
import jakarta.persistence.Table;

/**
 * Class OrgConfig that reflects the DB representation of rhnOrgConfiguration DB table:
 * rhnOrgConfiguration
 */
@Entity
@Table(name = "rhnOrgConfiguration")
public class OrgConfig extends BaseDomainHelper {

    protected static Logger log = LogManager.getLogger(OrgConfig.class);

    @Id
    @Column(name = "org_id", nullable = false)
    private Long orgId;

    @OneToOne
    @MapsId // This maps the primary key of OrgConfig to the primary key of Org
    @JoinColumn(name = "org_id") // References the primary key column in Org
    private Org org;

    @Column(name = "staging_content_enabled", nullable = false)
    @Type(type = "yes_no")
    private boolean stagingContentEnabled;

    @Column(name = "errata_emails_enabled", nullable = false)
    @Type(type = "yes_no")
    private boolean errataEmailsEnabled;

    @Column(name = "scapfile_upload_enabled", nullable = false)
    @Type(type = "yes_no")
    private boolean scapfileUploadEnabled;

    @Column(name = "scap_retention_period_days")
    private Long scapRetentionPeriodDays;

    @Column(name = "create_default_sg", nullable = false)
    @Type(type = "yes_no")
    private boolean createDefaultSg;

    @Column(name = "clm_sync_patches", nullable = false)
    @Type(type = "yes_no")
    private boolean clmSyncPatches;

    /**
     * Gets the current value of org
     * @return Returns the value of org
     */
    public Org getOrg() {
        return org;
    }

    /**
     * Sets the value of org to new value
     * @param orgIn New value for org
     */
    public void setOrg(Org orgIn) {
        org = orgIn;
    }

    /**
     * Gets the current value of org_id
     * @return Returns the value of org_id
     */
    public Long getOrgId() {
        return orgId;
    }

    /**
     * Sets the value of org_id to new value
     * @param orgIdIn New value for orgId
     */
    protected void setOrgId(Long orgIdIn) {
        orgId = orgIdIn;
    }

    /**
     * @return Returns the stageContentEnabled.
     */
    public boolean isStagingContentEnabled() {
        return stagingContentEnabled;
    }

    /**
     * @param stageContentEnabledIn The stageContentEnabled to set.
     */
    public void setStagingContentEnabled(boolean stageContentEnabledIn) {
        stagingContentEnabled = stageContentEnabledIn;
    }

    /**
     * @return Returns the errataEmailsEnabled.
     */
    public boolean isErrataEmailsEnabled() {
        return errataEmailsEnabled;
    }

    /**
     * @param errataEmailsEnabledIn The errataEmailsEnabled to set.
     */
    public void setErrataEmailsEnabled(boolean errataEmailsEnabledIn) {
        this.errataEmailsEnabled = errataEmailsEnabledIn;
    }

    /**
     * @return Returns the scapfileUploadEnabled flag.
     */
    public boolean isScapfileUploadEnabled() {
        return scapfileUploadEnabled;
    }

    /**
     * @param scapfileUploadEnabledIn The scapfileUploadEnabled to set.
     */
    public void setScapfileUploadEnabled(boolean scapfileUploadEnabledIn) {
        scapfileUploadEnabled = scapfileUploadEnabledIn;
    }

    /**
     * Get the org-wide period (in days) after which it is possible to remove SCAP scan.
     * @return Returns the org-wide SCAP retention period.
     */
    public Long getScapRetentionPeriodDays() {
        return scapRetentionPeriodDays;
    }

    /**
     * Set the org-wide SCAP period (in days) after which it is possible to remove SCAP
     * scan.
     * @param scapRetentionPeriodDaysIn The org-wide SCAP retention period.
     */
    public void setScapRetentionPeriodDays(Long scapRetentionPeriodDaysIn) {
        scapRetentionPeriodDays = scapRetentionPeriodDaysIn;
    }


    /**
     * @return Returns the createDefaultSg.
     */
    public boolean isCreateDefaultSg() {
        return createDefaultSg;
    }


    /**
     * @param createDefaultSgIn The createDefaultSg to set.
     */
    public void setCreateDefaultSg(boolean createDefaultSgIn) {
        createDefaultSg = createDefaultSgIn;
    }

    /**
     * Gets the clmSyncPatches.
     *
     * @return clmSyncPatches
     */
    public boolean isClmSyncPatches() {
        return clmSyncPatches;
    }

    /**
     * Sets the clmSyncPatches.
     *
     * @param clmSyncPatchesIn the clmSyncPatches
     */
    public void setClmSyncPatches(boolean clmSyncPatchesIn) {
        clmSyncPatches = clmSyncPatchesIn;
    }
}
