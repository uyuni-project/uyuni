/**
 * Copyright (c) 2014 SUSE
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

import com.redhat.rhn.frontend.struts.Selectable;

import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.collections.Predicate;
import org.apache.commons.lang.builder.CompareToBuilder;
import org.apache.commons.lang.builder.EqualsBuilder;
import org.apache.commons.lang.builder.HashCodeBuilder;
import org.apache.commons.lang.builder.ToStringBuilder;
import org.simpleframework.xml.Attribute;
import org.simpleframework.xml.Element;
import org.simpleframework.xml.Root;

import java.util.List;

/**
 * A SUSE Product.
 */
@Root(strict = false)
public class Product implements Selectable, Comparable<Product> {

    /** The architecture. */
    @Attribute
    private String arch;

    /** The ident ID. */
    @Attribute
    private String ident;

    /** The product readable name. */
    @Attribute
    private String name;

    /** The ident ID of the base product or an empty string. */
    @Attribute
    private String parent_product;

    /** The mandatory channels. */
    @Element(name = "mandatory_channels")
    private MandatoryChannels mandatoryChannels;

    /** The optional channels. */
    @Element(name = "optional_channels")
    private OptionalChannels optionalChannels;

    /** True if this product has been selected in the GUI. */
    private boolean selected;

    /**
     * Default constructor.
     */
    public Product() {
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
    public Product(String archIn, String identIn, String nameIn, String baseProductIdent,
            MandatoryChannels mandatoryChannelsIn, OptionalChannels optionalChannelsIn) {
        arch = archIn;
        ident = identIn;
        name = nameIn;
        parent_product = baseProductIdent;
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
        return parent_product;
    }

    /**
     * Checks if the product is a base product.
     * @return true if the product is a base product, false otherwise
     */
    public boolean isBaseProduct() {
        return parent_product.isEmpty();
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
     * Returns true if this product has already been synchronized or it is
     * synchronizing at the moment.
     * @return true or false
     */
    public boolean isSynchronizing() {
        return CollectionUtils.exists(
            CollectionUtils.union(getMandatoryChannels(), getOptionalChannels()),
            new Predicate() {
                @Override
                public boolean evaluate(Object channel) {
                    return ((Channel) channel).isSynchronizing();
                }
            }
        );
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public int compareTo(Product other) {
        return new CompareToBuilder()
        .append(!this.isBaseProduct(), !other.isBaseProduct())
        .append(this.name, other.getName())
        .append(this.arch, other.getArch())
        .toComparison();
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
}
