/**
 * Copyright (c) 2013 SUSE LLC
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

import org.apache.commons.lang3.builder.EqualsBuilder;
import org.apache.commons.lang3.builder.HashCodeBuilder;
import org.apache.commons.lang3.builder.ToStringBuilder;

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

    /**
     * Instantiates a new Product.
     * @param idIn the id in
     * @param labelIn the label in
     * @param recommendedIn the recommended flag in
     */
    public Product(Long idIn, String identifierIn, String labelIn, String archIn, Boolean recommendedIn) {
        id = idIn;
        label = labelIn;
        arch = archIn;
        identifier = identifierIn;
        recommended = recommendedIn;
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

    public String getArch() {
        return arch;
    }

    public String getIdentifier() {
        return identifier;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public boolean equals(Object other) {
        if (!(other instanceof Product)) {
            return false;
        }
        Product otherProduct= (Product) other;
        return new EqualsBuilder()
            .append(getId(), otherProduct.getId())
            .append(getLabel(), otherProduct.getLabel())
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
        .toString();
    }
}
