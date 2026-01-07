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

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.Table;
import javax.persistence.Transient;
import javax.persistence.UniqueConstraint;

/**
 * ChannelTemplate - link Product with Repository and hold data for channels
 */
@Entity
@Table(name = "suseChannelTemplate", uniqueConstraints =
@UniqueConstraint(columnNames = {"product_id", "root_product_id", "repo_id"}))
public class ChannelTemplate extends BaseDomainHelper {

    private Long id;
    private SUSEProduct product;
    private SUSEProduct rootProduct;
    private SCCRepository repository;
    private String channelLabel;
    private String parentChannelLabel;
    private String channelName;
    private boolean mandatory;
    private String updateTag;
    private String gpgKeyUrl;
    private String gpgKeyId;
    private String gpgKeyFingerprint;

    /**
     * @return Returns the id.
     */
    @Id
    @Column(name = "id")
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    public Long getId() {
        return id;
    }

    /**
     * @return Returns the product.
     */
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "product_id", nullable = false)
    public SUSEProduct getProduct() {
        return product;
    }

    /**
     * @return Returns the root product.
     */
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "root_product_id", nullable = false)
    public SUSEProduct getRootProduct() {
        return rootProduct;
    }

    /**
     * @return Returns the repoId.
     */
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "repo_id", nullable = false)
    public SCCRepository getRepository() {
        return repository;
    }

    /**
     * @return Returns the channelLabel.
     */
    @Column(name = "channel_label")
    public String getChannelLabel() {
        return channelLabel;
    }

    /**
     * @return Returns the parentChannelLabel.
     */
    @Column(name = "parent_channel_label")
    public String getParentChannelLabel() {
        return parentChannelLabel;
    }

    /**
     * @return Returns the channelName.
     */
    @Column(name = "channel_name")
    public String getChannelName() {
        return channelName;
    }

    /**
     * @return Returns the mandatory.
     */
    @Column(name = "mandatory")
    public boolean isMandatory() {
        return mandatory;
    }

    /**
     * @return Returns the updateTag.
     */
    @Column(name = "update_tag")
    public String getUpdateTag() {
        return updateTag;
    }

    /**
     * @return Returns the GPG key URL
     */
    @Column(name = "gpg_key_url")
    public String getGpgKeyUrl() {
        return gpgKeyUrl;
    }

    /**
     * @return Returns the GPG key id
     */
    @Column(name = "gpg_key_id")
    public String getGpgKeyId() {
        return gpgKeyId;
    }

    /**
     * @return Returns the GPG Key Fingerprint
     */
    @Column(name = "gpg_key_fp")
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
    @Transient
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
