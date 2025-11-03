/*
 * Copyright (c) 2012--2025 SUSE LLC
 *
 * This software is licensed to you under the GNU General Public License,
 * version 2 (GPLv2). There is NO WARRANTY for this software, express or
 * implied, including the implied warranties of MERCHANTABILITY or FITNESS
 * FOR A PARTICULAR PURPOSE. You should have received a copy of GPLv2
 * along with this software; if not, see
 * http://www.gnu.org/licenses/old-licenses/gpl-2.0.txt.
 */
package com.redhat.rhn.domain.product;

import com.redhat.rhn.domain.BaseDomainHelper;
import com.redhat.rhn.domain.channel.Channel;
import com.redhat.rhn.domain.channel.ChannelFamily;
import com.redhat.rhn.domain.rhnpackage.PackageArch;

import org.apache.commons.lang3.builder.EqualsBuilder;
import org.apache.commons.lang3.builder.HashCodeBuilder;
import org.apache.commons.lang3.builder.ToStringBuilder;
import org.hibernate.annotations.Type;

import java.io.Serializable;
import java.util.HashSet;
import java.util.Optional;
import java.util.Set;

import jakarta.persistence.CascadeType;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.JoinTable;
import jakarta.persistence.ManyToMany;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.OneToMany;
import jakarta.persistence.Table;

/**
 * POJO for a suseProducts row.
 */
