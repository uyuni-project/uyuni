/**
 * Copyright (c) 2012 SUSE LLC
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
import com.redhat.rhn.domain.channel.Channel;
import com.redhat.rhn.domain.channel.ChannelFamily;
import com.redhat.rhn.domain.rhnpackage.PackageArch;

import org.apache.commons.lang3.builder.EqualsBuilder;
import org.apache.commons.lang3.builder.HashCodeBuilder;

import java.io.Serializable;
import java.util.HashSet;
import java.util.Optional;
import java.util.Set;

/**
 * POJO for a suseProducts row.
 */
public class SUSEProduct extends BaseDomainHelper implements Serializable {

    /** The id. */
    private long id;

    /** The name. */
    private String name;

    /** The version. */
    private String version;

    /** The release. */
    private String release;

    /** The arch. */
    private PackageArch arch;

    /** The friendly name. */
    private String friendlyName;

    /** The description */
    private String description;

    /** The product id. */
    private long productId;

    /** The channel family */
    private ChannelFamily channelFamily;

    /** True if the product is a base product */
    private boolean base;

    /** True if the product is 'free' */
    private boolean free;

    /** The release stage */
    private ReleaseStage releaseStage;

    /** available upgrades for this product; */
    private Set<SUSEProduct> upgrades = new HashSet<>();

    /** available products from which upgrade to this is possible */
    private Set<SUSEProduct> downgrades = new HashSet<>();

    /** product channels */
    private Set<SUSEProductChannel> suseProductChannels = new HashSet<>();

    /** repositories */
    private Set<SUSEProductSCCRepository> repositories = new HashSet<>();

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
     * @return Returns the repositories provided by SCC.
     */
    public Set<SUSEProductSCCRepository> getRepositories() {
        return repositories;
    }

    /**
     * @param repositoriesIn The repositories to set.
     */
    public void setRepositories(Set<SUSEProductSCCRepository> repositoriesIn) {
        this.repositories = repositoriesIn;
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
        if (!(otherObject instanceof SUSEProduct)) {
            return false;
        }
        SUSEProduct other = (SUSEProduct) otherObject;
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
}
