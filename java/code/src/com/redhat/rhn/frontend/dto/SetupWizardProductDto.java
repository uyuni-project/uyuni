/**
 * Copyright (c) 2014 SUSE LLC
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

package com.redhat.rhn.frontend.dto;

import com.redhat.rhn.frontend.struts.Selectable;

import com.suse.manager.model.products.Channel;
import com.suse.manager.model.products.MandatoryChannels;
import com.suse.manager.model.products.OptionalChannels;

import org.apache.commons.lang3.builder.CompareToBuilder;
import org.apache.commons.lang3.builder.EqualsBuilder;
import org.apache.commons.lang3.builder.HashCodeBuilder;
import org.apache.commons.lang3.builder.ToStringBuilder;

import java.util.Date;
import java.util.LinkedList;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;

/**
 * A SUSE Product as it is shown in the Setup Wizard UI.
 */
public class SetupWizardProductDto implements Selectable,
        Comparable<SetupWizardProductDto> {
    /**
     * Aggregated product sync status.
     */
    public static class SyncStatus {

        /**
         * The stage of the synchronization status
         */
        public enum SyncStage {
            /** Product has never been installed at all. */
            NOT_MIRRORED,
            /** Product has been installed, synchronization is in progress. */
            IN_PROGRESS,
            /** Product has been installed and synchronized completely. */
            FINISHED,
            /** Product installation or sync went bad. */
            FAILED;

            /**
             * Returns a translation key for a stage.
             * @return the key
             */
            public String getTranslationKey() {
                return "setupwizard.syncstatus." +
                        toString().replace("_", ".").toLowerCase();
            }
        }

        // Stage of the sync
        private SyncStage stage;

        // Error message key
        private String messageKey;

        // Store additional debug information here
        private String details;

        // This is for showing synchronization progress
        private int syncProgress;

        // If FINISHED, the last reposync date, else null
        private Date lastSyncDate;

        /**
         * Constructor
         * @param stageIn of the synchronization stage
         */
        public SyncStatus(SyncStage stageIn) {
            this.stage = stageIn;
        }

        /**
         * Default constructor (not mirrored yet)
         */
        public SyncStatus() {
            this(SyncStage.NOT_MIRRORED);
        }

        /**
         * @return stage of the synchronization
         */
        public SyncStage getStage() {
            return stage;
        }

        /**
         * Convenience method to check equality with a given SyncStage.
         * @param stageIn the stage to compare to
         * @return whether the stages are equal
         */
        boolean equals(SyncStage stageIn) {
            return getStage().equals(stageIn);
        }

        /**
         * @param stageIn of the synchronization
         */
        public void setStage(SyncStage stageIn) {
            this.stage = stageIn;
        }

        /**
         * Get the error message key.
         * @return the message key
         */
        public String getMessageKey() {
            return messageKey;
        }

        /**
         * Set the error message key.
         * @param messageKeyIn key of the message to set
         */
        public void setMessageKey(String messageKeyIn) {
            this.messageKey = messageKeyIn;
        }

        /**
         * Get additional debug information (if available).
         * @return the details
         */
        public String getDetails() {
            return details;
        }

        /**
         * Set any additional debug information.
         * @param detailsIn the details to set
         */
        public void setDetails(String detailsIn) {
            this.details = detailsIn;
        }

        /**
         * Get the product synchronization progress.
         * @return the progress
         */
        public int getSyncProgress() {
            return syncProgress;
        }

        /**
         * Set the product synchronization progress.
         * @param syncProgressIn the progress to set
         */
        public void setSyncProgress(int syncProgressIn) {
            this.syncProgress = syncProgressIn;
        }

        /**
         * Gets the last sync date.
         * @return the last sync date
         */
        public Date getLastSyncDate() {
            return lastSyncDate;
        }

        /**
         * Sets the last sync date.
         *  @param lastSyncDateIn the new last sync date
         */
        public void setLastSyncDate(Date lastSyncDateIn) {
            lastSyncDate = lastSyncDateIn;
        }

        /**
         * Convenience method to query the stage
         * @return true if the stage is not mirrored
         */
        public boolean isNotMirrored() {
            return equals(SyncStage.NOT_MIRRORED);
        }

        /**
         * Convenience method to query the stage
         * @return true if the stage is in progress
         */
        public boolean isInProgress() {
            return equals(SyncStage.IN_PROGRESS);
        }

        /**
         * Convenience method to query the stage
         * @return true if the stage is failed
         */
        public boolean isFailed() {
            return equals(SyncStage.FAILED);
        }

        /**
         * Convenience method to query the stage
         * @return true if the stage is finished
         */
        public boolean isFinished() {
            return equals(SyncStatus.SyncStage.FINISHED);
        }
    };

    /** The architecture. */
    private String arch;

    /** The ident ID. */
    private String ident;

    /** The product readable name. */
    private String name;

    /** The ident ID of the base product or an empty string. */
    private String parentProduct;

    /** The mandatory channels. */
    private MandatoryChannels mandatoryChannels;

    /** The optional channels. */
    private OptionalChannels optionalChannels;

    /** True if this product has been selected in the GUI. */
    private boolean selected = false;

    /** Base product or null. */
    private SetupWizardProductDto baseProduct = null;

    /** Addon products. */
    private List<SetupWizardProductDto> addonProducts =
            new LinkedList<SetupWizardProductDto>();

    /** Aggregated product sync status. */
    private SyncStatus syncStatus;

    /**
     * Default constructor.
     */
    public SetupWizardProductDto() {
        // required by Simple XML
    }

    /**
     * Instantiates a new product.
     *
     * @param archIn the architecture
     * @param identIn the ident ID
     * @param nameIn the name
     * @param baseProductIdent the ident ID of the base product or an empty string
     * the base product ident ID
     * @param mandatoryChannelsIn the mandatory channels in
     * @param optionalChannelsIn the optional channels in
     */
    public SetupWizardProductDto(String archIn, String identIn, String nameIn,
            String baseProductIdent, MandatoryChannels mandatoryChannelsIn,
            OptionalChannels optionalChannelsIn) {
        arch = archIn;
        ident = identIn;
        name = nameIn;
        parentProduct = baseProductIdent;
        mandatoryChannels = mandatoryChannelsIn;
        optionalChannels = optionalChannelsIn;
    }

    /**
     * Gets the architecture.
     * @return the architecture
     */
    public String getArch() {
        return arch;
    }

    /**
     * Gets the ident ID.
     * @return the ident ID
     */
    public String getIdent() {
        return ident;
    }

    /**
     * Gets the name.
     * @return the name
     */
    public String getName() {
        return name;
    }

    /**
     * Gets the base product's ident ID, if any.
     * @return the parent product ident ID or an empty string if this is a base
     * product
     */
    public String getBaseProductIdent() {
        return parentProduct;
    }

    /**
     * Gets the mandatory channels.
     * @return the mandatory channels
     */
    public List<Channel> getMandatoryChannels() {
        return mandatoryChannels.getChannels();
    }

    /**
     * Gets the optional channels.
     * @return the optional channels
     */
    public List<Channel> getOptionalChannels() {
        return optionalChannels.getChannels();
    }

    /**
     * Gets all channels which are installer updates channel
     * @return installer updates channel
     */
    public List<Channel> getInstallerUpdateChannels() {
        return Stream.concat(
                getMandatoryChannels().stream(),
                getOptionalChannels().stream()
                )
                .filter(c -> c.isInstallerUpdates())
                .collect(Collectors.toList());
    }

    /**
     * Check if all mandatory channels are provided according to mgr-ncc-sync (P).
     * @return true if this product is provided, otherwise false
     */
    public boolean isProvided() {
        for (Channel c : getMandatoryChannels()) {
            if (!c.isProvided()) {
                return false;
            }
        }
        return true;
    }

    /**
     * Returns true iff this is a base product.
     *
     * @return true for base products
     */
    public boolean isBase() {
        return getBaseProductIdent().isEmpty();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public int compareTo(SetupWizardProductDto other) {
        return new CompareToBuilder()
        // base products first
        .append(!isBase(), !other.isBase())
        .append(this.name, other.getName())
        .append(this.arch, other.getArch())
        .append(this.ident, other.getIdent())
        .toComparison();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public boolean equals(Object other) {
        if (!(other instanceof SetupWizardProductDto)) {
            return false;
        }
        SetupWizardProductDto otherProduct = (SetupWizardProductDto) other;
        return new EqualsBuilder()
            .append(getIdent(), otherProduct.getIdent())
            .isEquals();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public int hashCode() {
        return new HashCodeBuilder()
            .append(getIdent())
            .toHashCode();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public String toString() {
        return new ToStringBuilder(this)
        .append("Name", getName())
        .append("Arch", getArch())
        .append("Ident", getIdent())
        .toString();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public boolean isSelectable() {
        return true;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public boolean isSelected() {
        return this.selected;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void setSelected(boolean selectedIn) {
        this.selected = selectedIn;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public String getSelectionKey() {
        return this.ident;
    }

    /**
     * Gets the base product.
     * @return the base product
     */
    public SetupWizardProductDto getBaseProduct() {
        return baseProduct;
    }

    /**
     * Sets the base product.
     * @param baseProductIn the new base product
     */
    public void setBaseProduct(SetupWizardProductDto baseProductIn) {
        baseProduct = baseProductIn;
    }

    /**
     * Gets the addon products.
     * @return the addon products
     */
    public List<SetupWizardProductDto> getAddonProducts() {
        return addonProducts;
    }

    /**
     * Set the product sync status.
     * @param syncStatusIn the status
     */
    public void setSyncStatus(SyncStatus syncStatusIn) {
        this.syncStatus = syncStatusIn;
    }

    /**
     * Get the product sync status.
     * @return the sync status
     */
    public SyncStatus getSyncStatus() {
        return this.syncStatus;
    }

    /**
     * Set the architecture.
     * @param archIn architecture
     */
    public void setArch(String archIn) {
        this.arch = archIn;
    }

    /**
     * Set the product identifier.
     * @param identIn identifier
     */
    public void setIdent(String identIn) {
        this.ident = identIn;
    }

    /**
     * Set the product name.
     * @param nameIn name
     */
    public void setName(String nameIn) {
        this.name = nameIn;
    }

    /**
     * Set the base product identification.
     * @param identIn identifier of base product
     */
    public void setBaseProductIdent(String identIn) {
        this.parentProduct = identIn;
    }

    /**
     * Convenience method to query the sync status
     * @return true if the product status is not mirrored
     * @see {@link com.redhat.rhn.frontend.dto.SetupWizardProductDto.SyncStatus.SyncStage}
     */
    public boolean isStatusNotMirrored() {
        return getSyncStatus().equals(SyncStatus.SyncStage.NOT_MIRRORED);
    }

    /**
     * Convenience method to set the sync status to not mirrored
     * @see {@link com.redhat.rhn.frontend.dto.SetupWizardProductDto.SyncStatus.SyncStage}
     */
    public void setStatusNotMirrored() {
        setSyncStatus(new SyncStatus(SyncStatus.SyncStage.NOT_MIRRORED));
    }

    /**
     * Convenience method to query the sync status
     * @return true if the product status is in progress
     * @see {@link com.redhat.rhn.frontend.dto.SetupWizardProductDto.SyncStatus.SyncStage}
     */
    public boolean isStatusInProgress() {
        return getSyncStatus().equals(SyncStatus.SyncStage.IN_PROGRESS);
    }

    /**
     * Convenience method to set the sync status to in progress
     * @see {@link com.redhat.rhn.frontend.dto.SetupWizardProductDto.SyncStatus.SyncStage}
     */
    public void setStatusInProgress() {
        setSyncStatus(new SyncStatus(SyncStatus.SyncStage.IN_PROGRESS));
    }

    /**
     * Convenience method to query the sync status
     * @return true if the product status is finished
     * @see {@link com.redhat.rhn.frontend.dto.SetupWizardProductDto.SyncStatus.SyncStage}
     */
    public boolean isStatusFinished() {
        return getSyncStatus().equals(SyncStatus.SyncStage.FINISHED);
    }

    /**
     * Convenience method to set the sync status to finished
     * @see {@link com.redhat.rhn.frontend.dto.SetupWizardProductDto.SyncStatus.SyncStage}
     */
    public void setStatusFinished() {
        setSyncStatus(new SyncStatus(SyncStatus.SyncStage.FINISHED));
    }

    /**
     * Convenience method to query the sync status
     * @return true if the product status is failed
     * @see {@link com.redhat.rhn.frontend.dto.SetupWizardProductDto.SyncStatus.SyncStage}
     */
    public boolean isStatusFailed() {
        return getSyncStatus().equals(SyncStatus.SyncStage.FAILED);
    }

    /**
     * Convenience method to set the sync status to failed
     * @see {@link com.redhat.rhn.frontend.dto.SetupWizardProductDto.SyncStatus.SyncStage}
     */
    public void setStatusFailed() {
        setSyncStatus(new SyncStatus(SyncStatus.SyncStage.FAILED));
    }
}
