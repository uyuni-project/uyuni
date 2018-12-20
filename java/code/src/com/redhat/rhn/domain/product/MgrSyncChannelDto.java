/**
 * Copyright (c) 2018 SUSE LLC
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
package com.redhat.rhn.domain.product;

import com.redhat.rhn.domain.rhnpackage.PackageArch;

import com.suse.mgrsync.MgrSyncStatus;

import java.util.Optional;

/**
 *  MgrSyncChannelDto - representation of a channel
 */
public class MgrSyncChannelDto {

    private final String name;
    private final String label;
    private final String summary;
    private final String description;
    private final boolean mandatory;
    private final Optional<PackageArch> arch;
    private final String parentLabel;
    private final String family;
    private final String productName;
    private final String productVersion;
    private final MgrSyncStatus status;
    private final boolean isSigned;
    private final String sourceUrl;
    private final String updateTag;

    /**
     * @return the name
     */
    public String getName() {
        return name;
    }

    /**
     * @return the label
     */
    public String getLabel() {
        return label;
    }

    /**
     * @return the summary
     */
    public String getSummary() {
        return summary;
    }

    /**
     * @return true if it is mandatory
     */
    public boolean isMandatory() {
        return mandatory;
    }

    /**
     * @return the arch
     */
    public Optional<PackageArch> getArch() {
        return arch;
    }

    /**
     * @return the sync status
     */
    public MgrSyncStatus getStatus() {
        return status;
    }

    /**
     * @return the description
     */
    public String getDescription() {
        return description;
    }

    /**
     * @return the family
     */
    public String getFamily() {
        return family;
    }

    /**
     * @return the isSigned
     */
    public boolean isSigned() {
        return isSigned;
    }

    /**
     * @return the parentLabel
     */
    public String getParentLabel() {
        return parentLabel;
    }

    /**
     * @return the productName
     */
    public String getProductName() {
        return productName;
    }

    /**
     * @return the productVersion
     */
    public String getProductVersion() {
        return productVersion;
    }

    /**
     * @return the sourceUrl
     */
    public String getSourceUrl() {
        return sourceUrl;
    }

    /**
     * @return the updateTag
     */
    public String getUpdateTag() {
        return updateTag;
    }

    /**
     * Constructor
     * @param nameIn the name
     * @param labelIn the label
     * @param summaryIn the summary
     * @param descriptionIn the description
     * @param mandatoryIn mandatory
     * @param archIn the arch
     * @param parentLabelIn the parent label
     * @param familyIn the channel family
     * @param productNameIn the product name
     * @param productVersionIn the product version
     * @param statusIn the sync status
     * @param isSignedIn is the repo signed?
     * @param sourceUrlIn the url
     * @param updateTagIn the update tag
     */
    public MgrSyncChannelDto(String nameIn, String labelIn, String summaryIn, String descriptionIn,
            boolean mandatoryIn, Optional<PackageArch> archIn, String parentLabelIn, String familyIn,
            String productNameIn, String productVersionIn, MgrSyncStatus statusIn, boolean isSignedIn,
            String sourceUrlIn, String updateTagIn) {
        this.name = nameIn;
        this.label = labelIn;
        this.summary = summaryIn;
        this.description = descriptionIn;
        this.mandatory = mandatoryIn;
        this.arch = archIn;
        this.parentLabel = parentLabelIn;
        this.family = familyIn;
        this.productName = productNameIn;
        this.productVersion = productVersionIn;
        this.status = statusIn;
        this.isSigned = isSignedIn;
        this.sourceUrl = sourceUrlIn;
        this.updateTag = updateTagIn;
    }
}