@Entity
@Table(name = "suseProducts")
public class SUSEProduct extends BaseDomainHelper implements Serializable {

    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "suse_product_seq")
    @SequenceGenerator(name = "suse_product_seq", sequenceName = "SUSE_PRODUCTS_ID_SEQ", allocationSize = 1)
    private Long id;

    @Column(name = "name")
    private String name;

    @Column(name = "version")
    private String version;

    @Column(name = "release")
    private String release;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "arch_type_id")
    private PackageArch arch;

    @Column(name = "friendly_name")
    private String friendlyName;

    @Column(name = "description")
    private String description;

    @Column(name = "product_id")
    private long productId;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "channel_family_id")
    private ChannelFamily channelFamily;

    @Column(name = "base", nullable = false)
    @Type(type = "yes_no")
    private boolean base;

    @Column(name = "free", nullable = false)
    @Type(type = "yes_no")
    private boolean free;

    @Enumerated(EnumType.STRING)
    @Column(name = "release_stage")
    private ReleaseStage releaseStage;



    @ManyToMany
    @JoinTable(
            name = "suseUpgradePath",
            joinColumns = @JoinColumn(name = "from_pdid"),
            inverseJoinColumns = @JoinColumn(name = "to_pdid")
    )
    private Set<SUSEProduct> upgrades = new HashSet<>();

    @ManyToMany(mappedBy = "upgrades")
    private Set<SUSEProduct> downgrades = new HashSet<>();

    @OneToMany(mappedBy = "product", cascade = CascadeType.ALL, orphanRemoval = true)
    private Set<SUSEProductChannel> suseProductChannels = new HashSet<>();

    @OneToMany(mappedBy = "product", cascade = CascadeType.REMOVE, orphanRemoval = true)
    private Set<ChannelTemplate> channelTemplates = new HashSet<>();

    /**
     * Default constructor.
     */
    public SUSEProduct() {
        // Just create an empty object
    }

    /**
     * Create a product with the given name. Convenience constructor for unit testing.
     *
     * @param nameIn the product name
     */
    public SUSEProduct(String nameIn) {
        this.name = nameIn;
    }

    /**
     * Gets the id.
     * @return the id
     */
    public long getId() {
       return id;
    }

    /**
     * Sets the id.
     * @param idIn the new id
     */
    public void setId(long idIn) {
       id = idIn;
    }

    /**
     * Gets the name.
     * @return the name
     */
    public String getName() {
        return name;
    }

    /**
     * Sets the name.
     * @param nameIn the new name
     */
    public void setName(String nameIn) {
        name = nameIn;
    }

    /**
     * Gets the version.
     * @return the version
     */
    public String getVersion() {
        return version;
    }

    /**
     * Sets the version.
     * @param versionIn the new version
     */
    public void setVersion(String versionIn) {
        version = versionIn;
    }

    /**
     * Gets the release.
     * @return the release
     */
    public String getRelease() {
        return release;
    }

    /**
     * Sets the release.
     * @param releaseIn the new release
     */
    public void setRelease(String releaseIn) {
        release = releaseIn;
    }

    /**
     * Gets the arch.
     * @return the arch
     */
    public PackageArch getArch() {
        return arch;
    }

    /**
     * Sets the arch.
     * @param archIn the new arch
     */
    public void setArch(PackageArch archIn) {
        arch = archIn;
    }

    /**
     * Gets the friendly name.
     * @return the friendly name
     */
    public String getFriendlyName() {
        return friendlyName;
    }

    /**
     * Sets the friendly name.
     * @param friendlyNameIn the new friendly name
     */
    public void setFriendlyName(String friendlyNameIn) {
        friendlyName = friendlyNameIn;
    }

    /**
     * Gets the description.
     * @return the description
     */
    public String getDescription() {
        return description;
    }

    /**
     * Sets the descriptions.
     * @param descriptionIn the new descriptions
     */
    public void setDescription(String descriptionIn) {
        description = descriptionIn;
    }

    /**
     * Gets the product id.
     * @return the product id
     */
    public long getProductId() {
        return productId;
    }

    /**
     * Sets the product id.
     * @param productIdIn the new product id
     */
    public void setProductId(long productIdIn) {
        productId = productIdIn;
    }

    /**
     * @return the channelFamily
     */
    public ChannelFamily getChannelFamily() {
        return channelFamily;
    }

    /**
     * @param channelFamilyIn the channelFamily to set
     */
    public void setChannelFamily(ChannelFamily channelFamilyIn) {
        this.channelFamily = channelFamilyIn;
    }

    /**
     * @return the base
     */
    public boolean isBase() {
        return base;
    }

    /**
     * @param baseIn the base to set
     */
    public void setBase(boolean baseIn) {
        this.base = baseIn;
    }

    /**
     * Is the product free?
     * @return the state of the free flag
     */
    public boolean getFree() {
        return free;
    }

    /**
     * Sets the free flag.
     * @param freeIn - the free flag
     */
    public void setFree(boolean freeIn) {
        free = freeIn;
    }

    /**
     * The release stage of the product
     * @return alpha, beta, released
     */
    public ReleaseStage getReleaseStage() {
        return releaseStage;
    }

    /**
     * Set the release stage
     * @param releaseStageIn - alpha, beta, released
     */
    public void setReleaseStage(ReleaseStage releaseStageIn) {
        releaseStage = releaseStageIn;
    }

    /**
     * List available upgrade path for this product
     * @return list available upgrade path for this product
     */
    public Set<SUSEProduct> getUpgrades() {
        return upgrades;
    }

    /**
     * Set the list of available upgrade path for this product
     * @param upgradesIn the list of available upgrade path for this product
     */
    public void setUpgrades(Set<SUSEProduct> upgradesIn) {
        this.upgrades = upgradesIn;
    }

    /**
     * List products that can upgrade to this product
     * @return list products that can upgrade to this product
     */
    public Set<SUSEProduct> getDowngrades() {
        return downgrades;
    }

    /**
     * Sets the list of products that can upgrade to this product
     * @param downgradesIn list of products that can upgrade to this product
     */
    public void setDowngrades(Set<SUSEProduct> downgradesIn) {
        this.downgrades = downgradesIn;
    }

    /**
     * List product channels for this product
     * @return set of SUSEProductChannel
     */
    public Set<SUSEProductChannel> getSuseProductChannels() {
        return suseProductChannels;
    }

    /**
     * Set list of product Channels
     * @param suseProductChannelsIn set of product channels
     */
    public void setSuseProductChannels(Set<SUSEProductChannel> suseProductChannelsIn) {
        this.suseProductChannels = suseProductChannelsIn;
    }

    /**
     * @return Returns the channel templates provided by SCC.
     */
    public Set<ChannelTemplate> getChannelTemplates() {
        return channelTemplates;
    }

    /**
     * @param channelTemplatesIn The channel templates to set.
     */
    public void setChannelTemplates(Set<ChannelTemplate> channelTemplatesIn) {
        this.channelTemplates = channelTemplatesIn;
    }

    /**
     * @return the parent channel for this product
     */
    public Optional<Channel> parentChannel() {
        return getSuseProductChannels().stream()
                .map(SUSEProductChannel::getChannel)
                .filter(c -> c.getParentChannel() == null)
                .findFirst();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public boolean equals(Object otherObject) {
        if (!(otherObject instanceof SUSEProduct other)) {
            return false;
        }
        return new EqualsBuilder()
            .append(getName(), other.getName())
            .append(getVersion(), other.getVersion())
            .append(getRelease(), other.getRelease())
            .append(getArch(), other.getArch())
            .isEquals();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public int hashCode() {
        return new HashCodeBuilder()
            .append(getName())
            .append(getVersion())
            .append(getRelease())
            .append(getArch())
            .toHashCode();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public String toString() {
        return new ToStringBuilder(this)
                .append("id", getId())
                .append("name", getName())
                .append("version", getVersion())
                .append("release", getRelease())
                .append("arch", getArch().getLabel())
                .append("productId", getProductId())
                .append("friendlyName", getFriendlyName())
                .toString();
    }
}
