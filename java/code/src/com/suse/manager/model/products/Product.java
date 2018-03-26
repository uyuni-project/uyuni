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

package com.suse.manager.model.products;

import com.suse.mgrsync.MgrSyncStatus;
import org.apache.commons.lang3.builder.EqualsBuilder;
import org.apache.commons.lang3.builder.HashCodeBuilder;
import org.apache.commons.lang3.builder.ToStringBuilder;

import java.util.Set;

/**
 * A Product.
 */
public class Product {

    /** The id. */
    private final Long id;

    /** The label. */
    private final String label;

    /** The recommended flag. */
    private final boolean recommended;

    private final String arch;

    private final String identifier;

    private final Set<Extension> extensions;

    private final Set<JsonChannel> channels;

    private final MgrSyncStatus status;

    /**
     * Instantiates a new Product.
     * @param idIn the id in
     * @param identifierIn the unique identifier of the Product
     * @param labelIn the label in
     * @param archIn the architecture in
     * @param recommendedIn the recommended flag in
     * @param statusIn the {@link MgrSyncStatus} in
     * @param extensionsIn the set of extensions
     * @param channelsIn the set of channels in
     */
    public Product(Long idIn, String identifierIn,
                   String labelIn, String archIn, Boolean recommendedIn,
                   MgrSyncStatus statusIn, Set<Extension> extensionsIn,
                   Set<JsonChannel> channelsIn) {
        id = idIn;
        label = labelIn;
        arch = archIn;
        identifier = identifierIn;
        recommended = recommendedIn;
        extensions = extensionsIn;
        channels = channelsIn;
        status = statusIn;
    }

    /**
     * Gets the id.
     * @return the id
     */
    public Long getId() {
        return id;
    }

    /**
     * Gets the label.
     * @return the label
     */
    public String getLabel() {
        return label;
    }

    /**
     * Gets the recommended flag.
     * @return the recommended
     */
    public Boolean isRecommended() {
        return recommended;
    }

    /**
     * Gets the architecture value.
     * @return the architecture
     */
    public String getArch() {
        return arch;
    }

    /**
     * Gets the identifiervalue.
     * @return the identifier
     */
    public String getIdentifier() {
        return identifier;
    }

    /**
     * Gets the extensions set.
     * @return the extensions set
     */
    public Set<Extension> getExtensions() {
        return extensions;
    }

    /**
     * Gets the channels set.
     * @return the channels set
     */
    public Set<JsonChannel> getChannels() {
        return channels;
    }

    /**
     * Gets the status value.
     * @return the {@link MgrSyncStatus} value
     */
    public MgrSyncStatus getStatus() {
        return status;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public boolean equals(Object other) {
        if (!(other instanceof Product)) {
            return false;
        }
        Product otherProduct = (Product) other;
        return new EqualsBuilder()
                .append(getId(), otherProduct.getId())
                .append(getLabel(), otherProduct.getLabel())
                .append(getArch(), otherProduct.getArch())
                .append(getIdentifier(), otherProduct.getIdentifier())
                .append(isRecommended(), otherProduct.isRecommended())
                .isEquals();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public int hashCode() {
        return new HashCodeBuilder()
                .append(getId())
                .append(getLabel())
                .append(getArch())
                .append(getIdentifier())
                .append(isRecommended())
                .toHashCode();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public String toString() {
        return new ToStringBuilder(this)
                .append("id", getId())
                .append("label", getLabel())
                .append("arch", getArch())
                .append("identifier", getIdentifier())
                .append("isRecommended", isRecommended())
                .toString();
    }
}
