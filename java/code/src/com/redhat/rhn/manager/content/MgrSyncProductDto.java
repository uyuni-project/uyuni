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
package com.redhat.rhn.manager.content;

import com.suse.mgrsync.MgrSyncStatus;
import com.suse.mgrsync.XMLChannel;

import org.apache.commons.lang3.builder.CompareToBuilder;
import org.apache.commons.lang3.builder.EqualsBuilder;
import org.apache.commons.lang3.builder.HashCodeBuilder;
import org.apache.commons.lang3.builder.ToStringBuilder;
import org.apache.commons.lang3.builder.ToStringStyle;

import java.util.HashSet;
import java.util.Set;
import java.util.SortedSet;
import java.util.TreeSet;

/**
 * A product, as listed by mgr-sync. This is conceptually different from:
 * {@link SCCProduct}, a deserialized version of how SCC represents a product,
 * {@link XMLProduct}, a deserialized version of how channels.xml encodes a product,
 * {@link SUSEProduct}, which is a deserialization of what we have in DB table suseProducts,
 * {@link SetupWizardProductDto}, which is how the WebUI represents a product.
 */
public class MgrSyncProductDto implements Comparable<MgrSyncProductDto> {

    /** The friendly name. */
    private String friendlyName;

    /** The id. */
    private Long id;

    /** The version. */
    private String version;

    /** The base channel for this product. */
    private XMLChannel baseChannel;

    /** The channels that make up this product. */
    private Set<XMLChannel> channels;

    /** The extensions products of this product. */
    private SortedSet<MgrSyncProductDto> extensions;

    /** Is this extension recommended */
    private boolean recommended;

    /**
     * Instantiates a new listed product.
     *
     * @param friendlyNameIn the friendly name
     * @param idIn the id
     * @param versionIn the version
     * @param baseChannelIn the base channel
     */
    public MgrSyncProductDto(String friendlyNameIn, Long idIn, String versionIn,
            XMLChannel baseChannelIn) {
        friendlyName = friendlyNameIn;
        id = idIn;
        version = versionIn;
        baseChannel = baseChannelIn;
        channels = new HashSet<XMLChannel>();
        extensions = new TreeSet<MgrSyncProductDto>();
        recommended = false;
    }

    /**
     * Is this a recommended extension
     * @return true in case this is a  recommended extension, otherwise false
     */
    public boolean isRecommended() {
        return recommended;
    }

    /**
     * Set as recommended extension
     * @param recommendedIn is recommended
     */
    public void setRecommended(boolean recommendedIn) {
        this.recommended = recommendedIn;
    }

    /**
     * Gets the friendly name.
     * @return the friendly name
     */
    public String getFriendlyName() {
        return friendlyName;
    }

    /**
     * Gets the id.
     * @return the id
     */
    public Long getId() {
        return id;
    }

    /**
     * Gets the version.
     * @return the version
     */
    public String getVersion() {
        return version;
    }

    /**
     * Gets the base channel.
     *
     * @return the base channel
     */
    public XMLChannel getBaseChannel() {
        return baseChannel;
    }

    /**
     * Gets the channels that make up this product.
     *
     * @return the channels
     */
    public Set<XMLChannel> getChannels() {
        return channels;
    }

    /**
     * Adds a channel.
     *
     * @param channel the channel
     */
    public void addChannel(XMLChannel channel) {
        channels.add(channel);
    }

    /**
     * Gets the extensions.
     * @return the extensions
     */
    public SortedSet<MgrSyncProductDto> getExtensions() {
        return extensions;
    }

    /**
     * Adds an extension to this product.
     *
     * @param extension the extension product
     */
    public void addExtension(MgrSyncProductDto extension) {
        extensions.add(extension);
    }

    /**
     * Gets the arch.
     *
     * @return the arch
     */
    public String getArch() {
        return getBaseChannel().getArch();
    }

    /**
     * Gets the status.
     *
     * A product is installed iff all mandatory channels are installed,
     * otherwise it is available.
     *
     * A product is unavailable iff at least one mandatory channel is
     * unavailable.
     * @return the status
     */
    public MgrSyncStatus getStatus() {
        MgrSyncStatus result = MgrSyncStatus.INSTALLED;
        for (XMLChannel channel : channels) {
            if (!channel.isOptional()) {
                if (channel.getStatus() == MgrSyncStatus.UNAVAILABLE) {
                    return MgrSyncStatus.UNAVAILABLE;
                }
                if (channel.getStatus() != MgrSyncStatus.INSTALLED) {
                    result = MgrSyncStatus.AVAILABLE;
                }
            }
        }
        return result;
    }

    /**
     * This returns an artificial identifier that was previously called "ident" and had been
     * used for triggering sync of a product with NCC via the CLI as well as the GUI. There
     * is no need for this ident anymore if we modify the GUI implementation to send channel
     * labels instead of those idents.
     *
     * @return ident
     */
    public String getIdent() {
        return id + "-" + getBaseChannel().getLabel();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public int compareTo(MgrSyncProductDto other) {
        return new CompareToBuilder()
                .append(!isRecommended(), !other.isRecommended())
                .append(getNormalizedName(), other.getNormalizedName())
                .append(getVersion(), other.getVersion())
                .append(getArch(), other.getArch())
                .append(getId(), other.getId())
                .append(getBaseChannel().getLabel(), other.getBaseChannel().getLabel())
                .toComparison();
    }

    /**
     * Gets the normalized name for sorting.
     *
     * @return the normalized name
     */
    private String getNormalizedName() {
        return getFriendlyName().toLowerCase().replace(' ', '-');
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public boolean equals(Object other) {
        if (!(other instanceof MgrSyncProductDto)) {
            return false;
        }
        MgrSyncProductDto otherProduct = (MgrSyncProductDto) other;
        return new EqualsBuilder()
                .append(getNormalizedName(), otherProduct.getNormalizedName())
                .append(getVersion(), otherProduct.getVersion())
                .append(getId(), otherProduct.getId())
                .append(getBaseChannel().getLabel(),
                        otherProduct.getBaseChannel().getLabel())
                 .isEquals();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public int hashCode() {
        return new HashCodeBuilder()
            .append(getId())
            .append(getBaseChannel().getLabel())
            .hashCode();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public String toString() {
        return new ToStringBuilder(this, ToStringStyle.SHORT_PREFIX_STYLE)
            .append("name", friendlyName)
            .append("id", id)
            .append("version", version)
            .append("baseChannel", baseChannel.getLabel())
            .toString();
    }
}
