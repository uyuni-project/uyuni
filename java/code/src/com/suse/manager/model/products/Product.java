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
import org.apache.tools.ant.taskdefs.condition.IsReachable;
import org.simpleframework.xml.Attribute;

/**
 * A Product.
 */
public class Product {

    /** The id. */
    @Attribute
    private Long id;

    /** The label. */
    @Attribute
    private String label;
    
    /** The recommended flag. */
    @Attribute
    private boolean recommended;
    

    /**
     * Default constructor.
     */
    public Product() { }

    /**
     * Instantiates a new Product.
     * @param idIn the id in
     * @param labelIn the label in
     * @param recommendedIn the recommended flag in
     */
    public Product(Long idIn, String labelIn, Boolean recommendedIn) {
        id = idIn;
        label = labelIn;
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
     * Set the id
     * @param idIn id
     */
    public void setId(Long idIn) {
        this.id = idIn;
    }

    /**
     * Gets the label.
     * @return the label
     */
    public String getLabel() {
        return label;
    }

    /**
     * Set the product label
     * @param labelIn label
     */
    public void setLabel(String labelIn) {
        this.label = labelIn;
    }

    /**
     * Gets the recommended flag.
     * @return the recommended
     */
    public Boolean isRecommended() {
        return recommended;
    }

    /**
     * Set the product recommended flag
     * @param recommendedIn recommended
     */
    public void setRecommended(Boolean recommendedIn) {
        this.recommended = recommendedIn;
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
