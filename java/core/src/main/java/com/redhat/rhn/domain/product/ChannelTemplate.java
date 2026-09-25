/*
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

import com.redhat.rhn.domain.BaseDomainHelper;
import com.redhat.rhn.domain.scc.SCCRepository;

import org.apache.commons.lang3.builder.EqualsBuilder;
import org.apache.commons.lang3.builder.HashCodeBuilder;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import jakarta.persistence.UniqueConstraint;

/**
 * ChannelTemplate - link Product with Repository and hold data for channels
 */
@Entity
@Table(name = "suseChannelTemplate", uniqueConstraints =
@UniqueConstraint(columnNames = {"product_id", "root_product_id", "repo_id"}))
public class ChannelTemplate extends BaseDomainHelper {


    @Id
    @Column(name = "id")
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "product_id", nullable = false)
    private SUSEProduct product;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "root_product_id", nullable = false)
    private SUSEProduct rootProduct;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "repo_id", nullable = false)
    private SCCRepository repository;

    @Column(name = "channel_label")
    private String channelLabel;

    @Column(name = "parent_channel_label")
    private String parentChannelLabel;

    @Column(name = "channel_name")
    private String channelName;

    @Column(name = "mandatory")
    private boolean mandatory;

    @Column(name = "update_tag")
    private String updateTag;

    @Column(name = "gpg_key_url")
    private String gpgKeyUrl;

    @Column(name = "gpg_key_id")
    private String gpgKeyId;

    @Column(name = "gpg_key_fp")
    private String gpgKeyFingerprint;

    /**
     * @return Returns the id.
     */
    public Long getId() {
        return id;
    }

    /**
     * @return Returns the product.
     */
    public SUSEProduct getProduct() {
        return product;
    }

    /**
     * @return Returns the root product.
     */
    public SUSEProduct getRootProduct() {
        return rootProduct;
    }

    /**
     * @return Returns the repoId.
     */
    public SCCRepository getRepository() {
        return repository;
    }

    /**
     * @return Returns the channelLabel.
     */
    public String getChannelLabel() {
        return channelLabel;
    }

    /**
     * @return Returns the parentChannelLabel.
     */
    public String getParentChannelLabel() {
        return parentChannelLabel;
    }

    /**
     * @return Returns the channelName.
     */
    public String getChannelName() {
        return channelName;
    }

    /**
     * @return Returns the mandatory.
     */
    public boolean isMandatory() {
        return mandatory;
    }

    /**
     * @return Returns the updateTag.
     */
    public String getUpdateTag() {
        return updateTag;
    }

    /**
     * @return Returns the GPG key URL
     */
    public String getGpgKeyUrl() {
        return gpgKeyUrl;
    }

    /**
     * @return Returns the GPG key id
     */
    public String getGpgKeyId() {
        return gpgKeyId;
    }

    /**
     * @return Returns the GPG Key Fingerprint
     */
    public String getGpgKeyFingerprint() {
        return gpgKeyFingerprint;
    }

    /**
     * @param idIn The id to set.
     */
    public void setId(Long idIn) {
        this.id = idIn;
    }

    /**
     * @param productIn The product to set.
     */
    public void setProduct(SUSEProduct productIn) {
        this.product = productIn;
    }

    /**
     * @param productIn The root product to set.
     */
    public void setRootProduct(SUSEProduct productIn) {
        this.rootProduct = productIn;
    }

    /**
     * @param repoIn The repoId to set.
     */
    public void setRepository(SCCRepository repoIn) {
        this.repository = repoIn;
    }

    /**
     * @param channelLabelIn The channelLabel to set.
     */
    public void setChannelLabel(String channelLabelIn) {
        this.channelLabel = channelLabelIn;
    }

    /**
     * @param parentChannelLabelIn The parentChannelLabel to set.
     */
    public void setParentChannelLabel(String parentChannelLabelIn) {
        this.parentChannelLabel = parentChannelLabelIn;
    }

    /**
     * @param channelNameIn The channelName to set.
     */
    public void setChannelName(String channelNameIn) {
        this.channelName = channelNameIn;
    }

    /**
     * @param mandatoryIn The mandatory to set.
     */
    public void setMandatory(boolean mandatoryIn) {
        this.mandatory = mandatoryIn;
    }

    /**
     * @param updateTagIn The updateTag to set.
     */
    public void setUpdateTag(String updateTagIn) {
        this.updateTag = updateTagIn;
    }

    /**
     * @return true of this item represents a root item
     */
    public boolean isRoot() {
        return getParentChannelLabel() == null;
    }

    /**
     * @param gpgKeyUrlIn The GPG Key Url
     */
    public void setGpgKeyUrl(String gpgKeyUrlIn) {
        gpgKeyUrl = gpgKeyUrlIn;
    }

    /**
     * @param gpgKeyIdIn the GPG Key ID
     */
    public void setGpgKeyId(String gpgKeyIdIn) {
        gpgKeyId = gpgKeyIdIn;
    }

    /**
     * @param gpgKeyFingerprintIn the GPG Key Fingerprint
     */
    public void setGpgKeyFingerprint(String gpgKeyFingerprintIn) {
        gpgKeyFingerprint = gpgKeyFingerprintIn;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public boolean equals(Object other) {
        if (!(other instanceof ChannelTemplate)) {
            return false;
        }
        ChannelTemplate otherCast = (ChannelTemplate) other;
        return new EqualsBuilder()
                .append(getChannelLabel(), otherCast.getChannelLabel())
                .append(getProduct(), otherCast.getProduct())
                .append(getRootProduct(), otherCast.getRootProduct())
                .append(getRepository(), otherCast.getRepository())
                .append(getChannelName(), otherCast.getChannelName())
                .append(isMandatory(), otherCast.isMandatory())
                .append(getUpdateTag(), otherCast.getUpdateTag())
                .append(getGpgKeyUrl(), otherCast.getGpgKeyUrl())
                .isEquals();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public int hashCode() {
        return new HashCodeBuilder()
                .append(getChannelLabel())
                .append(getProduct())
                .append(getRootProduct())
                .append(getRepository())
                .toHashCode();
    }

    @Override
    public String toString() {
        return "(ProductId: " + getProduct().getProductId() +
                ", RootProductId: " + getRootProduct().getProductId() +
                ", RepositoryId: " + getRepository().getSccId() +
                ", Label: " + getChannelLabel() +
                ", Parent: " + getParentChannelLabel() + ")";
    }
}
